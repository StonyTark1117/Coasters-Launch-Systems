package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.math.FlywheelStore;
import dev.coasterslaunchsystems.math.EnergyStore;
import dev.coasterslaunchsystems.math.HydraulicAccumulator;
import dev.coasterslaunchsystems.math.AirReceiver;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.CableTickets;
import dev.coasterslaunchsystems.runtime.CableRecovery;
import dev.coasterslaunchsystems.runtime.NativeFinClamp;
import dev.coasterslaunchsystems.runtime.NativeCableRig;
import dev.coasterslaunchsystems.runtime.OrdinaryMotionProvider;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3d;

/** Two separate GameTest server processes inspect one saved native carriage world. */
@GameTestHolder("coasterslaunchsystems_cable_restart")
public final class CableRestartGameTests {
  // Controller pressure cases occupy slots 19/20; this slot stays below the native-coordinate limit and outside cable mechanics.
  private static final int SLOT = 15;
  private static final BlockPos FROM = new BlockPos(32 + SLOT * 48, 224, -896);
  private static final BlockPos FLYWHEEL = FROM.offset(-1, -2, 8);
  private static final BlockPos RECOVERY_CONTROLLER = FROM.offset(4, -2, 8);
  private static final BlockPos RECOVERY_FIN = FROM.offset(1, -2, 8);
  private static final BlockPos RECOVERY_FIN_MOTOR = RECOVERY_FIN.east();
  private static final List<ActiveFixture> ACTIVE = new ArrayList<>();
  private static boolean listenerRegistered;
  private static final class ActiveFixture {
    final net.minecraft.server.level.ServerLevel level;
    final EquipmentCabinetEntity cabinet;
    final ServerSubLevel cart;
    final DirectedTrack track;
    final NativeFinClamp preparation;
    final Mechanism mechanism;
    final EnergyStore pullStore;
    NativeCableRig rig;
    boolean engageRequested, pullRequested, pulling, saveMoving, saveReturning;
    double interruptionTravel = 5;
    String failure = "";
    ActiveFixture(net.minecraft.server.level.ServerLevel level, EquipmentCabinetEntity cabinet, ServerSubLevel cart,
        DirectedTrack track, NativeFinClamp preparation, Mechanism mechanism) {
      this.level = level; this.cabinet = cabinet; this.cart = cart; this.track = track; this.preparation = preparation;
      this.mechanism = mechanism;
      pullStore = switch (mechanism) {
        case FLYWHEEL -> new FlywheelStore(4, 50, 23);
        case HYDRAULIC -> new HydraulicAccumulator(4, 2, 250, 10, 1.4, 0);
        case PNEUMATIC -> new AirReceiver(4, 10, 200, 0);
        default -> throw new IllegalArgumentException("Restart fixture needs a cable mechanism");
      };
      if (mechanism != Mechanism.FLYWHEEL) pullStore.charge(pullStore.capacity());
    }
  }
  private CableRestartGameTests() {}

  private static void beforePhysics(ForgeSablePrePhysicsTickEvent event) {
    for (var fixture : List.copyOf(ACTIVE)) {
      if (fixture.level != event.getPhysicsSystem().getLevel() || !fixture.failure.isEmpty()) continue;
      try {
        if (fixture.rig == null) {
          var contact = EngagedContacts.current(fixture.level, fixture.cart).orElse(null);
          if (contact != null) {
            double home = fixture.track.distance(contact.hit());
            fixture.rig = new NativeCableRig(fixture.level, fixture.track, home, 12, 20, FLYWHEEL, fixture.mechanism);
            fixture.cabinet.installCable(fixture.rig);
          }
        }
        if (fixture.engageRequested && fixture.rig != null) {
          fixture.engageRequested = false;
          fixture.cabinet.engageCable(fixture.rig, fixture.cart.getUniqueId());
        }
        if (fixture.pullRequested && fixture.rig != null) {
          fixture.pullRequested = false;
          fixture.preparation.close();
          fixture.rig.beginPull(); fixture.pulling = true;
        }
        if (fixture.pulling) {
          fixture.rig.step(fixture.pullStore, 3, 0, event.getTimeStep());
          fixture.cabinet.observeCablePhase(fixture.rig);
          fixture.cabinet.observeCableMotion(fixture.rig);
          if (fixture.saveReturning && fixture.rig.phase() == NativeCableRig.Phase.RETURNING) {
            fixture.pulling = false;
            continue;
          }
          if (fixture.rig.travel() >= fixture.interruptionTravel) {
            if (!fixture.saveMoving) fixture.rig.emergencyHold("Saved-world interrupted pull fixture");
            fixture.pulling = false;
          }
        }
      } catch (RuntimeException failure) { fixture.failure = failure.toString(); failure.printStackTrace(); }
    }
  }

  @GameTestGenerator public static List<TestFunction> restart() {
    String phase = System.getProperty("coasterslaunchsystems.cableRestartPhase", "");
    if (phase.isEmpty()) return List.of();
    return List.of(new TestFunction("launchsystems_cable_restart", "coasterslaunchsystems_cable_restart.saved_carriage_restart",
        "coasterslaunchsystems_cable_restart:empty", phase.startsWith("prepareActiveMidGuide") || phase.startsWith("prepareReturning") ? 1000
            : phase.startsWith("verifyReturning") ? 950
            : phase.startsWith("prepareActiveMoving") ? 700
            : phase.startsWith("prepareActive") ? 500 : 140, 0, true, CableRestartGameTests::run));
  }

  private static void run(GameTestHelper helper) {
    String phase = System.getProperty("coasterslaunchsystems.cableRestartPhase", "");
    helper.assertTrue(phase.equals("prepare") || phase.equals("verify") || phase.equals("reclaim")
        || phase.equals("verifyRetired") || phase.equals("prepareActive") || phase.equals("verifyActive")
        || phase.equals("prepareActiveMissingSocket") || phase.equals("verifyActiveMissingSocket")
        || phase.equals("prepareActiveArrow") || phase.equals("verifyActiveArrow")
        || phase.equals("prepareActiveMidGuide") || phase.equals("verifyActiveMidGuide")
        || phase.equals("prepareActiveMidGuideArrow") || phase.equals("verifyActiveMidGuideArrow")
        || (phase.equals("prepareActiveRelease") || phase.equals("prepareActiveReleaseArrow")) || phase.equals("prepareActiveReleaseHydraulic") || phase.equals("prepareActiveReleasePneumatic")
        || (phase.equals("verifyActiveRelease") || phase.equals("verifyActiveReleaseArrow")) || phase.equals("verifyActiveReleaseHydraulic") || phase.equals("verifyActiveReleasePneumatic")
        || (phase.equals("verifyActiveReleased") || phase.equals("verifyActiveReleasedArrow")) || phase.equals("verifyActiveReleasedHydraulic") || phase.equals("verifyActiveReleasedPneumatic")
        || phase.equals("prepareActiveMoving") || phase.equals("verifyActiveMoving")
        || phase.equals("prepareActiveMovingArrow") || phase.equals("verifyActiveMovingArrow")
        || phase.equals("prepareActiveHydraulic") || phase.equals("verifyActiveHydraulic")
        || phase.equals("prepareActivePneumatic") || phase.equals("verifyActivePneumatic")
        || phase.equals("prepareReturningHydraulic") || phase.equals("verifyReturningHydraulic")
        || phase.equals("prepareReturningPneumatic") || phase.equals("verifyReturningPneumatic"),
        "Set lsCableRestartPhase to an idle or active restart phase");
    if (phase.equals("prepare")) prepare(helper);
    else if (phase.equals("prepareActive") || phase.equals("prepareActiveMissingSocket") || phase.equals("prepareActiveArrow")
        || phase.equals("prepareActiveMidGuide") || phase.equals("prepareActiveMidGuideArrow") || (phase.equals("prepareActiveRelease") || phase.equals("prepareActiveReleaseArrow"))
        || phase.equals("prepareActiveReleaseHydraulic") || phase.equals("prepareActiveReleasePneumatic")
        || phase.equals("prepareActiveMoving") || phase.equals("prepareActiveMovingArrow")
        || phase.equals("prepareActiveHydraulic") || phase.equals("prepareActivePneumatic"))
      prepareActive(helper, phase.equals("prepareActiveMissingSocket"),
          phase.equals("prepareActiveArrow") || phase.equals("prepareActiveMidGuideArrow") || phase.equals("prepareActiveMovingArrow") || phase.equals("prepareActiveReleaseArrow"),
          phase.equals("prepareActiveMidGuide") || phase.equals("prepareActiveMidGuideArrow"),
          (phase.equals("prepareActiveRelease") || phase.equals("prepareActiveReleaseArrow")) || phase.equals("prepareActiveReleaseHydraulic") || phase.equals("prepareActiveReleasePneumatic"),
          phase.equals("prepareActiveMoving") || phase.equals("prepareActiveMovingArrow"),
          phase.equals("prepareActiveHydraulic") || phase.equals("prepareActiveReleaseHydraulic") ? Mechanism.HYDRAULIC
              : phase.equals("prepareActivePneumatic") || phase.equals("prepareActiveReleasePneumatic") ? Mechanism.PNEUMATIC : Mechanism.FLYWHEEL);
    else if (phase.equals("prepareReturningHydraulic") || phase.equals("prepareReturningPneumatic"))
      prepareActive(helper, false, false, false, false, false,
          phase.equals("prepareReturningHydraulic") ? Mechanism.HYDRAULIC : Mechanism.PNEUMATIC, true);
    else if (phase.equals("verifyActive") || phase.equals("verifyActiveArrow") || phase.equals("verifyActiveMidGuide")
        || phase.equals("verifyActiveMidGuideArrow"))
      verifyActive(helper, phase.startsWith("verifyActiveMidGuide"), false);
    else if (phase.equals("verifyActiveHydraulic") || phase.equals("verifyActivePneumatic")) verifyActive(helper, false, false);
    else if (phase.equals("verifyReturningHydraulic") || phase.equals("verifyReturningPneumatic")) verifyReturning(helper);
    else if (phase.equals("verifyActiveMoving") || phase.equals("verifyActiveMovingArrow")) verifyActive(helper, false, true);
    else if ((phase.equals("verifyActiveRelease") || phase.equals("verifyActiveReleaseArrow")) || phase.equals("verifyActiveReleaseHydraulic") || phase.equals("verifyActiveReleasePneumatic")) verifyActiveRelease(helper);
    else if ((phase.equals("verifyActiveReleased") || phase.equals("verifyActiveReleasedArrow")) || phase.equals("verifyActiveReleasedHydraulic") || phase.equals("verifyActiveReleasedPneumatic")) verifyActiveReleased(helper);
    else if (phase.equals("verifyActiveMissingSocket")) verifyActiveMissingSocket(helper);
    else if (phase.equals("verifyRetired")) verifyRetired(helper);
    else verify(helper, phase.equals("reclaim"));
  }

  private static void prepareActive(GameTestHelper helper, boolean missingSocket, boolean arrow, boolean midGuide, boolean release,
      boolean moving, Mechanism mechanism) {
    prepareActive(helper, missingSocket, arrow, midGuide, release, moving, mechanism, false);
  }

  private static void prepareActive(GameTestHelper helper, boolean missingSocket, boolean arrow, boolean midGuide, boolean release,
      boolean moving, Mechanism mechanism, boolean returning) {
    helper.assertTrue(!arrow || net.neoforged.fml.ModList.get().isLoaded("fourthdimension"),
        "The Arrow saved-world fixture requires the pinned Fourth Dimension provider");
    var ride = arrow ? new RideControllerGameTests.Scene(helper, SLOT, "arrow_controller_dispatch", 224) : null;
    var scene = arrow ? ride.base : new OrdinaryPassengerGameTests.Scene(helper, SLOT, 224, -896);
    var level = scene.level;
    var cart = new ServerSubLevel[] {arrow ? null : scene.cart(8)};
    var preparation = new NativeFinClamp[1];
    var fixture = new ActiveFixture[1];
    if (!listenerRegistered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CableRestartGameTests::beforePhysics); listenerRegistered = true; }
    helper.runAtTickTime(25, () -> {
      if (arrow) cart[0] = scene.parents.getFirst();
      var track = DirectedTrack.resolve(level, scene.from, scene.to).orElseThrow();
      for (int step = 0; step <= 128; step++)
        level.setBlockAndUpdate(NativeCableRig.guideBlockAt(level, track, track.length() * step / 128),
            EquipmentContent.CABLE_GUIDE.get().defaultBlockState());
      helper.assertTrue(cart[0].getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(2),
          EquipmentContent.CATCH_SOCKET.get().defaultBlockState(), 3), "Loaded cart socket did not install");
    });
    helper.runAtTickTime(40, () -> {
      if (arrow) ride.boardFourthDimension();
      else for (int seat = 0; seat < 4; seat++) scene.board(cart[0], seat);
    });
    helper.runAtTickTime(55, () -> {
      var cabinetBlock = switch (mechanism) {
        case FLYWHEEL -> EquipmentContent.FLYWHEEL.get();
        case HYDRAULIC -> EquipmentContent.HYDRAULIC.get();
        case PNEUMATIC -> EquipmentContent.PNEUMATIC.get();
        default -> throw new IllegalArgumentException("Restart fixture needs a cable cabinet");
      };
      level.setBlockAndUpdate(FLYWHEEL, cabinetBlock.defaultBlockState()
          .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      cabinet.bind(level, scene.from, scene.to);
      if (release) {
        level.setBlockAndUpdate(RECOVERY_CONTROLLER, EquipmentContent.CONTROLLER.get().defaultBlockState());
        var controller = (dev.coasterslaunchsystems.content.RideControllerEntity) level.getBlockEntity(RECOVERY_CONTROLLER);
        controller.bind(level, scene.from, scene.to); controller.link(cabinet);
        level.setBlockAndUpdate(RECOVERY_FIN, EquipmentContent.FIN.get().defaultBlockState()
            .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        level.setBlockAndUpdate(RECOVERY_FIN_MOTOR, AllBlocks.CREATIVE_MOTOR.getDefaultState()
            .setValue(DirectionalBlock.FACING, Direction.WEST));
        ((CreativeMotorBlockEntity) level.getBlockEntity(RECOVERY_FIN_MOTOR)).generatedSpeed.setValue(64);
        var fin = (EquipmentCabinetEntity) level.getBlockEntity(RECOVERY_FIN);
        fin.bind(level, scene.from, scene.to); controller.link(fin);
      }
      preparation[0] = new NativeFinClamp(SubLevelContainer.getContainer(level).physicsSystem().getPipeline(), cart[0],
          VehicleInterfaces.brakePoint(cart[0]), new Vector3d(0, 0, 1), 128);
    });
    helper.runAtTickTime(75, () -> {
      var track = DirectedTrack.resolve(level, scene.from, scene.to).orElseThrow();
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      fixture[0] = new ActiveFixture(level, cabinet, cart[0], track, preparation[0], mechanism); ACTIVE.add(fixture[0]);
      if (moving && arrow) fixture[0].interruptionTravel = 4;
      if (returning) fixture[0].interruptionTravel = 100;
    });
    helper.runAtTickTime(420, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(fixture[0].rig != null && fixture[0].failure.isEmpty()
          && fixture[0].rig.phase() == NativeCableRig.Phase.HOME
          && Math.abs(fixture[0].rig.travel()) < .03 && Math.abs(fixture[0].rig.speed()) < .03,
          "Native carriage was not home before occupied catch engagement: " + fixture[0].failure);
      fixture[0].engageRequested = true;
    });
    helper.runAtTickTime(430, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(cabinet.cableInstalled() && cabinet.cableInUse() && cart[0].getUniqueId().equals(cabinet.cableChassis())
          && fixture[0].rig.phase() == NativeCableRig.Phase.ENGAGED && scene.riders.stream().allMatch(pig -> pig.isPassenger())
          && fixture[0].failure.isEmpty(),
          "Occupied active cable journal or native catch was not established: " + fixture[0].failure);
      if (midGuide || moving || returning) {
        fixture[0].saveMoving = moving; fixture[0].saveReturning = returning; fixture[0].pullRequested = true; return;
      }
      if (release) {
        var controller = (dev.coasterslaunchsystems.content.RideControllerEntity) level.getBlockEntity(RECOVERY_CONTROLLER);
        controller.reserve(java.util.Set.of(cart[0].getUniqueId()));
        if (arrow) {
          controller.setArmed(true);
          helper.assertTrue(dev.coasterslaunchsystems.api.MotionProviders.reserveAuthority(level,
              cart[0].getUniqueId(), RECOVERY_CONTROLLER), "Current Arrow recovery fixture could not claim its real controller");
          controller.fail("Saved-world active catch inspection fixture");
        }
        helper.assertTrue(controller.reservation().equals(java.util.Set.of(cart[0].getUniqueId()))
            && (arrow ? !controller.fault().isEmpty() : !controller.armed()),
            "Active release fixture did not retain the captured controller reservation");
      }
      System.out.println("LS_CABLE_ACTIVE_RESTART prepared carriage=" + cabinet.cableCarriage()
          + " chassis=" + cabinet.cableChassis() + " socket_error=" + fixture[0].rig.socketError()
          + " guide_error=" + fixture[0].rig.guideError() + " removed_socket=" + missingSocket + " arrow=" + arrow
          + " mechanism=" + mechanism);
      if (missingSocket) helper.assertTrue(cart[0].getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(2),
          net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3),
          "Captured socket could not be removed before saved-world negative case");
      ACTIVE.remove(fixture[0]);
      helper.succeed();
    });
    if (midGuide) helper.runAtTickTime(820, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(fixture[0].failure.isEmpty() && !fixture[0].pulling
          && fixture[0].rig.phase() == NativeCableRig.Phase.FAULT
          && fixture[0].rig.travel() > 3 && fixture[0].rig.travel() < 12
          && Math.abs(fixture[0].rig.speed()) < .1 && fixture[0].rig.socketError() < .03
          && cabinet.cableInstalled() && cabinet.cableInUse(),
          "Interrupted cable pull did not settle at a retained mid-guide catch: travel="
              + fixture[0].rig.travel() + " speed=" + fixture[0].rig.speed()
              + " socket=" + fixture[0].rig.socketError() + " failure=" + fixture[0].failure);
      System.out.println("LS_CABLE_MID_GUIDE prepared carriage=" + cabinet.cableCarriage()
          + " chassis=" + cabinet.cableChassis() + " travel=" + fixture[0].rig.travel());
      ACTIVE.remove(fixture[0]); helper.succeed();
    });
    if (moving) helper.runAtTickTime(500, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(fixture[0].failure.isEmpty() && !fixture[0].pulling
          && fixture[0].rig.phase() == NativeCableRig.Phase.PULLING
          && fixture[0].rig.travel() > 3 && fixture[0].rig.travel() < 12
          && Math.abs(fixture[0].rig.speed()) > .2 && fixture[0].rig.socketError() < .03
          && cabinet.cableInstalled() && cabinet.cableInUse(),
          "Moving cable pull did not retain a bounded aligned save state: travel=" + fixture[0].rig.travel()
              + " speed=" + fixture[0].rig.speed() + " socket=" + fixture[0].rig.socketError()
              + " failure=" + fixture[0].failure);
      System.out.println("LS_CABLE_MOVING_RESTART prepared carriage=" + cabinet.cableCarriage()
          + " chassis=" + cabinet.cableChassis() + " travel=" + fixture[0].rig.travel()
          + " speed=" + fixture[0].rig.speed() + " journal_moving=" + cabinet.cableMoving() + " arrow=" + arrow);
      ACTIVE.remove(fixture[0]); helper.succeed();
    });
    if (returning) helper.runAtTickTime(820, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(fixture[0].failure.isEmpty() && !fixture[0].pulling
          && fixture[0].rig.phase() == NativeCableRig.Phase.RETURNING && fixture[0].rig.travel() > 3
          && Math.abs(fixture[0].rig.speed()) < .1 && cabinet.cableInstalled() && cabinet.cableInUse()
          && cabinet.cableReturning() && cabinet.cableChassis() == null && !cabinet.cableMoving(),
          "Released pressure carriage did not retain a bounded return journal: travel=" + fixture[0].rig.travel()
              + " speed=" + fixture[0].rig.speed() + " phase=" + fixture[0].rig.phase()
              + " returning=" + cabinet.cableReturning() + " failure=" + fixture[0].failure);
      System.out.println("LS_CABLE_RETURN_RESTART prepared carriage=" + cabinet.cableCarriage()
          + " travel=" + fixture[0].rig.travel() + " mechanism=" + mechanism);
      ACTIVE.remove(fixture[0]); helper.succeed();
    });
  }

  private static void verifyReturning(GameTestHelper helper) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      helper.assertTrue(cabinet != null && cabinet.cableInstalled() && cabinet.cableInUse() && cabinet.cableReturning()
          && cabinet.cableCarriage() != null && cabinet.cableChassis() == null && !cabinet.armed() && !cabinet.fault().isEmpty()
          && OrdinaryMotionProvider.ID.equals(cabinet.cableReturnProvider())
          && CableRecovery.stabilized(level, cabinet.cableCarriage()),
          "Saved released carriage did not reopen under its passive guide hold: " + (cabinet == null ? "missing" : cabinet.status()));
      level.setBlockAndUpdate(FLYWHEEL.west(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.EAST));
      ((CreativeMotorBlockEntity) level.getBlockEntity(FLYWHEEL.west())).generatedSpeed.setValue(64);
    });
    helper.runAtTickTime(100, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      CableRecovery.inspectResumeReturn(level, cabinet);
      helper.assertTrue(cabinet.cableReturning() && cabinet.cableInUse() && CableRecovery.stabilized(level, cabinet.cableCarriage()),
          "Inspected return resume lost its saved carriage before physical motion began");
    });
    helper.runAtTickTime(800, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var carriage = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel(cabinet.cableCarriage());
      var track = DirectedTrack.resolve(level, FROM, FROM.offset(0, 0, 64)).orElseThrow();
      var hit = track.hitAtDistance(level, cabinet.cableHome());
      var up = CoasterPathTrackFrame.trackBasisAtHit(level, hit).up();
      var expected = hit.point().add(up.scale(.3125 - 2));
      var center = carriage.getPlot().getCenterBlock().getCenter();
      var actual = NativeCableRig.worldPoint(carriage, new Vector3d(center.x, center.y, center.z));
      helper.assertTrue(cabinet.cableInstalled() && !cabinet.cableInUse() && !cabinet.cableReturning()
          && cabinet.cableChassis() == null && !cabinet.armed() && !cabinet.fault().isEmpty()
          && CableRecovery.stabilized(level, cabinet.cableCarriage()) && actual.distance(expected.x, expected.y, expected.z) < .03,
          "Inspected return did not reach its physical home: actual=" + actual + " expected=" + expected
              + " cabinet=" + cabinet.status());
      System.out.println("LS_CABLE_RETURN_RESTART verified carriage=" + cabinet.cableCarriage()
          + " separation=" + actual.distance(expected.x, expected.y, expected.z));
      helper.succeed();
    });
  }

  private static void verifyActiveMissingSocket(GameTestHelper helper) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      helper.assertTrue(level.getBlockEntity(FLYWHEEL) instanceof EquipmentCabinetEntity,
          "Saved active cabinet is missing in removed-socket restart case");
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var container = SubLevelContainer.getContainer(level);
      helper.assertTrue(cabinet.cableInstalled() && cabinet.cableInUse() && !cabinet.armed()
          && cabinet.cableCarriage() != null && cabinet.cableChassis() != null && !cabinet.fault().isEmpty()
          && CableRecovery.stabilized(level, cabinet.cableCarriage())
          && !CableRecovery.catchRestored(level, cabinet.cableCarriage(), cabinet.cableChassis())
          && container.getSubLevel(cabinet.cableChassis()) instanceof ServerSubLevel,
          "Removed socket did not leave the carriage passively held and catch joint denied");
      var chassis = (ServerSubLevel) container.getSubLevel(cabinet.cableChassis());
      helper.assertTrue(VehicleInterfaces.catchPoint(chassis) == null,
          "Saved removed socket unexpectedly reappeared");
      System.out.println("LS_CABLE_ACTIVE_RESTART denied_missing_socket carriage=" + cabinet.cableCarriage()
          + " chassis=" + cabinet.cableChassis() + " fault=" + cabinet.fault());
      helper.runAtTickTime(120, () -> {
        helper.assertTrue(CableRecovery.stabilized(level, cabinet.cableCarriage())
            && !CableRecovery.catchRestored(level, cabinet.cableCarriage(), cabinet.cableChassis()),
            "Removed socket acquired a catch joint or lost its passive carriage hold later in startup");
        helper.succeed();
      });
    });
  }

  private static void verifyActive(GameTestHelper helper, boolean midGuide, boolean moving) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      helper.assertTrue(level.getBlockEntity(FLYWHEEL) instanceof EquipmentCabinetEntity,
          "Saved active flywheel cabinet is missing after restart");
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var container = SubLevelContainer.getContainer(level);
      helper.assertTrue(cabinet.cableInstalled() && cabinet.cableInUse() && cabinet.cableCarriage() != null
          && cabinet.cableChassis() != null && !cabinet.armed() && !cabinet.fault().isEmpty(),
          "Saved active catch did not reload as a disarmed inspected journal: " + cabinet.status());
      helper.assertTrue(container.getSubLevel(cabinet.cableCarriage()) instanceof ServerSubLevel
          && container.getSubLevel(cabinet.cableChassis()) instanceof ServerSubLevel,
          "Captured train or carriage native body did not reload: carriage=" + container.getSubLevel(cabinet.cableCarriage())
              + " chassis=" + container.getSubLevel(cabinet.cableChassis())
              + " carriage_ticket=" + container.getAllTickets().get(cabinet.cableCarriage())
              + " chassis_ticket=" + container.getAllTickets().get(cabinet.cableChassis()));
      helper.assertTrue(CableRecovery.stabilized(level, cabinet.cableCarriage()),
          "Reloaded active catch has no passive native carriage guide and brake");
      helper.assertTrue(CableRecovery.catchRestored(level, cabinet.cableCarriage(), cabinet.cableChassis()),
          "Saved native catch joint was not reconstructed under inspection");
      var carriage = (ServerSubLevel) container.getSubLevel(cabinet.cableCarriage());
      var chassis = (ServerSubLevel) container.getSubLevel(cabinet.cableChassis());
      var center = carriage.getPlot().getCenterBlock().getCenter();
      var point = NativeCableRig.worldPoint(carriage, new Vector3d(center.x, center.y, center.z));
      var socket = NativeCableRig.worldPoint(chassis, VehicleInterfaces.catchPoint(chassis));
      if (midGuide || moving) {
        var track = DirectedTrack.resolve(level, FROM, FROM.offset(0, 0, 64)).orElseThrow();
        var homeHit = track.hitAtDistance(level, cabinet.cableHome());
        var up = CoasterPathTrackFrame.trackBasisAtHit(level, homeHit).up();
        var homePoint = homeHit.point().add(up.scale(.3125 - 2));
        var forward = track.forward(level, homeHit);
        double travel = new Vector3d(point).sub(homePoint.x, homePoint.y, homePoint.z)
            .dot(new Vector3d(forward.x, forward.y, forward.z));
        helper.assertTrue(travel > 3 && travel < 12, "Reloaded catch did not retain mid-guide travel: " + travel);
      }
      helper.assertTrue(point.distance(socket) < .03,
          "Reloaded occupied catch socket separated from its passively held carriage");
      System.out.println("LS_CABLE_ACTIVE_RESTART verified carriage=" + cabinet.cableCarriage()
          + " chassis=" + cabinet.cableChassis() + " separation=" + point.distance(socket)
          + " carriage_point=" + point + " socket=" + socket + " fault=" + cabinet.fault());
      helper.runAtTickTime(120, () -> {
        var held = NativeCableRig.worldPoint(carriage, new Vector3d(center.x, center.y, center.z));
        var captured = NativeCableRig.worldPoint(chassis, VehicleInterfaces.catchPoint(chassis));
        helper.assertTrue(CableRecovery.catchRestored(level, cabinet.cableCarriage(), cabinet.cableChassis())
            && CableRecovery.stabilized(level, cabinet.cableCarriage()) && held.distance(captured) < .03
            && (!(midGuide || moving) || held.distance(point) < .15) && !cabinet.armed(),
            "Reconstructed catch joint or passive hold did not survive startup inspection");
        if (moving) {
          var speed = SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
              .getLinearVelocity(carriage, new Vector3d()).length();
          helper.assertTrue(speed < .03, "Recovered moving carriage did not settle under its native guide brake: " + speed);
          helper.assertFalse(cabinet.cableMoving(), "Recovered moving catch retained its widened alignment journal");
        }
        helper.succeed();
      });
    });
  }

  private static void verifyActiveRelease(GameTestHelper helper) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var fin = (EquipmentCabinetEntity) level.getBlockEntity(RECOVERY_FIN);
      var controller = (dev.coasterslaunchsystems.content.RideControllerEntity) level.getBlockEntity(RECOVERY_CONTROLLER);
      System.out.println("LS_CABLE_ACTIVE_RELEASE reload cabinet=" + (cabinet == null ? "missing" : cabinet.status())
          + " fin=" + (fin == null ? "missing" : fin.status())
          + " controller=" + (controller == null ? "missing" : controller.phase() + "/" + controller.reservation() + "/" + controller.fault())
          + " restored=" + (cabinet != null && cabinet.cableChassis() != null
              && CableRecovery.catchRestored(level, cabinet.cableCarriage(), cabinet.cableChassis())));
      helper.assertTrue(cabinet != null && fin != null && controller != null && cabinet.cableInUse()
          && CableRecovery.catchRestored(level, cabinet.cableCarriage(), cabinet.cableChassis())
          && !controller.armed()
          && controller.reservation().equals(java.util.Set.of(cabinet.cableChassis())),
          "Saved active-release fixture did not retain its inspected catch and reservation");
      fin.setArmed(true);
    });
    helper.runAtTickTime(90, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var controller = (dev.coasterslaunchsystems.content.RideControllerEntity) level.getBlockEntity(RECOVERY_CONTROLLER);
      var container = SubLevelContainer.getContainer(level);
      var carriage = cabinet.cableCarriage(); var chassis = cabinet.cableChassis();
      helper.assertTrue(dev.coasterslaunchsystems.runtime.EquipmentRuntime.activeFinClamps(level, chassis) > 0,
          "Placed recovery fin did not establish an independent native clamp");
      CableRecovery.inspectReleaseActive(level, cabinet);
      var key = new CableTickets.Key(carriage, FLYWHEEL);
      helper.assertTrue(cabinet.cableInstalled() && !cabinet.cableInUse() && cabinet.cableChassis() == null
          && !cabinet.armed() && !controller.armed() && controller.reservation().isEmpty()
          && CableRecovery.stabilized(level, carriage) && !CableRecovery.catchRestored(level, carriage, chassis)
          && container.getAllTickets().entrySet().stream().noneMatch(entry -> entry.getValue().tickets().stream()
              .anyMatch(ticket -> CableTickets.typeOf(ticket) == CableTickets.CAPTURED_BODY && key.equals(CableTickets.keyOf(ticket)))),
          "Inspected active release left a catch, captured ticket or route reservation behind");
      System.out.println("LS_CABLE_ACTIVE_RELEASE verified carriage=" + carriage + " chassis=" + chassis
          + " fin_clamps=" + dev.coasterslaunchsystems.runtime.EquipmentRuntime.activeFinClamps(level, chassis));
      helper.succeed();
    });
  }

  private static void verifyActiveReleased(GameTestHelper helper) {
    var level = helper.getLevel();
    helper.runAtTickTime(40, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(FLYWHEEL);
      var controller = (dev.coasterslaunchsystems.content.RideControllerEntity) level.getBlockEntity(RECOVERY_CONTROLLER);
      System.out.println("LS_CABLE_ACTIVE_RELEASE persisted cabinet=" + (cabinet == null ? "missing" : cabinet.status())
          + " controller=" + (controller == null ? "missing" : controller.phase() + "/" + controller.reservation() + "/" + controller.fault())
          + " installed=" + (cabinet != null && cabinet.cableInstalled()) + " in_use=" + (cabinet != null && cabinet.cableInUse())
          + " carriage=" + (cabinet == null ? null : cabinet.cableCarriage()) + " chassis=" + (cabinet == null ? null : cabinet.cableChassis())
          + " armed=" + (cabinet != null && cabinet.armed()) + " release_fault=" + (cabinet != null && cabinet.fault().contains("released by inspection"))
          + " stable=" + (cabinet != null && cabinet.cableCarriage() != null && CableRecovery.stabilized(level, cabinet.cableCarriage())));
      helper.assertTrue(cabinet != null && controller != null && cabinet.cableInstalled() && !cabinet.cableInUse()
          && cabinet.cableCarriage() != null && cabinet.cableChassis() == null && !cabinet.armed()
          && cabinet.fault().contains("released by inspection") && controller.reservation().isEmpty()
          && CableRecovery.stabilized(level, cabinet.cableCarriage()),
          "Inspected active release did not persist as a disarmed idle carriage across restart");
      var key = new CableTickets.Key(cabinet.cableCarriage(), FLYWHEEL);
      helper.assertTrue(SubLevelContainer.getContainer(level).getAllTickets().entrySet().stream().noneMatch(entry -> entry.getValue().tickets().stream()
          .anyMatch(ticket -> CableTickets.typeOf(ticket) == CableTickets.CAPTURED_BODY && key.equals(CableTickets.keyOf(ticket)))),
          "Released active-catch tickets reappeared after restart");
      System.out.println("LS_CABLE_ACTIVE_RELEASE persisted carriage=" + cabinet.cableCarriage()
          + " controller_reservation=" + controller.reservation());
      helper.succeed();
    });
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
      helper.assertTrue(info != null && info.tickets().stream().anyMatch(ticket -> CableTickets.typeOf(ticket).equals(CableTickets.CARRIAGE)
          && new CableTickets.Key(id, FLYWHEEL).equals(CableTickets.keyOf(ticket))), "Native carriage loading ticket did not reload");
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
