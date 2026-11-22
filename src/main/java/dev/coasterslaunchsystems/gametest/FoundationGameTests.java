package dev.coasterslaunchsystems.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Reuse accepted native fixtures for only the contracts touched by the core migration. */
@GameTestHolder("coasterslaunchsystems_foundation")
public final class FoundationGameTests {
  @GameTestGenerator public static List<TestFunction> currentCoreContracts() {
    var tests = new ArrayList<TestFunction>();
    tests.add(new TestFunction("launchsystems_foundation", "coasterslaunchsystems_foundation.ordinary_mass_recovery",
        "coasterslaunchsystems_foundation:empty", 320, 0, true, OrdinaryPassengerGameTests::nativePassengerMassLifecycle));
    var cables = Set.of("coasterslaunchsystems_cable.reload_active", "coasterslaunchsystems_cable.mass_changed");
    CableRigGameTests.mechanics().stream().filter(test -> cables.contains(test.testName())).map(test -> new TestFunction(test.batchName(), test.testName(), "coasterslaunchsystems_foundation:empty", test.maxTicks(), test.setupTicks(), test.required(), test.function())).forEach(tests::add);
    CableControllerGameTests.ride().stream().filter(test -> test.testName().equals("coasterslaunchsystems_cable.ordinary_controller_dispatch")).map(test -> new TestFunction(test.batchName(), test.testName(), "coasterslaunchsystems_foundation:empty", test.maxTicks(), test.setupTicks(), test.required(), test.function())).forEach(tests::add);
    return tests;
  }
}
