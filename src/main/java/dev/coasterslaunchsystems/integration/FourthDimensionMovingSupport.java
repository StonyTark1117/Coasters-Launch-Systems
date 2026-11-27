package dev.coasterslaunchsystems.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Optional native dock authentication; never permits forces on a moving-host rail. */
public final class FourthDimensionMovingSupport {
  private FourthDimensionMovingSupport() {}
  public static boolean fixedDock(ServerLevel level,BlockPos from,BlockPos to) {
    try {
      var api=Class.forName("dev.fourthdimension.integration.RideAuthority");
      return api.getField("EXTERNAL_MOVING_API").getInt(null)==1
          &&(boolean)api.getMethod("movingFixedEdge",ServerLevel.class,BlockPos.class,BlockPos.class).invoke(null,level,from,to);
    } catch(ReflectiveOperationException | LinkageError unavailable) { return false; }
  }
}
