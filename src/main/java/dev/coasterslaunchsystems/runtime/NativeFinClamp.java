package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.math.Numbers;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** A bounded longitudinal pad constraint. Rail and carrier joints retain every other degree of freedom. */
public final class NativeFinClamp implements AutoCloseable {
  private final GenericConstraintHandle handle;
  private final ServerSubLevel chassis;
  private final Vector3d point;
  private final Vector3d worldAnchor;

  public NativeFinClamp(PhysicsPipeline pipeline, ServerSubLevel chassis, Vector3dc plotPoint,
      Vector3dc worldDirection, double maximumForce) {
    this.chassis = chassis; point = new Vector3d(plotPoint);
    worldAnchor = new Vector3d(point).sub(chassis.getMassTracker().getCenterOfMass())
        .rotate(chassis.logicalPose().orientation()).add(chassis.logicalPose().position());
    var worldFrame = frame(worldDirection);
    var localFrame = new Quaterniond(chassis.logicalPose().orientation()).invert().mul(worldFrame);
    handle = pipeline.addConstraint(null, chassis,
        new GenericConstraintConfiguration(worldAnchor, point, worldFrame, localFrame));
    if (handle == null || !handle.isValid()) throw new IllegalStateException("Native fin clamp constraint unavailable");
    setForce(maximumForce);
  }

  private static Quaterniond frame(Vector3dc direction) {
    if (!Double.isFinite(direction.lengthSquared()) || Math.abs(direction.lengthSquared() - 1) > 1e-6)
      throw new IllegalArgumentException("Fin clamp direction must be a unit vector");
    return new Quaterniond().rotationTo(new Vector3d(1, 0, 0), direction);
  }

  /** Damping targets zero slip; no position spring can pull a stopped train back to its entry point. */
  public void setForce(double maximumForce) {
    handle.setMotor(ConstraintJointAxis.LINEAR_X, 0, 0, 1_000_000, true,
        Numbers.nonnegative(maximumForce, "clamp force"));
  }

  public void align(Vector3dc direction) {
    var worldFrame = frame(direction);
    handle.setFrame1(worldAnchor, worldFrame);
    handle.setFrame2(point, new Quaterniond(chassis.logicalPose().orientation()).invert().mul(worldFrame));
  }
  public boolean valid() { return !chassis.isRemoved() && handle.isValid(); }
  @Override public void close() { if (handle.isValid()) handle.remove(); }
}
