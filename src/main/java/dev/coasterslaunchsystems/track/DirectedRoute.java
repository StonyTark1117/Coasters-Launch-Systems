package dev.coasterslaunchsystems.track;

import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** A unique fixed-rail continuation, including both equipment sections. Never grants proximity contact. */
public final class DirectedRoute {
  public record Span(BlockPos from, BlockPos to, long geometry) {
    public boolean sameRail(BlockPos a, BlockPos b) {
      return from.equals(a) && to.equals(b) || from.equals(b) && to.equals(a);
    }
  }
  private final List<DirectedTrack> tracks;
  private final List<Span> spans;
  private final double[] offsets;
  private final Set<BlockPos> chunks = new LinkedHashSet<>();
  private final List<GraphHit> samples = new ArrayList<>();
  private final double length;

  private DirectedRoute(ServerLevel level, List<DirectedTrack> tracks, List<Span> spans) {
    this.tracks = List.copyOf(tracks); this.spans = List.copyOf(spans); offsets = new double[tracks.size()];
    double total = 0;
    for (int i = 0; i < tracks.size(); i++) {
      var track = tracks.get(i); offsets[i] = total; total += track.length();
      if (!Double.isFinite(total) || total > 2048) throw new IllegalArgumentException("Transfer route exceeds the supported 2048-block length");
      chunks.add(spans.get(i).from()); chunks.add(spans.get(i).to());
      int count = Math.max(1, (int) Math.ceil(track.length()));
      for (int sample = 0; sample <= count; sample++) {
        var hit = track.hitAtDistance(level, track.length() * sample / count);
        if (!Double.isFinite(hit.point().lengthSqr())) throw new IllegalArgumentException("Invalid route geometry");
        var pos = BlockPos.containing(hit.point());
        chunks.add(new BlockPos(Math.floorDiv(pos.getX(), 16) * 16, pos.getY(), Math.floorDiv(pos.getZ(), 16) * 16));
        samples.add(hit);
      }
    }
    length = total;
    requireLoaded(level);
  }

  public static DirectedRoute between(ServerLevel level, BlockPos sourceFrom, BlockPos sourceTo, BlockPos targetFrom, BlockPos targetTo) {
    var first = DirectedTrack.resolve(level, sourceFrom, sourceTo).orElseThrow(() -> new IllegalArgumentException("Source rail is unavailable"));
    var last = DirectedTrack.resolve(level, targetFrom, targetTo).orElseThrow(() -> new IllegalArgumentException("Receiver rail is unavailable"));
    if (first.edge().equals(last.edge())) throw new IllegalArgumentException("Transfer requires distinct sections");
    var edges = CoasterPathGraphManager.get(level).edges();
    var tracks = new ArrayList<DirectedTrack>(); var spans = new ArrayList<Span>();
    tracks.add(first); spans.add(new Span(sourceFrom, sourceTo, first.fingerprint()));
    var visited = new HashSet<BlockPos>(); visited.add(sourceFrom);
    var vertex = sourceTo; var previous = first;
    while (true) {
      if (!visited.add(vertex) || tracks.size() >= 128) throw new IllegalArgumentException("Transfer route loops before its receiver");
      var joint = vertex;
      var incident = edges.stream().filter(e -> e.from().equals(joint) || e.to().equals(joint)).toList();
      if (incident.size() != 2) throw new IllegalArgumentException("Transfer route requires unbranched rail joints");
      var incoming = previous.edge();
      var nextEdge = incident.stream().filter(e -> !e.equals(incoming)).findFirst().orElseThrow();
      if (vertex.equals(targetFrom)) {
        if (!nextEdge.equals(last.edge())) throw new IllegalArgumentException("Receiver faces away from the directed route");
        join(level, previous, last); tracks.add(last); spans.add(new Span(targetFrom, targetTo, last.fingerprint())); break;
      }
      if (nextEdge.equals(last.edge())) throw new IllegalArgumentException("Receiver anchor order opposes the route");
      var nextVertex = nextEdge.from().equals(vertex) ? nextEdge.to() : nextEdge.from();
      var next = DirectedTrack.resolve(level, vertex, nextVertex).orElseThrow(() -> new IllegalArgumentException("Intermediate rail is unavailable or moving"));
      join(level, previous, next); tracks.add(next); spans.add(new Span(vertex, nextVertex, next.fingerprint()));
      previous = next; vertex = nextVertex;
    }
    return new DirectedRoute(level, tracks, spans);
  }
  private static void join(ServerLevel level, DirectedTrack a, DirectedTrack b) {
    var end = a.hitAtDistance(level, a.length()); var start = b.hitAtDistance(level, 0);
    if (end.point().distanceToSqr(start.point()) > .0001 || a.forward(level, end).dot(b.forward(level, start)) < .99999)
      throw new IllegalArgumentException("Transfer route has a discontinuous rail joint");
  }
  public List<Span> spans() { return spans; }
  public List<DirectedTrack> tracks() { return tracks; }
  public List<GraphHit> samples() { return Collections.unmodifiableList(samples); }
  public double length() { return length; }
  public double receiverStart() { return offsets[offsets.length - 1]; }
  public boolean matches(GraphHit hit) { return tracks.stream().anyMatch(t -> t.matches(hit)); }
  public boolean contains(CoasterPathEdge edge) { return tracks.stream().anyMatch(t -> t.edge().equals(edge)); }
  public DirectedTrack track(GraphHit hit) { return tracks.stream().filter(t -> t.matches(hit)).findFirst().orElseThrow(() -> new IllegalArgumentException("Contact left the reserved route")); }
  public double distance(GraphHit hit) {
    for (int i = 0; i < tracks.size(); i++) if (tracks.get(i).matches(hit)) return offsets[i] + tracks.get(i).distance(hit);
    throw new IllegalArgumentException("Contact left the reserved route");
  }
  public Vec3 forward(ServerLevel level, GraphHit hit) { return track(hit).forward(level, hit); }
  public void requireLoaded(ServerLevel level) {
    for (var pos : chunks) if (!level.hasChunkAt(pos)) throw new IllegalStateException("Reserved transfer route chunk is unavailable");
  }
}
