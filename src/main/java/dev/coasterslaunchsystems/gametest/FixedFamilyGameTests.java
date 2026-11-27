package dev.coasterslaunchsystems.gametest;

import dev.coasterslaunchsystems.math.Mechanism;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Exact catalog variants reuse the accepted finite work, rider, capture and service contracts. */
@GameTestHolder("coasterslaunchsystems_fixed")
public final class FixedFamilyGameTests {
  @GameTestGenerator public static List<TestFunction> families() {
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension")) return List.of();
    var tests = new ArrayList<TestFunction>(); int slot = 0;
    var selection = System.getenv("LS_FIXED_CASES");
    for (var family : List.of("wing", "fixed_inverted", "classic_spinning", "s2_spinning")) {
      for (var kind : List.of("tire", "lsm", "lim", "cable", "skid", "eddy")) {
        String name = family + "_" + kind;
        if (family.equals("fixed_inverted") && kind.equals("cable")) continue;
        if (selection == null ? family.equals("s2_spinning") : !Arrays.asList(selection.split(",")).contains(name)) continue;
        int index = slot++;
        TestFunction test;
        if ("true".equals(System.getenv("LS_FIXED_PLAYER")) && kind.equals("tire")) test = player(family, index);
        else if (kind.equals("skid") || kind.equals("eddy")) test = passive(family, kind, index);
        else if (kind.equals("cable")) test = CableControllerGameTests.currentCase(family + "_controller_dispatch", index);
        else if (kind.equals("tire")) test = new TestFunction("launchsystems_fixed", "coasterslaunchsystems_fixed." + name,
            "coasterslaunchsystems_fixed:empty", 1300, 0, true, h -> RideControllerGameTests.run(new RideControllerGameTests.Scene(h, index, family + "_forward")));
        else test = LinearControllerGameTests.catalogCase(family + "_forward", index, Mechanism.valueOf(kind.toUpperCase(Locale.ROOT)));
        tests.add(new TestFunction("launchsystems_fixed", "coasterslaunchsystems_fixed." + name,
            "coasterslaunchsystems_fixed:empty", test.maxTicks(), test.setupTicks(), true, test.function()));
      }
    }
    if (selection != null && Arrays.asList(selection.split(",")).contains("contact_boundary"))
      tests.add(new TestFunction("launchsystems_fixed", "coasterslaunchsystems_fixed.contact_boundary",
          "coasterslaunchsystems_fixed:empty", 300, 0, true, h -> {
            var scene = new RideControllerGameTests.Scene(h, 0, "fixed_inverted_forward");
            var enabled = new boolean[1];
            h.runAtTickTime(200, () -> { scene.track = dev.coasterslaunchsystems.track.DirectedTrack.resolve(scene.base.level, scene.base.from, scene.base.to).orElseThrow(); enabled[0] = true; });
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
                (dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent event) -> {
              if (!enabled[0] || scene.closed || event.getPhysicsSystem().getLevel() != scene.base.level || scene.base.parents.isEmpty()) return;
              if (dev.coasterslaunchsystems.track.EngagedContacts.current(scene.base.level, scene.base.parents.getFirst()).isEmpty()) return;
              try {
                var body = scene.base.parents.getFirst();
                var point = dev.coasterslaunchsystems.runtime.VehicleInterfaces.tirePoint(body);
                var hit = dev.coasterslaunchsystems.runtime.VehicleInterfaces.contactHit(scene.base.level, scene.track, body, point);
                var bearing = dev.coasterslaunchsystems.track.EngagedContacts.current(scene.base.level, body).orElseThrow().hit();
                h.assertTrue(hit != null && Math.abs(scene.track.distance(hit) - scene.track.distance(bearing) - 5) < .03,
                    "Translated physical interface did not project five metres ahead");
                var remote = new dev.coasterslaunchsystems.math.SpanInterval(scene.track.distance(bearing) - .5, scene.track.distance(bearing) + .5);
                h.assertTrue(!remote.intersects(scene.track.distance(hit), scene.track.distance(hit), .6),
                    "Bearing-only span remotely contacted translated interface");
                h.assertTrue(dev.coasterslaunchsystems.runtime.VehicleInterfaces.contactHit(scene.base.level, scene.track, body,
                    new org.joml.Vector3d(point).add(0, 0, -80)) == null,
                    "Point beyond accepted edge endpoint was admitted");
                System.out.println("LS_CONTACT_BOUNDARY actual=" + scene.track.distance(hit) + " bearing=" + scene.track.distance(bearing) + " endpoint=denied");
                scene.cleanup(); h.succeed();
              } catch (RuntimeException failure) { scene.cleanup(); h.fail(failure.toString()); }
            });
          }));
    return tests;
  }
  private static TestFunction player(String family, int slot) {
    return new TestFunction("launchsystems_fixed", "coasterslaunchsystems_fixed." + family + "_tire",
        "coasterslaunchsystems_fixed:empty", 400, 0, true, h -> {
      var s = new RideControllerGameTests.Scene(h, slot, family + "_forward");
      h.runAtTickTime(200, () -> {
        s.install(false);
        System.out.println("LS_FIXED_PLAYER prepared family=" + family + " from=" + s.base.from
            + " controller=" + s.controllerPosition + " chassis=" + s.base.parents.getFirst().getUniqueId());
        h.succeed();
      });
    });
  }
  private static TestFunction passive(String family, String kind, int slot) {
    return new TestFunction("launchsystems_fixed", "coasterslaunchsystems_fixed." + family + "_" + kind,
        "coasterslaunchsystems_fixed:empty", 1100, 0, true, h -> {
      var s = new RideControllerGameTests.Scene(h, slot, family + "_forward");
      var mechanism = kind.equals("skid") ? Mechanism.SKID_BRAKE : Mechanism.EDDY_BRAKE;
      var cabinet = new dev.coasterslaunchsystems.content.EquipmentCabinetEntity[1];
      var heat = new double[1];
      h.runAtTickTime(150, () -> {
        var body = s.base.parents.getFirst();
        var vehicle = s.vehicles.getFirst();
        var cert = dev.coasterslaunchsystems.integration.FourthDimensionCertificates.find(
            ((Enum<?>) FourthDimensionForceGameTests.read(vehicle, "family")).name(), (String) FourthDimensionForceGameTests.read(vehicle, "variant"));
        var plot = body.getPlot().getEmbeddedLevelAccessor(); var position = cert.offset(mechanism);
        if (position.getZ() < -5) {
          for (int y : new int[] {-1}) {
            var brace = new net.minecraft.core.BlockPos(0, y, position.getZ());
            if (plot.getBlockState(brace).isAir()) plot.setBlock(brace, net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(), 3);
          }
        }
        h.assertTrue(plot.getBlockState(position).isAir(), "Passive interface would replace native component");
        plot.setBlock(position, (kind.equals("skid") ? dev.coasterslaunchsystems.content.EquipmentContent.SKID_SHOE : dev.coasterslaunchsystems.content.EquipmentContent.CONDUCTIVE_FIN).get().defaultBlockState(), 3);
        s.boardFourthDimension();
      });
      h.runAtTickTime(180, () -> {
        s.base.level.setBlockAndUpdate(s.controllerPosition, dev.coasterslaunchsystems.content.EquipmentContent.CONTROLLER.get().defaultBlockState());
        s.controller = (dev.coasterslaunchsystems.content.RideControllerEntity) s.base.level.getBlockEntity(s.controllerPosition);
        s.controller.bind(s.base.level, s.base.from, s.base.to);
        for (int d = 8; d <= 16; d += 2) { s.module(d, false); s.module(d, true); }
        for (int d = 29; d <= 51; d += 2) s.module(d, true);
        s.track = dev.coasterslaunchsystems.track.DirectedTrack.resolve(s.base.level, s.base.from, s.base.to).orElseThrow();
        var pos = s.base.from.offset(kind.equals("skid") ? 1 : -1, s.cabinetY, kind.equals("skid") ? 21 : 20);
        s.base.level.setBlockAndUpdate(pos, (kind.equals("skid") ? dev.coasterslaunchsystems.content.EquipmentContent.SKID : dev.coasterslaunchsystems.content.EquipmentContent.EDDY).get().defaultBlockState()
            .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X));
        cabinet[0] = (dev.coasterslaunchsystems.content.EquipmentCabinetEntity) s.base.level.getBlockEntity(pos);
        cabinet[0].bind(s.base.level, s.base.from, s.base.to); cabinet[0].setArmed(true); s.equipment.add(pos);
        s.controller.setArmed(true);
      });
      h.runAtTickTime(220, () -> { h.assertTrue(s.controller.phase() == dev.coasterslaunchsystems.content.RideControllerEntity.State.READY, s.controller.status()); s.high(true); });
      net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
          (dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent event) -> {
        if (!s.closed && event.getPhysicsSystem().getLevel() == s.base.level && cabinet[0] != null) heat[0] = Math.max(heat[0], cabinet[0].heat());
      });
      h.runAtTickTime(1000, () -> {
        try {
          h.assertTrue(heat[0] > .001, "No physical passive contact work: " + family + " " + kind + " " + s.controller.status());
          h.assertTrue(s.base.riders.stream().allMatch(net.minecraft.world.entity.Entity::isPassenger), "Passive braking lost native rider");
          h.assertTrue(s.controller.reservation().contains(s.base.parents.getFirst().getUniqueId()), "Passive braking lost lease");
          System.out.println("LS_FIXED_PASSIVE family=" + family + " kind=" + kind + " heat=" + heat[0] + " riders=" + s.base.riders.size());
          s.cleanup(); h.succeed();
        } catch (RuntimeException failure) { s.cleanup(); throw failure; }
      });
    });
  }
}
