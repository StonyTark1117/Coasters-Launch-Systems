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
import dev.coasterslaunchsystems.runtime.EquipmentRuntime;
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
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Real equipment brakes only; physical entry forces end before the measured braking phase. */
@GameTestHolder("coasterslaunchsystems")
public final class FourthDimensionBrakeGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static final class Scene {
    final GameTestHelper helper;
    final ServerLevel level;
    final String design;
    final Mechanism mechanism;
    final boolean reverse, uneven, linked;
    final BlockPos from, to, module, motor;
    final List<Object> vehicles = new ArrayList<>();
    final List<ServerSubLevel> parents = new ArrayList<>();
    final List<Pig> riders = new ArrayList<>();
    final List<Double> initialAngles = new ArrayList<>();
    DirectedTrack track;
    double massBefore, initialSpeed, impulse, dissipated, peakForce, maxJointError, maxCouplerError,
        maxBrakeRotorResponse, groupDifference, impulseBaseline, heldSpeed;
    int vetoSteps, brakeSteps, unavailableSteps;
    boolean entry, braking, missingCarrier, closed;
    String failure = "";
    Scene(GameTestHelper helper, String design, Mechanism mechanism, int slot, boolean reverse, boolean uneven) {
      this.helper = helper; level = helper.getLevel(); this.design = design; this.mechanism = mechanism;
      this.reverse = reverse; this.uneven = uneven; linked = slot >= 17;
      from = new BlockPos(320 + slot * 32, 176, mechanism == Mechanism.FIN_BRAKE ? 576 : mechanism == Mechanism.SKID_BRAKE ? 768 : 960); to = from.offset(0, 0, 64);
      int contact = linked ? reverse ? 17 : 21 : reverse ? 18 : 20;
      module = from.offset(mechanism == Mechanism.FIN_BRAKE ? 1 : -1, -2, contact);
      motor = mechanism == Mechanism.FIN_BRAKE ? module.east() : module.west();
    }
    EquipmentCabinetEntity cabinet() { return (EquipmentCabinetEntity) level.getBlockEntity(module); }
    Object vehicle() { return vehicles.getFirst(); }
    ServerSubLevel chassis() { return parents.getFirst(); }
    void rpm(int value) { ((CreativeMotorBlockEntity) level.getBlockEntity(motor)).generatedSpeed.setValue(value); }
    double speed(ServerSubLevel parent) {
      var physics = SubLevelContainer.getContainer(level).physicsSystem(); physics.updatePose(parent);
      return physics.getPipeline().getLinearVelocity(parent, new Vector3d()).z;
    }
    void at(int tick, Runnable action) {
      helper.runAtTickTime(tick, () -> {
        try { action.run(); }
        catch (RuntimeException error) { cleanup(); throw error; }
      });
    }
    void cleanup() {
      if (closed) return; closed = true; SCENES.remove(this);
      var container = SubLevelContainer.getContainer(level);
      for (var vehicle : vehicles) for (var carrier : (List<?>) read(vehicle, "carriers"))
        if (container.getSubLevel((UUID) read(carrier, "body")) instanceof ServerSubLevel child)
          container.removeSubLevel(child, SubLevelRemovalReason.REMOVED);
      for (var parent : parents)
        if (container.getSubLevel(parent.getUniqueId()) == parent && !parent.isRemoved())
          container.removeSubLevel(parent, SubLevelRemovalReason.REMOVED);
      riders.forEach(Pig::discard);
      if (level.getBlockEntity(from) instanceof CoasterAnchorpointBlockEntity anchor) anchor.removeAnchorPeerCurve(level, to);
      for (var pos : List.of(module, motor, from, to)) level.removeBlock(pos, false);
    }
  }
  private static void beforePhysics(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.level != event.getPhysicsSystem().getLevel() || s.parents.isEmpty() || s.chassis().isRemoved()) continue;
      try {
        double totalForce = 0;
        for (var parent : s.parents) {
          var motion = MotionProviders.resolve(s.level, parent.getUniqueId()).orElseThrow();
          var group = parent.getQueuedForceGroups() == null ? null : parent.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
          var impulse = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalForce());
          int clamps = EquipmentRuntime.activeFinClamps(s.level, parent.getUniqueId());
          if (!motion.brakingAllowed() || !motion.exclusiveOperator().isEmpty()) {
            s.vetoSteps++;
            s.helper.assertTrue(impulse.lengthSquared() < 1e-18 && clamps == 0, "A real brake acted under a Fourth Dimension veto");
          }
          if (s.missingCarrier) {
            s.unavailableSteps++;
            s.helper.assertTrue(motion.provider().equals("fourthdimension:schema4") && !motion.brakingAllowed()
                && !motion.propulsionAllowed() && !motion.dispatchReady(), "Missing carrier allowed motion or fallback during native physics");
          }
          var contact = EngagedContacts.current(s.level, parent).orElse(null);
          if (contact == null || !s.track.matches(contact.hit())) continue;
          var axis = s.track.forward(s.level, contact.hit()); var direction = new Vector3d(axis.x, axis.y, axis.z);
          if (s.mechanism == Mechanism.EDDY_BRAKE || s.mechanism == Mechanism.SKID_BRAKE) {
            double force = impulse.rotate(parent.logicalPose().orientation()).dot(direction) / event.getTimeStep();
            totalForce += Math.abs(force); s.impulse += Math.abs(force) * event.getTimeStep();
            s.helper.assertTrue(Math.abs(force) <= motion.maximumBrakeForce() + 1e-8, "Magnetic force exceeded the provider brake rating");
            if (impulse.lengthSquared() > 1e-18) {
              var point = s.mechanism == Mechanism.SKID_BRAKE ? VehicleInterfaces.skidPoint(parent) : VehicleInterfaces.conductivePoint(parent);
              var dynamics = NativeForces.pointDynamics(parent, event.getPhysicsSystem().getPipeline(), point, direction);
              double work = DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(), force, event.getTimeStep());
              s.helper.assertTrue(force * dynamics.speed() <= 1e-8 && work <= 1e-8, "4D magnet added fin energy");
              s.dissipated += Math.max(0, -work); s.brakeSteps++;
            }
          } else if (clamps > 0) s.brakeSteps++;
          if (s.entry) {
            s.helper.assertTrue(motion.propulsionAllowed(), "Physical brake-fixture entry was not permitted");
            double force = Math.min(NativeForces.mass(s.level, motion) * 2, motion.maximumForce());
            NativeForces.pointForce(parent, parent.getMassTracker().getCenterOfMass(), direction.mul(force), event.getTimeStep());
          }
        }
        s.peakForce = Math.max(s.peakForce, totalForce);
        s.helper.assertTrue(totalForce <= (s.mechanism == Mechanism.SKID_BRAKE ? 192 : 128) + 1e-8, "One passive brake cabinet duplicated its total force budget");
      } catch (RuntimeException error) { s.failure = error.toString(); }
    }
  }
  private static void afterPhysics(ForgeSablePostPhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.level != event.getPhysicsSystem().getLevel() || s.parents.isEmpty() || s.chassis().isRemoved() || s.missingCarrier) continue;
      try {
        int angle = 0;
        for (int car = 0; car < s.vehicles.size(); car++) {
          var carriers = (List<?>) read(s.vehicles.get(car), "carriers");
          double first = ((Number) read(carriers.getFirst(), "angle")).doubleValue();
          for (var carrier : carriers) {
            s.maxJointError = Math.max(s.maxJointError, separation(s.level, s.parents.get(car), carrier));
            double value = ((Number) read(carrier, "angle")).doubleValue();
            if (s.braking) s.maxBrakeRotorResponse = Math.max(s.maxBrakeRotorResponse, Math.abs(value - s.initialAngles.get(angle)));
            s.groupDifference = Math.max(s.groupDifference, Math.abs(value - first)); angle++;
          }
        }
        if (s.parents.size() == 2) {
          var points = new ArrayList<Vector3d>();
          for (var parent : s.parents) {
            var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(parent);
            points.add(nativePoint(parent, new Vector3d(bearing.getX() + .5, bearing.getY() + .5, bearing.getZ() + .5)));
          }
          s.maxCouplerError = Math.max(s.maxCouplerError, Math.abs(points.getFirst().distance(points.getLast()) - 6));
        }
      } catch (RuntimeException error) { s.failure = error.toString(); }
    }
  }
  @GameTestGenerator
  public static List<TestFunction> equipmentTests() {
    if (!ModList.get().isLoaded("fourthdimension")) return List.of();
    var tests = new ArrayList<TestFunction>();
    for (var mechanism : List.of(Mechanism.FIN_BRAKE, Mechanism.SKID_BRAKE, Mechanism.EDDY_BRAKE)) {
      var designs = List.of("ARROW", "LEGACY", "FREE_SPIN", "REVISED");
      for (int family = 0; family < designs.size(); family++) for (int mode = 0; mode < 4; mode++) {
        int slot = family * 4 + mode; boolean reverse = (mode & 1) != 0, uneven = (mode & 2) != 0;
        String design = designs.get(family);
        tests.add(test(mechanism, design, slot, reverse, uneven));
      }
      tests.add(test(mechanism, "PAX", 16, false, false));
      tests.add(test(mechanism, "ARROW", 17, false, false)); tests.add(test(mechanism, "ARROW", 18, true, false));
    }
    return tests;
  }
  private static TestFunction test(Mechanism mechanism, String design, int slot, boolean reverse, boolean uneven) {
    String name = mechanism.name().toLowerCase(Locale.ROOT) + "_" + design.toLowerCase(Locale.ROOT)
        + (slot >= 17 ? "_linked" : "") + (reverse ? "_reverse" : "_forward") + (uneven ? "_uneven" : "_full");
    return new TestFunction("launchsystems_4d_brakes", "coasterslaunchsystems.4d_" + name, "coasterslaunchsystems:empty", 480, 0, true,
        helper -> run(new Scene(helper, design, mechanism, slot, reverse, uneven), slot));
  }
  private static void run(Scene s, int slot) {
    if (!registered) {
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, FourthDimensionBrakeGameTests::beforePhysics);
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, FourthDimensionBrakeGameTests::afterPhysics); registered = true;
    }
    var helper = s.helper; var level = s.level;
    for (int x = s.from.getX() / 16 - 1; x <= s.from.getX() / 16 + 1; x++)
      for (int z = s.from.getZ() / 16 - 1; z <= s.to.getZ() / 16 + 1; z++) level.setChunkForced(x, z, true);
    for (var pos : List.of(s.from, s.to)) level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var player = FakePlayerFactory.get(level, new GameProfile(new UUID(0x1a0c4db2L + s.mechanism.ordinal(), slot + 1), "LS4DBrake" + slot));
    player.setGameMode(GameType.CREATIVE); player.setPos(s.to.getX() + 2, s.to.getY() + 1, s.to.getZ());
    var designType = type("dev.fourthdimension.track.TrackDesign");
    Object design = java.util.Arrays.stream(designType.getEnumConstants()).filter(v -> ((Enum<?>) v).name().equals(s.design)).findFirst().orElseThrow();
    var trackItem = (Item) ((Supplier<?>) readStatic("dev.fourthdimension.track.TrackItems", "ARROW")).get();
    player.setItemInHand(InteractionHand.MAIN_HAND, trackItem.getDefaultInstance());
    invoke("dev.fourthdimension.track.TrackPlacement", "connect", new Class<?>[] {ServerLevel.class, BlockPos.class, BlockPos.class, designType, Player.class, ItemStack.class},
        level, s.from, s.to, design, player, player.getMainHandItem());
    level.setBlockAndUpdate(s.module, (s.mechanism == Mechanism.FIN_BRAKE ? EquipmentContent.FIN : s.mechanism == Mechanism.SKID_BRAKE ? EquipmentContent.SKID : EquipmentContent.EDDY).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
    level.setBlockAndUpdate(s.motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, s.mechanism == Mechanism.FIN_BRAKE ? Direction.WEST : Direction.EAST)); s.rpm(0);
    var vehicleType = type("dev.fourthdimension.runtime.FoundationData$Vehicle");
    s.at(15, () -> {
      var edge = CoasterPathGraphManager.get(level).edges().stream().filter(e -> e.from().equals(s.from) && e.to().equals(s.to) || e.from().equals(s.to) && e.to().equals(s.from)).findFirst().orElseThrow();
      for (int car = 0; car < (s.linked ? 2 : 1); car++) {
        double t = .3 + car * 6.0 / 64;
        var hit = new GraphHit(CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
        var vehicle = invoke("dev.fourthdimension.vehicle.VehicleFactory", "create", new Class<?>[] {ServerLevel.class, designType, GraphHit.class}, level, design, hit);
        var parent = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(vehicle, "chassis"));
        s.vehicles.add(vehicle); s.parents.add(parent);
        var cell = s.mechanism == Mechanism.FIN_BRAKE ? new BlockPos(1, -1, 0) : s.mechanism == Mechanism.SKID_BRAKE ? new BlockPos(0, -1, -1) : new BlockPos(-1, -1, 0);
        var plot = parent.getPlot().getEmbeddedLevelAccessor();
        helper.assertTrue(plot.getBlockState(cell).isAir(), "Brake fin would replace existing Fourth Dimension hardware at " + cell);
        helper.assertTrue(plot.setBlock(cell, (s.mechanism == Mechanism.FIN_BRAKE ? EquipmentContent.BRAKE_FIN : s.mechanism == Mechanism.SKID_BRAKE ? EquipmentContent.SKID_SHOE : EquipmentContent.CONDUCTIVE_FIN).get().defaultBlockState(), 3), "4D brake hardware installation failed");
      }
      s.track = DirectedTrack.resolve(level, s.reverse ? s.to : s.from, s.reverse ? s.from : s.to).orElseThrow(); SCENES.add(s);
    });
    s.at(90, () -> {
      if (s.linked) helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(level, s.parents.getFirst(), s.parents.getLast(), 6), "Native occupied Arrow brake consist could not couple");
      s.cabinet().bind(level, s.reverse ? s.to : s.from, s.reverse ? s.from : s.to); s.cabinet().setArmed(true);
    });
    s.at(100, () -> {
      var motion = MotionProviders.resolve(level, s.chassis().getUniqueId()).orElseThrow();
      helper.assertTrue(motion.interfaces().contains(s.mechanism) != s.design.equals("PAX"), "Unexpected pinned brake interface certificate");
      if (s.design.equals("PAX")) return;
      s.massBefore = totalMass(s);
      for (var vehicle : s.vehicles) {
        helper.assertTrue((boolean) invoke("dev.fourthdimension.vehicle.Boarding", "ready", new Class<?>[] {ServerLevel.class, vehicleType}, level, vehicle), "Brake car did not settle for public boarding");
        var carriers = (List<?>) read(vehicle, "carriers");
        for (int group = 0; group < carriers.size(); group++) {
          if (s.uneven && group != 0) continue;
          var carrier = carriers.get(group); var child = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(carrier, "body"));
          for (var seat : (List<?>) read(carrier, "seats")) {
            var rider = EntityType.PIG.create(level); rider.setNoAi(true); rider.setPos(s.from.getX(), s.from.getY() + 4, s.from.getZ() + 19); level.addFreshEntity(rider);
            SeatBlock.sitDown(level, child.getPlot().getCenterBlock().offset((BlockPos) seat), rider);
            helper.assertTrue(rider.isPassenger(), "Native 4D brake passenger did not board"); s.riders.add(rider);
          }
        }
      }
    });
    s.at(130, () -> {
      if (s.design.equals("PAX")) return;
      helper.assertTrue(Math.abs(totalMass(s) - s.massBefore - .8 * s.riders.size()) < 1e-5, "Brake provider omitted or duplicated real rider mass");
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "restraints", new Class<?>[] {ServerLevel.class, vehicleType, boolean.class}, level, s.vehicle(), true);
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "retention", new Class<?>[] {ServerLevel.class, vehicleType, boolean.class}, level, s.vehicle(), true);
    });
    s.at(150, () -> {
      if (s.design.equals("PAX")) return;
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "retention", new Class<?>[] {ServerLevel.class, vehicleType, boolean.class}, level, s.vehicle(), false);
      write(s.vehicle(), "fault", "Brake fixture authority veto");
    });
    s.at(160, () -> {
      if (s.design.equals("PAX")) return;
      helper.assertTrue(s.failure.isEmpty() && s.vetoSteps > 100 && s.impulse < 1e-9 && s.brakeSteps == 0, "Capture/restraint/retention/fault veto did not exclude real brake equipment: " + s.failure);
      write(s.vehicle(), "fault", ""); s.cabinet().setArmed(false);
    });
    s.at(170, () -> {
      if (s.design.equals("PAX")) return;
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "dispatch", new Class<?>[] {ServerLevel.class, vehicleType}, level, s.vehicle());
      if (s.mechanism == Mechanism.FIN_BRAKE) { s.rpm(64); s.cabinet().setArmed(true); }
      s.entry = true;
    });
    s.at(190, () -> {
      if (s.design.equals("PAX")) return;
      s.entry = false; s.initialSpeed = s.speed(s.chassis());
      helper.assertTrue(s.initialSpeed * (s.reverse ? -1 : 1) > 1.4, "Loaded brake car did not enter through native forces: " + s.initialSpeed);
      for (var vehicle : s.vehicles) for (var carrier : (List<?>) read(vehicle, "carriers")) s.initialAngles.add(((Number) read(carrier, "angle")).doubleValue());
      s.braking = true;
      if (s.mechanism == Mechanism.FIN_BRAKE) s.rpm(0); else s.cabinet().setArmed(true);
    });
    s.at(230, () -> {
      if (s.design.equals("PAX")) return;
      helper.assertTrue(s.failure.isEmpty(), "Native 4D brake sensor failed: " + s.failure);
      helper.assertTrue(s.brakeSteps > 10, "Actual 4D brake equipment never acted");
      s.impulseBaseline = s.impulse; write(s.vehicle(), "maintenance", true);
    });
    s.at(240, () -> {
      if (s.design.equals("PAX")) return;
      helper.assertTrue(s.failure.isEmpty() && Math.abs(s.impulse - s.impulseBaseline) < 1e-9, "Maintenance did not interrupt actual brake equipment: " + s.failure);
      write(s.vehicle(), "maintenance", false);
    });
    s.at(300, () -> {
      helper.assertTrue(s.failure.isEmpty(), "Native 4D brake sensor failed: " + s.failure);
      if (s.design.equals("PAX")) {
        helper.assertTrue(s.brakeSteps == 0 && s.impulse < 1e-9 && s.vetoSteps > 100, "A brake bypassed PAX winch ownership");
        System.out.println("LS_4D_BRAKE design=PAX mechanism=" + s.mechanism + " veto_steps=" + s.vetoSteps + " impulse=" + s.impulse);
        s.cleanup(); helper.succeed(); return;
      }
      double speed = s.speed(s.chassis()); s.heldSpeed = speed;
      System.out.println("LS_4D_BRAKE design=" + s.design + " mechanism=" + s.mechanism + " reverse=" + s.reverse + " uneven=" + s.uneven
          + " cars=" + s.parents.size() + " riders=" + s.riders.size() + " mass=" + totalMass(s) + " initial=" + s.initialSpeed + " speed=" + speed
          + " brake_steps=" + s.brakeSteps + " impulse=" + s.impulse + " work=" + s.dissipated + " peak_force=" + s.peakForce
          + " max_joint_error=" + s.maxJointError + " max_coupler_error=" + s.maxCouplerError + " brake_rotor_response=" + s.maxBrakeRotorResponse + " group_difference=" + s.groupDifference);
      helper.assertTrue(s.cabinet().fault().isEmpty(), "Loaded 4D brake cabinet faulted: " + s.cabinet().fault());
      if (s.mechanism == Mechanism.FIN_BRAKE) helper.assertTrue(Math.abs(speed) < .08, "Loaded native fin clamp did not stop the cart: " + speed);
      else helper.assertTrue(Math.abs(speed) < Math.abs(s.initialSpeed) - .2 && s.dissipated > 1 && s.impulse > 1, "Native 4D magnetic trim did not dissipate motion");
      helper.assertTrue(s.riders.stream().allMatch(Pig::isPassenger) && s.maxJointError < .01 && s.maxCouplerError < .05, "Loaded braking lost riders or native joints");
      for (var parent : s.parents) helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(parent) && Math.abs(s.speed(parent) - speed) < .1, "Brake consist lost support/common translation");
      if (!s.design.equals("ARROW")) {
        helper.assertTrue(s.maxBrakeRotorResponse > .02, "Braking suppressed native free carrier response");
        if (s.uneven) helper.assertTrue(s.groupDifference > .005, "Loaded brake carriers lost independent response");
      }
    });
    s.at(320, () -> {
      if (s.closed) return;
      helper.assertTrue(s.failure.isEmpty(), "Native brake sensor failed: " + s.failure);
      if (s.mechanism == Mechanism.FIN_BRAKE) helper.assertTrue(Math.abs(s.speed(s.chassis())) < .08, "4D fin hold did not remain stopped");
      s.missingCarrier = true;
      var id = (UUID) read(((List<?>) read(s.vehicle(), "carriers")).getFirst(), "body");
      // Permanent destruction intentionally removes the whole upstream vehicle. Model an
      // unavailable carrier instead, keeping its authoritative vehicle record and chassis.
      var container = SubLevelContainer.getContainer(level); container.removeSubLevel((ServerSubLevel) container.getSubLevel(id), SubLevelRemovalReason.UNLOADED);
    });
    s.at(330, () -> {
      if (s.closed) return;
      try {
        helper.assertTrue(s.failure.isEmpty() && s.unavailableSteps >= 10, "Missing carrier was not observed during real native physics: " + s.failure);
        System.out.println("LS_4D_BRAKE_COMPLETE design=" + s.design + " mechanism=" + s.mechanism + " reverse=" + s.reverse
            + " cars=" + s.parents.size() + " missing_carrier_steps=" + s.unavailableSteps + " riders_retained=" + s.riders.stream().filter(Pig::isPassenger).count());
      } finally { s.cleanup(); }
      helper.succeed();
    });
  }
  private static double totalMass(Scene s) {
    return s.parents.stream().mapToDouble(parent -> NativeForces.mass(s.level, MotionProviders.resolve(s.level, parent.getUniqueId()).orElseThrow())).sum();
  }
}
