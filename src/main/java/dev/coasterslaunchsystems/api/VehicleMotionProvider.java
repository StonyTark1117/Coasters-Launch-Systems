package dev.coasterslaunchsystems.api;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** Version 2 providers describe native articulated bodies; they never create a second physics solver. */
public interface VehicleMotionProvider {
  int API_VERSION = 2;
  String id();
  default int apiVersion() { return API_VERSION; }
  Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis);
}
