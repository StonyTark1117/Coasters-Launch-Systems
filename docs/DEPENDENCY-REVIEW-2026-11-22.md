# Dependency review — 2026-11-22

Scope: Minecraft 1.21.1 / NeoForge / Java 21. This is a planning audit of
upstream listings and local source, not a downloaded-artifact or runtime audit.
Release listings can be cached; exact file pages take precedence over older
project indexes. No dependency pin or support claim changes in this review.

## Required stack

| Component | Launch Systems today | Verified upstream observation | Decision |
| --- | --- | --- | --- |
| Minecraft / Java | 1.21.1 / 21 | Existing project and sister-mod target | Retain; no loader or game-version port in this roadmap. |
| NeoForge | Legacy 21.1.248; `fd-current` 21.1.256 | Official Maven metadata lists 21.1.267; metadata updated November 21 | Qualify FD parity at 21.1.256 first; assess 21.1.267 separately under LS002. |
| Coasters Simulated | 0.1.5, file 8655742 in both profiles and FD | **0.1.6, file 9654233**, November 16; changelog describes reducing repeated sublevel scans | Preferred next core upgrade. Review graph/cart/native hooks and affected performance before selecting it for release. It is newer than FD's current baseline too. |
| Sable | Legacy 2.0.3; `fd-current` 2.0.6 | CurseForge lists NeoForge 2.0.6, October 3 | Complete the existing migration; no newer stable Sable release was found in this review. Maven access returned 403. |
| Create | Development 6.0.10-280; runtime 6.0.10 | CurseForge stable remains 6.0.10/file 7963363; official Maven also lists development coordinates through 6.0.11-346 | Keep released 6.0.10 for the migration. Maven build availability alone does not establish a supported stable upgrade. |
| Flywheel | API/runtime 1.0.6 | Official 1.21.1 Maven metadata lists 1.0.7 | Candidate, assessed together with Create's supported renderer stack; no independent blind bump. |
| Ponder | 1.0.82+mc1.21.1 | Official Maven metadata lists 1.0.92+mc1.21.1 | Candidate for the Create/Ponder stack review; check changed scenes/render paths if adopted. |
| Registrate | MC1.21-1.3.0+67 | Configured repository metadata returned 403 | Retain; newer compatible version not verified. |
| Sable Companion / Veil | FD-current audit resolves 1.6.0 / 4.3.2 | FD's saved artifact audit and scheduler evidence | Recheck resolved/nested dependencies in the chosen package; ensure exactly one effective scheduler repair when combined. |
| ModDevGradle / Gradle | 2.0.148 / 9.2.1 | Official ModDevGradle metadata still ends at 2.0.148; Gradle latest not audited | Retain the working tools; no wrapper update required by this planning task. |

Sources: [Coasters 0.1.6](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated/files/9654233),
[Sable files](https://www.curseforge.com/minecraft/mc-mods/sable/files/all),
[Create release](https://www.curseforge.com/minecraft/mc-mods/create/files/7963363),
[NeoForge metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml),
[Create metadata](https://maven.createmod.net/com/simibubi/create/create-1.21.1/maven-metadata.xml),
[Flywheel metadata](https://maven.createmod.net/dev/engine-room/flywheel/flywheel-neoforge-1.21.1/maven-metadata.xml),
[Ponder metadata](https://maven.createmod.net/net/createmod/ponder/ponder-neoforge/maven-metadata.xml),
[ModDevGradle metadata](https://maven.neoforged.net/releases/net/neoforged/moddev-gradle/maven-metadata.xml),
and [FD's resolved audit](<../../Coasters: Fourth Dimension/docs/evidence/0.9/dependencies/README.md>).
XML metadata was read directly from the named Maven endpoints. This establishes
availability, not binary compatibility or testing of those coordinates.

## Sister mod and optional ecosystem

FD's inspected `main` is `59c40d11` with schema 8. Its current queue records
1.1 tools, 1.2 bounded multi-train operations and 1.3 model/body work complete.
The implementation artifact still says **1.1.0**; the published 1.0.0 package
is Beta. Milestone numbers, package labels and public release channels are
different identities. Select by source and JAR hash, using the
[current handoff](FOURTH-DIMENSION-HANDOFF.md).

| Optional project | Verified candidate / retained audit | Planning consequence |
| --- | --- | --- |
| Engineered | 1.1.1, file 8863559; public page still lists it | Retain existing conflict-denial evidence. LS801 owns positive shared control/resource/authority integration. |
| Additions | **0.2.5**, file 9243346, October 18; FD inspected runtime ID/version `coasterfins` / 0.2.2 | New launch-tool modes include hydraulic, flywheel and drive tires. LS803 must audit force suppression and catch/energy overlap, beyond station signals. Do not identify the artifact by embedded version alone. |
| Track Styles | **1.2.0**, file 9020601, September 30 | Supersedes the original LS 1.1.0-hotfix1 discovery baseline. LS804 owns appearance transitions, contact and attachment clearance. |
| Immersion | **1.1.1**, file 9542588, November 10 | Exact changelog says client-only. LS805 treats it as rider presentation, not a server propulsion dependency. |
| Magnetized | 1.1.0, file 8668703; public page still lists it | LS802 consumes FD's Wing/S2 authority work, then proves LS interaction. |
| Tracks and Balloons | 1.2, file 8718286; public page still lists it | LS807 retains exact license/API and ownership review; FD's positive adapter is a useful input, not LS certification. |
| Skin / Waterparked / Create Coasters | FD's prior selected files 9409938 / 9058331 / 8110328 | Secondary appearance/coexistence inputs only; latest versions not reverified here. No new mandatory dependency or broad support promise. |

Sources: [Engineered](https://www.curseforge.com/minecraft/mc-mods/coasters-engineered),
[Additions 0.2.5](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions/files/9243346),
[Track Styles 1.2.0](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles/files/9020601),
[Immersion 1.1.1](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion/files/9542588),
[Magnetized](https://www.curseforge.com/minecraft/mc-mods/coasters-magnetized),
[CTNB](https://www.curseforge.com/minecraft/mc-mods/ctnb), and the
[existing sibling audit inputs](OPTIONAL-INTEGRATIONS.md#current-sibling-audit-inputs--2026-11-22).

## Selection order

1. Finish LS001/LS002 on the existing `fd-current` profile: NeoForge 21.1.256,
   Sable 2.0.6, Coasters 0.1.5 and Create 6.0.10. This isolates the already
   prepared ticket/scheduler migration from new upstream changes.
2. Audit Coasters 0.1.6 as a distinct candidate with a copied artifact, checksum,
   metadata, hook diff and smallest affected native/client checks. Coordinate
   the FD side of combined testing; its 0.1.5 evidence does not prove 0.1.6.
3. Review NeoForge 21.1.267 and the Create/Flywheel/Ponder set independently.
   Record adopt/defer and a specific reason before the 0.4 release baseline is
   locked. A newer listing never silently changes `fd-current`.
4. Promote only a coherent tested set in Gradle, expanded metadata, adapter
   guards and support docs together. Unsupported candidates remain explicit;
   retained evidence keeps its actual original pins.

Only `legacy` and `fd-current` are implemented profile names today. Any additional
upstream-candidate profile is future work, not an available Gradle option. Review
optional adapters one at a time; their dependencies stay out of the default game.

## 0.1 selection outcome

The [foundation qualification](evidence/foundation-current-20261122/README.md)
selects the existing `fd-current` set for 0.1. It records the downloaded Coasters
0.1.6 hash, metadata and 21 changed classes, including native cart scan/link
paths. Defer that upgrade to a distinct affected-contract task; retain 0.1.5
parity with Fourth Dimension. NeoForge 21.1.267, Flywheel 1.0.7, Ponder 1.0.92
and Create 6.0.11 development builds are likewise deferred because none is
required for 0.1 and they would introduce unqualified loader/renderer inputs.
This supersedes the proposed next-upgrade priority above without claiming
runtime support for those candidates. Combined profile promotion still follows
the current bridge and player acceptance.
