# Skid brakes

The skid brake is a passive, contact-friction trim brake. It has a 192-force cabinet limit, bounded per physics step so one contact cannot reverse a chassis, and no static holding constraint. Use a friction fin for a stopped train or for a grade that needs to remain held.

Each compatible chassis needs a separate **Chassis Skid Brake Shoe** at the bearing's local north/two-below cell. This keeps it clear of tire, fin, conductive, LSM and LIM reaction hardware. An unmodified chassis does not certify this interface. Providers expose the interface only while the shoe exists; removing it stops further braking and disarms the cabinet with a clear fault.

Skid work is stored in the cabinet's existing 4096-work heat store. It cools at 8 work per second and reduces available braking force down to ten percent under overload. The saved heat and module identity are the source of truth after reload; native force requests and handles are transient. A restarted skid cabinet is disarmed and faulted for shoe inspection before it can be rearmed, so recovery cannot replay braking work from a stale handle.

Fourth Dimension support is optional. Its adapter grants the skid certificate only to a compatible, loaded vehicle with the physical shoe installed and honors the provider's existing braking authority vetoes.
