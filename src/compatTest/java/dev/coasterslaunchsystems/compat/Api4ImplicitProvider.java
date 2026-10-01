package dev.coasterslaunchsystems.compat;
import dev.coasterslaunchsystems.api.*;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
/** A real old binary inherits API-4's default version method and declares no capture operations. */
public final class Api4ImplicitProvider implements VehicleMotionProvider {
  public String id() { return "test:api4_implicit_binary"; }
  public Optional<MotionVehicle> resolve(ServerLevel level, UUID chassis) { return Optional.empty(); }
}
