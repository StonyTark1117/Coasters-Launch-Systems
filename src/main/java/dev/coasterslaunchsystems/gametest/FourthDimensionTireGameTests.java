package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Actual cabinet forces only: native queued impulses are observed, never injected by this fixture. */
@GameTestHolder("coasterslaunchsystems")
public final class FourthDimensionTireGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;

  private static final class Scene {
    final GameTestHelper helper;
    final ServerLevel level;
    final String design;
    final boolean reverse, uneven;
    final List<Pig> riders = new ArrayList<>();
    final List<Object> vehicles = new ArrayList<>();
    final List<ServerSubLevel> parents = new ArrayList<>();
    Object vehicle;
    ServerSubLevel chassis;
    DirectedTrack track;
    double impulse, work, shaftWork, vetoBaseline, massBefore, maxJointError, maxRotorAngle, groupDifference, maxCouplerError;
    int drivenSteps, monitoredSteps, vetoSteps;
    String sensorFailure = "";
    boolean measureJoints;
    final Vector3d start = new Vector3d();
    Scene(GameTestHelper helper, String design, boolean reverse, boolean uneven) {
      this.helper = helper; level = helper.getLevel(); this.design = design;
      this.reverse = reverse; this.uneven = uneven;
    }
  }

  private static void beforePhysics(ForgeSablePrePhysicsTickEvent event) {
    for (var scene : List.copyOf(SCENES)) {
      if (scene.level != event.getPhysicsSystem().getLevel() || scene.chassis == null || scene.chassis.isRemoved()) continue;
      try {
        scene.monitoredSteps++;
        double spent = 0; boolean driven = false;
        for (var chassis : scene.parents) {
          var groups = chassis.getQueuedForceGroups();
          var queued = groups == null ? null : groups.get(ForceGroups.PROPULSION.get());
          var impulse = queued == null ? new Vector3d() : new Vector3d(queued.getForceTotal().getLocalForce());
          scene.impulse += impulse.length();
          var motion = MotionProviders.resolve(scene.level, chassis.getUniqueId()).orElseThrow();
          if (!motion.propulsionAllowed() || !motion.exclusiveOperator().isEmpty()) {
            scene.vetoSteps++;
            scene.helper.assertTrue(impulse.lengthSquared() < 1e-18, "Native queue contains tire impulse under a vehicle veto");
          }
          if (impulse.lengthSquared() > 1e-18) {
            driven = true;
            var contact = EngagedContacts.current(scene.level, chassis).orElseThrow();
            var axis = scene.track.forward(scene.level, contact.hit());
            var direction = new Vector3d(axis.x, axis.y, axis.z);
            var point = VehicleInterfaces.tirePoint(chassis);
            scene.helper.assertTrue(point != null, "Queued tire force has no installed interface");
            var dynamics = NativeForces.pointDynamics(chassis, event.getPhysicsSystem().getPipeline(), point, direction);
            double signed = impulse.rotate(chassis.logicalPose().orientation()).dot(direction);
            spent += Math.max(0, DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(),
                signed / event.getTimeStep(), event.getTimeStep()));
          }
        }
        if (driven) {
          scene.drivenSteps++;
          double available = 8 * 64 * 2 * Math.PI / 60 * event.getTimeStep();
          scene.helper.assertTrue(spent <= available + 1e-8, "Native consist interface work exceeded the shaft budget");
          scene.work += spent; scene.shaftWork += available;
        }
      } catch (RuntimeException failure) { scene.sensorFailure = failure.toString(); }
    }
  }

  private static void afterPhysics(ForgeSablePostPhysicsTickEvent event) {
    for (var scene : List.copyOf(SCENES)) {
      if (scene.level != event.getPhysicsSystem().getLevel() || !scene.measureJoints || scene.chassis == null
          || scene.chassis.isRemoved()) continue;
      try {
        for (int car = 0; car < scene.vehicles.size(); car++) {
          var carriers = (List<?>) read(scene.vehicles.get(car), "carriers");
          double first = ((Number) read(carriers.getFirst(), "angle")).doubleValue();
          for (var carrier : carriers) {
            scene.maxJointError = Math.max(scene.maxJointError, separation(scene.level, scene.parents.get(car), carrier));
            double angle = ((Number) read(carrier, "angle")).doubleValue();
            scene.maxRotorAngle = Math.max(scene.maxRotorAngle, Math.abs(angle));
            scene.groupDifference = Math.max(scene.groupDifference, Math.abs(angle - first));
          }
        }
        if (scene.parents.size() == 2) {
          var points = new ArrayList<Vector3d>();
          for (var parent : scene.parents) {
            var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(parent);
            points.add(nativePoint(parent, new Vector3d(bearing.getX() + .5, bearing.getY() + .5, bearing.getZ() + .5)));
          }
          scene.maxCouplerError = Math.max(scene.maxCouplerError, Math.abs(points.getFirst().distance(points.getLast()) - 6));
        }
      } catch (RuntimeException failure) { scene.sensorFailure = failure.toString(); }
    }
  }

  @GameTestGenerator
  public static List<TestFunction> equipmentTests() {
    if (!ModList.get().isLoaded("fourthdimension")) return List.of();
    var tests = new ArrayList<TestFunction>();
    var designs = List.of("ARROW", "LEGACY", "FREE_SPIN", "REVISED");
    for (int family = 0; family < designs.size(); family++) for (int mode = 0; mode < 4; mode++) {
      int slot = family * 4 + mode;
      String design = designs.get(family);
      boolean reverse = (mode & 1) != 0, uneven = (mode & 2) != 0;
      String name = design.toLowerCase(Locale.ROOT) + (reverse ? "_reverse" : "_forward") + (uneven ? "_uneven" : "_full");
      tests.add(new TestFunction("launchsystems_4d_tire", "coasterslaunchsystems.4d_tire_" + name,
          "coasterslaunchsystems:empty", 380, 0, true, helper -> run(helper, design, slot, reverse, uneven)));
    }
    tests.add(new TestFunction("launchsystems_4d_tire", "coasterslaunchsystems.4d_tire_pax_veto",
        "coasterslaunchsystems:empty", 380, 0, true, helper -> run(helper, "PAX", 16, false, false)));
    for (int direction = 0; direction < 2; direction++) {
      boolean reverse = direction == 1; int slot = 17 + direction;
      tests.add(new TestFunction("launchsystems_4d_tire", "coasterslaunchsystems.4d_tire_arrow_linked_" + (reverse ? "reverse" : "forward"),
          "coasterslaunchsystems:empty", 380, 0, true, helper -> run(helper, "ARROW", slot, reverse, false)));
    }
    return tests;
  }

  private static void run(GameTestHelper helper, String designName, int slot, boolean reverse, boolean uneven) {
    if (!registered) {
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, FourthDimensionTireGameTests::beforePhysics);
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, FourthDimensionTireGameTests::afterPhysics);
      registered = true;
    }
    var scene = new Scene(helper, designName, reverse, uneven);
    var level = helper.getLevel();
    var from = new BlockPos(320 + slot * 32, 176, 320); var to = from.offset(0, 0, 64);
    var modulePos = from.offset(1, -2, 16); var motorPos = modulePos.west();
    for (int x = from.getX() / 16 - 1; x <= from.getX() / 16 + 1; x++)
      for (int z = 19; z <= 25; z++) level.setChunkForced(x, z, true);
    for (var pos : List.of(from, to)) level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var player = FakePlayerFactory.get(level, new GameProfile(new UUID(0x1a0c4d71L, slot + 1), "LS4DTire" + slot));
    player.setGameMode(GameType.CREATIVE); player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    var designType = type("dev.fourthdimension.track.TrackDesign");
    Object design = java.util.Arrays.stream(designType.getEnumConstants())
        .filter(v -> ((Enum<?>) v).name().equals(designName)).findFirst().orElseThrow();
    var trackItem = (Item) ((Supplier<?>) readStatic("dev.fourthdimension.track.TrackItems", "ARROW")).get();
    player.setItemInHand(InteractionHand.MAIN_HAND, trackItem.getDefaultInstance());
    invoke("dev.fourthdimension.track.TrackPlacement", "connect",
        new Class<?>[] {ServerLevel.class, BlockPos.class, BlockPos.class, designType, Player.class, ItemStack.class},
        level, from, to, design, player, player.getMainHandItem());
    level.setBlockAndUpdate(modulePos, EquipmentContent.TIRE.get().defaultBlockState()
        .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
    level.setBlockAndUpdate(motorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.EAST));
    ((CreativeMotorBlockEntity) level.getBlockEntity(motorPos)).generatedSpeed.setValue(64);
    var vehicleType = type("dev.fourthdimension.runtime.FoundationData$Vehicle");
    helper.runAtTickTime(15, () -> {
      var edge = CoasterPathGraphManager.get(level).edges().stream()
          .filter(e -> e.from().equals(from) && e.to().equals(to) || e.from().equals(to) && e.to().equals(from))
          .findFirst().orElseThrow();
      var hit = new GraphHit(CoasterPathGraphManager.sampleEdgePosition(level, edge, .25), edge, .25);
      scene.vehicle = invoke("dev.fourthdimension.vehicle.VehicleFactory", "create",
          new Class<?>[] {ServerLevel.class, designType, GraphHit.class}, level, design, hit);
      scene.chassis = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(scene.vehicle, "chassis"));
      scene.vehicles.add(scene.vehicle); scene.parents.add(scene.chassis);
      if (slot >= 17) {
        var secondHit = new GraphHit(CoasterPathGraphManager.sampleEdgePosition(level, edge, 22.0 / 64), edge, 22.0 / 64);
        var second = invoke("dev.fourthdimension.vehicle.VehicleFactory", "create",
            new Class<?>[] {ServerLevel.class, designType, GraphHit.class}, level, design, secondHit);
        scene.vehicles.add(second);
        scene.parents.add((ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(second, "chassis")));
      }
      for (var chassis : scene.parents) {
        var plot = chassis.getPlot().getEmbeddedLevelAccessor();
        helper.assertTrue(plot.getBlockState(BlockPos.ZERO.below()).isAir(), "Drive strip would replace existing Fourth Dimension hardware");
        helper.assertTrue(plot.setBlock(BlockPos.ZERO.below(), EquipmentContent.TIRE_SURFACE.get().defaultBlockState(), 3),
            "Fourth Dimension chassis strip installation failed");
      }
      scene.track = DirectedTrack.resolve(level, reverse ? to : from, reverse ? from : to).orElseThrow();
      SCENES.add(scene);
    });
    helper.runAtTickTime(90, () -> {
      if (scene.parents.size() == 2) helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(level,
          scene.parents.getFirst(), scene.parents.getLast(), 6), "Native six-block Arrow coupling was rejected");
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(modulePos);
      cabinet.bind(level, reverse ? to : from, reverse ? from : to); cabinet.setArmed(true);
    });
    helper.runAtTickTime(100, () -> {
      var motion = MotionProviders.resolve(level, scene.chassis.getUniqueId()).orElseThrow();
      helper.assertTrue(!motion.brakingAllowed(), "Uncertified Fourth Dimension fin brakes received authority");
      helper.assertTrue(motion.provider().equals("fourthdimension:schema4"), "Fourth Dimension equipment fell through to ordinary provider");
      helper.assertTrue(!motion.propulsionAllowed() && scene.impulse < 1e-9, "Captured vehicle received tire propulsion");
      helper.assertTrue(motion.interfaces().contains(Mechanism.TIRE) != designName.equals("PAX"), "Unexpected tire certificate");
      helper.assertTrue(motion.consist().size() == scene.parents.size(), "Provider lost a native coupled chassis");
      if (designName.equals("PAX")) return;
      helper.assertTrue((boolean) invoke("dev.fourthdimension.vehicle.Boarding", "ready",
          new Class<?>[] {ServerLevel.class, vehicleType}, level, scene.vehicle), "Initial carrier boarding did not settle");
      scene.massBefore = totalMass(scene);
      for (var vehicle : scene.vehicles) {
        var carriers = (List<?>) read(vehicle, "carriers");
        for (int group = 0; group < carriers.size(); group++) {
          if (uneven && group != 0) continue;
          var carrier = carriers.get(group);
          var child = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(carrier, "body"));
          for (var seat : (List<?>) read(carrier, "seats")) {
            var rider = EntityType.PIG.create(level); rider.setNoAi(true);
            rider.setPos(from.getX(), from.getY() + 4, from.getZ() + 16); level.addFreshEntity(rider);
            SeatBlock.sitDown(level, child.getPlot().getCenterBlock().offset((BlockPos) seat), rider);
            helper.assertTrue(rider.isPassenger(), "Real passenger did not board a Fourth Dimension seat");
            scene.riders.add(rider);
          }
        }
      }
    });
    helper.runAtTickTime(130, () -> {
      if (designName.equals("PAX")) return;
      helper.assertTrue(Math.abs(totalMass(scene) - scene.massBefore - .8 * scene.riders.size()) < 1e-5,
          "Loaded tire vehicle omitted or duplicated passenger mass");
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "restraints",
          new Class<?>[] {ServerLevel.class, vehicleType, boolean.class}, level, scene.vehicle, true);
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "retention",
          new Class<?>[] {ServerLevel.class, vehicleType, boolean.class}, level, scene.vehicle, true);
      helper.assertTrue(!MotionProviders.resolve(level, scene.chassis.getUniqueId()).orElseThrow().propulsionAllowed(),
          "Public maintenance retention did not veto the cabinet");
      helper.assertTrue(MotionProviders.captureState(level, scene.chassis.getUniqueId()) == dev.coasterslaunchsystems.api.CaptureState.DENIED
          && !MotionProviders.requestCapture(level, scene.chassis.getUniqueId(), Set.copyOf(MotionProviders.resolve(level, scene.chassis.getUniqueId()).orElseThrow().consist())),
          "Station capture overrode maintenance retention");
    });
    helper.runAtTickTime(150, () -> {
      if (designName.equals("PAX")) return;
      helper.assertTrue(scene.impulse < 1e-9, "Capture/maintenance received tire impulse");
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "retention",
          new Class<?>[] {ServerLevel.class, vehicleType, boolean.class}, level, scene.vehicle, false);
      write(scene.vehicle, "fault", "Tire fixture injected vehicle fault");
    });
    helper.runAtTickTime(160, () -> {
      if (designName.equals("PAX")) return;
      helper.assertTrue(scene.impulse < 1e-9 && !MotionProviders.resolve(level, scene.chassis.getUniqueId()).orElseThrow().propulsionAllowed(),
          "Vehicle fault allowed tire propulsion");
      helper.assertTrue(MotionProviders.captureState(level, scene.chassis.getUniqueId()) == dev.coasterslaunchsystems.api.CaptureState.DENIED,
          "Station capture granted boarding through a vehicle fault");
      write(scene.vehicle, "fault", "");
    });
    helper.runAtTickTime(170, () -> {
      if (designName.equals("PAX")) return;
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "dispatch",
          new Class<?>[] {ServerLevel.class, vehicleType}, level, scene.vehicle);
      SubLevelContainer.getContainer(level).physicsSystem().updatePose(scene.chassis);
      scene.start.set(scene.chassis.logicalPose().position()); scene.measureJoints = true;
    });
    helper.runAtTickTime(190, () -> {
      if (designName.equals("PAX")) return;
      helper.assertTrue(scene.drivenSteps > 10 && scene.impulse > 1, "Placed cabinet did not queue native strip forces");
      write(scene.vehicle, "maintenance", true); scene.vetoBaseline = scene.impulse;
    });
    helper.runAtTickTime(200, () -> {
      if (designName.equals("PAX")) return;
      helper.assertTrue(Math.abs(scene.impulse - scene.vetoBaseline) < 1e-9, "Maintenance failed to interrupt native tire force");
      write(scene.vehicle, "maintenance", false);
    });
    helper.runAtTickTime(230, () -> {
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(modulePos);
      helper.assertTrue(scene.sensorFailure.isEmpty(), "Native tire sensor failed: " + scene.sensorFailure);
      if (designName.equals("PAX")) {
        var motion = MotionProviders.resolve(level, scene.chassis.getUniqueId()).orElseThrow();
        helper.assertTrue(motion.exclusiveOperator().equals("fourthdimension:pax_winch")
            && !motion.propulsionAllowed() && scene.impulse < 1e-9 && scene.vetoSteps > 100,
            "Powered tire cabinet bypassed PAX ownership");
        System.out.println("LS_4D_TIRE design=PAX owner=" + motion.exclusiveOperator() + " impulse=" + scene.impulse);
        cleanup(scene, modulePos, motorPos, from, to); helper.succeed(); return;
      }
      var physics = SubLevelContainer.getContainer(level).physicsSystem(); physics.updatePose(scene.chassis);
      double speed = physics.getPipeline().getLinearVelocity(scene.chassis, new Vector3d()).z;
      helper.assertTrue(cabinet.fault().isEmpty(), "Loaded tire cabinet faulted: " + cabinet.fault());
      helper.assertTrue(speed * (reverse ? -1 : 1) > .4 && Math.abs(speed) < 2.8, "Loaded tire speed outside expected range: " + speed);
      helper.assertTrue(scene.chassis.logicalPose().position().distance(scene.start) > .2, "Articulated tire transport did not move");
      helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(scene.chassis), "Tire transport lost Fourth Dimension native support");
      for (var parent : scene.parents) {
        double memberSpeed = physics.getPipeline().getLinearVelocity(parent, new Vector3d()).z;
        helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(parent) && Math.abs(memberSpeed - speed) < .1,
            "Coupled Arrow member lost native support or common translation");
      }
      System.out.println("LS_4D_TIRE design=" + designName + " reverse=" + reverse + " uneven=" + uneven
          + " cars=" + scene.parents.size() + " riders=" + scene.riders.size() + " mass=" + totalMass(scene)
          + " speed=" + speed + " impulse=" + scene.impulse + " work=" + scene.work + " shaft_work=" + scene.shaftWork);
      ((CreativeMotorBlockEntity) level.getBlockEntity(motorPos)).generatedSpeed.setValue(0); scene.vetoBaseline = scene.impulse;
    });
    helper.runAtTickTime(250, () -> {
      if (designName.equals("PAX")) return;
      helper.assertTrue(Math.abs(scene.impulse - scene.vetoBaseline) < 1e-9, "Power loss kept queueing tire impulse");
      scene.chassis.getPlot().getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.below(), Blocks.AIR.defaultBlockState(), 3);
      helper.assertTrue(!MotionProviders.resolve(level, scene.chassis.getUniqueId()).orElseThrow().interfaces().contains(Mechanism.TIRE),
          "Removed Fourth Dimension strip kept tire eligibility");
    });
    helper.runAtTickTime(290, () -> {
      if (designName.equals("PAX")) return;
      try {
        System.out.println("LS_4D_TIRE_COMPLETE design=" + designName + " reverse=" + reverse + " uneven=" + uneven
            + " max_joint_error=" + scene.maxJointError + " max_rotor_angle=" + scene.maxRotorAngle
            + " group_difference=" + scene.groupDifference + " riders_retained=" + scene.riders.stream().allMatch(Pig::isPassenger)
            + " cars=" + scene.parents.size() + " max_coupler_error=" + scene.maxCouplerError
            + " driven_steps=" + scene.drivenSteps + " veto_steps=" + scene.vetoSteps);
        helper.assertTrue(scene.sensorFailure.isEmpty(), "Native tire sensor failed: " + scene.sensorFailure);
        helper.assertTrue(scene.riders.stream().allMatch(Pig::isPassenger), "Tire force lost an articulated passenger");
        helper.assertTrue(scene.maxJointError < .01, "Tire strip force separated native rotary joints: " + scene.maxJointError);
        helper.assertTrue(scene.maxCouplerError < .05, "Tire force stretched the native six-block Arrow coupler");
        if (!designName.equals("ARROW")) {
          // Full revised loads accelerate at ~0.43, far below the raw-force fixture's 2.
          // Require measurable free response above alignment jitter; uneven loads must also diverge.
          helper.assertTrue(scene.maxRotorAngle > (uneven ? .05 : .02), "Tire force suppressed native free rotation");
          if (uneven) helper.assertTrue(scene.groupDifference > .005, "Uneven load lost independent rotor response");
        }
        helper.assertTrue(scene.work <= scene.shaftWork + 1e-8 && scene.drivenSteps > 20, "Native tire work did not reconcile");
        var missingId = (UUID) read(((List<?>) read(scene.vehicle, "carriers")).getFirst(), "body");
        var container = SubLevelContainer.getContainer(level);
        container.removeSubLevel((ServerSubLevel) container.getSubLevel(missingId), SubLevelRemovalReason.REMOVED);
        var unavailable = MotionProviders.resolve(level, scene.chassis.getUniqueId()).orElseThrow();
        helper.assertTrue(unavailable.provider().equals("fourthdimension:schema4") && !unavailable.propulsionAllowed()
            && !unavailable.dispatchReady(), "Missing native carrier did not veto Fourth Dimension motion");
        for (var parent : scene.parents) helper.assertTrue(!MotionProviders.resolve(level, parent.getUniqueId()).orElseThrow().propulsionAllowed(),
            "A missing carrier allowed propulsion elsewhere in the native consist");
        for (var parent : scene.parents) helper.assertTrue(MotionProviders.captureState(level, parent.getUniqueId()) == dev.coasterslaunchsystems.api.CaptureState.DENIED
            && !MotionProviders.requestCapture(level, parent.getUniqueId(), Set.copyOf(unavailable.consist())), "Station capture granted authority after carrier loss");
      } finally { cleanup(scene, modulePos, motorPos, from, to); }
      helper.succeed();
    });
  }

  private static void cleanup(Scene scene, BlockPos module, BlockPos motor, BlockPos from, BlockPos to) {
    SCENES.remove(scene);
    var container = SubLevelContainer.getContainer(scene.level);
    for (var vehicle : scene.vehicles) for (var carrier : (List<?>) read(vehicle, "carriers"))
      if (container.getSubLevel((UUID) read(carrier, "body")) instanceof ServerSubLevel child)
        container.removeSubLevel(child, SubLevelRemovalReason.REMOVED);
    for (var parent : scene.parents) container.removeSubLevel(parent, SubLevelRemovalReason.REMOVED);
    scene.riders.forEach(Pig::discard);
    ((CoasterAnchorpointBlockEntity) scene.level.getBlockEntity(from)).removeAnchorPeerCurve(scene.level, to);
    for (var pos : List.of(module, motor, from, to)) scene.level.removeBlock(pos, false);
  }

  private static double totalMass(Scene scene) {
    return scene.parents.stream().mapToDouble(parent -> NativeForces.mass(scene.level,
        MotionProviders.resolve(scene.level, parent.getUniqueId()).orElseThrow())).sum();
  }
}
