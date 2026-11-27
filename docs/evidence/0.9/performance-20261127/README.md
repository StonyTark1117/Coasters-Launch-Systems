# LS903 finite performance and resource-accounting receipt

Status: complete, measured and stopped. Owner `/root/bridge`, existing checkout
`/home/braydon/Documents/Coasters: Launch Systems`, branch
`codex/launch-systems-0-4-0`, starting HEAD `ba555e9949853b142d9a1e2d7d546ee4ac24d1f2`.
Source edits are the opt-in `CallbackTiming`, a method-local wrapper around the
unchanged `EquipmentRuntime` body, the new `PerformanceGameTests`/template and
statistics unit. The accepted route observer gained package visibility only.
Other agents' work was preserved; no commits or dependency changes were made here.

The complete interpretation and accounting reconciliation are in
[PERFORMANCE](../../../PERFORMANCE.md). This receipt owns LS903 only; gameplay,
matrix, migration and publication remain their assigned packages.

## Exact package and workload

Measured LS 0.9.0 JAR SHA-256
`d18346da2fd1c43b8d5d10bd8076327086514a22776f4ffcd9318846b7807225`.
FD selected route/operator API1 JAR SHA-256
`d676d730a02c6f9abe217985db03a3bca424892f06236a0cf64e066b82e87738`.
Both exact paths, Java arguments, hardware and host context are in
[environment.json](environment.json). Current-core dependencies retain Minecraft
1.21.1, NeoForge 21.1.256, Create 6.0.10-280, Simulated Coasters 0.1.5,
Sable 2.0.6, Veil 4.3.2, Companion 1.6.0 and Flywheel 1.0.6. There is no client
or renderer in this measurement. FD's dirty newer primary source was not used.

Persistent root `/mnt/NVME4/launchsystems-runs/ls09-performance-20261127`, with
owned `build`, `project-cache`, `server-final/world` and `evidence-final` directories on XFS
NVMe storage (145 GiB initially free). Owner unit
`ls09-performance-20261127-final-server.service`, 2048 MiB heap, verified 6 GiB cgroup,
configured port25841, no RCON or graphical display.

`PerformanceGameTests` reuses the exact accepted physical-conflict route scene:
two real native Arrow chassis, four carrier bodies and eight seated synthetic
native riders; two controllers, 18 tire/31 fin cabinets and 49 shaft sources;
eight edges/six native reservation blocks. It waits through tick400 before
dispatching and measuring, including 220 ticks after installing equipment.
The measurement ends immediately before the original fortieth conflict-hold
tick assertion. Every active level callback is retained; there is no decimation.
The 78 hold samples represent 39 ticks at two physics callbacks/tick.

This is unpaced GameTest execution: 1,552 callbacks, 0.025s physics timestep,
38.80 simulated seconds and 5.242238 elapsed wall seconds. Two callbacks were
recorded between collector start and the first launch phase update; the active
launch/transfer/hold counts remain 240/1232/78. It is neither a
20-TPS wall-clock endurance test nor a client smoothness measurement. Results
are descriptive and have no invented pass budget. Exact distributions are in
[measurement.json](measurement.json).

## Verification and resource record

- [Collector unit](collector-unit.xml): 1/1 passed, nearest-rank quantiles and
  nanosecond-to-millisecond conversion; input observations are not mutated.
- [Packaged preparation](prepare.txt.gz): Java21/Gradle9.2.1, successful isolated
  direct launch generation. The collector unit was already accepted and was not
  rerun for the ledger-only change. No failed Minecraft attempt preceded this run.
- [Native log](native.log.gz): one required fixture passed. Original route
  assertions confirm actual positive work, complete riders/ownership and no
  additional work when the second train's next block conflicts.
- [Resource samples](resource-samples.jsonl): one-second OS/cgroup observation (23 live samples),
  including live MemoryMax6442450944 and later inactive/PID0. RSS and cgroup
  counters have different accounting boundaries; the short run does not supply
  a final complete RSS peak. Host load and CPU pressure accompany each sample;
  [resource-summary.json](resource-summary.json) records maxima and exact limits.
- [Shutdown](shutdown.json): all dimensions saved, natural successful exit,
  inactive/dead/PID0. No owned Minecraft, display or helper remains active.
- [Measured production class hashes](measured-production-classes.json) allow the
  coordinator to reconcile the final package without a hash-only replay.
  [Production parity](production-parity.json) verifies all 110 non-GameTest classes
  match the coordinator integration JAR `6f8a8adf…`; validation fixture hashes differ.
  [Source hashes](source.sha256) identify the exact collector/fixture/hook.
- [Accounting parity](accounting-class-parity.json) shows eight relevant math
  and finite-store classes unchanged from the accepted 0.4 artifact; linked
  current mechanism evidence in PERFORMANCE supplies the integration claims.
- [Accounting reconciliation](accounting-reconciliation.json) independently
  parses the retained linear/cable examples, verifies draw minus work minus
  heat, and records the source-only absent FE integration boundary.

## Superseded pre-ledger observation

The first measurement and immutable source/package receipts are preserved in
[pre-ledger](pre-ledger/README.md). That historical README describes its original
run, not the final release state. `6ebf1575…` lacked the saved provider-owner ledger.
The ledger changed the measured resolution path, so the identical finite scene
was run once against stable final production. Different concurrent host work
means the two observations do not isolate the cost of that ledger. The original
JAR is preserved as `artifacts/pre-ledger-6ebf1575.jar` under the run root.

## Reproduce only when the claim changes

Use Java21 and an independent persistent root/cache. Prepare with `test --tests
dev.coasterslaunchsystems.CallbackTimingTest createGameTestServerLaunchScript`,
`-PlsPackaged`, `-PlsTestNamespaces=coasterslaunchsystems_performance`, the
explicit `-PlsFourthDimensionJar` above and owned `lsBuildDirectory` /
`lsGameTestGameDirectory` / `--project-cache-dir`. The [direct command](direct-run.sh)
adds `-Xmx2048M -Dcoasterslaunchsystems.performance=true` to the generated game
JVM. Launch only that Java process under a memory-limited owned systemd unit;
Gradle preparation runs outside it. Preserve the emitted `LS_PERFORMANCE` JSON
and ordinary route/pass/save markers. This accepted measurement does not need
a second run merely because documentation, version metadata or unrelated
validation fixtures change.
