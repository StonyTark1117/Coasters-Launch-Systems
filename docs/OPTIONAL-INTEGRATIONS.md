# Optional integration audit

Updated October 2, 2026. Coasters: Launch Systems is a standalone NeoForge 1.21.1 addon for Create: Coasters Simulated. It does not require, bundle, or call any optional coaster addon. Optional support is enabled only after a version-pinned adapter proves a single owner for force, braking, reservations, dispatch, energy, and saved vehicle identity.

## Audited addons

| Addon and checked release | Publicly described overlap | Current Launch Systems behavior | Required before positive integration |
| --- | --- | --- | --- |
| [Coasters: Engineered 1.1.1](https://www.curseforge.com/minecraft/mc-mods/coasters-engineered) | Linear motors accelerate and brake carts; its controller also handles launch, trim, stop and multi-pass behavior. | Uses Engineered 1.1.1’s public span/controller registry at the exact cart contact. Disjoint Launch Systems equipment can run; an active Engineered launch, trim or stop command faults the Launch Systems module before it adds force. | An LS801 adapter must still account for shared controls, stored energy, rollback and 4D reservations. |
| [Create: Coasters Simulated Additions 0.2.0](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions) | Powered track-control fins, stations, restraint signals and optional automatic dispatch. | Detects its published `coasterfins` runtime identifier and disables Launch Systems cabinets. | An LS803 adapter must turn imported station/dispatch signals into requests, then let the vehicle owner and Launch Systems reservations decide readiness. |
| [Coasters: Immersion 0.2.3](https://www.curseforge.com/minecraft/mc-mods/coasters-immersion) | Its public description includes motors that launch, brake, hold and reverse carts, plus camera effects. | Unintegrated and not advertised as compatible. It is not a Launch Systems dependency. | A pinned public API or inspected artifact, an exact runtime identity, and an LS805 test matrix must establish exclusive force ownership before any adapter is enabled. |
| [Coasters: Tracks and Balloons 1.2](https://www.curseforge.com/minecraft/mc-mods/ctnb) | Boost, brake, station and sensor tracks alongside decorative track variants. | Unintegrated and not advertised as compatible. Its custom license is recorded before any artifact is loaded. | An LS807 audit must establish permitted distribution/testing, a public integration seam, and exclusive force, reservation and station ownership. |
| [Coasters Simulated: Track Styles 1.1.0-hotfix1](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles) | Track geometry and rendering styles; no published launch or braking controller. | No adapter is needed or registered. It remains a rendering/track-style compatibility question. | A support claim needs a packaged run with the style's supported base version and a rail-contact regression. |
| [MineCoasters 1.10](https://www.curseforge.com/minecraft/mc-mods/minecoasters) | A separate, physics-driven coaster system with its own track and train model. | Out of scope: it is neither a dependency nor a target provider. | A future provider must publish and validate a versioned `VehicleMotionProvider`; no reflection or cross-mod force injection is permitted. |

The release numbers above are the current public NeoForge 1.21.1 releases observed in the audit. They are discovery evidence, not compatibility certification.

## Integration rules

1. One vehicle section has exactly one active propulsion and braking owner. The core rejects known Engineered/Additions overlap rather than summing impulses.
2. Imported redstone, station, restraint, observer, or sensor signals become requests only. They cannot bypass provider readiness, passenger restraint checks, route reservations, stopping certificates, or fault holds.
3. An adapter may read public APIs and declared data. It may not inspect private fields, mutate another addon's vehicle state, prescribe velocity, teleport a train, or take ownership of Fourth Dimension carrier rotation, mass, restraints, or PAX state.
4. Each supported version gets standalone-absent, installed-disabled, installed-positive, rollback, occupied ordinary, occupied Fourth Dimension, persistence, and packaged-client/server validation. A missing or mismatched provider identity disables the adapter and retains the saved ownership boundary.
5. Cosmetic or track-only addons require a native contact regression before support is claimed. A compatible model or recipe namespace is not evidence that force or reservation behavior is safe.

The roadmap tracks this work under LS801, LS803, LS805 and LS807. Until those checks pass, use disjoint equipment spans and leave one controller family disabled for each ride section.
