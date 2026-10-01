package dev.coasterslaunchsystems.runtime;

import com.mojang.logging.LogUtils;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.math.Mechanism;
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

/** Restores a passive native guide/hold for an inspected, idle carriage after world load. */
public final class CableRecovery {
  private record Hold(ServerSubLevel carriage, GenericConstraintHandle guide, NativeFinClamp brake,
      Vector3d home, Vector3d point) {
    void close() { brake.close(); if (guide.isValid()) guide.remove(); }
    boolean valid() { return !carriage.isRemoved() && guide.isValid() && brake.valid(); }
  }
  private static final Map<ServerLevel, Map<UUID, Hold>> HOLDS = new HashMap<>();
  private static final Map<ServerLevel, Set<UUID>> WARNED = new HashMap<>();
  private CableRecovery() {}

  public static void stabilize(ServerLevel level, Collection<EquipmentCabinetEntity> cabinets) {
    var retained = HOLDS.computeIfAbsent(level, ignored -> new HashMap<UUID, Hold>());
    retained.entrySet().removeIf(entry -> {
      if (entry.getValue().valid()) return false;
      entry.getValue().close(); return true;
    });
    var candidates = new LinkedHashSet<>(cabinets);
    var container = SubLevelContainer.getContainer(level);
    if (container != null) for (var info : container.getAllTickets().values()) for (var ticket : info.tickets()) {
      if (ticket.getType() != CableTickets.CARRIAGE || !(ticket.getKey() instanceof CableTickets.Key key)
          || retained.containsKey(key.carriage()) || CableControllers.hasLiveCarriage(level, key.carriage())
          || Math.abs((long) key.cabinet().getX()) > 1024 || Math.abs((long) key.cabinet().getZ()) > 1024) continue;
      // A force-loaded native carriage must bring its owning cabinet into memory before the first physics step.
      level.getChunkAt(key.cabinet());
      if (level.getBlockEntity(key.cabinet()) instanceof EquipmentCabinetEntity cabinet) candidates.add(cabinet);
    }
    for (var cabinet : candidates) {
      if (cabinet.isRemoved() || cabinet.mechanism() != Mechanism.FLYWHEEL || !cabinet.cableInstalled()
          || cabinet.cableCarriage() == null || cabinet.fault().isEmpty()
          || CableControllers.hasLiveCarriage(level, cabinet.cableCarriage())
          || retained.containsKey(cabinet.cableCarriage())) continue;
      try {
        var hold = restorePassive(level, cabinet);
        if (hold != null) { retained.put(cabinet.cableCarriage(), hold); WARNED.computeIfAbsent(level, ignored -> new HashSet<>()).remove(cabinet.cableCarriage()); }
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

  /** Explicitly retire a reloaded idle installation only after its entire physical ownership is checked. */
  public static void inspectRetireIdle(ServerLevel level, EquipmentCabinetEntity cabinet) {
    if (cabinet.mechanism() != Mechanism.FLYWHEEL || !cabinet.cableInstalled() || cabinet.cableInUse()
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
    boolean modern = info != null && info.tickets().stream().anyMatch(t -> t.getType() == CableTickets.CARRIAGE && key.equals(t.getKey()));
    boolean legacy = info != null && info.tickets().stream().anyMatch(t -> t.getType() == CableTickets.LEGACY_CARRIAGE && id.equals(t.getKey()));
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

  public static void unload(ServerLevel level) { HOLDS.remove(level); WARNED.remove(level); }

  private static Hold restorePassive(ServerLevel level, EquipmentCabinetEntity cabinet) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return null;
    var id = cabinet.cableCarriage();
    var ticket = container.getAllTickets().get(id);
    if (ticket == null || ticket.tickets().stream().noneMatch(entry -> entry.getType() == CableTickets.CARRIAGE
        && new CableTickets.Key(id, cabinet.getBlockPos()).equals(entry.getKey())
        || entry.getType() == CableTickets.LEGACY_CARRIAGE && id.equals(entry.getKey()))) return null;
    if (!(container.getSubLevel(id) instanceof ServerSubLevel carriage) || carriage.isRemoved()) return null;
    if (cabinet.cableInUse()) {
      var chassis = cabinet.cableChassis();
      var body = chassis == null ? null : container.getSubLevel(chassis);
      var info = chassis == null ? null : container.getAllTickets().get(chassis);
      var key = new CableTickets.Key(id, cabinet.getBlockPos());
      if (!(body instanceof ServerSubLevel) || info == null || info.tickets().stream().noneMatch(
          entry -> entry.getType() == CableTickets.CAPTURED_BODY && key.equals(entry.getKey())))
        throw new IllegalStateException("Saved captured train body or its native loading ticket is unavailable");
    }
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
    double linear = pipeline.getLinearVelocity(carriage, new Vector3d()).length();
    double angular = pipeline.getAngularVelocity(carriage, new Vector3d()).length();
    if (Math.abs(travel) > .15 || lateral > .15 || linear > .5 || angular > .1)
      throw new IllegalStateException("Saved idle carriage moved from home: travel=" + travel + " lateral=" + lateral
          + " linear=" + linear + " angular=" + angular);
    var frame = new Quaterniond().rotationTo(new Vector3d(1, 0, 0), direction);
    var guide = pipeline.addConstraint(null, carriage, new GenericConstraintConfiguration(home, point, frame, frame,
        EnumSet.of(ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z, ConstraintJointAxis.ANGULAR_X,
            ConstraintJointAxis.ANGULAR_Y, ConstraintJointAxis.ANGULAR_Z)));
    if (guide == null || !guide.isValid()) throw new IllegalStateException("Native reload guide unavailable");
    guide.setLimit(ConstraintJointAxis.LINEAR_X, 0, 20);
    try { return new Hold(carriage, guide, new NativeFinClamp(pipeline, carriage, point, direction, 256), home, point); }
    catch (RuntimeException failure) { guide.remove(); throw failure; }
  }

  private static boolean carriageIntact(ServerSubLevel carriage) {
    var blocks = carriage.getPlot().getEmbeddedLevelAccessor();
    if (!blocks.getBlockState(BlockPos.ZERO).is(EquipmentContent.CATCH_CARRIAGE.get())) return false;
    return List.of(BlockPos.ZERO.below(), BlockPos.ZERO.below(2), BlockPos.ZERO.east().below(2),
        BlockPos.ZERO.west().below(2), BlockPos.ZERO.north().below(2), BlockPos.ZERO.south().below(2))
        .stream().allMatch(pos -> blocks.getBlockState(pos).is(Blocks.IRON_BLOCK));
  }
}
