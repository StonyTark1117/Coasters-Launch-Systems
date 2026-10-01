package dev.coasterslaunchsystems.api;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

public final class MotionProviders {
  private static final List<VehicleMotionProvider> PROVIDERS = new CopyOnWriteArrayList<>();
  private static volatile VehicleMotionProvider fallback;
  private MotionProviders() {}

  public static synchronized void register(VehicleMotionProvider provider) {
    if (provider.apiVersion() != VehicleMotionProvider.API_VERSION)
      throw new IllegalArgumentException("Unsupported motion provider API " + provider.id());
    if ((fallback != null && fallback.id().equals(provider.id()))
        || PROVIDERS.stream().anyMatch(p -> p.id().equals(provider.id())))
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
    if (result == null && fallback != null) return fallback.resolve(level, chassis);
    return Optional.ofNullable(result);
  }

  /** Dispatch is delegated only to the uniquely resolved owner after local route reservation. */
  public static boolean requestDispatch(ServerLevel level, UUID chassis) {
    var motion = resolve(level, chassis).orElse(null);
    if (motion == null || !motion.dispatchReady() || !motion.exclusiveOperator().isEmpty()) return false;
    for (var provider : PROVIDERS) if (provider.id().equals(motion.provider())) return provider.requestDispatch(level, chassis);
    return fallback != null && fallback.id().equals(motion.provider()) && fallback.requestDispatch(level, chassis);
  }

  /** An ordinary cart must never override an integration's vetoed or retained vehicle. */
  public static synchronized void registerOrdinaryFallback(VehicleMotionProvider provider) {
    if (fallback != null || provider.apiVersion() != VehicleMotionProvider.API_VERSION
        || PROVIDERS.stream().anyMatch(p -> p.id().equals(provider.id())))
      throw new IllegalArgumentException("Invalid ordinary fallback provider");
    fallback = provider;
  }
}
