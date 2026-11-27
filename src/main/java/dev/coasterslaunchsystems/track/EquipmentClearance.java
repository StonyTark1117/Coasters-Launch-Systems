package dev.coasterslaunchsystems.track;

import dev.coasterslaunchsystems.math.Mechanism;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Minimum fixed-world running clearance at a tire, fin or magnetic contact. */
public final class EquipmentClearance {
  private EquipmentClearance() {}

  /**
   * Reserve the bearing corridor, not an arbitrary player's complete vehicle envelope.
   * Floor supports remain below it. Real collision shapes allow harmless decorations
   * and partial blocks outside the corridor; unloaded cells cannot certify clearance.
   */
  public static void validate(ServerLevel level, DirectedTrack track, double start, double end,
      Mechanism mechanism) {
    if (mechanism != Mechanism.TIRE && mechanism != Mechanism.FIN_BRAKE
        && mechanism != Mechanism.EDDY_BRAKE) return;
    boolean movingDock=dev.coasterslaunchsystems.integration.FourthDimensionMovingSupport.fixedDock(level,track.edge().from(),track.edge().to());
    int segments = Math.max(1, (int) Math.ceil((end - start) / .125));
    Vec3 previous = track.hitAtDistance(level, start).point();
    for (int i = 1; i <= segments; i++) {
      Vec3 next = track.hitAtDistance(level, start + (end - start) * i / segments).point();
      // Include the swept interval between samples, including reverse-directed spans.
      var bounds = new AABB(Math.min(previous.x, next.x) - .6,
          Math.min(previous.y, next.y) + .05, Math.min(previous.z, next.z) - .6,
          Math.max(previous.x, next.x) + .6, Math.max(previous.y, next.y) + 1.05,
          Math.max(previous.z, next.z) + .6);
      for (var pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
          BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
        if (!level.hasChunkAt(pos))
          throw new IllegalArgumentException("Equipment running clearance unavailable at " + pos.toShortString());
        if(movingDock&&(pos.equals(track.edge().from())||pos.equals(track.edge().to()))
            &&level.getBlockEntity(pos) instanceof dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)continue;
        for (var box : level.getBlockState(pos).getCollisionShape(level, pos).toAabbs())
          if (bounds.intersects(box.move(pos)))
            throw new IllegalArgumentException("Obstructed equipment running clearance at " + pos.toShortString());
      }
      previous = next;
    }
  }
}
