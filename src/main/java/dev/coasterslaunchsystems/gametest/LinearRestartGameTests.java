package dev.coasterslaunchsystems.gametest;

import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.math.LinearMotor;
import dev.coasterslaunchsystems.math.Mechanism;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Separate server processes prove that a running linear section preserves state but cannot replay its field. */
@GameTestHolder("coasterslaunchsystems_linear_restart")
public final class LinearRestartGameTests {
  private static final int SLOT = 17;
  private static final BlockPos FROM = new BlockPos(32 + SLOT * 48, 192, -896);
  private static final BlockPos CONTROLLER = FROM.offset(4, -2, 8);

  private record RestartCase(String family, Mechanism mechanism) { boolean fourthDimension() { return !family.equals("ordinary"); } }

  private LinearRestartGameTests() {}

  @GameTestGenerator
  public static List<TestFunction> restart() {
    String phase = System.getProperty("coasterslaunchsystems.linearRestartPhase", "");
    if (phase.isEmpty()) return List.of();
    var test = restartCase(phase);
    if (test.fourthDimension() && !ModList.get().isLoaded("fourthdimension"))
      throw new IllegalStateException("Arrow linear restart validation requires the pinned Fourth Dimension adapter");
    return List.of(new TestFunction("launchsystems_linear_restart", "coasterslaunchsystems_linear_restart.saved_linear_restart_" + phase.toLowerCase(),
        "coasterslaunchsystems_linear_restart:empty", 700, 0, true, LinearRestartGameTests::run));
  }

  private static void run(GameTestHelper helper) {
    String phase = System.getProperty("coasterslaunchsystems.linearRestartPhase", "");
    var test = restartCase(phase);
    if (phase.startsWith("prepare")) prepare(helper, test); else verify(helper, test);
  }

  private static RestartCase restartCase(String phase) {
    return switch (phase) {
      case "prepare", "verify" -> new RestartCase("ordinary", Mechanism.LSM);
      case "prepareArrow", "verifyArrow" -> new RestartCase("arrow", Mechanism.LSM);
      case "prepareLIM", "verifyLIM" -> new RestartCase("ordinary", Mechanism.LIM);
      case "prepareArrowLIM", "verifyArrowLIM" -> new RestartCase("arrow", Mechanism.LIM);
      case "prepareLegacy", "verifyLegacy" -> new RestartCase("legacy", Mechanism.LSM);
      case "prepareLegacyLIM", "verifyLegacyLIM" -> new RestartCase("legacy", Mechanism.LIM);
      case "prepareFreeSpin", "verifyFreeSpin" -> new RestartCase("free_spin", Mechanism.LSM);
      case "prepareFreeSpinLIM", "verifyFreeSpinLIM" -> new RestartCase("free_spin", Mechanism.LIM);
      case "prepareRevised", "verifyRevised" -> new RestartCase("revised", Mechanism.LSM);
      case "prepareRevisedLIM", "verifyRevisedLIM" -> new RestartCase("revised", Mechanism.LIM);
      default -> throw new IllegalArgumentException("Set lsLinearRestartPhase to an ordinary, Arrow, Legacy, Free Spin or Revised prepare/verify mechanism pair");
    };
  }

  private static void prepare(GameTestHelper helper, RestartCase test) {
    var scene = new RideControllerGameTests.Scene(helper, SLOT, test.fourthDimension() ? test.family() + "_forward" : "single_forward", 192);
    scene.driveMechanism = test.mechanism();
    scene.at(40, () -> {
      if (test.fourthDimension()) scene.boardFourthDimension(); else for (int seat = 0; seat < 4; seat++) scene.base.board(scene.base.parents.getFirst(), seat);
    });
    scene.at(55, () -> {
      for (var body : scene.base.parents) body.getPlot().getEmbeddedLevelAccessor().setBlock(
          LinearMotorGameTests.reactionPosition(test.mechanism()), (test.mechanism() == Mechanism.LSM ? EquipmentContent.LSM_PLATE : EquipmentContent.LIM_PLATE).get().defaultBlockState(), 3);
      scene.base.level.setBlockAndUpdate(scene.controllerPosition, EquipmentContent.CONTROLLER.get().defaultBlockState());
      scene.controller = (RideControllerEntity) scene.base.level.getBlockEntity(scene.controllerPosition);
      scene.controller.bind(scene.base.level, scene.base.from, scene.base.to);
      for (int distance = 8; distance <= 16; distance += 2) { scene.module(distance, false); scene.module(distance, true); }
      for (int distance = 21; distance <= 61; distance += 2) scene.module(distance, true);
      scene.controller.setArmed(true);
    });
    scene.at(500, () -> {
      helper.assertTrue(scene.controller.phase() == RideControllerEntity.State.READY, "Linear restart fixture did not become ready: " + scene.controller.status());
    });
    scene.at(510, () -> scene.high(true));
    scene.at(550, () -> {
      var drives = linearDrives(helper);
      helper.assertTrue(scene.controller.phase() == RideControllerEntity.State.RUNNING && scene.controller.reservation().size() == 1
          && drives.size() == 5 && drives.stream().allMatch(cabinet -> cabinet.armed() && cabinet.energy() > 0 && cabinet.energy() < LinearMotor.CAPACITY),
          "Live linear section did not reach a persisted running state: " + scene.controller.status());
      System.out.println("LS_LINEAR_RESTART mode=" + test.family() + " mechanism=" + test.mechanism() + " prepared controller=" + scene.controller.identity() + " lease=" + scene.controller.reservation().size()
          + " drives=" + drives.size() + " energy=" + drives.getFirst().energy() + " heat=" + drives.getFirst().heat());
      helper.succeed();
    });
  }

  private static void verify(GameTestHelper helper, RestartCase test) {
    helper.runAtTickTime(40, () -> {
      var controller = (RideControllerEntity) helper.getLevel().getBlockEntity(CONTROLLER);
      var drives = linearDrives(helper);
      helper.runAtTickTime(100, () -> {
        boolean modulesMatch = controller != null && drives.stream().allMatch(cabinet -> controller.modules().stream().anyMatch(module ->
            module.position().equals(cabinet.getBlockPos()) && module.identity().equals(cabinet.identity())));
        helper.assertTrue(controller != null && !controller.armed() && controller.phase() == RideControllerEntity.State.INSPECTION
            && controller.reservation().size() == 1 && controller.modules().size() == 31 && controller.fault().contains("Reloaded controller requires inspection")
            && drives.size() == 5 && modulesMatch && drives.stream().allMatch(cabinet -> !cabinet.armed() && cabinet.energy() > 0
                && cabinet.energy() < LinearMotor.CAPACITY && cabinet.targetSpeed() == 3 && cabinet.accelerationLimit() == 2 && cabinet.jerkLimit() == 4),
            "Saved linear launch did not retain its lease/configuration while failing closed: "
                + (controller == null ? "missing controller" : controller.status()));
        System.out.println("LS_LINEAR_RESTART mode=" + test.family() + " mechanism=" + test.mechanism() + " verified controller=" + controller.identity() + " lease=" + controller.reservation().size()
            + " drives=" + drives.size() + " energy=" + drives.getFirst().energy() + " heat=" + drives.getFirst().heat());
        helper.succeed();
      });
    });
  }

  private static List<EquipmentCabinetEntity> linearDrives(GameTestHelper helper) {
    var drives = new ArrayList<EquipmentCabinetEntity>();
    for (int distance = 8; distance <= 16; distance += 2) {
      BlockPos position = FROM.offset(-1, -2, distance);
      if (helper.getLevel().getBlockEntity(position) instanceof EquipmentCabinetEntity cabinet && cabinet.mechanism().linear()) drives.add(cabinet);
    }
    return drives;
  }
}
