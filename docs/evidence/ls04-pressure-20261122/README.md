# LS401–LS403 pressure qualification — 2026-11-22

Status: **PASS — LS401–LS403 complete**. Owner `/root/foundation`, branch `codex/launch-systems-0-4-0`, starting HEAD `974c2c8`, checkout `/home/braydon/Documents/Coasters: Launch Systems`. Runtime root `/mnt/NVME4/launchsystems-runs/ls04-pressure-20261122`; reserved display `:398`, game/RCON ports `25791`/`25792` confirmed free before preparation. Separate `build/` and `project-cache/` paths. Java 21; intended client heap 1536 MiB and Minecraft-only 6 GiB unit.

Accepted carryforward: distinct finite models and native pressure controllers dev.29; pressure shaft-loss dev.30; separate-process retained catch recovery dev.31/34/35; physical return shaft-loss dev.32/33; loaded guide loss dev.53; ordinary hydraulic/pneumatic active release and subsequent restart dev.62; unchanged rendered shared Ponder dev.65. Current-profile native/shared contract checks and actual occupied pressure operation/service are closed below.

Changes are limited to exposing live pressure, hydraulic closed-loop oil inventory, physical phase and readiness in cabinet status, plus accurate pressure operating documentation. No force, charge, discharge or reset rule is changed by that guidance work. Upstream hydraulic labels were re-audited in [pressure mechanics](../../PRESSURE-MECHANICS.md); no dependency was added or upgraded.


## Focused current pressure contracts

`StoredEnergyTest` passed **4/4**. The focused `coasterslaunchsystems_pressure` wrapper reuses the accepted loaded controller fixtures without unrelated cable cases. Current Arrow hydraulic/pneumatic charging-shaft-loss and guide-loss passed **4/4**; after release, separate hydraulic/pneumatic return-shaft interruption cases passed **2/2**. Both server processes saved and exited successfully. The latter used `-PlsCableReturnShaftLoss=arrow`; both selected `-PlsPackaged -PlsDependencyProfile=fd-current -PlsTestNamespaces=coasterslaunchsystems_pressure` and the exact API1 FD artifact. Own native-build/native-project-cache and distinct native-game/return-native-game paths prevented concurrent output/world sharing.

The return preparation first caught a concurrent selector spelling error, then an intermediate shared adapter reflection read of a nonpublic `restored` field. No catch engaged in that failed runtime. Bytecode inspection identified the exact field access; the owner removed it and verified the public suspended-state/whole-consist brake contract. The corrected two cases passed. Preserve those diagnostic logs without treating them as pressure mechanics defects.

## Real recipe and status workshop

The separate workshop used a supplied stone platform and ingredients; no pressure cabinet was granted directly. Normal Survival Use placed a crafting table, proving reachable vanilla button input separately from a measured camera turn (0→3.15 degrees). Through its normal recipe book, the player crafted one hydraulic cabinet consuming 4 iron sheets, 2 brass sheets, 1 shaft, 1 precision mechanism and 1 copper block, then one pneumatic cabinet consuming the remaining matching ingredients and 1 light-blue concrete. Inventory readback contained exactly the two cabinets and no remaining ingredients.

Both crafted cabinets were placed through normal Use. Empty-hand status rendered hydraulic pressure 250 with 0 charged/2 returned closed-loop oil and pneumatic pressure 10/expansion guidance, both uncharged and disarmed. See hydraulic/pneumatic recipe and status frames and authoritative readbacks. Camera/input bindings remained raw-input/default rightUse/leftAttack. Runtime used the exact workshop artifact manifest. Server had 4-chunk distances; client render 4, while copied simulation 4 was below the vanilla minimum and logged validation fallback. This bounded workshop stayed safely below 6 GiB; later clients use valid simulation 5.

The workshop client and display stopped, and the server saved all dimensions and exited. Its saved world remains at server/workshop for provenance; the ride world will be a separately handed-off stopped snapshot, never a live copy.

## Updated teaching and prepared hydraulic rig

The pressure player artifact is `ee80b2125f9a27c208c43226122093eac81bf2fb2070b8d6f32ddced6058c6d0`, built after the shared Ponder corrections. Normal inventory hover/hold opened the hydraulic cable guide. `pressure-ponder.png` shows the complete localized pump/compressor, returned oil, expansion and shaft-loss text; the video also records guide/pull/physical-return/service teaching. The introductory vanilla movement tutorial overlaps part of the service pane, but does not cover the complete pressure pane.

The clean prelaunch snapshot was copied only after its owner's client/server stopped and all dimensions saved. It preserves the actual Survival-built 44-block straight track, continuous guide, home cabinet/fin, downstream fin bank, ordinary chassis and installed socket/brake/seat. See `rig-provenance.json`. In this separate copy, normal sneak-wrench removed the disarmed flywheel; normal placement installed the hydraulic cabinet. Span Tool clicks selected the original anchors, bound the new cabinet and linked its new identity. Normal Shift-Use armed each module. The supplied 64 RPM shaft filled the finite accumulator, and normal status displayed the full charge. Normal Survival Shift-Use placed the unpowered lever on the controller. No catch or vehicle motion was injected.

At that preparation checkpoint, dispatch waited for the shared rig owner's return-clearance diagnosis. The shared occupied flywheel proof received one normal high edge from this agent's separate Observer client; its rider remained seated through the downstream HOLD. The observer client/display stopped cleanly, and that result does not claim physical return completion.

## Housing correction handoff

The shared native carriage had been stopped by effectively zero lateral clearance against the full-cube home fin housing. The owner corrected collision and rendered cabinet housing to the same symmetric 1/16-block inset. Its saved-world inspected return then passed. The pressure continuation consumes corrected artifact `4ec667333242a50b77d8a2555003ae787d7c96fb2bf7a8506b81b561c05b3a5e`; native catch forces, ballast, pressure and release/return rules are unchanged. The prepared hydraulic world was saved and all owned runtimes stopped before rebuilding. On reload, the cable cabinet correctly required explicit rearming. Normal controller disarm, cabinet rearm and controller rearm restored READY; no saved enable state bypass was introduced.

## Occupied pressure cycles and normal service — PASS

Both rides used the corrected frozen artifact, ordinary native chassis `45605f08-e2d1-459c-a457-3ec1f84ad5af`, and the unchanged 44-block track/guide construction. The pneumatic world is a separate copy of the same stopped prelaunch snapshot; neither test moved a vehicle back by command. Supplied items, shafts, operator platforms and disclosed aim/position commands are setup. Cabinet removal/placement, binding/linking/arming, lever placement, seat boarding/exit, operator dispatch and controller service were normal client actions with authoritative readbacks after each action. Camera movement and reachable normal Use were demonstrated in each client configuration.

A second real player, Observer, operated the lever in Survival while Dev remained seated. Its owner and independent exterior recordings are preserved in [bridge observer evidence](../0.3/cable-current-20261122/pressure-observer/README.md). Each dispatch was one verified low-to-high edge. No rider teleport or movement command was sent during a run. Each native chassis increased from mass 4.5 to 5.3 on boarding and retained one exact `create:seat` identity throughout the sampled run.

| Observation | Hydraulic | Pneumatic |
| --- | --- | --- |
| Dispatch UTC | 21:21:33 | 21:25:32 |
| Starting finite work | 778.7698 (full) | 843.9776 of 1636.5858 (51.6%, naturally partial) |
| Lowest sampled work | 630.2296 | 696.0137 |
| Observed chassis peak speed | 2.2815 m/s | 2.2783 m/s |
| Chassis stop Z | 22.053938 | 22.057777 |
| Carriage start Z → returned Z | 8.291666 → 8.291665 | 8.291666 → 8.291665 |
| Work after shaft recharge | 778.7698 | 1636.5858 |
| Controller phases | READY → RUNNING → HOLD, no fault | READY → RUNNING → HOLD, no fault |

Each carriage released, entered physical RETURNING, and returned with native velocity toward home (about 0.966 m/s). Actual home then cleared `ls_cable_returning` and `ls_cable_in_use`. The receiver store resumed charging only after this physical cycle. The pneumatic pre-arm screenshot was initially labelled “full” in the action expectation; the actual readback was 802.8403 and the installed catch stopped charging at 843.9776. The accepted result is explicitly a **partial-charge** launch, not a full receiver claim.

After each HOME result, normal Shift exited the seat, removing RootVehicle and returning chassis mass to 4.5. Normal empty-hand Shift-Use on the controller disarmed it. Cabinet journal fields showed `ls_cable_installed=0`, no carriage UUID, and no in-use/returning flag. The proven `sable info` query changed from two bodies to only the original stopped train; the controller retained that train's exact UUID reservation. The reviewed normal `retireCable`/rig close path also removes the carriage loading ticket. No ticket-specific runtime query is claimed. A diagnostic `lsdiagnose` command was unavailable in this profile; the authoritative Sable body/journal readbacks establish removal. This normal HOME service complements the shared current Arrow saved-return/active-release proofs and accepted pressure restart evidence.

The corrected inset cabinet housings were visually inspected with the hydraulic and pneumatic full-recharge status frames. Their displayed pressure/oil/expansion information and models remained readable and matched actual behavior. Full current pressure text was already rendered through the shared Ponder scene; the housing-only correction does not invalidate its text proof.

## Closure and cleanup

LS401 combines the closed-loop inventory invariant, accepted finite hydraulic pressure/partial charge/interruption/reset records, current loaded pressure checks, upstream audit and actual occupied hydraulic return/recharge/service. LS402 combines the distinct pressure-volume receiver model and threshold, current loaded checks, actual partial-charge pneumatic ride and full recharge. LS403 combines current loaded guide/shaft interruption cases, the current shared exact-owner brake/catch/recovery checks, both retained occupied seats, normal physical return/service, real recipes and rendered pressure teaching. No additional matrix or unrelated family scope was introduced.

All pressure-owned client/server/Xvfb units are inactive with MainPID 0 and success; see `final-shutdown.txt`. Final server save completed all dimensions at **2026-11-22 21:27:09 UTC**. Bridge Observer397 also disconnected and stopped at 21:26:33 UTC. Its expected requested SIGTERM status is recorded by its owner. No runtime was left behind. The workshop, hydraulic `server/pressure-rig`, pneumatic `server/pneumatic-rig`, frozen JARs and raw media remain on persistent storage under the recorded run root. `final-memory.txt` records the last client below its 6 GiB cap; all game heaps used 1536 MiB, Java 21, private displays and separate build/project-cache paths.
