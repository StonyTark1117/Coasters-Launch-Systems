# Current work and resumption queue

Updated **2026-11-26**. The **0.5 LS501–LS504 assignment is complete**;
[the integration record](evidence/0.5/integration-20261126/README.md) records
its package, tests, exclusions and cleanup. The integration commit containing
this queue delivers the completed outcome on the existing branch. **0.1–0.4
remain accepted**, including the prior pushed `2d625b9407023be5989b1d2093e91ac1bac1d4cf`.
All later packages remain unassigned. Preserve unchanged accepted evidence.

Checkout: `codex/launch-systems-0-4-0` at
`/home/braydon/Documents/Coasters: Launch Systems`; 0.5 started from that clean commit. Continue the same branch for any later
authorized work. Current Git status is authoritative.

Coordinator chat `01a2049e-cf6c-7fe0-88fe-c814e690d101` (`/root`) owns canonical
status, final integration, build/version, commits and push. `/root/bridge`
owns the shared family certificate/provider implementation and LS501;
`/root/rides` owns LS502 articulated/posture family tests and operation;
`/root/foundation` owns LS503 Roller Ball/PAX boundaries and LS504 certificate
table/body-appearance persistence. Coordinate shared helper/source edits before
changing another owner's file. All agents share this checkout.

Use the accepted FD 1.3 plus external-API1 source/artifact below; new S2/Flying
checks use the narrow family-power overlay in the linked handoff. The current
sister checkout has since published 1.5.0 and integrated the additive overlay; inspect the pinned source with `git show`, not its dirty files as if they
were the accepted artifact. FD1.4 vehicles, moving tracks, LS0.6 multi-train
integration and optional positive PAX delegation remain outside this assignment.

The prerequisite FD facade used a dedicated parallel worktree at
`/home/braydon/.codex/worktrees/launch-systems-authority/Coasters: Fourth Dimension`,
branch `codex/launch-systems-authority`, base HEAD `59c40d11a78c0c7166370c7c8814431ac4f6b304`,
validated HEAD `8cebab779da3311b39e3471bbfb41ebf1dafcd19`. It was clean and
archived through Codex after integration; only disposable project cache was
ignored, and all runtime artifacts/evidence remain outside that worktree.
The active FD coordinator (`01a1a961-64bb-7701-9a19-2acee5ff23a3`) selectively
integrated it into `main` as `235255cf21845cda0e4921b5d036425c55a9a66b`
and verified the pushed remote SHA, preserving unfinished 1.4 changes.
For 0.5 the same isolated worktree was restored and resumed on its existing
branch. Its clean `dc1c83a543256a5ffeab6d7958e9a3656c132f74` was pushed
to its branch, then the worktree was archived after all runtimes stopped. The FD owner selectively integrated it as `22ea6e37`; remote main
`6f482d9d69e0b613eed5b1a8c30b5f818fbf9436` is verified, with its focused 1/1
authority check passing and server saved/stopped. Published 1.5 artifacts remain unchanged.
The additive family-power contract passes its focused 1/1 authority test;
S2/Flying native and occupied integration is accepted in LS501/LS502 below.
Native external/built-in authority checks passed 2/2 with clean shutdown.
The LS combined snapshot remains pinned to that isolated facade build;
[handoff](FOURTH-DIMENSION-HANDOFF.md) records its identity.

| Order / package | Remaining outcome | Next action / evidence to reuse | Owner |
| --- | --- | --- | --- |
| 1 — LS001/LS002 | Standalone current-core outcome complete | [Packaged client/server, 4/4 native and 1/1 + 1/1 restart evidence](evidence/foundation-current-20261122/README.md). Coasters 0.1.6 and newer loader/renderer candidates explicitly deferred. The accepted current stack is now the default; legacy remains explicit. | `/root/foundation`; closed |
| 2 — LS004 / affected LS003 | Current FD provider/authority bridge complete | [Native 3/3 and token 1/1](evidence/bridge-current-20261122/README.md), plus [real occupied Arrow, separate-JVM saved-world restart, explicit rearm and fresh dispatch](evidence/bridge-current-20261122/arrow/README.md). All Arrow runtimes stopped after saving. | `/root/bridge`; closed |
| Foundation — LS005 | 0.1 running-clearance case complete | [Packaged 1/1 pass and clean shutdown](evidence/0.1/clearance-20261122/README.md); existing swept-span, geometry and wrong-interface evidence retained. No runtime remains for this case. | `/root`; closed |
| Complete — LS105 | Three player-built demonstration rides | [Ordinary](evidence/ls01-rides-20261122/README.md), [Arrow](evidence/bridge-current-20261122/arrow/README.md) and [Free Spin](evidence/ls01-freespin-20261122/README.md): normal construction, repeated empty/occupied departure/traversal/capture, exit/reboard and fresh dispatch passed. Temporary Free Spin walking-strip obstruction was diagnosed and cleared in the corrected setup; original fault preserved. | Closed; all runtimes stopped |
| Complete — LS106 / relevant LS902 evidence | Required packaged ride/ownership lifecycle | Ordinary real multiplayer/reconnect/fresh dispatch and current Arrow separate-JVM reopen/rearm/fresh dispatch passed. Free Spin real observer/occupied cycle passed. Retain dev.65 late-join and dev.66–67 active chunk-unavailability evidence plus existing power/support-loss cases. | Closed; all runtimes stopped |
| Complete — LS201–LS205 | Linear/swing/skid outcomes accepted | [Current checks and normal-built occupied mixed swing/skid](evidence/ls02-linear-20261122/README.md), [independent operator](evidence/0.2/observer-20261122/README.md). Seven positive/veto checks, two corrected fault cases, one skid and two separate-JVM restart phases pass; accepted original-family/shared lifecycle evidence carries forward. | `/root/rides`; closed and cleaned |
| Complete — LS301–LS303 | Physical flywheel/catch service and current Arrow recovery | [Current native 2/2, brake-lease 1/1 and restart 3/3](evidence/0.3/cable-current-20261122/README.md); [normal-built occupied pull and corrected physical return](evidence/0.3/cable-current-20261122/PLAYER.md). Pressure contributes normal live-HOME disarm/retirement without clearing the parked train lease. | `/root/bridge`; flywheel runtime closed and cleaned |
| Complete — LS401–LS403 | Distinct hydraulic/pneumatic operation and recovery | [Pressure acceptance](evidence/ls04-pressure-20261122/README.md): store tests 4/4; current FD shaft/guide-loss 4/4 and return interruption 2/2. Normal recipes/status/Ponder, both occupied finite-store launches, physical HOME, recharge and normal retirement preserving the parked train lease passed. | `/root/foundation`; closed and cleaned |
| Complete — LS501 | Fixed-seat/spinning family certificates | Loaded contacts, Wing construction/service, translated Fixed catch and physical HOME, Classic/S2 occupied controls and service accepted. [Evidence](evidence/ls05-fixed-20261122/README.md). | `/root/bridge`; closed and cleaned |
| Complete — LS502 | Articulated/posture-dependent certificates | Loaded contacts, connected skid, power loss, missing deck and Powered Yaw denial accepted. All six eligible occupied launches and native unloading passed. [Evidence](evidence/ls05-articulated-20261122/README.md). | `/root/rides`; closed and cleaned |
| Complete — LS503 | Specialized cycle boundaries | Loaded Roller Ball/PAX ownership denials accepted; native cycle ownership remains exclusive. | `/root/foundation`; closed |
| Complete — LS504 | Finite family × equipment table and persistence | Seventeen variants disposed; native body/appearance, cable positives/guide denials and translated recovery 3/3 accepted. [Table](FD-VEHICLE-CERTIFICATES.md), [evidence](evidence/ls05-catalog-20261122/README.md). | `/root/foundation`; closed and cleaned |
| After interfaces — LS601/LS602 | FD bounded multi-train interoperability | Consume accepted FD 1.2 route semantics; qualify LS leases/requests under one reservation contract. 0.6 closure follows 0.4. | Unassigned; outside active 0.5 scope |
| Independent — LS801–LS807 | Optional adapters | Exact upstream inputs; Additions 0.2.5 adds launch overlap. Positive PAX handoff remains LS806. Optional support does not block standalone release. | Unassigned; outside active 0.5 scope |
| Final — LS901–LS904 / LS1001 | Expanded 1.0 qualification/readiness | Reconcile accepted evidence and remaining advertised matrix; no full-suite reset after documentation/artifact changes. | Unassigned; outside active 0.5 scope |

Record run ownership in the owned run.json and shutdown in evidence. Follow
[AGENTS](../AGENTS.md), [validation policy](VALIDATION-POLICY.md) and
[run preparation](RUN-PREPARATION.md). All 0.1–0.5 runtimes are stopped. The interrupted 0.5 units were verified inactive
on November 26 before resuming only the remaining owned fixtures; all resumed
clients, servers and displays are now stopped. Ownership, saved worlds and
verified shutdown are recorded in the linked evidence. Fourth Dimension's queue remains reference material;
this roadmap does not claim or reopen its cases.

Further workflow guidance (2026-11-22) is in the execution guide's
[document responsibilities](ROADMAP-EXECUTION.md#document-responsibilities) and
[resume/handoff procedure](ROADMAP-EXECUTION.md#resume-and-handoff), the run
guide's [specific input diagnostics](RUN-PREPARATION.md#specific-input-diagnostics),
and validation policy's visual, performance and release sections. This guidance
adds no package, implementation assignment, runtime acceptance or mandatory run.
