package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.*;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Native family bodies, actual riders and native station guards under local launch authority. */
@GameTestHolder("coasterslaunchsystems_articulated")
@net.neoforged.fml.common.EventBusSubscriber(modid = "coasterslaunchsystems")
public final class ArticulatedFamilyGameTests {
  private static final boolean PLAYER = "true".equals(System.getenv("LS_ARTICULATED_PLAYER"));
  static final List<String> FAMILIES = List.of("suspended", "axis", "pipeline", "dive", "flying", "new_generation_flying");
  private static final List<Run> ACTIVE = new ArrayList<>();
  private static boolean listening;
  private static final class Run {
    final RideControllerGameTests.Scene ride;
    final String family, kind;
    final Mechanism mechanism;
    EquipmentCabinetEntity skid, eddy; double skidHeat, eddyHeat;
    final List<BlockPos> docks = new ArrayList<>();
    double emptyMass, work, peak, distance, posturePeak, yawPeak;
    int steps;
    String failure = "";
    Run(GameTestHelper h, int slot, String family, String kind) {
      this.family = family; this.kind = kind; this.mechanism = kind.equals("lsm") ? Mechanism.LSM : kind.equals("lim") ? Mechanism.LIM : Mechanism.TIRE;
      ride = new RideControllerGameTests.Scene(h, slot, family + "_forward", 208); ride.driveMechanism = mechanism;
    }
    void at(int tick, Runnable task) {
      ride.base.helper.runAtTickTime(tick, () -> {
        try { task.run(); } catch (RuntimeException e) { cleanup(); throw e; }
      });
    }
    void cleanup() {
      ACTIVE.remove(this); docks.forEach(p -> ride.base.level.removeBlock(p, false));
      var data = invoke("dev.fourthdimension.runtime.FoundationData", "get", new Class<?>[] {ServerLevel.class}, ride.base.level);
      var vehicles = (Map<?, ?>) read(data, "vehicles");
      for (var vehicle : ride.vehicles) vehicles.remove(read(vehicle, "chassis"));
      ((List<?>) read(data, "rails")).removeIf(rail -> read(rail, "from").equals(ride.base.from) && read(rail, "to").equals(ride.base.to)
          || read(rail, "from").equals(ride.base.to) && read(rail, "to").equals(ride.base.from));
      ride.cleanup();
    }
  }
  @GameTestGenerator public static List<TestFunction> families() {
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) return List.of();
    var tests = new ArrayList<TestFunction>();
    var selection = System.getenv("LS_ARTICULATED_CASES");
    for (int i = 0; i < FAMILIES.size(); i++) for (String kind : List.of("tire", "lsm", "lim", "skid", "eddy", "power")) {
      String family = FAMILIES.get(i), name = family + "_" + kind; int slot = i;
      if (kind.equals("power") && !family.equals("flying")) continue;
      if (PLAYER && !kind.equals("tire")) continue;
      if (selection != null && !Arrays.asList(selection.split(",")).contains(name)
          && !(kind.equals("tire") && Arrays.asList(selection.split(",")).contains(family))) continue;
      tests.add(new TestFunction("launchsystems_articulated_" + kind, "coasterslaunchsystems_articulated." + name,
          "coasterslaunchsystems_articulated:empty", PLAYER ? 72000 : 2200, 0, true, h -> run(new Run(h, slot, family, kind))));
    }
    return tests;
  }
  @net.neoforged.bus.api.SubscribeEvent public static void commands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
    if (!PLAYER || !Boolean.getBoolean("coasterslaunchsystems.validation")) return;
    var root = net.minecraft.commands.Commands.literal("lsarticulated").requires(source -> source.hasPermission(2));
    root.then(net.minecraft.commands.Commands.literal("status").executes(ctx -> {
      for (var run : List.copyOf(ACTIVE)) {
        var r = run.ride; var v = r.vehicles.getFirst();
        var result = new LinkedHashMap<String, Object>();
        result.put("family", run.family); result.put("chassis", read(v, "chassis")); result.put("controller", r.controllerPosition.toShortString());
        result.put("state", r.controller.status()); result.put("work", run.work); result.put("steps", run.steps); result.put("distance", run.distance);
        result.put("nativeFault", read(v, "fault")); result.put("restraints", read(v, "restraints")); result.put("stopping", read(v, "stopping"));
        result.put("carrierIds", ((List<?>) read(v, "carriers")).stream().map(c -> read(c, "body")).toList());
        result.put("occupants", ((List<?>) read(v, "carriers")).stream().map(c -> read(c, "occupants")).toList());
        result.put("nativeAngles", ((List<?>) read(v, "carriers")).stream().map(c -> read(c, "angle")).toList());
        result.put("players", r.base.level.players().stream().map(player -> Map.of("name", player.getName().getString(), "seat", player.getRootVehicle().getUUID(), "riding", player.isPassenger())).toList());
        if (run.family.contains("flying")) { result.put("postureLock", read(read(v, "flying"), "lock")); result.put("body", read(read(v, "flying"), "body")); result.put("legs", read(read(v, "flying"), "legs")); }
        if (run.family.equals("new_generation_flying")) { result.put("deck", read(read(v, "yaw"), "deck")); result.put("yaw", read(read(v, "yaw"), "angle")); }
        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(new com.google.gson.Gson().toJson(result)), false);
      }
      return ACTIVE.size();
    }));
    event.getDispatcher().register(root);
  }
  private static String runtime(String family) {
    return "dev.fourthdimension.vehicle." + switch (family) {
      case "suspended" -> "SuspendedRuntime";
      case "axis" -> "AxisRuntime";
      case "pipeline" -> "PipelineRuntime";
      case "dive" -> "DiveRuntime";
      case "flying" -> "FlyingRuntime";
      case "new_generation_flying" -> "NewGenerationFlyingRuntime";
      default -> throw new IllegalArgumentException(family);
    };
  }
  private static Class<?> vehicleType() { return type("dev.fourthdimension.runtime.FoundationData$Vehicle"); }
  private static void run(Run s) {
    if (!listening) { NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ArticulatedFamilyGameTests::physics); listening = true; }
    var r = s.ride; var h = r.base.helper; var level = r.base.level;
    boolean flying = s.family.equals("flying"), compound = s.family.equals("new_generation_flying");
    int launch = flying || s.mechanism.linear() ? 600 : 270;
    s.at(25, () -> {
      for (var vehicle : r.vehicles) {
        var station = (Block) ((java.util.function.Supplier<?>) readStatic("dev.fourthdimension.station.StationContent", "STATION")).get();
        var pos = r.base.from.offset(7, -3, 24); s.docks.add(pos);
        level.setBlockAndUpdate(pos, station.defaultBlockState());
        write(level.getBlockEntity(pos), "vehicle", read(vehicle, "chassis")); write(vehicle, "dock", pos);
        var certificate = dev.coasterslaunchsystems.integration.FourthDimensionCertificates.find(((Enum<?>) read(vehicle, "family")).name(), (String) read(vehicle, "variant"));
        var body = (dev.ryanhcode.sable.sublevel.ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(vehicle, "chassis"));
        var plot = body.getPlot().getEmbeddedLevelAccessor();
        if (s.mechanism.linear()) {
          var offset = certificate.offset(s.mechanism); h.assertTrue(plot.getBlockState(offset).isAir(), "Reaction plate replaces native hardware");
          plot.setBlock(offset, (s.mechanism == Mechanism.LSM ? EquipmentContent.LSM_PLATE : EquipmentContent.LIM_PLATE).get().defaultBlockState(), 3);
        }
        if (passive(s)) {
          for (var mechanism : List.of(s.kind.equals("skid") ? Mechanism.SKID_BRAKE : Mechanism.EDDY_BRAKE)) {
            var offset = certificate.offset(mechanism); h.assertTrue(plot.getBlockState(offset).isAir(), "Passive hardware replaces native component");
            if (mechanism == Mechanism.SKID_BRAKE && plot.getBlockState(offset.above()).isAir()) plot.setBlock(offset.above(), net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(), 3);
            plot.setBlock(offset, (mechanism == Mechanism.SKID_BRAKE ? EquipmentContent.SKID_SHOE : EquipmentContent.CONDUCTIVE_FIN).get().defaultBlockState(), 3);
          }
        }
      }
    });
    s.at(100, () -> {
      for (var v : r.vehicles) invoke(runtime(s.family), "rebindDock",
          new Class<?>[] {ServerLevel.class, vehicleType(), BlockPos.class}, level, v, read(v, "dock"));
    });
    s.at(230, () -> {
      for (var b : r.base.parents) {
        var m = MotionProviders.resolve(level, b.getUniqueId()).orElseThrow();
        h.assertTrue(m.provider().equals("fourthdimension:authority1"), "Family did not use native authority provider");
        h.assertTrue(!m.propulsionAllowed(), "Captured native family allowed propulsion");
        s.emptyMass += NativeForces.mass(level, m);
      }
      if (!PLAYER) r.boardFourthDimension();
    });
    s.at(240, () -> {
      if (!PLAYER) for (var v : r.vehicles) {
        if (flying || compound) {
          invoke(runtime(s.family), "restraints", new Class<?>[] {ServerLevel.class, vehicleType(), boolean.class, boolean.class}, level, v, true, true);
          h.assertTrue(!MotionProviders.resolve(level, (UUID) read(v, "chassis")).orElseThrow().dispatchReady(), "Open leg restraint allowed dispatch");
          invoke(runtime(s.family), "restraints", new Class<?>[] {ServerLevel.class, vehicleType(), boolean.class, boolean.class}, level, v, false, true);
        } else invoke("dev.fourthdimension.vehicle.VehicleOperations", "restraints", new Class<?>[] {ServerLevel.class, vehicleType(), boolean.class}, level, v, true);
      }
      double loaded = r.base.parents.stream().mapToDouble(b -> NativeForces.mass(level, MotionProviders.resolve(level, b.getUniqueId()).orElseThrow())).sum();
      h.assertTrue(Math.abs(loaded - s.emptyMass - .8 * r.base.riders.size()) < 1e-5, "Native family rider mass omitted or counted twice");
      if (passive(s)) installPassive(s); else if (s.mechanism.linear()) installLinear(s); else r.install(false);
      if (compound) {
        nativeProgram(s);
        for (var p : r.motors) ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) level.getBlockEntity(p)).generatedSpeed.setValue(32);
      }
      r.controller.setArmed(true); ACTIVE.add(s);
      System.out.println("LS_ARTICULATED_LOAD family=" + s.family + " kind=" + s.kind + " riders=" + r.base.riders.size() + " empty=" + s.emptyMass + " loaded=" + loaded);
    });
    if (PLAYER) return;
    if (flying) s.at(260, () -> {
      var v = r.vehicles.getFirst();
      h.assertTrue(!MotionProviders.resolve(level, (UUID) read(v, "chassis")).orElseThrow().dispatchReady(), "Seated Flying posture allowed dispatch");
      invoke(runtime(s.family), "requestPosture", new Class<?>[] {ServerLevel.class, vehicleType(), boolean.class, boolean.class}, level, v, true, false);
    });
    if (s.kind.equals("power")) {
      s.at(290, () -> {
        var v = r.vehicles.getFirst();
        h.assertTrue((boolean) invoke(runtime(s.family), "powered", new Class<?>[] {ServerLevel.class, vehicleType()}, level, v), "Linked kinetic supply did not power native posture");
        h.assertTrue(read(read(v, "flying"), "phase").toString().equals("TO_PRONE"), "Power interruption missed actual changeover");
        for (var p : r.motors) ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) level.getBlockEntity(p)).generatedSpeed.setValue(0);
      });
      s.at(330, () -> {
        var v = r.vehicles.getFirst();
        h.assertTrue(!(boolean) invoke(runtime(s.family), "powered", new Class<?>[] {ServerLevel.class, vehicleType()}, level, v), "Stopped kinetic source still supplied native accessory power");
        h.assertTrue(read(v, "fault").toString().contains("power lost") && read(read(v, "flying"), "phase").toString().equals("INTERRUPTED_TRANSITION"), "Native power loss did not retain interrupted posture: " + read(v, "fault"));
        h.assertTrue((boolean) read(v, "stopping") && r.base.riders.stream().allMatch(p -> p.isPassenger()), "Power loss released native riders");
        for (var p : r.motors) ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) level.getBlockEntity(p)).generatedSpeed.setValue(64);
      });
      s.at(430, () -> {
        var v = r.vehicles.getFirst();
        h.assertTrue((boolean) invoke(runtime(s.family), "powered", new Class<?>[] {ServerLevel.class, vehicleType()}, level, v), "Restored kinetic source did not restore accessory supply");
        h.assertTrue(read(v, "fault").toString().contains("power lost") && read(read(v, "flying"), "phase").toString().equals("INTERRUPTED_TRANSITION"), "Restoring source silently resumed native posture");
        h.assertTrue(!MotionProviders.resolve(level, (UUID) read(v, "chassis")).orElseThrow().dispatchReady() && r.base.riders.stream().allMatch(p -> p.isPassenger()), "Restored supply bypassed explicit native recovery");
        System.out.println("LS_ARTICULATED_POWER family=flying riders=" + r.base.riders.size() + " actual_source_stopped=true restored_no_restart=true fault=" + read(v, "fault"));
        s.cleanup(); h.succeed();
      });
      return;
    }
    s.at(launch, () -> {
      h.assertTrue(r.controller.phase() == RideControllerEntity.State.READY && s.steps == 0,
          "Native dock/local controller not ready: " + r.controller.status());
      r.high(true);
    });
    s.at(launch + 40, () -> h.assertTrue(s.steps > 0 && s.work > .001,
        "Loaded native family received no local tire work: " + r.controller.status() + " steps=" + s.steps + " work=" + s.work + " distance=" + s.distance + " native=" + read(r.vehicles.getFirst(), "fault") + " " + s.failure));
    if (passive(s)) s.at(launch + 600, () -> {
      h.assertTrue(s.failure.isEmpty() && (s.kind.equals("skid") ? s.skidHeat : s.eddyHeat) > .001, "Passive family interfaces did no native dissipative work: " + s.family + " skid=" + s.skidHeat + " eddy=" + s.eddyHeat + " " + s.failure + " distance=" + s.distance + " cabinet=" + (s.skid != null ? s.skid.status() : s.eddy.status()) + " " + r.controller.status());
      h.assertTrue(r.base.riders.stream().allMatch(p -> p.isPassenger()), "Passive braking lost native riders");
      System.out.println("LS_ARTICULATED_PASSIVE family=" + s.family + " kind=" + s.kind + " riders=" + r.base.riders.size() + " skidHeat=" + s.skidHeat + " eddyHeat=" + s.eddyHeat + " distance=" + s.distance + " peak=" + s.peak);
      s.cleanup(); h.succeed();
    });
    s.at(launch + 980, () -> {
      if (r.closed) return;
      h.assertTrue(s.failure.isEmpty(), s.failure);
      h.assertTrue(r.controller.phase() == RideControllerEntity.State.HOLD && s.steps > 20 && s.distance > (Set.of("suspended", "pipeline", "flying", "new_generation_flying").contains(s.family) ? 14 : 19),
          "Native family did not reach held fin bank: " + r.controller.status() + " distance=" + s.distance);
      h.assertTrue(r.base.riders.stream().allMatch(p -> p.isPassenger()), "Articulated launch lost a native rider");
      h.assertTrue(Math.abs(r.base.speed(r.base.parents.getFirst())) < .03, "Whole loaded body did not stop");
      int steps = s.steps;
      s.at(launch + 1020, () -> {
        h.assertTrue(s.steps == steps && r.controller.phase() == RideControllerEntity.State.HOLD, "Held input replayed a native dispatch");
        var vehicle = r.vehicles.getFirst();
        if (flying || compound) h.assertTrue(s.posturePeak > .05, "Native posture did not move under its own joint control");
        if (compound) {
          h.assertTrue(s.yawPeak > .01, "Compound yaw deck did not retain native rotation");
          var yaw = read(vehicle, "yaw"); var original = read(yaw, "deck");
          write(yaw, "deck", UUID.randomUUID());
          var absent = MotionProviders.resolve(level, (UUID) read(vehicle, "chassis")).orElseThrow();
          h.assertTrue(!absent.dispatchReady() && !absent.propulsionAllowed() && !absent.brakingAllowed(), "Missing yaw deck failed open");
          write(yaw, "deck", null);
          var missing = MotionProviders.resolve(level, (UUID) read(vehicle, "chassis")).orElseThrow();
          h.assertTrue(!missing.dispatchReady() && !missing.propulsionAllowed() && !missing.brakingAllowed(), "Null yaw deck failed open");
          write(yaw, "deck", original);
        }
        write(vehicle, "maintenance", true);
        var denied = MotionProviders.resolve(level, (UUID) read(vehicle, "chassis")).orElseThrow();
        h.assertTrue(!denied.dispatchReady() && !denied.propulsionAllowed(), "Native maintenance was bypassed");
        System.out.println("LS_ARTICULATED_PASS family=" + s.family + " kind=" + s.kind + " riders=" + r.base.riders.size() + " work=" + s.work
            + " peak=" + s.peak + " steps=" + s.steps + " distance=" + s.distance + " posture=" + s.posturePeak + " yaw=" + s.yawPeak + " native_guard=true held_input=true");
        s.cleanup(); h.succeed();
      });
    });
  }
  private static void installLinear(Run s) {
    var r = s.ride; var level = r.base.level;
    level.setBlockAndUpdate(r.controllerPosition, EquipmentContent.CONTROLLER.get().defaultBlockState());
    r.controller = (RideControllerEntity) level.getBlockEntity(r.controllerPosition); r.controller.bind(level, r.base.from, r.base.to);
    for (int distance = 8; distance <= 16; distance += 2) {
      var pos = r.base.from.offset(-1, r.cabinetY, distance); var motor = pos.west(); r.equipment.add(pos); r.motors.add(motor);
      level.setBlockAndUpdate(pos, (s.mechanism == Mechanism.LSM ? EquipmentContent.LSM : EquipmentContent.LIM).get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X));
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(pos);
      cabinet.tuneLinear(s.family.equals("new_generation_flying") ? 1.5 : 2, cabinet.accelerationLimit(), cabinet.jerkLimit());
      level.setBlockAndUpdate(motor, com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(net.minecraft.world.level.block.DirectionalBlock.FACING, net.minecraft.core.Direction.EAST));
      ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) level.getBlockEntity(motor)).generatedSpeed.setValue(64);
      cabinet.bind(level, r.base.from, r.base.to); r.controller.link(cabinet, false); cabinet.setArmed(true); r.module(distance, true);
    }
    for (int distance = 21; distance <= 41; distance += 2) r.module(distance, true);
    r.track = dev.coasterslaunchsystems.track.DirectedTrack.resolve(level, r.base.from, r.base.to).orElseThrow();
  }
  private static boolean passive(Run s) { return s.kind.equals("skid") || s.kind.equals("eddy"); }
  private static void installPassive(Run s) {
    var r = s.ride; var level = r.base.level;
    level.setBlockAndUpdate(r.controllerPosition, EquipmentContent.CONTROLLER.get().defaultBlockState());
    r.controller = (RideControllerEntity) level.getBlockEntity(r.controllerPosition); r.controller.bind(level, r.base.from, r.base.to);
    for (int d = 8; d <= 16; d += 2) { r.module(d, false); r.module(d, true); }
    for (int d = 29; d <= 51; d += 2) r.module(d, true);
    for (var mechanism : List.of(s.kind.equals("skid") ? Mechanism.SKID_BRAKE : Mechanism.EDDY_BRAKE)) {
      var pos = r.base.from.offset(mechanism == Mechanism.SKID_BRAKE ? 1 : -1, r.cabinetY, mechanism == Mechanism.SKID_BRAKE ? 21 : 20);
      level.setBlockAndUpdate(pos, (mechanism == Mechanism.SKID_BRAKE ? EquipmentContent.SKID : EquipmentContent.EDDY).get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X));
      var cabinet = (EquipmentCabinetEntity) level.getBlockEntity(pos); cabinet.bind(level, r.base.from, r.base.to); cabinet.setArmed(true); r.equipment.add(pos);
      if (mechanism == Mechanism.SKID_BRAKE) s.skid = cabinet; else s.eddy = cabinet;
    }
    r.track = dev.coasterslaunchsystems.track.DirectedTrack.resolve(level, r.base.from, r.base.to).orElseThrow();
  }
  private static void nativeProgram(Run s) {
    var v = s.ride.vehicles.getFirst(); var l = s.ride.base.level;
    var rail = invoke(runtime(s.family), "rail", new Class<?>[] {ServerLevel.class, vehicleType()}, l, v);
    var tag = new net.minecraft.nbt.CompoundTag(); tag.putInt("version", 1); tag.putLong("revision", 1);
    tag.putLong("geometry", ((Number) read(rail, "geometryHash")).longValue());
    var owner = UUID.randomUUID(); tag.putUUID("owner", owner); write(read(v, "yaw"), "owner", owner);
    var list = new net.minecraft.nbt.ListTag(); var interval = new net.minecraft.nbt.CompoundTag();
    interval.putUUID("id", UUID.randomUUID()); interval.putString("name", "LS compound native profile");
    interval.putDouble("start", 0); interval.putDouble("end", 64); interval.putString("mode", "PROFILE");
    interval.putDouble("phi_from", 0); interval.putDouble("phi_to", Math.PI / 2); interval.putString("winding", "NEAREST");
    interval.putDouble("speed", .45); interval.putDouble("acceleration", .45);
    var keys = new net.minecraft.nbt.ListTag();
    for (double at : new double[] {0, 64}) { var key = new net.minecraft.nbt.CompoundTag(); key.putDouble("distance", at); key.putDouble("angle", at / 64 * Math.PI / 2); keys.add(key); }
    interval.put("keys", keys); list.add(interval); tag.put("intervals", list);
    write(rail, "yaw", invoke("dev.fourthdimension.vehicle.YawSection", "read", new Class<?>[] {net.minecraft.nbt.CompoundTag.class}, tag));
  }
  private static void physics(ForgeSablePrePhysicsTickEvent event) {
    for (var s : List.copyOf(ACTIVE)) {
      var r = s.ride; if (r.base.level != event.getPhysicsSystem().getLevel()) continue;
      try {
        if (s.skid != null) s.skidHeat = Math.max(s.skidHeat, s.skid.heat());
        if (s.eddy != null) s.eddyHeat = Math.max(s.eddyHeat, s.eddy.heat());
        for (var b : r.base.parents) {
          var v = r.vehicles.getFirst();
          for (var carrier : (List<?>) read(v, "carriers")) s.posturePeak = Math.max(s.posturePeak, Math.abs(((Number) read(carrier, "angle")).doubleValue()));
          if (s.family.equals("new_generation_flying")) s.yawPeak = Math.max(s.yawPeak, Math.abs(((Number) read(read(v, "yaw"), "angle")).doubleValue()));
          var contact = EngagedContacts.current(r.base.level, b).orElseThrow();
          r.base.helper.assertTrue(r.track.matches(contact.hit()), "Native family lost matching support");
          s.distance = r.track.distance(contact.hit());
          var velocity = event.getPhysicsSystem().getPipeline().getLinearVelocity(b, new Vector3d());
          s.peak = Math.max(s.peak, velocity.length());
          var group = b.getQueuedForceGroups() == null ? null : b.getQueuedForceGroups().get(ForceGroups.PROPULSION.get());
          if (group != null) {
            var impulse = new Vector3d(group.getForceTotal().getLocalForce()).rotate(b.logicalPose().orientation());
            if (impulse.lengthSquared() > 1e-12) {
              r.base.helper.assertTrue((r.controller.phase() == RideControllerEntity.State.RUNNING || passive(s)) && r.controller.reservation().contains(b.getUniqueId()), "Native impulse bypassed local reservation");
              s.work += Math.max(0, impulse.dot(velocity)); s.steps++;
            }
          }
        }
      } catch (RuntimeException e) { s.failure = e.toString(); }
    }
  }
}
