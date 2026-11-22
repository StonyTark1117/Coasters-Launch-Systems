package dev.coasterslaunchsystems.gametest;

import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlock;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("coasterslaunchsystems_clearance")
@PrefixGameTestTemplate(false)
public final class EquipmentClearanceGameTests {
  @GameTest(template = "empty", timeoutTicks = 60)
  public static void runningObstructionsRejectBindingAndRearmWithoutChangingIdentity(GameTestHelper h) {
    var level = h.getLevel();
    var from = new BlockPos(96, 104, -128); var to = from.south(48);
    for (int x = 5; x <= 7; x++) for (int z = -9; z <= -4; z++) level.setChunkForced(x, z, true);
    var anchor = SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState()
        .setValue(CoasterAnchorpointBlock.FACING, Direction.UP);
    level.setBlockAndUpdate(from, anchor); level.setBlockAndUpdate(to, anchor);
    var curve = CoasterTrackPlacement.buildAnchorAnchorBezier(level, from, to, CoasterTrackMaterials.COASTER, false);
    h.assertTrue(curve != null, "Floor-mounted upstream rail placement failed");
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
    var types = List.of(EquipmentContent.TIRE.get(), EquipmentContent.FIN.get(), EquipmentContent.EDDY.get());
    for (int i = 0; i < types.size(); i++) level.setBlockAndUpdate(from.offset(1, -2, 8 + 12 * i),
        types.get(i).defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
    h.runAtTickTime(10, () -> {
      var track = DirectedTrack.resolve(level, from, to).orElseThrow();
      for (int i = 0; i < types.size(); i++) {
        var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(from.offset(1, -2, 8 + 12 * i));
        var id = cabinet.identity();
        var point = track.hitAtDistance(level, 8 + 12 * i).point();
        var obstruction = BlockPos.containing(point).above();
        level.setBlockAndUpdate(obstruction, Blocks.IRON_BLOCK.defaultBlockState());
        denied(h, () -> cabinet.bind(level, from, to), "forward obstruction");
        denied(h, () -> cabinet.bind(level, to, from), "reverse obstruction");
        h.assertTrue(cabinet.identity().equals(id) && !cabinet.armed(), "Rejected binding changed identity/arming");
        level.removeBlock(obstruction, false);
        // Collision-shape precision: a low slab beneath the corridor is valid.
        var slab = BlockPos.containing(point);
        level.setBlockAndUpdate(slab, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        cabinet.bind(level, from, to);
        var interval = cabinet.interval();
        cabinet.setArmed(true); cabinet.setArmed(false);
        level.setBlockAndUpdate(obstruction, Blocks.IRON_BLOCK.defaultBlockState());
        denied(h, () -> cabinet.setArmed(true), "obstructed re-arm");
        denied(h, () -> cabinet.bind(level, to, from), "obstructed rebinding");
        h.assertTrue(!cabinet.armed() && cabinet.identity().equals(id) && cabinet.interval().equals(interval),
            "Rejected edit lost the accepted binding or armed equipment");
        level.removeBlock(obstruction, false);
        cabinet.setArmed(true);
        h.assertTrue(cabinet.resolve(level) != null && cabinet.identity().equals(id), "Clearance repair lost mechanical identity");
        cabinet.setArmed(false);
        level.removeBlock(slab, false);
      }
      System.out.println("LS005_CLEARANCE PASS tireFinMagnet=true forwardReverse=true blockedRearm=true shapePrecision=true identityRetained=true");
      h.succeed();
    });
  }

  private static void denied(GameTestHelper h, Runnable action, String claim) {
    try { action.run(); }
    catch (IllegalArgumentException expected) {
      h.assertTrue(expected.getMessage().contains("Obstructed equipment running clearance"),
          "Wrong rejection for " + claim + ": " + expected.getMessage());
      return;
    }
    h.fail("Accepted " + claim);
  }
}
