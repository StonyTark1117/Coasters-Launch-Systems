package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.MotionVehicle;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Queue a physical interface impulse once. Carrier rotation is left to native joints. */
public final class NativeForces {
  private NativeForces() {}

  public static double mass(ServerLevel level, MotionVehicle vehicle) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return 0;
    double result = 0;
    for (var id : vehicle.bodies()) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel body) || body.isRemoved()) return 0;
      var tracker = body.getMassTracker();
      if (tracker.isInvalid() || !Double.isFinite(tracker.getMass()) || tracker.getMass() <= 0) return 0;
      result += tracker.getMass();
    }
    return result;
  }

  public static void pointForce(ServerSubLevel chassis, Vector3dc plotPoint, Vector3dc worldForce,
      double dt) {
    dev.coasterslaunchsystems.math.Numbers.positive(dt, "dt");
    if (!Double.isFinite(worldForce.lengthSquared())) throw new IllegalArgumentException("Invalid force");
    var localImpulse = chassis.logicalPose().transformNormalInverse(new Vector3d(worldForce).mul(dt));
    // Sable 2.0.3 QueuedForceGroup stores impulse totals; it does not multiply by dt later.
    chassis.getOrCreateQueuedForceGroup(ForceGroups.PROPULSION.get())
        .applyAndRecordPointForce(plotPoint, localImpulse);
  }
}
