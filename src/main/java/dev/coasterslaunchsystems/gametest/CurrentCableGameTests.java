package dev.coasterslaunchsystems.gametest;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Affected current-provider catch and retained safety-lease checks. */
@GameTestHolder("coasterslaunchsystems_cable_current")
public final class CurrentCableGameTests {
  @GameTestGenerator public static List<TestFunction> current() {
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) return List.of();
    var tests = List.of(CableControllerGameTests.currentCase("arrow_controller_dispatch", 0),
        CableControllerGameTests.currentCase("arrow_controller_power_loss", 1),
        new TestFunction("launchsystems_cable_current", "coasterslaunchsystems_cable_current.brake_lease",
            "coasterslaunchsystems_cable_current:empty", 1200, 0, true, helper -> {
              var scene = new RideControllerGameTests.Scene(helper, 2, "arrow_forward");
              RideControllerGameTests.run(scene);
              scene.at(90, () -> verifyBrakeLease(scene));
            }));
    return Boolean.getBoolean("coasterslaunchsystems.brakeLeaseOnly")
        ? tests.stream().filter(t -> t.testName().endsWith("brake_lease")).toList() : tests;
  }

  private static void verifyBrakeLease(RideControllerGameTests.Scene scene) {
    try {
      var registry = dev.coasterslaunchsystems.api.MotionProviders.class.getDeclaredField("PROVIDERS");
      registry.setAccessible(true);
      var provider = ((List<?>) registry.get(null)).stream()
          .filter(p -> p.getClass().getSimpleName().equals("FourthDimensionMotionProvider")).findFirst().orElseThrow();
      var bridgeField = provider.getClass().getDeclaredField("authority"); bridgeField.setAccessible(true);
      var bridge = bridgeField.get(provider);
      var check = bridge.getClass().getDeclaredMethod("retainsBrakeLease", ServerLevel.class, Object.class, Set.class);
      check.setAccessible(true);
      var vehicle = scene.vehicles.getFirst();
      var state = FourthDimensionForceGameTests.read(vehicle, "authority");
      var members = Set.copyOf(scene.controller.reservation());
      java.util.function.BooleanSupplier permitted = () -> {
        try { return (boolean) check.invoke(bridge, scene.base.level, vehicle, members); }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
      };
      var suspended = state.getClass().getField("suspended"); var savedSuspension = suspended.get(state);
      suspended.set(state, true);
      try {
      scene.base.helper.assertTrue(permitted.getAsBoolean(), "Exact retained native brake lease was denied");
      for (var fieldName : List.of("provider", "owner", "cars")) {
        var field = state.getClass().getField(fieldName); var saved = field.get(state);
        try {
          field.set(state, fieldName.equals("provider") ? "foreign:provider"
              : fieldName.equals("owner") ? UUID.randomUUID() : List.of(UUID.randomUUID()));
          scene.base.helper.assertTrue(!permitted.getAsBoolean(), "Brake lease accepted mismatched " + fieldName);
        } finally { field.set(state, saved); }
      }
      var reservationField = scene.controller.getClass().getDeclaredField("reservation"); reservationField.setAccessible(true);
      @SuppressWarnings("unchecked") var reservation = (Set<UUID>) reservationField.get(scene.controller);
      try {
        reservation.clear();
        scene.base.helper.assertTrue(!permitted.getAsBoolean(), "Brake lease accepted a missing local reservation");
      } finally { reservation.addAll(members); }
      } finally { suspended.set(state, savedSuspension); }
      System.out.println("LS_CURRENT_BRAKE_LEASE exact owner accepted; foreign provider, replaced identity, changed consist and missing reservation denied");
    } catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
  }
}
