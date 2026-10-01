# Coasters: Launch Systems

A standalone Create: Coasters Simulated addon in development toward **0.4.0**. The planned release includes tire, LIM, LSM, flywheel, hydraulic and pneumatic launches, distinct friction/skid/magnetic brakes, local ride control and explicit compatibility with Fourth Dimension vehicles.

The current `0.0.1-dev.5` checkpoint adds an ordinary-cart friction fin clamp with bounded native stopping/holding force and a separate installed brake fin. It retains the dev.4 tire certification for four pinned Fourth Dimension variants with full/uneven loads and an occupied native coupled Arrow pair. The requested 0.4.0 release and its acceptance gates remain in progress. See [release roadmap](ROADMAP.md), [design](PLAN.md), [fin clamp checkpoint](docs/FIN-CLAMPS.md), [4D tire checkpoint](docs/FOURTH-DIMENSION-TIRES.md), and [validation status](docs/VALIDATION.md).

## Development stack

Minecraft 1.21.1, Java 21, NeoForge 21.1.248, Create 6.0.10-280, Coasters Simulated 0.1.5, Sable 2.0.3 and Flywheel 1.0.6. Gradle resolves Ponder, Registrate, Sable Companion and Veil alongside them. Optional coaster addons are not required.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runClient
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runServer
```

Unix project paths containing `:` redirect build outputs to `/tmp/coasterslaunchsystems-<path-hash>/build`. Use the actual Gradle output location for the JAR and test reports. GameTests use a disposable world; `runGameTestServer` recreates that world. Normal development runs are separate.

`-PlsPackaged` loads the built binary from an isolated source set and separate packaged run directories. `-PlsConnect=127.0.0.1:25565` connects a client, and `runObserver` starts a second client identity. `-PlsWorld=FolderName` selects a saved single-player world.

## Architecture

Mechanism math uses blocks, seconds and native Sable mass/work units. Energy stores are server-owned; hydraulic and air resources use original game-scale pressure models. No real-world performance or manufacturer replica fidelity is claimed.

`VehicleMotionProvider` API 2 describes body and consist identity, verified interfaces, operator ownership, dispatch vetoes and explicit braking permission. The acceleration limit bounds propulsion; brakes use the provider force limit and their declared mechanism ratings. Legacy motion snapshots grant no braking authority. Forces act at native chassis interfaces; child rotor angles and angular velocities remain owned by their native joints. Registering an API provider does not itself establish validated Fourth Dimension compatibility.

Source and original assets are MIT licensed. [Dependency provenance](THIRD_PARTY.md) records third-party tooling and library use.
