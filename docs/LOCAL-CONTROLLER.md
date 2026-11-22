# Local controller checkpoint: 0.0.1-dev.8

This page preserves its original checkpoint scope. See [dev.11 station scope](STATIONS.md) for optional controller capture, measured boarding readiness, API 5 and schema-4 persistence.

This records partial LS004/LS102/LS104 progress toward the full 0.4.0 goal at dev.8. The prototype controls one straight, level, unbanked fixed rail edge, within the existing equipment coordinate limits. It certifies linked tire drives and friction-fin modules. The later [dev.9 section transfer checkpoint](SECTION-TRANSFERS.md) adds departure into an adjacent reserved edge. A complete repeatable ride cycle and the remaining release gates are unfinished.

## Installation and operation

Before binding equipment to a horizontal, level section, mount both Coasters Simulated anchors on the **top face of their floor supports** so each anchor faces upward, then connect them with Coaster Track. This preserves the world-up rail frame required by tire drives and the local controller. Side-mounted anchors can form a horizontal centerline while retaining a horizontal rail normal; Launch Systems treats that frame as banked and refuses it. Place the tire shaft on the horizontal axis transverse to travel. When joining the second endpoint of a return curve, hold **Alt** as the upstream Coaster Track tooltip directs to suppress automatic smoothing; otherwise an adjoining straight equipment section can acquire a curve. Check the resulting rail shape before binding equipment.

1. Bind each disarmed drive/fin module with the span tool, using the same ordered anchors. Each cabinet retains its physical two-block contact interval.
2. Select those section anchors and use the tool on a local ride controller. Shift-use the tool on the controller to select it, then use it on each bound disarmed module to link them. Shift-use a module while its controller is selected to unlink it after both are disarmed and the reservation is empty. Unlink every module before rebinding/removing an empty controller. Shift-use elsewhere clears tool selection.
3. Install a spring-pad launch bank overlapping the tire contacts, and a continuous downstream friction-fin bank beyond every drive contact. Gaps and insufficient usable braking length reject launch. Passive magnets receive no final-stop credit.
4. Arm modules, provide correctly directed tire RPM between 1 and 128 and at least 32 absolute RPM to release the launch pads, then shift-interact with the controller using an empty hand to inspect/arm. Linked pads remain spring-applied until the controller grants launch; downstream pads remain applied even when powered.
5. A rising redstone signal from the block immediately above requests launch while READY. Inputs received before readiness are discarded. Held-high input does not repeat a launch. READY, reserved, inspection/fault and hold/ready outputs use the NORTH, SOUTH, EAST and WEST query faces respectively. Empty-hand interaction displays the current state and calculated/available brake length.

Arming the controller does not override vehicle ownership. API 4 adds a default-denied `requestDispatch` operation. The uniquely resolved ordinary owner grants its own cart permission; the pinned schema-4 Fourth Dimension adapter delegates to public `VehicleOperations.dispatch`. Fourth Dimension retains its restraint, capture, maintenance, support and exclusive PAX checks. The controller reserves first and then verifies every released member's propulsion/braking authority. It does not write vehicle carrier angles or native velocity/pose.

## Physical limits and state

`INSPECTION → HOLD → READY → RUNNING → BRAKING → HOLD` follows loaded native contacts. Faults stop controlled drives and close supported armed launch pads. Braking authority still belongs to the vehicle provider; if it withdraws that authority, no external clamp is imposed on the retained/recovered vehicle.

The certificate uses actual deduplicated native articulated mass, the lowest declared chassis brake rating, an enclosing radius from current loaded body bounds, chassis separation, maximum measured/commanded speed, 0.1-second response allowance and one extra block of margin. The present controller supports nominally level sections only and samples the native dimension gravity projected along the section, including placement tolerances. The unit model also accounts for adverse gravity and denies a hold when it consumes all available brake force. The usable bank excludes gaps and bearing-footprint extension. Loaded consists above 64 native mass units reject operation.

Adjacent modules share an intentional controller owner. Each cabinet retains its own finite shaft-work/pad-force budget, including the cross-work of simultaneous tire impulses; dissipative contacts grant no charging credit; all contacts on a chassis additionally share that provider's propulsion and brake ratings. Unrelated module overlaps still fault together. Station pad release requires both physical shaft power and the controller grant. When the leading car enters downstream fin contact, propulsion ends and launch pads reapply. A linked train may consequently stop with its rear still in the launch bank; it remains reserved. The stop bank's footprint can engage just before its nominal interval start, which the native tests measure explicitly.

Reservation chassis IDs, controller/module UUIDs, bindings and geometry fingerprints persist. Server loads always enter disarmed INSPECTION, retain the reservation and discard commands/input history. Replaced module identities, changed direction/geometry, missing support, unticking equipment, unavailable route chunks, missing native bodies, changed consists and competing section owners deny operation. Ordinary player breaking, Create wrench rotation and manual disarming of reserved equipment are rejected; unexpected block loss faults the controller. Native world teardown owns constraint disposal.

At dev.8, adjacent-edge clearance and rollback lacked native acceptance evidence. Dev.9 now requires a paired receiving reservation before any clearance and exercises loaded tail clearance and interrupted handoff; see [section transfers](SECTION-TRANSFERS.md). Missing contact or a missing body retains ownership. Final hold does not clear the receiver's reservation or restore Fourth Dimension boarding capture. A repeatable complete ride/capture cycle remains required before LS104/LS105 can be completed.

## Native evidence and limits

Ten standalone fixtures cover fully occupied ordinary singles and native six-block coupled pairs in both directions, a second train in the stopping region, a two-block bank too short for the load, release-power loss, controller NBT inspection/no replay, unexpected module removal and attempted module theft by another controller. READY fixtures post the actual NeoForge break event and verify that manual disarm is rejected. The four successful ordinary launch fixtures retain their native riders and guides, then hold while the original input remains high.

Six additional fixtures use the copied pinned Fourth Dimension artifact: fully occupied Arrow singles and six-block coupled pairs, plus fully occupied individual Free Spin cars, each in both directions. They board through real Create seats, close restraints through the public operation and leave dispatch to the controller's provider call. Native queued tire impulse, loaded mass, retained riders, joint/coupler separation and free carrier response are measured. No command supplies their entry velocity.

Measured fixture results and final binary hashes are recorded in [combined native evidence](evidence/controller-packaged-combined.txt), [standalone native evidence](evidence/controller-packaged-standalone.txt), [unit results](evidence/controller-unit.txt) and [artifact hashes](evidence/controller-artifacts.sha256). The exact upstream binary remains SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`; its active checkout/processes were left untouched.

The initial combined fixture reused ordinary test rail coordinates. Fourth Dimension's persisted rail records then affected later ordinary tests, producing three failures. Moving controller fixtures to separate coordinates resolved this fixture interference. The earlier phase check also assumed every car would reach the raw stop-bank start; real finite pads can stop the front at the extended contact boundary while launch pads hold the rear. Final HOLD now requires measured low speed and actual native clamps on the complete consist. Neither issue was resolved by relaxing native support, rider, force or joint checks.

At dev.8, occupied bank departure, adjacent-edge tail clearance and rollback were open; the dev.9 checkpoint supplies scoped native handoff evidence. Remaining acceptance includes rendered survival workflow and signals, closed ride cycles, mixed-interface banks, 4D slope/profile traversal, restart and real chunk unload/reload, packaged multiplayer/late join, Ponder and all LIM/LSM/skid/catch/stored-energy mechanisms. NBT reconstruction is not server-restart evidence. Scoped tests do not establish a completed 0.1 or 0.4.0 release.


## Reproduction

Run native worlds serially; the Gradle test task recreates its own disposable world. Java 21 is required.

```sh
./gradlew clean build runGameTestServer -PlsPackaged --no-daemon --max-workers=2
./gradlew runGameTestServer -PlsPackaged -PlsFourthDimensionJar=/tmp/coasterslaunchsystems-validation/dependencies/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

The optional upstream path must resolve to the copied SHA-256 identified above. A new upstream build with the same version string is a different validation target. The implementation source is commit `97b800a2060e1cba2890e1c7ceb2aaba1fc006a9`. [Diagnostic notes](evidence/controller-diagnostics.txt) preserve the earlier fixture conflict without treating that failed run as release evidence.


Implementation commit `97b800a2060e1cba2890e1c7ceb2aaba1fc006a9` is pushed to the private remote. [CI run 36821874795](https://github.com/StonyTark1117/Coasters-Launch-Systems/actions/runs/36821874795) passed a clean build, twenty-three unit tests and all fifty packaged standalone native tests. The downloaded CI JAR exactly matched the locally validated standalone/combined SHA-256 `d7163f67f398b062dbb3cc70f230104156efb0ae5757bdef0f5e8fb3c32a0baf`. [CI evidence](evidence/controller-ci.txt) records the source and artifact verification. Combined Fourth Dimension evidence remains local against its copied pinned JAR. The full 0.4.0 goal remains active.
