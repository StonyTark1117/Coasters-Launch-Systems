package dev.coasterslaunchsystems.mixin;

import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraph;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackHitCache", remap = false)
public abstract class EngagedContactMixin {
  @Inject(method = "engagedTickFrame", at = @At("RETURN"))
  private static void launchsystems$accepted(ServerLevel level, CoasterPathGraph graph,
      ServerSubLevel body, GraphHit hit, boolean negate, boolean hosted,
      CallbackInfoReturnable<Object> callback) {
    if (callback.getReturnValue() instanceof EngagedFrameAccessor frame)
      EngagedContacts.accepted(level, body, frame.launchsystems$hit());
    else EngagedContacts.forget(level, body);
  }
}
