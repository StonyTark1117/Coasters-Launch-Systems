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
    dev.coasterslaunchsystems.content.EquipmentContent.register(bus);
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOW,
        dev.coasterslaunchsystems.runtime.OrdinaryPassengers::physics);
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(dev.coasterslaunchsystems.runtime.OrdinaryPassengers::unload);
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
        dev.coasterslaunchsystems.runtime.EquipmentRuntime::physics);
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(dev.coasterslaunchsystems.runtime.EquipmentRuntime::unload);
    MotionProviders.registerOrdinaryFallback(new OrdinaryMotionProvider());
    if (ModList.get().isLoaded("fourthdimension"))
      MotionProviders.register(new FourthDimensionMotionProvider());
    LogUtils.getLogger().info("Coasters: Launch Systems loaded from {}",
        LaunchSystems.class.getProtectionDomain().getCodeSource().getLocation());
  }
}
