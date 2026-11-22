package dev.coasterslaunchsystems.runtime;

import com.mojang.logging.LogUtils;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.LinearMotor;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import dev.coasterslaunchsystems.track.EngagedContacts;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Restores native holds for saved cable carriages without granting dispatch authority. */
public final class CableRecovery {
  private static final class Hold {
    private final ServerSubLevel carriage;
    private final GenericConstraintHandle guide;
    private final NativeFinClamp brake;
    private final Vector3d home, point, direction;
    private final boolean movingAtRecovery;
    private GenericConstraintHandle catchJoint;
    private UUID caught;
    private boolean returnAuthorized;
    private Hold(ServerSubLevel carriage, GenericConstraintHandle guide, NativeFinClamp brake, Vector3d home, Vector3d point,
        Vector3d direction, boolean movingAtRecovery) {
      this.carriage = carriage; this.guide = guide; this.brake = brake; this.home = home; this.point = point;
      this.direction = direction;
      this.movingAtRecovery = movingAtRecovery;
    }
    ServerSubLevel carriage() { return carriage; }
    Vector3d home() { return home; }
    Vector3d point() { return point; }
    boolean movingAtRecovery() { return movingAtRecovery; }
    void close() { if (catchJoint != null && catchJoint.isValid()) catchJoint.remove(); brake.close(); if (guide.isValid()) guide.remove(); }
    boolean valid() { return !carriage.isRemoved() && guide.isValid() && brake.valid(); }
    boolean caught(UUID chassis) { return chassis != null && chassis.equals(caught) && catchJoint != null && catchJoint.isValid(); }
    void attach(UUID chassis, GenericConstraintHandle joint) {
      if (catchJoint != null && catchJoint.isValid()) catchJoint.remove();
      caught = chassis; catchJoint = joint;
    }
    void releaseCatch(UUID chassis) {
      if (!caught(chassis)) throw new IllegalStateException("Saved native catch joint changed during inspection");
      catchJoint.remove(); catchJoint = null; caught = null;
    }
    void authorizeReturn() { returnAuthorized = true; }
    void stepReturning(ServerLevel level, EquipmentCabinetEntity cabinet, double dt) {
      if (!returnAuthorized || !cabinet.cableReturning()) return;
      var container = SubLevelContainer.getContainer(level);
      if (container == null || !valid()) return;
      var pipeline = container.physicsSystem().getPipeline();
      if (!DriveEquipment.forwardPowered(cabinet)) { brake.setForce(256); return; }
      var dynamics = NativeForces.pointDynamics(carriage, pipeline, point, direction);
      var actual = NativeCableRig.worldPoint(carriage, point);
      double travel = new Vector3d(actual).sub(home).dot(direction);
      if (Math.abs(travel) < .03 && Math.abs(dynamics.speed()) < .03) {
        brake.setForce(256); returnAuthorized = false; cabinet.finishRecoveredCableReturn(carriage.getUniqueId()); return;
      }
      brake.setForce(0);
      double shaft = LinearMotor.chargingWork(cabinet.getSpeed(), dt);
      double force = DriveMath.affordableForce(DriveMath.lsmForce(dynamics.speed(), -1, 16), dynamics.speed(),
          dynamics.effectiveMass(), shaft, dt);
      NativeForces.pointForce(carriage, point, new Vector3d(direction).mul(force), dt);
    }
  }
  private static final Map<ServerLevel, Map<UUID, Hold>> HOLDS = new HashMap<>();
  private static final Map<ServerLevel, Set<UUID>> WARNED = new HashMap<>();
  private CableRecovery() {}

  public static void stabilize(ServerLevel level, Collection<EquipmentCabinetEntity> cabinets, double dt) {
    var retained = HOLDS.computeIfAbsent(level, ignored -> new HashMap<UUID, Hold>());
    retained.entrySet().removeIf(entry -> {
      if (entry.getValue().valid()) return false;
      entry.getValue().close(); return true;
    });
    var candidates = new LinkedHashSet<>(cabinets);
    var container = SubLevelContainer.getContainer(level);
    if (container != null) for (var info : container.getAllTickets().values()) for (var ticket : info.tickets()) {
      if (CableTickets.typeOf(ticket) != CableTickets.CARRIAGE || !(CableTickets.keyOf(ticket) instanceof CableTickets.Key key)
          || CableControllers.hasLiveCarriage(level, key.carriage())
          || Math.abs((long) key.cabinet().getX()) > 1024 || Math.abs((long) key.cabinet().getZ()) > 1024) continue;
      // A force-loaded native carriage must bring its owning cabinet into memory before the first physics step.
      level.getChunkAt(key.cabinet());
      if (level.getBlockEntity(key.cabinet()) instanceof EquipmentCabinetEntity cabinet) candidates.add(cabinet);
    }
    for (var cabinet : candidates) {
      if (cabinet.isRemoved() || !cabinet.mechanism().cable() || !cabinet.cableInstalled()
          || cabinet.cableCarriage() == null || cabinet.fault().isEmpty()
          || CableControllers.hasLiveCarriage(level, cabinet.cableCarriage())) continue;
      try {
        var hold = retained.get(cabinet.cableCarriage());
        if (hold == null) {
          hold = restorePassive(level, cabinet);
          if (hold != null) retained.put(cabinet.cableCarriage(), hold);
        }
        if (hold != null) {
          if (cabinet.cableReturning()) {
            hold.stepReturning(level, cabinet, dt);
            WARNED.computeIfAbsent(level, ignored -> new HashSet<>()).remove(cabinet.cableCarriage());
            continue;
          }
          if (cabinet.cableInUse() && !hold.caught(cabinet.cableChassis())) {
            try { restoreCatch(level, cabinet, hold); }
            catch (RuntimeException failure) {
              if (WARNED.computeIfAbsent(level, ignored -> new HashSet<>()).add(cabinet.cableCarriage()))
                LogUtils.getLogger().warn("Cable carriage {} is passively held; active catch joint is pending inspection: {}",
                    cabinet.cableCarriage(), failure.getMessage());
              continue;
            }
          }
          if (cabinet.cableMoving() && hold.caught(cabinet.cableChassis()) && captureSettled(level, cabinet, hold))
            cabinet.finishRecoveredCableMotion(cabinet.cableCarriage(), cabinet.cableChassis());
          WARNED.computeIfAbsent(level, ignored -> new HashSet<>()).remove(cabinet.cableCarriage());
        }
      } catch (RuntimeException failure) {
        // A mismatched carriage is never pulled toward the guide or made operable.
        if (WARNED.computeIfAbsent(level, ignored -> new HashSet<>()).add(cabinet.cableCarriage()))
          LogUtils.getLogger().warn("Cable carriage {} remains unrestrained pending inspection: {}",
              cabinet.cableCarriage(), failure.getMessage());
      }
    }
  }

  public static boolean stabilized(ServerLevel level, UUID carriage) {
    var hold = HOLDS.getOrDefault(level, Map.of()).get(carriage);
    return hold != null && hold.valid();
  }
  public static boolean catchRestored(ServerLevel level, UUID carriage, UUID chassis) {
    var hold = HOLDS.getOrDefault(level, Map.of()).get(carriage);
    return hold != null && hold.valid() && hold.caught(chassis);
  }

  /** An operator may resume only a released, passively stabilized return using the cabinet's live shaft. */
  public static void inspectResumeReturn(ServerLevel level, EquipmentCabinetEntity cabinet) {
    if (!cabinet.mechanism().cable() || !cabinet.cableInstalled() || !cabinet.cableInUse() || !cabinet.cableReturning()
        || cabinet.armed() || cabinet.fault().isEmpty() || cabinet.cableCarriage() == null || cabinet.cableChassis() != null)
      throw new IllegalArgumentException("Only a disarmed, reloaded released carriage can resume its physical return");
    if (!OrdinaryMotionProvider.ID.equals(cabinet.cableReturnProvider()))
      throw new IllegalArgumentException("This provider's released-return recovery remains passively held pending a verified home-guide adapter");
    if (!DriveEquipment.forwardPowered(cabinet))
      throw new IllegalArgumentException("Restore the cabinet charging shaft before resuming its return");
    var hold = HOLDS.getOrDefault(level, Map.of()).get(cabinet.cableCarriage());
    if (hold == null || !hold.valid())
      throw new IllegalArgumentException("Saved released carriage has no verified passive guide hold");
    var track = cabinet.resolve(level);
    if (track == null || cabinet.cableHome() + 20 > track.length())
      throw new IllegalArgumentException("Saved return guide no longer matches the cabinet");
    hold.authorizeReturn();
  }

  /** Explicitly retire a reloaded idle installation only after its entire physical ownership is checked. */
  public static void inspectRetireIdle(ServerLevel level, EquipmentCabinetEntity cabinet) {
    if (!cabinet.mechanism().cable() || !cabinet.cableInstalled() || cabinet.cableInUse()
        || cabinet.armed() || cabinet.fault().isEmpty() || cabinet.cableCarriage() == null)
      throw new IllegalArgumentException("Only a disarmed, reloaded idle cable carriage can be inspected for retirement");
    var id = cabinet.cableCarriage();
    if (CableControllers.hasLiveCarriage(level, id)) throw new IllegalArgumentException("Live cable catch requires physical return");
    var hold = HOLDS.getOrDefault(level, Map.of()).get(id);
    if (hold == null || !hold.valid()) throw new IllegalArgumentException("Saved carriage has no verified passive native hold");
    if (cabinet.controllerIdentity() != null) {
      if (cabinet.controllerPosition() == null || !level.hasChunkAt(cabinet.controllerPosition())
          || !(level.getBlockEntity(cabinet.controllerPosition()) instanceof RideControllerEntity controller)
          || !cabinet.controllerIdentity().equals(controller.identity()) || controller.armed()
          || !controller.reservation().isEmpty() || controller.transferring() || controller.incoming() != null
          || controller.modules().stream().noneMatch(m -> m.identity().equals(cabinet.identity())
              && m.position().equals(cabinet.getBlockPos())))
        throw new IllegalArgumentException("Linked cable controller is unavailable, armed or still owns a train");
    }
    var track = cabinet.resolve(level);
    if (track == null || cabinet.cableHome() + 20 > track.length())
      throw new IllegalArgumentException("Saved fixed guide rail no longer matches the cabinet");
    for (int i = 0; i <= 64; i++) {
      var bed = NativeCableRig.guideBlockAt(level, track, cabinet.cableHome() + 20.0 * i / 64);
      if (!level.hasChunkAt(bed) || !level.getBlockState(bed).is(EquipmentContent.CABLE_GUIDE.get()))
        throw new IllegalArgumentException("Cable guide bed needs repair before carriage retirement");
    }
    var container = SubLevelContainer.getContainer(level);
    if (container == null || container.getSubLevel(id) != hold.carriage())
      throw new IllegalArgumentException("The saved native carriage identity changed");
    if (!carriageIntact(hold.carriage()))
      throw new IllegalArgumentException("Native carriage hardware changed after its passive hold was restored");
    var info = container.getAllTickets().get(id);
    var key = new CableTickets.Key(id, cabinet.getBlockPos());
    boolean modern = info != null && info.tickets().stream().anyMatch(t -> CableTickets.typeOf(t) == CableTickets.CARRIAGE && key.equals(CableTickets.keyOf(t)));
    boolean legacy = info != null && info.tickets().stream().anyMatch(t -> CableTickets.typeOf(t) == CableTickets.LEGACY_CARRIAGE && id.equals(CableTickets.keyOf(t)));
    if (info == null || info.tickets().size() != 1 || modern == legacy)
      throw new IllegalArgumentException("Native carriage loading ticket is missing, foreign or ambiguous");
    var pipeline = container.physicsSystem().getPipeline();
    var actual = NativeCableRig.worldPoint(hold.carriage(), hold.point());
    double linear = pipeline.getLinearVelocity(hold.carriage(), new Vector3d()).length();
    double angular = pipeline.getAngularVelocity(hold.carriage(), new Vector3d()).length();
    if (actual.distance(hold.home()) >= .03 || linear >= .03 || angular >= .03)
      throw new IllegalArgumentException("Native carriage has not settled at its saved home");
    for (var body : container.getAllSubLevels()) {
      if (body == hold.carriage()) continue;
      var contact = EngagedContacts.current(level, body).orElse(null);
      if (contact != null && track.matches(contact.hit()))
        throw new IllegalArgumentException("Clear the entire rail section before retiring its cable carriage");
    }
    boolean removed = modern
        ? container.removeForceLoadTicket(hold.carriage(), CableTickets.CARRIAGE, key)
        : container.removeForceLoadTicket(hold.carriage(), CableTickets.LEGACY_CARRIAGE, id);
    if (!removed) throw new IllegalStateException("Native carriage ticket changed during inspection");
    hold.close();
    container.removeSubLevel(hold.carriage(), SubLevelRemovalReason.REMOVED);
    HOLDS.get(level).remove(id);
    cabinet.finishInspectedCableRetirement(id);
  }

  /**
   * Releases a reloaded occupied catch only after a separate, live friction-fin clamp holds
   * every chassis in the saved consist. This path never arms or dispatches the controller.
   */
  public static void inspectReleaseActive(ServerLevel level, EquipmentCabinetEntity cabinet) {
    if (!cabinet.mechanism().cable() || !cabinet.cableInstalled() || !cabinet.cableInUse()
        || cabinet.armed() || cabinet.fault().isEmpty() || cabinet.cableCarriage() == null || cabinet.cableChassis() == null)
      throw new IllegalArgumentException("Only a disarmed, reloaded active cable catch can be released by inspection");
    var hold = HOLDS.getOrDefault(level, Map.of()).get(cabinet.cableCarriage());
    if (hold == null || !hold.valid() || !hold.caught(cabinet.cableChassis()))
      throw new IllegalArgumentException("Saved active catch has no verified native guide and joint");
    if (hold.home().distance(NativeCableRig.worldPoint(hold.carriage(), hold.point())) >= .03)
      throw new IllegalArgumentException("Return the retained carriage to its physical home before active release");
    if (cabinet.controllerIdentity() == null || cabinet.controllerPosition() == null || !level.hasChunkAt(cabinet.controllerPosition())
        || !(level.getBlockEntity(cabinet.controllerPosition()) instanceof RideControllerEntity controller)
        || !cabinet.controllerIdentity().equals(controller.identity()) || controller.armed())
      throw new IllegalArgumentException("Linked controller is unavailable or armed during active-catch inspection");
    var capture = inspectCapture(level, cabinet, hold, true, false);
    if (!controller.reservation().equals(capture.consist()))
      throw new IllegalArgumentException("Linked controller no longer owns exactly the saved captured consist");
    var linkedModules = controller.modules().stream().map(RideControllerEntity.Module::identity)
        .collect(java.util.stream.Collectors.toSet());
    var pipeline = SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
    for (var chassis : capture.consist()) {
      if (!EquipmentRuntime.activeFinClampFrom(level, chassis, linkedModules))
        throw new IllegalArgumentException("Apply a rated linked friction-fin clamp to every captured chassis before release");
      if (!(SubLevelContainer.getContainer(level).getSubLevel(chassis) instanceof ServerSubLevel body)
          || pipeline.getLinearVelocity(body, new Vector3d()).length() >= .03
          || pipeline.getAngularVelocity(body, new Vector3d()).length() >= .03)
        throw new IllegalArgumentException("Captured train has not settled under its independent fin clamp");
    }
    hold.releaseCatch(cabinet.cableChassis());
    var key = new CableTickets.Key(cabinet.cableCarriage(), cabinet.getBlockPos());
    var container = SubLevelContainer.getContainer(level);
    for (var body : capture.bodies())
      if (container.getSubLevel(body) instanceof ServerSubLevel loaded
          && !container.removeForceLoadTicket(loaded, CableTickets.CAPTURED_BODY, key))
        throw new IllegalStateException("Captured native body ticket changed during inspected release");
    controller.clearVerifiedReservation();
    cabinet.finishInspectedActiveRelease(cabinet.cableCarriage(), cabinet.cableChassis());
  }

  public static void unload(ServerLevel level) { HOLDS.remove(level); WARNED.remove(level); }

  private static Hold restorePassive(ServerLevel level, EquipmentCabinetEntity cabinet) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return null;
    var id = cabinet.cableCarriage();
    var ticket = container.getAllTickets().get(id);
    if (ticket == null || ticket.tickets().stream().noneMatch(entry -> CableTickets.typeOf(entry) == CableTickets.CARRIAGE
        && new CableTickets.Key(id, cabinet.getBlockPos()).equals(CableTickets.keyOf(entry))
        || CableTickets.typeOf(entry) == CableTickets.LEGACY_CARRIAGE && id.equals(CableTickets.keyOf(entry)))) return null;
    if (!(container.getSubLevel(id) instanceof ServerSubLevel carriage) || carriage.isRemoved()) return null;
    var track = cabinet.resolve(level);
    if (track == null || cabinet.cableHome() + 20 > track.length()) return null;
    var hit = track.hitAtDistance(level, cabinet.cableHome());
    var forward = track.forward(level, hit);
    var direction = new Vector3d(forward.x, forward.y, forward.z);
    if (Math.abs(direction.y) > .001) throw new IllegalStateException("Guide rail is no longer level");
    for (int i = 0; i <= 64; i++) {
      var sample = track.hitAtDistance(level, cabinet.cableHome() + 20.0 * i / 64);
      var tangent = track.forward(level, sample);
      var up = CoasterPathTrackFrame.trackBasisAtHit(level, sample).up();
      var bed = NativeCableRig.guideBlockAt(level, track, cabinet.cableHome() + 20.0 * i / 64);
      if (Math.abs(tangent.y) > .001 || tangent.dot(forward) < .999999 || up.y < .999999
          || !level.hasChunkAt(bed) || !level.getBlockState(bed).is(EquipmentContent.CABLE_GUIDE.get()))
        throw new IllegalStateException("Saved cable guide or its fixed support changed");
    }
    var plot = carriage.getPlot();
    if (!carriageIntact(carriage))
      throw new IllegalStateException("Saved native body is not the installed steel catch carriage");
    var center = plot.getCenterBlock().getCenter();
    var point = new Vector3d(center.x, center.y, center.z);
    var up = CoasterPathTrackFrame.trackBasisAtHit(level, hit).up();
    var homePoint = hit.point().add(up.scale(.3125 - 2));
    var home = new Vector3d(homePoint.x, homePoint.y, homePoint.z);
    var pose = carriage.logicalPose().orientation();
    if (Math.abs(pose.w()) < .999) throw new IllegalStateException("Saved carriage rotated away from its guide");
    var pipeline = container.physicsSystem().getPipeline();
    var displacement = NativeCableRig.worldPoint(carriage, point).sub(home);
    double travel = displacement.dot(direction);
    double lateral = displacement.sub(new Vector3d(direction).mul(travel)).length();
    var velocity = pipeline.getLinearVelocity(carriage, new Vector3d());
    double longitudinal = velocity.dot(direction);
    double transverse = velocity.sub(new Vector3d(direction).mul(longitudinal)).length();
    double linear = velocity.length();
    double angular = pipeline.getAngularVelocity(carriage, new Vector3d()).length();
    boolean withinGuide = cabinet.cableInUse() ? travel >= -.15 && travel <= 20.15 : Math.abs(travel) <= .15;
    boolean boundedMoving = cabinet.cableInUse() && cabinet.cableMoving()
        && Math.abs(longitudinal) <= 3 && transverse <= .15;
    // An inspected release is no longer an active catch; its carriage must nevertheless reopen at rest.
    boolean recoverableSpeed = cabinet.cableInUse()
        ? boundedMoving || !cabinet.cableMoving() && linear <= .5
        : linear <= .5;
    if (!withinGuide || lateral > .15 || angular > .1 || !recoverableSpeed)
      throw new IllegalStateException("Saved cable carriage moved outside its recoverable guide: travel=" + travel + " lateral=" + lateral
          + " linear=" + linear + " longitudinal=" + longitudinal + " transverse=" + transverse + " angular=" + angular);
    var frame = new Quaterniond().rotationTo(new Vector3d(1, 0, 0), direction);
    var guide = pipeline.addConstraint(null, carriage, new GenericConstraintConfiguration(home, point, frame, frame,
        EnumSet.of(ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z, ConstraintJointAxis.ANGULAR_X,
            ConstraintJointAxis.ANGULAR_Y, ConstraintJointAxis.ANGULAR_Z)));
    if (guide == null || !guide.isValid()) throw new IllegalStateException("Native reload guide unavailable");
    guide.setLimit(ConstraintJointAxis.LINEAR_X, 0, 20);
    try { return new Hold(carriage, guide, new NativeFinClamp(pipeline, carriage, point, direction, 256), home, point,
        direction, cabinet.cableInUse() && cabinet.cableMoving()); }
    catch (RuntimeException failure) { guide.remove(); throw failure; }
  }

  private static void restoreCatch(ServerLevel level, EquipmentCabinetEntity cabinet, Hold hold) {
    if (!hold.valid() || !cabinet.cableInUse() || cabinet.cableChassis() == null)
      throw new IllegalStateException("Active catch journal or passive carriage hold is unavailable");
    var capture = inspectCapture(level, cabinet, hold, false, true);
    var container = SubLevelContainer.getContainer(level);
    var chassisId = cabinet.cableChassis();
    var chassis = capture.chassis();
    var socket = VehicleInterfaces.catchPoint(chassis);
    var track = cabinet.resolve(level);
    if (track == null) throw new IllegalStateException("Saved catch rail is unavailable");
    var forward = track.forward(level, track.hitAtDistance(level, cabinet.cableHome()));
    var direction = new Vector3d(forward.x, forward.y, forward.z);
    var pipeline = container.physicsSystem().getPipeline();
    var carriageVelocity = pipeline.getLinearVelocity(hold.carriage(), new Vector3d());
    double carriageSpeed = carriageVelocity.dot(direction);
    for (var id : capture.bodies()) {
      var body = (ServerSubLevel) container.getSubLevel(id);
      var velocity = pipeline.getLinearVelocity(body, new Vector3d());
      double speed = velocity.dot(direction);
      double transverse = velocity.sub(new Vector3d(direction).mul(speed)).length();
      boolean boundedMoving = hold.movingAtRecovery() && Math.abs(speed) <= 3 && transverse <= .15
          && Math.abs(speed - carriageSpeed) <= .25;
      if (!(boundedMoving || !hold.movingAtRecovery() && velocity.length() < .5)
          || pipeline.getAngularVelocity(body, new Vector3d()).length() >= .1)
        throw new IllegalStateException("Saved captured native body is not guide-aligned with its moving carriage");
    }
    hold.attach(chassisId, NativeCableRig.attachCatch(pipeline, hold.carriage(), chassis, hold.point(), socket, direction));
  }

  /** Requires the reconstructed catch to return to the normal tight tolerance before clearing its moving journal. */
  private static boolean captureSettled(ServerLevel level, EquipmentCabinetEntity cabinet, Hold hold) {
    try {
      var capture = inspectCapture(level, cabinet, hold, false, false);
      var pipeline = SubLevelContainer.getContainer(level).physicsSystem().getPipeline();
      if (pipeline.getLinearVelocity(hold.carriage(), new Vector3d()).length() >= .03
          || pipeline.getAngularVelocity(hold.carriage(), new Vector3d()).length() >= .03) return false;
      for (var id : capture.bodies()) {
        var body = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel(id);
        if (pipeline.getLinearVelocity(body, new Vector3d()).length() >= .03
            || pipeline.getAngularVelocity(body, new Vector3d()).length() >= .03) return false;
      }
      return true;
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  private record Capture(ServerSubLevel chassis, Set<UUID> consist, Set<UUID> bodies) {}
  /** Validates exactly the saved provider, consist, native bodies and tickets without granting motion. */
  private static Capture inspectCapture(ServerLevel level, EquipmentCabinetEntity cabinet, Hold hold, boolean requireBrakes,
      boolean allowMovingAlignment) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null || cabinet.cableChassis() == null) throw new IllegalStateException("Native active-catch state is unavailable");
    var chassisId = cabinet.cableChassis();
    var key = new CableTickets.Key(cabinet.cableCarriage(), cabinet.getBlockPos());
    var carriageTicket = container.getAllTickets().get(cabinet.cableCarriage());
    if (carriageTicket == null || carriageTicket.tickets().stream().noneMatch(
        t -> CableTickets.typeOf(t) == CableTickets.CARRIAGE && key.equals(CableTickets.keyOf(t))))
      throw new IllegalStateException("Saved carriage loading ticket changed before active inspection");
    if (!(container.getSubLevel(chassisId) instanceof ServerSubLevel chassis) || chassis.isRemoved())
      throw new IllegalStateException("Saved captured chassis is not loaded");
    var motion = MotionProviders.resolve(level, chassisId).orElseThrow(
        () -> new IllegalStateException("Captured vehicle owner is unavailable after reload"));
    var consist = Set.copyOf(motion.consist());
    var bodies = new HashSet<UUID>();
    for (var id : consist) {
      var member = MotionProviders.resolve(level, id).orElseThrow(
          () -> new IllegalStateException("Captured consist member is unavailable after reload"));
      if (!member.provider().equals(motion.provider()) || !Set.copyOf(member.consist()).equals(consist)
          || !member.interfaces().contains(cabinet.mechanism())
          || requireBrakes && !member.interfaces().contains(Mechanism.FIN_BRAKE))
        throw new IllegalStateException("Captured consist identity or cable/brake certificate changed");
      for (var body : member.bodies()) if (!bodies.add(body))
        throw new IllegalStateException("Captured consist has duplicate native bodies");
    }
    var ticketed = new HashSet<UUID>();
    for (var entry : container.getAllTickets().entrySet())
      if (entry.getValue().tickets().stream().anyMatch(t -> CableTickets.typeOf(t) == CableTickets.CAPTURED_BODY && key.equals(CableTickets.keyOf(t))))
        ticketed.add(entry.getKey());
    if (!ticketed.equals(bodies) || bodies.isEmpty() || !bodies.contains(chassisId)
        || bodies.stream().anyMatch(id -> !(container.getSubLevel(id) instanceof ServerSubLevel loaded) || loaded.isRemoved()))
      throw new IllegalStateException("Captured native bodies or their loading tickets changed");
    var socket = VehicleInterfaces.catchPoint(chassis);
    boolean moving = allowMovingAlignment && hold.movingAtRecovery();
    double alignmentLimit = moving ? .25 : .03;
    double separation = socket == null ? Double.NaN : NativeCableRig.worldPoint(chassis, socket).distance(
        NativeCableRig.worldPoint(hold.carriage(), hold.point()));
    if (socket == null || separation >= alignmentLimit)
      throw new IllegalStateException("Saved catch socket is missing or no longer aligned: separation=" + separation
          + " moving_journal=" + hold.movingAtRecovery());
    return new Capture(chassis, consist, Set.copyOf(bodies));
  }

  private static boolean carriageIntact(ServerSubLevel carriage) {
    var blocks = carriage.getPlot().getEmbeddedLevelAccessor();
    if (!blocks.getBlockState(BlockPos.ZERO).is(EquipmentContent.CATCH_CARRIAGE.get())) return false;
    return List.of(BlockPos.ZERO.below(), BlockPos.ZERO.below(2), BlockPos.ZERO.east().below(2),
        BlockPos.ZERO.west().below(2), BlockPos.ZERO.north().below(2), BlockPos.ZERO.south().below(2))
        .stream().allMatch(pos -> blocks.getBlockState(pos).is(Blocks.IRON_BLOCK));
  }
}
