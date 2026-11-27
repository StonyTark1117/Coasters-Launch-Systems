package dev.coasterslaunchsystems.gametest;

import static dev.coasterslaunchsystems.gametest.FourthDimensionForceGameTests.read;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.NativeForces;
import dev.coasterslaunchsystems.api.MotionProviders;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Authored release demonstration rigs; actual players own boarding and every dispatch. */
@GameTestHolder("coasterslaunchsystems_release_occupied")
@net.neoforged.fml.common.EventBusSubscriber(modid = "coasterslaunchsystems")
public final class ReleaseOccupiedGameTests {
  private record Run(RideControllerGameTests.Scene ride, Supplier<Map<String, Object>> measurements) {}
  private static final Map<String, Run> RUNS = new LinkedHashMap<>();
  static boolean enabled() {
    return Boolean.getBoolean("coasterslaunchsystems.validation") && "true".equals(System.getenv("LS_RELEASE_PLAYER"));
  }
  static boolean refresh(RideControllerGameTests.Scene ride) {
    var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(ride.base.level);
    for (int i = 0; i < ride.base.parents.size(); i++) {
      var current = container.getSubLevel(ride.base.parents.get(i).getUniqueId());
      if (!(current instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel body)) return false;
      ride.base.parents.set(i, body);
    }
    return true;
  }
  static void register(String name, RideControllerGameTests.Scene ride, Supplier<Map<String, Object>> measurements) {
    RUNS.put(name, new Run(ride, measurements));
    System.out.println("LS_RELEASE_OCCUPIED prepared=" + name + " controller=" + ride.controllerPosition
        + " chassis=" + ride.base.parents.getFirst().getUniqueId());
  }
  @GameTestGenerator public static List<TestFunction> rigs() {
    if (!enabled() || !net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) return List.of();
    return List.of(LinearControllerGameTests.playerCase("lsm", 0, Mechanism.LSM),
        LinearControllerGameTests.playerCase("lim", 1, Mechanism.LIM),
        CableControllerGameTests.playerCase("hydraulic", 2), CableControllerGameTests.playerCase("pneumatic", 3));
  }
  @net.neoforged.bus.api.SubscribeEvent public static void commands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
    if (!enabled()) return;
    event.getDispatcher().register(net.minecraft.commands.Commands.literal("lsrelease")
        .requires(source -> source.hasPermission(2))
        .then(net.minecraft.commands.Commands.literal("status").executes(ctx -> {
          for (var entry : RUNS.entrySet()) {
            var r = entry.getValue().ride(); if (!refresh(r)) continue; var v = r.vehicles.getFirst(); var level = r.base.level;
            var result = new LinkedHashMap<String, Object>();
            result.put("mechanism", entry.getKey()); result.put("controller", r.controllerPosition.toShortString());
            result.put("state", r.controller.status()); result.put("controllerNbt", r.controller.saveWithFullMetadata(level.registryAccess()).toString());
            result.put("chassis", read(v, "chassis")); result.put("nativeFault", read(v, "fault"));
            result.put("restraints", read(v, "restraints")); result.put("stopping", read(v, "stopping"));
            result.put("carriers", ((List<?>) read(v, "carriers")).stream().map(c -> Map.of("body", read(c, "body"),
                "seats", read(c, "seats").toString(), "occupants", read(c, "occupants").toString(), "capture", read(c, "capture"))).toList());
            result.put("bodies", r.base.parents.stream().map(b -> Map.of("id", b.getUniqueId(), "position", b.logicalPose().position().toString(),
                "speed", r.base.speed(b), "mass", NativeForces.mass(level, MotionProviders.resolve(level, b.getUniqueId()).orElseThrow()))).toList());
            result.put("equipment", r.equipment.stream().map(p -> {
              var c = (EquipmentCabinetEntity) level.getBlockEntity(p);
              return Map.of("position", p.toShortString(), "mechanism", c.mechanism(), "nbt", c.saveWithFullMetadata(level.registryAccess()).toString());
            }).toList());
            result.put("measurements", entry.getValue().measurements().get());
            result.put("players", level.players().stream().map(p -> Map.of("name", p.getName().getString(), "riding", p.isPassenger(),
                "seat", p.getRootVehicle().getUUID())).toList());
            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(new com.google.gson.Gson().toJson(result)), false);
          }
          return RUNS.size();
        })));
  }
}
