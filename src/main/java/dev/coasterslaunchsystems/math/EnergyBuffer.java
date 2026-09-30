package dev.coasterslaunchsystems.math;

public class EnergyBuffer implements EnergyStore {
  private final double capacity;
  private double energy;

  public EnergyBuffer(double capacity, double energy) {
    this.capacity = Numbers.positive(capacity, "capacity");
    if (Numbers.nonnegative(energy, "energy") > capacity)
      throw new IllegalArgumentException("Energy exceeds capacity");
    this.energy = energy;
  }

  @Override public double energy() { return energy; }
  @Override public double capacity() { return capacity; }
  @Override public double charge(double work) {
    double accepted = Math.min(Numbers.nonnegative(work, "work"), capacity - energy);
    energy += accepted;
    return accepted;
  }
  @Override public double draw(double work) {
    double delivered = Math.min(Numbers.nonnegative(work, "work"), energy);
    energy -= delivered;
    return delivered;
  }
}
