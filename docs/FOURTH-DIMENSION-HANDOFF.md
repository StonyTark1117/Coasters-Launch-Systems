# Fourth Dimension integration handoff

Inspected **2026-11-22**: `/home/braydon/Documents/Coasters: Fourth Dimension`,
`main`, HEAD `59c40d11` (completed 1.3 integration), FoundationData schema **8**.
The source `gradle.properties` still labels the artifact **1.1.0**. The selected
1.3 implementation JAR is `coasters-fourth-dimension-1.1.0.jar`, SHA-256
`90ddc4e88d0fc280cc572e0a821dfdad5520a30e5f609c5578dab087417ca24b`, as recorded
in its [integration manifest](<../../Coasters: Fourth Dimension/docs/evidence/1.3/integration-manifest.md>).
This is an unpublished implementation checkpoint, not a public 1.3 release.
The published 1.0.0 file is Beta; stable promotion remains separate.

The current queue records 1.0 readiness, 1.1 tools, 1.2 multi-train operation
and 1.3 body/model work complete. Its 1.2 scope is a fixed original-Arrow route
with eight segments, six blocks and at most three trains. 1.4 specialty vehicles
and moving-track mechanisms are later scope. Read the current queue/source
before selecting a copied artifact; top-level historical status prose can lag.

Before this handoff LS was `0.0.1-dev.72`; its exact `0.9.0-dev.1`/schema-4
adapter did not support this sibling. LS0.1 now consumes the registered contract
below while preserving that legacy adapter separately. Existing standalone and original-family evidence stays valid
within its recorded pins. Current FD's broader catalog is allocated explicitly
to LS501–LS504; bounded multi-train interoperability to LS601/LS602.

FD still builds with NeoForge 21.1.256, Sable 2.0.6 and Coasters 0.1.5.
Coasters 0.1.6 is a newer upstream candidate, not part of that FD acceptance;
see [dependency review](DEPENDENCY-REVIEW-2026-11-22.md).

## Registered authority handoff — 2026-11-22

The facade overlay is complete: isolated source commit
`8cebab779da3311b39e3471bbfb41ebf1dafcd19` atop the accepted `59c40d11`
baseline, selectively integrated and pushed to FD `main` as
`235255cf21845cda0e4921b5d036425c55a9a66b`. The owning FD coordinator
reviewed the assertions and raw native **2/2** pass with clean shutdown;
its unrelated unfinished 1.4 files remain preserved. The native provider path
retains its existing resolution and token behavior.

The selected combined JAR is `fourthdimension-authority-api1.jar`, SHA-256
`df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e`,
stored at `/mnt/NVME4/launchsystems-runs/bridge-20261122/artifacts/`.
It still reports 1.1.0 and schema 8, and additionally exposes external authority
API 1. The LS consumer now registers `coasterslaunchsystems:local_controller`;
this is an actual token/owner contract, not a widened version guard.
The earlier selected 1.3 JAR above lacks this facade and is not interchangeable.

LS focused current Arrow, Free Spin and native circuit checks passed **3/3**.
Serialized-token recovery passed **1/1**; see the
[bridge evidence](evidence/bridge-current-20261122/README.md). The [real Arrow
ride](evidence/bridge-current-20261122/arrow/README.md) also passed normal Survival
construction, empty/occupied circuits, exit/reboard/fresh dispatch and a separate-JVM
saved-world restart with explicit rearm and no old-input replay. All Arrow-owned
clients, server and displays are stopped with the world saved. LS004 is closed;
The separate [Free Spin LS105 player operation](evidence/ls01-freespin-20261122/README.md)
also passed its empty/occupied, capture, exit/reboard and fresh-dispatch criteria. The ordered
implementation notes below describe the original migration and its invariants;
this handoff supersedes their statements that no facade exists.

## 0.5 family-power extension — accepted

The selected 0.5 overlay is source commit
`dc1c83a543256a5ffeab6d7958e9a3656c132f74` on the existing
`codex/launch-systems-authority` branch, atop `8cebab77`. Its frozen JAR is
`/mnt/NVME4/launchsystems-runs/ls05-fd-family-20261122/fourthdimension-family-api1.jar`,
SHA-256 `3dbbc7a77e1b0cd02b3e5fa3a69e4b4823c5c5a3befca6f0e1e35d72381b7fba`.
It preserves schema 8 and external API 1 and adds `EXTERNAL_FAMILY_API = 1`.
S2 can select a registered external longitudinal owner without also commanding
its native governor. Flying/New-generation Flying can consume actual external
accessory power while retaining native posture/yaw and power-loss guards.
Held or suspended travel authority alone does not remove physical accessory
supply, and accessory supply never grants a travel permit.

The focused external owner/transfer/reload/loss and accessory-supply test passed
1/1; its server saved and exited. Actual-family native and occupied acceptance
is complete in LS501/LS502; see the [0.5 integration record](evidence/0.5/integration-20261126/README.md). On November 26 the FD coordinator selectively integrated the overlay
into current main as `22ea6e37046b5932e11ad670643e0b409bad2a59`, preserving
1.5 guards. Its focused authority test passed 1/1; final remote main
`6f482d9d69e0b613eed5b1a8c30b5f818fbf9436` was verified. See the
[main integration receipt](<../../Coasters: Fourth Dimension/docs/evidence/launch-systems-family-power-20261126/README.md>).
Published 1.5.0 artifacts were not replaced. LS keeps the frozen selected JAR above;
this integration does not broaden its tested vehicle catalog. Older family/body evidence stays
valid where these narrow changes do not affect its claim. Powered Inverted Yaw
retains its mandatory native whole-circuit Chain/governor ownership and is not
included in this extension.

## Select an integrated source and artifact

The primary checkout has since published 1.5.0 and integrated the family-power
overlay beyond the selected 1.3 baseline. A link into that checkout can
therefore show later work than the selected 1.3 JAR. Recheck `git status` and
the relevant diff; use `git show <selected-commit>:<path>` to inspect the accepted
source when necessary. Never reset the sibling checkout to obtain that view.

Select the existing accepted artifact and matching source/evidence, or explicitly
coordinate a newer integrated handoff. Do not rebuild the sibling's dirty tree
and describe the result as its accepted artifact merely because the version
string or HEAD matches. Record any intended source overlay and its affected
claims. Copy a stopped saved world and selected JAR into owned persistent run
storage; a live runtime's files are not an implicit snapshot or ownership transfer.

The source-entry links below are navigation aids, not a mandate to keep class
names unchanged or start a new shared framework. Preserve stable persisted
identities and make only the owning contract's necessary migration. Runtime
and artifact identity outrank filenames, milestone numbers and old summaries.

## Read these files before implementing LS004

All links below point into the sibling checkout; inspect its saved source and
evidence without modifying its active worlds, processes or task assignments.

| Source | What to carry into Launch Systems |
| --- | --- |
| [Current queue](<../../Coasters: Fourth Dimension/docs/CURRENT-QUALIFICATION.md#current-work-queue>) and [integrations](<../../Coasters: Fourth Dimension/docs/INTEGRATIONS.md>) | Current closures/profile status; historical paragraphs are not new assignments. |
| [Dependency baseline](<../../Coasters: Fourth Dimension/docs/evidence/0.9/dependencies/README.md>) | Exact core pins, Sable audit, client scheduler and restored-world evidence. |
| [FoundationData](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/runtime/FoundationData.java>) | Current persisted vehicle/carrier/authority state and migrations; save schema is not a stable public motion API. |
| [RideAuthority](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/integration/RideAuthority.java>) and [RideAuthorityState](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/integration/RideAuthorityState.java>) | Single longitudinal owner, epoch/sequence tokens, held/suspended/restored state, explicit transfer/resume. |
| [RideOperations](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/integration/RideOperations.java>) | Bound controller requests, server-thread validation and dispatch/capture authority. |
| [VehicleOperations](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/vehicle/VehicleOperations.java>), [Boarding](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/vehicle/Boarding.java>) | Vehicle-owned dispatch, capture, restraints, boarding and family-specific guards. |
| [EligibilityHooks](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/runtime/EligibilityHooks.java>) and [FamilyControllers](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/runtime/FamilyControllers.java>) | Actual accepted rail/vehicle support and controller identity. |
| [RideLocalOperations](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/integration/RideLocalOperations.java>) and [1.2 manifest](<../../Coasters: Fourth Dimension/docs/evidence/1.2/integration-manifest.md>) | Bounded route admission, current/next block ownership, release vetoes and explicit recovery; no assumption that raw dispatch is reservation-free. |
| [1.1 tools](<../../Coasters: Fourth Dimension/docs/evidence/1.1/README.md>) and [1.3 body choices](<../../Coasters: Fourth Dimension/docs/evidence/1.3/body-variants/README.md>) | Reuse operator requests and preserve independent appearance, seat/physics and mechanical identities. |
| [Authority checks](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/gametest/RideAuthorityGameTests.java>) and [conflict checks](<../../Coasters: Fourth Dimension/src/main/java/dev/fourthdimension/gametest/OperationsConflictGameTests.java>) | Existing ownership invariants to reuse when designing the adapter. |

## Ordered work and acceptance

1. Preserve the existing dirty source, workflow and GameTest work and select one
   LS004 owner. Snapshot
   a chosen FD artifact into persistent LS storage; record its hash, version,
   source HEAD and relevant source diff. The old accepted dev.1 artifact hash is
   in `VALIDATION.md`; do not relabel it as current evidence.
2. Use `lsDependencyProfile=fd-current` in an isolated build. Review the current
   neutral `VehicleMotionProvider` API 5 against the above interfaces. FD's
   `RideAuthority.claim` currently whitelists native_chain, Engineered,
   Magnetized and CTNB, **not Launch Systems**. A proper bridge needs a reviewed
   facade/registration or explicit extension; do not impersonate one of those
   providers, bypass it with raw `VehicleOperations.dispatch`, or change only
   `FoundationData.SCHEMA == 4` to 8. Coordinate any sibling-side change. Review
   `RideLocalOperations` dispatch/hold guards as well: until LS601 is accepted,
   explicitly deny an overlapping route-management mode rather than bypassing
   its reservations.
3. Establish a single owner for translation, braking, capture, dispatch, force
   and reservations; define stale-token, lost-controller, stopped/restarted
   world and unavailable-body behavior. Preserve FD's rotor/seat transforms,
   native passenger mass, restraints, maintenance/faults and family support.
   Do not double-count rider mass or let a rejected FD vehicle fall through to
   the ordinary provider. PAX remains exclusive until its explicit final-brake
   handoff is implemented under LS806. Consume its accepted lifecycle now; the
   historical schema-4 Arrow carrier-restoration limitation must be re-inspected
   against current source before calling it an upstream blocker.
4. Start with the existing advertised original families/variants and accepted
   LS hardware interfaces. New Wing, Flying, S2, Dive or other FD vehicles do
   not become eligible by sharing fields or a schema. Use LS501–LS504 for
   explicit certificates
   and corresponding scoped acceptance before expanding support.
5. Update exact metadata and adapter identity together after contract review.
   Validate affected missing/mismatched-provider denial, occupied ordinary and
   representative original-family ownership, restraint/capture veto, mass,
   competing controller, persistence and packaged client/server behavior. Reuse
   compatible earlier mechanics evidence and the relevant FD authority evidence;
   do not restart every package or full profile matrix.
6. Record exactly which FD artifact/core profile is accepted, update the current
   queue and compatibility docs, and stop all owned runtimes. LS004 closure
   does not automatically close player-built survival or multiplayer cases.

This file supplies the next implementation path. It does not claim that the
new adapter, scheduler integration or runtime qualification has been completed.
