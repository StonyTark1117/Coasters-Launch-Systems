# Launch Systems research evidence

Research first reviewed September 30, 2026; the published-addon inventory was refreshed October 1, 2026. Distinguish published features, inspected implementation, and proposed compatibility. The original research preceded implementation; subsequent Launch Systems code and native fixture evidence are tracked in [validation status](../docs/VALIDATION.md).

## Local project evidence

The sibling [Fourth Dimension README](../../Coasters:%20Fourth%20Dimension/README.md), `build.gradle`, `gradle.properties`, `PLAN.md`, `ROADMAP.md`, and `docs/REVISED-SHUTTLE.md` establish its development stack and family restrictions. The source currently uses rotary Sable constraints for independent carriers, native Coasters consist linking, and family-specific PAX winch ownership. Its documentation separates bounded accepted builds from the unfinished `0.9.0-dev.1` lifecycle gates.

Inspected source paths under `../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/`:

- `contract/VehicleContract.java` and `contract/TrackContract.java`: family/variant, running gear, clearance and mechanism eligibility; appearance cannot grant eligibility.
- `runtime/CarrierRuntime.java`: pre-physics callbacks, rotary constraints, loading update, stopping and maintenance calls.
- `vehicle/NativeLoading.java`: actual passengers/ballast contribute to carrier mass; native scale declaration is 100 kg/unit and 0.8 units/rider.
- `vehicle/VehicleOperations.java`: readiness/restraint/retention validation, PAX delegation, and common translation reduction across chassis/carriers when stopping.
- `shuttle/ShuttleRuntime.java` and `docs/REVISED-SHUTTLE.md`: catch/lift/release ownership and interrupted operation requirements, consulted through project documentation and source references.

The sibling `research/dependency-audit.json` contains earlier exact release coordinates, hashes and signatures. It is inherited evidence, not a fresh compatibility result. Its Track Styles review covers 1.1.0-hotfix1, which is also the current CurseForge 1.21.1 release checked in this refresh. Its Immersion review covers 0.2.3.

### Recovery of the exact pinned artifact, October 1, 2026

After temporary validation files were cleared, the schema-4 JAR was recovered from [Fourth Dimension CI run 36775040079](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/actions/runs/36775040079), artifact `validation-evidence` (ID `11124877451`). The downloaded 421806-byte JAR exactly matches historical SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`. The successful run identifies upstream source commit `a881e3d34415edd691baa166668f4f97d3a4a7e8`; the pin's source identity is now verified. Its declared version remains `0.9.0-dev.1`. A durable validation-cache copy is retained outside the upstream checkout. No active upstream checkout or process was changed.

### Pinned station operation inspection, October 1, 2026

Inspected `javap -c dev.fourthdimension.vehicle.VehicleOperations` and the public `Boarding` signatures in the copied schema-4 JAR, SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`. This artifact is isolated from the active sibling checkout and is the same pinned dependency used in the combined native tests.

`capture(ServerLevel, FoundationData.Vehicle)` first checks the upstream shuttle owner. A winch outside `BOARDING` is faulted for emergency inspection and the operation returns. Otherwise it resolves the consist, sets each vehicle's `stopping`, sets its carriers' capture flags, increments revisions and marks saved data dirty. `Boarding.ready` is a separate public query; calling capture alone is not readiness evidence. The existing dispatch adapter already delegates to `VehicleOperations.dispatch` after reservation. Dev.10 native circuits explicitly invoke this public capture operation after whole-train hold, wait for readiness, perform actual exit/reboarding and complete a fresh dispatch. Dev.11 adds optional provider/controller capture delegation after native whole-train hold, with separate measured readiness and PAX continuing to veto Launch Systems ownership.

The upstream stopping operation also owns common translation braking. A station adapter must request capture only after our complete train is physically held, retain its section lease and verify upstream readiness before boarding. It must not treat capture as an extra launch/brake-force grant or write carrier angles. The dev.10 fixtures supply scoped native station-cycle evidence through these public operations. Dev.11 removes the manual fixture capture call and exercises controller-owned requests through the adapter. Rendered/multiplayer station acceptance remains open.

## Engineered release inspection

Reinspected the existing local Engineered 1.1.1 artifact using ZIP metadata and `javap` signatures/bytecode; no mod was executed. Distribution reference: [CurseForge file 8863559](https://www.curseforge.com/minecraft/mc-mods/coasters-engineered/files/8863559).

- Mod ID `coastersengineered`; metadata license MIT.
- SHA-256 `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d`, matching the sibling audit.
- `MotorForce.mass` sums `CartAssembly.bodies`; `CartAssembly.index` discovers related bodies through rivet host identities, and `bodies` walks that index. This does not establish enumeration of Fourth Dimension's independently created rotary children.
- `MotorForce.apply` queues a propulsion point force on the passed body's center of mass. This is force-based operation, not evidence that every linked rotor's mass and ownership are handled.
- `MotorDrive.apply`, `TrackSpanGeometry`, `TrackOccupancy`, and controller signatures expose candidate seams; public signatures do not promise a stable third-party extension API.

Inference: articulated loaded vehicles need a provider/assembly prototype and ownership adapter. It would be premature to claim Engineered is incompatible, or that its existing assembly helper already handles our 4D bodies.

## Published addon references

| Source | What it supports |
| --- | --- |
| [Coasters Simulated](https://modrinth.com/mod/create-coasters-simulated) | Track/chain/vehicle physics and planned hydraulic/station scope; Aeronautics is not required by the base coaster mod. |
| [Engineered](https://www.curseforge.com/minecraft/mc-mods/coasters-engineered) | 1.1.1 NeoForge 1.21.1 release advertises linear motors, controller sequencing/rollback, observer, splashdown and optional FE. The separately inspected 1.1.1 JAR supplies no stable third-party extension contract. |
| [Magnetized](https://modrinth.com/mod/coasters-magnetized) | 1.21.1 NeoForge accelerating and braking tracks; current Modrinth page is MIT. No artifact/API inspection or adapter is complete. |
| [Additions](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions) | 0.2.0 beta NeoForge 1.21.1 release adds station, seats/restraints, sensors, force spans, loose chains and Ponder; it also has optional automatic dispatch. No implementation inspection in this project. |
| [Track Styles](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles) | Current 1.21.1 release is 1.1.0-hotfix1, not 1.2.0. It provides styles/models, an in-game style tool and mid-spline transitions; no new JAR inspection. |
| [Tracks and Balloons](https://www.curseforge.com/minecraft/mc-mods/ctnb) | Current 1.2 NeoForge 1.21.1 release has boost, launch, brake, reverse, station and sensor track alongside decorative styles. It uses a custom license and has not been downloaded or inspected here. |
| [Immersion](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion) | Advertised camera/launch presentation; inherited artifact caveats remain in the sibling audit. |

A similarly named “Magnetic Velocity Rail” appeared on a third-party repost site. No authoritative project/artifact identity was established during this research; do not count it as a separate verified dependency or rely on its download.

## Ecosystem refresh and coexistence policy, October 1, 2026

This refresh confirms that several current NeoForge 1.21.1 addons already operate a coaster's translation or station flow. Launch Systems remains a standalone core. Presence of another addon is never consent to touch its spans, energy stores, vehicle state or station dispatch.

- **Engineered 1.1.1:** its published motor/controller/FE surface overlaps linear propulsion. The local bytecode inspection above is still the only implementation evidence: no stable extension API or 4D assembly proof was found. Keep both systems disjoint until a pinned artifact exposes an auditable ownership seam; an eventual adapter must choose one force and energy owner for a given span.
- **Magnetized:** accelerating and braking tracks directly overlap propulsion and braking. Its MIT project page establishes distribution terms, but not a usable API. Support stays disabled until an exact artifact and loaded 4D test establish exclusive span ownership.
- **Additions 0.2.0:** its station, restraint and signal facilities can complement Launch Systems, but its Track Control force spans and automatic dispatch overlap core motion/controller duties. A future adapter may translate an external dispatch signal into a fresh Launch Systems request only after reservation and Fourth Dimension readiness; it must never bypass either gate or add force to an owned span.
- **Track Styles 1.1.0-hotfix1:** this is a cosmetic/geometry candidate. Acceptance must prove that a styled or transitioned spline still presents the same validated contact and clearance; style identity cannot grant Arrow or other 4D eligibility. Its Flywheel-dependent rendering remains a separate client concern.
- **Tracks and Balloons 1.2:** its functional launch/brake/station/reverse tracks duplicate much of the core control surface. Because its release declares a custom license and no artifact/API has been inspected, Launch Systems does not depend on, invoke or mirror it. A future coexistence audit must first establish terms and a public integration seam, then prove no double force or competing station ownership.
- **Immersion:** presentation may eventually observe an accepted Launch Systems event, but it receives no camera, transform, force or controller authority.

The Fourth Dimension adapter keeps its existing rule: the provider owns carrier rotation, mass, restraints, retention, and any PAX winch state. Launch Systems owns only an accepted translation operation after the provider's readiness and exclusive-ownership checks. All third-party adapters remain absent by default and must be independently safe when the companion mod is missing.

## Real mechanism references

Use the following primary sources for mechanism concepts. Detailed force curves, catches, dimensions, thermal ratings and 4D compatibility in our plan are original proposed game behavior.

| Source | Supported concept |
| --- | --- |
| [Intamin Family Launch Coaster](https://www.intamin.com/product/family-launch-coaster/) | Friction-wheel or LSM propulsion. |
| [Intamin LSM Launch Coaster](https://www.intamin.com/product/lsm-launch-coaster/) | Forward/backward, multiple and swing launches. Its mixed installation list must not be used to classify every listed historical ride as LSM. |
| [InTraSys linear drives](https://intrasys-gmbh.com/english/linear-drive-engineering/) and [motor principle](https://intrasys-gmbh.com/english/linear-drive-engineering/motors-principle/) | LIM and LSM are distinct contactless linear-drive families. |
| [InTraSys eddy-current principle](https://intrasys-gmbh.com/english/linear-eddy-current-brakes/principle-of-eddy-current-brakes/) | Moving conductive fin, opposing magnetic force and heat conversion. Zero-speed hold is excluded by our motion-dependent model. |
| [S&S Air Launch Coaster](https://www.s-s.com/ride-pages/air-launch-coaster) | Compressed-air launch technology. Page tower-ride content is not used as coaster acceleration data. |
| [Vekoma hydraulic winch patent US7192010B2](https://patents.google.com/patent/US7192010B2/en) | Pump, accumulator, hydraulic motor and winch concept; not an Intamin-specific circuit specification. |
| [Flywheel patent US7980181B2](https://patents.google.com/patent/US7980181B2/en) | Stored flywheel energy and selective transfer for ride launching; not proof of a specific historic shuttle mechanism. |
| [Patent JP2016506223A](https://patents.google.com/patent/JP2016506223A/en) | Background describes friction fin and skid braking; not a brake calibration dataset. |
| [NeoForge documentation](https://docs.neoforged.net/) | Official loader reference consulted; use the appropriate 1.21.1 documentation during implementation rather than assuming latest APIs match. |

The initial research added no third-party code, binaries, models or sounds. The implementation independently authors mechanism logic and equipment geometry, references installed vanilla textures, and resolves declared dependencies through Gradle. Dependency provenance and terms are recorded in [THIRD_PARTY.md](../THIRD_PARTY.md).

## Original linear model checkpoint, October 1, 2026

The LSM/LIM family distinction remains grounded in the primary [InTraSys motor principle](https://intrasys-gmbh.com/english/linear-drive-engineering/motors-principle/) reference. The [Intamin LSM launch page](https://www.intamin.com/product/lsm-launch-coaster/) and [Engineered project page](https://modrinth.com/mod/coasters-engineered) were rechecked during the original hardware implementation. The latter still describes track-fitted linear motors and optional FE; this does not establish a tested ownership adapter. No external motor implementation/assets were copied or enabled.

Source `23942385c2cf88aaa03ba44f0b40db96eab714d3` independently adds LSM/LIM chassis plates, cabinets, kinetic charging, bounded response, per-interface impulse/cross-work, buffer spending, heat and local operation. The selected response/efficiency/resistance numbers are declared game models rather than values extracted from a manufacturer. Fourth Dimension integration continues to use the unchanged isolated schema-4 artifact documented above; its public vehicle operations retain restraint, capture, rotor and exclusive-winching ownership. The active upstream checkout/processes were untouched. Exact native scope and limitations are in [linear equipment](../docs/LINEAR-MOTORS.md) and [validation](../docs/VALIDATION.md).

## Native cable inspection, October 1, 2026

The dev.14 prototype uses the installed Coasters 0.1.5 and Sable 2.0.3 artifacts, their public signatures and Sable's published source JARs. Inspection establishes sublevel allocation, embedded block installation, native generic joint limits/motors and removal, queued point impulses, native mass/inertia updates and the accepted bearing frame. Coasters' accepted bearing centre is sampled rail point plus track-up `0.3125`; its plot-origin placement helper instead adds a rotated half-block offset. That distinction caused an initial catch alignment failure and is preserved in the diagnostic evidence.

Sable plot bounds require physical geometry: an initial carriage block with no collision could leave an invalid/removed body. Original lower-jaw geometry and a connected, lowered steel spine/ballast now provide native bounds/mass with socket clearance. Accepted-contact cache entries are physics-phase observations for the current world tick. Engagement and dispatch are tested in that phase; a GameTest callback cannot substitute a stale cached hit. Fixture removal is deferred outside that observation pass so another case's native contact is not invalidated during the pass.

The installed Sable joint getter reports zero for the catch motor even during a successful physical pull. We retain it as diagnostic output and measure whole-body train momentum change separately. Neither value is advertised as a complete substep drum-load measurement. Original declared game-scale flywheel/work/rating models and scope are in [cable mechanics](../docs/CABLE-MECHANICS.md). This inspection adds no upstream source/assets to the distribution and enables no third-party launcher adapter. The active Fourth Dimension checkout/processes remain untouched; compatibility uses the immutable schema-4 pin documented above.

The primary [Coasters project listing](https://modrinth.com/mod/create-coasters-simulated) was rechecked during this checkpoint and still places hydraulic launches under planned features. [Engineered's listing](https://modrinth.com/mod/coasters-engineered) continues to advertise physical linear motors, sequencing/rollback controls and optional FE. This is a project-description recheck, not a new artifact audit or proof that another addon is safe to overlap with this prototype. Optional adapters remain disabled until exact implementation/ownership and loaded native behavior are verified.
