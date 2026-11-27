package dev.coasterslaunchsystems;

import dev.coasterslaunchsystems.math.Mechanism;
import dev.coasterslaunchsystems.track.EquipmentMounting;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentMountingTest {
  @Test void ordinaryThreeBlockReachRemainsAvailableForEveryMechanism() {
    for (var mechanism : Mechanism.values()) {
      assertTrue(EquipmentMounting.withinReach(mechanism, 3, 0, 0, false), mechanism.name());
      assertTrue(EquipmentMounting.withinReach(mechanism, 0, -3, 0, false), mechanism.name());
      assertTrue(EquipmentMounting.withinReach(mechanism, 0, 0, -3, false), mechanism.name());
      assertFalse(EquipmentMounting.withinReach(mechanism, 0, Math.nextUp(3.0), 0, false), mechanism.name());
    }
  }

  @Test void arrowBelowSweepMountRequiresCurrentDockAuthentication() {
    // Rail y130.3125 minus cabinet center y126.5, with one block lateral offset.
    // This tests the supplied authentication value, not the controller's recovery lifecycle.
    for (var mechanism : new Mechanism[] {Mechanism.TIRE, Mechanism.FIN_BRAKE, Mechanism.LSM, Mechanism.LIM}) {
      assertTrue(EquipmentMounting.withinReach(mechanism, 1, 3.8125, 0, true), mechanism.name());
      assertFalse(EquipmentMounting.withinReach(mechanism, 1, 3.8125, 0, false), mechanism.name());
      assertTrue(EquipmentMounting.withinReach(mechanism, 1, 3.8125, 0, true), mechanism.name());
      assertTrue(EquipmentMounting.withinReach(mechanism, 0, 3.8125, -1, true), mechanism.name());
    }
  }

  @Test void dockExtensionHasAFourBlockRadialLimit() {
    assertTrue(EquipmentMounting.withinReach(Mechanism.TIRE, 0, 4, 0, true));
    assertFalse(EquipmentMounting.withinReach(Mechanism.TIRE, 0, Math.nextUp(4.0), 0, true));
    // Four vertical blocks plus one lateral block exceeds four total blocks.
    assertFalse(EquipmentMounting.withinReach(Mechanism.TIRE, 1, 4, 0, true));
  }

  @Test void dockExtensionIsBelowRailAndWithinNominalReachPlusSocketTolerance() {
    assertTrue(EquipmentMounting.withinReach(Mechanism.FIN_BRAKE, 1, 3.5, 0, true));
    assertFalse(EquipmentMounting.withinReach(Mechanism.FIN_BRAKE, 1.026, 3.5, 0, true));
    assertFalse(EquipmentMounting.withinReach(Mechanism.FIN_BRAKE, 0, 3.5, -1.026, true));
    assertFalse(EquipmentMounting.withinReach(Mechanism.FIN_BRAKE, .8, 3.5, .8, true));
    assertFalse(EquipmentMounting.withinReach(Mechanism.FIN_BRAKE, 0, -3.8125, 0, true));
    assertFalse(EquipmentMounting.withinReach(Mechanism.FIN_BRAKE, 3.5, 0, 0, true));
  }

  @Test void measuredSourceAndReceiverResidualsRequireCurrentAuthentication() {
    for (var mechanism : new Mechanism[] {Mechanism.TIRE, Mechanism.FIN_BRAKE, Mechanism.LSM, Mechanism.LIM}) {
      for (var measured : new double[][] {
          {1.000000026077032, 3.8125000000001137, 2.431455072837707E-7},
          {-1.000030491616144, 3.812499999427871, -4.4938713017472764E-7}}) {
        // Actual combined FD Arrow source and receiving dock diagnostics.
        double dx = measured[0], dy = measured[1], dz = measured[2];
        assertTrue(EquipmentMounting.withinReach(mechanism, dx, dy, dz, true));
        assertTrue(EquipmentMounting.withinReach(mechanism, dz, dy, dx, true));
        assertFalse(EquipmentMounting.withinReach(mechanism, dx, dy, dz, false));
      }
    }
  }

  @Test void horizontalEnvelopeMatchesFdLockedSocketToleranceWithoutExpandingOtherBounds() {
    for (var mechanism : new Mechanism[] {Mechanism.TIRE, Mechanism.FIN_BRAKE, Mechanism.LSM, Mechanism.LIM}) {
      // FD 1.6 admits <=0.025m displacement at each actual locked socket.
      for (int side : new int[] {-1, 1}) {
        assertTrue(EquipmentMounting.withinReach(mechanism, side * 1.025, 3.8125, 0, true));
        assertTrue(EquipmentMounting.withinReach(mechanism, 0, 3.8125, side * 1.025, true));
        assertFalse(EquipmentMounting.withinReach(mechanism, side * 1.025, 3.8125, 0, false));
        assertFalse(EquipmentMounting.withinReach(mechanism, side * Math.nextUp(1.025), 3.8125, 0, true));
        assertFalse(EquipmentMounting.withinReach(mechanism, 0, 3.8125, side * Math.nextUp(1.025), true));
        assertFalse(EquipmentMounting.withinReach(mechanism, side * 1.0251, 3.8125, 0, true));
        assertFalse(EquipmentMounting.withinReach(mechanism, side * 1.026, 3.8125, 0, true));
      }
      // The margin is radial horizontally, not an independent allowance per axis.
      assertFalse(EquipmentMounting.withinReach(mechanism, .73, 3.8125, .73, true));
      assertFalse(EquipmentMounting.withinReach(mechanism, 1.025, 3.875, 0, true));
      assertFalse(EquipmentMounting.withinReach(mechanism, 1.025, -3.8125, 0, true));
      assertFalse(EquipmentMounting.withinReach(mechanism, 0, Math.nextUp(3.0), 0, false));
    }
  }

  @Test void dockAuthenticationDoesNotExtendUncertifiedMechanisms() {
    for (var mechanism : new Mechanism[] {Mechanism.SKID_BRAKE, Mechanism.EDDY_BRAKE,
        Mechanism.FLYWHEEL, Mechanism.HYDRAULIC, Mechanism.PNEUMATIC})
      assertFalse(EquipmentMounting.withinReach(mechanism, 0, 3.5, 0, true), mechanism.name());
  }

  @Test void nonfiniteCoordinatesCannotCertifyEitherMountingRange() {
    for (boolean authenticated : new boolean[] {false, true})
      for (double invalid : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
          Double.MAX_VALUE}) {
        assertFalse(EquipmentMounting.withinReach(Mechanism.TIRE, invalid, 0, 0, authenticated));
        assertFalse(EquipmentMounting.withinReach(Mechanism.TIRE, 0, invalid, 0, authenticated));
        assertFalse(EquipmentMounting.withinReach(Mechanism.TIRE, 0, 0, invalid, authenticated));
      }
  }
}
