# Current Fourth Dimension occupied ride observer/operator

Status: PASS — current-FD original Arrow occupied multiplayer operator/observer case. The bridge owner separately records construction, normal exit/reboarding and the same-world restart.

Independent observer owner `/root/foundation` uses player `Observer`, display `:394`, root `/mnt/NVME4/launchsystems-runs/ls01-fd-observer-20261122`, client unit `fd-ls-fdobserver-20261122-client.service` and display unit `fd-ls-fdobserver-20261122-xvfb.service`. Java 21, heap 1536 MiB, Minecraft-only memory limit 6 GiB, render/simulation distance 4. The bridge owner retains server/world `/mnt/NVME4/launchsystems-runs/bridge-20261122/arrow`, endpoint `127.0.0.1:25751`, unit `fd-ls-arrow-20261122-server.service`.

The observer uses independent copied artifacts: Launch Systems SHA-256 `67b2b08f571c78224be87b421010518c4cdc812362a1addf6b47ec68043b1d98`, current Fourth Dimension facade SHA-256 `df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e`. Direct launch arguments retain the bridge's dependency snapshot with only independent game/argument/artifact paths and username changed. Initial copying also relocated two NeoForge runtime paths incorrectly; the launcher failed before loading Minecraft. Those existing artifact paths were restored and the corrected launcher joined successfully at 19:32:37 UTC. This was a launcher-path repair, not a gameplay change.

The client logs show `Fourth Dimension owns repair`, confirming that the standalone LS Veil redirect yields to the current FD repair. Both client/server logs confirm the join. The independent window PID/cgroup, exact geometry, default mouse bindings and raw-input setting were verified. A normal camera turn changed the rendered view in this new client. The same guarded input helper used in the ordinary observer record checks the owned client/display and the named shared server's active connection without claiming ownership of the server.

The bridge owner authorized disclosed spectator/setup positioning and later source/receiver lever operation after a ready cue. Each actual Use is separate, has an expected result, and requires authoritative readback. The server/world/controller configuration remains bridge-owned. Runtime `run.json`, `evidence/input.jsonl`, `evidence/observer-rcon.jsonl`, screenshots and videos retain the complete trace. Selected logs, screenshots and videos are copied beside this record.

## Occupied Arrow result

At 19:35:22 UTC the [close-up](arrow-occupied-closeup.png) shows Dev in the original Arrow's native carrier and restraint. The owner independently recorded carrier mass increasing from 4.0 to 4.8 and native Ready/closed-restraint/Owner-hold state. Observer's server readback confirmed `create:seat` root UUID `d0141ed7-27a4-44bd-98b4-e2b05cb910ec`.

Both operator poses were the same proven reachable poses used in the ordinary run, with F3 identifying each exact floor lever before Use. At 19:35:50 the real source low edge retained `READY_EXIT`, the same reservation and seat. At 19:35:58 the separate real rising edge entered `EXITING`. The receiver subsequently reached `READY_EXIT` with no fault, source reservation empty, and the same chassis reservation. [Source recording](arrow-occupied-source.mp4) preserves the transition; a natural storm began during this transfer, so the bridge owner cleared weather before return for visibility.

At 19:37:01 the receiver low edge retained its occupied hold. The separate rising edge at 19:37:10 entered `EXITING`. The [return recording](arrow-occupied-return.mp4) and [motion frame](arrow-return-motion.png) show the native car moving through the track with Dev still attached. Source returned to `READY_EXIT` with no fault, the receiver reservation cleared, and the same chassis `caeeb5a5-ba62-4f84-8ada-aa36cf550406` was reserved. Exact reservation NBT remained `[-890325595,-1167962236,-1965381066,-816511994]`. The rider seat UUID stayed unchanged throughout this occupied roundtrip.

The original Arrow has two wide carriers and an overhead crossbeam. A distant oblique overview initially looked unusually long; the closer occupied view matches this native geometry. No visual defect was asserted from that distant view.


## Reboarding and cleanup

After the owner used the native UI to open restraints, normally exited, reboarded and closed restraints, a final source low edge at 19:41:01 UTC retained `READY_EXIT` with a fresh native Create seat on the same carrier/chassis. The separate rising edge at 19:41:10 entered `EXITING`, with no fault and Dev still attached. The bridge owner explicitly accepted the final receiving hold and saved-world restart; no extra observer input was needed.

Observer then disconnected and both owned client/display units stopped successfully: inactive/dead, `MainPID=0`, `Result=success`, as recorded in [shutdown.txt](shutdown.txt). The server list contained only Dev. The bridge owner retained its server and world; no server lifecycle action was sent by the observer. Complete runtime data and the initial launcher-path diagnostic remain on persistent NVME4 storage. A separate Free Spin observer run uses its own game directory.
