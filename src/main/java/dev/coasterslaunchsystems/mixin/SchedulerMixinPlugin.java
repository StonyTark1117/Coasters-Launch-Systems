package dev.coasterslaunchsystems.mixin;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Select one scheduler repair before either required constructor redirect is applied. */
public final class SchedulerMixinPlugin implements IMixinConfigPlugin {
  @Override public boolean shouldApplyMixin(String target, String mixin) {
    if (!mixin.endsWith(".VeilSchedulerStartupMixin")) return true;
    var mods = LoadingModList.get();
    // Legacy Veil is outside this patch's inspected contract. The pinned current
    // Fourth Dimension artifact already carries this same required redirect.
    boolean currentVeil = mods.getMods().stream().anyMatch(mod ->
        mod.getModId().equals("veil") && mod.getVersion().toString().equals("4.3.2"));
    boolean fourthDimension = mods.getModFileById("fourthdimension") != null;
    System.out.println("Launch Systems Veil scheduler repair: "
        + (currentVeil && !fourthDimension ? "standalone" : fourthDimension ? "Fourth Dimension owns repair" : "legacy Veil"));
    return currentVeil && !fourthDimension;
  }
  @Override public void onLoad(String mixinPackage) {}
  @Override public String getRefMapperConfig() { return null; }
  @Override public void acceptTargets(Set<String> own, Set<String> other) {}
  @Override public List<String> getMixins() { return null; }
  @Override public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
  @Override public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
}
