package dev.coasterslaunchsystems.gametest;

import java.util.List;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Small current-core bridge selection; retained controller force/rider assertions remain authoritative. */
@GameTestHolder("coasterslaunchsystems_authority")
public final class CurrentAuthorityGameTests {
  @GameTestGenerator public static List<TestFunction> nativeAuthority() {
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) return List.of();
    var tests = new java.util.ArrayList<>(List.of("arrow_forward", "free_spin_forward").stream().map(mode -> new TestFunction(
        "launchsystems_authority", "coasterslaunchsystems_authority." + mode,
        "coasterslaunchsystems_authority:empty", 1200, 0, true,
        helper -> {
          var scene = new RideControllerGameTests.Scene(helper, mode.startsWith("arrow") ? 0 : 1, mode);
          RideControllerGameTests.run(scene);
          scene.at(90, () -> {
            for (var nativeVehicle : scene.vehicles) {
              var id = (java.util.UUID) FourthDimensionForceGameTests.read(nativeVehicle, "chassis");
              var snapshot = dev.coasterslaunchsystems.api.MotionProviders.resolve(scene.base.level, id).orElseThrow();
              helper.assertTrue(snapshot.provider().equals("fourthdimension:authority1"), "Current vehicle fell back to an old/ordinary provider");
              var state = FourthDimensionForceGameTests.read(nativeVehicle, "authority");
              helper.assertTrue(FourthDimensionForceGameTests.read(state, "provider").equals("coasterslaunchsystems:local_controller")
                  && FourthDimensionForceGameTests.read(state, "owner").equals(scene.controller.identity()), "Native authority is not owned by the real local controller");
            }
          });
        })).toList());
    tests.add(new TestFunction("launchsystems_authority", "coasterslaunchsystems_authority.arrow_circuit",
        "coasterslaunchsystems_authority:empty", 7000, 0, true,
        helper -> CircuitGameTests.run(new CircuitGameTests.Scene(helper, 2, "arrow_forward"))));
    return tests;
  }
}
