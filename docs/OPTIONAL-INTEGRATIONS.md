# Optional integration audit

Updated 2026-11-22; the first table preserves the October 2 artifact audit. Coasters: Launch Systems is a standalone NeoForge 1.21.1 addon for Create: Coasters Simulated. It does not require or bundle optional coaster addons; its explicit adapters may call their inspected APIs. Optional support is enabled only after a version-pinned adapter proves a single owner for force, braking, reservations, dispatch, energy, and saved vehicle identity.

The [new upstream review](DEPENDENCY-REVIEW-2026-11-22.md) verifies newer
candidates. Additions 0.2.5 now advertises hydraulic, flywheel and tire launch
spans; LS803 must inspect their force ownership as well as station signals.
Immersion 1.1.1 is client-only presentation. Old motor and dependency descriptions
below are historical observations, not current requirements.

## Historical audited addons

| Addon and checked release | Publicly described overlap | Current Launch Systems behavior | Required before positive integration |
| --- | --- | --- | --- |
| [Coasters: Engineered 1.1.1](https://www.curseforge.com/minecraft/mc-mods/coasters-engineered) | Linear motors accelerate and brake carts; its controller also handles launch, trim, stop and multi-pass behavior. | Uses Engineered 1.1.1’s public span/controller registry at the exact cart contact; its active predicate is exercised against a real configured `LAUNCH` span. Disjoint Launch Systems equipment can run; an active Engineered launch, trim or stop command faults the Launch Systems module before it adds force. | An LS801 adapter must still account for shared controls, stored energy, rollback and 4D reservations. |
| [Create: Coasters Simulated Additions 0.2.0](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions) | Powered track-control fins, stations, restraint signals and optional automatic dispatch. | Detects its published `coasterfins` runtime identifier and disables Launch Systems cabinets. | An LS803 adapter must turn imported station/dispatch signals into requests, then let the vehicle owner and Launch Systems reservations decide readiness. |
| [Coasters: Immersion 0.2.3](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion) | Its public description includes motors that launch, brake, hold and reverse carts, plus camera effects. | Unintegrated and not advertised as compatible. It is not a Launch Systems dependency. | A pinned public API or inspected artifact, an exact runtime identity, and an LS805 test matrix must establish exclusive force ownership before any adapter is enabled. |
| [Coasters: Tracks and Balloons 1.2](https://www.curseforge.com/minecraft/mc-mods/ctnb) | Boost, brake, station and sensor tracks alongside decorative track variants. | Unintegrated and not advertised as compatible. Its custom license is recorded before any artifact is loaded. | An LS807 audit must establish permitted distribution/testing, a public integration seam, and exclusive force, reservation and station ownership. |
| [Coasters Simulated: Track Styles 1.1.0-hotfix1](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles) | Track geometry and rendering styles; no published launch or braking controller. | No adapter is needed or registered. It remains a rendering/track-style compatibility question. | A support claim needs a packaged run with the style's supported base version and a rail-contact regression. |
| [MineCoasters 1.10](https://www.curseforge.com/minecraft/mc-mods/minecoasters) | A separate, physics-driven coaster system with its own track and train model. | Out of scope: it is neither a dependency nor a target provider. | A future provider must publish and validate a versioned `VehicleMotionProvider`; no reflection or cross-mod force injection is permitted. |

The release numbers above are the historical NeoForge 1.21.1 releases observed in that audit. They are discovery evidence, not compatibility certification.

## Current sibling audit inputs — 2026-11-22

Read the sibling's [integration status](<../../Coasters: Fourth Dimension/docs/INTEGRATIONS.md>),
[backlog decisions](<../../Coasters: Fourth Dimension/docs/INTEGRATION-BACKLOG.md>) and
[0.9 dependency record](<../../Coasters: Fourth Dimension/docs/evidence/0.9/dependencies/README.md>).
These supersede old discovery versions as review inputs; FD acceptance does not
certify Launch Systems' force ownership or enable these dependencies here.

| Candidate | Selected FD artifact / disposition | LS direction |
| --- | --- | --- |
| Engineered | 1.1.1, file 8863559 | Retain existing conflict-denial evidence; review current shared authority for positive LS801 operation. |
| Additions | file 9243346, label 0.2.5, runtime `coasterfins` 0.2.2 | LS803 also audits hydraulic/flywheel/tire force spans and suppression seams; Aero/Interiors stay optional. |
| CTNB / Magnetized | 1.2 file 8718286 / 1.1.0 file 8668703 | Review FD's ownership integrations for LS807/LS802; no double drive. |
| Track Styles | 1.2.0, file 9020601 | Review exact selected metadata/geometry before updating LS804 support. |
| Immersion | 1.1.1, file 9542588, client-only | LS805 presentation review; do not load on dedicated server or bootstrap/LegacyClasspath. FD records corrected direct-script mod discovery. |
| Skin | 1.1.0, file 9409938 | Requires newer core; appearance never grants equipment eligibility. |
| Waterparked / Create Coasters | 0.2.8-beta-hotfix1 file 9058331 / 2.0 file 8110328 | Reference coexistence/seat/graph findings only; no new LS support promise. Waterparked needs KotlinForForge 5.10.0. |

Fourth Dimension itself needs [the current bridge migration](FOURTH-DIMENSION-HANDOFF.md).
Do not add every optional JAR to the default runtime. Copy only the selected
artifacts for the assigned adapter, record hashes and runtime identities, and
reuse applicable evidence before deciding which affected checks remain.

## Integration rules

1. One vehicle section has exactly one active propulsion and braking owner. The core rejects known Engineered/Additions overlap rather than summing impulses.
2. Imported redstone, station, restraint, observer, or sensor signals become requests only. They cannot bypass provider readiness, passenger restraint checks, route reservations, stopping certificates, or fault holds.
3. An adapter may read public APIs and declared data. It may not inspect private fields, mutate another addon's vehicle state, prescribe velocity, teleport a train, or take ownership of Fourth Dimension carrier rotation, mass, restraints, or PAX state.
4. Each support claim covers the applicable absent/disabled/positive, rollback, occupied ordinary/FD, persistence and packaged runtime behavior. Reuse accepted shared-path evidence under VALIDATION-POLICY; these dimensions are not a mandatory full cross-product. Client-only presentation belongs on clients, with server exclusion verified rather than a server load. A missing or mismatched provider identity disables the adapter and retains the saved ownership boundary.
5. Cosmetic or track-only addons require a native contact regression before support is claimed. A compatible model or recipe namespace is not evidence that force or reservation behavior is safe.

The roadmap tracks this work under LS801–LS807. Until those checks pass, use disjoint equipment spans and leave one controller family disabled for each ride section.
