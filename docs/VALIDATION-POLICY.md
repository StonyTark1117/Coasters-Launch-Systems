# Validation and stopping policy

[ROADMAP](../ROADMAP.md) defines required behavior. [CURRENT-WORK](CURRENT-WORK.md)
is the current queue; [VALIDATION](VALIDATION.md) preserves accumulated evidence.
Fourth Dimension's [policy](<../../Coasters: Fourth Dimension/docs/VALIDATION-POLICY.md>)
is the shared methodological reference. Its FD case assignments, release gates
and family matrix do not become Launch Systems requirements.

Before an authorized check, name the changed or unresolved behavior, smallest
check, expected result and stop condition. Reuse accepted evidence where the
relevant code, dependency and setup remain applicable. Documentation, branch
names, a new JAR hash, or test-only logging do not reset whole packages. A changed
dependency can invalidate its affected physics/loading claims; review the actual
interfaces and choose focused checks rather than assuming all prior work failed.
Keep historical artifact/profile identity accurate.

One passing targeted run closes that case. Reopen only a named affected case
for a relevant change or defect. Combine compatible mechanics, construction,
render and persistence evidence to complete outcomes; do not insist that all
observations occur in one session. Run a complete matrix at the release gate
when needed, not after each fix or documentation edit. Old evidence caveats
are not additional acceptance requirements. Preserve evidence files unchanged;
update the current status once instead of appending an expanding checklist.

Use native checks for forces, constraints, ownership invariants and failure
injection. Use connected clients for real input, rendered output, rider sync and
player walkthroughs. Synthetic passengers do not prove real multiplayer; setup
commands do not prove survival construction. Test shared paths representatively
and add distinct family/consist cases only where behavior differs. Do not waive
an advertised mechanic or lifecycle case merely to reduce testing.

After two inconclusive attempts with the same approach, preserve state and
diagnose targeting, connection, fixture preconditions, logs or handler guards.
Identify the failed condition and make a calculated correction before the next
targeted attempt. This is a pause on an unchanged method, not a permanent ban on
input or a reason to abandon the assignment. A local environment/fixture defect
within the owner's repair scope is not an external blocker. Explain any truly
external missing dependency and continue independent assigned work.

A blocker must name the required criterion and consuming LS package. Required
functionality, installation, persistence and safety failures block that outcome;
optional polish or more screenshots of an already proved behavior do not.
Follow [AGENTS](../AGENTS.md) for independent private displays, connection/input
readback, resource budgets and mandatory runtime shutdown. No display/input
global lock exists. Close a case with its evidence and stop its owned runtime
unless an explicit continuing owner accepts the live handoff.

## Visual changes and player guidance

For existing LS105/LS904 and mechanism criteria, inspect the changed player
representation: physical support/attachment, moving catch or restraint parts,
clearance, state cues, readable controls and matching item/Ponder/tutorial views.
Choose views that answer the actual defect, including occupied and motion-extreme
views when the change affects them. A linkage reaching an axle does not prove
the attached seat or equipment shell is connected; a valid model JSON or UV
check does not prove an in-game moving assembly. A screenshot of an old model
does not establish a replacement asset's appearance.

Carry unchanged mechanics forward for texture-only changes. Geometry, pivots,
interfaces or control-state changes need their affected fit/rider/mechanism
checks. Keep appearance identity separate from seat layout, physics identity
and equipment certificates; new body choices cannot silently grant launch or
catch eligibility. Missing promised assets, misleading required instructions
or broken clearance block their existing consumer. Additional aesthetic wishes
outside the adopted criterion remain follow-up work. Do not import FD917's
whole production program or every FD vehicle into LS acceptance.

References: FD's [visual production method](<../../Coasters: Fourth Dimension/docs/roadmap/pre-1.0-visual-production.md>)
and [base-model repair/refinement](<../../Coasters: Fourth Dimension/docs/roadmap/1.3-car-models.md>).

## Performance observations

For LS903, record the hardware, renderer, dependency/artifact profile, scene,
vehicle/equipment counts, occupied load, distances, heap/cgroup limits, settling
period, sample interval and concurrent host work. State what each collector
actually measures: LS callback time is not whole-server tick time, Sable cost,
client frame time or end-to-end latency. A bounded heap is not total RSS. Report
the relevant distribution/peak and units; separate software-rendered Xvfb
results from claims of GPU gameplay smoothness.

Use comparable retained measurements and a declared representative workload.
Develop a missing collector early when needed, then measure the accepted
integrated mechanism/assets for the final claim. Do not invent FD's numeric
budgets, 500-stage count or run duration as LS requirements. Choose a finite
sampling plan that answers LS903's actual claim before starting, and expand
only for a named uncertainty. FD's [performance documentation](<../../Coasters: Fourth Dimension/docs/PERFORMANCE.md>)
is a useful example of measurement boundaries, not a Launch Systems command API.

## Baseline and release reconciliation

For each release outcome, record its finite advertised capability/profile list,
source and JAR identity, dependency set, accepted visual baseline and exclusions
in the existing qualification record. Keep it stable during final checks. An
unrelated new build or another release's prototype does not reset it. A contract
or scope change revises that baseline explicitly; a targeted fix rechecks the
affected claim while preserving other acceptance.

LS901–LS904 and LS1001 consume accepted child evidence and actual remaining
criteria. A frozen artifact does not excuse a newly found required defect, and
an aggregate gate does not demand a fresh replay of every child. Package review
must use the distributable JAR, expanded metadata, required assets and correct
standalone/optional discovery. A source checkout or compile alone does not prove
installation. Publication follows the task's actual release authorization;
readiness, version labels, source integration and public release are distinct.

Reference: FD's [freeze and release guide](<../../Coasters: Fourth Dimension/docs/roadmap/1.0-release.md>).
