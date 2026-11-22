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
  /** Bind a complete local reservation before requesting native longitudinal authority. */
  default boolean reserveAuthority(ServerLevel level, UUID chassis, net.minecraft.core.BlockPos controller) { return true; }
  /** An explicit operator re-arm may acknowledge retained authority; this never dispatches. */
  default void resumeAuthority(ServerLevel level, net.minecraft.core.BlockPos controller) {}
  /** Return false while a native owner still requires a stopped handoff. */
  default boolean transferAuthority(ServerLevel level, UUID chassis, net.minecraft.core.BlockPos source,
      net.minecraft.core.BlockPos target) { return true; }
  Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis);
}
