# 0.5 integration record — November 26, 2026

Status: **0.5 accepted**. LS501–LS504 are complete; all owned runtimes
are stopped and saved worlds are preserved.
Owner `/root`, existing branch `codex/launch-systems-0-4-0`, checkout
`/home/braydon/Documents/Coasters: Launch Systems`, base
`2d625b9407023be5989b1d2093e91ac1bac1d4cf`. Three agents completed shared
certificates/LS501, articulated families/LS502, and catalog boundaries/LS503–504.
The coordinator reviewed production changes and integrated the evidence.
UTC receipts after midnight November 27 belong to the November 26 local run.

## Scope and inputs

The [finite certificate table](../../../FD-VEHICLE-CERTIFICATES.md) disposes of
all seventeen mechanical variants in the selected FD catalog. Fourteen have
positive tire, friction-fin, LSM, LIM, skid and eddy-current interfaces; twelve
have certified cable sockets. Powered Inverted Yaw, Roller Ball and PAX retain
exclusive native cycle ownership. Suspended and New-generation Flying also
exclude the cable guide because their measured occupied sweep intersects it.
These are evidenced mechanical exclusions, not deferred positive tests.

Native restraints, mass, joints, posture/yaw, maintenance, drop, speed and
retention guards remain authoritative. Compact Wing, open-frame Fixed Inverted
and colors preserve mechanical identity and do not create new eligibility.
Managed FD routes remain denied pending LS601/LS602; newer FD catalog additions,
positive PAX delegation and optional adapters are outside this milestone.

The selected core dependencies remain those in the accepted
[0.4 package](../../0.4/integration-20261122/README.md). New S2/Flying checks use
FD schema 8 / external API 1 / family API 1, source
`dc1c83a543256a5ffeab6d7958e9a3656c132f74`, JAR SHA-256
`3dbbc7a77e1b0cd02b3e5fa3a69e4b4823c5c5a3befca6f0e1e35d72381b7fba`.
Its additive contract passed a focused native authority check and was selectively
integrated into FD main as `22ea6e37046b5932e11ad670643e0b409bad2a59`.
The FD owner repeated the affected authority case, passed 1/1, saved/stopped
its server and pushed final main `6f482d9d69e0b613eed5b1a8c30b5f818fbf9436`.
The remote SHA was independently verified. Published FD 1.5.0 artifacts were
not replaced; the selected LS snapshot does not claim their expanded catalog.
See the [handoff](../../../FOURTH-DIMENSION-HANDOFF.md).

## Final package

`coasters-launch-systems-0.5.0.jar`, SHA-256
`b969dc874a17d511d67c91552d8eae4531a2e49c858177addef3b27b1eba6e3a`.
Persistent artifact: `/mnt/NVME4/launchsystems-runs/ls05-integration-20261126/build/libs/coasters-launch-systems-0.5.0.jar`.
No Fourth Dimension binary is bundled.

## Combined acceptance

| Package | Evidence |
| --- | --- |
| LS501 | [Fixed/spinning record](../../ls05-fixed-20261122/README.md): loaded native contacts and actual-contact boundaries; occupied Wing construction/pull/service, translated Fixed catch and physical HOME, Classic/S2 native controls. |
| LS502 | [Articulated record](../../ls05-articulated-20261122/README.md): all six contacts, connected mounting, actual accessory power loss, missing NG deck and Powered Yaw denial; occupied Suspended, Axis, Pipeline, Dive, Flying and NG Flying. |
| LS503 | [Catalog boundaries](../../ls05-catalog-20261122/README.md): actual loaded Roller Ball and PAX deny LS ownership and preserve native cycle state. |
| LS504 | [Catalog appearance/cable/recovery](../../ls05-catalog-20261122/README.md): two loaded appearance cases, central and hanging cable positives, two physical guide exclusions and three-process translated catch recovery. Accepted FD rendered/occupied appearance evidence carries forward. |
| Independent operation | [Root observer](../observer-20261126/README.md) supplies normal control inputs, held rider identities and exterior views; [foundation observer](../foundation-observer-20261126/README.md) supplies Fixed/Classic/S2 operation. |
| Prior outcomes | [0.4 integration](../../0.4/integration-20261122/README.md) retains 0.1–0.4 construction, stores, interruption, multiplayer and saved-world evidence where the shared contract is unchanged. |

Fixture-supplied rails, kits, positioning and isolated worlds are disclosed in
the case records. Normal boarding, restraints, controls and service use actual
client input. No velocity injection or seated rider teleport proves travel.
Setup misses, corrected mounting/clearance, early failed assertions and partial
process results are preserved and distinguished from the accepted cases.
A corrected targeted pass closes its own case without restarting its siblings.

## Implementation and validation

Installed chassis contacts are projected onto the accepted rail edge with
transverse mounting offsets retained. Controller readiness and braking use the
actual drive/fin station while whole-consist occupancy and native permissions
remain intact. Missing-hardware faults are local to the certified contact.
Translated catches use physical socket HOME and native fin release, with full
carriage return and persistence. New certificate eligibility never supplies a
synthetic force point or overrides a vehicle's native owner.

The FD family-power contract separates actual accessory supply from a propulsion
permit. S2 external mode does not also command its native governor. Flying
posture/yaw motors require powered, non-overstressed owned equipment and preserve
native interruption faults. Native Stop pauses the external owner; explicit
controller disarm/rearm can restore accessory power for seated return while
holding the train. S2 uses the same explicit recovery before native Capture
acknowledges Ready and permits opening restraints. A held high does not dispatch again.

Java 21 `test assemble` passed **29/29** unit checks, no failures, errors or skips;
see [results](unit-results.json) and [build output](build.log). The production classes in
that integration build match the Fixed occupied package byte for byte. Compared
with the articulated package, only the contact-local missing-hardware diagnostic
differs; installed positive contact behavior is unchanged, and the current
boundary/fixed native checks cover that diagnostic. Exact reconciliation is in
[artifact-reconciliation.json](artifact-reconciliation.json). Final Java 21 `assemble` passed; all 166 compiled classes and 129 source
resources are packaged, 100 JSON resources parse, and expanded metadata declares
0.5.0. See [final build](final-build.log), [artifact/source hashes](artifact.json)
and [metadata](packaged-metadata.toml). A metadata/test-fixture hash
change does not invalidate accepted runtime behavior. Source and authored
documentation whitespace checks pass. Raw hashed logs retain their original
trailing whitespace and Gradle/JVM output formatting.

## Delivery and cleanup

All ten final owned client/server/display units independently read
`inactive/dead`, `MainPID=0`, `Result=success`; see
[coordinator verification](runtime-cleanup.txt). Both servers saved and exited
cleanly; each owner preserves its exact save receipt. No live runtime was left
idle or handed off. The clean FD prerequisite worktree was archived after all
helper users finished; its pushed branch and integrated source are preserved.
A helper copy remains with the persistent integration run for later replay.
The sibling primary checkout retains its pre-existing untracked `logs/`, untouched.

The integration commit containing this record delivers the completed package on
the existing task branch; its remote identity is verified after push.
Saved worlds, full videos and runtime logs remain on persistent storage; curated
results, inputs, images and hash manifests are included in Git. No public release
tag or binary publication is requested. Push-triggered CI status is reported
separately from the completed local checks.
