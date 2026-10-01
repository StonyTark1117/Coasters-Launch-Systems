package dev.coasterslaunchsystems.math;

import java.util.function.DoubleFunction;

/** Distance lookup in a curve's fixed parameter direction, independent of train velocity. */
public final class ArcLengthTable {
  private final double[] distances;
  public record Point(double x, double y, double z) {
    public double distance(Point other) {
      double dx = x - other.x, dy = y - other.y, dz = z - other.z;
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
  }

  public ArcLengthTable(int segments, DoubleFunction<Point> sample) {
    if (segments < 16 || segments > 4096) throw new IllegalArgumentException("Invalid sample count");
    distances = new double[segments + 1];
    var previous = sample.apply(0);
    validate(previous);
    for (int i = 1; i <= segments; i++) {
      var point = sample.apply((double) i / segments);
      validate(point);
      distances[i] = distances[i - 1] + point.distance(previous);
      previous = point;
    }
    Numbers.positive(length(), "curve length");
  }

  private static void validate(Point point) {
    if (point == null || !Double.isFinite(point.x() * point.x() + point.y() * point.y() + point.z() * point.z()))
      throw new IllegalArgumentException("Invalid curve sample");
  }

  public double length() { return distances[distances.length - 1]; }

  public double distance(double parameter) {
    Numbers.finite(parameter, "curve parameter");
    if (parameter < 0 || parameter > 1) throw new IllegalArgumentException("Parameter outside curve");
    double index = parameter * (distances.length - 1);
    int lower = Math.min((int) index, distances.length - 2);
    return distances[lower] + (distances[lower + 1] - distances[lower]) * (index - lower);
  }

  public double parameter(double distance) {
    Numbers.finite(distance, "curve distance");
    if (distance < 0 || distance > length()) throw new IllegalArgumentException("Distance outside curve");
    int lower = 0, upper = distances.length - 1;
    while (upper - lower > 1) {
      int middle = (lower + upper) >>> 1;
      if (distances[middle] <= distance) lower = middle; else upper = middle;
    }
    double width = distances[upper] - distances[lower];
    return (lower + (width == 0 ? 0 : (distance - distances[lower]) / width))
        / (distances.length - 1);
  }
}
