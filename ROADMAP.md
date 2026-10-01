# Coasters: Launch Systems release roadmap

Implementation started September 30, 2026 toward the user's goal of a validated 0.4.0 release in its own private GitHub repository. LS001–LS005 and LS101–LS104 are in progress; remaining packages are planned. Track actual acceptance in [validation status](docs/VALIDATION.md). Stable LS identifiers allow later requests to name a concrete scope. Completing a package requires its evidence; version numbers do not establish completion. The user approved a standalone core with optional integrations.

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

The 0.1 gate requires all packages through LS106. It includes verified Arrow and at least one free-spin family; revised carriers and broader variants continue at later gates. PAX is recognized and vetoed when its winch owns the train.

Dev.10 adds the repeatable native circuit prototype described in [circuit scope](docs/CIRCUITS.md). A longer handoff resolves a unique, unbranched directed route, retains its geometry and loaded-chunk requirements, and reserves the route and complete receiver before departure. Actual accepted contact on intermediate curves keeps the whole-consist leases alive. Departure speed includes gravitational potential gain; native downhill acceptance remains pending. Tail clearance still uses loaded native body bounds. Handle-only edits invalidate the route even without a graph revision.

Five occupied fixtures complete two laps, actual passenger exit/reboarding and a fresh departure. Dev.11 adds optional controller/provider station capture, described in [station scope](docs/STATIONS.md). Its API-5 operation defaults to denial, verifies whole-train physical hold, live consist and exclusive ownership, and delegates public capture/readiness to the vehicle owner while retaining the lease. Open restraints keep departure disabled; closing does not replay a held-high request. Carrier angles remain native-owned. Real tool configuration and legacy opt-out are checked, but rendered survival instructions, general profile traversal and multiplayer lifecycle acceptance remain required.

Dev.12 adds [inspected receiving-controller recovery](docs/RECEIVER-RECOVERY.md) for an active retained transfer. A schema-5 equipment journal preserves coordinated spring braking and identifies the exact original modules. A one-time native inspection restores an empty replacement at the saved position only after whole-train hold, unchanged loaded route, complete equipment/ownership and stopping/conflict checks. Both controllers stay disarmed; separate arming and a fresh request complete native tail handoff. Ordinary coupled and occupied Arrow/Free Spin cases, moving/refused inspections and queued-request cancellation pass scoped native checks. Missing-source and changed-hardware/geometry repair, rendered operator setup and real restart/chunk/multiplayer validation remain open.

Acceptance remains three usable demonstration rides: ordinary, Arrow and individual free spin. Each must complete repeated departure, route traversal, whole-train stop, appropriate native capture, passenger exit/boarding and a fresh dispatch request. An occupied route, changed geometry, missing receiver, lost power/support, rollback or unavailable chunk must retain ownership and deny further propulsion. Rendered survival setup and packaged multiplayer/restart evidence remain required.

## Version 0.2 linear motors and local block operation

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS201 | Original LSM drive, buffer, vehicle reaction interface and tuning | 0.1. Finite power, target/jerk limits, rolling entry, reverse propulsion and braking pass loaded tests. |
| LS202 | Distinct LIM hardware and slip/loss curve | LS201. Force/resource behavior differs meaningfully from LSM under the same test load and configured supply. |
| LS203 | Bounded swing-launch program and direction-aware block reservations | LS201–LS202. Forward/backward/forward passes advance only after correct tail clearance; rollback retains reservations. |
| LS204 | Skid brake and heat/cooling limits | LS102–LS103. Contact rejects incompatible vehicles; brake work heats stores, overload changes behavior as documented, and recovery does not duplicate energy. |
| LS205 | All eligible 4D family linear-drive/brake fixtures | LS004, LS201–LS204. Legacy, Free Spin and revised groups retain independent loaded rotation; Arrow retraces profiles in rollback. PAX remains under its declared restrictions. |

## Version 0.3 physical flywheel launch

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS301 | Shared cable guide, catch socket, carriage and release/reset machine | 0.2. Real constrained pull, rated guide/end clearance, physical return and misalignment handling. No teleport, one-shot impulse or prescribed train path. |
| LS302 | Kinetic flywheel storage, clutch, drum and holding brake | LS301. Charge and launch work reconcile; store depletes, release occurs before the stop, and reset blocks premature launch. |
| LS303 | Cable-system interruption and 4D catch certification | LS302. Loaded ordinary and eligible Arrow consists survive power loss/restart within measured catch/drum limits. Unsupported individual-car sockets are rejected. |

## Version 0.4 hydraulic and pneumatic launch

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS401 | Hydraulic pump, oil return circuit, accumulator and drive | 0.3. Finite pressure/flow work drives the catch; closed-loop return, partial charge, interruption and reset pass. Reaudit any upstream hydraulic release first. |
| LS402 | Pneumatic compressor, receiver, valve and launch actuator | LS301. Pressure/volume discharge and recharge materially differ from hydraulic/flywheel models; no external pneumatic mod required. |
| LS403 | Loaded mechanisms and loss-of-support verification | LS401–LS402. Catch, brakes and release retain native train engagement in both normal and interrupted operations; recipes and Ponder match the actual mechanics. |

## Optional adapters

| ID | Adapter | Acceptance |
| --- | --- | --- |
| LS801 | Engineered 1.1.1 operating integration | Reuse its existing controls without double force, energy accounting or incompatible assembly assumptions. Loaded 4D and rollback tests pass; disjoint original equipment still works. |
| LS802 | Magnetized acceleration/braking integration | Inspect and pin its implementation. Define ownership and block unsupported overlaps; demonstrate actual 4D physics before enabling support. |
| LS803 | Additions station and restraint signals | Audit actual dependencies and force/chain overrides. Imported dispatch remains a request subject to Fourth Dimension readiness and our reservations. |
| LS804 | Track Styles appearance/geometry integration | Inspect current 1.2.0 separately from the old audit. Mid-curve styles retain contact/clearance and independent equipment visibility; no cosmetic Arrow 4D eligibility. |
| LS805 | FE, Jade, JEI/EMI and Immersion presentation | Each integration works absent/present independently; no duplicated energy or camera transform. |
| LS806 | PAX final-brake handoff | Only after Fourth Dimension's own shuttle lifecycle gates pass. Its state machine explicitly delegates/returns braking authority; no launch or unloading on normal station passes. |

Adapters can be developed alongside core releases, but do not block a standalone release. Unsupported adapters stay disabled and retain their saved provider identity. PAX veto behavior is core compatibility; LS806 is additional positive functionality.

## Version 0.9 release candidate and version 1.0

| ID | Work | Acceptance |
| --- | --- | --- |
| LS901 | Complete supported vehicle and provider matrix | All advertised hardware/contact/family combinations pass; unsupported combinations, mixed consists and conflicts reject predictably. |
| LS902 | Persistence, migration and packaged multiplayer lifecycle | Old schemas, server restart, rejoin, missing providers, removed support and chunk availability pass without replaying launches or changing vehicle families. |
| LS903 | Performance and resource accounting | Publish scene/hardware/load and measured per-step costs; reconcile energy/pressure/heat with motion, including no FE conversion loops. |
| LS904 | Release gameplay, documentation and artifact audit | Ordinary and occupied 4D demonstrations for all six launches; fin/skid/passive/active/tire braking instructions; tutorials, licenses and exact dependency pins match the packaged artifact. |
| LS1001 | Publish standalone 1.0 after every core gate passes | Completed LS001–LS005, LS101–LS106, LS201–LS205, LS301–LS303, LS401–LS403 and LS901–LS904, with linked evidence. Advertise optional integrations only for completed adapter packages. |

No calendar estimates are assigned before the loaded force and catch prototypes. Future weight-drop launching, moving-track equipment, complex switch routing and park scheduling remain outside the 1.0 gate.
