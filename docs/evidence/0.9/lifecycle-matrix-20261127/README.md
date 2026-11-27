# LS901 / LS902 release-candidate reconciliation

Owner `/root/foundation`; Launch Systems checkout `/home/braydon/Documents/Coasters: Launch Systems`,
branch `codex/launch-systems-0-4-0`, base `ba555e9949853b142d9a1e2d7d546ee4ac24d1f2`.
Persistent root `/mnt/NVME4/launchsystems-runs/ls09-lifecycle-20261127`.
Java 21.0.13.1 / Gradle 9.2.1, isolated build and project cache, current-core dependencies,
exact FD `d676d730a02c6f9abe217985db03a3bca424892f06236a0cf64e066b82e87738`.
No GUI, new dependency, optional adapter, or full-suite replay was used.

## Finite advertised matrix

[finite-matrix.json](finite-matrix.json) reconciles the production certificates:
ordinary Coasters has nine contacts/mechanisms; FD has seventeen mechanical variants,
120 positive and 33 explicitly denied cells across the nine columns. The six independent
contacts and three cable stores are counted separately. Cosmetic bodies do not add rows.
The [certificate table](../../../FD-VEHICLE-CERTIFICATES.md) remains authoritative for
mounting, load, route and native-owner preconditions. The current d676 overlay adds the
accepted bounded Arrow managed route; it does not expand the 0.5 family catalog.

| Advertised claim | Accepted evidence retained |
| --- | --- |
| Ordinary and original Arrow/Legacy/Free Spin/Revised tire, fin, skid, eddy, LSM and LIM | [0.1 integration](../../0.1/integration-20261122/README.md), [0.2 mechanism reconciliation](../../ls02-linear-20261122/README.md), cumulative dev.13–17 and dev.39–61 records linked there |
| Original and expanded cable contacts, all three finite stores | [Current Arrow cable](../../0.3/cable-current-20261122/README.md), [hydraulic/pneumatic](../../ls04-pressure-20261122/README.md), [central and trailing catalog couplings](../../ls05-catalog-20261122/README.md). The three stores share the unchanged real guide/catch rig; their distinct work, recharge and interruption cases remain separate. |
| Wing/Fixed/Classic/S2 individual contacts | [Fixed/spinning native matrix](../../ls05-fixed-20261122/README.md), including real shifted-point activation, passive dissipative work and contact/end-boundary denial |
| Suspended/Axis/Flying/NG/Pipeline/Dive individual contacts | [Articulated native matrix](../../ls05-articulated-20261122/README.md), including complete bodies, posture/yaw and powered-accessory loss |
| Unsupported contacts and operating ownership | [Roller Ball/PAX and cable swept-guide exclusions](../../ls05-catalog-20261122/README.md); [Powered Yaw governor exclusion](../../ls05-articulated-20261122/README.md). These are measured mechanical or native-owner exclusions, not missing-test labels. |
| Conflicting providers/controllers/consists/routes | [Current exact-owner brake-lease negatives](../../0.3/cable-current-20261122/README.md), [0.6 bounded physical two-train conflict](../../0.6/integration-20261126/README.md), and the new owner/mixed checks below |

New mixed checks passed through the actual native `linkCarts` API in **both directions**:
Wing + ordinary, and Wing + Classic Spinning. Each family was created on its supported
rail. The native coupling guard refused the operation; snapshots before/after retained
the same bodies, providers, permissions and individual consists. No native guard was
bypassed to manufacture a mixed train. This proves predictable input rejection;
the provider's additional `mixed_or_unbound` guard remains defense in depth, not a
claim of a normally constructible mixed train. Logs: [mixed-provider](mixed-provider.log.gz)
and [corrected cross-family](matrix-corrected.log.gz).

## Missing-provider defect and correction

A real saved Wing on ordinary Coasters rail exposed a required LS902 defect. With FD
actually absent from the next JVM's classpath, its loaded bearing resolved as
`simulatedcoasters:ordinary_0.1.5`, with tire/fin certificates and dispatch, propulsion
and braking all enabled. [Before-fix receipt](absent-diagnosed-before.log.gz).

`SavedMotionOwners` now records primary provider identity per chassis in world saved
data. `MotionProviders` refuses ordinary fallback or foreign takeover while that saved
provider is absent. A registered provider may deliberately release a **live, resolvable**
vehicle to the ordinary fallback, preserving the existing API-5 handoff. A missing or
unloaded body does not erase identity. Permanent Sable `REMOVED` retires its record;
`UNLOADED` retains it. Unknown provider IDs deny. Malformed/unknown schemas preserve
their original tag and deny resolution rather than throwing into SavedData's empty-data
fallback. Normal repeated snapshots do not dirty saved data or perform recurring disk reads.

Three separate JVMs passed on the **same saved world**:

1. [Prepare](prepare.log.gz): actual Wing `wing_fixed_v1`, ready local controller,
   original FD provider and real tire/fin surfaces; identity saved.
2. [FD absent](absent.log.gz): retained controller UUID and chassis reservation,
   disarmed inspection/fault state, empty interfaces and no dispatch/drive/brake grant.
   FD is absent from the classpath, not merely hidden by a test flag.
3. [FD restored](restored.log.gz): the same exact artifact, chassis, mechanical variant,
   FD provider and local reservation return. No stored request or propulsion replays.

The chassis is `d545fd0b-b5d4-4118-b58c-16a1c7898112`; controller is
`4414377f-56fd-43ce-98ca-c3d9d88c0a5e`. [identities.properties](identities.properties)
is the cross-process assertion input. Existing registered Sable loading tickets keep
the fixture bodies available without FD's runtime or a connected player; they grant no
force, catch, reservation or dispatch authority. The private worlds are retained.

The [owner-contract check](matrix.log.gz) also passed: simultaneous claims conflict;
absent owner A cannot be replaced by registered B; present A's explicit lifecycle
release returns to ordinary; unknown IDs and invalid metadata deny; resolution **after**
unload retains identity; permanent removal retires it. It tests metadata failure in memory,
separately from the real three-JVM saved-world proof.

### Migration precondition

Old worlds have no provider ledger. Load the fixed build **with the original provider
installed**, load the affected vehicles and save before removing that provider. Existing
native snapshots seed identity automatically. Removing an optional provider before any
fixed-build visit cannot retroactively recover its identity from generic bearing/LS-strip
blocks. Keep a backup before removing mods: this LS record preserves ownership, not
another mod's block assets. The selected same-world restoration passed; it does not
promise reconstruction of arbitrarily removed native blocks.

## Existing LS902 lifecycle cases remain closed

| Required behavior | Reused receipt |
| --- | --- |
| Old schemas | Existing Eddy schema1 → zero heat/retained identity; Linear schema3 → safe defaults; Cable schema5 → disarmed legacy fault/no fabricated carriage; controller schema3 defaults; transfer schema4 missing-manifest denial. Native fixture sources and accepted cumulative runs remain in [validation](../../../VALIDATION.md). The new ledger's absent file is its migration path; malformed present data is denied. |
| Restart and no replay | [Current ordinary foundation](../../foundation-current-20261122/README.md), [Arrow linear resource/identity restart](../../ls02-linear-20261122/README.md), [current active cable recovery](../../0.3/cable-current-20261122/README.md), [three-JVM translated catch release](../../ls05-catalog-20261122/README.md) |
| Rejoin, late join, real occupied saved restart | [Ordinary real observer/rejoin](../../ls01-rides-20261122/README.md), dev.65 late join in [validation](../../../VALIDATION.md), [0.6 occupied two-train restart and fresh recovery](../../0.6/integration-20261126/README.md) |
| Removed support and unavailable chunks | Existing pressure shaft/guide loss, missing native carrier/deck and interface loss; dev.66 ordinary and dev.67 FD active chunk tests in [validation](../../../VALIDATION.md), plus current family interruption receipts above |

This closes the remaining LS901/LS902 claims cumulatively. Real-player demonstrations
for all six mechanisms and final artifact/teaching review belong to LS904's separate
owner; synthetic loaded cases here are not relabeled as player evidence.

## Diagnoses, reproducibility and cleanup

The first absence fixture had no loaded native body; its snapshot was empty, not proof
of fallback. Explicit loading tickets corrected availability. Startup also legitimately
faulted a retained controller while its route chunks were unavailable; the assertion now
accepts disarmed INSPECTION or FAULT while requiring unchanged identity/reservation.
The initial mixed fixtures incorrectly expected unsupported coupling to succeed and tried
Classic on Wing rail. Corrected checks assert the native refusal with real supported
families. One newly spawned ordinary body needed a native tick before lookup; its setup
now precedes the checked action. All diagnostic logs are retained and superseded only
by their named corrected results; passed sibling cases were not repeated.

Use `LS_LIFECYCLE_ROOT` for the persistent run root and `LS_LIFECYCLE_PHASE`:
`prepare`, `absent`, `restored`, `mixed` (both mixed cases plus owner contracts),
`mixed_only`, or `mixed_provider`. Select `-PlsTestNamespaces=coasterslaunchsystems_lifecycle`
and `-PlsPackaged -PlsDependencyProfile=fd-current`, with separate `lsBuildDirectory`,
`lsGameTestGameDirectory` and `--project-cache-dir`. `absent` omits `lsFourthDimensionJar`;
all other phases use the exact pinned JAR. `absent` and `restored` must pass
`-PlsKeepGameTestWorld` and reuse prepare's game directory. Never use a valuable
unbacked world: default `runGameTestServer` deletes its selected test world.

[manifest.json](manifest.json) records final artifact, production class/source and log
hashes. The initial fix and subsequent unload/malformed-data corrections were validated
by their affected checks; it does not claim one identical binary across development.
Each GameTest server saved all dimensions and exited. No client, display or service
was launched. [cleanup.txt](cleanup.txt) records no remaining owned process.
