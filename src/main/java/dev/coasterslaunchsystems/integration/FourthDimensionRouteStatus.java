package dev.coasterslaunchsystems.integration;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** A view of FD's current safety decision; native reconciliation owns every route reservation. */
public record FourthDimensionRouteStatus(boolean managed, boolean permitted, UUID owner,
    int currentBlock, int nextBlock, String reason, boolean freshRequestRequired) {
  public static FourthDimensionRouteStatus query(ServerLevel level, UUID chassis) {
    try {
      var api = Class.forName("dev.fourthdimension.integration.RideAuthority");
      if (api.getField("EXTERNAL_ROUTE_API").getInt(null) != 1)
        return unavailable();
      var status = api.getMethod("routeStatus", ServerLevel.class, UUID.class).invoke(null, level, chassis);
      return new FourthDimensionRouteStatus((boolean) read(status, "managed"), (boolean) read(status, "permitted"),
          (UUID) read(status, "owner"), (int) read(status, "currentBlock"), (int) read(status, "nextBlock"),
          (String) read(status, "reason"), (boolean) read(status, "freshRequestRequired"));
    } catch (ClassNotFoundException | NoSuchFieldException absent) {
      return unavailable();
    } catch (ReflectiveOperationException | RuntimeException unavailable) {
      return new FourthDimensionRouteStatus(true, false, null, -1, -1,
          "Fourth Dimension route status unavailable; ownership retained", true);
    }
  }
  private static Object read(Object value, String name) throws ReflectiveOperationException {
    return value.getClass().getMethod(name).invoke(value);
  }
  private static FourthDimensionRouteStatus unavailable() {
    return new FourthDimensionRouteStatus(false, false, null, -1, -1,
        "Fourth Dimension route delegation API unavailable", true);
  }
}
