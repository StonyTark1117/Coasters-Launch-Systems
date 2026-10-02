package dev.coasterslaunchsystems.client.ponder;

import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import dev.coasterslaunchsystems.LaunchSystems;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Client-side Ponder entry point. Scenes describe only the implemented operating workflow. */
public final class LaunchSystemsPonderPlugin extends CreatePonderPlugin {
  @Override public String getModId() { return LaunchSystems.ID; }
  @Override public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
    LaunchSystemsPonderScenes.register(helper);
  }
  @Override public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
    LaunchSystemsPonderTags.register(helper);
  }
}
