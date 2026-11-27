# Operating Launch Systems

This guide describes the selected 1.0 package, unchanged from accepted 0.9 qualification.
[Dependencies](DEPENDENCIES.md) identifies the exact tested stack; the
[FD certificate table](FD-VEHICLE-CERTIFICATES.md) identifies eligible families
and hardware. Installing a plate or changing a body appearance does not grant
an unsupported vehicle or provider permission to launch.

## Build and commission a section

1. Build fixed, level, unbanked rail for a local launch section. Place both
   Coasters anchors on the top faces of floor supports before connecting them.
   When connecting a return curve, hold Alt to preserve the adjoining straight
   section. Inspect the actual rail shape before binding equipment.
2. Install the matching chassis interface on the bearing chassis, never on a
   rotating seat carrier. Ordinary placement uses chassis-local directions: the tire strip below the
   representative bearing, friction fin east/below, conductive brake fin
   west/below, LSM plate north/below and LIM plate south/below. Use each FD
   family's documented mounted locations and preserve its own hardware.
3. Place each cabinet within three blocks of its fixed rail, with its shaft
   transverse to travel. Use the Span Tool to select the ordered anchors and
   bind a disarmed cabinet. Each cabinet has a finite contact interval;
   selecting a long edge does not make one cabinet act along the whole edge.
4. Bind the local controller to the same ordered anchors. Shift-use the Span
   Tool on the controller to select it, then use it on each disarmed bound
   cabinet to link it. Shift-use a plain block to clear the selection; shift-use
   a linked cabinet instead unlinks it when the safe editing guards permit. Install
   overlapping launch spring pads and a continuous downstream friction-fin
   bank that satisfies the loaded stopping-distance readout. Passive magnets
   and tire retardation do not replace this holding bank.
5. Supply real Create shaft power and tune empty, disarmed equipment. Arm
   cabinets, then shift-use the controller with an empty hand to inspect/arm.
   Empty-hand use reads state and faults. Commission empty, then with the
   intended loaded consist; reserve capacity is checked against actual mass.

A rising redstone input immediately above a READY controller requests departure.
Input before readiness is consumed, and held-high input never repeats a launch.
Close FD restraints through FD's normal controls before requesting departure.
FD continues to own seats, rotation, capture, restraints and supported families.
The accepted bounded Arrow route mode also respects FD current/next reservations;
other route modes and optional operating integrations remain excluded.

## Choose the braking hardware

| Equipment | Normal use | Required limitation |
| --- | --- | --- |
| Friction-fin clamp | Install its separate friction fin; bind and arm the cabinet. Unlinked spring pads apply below 32 absolute shaft RPM and release at or above 32. Linked pads also require the controller's release grant. | Finite contact length, force and loaded stopping envelope determine the stop. Power alone cannot release a controller's retained stop bank. |
| Skid brake | Install the certified underside shoe and bind/arm the skid cabinet; see [placement and heat](SKID-BRAKES.md). | Dissipative trim with heat derating; no rated static hold and no local-controller final-stop credit. A wooden appearance is not a shoe certificate. |
| Passive magnetic brake | Install the separate conductive brake fin, then bind and arm the magnetic cabinet. No shaft power is required. | Force tends to zero at rest. Use friction fins for final hold. Removed energy becomes heat, never FE or shaft credit. |
| Active LSM/LIM braking | Install the matching reaction plate, supply shaft power and energy, and select zero target on an empty disarmed standalone cabinet; see [tuning](LINEAR-MOTORS.md). | Finite powered deceleration, with losses and heat. Supply loss disables the field; no static-hold rating or regeneration. |
| Drive-tire retardation | Use correctly directed powered tires with a lower wheel-speed target to position a compatible cart. | Available shaft work and contact limit the response. Power loss does not guarantee a parked train; retain a friction-fin holding bank. |

Watch cabinet energy/heat and the controller's actual readiness. Reaching a
speed setting is not proof of sufficient braking distance. A fin outside its
physical pads cannot hold a vehicle, and installing more drives cannot override
provider force or acceleration limits.

## Use the six launches

Tires use their shaft direction and speed. LSM and LIM use separate reaction
plates, finite buffers and signed targets; shaft direction does not select
field direction. The [linear guide](LINEAR-MOTORS.md) explains target,
acceleration, jerk and the bounded forward/backward/forward program. Linear
station cycles and transfers remain excluded.

Flywheel, hydraulic and pneumatic launches share a physical catch and bounded
straight guide but have distinct finite stores. Follow the [cable guide](CABLE-MECHANICS.md)
and [pressure guide](PRESSURE-MECHANICS.md) for attachment, charging and service.
Engage only inside the declared low-speed window. After release, wait for the
carriage's physical HOME return before another cycle; recharge alone does not
reset the catch. Inspect retained faults before normal disarm/retirement.

The four in-game Ponder guides cover controller/span binding, linear hardware,
brake interfaces and cable launches. Their item entry points and recipes are
part of the packaged assets. These guides teach installation; actual provider
certificates and controller readouts decide whether a particular build is ready.

## Boarding, interruption and recovery

To enable station capture, use the Span Tool on an empty disarmed controller
with a clock in the offhand, without sneaking. Arrival first stops the whole
train. FD capture then waits for measured alignment before boarding readiness;
request acceptance alone is not permission to board. Open/close restraints with
FD controls and provide a new departure edge after closing them.

| Controller face | Signal |
| --- | --- |
| Top | Fresh rising departure input |
| North | Departure ready |
| South | Retained chassis reservation |
| East | Fault or inspection |
| West | Held/ready/capturing/boarding state |
| Bottom | Measured whole-train boarding readiness |

After restart or an interrupted launch, inspect the saved lease, provider,
rail/support, equipment identities and charge before explicit rearming. Loading,
rejoining, restoring power or leaving input high cannot replay a launch.
Normal editing of occupied/reserved equipment is refused. Do not remove a
reservation to bypass a fault. Use the documented [receiver recovery](RECEIVER-RECOVERY.md)
procedure when its named replacement case applies. Back up worlds before
removing a mod that owns blocks or vehicles; an LS lease cannot recreate that
mod's removed blocks.

Version 0.9 remembers an observed optional provider in the world save. If that
provider is later absent, its chassis cannot become an ordinary launch-ready
cart. For an older world, first load and inspect the affected vehicles with
the provider installed under 0.9, then save before removal. Unvisited pre-0.9
vehicles have no saved LS ownership record to infer retroactively. Restoring
a provider preserves inspection/fault state and still requires explicit recovery.

Historical checkpoint pages retain their original experiments and limitations.
Current acceptance and exclusions are in [the roadmap](../ROADMAP.md) and
[current queue](CURRENT-WORK.md).
