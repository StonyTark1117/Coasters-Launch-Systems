# Current work and resumption queue

Updated **2026-11-22**. The user-assigned **0.1** outcome is complete;
implementation and evidence are committed as `2062e26eb54ec1aa5730cb39d9edfecfd6f19355`.
The coordinator handles delivery of that commit and this documentation receipt
on the existing branch; no gameplay or runtime work remains. Foundation LS001–LS005
and LS101–LS106 are accepted, including all three player-built rides and required
lifecycle evidence. All owned runtimes saved and stopped. The broader release
target remains **0.4.0**; later packages are unassigned for this task.
See [integration evidence](evidence/0.1/integration-20261122/README.md),
[ROADMAP](../ROADMAP.md) and the [execution guide](ROADMAP-EXECUTION.md).

Checkout: `codex/launch-systems-0-4-0` at
`/home/braydon/Documents/Coasters: Launch Systems`. Starting HEAD was
`1ac51d7b1dc7c3dffdb672b6df9ab7b9b875fb09`; the pre-existing workflow,
dependency, cable-ticket and GameTest edits were preserved and integrated in
`2062e26eb54ec1aa5730cb39d9edfecfd6f19355`. Current Git status is authoritative;
continue this branch for an authorized follow-up.

Active coordinator: chat `01a2049e-cf6c-7fe0-88fe-c814e690d101` (`/root`).
Delegated owners below share the existing checkout and coordinate disjoint source
files. Root owns canonical status/evidence reconciliation, commits and push.
Foundation completed standalone startup and owns the independent observer; root
now owns final build/properties. Bridge owns provider authority; rides owns player
demonstrations and affected gameplay. Existing
workflow/dependency/ticket changes are preserved. No new branch is required.
Fourth Dimension tests and private-client tooling are reuse inputs, per the user;
its unchanged accepted evidence is not a new Launch Systems runtime claim.

The prerequisite FD facade used a dedicated parallel worktree at
`/home/braydon/.codex/worktrees/launch-systems-authority/Coasters: Fourth Dimension`,
branch `codex/launch-systems-authority`, base HEAD `59c40d11a78c0c7166370c7c8814431ac4f6b304`,
validated HEAD `8cebab779da3311b39e3471bbfb41ebf1dafcd19`. It was clean and
archived through Codex after integration; only disposable project cache was
ignored, and all runtime artifacts/evidence remain outside that worktree.
The active FD coordinator (`01a1a961-64bb-7701-9a19-2acee5ff23a3`) selectively
integrated it into `main` as `235255cf21845cda0e4921b5d036425c55a9a66b`
and verified the pushed remote SHA, preserving unfinished 1.4 changes.
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
| Then — LS201–LS205 | Complete linear/swing/skid outcomes | Reconcile dev.54–61 and mechanism docs; finish missing behavior, gameplay and lifecycle. | Unassigned; outside active 0.1 scope |
| Then — LS301–LS403 | Complete physical flywheel/hydraulic/pneumatic service and recovery | Reuse cable/pressure results; inspect current FD restoration before replaying the old Arrow release limitation. Close 0.4 with all preceding required outcomes. | Unassigned; outside active 0.1 scope |
| After 0.4 — LS501–LS504 | Modern FD catalog certificates | Finite 0.5 scope, body-choice invariance and mechanical exclusions; no universal catch/linear eligibility. | Unassigned; outside active 0.1 scope |
| After interfaces — LS601/LS602 | FD bounded multi-train interoperability | Consume accepted FD 1.2 route semantics; qualify LS leases/requests under one reservation contract. 0.6 closure follows 0.4. | Unassigned; outside active 0.1 scope |
| Independent — LS801–LS807 | Optional adapters | Exact upstream inputs; Additions 0.2.5 adds launch overlap. Positive PAX handoff remains LS806. Optional support does not block standalone release. | Unassigned; outside active 0.1 scope |
| Final — LS901–LS904 / LS1001 | Expanded 1.0 qualification/readiness | Reconcile accepted evidence and remaining advertised matrix; no full-suite reset after documentation/artifact changes. | Unassigned; outside active 0.1 scope |

Record run ownership in the owned run.json and shutdown in evidence. Follow
[AGENTS](../AGENTS.md), [validation policy](VALIDATION-POLICY.md) and
[run preparation](RUN-PREPARATION.md). All foundation, clearance, ride and observer runtimes are stopped; run ownership,
saved worlds and verified shutdown are recorded in the linked evidence. Fourth Dimension's queue remains reference material;
this roadmap does not claim or reopen its cases.

Further workflow guidance (2026-11-22) is in the execution guide's
[document responsibilities](ROADMAP-EXECUTION.md#document-responsibilities) and
[resume/handoff procedure](ROADMAP-EXECUTION.md#resume-and-handoff), the run
guide's [specific input diagnostics](RUN-PREPARATION.md#specific-input-diagnostics),
and validation policy's visual, performance and release sections. This guidance
adds no package, implementation assignment, runtime acceptance or mandatory run.
