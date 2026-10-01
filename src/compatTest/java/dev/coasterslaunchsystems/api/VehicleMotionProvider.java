package dev.coasterslaunchsystems.api;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
/** Historical API-4 shape, compiled independently; never packaged as a runtime API. */
public interface VehicleMotionProvider {
  int API_VERSION = 4;
  String id();
  default int apiVersion() { return API_VERSION; }
  default boolean requestDispatch(ServerLevel level, UUID chassis) { return false; }
  Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis);
}
