package dev.coasterslaunchsystems;

import dev.coasterslaunchsystems.math.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StoredEnergyTest {
  @Test void flywheelLosesSpeedAsRealWorkIsTransferred() {
    var store = new FlywheelStore(4, 50, 20);
    assertEquals(800, store.energy());
    assertEquals(600, store.draw(600)); assertEquals(10, store.omega(), 1e-12);
    assertEquals(200, store.draw(1000)); assertEquals(0, store.omega());
    assertEquals(5000, store.charge(9000)); assertEquals(50, store.omega());
  }

  @Test void hydraulicOilReturnsAndPressureFallsOnDischarge() {
    var store = new HydraulicAccumulator(4, 2, 100, 10, 1.4, 0);
    store.charge(store.capacity());
    assertEquals(2, store.oilVolume(), 1e-10);
    double initial = store.pressure();
    store.draw(store.capacity() * .75);
    assertTrue(store.pressure() < initial); assertTrue(store.oilVolume() < 2);
    store.draw(store.capacity());
    assertEquals(0, store.oilVolume(), 1e-10); assertEquals(0, store.force(1));
  }

  @Test void compressedAirHasASeparateExpansionAndRechargeModel() {
    var store = new AirReceiver(4, 10, 200, 0);
    store.charge(store.capacity());
    assertEquals(200, store.pressure(), 1e-8);
    double closed = store.force(1, 0), expanded = store.force(1, 4);
    assertTrue(expanded > 0 && expanded < closed);
    store.draw(store.capacity() * .5); assertTrue(store.pressure() < 200);
    store.draw(store.capacity()); assertEquals(10, store.pressure(), 1e-6);
    assertEquals(0, store.force(1, 0), 1e-6);
  }

  @Test void repeatedTransferCannotCreateWork() {
    EnergyStore[] stores = {new EnergyBuffer(1000, 0), new FlywheelStore(4, 50, 0),
      new HydraulicAccumulator(4, 2, 100, 10, 1.4, 0), new AirReceiver(4, 10, 200, 0)};
    for (var store : stores) {
      double charged = store.charge(200);
      double drawn = store.draw(75) + store.draw(75) + store.draw(100);
      assertEquals(charged, drawn, 1e-10); assertEquals(0, store.energy(), 1e-10);
      assertEquals(0, store.draw(100));
      assertThrows(IllegalArgumentException.class, () -> store.charge(-1));
      assertThrows(IllegalArgumentException.class, () -> store.draw(Double.NaN));
    }
  }
}
