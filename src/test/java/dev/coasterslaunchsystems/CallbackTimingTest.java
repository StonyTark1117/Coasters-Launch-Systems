package dev.coasterslaunchsystems;

import dev.coasterslaunchsystems.runtime.CallbackTiming;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CallbackTimingTest {
  @Test void reportsNearestRankInMillisecondsWithoutMutatingSamples() {
    long[] values = {4_000_000, 1_000_000, 3_000_000, 2_000_000};
    var summary = CallbackTiming.distribution(values);
    assertEquals(4, summary.get("samples"));
    assertEquals(10d, summary.get("total_ms"));
    assertEquals(2.5, summary.get("mean_ms"));
    assertEquals(2d, summary.get("p50_ms"));
    assertEquals(4d, summary.get("p95_ms"));
    assertEquals(4d, summary.get("p99_ms"));
    assertEquals(4d, summary.get("max_ms"));
    assertEquals(4_000_000, values[0]);
    assertEquals(0d, CallbackTiming.distribution(new long[0]).get("mean_ms"));
  }
}
