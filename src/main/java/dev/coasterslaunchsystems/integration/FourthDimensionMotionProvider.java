package dev.coasterslaunchsystems.integration;

import dev.coasterslaunchsystems.api.MotionVehicle;
import dev.coasterslaunchsystems.api.VehicleMotionProvider;
import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.runtime.VehicleInterfaces;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Isolated schema-4 adapter; dispatch delegates to the upstream public operation after reservation. */
public final class FourthDimensionMotionProvider implements VehicleMotionProvider {
  private static final Map<String, String> HARDWARE_VARIANTS = Map.of(
      "ARROW", "arrow_four_rail_v1", "ZACSPIN", "zacspin_legacy_v1",
      "FREE_SPIN", "sns_magnetic_v1", "ZACSPIN_REVISED", "zacspin_revised_v1");
  private final Method data, markers, allows, ready, dispatchOperation, captureOperation;
  private final Field vehicles, carriers, carrierBody, capture, family, restraints, stopping,
      maintenance, fault, variant;

  public FourthDimensionMotionProvider() {
    try {
      var foundation = Class.forName("dev.fourthdimension.runtime.FoundationData");
      if (foundation.getField("SCHEMA").getInt(null) != 4)
        throw new ReflectiveOperationException("Unsupported Fourth Dimension save/API schema");
      var vehicle = Class.forName("dev.fourthdimension.runtime.FoundationData$Vehicle");
      var carrier = Class.forName("dev.fourthdimension.runtime.FoundationData$Carrier");
      dispatchOperation = Class.forName("dev.fourthdimension.vehicle.VehicleOperations").getMethod("dispatch", ServerLevel.class, vehicle);
      captureOperation = Class.forName("dev.fourthdimension.vehicle.VehicleOperations").getMethod("capture", ServerLevel.class, vehicle);
      data = foundation.getMethod("get", ServerLevel.class);
      vehicles = foundation.getField("vehicles");
      carriers = vehicle.getField("carriers"); carrierBody = carrier.getField("body");
      capture = carrier.getField("capture"); family = vehicle.getField("family");
      restraints = vehicle.getField("restraints"); stopping = vehicle.getField("stopping");
      maintenance = vehicle.getField("maintenance"); fault = vehicle.getField("fault");
      variant = vehicle.getField("variant");
      markers = Class.forName("dev.fourthdimension.runtime.FamilyControllers")
          .getMethod("find", SubLevel.class);
      allows = Class.forName("dev.fourthdimension.runtime.EligibilityHooks")
          .getMethod("allows", SubLevel.class, Level.class,
              dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit.class);
      ready = Class.forName("dev.fourthdimension.vehicle.Boarding")
          .getMethod("ready", ServerLevel.class, vehicle);
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Fourth Dimension adapter does not match installed API; launches disabled", failure);
    }
  }

  @Override public int apiVersion() { return API_VERSION; }
  @Override public String id() { return "fourthdimension:schema4"; }

  @Override public Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis) {
    var body = CoasterCartResolve.findCartById(level, chassis);
    if (body == null || body.isRemoved()) return Optional.empty();
    try {
      var map = (Map<?, ?>) vehicles.get(data.invoke(null, level));
      var consist = new LinkedHashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body));
      consist.add(chassis);
      boolean owned = false;
      for (var id : consist) {
        var cart = CoasterCartResolve.findCartById(level, id);
        if (cart == null) return Optional.of(veto(chassis, consist, "fourthdimension:missing_consist"));
        owned |= map.containsKey(id) || !((Set<?>) markers.invoke(null, cart)).isEmpty();
      }
      if (!owned) return Optional.empty();
      var vehicle = map.get(chassis);
      if (vehicle == null) return Optional.of(veto(chassis, consist, "fourthdimension:mixed_or_unbound"));
      var bodies = new LinkedHashSet<UUID>();
      bodies.add(chassis);
      for (var carrier : (List<?>) carriers.get(vehicle)) bodies.add((UUID) carrierBody.get(carrier));
      boolean dispatch = true, propel = true, brake = true;
      String operator = "";
      for (var id : consist) {
        var member = map.get(id);
        var cart = CoasterCartResolve.findCartById(level, id);
        if (member == null || !family.get(vehicle).equals(family.get(member)) || !variant.get(vehicle).equals(variant.get(member)))
          return Optional.of(veto(chassis, consist, "fourthdimension:mixed_or_unbound"));
        if (((Enum<?>) family.get(member)).name().equals("PAX")) operator = "fourthdimension:pax_winch";
        var contact = EngagedContacts.current(level, cart).orElse(null);
        boolean support = contact != null && carriersAvailable(level, member)
            && (boolean) allows.invoke(null, cart, level, contact.hit());
        boolean retained = maintenance.getBoolean(member) || !((String) fault.get(member)).isEmpty();
        boolean closed = restraints.getBoolean(member);
        dispatch &= support && !retained && closed && (boolean) ready.invoke(null, level, member);
        boolean released = support && !retained && closed && !stopping.getBoolean(member)
            && ((List<?>) carriers.get(member)).stream().noneMatch(this::captured);
        propel &= released;
        brake &= released;
      }
      if (!operator.isEmpty()) { dispatch = false; propel = false; brake = false; }
      String expected = HARDWARE_VARIANTS.get(((Enum<?>) family.get(vehicle)).name());
      var interfaces = java.util.EnumSet.noneOf(Mechanism.class);
      if (expected != null && expected.equals(variant.get(vehicle))) {
        if (VehicleInterfaces.tirePoint(body) != null) interfaces.add(Mechanism.TIRE);
        if (VehicleInterfaces.brakePoint(body) != null) interfaces.add(Mechanism.FIN_BRAKE);
        if (VehicleInterfaces.skidPoint(body) != null) interfaces.add(Mechanism.SKID_BRAKE);
        if (VehicleInterfaces.conductivePoint(body) != null) interfaces.add(Mechanism.EDDY_BRAKE);
        for (var mechanism : List.of(Mechanism.LSM, Mechanism.LIM)) if (VehicleInterfaces.linearPoint(body, mechanism) != null) interfaces.add(mechanism);
        if (((Enum<?>) family.get(vehicle)).name().equals("ARROW") && VehicleInterfaces.catchPoint(body) != null)
          interfaces.addAll(List.of(Mechanism.FLYWHEEL, Mechanism.HYDRAULIC, Mechanism.PNEUMATIC));
      }
      double loadedMass = dev.coasterslaunchsystems.runtime.NativeForces.mass(level, new ArrayList<>(bodies));
      dispatch &= loadedMass > 0; propel &= loadedMass > 0;
      brake &= loadedMass > 0 && (interfaces.contains(Mechanism.FIN_BRAKE) || interfaces.contains(Mechanism.SKID_BRAKE) || interfaces.contains(Mechanism.EDDY_BRAKE) || interfaces.contains(Mechanism.LSM) || interfaces.contains(Mechanism.LIM));
      return Optional.of(new MotionVehicle(id(), chassis, new ArrayList<>(bodies),
          new ArrayList<>(consist), interfaces, dispatch, propel, brake, operator, 2, 256,
          Math.min(256, loadedMass * 2)));
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Fourth Dimension motion snapshot failed; launch vetoed", failure);
    }
  }

  @Override public boolean requestDispatch(ServerLevel level, UUID chassis) {
    var snapshot = resolve(level, chassis).orElse(null);
    if (snapshot == null || !snapshot.dispatchReady() || !snapshot.exclusiveOperator().isEmpty()) return false;
    try {
      var vehicle = ((Map<?, ?>) vehicles.get(data.invoke(null, level))).get(chassis);
      if (vehicle == null) return false;
      dispatchOperation.invoke(null, level, vehicle);
      return resolve(level, chassis).map(MotionVehicle::propulsionAllowed).orElse(false);
    } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Fourth Dimension dispatch request was rejected", failure); }
  }

  @Override public dev.coasterslaunchsystems.api.CaptureState captureState(ServerLevel level, UUID chassis) {
    var snapshot = resolve(level, chassis).orElse(null);
    if (snapshot == null || !snapshot.exclusiveOperator().isEmpty() || snapshot.maximumBrakeForce() <= 0
        || !snapshot.interfaces().containsAll(Set.of(Mechanism.TIRE, Mechanism.FIN_BRAKE))) return dev.coasterslaunchsystems.api.CaptureState.DENIED;
    try {
      var map = (Map<?, ?>) vehicles.get(data.invoke(null, level)); boolean captured = true, aligned = true;
      for (var id : snapshot.consist()) {
        var member = map.get(id); var cart = CoasterCartResolve.findCartById(level, id);
        var contact = cart == null ? null : EngagedContacts.current(level, cart).orElse(null);
        if (member == null || contact == null || !carriersAvailable(level, member) || maintenance.getBoolean(member)
            || !((String) fault.get(member)).isEmpty() || !(boolean) allows.invoke(null, cart, level, contact.hit()))
          return dev.coasterslaunchsystems.api.CaptureState.DENIED;
        captured &= stopping.getBoolean(member) && ((List<?>) carriers.get(member)).stream().allMatch(this::captured);
        aligned &= (boolean) ready.invoke(null, level, member);
      }
      return captured ? aligned ? dev.coasterslaunchsystems.api.CaptureState.READY : dev.coasterslaunchsystems.api.CaptureState.ALIGNING
          : snapshot.propulsionAllowed() && snapshot.brakingAllowed() ? dev.coasterslaunchsystems.api.CaptureState.RELEASED : dev.coasterslaunchsystems.api.CaptureState.DENIED;
    } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Fourth Dimension capture readiness unavailable", failure); }
  }
  @Override public boolean requestCapture(ServerLevel level, UUID chassis) {
    var state = captureState(level, chassis);
    if (state == dev.coasterslaunchsystems.api.CaptureState.READY || state == dev.coasterslaunchsystems.api.CaptureState.ALIGNING) return true;
    if (state != dev.coasterslaunchsystems.api.CaptureState.RELEASED) return false;
    try {
      var vehicle = ((Map<?, ?>) vehicles.get(data.invoke(null, level))).get(chassis);
      if (vehicle == null) return false;
      captureOperation.invoke(null, level, vehicle);
      var after = captureState(level, chassis);
      return after == dev.coasterslaunchsystems.api.CaptureState.ALIGNING || after == dev.coasterslaunchsystems.api.CaptureState.READY;
    } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Fourth Dimension capture request rejected", failure); }
  }

  private boolean captured(Object carrier) {
    try { return capture.getBoolean(carrier); }
    catch (IllegalAccessException failure) { throw new IllegalStateException("Carrier capture unavailable", failure); }
  }

  private boolean carriersAvailable(ServerLevel level, Object member) throws IllegalAccessException {
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return false;
    for (var carrier : (List<?>) carriers.get(member)) {
      var loaded = container.getSubLevel((UUID) carrierBody.get(carrier));
      if (!(loaded instanceof ServerSubLevel body) || body.isRemoved() || body.getMassTracker().isInvalid()
          || !Double.isFinite(body.getMassTracker().getMass()) || body.getMassTracker().getMass() <= 0) return false;
    }
    return true;
  }

  private MotionVehicle veto(UUID chassis, Set<UUID> consist, String owner) {
    return new MotionVehicle(id(), chassis, List.of(chassis), new ArrayList<>(consist),
        Set.of(), false, false, owner, 2, 256);
  }
}
