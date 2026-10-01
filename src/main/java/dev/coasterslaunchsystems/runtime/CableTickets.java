package dev.coasterslaunchsystems.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.coasterslaunchsystems.LaunchSystems;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

/** Registered before world load so Sable can decode the carriage's saved loading ticket. */
public final class CableTickets {
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
  private CableTickets() {}
  public static void bootstrap() { /* Force static registration before Sable reads world tickets. */ }
}
