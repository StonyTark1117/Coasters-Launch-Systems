package dev.coasterslaunchsystems.api;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** Version 5 providers describe native articulated bodies; they never create a second physics solver. */
public interface VehicleMotionProvider {
  int API_VERSION = 5;
  String id();
  /** Implement explicitly so an old binary cannot inherit a newer contract version. */
  int apiVersion();
  default boolean requestDispatch(ServerLevel level, UUID chassis) { return false; }
  default boolean requestCapture(ServerLevel level, UUID chassis) { return false; }
  default CaptureState captureState(ServerLevel level, UUID chassis) { return CaptureState.UNSUPPORTED; }
  Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis);
}
