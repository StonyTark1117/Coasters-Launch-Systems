package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3d;

/** Generated only for a combined install. Uses the copied artifact's real public operations. */
@GameTestHolder("coasterslaunchsystems")
public final class FourthDimensionForceGameTests {
  private record Measurement(ServerLevel level, Runnable sample) {}
  private static final List<Measurement> MEASUREMENTS = new ArrayList<>();
  private static boolean registered;

  private static void afterPhysics(ForgeSablePostPhysicsTickEvent event) {
    for (var measurement : List.copyOf(MEASUREMENTS))
      if (measurement.level() == event.getPhysicsSystem().getLevel()) measurement.sample().run();
  }
  @GameTestGenerator
  public static List<TestFunction> integrationTests() {
    if (!ModList.get().isLoaded("fourthdimension")) return List.of();
    var names = List.of("ARROW", "LEGACY", "FREE_SPIN", "REVISED", "PAX");
    var tests = new ArrayList<TestFunction>();
    for (int i = 0; i < names.size(); i++) {
      int index = i;
      tests.add(new TestFunction("launchsystems_4d", "coasterslaunchsystems.4d_" + names.get(i).toLowerCase(),
          "coasterslaunchsystems:empty", 420, 0, true,
          helper -> run(helper, names.get(index), index)));
    }
    return tests;
  }

  private static void run(GameTestHelper helper, String family, int index) {
    NativeForceGameTests.registerPhysics();
    if (!registered) {
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, FourthDimensionForceGameTests::afterPhysics);
      registered = true;
    }
    var level = helper.getLevel();
    var from = new BlockPos(96 + index * 32, 140, 64);
    var to = from.offset(0, 0, 64);
    for (int x = 5; x <= 15; x++) for (int z = 3; z <= 9; z++) level.setChunkForced(x, z, true);
    for (var pos : List.of(from, to))
      level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var player = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(
        new UUID(0x1a0c400L, index + 1), "LS4D_" + family));
    player.setGameMode(GameType.CREATIVE);
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    var designClass = type("dev.fourthdimension.track.TrackDesign");
    Object design = java.util.Arrays.stream(designClass.getEnumConstants())
        .filter(v -> ((Enum<?>) v).name().equals(family)).findFirst().orElseThrow();
    var trackItem = (Item) ((Supplier<?>) readStatic("dev.fourthdimension.track.TrackItems", "ARROW")).get();
    player.setItemInHand(InteractionHand.MAIN_HAND, trackItem.getDefaultInstance());
    invoke("dev.fourthdimension.track.TrackPlacement", "connect",
        new Class<?>[] {ServerLevel.class, BlockPos.class, BlockPos.class, designClass, Player.class, ItemStack.class},
        level, from, to, design, player, player.getMainHandItem());
    var vehicleType = type("dev.fourthdimension.runtime.FoundationData$Vehicle");
    Object[] vehicle = new Object[1];
    NativeForceGameTests.Probe[] probe = new NativeForceGameTests.Probe[1];
    double[] impulseAtVeto = new double[1];
    double[] massBeforeRider = new double[1];
    double[] jointError = new double[1];
    double[] rotorExcursion = new double[1];
    double[] groupDifference = new double[1];
    var rider = net.minecraft.world.entity.EntityType.PIG.create(level);
    rider.setNoAi(true);
    rider.setPos(from.getX(), from.getY() + 4, from.getZ() + 16);
    level.addFreshEntity(rider);
    var measurement = new Measurement(level, () -> {
      if (vehicle[0] == null || probe[0] == null || probe[0].contactSteps == 0) return;
      var container = SubLevelContainer.getContainer(level);
      var parent = probe[0].body;
      container.physicsSystem().updatePose(parent);
      double firstAngle = ((Number) read(((List<?>) read(vehicle[0], "carriers")).getFirst(), "angle")).doubleValue();
      for (var carrier : (List<?>) read(vehicle[0], "carriers")) {
        var child = (ServerSubLevel) container.getSubLevel((UUID) read(carrier, "body"));
        container.physicsSystem().updatePose(child);
        var pc = parent.getPlot().getCenterBlock();
        var cc = child.getPlot().getCenterBlock();
        var pp = nativePoint(parent, new Vector3d(
            pc.getX() + .5 + ((Number) read(carrier, "side")).doubleValue(),
            pc.getY() + .5 + ((Number) read(carrier, "pivotHeight")).doubleValue(),
            pc.getZ() + .5 + ((Number) read(carrier, "longitudinal")).doubleValue()));
        var cp = nativePoint(child, new Vector3d(cc.getX() + .5,
            cc.getY() + .5 + ((Number) read(carrier, "childPivotOffset")).doubleValue(), cc.getZ() + .5));
        double error = pp.distance(cp);
        if (error > .01 && jointError[0] <= .01)
          System.out.println("LS_4D_JOINT_FIRST family=" + family + " error=" + error
              + " impulse=" + probe[0].impulse + " parent_anchor=" + pp + " child_anchor=" + cp
              + " parent_plot=" + pc + " child_plot=" + cc
              + " parent_rotation_point=" + parent.logicalPose().rotationPoint()
              + " child_rotation_point=" + child.logicalPose().rotationPoint());
        jointError[0] = Math.max(jointError[0], error);
        double angle = ((Number) read(carrier, "angle")).doubleValue();
        rotorExcursion[0] = Math.max(rotorExcursion[0], Math.abs(angle));
        groupDifference[0] = Math.max(groupDifference[0], Math.abs(angle - firstAngle));
      }
    });
    helper.runAtTickTime(15, () -> {
      var edge = CoasterPathGraphManager.get(level).edges().stream()
          .filter(e -> e.from().equals(from) && e.to().equals(to)
              || e.from().equals(to) && e.to().equals(from)).findFirst().orElseThrow();
      var hit = new GraphHit(CoasterPathGraphManager.sampleEdgePosition(level, edge, .25), edge, .25);
      vehicle[0] = invoke("dev.fourthdimension.vehicle.VehicleFactory", "create",
          new Class<?>[] {ServerLevel.class, designClass, GraphHit.class}, level, design, hit);
    });
    helper.runAtTickTime(100, () -> {
      var v = vehicle[0];
      var id = (UUID) read(v, "chassis");
      var body = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel(id);
      var motion = MotionProviders.resolve(level, id).orElseThrow();
      helper.assertTrue(motion.provider().equals("fourthdimension:schema4"), "4D fell through to ordinary provider");
      helper.assertTrue(motion.bodies().size() == ((List<?>) read(v, "carriers")).size() + 1,
          "Articulated carrier mass identities were omitted");
      helper.assertTrue(!motion.propulsionAllowed(), "Captured vehicle allowed propulsion");
      helper.assertTrue(NativeForces.mass(level, motion) > body.getMassTracker().getMass(),
          "Carrier native masses not included");
      if (family.equals("PAX")) {
        helper.assertTrue(motion.exclusiveOperator().equals("fourthdimension:pax_winch")
            && !motion.dispatchReady(), "PAX ownership veto was bypassed");
        System.out.println("LS_4D_PAX exclusive_owner=" + motion.exclusiveOperator());
        helper.succeed();
        return;
      }
      helper.assertTrue((boolean) invoke("dev.fourthdimension.vehicle.Boarding", "ready",
          new Class<?>[] {ServerLevel.class, vehicleType}, level, v), "Initial boarding did not settle");
      for (var c : (List<?>) read(v, "carriers"))
        System.out.println("LS_4D_BASELINE family=" + family + " separation=" + separation(level, body, c));
      massBeforeRider[0] = NativeForces.mass(level, motion);
      var carrier = ((List<?>) read(v, "carriers")).getFirst();
      var carrierBody = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(carrier, "body"));
      var seat = (BlockPos) ((List<?>) read(carrier, "seats")).getFirst();
      SeatBlock.sitDown(level, carrierBody.getPlot().getCenterBlock().offset(seat), rider);
      helper.assertTrue(rider.isPassenger(), "Real rider did not mount the carrier seat");
    });
    helper.runAtTickTime(110, () -> {
      if (family.equals("PAX")) return;
      var v = vehicle[0];
      var id = (UUID) read(v, "chassis");
      var body = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel(id);
      var motion = MotionProviders.resolve(level, id).orElseThrow();
      helper.assertTrue(Math.abs(NativeForces.mass(level, motion) - massBeforeRider[0] - .8) < 1e-5,
          "Rider mass was omitted or counted twice by the motion provider");
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "restraints",
          new Class<?>[] {ServerLevel.class, vehicleType, boolean.class}, level, v, true);
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "dispatch",
          new Class<?>[] {ServerLevel.class, vehicleType}, level, v);
      probe[0] = new NativeForceGameTests.Probe(level, body);
      probe[0].track = DirectedTrack.resolve(level, from, to).orElseThrow();
      NativeForceGameTests.PROBES.add(probe[0]);
      MEASUREMENTS.add(measurement);
    });
    helper.runAtTickTime(150, () -> {
      if (probe[0] == null) return;
      var p = probe[0];
      var speed = SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
          .getLinearVelocity(p.body, new Vector3d()).z;
      System.out.println("LS_4D_FORCE family=" + family + " phase=launch speed=" + speed
          + " group_mass=" + NativeForces.mass(level, MotionProviders.resolve(level, p.body.getUniqueId()).orElseThrow())
          + " impulse=" + p.impulse + " missing=" + p.missingSteps + " veto=" + p.vetoSteps);
      helper.assertTrue(speed > 2 && speed < 5 && p.missingSteps == 0 && p.vetoSteps == 0,
          "Native articulated launch/contact failed: " + family);
      helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(p.body), "4D launch lost guide engagement");
      write(vehicle[0], "maintenance", true);
      impulseAtVeto[0] = p.impulse;
    });
    helper.runAtTickTime(155, () -> {
      if (probe[0] == null) return;
      helper.assertTrue(probe[0].impulse == impulseAtVeto[0] && probe[0].vetoSteps >= 10,
          "Maintenance ownership did not suppress interface force");
      write(vehicle[0], "maintenance", false);
      probe[0].braking = true;
    });
    helper.runAtTickTime(210, () -> {
      if (probe[0] == null) return;
      var p = probe[0];
      var speed = SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
          .getLinearVelocity(p.body, new Vector3d()).z;
      System.out.println("LS_4D_FORCE family=" + family + " phase=stop speed=" + speed);
      helper.assertTrue(Math.abs(speed) < .12, "Articulated friction stop failed: " + family);
      p.braking = false; p.reverse = true;
    });
    helper.runAtTickTime(250, () -> {
      if (probe[0] == null) return;
      var p = probe[0];
      var speed = SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
          .getLinearVelocity(p.body, new Vector3d()).z;
      System.out.println("LS_4D_FORCE family=" + family + " phase=reverse speed=" + speed);
      helper.assertTrue(speed < -2 && speed > -5, "Articulated reverse launch failed: " + family);
      p.braking = true;
    });
    helper.runAtTickTime(310, () -> {
      if (probe[0] == null) return;
      var p = probe[0];
      var speed = SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
          .getLinearVelocity(p.body, new Vector3d()).z;
      helper.assertTrue(Math.abs(speed) < .12 && p.missingSteps == 0,
          "Reverse stop/contact failed: " + family);
      helper.assertTrue(!CoasterCartTrackSnap.isBearingDisengaged(p.body), "4D reverse lost guide engagement");
      helper.assertTrue(rider.isPassenger(), "Loaded rider was lost during launch/braking");
      helper.assertTrue(jointError[0] < .01, "Launch separated native carrier joints: " + jointError[0]);
      if (!family.equals("ARROW")) {
        helper.assertTrue(rotorExcursion[0] > .05, "Interface force suppressed native free rotation");
        helper.assertTrue(groupDifference[0] > .005, "Asymmetric loading lost independent carrier motion");
      }
      NativeForceGameTests.PROBES.remove(p);
      MEASUREMENTS.remove(measurement);
      System.out.println("LS_4D_FORCE family=" + family + " phase=complete speed=" + speed
          + " max_joint_error=" + jointError[0] + " max_rotor_excursion=" + rotorExcursion[0]
          + " max_group_difference=" + groupDifference[0] + " rider_retained=" + rider.isPassenger()
          + " carrier_angles=" + ((List<?>) read(vehicle[0], "carriers")).stream().map(c -> read(c, "angle")).toList());
      helper.succeed();
    });
  }

  private static Vector3d nativePoint(ServerSubLevel body, Vector3d plotPoint) {
    // Sable 2.0.3 joints subtract the body's native mass center, independently of display pose RP.
    return plotPoint.sub(body.getMassTracker().getCenterOfMass())
        .rotate(body.logicalPose().orientation()).add(body.logicalPose().position());
  }

  private static double separation(ServerLevel level, ServerSubLevel parent, Object carrier) {
    var container = SubLevelContainer.getContainer(level);
    var child = (ServerSubLevel) container.getSubLevel((UUID) read(carrier, "body"));
    container.physicsSystem().updatePose(parent);
    container.physicsSystem().updatePose(child);
    var pc = parent.getPlot().getCenterBlock();
    var cc = child.getPlot().getCenterBlock();
    var pp = nativePoint(parent, new Vector3d(
        pc.getX() + .5 + ((Number) read(carrier, "side")).doubleValue(),
        pc.getY() + .5 + ((Number) read(carrier, "pivotHeight")).doubleValue(),
        pc.getZ() + .5 + ((Number) read(carrier, "longitudinal")).doubleValue()));
    var cp = nativePoint(child, new Vector3d(cc.getX() + .5,
        cc.getY() + .5 + ((Number) read(carrier, "childPivotOffset")).doubleValue(), cc.getZ() + .5));
    return pp.distance(cp);
  }

  private static Class<?> type(String name) {
    try { return Class.forName(name); }
    catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
  }
  private static Object readStatic(String type, String field) {
    try { return type(type).getField(field).get(null); }
    catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
  }
  private static Object read(Object value, String field) {
    try { return value.getClass().getField(field).get(value); }
    catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
  }
  private static void write(Object value, String field, Object setting) {
    try { value.getClass().getField(field).set(value, setting); }
    catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
  }
  private static Object invoke(String type, String method, Class<?>[] signature, Object... args) {
    try { return type(type).getMethod(method, signature).invoke(null, args); }
    catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
  }
}
