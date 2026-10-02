package dev.coasterslaunchsystems.math;

/** Server-owned work budget; charge/draw return actual accepted/transferred work. */
public interface EnergyStore {
  double energy();
  double capacity();
  double charge(double work);
  double draw(double work);

  default double fraction() { return energy() / capacity(); }

  /** Maximum actuator force available at the present extension. Infinite means a rotary store sets the limit elsewhere. */
  default double outputForce(double extension) { return Double.POSITIVE_INFINITY; }
}
