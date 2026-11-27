package dev.coasterslaunchsystems.runtime;

import java.util.*;

/** Opt-in bounded wall-clock observations. Never participates in physics or readiness decisions. */
public final class CallbackTiming {
  public static final boolean ENABLED = Boolean.getBoolean("coasterslaunchsystems.performance");
  private static final int CAPACITY = 100_000;
  private static Session session;
  private CallbackTiming() {}

  private static final class Session {
    final Object level;
    final long started = System.nanoTime();
    final long[] nanos = new long[CAPACITY];
    final String[] phases = new String[CAPACITY];
    int count;
    long dropped;
    double simulatedSeconds;
    String phase = "settled";
    Session(Object level) { this.level = Objects.requireNonNull(level); }
  }

  public static synchronized void start(Object level) {
    if (!ENABLED) throw new IllegalStateException("Enable coasterslaunchsystems.performance explicitly");
    if (session != null) throw new IllegalStateException("A callback measurement is already active");
    session = new Session(level);
  }
  public static synchronized void phase(String phase) {
    if (session != null) session.phase = Objects.requireNonNull(phase);
  }
  static synchronized long begin(Object level) {
    return session != null && session.level == level ? System.nanoTime() : 0;
  }
  static void end(Object level, long started, double dt) {
    // Read the end before acquiring the recorder lock; storage/aggregation is outside the timed region.
    long elapsed = System.nanoTime() - started;
    synchronized (CallbackTiming.class) {
      if (started == 0 || session == null || session.level != level) return;
      if (session.count == CAPACITY) { session.dropped++; return; }
      session.nanos[session.count] = Math.max(0, elapsed);
      session.phases[session.count++] = session.phase;
      session.simulatedSeconds += dt;
    }
  }
  public static synchronized Map<String, Object> finish(Object level) {
    if (session == null || session.level != level) throw new IllegalStateException("No matching measurement");
    var s = session; session = null;
    var result = new LinkedHashMap<String, Object>();
    result.put("wall_seconds", (System.nanoTime() - s.started) / 1e9);
    result.put("simulated_seconds", s.simulatedSeconds);
    result.put("dropped_samples", s.dropped);
    result.put("all", distribution(Arrays.copyOf(s.nanos, s.count)));
    var byPhase = new LinkedHashMap<String, List<Long>>();
    for (int i = 0; i < s.count; i++) byPhase.computeIfAbsent(s.phases[i], ignored -> new ArrayList<>()).add(s.nanos[i]);
    var summaries = new LinkedHashMap<String, Object>();
    byPhase.forEach((phase, values) -> summaries.put(phase, distribution(values.stream().mapToLong(Long::longValue).toArray())));
    result.put("phases", summaries);
    return result;
  }
  /** Nearest-rank quantiles over every retained callback; nanoseconds converted to milliseconds. */
  public static Map<String, Number> distribution(long[] observations) {
    long[] sorted = observations.clone(); Arrays.sort(sorted);
    var result = new LinkedHashMap<String, Number>();
    result.put("samples", sorted.length);
    double total = Arrays.stream(sorted).mapToDouble(v -> v / 1e6).sum();
    result.put("total_ms", total);
    result.put("mean_ms", sorted.length == 0 ? 0 : total / sorted.length);
    for (var percentile : new int[] {50, 95, 99, 100})
      result.put(percentile == 100 ? "max_ms" : "p" + percentile + "_ms", sorted.length == 0 ? 0d
          : sorted[(int) Math.ceil(sorted.length * percentile / 100.0) - 1] / 1e6);
    return Collections.unmodifiableMap(result);
  }
}
