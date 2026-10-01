package dev.coasterslaunchsystems.track;

import dev.coasterslaunchsystems.math.ArcLengthTable;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Single fixed-host edge, with direction selected by the placed span's anchor order. */
public final class DirectedTrack {
  private final CoasterPathEdge edge;
  private final boolean reversed;
  private final ArcLengthTable distances;
  private final long fingerprint;

  private DirectedTrack(ServerLevel level, CoasterPathEdge edge, boolean reversed) {
    this.edge = edge;
    this.reversed = reversed;
    distances = new ArcLengthTable(512, t -> vector(
        CoasterPathGraphManager.sampleEdgePositionPlotLocal(level, edge, t)));
    long hash = 0xcbf29ce484222325L;
    for (int i = 0; i <= 64; i++) {
      double t = i / 64.0;
      var point = CoasterPathGraphManager.sampleEdgePositionPlotLocal(level, edge, t);
      var up = CoasterPathTrackFrame.trackBasisAtHit(level, new GraphHit(point, edge, t)).up();
      for (double value : new double[] {point.x, point.y, point.z, up.x, up.y, up.z}) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite track frame");
        hash = (hash ^ Math.round(value * 1_000_000)) * 0x100000001b3L;
      }
    }
    fingerprint = hash;
  }

  public static Optional<DirectedTrack> resolve(ServerLevel level, BlockPos from, BlockPos to) {
    if (from.equals(to) || !level.hasChunkAt(from) || !level.hasChunkAt(to)) return Optional.empty();
    var candidates = CoasterPathGraphManager.get(level).edges().stream()
        .filter(e -> e.from().equals(from) && e.to().equals(to)
            || e.from().equals(to) && e.to().equals(from)).toList();
    if (candidates.size() != 1) return Optional.empty();
    var edge = candidates.getFirst();
    // Moving rail hosts need relative speed and their own support certificate.
    if (CoasterPathGraphManager.edgeHostedOnSubLevel(level, edge)) return Optional.empty();
    return Optional.of(new DirectedTrack(level, edge, !edge.from().equals(from)));
  }

  public double length() { return distances.length(); }
  public CoasterPathEdge edge() { return edge; }
  public long fingerprint() { return fingerprint; }
  public boolean matches(GraphHit hit) { return hit != null && edge.equals(hit.edge()); }

  public GraphHit hitAtDistance(ServerLevel level, double distance) {
    double t = distances.parameter(reversed ? length() - distance : distance);
    return new GraphHit(CoasterPathGraphManager.sampleEdgePositionPlotLocal(level, edge, t), edge, t);
  }

  public double distance(GraphHit hit) {
    if (!matches(hit)) throw new IllegalArgumentException("Contact is on another rail");
    double distance = distances.distance(hit.t());
    return reversed ? length() - distance : distance;
  }

  public Vec3 forward(ServerLevel level, GraphHit hit) {
    if (!matches(hit)) throw new IllegalArgumentException("Contact is on another rail");
    var forward = CoasterPathGraphManager.plotLocalCenterlineTangentAlongIncreasingT(level, edge, hit.t());
    if (!Double.isFinite(forward.lengthSqr()) || forward.lengthSqr() < 1e-12)
      throw new IllegalArgumentException("Invalid track tangent");
    return forward.normalize().scale(reversed ? -1 : 1);
  }

  private static ArcLengthTable.Point vector(Vec3 value) {
    return new ArcLengthTable.Point(value.x, value.y, value.z);
  }
}
