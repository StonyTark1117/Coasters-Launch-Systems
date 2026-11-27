package dev.coasterslaunchsystems.api;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Optional ownership survives an absent provider; unloading is never a vehicle conversion. */
final class SavedMotionOwners extends SavedData {
  private final Map<UUID, String> owners = new HashMap<>();
  private boolean observing;
  private CompoundTag rejectedData;

  static SavedMotionOwners get(ServerLevel level) {
    var data = level.getDataStorage().computeIfAbsent(new Factory<>(SavedMotionOwners::new, SavedMotionOwners::load), "launchsystems_motion_owners");
    var container = SubLevelContainer.getContainer(level);
    if (!data.observing && container != null) {
      data.observing = true;
      container.addObserver(new SubLevelObserver() {
        @Override public void onSubLevelRemoved(SubLevel body, SubLevelRemovalReason reason) {
          if (reason == SubLevelRemovalReason.REMOVED) data.forget(body.getUniqueId());
        }
      });
    }
    return data;
  }
  String owner(UUID chassis) { return owners.get(chassis); }
  boolean valid() { return rejectedData == null; }
  void remember(UUID chassis, String provider) {
    if (!provider.equals(owners.put(chassis, provider))) setDirty();
  }
  void forget(UUID chassis) { if (owners.remove(chassis) != null) setDirty(); }
  private static SavedMotionOwners load(CompoundTag tag, HolderLookup.Provider registries) {
    var data = new SavedMotionOwners();
    try {
      if (tag.getInt("schema") != 1 || !tag.contains("owners", 9)) throw new IllegalArgumentException("Invalid owner schema");
      var rows = tag.getList("owners", 10);
      if (((ListTag) tag.get("owners")).size() != rows.size()) throw new IllegalArgumentException("Invalid owner list");
      for (var entry : rows) {
        var row = (CompoundTag) entry;
        String provider = row.getString("provider");
        if (net.minecraft.resources.ResourceLocation.tryParse(provider) == null
            || data.owners.put(row.getUUID("chassis"), provider) != null)
          throw new IllegalArgumentException("Invalid saved motion owner identity");
      }
    } catch (RuntimeException malformed) {
      // SavedData's loader catches thrown errors and may create a fresh empty value.
      // Retain the bad input instead, and deny resolution until an operator repairs it.
      data.rejectedData = tag.copy();
    }
    return data;
  }
  @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    if (rejectedData != null) return rejectedData.copy();
    tag.putInt("schema", 1); var rows = new ListTag();
    owners.forEach((chassis, provider) -> {
      var row = new CompoundTag(); row.putUUID("chassis", chassis); row.putString("provider", provider); rows.add(row);
    });
    tag.put("owners", rows); return tag;
  }
}
