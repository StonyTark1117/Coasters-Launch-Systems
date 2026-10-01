package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.math.LinearMotor;
import dev.coasterslaunchsystems.math.Numbers;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.fml.ModList;
import org.joml.Vector3d;

/** Collect first, arbitrate each consist, then queue one bounded physical interface drive. */
public final class EquipmentRuntime {
  private record Entry(EquipmentCabinetEntity module, long tick) {}
  private record Key(BlockPos module, UUID cart) {}
  private record History(long step, double distance, double acceleration) {}
  private record DriveKey(Object owner, UUID cart) {}
  private record Clamp(UUID identity, NativeFinClamp handle) {}
  private static final double FIN_FORCE = 256, FIN_MASS = 64, EDDY_FORCE = 128;
  private static final Map<ServerLevel, Map<Key, Clamp>> CLAMPS = new HashMap<>();
  private record Request(EquipmentCabinetEntity module, ServerSubLevel cart,
      double mass, double energyMass, double speed, Vector3d direction, Vector3d point, double force, Key key, double distance) {}
  private static final Map<ServerLevel, Map<BlockPos, Entry>> MODULES = new HashMap<>();
  private static final Map<ServerLevel, Map<Key, History>> HISTORY = new HashMap<>();
  private static final Map<ServerLevel, Map<DriveKey, History>> DRIVE_HISTORY = new HashMap<>();
  private static final Map<ServerLevel, Long> STEPS = new HashMap<>();
  private EquipmentRuntime() {}

  public static void register(ServerLevel level, EquipmentCabinetEntity module) {
    MODULES.computeIfAbsent(level, ignored -> new LinkedHashMap<>()).put(module.getBlockPos(), new Entry(module, level.getGameTime()));
  }
  public static boolean available(ServerLevel level, EquipmentCabinetEntity module) {
    var entry = MODULES.getOrDefault(level, Map.of()).get(module.getBlockPos());
    return entry != null && entry.module() == module && !module.isRemoved() && entry.tick() >= level.getGameTime() - 1;
  }
  public static boolean foreignRouteEquipment(ServerLevel level, dev.coasterslaunchsystems.content.RideControllerEntity source,
      dev.coasterslaunchsystems.content.RideControllerEntity target, dev.coasterslaunchsystems.track.DirectedRoute route) {
    return foreignRouteEquipment(level, source, target, route, List.of());
  }
  static boolean foreignRouteEquipment(ServerLevel level, dev.coasterslaunchsystems.content.RideControllerEntity source,
      dev.coasterslaunchsystems.content.RideControllerEntity target, dev.coasterslaunchsystems.track.DirectedRoute route,
      List<dev.coasterslaunchsystems.content.RideControllerEntity.Module> inspectedOrphans) {
    for (var entry : List.copyOf(MODULES.getOrDefault(level, Map.of()).values())) {
      var module = entry.module();
      if (!available(level, module) || !module.armed() || !module.fault().isEmpty()) continue;
      var track = module.resolve(level);
      if (track != null && route.contains(track.edge()) && ownerOutsidePair(level, module, source, target)
          && inspectedOrphans.stream().noneMatch(m -> m.position().equals(module.getBlockPos()) && m.identity().equals(module.identity()))) return true;
    }
    return false;
  }
  private static boolean ownerOutsidePair(ServerLevel level, EquipmentCabinetEntity module,
      dev.coasterslaunchsystems.content.RideControllerEntity source, dev.coasterslaunchsystems.content.RideControllerEntity target) {
    var owner = RideControllers.owner(level, module); return owner != source && owner != target;
  }
  /** Read-only native contact diagnostics; no saved or caller-owned constraint handles. */
  public static int activeFinClamps(ServerLevel level, UUID chassis) {
    var clamps = CLAMPS.get(level);
    return clamps == null ? 0 : (int) clamps.entrySet().stream()
        .filter(e -> e.getKey().cart().equals(chassis) && e.getValue().handle().valid()).count();
  }
  public static void unload(LevelEvent.Unload event) {
    if (event.getLevel() instanceof ServerLevel level) {
      MODULES.remove(level); HISTORY.remove(level); DRIVE_HISTORY.remove(level); STEPS.remove(level);
      // Sable world teardown owns all native joints. Avoid JNI calls after another unload listener disposes it.
      CLAMPS.remove(level);
    }
  }
  public static void physics(ForgeSablePrePhysicsTickEvent event) {
    var level = event.getPhysicsSystem().getLevel();
    var registered = MODULES.get(level);
    CableRecovery.stabilize(level, registered == null ? List.of() : registered.values().stream()
        .filter(entry -> entry.tick() >= level.getGameTime() - 1).map(Entry::module).toList());
    RideControllers.physics(level, event.getTimeStep());
    var modules = MODULES.get(level);
    if (modules == null) {
      var old = CLAMPS.remove(level);
      if (old != null) old.values().forEach(c -> c.handle().close());
      return;
    }
    modules.values().removeIf(e -> e.module().isRemoved() || e.tick() < level.getGameTime() - 1);
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    var history = HISTORY.computeIfAbsent(level, ignored -> new HashMap<>());
    var driveHistory = DRIVE_HISTORY.computeIfAbsent(level, ignored -> new HashMap<>());
    long step = STEPS.merge(level, 1L, Long::sum);
    double dt = event.getTimeStep();
    var groups = new LinkedHashMap<UUID, List<Request>>();
    for (var entry : List.copyOf(modules.values())) {
      var module = entry.module();
      module.cool(dt);
      module.chargeLinear(dt);
      module.chargeFlywheel(dt);
      if (module.mechanism() == Mechanism.FLYWHEEL) continue;
      if (!module.armed() || !module.fault().isEmpty()) continue;
      if (ModList.get().isLoaded("coastersengineered") || ModList.get().isLoaded("coasterfins")) {
        module.fail("An installed drive addon needs an ownership adapter"); continue;
      }
      var track = module.resolve(level);
      if (track == null) continue;
      if (!module.mechanism().drive() && module.mechanism() != Mechanism.FIN_BRAKE && module.mechanism() != Mechanism.EDDY_BRAKE) {
        module.fail("Matching brake reaction hardware is required"); continue;
      }
      var mechanism = module.mechanism();
      if (mechanism == Mechanism.TIRE && (Math.abs(module.getSpeed()) < 1 || Math.abs(module.getSpeed()) > 128)) continue;
      if (mechanism.linear() && !module.linearPowered()) continue;
      // Spring applied, shaft released. Loss of shaft power applies an armed, supported clamp.
      if (mechanism == Mechanism.FIN_BRAKE && RideControllers.finReleased(level, module)) continue;
      for (var cart : container.getAllSubLevels()) {
        var contact = EngagedContacts.current(level, cart).orElse(null);
        if (contact == null || !track.matches(contact.hit())) continue;
        var key = new Key(module.getBlockPos(), cart.getUniqueId());
        double distance = track.distance(contact.hit());
        var previous = history.get(key);
        double oldDistance = previous != null && previous.step() == step - 1 ? previous.distance() : distance;
        double oldAcceleration = previous != null && previous.step() == step - 1 ? previous.acceleration() : 0;
        history.put(key, new History(step, distance, 0));
        if (!module.interval().intersects(oldDistance, distance, .6)) continue;
        try {
          var motion = MotionProviders.resolve(level, cart.getUniqueId()).orElse(null);
          if (motion == null || !permitted(motion, mechanism)
              || mechanism.drive() && !RideControllers.permitsDrive(level, module, cart.getUniqueId())) continue;
          if (!motion.interfaces().contains(mechanism)) {
            module.fail(switch (mechanism) {
              case TIRE -> "Vehicle has no certified tire interface";
              case FIN_BRAKE -> "Vehicle has no certified friction fin";
              case LSM, LIM -> "Vehicle has no certified " + mechanism + " reaction plate";
              default -> "Vehicle has no certified conductive fin";
            }); continue;
          }
          boolean available = true; double consistMass = 0;
          var bodyIds = new HashSet<UUID>();
          for (var member : motion.consist()) {
            var linked = container.getSubLevel(member);
            var accepted = linked instanceof ServerSubLevel server ? EngagedContacts.current(level, server).orElse(null) : null;
            if (accepted == null) { available = false; break; }
            var linkedMotion = MotionProviders.resolve(level, member).orElse(null);
            if (linkedMotion == null || !permitted(linkedMotion, mechanism)
                || !linkedMotion.interfaces().contains(mechanism) || !linkedMotion.provider().equals(motion.provider())
                || !Set.copyOf(linkedMotion.consist()).equals(Set.copyOf(motion.consist()))) {
              available = false; break;
            }
            double memberMass = NativeForces.mass(level, linkedMotion);
            if (memberMass <= 0) { available = false; break; }
            for (var body : linkedMotion.bodies()) if (!bodyIds.add(body)) { available = false; break; }
            if (!available) { module.fail("Duplicate native body in equipment consist"); break; }
            consistMass += memberMass;
            if (ChainLiftSpanQuery.findAtGraphHit(level, accepted.hit(), 0) != null) {
              module.fail("Native chain drive owns part of this consist"); available = false; break;
            }
          }
          if (!available) continue;
          var point = switch (mechanism) {
            case TIRE -> VehicleInterfaces.tirePoint(cart);
            case FIN_BRAKE -> VehicleInterfaces.brakePoint(cart);
            case LSM, LIM -> VehicleInterfaces.linearPoint(cart, mechanism);
            default -> VehicleInterfaces.conductivePoint(cart);
          };
          if (point == null) { module.fail("Chassis interface hardware was removed"); continue; }
          double mass = NativeForces.mass(level, motion);
          if (mass <= 0) continue;
          var axis = track.forward(level, contact.hit());
          var direction = new Vector3d(axis.x, axis.y, axis.z);
          var dynamics = NativeForces.pointDynamics(cart, event.getPhysicsSystem().getPipeline(), point, direction);
          double speed = dynamics.speed();
          if (mechanism == Mechanism.FIN_BRAKE) {
            double gravity = Math.abs(dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(level).dot(direction));
            if (consistMass > FIN_MASS || gravity * consistMass > .8 * Math.min(FIN_FORCE, motion.maximumBrakeForce())) {
              module.fail("Loaded consist exceeds the fin clamp mass/slope rating"); continue;
            }
            UUID consist = motion.consist().stream().min(UUID::compareTo).orElseThrow();
            groups.computeIfAbsent(consist, ignored -> new ArrayList<>()).add(new Request(module, cart, mass,
                dynamics.effectiveMass(), speed, direction, point, Math.min(FIN_FORCE, motion.maximumBrakeForce()), key, distance));
            continue;
          }
          if (mechanism == Mechanism.EDDY_BRAKE) {
            // Half the free-body no-reversal impulse keeps the discrete curve asymptotic near rest.
            double limit = Math.min(Math.min(EDDY_FORCE, motion.maximumBrakeForce()), .5 * dynamics.effectiveMass() * Math.abs(speed) / dt);
            double force = Numbers.clamp(DriveMath.eddyForce(speed, 64, 4) * module.thermalFraction(), -limit, limit);
            UUID consist = motion.consist().stream().min(UUID::compareTo).orElseThrow();
            groups.computeIfAbsent(consist, ignored -> new ArrayList<>()).add(new Request(module, cart, mass,
                dynamics.effectiveMass(), speed, direction, point, force, key, distance));
            continue;
          }
          double target = mechanism.linear() ? module.targetSpeed() : DriveMath.wheelSpeed(module.getSpeed(), .4);
          double requested = mechanism.linear() ? LinearMotor.force(mechanism, speed, target, 20) * module.thermalFraction()
              : DriveMath.tireForce(speed, target, 20, 8, .4);
          double authority = mechanism.linear() && requested * speed < 0 ? motion.maximumBrakeForce() : motion.maximumForce();
          double limit = Math.min(Math.min(authority, mass * Math.min(motion.maximumAcceleration(), mechanism.linear() ? module.accelerationLimit() : 2)),
              dynamics.effectiveMass() * Math.abs(target - speed) / dt);
          requested = Numbers.clamp(requested, -limit, limit);
          double acceleration = mechanism.linear() ? requested / mass : DriveMath.tireAccelerationStep(oldAcceleration, requested / mass, 4, dt);
          var request = new Request(module, cart, mass, dynamics.effectiveMass(), speed,
              direction, point, acceleration * mass, key, distance);
          UUID consist = motion.consist().stream().min(UUID::compareTo).orElseThrow();
          groups.computeIfAbsent(consist, ignored -> new ArrayList<>()).add(request);
        } catch (IllegalArgumentException | IllegalStateException failure) { module.fail(failure.getMessage()); }
      }
    }
    var allocated = new LinkedHashMap<EquipmentCabinetEntity, List<Request>>();
    for (var requests : groups.values()) {
      var owners = requests.stream().map(Request::module).distinct().toList();
      if (owners.stream().map(m -> RideControllers.ownership(level, m)).distinct().count() != 1) {
        owners.forEach(m -> m.fail("Overlapping modules claim this consist; separate their contact zones")); continue;
      }
      for (var request : requests) allocated.computeIfAbsent(request.module(), ignored -> new ArrayList<>()).add(request);
    }
    // Coordinated adjacent drives still share the vehicle's force/acceleration rating.
    var tireDemand = new HashMap<UUID, Double>();
    var tireLimits = new HashMap<UUID, Double>();
    for (var allocation : allocated.entrySet()) {
      if (!allocation.getKey().mechanism().drive()) continue;
      for (var request : allocation.getValue()) {
        UUID id = request.cart().getUniqueId(); tireDemand.merge(id, Math.abs(request.force()), Double::sum);
        var motion = MotionProviders.resolve(level, id).orElseThrow();
        double authority = allocation.getKey().mechanism().linear() && request.force() * request.speed() < 0 ? motion.maximumBrakeForce() : motion.maximumForce();
        tireLimits.merge(id, Math.min(authority, request.mass() * Math.min(motion.maximumAcceleration(), allocation.getKey().mechanism().linear() ? allocation.getKey().accelerationLimit() : 2)), Math::min);
      }
    }
    // A bank's configured linear jerk limit belongs to the chassis, not to every duplicated pad.
    var jerkScales = new HashMap<UUID, Double>();
    var linearJerks = new HashMap<UUID, Double>();
    var signedDemand = new HashMap<UUID, Double>();
    var chassisMass = new HashMap<UUID, Double>();
    var driveKeys = new HashMap<UUID, DriveKey>();
    for (var allocation : allocated.entrySet()) if (allocation.getKey().mechanism().drive()) for (var r : allocation.getValue()) {
      UUID id = r.cart().getUniqueId(); signedDemand.merge(id, r.force(), Double::sum); chassisMass.put(id, r.mass());
      driveKeys.put(id, new DriveKey(RideControllers.ownership(level, r.module()), id));
      if (allocation.getKey().mechanism().linear()) linearJerks.merge(id, allocation.getKey().jerkLimit(), Math::min);
    }
    for (var entry : linearJerks.entrySet()) {
      var id = entry.getKey(); double requested = signedDemand.get(id);
      var previous = driveHistory.get(driveKeys.get(id));
      double old = previous != null && previous.step() == step - 1 ? previous.acceleration() : 0;
      double limited = DriveMath.tireAccelerationStep(old, requested / chassisMass.get(id), entry.getValue(), dt) * chassisMass.get(id);
      jerkScales.put(id, Math.abs(requested) > 1e-10 ? Math.min(1, Math.abs(limited / requested)) : 0);
    }
    for (var allocation : allocated.entrySet()) {
      if (!allocation.getKey().mechanism().drive()) continue;
      allocation.setValue(allocation.getValue().stream().map(r -> {
        double demand = tireDemand.get(r.cart().getUniqueId());
        double scale = Math.min(demand > 0 ? Math.min(1, tireLimits.get(r.cart().getUniqueId()) / demand) : 1,
            jerkScales.getOrDefault(r.cart().getUniqueId(), 1.0));
        return new Request(r.module(), r.cart(), r.mass(), r.energyMass(), r.speed(), r.direction(), r.point(), r.force() * scale, r.key(), r.distance());
      }).toList());
    }
    var clamps = CLAMPS.computeIfAbsent(level, ignored -> new HashMap<>());
    var retained = new HashSet<Key>();
    // A coordinated pad bank shares each chassis provider rating across all of its contacts.
    var finShares = new HashMap<Key, Double>();
    var chassisDemand = new HashMap<UUID, Double>();
    for (var allocation : allocated.entrySet()) {
      if (allocation.getKey().mechanism() != Mechanism.FIN_BRAKE) continue;
      double totalMass = allocation.getValue().stream().mapToDouble(Request::mass).sum();
      for (var request : allocation.getValue()) {
        double share = Math.min(request.force(), FIN_FORCE * request.mass() / totalMass);
        finShares.put(request.key(), share); chassisDemand.merge(request.cart().getUniqueId(), share, Double::sum);
      }
    }
    for (var requests : allocated.values()) for (var request : requests) {
      var share = finShares.get(request.key()); if (share == null) continue;
      double scale = Math.min(1, request.force() / chassisDemand.get(request.cart().getUniqueId()));
      finShares.put(request.key(), share * scale);
    }
    for (var allocation : allocated.entrySet()) {
      var module = allocation.getKey();
      if (module.mechanism() != Mechanism.FIN_BRAKE || !module.armed() || !module.fault().isEmpty()) continue;
      var requests = allocation.getValue();
      for (var request : requests) {
        double share = finShares.get(request.key());
        double gravity = Math.abs(dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(level).dot(request.direction()));
        if (gravity * request.mass() > .8 * share) {
          module.fail("Shared fin clamp load exceeds the slope rating"); break;
        }
      }
      if (!module.armed() || !module.fault().isEmpty()) continue;
      for (var request : requests) {
        try {
          double share = finShares.get(request.key());
          var existing = clamps.get(request.key());
          if (existing != null && (!existing.identity().equals(module.identity()) || !existing.handle().valid())) {
            existing.handle().close(); clamps.remove(request.key()); existing = null;
          }
          if (existing == null) {
            existing = new Clamp(module.identity(), new NativeFinClamp(event.getPhysicsSystem().getPipeline(),
                request.cart(), request.point(), request.direction(), share));
            clamps.put(request.key(), existing);
          }
          existing.handle().align(request.direction()); existing.handle().setForce(share);
          retained.add(request.key());
        } catch (IllegalArgumentException | IllegalStateException failure) { module.fail(failure.getMessage()); }
      }
      if (!module.armed() || !module.fault().isEmpty()) requests.forEach(r -> retained.remove(r.key()));
    }
    // Remove old pad constraints before queuing a new owner or allowing released transport.
    clamps.entrySet().removeIf(e -> {
      if (retained.contains(e.getKey())) return false;
      e.getValue().handle().close(); return true;
    });
    var driveGroups = new LinkedHashMap<Object, Map<EquipmentCabinetEntity, List<Request>>>();
    for (var allocation : allocated.entrySet()) {
      var module = allocation.getKey();
      if (!module.fault().isEmpty() || !module.armed() || !module.mechanism().drive()) continue;
      driveGroups.computeIfAbsent(RideControllers.ownership(level, module), ignored -> new LinkedHashMap<>())
          .put(module, allocation.getValue());
    }
    for (var bank : driveGroups.values()) {
      var responses = new HashMap<Key, Double>();
      var allRequests = bank.values().stream().flatMap(List::stream).toList();
      for (var r : allRequests) {
        double response = 0;
        for (var other : allRequests) if (r.cart() == other.cart()) response += other.force()
            * NativeForces.pointImpulseResponse(r.cart(), r.point(), r.direction(), other.point(), other.direction());
        responses.put(r.key(), response);
      }
      double lower = 0, upper = 1;
      // Account for the combined impulse's cross-work. Every cabinet retains its own budget;
      // a negative-work contact grants no credit to any positive-work contact.
      for (int iteration = 0; iteration < 50; iteration++) {
        double scale = (lower + upper) / 2; boolean fits = true;
        for (var allocation : bank.entrySet()) {
          var module = allocation.getKey();
          double budget = module.mechanism().linear() ? module.energy() : 8 * Math.abs(module.getSpeed()) * 2 * Math.PI / 60 * dt, demand = 0;
          for (var r : allocation.getValue()) {
            double force = r.force() * scale;
            double work = force * (r.speed() + .5 * responses.get(r.key()) * scale * dt) * dt;
            demand += module.mechanism().linear() ? LinearMotor.transfer(module.mechanism(), r.speed(), module.targetSpeed(), force, work, dt).energy() : Math.max(0, work);
          }
          if (demand > budget) { fits = false; break; }
        }
        if (fits) lower = scale; else upper = scale;
      }
      // Pay every independent cabinet before queueing any motor force in the coordinated bank.
      for (var allocation : bank.entrySet()) if (allocation.getKey().mechanism().linear()) {
        var module = allocation.getKey(); double energy = 0, heat = 0;
        for (var r : allocation.getValue()) {
          double force = r.force() * lower;
          double work = force * (r.speed() + .5 * responses.get(r.key()) * lower * dt) * dt;
          var transfer = LinearMotor.transfer(module.mechanism(), r.speed(), module.targetSpeed(), force, work, dt);
          energy += transfer.energy(); heat += transfer.heat();
        }
        module.spendLinear(energy); module.absorbHeat(heat);
      }
      var actual = new HashMap<UUID, Double>();
      for (var requests : bank.values()) for (var request : requests) {
        double force = request.force() * lower;
        NativeForces.pointForce(request.cart(), request.point(), new Vector3d(request.direction()).mul(force), dt);
        history.put(request.key(), new History(step, request.distance(), force / request.mass()));
        actual.merge(request.cart().getUniqueId(), force / request.mass(), Double::sum);
      }
      actual.forEach((id, acceleration) -> driveHistory.put(driveKeys.get(id), new History(step, 0, acceleration)));
    }
    for (var allocation : allocated.entrySet()) {
      var module = allocation.getKey();
      if (module.mechanism() != Mechanism.EDDY_BRAKE || !module.armed() || !module.fault().isEmpty()) continue;
      double demand = allocation.getValue().stream().mapToDouble(r -> Math.abs(r.force())).sum();
      double scale = demand > EDDY_FORCE ? EDDY_FORCE / demand : 1;
      for (var request : allocation.getValue()) {
        double force = request.force() * scale;
        double work = DriveMath.incrementalWork(request.energyMass(), request.speed(), force, dt);
        if (work > 1e-10) { module.fail("Passive magnetic request would add interface energy"); break; }
      }
      if (!module.armed() || !module.fault().isEmpty()) continue;
      for (var request : allocation.getValue()) {
        double force = request.force() * scale;
        NativeForces.pointForce(request.cart(), request.point(), new Vector3d(request.direction()).mul(force), dt);
        module.absorbHeat(Math.max(0, -DriveMath.incrementalWork(request.energyMass(), request.speed(), force, dt)));
      }
    }
    history.values().removeIf(h -> h.step() < step - 2);
    driveHistory.values().removeIf(h -> h.step() < step - 2);
  }
  private static boolean permitted(dev.coasterslaunchsystems.api.MotionVehicle motion, Mechanism mechanism) {
    return motion.exclusiveOperator().isEmpty()
        && (mechanism.linear() ? motion.propulsionAllowed() && motion.brakingAllowed() : mechanism == Mechanism.TIRE ? motion.propulsionAllowed() : motion.brakingAllowed());
  }
}
