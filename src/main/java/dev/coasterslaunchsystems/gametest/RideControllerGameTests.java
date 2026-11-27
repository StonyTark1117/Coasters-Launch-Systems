package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.*;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.*;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

@GameTestHolder("coasterslaunchsystems")
public final class RideControllerGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  static final class Scene {
    final OrdinaryPassengerGameTests.Scene base;
    final String mode;
    final boolean reverse, linked, fourthDimension;
    final String designName;
    final int cabinetY;
    final List<Object> vehicles = new ArrayList<>();
    final Map<UUID, UUID> riderBodies = new HashMap<>();
    final List<Double> initialAngles = new ArrayList<>();
    final Map<UUID, Double> oldDistances = new HashMap<>();
    final List<BlockPos> equipment = new ArrayList<>(), motors = new ArrayList<>();
    final BlockPos controllerPosition;
    RideControllerEntity controller;
    Mechanism driveMechanism = Mechanism.TIRE;
    DirectedTrack track;
    double work, peakSpeed, baseline, travel, measuredDistance, measuredFront, measuredBrakeFront, maxJointError, maxCouplerError, rotorResponse;
    int driveSteps, closedPadSteps, runSteps;
    boolean launched, closed;
    String failure = "";
    Scene(GameTestHelper helper, int slot, String mode) { this(helper, slot, mode, 240); }
    Scene(GameTestHelper helper, int slot, String mode, int height) {
      base = new OrdinaryPassengerGameTests.Scene(helper, slot, height, -896, mode.endsWith("_tool_bound")); this.mode = mode;
      reverse = mode.contains("reverse"); linked = mode.contains("linked");
      designName = design(mode);
      fourthDimension = designName != null;
      cabinetY = Set.of("SUSPENDED", "FIXED_INVERTED", "FLYING", "NEW_GENERATION_FLYING", "PIPELINE", "POWERED_INVERTED_YAW").contains(designName == null ? "" : designName) ? 2 : -2;
      controllerPosition = base.from.offset(4, "AXIS".equals(designName) ? -3 : cabinetY, 8);
      if (!fourthDimension) { base.cart(mode.contains("swing_") ? 14 : reverse ? 56 : 8); if (linked) base.cart(reverse ? 50 : 14); }
      else prepareFourthDimension();
    }
    static String design(String mode) {
      var names = new java.util.LinkedHashMap<String, String>();
      names.put("new_generation_flying", "NEW_GENERATION_FLYING"); names.put("powered_inverted", "POWERED_INVERTED_YAW");
      names.put("fixed_inverted", "FIXED_INVERTED"); names.put("s2_spinning", "S2_SPINNING");
      names.put("free_spin", "FREE_SPIN"); names.put("classic_spinning", "SPINNING");
      for (var name : List.of("arrow", "legacy", "revised", "pax", "wing", "suspended", "axis", "flying", "pipeline", "dive")) names.put(name, name.toUpperCase(java.util.Locale.ROOT));
      return names.entrySet().stream().filter(e -> mode.startsWith(e.getKey())).map(java.util.Map.Entry::getValue).findFirst().orElse(null);
    }
    void prepareFourthDimension() {
      var level = base.level;
      if (!designName.equals("WING")) ((dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity) level.getBlockEntity(base.from)).removeAnchorPeerCurve(level, base.to);
      var designType = type("dev.fourthdimension.track.TrackDesign");
      Object design = Arrays.stream(designType.getEnumConstants()).filter(v -> ((Enum<?>) v).name().equals(designName)).findFirst().orElse(null);
      if (!designName.equals("WING")) {
      var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,
          new com.mojang.authlib.GameProfile(UUID.randomUUID(), "LSController4D"));
      player.setGameMode(net.minecraft.world.level.GameType.CREATIVE); player.setPos(base.to.getX() + 2, base.to.getY() + 1, base.to.getZ());
      var trackHolder = designName.equals("DIVE") ? "dev.fourthdimension.vehicle.DiveContent"
          : designName.equals("S2_SPINNING") ? "dev.fourthdimension.vehicle.S2SpinningContent"
          : designName.equals("POWERED_INVERTED_YAW") ? "dev.fourthdimension.vehicle.PoweredInvertedYawContent" : "dev.fourthdimension.track.TrackItems";
      var trackItem = (net.minecraft.world.item.Item) ((java.util.function.Supplier<?>) readStatic(trackHolder,
          trackHolder.endsWith("TrackItems") ? designName : "TRACK")).get();
      player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, trackItem.getDefaultInstance());
      invoke("dev.fourthdimension.track.TrackPlacement", "connect", new Class<?>[] {net.minecraft.server.level.ServerLevel.class, BlockPos.class, BlockPos.class, designType,
          net.minecraft.world.entity.player.Player.class, net.minecraft.world.item.ItemStack.class}, level, base.from, base.to, design, player, player.getMainHandItem());
      }
      base.helper.runAtTickTime(15, () -> {
        var track = DirectedTrack.resolve(level, base.from, base.to).orElseThrow();
        for (int car = 0; car < (linked ? 2 : 1); car++) {
        var hit = track.hitAtDistance(level, reverse ? 56 - car * 6 : mode.contains("swing_") ? 14 + car * 8 : 8 + car * 6);
        var vehicle = designName.equals("WING") ? invoke("dev.fourthdimension.vehicle.WingVehicle", "create", new Class<?>[] {net.minecraft.server.level.ServerLevel.class, dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit.class}, level, hit) : invoke("dev.fourthdimension.vehicle.VehicleFactory", "create",
            new Class<?>[] {net.minecraft.server.level.ServerLevel.class, designType, dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit.class}, level, design, hit);
        vehicles.add(vehicle);
        var parent = (dev.ryanhcode.sable.sublevel.ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(vehicle, "chassis")); base.parents.add(parent);
        var plot = parent.getPlot().getEmbeddedLevelAccessor();
        var certificate = dev.coasterslaunchsystems.integration.FourthDimensionCertificates.find(((Enum<?>) read(vehicle, "family")).name(), (String) read(vehicle, "variant"));
        var tire = certificate == null ? BlockPos.ZERO.below() : certificate.offset(Mechanism.TIRE);
        var fin = certificate == null ? new BlockPos(1, -1, 0) : certificate.offset(Mechanism.FIN_BRAKE);
        if (certificate != null && certificate.longitudinalOffset() < 0) for (int z = -1; z >= certificate.longitudinalOffset(); z--) {
          var brace = new BlockPos(0, 0, z);
          if (plot.getBlockState(brace).isAir()) plot.setBlock(brace, Blocks.IRON_BLOCK.defaultBlockState(), 3);
          var rear = new BlockPos(0, 0, -z);
          if (mode.contains("_controller_") && plot.getBlockState(rear).isAir()) plot.setBlock(rear, Blocks.IRON_BLOCK.defaultBlockState(), 3);
        }
        if (certificate != null && certificate.longitudinalOffset() < 0 && mode.contains("_controller_")) {
          var connector = new BlockPos(0, -1, -certificate.longitudinalOffset());
          if (plot.getBlockState(connector).isAir()) plot.setBlock(connector, Blocks.IRON_BLOCK.defaultBlockState(), 3);
        }
        base.helper.assertTrue(plot.getBlockState(tire).isAir() && plot.getBlockState(fin).isAir(), "4D launch interfaces would replace family hardware");
        plot.setBlock(tire, EquipmentContent.TIRE_SURFACE.get().defaultBlockState(), 3);
        plot.setBlock(fin, EquipmentContent.BRAKE_FIN.get().defaultBlockState(), 3);
        }
      });
      if (Set.of("FIXED_INVERTED", "SPINNING", "S2_SPINNING").contains(designName)) {
        base.helper.runAtTickTime(25, () -> {
          for (var vehicle : vehicles) {
            var pos = base.from.offset(6, cabinetY, cabinetY == 2 ? 14 : 8);
            var station = (net.minecraft.world.level.block.Block) ((java.util.function.Supplier<?>) readStatic("dev.fourthdimension.station.StationContent", "STATION")).get();
            level.setBlockAndUpdate(pos, station.defaultBlockState());
            try { level.getBlockEntity(pos).getClass().getField("vehicle").set(level.getBlockEntity(pos), read(vehicle, "chassis"));
              vehicle.getClass().getField("dock").set(vehicle, pos);
            } catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
          }
        });
        if (Set.of("FIXED_INVERTED", "S2_SPINNING").contains(designName)) base.helper.runAtTickTime(100, () -> {
          for (var vehicle : vehicles) invoke("dev.fourthdimension.vehicle." + (designName.equals("FIXED_INVERTED") ? "FixedInvertedRuntime" : "S2SpinningRuntime"), "rebindDock",
              new Class<?>[] {net.minecraft.server.level.ServerLevel.class, type("dev.fourthdimension.runtime.FoundationData$Vehicle"), BlockPos.class}, level, vehicle, read(vehicle, "dock"));
        });
      }
      if (!"true".equals(System.getenv("LS_CABLE_PLAYER")) && !"true".equals(System.getenv("LS_FIXED_PLAYER")) && Set.of("ARROW", "LEGACY", "FREE_SPIN", "REVISED", "PAX", "WING", "FIXED_INVERTED", "SPINNING", "S2_SPINNING").contains(designName)) base.helper.runAtTickTime(170, () -> { for (var vehicle : vehicles) invoke("dev.fourthdimension.vehicle.VehicleOperations", "restraints",
          new Class<?>[] {net.minecraft.server.level.ServerLevel.class, type("dev.fourthdimension.runtime.FoundationData$Vehicle"), boolean.class}, level, vehicle, true); });
    }
    void boardFourthDimension() {
      for (var vehicle : vehicles) {
        base.helper.assertTrue((boolean) invoke("dev.fourthdimension.vehicle.Boarding", "ready",
            new Class<?>[] {net.minecraft.server.level.ServerLevel.class, type("dev.fourthdimension.runtime.FoundationData$Vehicle")}, base.level, vehicle), "4D controller boarding did not settle");
        for (var carrier : (List<?>) read(vehicle, "carriers")) {
          var body = (dev.ryanhcode.sable.sublevel.ServerSubLevel) SubLevelContainer.getContainer(base.level).getSubLevel((UUID) read(carrier, "body"));
          for (var seat : (List<?>) read(carrier, "seats")) {
            var pig = net.minecraft.world.entity.EntityType.PIG.create(base.level); pig.setNoAi(true); pig.setPos(base.from.getX(), base.from.getY() + 4, base.from.getZ() + 8);
            base.level.addFreshEntity(pig); com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(base.level, body.getPlot().getCenterBlock().offset((BlockPos) seat), pig);
            base.helper.assertTrue(pig.isPassenger(), "4D controller rider failed to board"); base.riders.add(pig); riderBodies.put(pig.getUUID(), body.getUniqueId());
          }
        }
      }
    }
    void install(boolean shortBank) {
      var level = base.level;
      level.setBlockAndUpdate(controllerPosition, EquipmentContent.CONTROLLER.get().defaultBlockState());
      controller = (RideControllerEntity) level.getBlockEntity(controllerPosition);
      if (!mode.endsWith("_tool_bound")) {
        var first = reverse ? base.to : base.from; var second = reverse ? base.from : base.to;
        controller.bind(level, first, second);
      }
      for (int s = 8; s <= 16; s += 2) { module(s, false); module(s, true); }
      for (int s = 21; s <= (shortBank ? 21 : 41); s += 2) module(s, true);
      if (mode.endsWith("_tool_bound")) bindWithSpanTool();
      track = DirectedTrack.resolve(level, reverse ? base.to : base.from, reverse ? base.from : base.to).orElseThrow();
    }
    void bindWithSpanTool() {
      var level = base.level;
      var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,
          new com.mojang.authlib.GameProfile(UUID.randomUUID(), "LSToolBoundOrdinary"));
      player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
      player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, EquipmentContent.TOOL.get().getDefaultInstance());
      for (var position : List.of(base.from, base.to, controllerPosition)) {
        player.setPos(position.getX() + .5, position.getY() + 1, position.getZ() + 2);
        base.helper.assertTrue(player.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(player,
            net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(position.getCenter(), Direction.UP, position, false)))
            == net.minecraft.world.InteractionResult.SUCCESS, "Span Tool failed while binding the ordinary controller at " + position);
      }
      player.setShiftKeyDown(true);
      player.setPos(controllerPosition.getX() + .5, controllerPosition.getY() + 1, controllerPosition.getZ() + 2);
      base.helper.assertTrue(player.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(player,
          net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(controllerPosition.getCenter(), Direction.UP, controllerPosition, false)))
          == net.minecraft.world.InteractionResult.SUCCESS, "Span Tool did not select the bound ordinary controller");
      player.setShiftKeyDown(false);
      for (var position : equipment) {
        player.setPos(position.getX() + .5, position.getY() + 1, position.getZ() + 2);
        base.helper.assertTrue(player.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(player,
            net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(position.getCenter(), Direction.UP, position, false)))
            == net.minecraft.world.InteractionResult.SUCCESS, "Span Tool failed while linking ordinary controller module at " + position);
        ((EquipmentCabinetEntity) level.getBlockEntity(position)).setArmed(true);
      }
      base.helper.assertTrue(controller.resolve(level) != null && controller.fault().isEmpty()
          && controller.modules().size() == equipment.size(), "Span Tool did not finish the ordinary controller installation");
    }
    void module(int distance, boolean fin) { module(distance, fin, false); }
    void module(int distance, boolean fin, boolean exit) {
      var level = base.level; var pos = base.from.offset(fin ? 1 : -1, cabinetY, reverse ? 64 - distance : distance);
      var motor = fin ? pos.east() : pos.west(); equipment.add(pos); motors.add(motor);
      level.setBlockAndUpdate(pos, (fin ? EquipmentContent.FIN : driveMechanism == Mechanism.LSM ? EquipmentContent.LSM : driveMechanism == Mechanism.LIM ? EquipmentContent.LIM : EquipmentContent.TIRE).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      level.setBlockAndUpdate(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, fin ? Direction.WEST : Direction.EAST));
      ((CreativeMotorBlockEntity) level.getBlockEntity(motor)).generatedSpeed.setValue(64);
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(pos);
      cabinet.bind(level, reverse ? base.to : base.from, reverse ? base.from : base.to);
      if (mode.contains("swing_") && cabinet.mechanism().linear()) cabinet.tuneLinear(1.5, cabinet.accelerationLimit(), cabinet.jerkLimit());
      if (!mode.endsWith("_tool_bound")) {
        controller.link(cabinet, exit);
        if (!"true".equals(System.getenv("LS_CABLE_PLAYER")) && !"true".equals(System.getenv("LS_FIXED_PLAYER"))) cabinet.setArmed(true);
      }
    }
    void at(int tick, Runnable action) {
      base.helper.runAtTickTime(tick + (fourthDimension ? 120 : 0), () -> { try { action.run(); }
        catch (RuntimeException failure) { System.err.println("LS_CONTROLLER_FAILURE mode=" + mode + " tick=" + tick + " "
            + (controller == null ? "controller not installed" : controller.status())); failure.printStackTrace(); cleanup(); throw failure; } });
    }
    double distance() {
      return measuredDistance;
    }
    void high(boolean value) { if (value) base.level.setBlockAndUpdate(controllerPosition.above(), Blocks.REDSTONE_BLOCK.defaultBlockState()); else base.level.removeBlock(controllerPosition.above(), false); }
    void cleanup() {
      if (closed) return; closed = true; SCENES.remove(this);
      base.level.removeBlock(controllerPosition.above(), false); base.level.removeBlock(controllerPosition, false);
      for (var vehicle : vehicles) for (var carrier : (List<?>) read(vehicle, "carriers")) {
        var container = SubLevelContainer.getContainer(base.level);
        if (container.getSubLevel((UUID) read(carrier, "body")) instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel body && !body.isRemoved())
          container.removeSubLevel(body, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
      }
      for (var vehicle : vehicles) if ("route_changeover_yaw_v1".equals(read(vehicle, "variant"))) {
        var deck = (UUID) read(read(vehicle, "yaw"), "deck");
        var container = SubLevelContainer.getContainer(base.level);
        if (deck != null && container.getSubLevel(deck) instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel body && !body.isRemoved())
          container.removeSubLevel(body, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
      }
      equipment.forEach(p -> base.level.removeBlock(p, false)); motors.forEach(p -> base.level.removeBlock(p, false)); base.cleanup();
    }
  }
  private static void observe(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.closed || s.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        if (s.controller.phase() == RideControllerEntity.State.RUNNING) s.runSteps++;
        s.measuredFront = 0; s.measuredBrakeFront = 0; double stepWork = 0; var contacted = new HashSet<BlockPos>();
        for (var cart : s.base.parents) {
          var contact = EngagedContacts.current(s.base.level, cart).orElseThrow();
          s.base.helper.assertTrue(s.track.matches(contact.hit()), "Controller train lost accepted native rail contact");
          s.measuredFront = Math.max(s.measuredFront, s.track.distance(contact.hit()));
          var brakeHit = VehicleInterfaces.contactHit(s.base.level, s.track, cart, VehicleInterfaces.brakePoint(cart));
          s.base.helper.assertTrue(brakeHit != null, "Controller fin missed accepted rail");
          s.measuredBrakeFront = Math.max(s.measuredBrakeFront, s.track.distance(brakeHit));
          if (cart == s.base.parents.getFirst()) s.measuredDistance = s.track.distance(contact.hit());
          var tireHit = VehicleInterfaces.contactHit(s.base.level, s.track, cart, VehicleInterfaces.tirePoint(cart));
          s.base.helper.assertTrue(tireHit != null, "Controller tire interface missed accepted rail");
          double distance = s.track.distance(tireHit), previous = s.oldDistances.getOrDefault(cart.getUniqueId(), distance);
          for (var pos : s.equipment) if (s.base.level.getBlockEntity(pos) instanceof EquipmentCabinetEntity cabinet
              && cabinet.mechanism() == Mechanism.TIRE && cabinet.interval().intersects(previous, distance, .6)) contacted.add(pos);
          s.oldDistances.put(cart.getUniqueId(), distance);
          var axis = s.track.forward(s.base.level, tireHit); var dir = new Vector3d(axis.x, axis.y, axis.z);
          var dynamics = NativeForces.pointDynamics(cart, event.getPhysicsSystem().getPipeline(), VehicleInterfaces.tirePoint(cart), dir);
          s.peakSpeed = Math.max(s.peakSpeed, Math.abs(dynamics.speed()));
          var group = cart.getQueuedForceGroups() == null ? null : cart.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
          var impulse = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalForce());
          double force = impulse.rotate(cart.logicalPose().orientation()).dot(dir) / event.getTimeStep();
          var motion = MotionProviders.resolve(s.base.level, cart.getUniqueId()).orElseThrow();
          s.base.helper.assertTrue(Math.abs(force) <= Math.min(motion.maximumForce(), NativeForces.mass(s.base.level, motion) * motion.maximumAcceleration()) + 1e-8,
              "Adjacent controlled drives exceeded the chassis force/acceleration rating");
          if (Math.abs(force) > 1e-9) {
            s.base.helper.assertTrue(s.controller.phase() == RideControllerEntity.State.RUNNING && s.controller.reservation().contains(cart.getUniqueId()), "Tire drive bypassed the controller reservation");
            double work = DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(), force, event.getTimeStep());
            s.work += Math.max(0, work); stepWork += Math.max(0, work); s.driveSteps++;
          }
          if (!s.launched && EquipmentRuntime.activeFinClamps(s.base.level, cart.getUniqueId()) > 0) s.closedPadSteps++;
        }
        double shaftBudget = contacted.stream().map(p -> (EquipmentCabinetEntity) s.base.level.getBlockEntity(p))
            .mapToDouble(c -> 8 * Math.abs(c.getSpeed()) * 2 * Math.PI / 60 * event.getTimeStep()).sum();
        s.base.helper.assertTrue(stepWork <= shaftBudget + 1e-8, "Adjacent tires created combined impulse work beyond their contacted shaft budget");
        s.base.helper.assertTrue(s.base.riders.stream().allMatch(net.minecraft.world.entity.animal.Pig::isPassenger), "Controller lost an ordinary rider");
      } catch (RuntimeException failure) { s.failure = failure.toString(); }
    }
  }
  private static void afterPhysics(dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.closed || s.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        int angle = 0;
        for (int car = 0; car < s.vehicles.size(); car++) for (var carrier : (List<?>) read(s.vehicles.get(car), "carriers")) {
          s.maxJointError = Math.max(s.maxJointError, separation(s.base.level, s.base.parents.get(car), carrier));
          if (s.launched && angle < s.initialAngles.size()) s.rotorResponse = Math.max(s.rotorResponse, Math.abs(((Number) read(carrier, "angle")).doubleValue() - s.initialAngles.get(angle)));
          angle++;
        }
        if (s.linked) s.maxCouplerError = Math.max(s.maxCouplerError, Math.abs(s.base.bearing(s.base.parents.getFirst()).distance(s.base.bearing(s.base.parents.getLast())) - 6));
      } catch (RuntimeException failure) { s.failure = failure.toString(); }
    }
  }
  @GameTestGenerator public static List<TestFunction> nativeControllerTests() {
    var tests = new ArrayList<TestFunction>(); int slot = 0;
    var modes = new ArrayList<>(List.of("single_forward", "single_reverse", "single_tool_bound", "linked_forward", "linked_reverse", "occupied_stop", "short_bank", "power_loss", "reload", "module_loss", "owned_module"));
    if (net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) modes.addAll(List.of("arrow_forward", "arrow_reverse", "arrow_tool_bound", "arrow_linked_forward", "arrow_linked_reverse", "free_spin_forward", "free_spin_reverse", "free_spin_tool_bound"));
    for (String mode : modes) {
      int index = slot++;
      tests.add(new TestFunction("launchsystems_controller", "coasterslaunchsystems.controller_" + mode,
          "coasterslaunchsystems:empty", 1200, 0, true, helper -> run(new Scene(helper, index, mode))));
    }
    return tests;
  }
  static void run(Scene s) {
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RideControllerGameTests::observe); NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RideControllerGameTests::afterPhysics); registered = true; }
    s.at(40, () -> { if (s.fourthDimension) s.boardFourthDimension(); else for (var cart : s.base.parents) for (int seat = 0; seat < 4; seat++) s.base.board(cart, seat); if (s.mode.equals("occupied_stop")) s.base.cart(32); });
    s.at(55, () -> {
      if (s.linked) s.base.helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(s.base.level, s.base.parents.getFirst(), s.base.parents.getLast(), 6), "Controller occupied cars could not couple");
      s.install(s.mode.equals("short_bank"));
      if (!s.mode.equals("owned_module")) s.controller.setArmed(true);
      s.baseline = s.base.speed(s.base.parents.getFirst()); SCENES.add(s);
    });
    s.at(60, () -> {
      if (!s.mode.equals("owned_module")) return;
      var cabinet = (EquipmentCabinetEntity) s.base.level.getBlockEntity(s.equipment.getFirst());
      cabinet.setArmed(false); s.controller.unlink(cabinet);
      s.base.helper.assertTrue(cabinet.controllerIdentity() == null && cabinet.controllerPosition() == null, "Empty module unlink retained its old owner");
      s.controller.link(cabinet); cabinet.setArmed(true); s.controller.setArmed(true);
    });
    s.at(80, () -> {
      if (s.mode.equals("short_bank") || s.mode.equals("occupied_stop")) {
        s.base.helper.assertTrue(s.controller.phase() == RideControllerEntity.State.FAULT && s.driveSteps == 0 && !s.controller.fault().isEmpty(), "Unusable/occupied stopping region granted a launch");
        s.high(true); return;
      }
      s.base.helper.assertTrue(s.controller.phase() == RideControllerEntity.State.READY && (s.fourthDimension ? s.closedPadSteps == 0 : s.closedPadSteps > 5) && s.driveSteps == 0,
          "Powered launch pads opened without permission or controller did not become ready: " + s.controller.status());
      s.base.helper.assertTrue(s.controller.signal(Direction.NORTH) == 15 && s.controller.signal(Direction.SOUTH) == 15, "Ready/reserved redstone outputs are incorrect");
      boolean disarmRejected = false;
      try { ((EquipmentCabinetEntity) s.base.level.getBlockEntity(s.equipment.getFirst())).setArmed(false); } catch (IllegalArgumentException expected) { disarmRejected = true; }
      s.base.helper.assertTrue(disarmRejected, "Manual disarm bypassed an occupied reservation");
      var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(s.base.level, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "LSReservedBreak"));
      var event = new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(s.base.level, s.equipment.getFirst(), s.base.level.getBlockState(s.equipment.getFirst()), player);
      NeoForge.EVENT_BUS.post(event); s.base.helper.assertTrue(event.isCanceled(), "Occupied module allowed normal player removal");
      if (s.mode.equals("reload")) {
        var tag = s.controller.saveWithFullMetadata(s.base.level.registryAccess());
        var restored = (RideControllerEntity) BlockEntity.loadStatic(s.controllerPosition, s.controller.getBlockState(), tag, s.base.level.registryAccess());
        restored.setLevel(s.base.level);
        s.base.helper.assertTrue(restored.identity().equals(s.controller.identity()) && restored.reservation().equals(s.controller.reservation())
            && !restored.armed() && restored.phase() == RideControllerEntity.State.INSPECTION && !restored.consumeRequest(), "Controller load replayed a launch or lost ownership");
        s.controller.loadWithComponents(tag, s.base.level.registryAccess());
        s.high(true); return;
      }
      if (s.mode.equals("module_loss")) { s.base.level.removeBlock(s.equipment.getFirst(), false); return; }
      if (s.mode.equals("owned_module")) {
        var pos = s.controllerPosition.south(2); s.equipment.add(pos);
        s.base.level.setBlockAndUpdate(pos, EquipmentContent.CONTROLLER.get().defaultBlockState());
        var other = (RideControllerEntity) s.base.level.getBlockEntity(pos); other.bind(s.base.level, s.base.from, s.base.to);
        // A second arming attempt cannot steal modules, and a retained reservation remains exclusive.
        boolean rejected = false;
        try { other.link((EquipmentCabinetEntity) s.base.level.getBlockEntity(s.equipment.getFirst())); } catch (IllegalArgumentException expected) { rejected = true; }
        s.base.helper.assertTrue(rejected, "Second controller stole an owned module"); return;
      }
      for (var vehicle : s.vehicles) for (var carrier : (List<?>) read(vehicle, "carriers")) s.initialAngles.add(((Number) read(carrier, "angle")).doubleValue());
      s.launched = true; s.high(true);
    });
    s.at(90, () -> {
      if (s.mode.startsWith("single") || s.linked || s.fourthDimension || s.mode.equals("power_loss")) {
        s.base.helper.assertTrue(s.controller.phase() == RideControllerEntity.State.RUNNING && s.driveSteps > 0 && s.work > .01, "Redstone launch did not queue native tire work");
        if (s.mode.equals("power_loss")) for (var pos : s.motors) ((CreativeMotorBlockEntity) s.base.level.getBlockEntity(pos)).generatedSpeed.setValue(0);
      }
    });
    s.at(120, () -> {
      s.base.helper.assertTrue(s.failure.isEmpty(), "Controller native observer failed: " + s.failure);
      if (s.mode.equals("power_loss") || s.mode.equals("module_loss")) {
        s.base.helper.assertTrue(s.controller.phase() == RideControllerEntity.State.FAULT && !s.controller.reservation().isEmpty(), "Interruption cleared ownership or continued launch");
        if (s.mode.equals("power_loss")) {
          s.base.helper.assertTrue(Math.abs(s.base.speed(s.base.parents.getFirst())) < .03 && EquipmentRuntime.activeFinClamps(s.base.level, s.base.parents.getFirst().getUniqueId()) > 0, "Power loss did not spring-apply the native launch pads");
          s.base.helper.assertTrue(s.distance() < 10, "Interrupted train escaped the contact window");
        }
      }
      if (s.mode.equals("reload")) s.base.helper.assertTrue(s.controller.phase() == RideControllerEntity.State.INSPECTION && s.driveSteps == 0
          && !s.controller.reservation().isEmpty(), "Saved input replayed through inspection");
      if (!(s.mode.startsWith("single") || s.linked || s.fourthDimension)) {
        System.out.println("LS_CONTROLLER mode=" + s.mode + " state=" + s.controller.status() + " work=" + s.work + " peak_speed=" + s.peakSpeed + " pads=" + s.closedPadSteps);
        s.cleanup(); s.base.helper.succeed();
      }
    });
    s.at(900, () -> {
      if (s.closed) return;
      s.base.helper.assertTrue(s.failure.isEmpty() && s.controller.phase() == RideControllerEntity.State.HOLD && s.driveSteps > 20 && s.runSteps > 20,
          "Controlled native launch did not reach its held stop: " + s.controller.status() + " observer=" + s.failure + " position=" + s.distance());
      s.base.helper.assertTrue(Math.abs(s.base.speed(s.base.parents.getFirst())) < .03 && s.measuredBrakeFront >= 19.4 && s.measuredBrakeFront < 42,
          "Loaded train escaped the certified fin bank");
      s.base.helper.assertTrue(s.controller.reservation().size() == s.base.parents.size(), "Final hold released the whole-consist reservation");
      s.base.helper.assertTrue(s.maxJointError < .01 && s.maxCouplerError < .05, "Controlled launch lost native carrier/coupler constraints");
      if (s.mode.startsWith("free_spin")) s.base.helper.assertTrue(s.rotorResponse > .02, "Controlled drive suppressed native free carrier response");
      int count = s.driveSteps;
      s.at(940, () -> {
        s.base.helper.assertTrue(s.driveSteps == count && s.controller.phase() == RideControllerEntity.State.HOLD && !s.controller.reservation().isEmpty(), "Held-high redstone replayed or final hold cleared occupancy");
        System.out.println("LS_CONTROLLER mode=" + s.mode + " cars=" + s.base.parents.size() + " riders=" + s.base.riders.size() + " mass=" + s.base.parents.stream().mapToDouble(s.base::mass).sum()
            + " state=" + s.controller.status() + " distance=" + s.distance() + " speed=" + s.base.speed(s.base.parents.getFirst()) + " work=" + s.work + " peak_speed=" + s.peakSpeed + " drive_steps=" + s.driveSteps + " joint_error=" + s.maxJointError + " coupler_error=" + s.maxCouplerError + " rotor_response=" + s.rotorResponse);
        s.cleanup(); s.base.helper.succeed();
      });
    });
  }
}
