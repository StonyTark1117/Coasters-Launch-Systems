package dev.coasterslaunchsystems.gametest;

import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.runtime.CableTickets;
import dev.coasterslaunchsystems.runtime.CableRecovery;
import dev.coasterslaunchsystems.runtime.NativeCableRig;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Two separate GameTest server processes inspect one saved native carriage world. */
@GameTestHolder("coasterslaunchsystems_cable")
public final class CableRestartGameTests {
  private static final int SLOT = 20;
  private static final BlockPos FROM = new BlockPos(32 + SLOT * 48, 224, -896);
  private static final BlockPos FLYWHEEL = FROM.offset(-1, -2, 8);
  private CableRestartGameTests() {}

  @GameTestGenerator public static List<TestFunction> restart() {
    if (System.getProperty("coasterslaunchsystems.cableRestartPhase", "").isEmpty()) return List.of();
    return List.of(new TestFunction("launchsystems_cable_restart", "coasterslaunchsystems_cable.saved_carriage_restart",
        "coasterslaunchsystems_cable:empty", 140, 0, true, CableRestartGameTests::run));
  }

  private static void run(GameTestHelper helper) {
    String phase = System.getProperty("coasterslaunchsystems.cableRestartPhase", "");
    helper.assertTrue(phase.equals("prepare") || phase.equals("verify") || phase.equals("reclaim")
        || phase.equals("verifyRetired"), "Set lsCableRestartPhase to prepare, verify, reclaim or verifyRetired");
    if (phase.equals("prepare")) prepare(helper);
    else if (phase.equals("verifyRetired")) verifyRetired(helper);
    else verify(helper, phase.equals("reclaim"));
  }

  private static void prepare(GameTestHelper helper) {
    var scene = new OrdinaryPassengerGameTests.Scene(helper, SLOT, 224, -896);
    var level = scene.level;
    helper.runAtTickTime(25, () -> {
      var track = DirectedTrack.resolve(level, scene.from, scene.to).orElseThrow();
      for (int step = 0; step <= 128; step++)
        level.setBlockAndUpdate(NativeCableRig.guideBlockAt(level, track, 8 + 20.0 * step / 128),
            EquipmentContent.CABLE_GUIDE.get().defaultBlockState());
      level.setBlockAndUpdate(FLYWHEEL, EquipmentContent.FLYWHEEL.get().defaultBlockState()
          .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      cabinet.bind(level, scene.from, scene.to);
      var rig = new NativeCableRig(level, track, 8, 12, 20, FLYWHEEL);
      cabinet.installCable(rig);
      helper.assertTrue(cabinet.cableInstalled() && !cabinet.cableInUse()
          && SubLevelContainer.getContainer(level).getAllTickets().containsKey(cabinet.cableCarriage()),
          "The installed idle carriage was not ticketed before save");
      System.out.println("LS_CABLE_RESTART prepared id=" + cabinet.cableCarriage() + " home=" + cabinet.cableHome()
          + " point=" + rig.carriagePoint());
      helper.succeed();
    });
  }

  private static void verify(GameTestHelper helper, boolean reclaim) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      helper.assertTrue(level.getBlockEntity(FLYWHEEL) instanceof EquipmentCabinetEntity,
          "Saved flywheel cabinet is missing; the GameTest world was not retained");
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var id = cabinet.cableCarriage();
      helper.assertTrue(cabinet.cableInstalled() && !cabinet.cableInUse() && id != null
          && !cabinet.armed() && cabinet.fault().contains("inspected recovery") && Math.abs(cabinet.cableHome() - 8) < 1e-8,
          "Saved idle carriage journal did not reload disarmed for inspection: " + cabinet.status());
      var container = SubLevelContainer.getContainer(level);
      var info = container.getAllTickets().get(id);
      helper.assertTrue(info != null && info.tickets().stream().anyMatch(ticket -> ticket.getType().equals(CableTickets.CARRIAGE)
          && new CableTickets.Key(id, FLYWHEEL).equals(ticket.getKey())), "Native carriage loading ticket did not reload");
      helper.assertTrue(container.getSubLevel(id) instanceof ServerSubLevel,
          "Sable did not load the saved native carriage body");
      var body = (ServerSubLevel) container.getSubLevel(id);
      helper.assertTrue(body.getPlot().getEmbeddedLevelAccessor().getBlockState(BlockPos.ZERO).is(EquipmentContent.CATCH_CARRIAGE.get()),
          "Reloaded native body is not the saved catch carriage");
      var track = DirectedTrack.resolve(level, FROM, FROM.offset(0, 0, 64)).orElseThrow();
      var hit = track.hitAtDistance(level, cabinet.cableHome());
      var up = CoasterPathTrackFrame.trackBasisAtHit(level, hit).up();
      var expected = hit.point().add(up.scale(.3125 - 2));
      var center = body.getPlot().getCenterBlock().getCenter();
      var actual = NativeCableRig.worldPoint(body, new org.joml.Vector3d(center.x, center.y, center.z));
      helper.assertTrue(CableRecovery.stabilized(level, id) && actual.distance(expected.x, expected.y, expected.z) < .15,
          "Reloaded idle carriage was not passively held at its saved physical home: " + actual + " expected=" + expected);
      if (reclaim) {
        CableRecovery.inspectRetireIdle(level, cabinet);
        helper.assertTrue(!cabinet.cableInstalled() && cabinet.cableCarriage() == null && !cabinet.fault().isEmpty()
            && !container.getAllTickets().containsKey(id) && container.getSubLevel(id) == null,
            "Inspected retirement left the carriage, ticket or installation journal behind");
      }
      System.out.println("LS_CABLE_RESTART verified id=" + id + " home=" + cabinet.cableHome()
          + " point=" + actual + " reclaimed=" + reclaim + " fault=" + cabinet.fault());
      helper.succeed();
    });
  }

  private static void verifyRetired(GameTestHelper helper) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      helper.assertTrue(level.getBlockEntity(FLYWHEEL) instanceof EquipmentCabinetEntity,
          "Retired cabinet is missing after the second restart");
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(!cabinet.cableInstalled() && !cabinet.cableInUse() && cabinet.cableCarriage() == null
          && !cabinet.armed() && cabinet.fault().contains("reclaimed by inspection"),
          "Inspected retirement did not persist disarmed across another restart: " + cabinet.status());
      System.out.println("LS_CABLE_RESTART retired journal persisted fault=" + cabinet.fault());
      helper.succeed();
    });
  }
}
