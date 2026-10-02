# Coasters: Launch Systems

A standalone Create: Coasters Simulated addon in development toward **0.4.0**. The planned release includes tire, LIM, LSM, flywheel, hydraulic and pneumatic launches, distinct friction/skid/magnetic brakes, local ride control and explicit compatibility with Fourth Dimension vehicles.

The current `0.0.1-dev.64` checkpoint contains independent tire, LSM, LIM, flywheel, hydraulic and pneumatic launches; friction-fin, skid and eddy-current brakes; a local controller; a versioned Fourth Dimension provider adapter; and a fail-closed Coasters: Engineered 1.1.1 active-span bridge. It includes 21 original recipes and four standalone Ponder guides covering controller/span binding, linear-motor hardware, brake interfaces and cable launches. The latest Engineered-present packaged matrix passed 132 required GameTests, including a real active-controller contact predicate; the standalone and combined release matrices are being repeated for this checkpoint. The latest focused checkpoint verifies inspection release after restart for ordinary flywheel, hydraulic and pneumatic catches.

This is still an in-development build, not a 0.4.0 release. Rendered Ponder and survival ride walkthroughs, a real packaged multiplayer/late-join session, and an active vehicle whose chunk becomes unavailable remain release gates. The adapter preserves Fourth Dimension ownership of carrier rotation, mass, restraints, retention and PAX winch state; Launch Systems only owns an accepted translation operation. See [cable scope](docs/CABLE-MECHANICS.md), [linear equipment and limits](docs/LINEAR-MOTORS.md), the [optional integration audit](docs/OPTIONAL-INTEGRATIONS.md), [release roadmap](ROADMAP.md), and [validation status](docs/VALIDATION.md).

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

`VehicleMotionProvider` API 5 describes body and consist identity, verified interfaces, operator ownership, dispatch vetoes, explicit braking permission and an independent brake-force rating. Its dispatch and capture requests default to denial; the ordinary provider grants its own cart permission and the pinned Fourth Dimension adapter calls that mod’s public dispatch operation only after reservation. Propulsive acceleration/force limits and brake-force limits remain distinct. Legacy API-1/API-2 motion snapshots grant no API-3 braking authority. Forces act at native chassis interfaces; child rotor angles and angular velocities remain owned by their native joints. API 5 requires an explicit version declaration; old binaries cannot inherit the new version implicitly. Station capture requests verify native whole-train hold and delegate to the unique vehicle owner. Capture acceptance does not establish boarding readiness. Older provider implementations require an explicit update to register with API 5. Registering an API provider does not itself establish validated Fourth Dimension compatibility.

Source and original assets are MIT licensed. [Dependency provenance](THIRD_PARTY.md) records third-party tooling and library use.
