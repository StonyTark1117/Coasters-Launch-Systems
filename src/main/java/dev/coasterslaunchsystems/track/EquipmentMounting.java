package dev.coasterslaunchsystems.track;

import dev.coasterslaunchsystems.integration.FourthDimensionMovingSupport;
import dev.coasterslaunchsystems.math.Mechanism;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Fixed cabinet reach; native dock authentication never waives running or moving clearance. */
public final class EquipmentMounting {
  // FD 1.6 MovingNativeAdapter.POSITION_TOLERANCE admits each locked rail socket
  // within 0.025m of its nominal endpoint; the fixed bridge keeps those actual
  // endpoints. Match that finite horizontal envelope, not successive observed
  // residuals. Ordinary reach and the four-block total limit remain exact, and
  // live FD authentication still checks endpoint alignment, attitude and locks.
  private static final double DOCK_HORIZONTAL_LIMIT = 1 + .025;
  private EquipmentMounting() {}

  /** Delta points from the cabinet center to the actual closest rail contact. */
  public static boolean withinReach(Mechanism mechanism, double dx, double dy, double dz, boolean authenticatedDock) {
    if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) return false;
    double horizontal = dx * dx + dz * dz;
    double distance = horizontal + dy * dy;
    if (distance <= 9) return true;
    return authenticatedDock && (mechanism == Mechanism.TIRE || mechanism == Mechanism.FIN_BRAKE
        || mechanism == Mechanism.LSM || mechanism == Mechanism.LIM)
        && dy > 0 && horizontal <= DOCK_HORIZONTAL_LIMIT * DOCK_HORIZONTAL_LIMIT && distance <= 16;
  }

  public static boolean available(ServerLevel level, DirectedTrack track, BlockPos cabinet, Vec3 contact, Mechanism mechanism) {
    var delta = contact.subtract(cabinet.getCenter());
    // Ordinary installations do not depend on the optional FD API. Deep installations
    // authenticate on every call, including cached bindings and restored block entities.
    if (withinReach(mechanism, delta.x, delta.y, delta.z, false)) return true;
    return withinReach(mechanism, delta.x, delta.y, delta.z, true)
        && FourthDimensionMovingSupport.fixedDock(level, track.edge().from(), track.edge().to());
  }
}
