# LS001/LS002 current-core foundation — 2026-11-22

Owner: `ls-foundation-20261122` (`/root/foundation`). Checkout
`/home/braydon/Documents/Coasters: Launch Systems`, branch
`codex/launch-systems-0-4-0`, base HEAD `1ac51d7b1dc7c3dffdb672b6df9ab7b9b875fb09`.
Existing dirty preparation/ticket changes were preserved; concurrent bridge and
clearance work was not treated as a clean release commit. No fourth-dimension
source was modified by this task.

## Accepted outcome

The selected standalone baseline is Minecraft 1.21.1, Java 21, NeoForge
21.1.256, Sable 2.0.6, Companion 1.6.0, Veil 4.3.2, Coasters 0.1.5/file 8655742,
Create 6.0.10, Flywheel 1.0.6, Ponder 1.0.82 and Registrate MC1.21-1.3.0+67.
The build profile is `fd-current`; `legacy` remains available for old evidence.
Promotion of the default and combined support is coordinated with LS004.

- The packaged standalone client loaded its shaders, joined the dedicated
  server, authenticated Sable UDP and rendered the world. See
  [startup excerpt](client-startup-excerpt.txt) and
  [connected view](connected-client.png). It disconnected normally before stop.
- The dedicated server started, accepted `save-all flush`, saved all dimensions
  and exited normally on `stop`: [server log](dedicated-server.log).
- Four focused native tests passed in **12.54s**: ordinary passenger mass
  rebuild/ownership, placed controller cable/ticket cycle, interrupted active
  cable reload and changed native mass. [Native log](native.log).
- A separate JVM saved an occupied active catch, then another JVM restored the
  same world. **1/1 prepare and 1/1 verify passed**. Carriage
  `cef17084-49b8-40e0-bd5b-2d6437637dc5` and chassis
  `1d88a182-327b-4761-bc42-34416acc0de1` retained their native catch/loading-ticket
  identity with no replay: [prepare](restart-prepare.log),
  [verify](restart-verify.log). Both saved all dimensions and exited.

These checks close the current-core standalone startup and affected Sable
mass/force/catch/ticket/restoration work. They do not certify an optional FD
facade, prove Survival construction, or reopen accepted 0.2–0.4 mechanics.

## Change and contract audit

`VeilSchedulerStartupMixin` reuses the inspected Fourth Dimension scheduler
repair: defer worker starts until the constructor publishes `threadCount`, and
complete an empty schedule. Its conditional plugin enables it only for Veil
4.3.2 without Fourth Dimension. Combined installations defer to the pinned
Fourth Dimension repair, so two required redirects do not compete. Exact Veil
metadata follows the profile (legacy 4.1.4/current 4.3.2). The current combined
branch requires FD 1.1.0 plus LS004's separate facade contract; a version label
alone is insufficient. See the integrated LS004 evidence for that acceptance.

The retained ticket shim resolves public `SubLevelLoadingTicket.type()/key()`
for 2.0.6, falling back to `getType()/getKey()` for 2.0.3. The codecs, registered
IDs and `(carriage UUID, cabinet BlockPos)` keys did not change. The serialized
restart above verifies the new accessors after loading a saved catch.

Sable 2.0.6 source confirms `QueuedForceGroup.applyAndRecordPointForce` still
calls `forceTotal.applyImpulseAtPoint` directly: LS multiplies world force by
`dt` once and transforms the impulse to local coordinates. The migration does
not insert another integration step. `ServerSubLevel.buildMassTracker` remains
the restoration hook. The ordinary test retained mass 25.6 through native
recovery, displacement 3.725290298461914e-9, removed LS passenger mass under
foreign ownership, restored it on return, then removed stale seat mass.

The cable mass-change test measured max work-accounting error
1.3322676295501878e-15; interrupted reload error was 8.881784197001252e-16.
The controller ticket cycle passed its installed and captured ticket assertions.
These native checks reuse existing fixtures via `FoundationGameTests`; they
introduce no alternative mechanics or synthetic replacement for a real client.

LS ordinary mass runs at Sable pre-physics LOW; equipment applies at LOWEST,
after native contact/ownership updates. `EquipmentRuntime` checks live provider
propulsion/braking permission before force application. The bridge owns current
FD longitudinal authority and optional-addon suppression: merely changing a
schema or mod-version guard cannot stop another addon applying force.

## Exact artifacts and upstream disposition

[Resolved artifact audit](runtime-artifacts.json) records SHA-256, paths and
embedded metadata, including the selected Companion/Veil/Rapier nested
candidates. The startup JAR SHA-256 is
`8bb78ede6decb007b53ace87c265923463cd3c3e77aa5cee59d0040829839c67`.
The native/restart JAR adds the focused test selection (and concurrent source
state) and is
`5dc8ec4cd2916f97b93bdf3bdf54eb24482981b2382745e57e66f9f8484d795e`.
Later metadata tightening does not invalidate the selected exact runtime's
behavior; integrated packaging still checks expanded final metadata.

Coasters 0.1.6/file 9654233 was downloaded from CurseMaven and audited separately:
SHA-256 `197c667ec57ff6d231b7fd81b270d96f4ba8109b02e706a6830d580e9d609cc5`,
embedded mod version 0.1.6 and MIT license. The
[candidate audit](coasters-candidate-audit.json) identifies 21 changed class
files, including `CoasterCartTrackSnap`, resolution/index/plot scans, train
linking and rivet reconciliation. Public/private hook signatures are retained
in [0.1.5](coasters-0.1.5-signatures.txt) and
[0.1.6](coasters-0.1.6-signatures.txt); full bytecode diffs remain in the owned
persistent audit directory. Its pre-physics body scan has substantial changes,
so a compile-only upgrade would not prove native contact/recovery semantics.
**Defer 0.1.6 for this milestone**, retaining the same 0.1.5 artifact as the FD
baseline and its accepted evidence. No 0.1.6 runtime compatibility is claimed.

Likewise defer NeoForge 21.1.267, Flywheel 1.0.7, Ponder 1.0.92 and Create 6.0.11
Maven development builds: none is required for 0.1 and changing the paired
renderer/loader set would mix in unqualified input. The dated dependency review
remains their availability evidence; this task did not re-audit latest listings.

## Runs, storage correction and shutdown

Java 21.0.13.1 and Gradle 9.2.1 were verified before preparation. Initial root:
`/home/braydon/.local/state/launchsystems-runs/foundation-20261122` on persistent
XFS. Preparation used the normal packaged Gradle tasks with explicit `ls*`
directories, profile `fd-current`, `--offline --no-daemon --max-workers=2`.
Client display :391, game/RCON 25731/25732, heap 1536M, Minecraft-only 6 GiB service,
render/simulation 4, FPS 30, raw input and default mouse bindings. No in-world
mouse-mechanics claim was needed. [Client cap/peak](client-memory.txt).

Two initial diagnostics were retained: first-run options needed their Minecraft
data version (3955), and the root drive developed severe I/O stalls. A live
cable fixture blocked waiting for chunk creation; later `save-all flush` blocked
in vanilla `ChunkStorage.flushWorker`, with its I/O worker in native `pwrite0`.
Both dedicated attempts hit the watchdog. The coordinator measured root NVMe
100% utilization at roughly 1.5 MB/s writes, flush latency 160 ms, I/O pressure some 47% /
full 27%. Native tests also advanced in bursts. This was a storage failure, not
permission to waive a recovery claim.

The diagnostic native JVM received SIGTERM and exited; its unfinished world and
logs remain untouched. Fresh corrected runtime worlds were placed at
`/mnt/NVME4/launchsystems-runs/foundation-20261122` (persistent XFS, 162 GiB free).
That disk showed under 1% utilization and about 1 ms await. Direct launch wrappers reused
the exact selected packaged JAR/classpath/VM args, changing only game directory
and requested test namespace/phase. The focused namespace is
`coasterslaunchsystems_foundation`; restart uses
`coasterslaunchsystems_cable_restart` with `prepareActive`, then `verifyActive`
without deleting its world. Native/restart services were capped 4 GiB with 2 GiB
heap. The fresh dedicated save/stop and native suite both succeeded there.

All owned Java processes, client/server/Xvfb and native/restart services are
stopped. [Shutdown verification](shutdown.txt) records MainPID 0 throughout;
the old root-disk server unit retains its failure state as diagnostic evidence,
not a live process. No live runtime is handed over. Original logs, dumps,
launchers and diagnostic worlds remain at the original root; passing worlds,
launchers and logs remain at the NVME4 root. No other task's runtime was stopped.
