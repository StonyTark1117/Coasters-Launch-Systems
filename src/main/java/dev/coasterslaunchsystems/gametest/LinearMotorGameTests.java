package dev.coasterslaunchsystems.gametest;

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
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Actual occupied chassis, placed motors and shaft networks. Sensors run before fixture entry forces. */
@GameTestHolder("coasterslaunchsystems_linear_motors")
public final class LinearMotorGameTests {
  private static final List<Scene> SCENES = new ArrayList<>();
  private static boolean registered;
  private static final class Scene {
    final OrdinaryPassengerGameTests.Scene base;
    final Mechanism mechanism;
    final String mode;
    final List<BlockPos> modules = new ArrayList<>(), motors = new ArrayList<>();
    final Map<BlockPos, Double> oldEnergy = new HashMap<>(), oldHeat = new HashMap<>();
    double work, spent, heat, charged, impulse, peakForce, lastAcceleration, maxAccountingError;
    double initialSpeed, cutImpulse;
    int driveSteps, depletedSteps;
    boolean entry, observe, closed;
    String failure = "";
    Scene(GameTestHelper helper, int slot, Mechanism mechanism, String mode) {
      base = new OrdinaryPassengerGameTests.Scene(helper, slot, 256, -512); this.mechanism = mechanism; this.mode = mode;
      var body = base.cart(8);
      var plot = body.getPlot().getEmbeddedLevelAccessor();
      plot.setBlock(reactionPosition(mechanism), (mechanism == Mechanism.LSM ? EquipmentContent.LSM_PLATE : EquipmentContent.LIM_PLATE).get().defaultBlockState(), 3);
      if (mode.equals("missing")) plot.setBlock(reactionPosition(mechanism), (mechanism == Mechanism.LSM ? EquipmentContent.LIM_PLATE : EquipmentContent.LSM_PLATE).get().defaultBlockState(), 3);
      base.track = DirectedTrack.resolve(base.level, base.from, base.to).orElseThrow();
      int contact = mode.equals("brake") || mode.equals("rolling") ? 10 : 8;
      cabinet(1, contact); if (mode.equals("conflict")) cabinet(1, contact - 1);
    }
    void cabinet(int side, int contact) {
      var pos = base.from.offset(side, -2, contact); var motor = side > 0 ? pos.east() : pos.west();
      modules.add(pos); motors.add(motor);
      base.level.setBlockAndUpdate(pos, (mechanism == Mechanism.LSM ? EquipmentContent.LSM : EquipmentContent.LIM).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
      base.level.setBlockAndUpdate(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalBlock.FACING, side > 0 ? Direction.WEST : Direction.EAST));
      ((CreativeMotorBlockEntity) base.level.getBlockEntity(motor)).generatedSpeed.setValue(64);
    }
    EquipmentCabinetEntity cabinet() { return (EquipmentCabinetEntity) base.level.getBlockEntity(modules.getFirst()); }
    void snapshot() {
      for (var pos : modules) { var cabinet = (EquipmentCabinetEntity) base.level.getBlockEntity(pos); oldEnergy.put(pos, cabinet.energy()); oldHeat.put(pos, cabinet.heat()); }
    }
    void cleanup() {
      if (closed) return; closed = true; SCENES.remove(this);
      modules.forEach(p -> base.level.removeBlock(p, false)); motors.forEach(p -> base.level.removeBlock(p, false)); base.cleanup();
    }
    void at(int tick, Runnable action) { base.helper.runAtTickTime(tick, () -> { try { action.run(); } catch (RuntimeException e) { cleanup(); throw e; } }); }
  }
  static BlockPos reactionPosition(Mechanism mechanism) { return new BlockPos(0, -1, mechanism == Mechanism.LSM ? -1 : 1); }

  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(SCENES)) {
      if (s.closed || s.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        var body = s.base.parents.getFirst(); var motion = MotionProviders.resolve(s.base.level, body.getUniqueId()).orElseThrow();
        var contact = EngagedContacts.current(s.base.level, body).orElseThrow();
        s.base.helper.assertTrue(s.base.track.matches(contact.hit()), "Linear vehicle lost native support");
        var direction = new Vector3d(0, 0, 1); double dt = event.getTimeStep();
        var group = body.getQueuedForceGroups() == null ? null : body.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
        var localImpulse = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalForce());
        var localTorque = group == null ? new Vector3d() : new Vector3d(group.getForceTotal().getLocalTorque());
        double force = new Vector3d(localImpulse).rotate(body.logicalPose().orientation()).z / dt;
        if (s.observe) {
          var velocity = body.logicalPose().transformNormalInverse(event.getPhysicsSystem().getPipeline().getLinearVelocity(body, new Vector3d()));
          var angular = body.logicalPose().transformNormalInverse(event.getPhysicsSystem().getPipeline().getAngularVelocity(body, new Vector3d()));
          double work = velocity.dot(localImpulse) + angular.dot(localTorque) + .5 * body.getMassTracker().getInverseMass() * localImpulse.lengthSquared()
              + .5 * localTorque.dot(body.getMassTracker().getInverseInertiaTensor().transform(new Vector3d(localTorque)));
          double spent = 0, heat = 0;
          for (var pos : s.modules) {
            var cabinet = (EquipmentCabinetEntity) s.base.level.getBlockEntity(pos);
            double charge = Math.min(LinearMotor.CAPACITY - s.oldEnergy.get(pos), LinearMotor.chargingWork(cabinet.getSpeed(), dt));
            double consumed = s.oldEnergy.get(pos) + charge - cabinet.energy();
            double heated = cabinet.heat() - Math.max(0, s.oldHeat.get(pos) - 8 * dt);
            s.base.helper.assertTrue(consumed >= -1e-8 && heated >= -1e-8 && cabinet.energy() >= 0 && cabinet.energy() <= LinearMotor.CAPACITY,
                "Linear store created charge/heat or exceeded bounds");
            spent += consumed; heat += heated; s.charged += charge;
            if (s.mode.equals("depleted") && cabinet.energy() < 1e-8 && Math.abs(force) > 1e-8) s.depletedSteps++;
          }
          double error = Math.abs(spent - heat - work); s.maxAccountingError = Math.max(s.maxAccountingError, error);
          s.base.helper.assertTrue(error < 1e-7, "Native impulse work did not reconcile with buffer/heat: " + error);
          double acceleration = force / NativeForces.mass(s.base.level, motion);
          s.base.helper.assertTrue(Math.abs(acceleration) <= s.cabinet().accelerationLimit() + 1e-8 && Math.abs(force) <= 20 + 1e-8, "Motor exceeded force/acceleration envelope");
          s.base.helper.assertTrue(Math.abs(acceleration) <= Math.abs(s.lastAcceleration) + s.cabinet().jerkLimit() * dt + 1e-8, "Linear field increased acceleration beyond its jerk ramp");
          s.lastAcceleration = acceleration; s.work += work; s.spent += spent; s.heat += heat; s.impulse += Math.abs(force) * dt;
          s.peakForce = Math.max(s.peakForce, Math.abs(force)); if (Math.abs(force) > 1e-8) s.driveSteps++;
        }
        s.snapshot();
        s.base.helper.assertTrue(s.base.riders.stream().allMatch(net.minecraft.world.entity.animal.Pig::isPassenger), "Linear drive lost an occupied seat");
        if (s.entry) NativeForces.pointForce(body, body.getMassTracker().getCenterOfMass(), direction.mul(s.base.mass(body) * 2), dt);
      } catch (RuntimeException failure) { s.failure = failure.toString(); }
    }
  }
  @GameTestGenerator public static List<TestFunction> motors() {
    var tests = new ArrayList<TestFunction>(); int slot = 0;
    for (var mechanism : List.of(Mechanism.LSM, Mechanism.LIM)) for (String mode : List.of("empty", "full", "reverse", "rolling", "brake", "depleted", "hot", "missing", "conflict", "reload")) {
      int selected = slot++;
      tests.add(new TestFunction("launchsystems_linear", "coasterslaunchsystems_linear_motors." + mechanism.name().toLowerCase(Locale.ROOT) + "_" + mode,
          "coasterslaunchsystems_linear_motors:empty", 240, 0, true, helper -> run(new Scene(helper, selected, mechanism, mode))));
    }
    return tests;
  }
  private static void run(Scene s) {
    if (!registered) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LinearMotorGameTests::physics); registered = true; }
    s.at(40, () -> { if (!s.mode.equals("empty")) for (int seat = 0; seat < 4; seat++) s.base.board(s.base.parents.getFirst(), seat); });
    s.at(50, () -> {
      for (var pos : s.modules) {
        var cabinet = (EquipmentCabinetEntity) s.base.level.getBlockEntity(pos); cabinet.bind(s.base.level, s.base.from, s.base.to);
        cabinet.tuneLinear(s.mode.equals("reverse") ? -3 : s.mode.equals("brake") ? 0 : 3, 2, 4);
        if (s.mode.equals("hot")) cabinet.absorbHeat(8192);
      }
      var snapshot = MotionProviders.resolve(s.base.level, s.base.parents.getFirst().getUniqueId()).orElseThrow();
      s.base.helper.assertTrue(snapshot.interfaces().contains(s.mechanism) != s.mode.equals("missing"), "Wrong reaction hardware received a motor certificate");
      for (var id : List.of(s.mechanism.name().toLowerCase(Locale.ROOT) + "_drive", s.mechanism.name().toLowerCase(Locale.ROOT) + "_reaction_plate"))
        s.base.helper.assertTrue(s.base.level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("coasterslaunchsystems", id)).isPresent(), "Linear recipe did not load: " + id);
      if (s.mode.equals("reload")) toolTuning(s);
      s.snapshot(); SCENES.add(s);
    });
    s.at(60, () -> {
      s.entry = s.mode.equals("rolling") || s.mode.equals("brake");
      if (s.mode.equals("depleted")) ((CreativeMotorBlockEntity) s.base.level.getBlockEntity(s.motors.getFirst())).generatedSpeed.setValue(1);
    });
    s.at(80, () -> {
      s.entry = false; s.initialSpeed = s.base.speed(s.base.parents.getFirst());
      if (s.mode.equals("rolling") || s.mode.equals("brake")) s.base.helper.assertTrue(s.initialSpeed > 1.5, "Linear rolling entry was not established through force");
      for (var pos : s.modules) {
        var cabinet = (EquipmentCabinetEntity) s.base.level.getBlockEntity(pos);
        if (s.mode.equals("depleted")) { s.base.helper.assertTrue(Math.abs(cabinet.getSpeed()) == 1, "Limited supply did not settle before depletion"); cabinet.spendLinear(cabinet.energy()); }
        if (s.mode.equals("hot")) s.base.helper.assertTrue(cabinet.thermalFraction() <= .1000001,
            "Overheated linear store did not reach its declared minimum authority");
        cabinet.setArmed(true);
      }
      s.snapshot(); s.observe = true; s.lastAcceleration = 0;
    });
    s.at(110, () -> {
      if (!s.mode.equals("reload")) return;
      var cabinet = s.cabinet(); var tag = cabinet.saveWithFullMetadata(s.base.level.registryAccess());
      var restored = (EquipmentCabinetEntity) BlockEntity.loadStatic(s.modules.getFirst(), cabinet.getBlockState(), tag, s.base.level.registryAccess());
      s.base.helper.assertTrue(restored.identity().equals(cabinet.identity()) && !restored.armed() && restored.energy() == cabinet.energy()
          && restored.targetSpeed() == cabinet.targetSpeed() && restored.accelerationLimit() == cabinet.accelerationLimit() && restored.jerkLimit() == cabinet.jerkLimit(), "Motor load replayed a field or refilled/lost configuration");
      var legacy = tag.copy(); legacy.putInt("ls_schema", 3);
      var old = (EquipmentCabinetEntity) BlockEntity.loadStatic(s.modules.getFirst(), cabinet.getBlockState(), legacy, s.base.level.registryAccess());
      s.base.helper.assertTrue(old.energy() == 0 && !old.armed(), "Legacy motor invented stored energy or an enabled field");
      cabinet.loadWithComponents(tag, s.base.level.registryAccess()); s.snapshot(); s.cutImpulse = s.impulse;
    });
    s.at(140, () -> {
      s.base.helper.assertTrue(s.failure.isEmpty(), "Linear sensor failed: " + s.failure);
      double speed = s.base.speed(s.base.parents.getFirst());
      if (s.mode.equals("missing") || s.mode.equals("conflict")) {
        for (var pos : s.modules) {
          var cabinet = (EquipmentCabinetEntity) s.base.level.getBlockEntity(pos);
          s.base.helper.assertTrue(cabinet.fault().contains(s.mode.equals("missing") ? "reaction plate" : "Overlapping"),
              "Invalid linear equipment did not fail before force at " + pos + ": " + cabinet.fault());
        }
        s.base.helper.assertTrue(s.impulse == 0 && s.spent < 1e-8, "Rejected linear equipment applied work");
      } else {
        s.base.helper.assertTrue(s.cabinet().fault().isEmpty() && s.driveSteps > 10 && s.spent > 0 && s.heat > 0, "Linear motor never acted with finite resources: " + s.cabinet().fault());
        if (s.mode.equals("brake")) s.base.helper.assertTrue(speed < s.initialSpeed - .2 && s.work < 0 && s.heat > s.spent, "Active motor brake did not dissipate work");
        else if (s.mode.equals("reload")) s.base.helper.assertTrue(!s.cabinet().armed() && Math.abs(s.impulse - s.cutImpulse) < 1e-8, "Load reenabled the motor");
        else if (s.mode.equals("depleted")) s.base.helper.assertTrue(s.depletedSteps > 10 && s.peakForce < 5, "Depleted store did not limit native demand");
        else if (s.mode.equals("hot")) s.base.helper.assertTrue(s.cabinet().thermalFraction() <= .1000001 && s.peakForce <= 2.000001
            && speed > .08 && s.work > .1, "Hot linear store did not apply its bounded derated authority");
        else s.base.helper.assertTrue(speed * (s.mode.equals("reverse") ? -1 : 1) > (s.mode.equals("rolling") ? s.initialSpeed + .05 : .2) && s.work > 0, "Linear motor did not physically propel its loaded chassis: " + speed);
      }
      s.cutImpulse = s.impulse;
      for (var pos : s.motors) ((CreativeMotorBlockEntity) s.base.level.getBlockEntity(pos)).generatedSpeed.setValue(0);
    });
    s.at(150, () -> { s.base.helper.assertTrue(s.cabinet().getSpeed() == 0, "Shaft did not lose supply"); s.cutImpulse = s.impulse; });
    s.at(160, () -> {
      try {
        s.base.helper.assertTrue(s.failure.isEmpty() && s.cabinet().getSpeed() == 0 && Math.abs(s.impulse - s.cutImpulse) < 1e-8, "Lost kinetic supply continued a field: " + s.failure);
        System.out.println("LS_LINEAR mechanism=" + s.mechanism + " mode=" + s.mode + " riders=" + s.base.riders.size() + " mass=" + s.base.mass(s.base.parents.getFirst())
            + " speed=" + s.base.speed(s.base.parents.getFirst()) + " work=" + s.work + " spent=" + s.spent + " heat=" + s.heat + " charged=" + s.charged
            + " remaining=" + s.cabinet().energy() + " peak_force=" + s.peakForce + " steps=" + s.driveSteps + " depleted_steps=" + s.depletedSteps + " accounting_error=" + s.maxAccountingError);
      } finally { s.cleanup(); }
      s.base.helper.succeed();
    });
  }
  private static void toolTuning(Scene s) {
    var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(s.base.level, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "LSLinearTuning"));
    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE); player.setPos(s.modules.getFirst().getX(), s.modules.getFirst().getY() + 1, s.modules.getFirst().getZ());
    player.setItemInHand(InteractionHand.MAIN_HAND, EquipmentContent.TOOL.get().getDefaultInstance());
    for (var item : List.of(Items.CLOCK, Items.IRON_NUGGET, Items.GOLD_NUGGET)) {
      player.setItemInHand(InteractionHand.OFF_HAND, item.getDefaultInstance());
      s.base.helper.assertTrue(player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
          new BlockHitResult(s.modules.getFirst().getCenter(), Direction.UP, s.modules.getFirst(), false))) == InteractionResult.SUCCESS, "Real span tool motor tuning failed");
    }
    s.base.helper.assertTrue(s.cabinet().targetSpeed() == 6 && s.cabinet().accelerationLimit() == .5 && s.cabinet().jerkLimit() == 8, "Tool did not change independent motor settings");
  }
}
