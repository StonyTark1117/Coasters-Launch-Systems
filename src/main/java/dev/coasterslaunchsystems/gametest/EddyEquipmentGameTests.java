package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.runtime.EquipmentRuntime;
import dev.coasterslaunchsystems.runtime.NativeFinClamp;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartSpawner;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Observe actual magnetic cabinet impulses before adding the fixture's physical entry/disturbance force. */
@GameTestHolder("coasterslaunchsystems")
public final class EddyEquipmentGameTests {
  private enum Fixture { FORWARD, REVERSE, FULL_LOAD, AT_REST, SLOPE, MISSING_FIN, CONFLICT, HOT, POWER_LOSS }
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static final class Scene {
    final GameTestHelper helper;
    final ServerLevel level;
    final ServerSubLevel body;
    final Fixture fixture;
    final BlockPos from, to, module, motor, tire, tireMotor;
    DirectedTrack track;
    NativeFinClamp preparationHold;
    double externalForce, impulse, dissipated, expectedHeat, peakForce, minimumSpeed = Double.POSITIVE_INFINITY;
    double entryDistance, lastDistance;
    int brakingContactSteps, availableSteps;
    String failure = "";
    boolean braking, closed, boundsVerified, entryVerified;
    Scene(GameTestHelper helper, ServerSubLevel body, Fixture fixture, BlockPos from, BlockPos to, BlockPos module) {
      this.helper = helper; level = helper.getLevel(); this.body = body; this.fixture = fixture;
      this.from = from; this.to = to; this.module = module;
      motor = module.west(); tire = module.east(2); tireMotor = tire.east();
    }
    EquipmentCabinetEntity cabinet() { return (EquipmentCabinetEntity) level.getBlockEntity(module); }
    double speed() {
      var physics = SubLevelContainer.getContainer(level).physicsSystem(); physics.updatePose(body);
      return physics.getPipeline().getLinearVelocity(body, new Vector3d()).z;
    }
    void at(int tick, Runnable action) {
      helper.runAtTickTime(tick, () -> {
        try { action.run(); }
        catch (RuntimeException error) { cleanup(); throw error; }
      });
    }
    void cleanup() {
      if (closed) return; closed = true; SCENES.remove(this);
      if (preparationHold != null) preparationHold.close();
      SubLevelContainer.getContainer(level).removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      if (level.getBlockEntity(from) instanceof CoasterAnchorpointBlockEntity anchor) anchor.removeAnchorPeerCurve(level, to);
      for (var pos : List.of(module, motor, tire, tireMotor, from, to)) level.removeBlock(pos, false);
    }
  }
  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.level != event.getPhysicsSystem().getLevel() || s.body.isRemoved()) continue;
      try {
        var contact = EngagedContacts.current(s.level, s.body).orElse(null);
        if (contact == null || !s.track.matches(contact.hit())) continue;
        s.lastDistance = s.track.distance(contact.hit());
        if (s.braking) {
          if (!s.entryVerified) {
            s.entryDistance = s.lastDistance;
            s.helper.assertTrue(s.cabinet().interval().intersects(s.entryDistance, s.entryDistance, .6)
                && EquipmentRuntime.available(s.level, s.cabinet()), "Magnetic entry missed its live physical cabinet: fixture=" + s.fixture
                + " distance=" + s.entryDistance + " interval=" + s.cabinet().interval() + " available=" + EquipmentRuntime.available(s.level, s.cabinet()));
            s.entryVerified = true;
          }
          if (s.cabinet().interval().intersects(s.lastDistance, s.lastDistance, .6)) s.brakingContactSteps++;
          if (EquipmentRuntime.available(s.level, s.cabinet())) s.availableSteps++;
        }
        if (s.fixture == Fixture.AT_REST && s.braking && !s.boundsVerified) {
          for (double invalid : new double[] {-.01, 1.01, Double.NaN, Double.POSITIVE_INFINITY}) {
            EngagedContacts.accepted(s.level, s.body, new CoasterPathTrackFrame.GraphHit(contact.hit().point(), contact.hit().edge(), invalid));
            s.helper.assertTrue(EngagedContacts.current(s.level, s.body).isEmpty(), "Extrapolated/non-finite guide frame certified contact");
          }
          EngagedContacts.accepted(s.level, s.body, contact.hit());
          s.helper.assertTrue(EngagedContacts.current(s.level, s.body).isPresent(), "Valid native frame did not recover after rejected input");
          s.boundsVerified = true;
        }
        var axis = s.track.forward(s.level, contact.hit()); var direction = new Vector3d(axis.x, axis.y, axis.z);
        if (s.fixture == Fixture.SLOPE && !s.braking) {
          if (s.preparationHold == null) s.preparationHold = new NativeFinClamp(event.getPhysicsSystem().getPipeline(),
              s.body, s.body.getMassTracker().getCenterOfMass(), direction, 128);
          s.preparationHold.align(direction);
        }
        var groups = s.body.getQueuedForceGroups();
        var group = groups == null ? null : groups.get(ForceGroups.PROPULSION.get());
        var impulse = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalForce());
        double force = impulse.rotate(s.body.logicalPose().orientation()).dot(direction) / event.getTimeStep();
        s.impulse += Math.abs(force) * event.getTimeStep(); s.peakForce = Math.max(s.peakForce, Math.abs(force));
        var point = VehicleInterfaces.conductivePoint(s.body);
        double work = 0;
        if (point != null) {
          var dynamics = NativeForces.pointDynamics(s.body, event.getPhysicsSystem().getPipeline(), point, direction);
          s.helper.assertTrue(force * dynamics.speed() <= 1e-8, "Magnetic impulse propelled the conductive fin");
          work = DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(), force, event.getTimeStep());
          s.helper.assertTrue(work <= 1e-8, "Magnetic impulse added interface energy");
        } else s.helper.assertTrue(Math.abs(force) < 1e-9, "Missing conductive hardware received a magnetic impulse");
        s.dissipated += Math.max(0, -work);
        s.expectedHeat = Math.max(0, s.expectedHeat - 8 * event.getTimeStep()) + Math.max(0, -work);
        s.helper.assertTrue(Math.abs(s.cabinet().heat() - s.expectedHeat) < 1e-7, "Actual magnetic work and stored/cooled heat disagree");
        double speed = event.getPhysicsSystem().getPipeline().getLinearVelocity(s.body, new Vector3d()).dot(direction);
        if (s.braking && s.fixture != Fixture.SLOPE) s.minimumSpeed = Math.min(s.minimumSpeed, speed * (s.fixture == Fixture.REVERSE ? -1 : 1));
        if (s.externalForce != 0) NativeForces.pointForce(s.body, s.body.getMassTracker().getCenterOfMass(),
            direction.mul(s.externalForce), event.getTimeStep());
      } catch (RuntimeException error) { s.failure = error.toString(); }
    }
  }
  @GameTestGenerator
  public static List<TestFunction> equipmentTests() {
    var result = new ArrayList<TestFunction>();
    for (var fixture : Fixture.values()) result.add(new TestFunction("launchsystems_eddy", "coasterslaunchsystems.eddy_" + fixture.name().toLowerCase(Locale.ROOT),
        "coasterslaunchsystems:empty", 260, 0, true, helper -> run(helper, fixture)));
    return result;
  }
  private static void run(GameTestHelper helper, Fixture fixture) {
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, EddyEquipmentGameTests::physics); registered = true; }
    var level = helper.getLevel(); boolean slope = fixture == Fixture.SLOPE;
    var from = new BlockPos(256 + fixture.ordinal() * 32, 176, -768); var to = from.offset(0, slope ? 16 : 0, 64);
    var module = from.offset(-1, slope ? 0 : -2, fixture == Fixture.REVERSE ? 6 : slope ? 7 : 8);
    for (int x = from.getX() / 16 - 1; x <= from.getX() / 16 + 1; x++)
      for (int z = -49; z <= -43; z++) level.setChunkForced(x, z, true);
    for (var pos : List.of(from, to)) level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var tangent = new Vec3(0, slope ? .25 : 0, 1).normalize(); var up = new Vec3(0, 1, slope ? -.25 : 0).normalize();
    var curve = new BezierConnection(Couple.create(from, to), Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level, from), CoasterAnchorpointBlockEntity.worldCenter(level, to)),
        Couple.create(tangent, tangent.scale(-1)), Couple.create(up, up), true, false, CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
    var edge = CoasterPathEdge.curve(from, to, curve);
    var hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, from.getCenter().add(0, slope ? 2 : 0, 8));
    var body = CoasterCartSpawner.spawnMinimalContraption(level, hit.point(), CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level, hit)), hit);
    if (fixture != Fixture.MISSING_FIN) helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(-1, -1, 0), EquipmentContent.CONDUCTIVE_FIN.get().defaultBlockState(), 3), "Conductive fin did not install");
    int count = fixture == Fixture.FULL_LOAD ? 15 : 6;
    for (int i = 0; i < count; i++) helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(i % 3 - 1, 1 + i / 9, i / 3 % 3 - 1), Blocks.IRON_BLOCK.defaultBlockState(), 3), "Magnetic fixture ballast did not install");
    if (fixture == Fixture.FULL_LOAD || fixture == Fixture.CONFLICT) body.getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(), EquipmentContent.TIRE_SURFACE.get().defaultBlockState(), 3);
    if (fixture == Fixture.FULL_LOAD) body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(1, -1, 0), EquipmentContent.BRAKE_FIN.get().defaultBlockState(), 3);
    var scene = new Scene(helper, body, fixture, from, to, module);
    level.setBlockAndUpdate(module, EquipmentContent.EDDY.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
    if (fixture == Fixture.POWER_LOSS) {
      level.setBlockAndUpdate(scene.motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.EAST));
      ((CreativeMotorBlockEntity) level.getBlockEntity(scene.motor)).generatedSpeed.setValue(64);
    }
    if (fixture == Fixture.CONFLICT) {
      level.setBlockAndUpdate(scene.tire, EquipmentContent.TIRE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      level.setBlockAndUpdate(scene.tireMotor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.WEST));
      ((CreativeMotorBlockEntity) level.getBlockEntity(scene.tireMotor)).generatedSpeed.setValue(0);
    }
    var start = new Vector3d(); var initial = new double[1]; var removalBaseline = new double[1];
    scene.at(slope ? 10 : 30, () -> {
      scene.track = DirectedTrack.resolve(level, from, to).orElseThrow(); SCENES.add(scene);
      scene.cabinet().bind(level, from, to);
      if (fixture == Fixture.CONFLICT) { var other = (EquipmentCabinetEntity) level.getBlockEntity(scene.tire); other.bind(level, from, to); other.setArmed(true); }
    });
    scene.at(40, () -> {
      var motion = MotionProviders.resolve(level, body.getUniqueId()).orElseThrow();
      helper.assertTrue(motion.interfaces().contains(Mechanism.EDDY_BRAKE) != (fixture == Fixture.MISSING_FIN), "Conductive certificate ignored physical hardware");
      helper.assertTrue(scene.cabinet().calculateStressApplied() == 0, "Passive magnet consumed shaft stress");
      helper.assertTrue(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("coasterslaunchsystems", "conductive_fin")).isPresent(), "Conductive fin recipe did not load");
      if (fixture == Fixture.FULL_LOAD) helper.assertTrue(Math.abs(NativeForces.mass(level, motion) - 64) < 1e-6, "Full magnetic fixture must have native mass 64");
      if (fixture != Fixture.AT_REST && !slope) scene.externalForce = NativeForces.mass(level, motion) * 2 * (fixture == Fixture.REVERSE ? -1 : 1);
    });
    scene.at(60, () -> {
      initial[0] = scene.speed(); scene.externalForce = 0;
      if (fixture != Fixture.AT_REST && !slope) helper.assertTrue(Math.abs(initial[0]) > 1.5, "Magnetic fixture did not enter through physical forces");
      start.set(body.logicalPose().position()); scene.braking = true; scene.cabinet().setArmed(true);
      if (scene.preparationHold != null) { scene.preparationHold.close(); scene.preparationHold = null; }
      if (fixture == Fixture.CONFLICT) ((CreativeMotorBlockEntity) level.getBlockEntity(scene.tireMotor)).generatedSpeed.setValue(64);
      if (fixture == Fixture.POWER_LOSS) ((CreativeMotorBlockEntity) level.getBlockEntity(scene.motor)).generatedSpeed.setValue(0);
      if (fixture == Fixture.HOT) {
        var tag = scene.cabinet().saveWithFullMetadata(level.registryAccess()); tag.putDouble("ls_heat", 8192);
        scene.cabinet().loadWithComponents(tag, level.registryAccess()); scene.expectedHeat = 8192;
      }
    });
    scene.at(100, () -> {
      double speed = scene.speed(); var cabinet = scene.cabinet();
      helper.assertTrue(scene.failure.isEmpty(), "Native magnetic observer failed: " + scene.failure);
      helper.assertTrue(scene.entryVerified, "Magnetic entry lacked accepted native contact");
      System.out.println("LS_EDDY_EQUIPMENT fixture=" + fixture + " mass=" + body.getMassTracker().getMass() + " initial=" + initial[0]
          + " speed=" + speed + " impulse=" + scene.impulse + " peak_force=" + scene.peakForce + " work=" + scene.dissipated + " heat=" + cabinet.heat() + " fault=" + cabinet.fault());
      System.out.println("LS_EDDY_CONTACT fixture=" + fixture + " entry=" + scene.entryDistance + " last=" + scene.lastDistance + " interval=" + cabinet.interval()
          + " contact_steps=" + scene.brakingContactSteps + " available_steps=" + scene.availableSteps + " armed=" + cabinet.armed());
      if (fixture == Fixture.MISSING_FIN || fixture == Fixture.CONFLICT) {
        helper.assertTrue(cabinet.fault().contains(fixture == Fixture.CONFLICT ? "Overlapping" : "conductive fin"), "Magnetic rejection lost its cause");
        helper.assertTrue(scene.impulse < 1e-9 && Math.abs(speed) > 1.5, "A rejected magnet or owner applied partial force");
        scene.cleanup(); helper.succeed(); return;
      }
      helper.assertTrue(cabinet.fault().isEmpty(), "Magnetic cabinet faulted: " + cabinet.fault());
      if (fixture == Fixture.AT_REST) {
        helper.assertTrue(scene.impulse < 1e-7 && Math.abs(speed) < .03, "Stationary magnet supplied propulsion or a hold impulse");
        helper.assertTrue(scene.boundsVerified, "Native endpoint validity checks did not execute");
        scene.externalForce = 20;
      } else if (slope) {
        helper.assertTrue(scene.impulse > 1 && scene.dissipated > 1 && speed < -.4 && body.logicalPose().position().distance(start) > .2, "Passive magnets pinned a native car on the slope");
      } else {
        helper.assertTrue(scene.dissipated > 1 && scene.impulse > 1 && scene.minimumSpeed > -.03, "Magnetic trim did not dissipate motion in the entered direction");
        helper.assertTrue(Math.abs(speed) < Math.abs(initial[0]) - (fixture == Fixture.HOT ? .02 : .4), "Placed magnet did not trim native motion");
        if (fixture == Fixture.HOT) helper.assertTrue(cabinet.thermalFraction() < .2 && scene.peakForce < 30, "Stored heat did not derate actual force");
      }
      var tag = cabinet.saveWithFullMetadata(level.registryAccess());
      var restored = (EquipmentCabinetEntity) BlockEntity.loadStatic(scene.module, cabinet.getBlockState(), tag, level.registryAccess());
      helper.assertTrue(restored.identity().equals(cabinet.identity()) && Math.abs(restored.heat() - cabinet.heat()) < 1e-9, "Magnetic work/identity changed across persistence");
      tag.putInt("ls_schema", 1); var legacy = (EquipmentCabinetEntity) BlockEntity.loadStatic(scene.module, cabinet.getBlockState(), tag, level.registryAccess());
      helper.assertTrue(legacy.identity().equals(cabinet.identity()) && legacy.heat() == 0, "Legacy module migration fabricated heat or changed identity");
    });
    scene.at(130, () -> {
      if (scene.closed) return;
      helper.assertTrue(scene.failure.isEmpty(), "Magnetic sensor failed: " + scene.failure);
      if (fixture == Fixture.AT_REST) {
        System.out.println("LS_EDDY_DISTURBANCE speed=" + scene.speed() + " moved=" + body.logicalPose().position().distance(start) + " impulse=" + scene.impulse + " work=" + scene.dissipated);
        helper.assertTrue(scene.impulse > .01 && scene.dissipated > .001 && scene.speed() > .001 && body.logicalPose().position().distance(start) > .002, "Magnet created a static holding constraint against a gentle disturbance");
      }
      removalBaseline[0] = scene.impulse;
      body.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(-1, -1, 0), Blocks.AIR.defaultBlockState(), 3);
      helper.assertTrue(!MotionProviders.resolve(level, body.getUniqueId()).orElseThrow().interfaces().contains(Mechanism.EDDY_BRAKE), "Removed conductor kept magnetic eligibility");
    });
    scene.at(150, () -> {
      if (scene.closed) return;
      try {
        helper.assertTrue(scene.failure.isEmpty(), "Magnetic sensor failed: " + scene.failure);
        helper.assertTrue(Math.abs(scene.impulse - removalBaseline[0]) < 1e-9, "Removed conductor kept receiving magnetic impulses");
        if (!slope) helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(body), "Magnetic equipment lost native engagement");
      } finally { scene.cleanup(); }
      helper.succeed();
    });
  }
}
