package dev.coasterslaunchsystems.gametest;

import dev.coasterslaunchsystems.api.*;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("coasterslaunchsystems_routes")
@PrefixGameTestTemplate(false)
public final class StationApiGameTests {
  @GameTest(template = "empty", timeoutTicks = 20)
  public static void captureDefaultsDenyAndDispatchOnlyApiRequiresUpdate(GameTestHelper helper) {
    var provider = new VehicleMotionProvider() {
      public String id() { return "test:default_capture"; }
      public int apiVersion() { return API_VERSION; }
      public Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis) { return Optional.empty(); }
    };
    var chassis = new UUID(0, 1);
    helper.assertTrue(!provider.requestCapture(helper.getLevel(), chassis) && provider.captureState(helper.getLevel(), chassis) == CaptureState.UNSUPPORTED,
        "Provider defaults granted capture or boarding authority");
    var old = new VehicleMotionProvider() {
      public String id() { return "test:api4"; }
      public int apiVersion() { return 4; }
      public Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis) { return Optional.empty(); }
    };
    boolean primaryRejected = false, fallbackRejected = false;
    try { MotionProviders.register(old); } catch (IllegalArgumentException expected) { primaryRejected = true; }
    try { MotionProviders.registerOrdinaryFallback(old); } catch (IllegalArgumentException expected) { fallbackRejected = true; }
    helper.assertTrue(primaryRejected && fallbackRejected, "Old dispatch-only API registered with capture authority");
    try (var stream = StationApiGameTests.class.getResourceAsStream("/fixtures/api4/Api4ImplicitProvider.class")) {
      if (stream == null) throw new IllegalStateException("Missing compiled API-4 binary fixture");
      var bytes = stream.readAllBytes();
      class OldLoader extends ClassLoader {
        OldLoader() { super(VehicleMotionProvider.class.getClassLoader()); }
        VehicleMotionProvider provider() throws ReflectiveOperationException {
          return (VehicleMotionProvider) defineClass("dev.coasterslaunchsystems.compat.Api4ImplicitProvider", bytes, 0, bytes.length).getConstructor().newInstance();
        }
      }
      var binary = new OldLoader().provider(); boolean implicitRejected = false;
      try { MotionProviders.register(binary); } catch (IllegalArgumentException expected) { implicitRejected = expected.getCause() instanceof AbstractMethodError; }
      helper.assertTrue(implicitRejected, "An old binary inherited the new API version without an explicit declaration");
    } catch (java.io.IOException | ReflectiveOperationException failure) { throw new IllegalStateException("API binary compatibility fixture failed", failure); }
    System.out.println("LS_STATION_API defaults_deny=true api4_rejected=true implicit_api4_binary_rejected=true"); helper.succeed();
  }
}
