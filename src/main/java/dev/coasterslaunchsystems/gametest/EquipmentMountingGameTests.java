package dev.coasterslaunchsystems.gametest;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.coasterslaunchsystems.track.DirectedTrack;
import dev.coasterslaunchsystems.track.EquipmentMounting;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlock;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.List;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("coasterslaunchsystems_mounting")
@PrefixGameTestTemplate(false)
public final class EquipmentMountingGameTests {
  @GameTest(template = "empty", timeoutTicks = 60)
  public static void ordinaryReachAndRestoredDeepBindingsRequireLiveDock(GameTestHelper h) {
    h.assertTrue(!ModList.get().isLoaded("fourthdimension"), "Run this ordinary mounting regression without Fourth Dimension");
    var level = h.getLevel();
    var from = new BlockPos(160, 104, -256);
    var to = from.south(64);
    for (int x = 9; x <= 11; x++) for (int z = -17; z <= -11; z++) level.setChunkForced(x, z, true);
    // Match FD's raw socket endpoints with worldCenterBase: worldCenter adds 0.5m
    // regardless of anchor facing. Keep the explicit level, unbanked frame below.
    var anchor = SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState()
        .setValue(CoasterAnchorpointBlock.FACING, Direction.UP);
    level.setBlockAndUpdate(from, anchor);
    level.setBlockAndUpdate(to, anchor);
    var curve = new BezierConnection(Couple.create(from, to),
        Couple.create(CoasterAnchorpointBlockEntity.worldCenterBase(level, from), CoasterAnchorpointBlockEntity.worldCenterBase(level, to)),
        Couple.create(new Vec3(0, 0, 1), new Vec3(0, 0, -1)),
        Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)), true, false, CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level, to, curve);
    var types = List.of(EquipmentContent.TIRE.get(), EquipmentContent.FIN.get(), EquipmentContent.LSM.get(), EquipmentContent.LIM.get());
    h.runAtTickTime(10, () -> {
      var track = DirectedTrack.resolve(level, from, to).orElseThrow();
      for (int i = 0; i < types.size(); i++) for (int side : new int[] {-1, 1}) {
        var shallowPos = from.offset(side, -3, 8 + 12 * i);
        var deepPos = shallowPos.below();
        var state = types.get(i).defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        level.setBlockAndUpdate(shallowPos, state);
        level.setBlockAndUpdate(deepPos, state);
        var shallow = (EquipmentCabinetEntity) level.getBlockEntity(shallowPos);
        var deep = (EquipmentCabinetEntity) level.getBlockEntity(deepPos);
        var mechanism = shallow.mechanism();
        var label = mechanism + " side=" + side;
        var nearDelta = CoasterPathTrackFrame.closestHitOnEdge(level, track.edge(), shallowPos.getCenter())
            .point().subtract(shallowPos.getCenter());
        var deepDelta = CoasterPathTrackFrame.closestHitOnEdge(level, track.edge(), deepPos.getCenter())
            .point().subtract(deepPos.getCenter());
        h.assertTrue(EquipmentMounting.withinReach(mechanism, nearDelta.x, nearDelta.y, nearDelta.z, false),
            "Ordinary y-3 mounting geometry changed: " + label + " delta=" + nearDelta);
        h.assertTrue(!EquipmentMounting.withinReach(mechanism, deepDelta.x, deepDelta.y, deepDelta.z, false),
            "Deep y-4 mounting fits ordinary reach unexpectedly: " + label + " delta=" + deepDelta);
        // Geometry only: true is not a native permission and is never supplied to bind/resolve.
        h.assertTrue(EquipmentMounting.withinReach(mechanism, deepDelta.x, deepDelta.y, deepDelta.z, true),
            "Actual closest-hit residual exceeds deep mounting geometry: " + label + " delta=" + deepDelta);

        shallow.bind(level, from, to);
        var id = shallow.identity();
        h.assertTrue(shallow.resolve(level) != null && shallow.resolve(level) != null,
            "Ordinary mounting or its cached resolution failed: " + label);
        shallow.setArmed(true);
        h.assertTrue(shallow.armed(), "Ordinary mounting could not arm: " + label);
        shallow.setArmed(false);
        level.setBlockAndUpdate(shallowPos, state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
        denied(h, () -> shallow.bind(level, from, to), "transverse shaft", "longitudinal shaft " + label);
        level.setBlockAndUpdate(shallowPos, state);
        shallow.bind(level, from, to);
        h.assertTrue(shallow.identity().equals(id), "Orientation inspection replaced cabinet identity: " + label);
        var saved = shallow.saveWithFullMetadata(level.registryAccess());
        var interval = shallow.interval();
        var deepId = deep.identity();
        denied(h, () -> deep.bind(level, from, to), "within three blocks", "unauthenticated deep bind " + label);
        h.assertTrue(deep.identity().equals(deepId) && !deep.armed(), "Rejected deep binding changed identity or armed state: " + label);

        // Move only saved cabinet state. No controller, vehicle, authority or native dock is synthesized.
        level.removeBlock(shallowPos, false);
        deep.loadWithComponents(saved.copy(), level.registryAccess());
        h.assertTrue(deep.resolve(level) == null && deep.resolve(level) == null,
            "Restored deep binding acquired a usable force path, including cached resolve: " + label);
        denied(h, () -> deep.setArmed(true), "unavailable track", "restored deep rearm " + label);
        h.assertTrue(!deep.armed() && deep.identity().equals(id) && deep.interval().equals(interval),
            "Deep refusal lost the saved identity/contact or armed the module: " + label);
        bindingUnchanged(h, saved, deep.saveWithFullMetadata(level.registryAccess()), label);

        // An already-armed save must still fail the runtime's resolve gate on every read.
        var armedSave = saved.copy();
        armedSave.putBoolean("ls_armed", true);
        deep.loadWithComponents(armedSave, level.registryAccess());
        h.assertTrue(deep.armed() == !mechanism.linear() && deep.resolve(level) == null && deep.resolve(level) == null,
            "Armed-save restore changed linear disarming or bypassed the current mounting check: " + label);
        bindingUnchanged(h, saved, deep.saveWithFullMetadata(level.registryAccess()), label);
        deep.setArmed(false);
        System.out.println("LS_MOUNTING PASS " + label + " ordinaryDelta=" + nearDelta + " deepDelta=" + deepDelta
            + " geometryOnlyDockReach=true nativeDockAbsent=true cachedResolveDenied=true identity=" + id);
        level.removeBlock(deepPos, false);
      }
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).removeAnchorPeerCurve(level, to);
      level.removeBlock(from, false);
      level.removeBlock(to, false);
      h.succeed();
    });
  }

  private static void bindingUnchanged(GameTestHelper h, CompoundTag before, CompoundTag after, String label) {
    for (var key : List.of("ls_identity", "ls_from", "ls_to", "ls_geometry", "ls_start", "ls_end", "ls_axis"))
      h.assertTrue(before.get(key) != null && before.get(key).equals(after.get(key)), "Rejected mounting changed " + key + ": " + label);
  }

  private static void denied(GameTestHelper h, Runnable action, String message, String claim) {
    try { action.run(); }
    catch (IllegalArgumentException expected) {
      h.assertTrue(expected.getMessage().contains(message), "Wrong rejection for " + claim + ": " + expected.getMessage());
      return;
    }
    h.fail("Accepted " + claim);
  }
}
