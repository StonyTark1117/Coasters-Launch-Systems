# Dependency and asset provenance

Launch Systems gameplay code and models are original except for the specific adaptations identified below; tests also reuse selected sister-project fixture patterns. The Gradle wrapper originates from the NeoForge ModDevGradle MDK via the sibling Fourth Dimension checkout and retains its Apache 2.0 notices. Runtime libraries are resolved from their publishers and are not bundled. The sibling project and inspected Engineered release informed API usage. No Engineered implementation or third-party artwork is bundled.

The addon is MIT licensed. Minecraft textures referenced by original model JSON remain provided by the installed game. Manufacturer names describe mechanism concepts; no manufacturer assets are distributed.

The 2026-11-15 workflow refresh references Fourth Dimension's local instructions,
dependency audit and private Xvfb helper in place. The helper and gameplay code
are not bundled into this addon. See [dependency baselines](docs/DEPENDENCIES.md)
for legacy and current-core candidate pins, and [the handoff](docs/FOURTH-DIMENSION-HANDOFF.md)
for the inspected sibling source revision. Optional artifact licenses remain
subject to their individual audits before redistribution.

## Current-core scheduler and fixture adaptations — 2026-11-22

`VeilSchedulerStartupMixin` adapts the constructor-ordering and empty-work repair
from Coasters: Fourth Dimension at `59c40d11a78c0c7166370c7c8814431ac4f6b304`.
That selected source is dedicated under **CC0 1.0 Universal**, as recorded in
its `LICENSE`; the older MIT label does not describe this revision. The LS
conditional mixin plugin selects one effective scheduler repair when FD is present.
Fourth Dimension authority and Survival-building tests and private-client tooling
are also reference/adaptation inputs for the 0.1 qualification. Neither mod
redistributes the proprietary Minecraft or third-party runtime dependency JARs.
