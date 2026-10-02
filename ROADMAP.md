# Coasters: Launch Systems release roadmap

Implementation started September 30, 2026 toward the user's goal of a validated 0.4.0 release in its own private GitHub repository. LS001–LS005, LS101–LS104, LS201–LS202 and LS301–LS303 are in progress; remaining packages are planned. Track actual acceptance in [validation status](docs/VALIDATION.md). Stable LS identifiers allow later requests to name a concrete scope. Completing a package requires its evidence; version numbers do not establish completion. The user approved a standalone core with optional integrations.

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

Dev.13 adds original LSM/LIM cabinets, matching chassis plates, finite kinetic charging, separate slip/loss response, signed targets and acceleration/jerk tuning. Scoped occupied local sections coordinate motor banks, wait for conservative whole-train charge and retain leases through loss/return of power. Arrow and individual Free Spin continue to use their native vehicle owner. See [linear scope](docs/LINEAR-MOTORS.md). LS201/LS202 and their 0.1 prerequisites remain open for rendered gameplay, lifecycle and broader cases; LS203 swing passes and linear section transfers are not enabled.

| ID | Work | Prerequisites and acceptance |
| --- | --- | --- |
| LS201 | Original LSM drive, buffer, vehicle reaction interface and tuning | 0.1. Finite power, target/jerk limits, rolling entry, reverse propulsion and braking pass loaded tests. |
| LS202 | Distinct LIM hardware and slip/loss curve | LS201. Force/resource behavior differs meaningfully from LSM under the same test load and configured supply. |
| LS203 | Bounded swing-launch program and direction-aware block reservations | LS201–LS202. Forward/backward/forward passes advance only after correct tail clearance; rollback retains reservations. |
| LS204 | Skid brake and heat/cooling limits | LS102–LS103. Contact rejects incompatible vehicles; brake work heats stores, overload changes behavior as documented, and recovery does not duplicate energy. |
| LS205 | All eligible 4D family linear-drive/brake fixtures | LS004, LS201–LS204. Legacy, Free Spin and revised groups retain independent loaded rotation; Arrow retraces profiles in rollback. PAX remains under its declared restrictions. |

## Version 0.3 physical flywheel launch

Dev.14 adds a [native cable-mechanics prototype](docs/CABLE-MECHANICS.md): a real constrained carriage pulls a separate chassis socket, releases, brakes and physically returns. Finite flywheel work, loaded ordinary/coupled/Arrow motion, misalignment and unsupported-family denial, external interruption hold, early reset denial, handle edits and changing mass pass scoped fixtures. This remains internal mechanics: playable guide/flywheel/drum controls, shared controller ownership, journals/restart/recovery, removed support and rendered/lifecycle acceptance remain open.

Dev.15 adds a craftable world cable-guide bed and a kinetic flywheel cabinet with a finite saved store. Rig creation and every physics step validate continuous guide placement. Removing a bed during pull causes a retained fault and ends resource draw. The flywheel cabinet charges from Create shaft RPM and reloads disarmed; its clutch cannot yet arm because controller/catch integration and inspected recovery are still required. The fixture still drives its own cable store and stopping clamps, so this is not a playable launch system.

Dev.16 feeds the native catch from the placed cabinet's actual saved flywheel store. Active catch ownership pauses background charging; shaft work first funds physical carriage return and only the remainder recharges the store. A persisted active-catch journal blocks automatic restart, binding edits and equipment removal after an interrupted reload. Test fixtures still request engagement/dispatch and provide final train stopping, so player controller/clutch integration and inspected recovery remain open.

Dev.17 adds a placed controller path for one flywheel, a catch-home launch fin and a continuous downstream stop bank. An occupied ordinary cart engages and dispatches from the cabinet and a fresh redstone input, then stops under linked pads while the carriage physically returns. Installation is journaled and only a disarmed, physically home carriage retires. A pinned Arrow case also launches with an independent preparation hold. The installed-carriage journal currently fails closed across reload; native identity inspection and reclamation, Arrow staging equipment, real chunk/restart and rendered operation remain open.

Dev.18 records the installed native carriage UUID and accepted rail home in the cabinet, and registers a Sable force-load ticket for that carriage. Controller dispatch checks the live carriage against the saved identity and home; physical retirement removes the ticket and journal. Legacy journals without an identity remain disarmed and faulted. In-memory NBT reload and ticket lifecycle have focused native coverage, but a real world restart, native constraint reconstruction, inspected reclamation and broader gameplay acceptance remain open.

Dev.19 adds a cabinet-addressed loading ticket and a passive guide/brake reconstructed for an idle carriage on world startup. A separate two-process packaged GameTest saves and reopens the same world, confirms the carriage stays at home through repeated restarts, then retires it under explicit empty-track inspection and confirms the retired journal persists. This closes the idle-carriage restart/reclamation slice; active catches, legacy UUID-only ticket stabilization, real chunk/multiplayer/rendered service and the remaining mechanism gates are still open.

Dev.20 journals the captured chassis and force-loads captured native bodies while the catch is engaged. The carriage now uses a mass-centred pose pivot so Sable's saved pose preserves its world position. A real occupied home catch survives a separate server restart with both bodies present, the socket aligned, a passive carriage guide/brake and the cabinet disarmed; the earlier idle restart/inspection path still passes. Reconstructing the active catch joint and an inspected operator release, mid-pull/return restarts, articulated-body saved-world coverage, chunk/multiplayer/rendered service and the remaining mechanism gates are still open.

Dev.21 reconstructs a bounded zero-target native catch joint after a saved home catch reopens. The passive carriage guide remains in place while startup verifies current provider/consist identity, every captured native body and its ticket, the socket and settled pose. Separate-process occupied ordinary and pinned Fourth Dimension Arrow fixtures check that the joint and alignment survive startup. A removed-socket fixture retains the passive carriage hold but denies joint reconstruction. The cabinet stays disarmed; active operator release, moving catches, actual chunk/multiplayer/rendered operation and the other release gates remain open.

Dev.22 extends that fail-closed native hold to a catch stopped partway along its verified guide after an interrupted pull. A real physics-driven fixture reaches roughly 5–6 blocks of travel, faults and settles before saving. Separate-process ordinary and pinned Arrow restarts keep the same body identities, guide position, socket alignment and reconstructed catch joint without resuming drive. Idle carriages still require home. This does not cover saving while moving, active operator release, support-loss repair or broader service acceptance.

Dev.23 adds the active home-catch inspection release. A recovered, disarmed catch can only disconnect after its exact saved provider/consist, all native bodies and tickets, socket, controller reservation and settled home pose pass inspection, and every captured chassis is physically held by an applied linked friction-fin clamp. A redstone torch in the span tool offhand requests that release; it cannot arm or dispatch. The operation drops captured-body tickets and reservation, retains the faulted home carriage and persists as an idle inspection state. The initial positive fixture is ordinary-cart only; Arrow active-release, moving restart, powered interruption and broader service acceptance remain open.

Dev.24 journals whether an ordinary catch was still physically pulling at save time. Reload permits only that recorded state to use a bounded 3-block/second guide-aligned recovery envelope, with 0.15-block transverse and 0.1 angular limits, exact body-ticket ownership and a 0.25-block temporary socket/body-speed alignment check. A passive guide/brake and native catch joint then settle the train; strict 0.03-block inspection resumes when the journal clears. The saved-world fixture records a catch at 5.59 blocks and 2.79 blocks/second, reloads it through the bounded window, and proves it settles with its reconstructed joint. Arrow moving restart, a placed-cabinet drive interruption, real chunk/multiplayer/rendered service and broader release gates remain open.

Dev.25 applies the same bounded moving-restart path to the pinned Fourth Dimension schema-4 Arrow provider. A two-process saved world preserves the Arrow’s provider-owned native bodies and tickets through a live pulling save, reconstructs the passive guide and catch joint while disarmed, and clears the movement journal once the train settles. The Arrow fixture stops its save at a four-block guide threshold so the loaded Arrow stays inside the same measured 3–12-block recovery span. Placed-cabinet drive interruption, Arrow active release, real chunk/multiplayer/rendered service and broader release gates remain open.

Dev.26 adds an end-to-end ordinary placed-controller shaft interruption. The live controller dispatches through its placed cabinet, guide, catch and fin bank; the fixture then stops the actual Create creative motor. The controller’s production loss branch faults the cabinet and applies the retained native emergency hold, leaving the occupied train settled, loaded and ineligible for rearm. Arrow controller interruption, Arrow active release, actual chunk/multiplayer/rendered service and broader release gates remain open.

Dev.27 applies that placed-controller interruption to the pinned Fourth Dimension Arrow provider. The Arrow uses its existing native preparation clamp only to establish safe engagement; after the controller starts its provider-owned pull, the fixture stops the actual cabinet motor. The production loss branch holds the loaded Arrow catch faulted and settled with its provider body tickets retained. Arrow active release, actual chunk/multiplayer/rendered service and broader release gates remain open.

Dev.28 begins LS401–LS402 world equipment. Original hydraulic and pneumatic launch cabinets now have craftable placed blocks, independent finite persisted accumulator/receiver stores and distinct Create shaft charging losses. They load disarmed, and their pressure models retain separate oil-return and gas-expansion behavior. Dev.29 connects those stores to the physical cable carriage: hydraulic force follows accumulator pressure, pneumatic force follows receiver expansion, and the controller, provider certificates and reload inspection retain the selected cable mechanism. Stored pressure funds the pull; Create shaft work drives charging and the physical carriage return. Dev.30 proves that an ordinary or pinned Arrow pressure launch retains its physical pull through a temporary charging-shaft loss, then resumes shaft-powered return after supply restoration. Dev.31 adds two-process ordinary active-catch restart coverage for both pressure stores; the cabinet stays disarmed and its native guide/catch reconstruct only for the saved journal. Dev.32 proves each ordinary pressure cabinet retains its physical return carriage during a temporary charging-shaft loss and resumes its bounded return after restoration. Dev.33 applies that return interruption to the pinned Arrow provider. Pressure restart while returning remains open.

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
