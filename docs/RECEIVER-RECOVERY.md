# Retained transfer receiver recovery

Development checkpoint 0.0.1-dev.12. This adds an inspection workflow for a receiver removed during an active, reserved fixed-section transfer. It does not complete the 0.1 or 0.4.0 release gates.

## Operator workflow

1. Let the complete train stop on supported spring fins. Shift-use the source controller with an empty hand to disarm it; its reservation stays intact.
2. Replace the missing receiving controller at its exact saved position. Use the span tool on the original receiving anchors in their original order, then on the empty replacement to bind it. Keep it disarmed; keep the original module blocks and their bindings intact.
3. Shift-use the span tool on the retained source. With a vanilla compass in the offhand, use the tool on the replacement controller.
4. The tool queues one inspection for the next accepted native guide observations. Use the source controller to read its result. A refusal leaves the source's train and route reservation intact and explains the failed check.
5. After success, both controllers remain disarmed in inspection. Arm the receiver first, then the source. Wait for departure readiness, take the top input low, then supply a fresh rising edge. A high input from before the interruption cannot replay departure.

Normal module linking cannot take equipment from a missing occupied controller. A successful inspection changes only the receiver ownership links and paired controller metadata; it retains module identity, geometry, roles, heat, native spring handles and the complete train lease. It grants no impulse, pose change, rotor command or boarding readiness.

## Inspection contract

Controller schema 5 saves a receiving-equipment manifest and its station-capture setting at destination linking and refreshes it when departure reserves the receiver. Repair requires an active retained transfer, a new controller identity at the exact saved position, original anchor order, unchanged ordered route fingerprints and loaded route chunks. Every original source and receiving module must still be available, armed, fault-free, correctly bound and owned by its recorded controller identity. Receiving modules are recovered by exact position and UUID; proximity never reconstructs an owner.

The inspection rejects conflicting route controllers/equipment, foreign accepted train contact and native chains. It verifies every reserved native chassis, complete identical consist membership, one provider, deduplicated loaded bodies, certified tire/fin interfaces, exclusive-owner vetoes, the supported mass/brake rating and receiving stopping coverage. Native chassis translation and the physical tire-interface velocity must both be below 0.03 blocks/second, with an actual native fin clamp or a positive vehicle-owned hold. Vehicle faults, missing bodies and unavailable support cannot be repaired into a motion grant.

While the receiver is missing, its exact journaled friction pads retain the source's brake coordination owner. This keeps adjacent pads from becoming competing orphan owners. Orphan tire drives remain denied. More than one possible retained owner remains a conflict. On successful repair, the full receiving lease and incoming-source link are reinstated before any later departure can be requested.

Inspection requests are transient and consumed once in the native physics phase; they are never written to saves or replayed after loading. Server loads remain disarmed in inspection. Controller schemas 1–4 cannot invent an equipment manifest: restore their original controller data and inspect the pair instead. Replacing a module, changing the route, or repairing a source that was itself removed is outside this workflow. Real chunk/restart/multiplayer acceptance and rendered operator setup remain open.

## Validation

Native acceptance and artifact evidence are recorded in [validation status](VALIDATION.md). The recovery suite uses real span-tool/compass interactions, loaded ordinary coupled trains and eligible pinned Fourth Dimension vehicles. Recovery is followed by separate arming and a fresh redstone departure; the native train must complete its tail handoff and final hold with passengers and joints retained. Adverse inspections retain ownership and add no tire work.

Focused reproduction:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged -PlsTestNamespaces=coasterslaunchsystems_recovery --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PlsPackaged -PlsTestNamespaces=coasterslaunchsystems_recovery -PlsFourthDimensionJar=/tmp/coasterslaunchsystems-validation/dependencies/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

The packaged runs consume a real binary JAR and a disposable native test world. The immutable schema-4 Fourth Dimension JAR is copied outside its active checkout; no upstream checkout or process is modified.
