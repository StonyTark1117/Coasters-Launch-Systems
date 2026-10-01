package dev.coasterslaunchsystems.api;

import dev.coasterslaunchsystems.math.Mechanism;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Bodies belong to this articulated cart; consist lists every coupled chassis, including this one. */
public record MotionVehicle(String provider, UUID chassis, List<UUID> bodies, List<UUID> consist,
    Set<Mechanism> interfaces, boolean dispatchReady, boolean propulsionAllowed,
    boolean brakingAllowed, String exclusiveOperator, double maximumAcceleration, double maximumForce, double maximumBrakeForce) {
  /** Legacy snapshots carry no authority for new brake equipment. */
  public MotionVehicle(String provider, UUID chassis, List<UUID> bodies, List<UUID> consist,
      Set<Mechanism> interfaces, boolean dispatchReady, boolean propulsionAllowed,
      String exclusiveOperator, double maximumAcceleration, double maximumForce) {
    this(provider, chassis, bodies, consist, interfaces, dispatchReady, propulsionAllowed,
        false, exclusiveOperator, maximumAcceleration, maximumForce, 0);
  }
  /** API-2 snapshots lack an explicit brake rating and therefore grant no API-3 brake authority. */
  public MotionVehicle(String provider, UUID chassis, List<UUID> bodies, List<UUID> consist,
      Set<Mechanism> interfaces, boolean dispatchReady, boolean propulsionAllowed,
      boolean brakingAllowed, String exclusiveOperator, double maximumAcceleration, double maximumForce) {
    this(provider, chassis, bodies, consist, interfaces, dispatchReady, propulsionAllowed,
        false, exclusiveOperator, maximumAcceleration, maximumForce, 0);
  }
  public MotionVehicle {
    Objects.requireNonNull(provider); Objects.requireNonNull(chassis);
    bodies = List.copyOf(bodies); consist = List.copyOf(consist); interfaces = Set.copyOf(interfaces);
    if (!bodies.contains(chassis) || bodies.stream().distinct().count() != bodies.size()
        || !consist.contains(chassis) || consist.stream().distinct().count() != consist.size())
      throw new IllegalArgumentException("Invalid or duplicated native body/consist identities");
    dev.coasterslaunchsystems.math.Numbers.positive(maximumAcceleration, "acceleration limit");
    dev.coasterslaunchsystems.math.Numbers.positive(maximumForce, "force limit");
    dev.coasterslaunchsystems.math.Numbers.nonnegative(maximumBrakeForce, "brake force limit");
    if (brakingAllowed && maximumBrakeForce == 0) throw new IllegalArgumentException("Braking authority requires a positive rating");
    Objects.requireNonNull(exclusiveOperator);
  }
}
