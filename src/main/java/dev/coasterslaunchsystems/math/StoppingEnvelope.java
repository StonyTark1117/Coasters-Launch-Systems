package dev.coasterslaunchsystems.math;

/** Conservative constant-force certificate. Passive magnets are deliberately given no hold credit. */
public record StoppingEnvelope(double mass, double speed, double force, double adverseGravity,
    double responseSeconds, double trainLength) {
  // Directed curve projection can separate touching block-sized pads by a few micrometres.
  // This joins numerical seams only; it does not add length or braking force to the bank.
  private static final double COVERAGE_GEOMETRY_TOLERANCE = 1e-4;

  public static boolean coverageConnects(double previousEnd, double nextStart) {
    return Double.isFinite(previousEnd) && Double.isFinite(nextStart)
        && nextStart <= previousEnd + COVERAGE_GEOMETRY_TOLERANCE;
  }

  public StoppingEnvelope {
    Numbers.positive(mass, "loaded mass"); Numbers.nonnegative(speed, "speed");
    Numbers.positive(force, "brake force"); Numbers.nonnegative(adverseGravity, "adverse gravity");
    Numbers.nonnegative(responseSeconds, "response"); Numbers.nonnegative(trainLength, "train length");
  }
  public double requiredLength() {
    double acceleration = force / mass - adverseGravity;
    if (acceleration <= 0) return Double.POSITIVE_INFINITY;
    double entrySpeed = speed + adverseGravity * responseSeconds;
    return trainLength + speed * responseSeconds + .5 * adverseGravity * responseSeconds * responseSeconds
        + entrySpeed * entrySpeed / (2 * acceleration) + 1;
  }
  public boolean fits(double usableLength) { return Numbers.nonnegative(usableLength, "usable length") >= requiredLength(); }
}
