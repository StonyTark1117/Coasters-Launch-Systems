# Coasters: Launch Systems

A standalone Create: Coasters Simulated addon with **1.0.0 standalone release**: tire,
LSM, LIM, finite flywheel, hydraulic and pneumatic launches; friction-fin,
skid and passive magnetic brakes; local ride control; and explicit certificates for the eligible Fourth Dimension catalog.

Normal-built occupied rides, linear swing launches, physical cable return,
pressure recharge/service and targeted interruption/restart checks passed.
The [0.4 integration record](docs/evidence/0.4/integration-20261122/README.md)
links exact artifacts, accepted evidence and exclusions. It builds on the
[accepted 0.1 foundation and three usable rides](docs/evidence/0.1/integration-20261122/README.md).
The [0.5 integration record](docs/evidence/0.5/integration-20261126/README.md)
adds the [finite family × equipment table](docs/FD-VEHICLE-CERTIFICATES.md),
actual mounted contacts, translated catches and occupied native controls.
The [0.6 integration record](docs/evidence/0.6/integration-20261126/README.md)
adds native bounded Arrow reservations, shared operator controls, occupied
conflict handling and explicit saved-world recovery without request replay.
The [0.9 integration record](docs/evidence/0.9/integration-20261127/README.md)
closes the supported matrix, provider-removal lifecycle, measured callback cost
and remaining occupied FD demonstrations. Old saves must first load affected
vehicles with 0.9 and their original provider installed, then save before provider
removal; see the migration instructions in the operator guide.

The [1.0.0 release](https://github.com/StonyTark1117/Coasters-Launch-Systems/releases/tag/v1.0.0)
provides the JAR and checksum. [Release notes](docs/releases/1.0.0.md) list exact
installation downloads and the optional FD snapshot boundary.

For installation, braking choices, fresh requests and recovery, read the
[current operator guide](docs/OPERATOR-GUIDE.md).

## Previous accepted checkpoint

The `0.0.1-dev.72` checkpoint contains independent tire, LSM, LIM, flywheel, hydraulic and pneumatic launches; friction-fin, skid and eddy-current brakes; a local controller; a versioned Fourth Dimension provider adapter; and a fail-closed Coasters: Engineered 1.1.1 active-span bridge. It includes 21 original recipes and four standalone Ponder guides covering controller/span binding, linear-motor hardware, brake interfaces and cable launches. The latest combined Engineered/Fourth Dimension packaged matrix passed 215 required GameTests, including a real active-controller contact predicate. All four Ponder guides render from the packaged client with localized scene text. The dev.67 focused lifecycle checks prove active ordinary, Arrow, and individual Free Spin reserved vehicles can become unavailable through normal chunk scheduling while their controllers retain ownership and stay faulted. Dev.69 also restores normal player Span Tool binding and linking on equipment cabinets and ride controllers under Minecraft 1.21 block-interaction ordering; its focused packaged GameTests passed 66/66 and its full standalone matrix passed 131/131. Dev.70 adds a regression for a player-buildable level/unbanked span: mount both Coasters Simulated anchors on the floor, then connect them with Coaster Track; side-mounted anchors retain a banked frame and are intentionally refused by the equipment guards. The dev.70 packaged standalone build passed 29 JUnit tests and 132/132 required GameTests; its JAR SHA-256 is `46ec7a6d772d1ae98bda5ac3ce320934522f857fc5e6ce315136900219f4d08f`. Dev.71 carries the same floor-anchor instruction into the in-game controller Ponder guide; the focused packaged build and normal Ponder entrypoint render are recorded in [the dev.71 evidence](docs/evidence/ponder-dev71-floor-anchor.txt). Dev.72 adds a composed floor-anchor Span Tool installation and occupied ordinary dispatch fixture; the packaged standalone build passed 29 JUnit tests and 133/133 required GameTests in 4.820 minutes.

That historical checkpoint preceded the accepted 0.1–0.4 work above. The adapter preserves Fourth Dimension ownership of carrier rotation, mass, restraints, retention and PAX winch state; Launch Systems only owns an accepted translation operation. See [cable scope](docs/CABLE-MECHANICS.md), [linear equipment and limits](docs/LINEAR-MOTORS.md), [pressure operation](docs/PRESSURE-MECHANICS.md), the [optional integration audit](docs/OPTIONAL-INTEGRATIONS.md), [release roadmap](ROADMAP.md), and [validation status](docs/VALIDATION.md).

## Updated version plan

The [November 22 roadmap](ROADMAP.md) keeps **0.4** as the first complete
six-launch milestone, now accepted with its core/FD authority, player rides
and equipment/lifecycle criteria. **0.5**, also accepted, adds explicit
certificates for the expanded FD catalog; **0.6**, now accepted, integrates its bounded multi-train operation;
**0.9**, now accepted, reconciles the supported package; **1.0** publishes that qualified standalone package. Accepted evidence carries
forward. See [upstream dependency findings](docs/DEPENDENCY-REVIEW-2026-11-22.md)
and the [execution guide](docs/ROADMAP-EXECUTION.md).

## Resume development

Start with [AGENTS.md](AGENTS.md), [current work](docs/CURRENT-WORK.md), and
[validation policy](docs/VALIDATION-POLICY.md). The old combined results above
used Fourth Dimension `0.9.0-dev.1` / schema 4. The selected Fourth Dimension integration snapshot uses
schema 8 and the accepted 1.3 implementation (artifact label 1.1.0), plus
external authority API 1 and the additive family-power contract. The sibling
project has since published 1.5.0; that does not expand this snapshot’s tested
catalog. The adapter requires the registered facade; native checks have
passed, including real occupied Arrow and separate-JVM saved-world restart. Follow the
[pinned handoff](docs/FOURTH-DIMENSION-HANDOFF.md) for exact source/JAR identity.

## Development stack

Minecraft 1.21.1 and Java 21. The default `fd-current` dependency profile uses
NeoForge 21.1.256 / Sable 2.0.6 / Veil 4.3.2.
`-PlsDependencyProfile=legacy` retains NeoForge 21.1.248 / Sable 2.0.3 / Veil 4.1.4
for reproducing historical evidence. Create 6.0.10-280, Coasters Simulated 0.1.5, Flywheel 1.0.6,
Ponder and Registrate remain shared. Read [dependency status](docs/DEPENDENCIES.md)
for exact qualified pins and the optional Fourth Dimension facade requirement.

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
"$JAVA_HOME/bin/java" -version
./gradlew --version
./gradlew assemble --no-daemon --max-workers=2
```

Unix checkout paths containing `:` redirect output to persistent
`~/.local/state/coasterslaunchsystems-<path-hash>/build`, not `/tmp`. Read the
actual Gradle `buildDir`; `lsBuildDirectory` overrides it. Use isolated persistent
client/observer/server/GameTest directories and keep valuable saves outside build
output. GameTests recreate their selected disposable world unless
`lsKeepGameTestWorld` is set.

For graphical testing use the [private run procedure](docs/RUN-PREPARATION.md):
Java 21 direct launch scripts, owned Xvfb, 1536M default client heap, a 6 GiB
Minecraft-only cgroup, measured increases when needed, and shutdown on completion.
`lsPackaged` uses the distributable JAR instead of loose mod classes;
`lsConnect` selects a direct multiplayer endpoint and `lsWorld` a singleplayer
save. Direct Connection does not create a saved server-list entry.

## Architecture

Mechanism math uses blocks, seconds and native Sable mass/work units. Energy stores are server-owned; hydraulic and air resources use original game-scale pressure models. No real-world performance or manufacturer replica fidelity is claimed.

`VehicleMotionProvider` API 5 describes body and consist identity, verified interfaces, operator ownership, dispatch vetoes, explicit braking permission and an independent brake-force rating. Its dispatch and capture requests default to denial; the ordinary provider grants its own cart permission and the pinned Fourth Dimension adapter calls that mod’s public dispatch operation only after reservation. Propulsive acceleration/force limits and brake-force limits remain distinct. Legacy API-1/API-2 motion snapshots grant no API-3 braking authority. Forces act at native chassis interfaces; child rotor angles and angular velocities remain owned by their native joints. API 5 requires an explicit version declaration; old binaries cannot inherit the new version implicitly. Station capture requests verify native whole-train hold and delegate to the unique vehicle owner. Capture acceptance does not establish boarding readiness. Older provider implementations require an explicit update to register with API 5. Registering an API provider does not itself establish validated Fourth Dimension compatibility.

Source and original assets are MIT licensed. [Dependency provenance](THIRD_PARTY.md) records third-party tooling and library use.
