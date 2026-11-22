# LS106 ordinary occupied ride: independent observer/operator

Status: PASS — ordinary occupied multiplayer operator/observer and reconnect case. Combine with the ride owner’s construction evidence and retained interruption/restart evidence; this record does not reopen those siblings.

## Ownership and fixed artifact

- World/server owner: `/root/rides`; endpoint `127.0.0.1:25741`, unit `fd-ls-rides-20261122-server.service`, persistent root `/home/braydon/.local/state/launchsystems-runs/ls01-rides-20261122`.
- Observer/operator owner: `/root/foundation`; player `Observer`; private display `:394`; independent client and Xvfb units `fd-ls-observer-20261122-client.service` and `fd-ls-observer-20261122-xvfb.service`.
- Observer persistent root: `/mnt/NVME4/launchsystems-runs/ls01-observer-20261122`; Java 21, 1536 MiB heap, 6 GiB Minecraft-only unit; four-chunk render and simulation distances.
- Fixed packaged Launch Systems JAR SHA-256: `8bb78ede6decb007b53ace87c265923463cd3c3e77aa5cee59d0040829839c67`; same current-core standalone profile as the ride owner's client/server. Fourth Dimension is absent from this ordinary-Coasters case.
- The observer launcher copies the owner's generated direct Java launcher arguments, substitutes only the independent packaged JAR/game directory/argument paths, and adds `--username Observer`.

## Setup and input bounds

Observer joined the existing world; both client/server logs confirmed the join. The owner authorized spectator mode and camera positioning through RCON. These disclosed setup commands establish a useful view and are not evidence of player construction or normal camera movement.

Normal camera motion was separately verified in the same live client with a relative XTest device turn through the existing Fourth Dimension private-client helper, unchanged default mouse bindings, and `rawMouseInput:true`. The before/after frames show a visible camera turn. The runtime-only wrapper keeps the helper's own-display, own-client cgroup, window PID, geometry and options checks, and replaces only the connection check with a read-only check of the explicitly shared server's unit, working directory, cgroup, and connected-player list. It does not claim ownership of that server or use its start/stop commands.

The ride owner then requested an actual second operator while `Dev` remains seated: only the source lever at `(5,99,4)` and receiving lever at `(21,99,28)`, after an explicit ready cue. Controller configuration and server/world lifecycle remain with the ride owner. Each state-changing click requires a named expected result, one real Use input, and authoritative state readback before continuing.

## Evidence locations

The persistent run's `evidence/input.jsonl` records expected outcomes, connection, exact window geometry, motion device, and before/after screenshots. `evidence/observer-rcon.jsonl` records disclosed setup and read-only observation commands without credentials. `run.json` records the ownership and artifact identity. Selected screenshots, videos, input and readback logs are copied beside this record. The complete runtime remains preserved at the path above.

## Observed result

The source and receiver levers were each positively identified in F3 at the owner's proven operator poses: source feet `(5.5,97,2.5)` and receiver feet `(21.5,97,26.5)`, yaw `0`, pitch `-14.8415`. Both use empty hand, default right-click, and the same live client configuration that already passed camera turning. These disclosed teleports position the supporting operator; the ride owner's record owns the Survival construction claim.

At 19:24:30 UTC, a real source low edge changed the lever to false and retained `READY_EXIT`, `boarding_ready=1`, no fault, and the seated rider. At 19:24:45, the separate real rising edge entered `EXITING`; the receiver progressed through `RECEIVING` and `RUNNING` to `READY_EXIT`. The observer could see Dev seated on the retained cart. At 19:25:58, a receiver low edge again retained the hold; at 19:26:08, its separate rising edge started the occupied return. The [return-motion frame](occupied-return-motion.png) and [recording](occupied-return.mp4) show the rider rounding the native track. Source returned to `READY_EXIT`, destination reservation cleared, and Dev retained seat `d8784d6b-5547-447c-97df-e95f35046fb5` throughout that occupied roundtrip.

The chassis identity stayed `35803c55-4bfd-46d9-b419-c9c6c1563f88`. Its exact reservation NBT is `[897596501,1274889945,-1273378362,-1051312248]`; the receiving and source controllers handed this same identity back and forth without a second reservation after capture.

After the owner demonstrated normal exit and reboard, Dev disconnected through the actual game menu at 19:27:38. The observer stayed connected and recorded only Observer online, the same stationary cart, source `READY_EXIT`, identical reservation, no fault, and a still-powered source lever without replay. The [absence frame](reconnect-dev-absent.png), [return frame](reconnect-current.png), and [watch log](reconnect-watch.jsonl) record this boundary. Dev reconnected and normally reboarded; Create produced a new seat entity `bc750b39-4acd-43b3-852f-ecca7ccf22d1` while the chassis stayed unchanged. This expected seat recreation is distinct from vehicle/lease identity.

At 19:29:18, the post-reconnect real low edge still left the ride held with the same lease. At 19:29:27, the separate rising edge entered `EXITING`. Receiver reached `READY_EXIT`, no fault, held the same chassis reservation, and Dev stayed seated; source reservation was empty. The [fresh-dispatch recording](reconnect-fresh-dispatch.mp4) and [final hold](reconnect-final-held.png) preserve that result. No further operator inputs were sent.

## Cleanup

Both owned observer services stopped successfully: client and Xvfb are inactive/dead with `MainPID=0`, recorded in [shutdown.txt](shutdown.txt). The shared server subsequently reported only Dev online. The ride owner retained its server/world lifecycle and saves it in its separate record. All observer game data, logs, screenshots and videos remain on persistent NVME4 storage. A later Arrow observer uses a new independent game directory and artifact snapshot.
