package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartSpawner;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

/** These carts are moved solely by the placed cabinet and a real Create kinetic network. */
@GameTestHolder("coasterslaunchsystems")
@PrefixGameTestTemplate(false)
public final class TireEquipmentGameTests {
  private enum Fixture { FORWARD, REVERSE, CONFLICT, MISSING_STRIP, LOADED, REVERSE_SHAFT }
  @GameTest(template = "empty", timeoutTicks = 200)
  public static void shaftPoweredForwardTransportAndPowerLoss(GameTestHelper helper) { transport(helper, Fixture.FORWARD); }
  @GameTest(template = "empty", timeoutTicks = 200)
  public static void anchorDirectedReverseTransport(GameTestHelper helper) { transport(helper, Fixture.REVERSE); }
  @GameTest(template = "empty", timeoutTicks = 200)
  public static void overlappingModulesVetoBeforeForce(GameTestHelper helper) { transport(helper, Fixture.CONFLICT); }
  @GameTest(template = "empty", timeoutTicks = 200)
  public static void missingChassisStripRejectsTireDrive(GameTestHelper helper) { transport(helper, Fixture.MISSING_STRIP); }
  @GameTest(template = "empty", timeoutTicks = 200)
  public static void loadedChassisUsesNativeBallastMass(GameTestHelper helper) { transport(helper, Fixture.LOADED); }
  @GameTest(template = "empty", timeoutTicks = 200)
  public static void negativeShaftRpmReversesTransport(GameTestHelper helper) { transport(helper, Fixture.REVERSE_SHAFT); }

  private static void transport(GameTestHelper helper, Fixture fixture) {
    boolean reverse = fixture == Fixture.REVERSE, conflict = fixture == Fixture.CONFLICT;
    boolean missing = fixture == Fixture.MISSING_STRIP, loaded = fixture == Fixture.LOADED;
    int rpm = fixture == Fixture.REVERSE_SHAFT ? -64 : 64;
    int direction = reverse || rpm < 0 ? -1 : 1;
    var level = helper.getLevel();
    var from = new BlockPos(64 + 32 * fixture.ordinal(), 112, -384);
    var to = from.offset(0, 0, 64);
    var modulePos = from.offset(1, -2, 8); var motorPos = modulePos.west();
    var secondPos = modulePos.east(2); var secondMotorPos = secondPos.east();
    for (int x = from.getX() / 16 - 1; x <= from.getX() / 16 + 1; x++)
      for (int z = -25; z <= -19; z++) level.setChunkForced(x, z, true);
    level.setBlockAndUpdate(from, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    level.setBlockAndUpdate(to, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var curve = new BezierConnection(Couple.create(from, to),
        Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level, from), CoasterAnchorpointBlockEntity.worldCenter(level, to)),
        Couple.create(new Vec3(0, 0, 1), new Vec3(0, 0, -1)), Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)),
        true, false, CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
    var state = EquipmentContent.TIRE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
    level.setBlockAndUpdate(modulePos, state);
    level.setBlockAndUpdate(motorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.EAST));
    ((CreativeMotorBlockEntity) level.getBlockEntity(motorPos)).generatedSpeed.setValue(0);
    if (conflict) {
      // Same contact coordinate on the opposite side, both within the three-block placement envelope.
      secondPos = modulePos.west(2); secondMotorPos = secondPos.west();
      level.setBlockAndUpdate(secondPos, state);
      level.setBlockAndUpdate(secondMotorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.EAST));
      ((CreativeMotorBlockEntity) level.getBlockEntity(secondMotorPos)).generatedSpeed.setValue(0);
    }
    final var conflictPos = secondPos; final var conflictMotor = secondMotorPos;
    var edge = CoasterPathEdge.curve(from, to, curve);
    var hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, from.getCenter().add(0, 0, 8));
    var body = CoasterCartSpawner.spawnMinimalContraption(level, hit.point(),
        CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level, hit)), hit);
    if (!missing) helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(),
        EquipmentContent.TIRE_SURFACE.get().defaultBlockState(), 3), "Chassis drive strip did not install");
    if (loaded) for (var cell : new BlockPos[] {new BlockPos(0, 1, 0), new BlockPos(0, 2, 0),
        new BlockPos(-1, 1, 0), new BlockPos(1, 1, 0), new BlockPos(0, 1, -1), new BlockPos(0, 1, 1)})
      helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().setBlock(cell, Blocks.IRON_BLOCK.defaultBlockState(), 3),
          "Native chassis ballast did not install");
    var start = new Vector3d(); var measured = new double[1];
    helper.runAtTickTime(30, () -> {
      var module = (EquipmentCabinetEntity) level.getBlockEntity(modulePos);
      var motion = MotionProviders.resolve(level, body.getUniqueId()).orElseThrow();
      helper.assertTrue(motion.interfaces().contains(Mechanism.TIRE) != missing, "Tire certificate ignored installed hardware");
      helper.assertTrue(!loaded || NativeForces.mass(level, motion) >= 7, "Ballast native mass was not included");
      module.bind(level, reverse ? to : from, reverse ? from : to); module.setArmed(true);
      if (conflict) {
        var other = (EquipmentCabinetEntity) level.getBlockEntity(conflictPos);
        other.bind(level, from, to); other.setArmed(true);
      }
    });
    helper.runAtTickTime(50, () -> {
      var physics = SubLevelContainer.getContainer(level).physicsSystem();
      helper.assertTrue(((EquipmentCabinetEntity) level.getBlockEntity(modulePos)).getSpeed() == 0,
          "Fixture motor did not begin at zero RPM");
      helper.assertTrue(Math.abs(physics.getPipeline().getLinearVelocity(body, new Vector3d()).z) < .05,
          "An unpowered tire module propelled the cart");
      physics.updatePose(body); start.set(body.logicalPose().position());
      ((CreativeMotorBlockEntity) level.getBlockEntity(motorPos)).generatedSpeed.setValue(rpm);
      if (conflict) ((CreativeMotorBlockEntity) level.getBlockEntity(conflictMotor)).generatedSpeed.setValue(64);
    });
    helper.runAtTickTime(100, () -> {
      var module = (EquipmentCabinetEntity) level.getBlockEntity(modulePos);
      var physics = SubLevelContainer.getContainer(level).physicsSystem();
      measured[0] = physics.getPipeline().getLinearVelocity(body, new Vector3d()).z;
      physics.updatePose(body);
      if (conflict) {
        var other = (EquipmentCabinetEntity) level.getBlockEntity(conflictPos);
        helper.assertTrue(module.fault().contains("Overlapping") && other.fault().contains("Overlapping"),
            "Conflicting contact owners were not faulted together");
        helper.assertTrue(Math.abs(measured[0]) < .05, "A rejected conflict applied a partial force");
      } else if (missing) {
        helper.assertTrue(module.fault().contains("certified tire interface"), "Missing drive strip did not fault");
        helper.assertTrue(Math.abs(measured[0]) < .05, "Bare bearing was propelled without a chassis strip");
      } else {
        helper.assertTrue(module.fault().isEmpty(), "Tire runtime fault: " + module.fault());
        double directed = measured[0] * direction;
        helper.assertTrue(directed > .8 && directed < 2.8, "Shaft tire speed outside rated range: " + measured[0]);
        helper.assertTrue(body.logicalPose().position().distance(start) > .5, "Cabinet did not physically move the cart");
        helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(body), "Tire transport lost native engagement");
      }
      System.out.println("LS_TIRE_EQUIPMENT fixture=" + fixture + " mass=" + NativeForces.mass(level,
          MotionProviders.resolve(level, body.getUniqueId()).orElseThrow()) + " rpm=" + module.getSpeed()
          + " speed=" + measured[0] + " moved=" + body.logicalPose().position().distance(start) + " fault=" + module.fault());
      ((CreativeMotorBlockEntity) level.getBlockEntity(motorPos)).generatedSpeed.setValue(0);
    });
    helper.runAtTickTime(120, () -> {
      var module = (EquipmentCabinetEntity) level.getBlockEntity(modulePos);
      helper.assertTrue(module.getSpeed() == 0, "Power loss did not reach Create module");
      double speed = SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body, new Vector3d()).z;
      helper.assertTrue(Math.abs(speed) <= Math.abs(measured[0]) + .05, "Unpowered cabinet continued accelerating");
      if (!missing) {
        body.getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(), Blocks.AIR.defaultBlockState(), 3);
        helper.assertTrue(!MotionProviders.resolve(level, body.getUniqueId()).orElseThrow().interfaces().contains(Mechanism.TIRE),
            "Removed chassis hardware retained its tire certificate");
      }
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).removeAnchorPeerCurve(level, to);
      helper.assertTrue(module.resolve(level) == null, "Removed supporting track still resolved");
      SubLevelContainer.getContainer(level).removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      for (var pos : new BlockPos[] {modulePos, motorPos, from, to}) level.removeBlock(pos, false);
      if (conflict) { level.removeBlock(conflictPos, false); level.removeBlock(conflictMotor, false); }
      helper.succeed();
    });
  }
}
