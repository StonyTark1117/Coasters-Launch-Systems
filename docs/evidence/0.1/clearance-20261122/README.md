# LS005 equipment running-clearance acceptance — 2026-11-22

Owner: root coordinator, chat `01a2049e-cf6c-7fe0-88fe-c814e690d101`.
Launch Systems checkout `/home/braydon/Documents/Coasters: Launch Systems`,
branch `codex/launch-systems-0-4-0`, base HEAD `1ac51d7`. Source was dirty with
preserved earlier work and concurrent bridge/foundation changes. This is a
bounded clearance result; [artifact identity](artifact.json) records the binary.

Required criterion: LS005 rejects obstructed equipment placement. The new guard
checks actual fixed-world collision shapes in the declared bearing running
corridor at tire/fin/passive-magnetic contact. The focused packaged native test
passed **1/1**: obstruction rejected in both span directions for all three types,
blocked re-arm/rebinding denied, low slab outside the corridor allowed, and UUID
plus accepted binding retained after rejection. Removing the obstruction allows
arming again. Native vehicle/custom-body clearance remains independent.

The first launch stopped before a test ran because the test annotation contained
an already-prefixed structure ID. It was corrected to the repository's `empty`
convention with a namespace-local structure. No gameplay failure was observed.
Concurrent home-drive I/O stalls were diagnosed with thread dumps and iostat;
the corrected disposable world uses persistent NVME4. The original diagnostic
log remains under `/home/braydon/.local/state/launchsystems-runs/ls01-coordinator-20261122`.

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew runGameTestServer -PlsPackaged -PlsDependencyProfile=fd-current \
  -PlsTestNamespaces=coasterslaunchsystems_clearance \
  -PlsBuildDirectory=/home/braydon/.local/state/launchsystems-runs/ls01-coordinator-20261122/build \
  -PlsGameTestGameDirectory=/mnt/NVME4/launchsystems-runs/ls01-coordinator-20261122/clearance-game \
  --no-daemon --max-workers=2 --offline --console=plain
```

[Raw log](gametest.log) records the PASS marker, all dimensions saved, GameTest
shutdown and successful exit. No graphical client, Xvfb or systemd run unit was
started for this case. The Gradle/server processes exited. No further clearance
run is required unless its guard or inputs change.
