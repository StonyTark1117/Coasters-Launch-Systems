# Section transfer checkpoint: 0.0.1-dev.9

This page preserves dev.9 acceptance. See [dev.10 circuit scope](CIRCUITS.md) for longer reserved routes, schema-3 persistence and explicit station capture/boarding fixtures.

This extends the straight, level, unbanked controller prototype with a reserved handoff between two consecutive fixed edges. It is partial LS004/LS104 progress toward the complete 0.4.0 release. A complete circuit, station boarding/capture/reset, curved routes, switches and the remaining release gates are unfinished.

## Installation

Bind each controller to its own ordered edge and link its inlet tire drives, inlet spring pads and continuous downstream friction-fin bank as described in [local controller setup](LOCAL-CONTROLLER.md). Use additional tire modules for transport from the stopped position through the source section's end. Select the disarmed empty source controller with shift-use of the span tool, then link these tires while holding a redstone torch in the offhand. This marks an exit transport role; exit tires receive no inlet-launch or stopping-bank credit.

The exit corridor needs supported friction pads so an interrupted straddling train can be held. The receiving section needs its own inlet tires/pads and continuous stop bank. Select the disarmed empty source controller again, then use the tool on the receiving controller to link the destination. Their ordered endpoints must meet with matching forward tangents at an unbranched joint. Controller UUIDs accompany the positions; replacing a destination does not inherit its reservation.

Arm both controllers and their modules. Correctly directed tire shafts use 1–128 RPM; pad release needs at least 32 absolute RPM. The initial rising top redstone input launches within the source section. After the native train stops in contact with exit transport, the controller reports READY_EXIT only when the receiving section is available, its complete stopping certificate fits and the required shafts are powered. A new rising input requests departure. Held-high input does not repeat it.

## Ownership and native motion

The source reserves the same complete chassis set in the receiver before dispatch or pad release. While EXITING/RECEIVING, only source exit tires and receiver inlet tires can drive. Source pads retract under physical power; receiver downstream pads remain applied. Both controllers use one intentional force coordinator owner during the handoff, while retaining every cabinet's own finite shaft-work budget and each chassis provider's force/acceleration/brake limits.

Every physics step rechecks actual accepted native engagement, live body and consist membership, deduplicated articulated mass, certified tire/fin interfaces, provider ownership, native chain exclusion, module identities, support and rail geometry. The destination certificate includes actual loaded bounds, consist length, current/commanded speed, declared brake force, projected native gravity, response allowance and margin. Both sections retain the chassis IDs while the train straddles their rail joint. The source clears only after every chassis is engaged on the receiving edge and its entire enclosing body radius is at least one block past the joint. A lost contact or missing body supplies no clearance credit.

Rollback, shaft-power loss, occupied reserved route, missing/replaced destination and withdrawn vehicle authority stop controlled drives and retain reservations. Pads apply only where the live vehicle provider allows external braking; they cannot override a missing Fourth Dimension carrier or exclusive operator. Entering the receiving stop bank before clearance also ends propulsion and keeps both leases.

Saved schema-2 controllers retain destination/incoming identity, exit roles and both leases, but load disarmed in INSPECTION with no input history or pending request. Schema-1 controllers migrate with no exit roles. An inspected saved pair can be armed one controller at a time while waiting with drives disabled. After both are armed, an actual held train, supported pads and a valid stopping certificate are required for READY_EXIT. A fresh input edge is still required to resume. This NBT reconstruction path is not evidence of a full server restart.

Fourth Dimension retains native rail transition, capture and dispatch ownership. Its pinned adapter delegates public dispatch only when the vehicle is captured and dispatch-ready; an already released train can transfer without requesting a second dispatch. No controller writes train velocity, pose or rotor angle. Final receiver hold retains the reservation and does not restore Fourth Dimension boarding capture.

## Acceptance scope

The native fixtures use two actual connected 64-block edges. Ordinary single/coupled trains carry four real riders per chassis; the coupled separation is six blocks. They exercise forward/reverse handoff, an occupied receiver, insufficient receiver stopping length, rollback from an external physical force, live shaft-power loss, paired NBT inspection/no replay, sequential rearming and fresh-request resumption, and unexpected receiver removal. Additional combined fixtures cover occupied Arrow singles/coupled pairs and individual Free Spin cars in both directions, plus loss of an Arrow carrier while straddling.

Observers measure queued native tire force and finite shaft work, actual world body corners at source release, retained riders, native carrier/coupler separation, straddling duration, both lease sets and final low-speed hold. No velocity setter or prescribed train path drives a fixture. Fixtures occupy a separate height from earlier controller/passenger tests so persisted upstream rail records do not interfere with unrelated tests.

The intentional missing-carrier fixture records the actual occupied seats before removing their body. It repeatedly checks zero queued propulsion and primary-provider denial on both surviving chassis, both retained reservations and riders on every surviving seat. It does not establish retention or recovery of riders whose seats were removed; that remains owned by the upstream vehicle lifecycle. Positive handoffs still require every original rider to remain seated.

Twenty-three unit tests, all sixty-one packaged standalone native tests and all 136 packaged combined native tests passed with the same binary JAR, including eighteen new handoff fixtures in the combined installation. Ordinary coupled trains straddled the real joint for 90 physics steps; coupled Arrow trains straddled for 90–92 steps and retained both leases for 184–186 receiving-edge steps before measured tail clearance. The coupled Arrow stopping certificate required about 21.21 of 22 usable brake blocks. Carrier separation remained below 0.00024 blocks and coupled Arrow error below 0.00012 blocks. The missing-carrier fixture recorded 1,215 consecutive native denial steps and identified two riders on the deliberately removed carrier; six riders on surviving seats remained seated.

See [combined native evidence](evidence/transfer-packaged-combined.txt), [standalone native evidence](evidence/transfer-packaged-standalone.txt), [unit results](evidence/transfer-unit.txt), [artifact hashes](evidence/transfer-artifacts.sha256) and [diagnostic notes](evidence/transfer-diagnostics.txt). Initial combined failures exposed fixture ordering, removed-seat retention and seat-count assumptions; they are recorded separately and are not acceptance evidence.

The copied schema-4 Fourth Dimension JAR remains SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`. Its active checkout and processes are untouched. Packaged test worlds must run serially; each invocation recreates its disposable world.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew clean build runGameTestServer -PlsPackaged --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PlsPackaged -PlsFourthDimensionJar=/tmp/coasterslaunchsystems-validation/dependencies/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

Remaining work includes complete repeatable rides and boarding capture, an inspected repair workflow after a missing/replaced peer, three Ponder demonstration rides, rendered survival setup/signals, obstruction/clearance checks, curved/profile traversal, live chunk/restart/multiplayer acceptance and all LIM/LSM/skid/catch/stored-energy mechanisms. This checkpoint does not complete the 0.1 or 0.4.0 gate.


Final implementation source `aca7af6b9f984d5f6ffbb2d528c64864b1751f36` is pushed to the private repository. [CI run 36830877470](https://github.com/StonyTark1117/Coasters-Launch-Systems/actions/runs/36830877470) passed a clean build, twenty-three unit tests and all sixty-one packaged standalone native tests. The downloaded CI JAR exactly matched local standalone/combined SHA-256 `057e14b6207bc7455174e179586e4781bf4419424723055524cc1dfa97a81d46`. [CI evidence](evidence/transfer-ci.txt) records the source and artifact verification. All 136 combined native tests passed locally against the same copied upstream JAR.

The earlier CI run 36827748987 failed the existing passive-magnetic forward fixture with zero recorded impulse. Native entry/contact/cabinet-availability checks were added; the final CI forward/reverse fixtures each measured eighty contact and live-cabinet steps and passed their unchanged dissipation/heat checks. The earlier failure and diagnostic correction are preserved in [diagnostic evidence](evidence/transfer-diagnostics.txt). Its original cause remains unconfirmed and repeatability remains an open final-release gate. The complete 0.4.0 goal remains active.
