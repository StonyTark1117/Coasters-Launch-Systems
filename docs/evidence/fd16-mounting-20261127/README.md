# FD 1.6 fixed dock cabinet mounting — 2026-11-27

Owner: Launch Systems `/root`, coordinated with FD's “Audit and improve roadmap”
owner. This is an unpublished integration candidate, not a new release or full
FD 1.6 compatibility acceptance. Published LS 1.0.0 remains unchanged.

Checkout `/home/braydon/Documents/Coasters: Launch Systems`, branch
`codex/launch-systems-0-4-0`, base HEAD
`be7f4f6178c006cc3e2b05ecd0b5ab1c955c294a`.
The FD owner owns the existing unpublished schema-9 adapter, version/metadata and
running-clearance edits. This task owns only `EquipmentCabinetEntity.java`, new
`track/EquipmentMounting.java`, the focused mounting tests/template, and this
evidence directory. FD's Arrow fixture and native/occupied runs remain theirs.

## Physical cause and bounded correction

The supplied Arrow sweep reaches down to world y127.6125. Existing cabinets at
y127 occupy y127.0625..127.9375; their adjacent motors conservatively reach y128.
The native moving-sweep rejection is correct. Lowering cabinets and motors to
y126 leaves housing top126.9375 (0.675m clearance) and motor top127 (0.6125m).
The rail is y130.3125, so the cabinet center y126.5 is 3.8125m below it. With a
one-block horizontal offset, the contact distance is approximately 3.9415m.
That does not fit the original three-block binding limit.

Ordinary reach remains three blocks for every mechanism. Only Tire, Fin, LSM and
LIM may extend to four blocks **total**, at most one block horizontally, below
the rail, after authentication through FD's live fixed-dock API. The cabinet
remains in the fixed world. Shaft, orientation, running-clearance and native
moving-sweep checks remain in effect. This is not a four-block lateral radius,
a cable/swing certificate, or permission to mount equipment on the moving bed.
Ordinary downstream exit modules retain their original mounting height.

Both binding and every runtime `resolve`, including cached bindings, check the
same mounting rule. Only the closest contact point is cached, never dock
permission. Loading a saved cabinet clears the point cache and recomputes it.
Missing FD or unavailable native dock authentication denies the extended mount.
Expected dock loss returns null without discarding the saved binding or
latching a cabinet fault that would require an impossible occupied rebind.
The existing runtime skips force allocation and removes unretained fin clamps;
the owning controller faults, clears its request and invalidates its panel
revision. Restored dock authorization alone cannot clear that controller fault.
Explicit arm restores HOLD and clears input/request state; a separate fresh
request remains necessary. The FD owner owns the combined positive lifecycle
test, including actual capture, movement, release and receiving controller arm.

## Targeted checks and artifact

Java 21.0.13.1 and Gradle 9.2.1 were verified. Persistent run/build root:
`/mnt/NVME4/launchsystems-runs/fd16-mounting-20261127`. Storage is XFS with 144GiB
available at preparation; host memory reported 39GiB available and ancestor
cgroup limits were unlimited. The FD owner's two clients/server were left alone.
No graphical client or display is owned by this task.

Six focused unit tests passed: unchanged ordinary reach, actual Arrow geometry,
authentication-required deep reach, radial/horizontal/direction boundaries,
mechanism exclusions and nonfinite input rejection. These are geometry tests,
not a claim that a supplied boolean proves native authorization or lifecycle.

The first two headless fixture attempts failed before binding with the rail
0.5m too high (measured shallow delta y3.3125). Explicit UP-facing anchors alone
did not change this. Inspection of FD's actual native curve construction and
the upstream methods identified the cause: the fixture used lifted
`worldCenter`, while FD uses `worldCenterBase` for raw endpoints. Correcting
that input matches FD's y130.3125 rail. Production limits were not relaxed;
both failure logs are preserved beside the corrected run.

Final native result and candidate identity are recorded in `result.json`.
The candidate incorporates the FD owner's unpublished adapter changes; its full
source manifest records exactly which bytes were built. The separately staged
mounting diff excludes those coordinator-owned edits. Frozen Wing inputs and
all live FD runtimes remain untouched. No new publication is performed here.

## Measured native dock tolerance follow-up

The combined FD Arrow run subsequently measured a live authenticated dock with
contact delta `(1.000000026077032, 3.8125000000001137, 2.431455072837707E-7)`
at cabinet `(-421,126,-436)`. Its horizontal radius exceeds one metre by only
approximately 26 nanometres. Unlike the ideal fixed-rail fixture above, the
native dock's settled pose therefore failed the exact horizontal comparison.
The diagnostic is preserved in `arrow-bind-diagnostic.txt.gz`.

The corrected candidate permits a **one-micrometre horizontal tolerance only**
for authenticated extended dock mounts: squared horizontal distance is compared
with `(1 + 1e-6)^2`. The ordinary three-metre limit, four-metre total limit,
below-rail direction, four permitted mechanisms and live authentication remain
unchanged. A two-micrometre or millimetre excess is rejected. Tests include the
exact observed vector, both signs and horizontal axes, missing authentication,
the tolerance endpoint and its next representable out-of-range value.

This arithmetic-only follow-up reuses the passing native absent-FD binding,
save/reload, shaft and cached-resolution evidence above. The smallest changed
check is the focused seven-test unit class plus JAR compilation; actual native
dock acceptance remains with the FD owner's combined Arrow test. The original
candidate is preserved. `tolerance-result.json` records the separately named
candidate/hash, unit result and package comparison; no frozen input is replaced.

## Locked-socket envelope — current candidate

The one-micrometre candidate allowed real source binding, LS approach, Capture
and transfer in combined Arrow run 7, but rejected the receiving dock at
`(-411,126,-436)`, with live authentication true and measured delta
`(-1.000030491616144,3.812499999427871,-4.4938713017472764E-7)`.
`arrow-7.txt.gz` preserves that run. A tolerance chosen from one source sample
was insufficient; the following contract-derived allowance supersedes it.

FD's `MovingNativeAdapter.POSITION_TOLERANCE` is **0.025m**, with independent
`ANGLE_TOLERANCE = radians(0.25)`. Its `dock()` verifies each actual hosted rail
endpoint is within 0.025m of the nominal socket, then constructs the fixed
bridge using those actual endpoints and the nominal dock tangent/up. Its
`lockedDockEdge()` checks pose and each actual socket displacement again, along
with native locks, phase and stopped state. `RideAuthority.movingFixedEdge()`
delegates to this live guard. Relevant source excerpts and source hashes are
preserved in `socket-contract.json`; no FD source was edited for this fix.

The extended mounting rule now uses a **1.025m horizontal radius**: nominal 1m
plus the native socket's 25mm positional allowance. This is a fixed bound tied
to FD 1.6's admitted endpoint envelope, not a margin repeatedly increased from
observations. No additional angular allowance is added; native endpoint and
attitude checks still decide whether the dock authenticates. The ordinary 3m
limit and total 4m limit stay exact, as do below-rail, mechanism and fixed-world
guards. Running clearance and moving sweep remain unchanged.

Eight focused unit tests cover both measured vectors, both horizontal axes and
signs, exact 1.025m and its next representable value, 25.1mm/26mm excess denial,
combined-axis radius, total-distance refusal and missing authentication.
The arithmetic change reuses the prior native binding/cache/restore checks.
`socket-envelope-result.json` identifies the new separately named candidate,
its package delta and tests. Earlier candidates and evidence remain preserved;
the FD owner consumes this one in the combined Arrow run. No publication.
