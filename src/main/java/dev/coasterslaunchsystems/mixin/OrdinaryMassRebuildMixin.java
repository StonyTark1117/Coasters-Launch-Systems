package dev.coasterslaunchsystems.mixin;

import dev.coasterslaunchsystems.runtime.OrdinaryPassengers;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restore owned passenger mass before Sable re-adds a recovered body to its native pipeline. */
@Mixin(value = ServerSubLevel.class, remap = false)
public abstract class OrdinaryMassRebuildMixin {
  @Inject(method = "buildMassTracker", at = @At("RETURN"))
  private void launchsystems$restorePassengers(CallbackInfo callback) {
    OrdinaryPassengers.rebuilt((ServerSubLevel) (Object) this);
  }
}
