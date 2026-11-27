package dev.coasterslaunchsystems;

import dev.coasterslaunchsystems.math.StoppingEnvelope;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StoppingEnvelopeTest {
  @Test void touchingFinProjectionSeamsDoNotAdmitPhysicalGaps() {
    assertTrue(StoppingEnvelope.coverageConnects(18.00000005045945, 18.000001538574388));
    assertTrue(StoppingEnvelope.coverageConnects(28.0, 28.000002014074244));
    assertTrue(StoppingEnvelope.coverageConnects(18, 17.5));
    assertFalse(StoppingEnvelope.coverageConnects(18, 18.001));
    assertFalse(StoppingEnvelope.coverageConnects(18, 20));
    assertFalse(StoppingEnvelope.coverageConnects(Double.NaN, 18));
    assertFalse(StoppingEnvelope.coverageConnects(18, Double.POSITIVE_INFINITY));
  }

  @Test void loadedTrainLengthAndResponseConsumeRealCoverage() {
    var envelope = new StoppingEnvelope(64, 4, 128, 0, .1, 12);
    assertEquals(17.4, envelope.requiredLength(), 1e-10);
    assertFalse(envelope.fits(17)); assertTrue(envelope.fits(17.4));
    assertTrue(new StoppingEnvelope(32, 4, 128, 0, .1, 12).requiredLength() < envelope.requiredLength());
  }
  @Test void sharedImpulseWorkIncludesCrossTermsWithoutDissipationCredit() {
    double a = dev.coasterslaunchsystems.math.DriveMath.incrementalWorkShare(2, 1, 20, 40, .025);
    double aggregate = dev.coasterslaunchsystems.math.DriveMath.incrementalWork(2, 1, 40, .025);
    assertEquals(aggregate, 2 * a, 1e-12);
    assertTrue(2 * a > 2 * dev.coasterslaunchsystems.math.DriveMath.incrementalWork(2, 1, 20, .025));
    double drive = dev.coasterslaunchsystems.math.DriveMath.incrementalWorkShare(2, 1, 20, 10, .025);
    double brake = dev.coasterslaunchsystems.math.DriveMath.incrementalWorkShare(2, 1, -10, 10, .025);
    assertEquals(dev.coasterslaunchsystems.math.DriveMath.incrementalWork(2, 1, 10, .025), drive + brake, 1e-12);
    assertTrue(Math.max(0, drive) + Math.max(0, brake) > drive + brake);
  }
  @Test void gravityAndResponseCannotInventAHoldRating() {
    assertFalse(new StoppingEnvelope(64, 2, 128, 2, .1, 8).fits(1000));
    var flat = new StoppingEnvelope(64, 2, 256, 0, .1, 8);
    assertTrue(new StoppingEnvelope(64, 2, 256, 1, .1, 8).requiredLength() > flat.requiredLength());
    assertThrows(IllegalArgumentException.class, () -> new StoppingEnvelope(64, Double.NaN, 128, 0, .1, 8));
  }
}
