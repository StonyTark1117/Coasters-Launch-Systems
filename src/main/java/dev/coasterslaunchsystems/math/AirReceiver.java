package dev.coasterslaunchsystems.math;

/** Isothermal receiver exergy relative to ambient, with an explicitly finite air charge. */
public final class AirReceiver implements EnergyStore {
  private final double volume, ambientPressure, maximumPressure;
  private final EnergyBuffer work;

  public AirReceiver(double volume, double ambientPressure, double maximumPressure, double initialWork) {
    this.volume = Numbers.positive(volume, "volume");
    this.ambientPressure = Numbers.positive(ambientPressure, "ambient pressure");
    this.maximumPressure = Numbers.positive(maximumPressure, "maximum pressure");
    if (maximumPressure <= ambientPressure) throw new IllegalArgumentException("No receiver pressure range");
    work = new EnergyBuffer(workAtPressure(maximumPressure), initialWork);
  }

  private double workAtPressure(double pressure) {
    return volume * (pressure * Math.log(pressure / ambientPressure) - pressure + ambientPressure);
  }

  public double pressure() {
    double low = ambientPressure, high = maximumPressure;
    for (int i = 0; i < 60; i++) {
      double pressure = .5 * (low + high);
      if (workAtPressure(pressure) < energy()) low = pressure; else high = pressure;
    }
    return .5 * (low + high);
  }

  public double force(double area, double expandedCylinderVolume) {
    Numbers.nonnegative(expandedCylinderVolume, "expanded volume");
    double expanded = pressure() * volume / (volume + expandedCylinderVolume);
    return Math.max(0, expanded - ambientPressure) * Numbers.positive(area, "area");
  }
  @Override public double energy() { return work.energy(); }
  @Override public double capacity() { return work.capacity(); }
  @Override public double charge(double value) { return work.charge(value); }
  @Override public double draw(double value) { return work.draw(value); }
}
