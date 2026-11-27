# Fixed, Classic and S2 independent occupied operator

Owner `/root/foundation`, supporting rider/server owner `/root/bridge`.
All three assigned occupied operator cases passed. Normal final disconnect and
owned client/display shutdown completed on 2026-11-27 at 02:46 UTC (November 26
local date). No runtime remains under this observer's ownership.

Persistent root `/mnt/NVME4/launchsystems-runs/ls05-fixed-20261122/foundation-observer`;
private display `:400`, player `Observer`, bridge endpoint `127.0.0.1:25801`.
The bridge retained exclusive server/world lifecycle ownership. This observer
used its own copied direct launch arguments, game directory and frozen artifacts:
LS `b5f838e7258df46cfb95031cb574dab623671f24e0e167fbafb37f2c466b4a56`,
FD `3dbbc7a77e1b0cd02b3e5fa3a69e4b4823c5c5a3befca6f0e1e35d72381b7fba`.
[Artifact identity](artifacts.json), [run identity](run.json),
[prelaunch resources](prelaunch-resources.txt) and [heap verification](heap-memory.txt)
record Java 21, 1536M heap, a 6GiB Minecraft-only service, 36GiB available host
memory and free display400. Render/simulation distance were four.

Default mouse bindings and raw input remained fixed. A live owned XTest camera
turn changed yaw0 to6.150001; after the disclosed pose was restored, F3 identified
the reachable vanilla floor and one normal Use placed stone at11,99,2, reducing
the supplied stack2 to1. Every later control used a fresh target/reach check,
one input and authoritative readback. Provided poses, floors, materials and
spectator viewpoints are setup; this does not claim new Survival construction
acceptance or normal camera operation from setup teleports.

## Occupied results

Times below are exact UTC input/readback timestamps, not inferred video times.
Each arming action was empty-hand Shift-Use; each dispatch was a separate
normal Use on a verified LOW floor lever. No second launch edge was sent.

| Family | Controller arm and dispatch | Independent arrival and rider |
| --- | --- | --- |
| Fixed Inverted, translated rear catch | Arm02:32:35.130 → armed1/READY/fault empty. Fresh high02:33:20.413 → RUNNING. | HOLD/fault empty02:33:38.314 with same seat `[-495243677,-1771224268,-1850078958,1177051704]`. At02:33:56.689 carriage returning0, physical socket HOME3.0000023166; in_use0 at02:34:07.122. |
| Classic Spinning | Arm02:35:37.266 → armed1/READY/fault empty. Fresh high02:36:16.817 → RUNNING. | HOLD/fault empty02:36:32.721 with same seat `[1957118250,-348568948,-1976192475,-256989698]`. |
| Launched Inverting Spinning (S2) | Arm02:41:27.094 → armed1/READY/fault empty. Fresh high02:42:07.477 → RUNNING. | HOLD/fault empty02:42:23.835 with same seat `[-1554269433,1052592423,-1775046701,1602341922]`. |

Actual exterior clips preserve [Fixed pull/return](fixed-occupied-exterior.mp4),
[Classic motion/hold](classic-occupied-exterior.mp4) and
[S2 motion/hold](s2-occupied-exterior.mp4). Selected held images show
[Fixed](20261127T023338.379319Z-state.png),
[Classic](20261127T023632.785030Z-state.png) and
[S2](20261127T024223.892579Z-state.png) with the seated Dev player.
The [rider record](../../ls05-fixed-20261122/README.md) supplies native editor,
restraint, exact mechanical work/body telemetry and final service outcomes.

## Native station coverage and explicit service

Classic's fixture dock was beyond the native twelve-block coverage at arrival.
The guard correctly waited for alignment. On the rider owner's request, the
observer normally placed a downstream native boarding station at38,238,-880
at02:38:14.743, then separately bound it by empty-hand Use at02:38:44.586.
Its vehicle NBT matched the actual Classic UUID beginning515655ae. The owner
then confirmed native Ready, normal restraint opening/Shift exit and controller
disarm with the parked lease retained. No native capture or position was forced.

Before S2 dispatch, the observer normally placed its midpoint native station at
86,238,-882 at02:40:01.666 and separately bound it at02:40:35.168. Its vehicle
NBT matched the actual S2 UUID beginningcd9053af. This corrected fixture station
coverage includes both the initial and final position. Binding opened the native
editor; Escape closed it without changing controls.

After the S2 held receipt the observer initially disconnected normally at
02:43:04.968. Native capture subsequently paused the LS owner, so the rider
owner requested two explicit service actions before unload. The same client
reconnected to the same endpoint; both client view and server list confirmed
the connection. Normal Shift-Use disarm at02:44:45.365 yielded armed0/INSPECTION;
separate normal Shift-Use rearm at02:44:58.550 yielded armed1/HOLD/fault empty.
The lever stayed HIGH and the original seat stayed attached: no launch replay.
The rider owner then confirmed native re-Capture → Ready/owner hold, normal
restraint opening and Shift exit with RootVehicle absent. This is deliberate
retained-owner service, not automatic authority restoration.

## Preservation and cleanup

[Input](input.jsonl) contains the33 completed original action records;
[readbacks](readbacks.jsonl) preserves63 original timestamped command/results.
The original raw before/after screenshots and logs remain in the persistent run.
Selected repository files have exact hashes in [manifest](manifest.sha256).
No credentials are copied. The observer changed only its authorized setup,
normal controls and viewpoints; it never stopped or saved the bridge server.

After the rider owner confirmed actual exit and released the observer, final
normal menu Disconnect at02:46:19.447 left only Dev connected. The
[cleanup receipt](cleanup.txt) records both owned client and Xvfb inactive,
MainPID0, Result success, exit0; Java387619 and the display400 socket are absent.
[Final memory](final-memory.txt) records peak2,627,121,152 bytes under the6GiB cap.
The server/world's separate save and shutdown remain in the rider owner's record.
