package dev.coasterslaunchsystems.integration;

import dev.coasterslaunchsystems.api.MotionVehicle;
import dev.coasterslaunchsystems.api.VehicleMotionProvider;
import dev.coasterslaunchsystems.track.EngagedContacts;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.ryanhcode.sable.sublevel.SubLevel;
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

/** Isolated read-only adapter for the schema-4 prototype, pending a published upstream facade. */
public final class FourthDimensionMotionProvider implements VehicleMotionProvider {
  private final Method data, markers, allows, ready;
  private final Field vehicles, carriers, carrierBody, capture, family, restraints, stopping,
      maintenance, fault, variant;

  public FourthDimensionMotionProvider() {
    try {
      var foundation = Class.forName("dev.fourthdimension.runtime.FoundationData");
      if (foundation.getField("SCHEMA").getInt(null) != 4)
        throw new ReflectiveOperationException("Unsupported Fourth Dimension save/API schema");
      var vehicle = Class.forName("dev.fourthdimension.runtime.FoundationData$Vehicle");
      var carrier = Class.forName("dev.fourthdimension.runtime.FoundationData$Carrier");
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
      boolean dispatch = true, propel = true;
      String operator = "";
      for (var id : consist) {
        var member = map.get(id);
        var cart = CoasterCartResolve.findCartById(level, id);
        if (member == null || !variant.get(vehicle).equals(variant.get(member)))
          return Optional.of(veto(chassis, consist, "fourthdimension:mixed_or_unbound"));
        if (((Enum<?>) family.get(member)).name().equals("PAX")) operator = "fourthdimension:pax_winch";
        var contact = EngagedContacts.current(level, cart).orElse(null);
        boolean support = contact != null && (boolean) allows.invoke(null, cart, level, contact.hit());
        boolean retained = maintenance.getBoolean(member) || !((String) fault.get(member)).isEmpty();
        boolean closed = restraints.getBoolean(member);
        dispatch &= support && !retained && closed && (boolean) ready.invoke(null, level, member);
        propel &= support && !retained && closed && !stopping.getBoolean(member)
            && ((List<?>) carriers.get(member)).stream().noneMatch(this::captured);
      }
      if (!operator.isEmpty()) { dispatch = false; propel = false; }
      return Optional.of(new MotionVehicle(id(), chassis, new ArrayList<>(bodies),
          new ArrayList<>(consist), Set.of(), dispatch, propel, operator, 2, 256));
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Fourth Dimension motion snapshot failed; launch vetoed", failure);
    }
  }

  private boolean captured(Object carrier) {
    try { return capture.getBoolean(carrier); }
    catch (IllegalAccessException failure) { throw new IllegalStateException("Carrier capture unavailable", failure); }
  }

  private MotionVehicle veto(UUID chassis, Set<UUID> consist, String owner) {
    return new MotionVehicle(id(), chassis, List.of(chassis), new ArrayList<>(consist),
        Set.of(), false, false, owner, 2, 256);
  }
}
