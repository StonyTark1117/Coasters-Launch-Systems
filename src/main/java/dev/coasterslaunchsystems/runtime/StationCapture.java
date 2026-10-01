package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.CaptureState;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.content.RideControllerEntity;
import net.minecraft.server.level.ServerLevel;

/** Vehicle-owned alignment is requested only after local whole-train hold. Reservations never clear here. */
public final class StationCapture {
  private StationCapture() {}
  public static boolean vehicleHeld(ServerLevel level, java.util.UUID chassis) {
    var state = MotionProviders.captureState(level, chassis);
    return state == CaptureState.READY || state == CaptureState.ALIGNING;
  }
  public static boolean evaluate(ServerLevel level, RideControllerEntity controller, boolean held) {
    if (!controller.stationCapture()) return false;
    if (!held || controller.reservation().isEmpty()) { controller.boardingReady(false); return false; }
    var member = controller.reservation().iterator().next(); var state = MotionProviders.captureState(level, member);
    if (state == CaptureState.RELEASED) {
      controller.boardingReady(false);
      if (!MotionProviders.requestCapture(level, member, controller.reservation())) controller.fail("Vehicle provider rejected whole-train station capture; reservation retained");
      else controller.state(RideControllerEntity.State.CAPTURING);
      controller.consumeRequest(); return true;
    }
    if (state == CaptureState.DENIED || state == CaptureState.UNSUPPORTED) {
      controller.fail("Vehicle provider cannot grant station capture; reservation retained"); controller.consumeRequest(); return true;
    }
    if (state == CaptureState.ALIGNING) {
      controller.boardingReady(false); controller.state(RideControllerEntity.State.CAPTURING); controller.consumeRequest(); return true;
    }
    controller.boardingReady(true);
    boolean ready = controller.reservation().stream().allMatch(id -> MotionProviders.resolve(level, id).map(m -> m.dispatchReady()).orElse(false));
    if (!ready) { controller.state(RideControllerEntity.State.BOARDING); controller.consumeRequest(); return true; }
    return false;
  }
}
