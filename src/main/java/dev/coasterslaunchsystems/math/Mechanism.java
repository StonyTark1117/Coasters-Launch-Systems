package dev.coasterslaunchsystems.math;

/** Physical equipment identities; trim/stop/reverse are operating modes, not brake types. */
public enum Mechanism {
  TIRE, LSM, LIM, FIN_BRAKE, SKID_BRAKE, EDDY_BRAKE, FLYWHEEL, HYDRAULIC, PNEUMATIC;

  public boolean cable() {
    return this == FLYWHEEL || this == HYDRAULIC || this == PNEUMATIC;
  }

  public boolean passiveBrake() {
    return this == FIN_BRAKE || this == SKID_BRAKE || this == EDDY_BRAKE;
  }
}
