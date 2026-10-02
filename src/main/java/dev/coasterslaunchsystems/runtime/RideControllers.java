package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.api.MotionVehicle;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.content.RideControllerEntity.State;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.math.StoppingEnvelope;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.DirectedRoute;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.joml.Vector3d;

/** Decisions run after native loading and engagement and before any controlled equipment request. */
public final class RideControllers {
  private record Entry(RideControllerEntity controller, long tick) {}
  private static final Map<ServerLevel, Map<BlockPos, Entry>> CONTROLLERS = new HashMap<>();
  private static final Map<ServerLevel, Map<UUID, Long>> REGISTRATION_GRACE = new HashMap<>();
  private static final long STARTUP_REGISTRATION_GRACE_TICKS = 20;
  private RideControllers() {}
  public static void register(ServerLevel level, RideControllerEntity controller) {
    CONTROLLERS.computeIfAbsent(level, ignored -> new LinkedHashMap<>()).put(controller.getBlockPos(), new Entry(controller, level.getGameTime()));
  }
  public static void unload(LevelEvent.Unload event) { if (event.getLevel() instanceof ServerLevel level) { CONTROLLERS.remove(level); REGISTRATION_GRACE.remove(level); CableControllers.unload(level); CableRecovery.unload(level); } }
  static boolean awaitingRegistration(ServerLevel level, RideControllerEntity controller) {
    if (!controller.reservation().isEmpty() || controller.phase() != State.HOLD) return false;
    long first = REGISTRATION_GRACE.computeIfAbsent(level, ignored -> new HashMap<>())
        .computeIfAbsent(controller.identity(), ignored -> level.getGameTime());
    // A newly armed, empty section cannot dispatch during registration. Let its cabinets finish
    // their first block-entity ticks under slow world startup before treating absence as a fault.
    return level.getGameTime() - first <= STARTUP_REGISTRATION_GRACE_TICKS;
  }
  static void registrationReady(ServerLevel level, RideControllerEntity controller) {
    var waiting = REGISTRATION_GRACE.get(level);
    if (waiting != null) waiting.remove(controller.identity());
  }
  public static RideControllerEntity owner(ServerLevel level, EquipmentCabinetEntity module) {
    if (module.controllerIdentity() == null || !level.hasChunkAt(module.controllerPosition())) return null;
    var entries = CONTROLLERS.getOrDefault(level, Map.of()); var entry = entries.get(module.controllerPosition());
    if (entry == null || entry.tick() < level.getGameTime() - 1 || entry.controller().isRemoved()
        || level.getBlockEntity(module.controllerPosition()) != entry.controller()
        || !module.controllerIdentity().equals(entry.controller().identity())
        || entry.controller().modules().stream().noneMatch(m -> m.position().equals(module.getBlockPos()) && m.identity().equals(module.identity()))) return null;
    return entry.controller();
  }
  public static RideControllerEntity controller(ServerLevel level, RideControllerEntity.Peer peer) {
    if (peer == null || !level.hasChunkAt(peer.position())) return null;
    var entry = CONTROLLERS.getOrDefault(level, Map.of()).get(peer.position());
    if (entry == null || entry.tick() < level.getGameTime() - 1 || entry.controller().isRemoved()
        || level.getBlockEntity(peer.position()) != entry.controller() || !entry.controller().identity().equals(peer.identity())) return null;
    return entry.controller();
  }
  public static boolean editingAllowed(ServerLevel level, EquipmentCabinetEntity module) {
    if (module.controllerIdentity() == null) return true;
    var controller = owner(level, module);
    return controller != null && controller.reservation().isEmpty() && !controller.armed();
  }
  public static boolean occupiedEquipment(ServerLevel level, BlockPos pos) {
    if (level.getBlockEntity(pos) instanceof EquipmentCabinetEntity module && (module.cableInUse() || module.cableInstalled() || !editingAllowed(level, module))) return true;
    if (level.getBlockEntity(pos) instanceof RideControllerEntity controller && (!controller.reservation().isEmpty() || !controller.modules().isEmpty())) return true;
    return CONTROLLERS.getOrDefault(level, Map.of()).values().stream().map(Entry::controller)
        .anyMatch(c -> !c.reservation().isEmpty() && c.endpoint(pos));
  }
  public static void breaking(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
    if (event.getLevel() instanceof ServerLevel level && occupiedEquipment(level, event.getPos())) {
      event.setCanceled(true); event.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Clear the native reservation, then disarm and unlink controller equipment before removing it"), true);
    }
  }
  public static void wrenching(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
    if (event.getLevel() instanceof ServerLevel level && event.getItemStack().is(com.simibubi.create.AllItems.WRENCH.get())
        && occupiedEquipment(level, event.getPos())) event.setCanceled(true);
  }
  public static Object ownership(ServerLevel level, EquipmentCabinetEntity module) {
    var owner = owner(level, module);
    if (owner != null) return leaseOwner(level, owner);
    // A lost receiver never grants drive commands. Its exact journaled spring pads retain the source brake owner.
    if (module.mechanism() == Mechanism.FIN_BRAKE && module.controllerIdentity() != null) {
      UUID retained = null;
      var track = module.resolve(level);
      if (track != null) for (var entry : CONTROLLERS.getOrDefault(level, Map.of()).values()) {
        var source = entry.controller(); var peer = source.destination();
        if (source.isRemoved() || entry.tick() < level.getGameTime() - 1 || !level.hasChunkAt(source.getBlockPos())
            || level.getBlockEntity(source.getBlockPos()) != source || !source.transferring() || source.reservation().isEmpty()
            || peer == null || !peer.position().equals(module.controllerPosition()) || !peer.identity().equals(module.controllerIdentity())
            || source.receiverManifest().stream().noneMatch(m -> m.position().equals(module.getBlockPos()) && m.identity().equals(module.identity()))
            || source.savedTransferSpans().isEmpty()) continue;
        var receiving = source.savedTransferSpans().getLast();
        if (!receiving.sameRail(track.edge().from(), track.edge().to()) || receiving.geometry() != track.fingerprint()) continue;
        if (retained != null && !retained.equals(source.identity())) return module; // Ambiguity cannot select an owner by callback order.
        retained = source.identity();
      }
      if (retained != null) return retained;
    }
    return module;
  }
  private static UUID leaseOwner(ServerLevel level, RideControllerEntity controller) {
    var source = controller(level, controller.incoming());
    return source != null && source.transferring() && source.destination() != null
        && source.destination().identity().equals(controller.identity()) && source.destination().position().equals(controller.getBlockPos())
        && source.reservation().equals(controller.reservation()) ? source.identity() : controller.identity();
  }
  public static boolean routeAvailable(ServerLevel level, RideControllerEntity source, RideControllerEntity target, DirectedRoute route) {
    return routeAvailable(level, source, target, route, List.of());
  }
  static boolean routeAvailable(ServerLevel level, RideControllerEntity source, RideControllerEntity target, DirectedRoute route,
      List<RideControllerEntity.Module> inspectedOrphans) {
    for (var entry : CONTROLLERS.getOrDefault(level, Map.of()).values()) {
      var other = entry.controller();
      if (other.isRemoved() || !level.hasChunkAt(other.getBlockPos()) || level.getBlockEntity(other.getBlockPos()) != other) continue;
      if (other == source || other == target || !other.armed() && other.reservation().isEmpty()) continue;
      if (other.claimedSpans().stream().anyMatch(s -> route.spans().stream().anyMatch(r -> r.sameRail(s.from(), s.to())))) return false;
    }
    return !EquipmentRuntime.foreignRouteEquipment(level, source, target, route, inspectedOrphans);
  }
  public static boolean permitsDrive(ServerLevel level, EquipmentCabinetEntity module, UUID chassis) {
    if (module.controllerIdentity() == null) return true;
    var controller = owner(level, module);
    if (controller == null || !controller.armed() || !controller.reservation().contains(chassis)) return false;
    if (controller.phase() == State.EXITING) return controller.exitDrive(module);
    if (controller.phase() == State.RUNNING) return !controller.exitDrive(module);
    if (controller.phase() != State.RECEIVING || controller.exitDrive(module)) return false;
    var source = controller(level, controller.incoming());
    return source != null && source.armed() && source.phase() == State.EXITING && source.transferring()
        && source.reservation().equals(controller.reservation());
  }
  public static boolean finReleased(ServerLevel level, EquipmentCabinetEntity module) {
    if (Math.abs(module.getSpeed()) < 32) return false;
    if (module.controllerIdentity() == null) return true;
    var controller = owner(level, module);
    if (controller == null || !controller.armed()) return false;
    if (controller.phase() == State.EXITING) return true;
    boolean running = controller.phase() == State.RUNNING;
    if (controller.phase() == State.RECEIVING) {
      var source = controller(level, controller.incoming());
      running = source != null && source.armed() && source.phase() == State.EXITING && source.transferring()
          && source.reservation().equals(controller.reservation());
    }
    if (!running) return false;
    // Only the launch pads retract; downstream stop pads remain applied even with a powered shaft.
    return controller.modules().stream().map(m -> level.getBlockEntity(m.position()))
        .filter(EquipmentCabinetEntity.class::isInstance).map(EquipmentCabinetEntity.class::cast)
        .anyMatch(m -> (m.mechanism().drive() && !controller.exitDrive(m) && m.interval().end() >= module.interval().start()
            && m.interval().start() <= module.interval().end())
            || m.mechanism().cable() && module.interval().start() <= m.interval().end()
                && module.interval().end() >= m.interval().start());
  }
  public static void physics(ServerLevel level, double dt) {
    var entries = CONTROLLERS.get(level); if (entries == null) { CableControllers.audit(level, List.of()); return; }
    entries.values().removeIf(e -> e.controller().isRemoved() || e.tick() < level.getGameTime() - 1
        || level.getBlockEntity(e.controller().getBlockPos()) != e.controller());
    var controllers = entries.values().stream().map(Entry::controller).toList();
    CableControllers.audit(level, controllers);
    for (var controller : controllers) controller.inspectRequestedReceiver(level);
    var tracks = new HashMap<RideControllerEntity, DirectedTrack>();
    for (var controller : controllers) {
      if (!controller.armed() && controller.reservation().isEmpty()) continue;
      var track = controller.resolve(level); if (track != null) tracks.put(controller, track);
    }
    // Section claims arbitrate together. Callback order never selects a winner.
    for (var a : controllers) for (var b : controllers)
      if (a != b && (a.armed() || !a.reservation().isEmpty()) && (b.armed() || !b.reservation().isEmpty())
          && !leaseOwner(level, a).equals(leaseOwner(level, b))
          && a.claimedSpans().stream().anyMatch(s -> b.claimedSpans().stream().anyMatch(r -> r.sameRail(s.from(), s.to())))) {
        a.fail("Another controller claims this reserved route"); b.fail("Another controller claims this reserved route");
      }
    for (var controller : controllers) {
      if (!controller.armed() || controller.phase() == State.FAULT) { CableControllers.idle(level, controller); controller.consumeRequest(); continue; }
      var track = tracks.get(controller); if (track == null) { CableControllers.idle(level, controller); controller.consumeRequest(); continue; }
      try {
        if (CableControllers.applies(level, controller)) CableControllers.evaluate(level, controller, track, dt);
        else if (!RideTransfers.active(level, controller, track)) evaluate(level, controller, track);
      }
      catch (IllegalArgumentException | IllegalStateException failure) { controller.fail(failure.getMessage()); }
      if (controller.phase() == State.FAULT) CableControllers.idle(level, controller);
    }
  }
  private static void evaluate(ServerLevel level, RideControllerEntity controller, DirectedTrack track) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null) { controller.fail("Native physics unavailable"); return; }
    var modules = new ArrayList<EquipmentCabinetEntity>();
    for (var linked : controller.modules()) {
      if (!level.hasChunkAt(linked.position())) { controller.fail("Linked module chunk unavailable at " + linked.position()); return; }
      if (!(level.getBlockEntity(linked.position()) instanceof EquipmentCabinetEntity module)) {
        controller.fail("Linked module missing or replaced at " + linked.position()); return;
      }
      if (!linked.identity().equals(module.identity()) || owner(level, module) != controller) {
        controller.fail("Linked module identity or controller ownership changed at " + linked.position()); return;
      }
      if (!EquipmentRuntime.available(level, module)) {
        // A newly armed section may be evaluated before a just-placed cabinet's first BE tick.
        // Once any consist is reserved, loss of registration is a real operating fault.
        if (awaitingRegistration(level, controller)) {
          controller.consumeRequest(); return;
        }
        controller.fail("Linked module has no current physics registration at " + linked.position()); return;
      }
      if (!module.armed() || !module.fault().isEmpty()) {
        controller.fail("Linked module disarmed or faulted at " + linked.position() + ": " + module.fault()); return;
      }
      var moduleTrack = module.resolve(level);
      if (moduleTrack == null) { controller.fail("Linked module binding invalid at " + linked.position() + ": " + module.fault()); return; }
      if (!moduleTrack.edge().equals(track.edge()) || moduleTrack.forward(level, moduleTrack.hitAtDistance(level, 0))
          .dot(track.forward(level, track.hitAtDistance(level, 0))) < .99) {
        controller.fail("A linked module changed its rail section or direction"); return;
      }
      modules.add(module);
    }
    registrationReady(level, controller);
    var drives = modules.stream().filter(m -> m.mechanism().drive() && !controller.exitDrive(m)).toList();
    if (drives.isEmpty()) { controller.fail("No linked drive"); return; }
    var driveInterfaces = drives.stream().map(EquipmentCabinetEntity::mechanism).collect(java.util.stream.Collectors.toSet());
    double launchEnd = drives.stream().mapToDouble(m -> m.interval().end()).max().orElseThrow();
    var stops = modules.stream().filter(m -> m.mechanism() == Mechanism.FIN_BRAKE && m.interval().start() > launchEnd)
        .sorted(Comparator.comparingDouble(m -> m.interval().start())).toList();
    if (stops.isEmpty()) { controller.fail("A downstream friction-fin bank is required"); return; }
    double stopStart = stops.getFirst().interval().start(), stopEnd = stops.getFirst().interval().end();
    for (var stop : stops.subList(1, stops.size())) {
      if (stop.interval().start() > stopEnd + 1e-6) { controller.fail("Downstream friction-fin coverage has a gap"); return; }
      stopEnd = Math.max(stopEnd, stop.interval().end());
    }
    // Identify every actual native cart on this section, including a second train in the stopping region.
    var occupying = new LinkedHashMap<UUID, MotionVehicle>();
    for (var body : container.getAllSubLevels()) {
      var contact = EngagedContacts.current(level, body).orElse(null);
      if (contact == null || !track.matches(contact.hit())) continue;
      var motion = MotionProviders.resolve(level, body.getUniqueId()).orElse(null);
      if (motion == null) { controller.fail("An unidentified cart occupies the section"); return; }
      occupying.put(body.getUniqueId(), motion);
    }
    if (occupying.isEmpty() && controller.reservation().isEmpty()) { controller.state(State.HOLD); controller.consumeRequest(); return; }
    Set<UUID> members = controller.reservation().isEmpty() ? Set.copyOf(occupying.values().iterator().next().consist()) : controller.reservation();
    if (!members.containsAll(occupying.keySet())) { controller.fail("Another consist occupies the reserved stopping region"); return; }
    // Reserve on approach/hold as well as launch. A rollback or missing contact never clears ownership.
    controller.reserve(members);
    boolean allClear = true, held = true, ready = true, running = controller.phase() == State.RUNNING || controller.phase() == State.BRAKING;
    double mass = 0, minimumForce = 256, maximumSpeed = 0, smallest = Double.POSITIVE_INFINITY, largest = Double.NEGATIVE_INFINITY, radius = 0;
    var bodyIds = new HashSet<UUID>();
    for (var id : members) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel cart) || cart.isRemoved()) { controller.fail("Reserved chassis unavailable; ownership retained"); return; }
      var motion = MotionProviders.resolve(level, id).orElse(null); var contact = EngagedContacts.current(level, cart).orElse(null);
      if (motion == null || contact == null || !Set.copyOf(motion.consist()).equals(members) || !motion.exclusiveOperator().isEmpty()
          || !motion.interfaces().contains(Mechanism.TIRE) || !motion.interfaces().contains(Mechanism.FIN_BRAKE)
          || !motion.interfaces().containsAll(driveInterfaces)) {
        controller.fail("Reserved vehicle support, consist or certified interfaces changed"); return;
      }
      if (ChainLiftSpanQuery.findAtGraphHit(level, contact.hit(), 0) != null) { controller.fail("A native chain owns the reserved consist"); return; }
      if (running && (!motion.propulsionAllowed() || !motion.brakingAllowed())) { controller.fail("Vehicle provider withdrew motion authority"); return; }
      double loadedMass = NativeForces.mass(level, motion); if (loadedMass <= 0) { controller.fail("Loaded native mass unavailable"); return; }
      for (var body : motion.bodies()) if (!bodyIds.add(body)) { controller.fail("Duplicate articulated body in consist"); return; }
      mass += loadedMass;
      // Captured 4D cars carry no external brake authority until their provider grants dispatch.
      minimumForce = Math.min(minimumForce, motion.maximumBrakeForce());
      if (minimumForce <= 0) { controller.fail("Vehicle has no declared friction brake rating"); return; }
      double bodyRadius = radius(level, motion); radius = Math.max(radius, bodyRadius);
      var point = bearing(cart);
      var closest = CoasterPathTrackFrame.closestHitOnEdge(level, track.edge(), new net.minecraft.world.phys.Vec3(point.x, point.y, point.z));
      boolean clear = !track.matches(contact.hit()) && point.distance(new Vector3d(closest.point().x, closest.point().y, closest.point().z)) > bodyRadius + 1;
      allClear &= clear;
      if (!track.matches(contact.hit())) {
        if (!running && !clear) { controller.fail("Part of the reserved consist has unavailable section contact"); return; }
        held = false; ready = false; continue;
      }
      double distance = track.distance(contact.hit()); smallest = Math.min(smallest, distance); largest = Math.max(largest, distance);
      var axis = track.forward(level, contact.hit()); var direction = new Vector3d(axis.x, axis.y, axis.z);
      double speed = NativeForces.pointDynamics(cart, container.physicsSystem().getPipeline(), VehicleInterfaces.tirePoint(cart), direction).speed();
      maximumSpeed = Math.max(maximumSpeed, Math.abs(speed));
      held &= Math.abs(speed) < .03 && ((EquipmentRuntime.activeFinClamps(level, id) > 0) || !motion.propulsionAllowed() && (motion.dispatchReady() || controller.stationCapture() && StationCapture.vehicleHeld(level, id)));
      ready &= motion.dispatchReady() && drives.stream().anyMatch(m -> m.interval().intersects(distance, distance, .6));
    }
    if (allClear) { controller.fail("Train left the section without a reserved transfer"); return; }
    if (!Double.isFinite(smallest) || !Double.isFinite(largest)) { controller.consumeRequest(); return; }
    double ceiling = drives.stream().mapToDouble(m -> Math.abs(DriveEquipment.speed(m))).max().orElse(0);
    boolean powered = drives.stream().allMatch(DriveEquipment::forwardPowered)
        && modules.stream().filter(m -> m.mechanism() == Mechanism.FIN_BRAKE && m.interval().start() <= launchEnd)
            .allMatch(m -> Math.abs(m.getSpeed()) >= 32);
    if (mass > 64) { controller.fail("Loaded consist exceeds the current 64-unit fin rating"); return; }
    double adverseGravity = 0;
    var gravity = dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(level);
    for (int sample = 0; sample <= 64; sample++) {
      var tangent = track.forward(level, track.hitAtDistance(level, track.length() * sample / 64));
      adverseGravity = Math.max(adverseGravity, Math.abs(gravity.dot(new Vector3d(tangent.x, tangent.y, tangent.z))));
    }
    var envelope = new StoppingEnvelope(mass, Math.max(maximumSpeed, ceiling), minimumForce, adverseGravity, .1, largest - smallest + 2 * radius);
    controller.envelope(envelope.requiredLength(), stopEnd - stopStart);
    if (!envelope.fits(stopEnd - stopStart)) { controller.fail("Downstream fin bank is too short for this loaded train and speed"); return; }
    if (running) {
      if (!powered && controller.phase() == State.RUNNING) { controller.fail("Launch release power lost; spring pads apply"); return; }
      if (maximumSpeed > ceiling + .1 && controller.phase() == State.RUNNING) { controller.fail("Reserved train exceeded the certified launch speed"); return; }
      if (largest >= stopStart - .6) controller.state(State.BRAKING);
      // Stop-bank arrival ends propulsion, while ownership remains until every native tail is clear.
      if (held && maximumSpeed < .03 && controller.phase() == State.BRAKING) controller.state(State.HOLD);
      controller.consumeRequest(); return;
    }
    if (StationCapture.evaluate(level, controller, held)) return;
    if (RideTransfers.departure(level, controller, track, held)) return;
    double launchMass = mass;
    boolean charged = drives.stream().filter(m -> m.mechanism().linear()).allMatch(m -> m.targetSpeed() > 0
        && m.energy() >= dev.coasterslaunchsystems.math.LinearMotor.launchReserve(m.mechanism(), launchMass, m.targetSpeed(), m.accelerationLimit(), m.jerkLimit()));
    controller.state(held && ready && powered && charged && largest + radius < stopStart ? State.READY : State.HOLD);
    if (!controller.consumeRequest() || controller.phase() != State.READY) return;
    // The route has already been reserved. Delegate release to the uniquely resolved vehicle owner.
    if (!MotionProviders.requestDispatch(level, members.iterator().next())) { controller.fail("Vehicle provider rejected dispatch"); return; }
    for (var id : members) {
      var released = MotionProviders.resolve(level, id).orElseThrow();
      if (!released.propulsionAllowed() || !released.brakingAllowed() || !released.exclusiveOperator().isEmpty()) {
        controller.fail("Vehicle provider did not grant launch and braking authority"); return;
      }
    }
    controller.state(State.RUNNING);
  }
  private static Vector3d bearing(ServerSubLevel body) {
    return new Vector3d(body.getPlot().getCenterBlock().getX() + .5, body.getPlot().getCenterBlock().getY() + .5,
        body.getPlot().getCenterBlock().getZ() + .5).sub(body.getMassTracker().getCenterOfMass())
        .rotate(body.logicalPose().orientation()).add(body.logicalPose().position());
  }
  /** Use actual loaded native bounds rather than a style-based car length; re-evaluate at every physics step. */
  static double radius(ServerLevel level, MotionVehicle motion) {
    var container = SubLevelContainer.getContainer(level); var cart = (ServerSubLevel) container.getSubLevel(motion.chassis());
    var origin = bearing(cart); double result = .6;
    for (var id : motion.bodies()) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel body) || body.isRemoved()) throw new IllegalArgumentException("Articulated body unavailable");
      var bounds = body.getPlot().getBoundingBox();
      for (double x : new double[] {bounds.minX(), bounds.maxX() + 1})
        for (double y : new double[] {bounds.minY(), bounds.maxY() + 1})
          for (double z : new double[] {bounds.minZ(), bounds.maxZ() + 1}) {
            var corner = new Vector3d(x, y, z).sub(body.getMassTracker().getCenterOfMass())
                .rotate(body.logicalPose().orientation()).add(body.logicalPose().position());
            result = Math.max(result, corner.distance(origin));
          }
    }
    return result;
  }
}
