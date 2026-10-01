# Native cable mechanics — dev.18

This is partial LS301–LS303 implementation toward the requested 0.4.0 release. The socket, carriage and guide bed have registered models and recipes. A kinetic flywheel cabinet supplies the native catch from its own persisted finite store. A placed ride controller installs and dispatches that catch for an occupied ordinary cart, then uses linked friction fins for the stop. The installed carriage now has a registered Sable loading ticket and a saved UUID/home journal. Actual restart loading, inspected reclamation and broader gameplay acceptance remain required.

## Physical connection

A separate Sable sublevel contains the carriage jaw, connected steel spine and symmetric ballast below the train. A world constraint locks lateral/vertical translation and rotation, with longitudinal limits from home to the guide end. The train retains its upstream rail guide. Its separate socket occupies two cells beneath the chassis bearing; tire strips, fins and seat magnets cannot stand in for it.

The jaws leave collision clearance. Home uses the upstream accepted-bearing convention: sampled rail point plus `5/16` block along track-up, then two blocks downward. Plot-origin placement's half-block offset is different. Geometry must be fixed, straight, level and unbanked. Release requires at least four carriage braking blocks before the guide end. A continuous placed guide bed, five blocks below the accepted rail bearing, must be loaded across the full carriage travel. Fixtures use 12 blocks of pull and a 20-block guide.

Engagement requires a stopped carriage at home, socket alignment within 0.03 block, and a stopped, supported, ready whole consist. Checks cover provider/consist/body identity, installed certificates, finite mass, exclusive ownership and upstream chains. The mass rating is 64 native units. Ordinary socket-equipped carts and Arrow on the pinned schema-4 Fourth Dimension adapter are eligible. Other articulated families remain denied; installing a socket does not override that restriction.

A native longitudinal catch motor constraint connects carriage to chassis with a 256-unit force cap, finite compliance and damping. Upstream guides/joints retain other degrees of freedom. Only the carriage receives queued propulsion impulse. Catch transfer, release, braking and return use native physics without train/carriage reset teleports, velocity setters or carrier-angle writes.

States are `HOME → ENGAGED → PULLING → RELEASING → RETURNING → HOME`, plus retained `FAULT`. Dispatch revalidates the stopped whole consist and delegates to its unique vehicle owner. Pulling rechecks support, hardware, identity, loaded mass and ratings. Loaded guide chunks, placed guide blocks and its geometry fingerprint remain required. A handle-only edit is detected without a graph revision. Removing a guide block during pull faults the rig and stops further resource draw. The retained native guide joint and catch remain present in the faulted prototype; physical loss of guide support and recovery need further work.

The catch releases at configured travel. A bounded brake stops the carriage, then a slow shaft-budgeted force returns it. Home requires actual return and near-zero speed; premature engagement is denied. The controller retains a whole-consist section reservation while the train occupies its block. Disarming after physical home return retires the native carriage and clears its installation journal; it does not clear an occupied train reservation.

## Placed controller path

On one fixed, straight, level, unbanked edge, install the catch socket on a certified chassis and continuous guide blocks beneath 20 blocks of travel. Bind one flywheel cabinet at the catch home to the edge with the span tool. Bind a ride controller to the same directed anchors and link the cabinet, one powered friction fin overlapping the home contact, and a continuous downstream friction-fin stop bank beginning beyond the 12-block release point. The sample test uses home 8, release 20, guide end 28 and stop modules at distances 23–43. Power the flywheel and launch fin from Create shafts; arm the modules and controller. The held, dispatch-ready train causes the controller to install and physically engage the carriage. Its READY signal precedes a fresh redstone dispatch pulse.

The controller rejects mixed drives, section transfers, missing or misplaced guide/fin equipment, unauthorised consists, insufficient charge or stopping coverage. Only the fin beside the catch is released during launch; downstream spring pads remain applied. A 64-unit loaded mass ceiling, current vehicle brake rating and stopping envelope gate dispatch. The finite cabinet store funds each pull step. A fault or vanished controller puts an installed catch into native emergency hold. The saved installation/catch journal blocks removal or silent rearming after interruption; real restart inspection and reclamation remain open.

The ordinary-cart controller fixture uses only placed cabinet, guide, socket, fins and redstone for engagement, dispatch and whole-train stop. An occupied pinned Fourth Dimension Arrow controller fixture passes when an independent native preparation clamp has already settled the captured vehicle. That fixture proves provider-owned dispatch and native pull compatibility, but does not establish a player-operated Arrow staging brake. Unsupported Free Spin remains denied a cable certificate.

## Finite work

The placed cabinet's `FlywheelStore` has inertia 4, maximum angular speed 50 and capacity 5,000 native work units. Angular speed is `sqrt(2E/I)`. Drum speed is `0.2 × angular speed`, capped by target 3 blocks/second. Bounded clutch demand follows carriage slip and live provider mass/force limits; rising acceleration ramps at 4 blocks/second³. An empty store cannot synthesize launch speed.

The interface's translational/angular lever response bounds affordable force. Positive pre-constraint free-body impulse work spends `work / 0.85`. Conversion loss and negative work become heat, without energy credit. Paid work may subsequently dissipate through guides and joints.

Charging reads actual Create RPM from the placed flywheel cabinet: `0.9 × 8 × |RPM| × 2π/60 × dt`. Installation journals the native carriage UUID and home position and registers a Sable force-load ticket. Engagement journals an active catch and pauses background charging. The same saved store funds each carriage impulse; return spends the step's shaft budget first and charges only from the remainder. Physical return home clears the active catch journal. Controller disarm at home retires the carriage and removes its ticket and installation journal. Loading either journal keeps the cabinet disarmed and faulted, blocks edits, and requires inspected recovery after a real restart. Legacy installed journals without an identity also fail closed. The live active-catch reload fixture enters a retained emergency hold. Native constraint handles are transient and are not reconstructed by this checkpoint.

## Evidence and limitations

The native suite includes eleven standalone cases and four additional Fourth Dimension cases: occupied ordinary single/coupled forward/reverse, misalignment, empty storage, external power-loss hold, active reload denial, premature reset, handle-only edit, removed guide bed, increased load, occupied Arrow single/coupled forward/reverse and unsupported Free Spin rejection. Positive cases reach fixture whole-train hold and actual carriage return. Riders, native contacts/joints/couplers and per-step impulse/work reconciliation are checked.

Focused ordinary single/coupled peaks are about 2.95/2.93 blocks/second; Arrow about 2.89/2.73. The cabinet flywheel speed falls from about 22.47 to 15.60–17.80 radians/second by release in the ordinary cases. Catch separation stays below 0.0013 block, guide error below 0.000009, Arrow carrier separation below 0.000192 and coupled error below 0.000062. Per-step accounting error stays below `2e-15` native work units. Exact final evidence is in [validation status](VALIDATION.md).

The native joint getter reports zero for this motor constraint; that is diagnostic data, not proof of absent or bounded load. Whole-train longitudinal momentum change, counting each body once, measures net pull traction around 20–40 native force units below the configured cap. It is not a substep motor-load or complete drum certification.

The separate mechanics fixtures still use fixture-created native clamps after release. The ordinary controller test stops with placed, linked friction fins, while the Arrow controller test uses a preparation clamp before dispatch. The controller fixture also checks its live ticket, saved journal identity/home, legacy fail-closed loading and ticket removal at retirement. It does not simulate an actual server restart or demonstrate Sable reconstructing the carriage and its native constraints. Mechanics power-loss remains an external emergency-hold call; the controller path additionally detects shaft/pad power loss during launch, but interrupted-drive acceptance is still pending. The geometry case edits collinear handles while preserving rail support. Removed rail/socket/carriage, curved/moving guides, return interruption, real chunk unload/restart, thermal gameplay, multiplayer/rendered setup and repeated ride operation remain open. This does not complete a flywheel or 0.4.0 gate.

## Reproduction

Native worlds must run serially; Gradle recreates its own disposable world.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged -PlsTestNamespaces=coasterslaunchsystems_cable --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged -PlsTestNamespaces=coasterslaunchsystems_cable -PlsFourthDimensionJar=/absolute/path/to/verified-schema4.jar --no-daemon --max-workers=2
```

Standalone has twelve cable cases including the placed-controller ride; the exact Fourth Dimension binary adds five including the preheld Arrow controller ride. The default full suite includes this namespace. Upstream pin: SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`, source `a881e3d34415edd691baa166668f4f97d3a4a7e8`. The active Fourth Dimension checkout/processes are untouched.
