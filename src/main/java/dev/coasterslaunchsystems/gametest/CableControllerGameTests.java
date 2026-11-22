package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.runtime.EquipmentRuntime;
import dev.coasterslaunchsystems.runtime.CableTickets;
import dev.coasterslaunchsystems.runtime.NativeFinClamp;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.coasterslaunchsystems.runtime.NativeCableRig;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** The placed controller, charged cabinet, launch pad and stop bank perform the whole ride. */
@GameTestHolder("coasterslaunchsystems_cable")
public final class CableControllerGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static final String PRESSURE_RETURN_SHAFT_LOSS_MODE = System.getProperty("coasterslaunchsystems.cableReturnShaftLoss", "");
  private static final boolean PRESSURE_RETURN_SHAFT_LOSS = !PRESSURE_RETURN_SHAFT_LOSS_MODE.isBlank()
      && !PRESSURE_RETURN_SHAFT_LOSS_MODE.equalsIgnoreCase("false");
  private static final boolean ARROW_PRESSURE_RETURN_SHAFT_LOSS = PRESSURE_RETURN_SHAFT_LOSS_MODE.equalsIgnoreCase("arrow");
  private static final class Scene {
    final RideControllerGameTests.Scene ride;
    final String mode;
    final List<NativeFinClamp> preparation = new ArrayList<>();
    final BlockPos flywheel, motor;
    final Mechanism mechanism;
    double peakSpeed, initialEnergy, minimumRunningEnergy = Double.POSITIVE_INFINITY, finalDistance;
    UUID carriage;
    boolean running, braking, interrupted;
    String failure = "";
    Scene(GameTestHelper helper, int slot, String mode) {
      this.mode = mode;
      mechanism = mode.contains("hydraulic") ? Mechanism.HYDRAULIC
          : mode.contains("pneumatic") ? Mechanism.PNEUMATIC : Mechanism.FLYWHEEL;
      ride = new RideControllerGameTests.Scene(helper, slot, mode, 224);
      flywheel = ride.base.from.offset(-1, -2, 8); motor = flywheel.west();
    }
    EquipmentCabinetEntity cabinet() { return (EquipmentCabinetEntity) ride.base.level.getBlockEntity(flywheel); }
    double cableEnergy() { return cabinet().cableEnergy(); }
    void at(int tick, Runnable action) { ride.at(tick, action); }
    void cleanup() { SCENES.remove(this); preparation.forEach(NativeFinClamp::close); preparation.clear(); ride.cleanup(); }
  }
  private static void observe(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      var r = s.ride;
      if (r.closed || r.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        if (r.controller.phase() == RideControllerEntity.State.RUNNING) {
          s.running = true;
          s.minimumRunningEnergy = Math.min(s.minimumRunningEnergy, s.cableEnergy());
          s.preparation.forEach(NativeFinClamp::close); s.preparation.clear();
        }
        if (r.controller.phase() == RideControllerEntity.State.BRAKING) s.braking = true;
        for (var body : r.base.parents) {
          var contact = EngagedContacts.current(r.base.level, body).orElseThrow();
          s.finalDistance = Math.max(s.finalDistance, r.track.distance(contact.hit()));
          s.peakSpeed = Math.max(s.peakSpeed, Math.abs(r.base.speed(body)));
        }
      } catch (RuntimeException failure) { if (s.failure.isEmpty()) s.failure = failure.toString(); }
    }
  }
  @GameTestGenerator public static List<TestFunction> ride() {
    var modes = PRESSURE_RETURN_SHAFT_LOSS
        ? new ArrayList<>(ARROW_PRESSURE_RETURN_SHAFT_LOSS
            ? List.of("arrow_hydraulic_controller_return_shaft_loss", "arrow_pneumatic_controller_return_shaft_loss")
            : List.of("ordinary_hydraulic_controller_return_shaft_loss", "ordinary_pneumatic_controller_return_shaft_loss"))
        : new ArrayList<>(List.of("ordinary_controller_dispatch", "ordinary_controller_power_loss"));
    if (!PRESSURE_RETURN_SHAFT_LOSS && net.neoforged.fml.ModList.get().isLoaded("fourthdimension"))
      // The Arrow fixture has two native-coordinate-safe slots; use them for the new pressure adapters.
      modes.addAll(List.of("arrow_hydraulic_controller_shaft_loss", "arrow_pneumatic_controller_shaft_loss",
          "arrow_hydraulic_controller_guide_loss", "arrow_pneumatic_controller_guide_loss"));
    else if (!PRESSURE_RETURN_SHAFT_LOSS)
      modes.addAll(List.of("ordinary_hydraulic_controller_shaft_loss", "ordinary_pneumatic_controller_shaft_loss",
          "ordinary_hydraulic_controller_guide_loss", "ordinary_pneumatic_controller_guide_loss"));
    // This shared server keeps upstream 4D rail metadata after a fixture closes.
    // Keep ordinary controller rails away from the cable-mechanics suite's slots 0..14.
    // Fourth Dimension guide-loss fixtures use their own compact negative lane.
    var tests = new ArrayList<TestFunction>(); int slot = ARROW_PRESSURE_RETURN_SHAFT_LOSS ? 19 : 17, guideSlot = 15, fourthDimensionGuideSlot = -1;
    for (var mode : modes) {
      int selected = mode.endsWith("_guide_loss") ? (mode.startsWith("arrow") ? fourthDimensionGuideSlot-- : guideSlot++) : slot++;
      tests.add(new TestFunction("launchsystems_cable_controller",
        "coasterslaunchsystems_cable." + mode, "coasterslaunchsystems_cable:empty", 1350, 0, true,
        helper -> run(new Scene(helper, selected, mode)))); }
    return tests;
  }
  static TestFunction currentCase(String mode, int slot) {
    return new TestFunction("launchsystems_cable_current", "coasterslaunchsystems_cable_current." + mode,
        "coasterslaunchsystems_cable_current:empty", 1350, 0, true,
        helper -> run(new Scene(helper, slot, mode)));
  }

  private static void run(Scene s) {
    var r = s.ride; var level = r.base.level;
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CableControllerGameTests::observe); registered = true; }
    s.at(25, () -> {
      var track = DirectedTrack.resolve(level, r.base.from, r.base.to).orElseThrow();
      for (int step = 0; step <= 128; step++)
        level.setBlockAndUpdate(NativeCableRig.guideBlockAt(level, track, track.length() * step / 128), EquipmentContent.CABLE_GUIDE.get().defaultBlockState());
      var plot = r.base.parents.getFirst().getPlot().getEmbeddedLevelAccessor();
      r.base.helper.assertTrue(plot.getBlockState(BlockPos.ZERO.below(2)).isAir(), "Cable socket would replace car hardware");
      plot.setBlock(BlockPos.ZERO.below(2), EquipmentContent.CATCH_SOCKET.get().defaultBlockState(), 3);
    });
    s.at(40, () -> { if (r.fourthDimension) r.boardFourthDimension(); else for (int seat = 0; seat < 4; seat++) r.base.board(r.base.parents.getFirst(), seat); });
    s.at(55, () -> {
      level.setBlockAndUpdate(r.controllerPosition, EquipmentContent.CONTROLLER.get().defaultBlockState());
      r.controller = (RideControllerEntity) level.getBlockEntity(r.controllerPosition);
      r.controller.bind(level, r.base.from, r.base.to);
      r.track = DirectedTrack.resolve(level, r.base.from, r.base.to).orElseThrow();
      var cabinetBlock = switch (s.mechanism) {
        case FLYWHEEL -> EquipmentContent.FLYWHEEL.get();
        case HYDRAULIC -> EquipmentContent.HYDRAULIC.get();
        case PNEUMATIC -> EquipmentContent.PNEUMATIC.get();
        default -> throw new IllegalStateException("Not a cable cabinet");
      };
      level.setBlockAndUpdate(s.flywheel, cabinetBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      level.setBlockAndUpdate(s.motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.EAST));
      ((CreativeMotorBlockEntity) level.getBlockEntity(s.motor)).generatedSpeed.setValue(64);
      r.equipment.add(s.flywheel); r.motors.add(s.motor);
      s.cabinet().bind(level, r.base.from, r.base.to); r.controller.link(s.cabinet()); s.cabinet().setArmed(true);
      r.module(8, true);
      for (int distance = 23; distance <= 43; distance += 2) r.module(distance, true);
      if (r.fourthDimension) for (var body : r.base.parents)
        s.preparation.add(new NativeFinClamp(SubLevelContainer.getContainer(level).physicsSystem().getPipeline(), body,
            VehicleInterfaces.brakePoint(body), new Vector3d(0, 0, 1), 128));
      r.controller.setArmed(true);
      SCENES.add(s);
    });
    s.at(420, () -> {
      r.base.helper.assertTrue(s.failure.isEmpty() && r.controller.phase() == RideControllerEntity.State.READY
          && s.cabinet().cableInUse() && s.cabinet().cableInstalled() && s.cabinet().cableReady(),
          "Placed cable controller did not hold and engage: " + r.controller.status() + " " + s.cabinet().status()
              + " installed=" + s.cabinet().cableInstalled() + " catch=" + s.cabinet().cableInUse()
              + " clamps=" + EquipmentRuntime.activeFinClamps(level, r.base.parents.getFirst().getUniqueId())
              + " ready=" + MotionProviders.resolve(level, r.base.parents.getFirst().getUniqueId()).orElseThrow().dispatchReady()
              + " speed=" + r.base.speed(r.base.parents.getFirst()) + " " + s.failure);
      s.carriage = s.cabinet().cableCarriage();
      var ticket = s.carriage == null ? null : SubLevelContainer.getContainer(level).getAllTickets().get(s.carriage);
      r.base.helper.assertTrue(ticket != null && ticket.tickets().stream().anyMatch(t -> CableTickets.typeOf(t).equals(CableTickets.CARRIAGE)
          && new CableTickets.Key(s.carriage, s.flywheel).equals(CableTickets.keyOf(t))), "Installed native carriage has no registered loading ticket");
      var captured = SubLevelContainer.getContainer(level).getAllTickets().get(r.base.parents.getFirst().getUniqueId());
      r.base.helper.assertTrue(captured != null && captured.tickets().stream().anyMatch(t -> CableTickets.typeOf(t) == CableTickets.CAPTURED_BODY
          && new CableTickets.Key(s.carriage, s.flywheel).equals(CableTickets.keyOf(t))),
          "Engaged train chassis has no restart loading ticket");
      var saved = s.cabinet().saveWithFullMetadata(level.registryAccess());
      var restored = (EquipmentCabinetEntity) BlockEntity.loadStatic(s.flywheel, s.cabinet().getBlockState(), saved, level.registryAccess());
      r.base.helper.assertTrue(restored != null && restored.cableInstalled() && s.carriage.equals(restored.cableCarriage())
          && Math.abs(restored.cableHome() - s.cabinet().cableHome()) < 1e-8
          && !restored.armed() && restored.fault().contains("inspected recovery"),
          "Saved cable journal did not retain native carriage identity, home or inspection state: "
              + (restored == null ? "null" : "installed=" + restored.cableInstalled() + " id=" + restored.cableCarriage()
                  + " home=" + restored.cableHome() + " armed=" + restored.armed() + " fault=" + restored.fault()));
      var legacyTag = saved.copy(); legacyTag.putInt("ls_schema", 5);
      legacyTag.remove("ls_cable_carriage"); legacyTag.remove("ls_cable_home");
      var legacy = (EquipmentCabinetEntity) BlockEntity.loadStatic(s.flywheel, s.cabinet().getBlockState(), legacyTag, level.registryAccess());
      r.base.helper.assertTrue(legacy != null && legacy.cableInstalled() && legacy.cableCarriage() == null
          && !legacy.armed() && legacy.fault().contains("Legacy cable installation"),
          "A pre-identity cable journal invented a native carriage or silently rearmed");
      s.initialEnergy = s.cableEnergy(); r.high(true);
    });
    s.at(425, () -> r.high(false));
    if (s.mode.endsWith("_controller_power_loss")) {
      s.at(470, () -> {
        r.base.helper.assertTrue(s.running && s.cabinet().cableInUse(),
            "Placed cable controller did not begin a physical pull before its shaft interruption");
        ((CreativeMotorBlockEntity) level.getBlockEntity(s.motor)).generatedSpeed.setValue(0);
        s.interrupted = true;
      });
      s.at(650, () -> {
        r.base.helper.assertTrue(s.failure.isEmpty() && s.interrupted && r.controller.phase() == RideControllerEntity.State.FAULT
            && s.cabinet().cableInstalled() && s.cabinet().cableInUse() && !s.cabinet().armed()
            && !s.cabinet().fault().isEmpty() && r.base.parents.stream().allMatch(body -> Math.abs(r.base.speed(body)) < .03),
            "Placed cable shaft loss did not retain and settle its faulted physical catch: " + r.controller.status()
                + " " + s.cabinet().status() + " speed=" + r.base.speed(r.base.parents.getFirst()) + " " + s.failure);
        System.out.println("LS_CABLE_CONTROLLER mode=" + s.mode + " phase=" + r.controller.phase()
            + " retained=" + s.cabinet().cableInUse() + " speed=" + r.base.speed(r.base.parents.getFirst())
            + " fault=" + s.cabinet().fault());
        s.cleanup(); r.base.helper.succeed();
      });
      return;
    }
    if (s.mode.endsWith("_controller_shaft_loss")) {
      s.at(470, () -> {
        r.base.helper.assertTrue(s.running && s.cabinet().cableInUse(),
            "Pressure cable controller did not begin a physical pull before its charging-shaft interruption");
        ((CreativeMotorBlockEntity) level.getBlockEntity(s.motor)).generatedSpeed.setValue(0);
        s.interrupted = true;
      });
      s.at(550, () -> {
        r.base.helper.assertTrue(s.failure.isEmpty() && s.interrupted && s.cabinet().fault().isEmpty()
            && (r.controller.phase() == RideControllerEntity.State.RUNNING || r.controller.phase() == RideControllerEntity.State.BRAKING)
            && s.cabinet().cableInUse(),
            "Stored pressure did not retain the physical pull after its charging shaft stopped: " + r.controller.status()
                + " " + s.cabinet().status() + " " + s.failure);
        ((CreativeMotorBlockEntity) level.getBlockEntity(s.motor)).generatedSpeed.setValue(64);
      });
    }
    if (s.mode.endsWith("_controller_guide_loss")) {
      s.at(470, () -> {
        r.base.helper.assertTrue(s.running && s.cabinet().cableInUse(),
            "Pressure cable controller did not begin a physical pull before guide support was removed");
        var missingBed = NativeCableRig.guideBlockAt(level, r.track, s.cabinet().cableHome() + 10);
        level.removeBlock(missingBed, false);
        s.interrupted = true;
      });
      s.at(620, () -> {
        r.base.helper.assertTrue(s.failure.isEmpty() && s.interrupted && r.controller.phase() == RideControllerEntity.State.FAULT
            && s.cabinet().cableInstalled() && s.cabinet().cableInUse() && !s.cabinet().armed() && !s.cabinet().fault().isEmpty()
            && r.base.parents.stream().allMatch(body -> !CoasterCartTrackSnap.isBearingDisengaged(body) && Math.abs(r.base.speed(body)) < .03),
            "Broken guide support did not retain and settle the pressure catch: " + r.controller.status() + " "
                + s.cabinet().status() + " speed=" + r.base.speed(r.base.parents.getFirst()) + " " + s.failure);
        System.out.println("LS_CABLE_CONTROLLER mode=" + s.mode + " phase=" + r.controller.phase()
            + " retained=" + s.cabinet().cableInUse() + " speed=" + r.base.speed(r.base.parents.getFirst())
            + " fault=" + s.cabinet().fault());
        s.cleanup(); r.base.helper.succeed();
      });
      return;
    }
    if (s.mode.endsWith("_controller_return_shaft_loss")) {
      s.at(700, () -> {
        r.base.helper.assertTrue(s.failure.isEmpty() && s.running && s.cabinet().cableInUse()
            && s.cabinet().cablePhase() == NativeCableRig.Phase.RETURNING && s.cabinet().fault().isEmpty(),
            "Pressure cable carriage did not enter its physical return before shaft interruption: " + r.controller.status()
                + " " + s.cabinet().status() + " phase=" + s.cabinet().cablePhase() + " " + s.failure);
        ((CreativeMotorBlockEntity) level.getBlockEntity(s.motor)).generatedSpeed.setValue(0);
        s.interrupted = true;
      });
      s.at(800, () -> {
        r.base.helper.assertTrue(s.failure.isEmpty() && s.interrupted && s.cabinet().cableInUse()
            && s.cabinet().cablePhase() == NativeCableRig.Phase.RETURNING && s.cabinet().fault().isEmpty(),
            "Pressure cable return did not retain its shaft-powered carriage while the charging shaft was unavailable: "
                + r.controller.status() + " " + s.cabinet().status() + " phase=" + s.cabinet().cablePhase() + " " + s.failure);
        ((CreativeMotorBlockEntity) level.getBlockEntity(s.motor)).generatedSpeed.setValue(64);
      });
    }
    s.at(1120, () -> {
      r.base.helper.assertTrue(s.failure.isEmpty() && s.running && s.braking && s.peakSpeed > .5 && s.finalDistance > 21.5
          && s.minimumRunningEnergy < s.initialEnergy && !s.cabinet().cableInUse()
          && r.base.parents.stream().noneMatch(body -> {
            var info = SubLevelContainer.getContainer(level).getAllTickets().get(body.getUniqueId());
            return info != null && info.tickets().stream().anyMatch(t -> CableTickets.typeOf(t) == CableTickets.CAPTURED_BODY);
          })
          && r.controller.phase() == RideControllerEntity.State.HOLD
          && r.base.parents.stream().allMatch(body -> Math.abs(r.base.speed(body)) < .03),
          "Placed cable dispatch did not launch, release, return and enter the stop bank: " + r.controller.status()
              + " " + s.cabinet().status() + " running=" + s.running + " braking=" + s.braking
              + " peak=" + s.peakSpeed + " distance=" + s.finalDistance + " energy=" + s.initialEnergy + "->" + s.minimumRunningEnergy + " " + s.failure);
      r.controller.setArmed(false);
    });
    s.at(1130, () -> {
      r.base.helper.assertTrue(!s.cabinet().cableInstalled() && s.cabinet().cableCarriage() == null
          && !SubLevelContainer.getContainer(level).getAllTickets().containsKey(s.carriage)
          && SubLevelContainer.getContainer(level).getSubLevel(s.carriage) == null,
          "Disarmed controller did not retire its physically home carriage and loading ticket");
      System.out.println("LS_CABLE_CONTROLLER mode=" + s.mode + " phase=" + r.controller.phase() + " peak_speed=" + s.peakSpeed + " distance=" + s.finalDistance
          + " energy=" + s.initialEnergy + "->" + s.cableEnergy());
      s.cleanup(); r.base.helper.succeed();
    });
  }
}
