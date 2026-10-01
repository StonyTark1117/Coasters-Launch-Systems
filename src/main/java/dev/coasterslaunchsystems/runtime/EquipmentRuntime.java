package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;
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
  private record Clamp(UUID identity, NativeFinClamp handle) {}
  private static final double FIN_FORCE = 256, FIN_MASS = 64;
  private static final Map<ServerLevel, Map<Key, Clamp>> CLAMPS = new HashMap<>();
  private record Request(EquipmentCabinetEntity module, ServerSubLevel cart,
      double mass, double energyMass, double speed, Vector3d direction, Vector3d point, double force, Key key, double distance) {}
  private static final Map<ServerLevel, Map<BlockPos, Entry>> MODULES = new HashMap<>();
  private static final Map<ServerLevel, Map<Key, History>> HISTORY = new HashMap<>();
  private static final Map<ServerLevel, Long> STEPS = new HashMap<>();
  private EquipmentRuntime() {}

  public static void register(ServerLevel level, EquipmentCabinetEntity module) {
    MODULES.computeIfAbsent(level, ignored -> new LinkedHashMap<>()).put(module.getBlockPos(), new Entry(module, level.getGameTime()));
  }
  public static void unload(LevelEvent.Unload event) {
    if (event.getLevel() instanceof ServerLevel level) {
      MODULES.remove(level); HISTORY.remove(level); STEPS.remove(level);
      var clamps = CLAMPS.remove(level);
      if (clamps != null) clamps.values().forEach(c -> c.handle().close());
    }
  }
  public static void physics(ForgeSablePrePhysicsTickEvent event) {
    var level = event.getPhysicsSystem().getLevel();
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
    long step = STEPS.merge(level, 1L, Long::sum);
    double dt = event.getTimeStep();
    var groups = new LinkedHashMap<UUID, List<Request>>();
    for (var entry : List.copyOf(modules.values())) {
      var module = entry.module();
      if (!module.armed() || !module.fault().isEmpty()) continue;
      if (ModList.get().isLoaded("coastersengineered") || ModList.get().isLoaded("coasterfins")) {
        module.fail("An installed drive addon needs an ownership adapter"); continue;
      }
      var track = module.resolve(level);
      if (track == null) continue;
      if (module.mechanism() != Mechanism.TIRE && module.mechanism() != Mechanism.FIN_BRAKE) {
        module.fail("Matching brake reaction hardware is required"); continue;
      }
      var mechanism = module.mechanism();
      if (mechanism == Mechanism.TIRE && (Math.abs(module.getSpeed()) < 1 || Math.abs(module.getSpeed()) > 128)) continue;
      // Spring applied, shaft released. Loss of shaft power applies an armed, supported clamp.
      if (mechanism == Mechanism.FIN_BRAKE && Math.abs(module.getSpeed()) >= 32) continue;
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
          if (motion == null || !permitted(motion, mechanism)) continue;
          if (!motion.interfaces().contains(mechanism)) {
            module.fail(mechanism == Mechanism.TIRE ? "Vehicle has no certified tire interface" : "Vehicle has no certified friction fin"); continue;
          }
          boolean available = true; double consistMass = 0;
          for (var member : motion.consist()) {
            var linked = container.getSubLevel(member);
            var accepted = linked instanceof ServerSubLevel server ? EngagedContacts.current(level, server).orElse(null) : null;
            if (accepted == null) { available = false; break; }
            var linkedMotion = MotionProviders.resolve(level, member).orElse(null);
            if (linkedMotion == null || !permitted(linkedMotion, mechanism)
                || !linkedMotion.interfaces().contains(mechanism)) {
              available = false; break;
            }
            double memberMass = NativeForces.mass(level, linkedMotion);
            if (memberMass <= 0) { available = false; break; }
            consistMass += memberMass;
            if (ChainLiftSpanQuery.findAtGraphHit(level, accepted.hit(), 0) != null) {
              module.fail("Native chain drive owns part of this consist"); available = false; break;
            }
          }
          if (!available) continue;
          var point = mechanism == Mechanism.TIRE ? VehicleInterfaces.tirePoint(cart) : VehicleInterfaces.brakePoint(cart);
          if (point == null) { module.fail("Chassis interface hardware was removed"); continue; }
          double mass = NativeForces.mass(level, motion);
          if (mass <= 0) continue;
          var axis = track.forward(level, contact.hit());
          var direction = new Vector3d(axis.x, axis.y, axis.z);
          var dynamics = NativeForces.pointDynamics(cart, event.getPhysicsSystem().getPipeline(), point, direction);
          double speed = dynamics.speed();
          if (mechanism == Mechanism.FIN_BRAKE) {
            double gravity = Math.abs(dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(level).dot(direction));
            if (consistMass > FIN_MASS || gravity * consistMass > .8 * Math.min(FIN_FORCE, motion.maximumForce())) {
              module.fail("Loaded consist exceeds the fin clamp mass/slope rating"); continue;
            }
            UUID consist = motion.consist().stream().min(UUID::compareTo).orElseThrow();
            groups.computeIfAbsent(consist, ignored -> new ArrayList<>()).add(new Request(module, cart, mass,
                dynamics.effectiveMass(), speed, direction, point, Math.min(FIN_FORCE, motion.maximumForce()), key, distance));
            continue;
          }
          double wheelSpeed = DriveMath.wheelSpeed(module.getSpeed(), .4);
          double requested = DriveMath.tireForce(speed, wheelSpeed, 20, 8, .4);
          double limit = Math.min(Math.min(motion.maximumForce(), mass * motion.maximumAcceleration()),
              dynamics.effectiveMass() * Math.abs(wheelSpeed - speed) / dt);
          requested = Numbers.clamp(requested, -limit, limit);
          double acceleration = DriveMath.tireAccelerationStep(oldAcceleration, requested / mass, 4, dt);
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
      if (owners.size() != 1) {
        owners.forEach(m -> m.fail("Overlapping modules claim this consist; separate their contact zones")); continue;
      }
      var module = owners.getFirst();
      allocated.computeIfAbsent(module, ignored -> new ArrayList<>()).addAll(requests);
    }
    var clamps = CLAMPS.computeIfAbsent(level, ignored -> new HashMap<>());
    var retained = new HashSet<Key>();
    for (var allocation : allocated.entrySet()) {
      var module = allocation.getKey();
      if (module.mechanism() != Mechanism.FIN_BRAKE || !module.armed() || !module.fault().isEmpty()) continue;
      var requests = allocation.getValue();
      double contactedMass = requests.stream().mapToDouble(Request::mass).sum();
      for (var request : requests) {
        double share = Math.min(request.force(), FIN_FORCE * request.mass() / contactedMass);
        double gravity = Math.abs(dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(level).dot(request.direction()));
        if (gravity * request.mass() > .8 * share) {
          module.fail("Shared fin clamp load exceeds the slope rating"); break;
        }
      }
      if (!module.armed() || !module.fault().isEmpty()) continue;
      for (var request : requests) {
        try {
          double share = Math.min(request.force(), FIN_FORCE * request.mass() / contactedMass);
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
    for (var allocation : allocated.entrySet()) {
      var module = allocation.getKey();
      // A later contact can invalidate earlier requests collected from this module.
      if (!module.fault().isEmpty() || !module.armed() || module.mechanism() != Mechanism.TIRE) continue;
      var requests = allocation.getValue();
      double work = 8 * Math.abs(module.getSpeed()) * 2 * Math.PI / 60 * dt;
      // Share one shaft-work budget across all contacted consists. Dissipation is not credit.
      double lower = 0, upper = 1;
      for (int iteration = 0; iteration < 50; iteration++) {
        double scale = (lower + upper) / 2, demand = 0;
        for (var request : requests) demand += Math.max(0,
            DriveMath.incrementalWork(request.energyMass(), request.speed(), request.force() * scale, dt));
        if (demand <= work) lower = scale; else upper = scale;
      }
      for (var request : requests) {
        double force = request.force() * lower;
        NativeForces.pointForce(request.cart(), request.point(),
            new Vector3d(request.direction()).mul(force), dt);
        history.put(request.key(), new History(step, request.distance(), force / request.mass()));
      }
    }
    history.values().removeIf(h -> h.step() < step - 2);
  }
  private static boolean permitted(dev.coasterslaunchsystems.api.MotionVehicle motion, Mechanism mechanism) {
    return motion.exclusiveOperator().isEmpty()
        && (mechanism == Mechanism.TIRE ? motion.propulsionAllowed() : motion.brakingAllowed());
  }
}
