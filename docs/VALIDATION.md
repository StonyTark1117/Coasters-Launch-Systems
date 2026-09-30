# Launch Systems validation status

Updated September 30, 2026. The goal is a complete 0.4.0 release validated and pushed to this addon's own private GitHub repository. Foundation checkpoints do not satisfy that release goal.

| Requirement | Current state |
| --- | --- |
| Pinned standalone project and build | Build and native test-server boot passed; packaged client/server checks remain |
| Force, brake, heat and finite energy math | 12 unit tests passed; gameplay integration remains |
| Native point-force launch and friction stop | Native fixture passed; translation and retained engagement verified |
| Versioned provider contract | Defined; ordinary and Fourth Dimension implementations pending |
| Tire modules and passive/friction brakes | Pending |
| Local control, signals, conflict ownership and occupancy | Pending |
| Original LIM and LSM equipment and swing launches | Pending |
| Skid brakes and gameplay heat limits | Pending |
| Physical guide, catch, release, reset and flywheel launch | Pending |
| Hydraulic/pneumatic gameplay equipment | Pending |
| Loaded ordinary and all eligible 4D mechanisms | Pending |
| Recipes, models, tutorials and Ponder scenes | Pending |
| Packaged client, dedicated server, multiplayer and interrupted saves | Pending |
| 0.4.0 artifact, commit and verified private remote push | Pending |

The native force fixture does not prescribe cart velocity or teleport a train. It creates a near-origin Coasters track/cart, queues timestep-scaled forces on the real chassis, and verifies translation, retained engagement and final stop. It is foundation evidence only, not an occupied gameplay or articulated-vehicle acceptance test.

Source references and inherited addon audit evidence remain in [research/EVIDENCE.md](../research/EVIDENCE.md). New release evidence will identify the exact source commit, built JAR checksum, test scope and limitations.

## Foundation checkpoint

`./gradlew build runGameTestServer --no-daemon --max-workers=2` passed on September 30, 2026 with Java 21: 12 unit tests and one required native GameTest. The native cart reached 3.991 blocks/second after two simulated seconds of timestep-scaled chassis force, moved 4.050 blocks and stopped at zero longitudinal speed under friction braking, retaining native engagement. No velocity-setting command drove this fixture.

Evidence: [unit results](evidence/foundation-unit.txt), [native/build excerpt](evidence/foundation-native.txt), and [JAR checksum](evidence/foundation-artifact.sha256). This does not prove articulated loading, occupied gameplay, packaged clients, or any 0.4.0 release gate.
