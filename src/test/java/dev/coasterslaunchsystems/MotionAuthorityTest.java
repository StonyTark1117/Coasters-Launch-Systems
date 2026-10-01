package dev.coasterslaunchsystems;

import static org.junit.jupiter.api.Assertions.*;

import dev.coasterslaunchsystems.api.MotionVehicle;
import dev.coasterslaunchsystems.math.Mechanism;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class MotionAuthorityTest {
  private final UUID body = new UUID(0, 1);
  @Test void legacySnapshotsCannotGrantUnratedBraking() {
    var old = new MotionVehicle("legacy", body, List.of(body), List.of(body), Set.of(Mechanism.FIN_BRAKE), true, true, "", 2, 256);
    var api2 = new MotionVehicle("legacy", body, List.of(body), List.of(body), Set.of(Mechanism.FIN_BRAKE), true, true, true, "", 2, 256);
    for (var snapshot : List.of(old, api2)) {
      assertTrue(snapshot.propulsionAllowed());
      assertFalse(snapshot.brakingAllowed());
      assertEquals(0, snapshot.maximumBrakeForce());
    }
  }
  @Test void brakeAuthorityHasAnIndependentFinitePositiveRating() {
    var snapshot = rated(32);
    assertTrue(snapshot.brakingAllowed());
    assertEquals(32, snapshot.maximumBrakeForce());
    assertEquals(256, snapshot.maximumForce());
    for (double invalid : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY})
      assertThrows(IllegalArgumentException.class, () -> rated(invalid));
  }
  private MotionVehicle rated(double brakeForce) {
    return new MotionVehicle("rated", body, List.of(body), List.of(body), Set.of(Mechanism.FIN_BRAKE),
        true, true, true, "", 2, 256, brakeForce);
  }
}
