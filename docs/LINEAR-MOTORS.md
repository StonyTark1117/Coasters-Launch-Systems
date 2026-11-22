# Linear motors and swing launches

Launch Systems implements original standalone linear synchronous and induction cabinets. Its models, tuning and resource scale are game approximations. Optional launch addons are not required. Existing tire drives, spring friction fins, passive magnets, provider capture and receiver repair remain available. The 0.2 acceptance record combines native accounting, interruption and persistence checks with an occupied Survival-built mixed LSM/LIM swing.

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

Heat uses the existing 4096-unit store and 8 units/s cooling, with force derating above capacity and a 10% force floor. The isolated native motor suite heats real LSM/LIM cabinets to twice capacity and verifies their reduced authority, physical travel and impulse-work accounting. A running local section fails closed when a participating linear cabinet crosses the derating threshold, retaining its reservation and spring-fin hold. Resources, settings and geometry persist in equipment schema 12; accepted restart and gameplay evidence is linked below. Legacy schemas load linear buffers empty. Server loading always disables the field; buffer energy cannot replay a launch. Native handles, drive history and requests are not saved.

## Local sections and Fourth Dimension

Link disarmed linear cabinets to a bound local ride controller with the existing span tool. Launch pads and a contiguous downstream friction-fin bank are still required. The chassis also keeps its certified tire strip and friction fin for the existing station-hold operations. Each reserved member must have every participating drive interface; missing or mixed providers, duplicate bodies, exclusive ownership and native chain conflicts deny propulsion.

Before `READY`, every linear cabinet must independently fund a conservative launch of the whole loaded consist from rest, including worst forward-slip conversion, copper/excitation, acceleration/jerk duration and a 32-unit margin. A target/load whose reserve exceeds a cabinet's capacity stays held. Partial charge does not consume a held-high request later: provide a new low/high edge after readiness. Only `RUNNING` grants motor work. Loss of release/supply power faults the section, applies supported armed spring pads and retains the train's lease. Returning power or leaving the input high does not resume a faulted launch.

The pinned Fourth Dimension shared-authority API1 adapter adds a linear certificate only for its explicitly supported hardware variants and an actually installed matching plate. Dispatch, restraints, support/fault checks, boarding and carrier rotation remain owned by Fourth Dimension's public operations. PAX retains its exclusive winch veto: focused LSM and LIM controller cases prove it faults before a reservation can issue a field impulse. Positive native linear sections cover both motor types on occupied Arrow singles and coupled reverse pairs, plus individual Legacy, Free Spin and Revised cars. The fixtures use the provider's non-overlapping chassis interface locations and do not replace family-owned hardware. The active sibling checkout is not the integration dependency.

## Bounded linear-motor swing launches

With a disarmed, bound local controller that has no transfer or station-capture role, use the span tool with a compass in the offhand on the controller to select the forward/backward/forward program. Use positive LSM or LIM targets: the controller signs the live field command for the return leg without changing the saved cabinet tuning. Install continuous spring-friction fin banks at both fixed-section ends, with each bank long enough for the same loaded stopping envelope. The train stages held on the upstream bank.

Every participating LSM or LIM cabinet must reserve three conservative launch budgets before the controller can become ready. The controller retains its ordinary full-section lease for the entire program. It releases the active end bank while driving, records the whole consist tail crossing the fixed midpoint in the active direction, enters the opposite end bank only after that tail-clearance proof, and starts the next leg only after the spring-fin hold. After the third forward leg it remains held. Loss of authority during any leg faults closed, applies the linked fin hold and keeps the lease; restoring authority cannot replay the field. The scoped native fixtures cover occupied ordinary, pinned Fourth Dimension Arrow, Legacy, Free Spin and Revised vehicles, and a linked Arrow normal run for each motor type. Reversed-leg power interruption is covered for eligible single-car fixtures and linked Arrow.

Linear exit transport and section transfers are explicitly rejected pending certification. Retained-world restart fixtures cover running ordinary and pinned Fourth Dimension Arrow, Legacy, Free Spin and Revised LSM/LIM sections: controller/cabinet identity, lease, energy, heat and configuration reopen, while every cabinet is disarmed and the controller remains in inspection so no field request can replay. Linear station cycles remain outside this package; mixed-provider consists remain denied unless a provider publishes a joint-consist contract. Accepted common multiplayer, reconnect and interruption behavior carries forward from 0.1. The Ponder linear hardware guide supplements the normal hardware and controller workflow. A removed matching reaction plate or thermal derating during a running local section faults the controller, preserves its lease and applies the downstream spring fin hold without replay. The [0.2 acceptance record](evidence/ls02-linear-20261122/README.md) identifies current artifacts, carried-forward evidence and observed player outcomes. It does not certify the explicitly excluded station/transfer modes.
