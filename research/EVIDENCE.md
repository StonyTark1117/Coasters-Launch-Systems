# Launch Systems research evidence

Research reviewed September 30, 2026. Distinguish published features, inspected implementation, and proposed compatibility. The original research preceded implementation; subsequent Launch Systems code and native fixture evidence are tracked in [validation status](../docs/VALIDATION.md).

## Local project evidence

The sibling [Fourth Dimension README](../../Coasters:%20Fourth%20Dimension/README.md), `build.gradle`, `gradle.properties`, `PLAN.md`, `ROADMAP.md`, and `docs/REVISED-SHUTTLE.md` establish its development stack and family restrictions. The source currently uses rotary Sable constraints for independent carriers, native Coasters consist linking, and family-specific PAX winch ownership. Its documentation separates bounded accepted builds from the unfinished `0.9.0-dev.1` lifecycle gates.

Inspected source paths under `../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/`:

- `contract/VehicleContract.java` and `contract/TrackContract.java`: family/variant, running gear, clearance and mechanism eligibility; appearance cannot grant eligibility.
- `runtime/CarrierRuntime.java`: pre-physics callbacks, rotary constraints, loading update, stopping and maintenance calls.
- `vehicle/NativeLoading.java`: actual passengers/ballast contribute to carrier mass; native scale declaration is 100 kg/unit and 0.8 units/rider.
- `vehicle/VehicleOperations.java`: readiness/restraint/retention validation, PAX delegation, and common translation reduction across chassis/carriers when stopping.
- `shuttle/ShuttleRuntime.java` and `docs/REVISED-SHUTTLE.md`: catch/lift/release ownership and interrupted operation requirements, consulted through project documentation and source references.

The sibling `research/dependency-audit.json` contains earlier exact release coordinates, hashes and signatures. It is inherited evidence, not a fresh compatibility result. Its Track Styles review covers 1.1.0-hotfix1; the current project page now advertises 1.2.0. Its Immersion review covers 0.2.3.

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
| [Engineered](https://www.curseforge.com/minecraft/mc-mods/coasters-engineered) | Existing generic motor and operating features summarized in PLAN.md; version listed is 1.1.1. |
| [Magnetized](https://modrinth.com/mod/coasters-magnetized) | Published accelerating/braking tracks. Force model and current adapter remain unverified. |
| [Additions](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions) | Published station/seat/signal/force/chain behavior and dependency list, with 0.2.0 advertised. No implementation inspection this turn. |
| [Track Styles](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles) | Current 1.2.0 listing, appearance transitions and future Arrow 4D. No new JAR inspection. |
| [Immersion](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion) | Advertised camera/launch presentation; inherited artifact caveats remain in the sibling audit. |

A similarly named “Magnetic Velocity Rail” appeared on a third-party repost site. No authoritative project/artifact identity was established during this research; do not count it as a separate verified dependency or rely on its download.

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
