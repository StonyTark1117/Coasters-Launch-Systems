package dev.coasterslaunchsystems;

import dev.coasterslaunchsystems.math.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinearMotorTest {
  @Test void distinctSlipCurveAndLosses() {
    assertEquals(20, LinearMotor.force(Mechanism.LSM, 0, 4, 20));
    assertTrue(LinearMotor.force(Mechanism.LIM, 0, 4, 20) < 10);
    assertEquals(20, LinearMotor.force(Mechanism.LIM, 3, 4, 20));
    assertEquals(0, LinearMotor.force(Mechanism.LIM, 4, 4, 20));
    assertTrue(LinearMotor.transfer(Mechanism.LIM, 0, 4, 10, 5, .05).energy()
        > LinearMotor.transfer(Mechanism.LSM, 0, 4, 10, 5, .05).energy());
    for (var mechanism : new Mechanism[] {Mechanism.LSM, Mechanism.LIM})
      assertEquals(-LinearMotor.force(mechanism, 2, 4, 20), LinearMotor.force(mechanism, -2, -4, 20), 1e-12);
  }
  @Test void activeBrakingConsumesEnergyAndCannotRefillBuffer() {
    for (var mechanism : new Mechanism[] {Mechanism.LSM, Mechanism.LIM}) {
      var transfer = LinearMotor.transfer(mechanism, 3, 0, -10, -4, .05);
      assertTrue(transfer.energy() > 0); assertEquals(4 + transfer.energy(), transfer.heat(), 1e-12);
      var buffer = new EnergyBuffer(100, 10); buffer.draw(transfer.energy()); assertTrue(buffer.energy() < 10);
      var forward = LinearMotor.transfer(mechanism, 3, 6, 10, 4, .05);
      assertEquals(forward.energy(), 4 + forward.heat(), 1e-12);
      assertEquals(0, LinearMotor.transfer(mechanism, 0, 0, 0, 0, .05).energy());
    }
  }
  @Test void onlyBoundedKineticSupplyCharges() {
    assertEquals(0, LinearMotor.chargingWork(0, .05)); assertEquals(0, LinearMotor.chargingWork(129, .05));
    assertEquals(LinearMotor.chargingWork(64, .05), LinearMotor.chargingWork(-64, .05));
    assertEquals(.9 * 8 * 64 * 2 * Math.PI / 60 * .05, LinearMotor.chargingWork(64, .05));
    assertThrows(IllegalArgumentException.class, () -> LinearMotor.chargingWork(Double.NaN, .05));
    assertThrows(IllegalArgumentException.class, () -> LinearMotor.chargingWork(64, 0));
    assertThrows(IllegalArgumentException.class, () -> LinearMotor.force(Mechanism.TIRE, 0, 1, 10));
  }
  @Test void localLaunchReserveIncludesWholeTrainAndDistinctWorstLosses() {
    double lsm = LinearMotor.launchReserve(Mechanism.LSM, 56, 3, 2, 4);
    double lim = LinearMotor.launchReserve(Mechanism.LIM, 56, 3, 2, 4);
    assertTrue(lsm > .5 * 56 * 9 / .9 && lim > lsm && lim < LinearMotor.CAPACITY);
    assertTrue(LinearMotor.launchReserve(Mechanism.LSM, 64, 12, 2, 4) > LinearMotor.CAPACITY);
  }
}
