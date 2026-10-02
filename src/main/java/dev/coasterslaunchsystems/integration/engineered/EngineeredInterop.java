package dev.coasterslaunchsystems.integration.engineered;

import dev.jwaterfall.coastersengineered.controller.LinearMotorControllerIndex;
import dev.jwaterfall.coastersengineered.span.TrackSpanIndex;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import net.minecraft.server.level.ServerLevel;

/**
 * Version-pinned Coasters: Engineered 1.1.1 bridge. It observes its public
 * span/controller registry only; it never drives a cart or changes its state.
 */
public final class EngineeredInterop {
  private EngineeredInterop() {}

  /** True only where Engineered's active command can apply motor force. */
  public static boolean appliesForce(ServerLevel level, GraphHit hit) {
    var run = TrackSpanIndex.runAt(level, hit.edge(), hit.t());
    if (run == null || !run.hasSpan()) return false;
    var controller = LinearMotorControllerIndex.boundTo(level, run.pos());
    return controller != null && controller.commandMode() != null && controller.commandMode().drives();
  }
}
