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
  /** A separate vertical fin beside the bearing; tire strips cannot stand in for a brake fin. */
  public static Vector3d brakePoint(ServerSubLevel chassis) {
    var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
    var fin = bearing == null ? null : bearing.east().below();
    if (fin == null || !chassis.getLevel().getBlockState(fin).is(EquipmentContent.BRAKE_FIN.get())) return null;
    return new Vector3d(fin.getX() + .5, fin.getY() + .5, fin.getZ() + .5);
  }
  /** Conductive hardware is distinct from the friction fin and from native seat magnets. */
  public static Vector3d conductivePoint(ServerSubLevel chassis) {
    var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
    var fin = bearing == null ? null : bearing.west().below();
    if (fin == null || !chassis.getLevel().getBlockState(fin).is(EquipmentContent.CONDUCTIVE_FIN.get())) return null;
    return new Vector3d(fin.getX() + .5, fin.getY() + .5, fin.getZ() + .5);
  }
  public static Vector3d linearPoint(ServerSubLevel chassis, dev.coasterslaunchsystems.math.Mechanism mechanism) {
    if (!mechanism.linear()) throw new IllegalArgumentException("Not a linear motor interface");
    var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
    var plate = bearing == null ? null : (mechanism == dev.coasterslaunchsystems.math.Mechanism.LSM ? bearing.north() : bearing.south()).below();
    var hardware = mechanism == dev.coasterslaunchsystems.math.Mechanism.LSM ? EquipmentContent.LSM_PLATE : EquipmentContent.LIM_PLATE;
    if (plate == null || !chassis.getLevel().getBlockState(plate).is(hardware.get())) return null;
    return new Vector3d(plate.getX() + .5, plate.getY() + .875, plate.getZ() + .5);
  }
  public static Vector3d catchPoint(ServerSubLevel chassis) {
    var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
    var socket = bearing == null ? null : bearing.below(2);
    if (socket == null || !chassis.getLevel().getBlockState(socket).is(EquipmentContent.CATCH_SOCKET.get())) return null;
    return new Vector3d(socket.getX() + .5, socket.getY() + .5, socket.getZ() + .5);
  }
}
