package dev.coasterslaunchsystems;

import dev.coasterslaunchsystems.math.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MechanismMathTest {
  @Test void eddyBrakeCannotLaunchReverseOrHoldAtRest() {
    for (double speed : new double[] {-100, -20, -1, 0, 1, 20, 100}) {
      double force = DriveMath.eddyForce(speed, 8, 15);
      assertTrue(force * speed <= 0);
      assertEquals(-force, DriveMath.eddyForce(-speed, 8, 15), 1e-10);
    }
    assertEquals(0, DriveMath.eddyForce(0, 8, 15), 1e-12);
  }

  @Test void frictionBrakeCannotReverseVelocityWithinAStep() {
    for (double dt : new double[] {.05, .01, .0025})
      for (double speed : new double[] {-25, -.01, 0, .01, 25}) {
        double result = speed + DriveMath.brakeForce(speed, 13, 1000, dt) / 13 * dt;
        assertTrue(result * speed >= -1e-12);
        assertTrue(Math.abs(result) <= Math.abs(speed) + 1e-12);
      }
  }

  @Test void tireSpeedAndTorqueHavePhysicalLimits() {
    assertEquals(Math.PI, DriveMath.wheelSpeed(60, .5), 1e-12);
    assertTrue(DriveMath.tireForce(5, 2, 100, 10, .5) < 0);
    assertTrue(Math.abs(DriveMath.tireForce(0, 20, 100, 10, .5)) <= 20);
    assertEquals(0, DriveMath.tireForce(2, 2, 100, 10, .5));
  }

  @Test void inductionHasSlipCurveDistinctFromSynchronous() {
    double field = 20;
    assertEquals(0, DriveMath.limForce(field, field, 100));
    assertTrue(DriveMath.limForce(15, field, 100) > DriveMath.limForce(0, field, 100));
    assertNotEquals(DriveMath.limForce(0, field, 100), DriveMath.lsmForce(0, field, 100));
    assertTrue(DriveMath.limForce(25, field, 100) < 0);
  }

  @Test void startingFromRestConsumesFiniteEnergyWithoutDivisionBySpeed() {
    double force = DriveMath.affordableForce(1000, 0, 20, .5, .01);
    assertEquals(.5, DriveMath.incrementalWork(20, 0, force, .01), 1e-10);
    assertEquals(0, DriveMath.affordableForce(1000, 0, 20, 0, .01), 1e-8);
  }

  @Test void jerkLimiterIsTimestepConsistent() {
    double coarse = 0, fine = 0;
    for (int i = 0; i < 20; i++) coarse = DriveMath.accelerationStep(coarse, 10, 4, .05);
    for (int i = 0; i < 100; i++) fine = DriveMath.accelerationStep(fine, 10, 4, .01);
    assertEquals(4, coarse, 1e-10); assertEquals(coarse, fine, 1e-10);
  }

  @Test void tireActuatorHistoryCannotExceedTractionOrPushAgainstSlip() {
    for (double previous : new double[] {-2, -.1, 0, .1, 2})
      for (double request : new double[] {-2, -.01, 0, .01, 2}) {
        double result = DriveMath.tireAccelerationStep(previous, request, 4, .025);
        assertTrue(result * request >= 0);
        assertTrue(Math.abs(result) <= Math.abs(request));
      }
    assertEquals(0, DriveMath.tireAccelerationStep(2, -.01, 4, .025));
    assertEquals(.1, DriveMath.tireAccelerationStep(0, 2, 4, .025), 1e-12);
  }

  @Test void overheatedBrakeDeratesAndCoolsWithoutCreatingEnergy() {
    var heat = new BrakeHeat(100, 10, 0);
    heat.absorb(150); assertTrue(heat.overheated()); assertEquals(.5, heat.forceFraction());
    heat.cool(5); assertEquals(100, heat.heat()); assertEquals(1, heat.forceFraction());
    heat.cool(100); assertEquals(0, heat.heat());
  }

  @Test void invalidNumericInputsAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> DriveMath.wheelSpeed(Double.NaN, .5));
    assertThrows(IllegalArgumentException.class, () -> DriveMath.brakeForce(1, 0, 2, .01));
    assertThrows(IllegalArgumentException.class, () -> new EnergyBuffer(10, 11));
    assertThrows(IllegalArgumentException.class, () -> new EnergyBuffer(10, -1));
  }
}
