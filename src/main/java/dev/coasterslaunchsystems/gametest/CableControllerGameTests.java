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
import dev.coasterslaunchsystems.runtime.NativeCableRig;
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
  private static final class Scene {
    final RideControllerGameTests.Scene ride;
    final String mode;
    final List<NativeFinClamp> preparation = new ArrayList<>();
    final BlockPos flywheel, motor;
    double peakSpeed, initialEnergy, minimumRunningEnergy = Double.POSITIVE_INFINITY, finalDistance;
    UUID carriage;
    boolean running, braking;
    String failure = "";
    Scene(GameTestHelper helper, int slot, String mode) {
      this.mode = mode;
      ride = new RideControllerGameTests.Scene(helper, slot, mode, 224);
      flywheel = ride.base.from.offset(-1, -2, 8); motor = flywheel.west();
    }
    EquipmentCabinetEntity cabinet() { return (EquipmentCabinetEntity) ride.base.level.getBlockEntity(flywheel); }
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
          s.minimumRunningEnergy = Math.min(s.minimumRunningEnergy, s.cabinet().flywheel().energy());
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
    var modes = new ArrayList<>(List.of("ordinary_controller_dispatch"));
    if (net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) modes.add("arrow_controller_dispatch");
    // This shared server keeps upstream 4D rail metadata after a fixture closes.
    // Keep controller rails away from the cable-mechanics suite's slots 0..14.
    var tests = new ArrayList<TestFunction>(); int slot = 17;
    for (var mode : modes) { int selected = slot++; tests.add(new TestFunction("launchsystems_cable_controller",
        "coasterslaunchsystems_cable." + mode, "coasterslaunchsystems_cable:empty", 1350, 0, true,
        helper -> run(new Scene(helper, selected, mode)))); }
    return tests;
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
      level.setBlockAndUpdate(s.flywheel, EquipmentContent.FLYWHEEL.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
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
          && s.cabinet().cableInUse() && s.cabinet().cableInstalled() && s.cabinet().flywheel().energy() > 750,
          "Placed cable controller did not hold and engage: " + r.controller.status() + " " + s.cabinet().status()
              + " installed=" + s.cabinet().cableInstalled() + " catch=" + s.cabinet().cableInUse()
              + " clamps=" + EquipmentRuntime.activeFinClamps(level, r.base.parents.getFirst().getUniqueId())
              + " ready=" + MotionProviders.resolve(level, r.base.parents.getFirst().getUniqueId()).orElseThrow().dispatchReady()
              + " speed=" + r.base.speed(r.base.parents.getFirst()) + " " + s.failure);
      s.carriage = s.cabinet().cableCarriage();
      var ticket = s.carriage == null ? null : SubLevelContainer.getContainer(level).getAllTickets().get(s.carriage);
      r.base.helper.assertTrue(ticket != null && ticket.tickets().stream().anyMatch(t -> t.getType().equals(CableTickets.CARRIAGE)
          && new CableTickets.Key(s.carriage, s.flywheel).equals(t.getKey())), "Installed native carriage has no registered loading ticket");
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
      s.initialEnergy = s.cabinet().flywheel().energy(); r.high(true);
    });
    s.at(425, () -> r.high(false));
    s.at(1120, () -> {
      r.base.helper.assertTrue(s.failure.isEmpty() && s.running && s.braking && s.peakSpeed > .5 && s.finalDistance > 21.5
          && s.minimumRunningEnergy < s.initialEnergy && !s.cabinet().cableInUse()
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
          + " energy=" + s.initialEnergy + "->" + s.cabinet().flywheel().energy());
      s.cleanup(); r.base.helper.succeed();
    });
  }
}
