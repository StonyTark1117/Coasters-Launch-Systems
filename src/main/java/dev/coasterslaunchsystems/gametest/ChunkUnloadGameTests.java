package dev.coasterslaunchsystems.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.ChunkPos;
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
    return List.of(new TestFunction("launchsystems_chunk_unload", "coasterslaunchsystems_chunk_unload.releases_remote_forced_chunk",
        "coasterslaunchsystems_chunk_unload:empty", 160, 0, true, ChunkUnloadGameTests::run));
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
