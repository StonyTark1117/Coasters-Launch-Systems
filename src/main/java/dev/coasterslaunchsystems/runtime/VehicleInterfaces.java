package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import org.joml.Vector3d;

/** Certificates are derived from installed chassis hardware, never inferred from a seat magnet. */
public final class VehicleInterfaces {
  private VehicleInterfaces() {}
  public static Vector3d tirePoint(ServerSubLevel chassis) {
    var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
    if (bearing == null || !chassis.getLevel().getBlockState(bearing.below()).is(EquipmentContent.TIRE_SURFACE.get()))
      return null;
    return new Vector3d(bearing.getX() + .5, bearing.getY() - .0625, bearing.getZ() + .5);
  }
}
