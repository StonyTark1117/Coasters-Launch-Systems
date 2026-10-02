package dev.coasterslaunchsystems.client;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.coasterslaunchsystems.LaunchSystems;
import dev.coasterslaunchsystems.client.ponder.LaunchSystemsPonderPlugin;
import dev.coasterslaunchsystems.content.EquipmentContent;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = LaunchSystems.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EquipmentClient {
  @SubscribeEvent
  public static void ponder(FMLClientSetupEvent event) {
    PonderIndex.addPlugin(new LaunchSystemsPonderPlugin());
  }

  @SubscribeEvent
  public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(EquipmentContent.ENTITY.get(), KineticBlockEntityRenderer::new);
  }
}
