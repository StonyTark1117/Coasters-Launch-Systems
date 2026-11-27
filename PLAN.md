# Coasters: Launch Systems implementation plan

Updated **2026-11-27**. Build a standalone Create: Coasters Simulated addon
with physical tire, LSM, LIM, flywheel, hydraulic and pneumatic launches,
distinct brakes, finite resources and explicit vehicle/ride ownership. Native
mechanics and tool workflows are accepted through `0.9.0`, including the foundation, usable rides, linear/skid,
physical cable, pressure, finite modern FD family certificates and bounded Arrow route/operator outcomes. The accepted 0.9 release candidate reconciles its matrix, lifecycle, measured cost and remaining occupied gameplay.

The [version roadmap](ROADMAP.md) is authoritative for scope and acceptance;
[current work](docs/CURRENT-WORK.md) assigns remaining outcomes, and the
[execution guide](docs/ROADMAP-EXECUTION.md) orders their inputs and handoffs.
[Validation](docs/VALIDATION.md) retains the exact accepted mechanics and
player-tool evidence. Original design provenance remains in
[research](research/EVIDENCE.md).

## Current implementation direction

1. Retain the selected current-core stack and its completed standalone
   startup/native/restart qualification. [The November 22 review](docs/DEPENDENCY-REVIEW-2026-11-22.md)
   identifies Coasters 0.1.6, NeoForge 21.1.267 and newer Flywheel/Ponder Maven
   artifacts; availability is not a blanket upgrade instruction.
2. Retain the accepted occupied Arrow and saved-world qualification of the
   registered schema-8 external authority API 1 bridge; preserve the legacy adapter separately. FD has accepted 1.1 tools, 1.2 bounded multi-train operation and
   1.3 model/body work, and the selected snapshot retains artifact label 1.1.0/schema 8. Later published
   FD 1.5.0 does not expand this snapshot’s tested scope.
   Pin source and artifact identity using [the handoff](docs/FOURTH-DIMENSION-HANDOFF.md).
3. Preserve completed **0.1–0.4**: three player-built rides, real-client/saved-world
   lifecycle, linear/skid operation, physical cable return and distinct pressure
   operation/service. [The integration record](docs/evidence/0.4/integration-20261122/README.md)
   reconciles this completed six-launch outcome.
4. The completed **0.5** assignment extends certificates to the finite modern FD
   catalog; [integration evidence](docs/evidence/0.5/integration-20261126/README.md)
   records the exact scope. Completed **0.6** integrates the bounded Arrow
   reservation/controls contract; see its [record](docs/evidence/0.6/integration-20261126/README.md). Cosmetic
   variants never create new mechanical eligibility. Optional operating and
   presentation adapters remain independent. **0.9** reconciliation is accepted in
   its [integration record](docs/evidence/0.9/integration-20261127/README.md);
   1.0 publication remains separate.

## Product scope and dependencies

Use **Coasters: Launch Systems**, mod ID `coasterslaunchsystems`, and original models, sounds, and recipes. Add equipment to existing coaster curves instead of replacing the track graph or introducing another vehicle solver. Start with fixed-world tracks; moving contraption tracks remain future work.

| Component | Reproducible legacy | Selected current 0.9 qualification stack |
| --- | --- | --- |
| Minecraft / Java | 1.21.1 / 21 | Same |
| NeoForge / Sable | 21.1.248 / 2.0.3 | 21.1.256 / 2.0.6 |
| Create / Coasters Simulated | 6.0.10-280 / 0.1.5 file 8655742 | Same |
| Flywheel / Ponder / Registrate | 1.0.6 / 1.0.82+mc1.21.1 / MC1.21-1.3.0+67 | Same |
| FD integration | Exact 0.9.0-dev.1 / schema 4 | Pinned 1.1.0/schema 8 with registered external authority API 1; occupied Arrow and saved-world qualification passed |

[Dependency profiles](docs/DEPENDENCIES.md) describe what is implemented.
The [dated review](docs/DEPENDENCY-REVIEW-2026-11-22.md) separates newer
upstream candidates and adoption decisions. Keep a reproducible baseline while
qualifying changed hooks; promote Gradle pins, metadata and support claims together.
Required mods remain Create, Coasters Simulated and their physics/render stack.
Fourth Dimension and operating/presentation addons stay optional. Aeronautics
is not required for our mechanisms; optional companions may bring their own stack.

Keep build outputs outside colon-containing Unix checkout paths, following Fourth Dimension's existing Gradle workaround. Separate packaged client/server runs from loose development classes.

## Existing systems and integration decisions

| Project | Current planning input | Relationship |
| --- | --- | --- |
| Coasters Simulated | Released 0.1.6 improves sublevel-scan cost; both local projects still pin 0.1.5 | Reuse graph, engagement, linking and chain behavior. Audit the changed hooks before adopting 0.1.6. |
| Fourth Dimension | Accepted implementation through 1.3; schema 8; explicit longitudinal authority and bounded reservations | Accepted bridge, finite modern family certificates (0.5) and bounded Arrow multi-train interaction (0.6). |
| Engineered | Exact 1.1.1 public seam and existing LS conflict-denial evidence | LS801 positive control/resource handoff; retain disjoint operation without double force. |
| Additions | 0.2.5 introduces hydraulic/flywheel/tire launch spans as well as station/control functions | LS803 audits launch ownership and suppression, not only signals. Do not copy restricted implementation/assets. |
| Magnetized / CTNB | FD has positive scoped authority adapters on selected families | Inputs for LS802/LS807; their FD acceptance does not establish LS compatibility. |
| Track Styles | 1.2.0 and FD's current geometry/ownership work | LS804 preserves appearance, contact and equipment clearance; a style never grants functional eligibility. |
| Immersion | 1.1.1 exact release is client-only | LS805 presentation after native rider transforms; exclude from dedicated-server/bootstrap classpaths. |
| Skin / Waterparked / Create Coasters | FD's audited appearance/coexistence records | Secondary references, not mandatory LS dependencies or new support promises. |

Exact source links and verification limits are in the
[November 22 review](docs/DEPENDENCY-REVIEW-2026-11-22.md) and
[optional adapter record](docs/OPTIONAL-INTEGRATIONS.md).

The ecosystem already covers generic acceleration and stopping. Our differentiators are mechanical tire drives, finite stored-energy launchers, physically distinct brakes, and tested operation with articulated 4D vehicles. Original standalone LIM/LSM implementations are accepted in 0.2; optional providers remain separate equipment.

No inspected release establishes the complete set below. This is an evidence-limited finding, not proof that no other addon exists. Source inspection and combined runtime tests are required before claiming any external compatibility.

## Launch mechanisms

The following are the target mechanism contracts; the validation record distinguishes implemented behavior from remaining acceptance. Real-world sources establish the mechanism families; the models, limits, and resources are original game approximations.

| Mechanism | Player equipment | Distinct game behavior | First target |
| --- | --- | --- | --- |
| Friction tire drive | Shaft-powered tire modules and a matching chassis drive surface | Wheel surface speed follows RPM and radius. Traction, torque, and contact length bound acceleration; supports station transport, modest launches, reverse drive, and boosters. | 0.1 |
| Linear synchronous motor, LSM | Stator spans, magnetic reaction equipment, drive cabinet and energy buffer | Controlled target speed, direction, acceleration and jerk limits; single, rolling, multi-pass and swing launches. Requires its own vehicle interface. | 0.2 |
| Linear induction motor, LIM | Induction spans and conductive reaction plate | A distinct slip-dependent force curve with declared conversion losses. Shares placement/control plumbing with LSM, but has different tuning and hardware. | 0.2 |
| Flywheel cable launcher | Kinetic charging shaft, dedicated energy-storage flywheel, clutch, drum, cable guide and catch carriage | Charge takes time. Clutch engagement transfers finite energy to a physical catch; flywheel speed falls, the catch releases, and the carriage resets before reuse. | 0.3 |
| Hydraulic cable launcher | Shaft-driven pump, closed oil loop, accumulator, hydraulic drive drum and catch carriage | Accumulator charge limits available work and peak force. Launch requires stopped engagement; pressure falls during the pull and recovers between cycles. | 0.4 |
| Pneumatic launcher | Compressor, air receiver, valve, cylinder/cable mechanism and catch carriage | A distinct pressure/volume discharge curve and recharge cycle. Uses simulated compressed air; it is not an LSM with a different sound. | 0.4 |

[Intamin's family coaster page](https://www.intamin.com/product/family-launch-coaster/) confirms friction-wheel and LSM options; its [LSM page](https://www.intamin.com/product/lsm-launch-coaster/) confirms forward/backward and swing operation. [InTraSys](https://intrasys-gmbh.com/english/linear-drive-engineering/) documents both LIM and LSM drives. [S&S](https://www.s-s.com/ride-pages/air-launch-coaster) confirms compressed-air launching. A [Vekoma hydraulic winch patent](https://patents.google.com/patent/US7192010B2/en) and [flywheel launch patent](https://patents.google.com/patent/US7980181B2/en) support the stored-energy concepts, without establishing exact implementation for every manufacturer.

Use one validated catch-carriage subsystem across cable launchers, with separate energy sources and force curves. A high-speed catch launch must have a straight, bounded guide and verified release/reset space; arbitrary curved cable routing is deferred. Engage only at a declared low-speed capture window. The catch accelerates the real train through a constraint, releases before its end stop, then physically returns. Teleporting the train, forcing a prescribed path position, or substituting a single impulse does not satisfy the mechanism.

Chain lifts remain upstream equipment. A later weight-drop launcher or powered-coaster drive can reuse the interfaces, but neither blocks 1.0. The PAX lift in Fourth Dimension is a lift-and-release mechanism and keeps its own cycle.

## Braking mechanisms and operating modes

| Mechanism | Proposed behavior | Limit or interlock |
| --- | --- | --- |
| Friction fin clamp | Pads grip a chassis brake fin; configurable clamp force, response time, and conservative heat capacity | Provides a final stop and static hold within its rated load/slope. Proposed spring-applied, power-released variant defaults to applied. |
| Skid brake | Track shoes contact a verified underside brake surface | Alternative hardware for suitable ordinary cars; a wooden appearance alone does not make a cart compatible. |
| Passive magnetic eddy-current brake | Conductive fin passes permanent magnets; speed-dependent opposing force converts movement into heat | Force approaches zero at rest. Requires a friction clamp for a station stop/hold; provides no FE regeneration. |
| Active linear-motor brake | LIM/LSM equipment requests bounded deceleration | Depends on available powered drive behavior. Optional regeneration has a capped receiver; excess is dissipated or braking is derated as declared. |
| Drive-tire retardation | Powered tires reduce speed toward a lower wheel speed | Low-speed positioning aid. Loss of drive power does not by itself guarantee a parked train. |
| Water drag | Existing compatible scoop/provider can contribute drag | Prefer Engineered's existing scoop adapter; not required for standalone 1.0 and never treated as a static holding brake. |

The [InTraSys brake explanation](https://intrasys-gmbh.com/english/linear-eddy-current-brakes/principle-of-eddy-current-brakes/) establishes the opposing force and heat conversion. A [published patent's background](https://patents.google.com/patent/JP2016506223A/en) describes coaster fin and skid brakes. The proposed clamp construction and numerical curves must be verified in our prototypes.

Trim, service stop, station hold, rollback arrest, and emergency stop are controller **modes**, not additional physical brake types. Never let a passive magnetic span pin a train at zero speed. A brake can remove speed without reversing its direction; reversing requires a drive command. Retraction and clamp animations follow the server's physical state.

Introduce finite heat/cooling state by the later releases, with clear capacity warnings. Keep wear/replacement chores optional and defer detailed material thermodynamics. Provide conservative rated limits before advanced heat simulation exists.

## Player workflow and power

1. Build a normal or eligible family-specific coaster route and select matching vehicle interfaces.
2. Use an equipment tool to select a directed stretch between existing anchors. Preview contact surfaces, mechanism clearance, direction, launch/brake length, and incompatible equipment.
3. Place and bind a drive/brake cabinet nearby; cable systems additionally need a clear catch guide and reset bay.
4. Supply Create shaft power. Configure speed in blocks/second, acceleration in blocks/second squared, and jerk in blocks/second cubed; distances are blocks. Optional display units never imply a measured real-world scale.
5. Connect ready, occupied, held, released, low-energy, fault and emergency signals. Run an empty commissioning cycle, then progressively load the actual intended train.

Use Create kinetics as the standalone power path. Tire and pump speed follows shaft RPM; dynamic stress reflects requested load. Define one explicit game conversion between shaft work, internal stored energy, and Sable units. Create stress units are not real watts. A server-owned buffer/charging model prevents a high-RPM unloaded network from granting unlimited energy. A dedicated launcher flywheel stores energy; Create's decorative flywheel alone is not the storage implementation.

Optional FE can feed compatible cabinets through a documented server-configured conversion. Do not charge both FE and shaft work for the same operation or allow adapter transfer loops to create energy. Hydraulic oil circulates with return flow; compressed-air reserve lives in the receiver. Provide our own minimal resources so pneumatic or hydraulic addons are not mandatory.

Use native redstone and Redstone Links for basic operation, Engineer's Goggles and Display Links for diagnosis, and optional Jade for inspection. Expose recipes in JEI/EMI where installed. Prefer ordinary Create crafting/processing recipes; add a custom recipe category only if a new process needs one. Supply Ponder scenes for placement, clamp versus magnetic braking, charge/reset cycles, rollback, and 4D commissioning.

## Physics and track attachment architecture

Resolve the actual engaged edge and canonical path direction through the pinned Coasters interfaces. Cache arc-length tables and span indexes; never search the whole graph every render frame. Store span identity, ordered edge references, start/end distances, geometry revision, mechanism type, owner, and configuration. A visual style edit and a curve geometry edit have different invalidation rules.

Operate on Sable's physics timestep on the server. Native loading must settle before mass sampling; collect mechanism requests, resolve ownership, apply forces, then let native integration/constraints run. Event priority alone is insufficient unless ordering with the pinned mods is demonstrated. A client visual or ordinary 20 Hz detection callback cannot be the high-speed force authority.

Project velocity onto the local track tangent. Bound force by the mechanism's force curve, stored energy/power, target, direction, acceleration/jerk policy, attachment length, and vehicle/coupler rating. Deduplicate body and contact IDs so each physical force and passenger contribution is counted once. Estimate work from the applied motion, including gravity, losses, and rotational effects where relevant; reconcile stored energy against measured work rather than just desired exit speed.

Start with one declared numerical scale and calibrate it against native physics. The original Fourth Dimension baseline declared 100 kg per native mass unit and adds each rider as 0.8 native units on the carrier. Our adapter reads the resulting native mass; it does not add those passengers again.

Continuous entry/exit detection must catch traversal of a complete short span within one physics step. Count a pass using actual direction and the whole consist's tail clearing; oscillation around an endpoint must not duplicate it. Use collision/contact shapes independently of appearance so the equipment does not collide with riders or running gear unexpectedly.

## Fourth Dimension compatibility contract

Preserve ownership: Fourth Dimension controls seat profiles, rotor joints, carrier mass, restraints, boarding alignment, retention, family eligibility, and PAX lift/capture. Launch Systems controls its longitudinal equipment. Its current API-5 `VehicleMotionProvider` supplies chassis/body IDs, deduplicated consist membership, installed interface certificates, dispatch/motion vetoes, separate propulsion/brake limits and current exclusive operator. Native mass and accepted engagement are read from the referenced bodies and native guide observations. The legacy adapter retains its historical pinned schema-4 operations. The current adapter registers an explicit API 1 external owner and uses native epoch/sequence tokens for dispatch, capture, transfer and restored hold acknowledgement. It rejects unsupported variants and uncertified routes. The accepted 0.6 mode uses the bounded Arrow route authority and shared operator contract; native joint/seat behavior remains with FD. Occupied and separate-JVM lifecycle evidence is retained in the 0.1–0.6 integration records.

For the current schema-8/ride-authority migration, follow [the handoff](docs/FOURTH-DIMENSION-HANDOFF.md); do not interpret the old adapter as current compatibility.

Keep the neutral interface artifact with Launch Systems. Fourth Dimension can compile against that small API and register only when Launch Systems is present. Isolate optional classes and test either mod installed alone. Avoid a new mandatory shared runtime mod or circular dependence on both implementation JARs. If Fourth Dimension needs a public facade first, add it during the contract package rather than binding every adapter to private save structures.

The following table preserves initial design intent; the [current certificate table](docs/FD-VEHICLE-CERTIFICATES.md) owns implemented family eligibility.

| Fourth Dimension family | Initial planned launch/brake eligibility |
| --- | --- |
| Arrow controlled 4D | Candidate for tire, LIM/LSM and clamps after chassis interface/clearance certification. Cable launchers need a separately tested catch socket and loaded coupling limits. Rotation continues to follow position along matching Arrow track in either direction. |
| Legacy ZacSpin | Individual-car tire/linear launch and braking candidates. Free rotation must retain acceleration-induced dynamics; preserve its prohibition on train coupling. |
| Magnetic Free Spin | Same individual-car rules, with explicit separation between longitudinal propulsion and existing seat-spin inducers/dampers. No new train coupling. |
| Revised ZacSpin | Candidate only after its larger envelope and four independent carrier bodies pass commissioning. Preserve its matching track and spacing. |
| PAX shuttle | Observe cycle/readiness and faults initially. External propulsion is prohibited while the winch owns the train. Later final-brake assistance requires an explicit handoff controlled by the PAX state machine; no automatic relaunch on a normal return pass. |
| Fitting ordinary cart on 4D track | Eligible only where Fourth Dimension's existing gear/clearance contract permits access. Track access does not certify towing or a catch attachment. |

The historical schema-4 `VehicleOperations.brake` applies a bounded common translation reduction to chassis and carriers. Ordinary stopping, maintenance retention, boundary faults, and PAX capture can therefore already compete with external propulsion. A provider must expose these active states and refuse launch while any of them owns stopping. Closed restraints alone are insufficient: dispatch also requires the existing ready/captured state, valid family track, released retention, and fault-free route.

Prototype forces at the real chassis drive/brake interface and let native joints transmit acceleration. Simply giving every child the same translation increment can suppress the very inertia that makes free-spin cars rotate. Conversely, a large chassis-only correction can overload joints. Compare native point forces, supported motor constraints, and bounded recovery correction; choose the method from loaded stability and rotor-response evidence. Never overwrite carrier angular velocity or rotor angle to make a launch test pass. A supported approximation must state its limits, conserve the declared net impulse/work, and preserve required free-spin response.

Mechanical eligibility remains strict: a launcher does not turn ordinary track into Arrow/ZacSpin/Free Spin/PAX track. Verify force plates, fins, drive surfaces, catches and their swept clearance individually. Existing seat magnets cannot satisfy an LSM reaction interface merely because both use magnets. No locked-seat bypass, resized running gear, or automatic snap to nearby incompatible rails.

## Multiple providers and ride control

One authoritative motion coordinator resolves each train's requests per physics step. One active drive owns a vehicle contact; different cars may occupy separate spans, but their contributions must be physically and energetically aggregated once. Intentional passive brakes may coexist with a drive only under a declared arrangement. Emergency/vehicle-fault stopping vetoes propulsion; maintenance hold and active catch ownership take precedence over normal dispatch. Configuration overlap is an error, not whichever callback ran last.

Detect upstream chains, Engineered spans, Magnetized track, Additions force spans and our attachments. A validated adapter must either submit requests through the coordinator or provably suppress its own effect when another controller owns the contact. Detecting an addon is not enough to intercept it. If version-specific hooks cannot prevent double force, disable that combination on the affected span and explain it in the tool. Do not globally disable another addon or silently delete its equipment.

Provide a compact local controller for standalone arming, charge, hold, launch, release/reset, and fault handling. Support simple single launches and a bounded forward/backward/forward program for linear drives. Prefer Engineered's existing sequencing when an adapter is validated. A redstone pulse is a request, not permission to override a restraint, catch, or blocked route.

Local block sections reserve the downstream stopping region before release and remain occupied until every coupled car clears. Bidirectional shuttles require direction-aware reservations; a rollback retains occupancy. A stop/hold span alone is not a complete block system. Start with simple unbranched circuits and shuttles; defer arbitrary switches, transfer tables, drop tracks and park-wide scheduling.

Calculate a conservative stopping envelope from loaded mass, slope, brake response, available force, train length and usable span. Constant-deceleration formulas are an initial estimate only; validate speed-dependent magnetic braking with simulation and final clamp engagement. Insufficient brake distance or unavailable downstream chunks blocks arming, rather than guaranteeing a stop that the hardware cannot deliver.

FD's accepted 1.2 `RideLocalOperations` already reserves a bounded Arrow route.
Accepted LS601/LS602 consume its admission and current/next ownership through an
explicit contract while retaining LS stopping-distance/equipment guards. Other
route modes continue to deny overlapping control. Expose ownership,
charge, blocked reason and fresh-request status through existing operator tools;
do not introduce a second independent route scheduler. FD 1.3 body and palette
choices remain separate from force interfaces, loaded mass and save identity.

## Persistence and interruption behavior

Persist configuration, energy/pressure/flywheel state, mechanism phase, bindings, and block reservations with schema versions. Physics bodies remain Sable-owned. On restart, validate body/route/owner IDs and resume in an interrupted inspection state; do not simulate unattended launches or issue a saved redstone request again.

Removing or changing occupied equipment is rejected through normal editing. Unexpected block/support loss faults the controller and requests the vehicle's supported stop/recovery behavior. Missing ticking chunks are not equivalent to available physics; lose readiness when required bodies or route chunks are unavailable. Avoid unlimited forced chunk loading.

Power loss is mechanism-specific: passive magnets continue braking, a rated spring clamp applies, active motors cease drive, and catch systems enter a validated retained/fault state. A cable launcher needs its own drum holding brake and end stops; neither follows automatically from the PAX prototype. Recover only after inspecting engagement, route and remaining energy. Never restore a static hold by teleporting.

Optional integration removal must preserve our functional data and disable unresolved provider spans with an actionable fault. Removing another mod's own blocks still requires a world backup and explicit migration; no generic adapter can promise otherwise. Keep versioned provider IDs and clear unsupported-version messages.

## Release order and acceptance

Ship a coherent small release first: tire transport/launch, friction clamp, passive magnetic trim, local control, working recipes/Ponder, and a loaded Arrow/free-spin adapter demonstration. Follow with distinct LIM/LSM drives and swing control, then flywheel, then hydraulic/pneumatic launchers. Keep optional compatibility work separate from standalone release gates.

Validation must include ordinary carts, maximum declared consists, empty and unevenly occupied 4D carriers, reverse traversal, high-speed short spans, slopes, rollback, catch misalignment, wrong interfaces, wrong track families, restraint/retention vetoes, multi-provider overlap, depleted stores, active-brake power loss, server restart, unload/reload, late join, and equipment removal. Use unit tests for force/energy/state mathematics, native fixtures or GameTests for graph/ownership/lifecycle behavior, and packaged multiplayer runs for actual physics and rider motion.

Record launch time/distance/speed, impulse/work budget, braking distance and creep, free rotor response, constraint error, coupling retention, and server/frame costs. Establish numerical tolerances from baseline fixtures before calling any gate complete. Every advertised mechanism requires an occupied gameplay demonstration and packaged dedicated-server evidence; a rendered model or command-applied velocity is insufficient.

Current-core qualification, authority migration, 0.5 family certificates and 0.6 bounded route interoperability are complete. The accepted 0.9 outcome reconciles the finite support matrix, provider lifecycle and occupied demonstrations, measured callback costs and packaged instructions. Optional adapters and 1.0 publication are separate assignments.
