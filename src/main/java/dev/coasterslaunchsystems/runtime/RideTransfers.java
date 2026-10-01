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
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** Paired fixed-section reservation. Saved leases never grant motion without a new inspected request. */
public final class RideTransfers {
  private record Section(RideControllerEntity controller, DirectedTrack track, List<EquipmentCabinetEntity> launch,
      List<EquipmentCabinetEntity> exit, List<EquipmentCabinetEntity> fins, double brakeStart, double brakeEnd) {
    double launchEnd() { return launch.stream().mapToDouble(m -> m.interval().end()).max().orElseThrow(); }
    double ceiling(List<EquipmentCabinetEntity> drives) {
      return drives.stream().mapToDouble(m -> Math.abs(DriveMath.wheelSpeed(m.getSpeed(), .4))).max().orElse(0);
    }
    boolean powered(List<EquipmentCabinetEntity> drives) { return !drives.isEmpty() && drives.stream().allMatch(m -> m.getSpeed() >= 1 && m.getSpeed() <= 128); }
    boolean releasePowered(boolean all) {
      return fins.stream().filter(m -> all || m.interval().start() <= launchEnd()).allMatch(m -> Math.abs(m.getSpeed()) >= 32);
    }
    boolean contact(List<EquipmentCabinetEntity> drives, double distance) {
      return drives.stream().anyMatch(m -> m.interval().intersects(distance, distance, .6));
    }
  }
  private record Row(ServerSubLevel cart, MotionVehicle motion, EngagedContacts.Contact contact, double distance, double speed, double radius) {}
  private record Train(List<Row> rows, double mass, double force, double length, double speed) {
    boolean held(ServerLevel level) {
      return rows.stream().allMatch(r -> Math.abs(r.speed()) < .03 && (EquipmentRuntime.activeFinClamps(level, r.cart().getUniqueId()) > 0
          || r.motion().dispatchReady() && !r.motion().propulsionAllowed()));
    }
    boolean releasable() { return rows.stream().allMatch(r -> r.motion().dispatchReady() || r.motion().propulsionAllowed() && r.motion().brakingAllowed()); }
    boolean released() { return rows.stream().allMatch(r -> r.motion().propulsionAllowed() && r.motion().brakingAllowed()); }
  }
  private RideTransfers() {}

  private static Section section(ServerLevel level, RideControllerEntity controller, DirectedTrack track) {
    if (!controller.armed() || controller.phase() == State.FAULT || track == null) throw new IllegalStateException("Transfer section is unarmed or unavailable");
    var launch = new ArrayList<EquipmentCabinetEntity>(); var exit = new ArrayList<EquipmentCabinetEntity>(); var fins = new ArrayList<EquipmentCabinetEntity>();
    for (var link : controller.modules()) {
      if (!level.hasChunkAt(link.position()) || !(level.getBlockEntity(link.position()) instanceof EquipmentCabinetEntity module)
          || !link.identity().equals(module.identity()) || RideControllers.owner(level, module) != controller
          || !EquipmentRuntime.available(level, module) || !module.armed() || !module.fault().isEmpty())
        throw new IllegalStateException("Transfer equipment is unavailable or replaced");
      var bound = module.resolve(level);
      if (bound == null || !track.edge().equals(bound.edge()) || track.forward(level, track.hitAtDistance(level, 0))
          .dot(bound.forward(level, bound.hitAtDistance(level, 0))) < .99) throw new IllegalStateException("Transfer module binding changed");
      if (module.mechanism() == Mechanism.TIRE) (link.exitDrive() ? exit : launch).add(module);
      else if (module.mechanism() == Mechanism.FIN_BRAKE) fins.add(module);
      else throw new IllegalStateException("Uncertified transfer mechanism");
    }
    if (launch.isEmpty()) throw new IllegalStateException("Transfer section has no inlet drives");
    double launchEnd = launch.stream().mapToDouble(m -> m.interval().end()).max().orElseThrow();
    var bank = fins.stream().filter(m -> m.interval().start() > launchEnd).sorted(Comparator.comparingDouble(m -> m.interval().start())).toList();
    if (bank.isEmpty()) throw new IllegalStateException("Transfer section has no downstream friction bank");
    double start = bank.getFirst().interval().start(), end = bank.getFirst().interval().end();
    for (var module : bank.subList(1, bank.size())) {
      if (module.interval().start() > end + 1e-6) throw new IllegalStateException("Transfer stopping coverage has a gap");
      end = Math.max(end, module.interval().end());
    }
    return new Section(controller, track, launch, exit, fins, start, end);
  }
  private static RideControllerEntity destination(ServerLevel level, RideControllerEntity source) {
    var target = RideControllers.controller(level, source.destination());
    if (target == null || !source.to().equals(target.from())) return null;
    var a = source.resolve(level); var b = target.resolve(level);
    if (a == null || b == null || a.forward(level, a.hitAtDistance(level, a.length())).dot(b.forward(level, b.hitAtDistance(level, 0))) < .99999) return null;
    long degree = dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.get(level).edges().stream()
        .filter(e -> e.from().equals(source.to()) || e.to().equals(source.to())).count();
    return degree == 2 ? target : null;
  }
  private static boolean paired(RideControllerEntity source, RideControllerEntity target) {
    return source.transferring() && target.incoming() != null && target.incoming().identity().equals(source.identity())
        && target.incoming().position().equals(source.getBlockPos()) && source.reservation().equals(target.reservation());
  }
  private static void failPair(RideControllerEntity source, RideControllerEntity target, String reason) {
    source.fail(reason); if (target != null && paired(source, target)) target.fail(reason);
  }
  private static boolean occupied(ServerLevel level, Section section, Set<UUID> allowed) {
    for (var cart : SubLevelContainer.getContainer(level).getAllSubLevels()) {
      var contact = EngagedContacts.current(level, cart).orElse(null);
      if (contact != null && section.track().matches(contact.hit()) && !allowed.contains(cart.getUniqueId())) return true;
    }
    return false;
  }
  private static Train train(ServerLevel level, RideControllerEntity source, Section first, Section second) {
    var container = SubLevelContainer.getContainer(level); var rows = new ArrayList<Row>(); var bodies = new HashSet<UUID>();
    var axis = first.track().forward(level, first.track().hitAtDistance(level, 0)); var direction = new Vector3d(axis.x, axis.y, axis.z);
    double mass = 0, force = 256, speed = 0, minimum = Double.POSITIVE_INFINITY, maximum = Double.NEGATIVE_INFINITY, largestRadius = 0;
    for (var id : source.reservation()) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel cart) || cart.isRemoved()) throw new IllegalStateException("Reserved transfer chassis unavailable");
      var motion = MotionProviders.resolve(level, id).orElse(null); var contact = EngagedContacts.current(level, cart).orElse(null);
      if (motion == null || contact == null || !Set.copyOf(motion.consist()).equals(source.reservation()) || !motion.exclusiveOperator().isEmpty()
          || !motion.interfaces().containsAll(Set.of(Mechanism.TIRE, Mechanism.FIN_BRAKE)) || motion.maximumBrakeForce() <= 0)
        throw new IllegalStateException("Transfer vehicle support, ownership or interfaces changed");
      if (!first.track().matches(contact.hit()) && !second.track().matches(contact.hit())) throw new IllegalStateException("Reserved train left the paired route");
      if (ChainLiftSpanQuery.findAtGraphHit(level, contact.hit(), 0) != null) throw new IllegalStateException("Native chain owns the transfer train");
      double loaded = NativeForces.mass(level, motion); if (loaded <= 0) throw new IllegalStateException("Transfer native mass unavailable");
      for (var body : motion.bodies()) if (!bodies.add(body)) throw new IllegalStateException("Duplicate body in transfer consist");
      mass += loaded; force = Math.min(force, motion.maximumBrakeForce());
      double radius = radius(level, motion); largestRadius = Math.max(largestRadius, radius);
      var point = bearing(cart); double position = point.dot(direction); minimum = Math.min(minimum, position); maximum = Math.max(maximum, position);
      double velocity = NativeForces.pointDynamics(cart, container.physicsSystem().getPipeline(), VehicleInterfaces.tirePoint(cart), direction).speed();
      speed = Math.max(speed, Math.abs(velocity));
      double distance = first.track().matches(contact.hit()) ? first.track().distance(contact.hit()) : first.track().length() + second.track().distance(contact.hit());
      rows.add(new Row(cart, motion, contact, distance, velocity, radius));
    }
    if (rows.isEmpty() || mass > 64) throw new IllegalStateException("Transfer consist exceeds the supported fin rating");
    return new Train(rows, mass, force, maximum - minimum + 2 * largestRadius, speed);
  }
  private static boolean certificate(ServerLevel level, Section first, Section second, Train train) {
    double speed = Math.max(train.speed(), Math.max(first.ceiling(first.exit()), second.ceiling(second.launch()))), gravity = 0;
    var nativeGravity = dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(level);
    for (var section : List.of(first, second)) for (int sample = 0; sample <= 64; sample++) {
      var tangent = section.track().forward(level, section.track().hitAtDistance(level, section.track().length() * sample / 64));
      gravity = Math.max(gravity, Math.abs(nativeGravity.dot(new Vector3d(tangent.x, tangent.y, tangent.z))));
    }
    var envelope = new StoppingEnvelope(train.mass(), speed, train.force(), gravity, .1, train.length());
    first.controller().envelope(envelope.requiredLength(), second.brakeEnd() - second.brakeStart());
    return envelope.fits(second.brakeEnd() - second.brakeStart());
  }

  /** Called before ordinary local decisions; receiver commands are suppressed while another section owns its lease. */
  public static boolean active(ServerLevel level, RideControllerEntity controller, DirectedTrack track) {
    if (controller.incoming() != null) {
      var source = RideControllers.controller(level, controller.incoming());
      if (source == null || source.phase() == State.FAULT || !paired(source, controller))
        controller.fail("Transfer source unavailable; reservation retained");
      else if (!source.armed() && controller.phase() == State.HOLD && source.phase() == State.INSPECTION)
        controller.state(State.HOLD); // An inspected saved pair may be armed one controller at a time.
      else if (!source.armed()) controller.fail("Transfer source disarmed; reservation retained");
      else controller.state(State.RECEIVING);
      controller.consumeRequest(); return true;
    }
    if (!controller.transferring()) return false;
    var target = destination(level, controller);
    try {
      if (target == null || !paired(controller, target)) throw new IllegalStateException("Transfer destination unavailable; reservation retained");
      if (!target.armed() && target.phase() == State.INSPECTION && (controller.phase() == State.HOLD || controller.phase() == State.READY_EXIT)) {
        controller.state(State.HOLD); controller.consumeRequest(); return true;
      }
      var first = section(level, controller, track); var second = section(level, target, target.resolve(level)); var train = train(level, controller, first, second);
      if (occupied(level, first, controller.reservation()) || occupied(level, second, controller.reservation())) throw new IllegalStateException("Another train entered the reserved transfer");
      if (controller.phase() != State.EXITING && controller.phase() != State.TRANSFER_BRAKING) {
        prepare(level, controller, target, first, second, train); return true;
      }
      if (!train.released()) throw new IllegalStateException("Vehicle provider withdrew transfer motion authority");
      if (!certificate(level, first, second, train)) throw new IllegalStateException("Loaded transfer stopping certificate no longer fits");
      if (train.rows().stream().anyMatch(r -> r.speed() < -.1)) throw new IllegalStateException("Rollback during transfer; both sections remain reserved");
      if (!first.powered(first.exit()) || !first.releasePowered(true) || !second.powered(second.launch()) || !second.releasePowered(false))
        throw new IllegalStateException("Transfer release power lost; spring pads apply");
      double ceiling = Math.max(first.ceiling(first.exit()), second.ceiling(second.launch()));
      if (train.speed() > ceiling + .1) throw new IllegalStateException("Transfer exceeded certified speed");
      boolean clear = train.rows().stream().allMatch(r -> second.track().matches(r.contact().hit())
          && second.track().distance(r.contact().hit()) > r.radius() + 1);
      if (clear) { controller.finishTransfer(target); controller.consumeRequest(); return true; }
      if (train.rows().stream().anyMatch(r -> second.track().matches(r.contact().hit()) && second.track().distance(r.contact().hit()) >= second.brakeStart() - .6))
        controller.state(State.TRANSFER_BRAKING);
      controller.consumeRequest(); return true;
    } catch (IllegalArgumentException | IllegalStateException failure) { failPair(controller, target, failure.getMessage()); controller.consumeRequest(); return true; }
  }
  public static boolean departure(ServerLevel level, RideControllerEntity controller, DirectedTrack track, boolean held) {
    if (!held || controller.modules().stream().noneMatch(RideControllerEntity.Module::exitDrive)) return false;
    var target = destination(level, controller); if (target == null) return false;
    try {
      var first = section(level, controller, track); var second = section(level, target, target.resolve(level)); var train = train(level, controller, first, second);
      boolean atExit = train.rows().stream().allMatch(r -> first.track().matches(r.contact().hit()) && first.contact(first.exit(), r.distance()));
      if (!atExit) return false;
      prepare(level, controller, target, first, second, train); return true;
    } catch (IllegalArgumentException | IllegalStateException blocked) {
      controller.state(State.HOLD); controller.consumeRequest(); return true;
    }
  }
  private static void prepare(ServerLevel level, RideControllerEntity source, RideControllerEntity target, Section first, Section second, Train train) {
    boolean ready = train.held(level) && train.releasable() && first.powered(first.exit()) && first.releasePowered(true)
        && second.powered(second.launch()) && second.releasePowered(false) && certificate(level, first, second, train)
        && (target.reservation().isEmpty() || paired(source, target)) && !occupied(level, second, source.reservation());
    if (!ready) { source.state(State.HOLD); source.consumeRequest(); return; }
    source.state(State.READY_EXIT);
    if (!source.consumeRequest()) return;
    // Claim both sections before any provider release or pad retraction.
    source.beginTransfer(target);
    if (!train.released() && !MotionProviders.requestDispatch(level, source.reservation().iterator().next())) {
      failPair(source, target, "Vehicle provider rejected transfer dispatch"); return;
    }
    var released = train(level, source, first, second);
    if (!released.released()) failPair(source, target, "Vehicle provider did not grant transfer motion authority");
  }
  private static Vector3d bearing(ServerSubLevel body) {
    return new Vector3d(body.getPlot().getCenterBlock().getX() + .5, body.getPlot().getCenterBlock().getY() + .5,
        body.getPlot().getCenterBlock().getZ() + .5).sub(body.getMassTracker().getCenterOfMass()).rotate(body.logicalPose().orientation()).add(body.logicalPose().position());
  }
  private static double radius(ServerLevel level, MotionVehicle motion) {
    var container = SubLevelContainer.getContainer(level); var origin = bearing((ServerSubLevel) container.getSubLevel(motion.chassis())); double radius = .6;
    for (var id : motion.bodies()) {
      var body = (ServerSubLevel) container.getSubLevel(id); var bounds = body.getPlot().getBoundingBox();
      for (double x : new double[] {bounds.minX(), bounds.maxX() + 1}) for (double y : new double[] {bounds.minY(), bounds.maxY() + 1}) for (double z : new double[] {bounds.minZ(), bounds.maxZ() + 1}) {
        var point = new Vector3d(x, y, z).sub(body.getMassTracker().getCenterOfMass()).rotate(body.logicalPose().orientation()).add(body.logicalPose().position()); radius = Math.max(radius, point.distance(origin));
      }
    }
    return radius;
  }
}
