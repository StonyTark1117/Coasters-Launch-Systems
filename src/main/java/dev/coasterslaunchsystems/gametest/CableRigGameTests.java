package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.*;
import dev.coasterslaunchsystems.math.*;
import dev.coasterslaunchsystems.runtime.*;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** The train receives no fixture propulsion impulse: a real constrained carriage pulls its socket. */
@GameTestHolder("coasterslaunchsystems_cable")
public final class CableRigGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static final class Scene {
    final RideControllerGameTests.Scene ride;
    final String mode;
    final List<NativeFinClamp> preparation = new ArrayList<>(), stopping = new ArrayList<>();
    final BlockPos charger, motor;
    NativeCableRig rig;
    ServerSubLevel lead;
    double charged, drawn, work, heat, returnWork, shaftWork, peakSpeed, peakCatchForce, peakTrainTraction, beforeMomentum, maxSocketError, maxGuideError, maxJointError, maxCouplerError;
    double beginEnergy, beginOmega, releaseOmega, releaseDistance, maxAccountingError, homeDistance, interruptionDraw, interruptionEnergy;
    int pullSteps, returnSteps, releases;
    boolean active, released, stopped, closed, rejected, engageRequested, pullRequested, sampleTraction;
    String failure = "";
    Scene(GameTestHelper helper, int slot, String mode) {
      this.mode = mode; ride = new RideControllerGameTests.Scene(helper, slot, mode, 224);
      charger = ride.base.from.offset(5, -2, 8); motor = charger.west();
      ride.base.level.setBlockAndUpdate(charger, EquipmentContent.FLYWHEEL.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      ride.base.level.setBlockAndUpdate(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, Direction.EAST));
      ((CreativeMotorBlockEntity) ride.base.level.getBlockEntity(motor)).generatedSpeed.setValue(64);
    }
    EquipmentCabinetEntity cabinet() { return (EquipmentCabinetEntity) ride.base.level.getBlockEntity(charger); }
    FlywheelStore store() { return cabinet().flywheel(); }
    void at(int tick, Runnable action) { ride.base.helper.runAtTickTime(tick + (ride.fourthDimension ? 120 : 0), () -> { try { action.run(); } catch (RuntimeException e) { System.err.println("LS_CABLE_FAILURE mode=" + mode + " tick=" + tick); e.printStackTrace(); cleanup(); throw e; } }); }
    void cleanup() {
      if (closed) return; closed = true; SCENES.remove(this);
      preparation.forEach(NativeFinClamp::close); stopping.forEach(NativeFinClamp::close);
      if (rig != null) rig.close();
      ride.base.level.removeBlock(charger, false); ride.base.level.removeBlock(motor, false); ride.cleanup();
    }
  }
  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      var r = s.ride; if (s.closed || s.rejected || r.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        if (s.rig == null) {
          if (r.base.parents.stream().anyMatch(b -> EngagedContacts.current(r.base.level, b).isEmpty())) continue;
          s.lead = r.base.parents.stream().max(Comparator.comparingDouble(b -> r.track.distance(EngagedContacts.current(r.base.level, b).orElseThrow().hit()))).orElseThrow();
          s.homeDistance = r.track.distance(EngagedContacts.current(r.base.level, s.lead).orElseThrow().hit());
          s.rig = new NativeCableRig(r.base.level, r.track, s.homeDistance + (s.mode.equals("misaligned") ? 1 : 0), 12, 20);
          continue;
        }
        if (s.engageRequested) {
          s.engageRequested = false;
          if (s.mode.equals("misaligned") || s.mode.equals("free_spin_reject")) {
            boolean rejected = false; try { s.rig.engage(s.lead.getUniqueId()); } catch (IllegalStateException expected) { rejected = true; }
            r.base.helper.assertTrue(rejected && s.rig.phase() == NativeCableRig.Phase.HOME && s.rig.consist().isEmpty(), "Uncertified/misaligned catch was engaged");
            System.out.println("LS_CABLE_REJECT mode=" + s.mode + " phase=" + s.rig.phase()); s.rejected = true; continue;
          }
          if (s.mode.equals("depleted")) s.cabinet().consumeFlywheel(s.store().energy());
          s.cabinet().engageCable(s.rig, s.lead.getUniqueId());
          r.base.helper.assertTrue(s.rig.consist().equals(r.base.parents.stream().map(ServerSubLevel::getUniqueId).collect(java.util.stream.Collectors.toSet())), "Catch omitted a native consist member");
          s.beginEnergy = s.store().energy(); s.beginOmega = s.store().omega();
        }
        if (s.pullRequested) {
          s.pullRequested = false; s.rig.beginPull(); s.preparation.forEach(NativeFinClamp::close); s.preparation.clear(); s.active = true;
        }
        double dt = event.getTimeStep();
        double oldEnergy = s.store().energy(); var phase = s.rig.phase();
        NativeCableRig.Step step = new NativeCableRig.Step(0, 0, 0, 0, false);
        double acceptedCharge = 0, shaftBudget = 0;
        if (s.active) {
          s.peakCatchForce = Math.max(s.peakCatchForce, s.rig.catchForce(dt));
          s.maxSocketError = Math.max(s.maxSocketError, s.rig.socketError()); s.maxGuideError = Math.max(s.maxGuideError, s.rig.guideError());
          var result = s.cabinet().stepCable(s.rig, dt);
          step = result.motion(); acceptedCharge = result.charged(); shaftBudget = result.shaftBudget();
          if (phase == NativeCableRig.Phase.PULLING) s.pullSteps++;
          if (phase == NativeCableRig.Phase.RETURNING) s.returnSteps++;
          if (s.rig.phase() == NativeCableRig.Phase.HOME) s.active = false;
        }
        s.sampleTraction = phase == NativeCableRig.Phase.PULLING && s.rig.phase() == NativeCableRig.Phase.PULLING && Math.abs(step.force()) > 0;
        if (s.sampleTraction) s.beforeMomentum = trainMomentum(s);
        var carriage = s.rig.carriage();
        var group = carriage.getQueuedForceGroups() == null ? null : carriage.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
        var impulse = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalForce());
        var torque = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalTorque());
        var velocity = carriage.logicalPose().transformNormalInverse(event.getPhysicsSystem().getPipeline().getLinearVelocity(carriage, new Vector3d()));
        var angular = carriage.logicalPose().transformNormalInverse(event.getPhysicsSystem().getPipeline().getAngularVelocity(carriage, new Vector3d()));
        double actualWork = velocity.dot(impulse) + angular.dot(torque) + .5 * carriage.getMassTracker().getInverseMass() * impulse.lengthSquared()
            + .5 * torque.dot(carriage.getMassTracker().getInverseInertiaTensor().transform(new Vector3d(torque)));
        double error = Math.abs(actualWork - step.work()); s.maxAccountingError = Math.max(s.maxAccountingError, error);
        r.base.helper.assertTrue(error < 1e-8 && Math.abs(oldEnergy + acceptedCharge - s.store().energy() - step.storeDraw()) < 1e-8, "Native carriage impulse did not reconcile with cabinet flywheel work");
        s.drawn += step.storeDraw(); s.heat += step.heat();
        if (phase == NativeCableRig.Phase.PULLING) s.work += step.work();
        if (step.returning()) { s.returnWork += Math.max(0, step.work()); s.shaftWork += shaftBudget; }
        s.charged += acceptedCharge;
        if (!s.released && s.rig.phase() == NativeCableRig.Phase.RELEASING) { s.released = true; s.releases++; s.releaseDistance = s.rig.travel(); s.releaseOmega = s.store().omega(); }
        for (var body : r.base.parents) {
          var contact = EngagedContacts.current(r.base.level, body).orElseThrow();
          r.base.helper.assertTrue(r.track.matches(contact.hit()), "Cable train lost native rail contact");
          var forces = body.getQueuedForceGroups(); var trainGroup = forces == null ? null : forces.get(ForceGroups.PROPULSION.get());
          r.base.helper.assertTrue(trainGroup == null || trainGroup.getForceTotal().getLocalForce().lengthSquared() < 1e-16,
              "A fixture directly propelled the train instead of pulling its catch");
          s.peakSpeed = Math.max(s.peakSpeed, Math.abs(r.base.speed(body)));
        }
        if (s.released && !s.stopped && r.track.distance(EngagedContacts.current(r.base.level, s.lead).orElseThrow().hit()) > s.homeDistance + 16) {
          s.stopped = true;
          for (var body : r.base.parents) stoppingClamp(s, body);
        }
        r.base.helper.assertTrue(r.base.riders.stream().allMatch(net.minecraft.world.entity.animal.Pig::isPassenger), "Cable pull lost a rider");
      } catch (RuntimeException failure) { if (s.failure.isEmpty()) { s.failure = failure.toString(); failure.printStackTrace(); } }
    }
  }
  private static void stoppingClamp(Scene s, ServerSubLevel body) {
    var pipeline = SubLevelContainer.getContainer(s.ride.base.level).physicsSystem().getPipeline();
    s.stopping.add(new NativeFinClamp(pipeline, body, VehicleInterfaces.brakePoint(body), new Vector3d(0, 0, s.ride.reverse ? -1 : 1), 128));
  }
  private static double trainMomentum(Scene s) {
    var container = SubLevelContainer.getContainer(s.ride.base.level); var pipeline = container.physicsSystem().getPipeline();
    var bodies = new HashSet<UUID>(); double momentum = 0;
    for (var parent : s.ride.base.parents) for (var id : MotionProviders.resolve(s.ride.base.level, parent.getUniqueId()).orElseThrow().bodies()) {
      s.ride.base.helper.assertTrue(bodies.add(id), "Cable traction counted an articulated body twice");
      var body = (ServerSubLevel) container.getSubLevel(id);
      momentum += body.getMassTracker().getMass() * pipeline.getLinearVelocity(body, new Vector3d()).z * (s.ride.reverse ? -1 : 1);
    }
    return momentum;
  }
  private static void after(dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      var r = s.ride; if (s.closed || r.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        if (s.sampleTraction) s.peakTrainTraction = Math.max(s.peakTrainTraction, Math.abs(trainMomentum(s) - s.beforeMomentum) / event.getTimeStep());
        for (int car = 0; car < r.vehicles.size(); car++) for (var carrier : (List<?>) read(r.vehicles.get(car), "carriers"))
          s.maxJointError = Math.max(s.maxJointError, separation(r.base.level, r.base.parents.get(car), carrier));
        if (r.linked) s.maxCouplerError = Math.max(s.maxCouplerError, Math.abs(r.base.bearing(r.base.parents.getFirst()).distance(r.base.bearing(r.base.parents.getLast())) - 6));
      } catch (RuntimeException failure) { if (s.failure.isEmpty()) s.failure = failure.toString(); }
    }
  }
  @GameTestGenerator public static List<TestFunction> mechanics() {
    var modes = new ArrayList<>(List.of("single_forward", "linked_forward", "linked_reverse", "misaligned", "depleted", "power_loss", "reload_active", "early_reset", "guide_edited", "guide_removed", "mass_changed"));
    if (net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) modes.addAll(List.of("arrow_forward", "arrow_linked_forward", "arrow_linked_reverse", "free_spin_reject"));
    var tests = new ArrayList<TestFunction>(); int slot = 0;
    for (String mode : modes) { int selected = slot++; tests.add(new TestFunction("launchsystems_cable_mechanics", "coasterslaunchsystems_cable." + mode,
        "coasterslaunchsystems_cable:empty", 1600, 0, true, helper -> run(new Scene(helper, selected, mode)))); }
    return tests;
  }
  private static void run(Scene s) {
    var r = s.ride;
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CableRigGameTests::physics); NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CableRigGameTests::after); registered = true; }
    s.at(40, () -> { if (r.fourthDimension) r.boardFourthDimension(); else for (var cart : r.base.parents) for (int seat = 0; seat < 4; seat++) r.base.board(cart, seat); });
    s.at(25, () -> {
      var installedGuide = DirectedTrack.resolve(r.base.level, r.reverse ? r.base.to : r.base.from, r.reverse ? r.base.from : r.base.to).orElseThrow();
      for (int step = 0; step <= 128; step++) {
        var bed = NativeCableRig.guideBlockAt(r.base.level, installedGuide, installedGuide.length() * step / 128);
        r.base.level.setBlockAndUpdate(bed, EquipmentContent.CABLE_GUIDE.get().defaultBlockState());
      }
      for (var cart : r.base.parents) {
        var plot = cart.getPlot().getEmbeddedLevelAccessor(); r.base.helper.assertTrue(plot.getBlockState(BlockPos.ZERO.below(2)).isAir(), "Socket would replace family hardware");
        plot.setBlock(BlockPos.ZERO.below(2), EquipmentContent.CATCH_SOCKET.get().defaultBlockState(), 3);
      }
    });
    s.at(55, () -> {
      if (r.linked) r.base.helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(r.base.level, r.base.parents.getFirst(), r.base.parents.getLast(), 6), "Cable cars did not couple");
      r.track = DirectedTrack.resolve(r.base.level, r.reverse ? r.base.to : r.base.from, r.reverse ? r.base.from : r.base.to).orElseThrow();
      for (var body : r.base.parents) s.preparation.add(new NativeFinClamp(SubLevelContainer.getContainer(r.base.level).physicsSystem().getPipeline(), body,
          VehicleInterfaces.brakePoint(body), new Vector3d(0, 0, r.reverse ? -1 : 1), 128));
    });
    s.at(75, () -> SCENES.add(s));
    s.at(420, () -> {
      var cabinet = (EquipmentCabinetEntity) r.base.level.getBlockEntity(s.charger);
      r.base.helper.assertTrue(cabinet.flywheel().energy() > 750 && cabinet.flywheel().energy() <= 5000
          && cabinet.flywheel().omega() > 0 && cabinet.calculateStressApplied() == 16,
          "Placed kinetic flywheel did not charge within its work and speed ratings");
      r.base.helper.assertTrue(s.failure.isEmpty() && s.rig.phase() == NativeCableRig.Phase.HOME && Math.abs(s.rig.speed()) < .03 && s.store().energy() > 750, "Cable carriage/shaft charging did not settle: " + s.failure);
      System.out.println("LS_CABLE_HOME mode=" + s.mode + " travel=" + s.rig.travel() + " speed=" + s.rig.speed() + " guide_error=" + s.rig.guideError()
          + " socket=" + NativeCableRig.worldPoint(s.lead, VehicleInterfaces.catchPoint(s.lead)).toString(java.text.NumberFormat.getNumberInstance(java.util.Locale.ROOT))
          + " carriage=" + s.rig.carriagePoint().toString(java.text.NumberFormat.getNumberInstance(java.util.Locale.ROOT)));
      s.engageRequested = true;
    });
    s.at(430, () -> {
      if (s.closed) return;
      if (s.rejected) { s.cleanup(); r.base.helper.succeed(); return; }
      s.pullRequested = true;
    });
    s.at(460, () -> {
      s.interruptionDraw = s.drawn;
      s.interruptionEnergy = s.store().energy();
      if (s.mode.equals("power_loss")) s.rig.emergencyHold("Shaft release power lost");
      if (s.mode.equals("reload_active")) {
        var cabinet = s.cabinet();
        double energy = cabinet.flywheel().energy();
        cabinet.loadWithComponents(cabinet.saveWithFullMetadata(r.base.level.registryAccess()), r.base.level.registryAccess());
        r.base.helper.assertTrue(cabinet.cableInUse() && !cabinet.armed() && cabinet.fault().contains("inspected recovery")
            && Math.abs(cabinet.flywheel().energy() - energy) < 1e-8,
            "Reload did not retain the cable journal, energy and inspection fault");
        boolean bindingDenied = false, linkingDenied = false;
        try { cabinet.bind(r.base.level, r.base.from, r.base.to); } catch (IllegalArgumentException expected) { bindingDenied = true; }
        try { cabinet.linkController(r.base.from, UUID.randomUUID()); } catch (IllegalArgumentException expected) { linkingDenied = true; }
        r.base.helper.assertTrue(bindingDenied && linkingDenied && RideControllers.occupiedEquipment(r.base.level, s.charger),
            "Retained cable catch allowed rebinding, relinking or block removal");
      }
      if (s.mode.equals("early_reset")) {
        boolean denied = false; try { s.rig.engage(s.lead.getUniqueId()); } catch (IllegalStateException expected) { denied = true; }
        r.base.helper.assertTrue(denied && s.rig.phase() == NativeCableRig.Phase.PULLING && !s.rig.consist().isEmpty(), "Early reset changed the active catch owner");
      }
      if (s.mode.equals("guide_edited")) {
        int revision = dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.get(r.base.level).revision();
        dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths.putCanonical(r.track.edge().bezier(), 12, 12);
        r.base.helper.assertTrue(dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.get(r.base.level).revision() == revision, "Guide handle edit unexpectedly rebuilt the graph");
      }
      if (s.mode.equals("guide_removed"))
        r.base.level.removeBlock(NativeCableRig.guideBlockAt(r.base.level, r.track, s.homeDistance + 4), false);
      if (s.mode.equals("mass_changed")) for (int x = 2; x <= 5; x++)
        r.base.helper.assertTrue(s.lead.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(x, 0, 0), Blocks.IRON_BLOCK.defaultBlockState(), 3), "Cable load extension did not install");
    });
    s.at(1350, () -> {
      if (s.closed) return;
      try {
        r.base.helper.assertTrue(s.failure.isEmpty(), "Native cable sensor failed: " + s.failure);
        if (s.mode.equals("depleted")) {
          r.base.helper.assertTrue(s.store().energy() < 1e-8 && s.drawn <= s.beginEnergy + 1e-8 && !s.released && s.rig.phase() == NativeCableRig.Phase.PULLING, "Empty flywheel created a launch/reset");
          r.base.helper.assertTrue(Math.abs(s.rig.travel()) < .03 && s.peakSpeed < .03 && s.drawn == 0, "Empty flywheel moved its catch/train");
          s.rig.emergencyHold("Depleted cable needs inspection");
        } else if (s.mode.equals("power_loss") || s.mode.equals("reload_active") || s.mode.equals("guide_edited") || s.mode.equals("guide_removed")) {
          r.base.helper.assertTrue(s.rig.phase() == NativeCableRig.Phase.FAULT && !s.released && !s.rig.consist().isEmpty()
              && !s.rig.fault().isEmpty() && Math.abs(s.drawn - s.interruptionDraw) < 1e-8
              && Math.abs(s.store().energy() - s.interruptionEnergy) < 1e-8
              && s.cabinet().cableInUse() && r.base.parents.stream().allMatch(b -> Math.abs(r.base.speed(b)) < .03),
              "Cable interruption hold lost its physical catch/train or added launch work");
        } else {
          r.base.helper.assertTrue(s.releases == 1 && s.releaseDistance >= 12 && s.releaseDistance < 12.2 && s.rig.phase() == NativeCableRig.Phase.HOME
              && Math.abs(s.rig.travel()) < .03 && Math.abs(s.rig.speed()) < .03 && s.rig.consist().isEmpty(), "Physical cable release/return failed: " + s.rig.phase() + " travel=" + s.rig.travel() + " released=" + s.releases);
          r.base.helper.assertTrue(s.pullSteps > 20 && s.returnSteps > 20 && s.peakSpeed > .5 && s.work > 1 && s.drawn > s.work && s.drawn <= s.beginEnergy + 1e-8
              && s.releaseOmega < s.beginOmega - .5
              && s.returnWork > 1 && s.returnWork <= s.shaftWork + 1e-8 && s.stopped && r.base.parents.stream().allMatch(b -> Math.abs(r.base.speed(b)) < .03), "Finite physical cable work/return/whole-train stop failed");
          r.base.helper.assertTrue(s.maxSocketError < .1 && s.maxGuideError < .03 && s.maxJointError < .05 && s.maxCouplerError < .05 && s.peakCatchForce <= 256.01,
              "Cable catch/guide or native train joint exceeded its rating");
          r.base.helper.assertTrue(s.peakTrainTraction > 1 && s.peakTrainTraction <= 256.01, "Native catch traction was absent or exceeded its configured rating");
        }
        if (s.mode.equals("single_forward")) {
          var cabinet = (EquipmentCabinetEntity) r.base.level.getBlockEntity(s.charger);
          var saved = cabinet.saveWithFullMetadata(r.base.level.registryAccess());
          double energy = cabinet.flywheel().energy();
          cabinet.loadWithComponents(saved, r.base.level.registryAccess());
          r.base.helper.assertTrue(!cabinet.armed() && !cabinet.cableInUse() && Math.abs(cabinet.flywheel().energy() - energy) < 1e-8,
              "Flywheel reload lost its finite work or rearmed the clutch");
          boolean denied = false;
          try { cabinet.setArmed(true); } catch (IllegalArgumentException expected) { denied = true; }
          r.base.helper.assertTrue(denied, "Unconnected flywheel armed without a cable controller");
        }
        System.out.println("LS_CABLE mode=" + s.mode + " phase=" + s.rig.phase() + " cars=" + r.base.parents.size() + " riders=" + r.base.riders.size() + " begin_energy=" + s.beginEnergy
            + " drawn=" + s.drawn + " work=" + s.work + " heat=" + s.heat + " return_work=" + s.returnWork + " shaft_work=" + s.shaftWork + " peak_speed=" + s.peakSpeed
            + " begin_omega=" + s.beginOmega + " release_omega=" + s.releaseOmega
            + " joint_reported_force=" + s.peakCatchForce + " train_traction=" + s.peakTrainTraction + " socket_error=" + s.maxSocketError + " guide_error=" + s.maxGuideError + " joint_error=" + s.maxJointError + " coupler_error=" + s.maxCouplerError
            + " release_distance=" + s.releaseDistance + " accounting_error=" + s.maxAccountingError + " pull_steps=" + s.pullSteps + " return_steps=" + s.returnSteps);
      } finally { s.cleanup(); }
      r.base.helper.succeed();
    });
  }
}
