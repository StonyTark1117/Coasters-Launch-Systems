# LS904 occupied FD mechanism demonstrations — 2026-11-27

Owner `/root/rides`, checkout `/home/braydon/Documents/Coasters: Launch Systems`,
existing branch `codex/launch-systems-0-4-0`, starting HEAD `ba555e9`.

## Remaining criterion and carried evidence

LS904 requires ordinary and occupied Fourth Dimension demonstrations for all six
launch mechanisms. The accepted 0.2 mixed LSM/LIM player ride and 0.4 hydraulic /
pneumatic player rides use ordinary chassis; their FD native tests use synthetic
passengers. Those results remain accepted for their stated claims. This record
closes only the four remaining **real occupied FD** demonstrations. Tire rides
and the occupied translated Fixed Inverted flywheel remain carried forward.

The new validation-only selector wrapper reuses the existing original Arrow
linear and pressure rigs, with no synthetic boarding, automatic restraint closure
or scripted dispatch. Authored native rail, car, installed hardware, motors,
module bindings and charged cabinets are disclosed setup. Normal construction,
recipes, detailed physics/accounting, family certificates and shared lifecycle
acceptance remain linked in prior records; no new construction claim is made.

Each independent rig requires a real player seat, native restraint operation,
explicit controller arm/recovery and a fresh normal request; acceptance observes
actual motion, matching mechanism resource use, retained seat and downstream
hold. Hydraulic and pneumatic additionally observe physical carriage HOME.

## Owned runtime

Persistent root `/mnt/NVME4/launchsystems-runs/ls09-occupied-20261127`, private
Xvfb `:396`, game/RCON `25831/25832`, unique build and project cache, Java 21.
Units `fd-ls-release-occupied-20261127-{client,server,xvfb}`. Client heap 1536M,
Minecraft-only cap 6GiB; server heap 2048M/cap 4GiB. Before preparation: 45GiB
available host memory, 145GiB free on persistent NVME4. No 0.6 world is reused.
Exact frozen artifact identities, action/readback results and cleanup are recorded below.

## Artifact and fixture boundary

The demonstrated LS package is `coasters-launch-systems-0.9.0.jar`, SHA256
`6f8a8adf50528fcb5597c8a13e691a7971e8f616d81ed8ff8c77fb7ea4aeba85`;
FD is the accepted tagged route/operator API1 package SHA256
`d676d730a02c6f9abe217985db03a3bca424892f06236a0cf64e066b82e87738`.
Root independently matched the occupied package's 109 production classes against
the final release candidate. Later changes preserve the older catalog fixture's
completion behavior and add lifecycle assertions; they do not change these demos.

The original Arrow lanes are 64m straight, starting near distance8. Pressure
lanes are at y224, linear lanes at y192. Each has its own controller and modules.
Dev boards by normal empty-hand Use, opens the native Rotation Tool editor and
closes restraints normally. Observer uses normal empty-hand ShiftUse to arm and a
separate normal lever Use for the fresh high edge. Supplied materials, platforms,
lever placement, player poses and mode changes are recorded setup; no train
position, velocity, rider attachment or dispatch is injected. Temporary boarding
strips are removed before motion. Native Capture, Open restraints and Shift exit
are normal inputs after the held arrival. Native Capture deliberately pauses the
external owner and leaves the inspected LS reservation retained.

## Diagnosed setup attempts

Two initial setup worlds are retained, with no accepted dispatch from either:
`ls09-four-mechanisms` (package b02cb7f3) and
`ls09-four-mechanisms-live-refs` (package1e5dc6ab). Multiple consecutive vanilla
`test run` commands cancel earlier fixture preparation. In addition, Sable 2.0.6
`GameTestInfoMixin` removes intersecting sublevels on `GameTestInfo.succeed`.
The initial bootstrap called succeed at tick 200, so the body disappeared while
FD correctly retained geometry occupancy; retrying construction was rejected.
The corrected validation-only release bootstrap stays active until shutdown,
refreshes cached body references by their unchanged UUID and prepares one lane
at a time. No production safety guard changed. The accepted world is
`server/ls09-occupied-live-bootstrap`; all preparation, movement and service
receipts below come from it. Both earlier worlds were saved and stopped cleanly.

## Completed pressure outcomes

| Mechanism | Native chassis / occupied seat | Peak speed | Travel distance | Finite store / physical return |
|---|---|---:|---:|---|
| Pneumatic | `30dd6a5e-297f-423b-b338-0137a34eb959` / `5401ecb4-7baf-4aaf-bb93-e0d5ae19f237` | 2.88459m/s | 8→23.36456m | 1636.59→1305.94 work; carriage `be0f7e60-80b9-43d0-a746-804a5ca2b6c2` physically HOME at `(176.5,221.35251,-887.5)`, linear/angular velocity zero |
| Hydraulic | `2bc2b9c3-cfdc-4388-8a24-bdf6f9d0ac98` / `3646aed9-fa55-4b24-ac0f-59f07018e674` | 2.88412m/s | 8→23.36139m | 778.77→448.14 work; carriage `c6e81d4b-6724-485e-87a3-c59d38c1ed59` physically HOME at `(128.5,221.35251,-887.5)`, linear/angular velocity zero |

Both native loaded masses changed18.0→18.8 on actual boarding. The same native
seat remained occupied through RUNNING, BRAKING and downstream HOLD with no fault.
Carriage journals independently cleared in-use/returning while physical Sable
readbacks proved actual HOME. Pneumatic normal arm/high were 07:27:42/07:28:28UTC;
hydraulic 07:34:24/07:34:58UTC. Native service opened restraints and normal Shift
exit reduced loaded mass back to18.0 at 07:32:18 and07:36:23 respectively. Observer
exterior views independently show the occupied stopped trains.

## Occupied linear outcomes

| Mechanism | Native chassis / occupied seat | Peak speed | Work / spent / heat | Maximum accounting error |
|---|---|---:|---:|---:|
| LSM | `422f9cfc-5965-4e65-84d7-31e7e032be04` / `e806f73f-8e7e-4c8d-b052-ea33bc1da4da` | 2.98508m/s | 105.15190 / 148.10817 / 42.95627 | 2.25e-13 |
| LIM | `090fc788-a037-4c40-915a-d7eb6405862c` / `7f5fdfee-918e-42e7-b068-d0838779d396` | 2.97706m/s | 106.30395 / 231.48890 / 125.18495 | 2.20e-13 |

Both use actual original Arrow passengers, loaded mass 18.8. LSM made 160 measured
force steps; LIM 229. Each moved from the authored start near z−887.5 to its
physical downstream stop near z−874, retained the same seat throughout, and
reached HOLD without native or controller fault. Energy spent equals mechanical
work plus heat within the measured numerical errors; LIM's greater loss remains
visible. Charged continuous motors replenish the finite banks after the pulse,
so final full charge is not used as a substitute for the per-step resource proof.

Normal LSM boarding/closure occurred 07:36:58/07:37:19UTC, arm 07:39:54,
fresh high 07:40:32. LIM boarding/closure occurred 07:42:23/07:42:45,
arm 07:43:13, fresh high 07:43:46. Native Capture deliberately pauses external
authority for service; it does not erase the retained reservation or replay the
held-high lever. LSM then opened restraints and exited normally at 07:41:42.

## Evidence and cleanup

**Four of four remaining occupied FD demonstrations passed.** LIM normal native
Open restraints and Shift exit completed at 07:45:10/07:45:12 UTC; loaded mass
returned to 18.0. Dev normally disconnected at 07:45:27. Observer separately
normally disconnected and its owner stopped both observer units. Server list
confirmed zero players before `save-all flush` returned `Saved the game`.
The server saved all dimensions and exited cleanly at 07:45:59 UTC. All three
owned units are inactive/dead, MainPID0, Result success, exit0; exact executable
and argv inspection found no remaining owned telemetry or video helpers.
See [runtime-cleanup.txt](runtime-cleanup.txt). The last live client measurement
was 2.547GB peak under its 6GiB cap; server peak was 1.612GB under its 4GiB cap.
No owned runtime remains active and all three saved worlds are preserved.

- [state-transitions.jsonl](state-transitions.jsonl) retains complete controller,
  equipment, native car, carrier occupancy, loaded mass, motion and accounting
  readbacks at meaningful state changes, plus final readings. The `players`
  field is the server player list; each rig's `carriers.occupants` and loaded
  mass identify the actual occupied rig. Earlier completed rigs remain visible
  while Dev changes to the next independent lane.
- [input.jsonl](input.jsonl) contains the actual private-client input receipts;
  before/after records for the same timestamp are one input, not a repeated click.
- [setup-and-readbacks.jsonl](setup-and-readbacks.jsonl) preserves disclosed
  setup and exact physical carriage HOME queries, including diagnosed earlier
  setup attempts. No RCON credential or run configuration is included.
- [accepted-server.log](accepted-server.log) is the final JVM's native server
  log, from successful bootstrap through saved shutdown. Its timestamp timezone
  is America/Phoenix; input and telemetry timestamps are UTC.
- Selected boarding and native-service screenshots are stored alongside this
  receipt with [selected-images.json](selected-images.json). The coordinating
  operator's independent normal arm/high and exterior receipts are under
  [integration observer receipt](../integration-20261127/observer/).
- [persistent-evidence.sha256](persistent-evidence.sha256) identifies full raw
  telemetry, input images, videos, setup diagnostics and build logs retained in
  the persistent run directory. [tracked-evidence.sha256](tracked-evidence.sha256)
  identifies the curated Git evidence; the `.log` receipt must be force-added if
  ignored by the repository's general log rule.

These are player acceptance outcomes, not synthetic GameTest-success counts.
The validation bootstrap intentionally stays active while clients operate it;
server shutdown ends it after the required outcomes. Existing native mechanism,
family, construction and lifecycle evidence remains accepted without replay.
