# Changelog

## 1.0.1 — Fourth Dimension 1.6 companion (2026-11-27)

- Add the versioned FD 1.6 adapter and accepted original Arrow/Wing moving-bed
  handoffs with fixed Tire/LSM/LIM equipment and fin brakes.
- Preserve native retention, explicit receiving arm and fresh launch requests
  through movement, power interruption, restart and dependency/controller loss.
- Add authenticated below-dock mounting with a horizontal allowance matching
  FD's 25mm socket tolerance; ordinary reach remains unchanged.
- Publish the exact accepted integration JAR, with standalone/fixed-track
  evidence retained. [Release notes](docs/releases/1.0.1.md) and
  [integration evidence](docs/evidence/fd16-integration-20261127/README.md).

## 1.0.0 — standalone stable release (2026-11-27)

- Publish the accepted six-launch package for Minecraft 1.21.1 / NeoForge
  21.1.256, with exact installation downloads, a checksum and migration notes.
- Promote 0.9 without gameplay, dependency or asset changes: all 183 compiled
  class entries and every other resource match except the mod-version metadata.
  The Java 21 build and 31/31 unit tests passed locally.
- Keep optional integrations scoped to their accepted evidence. The exact FD
  qualification snapshot is unpublished and not included; standalone operation
  does not require FD. Optional LS801–LS807 adapters remain unadvertised.
- [Release notes](docs/releases/1.0.0.md) and
  [qualification](docs/evidence/1.0/release-20261127/README.md) record the artifact,
  carried evidence and hosted CI limitations.

## 0.9.0 — release candidate (2026-11-27)

- Preserve optional vehicle ownership across provider removal and restoration.
  A saved Fourth Dimension vehicle cannot silently become an ordinary launch-ready
  cart; unloading retains identity, while deliberate live release and permanent
  removal retain their intended behavior. Malformed ownership data fails closed.
- Reconcile the finite 17-variant matrix, native mixed-consist refusals and actual
  installed/absent/restored provider lifecycle. For old saves, first load and
  inspect affected vehicles with 0.9 and their original provider installed, then
  save before removing that provider; unvisited vehicles cannot be inferred.
- Complete the four remaining real occupied FD LSM, LIM, hydraulic and pneumatic
  demonstrations, carrying forward accepted tire/flywheel and ordinary rides.
- Add opt-in bounded callback timing and publish a representative two-train,
  eight-rider, 49-cabinet measurement and resource reconciliation. This measures
  LS callback cost, not whole-server or graphical performance.
- Reconcile the operator guide, five braking choices, recipes/Ponder, dependency
  pins and licenses against the final package. Java 21 build and 31/31 unit tests
  passed. [Integration evidence](docs/evidence/0.9/integration-20261127/README.md)
  records exact artifacts, targeted checks, occupied outcomes and shutdown.
  Optional adapters and 1.0 publication remain separate.

## 0.6.0 — bounded ride-local interoperability (2026-11-26)

- Consume Fourth Dimension’s original Arrow admission and current/next block
  reservations through an additive opt-in contract; native FD remains the sole
  route reservation owner. Equipment leases retain stopping authority on denied
  reservations, changed owners, opposing motion, rollback and unavailable bodies.
- Share controller identity, charge, blocking reason and fresh-request state with
  the existing FD operator panel and local controller/cabinet status. Dispatch
  enters the normal LS request loop with a transient freshness token.
- Keep captured route handoffs held until explicit recovery and a separate new
  dispatch. Saved requests and pre-reload operator tokens cannot restart a ride.
- The supported route remains eight segments, six blocks and at most three
  original Arrow trains. Occupied separation, blocked-world restart/rejoin,
  explicit rearm and separate fresh dispatch passed; all owned runtimes stopped.
  [Integration evidence](docs/evidence/0.6/integration-20261126/README.md) preserves
  exact artifacts, targeted fixes and disclosed fixture setup. Earlier accepted
  0.1–0.5 behavior carries forward where unchanged.

## 0.5.0 — expanded Fourth Dimension certificates (2026-11-26)

- Publish the complete seventeen-variant family × equipment disposition: fourteen
  positive tire/fin/linear/skid/eddy rows, twelve cable rows, and evidenced native
  ownership or physical guide exclusions.
- Resolve forces and stopping regions at installed chassis contacts; support
  translated rear catches, physical HOME return and saved-world recovery.
- Preserve native restraints, posture, yaw, drop and retention controls. The
  additive FD family-power contract supplies S2/Flying accessories through the
  same verified owner without granting propulsion or clearing native faults.
- Verify loaded native contacts, occupied operation/service, explicit recovery,
  appearance invariance and targeted boundaries. Java 21 integration: 29/29 unit
  checks. Exact artifacts, exclusions and cleanup are in the
  [integration record](docs/evidence/0.5/integration-20261126/README.md).
- Bounded multi-train interoperability, later FD catalog additions and positive
  PAX delegation remain outside this milestone.

## 0.4.0 — six launch mechanisms (2026-11-22)

- Complete the cumulative linear/swing/skid, physical flywheel and pressure
  milestones using targeted native, saved-world and occupied-player evidence.
- Preserve exact-owner safety braking after Fourth Dimension authority
  suspension without restoring propulsion or replaying a dispatch. Acquire
  native cable authority before engaging the retained train.
- Inset cabinet collision and visible housings to clear the returning carriage;
  verify physical home and normal/inspected service without moving bodies by command.
- Show finite pressure, work, phase, readiness and charged/returned hydraulic
  oil; update cable/pressure Ponder and operating instructions.
- Add focused current-provider regression selectors and extend the opt-in
  read-only geometry diagnostic to exact loaded ordinary bodies.
- Retain the accepted 0.1 dependency baseline and explicit vehicle certificates.
  Modern FD catalog and shared multi-train operation remain 0.5/0.6 work.

## 0.1.0 — current-core usable rides (2026-11-22)

- Select NeoForge 21.1.256, Sable 2.0.6 and Veil 4.3.2 by default, retaining
  Coasters 0.1.5 and an explicit reproducible legacy profile. Repair standalone
  Veil startup conditionally; combined runs use Fourth Dimension's repair.
- Register the current Fourth Dimension external authority API 1 provider with
  native owner/epoch/sequence validation, whole-train capture and transfer,
  explicit restart rearm, and family, restraint and managed-route vetoes.
  Preserve the original schema-4 adapter separately.
- Reject blocked tire/fin/magnetic bearing corridors during binding and fresh
  arming, using actual collision shapes without dropping retained identities.
- Qualify the current foundation, saved catch/ticket recovery, native bridge,
  real ordinary reconnect, and occupied Arrow construction/operation/restart.
  Complete all three player-built empty/occupied rides, including normal
  exit/reboard and fresh dispatch. Diagnose and remove temporary Free Spin
  construction obstructions while retaining rollback protection.
- Add an opt-in read-only vehicle geometry diagnostic for private validation.
  It does not apply forces or alter vehicle/controller authority.

## Unreleased — workflow and dependency preparation (2026-11-15)

- Add branch continuity, Java 21, evidence reuse, private display/input,
  resource budgeting and mandatory runtime shutdown instructions.
- Move colon-safe build output off RAM-backed `/tmp`; add isolated build/run
  directories and client/server heap settings to generated launches.
- Adapt Sable loading-ticket reads to the public getters (2.0.3) or record
  accessors (2.0.6), preserving both dependency profiles.
- Make packaged launch-script generation build its consumed JAR automatically.
- Add the explicit NeoForge 21.1.256 / Sable 2.0.6 migration candidate profile,
  preserving the working legacy default and exact schema-4 adapter guard.
- Link the current Fourth Dimension dependency/authority source and evidence;
  define the remaining bridge and scheduler migration instead of treating old
  combined results as current compatibility.


## 0.0.1-dev.7 occupied ordinary cart development

- Add equal declared native mass/inertia for real ordinary Create seat passengers before equipment force collection, respecting primary-provider mass ownership.
- Remove contributions on dismount, seat loss or provider transfer, and restore them once on return to ordinary ownership.
- Restore previously owned rider contributions during Sable's tracker rebuild before native body recovery initialization, fixing a measured 0.0625-block COM-frame displacement without writing train pose or velocity.
- Add a passenger lifecycle fixture and twelve occupied bidirectional tire/fin/magnetic equipment fixtures, including real fully occupied native six-block coupled pairs and rolling tire entry.

Twenty unit checks, forty packaged standalone native tests and 102 packaged combined native tests passed with the same JAR. This is partial LS003/LS004/LS101–LS103 progress. Full stopping envelopes, controllers, occupied rivet children, rendered/multiplayer lifecycle and the remaining 0.4.0 mechanisms remain open. See [scope and evidence](docs/ORDINARY-PASSENGERS.md).

## 0.0.1-dev.6 magnetic and articulated brake development

- Add an installed copper conductive fin and passive magnetic cabinet behavior with point-speed force, bounded dissipative impulses, a shared force budget and zero shaft stress.
- Persist brake heat in equipment schema 2, migrate schema 1 with cold heat, cool loaded cabinets and derate overheated magnetic force.
- Introduce provider API 3 with a separate finite brake-force rating. Older snapshot overloads deny unrated new brake authority.
- Certify installed friction/conductive fins on four exact pinned Fourth Dimension variants with conservative loaded-mass brake ratings and whole-consist vehicle vetoes; PAX remains exclusive.
- Reject extrapolated or nonfinite native guide contacts before distance lookup, fixing a server crash exposed by a car rolling beyond an edge.
- Add nine standalone magnetic fixtures and thirty-eight loaded articulated brake fixtures, including occupied native Arrow pairs, maintenance/capture/restraint/fault vetoes, PAX and unavailable carriers.

Twenty unit checks, twenty-seven packaged standalone native tests and eighty-nine packaged combined native tests passed with the same JAR. This is partial LS003/LS004/LS102/LS103 progress. Complete stopping envelopes, 4D slope holds, ordinary passengers, controllers, rendered assets, active interruption/multiplayer validation and the remaining 0.4.0 mechanisms stay open. See [scope and evidence](docs/MAGNETIC-AND-4D-BRAKES.md).

## 0.0.1-dev.5 friction fin development

- Implement an ordinary-cart spring-applied, shaft-released fin cabinet with a native longitudinal damping constraint, bounded force and no position spring or hard axis lock.
- Add a separate installed chassis brake fin, original model, recipe, loot and creative-tab entry.
- Share the cabinet's 256-unit native force rating across contacted chassis; check loaded consist mass, unbanked slope and available gravity margin before engagement.
- Introduce explicit braking permission in provider API 2; old motion snapshot construction denies brakes. Fourth Dimension retains tire certification and grants no fin-brake authority yet.
- Remove native clamp handles when the physical fin, contact, cabinet or ownership becomes unavailable; power loss reapplies an armed, supported spring clamp.
- Add four native constraint proofs and six actual cabinet fixtures, including bidirectional loaded stops, mass-64 slope hold/release/reapplication, physical hardware removal, missing fins, excessive load and competing owners.

Eighteen unit checks, eighteen packaged standalone native tests and forty-two packaged combined native tests passed with the same JAR. This is partial LS102 progress; 4D brakes, complete stopping envelopes, controller interlocks, heat, animations and packaged multiplayer/lifecycle acceptance remain pending. See [scope and evidence](docs/FIN-CLAMPS.md).

## 0.0.1-dev.4 Fourth Dimension tire development

- Certify installed chassis tire strips for the pinned Arrow, legacy ZacSpin, Free Spin and revised ZacSpin variants, preserving restraint, capture, maintenance, fault, support and PAX ownership vetoes.
- Veto the whole native consist when an authoritative carrier body is missing, removed or has invalid mass.
- Add nineteen required combined native equipment tests: four families with full/uneven loads in both directions, a powered PAX veto, and fully occupied native six-block Arrow pairs in both directions.
- Observe actual cabinet impulses and reconcile interface work with the shared shaft budget; verify rider mass/retention, native joint and coupler separation, free carrier response, power loss and removed hardware.

Eight packaged standalone and thirty-two packaged combined native tests passed, alongside eighteen unit tests. Compatibility is limited to the copied schema-4 artifact and specified fixed, level/unbanked tire setup; profile traversal, rendered/multiplayer lifecycle and other mechanisms remain pending. See [checkpoint evidence](docs/FOURTH-DIMENSION-TIRES.md). This is partial progress toward 0.4.0.

## 0.0.1-dev.3 equipment development

- Add original tire/fin/passive-magnet cabinet models, recipes, block entities and a span tool.
- Bind each cabinet to a two-block zone on one fixed loaded edge; validate level/unbanked rail and transverse shaft alignment, persist identity and invalidate edited support.
- Require an installed chassis tire drive strip; derive its certificate from physical hardware and apply native force at its contact point.
- Use real Create RPM and stress consumption, traction/torque limits, a shared shaft-work budget and bounded acceleration demand. Include the strip's angular lever arm in impulse-work accounting.
- Add native fixtures for the actual tool, save/load, power/support loss, forward/reverse transport, negative shaft RPM, ballasted chassis and conflict/missing-hardware vetoes.

Fin and magnetic cabinet blocks are scaffolds; their braking behavior remains unimplemented. Fourth Dimension tire interfaces remain disabled pending equipment certification. This is a development checkpoint, not the requested 0.4.0 release.

## 0.0.1-dev.2 contact and provider development

- Observe the returned native guide frame, with tick/revision/disengagement invalidation.
- Add directed fixed-edge distance tables, geometry fingerprints and swept footprint intervals.
- Register an ordinary motion fallback and isolated Fourth Dimension schema-4 adapter with authoritative operation vetoes and PAX ownership.
- Extend native fixtures to forward/reverse propulsion, friction braking, loaded articulated carts, rider mass, maintenance vetoes and native joint separation.

Equipment interfaces are not yet certified. The full 0.4.0 gameplay and validation gates remain in progress.

## 0.0.1-dev.1 foundation development

- Establish the pinned standalone NeoForge project and packaged run configurations.
- Implement distinct tire/LIM/LSM/magnetic/friction force functions, jerk limiting and brake heat.
- Implement bounded work buffers, flywheel storage, gas-backed hydraulic accumulators and compressed-air receiver models.
- Define the versioned native vehicle-motion provider contract with explicit body/consist identities and operator vetoes.
- Add meaningful mathematical tests and a real native-cart force/braking fixture.

The requested 0.4.0 feature and validation gates remain in progress.
