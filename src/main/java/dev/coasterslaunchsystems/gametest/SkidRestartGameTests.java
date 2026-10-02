package dev.coasterslaunchsystems.gametest;

import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Separate server processes prove a live skid interruption persists heat but fails closed before any stale shoe request. */
@GameTestHolder("coasterslaunchsystems_skid_restart")
public final class SkidRestartGameTests {
  private static final int SLOT = 15;
  private static final BlockPos FROM = new BlockPos(32 + SLOT * 48, 224, -704);
  private static final BlockPos TO = FROM.offset(0, 0, 64);
  private static final BlockPos CABINET = FROM.offset(1, -2, 16);
  private static final List<ActiveFixture> ACTIVE = new ArrayList<>();
  private static boolean registered;

  private static final class ActiveFixture {
    final ServerLevel level;
    final ServerSubLevel chassis;
    boolean drive = true;
    String failure = "";
    ActiveFixture(ServerLevel level, ServerSubLevel chassis) { this.level = level; this.chassis = chassis; }
  }

  private SkidRestartGameTests() {}

  private static void beforePhysics(ForgeSablePrePhysicsTickEvent event) {
    for (var fixture : List.copyOf(ACTIVE)) {
      if (fixture.level != event.getPhysicsSystem().getLevel() || !fixture.drive || fixture.chassis.isRemoved()) continue;
      try {
        var direction = new Vector3d(0, 0, 1);
        NativeForces.pointForce(fixture.chassis, fixture.chassis.getMassTracker().getCenterOfMass(), direction.mul(96), event.getTimeStep());
      } catch (RuntimeException failure) { fixture.failure = failure.toString(); }
    }
  }

  @GameTestGenerator
  public static List<TestFunction> restart() {
    String phase = System.getProperty("coasterslaunchsystems.skidRestartPhase", "");
    if (phase.isEmpty()) return List.of();
    return List.of(new TestFunction("launchsystems_skid_restart", "coasterslaunchsystems_skid_restart.saved_skid_restart",
        "coasterslaunchsystems_skid_restart:empty", 180, 0, true, SkidRestartGameTests::run));
  }

  private static void run(GameTestHelper helper) {
    String phase = System.getProperty("coasterslaunchsystems.skidRestartPhase", "");
    helper.assertTrue(phase.equals("prepare") || phase.equals("verify"), "Set lsSkidRestartPhase to prepare or verify");
    if (phase.equals("prepare")) prepare(helper); else verify(helper);
  }

  private static void prepare(GameTestHelper helper) {
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SkidRestartGameTests::beforePhysics); registered = true; }
    var scene = new OrdinaryPassengerGameTests.Scene(helper, SLOT, 224, -704);
    var fixture = new ActiveFixture[1];
    helper.runAtTickTime(20, () -> {
      var chassis = scene.cart(8);
      scene.level.setBlockAndUpdate(CABINET, EquipmentContent.SKID.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X));
      var cabinet = (EquipmentCabinetEntity) scene.level.getBlockEntity(CABINET);
      cabinet.bind(scene.level, FROM, TO);
      cabinet.setArmed(true);
      fixture[0] = new ActiveFixture(scene.level, chassis); ACTIVE.add(fixture[0]);
    });
    helper.runAtTickTime(70, () -> fixture[0].drive = false);
    helper.runAtTickTime(100, () -> {
      var cabinet = (EquipmentCabinetEntity) scene.level.getBlockEntity(CABINET);
      helper.assertTrue(fixture[0].failure.isEmpty() && cabinet.heat() > 1 && cabinet.armed(),
          "Live skid interruption did not accumulate persisted heat: " + fixture[0].failure + " heat=" + cabinet.heat());
      helper.assertTrue(MotionProviders.resolve(scene.level, fixture[0].chassis.getUniqueId()).orElseThrow().interfaces().contains(Mechanism.SKID_BRAKE),
          "Live skid shoe was not certified before the saved-world interruption");
      System.out.println("LS_SKID_RESTART prepared cabinet=" + cabinet.identity() + " heat=" + cabinet.heat()
          + " chassis=" + fixture[0].chassis.getUniqueId());
      ACTIVE.remove(fixture[0]);
      helper.succeed();
    });
  }

  private static void verify(GameTestHelper helper) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(CABINET);
      var reopenedSkidChassis = SubLevelContainer.getContainer(level).getAllSubLevels().stream()
          .filter(ServerSubLevel.class::isInstance).map(ServerSubLevel.class::cast)
          .anyMatch(body -> VehicleInterfaces.skidPoint(body) != null);
      helper.runAtTickTime(100, () -> {
        helper.assertTrue(cabinet != null && !cabinet.armed() && cabinet.heat() > 0
            && cabinet.fault().contains("paused after reload") && !reopenedSkidChassis,
            "Saved skid interruption did not retain heat or fail closed without a force-loaded shoe: "
                + (cabinet == null ? "missing" : cabinet.status()));
        System.out.println("LS_SKID_RESTART verified cabinet=" + cabinet.identity() + " heat=" + cabinet.heat()
            + " reopened_shoe=" + reopenedSkidChassis + " fault=" + cabinet.fault());
        helper.succeed();
      });
    });
  }
}
