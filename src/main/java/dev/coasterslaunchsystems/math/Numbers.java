package dev.coasterslaunchsystems.math;

public final class Numbers {
  private Numbers() {}

  public static double finite(double value, String name) {
    if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
    return value;
  }

  public static double nonnegative(double value, String name) {
    if (finite(value, name) < 0) throw new IllegalArgumentException(name + " must be nonnegative");
    return value;
  }

  public static double positive(double value, String name) {
    if (finite(value, name) <= 0) throw new IllegalArgumentException(name + " must be positive");
    return value;
  }

  public static double clamp(double value, double minimum, double maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }
}
