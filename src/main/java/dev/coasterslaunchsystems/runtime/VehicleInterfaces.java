package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.content.EquipmentContent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import org.joml.Vector3d;

/** Certificates are derived from installed chassis hardware, never inferred from a seat magnet. */
public final class VehicleInterfaces {
  private VehicleInterfaces() {}
  private static java.util.function.BiFunction<ServerSubLevel, dev.coasterslaunchsystems.math.Mechanism, net.minecraft.core.BlockPos> layout;
  /** Optional provider supplies only mounting geometry; motion authority remains in its snapshot. */
  public static void installLayoutResolver(java.util.function.BiFunction<ServerSubLevel, dev.coasterslaunchsystems.math.Mechanism, net.minecraft.core.BlockPos> resolver) {
    if (layout != null) throw new IllegalStateException("Vehicle interface layout resolver already installed");
    layout = java.util.Objects.requireNonNull(resolver);
  }
  private static net.minecraft.core.BlockPos position(ServerSubLevel chassis, dev.coasterslaunchsystems.math.Mechanism mechanism) {
    var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
    if (bearing == null) return null;
    var offset = layout == null ? null : layout.apply(chassis, mechanism);
    if (offset == null) offset = dev.coasterslaunchsystems.integration.FourthDimensionCertificates.defaultOffset(mechanism);
    return bearing.offset(offset);
  }
  public static Vector3d point(ServerSubLevel chassis, dev.coasterslaunchsystems.math.Mechanism mechanism) {
    return switch (mechanism) {
      case TIRE -> tirePoint(chassis);
      case FIN_BRAKE -> brakePoint(chassis);
      case SKID_BRAKE -> skidPoint(chassis);
      case EDDY_BRAKE -> conductivePoint(chassis);
      case LSM, LIM -> linearPoint(chassis, mechanism);
      default -> catchPoint(chassis);
    };
  }
  /** Nominal certified mounting position, used only to report missing hardware at a contacting module. */
  public static Vector3d mountingPoint(ServerSubLevel chassis, dev.coasterslaunchsystems.math.Mechanism mechanism) {
    var pos = position(chassis, mechanism);
    if (pos == null) return null;
    double height = switch (mechanism) {
      case TIRE -> .9375;
      case SKID_BRAKE -> .25;
      case LSM, LIM -> .875;
      default -> .5;
    };
    return new Vector3d(pos.getX() + .5, pos.getY() + height, pos.getZ() + .5);
  }
  /** A long mounting arm must actually meet this fixed span; bearing support alone is insufficient. */
  public static dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit contactHit(
      net.minecraft.server.level.ServerLevel level, dev.coasterslaunchsystems.track.DirectedTrack track,
      ServerSubLevel chassis, Vector3d point) {
    var accepted = dev.coasterslaunchsystems.track.EngagedContacts.current(level, chassis).orElse(null);
    var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
    if (accepted == null || bearing == null || !track.matches(accepted.hit())) return null;
    var world = NativeCableRig.worldPoint(chassis, point);
    var hit = dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.closestHitOnEdge(level,
        track.edge(), new net.minecraft.world.phys.Vec3(world.x, world.y, world.z));
    if (hit == null || !Double.isFinite(hit.t())) return null;
    // Native nearest-point sampling can extrapolate at an edge end. Project onto
    // the real endpoint before applying the existing physical pad tolerance;
    // never pass an out-of-domain parameter into fixed-section arc distance.
    if (hit.t() < 0 || hit.t() > 1) {
      double t = Math.max(0, Math.min(1, hit.t()));
      hit = new dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit(
          dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.sampleEdgePositionPlotLocal(level, track.edge(), t), track.edge(), t);
    }
    var center = new Vector3d(bearing.getX() + .5, bearing.getY() + .5, bearing.getZ() + .5);
    var transverse = NativeCableRig.worldPoint(chassis, center)
        .sub(accepted.hit().point().x, accepted.hit().point().y, accepted.hit().point().z)
        .add(new Vector3d(point.x - center.x, point.y - center.y, 0).rotate(chassis.logicalPose().orientation()));
    // Keep the normal installed lateral/vertical offset, but refuse a mounting arm which
    // misses the curve or projects beyond an endpoint by more than the pad contact tolerance.
    return new Vector3d(world).sub(hit.point().x, hit.point().y, hit.point().z).sub(transverse).lengthSquared() <= .36 ? hit : null;
  }
  public static Vector3d tirePoint(ServerSubLevel chassis) {
    var strip = position(chassis, dev.coasterslaunchsystems.math.Mechanism.TIRE);
    if (strip == null || !chassis.getLevel().getBlockState(strip).is(EquipmentContent.TIRE_SURFACE.get())) return null;
    return new Vector3d(strip.getX() + .5, strip.getY() + .9375, strip.getZ() + .5);
  }
  /** A separate vertical fin beside the bearing; tire strips cannot stand in for a brake fin. */
  public static Vector3d brakePoint(ServerSubLevel chassis) {
    var fin = position(chassis, dev.coasterslaunchsystems.math.Mechanism.FIN_BRAKE);
    if (fin == null || !chassis.getLevel().getBlockState(fin).is(EquipmentContent.BRAKE_FIN.get())) return null;
    return new Vector3d(fin.getX() + .5, fin.getY() + .5, fin.getZ() + .5);
  }
  /** A low-friction skid shoe is independently certified and cannot substitute for the held fin clamp. */
  public static Vector3d skidPoint(ServerSubLevel chassis) {
    var shoe = position(chassis, dev.coasterslaunchsystems.math.Mechanism.SKID_BRAKE);
    if (shoe == null || !chassis.getLevel().getBlockState(shoe).is(EquipmentContent.SKID_SHOE.get())) return null;
    return new Vector3d(shoe.getX() + .5, shoe.getY() + .25, shoe.getZ() + .5);
  }
  /** Conductive hardware is distinct from the friction fin and from native seat magnets. */
  public static Vector3d conductivePoint(ServerSubLevel chassis) {
    var fin = position(chassis, dev.coasterslaunchsystems.math.Mechanism.EDDY_BRAKE);
    if (fin == null || !chassis.getLevel().getBlockState(fin).is(EquipmentContent.CONDUCTIVE_FIN.get())) return null;
    return new Vector3d(fin.getX() + .5, fin.getY() + .5, fin.getZ() + .5);
  }
  public static Vector3d linearPoint(ServerSubLevel chassis, dev.coasterslaunchsystems.math.Mechanism mechanism) {
    if (!mechanism.linear()) throw new IllegalArgumentException("Not a linear motor interface");
    var plate = position(chassis, mechanism);
    var hardware = mechanism == dev.coasterslaunchsystems.math.Mechanism.LSM ? EquipmentContent.LSM_PLATE : EquipmentContent.LIM_PLATE;
    if (plate == null || !chassis.getLevel().getBlockState(plate).is(hardware.get())) return null;
    return new Vector3d(plate.getX() + .5, plate.getY() + .875, plate.getZ() + .5);
  }
  public static Vector3d catchPoint(ServerSubLevel chassis) {
    var socket = position(chassis, dev.coasterslaunchsystems.math.Mechanism.FLYWHEEL);
    if (socket == null || !chassis.getLevel().getBlockState(socket).is(EquipmentContent.CATCH_SOCKET.get())) return null;
    return new Vector3d(socket.getX() + .5, socket.getY() + .5, socket.getZ() + .5);
  }
}
