package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.MotionVehicle;
import dev.coasterslaunchsystems.api.VehicleMotionProvider;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.rivet.RivetBlockEntity;
import dev.silvergold.simulatedcoasters.rivet.RivetHostKey;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Native Coasters carts and rigid rivet descendants; foreign articulated carts need providers. */
public final class OrdinaryMotionProvider implements VehicleMotionProvider {
  public static final String ID = "simulatedcoasters:ordinary_0.1.5";
  @Override public boolean requestDispatch(ServerLevel level, UUID chassis) {
    return resolve(level, chassis).map(m -> m.dispatchReady() && m.propulsionAllowed()).orElse(false);
  }
  @Override public String id() { return ID; }

  @Override public Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis) {
    var body = CoasterCartResolve.findCartById(level, chassis);
    var container = SubLevelContainer.getContainer(level);
    if (body == null || body.isRemoved() || container == null || !CoasterCartResolve.isCartSubLevel(body))
      return Optional.empty();
    var bodies = new LinkedHashSet<UUID>();
    bodies.add(chassis);
    boolean changed;
    do {
      changed = false;
      for (var candidate : container.getAllSubLevels()) {
        if (candidate.isRemoved() || !RivetHostKey.isRivetSubLevel(candidate)) continue;
        var entity = candidate.getPlot().getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO);
        if (entity instanceof RivetBlockEntity rivet && rivet.hostKey() != null
            && bodies.contains(rivet.hostKey().hostSubLevelId()))
          changed |= bodies.add(candidate.getUniqueId());
      }
    } while (changed);
    var consist = new LinkedHashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body));
    consist.add(chassis);
    // A real installed drive strip is required. Fin, magnet and catch surfaces are separate.
    var interfaces = java.util.EnumSet.noneOf(dev.coasterslaunchsystems.math.Mechanism.class);
    if (VehicleInterfaces.tirePoint(body) != null) interfaces.add(dev.coasterslaunchsystems.math.Mechanism.TIRE);
    if (VehicleInterfaces.brakePoint(body) != null) interfaces.add(dev.coasterslaunchsystems.math.Mechanism.FIN_BRAKE);
    if (VehicleInterfaces.conductivePoint(body) != null) interfaces.add(dev.coasterslaunchsystems.math.Mechanism.EDDY_BRAKE);
    return Optional.of(new MotionVehicle(id(), chassis, new ArrayList<>(bodies),
        new ArrayList<>(consist), interfaces, true, true, true, "", 2, 256, 256));
  }
}
