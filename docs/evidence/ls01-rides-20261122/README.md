# 0.1 rendered ride qualification — 2026-11-22

Owner: `/root/rides`; worktree `/home/braydon/Documents/Coasters: Launch Systems`, branch `codex/launch-systems-0-4-0`, starting HEAD `1ac51d7`. Existing changes preserved. The ordinary ride and multiplayer observations below are complete; distinct FD family outcomes are recorded separately.

## Ordinary run

Owned root `/home/braydon/.local/state/launchsystems-runs/ls01-rides-20261122`, display `:392`, server game/RCON ports `25741`/`25742`, units `fd-ls-rides-20261122-{server,client,xvfb}.service`. Java 21; client heap 1536M and Minecraft-only 6 GiB limit; server heap 2048M, limit 4 GiB. Current-core standalone profile: NeoForge 21.1.256, Sable 2.0.6, Coasters 0.1.5, Create 6.0.10. No Fourth Dimension JAR loaded. Launchers generated from current source with the foundation agent's standalone Veil repair.

The host had 43 GiB available and 250 GiB disk free before preparation. Cgroup ancestors had unlimited `memory.max`. World/game/build/evidence are on persistent XFS, outside `/tmp`. Preparation and runtime logs, run.json, input.jsonl, construction-readbacks.jsonl and per-action before/after screenshots remain under the run root. RCON credentials remain only in private server.properties.

The private helper proved camera turning (-90° to -80.85° yaw) and a reachable vanilla cobblestone placement (64 to 63 items) before mod interactions. Raw Input on, right-click Use, left-click Attack; input configuration stays fixed. All construction actions below use real connected `Dev` in Survival mode. Commands supplied a stone work platform/supports, item kit, upstream Create power sources and disclosed player setup poses; they did not place the functional rail, vehicle, interfaces, LS modules or controllers. This is a controlled kit demonstration, not resource gathering.

### Native track construction finding

Four floor-up anchors at A=(0,100,0), B=(0,100,32), C=(16,100,32), D=(16,100,0) were placed through normal Use, consuming the kit. The first naive B-C connection auto-smoothed the neighboring straight. That empty failed layout was removed with disclosed setup commands and rebuilt through the same Survival placement actions.

The installed Coasters 0.1.5 language resource explicitly says `Track smoothing can be disabled by holding left alt during placement` (`ponder_track_placement.text_5`). Holding Left Alt for the final Track click preserves prior straight geometry. The corrected A-B, C-D, B-C, D-A connections consumed 96 Track items. Readback confirms all normals world-up, straight axes ±Z and the two U-turns tangent to those sections. No curve handles or geometry were injected.

### Vehicle and equipment

Normal mid-track cart placement at A+4m consumed one Coaster Cart. Chassis UUID `35803c55-4bfd-46d9-b419-c9c6c1563f88`, native bearing local block `(20481032,128,20481032)`. Normal Survival block placement attached the tire strip below, friction fin east/below, and one real Create red seat above. Each item was consumed and each local block was verified. Empty native mass after these attachments: 3.5.

Tire/fin modules are placed through normal right-click against provided powered shafts, all on transverse X axes; actual speed is +64 RPM for tires and -64 RPM for fin modules. Two Survival-placed controllers at (5,98,4) and (21,98,28) control opposite 32-block straights. All 54 modules were individually bound with the Span Tool, linked to their controller, and armed by normal Survival input. Exit tires used the documented offhand Redstone Torch role. Both controllers were mutually destination-linked and had station capture enabled with the offhand Clock. Two normally placed levers provide distinct low/high dispatch edges. All readbacks showed valid bindings, expected controller UUIDs and no fault.

## Reused evidence and scope

Reuse dev.69–72 interaction/Ponder/tool binding, native tire/fin/magnetic/controller, station/circuit/receiver-recovery, dev.65 basic packaged late join, and dev.66–67 actual active chunk unavailability. Current FD bridge changes require the specifically affected provider paths, owned jointly with the bridge agent.

The ordinary normal construction, repeated empty/occupied operation, native hold, exit/reboarding, fresh dispatch and actual multiplayer ownership reconnect cases are completed below. The distinct current Arrow/Free Spin outcomes are owned in their separate records; this record does not close the entire 0.1 milestone by itself. Historical caveats alone do not reopen accepted siblings.

### Completed ordinary operation

Packaged JAR SHA-256: `8bb78ede6decb007b53ace87c265923463cd3c3e77aa5cee59d0040829839c67`. The source controller UUID is `[1521362031,994068143,-1110964091,-1505735885]`, receiver `[-1106271071,-1240383199,-1083511986,333467518]`. Native chassis identity stayed unchanged throughout.

The first normal source lever edge launched the empty cart from A+4 m, then the fin bank stopped and retained it at body z=11.9475 with READY_EXIT, boarding readiness and no fault. A separate low/high edge transferred it over the far U-turn to the receiver: source became HOLD with an empty reservation, receiver READY_EXIT/boarding_ready=1, body `(16.7857,100.7768,20.9361)`, longitudinal velocity effectively zero. The receiver's fresh edge returned it over the near U-turn to source READY_EXIT at z=12.0352. Both complete transfer paths were native movement, with no cart pose, velocity, controller phase or reservation injection.

At 19:23:59 UTC Dev boarded the held Create seat with normal right-click. RootVehicle readback identifies `create:seat` UUID `d8784d6b-5547-447c-97df-e95f35046fb5` (authoritative raw integer UUID is retained in the console log); native mass increased from 3.5 to 4.3. The separate actual client Observer joined and operated the source lever with normal Use: low edge retained READY_EXIT, high edge at 19:24:45 UTC produced EXITING/RECEIVING. Dev remained the same real seat passenger during traversal at approximately 2.67 m/s, the U-turn, and receiving stop. The receiver reached READY_EXIT/boarding_ready=1 with no fault at `(16.7326,101.2137,20.9479)`, held at effectively zero longitudinal speed through subsequent samples. Observer rendered the seated cart from its independent client; see the observer evidence record.

Player setup poses for aiming, supplied standing blocks and item kits are disclosed above. While occupied, Dev was never teleported; a second actual Survival operator sent the control inputs. Full per-action snapshots and native readbacks remain in the owned runtime evidence directory.

At 19:26:08 UTC Observer sent a distinct receiver low/high request and the occupied cart returned to source READY_EXIT. Normal Shift exit at 19:26:44 removed Dev's RootVehicle and returned native mass to 3.5. Normal Use at 19:27:00 reboarded the same chassis (Create creates a new ephemeral seat entity on each boarding).

At 19:27:38 UTC Dev used the real game-menu Disconnect button. The server listed only Observer; source remained READY_EXIT, with the exact original chassis reservation and high lever. The independent observer saw the empty, stationary retained cart. At 19:28:06 Dev rejoined the same endpoint through Direct Connection. No dispatch replay occurred: phase and reservation remained unchanged, and Dev returned standing rather than automatically seated. Normal Use at 19:28:45 boarded again. Observer's new low/high request at 19:29:27 produced another occupied transfer, concluding at receiver READY_EXIT/boarding_ready=1 with the same chassis reservation and no fault. Normal Shift exit at 19:30:08 succeeded.

This closes ordinary rendered Survival construction, repeated empty/occupied departure and traversal, whole-cart friction capture and hold, passenger exit/reboarding, fresh dispatch, and real two-client reconnect/ownership observations. It combines with accepted native loaded-consist, rollback, power/support, changed-geometry and active-chunk interruption evidence; it does not replace the pending distinct FD family outcomes.

Selected screenshots and compressed exact input/construction/console/telemetry records are retained alongside this README. Independent external motion video and multiplayer readbacks are in [observer evidence](../ls01-observer-20261122/README.md). Full original screenshots and the saved world remain at the runtime path above.

Cleanup: both client and private Xvfb exited (MainPID=0). `save-all flush` returned `Saved the game` before the RCON `stop` command. The server completed its slow host-filesystem RegionFile write and logged `All dimensions are saved` at 19:37:31 UTC (seven minutes after stop). All three owned units then verified inactive, MainPID=0, Result=success; see `shutdown.log`. No forced termination was used. Ordinary outcome and cleanup are complete.
