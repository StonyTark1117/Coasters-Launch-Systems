package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.*;
import dev.coasterslaunchsystems.math.*;
import dev.coasterslaunchsystems.runtime.*;
import dev.coasterslaunchsystems.track.*;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.neoforge.event.*;
import dev.silvergold.simulatedcoasters.*;
import dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Real closed native rails, two retained sections, two complete laps and a fresh boarding departure. */
@GameTestHolder("coasterslaunchsystems_routes")
public final class CircuitGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static final class Scene {
    final RideControllerGameTests.Scene source;
    final String guard;
    ServerSubLevel foreign;
    final BlockPos bFrom, bTo, bPos;
    final List<BlockPos> modules = new ArrayList<>(), motors = new ArrayList<>();
    final Map<UUID, Map<BlockPos, Double>> previous = new HashMap<>();
    RideControllerEntity receiver;
    DirectedTrack aTrack, bTrack;
    int stage = 1, departures = 1, curveSteps, retainedSteps, boardedSteps;
    boolean pending, boarding, reboarded, closed;
    double work, peakSpeed, maxJoint, maxCoupler, rotorResponse;
    double guardWork;
    double loadedMass, emptyMass;
    int seatedCount;
    boolean guardApplied, movingCaptureDenied;
    int capturingSteps, boardingDeniedSteps;
    long lastReport = -1;
    String failure = "";
    Scene(GameTestHelper helper, int slot, String mode) { this(helper, slot, mode, ""); }
    Scene(GameTestHelper helper, int slot, String mode, String guard) {
      this.guard = guard;
      source = new RideControllerGameTests.Scene(helper, slot, mode, 304);
      bFrom = source.base.to.east(16); bTo = source.base.from.east(16); bPos = bFrom.offset(4, -2, -8);
      for (var p : List.of(bFrom, bTo)) source.base.level.setBlockAndUpdate(p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if (source.fourthDimension) helper.runAtTickTime(10, this::buildRails); else buildRails();
    }
    void buildRails() {
      if (source.fourthDimension) {
        var designType = type("dev.fourthdimension.track.TrackDesign");
        var design = Arrays.stream(designType.getEnumConstants()).filter(v -> ((Enum<?>) v).name().equals(source.mode.startsWith("arrow") ? "ARROW" : "FREE_SPIN")).findFirst().orElseThrow();
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(source.base.level, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "LSCircuit"));
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var item = (net.minecraft.world.item.Item) ((java.util.function.Supplier<?>) readStatic("dev.fourthdimension.track.TrackItems", "ARROW")).get(); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, item.getDefaultInstance());
        for (var ends : List.of(List.of(bFrom, bTo), List.of(source.base.to, bFrom), List.of(bTo, source.base.from))) {
          player.setPos(ends.getLast().getX() + 2, ends.getLast().getY() + 1, ends.getLast().getZ());
          invoke("dev.fourthdimension.track.TrackPlacement", "connect", new Class<?>[] {net.minecraft.server.level.ServerLevel.class, BlockPos.class, BlockPos.class, designType,
              net.minecraft.world.entity.player.Player.class, net.minecraft.world.item.ItemStack.class}, source.base.level, ends.getFirst(), ends.getLast(), design, player, player.getMainHandItem());
        }
        // The pinned connect operation optimizes new axes without applying its neighbors' join writes.
        // Author the empty turn geometry, then use the public profile rebind before creating any vehicle.
        curve(source.base.to, bFrom, new Vec3(0, 0, 1), new Vec3(0, 0, 1), 32.0 / 3);
        curve(bTo, source.base.from, new Vec3(0, 0, -1), new Vec3(0, 0, -1), 32.0 / 3);
        var data = invoke("dev.fourthdimension.runtime.FoundationData", "get", new Class<?>[] {net.minecraft.server.level.ServerLevel.class}, source.base.level);
        for (var rail : (List<?>) read(data, "rails")) {
          var from = (BlockPos) read(rail, "from"); var to = (BlockPos) read(rail, "to");
          if (!(from.equals(source.base.to) && to.equals(bFrom) || from.equals(bFrom) && to.equals(source.base.to)
              || from.equals(bTo) && to.equals(source.base.from) || from.equals(source.base.from) && to.equals(bTo))) continue;
          player.setPos(from.getX() + 2, from.getY() + 1, from.getZ());
          invoke("dev.fourthdimension.track.ProfileEditor", "rebind", new Class<?>[] {net.minecraft.server.level.ServerPlayer.class, UUID.class, long.class}, player, read(rail, "id"), ((Number) read(rail, "revision")).longValue());
        }
      } else {
        curve(bFrom, bTo, new Vec3(0, 0, -1), new Vec3(0, 0, 1), 64.0 / 3);
        curve(source.base.to, bFrom, new Vec3(0, 0, 1), new Vec3(0, 0, 1), 32.0 / 3);
        curve(bTo, source.base.from, new Vec3(0, 0, -1), new Vec3(0, 0, -1), 32.0 / 3);
      }
    }
    void curve(BlockPos from, BlockPos to, Vec3 a, Vec3 b, double length) {
      var level = source.base.level;
      var curve = new BezierConnection(Couple.create(from, to), Couple.create(CoasterAnchorpointBlockEntity.worldCenter(level, from), CoasterAnchorpointBlockEntity.worldCenter(level, to)),
          Couple.create(a, b), Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)), true, false, CoasterTrackMaterials.COASTER);
      CoasterPeerCurveHandleLengths.putCanonical(curve, length, length);
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
    }
    void install() {
      var level = source.base.level; source.install(false); aTrack = source.track;
      for (int s = 11; s <= 63; s += 2) source.module(s, false, true);
      for (int s = 43; s <= 63; s += 2) source.module(s, true);
      level.setBlockAndUpdate(bPos, EquipmentContent.CONTROLLER.get().defaultBlockState()); receiver = (RideControllerEntity) level.getBlockEntity(bPos);
      receiver.bind(level, bFrom, bTo); bTrack = DirectedTrack.resolve(level, bFrom, bTo).orElseThrow();
      for (int s = 2; s <= 16; s += 2) module(s, false, false);
      for (int s = 1; s <= 17; s += 2) module(s, true, false);
      for (int s = 21; s <= 63; s += 2) module(s, true, false);
      for (int s = 11; s <= 63; s += 2) module(s, false, true);
      if (source.fourthDimension) {
        var curve = DirectedTrack.resolve(level, source.base.to, bFrom).orElseThrow();
        System.out.println("LS_CIRCUIT_JOIN mode=" + source.mode + " a_end=" + aTrack.hitAtDistance(level, aTrack.length()).point()
            + " a_forward=" + aTrack.forward(level, aTrack.hitAtDistance(level, aTrack.length())) + " curve_start=" + curve.hitAtDistance(level, 0).point()
            + " curve_forward=" + curve.forward(level, curve.hitAtDistance(level, 0)) + " curve_end_forward=" + curve.forward(level, curve.hitAtDistance(level, curve.length()))
            + " b_forward=" + bTrack.forward(level, bTrack.hitAtDistance(level, 0)));
      }
      source.controller.connectDestination(receiver); receiver.connectDestination(source.controller);
      source.controller.setStationCapture(guard.isEmpty());
      source.controller.setArmed(true); receiver.setArmed(true);
      source.base.helper.assertTrue(source.controller.transferRoute(level, receiver).tracks().size() == 3, "Circuit did not reserve its curved intermediate rail");
    }
    void module(int s, boolean fin, boolean exit) {
      var level = source.base.level; var pos = bFrom.offset(fin ? 1 : -1, -2, -s); var motor = fin ? pos.east() : pos.west(); modules.add(pos); motors.add(motor);
      level.setBlockAndUpdate(pos, (fin ? EquipmentContent.FIN : EquipmentContent.TIRE).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      level.setBlockAndUpdate(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, fin ? Direction.WEST : Direction.EAST));
      ((CreativeMotorBlockEntity) level.getBlockEntity(motor)).generatedSpeed.setValue(64);
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(pos); cabinet.bind(level, bFrom, bTo); receiver.link(cabinet, exit); cabinet.setArmed(true);
    }
    void high(RideControllerEntity controller, boolean value) {
      source.base.level.setBlockAndUpdate(controller.getBlockPos().above(), (value ? Blocks.REDSTONE_BLOCK : Blocks.AIR).defaultBlockState());
    }
    void tick() {
      if (closed || receiver == null) return;
      var h = source.base.helper;
      h.assertTrue(failure.isEmpty(), "Circuit native observer failed: " + failure);
      h.assertTrue(source.controller.phase() != RideControllerEntity.State.FAULT && receiver.phase() != RideControllerEntity.State.FAULT,
          "Circuit faulted: " + source.controller.status() + " / " + receiver.status());
      if (pending) return;
      if (!guard.isEmpty()) {
        if (guardApplied || source.controller.phase() != RideControllerEntity.State.READY_EXIT) return;
        guardApplied = true; guardWork = work;
        var curve = DirectedTrack.resolve(source.base.level, source.base.to, bFrom).orElseThrow();
        if (guard.equals("occupied_curve")) {
          foreign = source.base.cart(curve.hitAtDistance(source.base.level, curve.length() / 2)); source.base.parents.remove(foreign);
        } else {
          int revision = CoasterPathGraphManager.get(source.base.level).revision();
          CoasterPeerCurveHandleLengths.putCanonical(curve.edge().bezier(), 12, 12);
          h.assertTrue(CoasterPathGraphManager.get(source.base.level).revision() == revision, "Handle-only edit unexpectedly revised the graph; cache invalidation was not isolated");
        }
        high(source.controller, false); pending = true;
        h.runAfterDelay(4, () -> high(source.controller, true));
        h.runAfterDelay(40, () -> {
          h.assertTrue(failure.isEmpty() && source.controller.phase() == RideControllerEntity.State.HOLD && !source.controller.transferring()
              && receiver.reservation().isEmpty() && source.controller.reservation().size() == source.base.parents.size() && Math.abs(work - guardWork) < 1e-8,
              "Unsafe intermediate route released a lease or performed launch work: " + source.controller.status() + " observer=" + failure);
          System.out.println("LS_ROUTE_GUARD mode=" + guard + " retained=" + source.controller.reservation().size() + " added_work=" + (work - guardWork));
          cleanup(); h.succeed();
        });
        return;
      }
      if (stage <= 4) {
        var sender = stage % 2 == 1 ? source.controller : receiver;
        if (sender.phase() != RideControllerEntity.State.READY_EXIT) return;
        pending = true; high(sender, false);
        h.runAfterDelay(4, () -> { h.assertTrue(sender.phase() == RideControllerEntity.State.READY_EXIT, "Fresh circuit request lost readiness"); high(sender, true); departures++; stage++; pending = false; });
      } else if (stage == 5 && source.controller.phase() == RideControllerEntity.State.READY_EXIT) {
        h.assertTrue(receiver.reservation().isEmpty() && !source.controller.reservation().isEmpty() && curveSteps > 100 && retainedSteps > 100, "Two laps lost complete route ownership");
        boarding = true; stage = 6;
        h.assertTrue(source.controller.boardingReady() && source.controller.signal(Direction.DOWN) == 15,
            "Automatic station capture did not establish whole-train boarding readiness");
      } else if (stage == 6) {
        if (source.fourthDimension && source.vehicles.stream().anyMatch(v -> !(boolean) invoke("dev.fourthdimension.vehicle.Boarding", "ready", new Class<?>[] {net.minecraft.server.level.ServerLevel.class, type("dev.fourthdimension.runtime.FoundationData$Vehicle")}, source.base.level, v))) return;
        h.assertTrue(!MotionProviders.requestCapture(source.base.level, source.base.parents.getFirst().getUniqueId(), Set.of()), "An empty lease granted capture");
        if (source.linked) h.assertTrue(!MotionProviders.requestCapture(source.base.level, source.base.parents.getFirst().getUniqueId(), Set.of(source.base.parents.getFirst().getUniqueId())), "An incomplete coupled lease granted capture");
        if (source.fourthDimension) {
          invoke("dev.fourthdimension.vehicle.VehicleOperations", "restraints", new Class<?>[] {net.minecraft.server.level.ServerLevel.class, type("dev.fourthdimension.runtime.FoundationData$Vehicle"), boolean.class}, source.base.level, source.vehicles.getFirst(), false);
          high(source.controller, false); h.runAfterDelay(4, () -> high(source.controller, true));
        }
        for (var rider : List.copyOf(source.base.riders)) { rider.stopRiding(); h.assertTrue(!rider.isPassenger(), "Captured station denied ordinary passenger exit"); rider.discard(); }
        source.base.riders.clear(); source.riderBodies.clear(); pending = true;
        h.runAfterDelay(20, () -> {
          emptyMass = source.base.parents.stream().mapToDouble(source.base::mass).sum();
          h.assertTrue(Math.abs(loadedMass - emptyMass - .8 * seatedCount) < 1e-6, "Station exit did not remove each real rider's native mass");
          if (source.fourthDimension) source.boardFourthDimension(); else for (var cart : source.base.parents) for (int seat = 0; seat < 4; seat++) source.base.board(cart, seat);
          h.runAfterDelay(40, () -> {
            if (source.fourthDimension) invoke("dev.fourthdimension.vehicle.VehicleOperations", "restraints", new Class<?>[] {net.minecraft.server.level.ServerLevel.class, type("dev.fourthdimension.runtime.FoundationData$Vehicle"), boolean.class}, source.base.level, source.vehicles.getFirst(), true);
            h.assertTrue(Math.abs(source.base.parents.stream().mapToDouble(source.base::mass).sum() - loadedMass) < 1e-6, "Station reboarding did not restore native rider mass");
            reboarded = true; boarding = false; stage = 7; pending = false;
          });
        });
      } else if (stage == 7 && source.controller.phase() == RideControllerEntity.State.READY_EXIT) {
        pending = true; high(source.controller, false);
        h.runAfterDelay(4, () -> { high(source.controller, true); departures++; stage = 8; pending = false; });
      } else if (stage == 8 && receiver.phase() == RideControllerEntity.State.READY_EXIT) {
        h.assertTrue(movingCaptureDenied, "Circuit did not test moving-train capture refusal");
        if (source.fourthDimension) h.assertTrue(capturingSteps > 0 && boardingDeniedSteps > 0, "Circuit did not observe automatic capture and open-restraint departure denial");
        h.assertTrue(departures == 6 && reboarded && boardedSteps > 100 && source.controller.reservation().isEmpty() && receiver.reservation().size() == source.base.parents.size(), "Freshly boarded train did not complete its next reserved departure");
        h.assertTrue(maxJoint < .01 && maxCoupler < .05, "Circuit lost native articulated or coupler joints");
        if (source.mode.startsWith("free_spin")) h.assertTrue(rotorResponse > .02, "Circuit suppressed independent carrier rotation");
        System.out.println("LS_CIRCUIT mode=" + source.mode + " departures=" + departures + " cars=" + source.base.parents.size() + " riders=" + source.base.riders.size()
            + " curve_steps=" + curveSteps + " retained_steps=" + retainedSteps + " boarded_steps=" + boardedSteps + " work=" + work + " peak_speed=" + peakSpeed
            + " joint_error=" + maxJoint + " coupler_error=" + maxCoupler + " rotor_response=" + rotorResponse + " loaded_mass=" + loadedMass + " empty_mass=" + emptyMass + " capturing_steps=" + capturingSteps + " boarding_denied_steps=" + boardingDeniedSteps + " moving_capture_denied=" + movingCaptureDenied);
        cleanup(); h.succeed();
      }
    }
    void cleanup() {
      if (closed) return; closed = true; SCENES.remove(this); source.cleanup();
      var level = source.base.level;
      if (foreign != null && !foreign.isRemoved()) SubLevelContainer.getContainer(level).removeSubLevel(foreign, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
      for (var ends : List.of(List.of(bFrom, bTo), List.of(bTo, source.base.from))) if (level.getBlockEntity(ends.getFirst()) instanceof CoasterAnchorpointBlockEntity a) a.removeAnchorPeerCurve(level, ends.getLast());
      modules.forEach(p -> level.removeBlock(p, false)); motors.forEach(p -> level.removeBlock(p, false));
      for (var p : List.of(bPos.above(), bPos, bFrom, bTo)) level.removeBlock(p, false);
    }
  }
  private static void before(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.closed || s.source.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        var h = s.source.base.helper; var level = s.source.base.level;
        s.aTrack = s.source.controller.resolve(level); s.bTrack = s.receiver.resolve(level);
        var sender = s.source.controller.transferring() ? s.source.controller : s.receiver.transferring() ? s.receiver : null;
        var target = sender == s.source.controller ? s.receiver : s.source.controller;
        var route = sender == null ? null : sender.transferRoute(level, target);
        if (sender != null) { h.assertTrue(sender.reservation().equals(target.reservation()) && sender.reservation().size() == s.source.base.parents.size(), "Circuit cleared a whole-consist lease early"); s.retainedSteps++; }
        if (s.source.controller.phase() == RideControllerEntity.State.CAPTURING) s.capturingSteps++;
        if (s.source.fourthDimension && s.boarding && s.source.controller.phase() == RideControllerEntity.State.BOARDING) {
          h.assertTrue(s.source.controller.signal(Direction.NORTH) == 0 && s.source.controller.signal(Direction.DOWN) == 15
              && !s.source.controller.transferring() && s.source.controller.reservation().size() == s.source.base.parents.size(), "Open restraints granted departure or lost boarding ownership");
          s.boardingDeniedSteps++;
        }
        double stepWork = 0; var contacted = new HashSet<BlockPos>();
        var all = new ArrayList<BlockPos>(s.source.equipment); all.addAll(s.modules);
        for (var cart : s.source.base.parents) {
          var contact = EngagedContacts.current(level, cart).orElseThrow();
          var track = s.aTrack.matches(contact.hit()) ? s.aTrack : s.bTrack.matches(contact.hit()) ? s.bTrack : route == null ? null : route.track(contact.hit());
          h.assertTrue(track != null, "Circuit lost accepted section/route contact");
          if (track != s.aTrack && track != s.bTrack) s.curveSteps++;
          var axis = track.forward(level, contact.hit()); var direction = new Vector3d(axis.x, axis.y, axis.z);
          var dynamics = NativeForces.pointDynamics(cart, event.getPhysicsSystem().getPipeline(), VehicleInterfaces.tirePoint(cart), direction);
          s.peakSpeed = Math.max(s.peakSpeed, Math.abs(dynamics.speed()));
          if (s.guard.isEmpty() && !s.movingCaptureDenied && s.stage == 1 && Math.abs(dynamics.speed()) > .1) {
            h.assertTrue(!MotionProviders.requestCapture(level, cart.getUniqueId(), s.source.controller.reservation())
                && MotionProviders.captureState(level, cart.getUniqueId()) == dev.coasterslaunchsystems.api.CaptureState.RELEASED, "Moving train accepted capture or changed vehicle ownership");
            s.movingCaptureDenied = true;
          }
          var group = cart.getQueuedForceGroups() == null ? null : cart.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
          double force = group == null ? 0 : new Vector3d(group.getForceTotal().getLocalForce()).rotate(cart.logicalPose().orientation()).dot(direction) / event.getTimeStep();
          var motion = MotionProviders.resolve(level, cart.getUniqueId()).orElseThrow();
          h.assertTrue(Math.abs(force) <= Math.min(motion.maximumForce(), NativeForces.mass(level, motion) * motion.maximumAcceleration()) + 1e-8, "Circuit exceeded the provider force limit");
          var old = s.previous.computeIfAbsent(cart.getUniqueId(), ignored -> new HashMap<>());
          for (var p : all) if (level.getBlockEntity(p) instanceof EquipmentCabinetEntity cabinet && cabinet.mechanism() == Mechanism.TIRE) {
            var bound = cabinet.resolve(level);
            if (bound != null && bound.matches(contact.hit())) {
              double distance = bound.distance(contact.hit());
              if (cabinet.interval().intersects(old.getOrDefault(p, distance), distance, .6)) contacted.add(p);
              old.put(p, distance);
            } else old.remove(p);
          }
          double work = DriveMath.incrementalWork(dynamics.effectiveMass(), dynamics.speed(), force, event.getTimeStep());
          s.work += Math.max(0, work); stepWork += Math.max(0, work);
          if (s.boarding || s.source.controller.phase() == RideControllerEntity.State.CAPTURING) h.assertTrue(Math.abs(force) < 1e-8 && Math.abs(dynamics.speed()) < .1, "Station capture/boarding continued propulsion");
        }
        double budget = contacted.stream().map(p -> (EquipmentCabinetEntity) level.getBlockEntity(p)).mapToDouble(c -> 8 * Math.abs(c.getSpeed()) * 2 * Math.PI / 60 * event.getTimeStep()).sum();
        h.assertTrue(stepWork <= budget + 1e-8, "Circuit duplicated contacted shaft work");
        h.assertTrue(s.source.base.riders.stream().allMatch(net.minecraft.world.entity.animal.Pig::isPassenger), "Circuit lost a real seated rider");
        if (s.reboarded) s.boardedSteps++;
        if (level.getGameTime() % 200 == 0 && s.lastReport != level.getGameTime()) {
          s.lastReport = level.getGameTime();
          System.out.println("LS_CIRCUIT_PROGRESS mode=" + s.source.mode + " stage=" + s.stage + " source=" + s.source.controller.status()
              + " receiver=" + s.receiver.status() + " curve_steps=" + s.curveSteps + " peak_speed=" + s.peakSpeed + " position=" + s.source.base.parents.getFirst().logicalPose().position());
        }
      } catch (RuntimeException e) { if (s.failure.isEmpty()) s.failure = e.toString(); }
    }
  }
  private static void after(ForgeSablePostPhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) if (!s.closed && s.source.base.level == event.getPhysicsSystem().getLevel()) try {
      int index = 0;
      for (int car = 0; car < s.source.vehicles.size(); car++) for (var carrier : (List<?>) read(s.source.vehicles.get(car), "carriers")) {
        s.maxJoint = Math.max(s.maxJoint, separation(s.source.base.level, s.source.base.parents.get(car), carrier));
        double angle = ((Number) read(carrier, "angle")).doubleValue();
        if (index < s.source.initialAngles.size()) s.rotorResponse = Math.max(s.rotorResponse, Math.abs(angle - s.source.initialAngles.get(index))); index++;
      }
      if (s.source.linked) s.maxCoupler = Math.max(s.maxCoupler, Math.abs(s.source.base.bearing(s.source.base.parents.getFirst()).distance(s.source.base.bearing(s.source.base.parents.getLast())) - 6));
    } catch (RuntimeException e) { if (s.failure.isEmpty()) s.failure = e.toString(); }
  }
  @GameTestGenerator public static List<TestFunction> circuits() {
    var modes = new ArrayList<>(List.of("single_forward", "linked_forward"));
    if (ModList.get().isLoaded("fourthdimension")) modes.addAll(List.of("arrow_forward", "arrow_linked_forward", "free_spin_forward"));
    var tests = new ArrayList<TestFunction>(); int slot = 0;
    for (var mode : modes) { int index = slot++; tests.add(new TestFunction("launchsystems_circuit", "coasterslaunchsystems_routes.circuit_" + mode, "coasterslaunchsystems_routes:empty", 7000, 0, true, h -> run(new Scene(h, index, mode)))); }
    for (var guard : List.of("occupied_curve", "edited_curve")) { int index = slot++; tests.add(new TestFunction("launchsystems_circuit", "coasterslaunchsystems_routes.guard_" + guard, "coasterslaunchsystems_routes:empty", 1500, 0, true, h -> run(new Scene(h, index, "linked_forward", guard)))); }
    return tests;
  }
  private static void run(Scene s) {
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CircuitGameTests::before); NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CircuitGameTests::after); registered = true; }
    s.source.at(40, () -> { if (s.source.fourthDimension) s.source.boardFourthDimension(); else for (var c : s.source.base.parents) for (int seat = 0; seat < 4; seat++) s.source.base.board(c, seat); });
    s.source.at(55, () -> {
      if (s.source.linked) s.source.base.helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(s.source.base.level, s.source.base.parents.getFirst(), s.source.base.parents.getLast(), 6), "Circuit did not couple its occupied pair");
      for (var v : s.source.vehicles) for (var carrier : (List<?>) read(v, "carriers")) s.source.initialAngles.add(((Number) read(carrier, "angle")).doubleValue());
      s.loadedMass = s.source.base.parents.stream().mapToDouble(s.source.base::mass).sum(); s.seatedCount = s.source.base.riders.size();
      s.install(); SCENES.add(s);
    });
    s.source.at(80, () -> { s.source.base.helper.assertTrue(s.source.controller.phase() == RideControllerEntity.State.READY, "Circuit initial station is not ready"); s.high(s.source.controller, true); });
    s.source.base.helper.onEachTick(() -> { try { s.tick(); } catch (RuntimeException e) { s.cleanup(); throw e; } });
  }
}
