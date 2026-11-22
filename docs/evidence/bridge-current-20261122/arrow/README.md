# Arrow player construction, occupied contract, and saved-world restart

Owner `/root/bridge`, 2026-11-22. Accepted inputs: packaged LS `67b2b08f571c78224be87b421010518c4cdc812362a1addf6b47ec68043b1d98`, FD `df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e`, current-core Sable 2.0.6 and Create 6.0.10. These JARs were pinned before this run; a later integration rebuild does not invalidate this behavior evidence.

## Construction and setup disclosure

Dev was in Survival. Setup supplied the level platform, support blocks, kits, food, 64 RPM Creative Motor power sources, and exact player poses for reproducible reachable interactions. Weather was cleared for the return observation. Setup commands did not author the completed ride, native vehicle, interface blocks, module bindings, controller links, or authority records. This proves normal item/tool construction and operation, not gathering/crafting materials or unaided camera navigation.

The private client first demonstrated camera motion and reachable vanilla right-click placement with normal use/right and attack/left bindings and Raw Input true. Four floor-up anchors A(0,100,0), B(0,100,32), C(16,100,32), D(16,100,0), native original Arrow rails, normal Arrow car, chassis tire strip and brake fin were then placed through real Survival item use. Native rail construction preserved the two straight tangents and curved joins; no curve or vehicle NBT was authored. Each bank has 14 tire modules and 13 fin brakes, all normally placed, bound and linked. Two normally placed controllers at (5,98,4) and (21,98,28) have capture enabled through the normal Span Tool/clock action and paired destinations through normal tool clicks. Lever inputs are real use events. `construction-readbacks.jsonl` and `input.jsonl` record each operation and its asserted result; full screenshots remain in the preserved run directory.

Native identity throughout: chassis `caeeb5a5-ba62-4f84-8ada-aa36cf550406`, right carrier `3c989e64-ad2f-4697-b858-2dfc402c5848`, left carrier `31fac2cb-4057-48ba-9e7c-58064452677b`. Empty mass was 17; Dev increased the right carrier from 4 to 4.8, total 17.8. Real native restraint GUI and original seats were used.

## Actual player result

An empty source-to-receiver-to-source circuit passed both curved joins and native captured stops. The complete chassis reservation moved atomically between the same two controllers; the old section became empty after each successful transfer.

Dev boarded by normal empty-hand use of a verified reachable native seat, closed restraints in the native GUI, and stayed in that same seat for the occupied circuit. The independent Observer client operated source low/high at 19:35:50/19:35:58 UTC, then receiver low/high at 19:37:01/19:37:10. Both stations reached READY_EXIT, boarding-ready true, no fault, with the same chassis reservation. Independent close-up and motion evidence is in `../../ls01-fd-observer-20261122/`.

At the returned source stop, native Open restraints followed by normal Shift exit removed Dev's RootVehicle while preserving the chassis reservation. Normal reboard at 19:38:21 created a new Create seat mount on the same native carrier; native Close restored Ready/Owner hold. A fresh operator low/high at 19:41:01/19:41:10 departed again, reached receiver READY_EXIT, and Dev exited normally. The first receiver Open request used a GUI snapshot opened while moving and correctly reported stale settings; the refreshed captured-stop request succeeded. No guard was bypassed. Observer disconnected and released the connection before restart.

## Separate-JVM restoration

At 19:42 UTC Dev disconnected, the dedicated server saved all dimensions and exited, and the client process stopped. `pre-restart-foundation.json` is decoded from the stopped world's saved native data, not a serializer-only fixture. It records receiver owner UUID `[206887987,-1533787541,-1625766488,-2043218469]`, controller position 5772436160610, provider `coasterslaunchsystems:local_controller`, the same chassis, epoch 6, held true.

A new server JVM reopened the same `LS01Arrow` world and the client reconnected. The controller was INSPECTION/disarmed with the same reservation and identity; already-powered receiver lever did not move the train. Native authority restored epoch 7, sequence 1, held and suspended, reason `Restored authority requires explicit Resume`. The same chassis position was 16.388891,102.173607,19.406361. `restart-held-foundation.json` was read after completed save-all flush. The native GUI visibly showed Owner paused.

Normal empty-hand Shift inspection/rearm of the two controllers resumed epoch 8, sequence 2 but retained the hold. Modules retained their saved armed settings; they were not toggled or recreated. With the old high lever the receiver remained READY_EXIT and the chassis stayed stopped. Only separate normal low then high input entered EXITING. The empty car traversed the other curved join and returned source READY_EXIT, no fault, same chassis reservation, empty receiver reservation. `restart-final-readback.txt` and `final-stopped-foundation.json` preserve the result. This proves actual saved-world native ownership, explicit rearm and fresh dispatch; no extra occupied loop was required after the already accepted occupied contract.

## Resources and cleanup

Persistent run `/mnt/NVME4/launchsystems-runs/bridge-20261122/arrow`, world `server/LS01Arrow`; private display :393, loopback game/RCON ports 25751/25752. Java 21, client heap 1536M and Minecraft-only 6 GiB unit, server heap 2048M and 4 GiB unit. Client memory observed about 2.6 GiB; sufficient host/cgroup headroom checked. First-run options reset render distance to 12 while server view stayed 4; restored client used render/simulation 4. Default mouse bindings and Raw Input stayed fixed. Poses were setup aids, not evidence of unaided aiming. No user desktop or another agent's display was controlled.

After the final accepted transfer, Dev disconnected, server saved all dimensions and exited, then owned client and Xvfb stopped. `cleanup.txt` verifies all three `fd-ls-arrow-20261122-{server,client,xvfb}.service` units inactive, MainPID 0, Result success. Runtime limits disappear from unloaded transient units; live limit evidence is recorded in the run and launch procedure. There is no continuing owner or idle live runtime. World, logs, source snapshot, raw screenshots and scripts remain on persistent NVME4 storage. Tracked excerpts omit server.properties/RCON credentials. `sha256.json` covers the curated raw evidence files.
