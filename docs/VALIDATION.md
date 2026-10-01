# Launch Systems validation status

Updated September 30, 2026. The goal is a complete 0.4.0 release validated and pushed to this addon's own private GitHub repository. Foundation checkpoints do not satisfy that release goal.

| Requirement | Current state |
| --- | --- |
| Pinned standalone project and build | Build and packaged native test-server passed; rendered client and normal dedicated-server gameplay checks remain |
| Force, brake, heat, energy and distance math | 18 unit tests passed, including tire demand/traction-envelope regression; further gameplay integration remains |
| Native point-force launch and friction stop | Forward/reverse ordinary and single-rider 4D fixtures passed; native contact and joint separation verified |
| Versioned provider contract | Ordinary fallback and read-only schema-4 Fourth Dimension adapter registered; pinned tire interfaces certified; stable facade and lifecycle validation remain |
| Directed contact and span occupancy | Actual span-tool placement, persisted two-block binding, level/shaft checks and support invalidation tested; obstruction envelopes, adjacent edges and train reservations pending |
| Tire modules and passive/friction brakes | Ordinary and four pinned 4D tire variants passed loaded bidirectional fixtures, including occupied linked Arrow; ordinary fin clamp stops/holds passed through mass 64; complete tire/brake gates and magnetic brakes pending |
| Local control, signals, conflict ownership and occupancy | Pending |
| Original LIM and LSM equipment and swing launches | Pending |
| Skid brakes and gameplay heat limits | Pending |
| Physical guide, catch, release, reset and flywheel launch | Pending |
| Hydraulic/pneumatic gameplay equipment | Pending |
| Loaded ordinary and all eligible 4D mechanisms | Full/uneven 4D tire loads and fully occupied native linked Arrow pairs passed; ordinary passengers, profile traversal and other equipment mechanisms pending |
| Recipes, models, tutorials and Ponder scenes | Original cabinet/drive-strip models and five recipes added; rendered QA, complete mechanisms, tutorials and Ponder remain |
| Packaged client, dedicated server, multiplayer and interrupted saves | Pending |
| 0.4.0 artifact, commit and verified private remote push | Pending |

The native force fixture does not prescribe cart velocity or teleport a train. It creates a near-origin Coasters track/cart, queues timestep-scaled forces on the real chassis, and verifies translation, retained engagement and final stop. It is foundation evidence only, not an occupied gameplay or articulated-vehicle acceptance test.

Source references and inherited addon audit evidence remain in [research/EVIDENCE.md](../research/EVIDENCE.md). New release evidence will identify the exact source commit, built JAR checksum, test scope and limitations.

## Foundation checkpoint

`./gradlew build runGameTestServer --no-daemon --max-workers=2` passed on September 30, 2026 with Java 21: 12 unit tests and one required native GameTest. The native cart reached 3.991 blocks/second after two simulated seconds of timestep-scaled chassis force, moved 4.050 blocks and stopped at zero longitudinal speed under friction braking, retaining native engagement. No velocity-setting command drove this fixture.

Evidence: [unit results](evidence/foundation-unit.txt), [native/build excerpt](evidence/foundation-native.txt), and [JAR checksum](evidence/foundation-artifact.sha256). This does not prove articulated loading, occupied gameplay, packaged clients, or any 0.4.0 release gate.

## Contact/provider checkpoint: 0.0.1-dev.2

Seventeen unit tests passed, followed by the native ordinary fixture without Fourth Dimension. The packaged combined installation loaded the Launch Systems JAR and the copied Fourth Dimension JAR and passed all six required native tests. Four articulated families each carried one seated passenger, gained exactly 0.8 native mass, launched and braked in both directions, retained the rider, and preserved native joints. Native forces stayed within the provider's acceleration/force limits. Maintenance suppressed the drive, and PAX retained exclusive ownership.

The packaged loaded results recorded maximum joint separations of 0.000173 blocks (Arrow), 0.000214 (legacy), 0.000137 (Free Spin), and 0.000130 (revised). The three free families retained different carrier angles under asymmetric loading. These are straight-rail chassis-force prototypes, not completed tire/linear/catch equipment, full loads, controlled Arrow profile traversal, or multiplayer acceptance.

See [provider/contact scope and reproduction](FOUNDATION-PROVIDERS.md), [unit results](evidence/contact-unit.txt), [standalone native results](evidence/contact-standalone.txt), [packaged standalone results](evidence/contact-packaged-standalone.txt), [packaged combined results](evidence/contact-packaged-combined.txt), and [both artifact checksums](evidence/contact-artifacts.sha256). The adapter was tested against the copied artifact identified there; the same development version string alone does not prove an upstream rebuild matches it. No Fourth Dimension checkout or process was changed.

## Equipment checkpoint: 0.0.1-dev.3

The actual shaft-powered cabinet moved native ordinary chassis in both directions through an installed tire drive strip. The empty equipment fixture had native mass 2; six iron ballast blocks raised native mass to 26. Negative shaft RPM and reversed anchor selection each reversed motion. Missing drive strips and overlapping owners faulted without propelling the cart. The placement fixture used the real item interaction, verified Create RPM/stress and recipe loading, reconstructed persisted module identity, and invalidated removed support.

This is partial LS005/LS101 progress. At this dev.3 checkpoint, complete release gates still required Fourth Dimension tire certification, linked/occupied equipment, rolling entry, obstructed envelopes, controllers, brakes and interruption/multiplayer tests. Dev.4 supplies the scoped 4D tire evidence below. Fin and magnetic cabinets currently reject operation. See [equipment scope and reproduction](EQUIPMENT-PROTOTYPE.md).

Implementation commit `c2d192390c24f199b19ce6b40b7a96b42ccdce73` was pushed to the private remote. [Its CI run](https://github.com/StonyTark1117/Coasters-Launch-Systems/actions/runs/36804981215) passed a clean build and all eight packaged standalone native tests; the downloaded JAR hash matched the locally validated artifact. Eighteen unit tests and all thirteen packaged combined native fixtures passed locally. The full 0.4.0 goal remains active.

## Fourth Dimension tire checkpoint: 0.0.1-dev.4

Eighteen unit tests, eight packaged standalone native tests and thirty-two packaged combined native tests passed. Nineteen new combined fixtures use actual cabinet forces on installed chassis strips: full/uneven loads in both directions for four pinned variants, powered PAX rejection, and fully occupied native six-block Arrow pairs in both directions. Passenger mass, carrier/coupler joints, independent rotor response, work budgets and authoritative vehicle vetoes are measured. A removed carrier prevents propulsion throughout the consist without falling through to the ordinary provider.

See [scope, measured results and reproduction](FOURTH-DIMENSION-TIRES.md), [unit evidence](evidence/fourthdimension-tire-unit.txt), [packaged standalone](evidence/fourthdimension-tire-packaged-standalone.txt), [packaged combined](evidence/fourthdimension-tire-packaged-combined.txt), and [artifact checksums](evidence/fourthdimension-tire-artifacts.sha256). This remains partial LS003/LS004/LS101 progress. Complete release gates, other brakes/launch mechanisms, ordinary passengers, profile traversal, client/multiplayer and lifecycle evidence remain open.

Implementation commit `929ca5981de96986501e5d925de7e12dfa4a8ab8` is pushed. [Its CI run](https://github.com/StonyTark1117/Coasters-Launch-Systems/actions/runs/36808157969) passed the clean build and eight packaged standalone native tests; the downloaded JAR matched the local artifact exactly. [CI evidence](evidence/fourthdimension-tire-ci.txt) records that verification. The 0.4.0 release goal remains active.

## Friction fin checkpoint: 0.0.1-dev.5

Eighteen unit checks, eighteen packaged standalone native tests and forty-two packaged combined native tests passed with the same Launch Systems JAR. Four new native constraint proofs establish loaded forward/reverse stops, bounded overload slipping, slope hold and release. Six actual cabinet fixtures establish an installed fin certificate, rolling stops in both directions, a mass-64 chassis holding a 1-in-4 slope for ten seconds, shaft release/power-loss reapplication, physical fin removal and rejection of missing hardware/excessive mass/competing owners.

See [ratings, scope and contact-length limitations](FIN-CLAMPS.md), [unit evidence](evidence/fin-unit.txt), [packaged standalone](evidence/fin-packaged-standalone.txt), [packaged combined](evidence/fin-packaged-combined.txt), and [artifact checksums](evidence/fin-artifacts.sha256). The API now carries explicit braking authority; Fourth Dimension remains tire-certified and brake-vetoed pending its own loaded fin fixtures. This is partial LS102 progress. Complete stopping envelopes, controller/retention interlocks, occupied/linked brakes, rendered geometry/animation, active restart/chunk/multiplayer tests and remaining 0.4.0 mechanisms are open.
