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

  private static int declaredVersion(VehicleMotionProvider provider) {
    try { return provider.apiVersion(); }
    catch (AbstractMethodError oldBinary) { throw new IllegalArgumentException("Motion provider must explicitly declare its API version: " + provider.id(), oldBinary); }
  }

  public static synchronized void register(VehicleMotionProvider provider) {
    if (declaredVersion(provider) != VehicleMotionProvider.API_VERSION)
      throw new IllegalArgumentException("Unsupported motion provider API " + provider.id());
    if ((fallback != null && fallback.id().equals(provider.id()))
        || PROVIDERS.stream().anyMatch(p -> p.id().equals(provider.id())))
      throw new IllegalArgumentException("Duplicated motion provider " + provider.id());
    PROVIDERS.add(provider);
  }

  public static Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis) {
    var saved = SavedMotionOwners.get(level);
    if (!saved.valid())
      return Optional.of(new MotionVehicle("launchsystems:invalid_owner_data", chassis, List.of(chassis), List.of(chassis),
          java.util.Set.of(), false, false, false, "launchsystems:invalid_saved_ownership", 2, 256, 0));
    String previous = saved.owner(chassis);
    // A missing optional implementation cannot relinquish its persisted vehicle to fallback
    // or to a different provider. A present provider may still deliberately decline ownership.
    if (previous != null && PROVIDERS.stream().noneMatch(p -> p.id().equals(previous)))
      return Optional.of(new MotionVehicle(previous, chassis, List.of(chassis), List.of(chassis),
          java.util.Set.of(), false, false, false, "launchsystems:missing_provider:" + previous, 2, 256, 0));
    MotionVehicle result = null;
    for (var provider : PROVIDERS) {
      var found = provider.resolve(level, chassis);
      if (found.isEmpty()) continue;
      if (result != null) throw new IllegalStateException("Multiple providers claimed vehicle " + chassis);
      result = found.get();
    }
    if (result != null) saved.remember(chassis, result.provider());
    else {
      var ordinary = fallback == null ? Optional.<MotionVehicle>empty() : fallback.resolve(level, chassis);
      // A missing/unloaded body is not an intentional release. Preserve API-5's
      // primary -> ordinary handoff only when the fallback has a live vehicle.
      if (ordinary.isPresent()) saved.forget(chassis);
      return ordinary;
    }
    return Optional.ofNullable(result);
  }

  /** Dispatch is delegated only to the uniquely resolved owner after local route reservation. */
  public static boolean requestDispatch(ServerLevel level, UUID chassis) {
    var motion = resolve(level, chassis).orElse(null);
    if (motion == null || !motion.dispatchReady() || !motion.exclusiveOperator().isEmpty()) return false;
    for (var provider : PROVIDERS) if (provider.id().equals(motion.provider())) return provider.requestDispatch(level, chassis);
    return fallback != null && fallback.id().equals(motion.provider()) && fallback.requestDispatch(level, chassis);
  }

  private static VehicleMotionProvider owner(MotionVehicle motion) {
    if (motion == null) return null;
    for (var provider : PROVIDERS) if (provider.id().equals(motion.provider())) return provider;
    return fallback != null && fallback.id().equals(motion.provider()) ? fallback : null;
  }

  public static boolean reserveAuthority(ServerLevel level, UUID chassis, net.minecraft.core.BlockPos controller) {
    var provider = owner(resolve(level, chassis).orElse(null));
    return provider != null && provider.reserveAuthority(level, chassis, controller);
  }

  public static void resumeAuthority(ServerLevel level, net.minecraft.core.BlockPos controller) {
    for (var provider : PROVIDERS) provider.resumeAuthority(level, controller);
  }
  public static boolean transferAuthority(ServerLevel level, UUID chassis, net.minecraft.core.BlockPos source,
      net.minecraft.core.BlockPos target) {
    var provider = owner(resolve(level, chassis).orElse(null));
    return provider != null && provider.transferAuthority(level, chassis, source, target);
  }

  /** No fallback operation may override the uniquely resolved primary vehicle owner. */
  public static CaptureState captureState(ServerLevel level, UUID chassis) {
    var motion = resolve(level, chassis).orElse(null); var provider = owner(motion);
    if (provider == null || !motion.exclusiveOperator().isEmpty()) return CaptureState.DENIED;
    var state = java.util.Objects.requireNonNull(provider.captureState(level, chassis), "provider capture state");
    if (state == CaptureState.DENIED || state == CaptureState.UNSUPPORTED) return state;
    var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level);
    if (container == null) return CaptureState.DENIED;
    var members = java.util.Set.copyOf(motion.consist()); var bodies = new java.util.HashSet<UUID>();
    for (var id : members) {
      var member = resolve(level, id).orElse(null);
      if (member == null || !member.provider().equals(motion.provider()) || !member.exclusiveOperator().isEmpty()
          || !java.util.Set.copyOf(member.consist()).equals(members)
          || dev.coasterslaunchsystems.runtime.NativeForces.mass(level, member) <= 0
          || !(container.getSubLevel(id) instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel cart)
          || dev.coasterslaunchsystems.track.EngagedContacts.current(level, cart).isEmpty()) return CaptureState.DENIED;
      for (var body : member.bodies()) if (!bodies.add(body)) return CaptureState.DENIED;
    }
    return state;
  }

  /** Request only after native whole-train hold, with the caller's complete reserved membership. */
  public static boolean requestCapture(ServerLevel level, UUID chassis, java.util.Set<UUID> reserved) {
    var first = resolve(level, chassis).orElse(null); var provider = owner(first);
    var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level);
    if (provider == null || container == null || reserved.isEmpty() || !reserved.contains(chassis)
        || !java.util.Set.copyOf(first.consist()).equals(reserved)) return false;
    var bodies = new java.util.HashSet<UUID>();
    for (var id : reserved) {
      var motion = resolve(level, id).orElse(null);
      if (motion == null || !motion.provider().equals(first.provider()) || !motion.exclusiveOperator().isEmpty()
          || !java.util.Set.copyOf(motion.consist()).equals(reserved)
          || !motion.interfaces().containsAll(java.util.Set.of(dev.coasterslaunchsystems.math.Mechanism.TIRE, dev.coasterslaunchsystems.math.Mechanism.FIN_BRAKE))
          || motion.maximumBrakeForce() <= 0 || dev.coasterslaunchsystems.runtime.NativeForces.mass(level, motion) <= 0) return false;
      for (var body : motion.bodies()) if (!bodies.add(body)) return false;
      if (!(container.getSubLevel(id) instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel cart) || cart.isRemoved()) return false;
      var contact = dev.coasterslaunchsystems.track.EngagedContacts.current(level, cart).orElse(null);
      if (contact == null) return false;
      var pipeline = container.physicsSystem().getPipeline();
      var velocity = pipeline.getLinearVelocity(cart, new org.joml.Vector3d());
      if (!Double.isFinite(velocity.lengthSquared()) || velocity.length() >= .03) return false;
      var axis = dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.plotLocalCenterlineTangentAlongIncreasingT(level, contact.hit().edge(), contact.hit().t());
      var point = dev.coasterslaunchsystems.runtime.VehicleInterfaces.tirePoint(cart);
      if (point == null || Math.abs(dev.coasterslaunchsystems.runtime.NativeForces.pointDynamics(cart, pipeline, point,
          new org.joml.Vector3d(axis.x, axis.y, axis.z).normalize()).speed()) >= .03) return false;
      var state = captureState(level, id);
      if (state == CaptureState.DENIED || state == CaptureState.UNSUPPORTED) return false;
      boolean vehicleHeld = !motion.propulsionAllowed() && (state == CaptureState.ALIGNING || state == CaptureState.READY);
      if (!vehicleHeld && dev.coasterslaunchsystems.runtime.EquipmentRuntime.activeFinClamps(level, id) == 0) return false;
    }
    return provider.requestCapture(level, chassis);
  }

  /** An ordinary cart must never override an integration's vetoed or retained vehicle. */
  public static synchronized void registerOrdinaryFallback(VehicleMotionProvider provider) {
    if (fallback != null || declaredVersion(provider) != VehicleMotionProvider.API_VERSION
        || PROVIDERS.stream().anyMatch(p -> p.id().equals(provider.id())))
      throw new IllegalArgumentException("Invalid ordinary fallback provider");
    fallback = provider;
  }
}
