# 0.6 integration record — November 26, 2026

Status: **accepted — LS601/LS602 complete**. Native physical/guard and shared
operator checks combine with actual occupied separation, saved-world
restart/rejoin and explicit recovery requiring a separate fresh input. All owned
runtimes saved/stopped. UTC receipts after midnight November 27 belong to the
November 26 local run. Earlier accepted 0.1–0.5 evidence remains closed.

Coordinator `/root` uses the existing branch `codex/launch-systems-0-4-0` in
`/home/braydon/Documents/Coasters: Launch Systems`, starting from accepted 0.5
`f7cbb6de1d4d789ecea2d7021228056520ec11f5`. `/root/bridge` owns route delegation,
`/root/foundation` shared controls, and `/root/rides` physical and occupied
acceptance. Accepted 0.1–0.5 cases remain closed where compatible.

## Scope and prerequisite

FD owns its original Arrow eight-segment, six-block, maximum-three-train route.
LS uses the native current/next reservation decision while retaining physical
equipment and stopping leases. No second reservation manager, route capacity
expansion, general park UI or optional-adapter qualification is included.

The additive route/operator overlay and native readiness query are pushed in
FD source `79119987a600f8f427656bda7b404546ede3ab47`. Their selective current-main
integration is pushed as `05d38b81e84cab6d4b436c5d584f95e97154f99c`, independently
remote-verified. Current-main family/route/operator checks passed 3/3, followed
by the affected readiness/route check 1/1; servers saved and stopped. Published
FD 1.5 artifacts and moving/specialty guards remain unchanged. The selected LS
runtime remains the pinned baseline plus additive overlay; exact inputs are in
[the handoff](../../../FOURTH-DIMENSION-HANDOFF.md).

## Final package audit

Java21 `assemble` passed; [output](assemble.txt). The final 0.6.0 JAR contains
all 173 compiled classes and 132 processed resources. All 100 JSON resources parse;
[expanded metadata](packaged-metadata.toml) declares 0.6.0 and no FD binary is
bundled. [Artifact record](artifact.json) stores the exact SHA and production
class hashes for reconciliation with the occupied package. A fixture or metadata
change alone does not invalidate compatible runtime evidence.

## Review and targeted corrections

Actual native geometry exposed touching fin intervals separated by 1.49–2.01
micrometres. A shared 0.0001 m seam tolerance fixes only coverage joins; stopping
length and force remain unchanged. Four targeted stopping-envelope tests passed,
including rejection of a 1 mm gap and nonfinite input. See the
[implementation receipt](../ls601-route-delegation-20261126/README.md).

The first consumer request exposed LS READY before FD's required five stopped
ticks. LS now consumes native exact-token readiness. The physical case requests
at the first advertised READY without an artificial delay and applies actual
force. The query can safety-refresh invalid native state but never releases or
dispatches; final dispatch still checks equipment guards.

Final source review checked exact owner/controller/reservation, permission/range,
transient request nonce, save/reload invalidation and queue-only dispatch.
It found one native editor regression: nonblank tuning text could be interpreted
as a panel request nonce for action 10. The explicit `ls-operator:` marker correction passed its focused panel test:
native editor text performs Capture without consuming the external nonce; tagged
Hold retains replay protection. FD source `b6147959` is pushed and selectively integrated into remote-verified
main `3ac29538b928b00939f1ce690f331f3a4a438a72`. Its current-main panel check
passed 1/1 and server saved/stopped. The selected occupied input is `d676d730…` from the handoff. A subsequent occupied restart exposed the retained-transfer recovery omission described below.

## Combined runtime evidence

The [route record](../../ls06-route-20261126/README.md) owns the actual physical
conflict and five fault cases. [Independent reconciliation](native-verification.json)
checks their original hashes, pass markers and six native server save/exits,
and confirms the original tested and then-distributable LS packages were byte-identical.
That historical package is preserved as `coasters-launch-systems-0.6.0-before-recovery.jar`;
[its receipt](artifact-before-recovery.json) and [source hashes](source-before-recovery.sha256)
retain the original identity. The separately continuing occupied runtime is excluded
from the native shutdown claim.

The live native panel first Dispatch entered Running with the same seated Dev
and no fault. After normal capture and explicit recovery, a separate fresh input
transferred A physically to rail 2. B's reservation blocked the next native block:
A retained blocks 1/2, B retained 3/4, both LS stopping leases remained, and the
same rider stayed seated. Work remained at 62.669759 across 895 propulsion steps
for over 60 seconds after denial. The existing physical lever remained high.
This exact world passed restart/rejoin and the targeted fresh recovery below.

The blocked native panel displayed the same LS owner, continuous supply, retained
route/recovery reason and fresh-request state; Dispatch was disabled. One click
on that disabled control had no effect. This is rendered refusal, not a claim
that a dispatch packet reached the server; native focused tests cover that guard.
The observer disconnected normally and stopped its client before the server save.

## Targeted retained-transfer recovery correction

The same blocked world restarted with A’s pair 1/2 and B’s pair 3/4 intact,
controllers under inspection, and the lever still high without request replay.
Normal removal of empty B released only B’s pair. Explicit receiver/source rearm
then exposed a missing call in the active transfer branch: it bypassed the local
loop that consumes the queued native Resume. The saved world was preserved.

The correction reaches the existing source-only authority-reservation path after
exact pair, equipment, route, occupancy and complete-train checks. Every chassis
and transformed tire point must have finite speed below 0.03 m/s and a physical
clamp or native hold. It resamples the train before ordinary preparation; moving
transfer and receiver branches are unchanged. Explicit rearm remains separate
from dispatch. Root, bridge and operator review found no request replay path.

The authored fixture’s first receiver tire was one block beyond contact with the
stopped train. The recovery run moves that existing tire/motor one block upstream
with a validation-only command, preserving its identity/settings and comparing
all native car/route data, chassis/carrier poses and velocities, and controller
request state before/after. Only the hardware interval and module-manifest positions
change. This is disclosed setup correction, not a normal occupied-editing claim.

The corrected Java21 build passed and is frozen as
`coasters-launch-systems-0.6.0-recovery-eb0c84cefa90.jar`, SHA256
`eb0c84cefa907a62eb43a58166225326489ebeaf749569037de3926632ce9c78`.
[Package delta](recovery-package-delta.json) proves `RideTransfers.class` is the
only changed gameplay class; all 104 other production classes and the separate
API4 test fixture are unchanged. GameTest-only additions provide the disclosed
setup correction. The affected occupied recovery check passed: normal rearm reached READY_EXIT with
native held authority, the high lever did not replay, and a separate low/high input
produced actual 1.312 m/s motion with the same rejoined rider and valid pair 2/3.
Both players then disconnected normally. The server saved all dimensions and
exited at 05:23:01 UTC. [Independent shutdown verification](shutdown-verification.json)
records all five client/server/display units inactive with PID 0 and successful exits;
no run helper remains. The original blocked snapshot, post-removal snapshot and
final world remain preserved.

[Independent recovery verification](recovery-verification.json) confirms
input ordering, native ownership and rider continuity through the targeted fix.
[Independent package reconciliation](recovery-package-verification.json) confirms
the targeted production delta and unchanged resources. The
[operator record](../ls602-operator-20261126/occupied-observer/README.md) and
[route record](../../ls06-route-20261126/README.md) preserve exact inputs,
readbacks, visuals, timestamps and the correction’s scope. No public release or
new optional-adapter qualification is claimed.

The clean FD prerequisite worktree was archived after all owned runtimes stopped.
Its pushed `b6147959` history and integrated current-main `3ac29538` are preserved.
The private-client helper was copied to the persistent integration run’s `helpers/`
directory; worlds, logs and artifacts remain outside the archived checkout.
FD primary retains only its pre-existing untracked `logs/`, untouched by this task.
