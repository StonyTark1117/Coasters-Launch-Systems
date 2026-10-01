package dev.coasterslaunchsystems.api;

import dev.coasterslaunchsystems.math.Mechanism;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Bodies belong to this articulated cart; consist lists every coupled chassis, including this one. */
public record MotionVehicle(String provider, UUID chassis, List<UUID> bodies, List<UUID> consist,
    Set<Mechanism> interfaces, boolean dispatchReady, boolean propulsionAllowed,
    String exclusiveOperator, double maximumAcceleration, double maximumForce) {
  public MotionVehicle {
    Objects.requireNonNull(provider); Objects.requireNonNull(chassis);
    bodies = List.copyOf(bodies); consist = List.copyOf(consist); interfaces = Set.copyOf(interfaces);
    if (!bodies.contains(chassis) || bodies.stream().distinct().count() != bodies.size()
        || !consist.contains(chassis) || consist.stream().distinct().count() != consist.size())
      throw new IllegalArgumentException("Invalid or duplicated native body/consist identities");
    dev.coasterslaunchsystems.math.Numbers.positive(maximumAcceleration, "acceleration limit");
    dev.coasterslaunchsystems.math.Numbers.positive(maximumForce, "force limit");
    Objects.requireNonNull(exclusiveOperator);
  }
}
