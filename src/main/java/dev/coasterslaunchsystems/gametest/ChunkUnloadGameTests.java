package dev.coasterslaunchsystems.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Verifies that lifecycle fixtures can observe a genuine server chunk unload after releasing its forced ticket. */
@GameTestHolder("coasterslaunchsystems_chunk_unload")
public final class ChunkUnloadGameTests {
  // This lies outside every GameTest structure and its loading tickets.
  private static final BlockPos REMOTE = new BlockPos(4096, 128, -4096);

  private ChunkUnloadGameTests() {}

  @GameTestGenerator
  public static List<TestFunction> unloadProbe() {
    if (!Boolean.getBoolean("coasterslaunchsystems.chunkUnloadProbe")) return List.of();
    return List.of(
        new TestFunction("launchsystems_chunk_unload", "coasterslaunchsystems_chunk_unload.releases_remote_forced_chunk",
            "coasterslaunchsystems_chunk_unload:empty", 160, 0, true, ChunkUnloadGameTests::run),
        new TestFunction("launchsystems_chunk_unload", "coasterslaunchsystems_chunk_unload.reserved_active_cart_chunk_unavailable_faults_retains_lease",
            "coasterslaunchsystems_chunk_unload:empty", 360, 0, true, ChunkUnloadGameTests::runReservedActiveCart));
  }

  private static void runReservedActiveCart(GameTestHelper helper) {
    var scene = new RideControllerGameTests.Scene(helper, 12, "chunk_unavailable");
    var level = scene.base.level;
    var controllerPosition = scene.base.from.offset(48, -2, 8);
    int controllerChunkX = Math.floorDiv(controllerPosition.getX(), 16);
    int controllerChunkZ = Math.floorDiv(controllerPosition.getZ(), 16);
    level.setChunkForced(controllerChunkX, controllerChunkZ, true);

    scene.at(40, () -> scene.base.board(scene.base.parents.getFirst(), 0));
    scene.at(55, () -> {
      level.setBlockAndUpdate(controllerPosition, dev.coasterslaunchsystems.content.EquipmentContent.CONTROLLER.get().defaultBlockState());
      scene.controller = (dev.coasterslaunchsystems.content.RideControllerEntity) level.getBlockEntity(controllerPosition);
      scene.controller.bind(level, scene.base.from, scene.base.to);
      scene.track = dev.coasterslaunchsystems.track.DirectedTrack.resolve(level, scene.base.from, scene.base.to).orElseThrow();
      for (int distance = 8; distance <= 16; distance += 2) { scene.module(distance, false); scene.module(distance, true); }
      for (int distance = 21; distance <= 41; distance += 2) scene.module(distance, true);
      scene.controller.setArmed(true);
    });
    scene.at(80, () -> {
      helper.assertTrue(scene.controller.phase() == dev.coasterslaunchsystems.content.RideControllerEntity.State.READY
              && scene.controller.reservation().contains(scene.base.parents.getFirst().getUniqueId()),
          "Reserved controller was not ready before the scheduled chunk loss: " + scene.controller.status());
      level.setBlockAndUpdate(controllerPosition.above(), Blocks.REDSTONE_BLOCK.defaultBlockState());
    });
    scene.at(95, () -> {
      helper.assertTrue(scene.controller.phase() == dev.coasterslaunchsystems.content.RideControllerEntity.State.RUNNING,
          "Controller did not enter active running state before the scheduled chunk loss: " + scene.controller.status());
      forceSceneTrack(level, scene, false);
    });
    scene.at(170, () -> {
      var chassis = scene.base.parents.getFirst().getUniqueId();
      helper.assertTrue(dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level).getSubLevel(chassis) == null,
          "Reserved active cart body did not become unavailable through the normal chunk scheduler");
      helper.assertTrue(scene.controller.phase() == dev.coasterslaunchsystems.content.RideControllerEntity.State.FAULT
              && scene.controller.reservation().contains(chassis)
              && scene.controller.fault().contains("Reserved route chunk is unavailable"),
          "Scheduled cart unavailability did not fail closed with the reservation retained: " + scene.controller.status());
      System.out.println("LS_RESERVED_CHUNK_UNLOAD phase=faulted chassis=" + chassis + " reservation="
          + scene.controller.reservation() + " fault=" + scene.controller.fault());
      forceSceneTrack(level, scene, true);
    });
    scene.at(280, () -> {
      var chassis = scene.base.parents.getFirst().getUniqueId();
      helper.assertTrue(level.hasChunkAt(scene.base.from), "Track chunk did not reload for post-fault inspection");
      helper.assertTrue(scene.controller.phase() == dev.coasterslaunchsystems.content.RideControllerEntity.State.FAULT
              && scene.controller.reservation().contains(chassis),
          "Reload replayed or cleared the reserved controller fault: " + scene.controller.status());
      System.out.println("LS_RESERVED_CHUNK_UNLOAD phase=reloaded chassis=" + chassis + " reservation="
          + scene.controller.reservation() + " fault=" + scene.controller.fault());
      level.removeBlock(controllerPosition.above(), false);
      level.removeBlock(controllerPosition, false);
      for (var position : scene.equipment) level.removeBlock(position, false);
      for (var position : scene.motors) level.removeBlock(position, false);
      scene.base.cleanup();
      helper.succeed();
    });
  }

  private static void forceSceneTrack(net.minecraft.server.level.ServerLevel level, RideControllerGameTests.Scene scene, boolean forced) {
    for (int x = Math.floorDiv(scene.base.from.getX(), 16) - 1; x <= Math.floorDiv(scene.base.from.getX(), 16) + 1; x++)
      for (int z = Math.floorDiv(scene.base.from.getZ(), 16) - 1; z <= Math.floorDiv(scene.base.to.getZ(), 16) + 1; z++)
        level.setChunkForced(x, z, forced);
  }

  private static void run(GameTestHelper helper) {
    var level = helper.getLevel();
    var chunk = new ChunkPos(REMOTE);
    level.setChunkForced(chunk.x, chunk.z, true);
    helper.runAtTickTime(20, () -> {
      helper.assertTrue(level.hasChunkAt(REMOTE), "Remote forced chunk did not become loaded");
      level.setChunkForced(chunk.x, chunk.z, false);
    });
    helper.runAtTickTime(120, () -> {
      helper.assertTrue(!level.hasChunkAt(REMOTE), "Remote chunk stayed loaded after its forced ticket was released");
      System.out.println("LS_CHUNK_UNLOAD remote=" + chunk + " loaded=" + level.hasChunkAt(REMOTE));
      helper.succeed();
    });
  }
}
