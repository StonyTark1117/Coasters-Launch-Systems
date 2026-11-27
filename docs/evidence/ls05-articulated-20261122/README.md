# LS502 articulated family qualification — 2026-11-22

Owner: `/root/rides`. Status: native and occupied acceptance complete; clean runtime shutdown verified below.

Persistent runtime: `/mnt/NVME4/launchsystems-runs/ls05-articulated-20261122`. LS checkout: `/home/braydon/Documents/Coasters: Launch Systems`, branch `codex/launch-systems-0-4-0`, starting HEAD `2d625b9407023be5989b1d2093e91ac1bac1d4cf`; no sibling FD checkout edits. Java21, separate build/cache/world paths. Client uses private display396, endpoint25811, 1536M heap and6GiB process scope.

FD dependency is the selected family API1 overlay at `/mnt/NVME4/launchsystems-runs/ls05-fd-family-20261122/fourthdimension-family-api1.jar`, SHA256 `3dbbc7a77e1b0cd02b3e5fa3a69e4b4823c5c5a3befca6f0e1e35d72381b7fba`. The current unrelated primary FD1.5 working tree was not built or modified. Frozen occupied-client LS JAR SHA256 `00276d3d22c5b40ec26e5dd611f0d8ebf1269166011ca97f8fd66aab6a013f92`.

## Native cases and corrections

`ArticulatedFamilyGameTests` uses native family creation, native joint bodies and Create seat passengers. It asserts loaded mass includes every native child once, local force under an exact reservation, native restraint/posture/yaw guards, whole loaded stop, and no replay from held input. Flying uses separate body and leg restraint operations and native prone changeover. New Generation Flying authors a native route yaw/posture profile, includes its intermediate yaw deck in mass/availability, preserves native yaw and posture, and denies dispatch/force with a missing deck.

The corrected interface layout is physically supported by a fore spine for hanging bodies. A discovered production bug activated force at the bearing station rather than the shifted hardware station; the fix projects actual tire/fin/plate/shoe points onto the accepted rail and rejects an arm missing that rail. Controller READY and stop-bank arrival use those interface stations while native bearing and complete body bounds still determine section occupancy and stopping envelope. Four affected fore-fin arrival cases requalify that change. Centered Axis/Dive tire evidence remains accepted.

Fixture corrections were specific: Axis controller moved below its native sweep; linear settings are authored before binding/arming; separate skid and eddy trim cases preserve the intended overlapping-owner veto; cleanup removes only owned native vehicle/rail records before reusing a lane. Simultaneous unlinked trim contact was correctly rejected with `Overlapping modules claim this consist; separate their contact zones`. No safety guard was weakened.

`flying_power` passed with four native riders. Actual linked Creative Motors were stopped during TO_PRONE; native fault became `Flying power lost; retained, explicitly resume after repair`, with INTERRUPTED_TRANSITION and riders retained. Restoring motors restored accessory supply but did not clear the fault, restart posture, or grant dispatch. This tests the shared `FlyingRuntime.powered` predicate consumed by New Generation Flying; its separate loaded posture/yaw behavior is covered by that family's own positive cases.

`PoweredYawBoundaryGameTests` passed with two native riders and the real native Chain/governor lease. Provider reserve, dispatch and capture requests were refused; saved authority and governor state were unchanged and riders retained. The exclusion is the mandatory whole-circuit native governor's ownership of speed, dwell, reversal and capture, not absence of a callback or a guessed mounting problem.

## Occupied client procedure

Fixtures supply track, native vehicles, real hardware, kinetic sources and linked local controllers. Position/material commands are disclosed setup, not Survival construction evidence;0.5 permits setup fixtures and reuses0.1 construction acceptance. Each claimed boarding/restraint/posture/lever operation uses actual input on the private client, with one action followed by native state readback. Default mouse bindings and raw input remained fixed. Camera turn and reachable vanilla placement passed in this same client. A missing temporary boarding floor caused one disclosed setup fall/death before the first ride; it was corrected and normal respawn/boarding followed.

Raw input receipts/screenshots and RCON readbacks are in the persistent runtime `evidence/`; curated input and native state receipts are included beside this report.

## Loaded native measurements

| Family | Tire/fin work | LSM work | LIM work | Skid heat | Eddy heat |
|---|---:|---:|---:|---:|---:|
| suspended | 104.442205 | 67.989930 | 71.101225 | 94.493200 | 86.152185 |
| axis | 44.868830 | 28.625185 | 30.688796 | 36.363495 | 22.286654 |
| pipeline | 90.277847 | 55.438843 | 55.781998 | 91.716028 | 81.690611 |
| dive | 91.767751 | 52.802423 | 53.105715 | 92.358392 | 80.815816 |
| flying | 101.758103 | 65.751628 | 68.814337 | 90.919951 | 82.735336 |
| new_generation_flying | 35.682266 | 44.425340 | 47.680303 | 26.520595 | 21.014951 |

All forces act through native Sable bodies; positive-work samples are per-step impulse·velocity sums. Heat is measured cabinet dissipated work before cooling. Every cell retained native seated passengers. Tire cases include whole-body fin stop, held input refusal and native maintenance veto; NG additionally proves intermediate deck availability.

The six final skid cases add a face-adjacent vertical connector above the shoe; their revised native mass and heat passed6/6. LSM already connects directly to the tire strip and needed no extra brace. The focused New Generation Flying case passed both unavailable-UUID and null intermediate-deck vetoes1/1.

Nov26 continuation preserves the cleanly saved Nov22 world. Only remaining occupied fixtures use `ls05-articulated-resume-20261126` with the identical frozen JARs. Default mouse bindings, raw input,4-chunk render/simulation distance and6GiB/1536M limits were verified again. Camera rotation and a reachable normal vanilla placement passed before any mod input.

## Occupied results

- Suspended: normal native boarding and restraint close, then observer fresh lever input at 2026-11-22T22:13:41Z. Same native seat `23fbd75e-a874-44b8-9ea4-67cb9019e32d` retained through RUNNING→HOLD, 102.310472 work across 124 steps, bearing station 15.897415 and native carrier swing approximately 0.042 rad. Native capture, open restraints and normal dismount completed.
- Axis: normal boarding/close followed by observer fresh lever input at 2026-11-27T02:24:42Z; HOLD at 02:24:50Z. Same seat `44dfa0df-6d97-42cd-b480-63cdfcdb32cc`, 40.524183 work across 182 steps, bearing station 20.997498; native roll reached 0.151996 rad. Native capture/open restraints/normal dismount completed. Video: `evidence/axis-occupied-20261126.mp4` in the persistent runtime.

After these arrivals, the player's explicit native Capture pauses the external owner and retains its lease; LS reports retained FAULT while native fault remains empty. Reopening the native UI after its revision changes permits normal restraint opening and exit. This is deliberate native takeover after the accepted run, not automatic recovery or an extra launch.

- Pipeline: observer fresh high at 2026-11-27T02:28:50Z, HOLD at 02:28:58Z; seat `e9fdb964-979b-4822-89a5-5e02c986c8d1` retained, 83.925137 work across 113 steps, bearing station 16.122458. Native stop/open/normal exit completed. Root exterior stills and state receipts cover the transit; the initially scheduled rider video ended before dispatch, and no redundant ride was run for a replacement video.
- Dive setup correction: the first Use still held the prior Rotation Tool, so it opened the native editor instead of boarding. Removing its temporary floor before the seat readback succeeded caused a setup fall. Normal respawn, restored floor and explicitly empty hand corrected the preconditions; actual boarding was then confirmed before removing the floor. No dispatch occurred during this setup error.

- Dive: fresh observer high at 2026-11-27T02:31:01Z, HOLD at 02:31:09Z. Seat `f4830b01-cf55-4e75-8411-9c5e6c23f9f7` retained, 77.443693 work across 197 steps, bearing station 21.233644. After the accepted arrival, a supplied exit floor at y207 triggered the real native occupied-sweep guard. Removing that floor and placing it at y204, followed by explicit native Stop acknowledgement, cleared the native fault; opening restraints and normal exit then passed. This setup obstruction neither invalidates the completed run nor demonstrates a production failure.

- Conventional Flying: empty-hand normal boarding, separate native body and leg closures, then Set prone produced PRONE lock at approximately -1.555 rad before LS permitted READY. Observer fresh input completed occupied HOLD with seat `e765528c-275f-44e9-b696-a4166b55d603`, 96.048336 work/120 steps, bearing station 15.979574 and unchanged prone lock. Native Set seated correctly refused accessory power after the operator's native Stop suspended the owner and LS entered retained FAULT; normal local controller disarm/re-arm is required before powered posture service. External occupied frame is root observer `20261127T023623.408457Z-after.png`.

## Cable disposition carried from its owner

The [catalog evidence](../ls05-catalog-20261122/README.md) and [finite certificate table](../../FD-VEHICLE-CERTIFICATES.md) close cable mechanics independently: Axis/Dive central and Pipeline/Conventional Flying rear socket +5 passed loaded constrained pull, release, physical HOME, fin HOLD and retirement. Suspended/New-generation Flying are excluded by measured guide clearance: actual 3/16-block guide top leaves 4.625m below rail versus native lower sweeps 4.7m/5.25m, overlaps 0.075m/0.625m. Real loaded native sweep faults retained riders. This is a concrete mechanical incompatibility, not an untested or arbitrary family denial. Powered Inverted Yaw remains excluded by its exclusive native governor contract above.

Flying service completed by normal two-step controller disarm/re-arm, native Set seated, waiting for SEATED lock, separate Open body/Open legs, and normal Shift exit. Work and step totals remained unchanged through re-arm despite the held-high request.

- New-generation Flying: actual seat `db422385-fca1-4265-8dea-3e1e46cb4ab6`, separate body and leg closures, then observer fresh high at approximately 2026-11-27T02:39:43Z. HOLD by 02:39:54Z, 34.701255 work/173 steps, bearing station 14.813619. Native posture -0.213459 rad and physical deck yaw +0.213491 rad both evolved during travel; same rider remained attached. Root exterior `20261127T023949.119073Z-state.png` shows the occupied articulated assembly.

NG service completed: native Stop, explicit local controller disarm/re-arm with unchanged work/steps and no held-high replay, then normal native Unwind + seated. Physical yaw returned approximately zero and posture reached SEATED; separate body/leg opening and normal Shift exit passed. Thus every advertised LS502 eligible family has actual occupied operation and native unloading, while the Powered Inverted Yaw exclusion has a loaded native denial proof.

## Reproducible evidence

`occupied-state-readbacks.jsonl` retains phase/restraint/posture/seat changes and samples each 20 force steps from the raw authoritative stream. `occupied-input-receipts.jsonl` preserves completed actual-input receipts and paths to their persistent before/after screenshots. `occupied-setup-readbacks.txt` discloses fixture/setup and saved-world operations; it contains no connection credentials. Root's independent [operator evidence](../0.5/observer-20261126/README.md) records the fresh lever edges, exterior occupied views and explicit two-step service re-arms.

The complete bounded native result logs accompany their summaries. `native-corrected.log` preserves the initial Axis setup failure alongside the three accepted tire cases; `axis-powered.log` supersedes Axis and proves Powered Yaw denial. `contact-axis-tuned.log` preserves the deliberately corrected combined-passive overlap rejection; `contact-final.log` closes the independent contacts, and `skid-connected.log` supersedes skid with its physical connector. These historical fixture failures are disclosed and are not outstanding gates. `power-native.log` and `null-deck.log` close their specific negative claims. `SHA256SUMS` pins these logs and curated receipts. No broad qualification replay was performed.

## Cleanup

At 2026-11-27T02:43Z, owned client, server and Xvfb units were all inactive/dead with MainPID0 and Result success. `save-all flush` returned Saved the game, then RCON `stop` saved all dimensions and exited normally. No forced server kill was used. The original Nov22 world and resumed `ls05-articulated-resume-20261126` world remain preserved under the persistent runtime. Root independently stopped observer399. See `runtime-cleanup.txt` for exact unit and save receipts.
