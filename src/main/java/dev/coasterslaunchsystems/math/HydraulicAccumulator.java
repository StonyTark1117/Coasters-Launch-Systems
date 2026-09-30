package dev.coasterslaunchsystems.math;

/** Original gas-backed, closed-oil-loop game accumulator. Pressure uses the polytropic gas law. */
public final class HydraulicAccumulator implements EnergyStore {
  private final double gasVolume, minimumVolume, prechargePressure, ambientPressure, exponent;
  private final EnergyBuffer work;

  public HydraulicAccumulator(double gasVolume, double minimumVolume, double prechargePressure,
      double ambientPressure, double exponent, double initialWork) {
    this.gasVolume = Numbers.positive(gasVolume, "gas volume");
    this.minimumVolume = Numbers.positive(minimumVolume, "minimum gas volume");
    this.prechargePressure = Numbers.positive(prechargePressure, "precharge pressure");
    this.ambientPressure = Numbers.positive(ambientPressure, "ambient pressure");
    this.exponent = Numbers.positive(exponent, "exponent");
    if (minimumVolume >= gasVolume || exponent <= 1 || prechargePressure <= ambientPressure)
      throw new IllegalArgumentException("Invalid accumulator geometry or gas law");
    work = new EnergyBuffer(workAtVolume(minimumVolume), initialWork);
  }

  private double workAtVolume(double volume) {
    double gasWork = prechargePressure * Math.pow(gasVolume, exponent) / (exponent - 1)
        * (Math.pow(volume, 1 - exponent) - Math.pow(gasVolume, 1 - exponent));
    return gasWork - ambientPressure * (gasVolume - volume);
  }

  public double gasVolume() {
    double low = minimumVolume, high = gasVolume;
    for (int i = 0; i < 60; i++) {
      double volume = .5 * (low + high);
      if (workAtVolume(volume) > energy()) low = volume; else high = volume;
    }
    return .5 * (low + high);
  }

  public double pressure() { return prechargePressure * Math.pow(gasVolume / gasVolume(), exponent); }
  public double oilVolume() { return gasVolume - gasVolume(); }
  public double force(double pistonArea) {
    return energy() == 0 ? 0 : (pressure() - ambientPressure) * Numbers.positive(pistonArea, "area");
  }
  @Override public double energy() { return work.energy(); }
  @Override public double capacity() { return work.capacity(); }
  @Override public double charge(double value) { return work.charge(value); }
  @Override public double draw(double value) { return work.draw(value); }
}
