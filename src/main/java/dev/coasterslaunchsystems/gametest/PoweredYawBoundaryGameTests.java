package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.*;
import dev.coasterslaunchsystems.api.*;
import dev.coasterslaunchsystems.content.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** The native powered-yaw circuit owns speed, dwell, reversals and capture as one mechanism. */
@GameTestHolder("coasterslaunchsystems_articulated")
public final class PoweredYawBoundaryGameTests {
  private static Object method(Object target, String name) {
    try { return target.getClass().getMethod(name).invoke(target); }
    catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
  }
  @GameTestGenerator public static List<TestFunction> tests() {
    String selection = System.getenv("LS_ARTICULATED_CASES");
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension")
        || selection != null && !Arrays.asList(selection.split(",")).contains("powered_inverted")) return List.of();
    return List.of(new TestFunction("launchsystems_articulated", "coasterslaunchsystems_articulated.powered_inverted_governor",
        "coasterslaunchsystems_articulated:empty", 180, 0, true, PoweredYawBoundaryGameTests::run));
  }
  private static void run(GameTestHelper h) {
    var level = h.getLevel(); var origin = new BlockPos(-800, 160, 0);
    var example = new Object[1]; var vehicle = new Object[1];
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    double[] emptyMass = {0};
    h.runAtTickTime(1, () -> {
      for (int x = -51; x <= -45; x++) for (int z = -3; z <= 11; z++) level.setChunkForced(x, z, true);
      example[0] = invoke("dev.fourthdimension.vehicle.PoweredInvertedYawExamples", "create",
          new Class<?>[] {ServerLevel.class, BlockPos.class, int.class, UUID.class}, level, origin, 1, UUID.randomUUID());
      vehicle[0] = ((List<?>) method(example[0], "cars")).getFirst();
    });
    h.runAtTickTime(50, () -> invoke("dev.fourthdimension.vehicle.PoweredInvertedYawExamples", "bind",
        new Class<?>[] {ServerLevel.class, type("dev.fourthdimension.vehicle.PoweredInvertedYawExamples$Example")}, level, example[0]));
    h.runAtTickTime(65, () -> {
      var carrier = ((List<?>) read(vehicle[0], "carriers")).getFirst();
      var body = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(carrier, "body"));
      emptyMass[0] = body.getMassTracker().getMass();
      for (var seat : (List<?>) read(carrier, "seats")) {
        var rider = net.minecraft.world.entity.EntityType.PIG.create(level); rider.setNoAi(true); level.addFreshEntity(rider);
        com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(level, body.getPlot().getCenterBlock().offset((BlockPos) seat), rider);
        h.assertTrue(rider.isPassenger(), "Powered yaw native seat did not retain rider"); riders.add(rider);
      }
    });
    h.runAtTickTime(100, () -> {
      var v = vehicle[0]; var id = (UUID) read(v, "chassis");
      var vehicleType = type("dev.fourthdimension.runtime.FoundationData$Vehicle");
      h.assertTrue((boolean) invoke("dev.fourthdimension.vehicle.Boarding", "ready", new Class<?>[] {ServerLevel.class, vehicleType}, level, v), "Native powered governor dock is not ready");
      invoke("dev.fourthdimension.vehicle.VehicleOperations", "restraints", new Class<?>[] {ServerLevel.class, vehicleType, boolean.class}, level, v, true);
      var carrier = ((List<?>) read(v, "carriers")).getFirst();
      var body = (ServerSubLevel) SubLevelContainer.getContainer(level).getSubLevel((UUID) read(carrier, "body"));
      h.assertTrue(Math.abs(body.getMassTracker().getMass() - emptyMass[0] - .8 * riders.size()) < 1e-5, "Native powered rider loading wrong");
      var before = method(read(v, "authority"), "save");
      var yawBefore = method(read(v, "poweredYaw"), "save");
      h.assertTrue(read(read(v, "authority"), "owner") != null, "No actual native governor lease to protect");
      var pos = origin.offset(12, 0, 12);
      level.setBlockAndUpdate(pos, EquipmentContent.CONTROLLER.get().defaultBlockState());
      ((RideControllerEntity) level.getBlockEntity(pos)).reserve(Set.of(id));
      var snapshot = MotionProviders.resolve(level, id).orElseThrow();
      h.assertTrue(snapshot.exclusiveOperator().equals("fourthdimension:powered_yaw_governor")
          && !snapshot.dispatchReady() && !snapshot.propulsionAllowed() && !snapshot.brakingAllowed() && snapshot.interfaces().isEmpty(), "Native powered circuit gained an external local certificate");
      h.assertTrue(!MotionProviders.reserveAuthority(level, id, pos) && !MotionProviders.requestDispatch(level, id)
          && !MotionProviders.requestCapture(level, id, Set.of(id)), "Local LS stole a powered governor operation");
      h.assertTrue(before.equals(method(read(v, "authority"), "save")) && yawBefore.equals(method(read(v, "poweredYaw"), "save"))
          && riders.stream().allMatch(p -> p.isPassenger()), "Denied LS call changed native owner/dwell/direction/retention");
      level.removeBlock(pos, false);
      System.out.println("LS_POWERED_YAW_BOUNDARY PASS riders=" + riders.size() + " exactMassDelta=" + (body.getMassTracker().getMass() - emptyMass[0])
          + " nativeLease=" + before + " allLocalMechanismsDenied=true reserveDispatchCaptureUnchanged=true");
      h.succeed();
    });
  }
}
