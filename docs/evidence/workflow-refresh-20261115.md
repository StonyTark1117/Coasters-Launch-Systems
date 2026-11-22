# Workflow/dependency preparation — 2026-11-15

Source checkout: `/home/braydon/Documents/Coasters: Launch Systems`, branch
`codex/launch-systems-0-4-0`, base HEAD `1ac51d7`, with the local workflow,
dependency and cable-ticket accessor changes. This is a build/configuration
record, not new gameplay acceptance. No JUnit/GameTest suite, client, server or
Xvfb was run. No live runtime was left behind.

The existing dirty `RideControllerGameTests.java` was preserved byte-for-byte:
SHA-256 `494ae2dea2df12606b43f631f296936477b8a6766b182e6a2b98bd4b540bec72`.
It was included in these builds, so these artifacts must not be described as
clean committed dev.72 release binaries.

## Environment and commands

Java and Gradle launcher/daemon: `/usr/lib/jvm/java-21-openjdk`, Java
21.0.13.0.3; Gradle 9.2.1. Before preparation, host reported about 31 GiB
available RAM and 262 GiB free on persistent XFS. Current and ancestor cgroups
had no finite `memory.max`; other processes were inspected. No other runtime
was stopped or changed. Builds used one terminating Gradle daemon and two workers.

For each profile, from the source checkout with Java 21 first in PATH:

```sh
./gradlew assemble createClientLaunchScript createObserverLaunchScript createServerLaunchScript \
  -PlsPackaged "-PlsDependencyProfile=$PROFILE" \
  "-PlsBuildDirectory=$PREP/build" \
  "-PlsClientGameDirectory=$PREP/client" \
  "-PlsObserverGameDirectory=$PREP/observer" \
  "-PlsServerGameDirectory=$PREP/server" \
  --offline --no-daemon --max-workers=2 --console=plain
```

`PROFILE=fd-current` used
`PREP=/home/braydon/.local/state/launchsystems-preparation-20261115/current`;
`PROFILE=legacy` used the sibling `legacy` directory.
Both final invocations reported **BUILD SUCCESSFUL in 6s**. Existing event-bus
deprecation and Gradle 10 compatibility warnings remain; this did not migrate
the wrapper/plugin. Gradle's asset preparation reported an asset-index request
despite `--offline`; no game process was launched.

The initial current-profile compile failed at 25 references to Sable loading
ticket getters. Inspection with Java 21 `javap` confirmed 2.0.3 has
`getType()/getKey()` while 2.0.6 has `type()/key()`. `CableTickets` now resolves
these public accessors once; recovery and existing cable fixtures use that
layer. Neither ticket identity nor saved key encoding changed. Compilation
against both artifacts passed after this correction. Runtime recovery behavior
on the new core still needs its affected qualification; no test result is implied.

## Artifact and launcher inspection

Both JARs are named `coasters-launch-systems-0.0.1-dev.72.jar` under their
separate `build/libs` directories:

| Profile | JAR SHA-256 | Expanded exact core requirements |
| --- | --- | --- |
| legacy | `7121e97e1130b1957d40238da02c2623729d08399b1439b4b3016653520a4cca` | NeoForge 21.1.248, Sable 2.0.3 |
| fd-current | `3b3518178ff4b750540608033be45e00c840f2900cb173445d6d57e73736186b` | NeoForge 21.1.256, Sable 2.0.6 |

Both retain the exact optional Fourth Dimension dev.1 pin. The current profile
does not support schema-8 combined installation yet.

For all six generated `build/moddev/run{Client,Observer,Server}.sh` scripts:

- The executable is Java 21 and working directory is the corresponding isolated
  persistent client, observer or server directory.
- Referenced VM argument files contain `-Xmx1536M` for client/observer and
  `-Xmx2048M` for server.
- Referenced runtime classpath files include that profile's packaged JAR;
  `fml.modFolders` is empty, so this is packaged launch preparation.
- Generated scripts contain no Gradle launcher. Runtime process-group limits
  still require the owned systemd procedure before any actual game launch.

Local documentation links resolved and `git diff --check` was clean. See
[dependency disposition](../DEPENDENCIES.md) and [current FD migration](../FOURTH-DIMENSION-HANDOFF.md)
for the remaining scheduler, authority and runtime work. Legacy remains default.
