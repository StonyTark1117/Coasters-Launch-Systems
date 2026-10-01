package dev.coasterslaunchsystems;

import dev.coasterslaunchsystems.math.ArcLengthTable;
import dev.coasterslaunchsystems.math.SpanInterval;
import dev.coasterslaunchsystems.math.ArcLengthTable.Point;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrackDistanceTest {
  @Test void nonuniformCurveUsesDistanceRatherThanParameter() {
    var table = new ArcLengthTable(512, t -> new Point(0, 0, 100 * t * t));
    assertEquals(100, table.length(), 1e-9);
    assertEquals(25, table.distance(.5), 1e-9);
    assertEquals(.5, table.parameter(25), 1e-9);
    for (int i = 0; i <= 100; i++) {
      double parameter = i / 100.0;
      assertEquals(parameter, table.parameter(table.distance(parameter)), 1e-10);
    }
  }

  @Test void curvedRailLengthConvergesAndKeepsMonotonicDistance() {
    var table = new ArcLengthTable(512,
        t -> new Point(10 * Math.cos(t * Math.PI), 10 * Math.sin(t * Math.PI), 0));
    assertEquals(10 * Math.PI, table.length(), .001);
    assertTrue(table.distance(.8) > table.distance(.2));
  }

  @Test void sweptEntryCannotSkipShortSpanInEitherDirection() {
    var span = new SpanInterval(10, 11);
    assertTrue(span.intersects(8, 13, .1));
    assertTrue(span.intersects(13, 8, .1));
    assertFalse(span.intersects(7, 9, .1));
  }

  @Test void tailMustClearBeforeOccupancyEnds() {
    var span = new SpanInterval(10, 20);
    assertTrue(span.contains(20.5, .6));
    assertFalse(span.contains(20.7, .6));
    assertTrue(span.contains(9.5, .6));
    assertFalse(span.contains(9.3, .6));
  }

  @Test void invalidGeometryAndIntervalsAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new ArcLengthTable(512, t -> new Point(0, 0, 0)));
    assertThrows(IllegalArgumentException.class,
        () -> new ArcLengthTable(512, t -> new Point(Double.NaN, 0, 0)));
    assertThrows(IllegalArgumentException.class, () -> new SpanInterval(20, 10));
    assertThrows(IllegalArgumentException.class, () -> new SpanInterval(0, 2).contains(1, -1));
    var table = new ArcLengthTable(16, t -> new Point(t, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> table.distance(-.1));
    assertThrows(IllegalArgumentException.class, () -> table.parameter(2));
  }
}
