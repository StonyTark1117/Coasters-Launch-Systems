# Skid brakes

The skid brake is a passive, contact-friction trim brake. It has a 192-force cabinet limit, bounded per physics step so one contact cannot reverse a chassis, and no static holding constraint. Use a friction fin for a stopped train or for a grade that needs to remain held.

Each compatible chassis needs a separate **Chassis Skid Brake Shoe** at the bearing's local north/two-below cell. This keeps it clear of tire, fin, conductive, LSM and LIM reaction hardware. An unmodified chassis does not certify this interface. Providers expose the interface only while the shoe exists; removing it stops further braking and disarms the cabinet with a clear fault.

Skid work is stored in the cabinet's existing 4096-work heat store. It cools at 8 work per second and reduces available braking force down to ten percent under overload. The saved heat and module identity are the source of truth after reload; native force requests and handles are transient. A restarted skid cabinet is disarmed and faulted for shoe inspection before it can be rearmed, so recovery cannot replay braking work from a stale handle.

Fourth Dimension support is optional. Its adapter grants the skid certificate only to a compatible, loaded vehicle with the physical shoe installed and honors the provider's existing braking authority vetoes.

Keep passive skid contact zones separate from controller-owned launch and fin contact zones, including the entire chassis sweep. A skid cabinet cannot be linked as a local controller drive or holding fin. Simultaneous unlinked and controller-owned requests correctly fault competing owners; increasing the number of pads does not grant another lease.

The [0.2 acceptance record](evidence/ls02-linear-20261122/README.md) combines existing shoe/thermal/restart coverage with current API1 Arrow braking and an actual occupied Survival-built trim demonstration. Its observed near-zero speed is dissipative braking, not a claim of a static holding constraint.
