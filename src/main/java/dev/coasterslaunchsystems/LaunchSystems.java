package dev.coasterslaunchsystems;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(LaunchSystems.ID)
public final class LaunchSystems {
  public static final String ID = "coasterslaunchsystems";

  public LaunchSystems(IEventBus bus) {
    LogUtils.getLogger().info("Coasters: Launch Systems loaded from {}",
        LaunchSystems.class.getProtectionDomain().getCodeSource().getLocation());
  }
}
