package dev.coasterslaunchsystems.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

public final class MotionProviders {
  private static final List<VehicleMotionProvider> PROVIDERS = new ArrayList<>();
  private MotionProviders() {}

  public static synchronized void register(VehicleMotionProvider provider) {
    if (provider.apiVersion() != VehicleMotionProvider.API_VERSION)
      throw new IllegalArgumentException("Unsupported motion provider API " + provider.id());
    if (PROVIDERS.stream().anyMatch(p -> p.id().equals(provider.id())))
      throw new IllegalArgumentException("Duplicated motion provider " + provider.id());
    PROVIDERS.add(provider);
  }

  public static Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis) {
    MotionVehicle result = null;
    for (var provider : PROVIDERS) {
      var found = provider.resolve(level, chassis);
      if (found.isEmpty()) continue;
      if (result != null) throw new IllegalStateException("Multiple providers claimed vehicle " + chassis);
      result = found.get();
    }
    return Optional.ofNullable(result);
  }
}
