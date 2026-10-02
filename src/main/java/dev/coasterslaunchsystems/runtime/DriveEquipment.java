package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.content.EquipmentCabinetEntity;
import dev.coasterslaunchsystems.math.DriveMath;
import dev.coasterslaunchsystems.math.Mechanism;

/** A local section's forward speed certificate includes commanded fields, not just shaft RPM. */
public final class DriveEquipment {
  private DriveEquipment() {}
  public static double speed(EquipmentCabinetEntity module) {
    return module.mechanism().linear() || module.mechanism().cable()
        ? module.targetSpeed() : DriveMath.wheelSpeed(module.getSpeed(), .4);
  }
  public static boolean forwardPowered(EquipmentCabinetEntity module) {
    return module.mechanism().linear() ? module.linearPowered() && module.targetSpeed() > 0
        : module.getSpeed() >= 1 && module.getSpeed() <= 128;
  }
}
