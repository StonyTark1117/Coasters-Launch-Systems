# Performance and resource accounting

LS903 uses one disclosed integrated workload and the accepted mechanism accounting
records. The [November 27 measurement](evidence/0.9/performance-20261127/README.md)
is a server-side observation, not a hardware-independent performance guarantee.

## Measured callback cost

The scene contains two admitted original Arrow trains, six native bodies, eight
native seated synthetic riders, two LS controllers, eighteen tire cabinets and
thirty-one friction-fin cabinets. It exercises local launch/capture, transfer
across a native curve, and a conflicting native route reservation that retains
the hold. Its eight edges span about 186.27 blocks; thirty chunks are forced.
Each loaded train has mass 20.2 in the game's native units.

After 400 game ticks of setup/settling (220 after equipment installation), every
LS equipment callback was sampled until the final conflict assertion. There
were 1,552 samples covering 38.80 simulated seconds in 5.242 wall seconds. This
is NeoForge's unpaced GameTest execution, not a real-time 20-TPS server run or a
long-duration warmed-JVM throughput claim. Quantiles use nearest rank.

| Callback phase | Samples | Mean ms | p50 ms | p95 ms | p99 ms | Maximum ms |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| All | 1552 | 1.659 | 1.603 | 3.354 | 4.123 | 11.465 |
| Settled before launch | 2 | 2.731 | 2.469 | 2.992 | 2.992 | 2.992 |
| Local launch/capture | 240 | 2.994 | 2.813 | 4.013 | 6.111 | 11.465 |
| Route transfer | 1232 | 1.457 | 1.427 | 2.849 | 3.680 | 10.231 |
| Conflict hold | 78 | 0.726 | 0.677 | 1.015 | 1.401 | 1.401 |

These final measurements include the saved provider-owner ledger. The earlier
[pre-ledger observation](evidence/0.9/performance-20261127/pre-ledger/measurement.json)
is retained as history, not the release headline or an isolated estimate of ledger
overhead: host concurrency differed. Two callbacks fell between collector start
and the first launch phase update; the three active phase sample counts are unchanged.

Timing surrounds `EquipmentRuntime`'s existing physics callback body, including
its synchronous controller, cable-recovery, authority, contact and force-queue
calls. It excludes recorder aggregation, other event listeners, the native
solver and the remainder of a Minecraft tick. Wall-clock samples can include
thread descheduling, JIT and garbage collection; they are not CPU-only samples.
No server-tick, Sable-solver, client-frame or end-to-end latency distribution is
claimed. This tire/fin route represents shared equipment and route authority
cost, not a separate timing certification of every mechanism or family.

The host was an AMD Ryzen 9 7900X3D (12 cores/24 threads), 61 GiB RAM, Linux
7.3.5-zen1-1-zen and Java 21.0.13.1. The server had a 2048 MiB heap and verified
6 GiB process-group cap. It ran headlessly, with **no renderer**, and configured
view/simulation distances of four. About 39 GiB host memory was available before
launch. One other Minecraft server, two private clients, a Gradle daemon and
agent/host work were present; this was not an idle or CPU-isolated benchmark.
The one-second resource sampler covered startup, workload, save and shutdown.
Its maximum observed RSS was 1,768,443,904 bytes and maximum observed cgroup
peak-so-far was 1,747,775,488 bytes. These distinct OS counters are sampled
observations, not the heap size or a claimed final complete process peak. Full environment and sampling
records are linked with the measurement.

## Resource reconciliation

Work, force, mass, pressure and heat below use game-native units. They do not
claim SI calibration, manufacturer efficiency or total mechanical work after
constraints and collisions. Compatible accepted records remain valid; a timing
collector does not reset their qualification.

| Mechanism | Reconciled physical/accounting claim | Accepted evidence |
| --- | --- | --- |
| Tire and friction fin | Contacted shaft work bounds positive tire impulse work; whole-consist force/acceleration and rated spring hold remain bounded. The measured route delivered 74.59111 positive queued-work units over 891 force steps, peak speed 1.32518 blocks/s, then no additional positive work through the conflict hold. | [Circuit and original mechanics](VALIDATION.md), [0.1](evidence/0.1/integration-20261122/README.md), measured route log |
| LSM and LIM | The native observer reconciles buffer draw, accepted shaft charge, cooling, heat and actual queued translational/angular impulse work, including adjacent-pad cross-work. Active braking spends energy and turns removed motion plus input into heat; it does not refill the buffer. | [Native linear accounting](evidence/linear-packaged-combined.txt), [current acceptance](evidence/ls02-linear-20261122/README.md) |
| Passive eddy and skid | Motion is dissipated into the heat store with cooling and derating; neither passive trim claims a zero-speed static hold or energy generation. | [Eddy measurements](evidence/brakes-packaged-combined.txt), [current native/occupied skid](evidence/ls02-linear-20261122/README.md) |
| Flywheel/catch | The carriage observer compares actual queued linear/angular impulse work against store draw and heat; charging is bounded, and physical return uses shaft work. Example accepted single-car pull: draw 376.368578 = work 319.913291 + heat 56.455287; per-step impulse accounting error below 9e-16. Catch/guide constraints transmit the force; these numbers are pre-constraint input work, not a total train-energy identity. | [Native cable accounting](evidence/cable-dev17-focused-combined.txt), [current cable/recovery](evidence/0.3/cable-current-20261122/README.md) |
| Hydraulic | Finite polytropic accumulator work decreases on discharge; fixed oil inventory is partitioned between accumulator and return reservoir. Physical release/return/HOME precedes recharge/service. | [Store assertions](../src/test/java/dev/coasterslaunchsystems/StoredEnergyTest.java), [current pressure acceptance](evidence/ls04-pressure-20261122/README.md) |
| Pneumatic | Finite isothermal receiver work and expanding-cylinder pressure determine available force; discharge reduces pressure, shaft charging restores it. The occupied partial-charge run started at 843.9776/1636.5858, reached HOME and recharged to capacity. | [Current pressure acceptance](evidence/ls04-pressure-20261122/README.md) |

For a concrete linear example, the retained LSM braking record reports work
-56.153873, input 31.540509 and heat 87.694381: input minus work equals heat.
Its maximum per-step discrepancy was 3.28e-14. Buffer snapshots account for
charging and cooling separately; subtracting two arbitrary live snapshots
without those terms is not an energy audit.

Eight core accounting/math/store classes in the measured package are byte-for-byte
identical to the accepted 0.4 package, as recorded in
[accounting class parity](evidence/0.9/performance-20261127/accounting-class-parity.json).
The later accepted contact, coupling and authority records cover their changed
integration paths. Pressure inventory and expansion are finite game models;
the evidence does not assert that pneumatic exhaust is regenerated as oil,
shaft energy or another store.

## FE and the optional boundary

FE integration remains unimplemented optional LS805. The release source exposes
no `IEnergyStorage`, `receiveEnergy`, `extractEnergy` or NeoForge energy capability.
There is no FE-to-shaft or pressure-to-FE conversion path to cycle. Source charging
is bounded Create shaft work; active linear braking consumes its finite buffer;
the accepted repeated-transfer store test proves a finite charge cannot yield
more accumulated draw. This is an explicit absent-integration boundary and
internal accounting result, not a claim of tested third-party FE compatibility.

## Collector and reproduction

`CallbackTiming` is disabled unless the game JVM starts with
`-Dcoasterslaunchsystems.performance=true`. The disabled hook calls the unchanged
physics body without clocks or sample allocation. The opt-in recorder is bounded
to 100,000 samples, reports dropped samples, and has no influence on readiness,
forces, reservations or timestep. A validation fixture starts/finishes one
level's session; normal play does not start a session automatically.

The dedicated namespace is `coasterslaunchsystems_performance`; use the exact
selected FD artifact and persistent owned build/game/cache paths in the linked
receipt. Its original route assertions still determine success. Only the
collector's quantile test and this finite workload were used for LS903;
the workload was repeated once after the provider ledger changed the measured path;
no unrelated matrix or numerical performance target was introduced. The owned
server saved all dimensions and exited successfully; no client/display started.
