package dev.coasterslaunchsystems.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.coasterslaunchsystems.LaunchSystems;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicket;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

/** Registered before world load so Sable can decode the carriage's saved loading ticket. */
public final class CableTickets {
  // Sable 2.0.6 uses record accessors; 2.0.3 exposes the same public data via getters.
  // Resolve once and fail clearly if neither audited API is present.
  private static final Method TICKET_TYPE = accessor("type", "getType");
  private static final Method TICKET_KEY = accessor("key", "getKey");

  private static Method accessor(String current, String legacy) {
    try {
      return SubLevelLoadingTicket.class.getMethod(current);
    } catch (NoSuchMethodException absentCurrent) {
      try { return SubLevelLoadingTicket.class.getMethod(legacy); }
      catch (NoSuchMethodException absentLegacy) {
        throw new IllegalStateException("Unsupported Sable loading-ticket API: " + current, absentLegacy);
      }
    }
  }

  private static Object read(Method accessor, SubLevelLoadingTicket<?> ticket) {
    try { return accessor.invoke(ticket); }
    catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Cannot read Sable loading ticket; cable recovery denied", failure);
    }
  }

  public static SubLevelLoadingTicketType<?> typeOf(SubLevelLoadingTicket<?> ticket) {
    return (SubLevelLoadingTicketType<?>) read(TICKET_TYPE, ticket);
  }

  public static Object keyOf(SubLevelLoadingTicket<?> ticket) {
    return read(TICKET_KEY, ticket);
  }

  /** Dev.18 worlds may still contain UUID-only tickets and must remain decodable. */
  public static final SubLevelLoadingTicketType<UUID> LEGACY_CARRIAGE = SubLevelLoadingTicketType.create(
      ResourceLocation.fromNamespaceAndPath(LaunchSystems.ID, "cable_carriage"), UUIDUtil.CODEC);
  public record Key(UUID carriage, BlockPos cabinet) {}
  public static final Codec<Key> KEY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
      UUIDUtil.CODEC.fieldOf("carriage").forGetter(Key::carriage),
      BlockPos.CODEC.fieldOf("cabinet").forGetter(Key::cabinet)).apply(instance, Key::new));
  /** The cabinet address lets startup restore the passive guide before a falling body drifts. */
  public static final SubLevelLoadingTicketType<Key> CARRIAGE = SubLevelLoadingTicketType.create(
      ResourceLocation.fromNamespaceAndPath(LaunchSystems.ID, "cable_carriage_v2"), KEY_CODEC);
  /** Holds every captured native body through an interrupted catch until physical release. */
  public static final SubLevelLoadingTicketType<Key> CAPTURED_BODY = SubLevelLoadingTicketType.create(
      ResourceLocation.fromNamespaceAndPath(LaunchSystems.ID, "cable_captured_body"), KEY_CODEC);
  private CableTickets() {}
  public static void bootstrap() { /* Force static registration before Sable reads world tickets. */ }
}
