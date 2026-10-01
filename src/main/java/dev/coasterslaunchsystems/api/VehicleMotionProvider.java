package dev.coasterslaunchsystems.api;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** Version 4 providers describe native articulated bodies; they never create a second physics solver. */
public interface VehicleMotionProvider {
  int API_VERSION = 4;
  String id();
  default int apiVersion() { return API_VERSION; }
  default boolean requestDispatch(ServerLevel level, UUID chassis) { return false; }
  Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis);
}
