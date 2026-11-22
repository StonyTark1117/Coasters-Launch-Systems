# 0.4 integration record — 2026-11-22

Status: 0.4 accepted. All required cases passed, saved worlds are preserved and
all owned clients, servers and displays are stopped.
Owner `/root`, existing branch `codex/launch-systems-0-4-0`, checkout
`/home/braydon/Documents/Coasters: Launch Systems`, starting HEAD
`974c2c89a959cf615352db255a8abca07ad58c43`. Three agents completed the assigned
linear/skid, cable and pressure packages in the shared checkout; the coordinator
reviewed and integrated their changes. No new branch or Fourth Dimension build
was substituted during this work.

## Final package and scope

`coasters-launch-systems-0.4.0.jar`, SHA-256
`e8c9eb862a0fe047ad05d0a8b61eea84263ce2e241eeaf3f61514cf229441933`.
Persistent artifact: `/mnt/NVME4/launchsystems-runs/ls04-integration-20261122/build/libs/coasters-launch-systems-0.4.0.jar`.
Java 21.0.13.1 / Gradle 9.2.1 `build` passed **29/29** unit tests, no failures,
errors or skips. All **155** compiled main classes and **125** source resources
are in the JAR; JSON resources parse and metadata is expanded. See
[build output](final-build.txt), [exact source/input hashes](artifact.json) and
[packaged metadata](packaged-metadata.toml). This integration check launched no
Minecraft process. Its separate build/cache paths are under the persistent root.

The default `fd-current` profile stays on Minecraft 1.21.1, NeoForge 21.1.256,
Create 6.0.10-280, Coasters Simulated 0.1.5, Sable 2.0.6, Companion 1.6.0,
Veil 4.3.2, Flywheel 1.0.6, Ponder 1.0.82+mc1.21.1 and Registrate
MC1.21-1.3.0+67. The optional exact Fourth Dimension API1/schema8 artifact is
`df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e`,
source `8cebab779da3311b39e3471bbfb41ebf1dafcd19`, already integrated/pushed to FD
main as `235255cf21845cda0e4921b5d036425c55a9a66b`. No FD JAR is bundled.
Newer dependency candidates remain explicitly deferred. Legacy reproduction
retains its original pins.

This cumulative milestone supplies tire, LSM, LIM, finite flywheel, hydraulic
and pneumatic launches, friction-fin final hold, passive magnetic trim and
heated skid braking, plus local controllers, recipes and Ponder. Original
eligible FD family linear/brake certificates carry forward; cable requires an
ordinary or original Arrow certified socket and bounded loaded consist. Modern
FD catalog certificates (0.5), bounded multi-train interoperability (0.6),
linear station/section transfers, positive PAX delegation and optional addon
control handoffs are outside this acceptance. PAX and unqualified owners retain
explicit vetoes. 0.4 is a completed development milestone, not a 1.0 support or
public publication claim.

## Combined acceptance

| Packages | Evidence and outcome |
| --- | --- |
| LS001–LS005, LS101–LS106 | [Accepted 0.1 integration](../../0.1/integration-20261122/README.md): unchanged foundation, three normal-built ordinary/Arrow/Free Spin rides, actual occupied operation, multiplayer/rejoin, native capture and separate-process ownership recovery carry forward. |
| LS201–LS205 | [Linear/skid acceptance](../../ls02-linear-20261122/README.md): seven current positive/veto cases, two corrected authority-loss cases, one current Arrow skid case, and two separate-JVM linear restart phases passed. Normal-built occupied mixed LSM/LIM swing completed all three legs; independent skid dissipated speed with unchanged rider and no fin clamp. [Independent normal operator input](../../0.2/observer-20261122/README.md). |
| LS301–LS303 | [Cable/current authority](../../0.3/cable-current-20261122/README.md): Arrow dispatch and shaft loss 2/2, exact-owner/negative brake lease 1/1, active catch release/idle persistence 3/3 separate JVMs. [Normal-built occupied flywheel and saved-world physical return](../../0.3/cable-current-20261122/PLAYER.md) passed; pressure provides complementary normal live-HOME retirement. |
| LS401–LS403 | [Pressure acceptance](../../ls04-pressure-20261122/README.md): four store tests, current Arrow shaft/guide loss 4/4 and return interruption 2/2 passed. Normal recipes, live pressure/oil status and updated pressure Ponder passed. Both occupied pressure launches reached actual HOME, recharged and retired normally while preserving the parked train lease; pneumatic dispatched at natural partial charge. |

The original native mechanics, loaded family, power/support/chunk loss and
saved-world evidence is reused as itemized in the package records. Historical
artifact identities remain accurate; the version bump does not relabel earlier
runtime candidates or require another sweep.

## Changed behavior and targeted validation

A suspended Fourth Dimension lease now retains **brake-only** permission for
the same loaded controller identity and exact complete local/native consist.
Normal propulsion, dispatch and capture still require a live authority token.
The cable controller now obtains native authority after its local reservation.
Negative tests reject foreign providers, replacement owners, changed consists
and missing reservations; affected linear/pressure faults physically hold with
no replay. Independent review found no actionable defect in these changes.

The real released carriage originally stopped short of home against a nominally
face-tangent full-block cabinet. Read-only geometry identified the clearance
condition; removing temporary stones did not fix it and that failed hypothesis
is preserved. All cabinet housings now have matching symmetric 1/16-block inset
collision/outline and rendered shells. The same saved carriage then physically
reached its original home without changed mass, forces, tolerances or injected
motion. Pressure cycles reuse that corrected geometry. Real client inspection
covers the changed housing and pressure teaching pane; unchanged models and
other guides retain accepted visual evidence.

Pressure status exposes finite work, phase, readiness, pressure and hydraulic
charged/returned oil. The oil inventory is analytical readout of the existing
closed model, not a new external fluid network. The diagnostic command remains
opt-in, permission-gated and read-only; it can now inspect an exact loaded
ordinary native body as well as registered FD assemblies.

A parked train's section reservation remains retained after carriage return.
Normal live-HOME controller disarm can retire its released carriage while
preserving that lease. Restarted idle retirement still requires the empty,
disarmed matching section; no occupied ownership is silently cleared. The
saved-world inspection and live-HOME service proofs combine without relaxing
these distinct guards.

## Delivery and cleanup

All required evidence and owned-runtime cleanup are reconciled. The integration
commit containing this record delivers the package on the existing task branch. No release tag or
public binary publication is part of this Git-delivery request. Push-triggered
GitHub Actions status is reported separately; prior runs could not start due
to the repository account's payment/spending-limit restriction.

All linear/skid, flywheel, pressure and independent observer runtimes are
stopped. [Coordinator cleanup verification](runtime-cleanup.txt) confirms PID0;
individual records preserve server saves and any requested signal termination.
The pressure Observer exit143 was requested client shutdown, not a gameplay
failure. Flywheel cleanup is in its own linked record. No owned live runtime
was handed off or left idle.
