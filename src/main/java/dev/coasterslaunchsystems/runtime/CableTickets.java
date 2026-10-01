package dev.coasterslaunchsystems.runtime;

import dev.coasterslaunchsystems.LaunchSystems;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

/** Registered before world load so Sable can decode the carriage's saved loading ticket. */
public final class CableTickets {
  public static final SubLevelLoadingTicketType<UUID> CARRIAGE = SubLevelLoadingTicketType.create(
      ResourceLocation.fromNamespaceAndPath(LaunchSystems.ID, "cable_carriage"), UUIDUtil.CODEC);
  private CableTickets() {}
  public static void bootstrap() { /* Force static registration before Sable reads world tickets. */ }
}
