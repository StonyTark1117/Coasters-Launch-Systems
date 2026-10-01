package dev.coasterslaunchsystems.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.content.*;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import java.util.UUID;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("coasterslaunchsystems")
@PrefixGameTestTemplate(false)
public final class EquipmentPlacementGameTests {
  @GameTest(template = "empty", timeoutTicks = 80)
  public static void realToolBindingKineticPowerPersistenceAndSupportLoss(GameTestHelper helper) {
    var level = helper.getLevel();
    var from = new BlockPos(48, 104, -256); var to = from.offset(0, 0, 64);
    var module = from.offset(1, -2, 8); var motor = module.west(); var controllerPos = from.offset(4, -2, 8);
    for (int x = 2; x <= 4; x++) for (int z = -17; z <= -11; z++) level.setChunkForced(x, z, true);
    level.setBlockAndUpdate(from, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    level.setBlockAndUpdate(to, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var curve = new BezierConnection(Couple.create(from, to),
        Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level, from), CoasterAnchorpointBlockEntity.worldCenter(level, to)),
        Couple.create(new Vec3(0, 0, 1), new Vec3(0, 0, -1)), Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)),
        true, false, CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
    var state = EquipmentContent.TIRE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
    level.setBlockAndUpdate(module, state);
    level.setBlockAndUpdate(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.EAST));
    var player = FakePlayerFactory.get(level, new GameProfile(new UUID(0x1a0c5L, 1), "LSPlacement"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, EquipmentContent.TOOL.get().getDefaultInstance());
    helper.runAtTickTime(10, () -> {
      for (var pos : new BlockPos[] {from, to, module}) {
        player.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + 2);
        var result = player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(pos.getCenter(), Direction.UP, pos, false)));
        helper.assertTrue(result == InteractionResult.SUCCESS, "Real span-tool interaction failed at " + pos);
      }
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(module);
      helper.assertTrue(cabinet.resolve(level) != null && cabinet.interval().end() - cabinet.interval().start() <= 2.001,
          "Module claimed an entire long track edge");
      helper.assertTrue(player.getMainHandItem().get(EquipmentContent.FROM.get()) == null,
          "Selection retained after successful installation");
      level.setBlockAndUpdate(controllerPos, EquipmentContent.CONTROLLER.get().defaultBlockState());
      for (var pos : new BlockPos[] {from, to, controllerPos}) {
        player.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + 2);
        helper.assertTrue(player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(pos.getCenter(), Direction.UP, pos, false))) == InteractionResult.SUCCESS, "Station controller tool binding failed");
      }
      var controller = (RideControllerEntity) level.getBlockEntity(controllerPos);
      player.setItemInHand(InteractionHand.OFF_HAND, net.minecraft.world.item.Items.CLOCK.getDefaultInstance());
      helper.assertTrue(player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
          new BlockHitResult(controllerPos.getCenter(), Direction.UP, controllerPos, false))) == InteractionResult.SUCCESS && controller.stationCapture(), "Clock/tool did not enable station capture");
      var controllerTag = controller.saveWithFullMetadata(level.registryAccess());
      var restoredController = (RideControllerEntity) BlockEntity.loadStatic(controllerPos, controller.getBlockState(), controllerTag, level.registryAccess()); restoredController.setLevel(level);
      helper.assertTrue(restoredController.stationCapture() && !restoredController.boardingReady() && !restoredController.armed()
          && restoredController.phase() == RideControllerEntity.State.INSPECTION && !restoredController.consumeRequest(), "Saved station replayed readiness or a departure request");
      controllerTag.putInt("ls_controller_schema", 3); controllerTag.remove("station_capture"); controllerTag.remove("boarding_ready");
      restoredController.loadWithComponents(controllerTag, level.registryAccess());
      helper.assertTrue(!restoredController.stationCapture(), "Legacy controller opted into new station behavior");
      player.setItemInHand(InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
      cabinet.setArmed(true);
      ((CreativeMotorBlockEntity) level.getBlockEntity(motor)).generatedSpeed.setValue(64);
    });
    helper.runAtTickTime(20, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(module);
      helper.assertTrue(Math.abs(cabinet.getSpeed()) == 64, "Create shaft power did not reach the module");
      helper.assertTrue(cabinet.calculateStressApplied() == 16, "Tire module is not a Create stress consumer");
      var tag = cabinet.saveWithFullMetadata(level.registryAccess());
      var restored = (EquipmentCabinetEntity) BlockEntity.loadStatic(module, state, tag, level.registryAccess());
      restored.setLevel(level);
      helper.assertTrue(restored.identity().equals(cabinet.identity()) && restored.interval().equals(cabinet.interval())
          && restored.armed() && restored.resolve(level) != null, "Module identity/contact changed on save/load");
      for (String id : new String[] {"tire_drive", "fin_brake", "eddy_brake", "span_tool", "tire_surface", "brake_fin", "conductive_fin", "ride_controller"})
        helper.assertTrue(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("coasterslaunchsystems", id)).isPresent(),
            "Recipe did not load: " + id);
      level.setBlockAndUpdate(module, state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
      helper.assertTrue(cabinet.resolve(level) == null && !cabinet.armed(), "Rotated shaft kept an active binding");
      boolean rejected = false;
      try { cabinet.bind(level, from, to); } catch (IllegalArgumentException expected) { rejected = true; }
      helper.assertTrue(rejected, "Longitudinal shaft passed tire placement checks");
      level.setBlockAndUpdate(module, state);
      cabinet.bind(level, from, to); cabinet.setArmed(true);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).removeAnchorPeerCurve(level, to);
    });
    helper.runAtTickTime(30, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(module);
      helper.assertTrue(cabinet.resolve(level) == null && !cabinet.fault().isEmpty() && !cabinet.armed(),
          "Removed track did not invalidate/disarm the module");
      System.out.println("LS_EQUIPMENT_PLACEMENT rpm=" + cabinet.getSpeed() + " contact=" + cabinet.interval()
          + " identity=" + cabinet.identity() + " support_fault=" + cabinet.fault());
      level.removeBlock(module, false); level.removeBlock(motor, false); level.removeBlock(controllerPos, false);
      level.removeBlock(from, false); level.removeBlock(to, false);
      helper.succeed();
    });
  }
}
