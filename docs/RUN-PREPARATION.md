# Owned Launch Systems runs

Read [AGENTS](../AGENTS.md), [validation policy](VALIDATION-POLICY.md) and
[dependency profiles](DEPENDENCIES.md) first. Use the smallest affected case.
These are procedures for authorized runs, not instructions to start a client
on every task. No live FD runtime or saved world is implicitly handed over.

## Prepare persistent, isolated inputs

Choose an absolute, colon-free persistent root such as
`/home/braydon/.local/state/launchsystems-runs/<task>-<date>` with `client/`,
`server/`, `build/`, `project-cache/` and `evidence/` subdirectories. Record its task owner, reserved
private display (>=100), unique game/RCON ports and unit names. Check ports,
Xvfb sockets, live units, `free -h`, cgroup limits/usage and `df -h` first.
Use unique roots for independent clients; an observer must have its own game
directory. Do not read a running world's files as a consistent saved snapshot.

Set `LS_RUN`, `LS_PORT` and `LS_OWNER` to those actual values. From this checkout:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
"$JAVA_HOME/bin/java" -version
./gradlew --version
./gradlew createClientLaunchScript createServerLaunchScript \
  -PlsPackaged -PlsValidation -PlsDependencyProfile=fd-current \
  -PlsClientMaxHeap=1536M -PlsServerMaxHeap=2048M \
  "-PlsConnect=127.0.0.1:$LS_PORT" \
  "-PlsBuildDirectory=$LS_RUN/build" \
  "-PlsClientGameDirectory=$LS_RUN/client" \
  "-PlsServerGameDirectory=$LS_RUN/server" \
  "--project-cache-dir=$LS_RUN/project-cache" \
  --no-daemon --max-workers=2 --console=plain
```

Use a separate persistent `--project-cache-dir` for each concurrent build owner,
as well as a separate `lsBuildDirectory`. A different output directory alone
still shares checkout `.gradle` task history and file-hash locks; concurrent FD
builds produced an incomplete JAR until both were isolated. If affected, stop
only the owned preparation, regenerate its compile/resources/JAR outputs, and
verify the expected class inventory and exported APIs before launching. Do not
clean another owner's output or the shared dependency cache.

On 2026-11-22 the home/root NVMe stalled severely during cache writes and world
flushes. Fresh owned worlds on persistent `/mnt/NVME4/launchsystems-runs/`
completed normally. Check actual mount, free space and I/O pressure before
choosing storage; the path itself is not a permanent performance guarantee.
Preserve stopped original saves and diagnostic logs when relocating a run.

This prepares the JAR and scripts, not a test suite. Both generated launch tasks
depend on the packaged JAR. For an observer use `createObserverLaunchScript` and
`lsObserverGameDirectory`. Read the actual output scripts and referenced argument
files: verify Java 21, selected heap, packaged mod discovery, profile, game
directory and endpoint. Do not copy FD's `fd*` properties into these commands.
To inspect the default build directory, use `./gradlew -q properties` and read
`buildDir`; never guess the path hash. Reuse a matching launcher rather than
regenerating it for every input attempt.

The default `fd-current` profile is qualified against the exact inputs in
[the handoff](FOURTH-DIMENSION-HANDOFF.md). Legacy/current outputs and worlds
must be separate. Preserve an original save before migration; no downgrade
compatibility is claimed.

## Reuse the proven input helper

Use the sibling's current
[`scripts/fd-private-client.py`](<../../Coasters: Fourth Dimension/scripts/fd-private-client.py>)
and read its [run/input procedure](<../../Coasters: Fourth Dimension/docs/FD403-RUN-PREPARATION.md>)
for input details. Its resource ownership checks work with our generated direct
Java scripts. It currently requires an `fd`-prefixed unit name; choose a unique
`fd-ls-<task>-<date>` prefix. That spelling does not give ownership of an FD run.
Do not copy its retained Arrow run.json or use display `:298` from old evidence.

Create your own `$LS_RUN/run.json`, with **real reserved values** replacing this
illustrative record, and actual generated script paths:

```json
{
  "root": "/absolute/persistent/launchsystems-run",
  "owner": "actual-task-thread-id",
  "display": 399,
  "unit_prefix": "fd-ls-unique-task-date",
  "player": "Dev",
  "client_script": "/absolute/persistent/launchsystems-run/build/path/to/client.sh",
  "server_script": "/absolute/persistent/launchsystems-run/build/path/to/server.sh",
  "client_limit": "6G",
  "server_limit": "4G"
}
```

The helper expects `$LS_RUN/client/options.txt` with at least
`rawMouseInput:true`, `key_key.use:key.mouse.right` and
`key_key.attack:key.mouse.left`. Preserve all unrelated settings. Record low
render/simulation distances appropriate to the case. Configure the local server
with `server-ip=127.0.0.1`, the reserved game port, `enable-rcon=true`, a distinct
RCON port, and a random password stored only in its mode-0600
`server.properties`. Set `eula=true` only for the user's authorized development
server; never put passwords in evidence. Use the actual local test player's
name, and keep any offline development server loopback-only.

```sh
LS_HELPER='/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py'
python3 "$LS_HELPER" "$LS_RUN/run.json" --owner "$LS_OWNER" start server
# Inspect server log for successful startup before starting the client.
python3 "$LS_HELPER" "$LS_RUN/run.json" --owner "$LS_OWNER" start xvfb
python3 "$LS_HELPER" "$LS_RUN/run.json" --owner "$LS_OWNER" start client
python3 "$LS_HELPER" "$LS_RUN/run.json" --owner "$LS_OWNER" status
python3 "$LS_HELPER" "$LS_RUN/run.json" --owner "$LS_OWNER" snapshot
```

The helper starts owned systemd services with memory caps and refuses a live
unit or an ownership mismatch. It uses default `JAVA_TOOL_OPTIONS` heaps of
1536M client / 2048M server; our run-model JVM arguments also carry the selected
heaps. Inspect actual `VM.flags` when adjusting limits so an inherited option
does not silently override your choice. Prepare Gradle outside those services.
Raising `client_limit` based on measurements is allowed under AGENTS; regenerate
the direct script with a higher `lsClientMaxHeap` if Java heap pressure requires it.

For one in-world action, first identify the target and expected readback:

```sh
python3 "$LS_HELPER" "$LS_RUN/run.json" --owner "$LS_OWNER" \
  input use --expect 'The targeted disarmed cabinet opens its configuration screen'
```

Inspect the captured result before sending another action. `turn dx dy` means
relative camera motion; `move x y --menu` means menu-pointer placement. For
Create scroll-value selection use the documented `hold-use` procedure, not a
sequence of blind taps. `status` confirms window ownership, options and server
connection. Use `--menu` explicitly for reconnection/menu actions. A timeout
screen needs connection diagnosis and the recorded endpoint, not new bindings.

## Specific input diagnostics

These are diagnosed procedures from FD's [input/readback guide](<../../Coasters: Fourth Dimension/docs/FD403-RUN-PREPARATION.md#one-action-then-its-readback>).
Apply only the one relevant to the current miss; they are not a new checklist
to repeat before every interaction.

- `--expect` records intent and `--menu` labels a menu action; neither detects
  the current screen or proves success. Read the actual returned view and
  server result. Never reuse menu coordinates after returning to gameplay.
- `use` is right button 3; `attack` is left button 1 and may instantly break
  Creative-world equipment. Do not use Attack to probe a value control. If an
  earlier diagnostic may have broken the target, confirm it still exists.
- Shift changes eye height. Establish the hit/reach in the action's actual pose
  before Shift+Wrench or Shift+Use. The helper's `before` image precedes the
  modifier and its `after` image is taken while it is held; these are different
  poses. A rail block hit can also differ from the native curve/span hit.
- For chat, send `key slash`, inspect the field, then type the command without
  a second leading slash and press Return. `type` assumes an already open field.
  Confirm command acceptance rather than inferring it from typed characters.
- Minecraft can discard the first motion callback after mouse recapture. The
  helper's `turn` already includes the diagnosed lead-in; do not repeatedly
  open/close a menu or replace it with pointer warps. Read the actual rotation
  and target afterward; identical input deltas are not exact world coordinates.
- Create's hold-to-edit uses held Use. Inspect with `input hold-use` (cancels on
  release), then use `input hold-use X Y` with coordinates from that held view
  to submit a named value. Verify both the control and driven equipment state.
  Live NBT edits of motor `Speed`/`ScrollValue` bypass the normal callback and
  can leave the kinetic network inconsistent. A displayed/stored sign or chain
  animation alone does not establish world-axis drive direction or native work.
- For a moving-body target, match the current body UUID and sublevel-local
  interaction shape, transform it using the selected Sable version, and inspect
  live reach/obstruction from a supported player position. Console selectors
  are not automatically relative to the player. Follow FD's
  [moving-body procedure](<../../Coasters: Fourth Dimension/docs/FD403-RUN-PREPARATION.md#recover-a-moving-body-target-without-angle-guessing>)
  after checking its version-specific command syntax. Do not substitute a body
  teleport for player setup, or assume an FD seat's local hit shape applies to
  an LS cabinet, interface or catch. Record the successful UUID/local point and
  readback so resumption does not rediscover the same target.

Distinguish an input event, a handler's accepted result, and the resulting game
state. An editor-open diagnostic is not a dispatch; a request can be denied by
guards. Read the installed build's handler/log meanings and current screen
lifecycle. Newer FD editor behavior is not automatically Launch Systems behavior.
After a diagnosed correction, continue the remaining check using the proven
configuration; do not restart a generic mouse investigation.

## Save, stop and report

Quit the owned client normally. Stop its exact client service if it has not
exited. Through the helper's `console` command, issue `save-all flush` and then
`stop`; inspect the server log and wait for clean exit. Stop only the recorded
Xvfb/service units after the Java processes exit. The helper has **no stop
subcommand**; use `systemctl --user stop <exact-owned-unit>` where needed.
Verify with `systemctl --user show <unit> -p ActiveState -p MainPID` and recorded
PIDs. A failed graceful shutdown must be diagnosed and reported before any
forced termination that could lose the save.

Retain the world, launch configuration, artifact identity and relevant evidence
on disk, and report the stopped units. Live handoff requires a named task's
explicit acceptance of every resource. A completion message saying "left
running for inspection" is not sufficient. Never clean up another active run.
