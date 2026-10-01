# Reserved circuit prototype: 0.0.1-dev.10

This page preserves its original checkpoint scope. See [dev.12 receiver recovery](RECEIVER-RECOVERY.md) for inspected replacement of a missing receiver. See [dev.11 station scope](STATIONS.md) for optional controller capture, measured boarding readiness, API 5 and schema-4 persistence.

This extends the adjacent-section handoff with a unique, unbranched fixed-rail route between straight equipment sections. It is partial LS004/LS104/LS105 progress toward 0.4.0. The native fixture includes repeated laps and a provider-owned station capture/boarding prototype; automatic operator station capture and rendered demonstration rides remain unfinished.

## Configuration and ownership

Use the [section setup](SECTION-TRANSFERS.md) for inlet tires, powered spring-pad release, a continuous downstream fin bank and exit transport tires. A destination may now follow intermediate native curves. Its ordered rail direction must continue from the source through matching endpoint tangents; branches, a loop before the receiver, moving hosts, ambiguous rails and discontinuous joints reject linking. The route is limited to 128 edges and 2048 arc blocks.

Link the destination while the source is disarmed and empty. Linking records every ordered span and geometry fingerprint. A fresh redstone edge requests departure only after the complete train is held, the receiver is available and the loaded stopping envelope fits. Both sections reserve the complete consist before vehicle dispatch or pad release. The source also claims every intermediate edge while transferring, including during a retained fault or inspection. Foreign controller/cabinet ownership, route occupancy and native chain ownership deny departure.

Only actual accepted native rail contact establishes train position. Intermediate curves have no synthetic propulsion; the native train coasts between source exit tires and receiver inlet tires. Each chassis uses its local rail tangent. The source clears only after every chassis reaches the receiver and its loaded enclosing body radius plus one block has cleared the receiver entrance. Callback order does not select a competing route owner.

The stopping certificate includes deduplicated loaded native mass, brake force, train length, measured speed, drive RPM and gravitational potential gain along the remaining route. Nonlevel routes receive an additional conservative sampling allowance. A speed bound recorded before departure cannot grow to excuse overspeed during travel. Downhill native acceptance is still pending; the passing circuit fixtures use level rails.

Graph revisions and native curve-handle revisions both invalidate controller, cabinet and route caches. Every resolved route must retain its original ordered fingerprints and loaded chunks. A handle edit without a graph revision therefore cannot silently alter the inspected route. Schema-3 controllers persist route spans and the departure speed bound; server loads remain disarmed in INSPECTION with retained leases and no redstone replay. Schema-1/2 destinations may migrate only an adjacent route. A longer route requires explicit inspected relinking.

## Native circuit and boarding fixture

The fixture builds two 64-block straight equipment sections joined by native U-turn curves. It exercises ordinary singles/coupled pairs, Arrow singles/coupled pairs and an individual Free Spin car. Ordinary and Arrow chassis carry four real seated riders each; the Free Spin car carries eight. Coupled trains use the native six-block coupler.

After the initial launch, fresh redstone requests perform two complete laps. Each receiving stop retains the complete lease. At the final source hold, the Fourth Dimension fixture calls its pinned public `VehicleOperations.capture`; it waits for public `Boarding.ready`, opens restraints through the public operation, dismounts the actual passengers, waits for native mass removal, boards new passengers, verifies native mass restoration, closes restraints and requests another departure. Ordinary carts exercise actual seat exit/reboarding through the same held-stop sequence. Six departure requests complete per positive fixture.

Capture is currently an explicit test/operator operation, not a new Launch Systems provider API operation or automatic station behavior. Fourth Dimension owns capture, readiness, restraints and rotor motion. Its exclusive PAX restriction remains unchanged. The fixture checks that propulsion is zero while capturing and boarding; calling capture alone does not establish boarding readiness.

The copied schema-4 Fourth Dimension `TrackPlacement.connect` produces primary curves whose optimized endpoint axes do not meet these straight sections tangentially. The fixture first uses that public placement operation, then edits the empty native curves to continuous U-turn geometry and invokes public `ProfileEditor.rebind` on the changed rails before creating vehicles. The runtime continuity check remains unchanged. This establishes the inspected empty-geometry/rebind workflow against the pinned artifact; it does not establish an upstream placement fix or general Arrow profile traversal.

Physics observers check accepted curve contact, retained whole-consist leases, actual riders, provider force/acceleration limits, per-cabinet contacted shaft-work budgets, carrier/coupler separation and independent Free Spin response. Native passenger mass must decrease by exactly 0.8 per removed rider and return after reboarding. No fixture sets train velocity, prescribes its path or writes rotor angles.

Two negative fixtures request departure with a foreign native cart on the intermediate curve, or after changing only the native curve handles while verifying that the graph revision stays unchanged. Both require HOLD, unchanged complete source ownership, an empty receiver lease and zero added launch work.

## Reproduction and remaining gates

Use the committed binary and run native worlds serially. Each GameTest invocation recreates its own disposable world.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PlsPackaged -PlsFourthDimensionJar=/tmp/coasterslaunchsystems-validation/dependencies/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

`-PlsTestNamespaces=coasterslaunchsystems_routes` selects just the new circuit/refusal fixtures for diagnosis. Full local and CI runs use both test namespaces by default. Evidence and artifact identity are recorded in [validation status](VALIDATION.md). [Development diagnostics](evidence/circuit-diagnostics.txt) preserve the fixture-layout and initial joint-continuity failures separately from acceptance evidence.

The pinned upstream JAR is SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`. The active Fourth Dimension checkout and processes remain untouched.

Remaining work includes automatic provider-owned station capture, operator readiness/signals, missing-peer repair, reverse/downhill/profile circuits, obstruction clearance, rendered survival instructions and three Ponder demonstrations, real restart/chunk/multiplayer acceptance and all remaining linear-motor/skid/catch/flywheel/hydraulic/pneumatic mechanisms. This prototype does not complete the 0.1 or 0.4.0 gate.
