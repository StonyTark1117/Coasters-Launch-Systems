# Branch and task continuity

Before editing, run `git status --short --branch` and `git worktree list`.
Continue the branch/worktree already assigned to this task, including across
compaction. This checkout currently uses `codex/launch-systems-0-4-0`; do not
switch it to Fourth Dimension's `main` or create branches to get a clean tree.
Inspect and preserve existing changes. Create one stable task worktree only
for genuinely parallel work that cannot reuse an existing one. Preserve unique
commits and dirty files before integration or removal. Record branch, absolute
path, HEAD, dirty files, assigned case and next action in handoffs.

# Start here and finish the assigned outcome

Read [the current work queue](docs/CURRENT-WORK.md), [roadmap](ROADMAP.md),
[validation policy](docs/VALIDATION-POLICY.md), and the relevant section of the
[execution guide](docs/ROADMAP-EXECUTION.md).
The roadmap defines Launch Systems scope; the queue identifies remaining work.
Historical evidence's "still open" prose does not assign a new task. Do not
replay all development checkpoints or stop after an arbitrary evidence slice.
Reuse passing evidence unless a named change affects its claim. Close the whole
assigned outcome when its required criteria are covered. After two inconclusive
attempts, diagnose and change the failed condition before another attempt;
this does not prohibit a corrected targeted run or make local setup repair an
external blocker. Report a blocker with the exact unmet criterion and evidence.

After compaction, use the execution guide's [resume and handoff procedure](docs/ROADMAP-EXECUTION.md#resume-and-handoff).
Read the current case, latest successful action/readback and actual runtime
record before another command. A summary's old display, coordinates, input
failure or "still open" note is not live state. Keep one current next-action
entry; do not create a second status tracker or new acceptance requirements.

Fourth Dimension is the sister project at
`/home/braydon/Documents/Coasters: Fourth Dimension`. Read the
[integration handoff](docs/FOURTH-DIMENSION-HANDOFF.md) before combined work.
Its current progress enables an adapter migration; it does not certify our
old schema-4 provider against schema 8. Never fix that mismatch by relaxing a
version/schema guard alone. Fourth Dimension's case queue is not our assignment.
Use its files as references; coordinate changes to its source with its owner.
Work on standalone ordinary-cart cases can proceed during adapter migration.
Shared interface changes need a named owner and an integrated handoff before
dependent implementation consumes them; this is not a lock on independent
private displays. Stable specifications can unblock independent content work.
Use the selected artifact and its matching source, not unfinished sibling edits
that happen to be in the primary checkout. See [source/artifact selection](docs/FOURTH-DIMENSION-HANDOFF.md#select-an-integrated-source-and-artifact).

# Java, dependencies and storage

Use Java 21 for Gradle, GameTests, servers, clients and child processes:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
"$JAVA_HOME/bin/java" -version
./gradlew --version
```

Both must report 21. The toolchain does not choose Gradle's launcher JVM; the
shell default may be Java 27. Fix the environment before retrying a crash.
Read [dependency profiles](docs/DEPENDENCIES.md). Use `ls*` project properties,
not Fourth Dimension's `fd*` properties. Current-core migration is explicit;
do not silently mix legacy/current JARs or claim either profile covers the other.

`/tmp` is RAM-backed and may disappear on reboot. Keep builds, game directories,
worlds, extracted artifacts, logs and evidence on persistent storage. Default
colon-safe output is under `~/.local/state/coasterslaunchsystems-<hash>/build`;
read Gradle's actual `buildDir`. Use distinct `lsBuildDirectory`,
`lsClientGameDirectory`, `lsObserverGameDirectory`, `lsServerGameDirectory`, and
`lsGameTestGameDirectory` absolute paths for isolated runs. Keep valuable worlds
outside build output. `runGameTestServer` deletes its selected world's directory
unless `lsKeepGameTestWorld` is set: never point it at a valuable save.

Check `findmnt -T /absolute/path`, `df -h` and host RAM before large operations.
If disk is low, inspect project-owned storage with `du -x -h --max-depth=2`.
Preserve needed worlds/evidence, confirm no runtime uses a directory, then remove
disposable data from completed runs you own. `clean` is only for idle build trees
whose default run worlds are disposable. Recheck free space and record cleanup.
Preserve any needed old `/tmp` worlds/evidence before reboot or cleanup.
Never delete another task's data, shared caches, tracked evidence, unique Git
history or dirty work for space. Tiny disposable scratch files may use `/tmp`.

# Private testing and resource ownership

Follow [the local run procedure](docs/RUN-PREPARATION.md). Build and run on this
development host. Headless checks need no display. GUI tests use an owned private
Xvfb display with explicit `DISPLAY` for every client, screenshot and input call.
Never use the user's desktop, mouse, keyboard or browser, ChatGPT's built-in
Agent Workspace, host `ydotool`, or `/dev/uinput` for Minecraft tests.

Independent tasks may use live input concurrently with separate owned displays,
game directories, worlds/ports and process units, if memory permits. Another
agent's input is not a global lock. Coordinate only shared resources and case
ownership; user-assigned parallel same-case runs may use independent copies and
reconcile evidence. Verify PID/cgroup/window ownership. Never adopt an old display
number or edit another run's owner record to bypass a handoff.

Before each launch check `free -h`, current and ancestor cgroup `memory.max` /
`memory.current`, and other Minecraft, Gradle and agent processes. Leave room for
the user's work and all servers/clients. Default client heap is **1536M** via
`lsClientMaxHeap`; default Minecraft-only cgroup cap is **6 GiB**. **3 GiB is
retired.** Prepare direct Java launch scripts outside that client scope. Gradle
has its own memory demand and must not share the client's cap. Verify the actual
launch arguments, Java version and active unit's limit. A heap flag alone is
not a process-group cap. Do not launch a graphical client unbounded.

Raise heap for measured Java heap pressure, or the scope for measured RSS/cgroup
pressure, with reasonable headroom after checking host limits. No separate
permission is needed. Reduce concurrency first, record the reason/limits/peak,
and restore defaults for unrelated runs. Low render/simulation distance helps
copied-world checks; record it. Insufficient headroom means wait or reduce load.

# Input: one action and a checked result

Use Fourth Dimension's proven `scripts/fd-private-client.py` as described in our
run guide. Read its current procedure before experimenting with controls.
`turn` sends relative XTest device motion; `move` positions the menu pointer.
Keep `rawMouseInput:true`, use on right mouse and attack on left mouse. Inspect
the owned options before/after startup. Record, revert and restart after any
diagnostic remap. Prove camera turning and reachable vanilla in-world use once
in a new configuration, then keep it fixed. Menu clicks are not in-world proof.

Before input, confirm an in-world view and the player's connection in the owned
server log/status. A Java process may be sitting at a timeout screen. Reconnect
using the recorded endpoint/server.properties; Direct Connection does not save a
server-list entry. Diagnose repeated timeouts separately from mouse controls.
After restarts/resizes/reloads read fresh geometry and screenshots. Bare Xvfb
may lack a window manager: use the owned window's `windowfocus` and targeted
events, not `windowactivate`. Target `/` for commands; do not assume `T` opens chat.

Name the expected result, send one action, inspect screenshot/log/world state,
then continue. On a miss check focus, F3 target, actual reach, obstruction, item,
bindings and interaction guards. Do not batch blind clicks, repeatedly warp the
pointer or guess teleport angles. An F3 hit does not prove reach. Inspect moving
body transforms using the sister guide when relevant. Disclose setup commands;
they do not prove player construction or normal camera control. Set mod configs
before startup; integrated servers may pause in menus, so timed GUI checks need
a dedicated server or verified ticking LAN world.
Read the run guide's [specific input diagnostics](docs/RUN-PREPARATION.md#specific-input-diagnostics)
for Shift targeting, Create hold-to-edit, kinetic readback and moving bodies.
Helper flags and screenshots do not by themselves prove an operation was accepted.

# Completion includes shutdown

Before marking a case/goal complete, stop its owned client, save and stop its
server cleanly, then stop Xvfb and all owned run services/scopes. Verify PIDs/units
inactive; report any failure. Preserve worlds, logs and requested evidence on
disk. Apply the same cleanup to abandoned/blocked runs without an active owner.
Leave a runtime alive only after a named continuing task explicitly accepts the
exact client, world/server, display and units. Idle processes are not a handoff.
Do not stop another active task. Ask its owner to clean up completed runs. A
coordinator may stop a confirmed orphan only after verifying the case is closed,
no task/client uses it, its world/logs are saved and the exact unit belongs to
that completed run; record that verification. Ambiguous runs require coordination.
