package dev.coasterslaunchsystems.math;

public final class FlywheelStore extends EnergyBuffer {
  private final double inertia;

  public FlywheelStore(double inertia, double maximumOmega, double omega) {
    super(.5 * Numbers.positive(inertia, "inertia")
            * Math.pow(Numbers.positive(maximumOmega, "maximum speed"), 2),
        .5 * inertia * Math.pow(Numbers.nonnegative(omega, "speed"), 2));
    this.inertia = inertia;
  }

  public double omega() { return Math.sqrt(2 * energy() / inertia); }
  public double rimSpeed(double radius) { return omega() * Numbers.positive(radius, "radius"); }
}
