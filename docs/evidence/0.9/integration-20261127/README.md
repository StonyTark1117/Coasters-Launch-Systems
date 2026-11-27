# Launch Systems 0.9 integration — 2026-11-27

Status: LS901–LS904 accepted; all owned runtimes saved and stopped. Starting source `ba555e9949853b142d9a1e2d7d546ee4ac24d1f2`,
existing branch `codex/launch-systems-0-4-0`, checkout
`/home/braydon/Documents/Coasters: Launch Systems`. Root coordinates the three
agents and owns final packaging, documentation, observer input and Git delivery.
No dependency upgrade, optional adapter expansion or 1.0 publication is included.

## Accepted baseline and finite scope

Accepted 0.1–0.6 records remain valid for unchanged behavior. This qualification
consumes the ordinary six-launch demonstrations, original FD tire rides,
occupied translated Fixed Inverted flywheel, the finite 17-variant family table,
physical catch/service and resource checks, schemas/restarts/chunk/late-join
coverage and bounded Arrow route/operator recovery. No historical case was
reopened simply because the package version changed.

The exact optional FD input remains the accepted 1.3 baseline with the additive
external authority/family/route/operator API 1 overlay, artifact label 1.1.0/schema 8,
SHA-256 `d676d730a02c6f9abe217985db03a3bca424892f06236a0cf64e066b82e87738`.
It is not the sibling's newer published release. The [certificate table](../../../FD-VEHICLE-CERTIFICATES.md)
retains its explicit exclusions. Optional FE and other adapters remain outside
this gate; linear station/transfer and general FD routes are not advertised.

## Package and instruction audit

Java 21.0.13.1 and Gradle 9.2.1 were verified. The complete `build` and all 31 unit
tests passed with no failures, errors or skips; see [build.log](build.log) and
[unit results](unit-results.json).
Subsequent player-fixture-only refreshes do not invalidate those unit results.
The final JAR identity and expanded metadata are in [package-audit.json](package-audit.json).
The audit parses every JSON, checks local model/texture references, rejects
unexpected foreign classes or bundled dependency JARs, and verifies the original
MIT license. The API 4 historical binary is the sole intentional fixture class
outside the mod package; its old API/Minecraft compilation stubs are absent.

There are 20 packaged recipes and 115 original namespace asset/data files.
[Asset parity](asset-parity.json) shows all 115 unchanged from the accepted 0.6
package, carrying forward the normal recipe and four Ponder renders. The new
[operator guide](../../../OPERATOR-GUIDE.md) explains all five braking choices,
controller/boarding signals, six launches and explicit recovery. Historical
checkpoint pages now identify their dated scope rather than presenting old
remaining-work notes as current limitations.

[Runtime artifact audit](runtime-artifact-audit.json) records resolved mod hashes,
metadata, license notices and nested artifacts. Current pins remain Minecraft 1.21.1,
NeoForge 21.1.256, Create 6.0.10-280, Coasters 0.1.5/file 8655742, Sable 2.0.6,
Companion 1.6.0, Veil 4.3.2, Flywheel 1.0.6, Ponder 1.0.82+mc1.21.1 and
Registrate MC1.21-1.3.0+67. Runtime libraries are not redistributed in LS. Ponder's
loader range is broader than the tested pin; no broader compatibility is claimed.
The runtime dependency graph resolves Create full and slim artifacts while the
packaged launch discovers one Create mod. Engineered remains compile-only unless
explicitly selected as an optional runtime input.

## Matrix, lifecycle and measured cost

[LS901/LS902](../lifecycle-matrix-20261127/README.md) close the finite matrix and
remaining provider lifecycle. A real saved Wing could previously resolve as an
ordinary launch-ready cart when FD was removed. The new saved ownership ledger
vetoes that fallback and foreign takeover; unloaded bodies retain identity, while
a deliberate live release and permanent removal retain their existing semantics.
Actual installed/absent/restored JVM phases, owner-contract checks and both native
mixed-provider/family refusal cases passed. The old-world migration precondition
is explicit in that receipt and the operator guide.

[LS903](../performance-20261127/README.md) measures the final ledger-enabled
package on one declared two-train, eight-rider, 49-cabinet route. It reports 1,552
callbacks, median 1.603 ms and p95 3.354 ms, with no dropped samples. These are
bounded wall-clock callback observations in an unpaced headless GameTest, not
whole-tick, native solver or GPU frame costs. Prior energy/pressure/heat evidence
is reconciled and the absence of optional FE conversion is explicit. Its owned
server saved and exited; the pre-ledger measurement remains historical.

The final distributable SHA-256 is
`693a955ed3855a2a4cfa212d562b01fcce270501db0868629029d42f2518adca`,
retained at `/mnt/NVME4/launchsystems-runs/ls09-integration-20261127/artifacts/coasters-launch-systems-0.9.0-final.jar`.
[Production parity](production-parity.json) verifies all 109 mod runtime classes
against the occupied package `6f8a8adf…` and lifecycle/performance package
`d18346da…`. Package differences are later lifecycle test-fixture corrections and
preservation of the legacy player-fixture completion behavior; neither changes
the running release demonstrations or production logic.
The historical API4 binary fixture is separate from those 109 runtime classes.

## Occupied operator evidence

Root operated an independent client named Observer on private Xvfb `:397`,
unit prefix `fd-ls09-observer-20261127`, 1536 MiB heap and verified 6 GiB cap.
The observer used the same packaged code as the rider/server. Camera motion
and a reachable vanilla placement were proved once, with normal bindings and
Raw Input retained. Disclosed setup positioned the operator on supported
platforms; each arming and fresh lever request used normal client input.
F3 established target identity and server readback established the result.
Spectator exterior positioning supplied observation only.

The [occupied record](../occupied-20261127/README.md) owns native telemetry and
rider controls. All four remaining FD demonstrations passed with real Dev boarding,
normal restraint controls, Observer arming and fresh lever input, retained native
seat/mass, finite work and downstream HOLD. Pressure carriages physically returned
HOME. LSM and LIM show distinct expenditure/heat under the same loaded mass.
Native capture, opening and normal exit completed after each run.

| Mechanism | Normal arm UTC | Fresh request UTC | Observed peak speed |
| --- | --- | --- | ---: |
| Pneumatic | 07:27:42 | 07:28:28 | 2.88459 m/s |
| Hydraulic | 07:34:24 | 07:34:58 | 2.88412 m/s |
| LSM | 07:39:54 | 07:40:32 | 2.9851 m/s |
| LIM | 07:43:13 | 07:43:46 | 2.97706 m/s |

Observer [actions](observer/input.jsonl), [curated view identities](observer/views.json),
[ownership](observer/run.json), logs and [shutdown](observer/shutdown.json) are
retained here. Exterior views show the occupied arrivals; the final LIM view
includes cloud occlusion and is supplementary to the rider view/native readback.
Observer disconnected normally at 07:45:19 UTC. Its client and Xvfb then exited
with success, inactive/dead and PID 0; client peak was 2,807,496,704 bytes under
the 6 GiB cap. Root never stopped the separately owned rider server.

## Shutdown and Git delivery

The rider/server owner verified normal player exit/disconnect, `save-all flush`,
all dimensions saved at 07:45:59 UTC, and all three rider/server/display units
inactive/dead/PID 0 with success. [Cleanup](../occupied-20261127/runtime-cleanup.txt)
records the exact units and no remaining video/telemetry helpers. The accepted
world and both diagnosed setup worlds remain on persistent storage. Lifecycle
and performance owners separately record their clean server exits.

The delivery stays on `codex/launch-systems-0-4-0`; no PR, release tag or public
artifact upload is part of this assignment. The enclosing acceptance commit owns
this final source/evidence state. Remote delivery and the actual hosted CI result
are verified after push and reported in the task; local validation above does not
claim a hosted CI pass.
