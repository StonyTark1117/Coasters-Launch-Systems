package dev.coasterslaunchsystems.track;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;

/** Observes accepted native guide frames; proximity queries never grant equipment contact. */
public final class EngagedContacts {
  private static final Map<ServerLevel, Map<UUID, Contact>> CONTACTS = new WeakHashMap<>();
  public record Contact(GraphHit hit, int graphRevision, long tick) {}
  private EngagedContacts() {}

  public static void accepted(ServerLevel level, ServerSubLevel body, GraphHit hit) {
    if (hit == null || body.isRemoved() || CoasterCartTrackSnap.isBearingDisengaged(body)) {
      forget(level, body);
      return;
    }
    var graph = CoasterPathGraphManager.get(level);
    if (!graph.edges().contains(hit.edge())) return;
    var entries = CONTACTS.computeIfAbsent(level, ignored -> new HashMap<>());
    // Removed/unloaded bodies cannot leave a permanently growing per-world cache.
    entries.values().removeIf(c -> c.tick() < level.getGameTime() - 2);
    entries.put(body.getUniqueId(), new Contact(hit, graph.revision(), level.getGameTime()));
  }

  public static Optional<Contact> current(ServerLevel level, ServerSubLevel body) {
    var contact = CONTACTS.getOrDefault(level, Map.of()).get(body.getUniqueId());
    var graph = CoasterPathGraphManager.get(level);
    if (contact == null || body.isRemoved() || CoasterCartTrackSnap.isBearingDisengaged(body)
        || contact.tick() != level.getGameTime() || contact.graphRevision() != graph.revision()
        || !graph.edges().contains(contact.hit().edge())) {
      forget(level, body);
      return Optional.empty();
    }
    return Optional.of(contact);
  }

  public static void forget(ServerLevel level, ServerSubLevel body) {
    var entries = CONTACTS.get(level);
    if (entries != null) entries.remove(body.getUniqueId());
  }
}
