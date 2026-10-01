package dev.coasterslaunchsystems;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import dev.coasterslaunchsystems.api.MotionProviders;
import dev.coasterslaunchsystems.runtime.OrdinaryMotionProvider;
import dev.coasterslaunchsystems.integration.FourthDimensionMotionProvider;

@Mod(LaunchSystems.ID)
public final class LaunchSystems {
  public static final String ID = "coasterslaunchsystems";

  public LaunchSystems(IEventBus bus) {
    MotionProviders.registerOrdinaryFallback(new OrdinaryMotionProvider());
    if (ModList.get().isLoaded("fourthdimension"))
      MotionProviders.register(new FourthDimensionMotionProvider());
    LogUtils.getLogger().info("Coasters: Launch Systems loaded from {}",
        LaunchSystems.class.getProtectionDomain().getCodeSource().getLocation());
  }
}
