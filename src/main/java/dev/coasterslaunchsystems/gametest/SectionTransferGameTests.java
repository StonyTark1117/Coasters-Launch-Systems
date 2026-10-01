package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.*;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.*;
import dev.coasterslaunchsystems.track.*;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

@GameTestHolder("coasterslaunchsystems")
public final class SectionTransferGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static final class Scene {
    final RideControllerGameTests.Scene source;
    final String mode;
    final BlockPos otherEnd, receiverPos;
    final List<BlockPos> modules = new ArrayList<>(), motors = new ArrayList<>();
    final List<ServerSubLevel> train = new ArrayList<>();
    final Set<UUID> removedSeatRiders = new HashSet<>();
    final Map<UUID, Double> oldDistances = new HashMap<>();
    RideControllerEntity receiver;
    DirectedTrack sourceTrack, targetTrack;
    double work, peakSpeed, maxJoint, maxCoupler, minimumTail = Double.POSITIVE_INFINITY;
    int straddling, retainedTailSteps, transferForceSteps, postClearSteps, faultForceSteps, stoppedDriveSteps, missingCarrierDenialSteps;
    boolean departure, disturbed, missingCarrier, closed;
    String failure = "";
    Scene(GameTestHelper helper, int slot, String mode) {
      this.mode = mode;
      String design = mode.startsWith("arrow") || mode.startsWith("free_spin") ? mode : mode.contains("single") ? mode : "linked_forward";
      // Adverse ordinary modes still use a fully occupied native coupled pair.
      if (mode.equals("power_loss") || mode.equals("rollback") || mode.startsWith("reload") || mode.equals("occupied_destination") || mode.equals("short_destination") || mode.equals("missing_destination")) design = "linked_forward";
      if (mode.equals("arrow_missing_carrier")) design = "arrow_linked_forward";
      source = new RideControllerGameTests.Scene(helper, slot, design, 272);
      otherEnd = source.reverse ? source.base.from.offset(0, 0, -64) : source.base.to.offset(0, 0, 64);
      var joint = source.reverse ? source.base.from : source.base.to; receiverPos = joint.offset(4, -2, source.reverse ? -8 : 8);
      for (int x = Math.floorDiv(joint.getX(), 16) - 1; x <= Math.floorDiv(joint.getX(), 16) + 1; x++)
        for (int z = Math.floorDiv(Math.min(joint.getZ(), otherEnd.getZ()), 16) - 1; z <= Math.floorDiv(Math.max(joint.getZ(), otherEnd.getZ()), 16) + 1; z++) source.base.level.setChunkForced(x, z, true);
      source.base.level.setBlockAndUpdate(otherEnd, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if (!source.fourthDimension) connectOrdinary(joint, otherEnd);
      else {
        // Build the connected route before the public factory creates a vehicle at tick 15.
        source.base.helper.runAtTickTime(10, () -> {
          var designType = type("dev.fourthdimension.track.TrackDesign");
          Object selected = Arrays.stream(designType.getEnumConstants()).filter(v -> ((Enum<?>) v).name().equals(mode.startsWith("arrow") ? "ARROW" : "FREE_SPIN")).findFirst().orElseThrow();
          var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(source.base.level, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "LSTransferRail"));
          player.setGameMode(net.minecraft.world.level.GameType.CREATIVE); player.setPos(otherEnd.getX() + 2, otherEnd.getY() + 1, otherEnd.getZ());
          var item = (net.minecraft.world.item.Item) ((java.util.function.Supplier<?>) readStatic("dev.fourthdimension.track.TrackItems", "ARROW")).get(); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, item.getDefaultInstance());
          invoke("dev.fourthdimension.track.TrackPlacement", "connect", new Class<?>[] {net.minecraft.server.level.ServerLevel.class, BlockPos.class, BlockPos.class, designType,
              net.minecraft.world.entity.player.Player.class, net.minecraft.world.item.ItemStack.class}, source.base.level, joint, otherEnd, selected, player, player.getMainHandItem());
        });
      }
    }
    void connectOrdinary(BlockPos from, BlockPos to) {
      var level = source.base.level; double sign = to.getZ() > from.getZ() ? 1 : -1;
      var curve = new BezierConnection(Couple.create(from, to), Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level, from), CoasterAnchorpointBlockEntity.worldCenter(level, to)),
          Couple.create(new net.minecraft.world.phys.Vec3(0, 0, sign), new net.minecraft.world.phys.Vec3(0, 0, -sign)),
          Couple.create(new net.minecraft.world.phys.Vec3(0, 1, 0), new net.minecraft.world.phys.Vec3(0, 1, 0)), true, false, CoasterTrackMaterials.COASTER);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
    }
    void install() {
      var level = source.base.level; source.install(false); sourceTrack = source.track;
      for (int s = 11; s <= 63; s += 2) source.module(s, false, true);
      for (int s = 43; s <= 63; s += 2) source.module(s, true);
      var joint = source.reverse ? source.base.from : source.base.to;
      targetTrack = DirectedTrack.resolve(level, joint, otherEnd).orElseThrow();
      level.setBlockAndUpdate(receiverPos, EquipmentContent.CONTROLLER.get().defaultBlockState()); receiver = (RideControllerEntity) level.getBlockEntity(receiverPos);
      receiver.bind(level, joint, otherEnd);
      for (int s = 1; s <= 17; s += 2) { module(s, false); module(s, true); }
      for (int s = 21; s <= (mode.equals("short_destination") ? 21 : 41); s += 2) module(s, true);
      source.controller.connectDestination(receiver); receiver.setArmed(true); source.controller.setArmed(true);
    }
    void module(int distance, boolean fin) {
      var level = source.base.level; var joint = source.reverse ? source.base.from : source.base.to;
      var pos = joint.offset(fin ? 1 : -1, -2, source.reverse ? -distance : distance); var motor = fin ? pos.east() : pos.west(); modules.add(pos); motors.add(motor);
      level.setBlockAndUpdate(pos, (fin ? EquipmentContent.FIN : EquipmentContent.TIRE).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      level.setBlockAndUpdate(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, fin ? Direction.WEST : Direction.EAST));
      ((CreativeMotorBlockEntity) level.getBlockEntity(motor)).generatedSpeed.setValue(64);
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(pos); cabinet.bind(level, joint, otherEnd); receiver.link(cabinet); cabinet.setArmed(true);
    }
    void at(int tick, Runnable action) {
      source.base.helper.runAtTickTime(tick + (source.fourthDimension ? 120 : 0), () -> {
        try { action.run(); } catch (RuntimeException failure) {
          System.err.println("LS_TRANSFER_FAILURE mode=" + mode + " tick=" + tick + " source=" + (source.controller == null ? "unbound" : source.controller.status()) + " receiver=" + (receiver == null ? "unbound" : receiver.status())); failure.printStackTrace(); cleanup(); throw failure;
        }
      });
    }
    void cleanup() {
      if (closed) return; closed = true; SCENES.remove(this);
      var joint = source.reverse ? source.base.from : source.base.to;
      if (source.base.level.getBlockEntity(joint) instanceof CoasterAnchorpointBlockEntity anchor) anchor.removeAnchorPeerCurve(source.base.level, otherEnd);
      modules.forEach(p -> source.base.level.removeBlock(p, false)); motors.forEach(p -> source.base.level.removeBlock(p, false));
      source.base.level.removeBlock(receiverPos, false); source.base.level.removeBlock(otherEnd, false); source.cleanup();
    }
  }
  private static void before(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.closed || s.source.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        if (s.missingCarrier) {
          for (var cart : s.train) {
            var group = cart.getQueuedForceGroups() == null ? null : cart.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
            s.source.base.helper.assertTrue(group == null || group.getForceTotal().getLocalForce().lengthSquared() < 1e-18,
                "Unavailable Arrow carrier continued whole-consist propulsion");
            var motion = MotionProviders.resolve(s.source.base.level, cart.getUniqueId()).orElseThrow();
            s.source.base.helper.assertTrue(!motion.propulsionAllowed() && !motion.brakingAllowed(), "Missing primary carrier fell through to an ordinary motion grant");
          }
          s.missingCarrierDenialSteps++; continue;
        }
        boolean onSource = false, onTarget = false; double stepWork = 0; var contacted = new HashSet<BlockPos>();
        var direction = s.sourceTrack.forward(s.source.base.level, s.sourceTrack.hitAtDistance(s.source.base.level, 0)); var axis = new Vector3d(direction.x, direction.y, direction.z);
        double joint = s.targetTrack.hitAtDistance(s.source.base.level, 0).point().z * (s.source.reverse ? -1 : 1), minimumCorner = Double.POSITIVE_INFINITY;
        for (var cart : s.train) {
          var contact = EngagedContacts.current(s.source.base.level, cart).orElseThrow();
          s.source.base.helper.assertTrue(s.sourceTrack.matches(contact.hit()) || s.targetTrack.matches(contact.hit()), "Transfer lost accepted native rail engagement");
          onSource |= s.sourceTrack.matches(contact.hit()); onTarget |= s.targetTrack.matches(contact.hit());
          var current = s.sourceTrack.matches(contact.hit()) ? s.sourceTrack : s.targetTrack; double distance = current.distance(contact.hit());
          double global = distance + (current == s.targetTrack ? 64 : 0), old = s.oldDistances.getOrDefault(cart.getUniqueId(), global); s.oldDistances.put(cart.getUniqueId(), global);
          for (var pos : concat(s.source.equipment, s.modules)) if (s.source.base.level.getBlockEntity(pos) instanceof EquipmentCabinetEntity cabinet && cabinet.mechanism() == Mechanism.TIRE) {
            var owner = RideControllers.owner(s.source.base.level, cabinet); double offset = owner == s.receiver ? 64 : 0;
            if (cabinet.interval().intersects(old - offset, global - offset, .6)) contacted.add(pos);
          }
          var motion = MotionProviders.resolve(s.source.base.level, cart.getUniqueId()).orElseThrow();
          for (var id : motion.bodies()) {
            var body = (ServerSubLevel) SubLevelContainer.getContainer(s.source.base.level).getSubLevel(id); var bounds = body.getPlot().getBoundingBox();
            for (double x : new double[] {bounds.minX(), bounds.maxX() + 1}) for (double y : new double[] {bounds.minY(), bounds.maxY() + 1}) for (double z : new double[] {bounds.minZ(), bounds.maxZ() + 1}) {
              var point = nativePoint(body, new Vector3d(x, y, z)); minimumCorner = Math.min(minimumCorner, point.z * (s.source.reverse ? -1 : 1));
            }
          }
          var group = cart.getQueuedForceGroups() == null ? null : cart.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
          var impulse = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalForce()); double force = impulse.rotate(cart.logicalPose().orientation()).dot(axis) / event.getTimeStep();
          var dynamics = NativeForces.pointDynamics(cart, event.getPhysicsSystem().getPipeline(), VehicleInterfaces.tirePoint(cart), axis); s.peakSpeed = Math.max(s.peakSpeed, Math.abs(dynamics.speed()));
          s.source.base.helper.assertTrue(Math.abs(force) <= Math.min(motion.maximumForce(), NativeForces.mass(s.source.base.level, motion) * motion.maximumAcceleration()) + 1e-8, "Transfer drive exceeded vehicle limits");
          if (Math.abs(force) > 1e-9) {
            double work = DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(), force, event.getTimeStep()); s.work += Math.max(0, work); stepWork += Math.max(0, work);
            if (s.departure) s.transferForceSteps++;
            if (s.source.controller.phase() == RideControllerEntity.State.FAULT || s.receiver.phase() == RideControllerEntity.State.FAULT) s.faultForceSteps++;
          }
        }
        s.minimumTail = Math.min(s.minimumTail, minimumCorner - joint);
        if (s.departure && s.source.controller.reservation().isEmpty()) {
          s.source.base.helper.assertTrue(!onSource && minimumCorner > joint + .99 && !s.receiver.reservation().isEmpty(), "Source released before the physical native tail cleared into its reserved destination"); s.postClearSteps++;
        } else if (s.departure && onTarget) {
          s.source.base.helper.assertTrue(s.source.controller.reservation().size() == s.train.size() && s.receiver.reservation().equals(s.source.controller.reservation()), "Transfer failed to retain both whole-consist leases"); s.retainedTailSteps++;
        }
        if (onSource && onTarget) s.straddling++;
        double budget = contacted.stream().map(p -> (EquipmentCabinetEntity) s.source.base.level.getBlockEntity(p)).mapToDouble(c -> 8 * Math.abs(c.getSpeed()) * 2 * Math.PI / 60 * event.getTimeStep()).sum();
        s.source.base.helper.assertTrue(stepWork <= budget + 1e-8, "Transfer duplicated contacted shaft work");
        s.source.base.helper.assertTrue(s.source.base.riders.stream().allMatch(net.minecraft.world.entity.animal.Pig::isPassenger), "Transfer lost a real rider");
        if (onSource && onTarget && !s.disturbed && s.departure) {
          if (s.mode.equals("power_loss")) { ((CreativeMotorBlockEntity) s.source.base.level.getBlockEntity(s.motors.getFirst())).generatedSpeed.setValue(0); s.disturbed = true; }
          if (s.mode.startsWith("reload")) {
            var a = s.source.controller.saveWithFullMetadata(s.source.base.level.registryAccess());
            var b = s.receiver.saveWithFullMetadata(s.source.base.level.registryAccess());
            s.source.controller.loadWithComponents(a, s.source.base.level.registryAccess()); s.receiver.loadWithComponents(b, s.source.base.level.registryAccess()); s.disturbed = true;
          }
          if (s.mode.equals("missing_destination")) { s.source.base.level.removeBlock(s.receiverPos, false); s.disturbed = true; }
          if (s.mode.equals("arrow_missing_carrier")) {
            var carrier = ((List<?>) read(s.source.vehicles.getFirst(), "carriers")).getFirst();
            var id = (UUID) read(carrier, "body"); var body = SubLevelContainer.getContainer(s.source.base.level).getSubLevel(id);
            // Seat entities are transformed to world space; preserve native body identity when actually boarding.
            for (var rider : s.source.base.riders) if (id.equals(s.source.riderBodies.get(rider.getUUID()))) s.removedSeatRiders.add(rider.getUUID());
            int seats = ((List<?>) read(carrier, "seats")).size();
            s.source.base.helper.assertTrue(seats > 0 && s.removedSeatRiders.size() == seats, "Missing-carrier fixture did not identify its actual occupied seats: " + s.removedSeatRiders.size() + "/" + seats);
            SubLevelContainer.getContainer(s.source.base.level).removeSubLevel(body, SubLevelRemovalReason.UNLOADED); s.disturbed = true; s.missingCarrier = true;
          }
        }
        if (s.mode.equals("rollback") && s.departure && s.straddling > 0 && s.source.controller.phase() == RideControllerEntity.State.EXITING) {
          s.disturbed = true;
          for (var cart : s.train) NativeForces.pointForce(cart, cart.getMassTracker().getCenterOfMass(), new Vector3d(axis).mul(-s.source.base.mass(cart) * 6), event.getTimeStep());
        }
      } catch (RuntimeException failure) { s.failure = failure.toString(); }
    }
  }
  private static List<BlockPos> concat(List<BlockPos> a, List<BlockPos> b) { var all = new ArrayList<>(a); all.addAll(b); return all; }
  private static void after(ForgeSablePostPhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.closed || s.source.base.level != event.getPhysicsSystem().getLevel() || s.missingCarrier) continue;
      try {
        for (int car = 0; car < s.source.vehicles.size(); car++) for (var carrier : (List<?>) read(s.source.vehicles.get(car), "carriers"))
          s.maxJoint = Math.max(s.maxJoint, separation(s.source.base.level, s.train.get(car), carrier));
        if (s.train.size() == 2) s.maxCoupler = Math.max(s.maxCoupler, Math.abs(s.source.base.bearing(s.train.getFirst()).distance(s.source.base.bearing(s.train.getLast())) - 6));
      } catch (RuntimeException failure) { s.failure = failure.toString(); }
    }
  }
  @GameTestGenerator public static List<TestFunction> transfers() {
    var modes = new ArrayList<>(List.of("single_forward", "single_reverse", "linked_forward", "linked_reverse", "occupied_destination", "short_destination", "rollback", "power_loss", "reload", "reload_resume", "missing_destination"));
    if (ModList.get().isLoaded("fourthdimension")) modes.addAll(List.of("arrow_forward", "arrow_reverse", "arrow_linked_forward", "arrow_linked_reverse", "free_spin_forward", "free_spin_reverse", "arrow_missing_carrier"));
    var tests = new ArrayList<TestFunction>(); int slot = 0;
    for (var mode : modes) { int index = slot++; tests.add(new TestFunction("launchsystems_section_transfer", "coasterslaunchsystems.transfer_" + mode, "coasterslaunchsystems:empty", mode.equals("reload_resume") ? 3000 : 2300, 0, true, h -> run(new Scene(h, index, mode)))); }
    return tests;
  }
  private static void run(Scene s) {
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SectionTransferGameTests::before); NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SectionTransferGameTests::after); registered = true; }
    s.at(40, () -> { s.train.addAll(s.source.base.parents); if (s.source.fourthDimension) s.source.boardFourthDimension(); else for (var cart : s.train) for (int seat = 0; seat < 4; seat++) s.source.base.board(cart, seat); });
    s.at(55, () -> {
      if (s.train.size() == 2) s.source.base.helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(s.source.base.level, s.train.getFirst(), s.train.getLast(), 6), "Loaded native transfer pair did not couple");
      s.install();
      if (s.mode.equals("occupied_destination")) {
        var foreign = s.source.base.cart(s.targetTrack.hitAtDistance(s.source.base.level, 32)); for (int seat = 0; seat < 4; seat++) s.source.base.board(foreign, seat);
      }
      SCENES.add(s);
    });
    s.at(80, () -> { s.source.base.helper.assertTrue(s.source.controller.phase() == RideControllerEntity.State.READY, "Initial section was not ready: " + s.source.controller.status()); s.source.high(true); });
    s.at(900, () -> {
      s.source.base.helper.assertTrue(s.failure.isEmpty(), "Native transfer observer failed: " + s.failure);
      boolean blocked = s.mode.equals("occupied_destination") || s.mode.equals("short_destination");
      s.source.base.helper.assertTrue(s.source.controller.phase() == (blocked ? RideControllerEntity.State.HOLD : RideControllerEntity.State.READY_EXIT), "Held section did not evaluate its downstream gate: " + s.source.controller.status());
      s.source.base.helper.assertTrue(!s.source.controller.transferring() && s.transferForceSteps == 0, "Held-high launch input replayed a departure");
      s.source.high(false);
    });
    s.at(940, () -> { s.departure = true; s.source.high(true); });
    s.at(980, () -> {
      if (s.mode.equals("occupied_destination") || s.mode.equals("short_destination")) {
        s.source.base.helper.assertTrue(!s.source.controller.transferring() && s.transferForceSteps == 0 && s.source.controller.reservation().size() == s.train.size(), "Blocked downstream gate released its train");
        System.out.println("LS_TRANSFER mode=" + s.mode + " source=" + s.source.controller.status() + " receiver=" + s.receiver.status() + " work=" + s.work); s.cleanup(); s.source.base.helper.succeed();
      } else s.source.base.helper.assertTrue(s.source.controller.phase() == RideControllerEntity.State.EXITING && s.receiver.phase() == RideControllerEntity.State.RECEIVING
          && s.source.controller.reservation().equals(s.receiver.reservation()) && s.transferForceSteps > 10, "Departure did not reserve both sections before applying native tire work");
    });
    if (s.mode.equals("reload_resume")) {
      s.at(1600, () -> {
        s.source.base.helper.assertTrue(s.disturbed && !s.source.controller.armed() && !s.receiver.armed()
            && s.receiver.reservation().equals(s.source.controller.reservation()), "Reload lost the paired inspection reservation");
        s.stoppedDriveSteps = s.transferForceSteps; s.receiver.setArmed(true);
      });
      s.at(1640, () -> {
        s.source.base.helper.assertTrue(s.receiver.phase() == RideControllerEntity.State.HOLD && s.transferForceSteps == s.stoppedDriveSteps,
            "First inspected controller faulted or restarted the saved transfer");
        s.source.controller.setArmed(true);
      });
      s.at(1680, () -> {
        s.source.base.helper.assertTrue(s.source.controller.phase() == RideControllerEntity.State.READY_EXIT && s.transferForceSteps == s.stoppedDriveSteps,
            "Sequential rearming failed readiness or replayed held-high input: " + s.source.controller.status());
        s.source.high(false);
      });
      s.at(1720, () -> s.source.high(true));
    }
    s.at(s.mode.equals("reload_resume") ? 2700 : 1900, () -> {
      if (s.closed) return;
      s.source.base.helper.assertTrue(s.failure.isEmpty(), "Native transfer observer failed: " + s.failure);
      boolean interrupted = List.of("rollback", "power_loss", "reload", "missing_destination", "arrow_missing_carrier").contains(s.mode);
      if (interrupted) {
        s.source.base.helper.assertTrue(s.disturbed && s.source.controller.reservation().size() == s.train.size() && s.source.controller.transferring()
            && s.faultForceSteps == 0, "Interruption cleared a lease or continued a faulted drive");
        if (s.mode.equals("reload")) s.source.base.helper.assertTrue(!s.source.controller.armed() && !s.receiver.armed() && s.source.controller.phase() == RideControllerEntity.State.INSPECTION
            && s.receiver.phase() == RideControllerEntity.State.INSPECTION && s.receiver.reservation().equals(s.source.controller.reservation()), "Transfer save replayed or lost either lease");
        else s.source.base.helper.assertTrue(s.source.controller.phase() == RideControllerEntity.State.FAULT, "Transfer interruption never faulted");
        if (!s.mode.equals("missing_destination")) s.source.base.helper.assertTrue(s.receiver.reservation().equals(s.source.controller.reservation()),
            "Transfer interruption lost the receiving consist reservation");
        if (!s.mode.equals("missing_destination") && !s.mode.equals("reload")) s.source.base.helper.assertTrue(s.receiver.phase() == RideControllerEntity.State.FAULT,
            "Live transfer receiver failed to revoke its drive grant after interruption");
        if (s.mode.equals("arrow_missing_carrier")) s.source.base.helper.assertTrue(s.missingCarrierDenialSteps > 20,
            "Missing carrier lacked repeated native whole-consist denial evidence");
      } else {
        s.source.base.helper.assertTrue(s.source.controller.reservation().isEmpty() && !s.source.controller.transferring() && s.receiver.incoming() == null
            && s.receiver.reservation().size() == s.train.size() && s.receiver.phase() == RideControllerEntity.State.HOLD && s.postClearSteps > 20 && s.retainedTailSteps > 5,
            "Native tail transfer did not finish into a held reserved destination: " + s.source.controller.status() + " / " + s.receiver.status());
        if (s.train.size() == 2) s.source.base.helper.assertTrue(s.straddling > 10, "Coupled fixture did not straddle the actual rail joint");
        s.source.base.helper.assertTrue(s.train.stream().allMatch(c -> Math.abs(s.source.base.speed(c)) < .03), "Received train lost its final hold");
      }
      s.source.base.helper.assertTrue(s.maxJoint < .01 && s.maxCoupler < .05, "Section transfer lost native constraints: " + s.maxJoint + " / " + s.maxCoupler);
      s.source.base.helper.assertTrue(s.source.base.riders.stream().filter(r -> !s.removedSeatRiders.contains(r.getUUID())).allMatch(net.minecraft.world.entity.animal.Pig::isPassenger),
          "Section transfer lost a rider from a surviving native seat");
      System.out.println("LS_TRANSFER mode=" + s.mode + " cars=" + s.train.size() + " riders=" + s.source.base.riders.size() + " source=" + s.source.controller.status()
          + " receiver=" + s.receiver.status() + " straddling=" + s.straddling + " retained_tail_steps=" + s.retainedTailSteps + " post_clear_steps=" + s.postClearSteps
          + " drive_steps=" + s.transferForceSteps + " missing_carrier_denial_steps=" + s.missingCarrierDenialSteps + " removed_seat_riders=" + s.removedSeatRiders.size()
          + " work=" + s.work + " peak_speed=" + s.peakSpeed + " joint_error=" + s.maxJoint + " coupler_error=" + s.maxCoupler);
      s.cleanup(); s.source.base.helper.succeed();
    });
  }
}
