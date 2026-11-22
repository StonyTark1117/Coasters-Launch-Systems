package dev.coasterslaunchsystems.gametest;

import java.util.List;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Focus the existing loaded pressure fixtures without replaying unrelated cable cases. */
@GameTestHolder("coasterslaunchsystems_pressure")
public final class PressureGameTests {
  @GameTestGenerator public static List<TestFunction> pressureContracts() {
    return CableControllerGameTests.ride().stream()
        .filter(test -> test.testName().contains("_hydraulic_") || test.testName().contains("_pneumatic_"))
        .map(test -> new TestFunction(test.batchName(),
            test.testName().replace("coasterslaunchsystems_cable.", "coasterslaunchsystems_pressure."),
            "coasterslaunchsystems_pressure:empty", test.maxTicks(), test.setupTicks(), test.required(), test.function()))
        .toList();
  }
}
