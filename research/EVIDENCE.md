# Launch Systems research evidence

Reviewed September 30, 2026. Distinguish published features, inspected implementation, and proposed compatibility. No Launch Systems code or runtime fixtures exist yet.

## Local project evidence

The sibling [Fourth Dimension README](../../Coasters:%20Fourth%20Dimension/README.md), `build.gradle`, `gradle.properties`, `PLAN.md`, `ROADMAP.md`, and `docs/REVISED-SHUTTLE.md` establish its development stack and family restrictions. The source currently uses rotary Sable constraints for independent carriers, native Coasters consist linking, and family-specific PAX winch ownership. Its documentation separates bounded accepted builds from the unfinished `0.9.0-dev.1` lifecycle gates.

Inspected source paths under `../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/`:

- `contract/VehicleContract.java` and `contract/TrackContract.java`: family/variant, running gear, clearance and mechanism eligibility; appearance cannot grant eligibility.
- `runtime/CarrierRuntime.java`: pre-physics callbacks, rotary constraints, loading update, stopping and maintenance calls.
- `vehicle/NativeLoading.java`: actual passengers/ballast contribute to carrier mass; native scale declaration is 100 kg/unit and 0.8 units/rider.
- `vehicle/VehicleOperations.java`: readiness/restraint/retention validation, PAX delegation, and common translation reduction across chassis/carriers when stopping.
- `shuttle/ShuttleRuntime.java` and `docs/REVISED-SHUTTLE.md`: catch/lift/release ownership and interrupted operation requirements, consulted through project documentation and source references.

The sibling `research/dependency-audit.json` contains earlier exact release coordinates, hashes and signatures. It is inherited evidence, not a fresh compatibility result. Its Track Styles review covers 1.1.0-hotfix1; the current project page now advertises 1.2.0. Its Immersion review covers 0.2.3.

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

No third-party code, binaries, models or sounds were added to this workspace. Implementation should independently author mechanisms and record licenses for any permitted code reuse separately from installed-mod compatibility.
