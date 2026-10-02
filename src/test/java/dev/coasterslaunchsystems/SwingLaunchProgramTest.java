package dev.coasterslaunchsystems;

import dev.coasterslaunchsystems.math.SwingLaunchProgram;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SwingLaunchProgramTest {
  @Test void boundedForwardBackwardForwardSequenceRequiresTailClearance() {
    assertEquals(SwingLaunchProgram.Direction.FORWARD, SwingLaunchProgram.direction(0));
    assertEquals(SwingLaunchProgram.Direction.BACKWARD, SwingLaunchProgram.direction(1));
    assertEquals(SwingLaunchProgram.Direction.FORWARD, SwingLaunchProgram.direction(2));
    assertEquals(1, SwingLaunchProgram.advance(0, true));
    assertEquals(2, SwingLaunchProgram.advance(1, true));
    assertEquals(3, SwingLaunchProgram.advance(2, true));
    assertThrows(IllegalArgumentException.class, () -> SwingLaunchProgram.advance(1, false));
    assertThrows(IllegalArgumentException.class, () -> SwingLaunchProgram.direction(3));
  }

  @Test void allPassesAreFundedBeforeRelease() {
    assertEquals(37.5, SwingLaunchProgram.reserve(12.5), 1e-12);
    assertThrows(IllegalArgumentException.class, () -> SwingLaunchProgram.reserve(-.1));
  }
}
