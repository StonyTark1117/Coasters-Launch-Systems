package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.api.MotionVehicle;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Queue a physical interface impulse once. Carrier rotation is left to native joints. */
public final class NativeForces {
  private NativeForces() {}

  public static double mass(ServerLevel level, MotionVehicle vehicle) {
    return mass(level, vehicle.bodies());
  }

  public static double mass(ServerLevel level, java.util.List<java.util.UUID> bodies) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return 0;
    double result = 0;
    for (var id : bodies) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel body) || body.isRemoved()) return 0;
      var tracker = body.getMassTracker();
      if (tracker.isInvalid() || !Double.isFinite(tracker.getMass()) || tracker.getMass() <= 0) return 0;
      result += tracker.getMass();
    }
    return Double.isFinite(result) ? result : 0;
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

  public record PointDynamics(double speed, double effectiveMass) {}

  /** Velocity response at one interface to a unit impulse at another on the same native body. */
  public static double pointImpulseResponse(ServerSubLevel chassis, Vector3dc receiverPoint, Vector3dc receiverDirection,
      Vector3dc sourcePoint, Vector3dc sourceDirection) {
    var tracker = chassis.getMassTracker();
    var receiver = chassis.logicalPose().transformNormalInverse(new Vector3d(receiverDirection));
    var source = chassis.logicalPose().transformNormalInverse(new Vector3d(sourceDirection));
    var receiverLever = new Vector3d(receiverPoint).sub(tracker.getCenterOfMass()).cross(receiver);
    var sourceLever = new Vector3d(sourcePoint).sub(tracker.getCenterOfMass()).cross(source);
    return dev.coasterslaunchsystems.math.Numbers.finite(tracker.getInverseMass() * receiver.dot(source)
        + receiverLever.dot(tracker.getInverseInertiaTensor().transform(sourceLever)), "point impulse response");
  }

  /** Point speed and free-body impulse work include the strip's angular lever arm. */
  public static PointDynamics pointDynamics(ServerSubLevel chassis, PhysicsPipeline pipeline,
      Vector3dc plotPoint, Vector3dc worldDirection) {
    var tracker = chassis.getMassTracker();
    var arm = new Vector3d(plotPoint).sub(tracker.getCenterOfMass());
    var direction = chassis.logicalPose().transformNormalInverse(new Vector3d(worldDirection));
    var lever = new Vector3d(arm).cross(direction);
    double inverseMass = tracker.getInverseMass()
        + lever.dot(tracker.getInverseInertiaTensor().transform(new Vector3d(lever)));
    dev.coasterslaunchsystems.math.Numbers.positive(inverseMass, "point inverse mass");
    var worldArm = arm.rotate(chassis.logicalPose().orientation());
    var velocity = pipeline.getAngularVelocity(chassis, new Vector3d()).cross(worldArm)
        .add(pipeline.getLinearVelocity(chassis, new Vector3d()));
    return new PointDynamics(dev.coasterslaunchsystems.math.Numbers.finite(velocity.dot(worldDirection), "point speed"),
        1 / inverseMass);
  }
}
