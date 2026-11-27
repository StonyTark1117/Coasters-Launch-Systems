package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.math.*;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Native carriage/catch mechanics shared by stored-energy cable launchers.
 * This object owns transient constraint handles, never a train pose or carrier angle. */
public final class NativeCableRig implements AutoCloseable {
  public enum Phase { HOME, ENGAGED, PULLING, RELEASING, RETURNING, FAULT }
  public record Step(double force, double work, double storeDraw, double heat, boolean returning) {}
  private final ServerLevel level;
  private final BlockPos cabinetPosition;
  private final Mechanism mechanism;
  private final DirectedTrack track;
  private final PhysicsPipeline pipeline;
  private final ServerSubLevel carriage;
  private final Vector3d point, home, direction;
  private final double homeDistance, releaseTravel, guideTravel;
  private final GenericConstraintHandle guide;
  private final NativeFinClamp holding;
  private GenericConstraintHandle catchJoint;
  private ServerSubLevel caught;
  private Set<UUID> consist = Set.of();
  private Set<UUID> loadedBodies = Set.of();
  private Set<UUID> ticketedBodies = Set.of();
  private String provider = "";
  private double loadedMass, maximumForce, previousAcceleration;
  private Phase phase = Phase.HOME;
  private String fault = "";

  public NativeCableRig(ServerLevel level, DirectedTrack track, double homeDistance, double releaseTravel, double guideTravel) {
    this(level, track, homeDistance, releaseTravel, guideTravel, null, Mechanism.FLYWHEEL);
  }
  public NativeCableRig(ServerLevel level, DirectedTrack track, double homeDistance, double releaseTravel, double guideTravel,
      BlockPos cabinetPosition) {
    this(level, track, homeDistance, releaseTravel, guideTravel, cabinetPosition, Mechanism.FLYWHEEL);
  }
  public NativeCableRig(ServerLevel level, DirectedTrack track, double homeDistance, double releaseTravel, double guideTravel,
      BlockPos cabinetPosition, Mechanism mechanism) {
    this.level = level; this.track = track; this.homeDistance = Numbers.nonnegative(homeDistance, "guide home");
    this.cabinetPosition = cabinetPosition == null ? null : cabinetPosition.immutable();
    this.mechanism = Objects.requireNonNull(mechanism, "cable mechanism");
    if (!mechanism.cable()) throw new IllegalArgumentException("Native carriage requires a cable mechanism");
    this.releaseTravel = Numbers.positive(releaseTravel, "release travel"); this.guideTravel = Numbers.positive(guideTravel, "guide travel");
    if (releaseTravel + 4 > guideTravel || homeDistance + guideTravel > track.length())
      throw new IllegalArgumentException("Leave four supported carriage braking blocks after cable release");
    var hit = track.hitAtDistance(level, homeDistance); var forward = track.forward(level, hit);
    direction = new Vector3d(forward.x, forward.y, forward.z);
    for (int i = 0; i <= 64; i++) {
      var sample = track.hitAtDistance(level, homeDistance + guideTravel * i / 64);
      var tangent = track.forward(level, sample); var basis = CoasterPathTrackFrame.trackBasisAtHit(level, sample);
      if (Math.abs(tangent.y) > .001 || tangent.dot(forward) < .999999 || basis.up().y < .999999)
        throw new IllegalArgumentException("Cable guide requires fixed straight level unbanked rail");
      var bed = guideBlockAt(level, track, homeDistance + guideTravel * i / 64);
      if (!level.hasChunkAt(bed) || !level.getBlockState(bed).is(dev.coasterslaunchsystems.content.EquipmentContent.CABLE_GUIDE.get()))
        throw new IllegalArgumentException("Cable guide bed must be placed continuously beneath its full travel");
    }
    // Accepted native bearing centres use the rail's 5/16-block track-up offset,
    // rather than the half-block plot-origin placement offset.
    var up = CoasterPathTrackFrame.trackBasisAtHit(level, hit).up();
    var socket = hit.point().add(up.scale(.3125 - 2));
    home = new Vector3d(socket.x, socket.y, socket.z);
    var container = Objects.requireNonNull(SubLevelContainer.getContainer(level), "Native container unavailable");
    pipeline = container.physicsSystem().getPipeline();
    var pose = new Pose3d(); pose.position().set(home);
    carriage = (ServerSubLevel) container.allocateNewSubLevel(pose);
    var plot = carriage.getPlot(); plot.newEmptyChunk(plot.getCenterChunk());
    var embedded = plot.getEmbeddedLevelAccessor();
    if (!embedded.setBlock(BlockPos.ZERO, dev.coasterslaunchsystems.content.EquipmentContent.CATCH_CARRIAGE.get().defaultBlockState(), 3))
      throw new IllegalStateException("Catch carriage body did not install");
    // A real, symmetric steel carriage mass, separate from the train and its seat carriers.
    for (var cell : List.of(BlockPos.ZERO.below(), BlockPos.ZERO.below(2)))
      if (!embedded.setBlock(cell, net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(), 3)) throw new IllegalStateException("Carriage spine did not install");
    for (var cell : List.of(BlockPos.ZERO.east().below(2), BlockPos.ZERO.west().below(2), BlockPos.ZERO.north().below(2), BlockPos.ZERO.south().below(2)))
      if (!embedded.setBlock(cell, net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(), 3)) throw new IllegalStateException("Carriage ballast did not install");
    var center = plot.getCenterBlock().getCenter(); point = new Vector3d(center.x, center.y, center.z);
    // Rapier locates this body at its mass centre. Keep Sable's pose pivot there
    // too, while placing the physical catch point at the rail-aligned home.
    var centerOfMass = carriage.getMassTracker().getCenterOfMass();
    carriage.logicalPose().rotationPoint().set(centerOfMass);
    carriage.logicalPose().position().set(new Vector3d(home).sub(new Vector3d(point).sub(centerOfMass)));
    carriage.updateLastPose(); pipeline.onStatsChanged(carriage);
    var frame = new Quaterniond().rotationTo(new Vector3d(1, 0, 0), direction);
    guide = pipeline.addConstraint(null, carriage, new GenericConstraintConfiguration(home, point, frame, frame,
        EnumSet.of(ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z, ConstraintJointAxis.ANGULAR_X,
            ConstraintJointAxis.ANGULAR_Y, ConstraintJointAxis.ANGULAR_Z)));
    if (guide == null || !guide.isValid()) throw new IllegalStateException("Native cable guide unavailable");
    guide.setLimit(ConstraintJointAxis.LINEAR_X, 0, guideTravel);
    holding = new NativeFinClamp(pipeline, carriage, point, direction, 256);
    boolean ticketed = cabinetPosition == null
        ? container.addForceLoadTicket(carriage, CableTickets.LEGACY_CARRIAGE, carriage.getUniqueId())
        : container.addForceLoadTicket(carriage, CableTickets.CARRIAGE,
            new CableTickets.Key(carriage.getUniqueId(), cabinetPosition));
    if (!ticketed) {
      holding.close(); guide.remove();
      container.removeSubLevel(carriage, SubLevelRemovalReason.REMOVED);
      throw new IllegalStateException("Native cable carriage loading ticket was not installed");
    }
  }
  public Phase phase() { return phase; }
  public Mechanism mechanism() { return mechanism; }
  /** Provider identity remains available through release and physical return for restart journaling. */
  public String provider() { return provider; }
  public double homeDistance() { return homeDistance; }
  /** Physical socket station, independent of an outboard/longitudinal chassis mounting arm. */
  public static double socketDistance(ServerLevel level, DirectedTrack track, ServerSubLevel cart) {
    var socket = VehicleInterfaces.catchPoint(cart);
    if (socket == null) throw new IllegalArgumentException("Cable cart has no certified socket");
    return pointDistance(level, track, cart, socket);
  }
  /** Project a real installed interface onto the directed, fixed guide rail. */
  public static double pointDistance(ServerLevel level, DirectedTrack track, ServerSubLevel cart, org.joml.Vector3dc point) {
    var contact = EngagedContacts.current(level, cart).orElseThrow(() -> new IllegalArgumentException("Cable cart lost rail contact"));
    if (!track.matches(contact.hit())) throw new IllegalArgumentException("Cable interface is on another rail");
    var direction = track.forward(level, contact.hit());
    var hit = contact.hit().point();
    var offset = worldPoint(cart, new Vector3d(point)).sub(hit.x, hit.y, hit.z);
    return Numbers.finite(track.distance(contact.hit()) + offset.dot(direction.x, direction.y, direction.z), "interface station");
  }
  /** Bed top stays below the carriage's lowest steel ballast block. */
  public static BlockPos guideBlockAt(ServerLevel level, DirectedTrack track, double distance) {
    var hit = track.hitAtDistance(level, distance);
    var up = CoasterPathTrackFrame.trackBasisAtHit(level, hit).up();
    return BlockPos.containing(hit.point().add(up.scale(.3125 - 5)));
  }
  public String fault() { return fault; }
  public ServerSubLevel carriage() { return carriage; }
  public Vector3d carriagePoint() { return worldPoint(carriage, point); }
  public Set<UUID> consist() { return consist; }
  public double travel() { return worldPoint(carriage, point).sub(home).dot(direction); }
  public double speed() { return NativeForces.pointDynamics(carriage, pipeline, point, direction).speed(); }
  public double socketError() { return caught == null ? 0 : worldPoint(caught, VehicleInterfaces.catchPoint(caught)).distance(worldPoint(carriage, point)); }
  public double guideError() { var offset = worldPoint(carriage, point).sub(home); return offset.sub(new Vector3d(direction).mul(offset.dot(direction))).length(); }
  public double catchForce(double dt) {
    if (catchJoint == null || !catchJoint.isValid()) return 0;
    var linear = new Vector3d(); catchJoint.getJointImpulses(linear, new Vector3d()); return linear.length() / Numbers.positive(dt, "dt");
  }
  public void engage(UUID chassis) {
    validateGuide();
    if (phase != Phase.HOME || Math.abs(travel()) > .03 || Math.abs(speed()) >= .03) throw new IllegalStateException("Carriage is not physically home and held: travel=" + travel() + " speed=" + speed());
    var motion = MotionProviders.resolve(level, chassis).orElseThrow(() -> new IllegalStateException("Cable vehicle provider unavailable"));
    var container = SubLevelContainer.getContainer(level); var bodies = new HashSet<UUID>(); double mass = 0, force = 64;
    for (var id : motion.consist()) {
      var member = MotionProviders.resolve(level, id).orElseThrow();
      var cart = container.getSubLevel(id) instanceof ServerSubLevel loaded ? loaded : null;
      var contact = cart == null ? null : EngagedContacts.current(level, cart).orElse(null);
      if (contact == null || !track.matches(contact.hit()) || !member.provider().equals(motion.provider())
          || !Set.copyOf(member.consist()).equals(Set.copyOf(motion.consist())) || !member.dispatchReady()
          || !member.exclusiveOperator().isEmpty() || !member.interfaces().contains(mechanism)
          || ChainLiftSpanQuery.findAtGraphHit(level, contact.hit(), 0) != null)
        throw new IllegalStateException("Cable consist is not ready, supported, certified and exclusively available");
      if (pipeline.getLinearVelocity(cart, new Vector3d()).length() >= .03 || pipeline.getAngularVelocity(cart, new Vector3d()).length() >= .03)
        throw new IllegalStateException("Hold the whole native consist before engaging its catch");
      for (var body : member.bodies()) if (!bodies.add(body)) throw new IllegalStateException("Duplicate articulated cable body");
      double loaded = NativeForces.mass(level, member); if (loaded <= 0) throw new IllegalStateException("Cable loaded mass unavailable");
      mass += loaded; force = Math.min(force, Math.min(member.maximumForce(), loaded * member.maximumAcceleration()));
    }
    if (mass > 64) throw new IllegalStateException("Cable consist exceeds its 64-unit rating");
    var cart = (ServerSubLevel) container.getSubLevel(chassis); var socket = VehicleInterfaces.catchPoint(cart);
    if (socket == null || worldPoint(cart, socket).distance(worldPoint(carriage, point)) > .03)
      throw new IllegalStateException("Catch socket is not aligned with the physical carriage");
    catchJoint = attachCatch(pipeline, carriage, cart, point, socket, direction);
    if (cabinetPosition != null) {
      var ticketed = new HashSet<UUID>();
      var key = new CableTickets.Key(carriage.getUniqueId(), cabinetPosition);
      try {
        for (var id : bodies) {
          if (!(container.getSubLevel(id) instanceof ServerSubLevel body)
              || !container.addForceLoadTicket(body, CableTickets.CAPTURED_BODY, key))
            throw new IllegalStateException("Captured native body loading ticket was not installed");
          ticketed.add(id);
        }
      } catch (RuntimeException failure) {
        for (var id : ticketed)
          if (container.getSubLevel(id) instanceof ServerSubLevel body)
            container.removeForceLoadTicket(body, CableTickets.CAPTURED_BODY, key);
        catchJoint.remove(); catchJoint = null;
        throw failure;
      }
      ticketedBodies = Set.copyOf(ticketed);
    }
    caught = cart; consist = Set.copyOf(motion.consist()); loadedBodies = Set.copyOf(bodies); provider = motion.provider(); loadedMass = mass; maximumForce = force; phase = Phase.ENGAGED;
  }
  public void beginPull() {
    if (phase != Phase.ENGAGED) throw new IllegalStateException("Catch is not held and engaged");
    try {
      validateGuide();
      if (catchJoint == null || !catchJoint.isValid() || socketError() > .03 || Math.abs(speed()) >= .03)
        throw new IllegalStateException("Physical catch is no longer held and aligned");
      for (var id : consist) {
        var member = MotionProviders.resolve(level, id).orElseThrow();
        var cart = SubLevelContainer.getContainer(level).getSubLevel(id) instanceof ServerSubLevel body ? body : null;
        var contact = cart == null ? null : EngagedContacts.current(level, cart).orElse(null);
        if (!member.dispatchReady() || !member.exclusiveOperator().isEmpty() || !member.provider().equals(provider)
            || !Set.copyOf(member.consist()).equals(consist) || !member.interfaces().contains(mechanism)
            || contact == null || !track.matches(contact.hit()) || ChainLiftSpanQuery.findAtGraphHit(level, contact.hit(), 0) != null
            || pipeline.getLinearVelocity(cart, new Vector3d()).length() >= .03 || pipeline.getAngularVelocity(cart, new Vector3d()).length() >= .03)
          throw new IllegalStateException("Whole cable consist is no longer dispatch ready and held");
      }
      if (!MotionProviders.requestDispatch(level, caught.getUniqueId())) throw new IllegalStateException("Provider rejected catch dispatch");
      validateReleased(); holding.setForce(0); previousAcceleration = 0; phase = Phase.PULLING;
    } catch (RuntimeException failure) { emergencyHold(failure.getMessage()); throw failure; }
  }
  private void validateReleased() {
    var bodies = new HashSet<UUID>(); double mass = 0, force = 64;
    for (var id : consist) {
      var motion = MotionProviders.resolve(level, id).orElseThrow();
      if (!motion.propulsionAllowed() || !motion.brakingAllowed() || !motion.exclusiveOperator().isEmpty()
          || !motion.provider().equals(provider) || !motion.interfaces().contains(mechanism) || !Set.copyOf(motion.consist()).equals(consist))
        throw new IllegalStateException("Cable vehicle owner withdrew motion authority");
      var cart = SubLevelContainer.getContainer(level).getSubLevel(id) instanceof ServerSubLevel body ? body : null;
      var contact = cart == null ? null : EngagedContacts.current(level, cart).orElse(null);
      if (contact == null || !track.matches(contact.hit()) || ChainLiftSpanQuery.findAtGraphHit(level, contact.hit(), 0) != null)
        throw new IllegalStateException("Cable consist lost native support or met a chain");
      for (var body : motion.bodies()) if (!bodies.add(body)) throw new IllegalStateException("Duplicate articulated cable body");
      double loaded = NativeForces.mass(level, motion); if (loaded <= 0) throw new IllegalStateException("Cable loaded mass unavailable");
      mass += loaded; force = Math.min(force, Math.min(motion.maximumForce(), loaded * motion.maximumAcceleration()));
    }
    if (!bodies.equals(loadedBodies) || mass > 64) throw new IllegalStateException("Cable articulated membership changed or exceeded its rating");
    loadedMass = mass; maximumForce = force;
  }
  private void validateGuide() {
    if (carriage.isRemoved() || !guide.isValid() || !holding.valid()) throw new IllegalStateException("Cable carriage/guide is unavailable");
    var fresh = DirectedTrack.resolve(level, track.edge().from(), track.edge().to()).orElseThrow(() -> new IllegalStateException("Cable rail is unavailable"));
    if (fresh.fingerprint() != track.fingerprint()) throw new IllegalStateException("Cable guide geometry changed");
    for (int i = 0; i <= 64; i++) {
      var bed = guideBlockAt(level, track, homeDistance + guideTravel * i / 64);
      if (!level.hasChunkAt(bed) || !level.getBlockState(bed).is(dev.coasterslaunchsystems.content.EquipmentContent.CABLE_GUIDE.get()))
        throw new IllegalStateException("Cable guide bed is unavailable or broken");
    }
  }
  /** A bounded motor force acts only on the carriage. The native catch pulls the train. */
  public Step step(EnergyStore store, double target, double shaftWork, double dt) {
    Numbers.positive(target, "cable target"); Numbers.nonnegative(shaftWork, "return shaft budget"); Numbers.positive(dt, "dt");
    if (phase == Phase.FAULT) return new Step(0, 0, 0, 0, false);
    try { validateGuide(); return checkedStep(store, target, shaftWork, dt); }
    catch (RuntimeException failure) { emergencyHold(failure.getMessage()); return new Step(0, 0, 0, 0, false); }
  }
  private Step checkedStep(EnergyStore store, double target, double shaftWork, double dt) {
    if (phase == Phase.PULLING) {
      validateReleased();
      if (catchJoint == null || !catchJoint.isValid() || socketError() > .1 || guideError() > .03 || travel() < -.03)
        throw new IllegalStateException("Native catch/guide alignment failed");
      if (travel() >= releaseTravel) {
        catchJoint.remove(); catchJoint = null; caught = null; previousAcceleration = 0;
        releaseCapturedTickets();
        phase = Phase.RELEASING; holding.setForce(256); return new Step(0, 0, 0, 0, false);
      }
      double mass = loadedMass + NativeForces.mass(level, List.of(carriage.getUniqueId()));
      double driveSpeed = store instanceof FlywheelStore flywheel ? Math.min(target, flywheel.rimSpeed(.2)) : target;
      // The drum/clutch follows finite flywheel rim speed; an empty flywheel cannot synthesize a field speed.
      double requested = Math.tanh((driveSpeed - speed()) * 2) * Math.min(maximumForce, store.outputForce(travel()));
      double acceleration = DriveMath.tireAccelerationStep(previousAcceleration, requested / mass, 4, dt);
      var dynamics = NativeForces.pointDynamics(carriage, pipeline, point, direction);
      double desired = acceleration * mass;
      double affordable = DriveMath.affordableForce(desired, dynamics.speed(), dynamics.effectiveMass(), store.energy() * .85, dt);
      double work = DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(), affordable, dt);
      double draw = Math.max(0, work) / .85;
      if (draw > store.energy() + 1e-8) throw new IllegalStateException("Cable work exceeds its energy store");
      store.draw(draw); previousAcceleration = affordable / mass;
      NativeForces.pointForce(carriage, point, new Vector3d(direction).mul(affordable), dt);
      return new Step(affordable, work, draw, draw - work, false);
    }
    if (phase == Phase.RELEASING) {
      if (Math.abs(speed()) < .03) { holding.setForce(0); phase = Phase.RETURNING; }
      return new Step(0, 0, 0, 0, false);
    }
    if (phase == Phase.RETURNING) {
      if (Math.abs(travel()) < .03 && Math.abs(speed()) < .03) { holding.setForce(256); phase = Phase.HOME; consist = Set.of(); loadedBodies = Set.of(); provider = ""; return new Step(0, 0, 0, 0, true); }
      var dynamics = NativeForces.pointDynamics(carriage, pipeline, point, direction);
      // Slow physical return, braked by the guide's home limit; no reset teleport/velocity write.
      double force = DriveMath.affordableForce(DriveMath.lsmForce(dynamics.speed(), -1, 16), dynamics.speed(), dynamics.effectiveMass(), shaftWork, dt);
      double work = DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(), force, dt);
      NativeForces.pointForce(carriage, point, new Vector3d(direction).mul(force), dt);
      return new Step(force, work, 0, Math.max(0, -work), true);
    }
    return new Step(0, 0, 0, 0, false);
  }
  public void emergencyHold(String reason) {
    fault = reason == null || reason.isBlank() ? "Cable operation needs inspection" : reason;
    phase = Phase.FAULT; if (holding.valid()) holding.setForce(256); previousAcceleration = 0;
  }
  /** A stationary, bounded native coupling; dispatch authority remains with the owner. */
  static GenericConstraintHandle attachCatch(PhysicsPipeline pipeline, ServerSubLevel carriage, ServerSubLevel chassis,
      Vector3d carriagePoint, Vector3d socket, Vector3d direction) {
    var frame = new Quaterniond().rotationTo(new Vector3d(1, 0, 0), direction);
    var joint = pipeline.addConstraint(carriage, chassis, new GenericConstraintConfiguration(carriagePoint, socket, frame,
        new Quaterniond(chassis.logicalPose().orientation()).invert().mul(frame)));
    if (joint == null || !joint.isValid()) throw new IllegalStateException("Native catch unavailable");
    joint.setContactsEnabled(false);
    joint.setMotor(ConstraintJointAxis.LINEAR_X, 0, 100_000, 10_000, true, 256);
    return joint;
  }
  private void releaseCapturedTickets() {
    var container = SubLevelContainer.getContainer(level);
    if (container == null || cabinetPosition == null || ticketedBodies.isEmpty()) return;
    var key = new CableTickets.Key(carriage.getUniqueId(), cabinetPosition);
    for (var id : ticketedBodies)
      if (container.getSubLevel(id) instanceof ServerSubLevel body)
        container.removeForceLoadTicket(body, CableTickets.CAPTURED_BODY, key);
    ticketedBodies = Set.of();
  }
  public static Vector3d worldPoint(ServerSubLevel body, Vector3d plotPoint) {
    if (plotPoint == null) throw new IllegalStateException("Native interface removed");
    return new Vector3d(plotPoint).sub(body.getMassTracker().getCenterOfMass()).rotate(body.logicalPose().orientation()).add(body.logicalPose().position());
  }
  @Override public void close() {
    if (catchJoint != null && catchJoint.isValid()) catchJoint.remove();
    releaseCapturedTickets();
    holding.close(); if (guide.isValid()) guide.remove();
    var container = SubLevelContainer.getContainer(level);
    if (container != null) {
      if (cabinetPosition == null) container.removeForceLoadTicket(carriage, CableTickets.LEGACY_CARRIAGE, carriage.getUniqueId());
      else container.removeForceLoadTicket(carriage, CableTickets.CARRIAGE,
          new CableTickets.Key(carriage.getUniqueId(), cabinetPosition));
      if (!carriage.isRemoved()) container.removeSubLevel(carriage, SubLevelRemovalReason.REMOVED);
    }
  }
}
