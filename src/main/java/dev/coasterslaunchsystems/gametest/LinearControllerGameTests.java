package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.*;
import dev.coasterslaunchsystems.math.*;
import dev.coasterslaunchsystems.runtime.*;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Local block launches certify the whole occupied consist before releasing any station pads. */
@GameTestHolder("coasterslaunchsystems_linear")
public final class LinearControllerGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static boolean swing(String mode) { return mode.equals("swing_forward") || mode.equals("swing_power_loss") || mode.contains("_swing_"); }
  private static boolean swingPowerLoss(String mode) { return mode.equals("swing_power_loss") || mode.endsWith("_swing_power_loss"); }
  private static final class Scene {
    final RideControllerGameTests.Scene ride;
    final Mechanism mechanism;
    final Map<BlockPos, Double> energy = new HashMap<>(), heat = new HashMap<>(), pendingSourceCharge = new HashMap<>();
    double work, spent, thermal, peakSpeed, maxError, maxJoint, maxCoupler, rotorResponse;
    int steps;
    int interruptedSteps;
    boolean interrupted, restored;
    String failure = "";
    EquipmentCabinetEntity skid;
    double skidPeakHeat;
    Scene(GameTestHelper helper, int slot, String mode, Mechanism mechanism) {
      ride = new RideControllerGameTests.Scene(helper, slot, mode, 192); this.mechanism = mechanism; ride.driveMechanism = mechanism;
    }
    List<EquipmentCabinetEntity> drives() { return ride.equipment.stream().map(p -> (EquipmentCabinetEntity) ride.base.level.getBlockEntity(p)).filter(c -> c.mechanism().linear()).toList(); }
    void snapshot() { for (var c : drives()) { energy.put(c.getBlockPos(), c.energy()); heat.put(c.getBlockPos(), c.heat()); } }
    void cleanup() { SCENES.remove(this); ride.cleanup(); }
  }
  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      var r = s.ride; if (r.closed || r.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        if (s.skid != null) s.skidPeakHeat = Math.max(s.skidPeakHeat, s.skid.heat());
        // Interrupt the return leg only after the first end-bank reversal has become live. This
        // proves a swing program cannot silently replay after authority is restored.
        double work = 0, spent = 0, heat = 0; double dt = event.getTimeStep();
        if (swingPowerLoss(r.mode) && !s.interrupted && r.controller.swingPassIndex() == 1
            && r.controller.phase() == RideControllerEntity.State.RUNNING && s.steps > 150) {
          // Cabinet charging for this physics step is already queued at the prior shaft speed.
          // Preserve that one measured charge while changing the actual source for the next step.
          for (var c : s.drives()) s.pendingSourceCharge.put(c.getBlockPos(),
              Math.min(LinearMotor.CAPACITY - s.energy.get(c.getBlockPos()), LinearMotor.chargingWork(c.getSpeed(), dt)));
          for (var p : r.motors)
            ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) r.base.level.getBlockEntity(p)).generatedSpeed.setValue(0);
          s.interrupted = true;
        }
        for (var body : r.base.parents) {
          var motion = MotionProviders.resolve(r.base.level, body.getUniqueId()).orElseThrow();
          var contact = EngagedContacts.current(r.base.level, body).orElseThrow();
          r.base.helper.assertTrue(r.track.matches(contact.hit()), "Linear section lost native support");
          var axis = r.track.forward(r.base.level, contact.hit()); var direction = new Vector3d(axis.x, axis.y, axis.z);
          var group = body.getQueuedForceGroups() == null ? null : body.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
          var impulse = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalForce());
          var torque = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalTorque());
          double force = new Vector3d(impulse).rotate(body.logicalPose().orientation()).dot(direction) / dt;
          if (Math.abs(force) > 1e-8 && (s.skid == null || s.skidPeakHeat == 0)) {
            r.base.helper.assertTrue(r.controller.phase() == RideControllerEntity.State.RUNNING && r.controller.reservation().contains(body.getUniqueId()), "Linear impulse bypassed a local lease or ready request");
            r.base.helper.assertTrue(Math.abs(force) <= Math.min(motion.maximumForce(), NativeForces.mass(r.base.level, motion) * motion.maximumAcceleration()) + 1e-8,
                "Adjacent linear pads duplicated a chassis rating"); s.steps++;
          }
          var velocity = body.logicalPose().transformNormalInverse(event.getPhysicsSystem().getPipeline().getLinearVelocity(body, new Vector3d()));
          var angular = body.logicalPose().transformNormalInverse(event.getPhysicsSystem().getPipeline().getAngularVelocity(body, new Vector3d()));
          work += velocity.dot(impulse) + angular.dot(torque) + .5 * body.getMassTracker().getInverseMass() * impulse.lengthSquared()
              + .5 * torque.dot(body.getMassTracker().getInverseInertiaTensor().transform(new Vector3d(torque)));
          var point = VehicleInterfaces.linearPoint(body, s.mechanism);
          if (point != null) s.peakSpeed = Math.max(s.peakSpeed,
              Math.abs(NativeForces.pointDynamics(body, event.getPhysicsSystem().getPipeline(), point, direction).speed()));
        }
        for (var c : s.drives()) {
          boolean active = EquipmentRuntime.available(r.base.level, c);
          Double queuedCharge = s.pendingSourceCharge.remove(c.getBlockPos());
          double charge = queuedCharge != null ? queuedCharge : active ? Math.min(LinearMotor.CAPACITY - s.energy.get(c.getBlockPos()), LinearMotor.chargingWork(c.getSpeed(), dt)) : 0;
          spent += s.energy.get(c.getBlockPos()) + charge - c.energy(); heat += c.heat() - (active ? Math.max(0, s.heat.get(c.getBlockPos()) - 8 * dt) : s.heat.get(c.getBlockPos()));
        }
        s.maxError = Math.max(s.maxError, Math.abs(spent - heat - work));
        // The new passive trim fixture shares PROPULSION's native force group with the field.
        // Once the shoe dissipates work, that group's impulse is no longer linear-only accounting.
        if (s.skid == null || s.skidPeakHeat == 0) r.base.helper.assertTrue(s.maxError < 1e-7, "Coordinated linear bank duplicated native work/resources: " + s.maxError);
        s.work += work; s.spent += spent; s.thermal += heat; s.snapshot();
        r.base.helper.assertTrue(r.base.riders.stream().allMatch(net.minecraft.world.entity.animal.Pig::isPassenger), "Linear launch lost a passenger");
        if (swingPowerLoss(r.mode) && s.interrupted && !s.restored && r.controller.phase() == RideControllerEntity.State.FAULT) {
          s.interruptedSteps = s.steps;
          for (var p : r.motors)
            ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) r.base.level.getBlockEntity(p)).generatedSpeed.setValue(64);
          s.restored = true;
        }
      } catch (RuntimeException failure) { if (s.failure.isEmpty()) s.failure = failure.toString(); s.snapshot(); }
    }
  }
  private static void after(dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      var r = s.ride; if (r.closed || r.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        int angle = 0;
        for (int car = 0; car < r.vehicles.size(); car++) for (var carrier : (List<?>) read(r.vehicles.get(car), "carriers")) {
          s.maxJoint = Math.max(s.maxJoint, separation(r.base.level, r.base.parents.get(car), carrier));
          if (r.launched && angle < r.initialAngles.size()) s.rotorResponse = Math.max(s.rotorResponse, Math.abs(((Number) read(carrier, "angle")).doubleValue() - r.initialAngles.get(angle)));
          angle++;
        }
        if (r.linked) s.maxCoupler = Math.max(s.maxCoupler, Math.abs(r.base.bearing(r.base.parents.getFirst()).distance(r.base.bearing(r.base.parents.getLast())) - 6));
      } catch (RuntimeException failure) { s.failure = failure.toString(); }
    }
  }
  @GameTestGenerator public static List<TestFunction> sections() {
    var tests = new ArrayList<TestFunction>(); int slot = 0, fourthDimensionSlot = 0, fourthDimensionSwingSlot = 0;
    for (var mechanism : List.of(Mechanism.LSM, Mechanism.LIM)) {
      var modes = new ArrayList<>(List.of("single_forward", "linked_forward", "linked_reverse", "power_loss", "thermal_loss"));
      if (mechanism == Mechanism.LSM) modes.add("mixed_forward");
      modes.addAll(List.of("swing_forward", "swing_power_loss"));
      if (net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) modes.addAll(List.of(
          "arrow_forward", "arrow_linked_reverse", "arrow_plate_loss", "arrow_thermal_loss", "legacy_forward", "free_spin_forward", "revised_forward", "pax_veto"));
      if (net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) modes.addAll(List.of(
          "arrow_swing_forward", "arrow_swing_power_loss",
          "arrow_linked_swing_forward", "arrow_linked_swing_power_loss",
          "legacy_swing_forward", "legacy_swing_power_loss",
          "free_spin_swing_forward", "free_spin_swing_power_loss",
          "revised_swing_forward", "revised_swing_power_loss"));
      if (mechanism == Mechanism.LSM && net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) modes.add("arrow_skid_trim");
      for (String mode : modes) {
        String selection = System.getProperty("coasterslaunchsystems.linearCases", "");
        String caseName = mechanism.name().toLowerCase(Locale.ROOT) + "_section_" + mode;
        if (!selection.isBlank() && !List.of(selection.split(",")).contains(caseName)) continue;
        boolean fourthDimension = mode.startsWith("arrow") || mode.startsWith("legacy") || mode.startsWith("free_spin")
            || mode.startsWith("revised") || mode.startsWith("pax");
        // Optional-provider fixtures have their own compact lane; do not let an added provider
        // case move an ordinary fixture beyond the native near-origin equipment bound.
        boolean familySwing = fourthDimension && swing(mode);
        int fixtureSlot = fourthDimension ? -1 - (familySwing ? fourthDimensionSwingSlot++ : fourthDimensionSlot++) : slot++;
        // Sable's native world coordinates are bounded. Family swings execute in a separate batch
        // and reuse the compact negative lane after the Arrow batch has cleaned up its fixtures.
        String namespace = fourthDimension ? "coasterslaunchsystems_4d_linear"
            : mode.equals("thermal_loss") ? "coasterslaunchsystems_thermal_linear" : "coasterslaunchsystems_linear";
        String batch = familySwing ? "coasterslaunchsystems_4d_swing_linear_sections" : namespace + "_sections";
        int timeout = swing(mode) ? (mode.contains("linked") ? 12000 : 8000) : 1600;
        tests.add(new TestFunction(batch, namespace + "." + mechanism.name().toLowerCase(Locale.ROOT) + "_section_" + mode,
            namespace + ":empty", timeout, 0, true, helper -> run(new Scene(helper, fixtureSlot, mode, mechanism))));
      }
    }
    return tests;
  }
  static TestFunction catalogCase(String mode, int slot, Mechanism mechanism) {
    return new TestFunction("launchsystems_fixed", "coasterslaunchsystems_fixed." + mechanism.name().toLowerCase(java.util.Locale.ROOT) + "_" + mode,
        "coasterslaunchsystems_fixed:empty", 1700, 0, true, helper -> run(new Scene(helper, slot, mode, mechanism)));
  }
  private static String faultState(Scene s) {
    var r = s.ride;
    return r.controller.status() + " steps=" + s.steps + "/" + s.interruptedSteps
        + " interrupted=" + s.interrupted + " restored=" + s.restored
        + " bodies=" + r.base.parents.stream().map(body -> body.getUniqueId()
            + ":speed=" + r.base.speed(body) + ":clamps=" + EquipmentRuntime.activeFinClamps(r.base.level, body.getUniqueId())
            + ":brake=" + MotionProviders.resolve(r.base.level, body.getUniqueId()).map(v -> v.brakingAllowed()).orElse(false)
            + ":propel=" + MotionProviders.resolve(r.base.level, body.getUniqueId()).map(v -> v.propulsionAllowed()).orElse(false)).toList();
  }
  private static void run(Scene s) {
    var r = s.ride;
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LinearControllerGameTests::physics); NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LinearControllerGameTests::after); registered = true; }
    r.at(40, () -> { if (r.fourthDimension) r.boardFourthDimension(); else for (var body : r.base.parents) for (int seat = 0; seat < 4; seat++) r.base.board(body, seat); });
    r.at(55, () -> {
      if (r.linked) r.base.helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(r.base.level, r.base.parents.getFirst(), r.base.parents.getLast(), 6), "Linear native consist did not couple");
      for (var body : r.base.parents) {
        if (r.mode.equals("arrow_skid_trim")) body.getPlot().getEmbeddedLevelAccessor().setBlock(
            new BlockPos(0, -2, -1), EquipmentContent.SKID_SHOE.get().defaultBlockState(), 3);
        for (var mechanism : r.mode.equals("mixed_forward") ? List.of(Mechanism.LSM, Mechanism.LIM) : List.of(s.mechanism)) {
          var plot = body.getPlot().getEmbeddedLevelAccessor(); var plate = LinearMotorGameTests.reactionPosition(mechanism);
          if (r.fourthDimension) {
            var vehicle = r.vehicles.stream().filter(v -> read(v, "chassis").equals(body.getUniqueId())).findFirst().orElseThrow();
            var certificate = dev.coasterslaunchsystems.integration.FourthDimensionCertificates.find(((Enum<?>) read(vehicle, "family")).name(), (String) read(vehicle, "variant"));
            if (certificate != null) plate = certificate.offset(mechanism);
          }
          r.base.helper.assertTrue(plot.getBlockState(plate).isAir(), "Linear plate would overwrite vehicle-family hardware");
          plot.setBlock(plate, (mechanism == Mechanism.LSM ? EquipmentContent.LSM_PLATE : EquipmentContent.LIM_PLATE).get().defaultBlockState(), 3);
        }
      }
      r.base.level.setBlockAndUpdate(r.controllerPosition, EquipmentContent.CONTROLLER.get().defaultBlockState());
      r.controller = (RideControllerEntity) r.base.level.getBlockEntity(r.controllerPosition);
      r.controller.bind(r.base.level, r.reverse ? r.base.to : r.base.from, r.reverse ? r.base.from : r.base.to);
      r.track = DirectedTrack.resolve(r.base.level, r.reverse ? r.base.to : r.base.from, r.reverse ? r.base.from : r.base.to).orElseThrow();
      if (swing(r.mode)) {
        // The full field is available between two independent spring-fin end banks. The first
        // field direction is forward; the controller reverses it only after each native tail clears.
        for (int distance = 14; distance <= 60; distance += 2) r.module(distance, false);
        // Free Spin and Revised need 16-block banks; a coupled Arrow needs overlapping
        // 30-block end banks so both cars are physically clamped before reversal.
        int bankStart = r.linked ? 30 : 18, bankEnd = r.linked ? 34 : 48;
        for (int distance = 2; distance <= bankStart; distance += 2) r.module(distance, true);
        for (int distance = bankEnd; distance <= 62; distance += 2) r.module(distance, true);
        r.controller.setSwingProgram(true);
      } else {
        for (int distance = 8; distance <= 16; distance += 2) {
          if (r.mode.equals("mixed_forward")) r.driveMechanism = distance % 4 == 0 ? Mechanism.LSM : Mechanism.LIM;
          r.module(distance, false); r.module(distance, true);
        }
        for (int distance = r.mode.equals("arrow_skid_trim") ? 29 : 21; distance <= 61; distance += 2) r.module(distance, true);
      }
      if (r.mode.equals("arrow_skid_trim")) {
        var position = r.base.from.offset(1, -2, 20);
        r.base.level.setBlockAndUpdate(position, EquipmentContent.SKID.get().defaultBlockState()
            .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X));
        s.skid = (EquipmentCabinetEntity) r.base.level.getBlockEntity(position);
        s.skid.bind(r.base.level, r.base.from, r.base.to); s.skid.setArmed(true);
        r.equipment.add(position);
      }
      r.controller.setArmed(true); s.snapshot(); SCENES.add(s);
    });
    r.at(80, () -> {
      if (swing(r.mode)) return;
      if (r.mode.equals("pax_veto")) {
        r.base.helper.assertTrue(r.controller.phase() == RideControllerEntity.State.FAULT && s.steps == 0 && !r.controller.fault().isEmpty(),
            "PAX winch ownership allowed a linear reservation or field impulse: " + r.controller.status());
        r.cleanup(); r.base.helper.succeed(); return;
      }
      r.base.helper.assertTrue(r.controller.phase() == RideControllerEntity.State.HOLD && s.steps == 0, "Partial linear charge released a loaded train: " + r.controller.status()); r.high(true);
    });
    int readyTick = 500;
    r.at(readyTick, () -> {
      r.base.helper.assertTrue(s.failure.isEmpty() && r.controller.phase() == RideControllerEntity.State.READY && s.steps == 0, "Charged linear section did not wait for a fresh request: " + r.controller.status() + " " + s.failure);
      for (var vehicle : r.vehicles) for (var carrier : (List<?>) read(vehicle, "carriers")) r.initialAngles.add(((Number) read(carrier, "angle")).doubleValue());
      r.high(false);
    });
    r.at(readyTick + 10, () -> { r.launched = true; r.high(true); });
    r.at(550, () -> {
      if (!r.mode.equals("power_loss") && !r.mode.equals("arrow_plate_loss") && !r.mode.endsWith("thermal_loss")) return;
      r.base.helper.assertTrue(s.steps > 10, "Interrupted linear fixture never launched");
      if (r.mode.equals("power_loss"))
        for (var p : r.motors) ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) r.base.level.getBlockEntity(p)).generatedSpeed.setValue(0);
      else if (r.mode.equals("arrow_plate_loss")) for (var body : r.base.parents) body.getPlot().getEmbeddedLevelAccessor().setBlock(LinearMotorGameTests.reactionPosition(s.mechanism),
          net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
      else { s.drives().forEach(cabinet -> cabinet.absorbHeat(8192)); s.snapshot(); }
    });
    r.at(610, () -> {
      if (!r.mode.equals("power_loss") && !r.mode.equals("arrow_plate_loss") && !r.mode.endsWith("thermal_loss")) return;
      r.base.helper.assertTrue(r.controller.phase() == RideControllerEntity.State.FAULT && r.controller.reservation().size() == r.base.parents.size()
          && r.base.parents.stream().allMatch(b -> Math.abs(r.base.speed(b)) < .03 && EquipmentRuntime.activeFinClamps(r.base.level, b.getUniqueId()) > 0),
          "Lost linear authority did not retain the held train: " + faultState(s));
      s.interruptedSteps = s.steps;
      if (r.mode.equals("power_loss"))
        for (var p : r.motors) ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) r.base.level.getBlockEntity(p)).generatedSpeed.setValue(64);
    });
    r.at(700, () -> {
      if (s.skid == null) return;
      r.base.helper.assertTrue(s.skidPeakHeat > 1 && s.skid.fault().isEmpty()
          && r.controller.reservation().contains(r.base.parents.getFirst().getUniqueId()),
          "Current-authority skid did not dissipate work under the retained linear lease: peak=" + s.skidPeakHeat
              + " heat=" + s.skid.heat() + " cabinet=" + s.skid.status() + " " + faultState(s));
      System.out.println("LS_CURRENT_SKID peak_heat=" + s.skidPeakHeat + " heat=" + s.skid.heat() + " phase=" + r.controller.phase()
          + " speed=" + r.base.speed(r.base.parents.getFirst()));
      s.skid.setArmed(false);
    });
    r.at(swing(r.mode) ? (r.linked ? 11000 : 7600) : 1250, () -> {
      try {
        if (r.mode.equals("power_loss") || r.mode.equals("arrow_plate_loss") || r.mode.endsWith("thermal_loss") || swingPowerLoss(r.mode)) {
          r.base.helper.assertTrue(s.failure.isEmpty() && r.controller.phase() == RideControllerEntity.State.FAULT && s.steps == s.interruptedSteps
              && r.controller.reservation().size() == r.base.parents.size() && s.drives().stream().allMatch(c -> c.energy() > 0)
              && r.base.parents.stream().allMatch(b -> Math.abs(r.base.speed(b)) < .03 && EquipmentRuntime.activeFinClamps(r.base.level, b.getUniqueId()) > 0)
              && (!swingPowerLoss(r.mode) || s.interrupted && s.restored), "Interrupted linear field replayed a launch: " + s.failure + " " + faultState(s));
          System.out.println("LS_LINEAR_INTERRUPTION mechanism=" + s.mechanism + " mode=" + r.mode + " phase=" + r.controller.phase() + " lease=" + r.controller.reservation().size()
              + " steps=" + s.steps + " spent=" + s.spent + " work=" + s.work + " accounting_error=" + s.maxError);
          r.base.helper.succeed(); return;
        }
        if (s.skid != null) {
          r.base.helper.assertTrue(s.skidPeakHeat > 1 && s.failure.isEmpty() && r.controller.fault().isEmpty()
              && r.controller.reservation().contains(r.base.parents.getFirst().getUniqueId()),
              "Separate skid trim lost work or retained controller lease: " + s.failure + " " + faultState(s));
          r.base.helper.succeed(); return;
        }
        r.base.helper.assertTrue(s.failure.isEmpty() && r.controller.fault().isEmpty() && r.controller.phase() == RideControllerEntity.State.HOLD, "Linear section did not stop in its reserved fin bank: " + r.controller.status() + " " + s.failure);
        if (swing(r.mode)) r.base.helper.assertTrue(r.controller.swingPassIndex() == 2,
            "Swing launch did not complete its forward/backward/forward program: " + r.controller.status());
        r.base.helper.assertTrue(s.steps > 20 && s.work > 1 && s.spent > s.work && s.thermal > 0 && s.peakSpeed > .5 && s.peakSpeed <= 3.1,
            "Loaded linear launch lacked bounded native work: steps=" + s.steps + " speed=" + s.peakSpeed);
        r.base.helper.assertTrue(r.controller.reservation().equals(r.base.parents.stream().map(b -> b.getUniqueId()).collect(java.util.stream.Collectors.toSet()))
            && r.base.parents.stream().allMatch(b -> Math.abs(r.base.speed(b)) < .03 && EquipmentRuntime.activeFinClamps(r.base.level, b.getUniqueId()) > 0), "Linear stop lost whole-train ownership/hold");
        r.base.helper.assertTrue(s.maxCoupler < .05 && s.maxJoint < .05, "Linear launch destabilized native coupler/carrier joints");
        if (r.mode.startsWith("free_spin")) r.base.helper.assertTrue(s.rotorResponse > .02, "Linear launch suppressed native free-spin response");
        System.out.println("LS_LINEAR_SECTION mechanism=" + s.mechanism + " mode=" + r.mode + " cars=" + r.base.parents.size() + " riders=" + r.base.riders.size()
            + " mass=" + r.base.parents.stream().mapToDouble(r.base::mass).sum() + " work=" + s.work + " spent=" + s.spent + " heat=" + s.thermal + " peak_speed=" + s.peakSpeed
            + " steps=" + s.steps + " accounting_error=" + s.maxError + " joint_error=" + s.maxJoint + " coupler_error=" + s.maxCoupler + " rotor_response=" + s.rotorResponse);
      } finally { s.cleanup(); }
      r.base.helper.succeed();
    });
  }
}
