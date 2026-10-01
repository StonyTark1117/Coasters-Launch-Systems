package dev.coasterslaunchsystems.math;

/** Occupancy includes the whole bearing footprint and the distance swept this physics step. */
public record SpanInterval(double start, double end) {
  public SpanInterval {
    Numbers.nonnegative(start, "span start");
    Numbers.positive(end, "span end");
    if (end <= start) throw new IllegalArgumentException("Empty or reversed span");
  }

  public boolean intersects(double previous, double current, double halfFootprint) {
    Numbers.finite(previous, "previous distance");
    Numbers.finite(current, "current distance");
    Numbers.nonnegative(halfFootprint, "bearing footprint");
    return Math.max(previous, current) + halfFootprint >= start
        && Math.min(previous, current) - halfFootprint <= end;
  }

  public boolean contains(double distance, double halfFootprint) {
    return intersects(distance, distance, halfFootprint);
  }
}
