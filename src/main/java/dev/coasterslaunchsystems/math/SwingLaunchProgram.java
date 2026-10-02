package dev.coasterslaunchsystems.math;

/**
 * The bounded LSM shuttle sequence. A controller owns its whole fixed block for all three
 * passes, and may change direction only after the moving tail cleared the end it just reached.
 */
public final class SwingLaunchProgram {
  public static final int PASSES = 3;
  public enum Direction {
    FORWARD(1), BACKWARD(-1);
    private final int sign;
    Direction(int sign) { this.sign = sign; }
    public int sign() { return sign; }
  }
  private SwingLaunchProgram() {}

  public static Direction direction(int completedPasses) {
    if (completedPasses < 0 || completedPasses >= PASSES)
      throw new IllegalArgumentException("Swing launch pass is outside its bounded program");
    return completedPasses == 1 ? Direction.BACKWARD : Direction.FORWARD;
  }

  public static int advance(int completedPasses, boolean tailCleared) {
    direction(completedPasses);
    if (!tailCleared) throw new IllegalArgumentException("Swing-launch tail did not clear its reversal block");
    return completedPasses + 1;
  }

  /** Each independent cabinet must reserve all three powered traversals before the first release. */
  public static double reserve(double onePass) {
    Numbers.nonnegative(onePass, "One swing pass reserve");
    return PASSES * onePass;
  }
}
