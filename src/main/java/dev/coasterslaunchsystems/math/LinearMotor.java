package dev.coasterslaunchsystems.math;

/** Declared game-scale motor response. Electrical losses are heat, never returned shaft work. */
public final class LinearMotor {
  public static final double CAPACITY = 2048;
  private LinearMotor() {}

  public static double chargingWork(double rpm, double dt) {
    Numbers.finite(rpm, "rpm"); Numbers.positive(dt, "dt");
    return Math.abs(rpm) < 1 || Math.abs(rpm) > 128 ? 0 : .9 * 8 * Math.abs(rpm) * 2 * Math.PI / 60 * dt;
  }
  public static double force(Mechanism mechanism, double speed, double target, double limit) {
    return switch (mechanism) {
      case LSM -> DriveMath.lsmForce(speed, target, limit);
      case LIM -> DriveMath.limForce(speed, target, limit);
      default -> throw new IllegalArgumentException("Not a linear motor");
    };
  }
  public static double efficiency(Mechanism mechanism, double speed, double target) {
    Numbers.finite(speed, "speed"); Numbers.finite(target, "target");
    return switch (mechanism) {
      case LSM -> .9;
      case LIM -> .75 / (1 + Math.abs((target - speed) / Math.max(1, Math.abs(target))));
      default -> throw new IllegalArgumentException("Not a linear motor");
    };
  }
  public record Transfer(double energy, double heat) {}
  /** Conservative local-section ready threshold. Each cabinet must independently fund the
   * declared loaded launch from rest, with worst forward slip and ramp/copper-loss margin. */
  public static double launchReserve(Mechanism mechanism, double mass, double target, double acceleration, double jerk) {
    Numbers.positive(mass, "mass"); Numbers.positive(target, "forward target");
    Numbers.positive(acceleration, "acceleration"); Numbers.positive(jerk, "jerk");
    double ratedAcceleration = Math.min(acceleration, 20 / mass);
    double duration = target / ratedAcceleration + ratedAcceleration / jerk;
    double efficiency = mechanism == Mechanism.LSM ? .9 : mechanism == Mechanism.LIM ? .375 : 0;
    if (efficiency == 0) throw new IllegalArgumentException("Not a linear motor");
    return .5 * mass * target * target / efficiency + ((mechanism == Mechanism.LIM ? .05 : .025) * 400 + 2) * duration + 32;
  }
  /** Positive impulse work, excitation and copper losses consume the finite buffer.
   * Negative impulse work is dissipated: it cannot fund another contact or recharge a store. */
  public static Transfer transfer(Mechanism mechanism, double speed, double target, double force, double work, double dt) {
    Numbers.finite(force, "force"); Numbers.finite(work, "work"); Numbers.positive(dt, "dt");
    double useful = Math.max(0, work);
    double losses = useful * (1 / efficiency(mechanism, speed, target) - 1)
        + (mechanism == Mechanism.LIM ? .05 : .025) * force * force * dt + .1 * Math.abs(force) * dt;
    return new Transfer(useful + losses, Math.max(0, -work) + losses);
  }
}
