# Dependency baselines

Updated 2026-11-27. Profiles select a reproducible dependency set; they are not
compatibility or release acceptance claims.

The [November 22 upstream review](DEPENDENCY-REVIEW-2026-11-22.md) adds Coasters
0.1.6 and newer Maven candidates. Neither implemented profile selects them.
`fd-current` means parity with the inspected sister stack, not automatically
the newest upstream versions. The [roadmap](../ROADMAP.md) retains the accepted LS001/LS002 selection
for the accepted 0.1–0.9 baseline and unchanged 1.0 release inputs.

Both profiles compile and generate packaged Java 21 launch scripts in the
[2026-11-15 preparation record](evidence/workflow-refresh-20261115.md).
Runtime qualification was not run as part of that refresh.

| Component | `legacy` (explicit historical reproduction) | `fd-current` (default 1.0 release stack) |
| --- | --- | --- |
| Minecraft / Java | 1.21.1 / 21 | 1.21.1 / 21 |
| NeoForge | 21.1.248 | 21.1.256 |
| Sable | 2.0.3 | 2.0.6 |
| Create | 6.0.10-280, runtime 6.0.10 | same |
| Simulated Coasters | 0.1.5, file 8655742 | same |
| Flywheel | 1.0.6 API/runtime | same |
| Ponder / Registrate | 1.0.82+mc1.21.1 / MC1.21-1.3.0+67 | same |
| Veil | 4.1.4 | 4.3.2, conditional standalone scheduler repair |
| Sable Companion | Selected transitively by the historical Sable profile | 1.6.0 |
| Fourth Dimension adapter | exact 0.9.0-dev.1, schema 4 | 1.1.0/schema 8 **with external authority API 1**; see pinned handoff |

Select with `-PlsDependencyProfile=fd-current` or `legacy`. Use separate build
and run directories for each. `processResources` writes the selected exact
NeoForge/Sable requirements into the JAR metadata. The default is the qualified current stack. Standalone startup, mass, native
catch/ticket and restart checks passed, along with real occupied Arrow and
current FD saved-world ownership recovery. Old evidence remains reproducible
with the explicit legacy profile. The overall 0.1–0.6 gates are accepted in CURRENT-WORK. No current FD JAR is downloaded,
bundled or silently substituted.

Fourth Dimension's [0.9 dependency record](<../../Coasters: Fourth Dimension/docs/evidence/0.9/dependencies/README.md>)
and [artifact audit](<../../Coasters: Fourth Dimension/docs/evidence/0.9/dependencies/artifact-audit.json>)
remain the core migration inputs; the current source/artifact identity is in
[the updated FD handoff](FOURTH-DIMENSION-HANDOFF.md). Sable 2.0.6 brings Companion 1.6.0 and Veil
4.3.2; confirm resolved and nested artifacts when recording a run. Keep the
current Gradle wrapper 9.2.1 and ModDevGradle 2.0.148, already shared with FD.

## Accepted migration outcome

The [current-core foundation record](evidence/foundation-current-20261122/README.md)
closes standalone startup and affected native Sable contracts. The ticket accessor
layer preserves the same registered IDs and saved keys across Sable 2.0.3/2.0.6.
The conditional Veil scheduler repair runs only on standalone Veil 4.3.2; the
pinned Fourth Dimension facade build supplies the repair in combined runs.

The [registered FD handoff](FOURTH-DIMENSION-HANDOFF.md#registered-authority-handoff--2026-11-22)
adds an actual owner/token facade with explicit restore acknowledgement and
route-management vetoes. Focused current Arrow/Free Spin/circuit native checks
and serialized-token recovery have passed, together with real occupied Arrow
and a separate-JVM same-world restart, rearm and fresh dispatch.
Neither artifact label 1.1.0 nor save schema 8 alone certifies that facade.
An older schema-8 JAR without API 1 is rejected clearly at startup.

Coasters 0.1.6 was audited and explicitly deferred, as were newer loader/renderer
candidates. Their availability is not a compatibility claim. The
`fd-current` default follows the accepted bridge qualification; the legacy
profile and original save/artifact snapshots remain available.

`lsFourthDimensionJar` and `lsEngineeredJar` accept copied, hashed JARs on
persistent task storage. Never reference a sibling's active build output or
alter a live run. Engineered stays optional runtime, compile-only file 8863559
(1.1.1). Additional FD profiles are reference material, not automatic LS deps.
See [optional integrations](OPTIONAL-INTEGRATIONS.md).

Run configuration uses the upstream [ModDevGradle run DSL](https://github.com/neoforged/ModDevGradle#runs).
Client heap is configured on the run model so both Gradle launches and generated
direct scripts receive it. Inspect generated arguments before execution.

## Qualification pins versus metadata ranges

The table identifies tested inputs, not all versions accepted by a loader range.
Ponder metadata permits `1.0.82` and newer, while qualification selects exactly
`1.0.82+mc1.21.1`. Create resolves the `6.0.10-280` artifact and advertises runtime
version `6.0.10`. The expanded NeoForge, Minecraft, Create, Sable, Veil and optional
FD requirements are inspected in the final packaged JAR. No optional dependency
JAR is embedded. Gradle may resolve both full and slim Create artifacts; runtime
mod discovery selects one Create instance, as shown by the packaged startup log.
