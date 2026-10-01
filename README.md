# Coasters: Launch Systems

A standalone Create: Coasters Simulated addon in development toward **0.4.0**. The planned release includes tire, LIM, LSM, flywheel, hydraulic and pneumatic launches, distinct friction/skid/magnetic brakes, local ride control and explicit compatibility with Fourth Dimension vehicles.

The current `0.0.1-dev.20` checkpoint connects the placed flywheel cabinet and physical cable carriage to a ride controller. An ordinary occupied cart can engage its catch, respond to a fresh redstone dispatch request, release the carriage and stop in a linked fin bank without fixture commands. A pinned Fourth Dimension Arrow fixture also passes when its captured train has an independent native preparation hold. An idle carriage survives a real world restart under a reconstructed passive guide and can be retired by explicit empty-track inspection. An occupied catch held at home now survives restart with both native bodies loaded, aligned and disarmed under a passive carriage guide; the catch joint and inspected release are not yet restored. Rendered setup, hydraulic/pneumatic systems and other release gates remain open. Earlier LSM/LIM, tire/fin/passive-magnet equipment, provider-owned station capture and inspected receiver recovery remain available. This is partial implementation toward the requested 0.4.0 release. See [cable scope](docs/CABLE-MECHANICS.md), [linear equipment and limits](docs/LINEAR-MOTORS.md), [release roadmap](ROADMAP.md), and [validation status](docs/VALIDATION.md).

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
