package dev.coasterslaunchsystems.mixin;

import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read the returned cached frame, which can differ from the caller's candidate this tick. */
@Mixin(targets = "dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackHitCache$EngagedTickFrame", remap = false)
public interface EngagedFrameAccessor {
  @Accessor("graphHit") GraphHit launchsystems$hit();
}
