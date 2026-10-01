# Provider-owned station capture: 0.0.1-dev.11

This page records the dev.11 station checkpoint. See [dev.12 receiver recovery](RECEIVER-RECOVERY.md) for schema-5 equipment journals, inspected controller replacement and final scoped evidence.

A station controller returns an actually held train to its vehicle owner for alignment and boarding. This is partial LS004/LS104/LS105 progress. Ordinary carts remain physically held by their fins; Fourth Dimension capture and rotor alignment use its pinned public vehicle operations.

## Operator setup

Bind and equip the controller using [section setup](SECTION-TRANSFERS.md) or [circuit setup](CIRCUITS.md). While the bound controller is disarmed and has no reservation, use the span tool on it with a vanilla clock in the offhand to toggle station capture. Do not sneak for this action. Existing controllers default to station capture off. Arm the controller and its modules normally.

Arrival first ends tire propulsion and holds every chassis. The station retains the complete lease. For a released Fourth Dimension train it requests whole-consist capture; CAPTURING waits for the vehicle owner's measured alignment. The controller never writes carrier angles, pose or velocity. Already captured vehicles do not receive repeated capture operations.

The bottom redstone output indicates measured boarding readiness. This differs from the north departure-ready output. Open restraints can permit boarding while keeping departure readiness off. Use Fourth Dimension's own restraint/boarding controls to open restraints, exit and board, then close restraints. Launch Systems does not automatically open or close restraints. While open, the controller reports BOARDING and consumes departure requests. Closing restraints does not replay an input held high during boarding; a fresh rising top input is required after READY or READY_EXIT.

| Signal face | Meaning |
| --- | --- |
| Top | Fresh rising departure request, accepted only in READY/READY_EXIT |
| North | Departure ready |
| South | Retained chassis reservation |
| East | Fault or inspection |
| West | Held/ready/capturing/boarding controller state |
| Bottom | Measured whole-train boarding readiness |

Changes to station mode require an empty disarmed controller. Fault, disarm, inspection, moving phases and reservation clearance invalidate its boarding-ready flag. No station action clears the reservation. Schema-4 saves retain station configuration but server loads are disarmed in INSPECTION, with no readiness or request replay. Schemas 1–3 remain station-disabled. This NBT reconstruction is not full server-restart evidence.

## Provider contract

API 5 requires an explicit `apiVersion()` implementation. Historical provider binaries that inherited API 4's default method cannot silently inherit version 5. Registration reports a missing version declaration as a rejected provider. A compiled historical binary fixture verifies this actual JVM compatibility path; its API/Minecraft compilation stubs are excluded from the distribution JAR.

`requestCapture` defaults to false; `captureState` defaults to UNSUPPORTED. Capture acceptance and readiness remain separate. The readiness states are UNSUPPORTED, DENIED, RELEASED, ALIGNING and READY. Operations go to the uniquely resolved owner; a primary denial cannot fall through to ordinary-cart behavior.

Before a capture request, the facade checks the caller's exact reserved membership, the complete live consist, unique body IDs, loaded native masses, accepted guide contact, tire/fin interfaces, brake rating and absence of an exclusive operator. Every chassis must be below 0.03 blocks/second in native translation and at its interface point, and have an actual fin clamp or a positive vehicle-owned capture/hold state. A moving train or incomplete lease receives no capture operation.

The pinned Fourth Dimension adapter calls public `VehicleOperations.capture` after these checks. It reports ALIGNING until public `Boarding.ready` passes on every vehicle. Maintenance, faults, unavailable carriers, invalid support and PAX exclusive ownership deny capture/readiness. Its native capture remains responsible for articulated braking/alignment; this operation grants no additional external force authority. Ordinary carts need no rotor capture operation and report readiness only for their live stopped consist.

## Validation scope and remaining work

The occupied circuits run controller capture with the former manual capture call removed. They perform two laps, actual exit/reboarding and a fresh departure for ordinary singles/coupled pairs, Arrow singles/coupled pairs and individual Free Spin. Observers require zero propulsion during capture/boarding, retained leases, native rider mass removal/restoration and unchanged force/work/joint limits. A fresh high input while restraints are open must not depart, and remaining high after closing must not replay it. Moving-train, empty-lease and partial coupled-lease capture requests must fail. Broader equipment fixtures check PAX, maintenance, fault and missing-carrier denial.

The real span-tool/clock interaction, station persistence and legacy opt-out are exercised in the packaged placement fixture. The native contract fixture checks default denial, explicit API-4 rejection and a real compiled API-4 binary without a version override. See [validation status](VALIDATION.md) for final suite evidence and artifact identity; [development diagnostics](evidence/station-diagnostics.txt) preserve the failed test-layout/build attempts separately.

Rendered survival/operator workflows, late-join signals, real restart/chunk interruption, missing-peer repair, reverse/downhill/profile circuits, Ponder demonstrations and all later linear-motor/skid/catch/flywheel/hydraulic/pneumatic gates remain required. This does not complete the 0.1 or 0.4.0 release.
