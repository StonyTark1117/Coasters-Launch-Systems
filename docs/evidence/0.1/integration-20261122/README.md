# 0.1 integration record — 2026-11-22

Status: 0.1 accepted. All required gameplay cases passed and all owned runtimes
saved and stopped. Source and evidence are committed; this documentation-only
receipt identifies the completed integration.
Owner `/root`, existing branch `codex/launch-systems-0-4-0`, checkout
`/home/braydon/Documents/Coasters: Launch Systems`, initial HEAD
`1ac51d7b1dc7c3dffdb672b6df9ab7b9b875fb09`. Pre-existing workflow, dependency
and ticket-accessor changes were preserved and reviewed with the new work.

## Integrated package

Java 21.0.13.1 / Gradle 9.2.1, default `fd-current`, version `0.1.0`. The final package rebuild also
includes the opt-in read-only geometry diagnostic; production forces and
authority behavior are unchanged. `build` passed **29/29** unit tests with no failures,
errors or skips. All **153** compiled main classes are present in the JAR;
expanded metadata requires NeoForge 21.1.256, Sable 2.0.6, Coasters 0.1.5,
Create 6.0.10 and client Veil 4.3.2, with optional FD 1.1.0 plus the runtime
external-API-1 guard. [Final build output](final-build.txt) and
[artifact/input manifest](artifact.json) preserve the exact package identity.
The final package hash does not relabel the earlier live run artifacts.

Persistent owner-specific output and Gradle project cache:
`/mnt/NVME4/launchsystems-runs/ls01-integration-20261122/{build,project-cache}`.
This check launched no Minecraft client, server or display. No root-owned
runtime remains. The input manifest matches the final source and build settings. Accepted live
run artifacts retain their own hashes; version/profile promotion and the opt-in
read-only diagnostic do not invalidate unchanged gameplay.

## Evidence reconciliation

| Required outcome | Evidence and current disposition |
| --- | --- |
| LS001/LS002 current-core foundation | [Standalone packaged startup, affected native contracts and actual restart](../../foundation-current-20261122/README.md): complete. Newer dependency candidates explicitly deferred. |
| LS003/LS004 current FD authority | [Registered facade and current consumer](../../bridge-current-20261122/README.md): native 3/3 and serialized token 1/1 pass. [Real occupied Arrow, normal exit/reboard and separate-JVM world reopen/rearm/fresh dispatch](../../bridge-current-20261122/arrow/README.md) passed and cleaned. Native FD facade commit integrated/pushed as `235255cf21845cda0e4921b5d036425c55a9a66b`. |
| LS005 geometry/contact/clearance | [Clearance](../clearance-20261122/README.md): focused 1/1 pass, both directions, shapes and retained identity. Existing swept interval, wrong interface and handle-edit cases carry forward. |
| LS101–LS104 mechanisms/controller | Retain accepted tire, fin, magnet, station, circuit and receiver-recovery records in [validation](../../../VALIDATION.md); current bridge checks cover changed authority paths. |
| LS105 player construction and usable cycles | [Ordinary owner](../../ls01-rides-20261122/README.md): complete and cleaned. Arrow empty/occupied circuits, exit/reboard/fresh dispatch and restart passed and cleaned; [Free Spin](../../ls01-freespin-20261122/README.md) normal construction, corrected empty/occupied circuits, native capture, exit/reboard/fresh dispatch passed and cleaned. |
| LS106 packaged multiplayer/interruption | [Independent observer](../../ls01-observer-20261122/README.md): ordinary occupied operation, reconnect without replay, fresh dispatch and observer cleanup passed. [Free Spin observer](../../ls01-freespin-observer-20261122/README.md) also passed its occupied circuit and reboard/fresh dispatch. Current Arrow actual saved-world restart/rearm is linked above. Reuse dev.65 connection/late-join and dev.66–67 actual active chunk-unavailability results. |

The ordinary live run uses the separately identified standalone artifact. New
provider reserve/transfer hooks preserve ordinary behavior through neutral
true defaults; the ordinary re-arm callback is a no-op. The new captured-target
HOLD path supports the FD stopped authority handoff; the ordinary moving
handoff remains RELEASED/RUNNING. The new placement guard has its own bounded
native evidence and does not change live motion/hold forces. These source
changes do not invalidate unrelated accepted mechanism or ordinary lifecycle
claims solely because the final JAR hash differs.

The package is `coasters-launch-systems-0.1.0.jar`, SHA-256
`6fdc4f697783a9c28988056734f7809cc5d8b802e563e21fd3af09927ad1465b`.
The final default is `fd-current`; explicit `legacy` retains its original
NeoForge 21.1.248 pin. No public release/tag is created by this Git-delivery task.

All ordinary, Arrow, Free Spin, observer, foundation and clearance runtimes are
stopped with their saved worlds/logs preserved. The isolated FD facade worktree
was clean and archived after its commit was integrated upstream; only its
unneeded project cache was ignored. The FD facade is already pushed to main as
`235255cf21845cda0e4921b5d036425c55a9a66b`; unrelated FD1.4 work was preserved.

Raw logs/readbacks retain their original whitespace and content for hash
verification. Source/document whitespace and all local Markdown targets pass
inspection. The source/evidence commit is `2062e26eb54ec1aa5730cb39d9edfecfd6f19355`.
This following receipt changes documentation only; all recorded runtime and
build inputs still match that commit. Delivery uses the existing
`codex/launch-systems-0-4-0` branch. The coordinator verifies the remote head
and reports the push-triggered CI outcome separately; no new runtime run is
implied by this documentation receipt.
