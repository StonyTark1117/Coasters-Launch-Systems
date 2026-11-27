package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.read;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import dev.coasterslaunchsystems.runtime.CallbackTiming;
import dev.coasterslaunchsystems.runtime.NativeForces;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** One declared integrated workload; no performance budget or gameplay timing dependency. */
@GameTestHolder("coasterslaunchsystems_performance")
public final class PerformanceGameTests {
  @GameTestGenerator public static List<TestFunction> representativeRoute() {
    if (!CallbackTiming.ENABLED || !ModList.get().isLoaded("fourthdimension")) return List.of();
    return List.of(new TestFunction("launchsystems_performance", "coasterslaunchsystems_performance.loaded_route",
        "coasterslaunchsystems_performance:empty", 3400, 0, true, h -> {
      NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RouteInteropGameTests::observe);
      var scene = new RouteInteropGameTests.Scene(h, "physical_conflict");
      boolean[] measuring = {false};
      var description = new LinkedHashMap<String, Object>();
      h.runAtTickTime(400, () -> {
        h.assertTrue(scene.source.phase() == RideControllerEntity.State.READY, "Performance scene did not settle before sampling");
        description.put("settling_ticks", 400);
        description.put("settling_after_equipment_install_ticks", 220);
        description.put("controllers", 2);
        description.put("route_edges", 8);
        description.put("route_reservation_blocks", 6);
        description.put("route_edge_lengths_blocks", scene.tracks.stream().map(t -> t.length()).toList());
        description.put("forced_chunks", scene.chunks.size());
        description.put("trains", scene.cars.size());
        description.put("synthetic_native_seated_riders", scene.riders.size());
        description.put("native_bodies", scene.cars.stream().mapToInt(c -> 1 + ((List<?>) read(c, "carriers")).size()).sum());
        description.put("loaded_train_masses_native_units", scene.cars.stream().map(c -> NativeForces.mass(scene.level,
            MotionProviders.resolve(scene.level, (UUID) read(c, "chassis")).orElseThrow())).toList());
        var cabinets = new TreeMap<String, Integer>();
        for (var p : scene.placed) if (scene.level.getBlockEntity(p) instanceof EquipmentCabinetEntity cabinet)
          cabinets.merge(cabinet.mechanism().name(), 1, Integer::sum);
        description.put("equipment", cabinets);
        description.put("creative_shaft_sources", cabinets.values().stream().mapToInt(Integer::intValue).sum());
        description.put("origin", scene.origin.toShortString());
        CallbackTiming.start(scene.level); measuring[0] = true;
      });
      h.onEachTick(() -> {
        if (!measuring[0]) return;
        try {
          CallbackTiming.phase(scene.stage < 2 ? "local_launch_capture" : scene.stage == 2 ? "route_transfer" : "conflict_hold");
          if (scene.stage == 3 && scene.conflictTicks == 39) {
            var timing = CallbackTiming.finish(scene.level); measuring[0] = false;
            description.put("timing", timing);
            description.put("positive_queued_work_native_units", scene.work);
            description.put("positive_force_steps", scene.driveSteps);
            description.put("peak_native_speed_blocks_per_second", scene.peakSpeed);
            h.assertTrue(((Number) timing.get("dropped_samples")).longValue() == 0, "Timing capacity truncated the declared scene");
            h.assertTrue(((Number) ((Map<?, ?>) timing.get("all")).get("samples")).longValue() > 0, "Timing collector received no callbacks");
            System.out.println("LS_PERFORMANCE " + new com.google.gson.Gson().toJson(description));
          }
          // The original physical work, route-conflict, rider and retained-owner assertions still decide success.
          scene.tick();
        } catch (RuntimeException failure) {
          if (measuring[0]) { CallbackTiming.finish(scene.level); measuring[0] = false; }
          scene.cleanup(); throw failure;
        }
      });
    }));
  }
}
