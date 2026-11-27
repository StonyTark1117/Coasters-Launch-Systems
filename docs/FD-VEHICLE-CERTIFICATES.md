# Fourth Dimension equipment certificates — 0.5

The finite catalog is the sixteen mechanical variants in FD's accepted pre-1.0
`TrackDesign` plus ordinary-track Wing: seventeen mechanical variants, with
compact Wing and open-frame Fixed Inverted as cosmetic body choices.
It does not include the sibling checkout's later 1.4/1.5 work. The selected
source is `8cebab779da3311b39e3471bbfb41ebf1dafcd19` (accepted 1.3 baseline plus
external authority API 1); runtime artifact SHA-256 is
`3dbbc7a77e1b0cd02b3e5fa3a69e4b4823c5c5a3befca6f0e1e35d72381b7fba`.
This is isolated external-family API 1 overlay commit
`dc1c83a543256a5ffeab6d7958e9a3656c132f74` on the accepted source; it
adds the narrow S2/Flying authority seam without importing later sibling work.
Unchanged boundaries and appearance also retain the original API 1 artifact
`df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e` evidence.
See [integration selection](FOURTH-DIMENSION-HANDOFF.md).

A certificate requires the exact mechanical family/variant and a separate,
correctly positioned LS block on the native chassis. A native seat magnet,
rendered trim, body choice, color or shared save schema grants no interface.
All positive contacts remain subject to loaded bodies, matching rail,
restraints, native readiness/faults, whole-consist identity, local reservation
and exact native authority. Managed FD routes remain excluded until LS601.
Native capture/locks/posture continue to belong to FD.

## Finite disposition table

`Yes` means the explicit contact is certified within its declared operating
conditions; it does not promise every slope, posture or consist. `No` references
a mechanical exclusion below. This is the complete seventeen-variant source
contract: fourteen variants have six independent contacts; twelve also have
the shared cable coupling. Evidence roles and package acceptance are mapped below.

| Mechanical vehicle / variant | Tire | Fin | Skid | Eddy | LSM | LIM | Flywheel / hydraulic / pneumatic |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Arrow — `arrow_four_rail_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes, certified socket/rated consist |
| Legacy ZacSpin — `zacspin_legacy_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes, whole native chassis socket |
| Free Spin — `sns_magnetic_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes, whole native chassis socket |
| Revised ZacSpin — `zacspin_revised_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes, whole native chassis socket |
| Wing — `wing_fixed_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Fixed Inverted — `four_abreast_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Classic Spinning — `conventional_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Launched Inverting Spinning — `launched_inverting_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Suspended — `lateral_swing_v1` | Yes | Yes | Yes | Yes | Yes | Yes | No, G |
| Axis — `longitudinal_roll_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Conventional Flying — `station_changeover_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| New-generation Flying — `route_changeover_yaw_v1` | Yes | Yes | Yes | Yes | Yes | Yes | No, G |
| Pipeline — `seated_side_rail_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Dive — `six_abreast_v1` | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Powered Inverted Yaw — `upright_yaw_v1` | No, Y | No, Y | No, Y | No, Y | No, Y | No, Y | No, Y |
| Roller Ball — `vertical_zigzag_v1` | No, R | No, R | No, R | No, R | No, R | No, R | No, R |
| PAX — `pax_shuttle_v1` | No, P | No, P | No, P | No, P | No, P | No, P | No, P |

See [normal interface installation](FD-INTERFACE-INSTALLATION.md) for item placement,
continuous support and native controls.

## Contact mounting and cable operating limits

The six individual contacts use the explicit chassis-local slots in
`FourthDimensionCertificates`, measured from the representative native bearing.
Fixed Inverted, Suspended, Flying (both variants), and Pipeline place those
contacts five blocks forward on a rigid chassis arm (local z=-5). Add the
iron spine only in air; preserve all native controller, frame and seat blocks.
The conventional layouts keep their original slots. Installed parts, not the
body's visual appearance, determine the available interface. The physical
contact must overlap its cabinet's two-block interval and remain within the
certified rail corridor.

The positive hanging cable layouts (Fixed Inverted, conventional Flying and
Pipeline) use a separate **trailing** socket at local `(0,-2,+5)` with an
air-only rear spine plus vertical connector at `(0,-1,+5)`. Their fore friction fin is ten blocks ahead of that
socket. A fore-facing extended catch is denied because its returning carriage
would cross the occupied hanging-body sweep. Native carriage force acts only
on the carriage and the real catch transmits it to the complete vehicle.

All three cable stores share the same fixed straight, level, unbanked guide,
12-block pull, 20-block carriage travel and 64-unit loaded-mass ceiling.
The launch pad follows the actual fin station; the stop bank begins beyond
that fin's 12-block released travel and must satisfy the complete train's
stopping envelope. Only the launch pad releases. Braking begins when the
actual leading fin reaches the stop bank. HOME is the physical socket station,
which persists independently of the bearing position. A normal HOME disarm
retires the carriage and its loading tickets while retaining the train lease.

A validated hanging example starts the bearing at route distance 8, rear
socket at 3 and fore fin at 13. Put the cable cabinet above the rail at relative
`(0,+2,3)`, launch fin at `(1,+2,13)`, and contiguous stop fins at distances
27 through 47. Cabinet/motor placement must also clear native forward and
backward safety lookahead. The new loaded family tests use flywheel work to
qualify this common rig; accepted hydraulic/pneumatic finite-store and
interruption evidence remains applicable to the unchanged shared coupling.

## Cable guide conflicts with full native sweeps (G)

The guide is a 3/16-block collision shape at
`floor(railPoint + up * (0.3125 - 5))`. On the selected native level rail,
rail y=224.8125 and guide top y=220.1875 leave **4.625 blocks** of vertical
clearance. Suspended reserves 4.7 below rail, so the guide intrudes by 0.075;
new-generation Flying reserves 5.25, so it intrudes by 0.625. Relocating the
socket cannot remove this continuous guide under the swept path.

The loaded negative fixtures first board the native riders with no obstruction,
then place the actual guide. Suspended reports `Suspended cabin sweep obstructed`;
new-generation Flying reports `COMBINED_SWEEP_OBSTRUCTED` at an actual guide
block. Both retain their riders and deny dispatch. Their six independent
contacts remain certified on a clear route; the three cable mechanisms are
excluded for this physical guide contract, not because testing was omitted.
Changing native clearance or inventing a lower guide would change the accepted
mechanical contract and is outside this certificate.

## Powered Inverted Yaw retains its native governor (Y)

This family requires the native whole-circuit Chain governor for speed, dwell,
reversal and capture. Those operations are a single native owner contract;
a local LS motor, brake or catch would introduce a competing longitudinal
owner. Its explicit denial is independent of body hardware or missing adapter
callbacks. The loaded ownership-denial fixture belongs to the articulated
family evidence.

## Roller Ball: lift, free travel and return are one native cycle (R)

The selected `RollerBallRuntime` independently owns the native guided travel
limit, return braking, lift-load sharing and hold constraint. It does not cede
those operations to an LS contact owner. `RollerBallTrack` validates the closed
DOCK/LIFT/SUMMIT/FREE/RETURN route and geometry hash; native boarding requires
its dock, stopped carrier latch and matching rail. The accepted running gear
has a 4.50-block lateral, 4.75-block upper, .75-block lower and 2.75-block
longitudinal envelope, a transverse tumble hinge and four seats.

On LIFT/SUMMIT, missing Chain power is a native fault. Chain supplies uphill
travel and shares its speed correction with the loaded carrier. Guided travel
is capped at 1.75 blocks/s, lower near curve joins; RETURN is capped at 2.5,
and free travel at 6.5. FREE release and the return latch are native cycle
transitions, not generic LS dispatch/capture. Adding an LS motor or brake to
that unceded cycle would introduce a second longitudinal operator; fitting a
socket would likewise bypass the lift/return and latch contract. No separate
physically meaningful external operating phase is delegated by the selected
artifact. LS therefore rejects this entire combination while preserving the
native Roller Ball ride, rather than pretending a visible block grants control.

The current LS-loaded test installs all six ordinary contact blocks and a
catch socket on a real four-seat-loaded Roller Ball. It verifies empty LS
certificates and denial of dispatch, propulsion, braking, capture and authority
reservation without changing the native revision; the inherited native test
also checks the actual hinge, rider mass and saved cycle identity. No LS
occupied ride is advertised for this explicitly excluded combination.

## PAX retains its winch (P)

PAX owns its complete shuttle lifecycle. FD API 1 rejects PAX ride-local
longitudinal claims, and LS identifies `fourthdimension:pax_winch` as its
exclusive operator. A real native PAX fitted with all ordinary LS contacts and
a catch socket still receives no interface, drive, brake, dispatch or capture
permission; denial leaves native revision unchanged. Positive final-brake
handoff remains optional LS806, requiring explicit native delegation and return
of ownership. This is an ownership exclusion, not a missing test label.

## Body, appearance and colors

`wing_compact` and `fixed_inverted_open_frame` are body identifiers independent
of mechanical variant and palette. Their outer clearance, physical masses,
contacts, seat/capture behavior and certificate must remain identical to their
base family. The selected FD [accepted body evidence](<../../Coasters: Fourth Dimension/docs/evidence/1.3/body-variants/README.md>)
covers actual editor/world preview, colors, occupied fit and cold-process
persistence. Its native transaction and snapshot-codec tests are reused with
LS loaded, with additional LS certificate, installed-contact, body-ID, mass
and palette-save assertions. This does not import a new cosmetic requirement
or claim that synthetic native passengers prove player operation.

## Evidence map

Loaded native fixtures establish each advertised contact's work, physical
coupling and native passenger/guard behavior. Actual player demonstrations
establish the relevant family controls, occupied motion, stop and service.
Those are complementary roles: a representative occupied ride does not claim
a separate player replay of every interface or each of the three stores.

| Catalog rows / requirement | Loaded native and boundary evidence | Relevant occupied or retained evidence |
| --- | --- | --- |
| Arrow, Legacy ZacSpin, Free Spin, Revised ZacSpin | Accepted original-family contacts in [0.2](evidence/ls02-linear-20261122/README.md); Arrow cable in [0.3](evidence/0.3/cable-current-20261122/README.md); new Legacy/Free Spin/Revised cable cases in [catalog](evidence/ls05-catalog-20261122/README.md). | [0.1 integration](evidence/0.1/integration-20261122/README.md) and [0.2 player acceptance](evidence/ls02-linear-20261122/README.md) retain the original-family occupied contract. [Hydraulic/pneumatic player cycles](evidence/ls04-pressure-20261122/README.md) retain the unchanged finite stores and shared coupling/service contract. |
| Wing, Fixed Inverted, Classic Spinning, Launched Inverting Spinning — LS501 | [Fixed/spinning evidence](evidence/ls05-fixed-20261122/README.md) covers six contacts and central cable; [catalog](evidence/ls05-catalog-20261122/README.md) covers Fixed's trailing catch, full physical return and three-process connected recovery. | [LS501 player records](evidence/ls05-fixed-20261122/README.md) identify the actual family controls and occupied cases, including the distinct translated Fixed catch. |
| Suspended, Axis, Conventional Flying, New-generation Flying, Pipeline, Dive — LS502 | [Articulated evidence](evidence/ls05-articulated-20261122/README.md) measures all six contacts, locks/posture/yaw and power-loss retention. [Catalog cable cases](evidence/ls05-catalog-20261122/README.md) cover Axis/Dive and Pipeline/Flying positives, and the two loaded guide-sweep denials. | [LS502 player records](evidence/ls05-articulated-20261122/README.md) identify each family's native controls and occupied operation. |
| Powered Inverted Yaw, Roller Ball, PAX — LS502/LS503 | [Real governor ownership denial](evidence/ls05-articulated-20261122/README.md) and [Roller/PAX native denials](evidence/ls05-catalog-20261122/README.md), including loaded Roller hinge and saved cycle. | No LS occupied ride is advertised for these excluded combinations. Their native cycle remains authoritative. |
| Compact Wing, open-frame Fixed Inverted and colors — LS504 | [Two LS-loaded transaction/codec cases](evidence/ls05-catalog-20261122/README.md) preserve certificate, mass, installed points, body identity and palette. | The accepted FD body record linked above supplies unchanged rendered, occupied-fit and cold-process appearance evidence. Cosmetic choice does not create a new mechanical variant. |

The expanded eligible families' relevant occupied cases are complete in the
linked LS501/LS502 records, including native controls, retained riders, arrival
and normal service. Independent [Wing/articulated operation](evidence/0.5/observer-20261126/README.md)
and [Fixed/Classic/S2 operation](evidence/0.5/foundation-observer-20261126/README.md)
preserve normal operator inputs, state readbacks and exterior views. The latter
includes Fixed's physical rear-catch HOME and S2's explicit service recovery;
these are not extrapolated from appearance or synthetic riders.

Exact LS/FD artifacts, individual outcomes, diagnosed fixture corrections and
saved shutdowns belong to those owner records. All participating clients,
servers and displays are stopped. The finite table, loaded native matrix,
mechanical exclusions, appearance persistence and relevant occupied evidence
together satisfy LS501–LS504 within the selected catalog. Canonical package
integration and delivery remain recorded in the roadmap.
