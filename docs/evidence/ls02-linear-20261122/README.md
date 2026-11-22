# 0.2 linear, swing and skid acceptance — 2026-11-22

Owner `/root/rides`; checkout `/home/braydon/Documents/Coasters: Launch Systems`, branch `codex/launch-systems-0-4-0`, starting HEAD `974c2c8`. This record combines accepted dev. 13–17 and dev. 39–61 cases with the finite current-authority and player gaps. It does not reopen unchanged siblings or certify excluded linear station/transfer modes.

## Scope and accepted evidence

| Criterion | Reused evidence | Current closure |
| --- | --- | --- |
| LS201 LSM; LS202 LIM | Native response, force/work/loss, reserve, tuning, reverse and powered braking, dev. 13–17 and dev. 54–61 | Current loaded family/provider checks plus both actual mechanisms in the occupied mixed bank |
| LS203 swing | Three-pass single and linked Arrow runs, both directions, end-tail clearance and interruption, dev. 54–60 | Current Free Spin LIM swing, Arrow interruption fixes, actual occupied mixed three-pass ordinary run |
| LS204 skid | Shoe certificate, dissipation, heat/derating, persistence and no static hold, dev. 39–40 | Current API1 Arrow separate-zone skid and occupied player trim |
| LS205 eligible original FD | Arrow/Legacy/Free Spin/Revised certificates, loaded limits, PAX veto, persisted families, dev. 41–61 | Selected distinct changed authority/force paths and current Arrow separate-JVM persistence |

Shared actual multiplayer/reconnect and chunk-unavailability behavior carries forward from accepted 0.1. No mechanism-specific change invalidated that common matrix. PAX remains exclusive and unsupported for external linear launch. These claims require actual installed chassis hardware; native seat magnets are not substitute certificates.

## Current native results

[Seven positive/veto cases](current-linear-native.log.gz) passed on the current API1 runtime: LSM Arrow, LIM Arrow, LSM Legacy, LIM Revised, LSM Free Spin, LSM PAX veto and LIM Free Spin swing. The two interrupted Arrow cases exposed a real shared-authority defect: after suspension, even the retaining controller lost permission to apply its spring brakes. Bridge repaired only same-controller, complete-consist, suspended lease braking. Propulsion/dispatch/capture still require live authority; another owner cannot take over. The two affected cases then passed, retaining lease and physical hold without replay: LSM return-leg power loss, and LIM thermal loss. The original seven passing cases were not repeated.

The current Arrow skid fixture uses a shoe at local north/two-below, a passive cabinet at route distance 20, and the downstream fin bank beginning at 29. The first setups correctly rejected simultaneous unlinked skid/controller-owned contacts. At distance 20 it already dissipated work, but the old fin bank at 21 still overlapped the complete chassis. The corrected gap removes that condition. Because passive braking and linear propulsion share Sable's native PROPULSION force group, the new trim fixture stops interpreting that group's work as linear-only once shoe heat appears; all existing linear accounting assertions remain unchanged. It reports peak heat 70.1418, speed 0.00001221 at tick 700, no fault, retained controller lease and no new static-hold claim. Final focused result: **1/1 passed**.

Separate Java processes prepared and reopened a live Arrow LSM section. Both phases passed 1/1. Controller `8baacbfd-1387-478f-bb90-cee78a21d14f`, one-member reservation, five drive identities, energy 1133.418070287037 and heat 9.154203462134088 persisted exactly. Reopened cabinets are disarmed and controller INSPECTION, so stored resources cannot replay motion. Other accepted family/mechanism restart cases remain valid.

Native logs: [selected current cases](current-linear-native.log.gz), [affected fault fixes](current-linear-fault-fix.log.gz), [separate skid](current-skid-final.log.gz), [restart prepare](current-linear-restart-prepare.log.gz), [restart verify](current-linear-restart-verify.log.gz).

## Runtime and normal construction

Persistent root `/mnt/NVME4/launchsystems-runs/ls02-linear-20261122`; separate `build`/`project-cache` for native tests and `client-build`/`client-project-cache` for gameplay. Java21; client 1536M heap in a 6GiB Minecraft-only unit; server 2048M/4GiB. Private Xvfb`:396`, client window2097160, server25771/RCON25772, world `server/ls02-linear-survival`. Units `fd-ls-linear-20261122-{client,server,xvfb}`. Initial host available memory 38GiB, persistent disk 161GiB free. Camera turning and ordinary reachable placement passed on raw input true and default mouse bindings; those settings remained fixed.

Gameplay is packaged standalone LS 0.1.0 SHA256 `29c15b68d41db6e143747b4d3d5925f7d384fef8f7f354ede854c763242e247b`. Current native final JAR is `1aaee506509d1695c70cc3f02ce973984b08a1eab387201433af351358dc2169`, with pinned FD API1 JAR `df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e`. Shared FD braking changes do not affect the standalone player run. Each generated launcher pins its own build/classpath; no shared-build race was used.

Dev is a real Survival player. Commands supplied materials, floor/scaffolds, Create source power, protection and disclosed positions/aim; they did not construct functional rail, cabinets, controllers, chassis interfaces, seats, leases or physics. Functional construction/configuration used normal Use/ShiftUse/Alt input with one expected state and block/NBT readback per action. A 64m attempted connection correctly exceeded the server 45m span limit; the actual straight is 40m. Ordinary Track uses its native `create:track_connecting_from` component, and Alt on its second endpoint preserves straight rail geometry. A temporary ordinary native scaffold was normally mined with supplied efficiency/haste; no functional body blocks were command-edited.

One input sequencing mistake let an arming script overlap an attempted link script; both stopped on failed readbacks. After both exited, the affected modules were explicitly disarmed, linked and rearmed normally. The link handler's armed-module veto was preserved. Earlier moving-target attachment misses were resolved by inspecting native positions and using a held chassis/measured face. No velocity, impulse or vehicle-position injection was used. A zero-speed supplied motor initially kept the controller in HOLD; its saved 0 RPM was read back, the supplied motor was replaced at 64 RPM, and READY followed without a controller change.

## Occupied LSM/LIM swing

Normal-built rail runs `(0,100,0)`→`(0,100,40)`. Alternating linear modules at x=-1,y98,z2..36 (even z; multiples of4 LSM, other even z LIM); spring fins x1,y98,z2..10 and30..38; controller `(5,98,8)`. The controller is bound and linked through normal Span Tool use; Compass offhand selects the three-pass program. Its floor-mounted lever was placed and operated normally by the independent Observer on root-owned display 399. An unlinked skid at `(1,98,24)` stays disarmed during the swing, preserving the ownership rule.

Chassis `5eb32f51-6661-4346-9a6e-a6df7ae05793` has actual tire strip, friction fin, both reaction plates and a Create seat. Dev boarded normally, yielding seat attachment `[53552271,-1126741280,-1642487949,-1948602358]`; loaded mass 7.3. After READY, Observer's fresh real high edge at21:12:48.909641Z started the run. Samples show passes 0, 1, 2, both reversals, then final HOLD/no fault with that same rider attached. Final normal Shift exit removed RootVehicle.

Both mechanisms actually operated: LSM peak sampled heat 0.7576736 and minimum buffer 2047.059188; LIM peak heat 24.2997755 and minimum buffer 2044.911387, from initially full 2048 stores. Native accounting separately certifies their distinct force/loss behavior. The [100-sample telemetry](occupied-mixed-swing.jsonl.gz) and [compact summary](mixed-summary.json) preserve phases, every module's resources, native body speed and rider attachment.

## Occupied passive skid

A separate normal-built 40m straight at x16 uses tire drive `(15,98,8)`, a disarmed staging fin `(17,98,8)`, and skid `(17,98,24)`. Tire and skid contact zones are separated. Chassis `273b2cd9-e266-4cd7-8a23-90cbd7ab3d93` has normal installed tire/fin/shoe and seat; the shoe sits below a normally placed north mounting plate. A low supplied construction floor lets the player place the shoe without body contact. No scaffold remains in its moving sweep. Dev boarded the real Create seat `[-1253195571,-1587392999,-1732836164,370109232]`, loaded mass 6.3.

Observer's normal empty-hand ShiftUse armed the tire at **21:17:26.539747Z**. Native speed reached 2.558529 blocks/s, then the car coasted to the separate shoe contact. At 21:17:33.577Z heat reached 12.056431 and speed dropped to 0.220474; it then settled at z23.457878 with near-zero speed, no skid fault and the same rider retained. The staging fin was disarmed and geographically absent from this contact. This is an actual dissipative trim, without a static holding claim. [240 half-second samples](occupied-skid.jsonl.gz), [summary](skid-summary.json), [rider video](occupied-skid-rider.mp4) and [final rider frame](occupied-skid-final.png) preserve the result.

## Evidence and cleanup

[Input](input.jsonl.gz), [construction readbacks](construction-readbacks.jsonl.gz) and [console readbacks](console.log.gz) disclose normal actions and setup commands. Included construction/sampling scripts make the inputs reproducible; runtime configuration and RCON credentials are excluded. Saved worlds, full screenshots and original build outputs remain in the persistent run root. Observer evidence is separately owned by root.

The [third-person final still](occupied-skid-third-person.png) visibly confirms Dev seated on the trim cart. Normal Shift exit at 21:21:01Z removed RootVehicle. Root independently stopped its Observer/client display399; then the owned client stopped, server `list` confirmed zero players, and the `save-all flush` save operation completed at 21:21:05.974Z. RCON `stop` at 21:21:16.999Z was followed by all dimensions/sub-levels saved at 21:21:17.551Z and natural process exit. Owned Xvfb stopped after the client exited. [All three units](shutdown.txt) are inactive/dead, MainPID0, Result success, ExecMainStatus0. No forced kill or save timeout occurred. Client peak usage was 2,749,849,600 bytes under6GiB; server peak1,539,117,056 under4GiB. [Final server log](server-final.log.gz) and the preserved world support reopening if needed.

This closes the assigned LS201–LS205 0.2 outcomes when combined with the accepted evidence above. The root agent owns canonical roadmap reconciliation and final integration/push.
