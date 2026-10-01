# Native cable mechanics — dev.14

This is partial LS301–LS303 implementation toward the requested 0.4.0 release. `NativeCableRig` is an internal native-physics prototype exercised by GameTests. The registered socket/carriage have original JSON models and recipes. Playable guide placement, flywheel controls, clutch/drum equipment, persistence and inspected recovery remain required.

## Physical connection

A separate Sable sublevel contains the carriage jaw, connected steel spine and symmetric ballast below the train. A world constraint locks lateral/vertical translation and rotation, with longitudinal limits from home to the guide end. The train retains its upstream rail guide. Its separate socket occupies two cells beneath the chassis bearing; tire strips, fins and seat magnets cannot stand in for it.

The jaws leave collision clearance. Home uses the upstream accepted-bearing convention: sampled rail point plus `5/16` block along track-up, then two blocks downward. Plot-origin placement's half-block offset is different. Geometry must be fixed, straight, level and unbanked. Release requires at least four carriage braking blocks before the guide end. Fixtures use 12 blocks of pull and a 20-block guide.

Engagement requires a stopped carriage at home, socket alignment within 0.03 block, and a stopped, supported, ready whole consist. Checks cover provider/consist/body identity, installed certificates, finite mass, exclusive ownership and upstream chains. The mass rating is 64 native units. Ordinary socket-equipped carts and Arrow on the pinned schema-4 Fourth Dimension adapter are eligible. Other articulated families remain denied; installing a socket does not override that restriction.

A native longitudinal catch motor constraint connects carriage to chassis with a 256-unit force cap, finite compliance and damping. Upstream guides/joints retain other degrees of freedom. Only the carriage receives queued propulsion impulse. Catch transfer, release, braking and return use native physics without train/carriage reset teleports, velocity setters or carrier-angle writes.

States are `HOME → ENGAGED → PULLING → RELEASING → RETURNING → HOME`, plus retained `FAULT`. Dispatch revalidates the stopped whole consist and delegates to its unique vehicle owner. Pulling rechecks support, hardware, identity, loaded mass and ratings. Loaded guide chunks and its geometry fingerprint remain required. A handle-only edit is detected without a graph revision. Failures cease impulses and apply the bounded carriage brake; no recovery operation currently clears the fault.

The catch releases at configured travel. A bounded brake stops the carriage, then a slow shaft-budgeted force returns it. Home requires actual return and near-zero speed; premature engagement is denied. Clearing the rig's internal membership after return does not implement a ride-controller reservation or certify the train's operating block.

## Finite work

The fixture's `FlywheelStore` has inertia 4, maximum angular speed 50 and capacity 5,000 native work units. Angular speed is `sqrt(2E/I)`. Drum speed is `0.2 × angular speed`, capped by target 3 blocks/second. Bounded clutch demand follows carriage slip and live provider mass/force limits; rising acceleration ramps at 4 blocks/second³. An empty store cannot synthesize launch speed.

The interface's translational/angular lever response bounds affordable force. Positive pre-constraint free-body impulse work spends `work / 0.85`. Conversion loss and negative work become heat, without energy credit. Paid work may subsequently dissipate through guides and joints.

Charging reads actual Create RPM from an unarmed test tire cabinet: `0.9 × 8 × |RPM| × 2π/60 × dt`. Charging pauses while engaged/pulling. Return spends that step's shaft budget before unused work charges storage. This fixture wiring does not implement a playable flywheel cabinet or resource journal.

## Evidence and limitations

Thirteen focused combined cases pass: occupied ordinary single/coupled forward/reverse, misalignment, empty storage, external power-loss hold, premature reset, handle-only edit, increased load, occupied Arrow single/coupled forward/reverse and unsupported Free Spin rejection. Positive cases reach fixture whole-train hold and actual carriage return. Riders, native contacts/joints/couplers and per-step impulse/work reconciliation are checked.

Focused ordinary single/coupled peaks are about 2.95/2.74 blocks/second; Arrow about 2.89/2.73. Flywheel speed falls from about 20.39 to 13.64–15.42 by release. Catch separation stays below 0.0013 block, guide error below 0.000009, Arrow carrier separation below 0.000192 and coupled error below 0.000062. Per-step accounting error stays below `2e-15` native work units. Exact final evidence is in [validation status](VALIDATION.md).

The native joint getter reports zero for this motor constraint; that is diagnostic data, not proof of absent or bounded load. Whole-train longitudinal momentum change, counting each body once, measures net pull traction around 20–40 native force units below the configured cap. It is not a substep motor-load or complete drum certification.

Final braking uses fixture-created native clamps after release, not a player stopping bank or controller certificate. Power loss is an external emergency-hold call, not cabinet supply detection. The geometry case edits collinear handles while preserving rail support. Removed rail/socket/carriage, curved/moving guides, return interruption, real chunk unload/restart, envelopes, thermal gameplay, multiplayer/rendered setup and repeated ride operation remain open. This does not complete a flywheel or 0.4.0 gate.

## Reproduction

Native worlds must run serially; Gradle recreates its own disposable world.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged -PlsTestNamespaces=coasterslaunchsystems_cable --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged -PlsTestNamespaces=coasterslaunchsystems_cable -PlsFourthDimensionJar=/absolute/path/to/verified-schema4.jar --no-daemon --max-workers=2
```

Standalone has nine cable cases; the exact Fourth Dimension binary adds four. The default full suite includes this namespace. Upstream pin: SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`, source `a881e3d34415edd691baa166668f4f97d3a4a7e8`. The active Fourth Dimension checkout/processes are untouched.
