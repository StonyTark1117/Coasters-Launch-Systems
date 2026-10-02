# Original linear equipment, dev.13

This checkpoint implements original standalone linear synchronous and induction cabinets. Its models, tuning and resource scale are game approximations. Optional launch addons are not required. Existing tire drives, spring friction fins, passive magnets, provider capture and receiver repair remain available. This is partial LS201/LS202 work toward 0.4.0; the complete release is not certified.

## Hardware and placement

| Equipment | Chassis interface | Behavior |
| --- | --- | --- |
| Linear Synchronous Drive | LSM Magnet Reaction Plate one local block north of the representative bearing, one block below it | A bounded speed-error response drives toward the signed target. |
| Linear Induction Drive | LIM Conductive Reaction Plate one local block south of the representative bearing, one block below it | A separate slip response peaks at slip 0.25 and falls at excessive slip. Conversion losses increase with absolute slip. |

These are chassis-local positions. Place each plate on the bearing's chassis rather than a rotating seat carrier. They are distinct from the tire strip under the bearing, friction fin east of it, conductive brake fin west of it and Fourth Dimension seat magnets. Installing the other motor's plate does not certify this motor. Do not overwrite a vehicle family's hardware. The provider derives certificates from actual installed blocks and withdraws them on removal.

Place a cabinet within three blocks of fixed rail near the world origin (within 1024 X/Z). Its transverse shaft must use axis X or Z. Select the two edge anchors with the span tool, in drive order, then use it on the disarmed cabinet. Each cabinet binds only two arc blocks around its own contact. Linear equipment currently accepts level unbanked rail; moving tracks and general sloped/profile equipment are outside this checkpoint. Four survival recipes and independent block/item models accompany the two cabinets and two plates.

## Supply and tuning

Each cabinet starts with zero energy and has a 2048-unit buffer. Attach a real Create shaft network. The declared charger accepts 90% of `8 × |RPM| × 2π/60` native work per second while supplied at 1–128 RPM. Charging continues while disarmed and never exceeds capacity. A cabinet applies the same 16 stress units as a tire drive. Shaft direction does not select field direction; the signed target does. Supply outside the supported range disables the field even if energy remains.

Tune an empty, disarmed cabinet and controller before boarding or reserving a train. Use the span tool on the cabinet with one of these items in the offhand; clear a selected controller first if necessary.

| Offhand item | Setting |
| --- | --- |
| Clock | Target increases by 3 blocks/s, cycling through -12..12. Zero requests active braking. |
| Iron nugget | Acceleration cycles 0.5, 1, 1.5, 2 native blocks/s². |
| Gold nugget | Jerk ramp cycles 1, 2, 4, 8 native blocks/s³. |

Empty-hand use shows status, energy, heat, RPM and settings; shift-use arms a bound fault-free cabinet. Standalone negative targets propel in reverse. A local controller supports a positive target relative to its selected anchor order; reversing the anchor order reverses world travel. A negative local target holds departure disabled. Active braking uses finite power and has no declared static-hold rating: install friction fins for final hold.

The actuator ramps increases in chassis acceleration within the strictest participating linear jerk setting. Reduced authority, exhausted supply, removed contact and changed demand may reduce force immediately. Multiple adjacent pads share the provider's chassis force/acceleration envelope and one coordination owner. They cannot multiply the configured acceleration by adding cabinets. A conflicting unowned cabinet bank faults all claimants before force.

## Work and heat

LSM's declared conversion efficiency is 0.9. LIM uses `0.75 / (1 + |slip|)`, where `slip = (target - speed) / max(1, |target|)`. The force response is LSM's bounded speed-error curve or LIM's peaked slip curve. Both have a 20-unit per-contact force limit, additionally bounded by loaded provider ratings, tuning, target approach and heat. These values are not manufacturer calibration data.

Positive point-impulse work consumes `work / efficiency`. Copper losses are `0.025 × force² × dt` for LSM and `0.05 × force² × dt` for LIM; excitation adds `0.1 × |force| × dt`. All losses become heat. Negative mechanical work also becomes heat, consumes excitation/copper energy, and never recharges the buffer. A braking contact cannot fund a propelling contact or another cabinet. No FE conversion is implemented.

The allocator includes translation, angular lever arms and cross-work between different points on one native chassis. Each cabinet independently funds its requests before any impulse is queued. Sensors reconcile queued free-body impulse work before native constraints with spent energy and added heat. Native guides/couplers remain responsible for constrained motion; this is not a claim that all input work survives joint/friction losses as kinetic energy.

Heat uses the existing 4096-unit store and 8 units/s cooling, with force derating above capacity. Linear hot-store gameplay/restart acceptance remains an additional gate. Real resources, settings and geometry persist in equipment schema 4. Legacy schemas load linear buffers empty. Server loading always disables the field; buffer energy cannot replay a launch. Native handles, drive history and requests are not saved.

## Local sections and Fourth Dimension

Link disarmed linear cabinets to a bound local ride controller with the existing span tool. Launch pads and a contiguous downstream friction-fin bank are still required. The chassis also keeps its certified tire strip and friction fin for the existing station-hold operations. Each reserved member must have every participating drive interface; missing or mixed providers, duplicate bodies, exclusive ownership and native chain conflicts deny propulsion.

Before `READY`, every linear cabinet must independently fund a conservative launch of the whole loaded consist from rest, including worst forward-slip conversion, copper/excitation, acceleration/jerk duration and a 32-unit margin. A target/load whose reserve exceeds a cabinet's capacity stays held. Partial charge does not consume a held-high request later: provide a new low/high edge after readiness. Only `RUNNING` grants motor work. Loss of release/supply power faults the section, applies supported armed spring pads and retains the train's lease. Returning power or leaving the input high does not resume a faulted launch.

The pinned schema-4 Fourth Dimension adapter adds a linear certificate only for its explicitly supported hardware variants and an actually installed matching plate. Dispatch, restraints, support/fault checks, boarding and carrier rotation remain owned by Fourth Dimension's public operations. PAX retains its exclusive winch veto: focused LSM and LIM controller cases prove it faults before a reservation can issue a field impulse. Positive native linear sections cover both motor types on occupied Arrow singles and coupled reverse pairs, plus individual Legacy, Free Spin and Revised cars. The fixtures use the provider's non-overlapping chassis interface locations and do not replace family-owned hardware. The active sibling checkout is not the integration dependency.

Linear exit transport and section transfers are explicitly rejected pending certification. Swing/multiple-pass programming, direction-aware block reservations, linear station cycles, hot-store/missing-support interruptions, real restart/chunk/multiplayer behavior, rendered survival setup and Ponder remain open. [Validation status](VALIDATION.md) identifies exact artifacts and observed test scope; a passing focused fixture is not a complete LS201/LS202 or 0.4.0 gate.
