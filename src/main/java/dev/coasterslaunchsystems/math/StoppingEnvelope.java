package dev.coasterslaunchsystems.math;

/** Conservative constant-force certificate. Passive magnets are deliberately given no hold credit. */
public record StoppingEnvelope(double mass, double speed, double force, double adverseGravity,
    double responseSeconds, double trainLength) {
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
