# Current Fourth Dimension Free Spin observer/operator

Status: PASS — occupied full circuit, normal reboarding with fresh dispatch, and observer cleanup complete.

Observer owner `/root/foundation` uses independent root `/mnt/NVME4/launchsystems-runs/ls01-freespin-observer-20261122`, player `Observer`, private display `:394`, and units `fd-ls-freeobserver-20261122-client.service` / `fd-ls-freeobserver-20261122-xvfb.service`. Java 21, heap 1536 MiB, Minecraft-only memory limit 6 GiB, four-chunk render/simulation distance. The ride owner `/root/rides` retains world/server root `/mnt/NVME4/launchsystems-runs/ls01-freespin-20261122`, endpoint `127.0.0.1:25761`, unit `fd-ls-freespin-20261122-server.service`.

The initial copied artifacts matched the Arrow current-FD observer: LS SHA-256 `67b2b08f571c78224be87b421010518c4cdc812362a1addf6b47ec68043b1d98`, FD facade SHA-256 `df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e`. Existing immutable dependency runtime paths are retained; game/argument/artifact paths and endpoint are independent. The previous Arrow observer was fully stopped and released before this run launched on the Free Spin owner's invitation.

The guarded input procedure validates the owned display, client unit, actual window PID/cgroup, geometry, default mouse bindings, and connection to the named shared server. Pose/mode commands are disclosed observer/operator setup only; construction, native UI and rider claims remain with the ride owner. Real source/receiver lever use occurs only after a ready cue, one expected input followed by authoritative readback.


## Preserved initial empty-transfer obstruction

Before any observer gameplay Use, the ride owner's first empty transfer faulted at the first turn. Both source and receiver reported `FAULT`, `Rollback during transfer; both sections remain reserved`, and the same exact chassis reservation `[-1739559508,223757886,-2136892887,-819792850]`. The observer preserved [wide view](freespin-empty-transfer-stop-overview.png) and [closer turn geometry](freespin-empty-transfer-stop-closeup.png). Only the disclosed spectator camera pose changed; no ride/controller/vehicle mutation was sent. The ride and bridge owners diagnosed this failed condition before the corrected attempt below. No occupied acceptance is claimed by this diagnostic.


The owner preserved the fault world with `save-all flush` and requested a clean server restart for a read-only collision diagnostic. Observer disconnected, then both owned client/Xvfb units stopped successfully with `MainPID=0` ([pause shutdown](diagnostic-pause-shutdown.txt)); the server reported only Dev. The observer remained stopped during diagnosis; display `:394` was reserved for the explicitly cued continuation.

The owner’s read-only collision diagnostic identified the fin blade’s next 5 cm sweep intersecting temporary stone that contributes a Sable collision shape. The owner preserved that faulted setup and constructed the same loop at `x + 64` with clearance; no rollback guard change was needed. The observer prepared a separate diagnostic LS artifact SHA-256 `046993cecf9837081023992c5fda5512fd06ae3163df9fde527e7bc8219eef6e`, leaving the original snapshot intact and FD unchanged. Corrected source/receiver controllers are `(69,98,4)` / `(85,98,28)` and chassis `006fd447-22ab-4bb8-801f-aab0e7a8fe20`. The observer stayed stopped until the owner confirmed the corrected empty circuit and occupied cue.


## Corrected occupied circuit

After the owner proved the corrected empty circuit, the exact diagnostic client joined at 20:09:26Z. Resource checks found 34 GiB available host memory and 161 GiB free on NVME4; the client had its own 6 GiB cap, Java 21 and 1536 MiB heap. Default use/attack bindings, raw input, four-chunk render/simulation distance, owned window/cgroup and live server connection were verified. The new live client accepted camera turn; subsequent reachable vanilla lever Use proved button input separately.

The rider normally boarded native carrier `57e0446c-0602-4e0d-a48a-3e46e8875e89` and closed restraints through the owner's client. Observer recorded the same native `create:seat` attachment `[1869160461,583419507,-1509977475,-1493518096]` throughout both transfers. The corrected chassis reservation is `[7328839,581651384,-2145408336,-408355296]` (`006fd447-22ab-4bb8-801f-aab0e7a8fe20`).

- Source normal Use at 20:09:51Z lowered lever `(69,99,4)`; authoritative readback stayed `READY_EXIT` with the same rider. A distinct Use at 20:10:05Z raised it, producing `EXITING` with no fault and the same seat attachment. [Departure video](freespin-occupied-source.mp4).
- Receiver reached `READY_EXIT`, `boarding_ready=1`, no fault, same chassis lease and same seat. The source reservation became empty. [Occupied native carrier closeup](freespin-occupied-receiver-closeup.png).
- Receiver normal Use at 20:11:02Z lowered `(85,99,28)` without departure; fresh Use at 20:11:11Z raised it and produced `EXITING`, no fault and the same seat attachment. [Return video](freespin-occupied-return.mp4) shows the [occupied return curve](freespin-occupied-return-curve.png).
- Source recaptured the whole vehicle at 20:11:44Z: `READY_EXIT`, `boarding_ready=1`, no fault and the same reservation. Receiver reservation cleared. [Returned rider and vehicle](freespin-occupied-source-return.png). Owner telemetry separately verifies all four native carriers captured.

Disclosed operator setup feet were `(69.5,97,2.5)` and `(85.5,97,26.5)`, yaw 0, pitch -14.8415, Survival with empty hand. F3 confirmed each exact reachable floor lever before clicking. Spectator poses changed only Observer for rendered motion. No Dev teleport, vehicle/controller edit, fabricated mount or direct redstone mutation was used by the observer.


## Normal exit/reboard and fresh dispatch

The owner normally opened restraints, used Shift to exit (no RootVehicle/zero native occupants), then used the seat again and closed restraints. Observer independently saw the fresh seat attachment `[356842126,-530889493,-1732385322,-1525747710]` while the same chassis remained held. Source returned to `READY_EXIT`; no automatic departure replayed while the lever remained high.

Normal source Use at 20:15:44Z lowered the lever and left `READY_EXIT`, the same chassis reservation and new seat attachment intact. Separate Use at 20:15:53Z raised it, producing `EXITING` with no fault and that same new seat. [Fresh-dispatch video](freespin-reboard-fresh-dispatch.mp4) and [occupied transit frame](freespin-reboard-transit.png) preserve the result. At 20:16:20Z the receiver held `READY_EXIT`, `boarding_ready=1`, no fault and the same new rider attachment; source lease was empty. [Final held vehicle](freespin-reboard-final-held.png). Owner telemetry confirms all four native carriers captured.

## Cleanup and retained evidence

After the ride owner released the observer and video completed, the owned client stopped before its Xvfb. Both units are `inactive/dead`, `MainPID=0`, `Result=success`; both recorded process IDs were absent. The shared server then listed only Dev. See [shutdown](shutdown.txt), [memory](corrected-memory.txt), [input events](input.jsonl), [authoritative readbacks](observer-rcon.jsonl), and [run ownership/artifact manifest](run.json). Client peak was 2,475,978,752 bytes, below the 6 GiB cap. The ride owner retained the exact world/server for final native unload, save and shutdown; no server lifecycle action was taken by this observer.

The original obstructed empty-transfer setup remains diagnostic evidence. The corrected occupied result completes this observer assignment without changing rollback guards or reopening accepted sibling cases. Source, runtime artifacts and complete raw frame logs remain at the persistent run root above.
