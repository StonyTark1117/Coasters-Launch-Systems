package dev.coasterslaunchsystems.math;

/** All values use one game scale: blocks, seconds, native Sable mass, and native work. */
public final class DriveMath {
  private DriveMath() {}

  public static double wheelSpeed(double rpm, double radius) {
    return Numbers.finite(rpm, "rpm") * 2 * Math.PI * Numbers.positive(radius, "radius") / 60;
  }

  public static double tireForce(double speed, double wheelSpeed, double traction, double torque,
      double radius) {
    Numbers.finite(speed, "speed"); Numbers.finite(wheelSpeed, "wheel speed");
    double limit = Math.min(Numbers.nonnegative(traction, "traction"),
        Numbers.nonnegative(torque, "torque") / Numbers.positive(radius, "radius"));
    return Math.tanh((wheelSpeed - speed) * 2) * limit;
  }

  public static double lsmForce(double speed, double target, double maximumForce) {
    Numbers.finite(speed, "speed"); Numbers.finite(target, "target");
    return Numbers.clamp((target - speed) * 4, -1, 1)
        * Numbers.nonnegative(maximumForce, "maximum force");
  }

  public static double limForce(double speed, double fieldSpeed, double maximumForce) {
    Numbers.finite(speed, "speed"); Numbers.finite(fieldSpeed, "field speed");
    double slip = (fieldSpeed - speed) / Math.max(1, Math.abs(fieldSpeed));
    // Declared game response: peak thrust at slip 0.25, falling with excessive slip.
    return (2 * slip / .25) / (1 + Math.pow(slip / .25, 2))
        * Numbers.nonnegative(maximumForce, "maximum force");
  }

  public static double eddyForce(double speed, double coefficient, double peakSpeed) {
    Numbers.finite(speed, "speed");
    return -Numbers.nonnegative(coefficient, "coefficient") * speed
        / (1 + Math.pow(speed / Numbers.positive(peakSpeed, "peak speed"), 2));
  }

  public static double brakeForce(double speed, double mass, double maximumForce, double dt) {
    Numbers.finite(speed, "speed");
    return -Math.signum(speed) * Math.min(Numbers.nonnegative(maximumForce, "maximum force"),
        Math.abs(speed) * Numbers.positive(mass, "mass") / Numbers.positive(dt, "dt"));
  }

  public static double accelerationStep(double previous, double requested, double jerk, double dt) {
    Numbers.finite(previous, "previous acceleration"); Numbers.finite(requested, "acceleration");
    double step = Numbers.positive(jerk, "jerk") * Numbers.positive(dt, "dt");
    return Numbers.clamp(requested, previous - step, previous + step);
  }

  /** Ramp tire demand upward, but immediately respect a reduced traction/slip envelope. */
  public static double tireAccelerationStep(double previous, double requested, double ramp, double dt) {
    double candidate = accelerationStep(previous, requested, ramp, dt);
    // Stored actuator history cannot push against the direction of physical wheel slip.
    if (candidate * requested <= 0) return 0;
    return Math.copySign(Math.min(Math.abs(candidate), Math.abs(requested)), requested);
  }

  public static double incrementalWork(double mass, double speed, double force, double dt) {
    Numbers.positive(mass, "mass"); Numbers.finite(speed, "speed");
    Numbers.finite(force, "force"); Numbers.positive(dt, "dt");
    return force * (speed + .5 * force / mass * dt) * dt;
  }

  /** Maximum propulsion magnitude affordable over this step, including work from rest. */
  public static double affordableForce(double requested, double speed, double mass, double work,
      double dt) {
    Numbers.finite(requested, "force"); Numbers.finite(speed, "speed");
    Numbers.positive(mass, "mass"); Numbers.nonnegative(work, "work"); Numbers.positive(dt, "dt");
    if (incrementalWork(mass, speed, requested, dt) <= work) return requested;
    double low = 0, high = Math.abs(requested), sign = Math.signum(requested);
    for (int i = 0; i < 60; i++) {
      double candidate = .5 * (low + high);
      if (incrementalWork(mass, speed, sign * candidate, dt) <= work) low = candidate;
      else high = candidate;
    }
    return sign * low;
  }
}
