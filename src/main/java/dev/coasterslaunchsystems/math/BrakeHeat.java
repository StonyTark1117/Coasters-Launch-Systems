package dev.coasterslaunchsystems.math;

public final class BrakeHeat {
  private final double capacity, coolingPower;
  private double heat;
  public BrakeHeat(double capacity, double coolingPower, double heat) {
    this.capacity = Numbers.positive(capacity, "heat capacity");
    this.coolingPower = Numbers.nonnegative(coolingPower, "cooling power");
    this.heat = Numbers.nonnegative(heat, "heat");
  }
  public double heat() { return heat; }
  public double forceFraction() { return Numbers.clamp(2 - heat / capacity, .1, 1); }
  public boolean overheated() { return heat > capacity; }
  public void absorb(double work) { heat += Numbers.nonnegative(work, "braking work"); }
  public void cool(double dt) { heat = Math.max(0, heat - coolingPower * Numbers.positive(dt, "dt")); }
}
