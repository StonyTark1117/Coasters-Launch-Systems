# Coasters: Launch Systems version roadmap

Updated **2026-11-27 (America/Phoenix)**. **0.1.0 is accepted**: foundation
LS001–LS005 and usable-ride packages LS101–LS106 are complete, with exact scope
and inputs in the [integration record](docs/evidence/0.1/integration-20261122/README.md).
The **0.4.0 six-launch outcome is accepted**, including LS201–LS205,
LS301–LS303 and LS401–LS403; see the [combined integration record](docs/evidence/0.4/integration-20261122/README.md).
**0.5 (LS501–LS504)** is accepted; see the [family integration record](docs/evidence/0.5/integration-20261126/README.md).
**0.6 (LS601–LS602)** is accepted; see the [route/operator integration record](docs/evidence/0.6/integration-20261126/README.md). **0.9 (LS901–LS904)** is assigned for release-candidate qualification. Optional adapters and 1.0 publication remain outside this assignment.
[Validation](docs/VALIDATION.md) owns accepted evidence;
[CURRENT-WORK](docs/CURRENT-WORK.md) owns assignments. Stable LS IDs retain
scope and acceptance. Version milestones express outcomes and order, not dates.

## Release map

| Version / stage | Player outcome | Packages / status |
| --- | --- | --- |
| Foundation refresh | A working modern standalone stack and an explicit current Fourth Dimension bridge | LS001–LS005 accepted, including standalone startup, current FD authority and clearance. |
| 0.1 — Usable rides | Tire transport/launch, final-hold fin clamps, passive magnetic trim, local controller, three player-built ordinary/Arrow/Free Spin rides | LS101–LS106 accepted: three player-built empty/occupied rides, normal exit/reboard and packaged lifecycle evidence. |
| 0.2 — Linear launches | Distinct LSM and LIM, bounded swing launches, skid/thermal braking and original-family certification | LS201–LS205 accepted: native original-family coverage, actual occupied swing/skid and persistence. |
| 0.3 — Physical cable launch | Finite flywheel energy, constrained catch/carriage, release, physical return and inspected recovery | LS301–LS303 accepted: occupied pull, finite work, physical return/service and current Arrow recovery. |
| 0.4 — Complete six-launch release | Hydraulic and pneumatic equipment join tire, LSM, LIM and flywheel; coherent usable content and interruption behavior | LS401–LS403 and preceding gates accepted: distinct occupied pressure operation, recharge, service and interruption. |
| 0.5 — Expanded FD vehicle support | Explicit launch/brake certificates for the modern FD catalog, preserving each family's mechanics and body choices | LS501–LS504 accepted: finite family certificates, occupied controls, body-choice invariance and explicit mechanical exclusions. |
| 0.6 — Shared ride operation | Launch Systems equipment respects FD's bounded multi-train reservations and operator controls | LS601–LS602 accepted; native bounded route, occupied conflict and explicit saved-world recovery. |
| 0.9 — Release candidate | Reconciled advertised matrix, migration/multiplayer, measured costs and finished instructions | LS901–LS904 accepted; finite matrix, provider lifecycle, measured cost and occupied release gameplay reconciled. |
| 1.0 — Stable | A supported, documented package with all required core and adopted FD outcomes accepted | LS1001; planned. Optional LS801–LS807 adapters ship only when independently accepted. |
| After 1.0 | Consider moving-track launch interfaces, new specialty vehicles and wider routing | Separate feasibility/design decisions; no implied support or date. |

0.1–0.4 are cumulative acceptance milestones, not four required publication
ceremonies. Preserve the original six-launch scope. Do not delay 0.4 for the
new 0.5/0.6 expansion or optional adapters. Newly allocated 0.5/0.6 outcomes
belong to the expanded 1.0 plan. FD 1.4 specialty vehicles and moving tracks
remain outside that finite catalog. No source version bump, upload or tag is
part of this planning change.

## What changed and what to do next

The accepted Fourth Dimension baseline is `59c40d11` (1.0 readiness and
1.1/1.2/1.3 implementation), with our authority facade integrated and pushed
on `main` as `235255cf`. The current artifact
label is still **1.1.0**, schema **8**; published 1.0.0 is Beta. The old LS
`0.9.0-dev.1`/schema-4 adapter supports neither that label nor those contracts.
Use the [source/artifact handoff](docs/FOURTH-DIMENSION-HANDOFF.md), not a
milestone number as a compatibility check.

The [dated dependency review](docs/DEPENDENCY-REVIEW-2026-11-22.md) verifies
Coasters Simulated **0.1.6**, newer NeoForge/Ponder/Flywheel Maven coordinates,
Additions **0.2.5**, Track Styles **1.2.0** and Immersion **1.1.1**. Create's
released baseline and Sable remain 6.0.10 and 2.0.6 in the reviewed listings.
Newer available artifacts are candidates until their consumed contracts pass.

1. **LS001/LS002:** standalone qualification passed on NeoForge 21.1.256 /
   Sable 2.0.6, including Veil startup and affected cable ticket/mass recovery.
   Coasters 0.1.6 was audited and explicitly deferred for 0.1, retaining FD
   parity at 0.1.5. See [dependency selection](docs/DEPENDENCIES.md).
2. **LS004 / affected LS003:** the registered API 1 facade is integrated
   upstream; native, serialized-token, real occupied Arrow and separate-JVM
   restart proof pass. Preserve the registered owner/token contract
   and native family, restraint and route-management vetoes.
3. **LS105/LS106:** complete. Ordinary, Arrow and Free Spin player-built rides
   passed empty/occupied circuits, exit/reboard and fresh dispatch. Reconnect,
   current authority restart and retained interruption evidence cover lifecycle.
   All owned runtimes saved and stopped; preserve applicable dev.65–72 evidence.
4. **LS201–LS403:** accepted linear, cable and pressure outcomes include
   current-Arrow catch recovery, normal occupied construction and physical service.
5. **LS501–LS602:** accepted finite family certificates and bounded Arrow
   route/operator recovery. **LS901–LS904** reconcile the supported package;
   [current work](docs/CURRENT-WORK.md) owns its latest disposition. Optional
   adapters and 1.0 publication remain separate assignments.

[Execution guide](docs/ROADMAP-EXECUTION.md) gives package inputs and handoffs.
[Historical dev notes](docs/ROADMAP-CHECKPOINT-HISTORY-2026-11-22.md) preserve
the removed checkpoint narrative. Follow [validation policy](docs/VALIDATION-POLICY.md):
a documentation change or new artifact hash never resets closed cases.

## Foundation

| ID | Work | Acceptance |
| --- | --- | --- |
| LS001 | Scaffold the pinned NeoForge project, colon-safe output directory, dependency manifest, packaged client/server runs and license inventory | Reproducible clean build and packaged client/dedicated-server startup using the same base stack as Fourth Dimension. |
| LS002 | Audit current Coasters and companion force hooks; inspect exact released artifacts and their terms | Record versions, hashes, signatures, event ordering and control ownership. Identify where an addon can actually be prevented from applying a second force. |
| LS003 | Prototype loaded ordinary, Arrow and independent free-spin launch/brake behavior | Compare interface forces and recovery corrections. Record mass/work/impulse, joint error and rotor response in both directions. Preserve native engagement and rider motion without resetting angles. |
| LS004 | Define/register the versioned motion-provider API and Fourth Dimension facade | Both mods work independently. Combined installation preserves family restrictions, restraint/retention/fault vetoes, carrier mass and exclusive PAX ownership. Missing/stale providers fail clearly. |
| LS005 | Implement directed span placement, geometry revisions, contact interfaces and occupancy | A complete span crossed in one physics step is detected. Wrong interfaces and obstructed envelopes reject placement; edits invalidate correctly without losing mechanical identity to a style change. |

Foundation is a release gate. LS003 and LS004 require a validated Fourth Dimension bridge. Keep integration work isolated from other active checkouts and require a reviewed public facade or a pinned adapter before publishing compatibility.

## Version 0.1 tire drives and distinct brakes

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS101 | Shaft-powered tire transport, reverse and launch modules | Foundation. RPM/traction/power bound force; empty/full and rolling-entry behavior pass native fixtures. |
| LS102 | Friction fin clamp with final stop and rated static hold | Foundation. Stops and holds the declared loaded train on supported slopes; power loss, creep and restraint interlocks pass. |
| LS103 | Passive magnetic brake | Foundation. Opposes both travel directions, approaches zero force at rest, dissipates energy, and needs LS102 for final hold. |
| LS104 | Local arm/hold/launch/fault controller, signals and conflict coordinator | LS101–LS103. No launch through vehicle faults or occupied stopping regions; upstream chain/unknown provider overlaps reject clearly. |
| LS105 | Recipes, placement tool, Ponder and three demonstration rides | LS101–LS104. Survival setup and empty/occupied ordinary, Arrow and individual free-spin rides work from documented instructions. |
| LS106 | Packaged multiplayer and interruption evidence | LS105. Late join, restart, power/support loss and active chunk unavailability preserve mechanism and vehicle ownership. |

The 0.1 gate is accepted with all packages through LS106. It includes verified Arrow and at least one free-spin family; revised carriers and broader variants continue at later gates. PAX is recognized and vetoed when its winch owns the train.

Acceptance remains three usable demonstration rides: ordinary, Arrow and individual free spin. Each must complete repeated departure, route traversal, whole-train stop, appropriate native capture, passenger exit/boarding and a fresh dispatch request. An occupied route, changed geometry, missing receiver, lost power/support, rollback or unavailable chunk must retain ownership and deny further propulsion. Rendered survival setup and packaged multiplayer/restart evidence remain required.

## Version 0.2 linear motors and local block operation

Accepted with [current linear/skid evidence](docs/evidence/ls02-linear-20261122/README.md) and its explicitly retained native/shared lifecycle cases.

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS201 | Original LSM drive, buffer, vehicle reaction interface and tuning | 0.1. Finite power, target/jerk limits, rolling entry, reverse propulsion and braking pass loaded tests. |
| LS202 | Distinct LIM hardware and slip/loss curve | LS201. Force/resource behavior differs meaningfully from LSM under the same test load and configured supply. |
| LS203 | Bounded swing-launch program and direction-aware block reservations | LS201–LS202. Scoped LSM/LIM forward/backward/forward ordinary plus pinned Arrow, Legacy, Free Spin and Revised fixtures pass after the correct midpoint tail clearance. Linked Arrow normal operation also passes only after the full consist tail clears midpoint. Eligible single-car families and linked Arrow reversed-leg authority-loss cases retain the full-section lease and fin hold after power returns. Current authority, persistence and occupied player-operation acceptance is complete; unchanged shared lifecycle evidence carries forward. |
| LS204 | Skid brake and heat/cooling limits | LS102–LS103. A standalone skid cabinet, separately placed shoe, ordinary/4D adapters and bounded heat-derated force path are implemented. Focused optional-provider and fail-closed saved-world interruption acceptance passed; current Arrow skid and actual occupied trim complete gameplay acceptance. |
| LS205 | All eligible 4D family linear-drive/brake fixtures | LS004, LS201–LS204. Focused LSM/LIM fixtures cover Arrow, Legacy, Free Spin and Revised with loaded independent carrier rotation; coupled reverse Arrow remains within native limits. Current authority-loss, Arrow persistence and occupied gameplay checks complete the assigned original-family outcome. PAX retains its declared restrictions. |

## Version 0.3 physical flywheel launch

Accepted with [current native/restart evidence](docs/evidence/0.3/cable-current-20261122/README.md), normal occupied construction/pull, corrected physical return and shared pressure service proof.

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS301 | Shared cable guide, catch socket, carriage and release/reset machine | 0.2. Real constrained pull, rated guide/end clearance, physical return and misalignment handling. No teleport, one-shot impulse or prescribed train path. |
| LS302 | Kinetic flywheel storage, clutch, drum and holding brake | LS301. Charge and launch work reconcile; store depletes, release occurs before the stop, and reset blocks premature launch. |
| LS303 | Cable-system interruption and 4D catch certification | LS302. Loaded ordinary and eligible Arrow consists survive power loss/restart within measured catch/drum limits. Unsupported individual-car sockets are rejected. |

## Version 0.4 hydraulic and pneumatic launch

Accepted with [pressure evidence](docs/evidence/ls04-pressure-20261122/README.md): distinct finite stores, actual occupied operation/recharge/service, current interruption checks and rendered instructions.

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS401 | Hydraulic pump, oil return circuit, accumulator and drive | 0.3. Finite pressure/flow work drives the catch; closed-loop return, partial charge, interruption and reset pass. Reaudit any upstream hydraulic release first. |
| LS402 | Pneumatic compressor, receiver, valve and launch actuator | LS301. Pressure/volume discharge and recharge materially differ from hydraulic/flywheel models; no external pneumatic mod required. |
| LS403 | Loaded mechanisms and loss-of-support verification | LS401–LS402. Catch, brakes and release retain native train engagement in both normal and interrupted operations; recipes and Ponder match the actual mechanics. |

## Version 0.5 expanded Fourth Dimension certificates

This extends the original-family LS205 outcome; it does not reopen it. The
catalog is FD's accepted pre-1.0 vehicle set plus 1.3 appearance/body variants,
not every future FD vehicle. Shared schema fields and similar models do not
establish shared drive surfaces, brake fins or catch sockets.

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS501 | Fixed-seat and spinning family interfaces | 0.4 and LS004. Certify tire/fin/linear interfaces where physically valid for Wing, Fixed Inverted, Classic Spinning and Launched Inverting Spinning. Preserve native pivot/mass, inversion/rotation limits and capture. Publish positive combinations and specific mechanical exclusions. |
| LS502 | Articulated and posture-dependent interfaces | LS501's shared certificate contract. Assess Suspended, Axis, Conventional Flying, New-generation Flying, Pipeline, Dive and Powered Inverted Yaw with their native locks/postures/holds. Provide usable propulsion/braking on eligible interfaces; posture/yaw/vertical-drop state cannot be bypassed by a launch request. |
| LS503 | Roller Ball and specialized cycle boundaries | LS004 and the applicable cable/linear/brake gates. Resolve Roller Ball's route/lift/return contact separately; certify only a physically meaningful interface. Retain PAX's core veto; positive PAX final-brake delegation remains optional LS806. No generic catch fitted to every vehicle. |
| LS504 | Integrated family support and body/appearance persistence | LS501–LS503. Publish a finite family × equipment certificate table; each advertised combination has loaded native and relevant occupied behavior, each excluded combination has a reason and denial. FD's compact Wing/open-frame Fixed Inverted choices and colors must preserve the certificate, mass, contacts and saved mechanical identity. Shared evidence is reused where the contract is unchanged. |

0.5 closes with a complete disposition of that catalog and functioning eligible
interfaces. A technically unsupported combination needs an evidenced mechanical
reason; "not tested yet" remains open. Cable/flywheel/hydraulic/pneumatic support
continues to require a certified socket and measured loaded-consist limits.

## Version 0.6 ride-local operations interoperability

Accepted with [integrated native and occupied recovery evidence](docs/evidence/0.6/integration-20261126/README.md); all owned runtimes saved/stopped.

FD 1.2 already owns a bounded original-Arrow circuit: eight route segments,
six reservation blocks, at most three trains. It is not an arbitrary park
scheduler. Launch Systems must respect those reservations as well as its own
physical stopping and equipment leases. Until positive integration is accepted,
reject the overlapping operating mode with a clear reason; never run two
independent block owners on the same route.

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS601 | Equipment leases under FD route authority | 0.4, LS004 and the relevant Arrow certificate. Reconcile FD admission/current-next reservations with LS stopping-region leases through an explicit contract. A denied/changed reservation, stale owner, opposing move, rollback or unloaded body prevents propulsion and retains the required hold. The first supported scope is FD's actual bounded Arrow circuit. |
| LS602 | Operator requests and multi-train recovery | LS601. FD operator controls and LS controls report the same owner, blocked reason, charge and fresh-request state. An occupied multi-train run proves separation and denied dispatch, followed by the affected restart/rejoin/resume path without replaying stored requests. Preserve existing FD tools, vehicle controls and appearance data; no second general park-control UI. |

The 0.6 gate is the integrated bounded outcome, not an expansion of FD's train
capacity or route grammar. Individual launch mechanics and FD's prior route
acceptance remain closed when compatible; qualify the new interaction.

## Optional adapters

| ID | Adapter | Acceptance |
| --- | --- | --- |
| LS801 | Engineered 1.1.1 operating integration | The public span/controller bridge now cedes an active shared contact before Launch Systems adds force, and a real configured Engineered `LAUNCH` span proves the predicate; disjoint original equipment passes standalone and loaded-4D matrices. Reusing controls still requires shared energy, rollback and reservation acceptance without incompatible assembly assumptions. |
| LS802 | Magnetized acceleration/braking integration | Inspect and pin its implementation. Define ownership and block unsupported overlaps; demonstrate actual 4D physics before enabling support. |
| LS803 | Additions 0.2.5 station, restraint and launch-span coexistence | Audit actual dependencies, force/chain overrides and hydraulic/flywheel/tire span overlap. Imported dispatch remains a request subject to Fourth Dimension readiness and our reservations. |
| LS804 | Track Styles 1.2.0 appearance/geometry integration | Inspect and pin the current artifact separately from the old audit. Mid-curve styles retain contact/clearance and independent equipment visibility; no cosmetic Arrow 4D eligibility. |
| LS805 | FE, Jade, JEI/EMI and Immersion presentation | Each integration works absent/present independently; no duplicated energy or camera transform. |
| LS806 | PAX final-brake handoff | Consume Fourth Dimension's accepted shuttle lifecycle and current ownership contract. Its state machine explicitly delegates/returns braking authority; no launch or unloading on normal station passes. |
| LS807 | Tracks and Balloons 1.2 coexistence audit | Establish its custom-license terms and a public integration seam before loading any artifact. If supported, prove exclusive force, reservation and station ownership with no double operation; otherwise retain independent, disjoint equipment. |

Adapters can be developed alongside core releases, but do not block a standalone release. Unsupported adapters stay disabled and retain their saved provider identity. PAX veto behavior is core compatibility; LS806 is additional positive functionality.

## Version 0.9 release candidate and version 1.0

0.9 is accepted with the [integration record](docs/evidence/0.9/integration-20261127/README.md).
All owned runtimes saved/stopped. Optional adapters and LS1001 publication remain separate.

| ID | Work | Acceptance |
| --- | --- | --- |
| LS901 | Complete supported vehicle and provider matrix | All advertised hardware/contact/family combinations pass; unsupported combinations, mixed consists and conflicts reject predictably. |
| LS902 | Persistence, migration and packaged multiplayer lifecycle | Old schemas, server restart, rejoin, missing providers, removed support and chunk availability pass without replaying launches or changing vehicle families. |
| LS903 | Performance and resource accounting | Publish scene/hardware/load and measured per-step costs; reconcile energy/pressure/heat with motion, including no FE conversion loops. |
| LS904 | Release gameplay, documentation and artifact audit | Ordinary and occupied 4D demonstrations for all six launches; fin/skid/passive/active/tire braking instructions; tutorials, licenses and exact dependency pins match the packaged artifact. |
| LS1001 | Publish standalone 1.0 after every core gate passes | Completed LS001–LS005, LS101–LS106, LS201–LS205, LS301–LS303, LS401–LS403, LS501–LS504, LS601–LS602 and LS901–LS904, with linked evidence. Advertise optional integrations only for completed adapter packages. |

Milestones are sequenced by accepted outcomes; no calendar dates are promised. Future weight-drop launching, moving-track equipment, complex switch routing and park scheduling remain outside the 1.0 gate.
