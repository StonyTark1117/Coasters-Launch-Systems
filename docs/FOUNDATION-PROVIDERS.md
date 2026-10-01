# Contact and motion-provider checkpoint

This development checkpoint advances LS002–LS005. It does not complete the 0.4.0 release or certify player-facing launch equipment. The core works without Fourth Dimension; the combined installation uses an isolated, read-only adapter for its declared `0.9.0-dev.1`, schema-4 API shape.

## Contact and direction

The required mixin observes the returned native `engagedTickFrame`, including its cached `graphHit`. It does not grant contact through a nearest-rail query or change Coasters' rail solver. Contact expires when the game tick or graph revision changes, the native body is removed/disengaged, or the edge is no longer present.

`DirectedTrack` resolves one fixed-host edge between a specific ordered pair of anchors. It rejects ambiguous edges and moving rail hosts. A 512-segment distance table retains that placement direction during rollback; reversing the anchor order reverses the force axis and makes distances complementary. Geometry fingerprints include positions and frame up vectors. These are foundations for equipment placement and invalidation, not a completed placement tool or clearance certificate.

The pure interval tests include both travel directions across an entire short span in one step and an axle footprint that has not yet cleared the end. Train-wide reservations, adjacent-edge traversal, equipment footprint checks, and actual span placement remain unfinished.

## Motion providers

The ordinary fallback includes a Coasters chassis and loaded rigid rivet descendants. Integration providers resolve first. An explicit retained/vetoed snapshot cannot fall through to the ordinary provider. Duplicate claims reject rather than combining control. Each snapshot identifies the bodies belonging to one articulated cart and every coupled chassis in its consist; the runtime reads native body masses without adding another passenger mass.

The Fourth Dimension adapter reads its authoritative vehicles and carriers, checks native accepted support through `EligibilityHooks.allows`, and preserves restraints, capture, maintenance, faults, variant consistency, and PAX ownership. It does not dispatch or change Fourth Dimension state. A mismatched API fails clearly. The exact declared optional dependency is intentional while there is no published stable facade.

Both providers currently advertise an empty set of certified equipment interfaces. The native fixtures are force-path experiments; reaction surfaces, friction interfaces, catch sockets, and their placement certificates still need implementation. A registered provider is not permission for arbitrary equipment to act on every vehicle.

## Native fixture scope

The ordinary fixture drives the real native cart through the accepted rail contact, in both directions, and applies friction braking. It verifies that reversal preserves the axis convention, contact remains available during each physics step, and disengagement removes force contact.

Combined-install fixtures use Fourth Dimension's real track placement, vehicle factory, boarding operations, restraints, and dispatch. Arrow, legacy ZacSpin, Free Spin and revised ZacSpin each carry one real seated pig. The adapter must observe exactly 0.8 additional native mass, without counting it again. Chassis point forces accelerate each articulated cart; no velocity command, angular reset, or train teleport drives the experiment. Maintenance suppresses the interface force. PAX has a separate exclusive-owner veto fixture.

Joint separation is measured after native integration. Its plot anchors use the native mass center, matching the pinned solver's calculation. The first draft used the presentation pose's rotation point, producing apparent offsets already present before launch. Correcting that measurement required no vehicle or physics change. The fixture now bounds native separation to 0.01 blocks and checks passenger retention and independently moving free rotors. Rendered alignment and camera/passenger presentation still require packaged client verification.

Pinned source: Sable 2.0.3 tag `mc1.21.1-2.0.3-neoforge`, commit `17d29f5b99d50e3cc13b536b23f9181ade99f14f`, [native joint anchor conversion](https://github.com/ryanhcode/sable/blob/17d29f5b99d50e3cc13b536b23f9181ade99f14f/sable_rapier/src/main/rust/rapier/src/joints.rs) and [native pose retrieval](https://github.com/ryanhcode/sable/blob/17d29f5b99d50e3cc13b536b23f9181ade99f14f/sable_rapier/src/main/rust/rapier/src/lib.rs). This source inspection supports the measurement coordinate convention; runtime results establish the tested behavior.

## Reproduce

Build and run the standalone fixture with Java 21:

```sh
./gradlew build runGameTestServer --no-daemon --max-workers=2
```

For combined validation, first copy the Fourth Dimension artifact out of its active build directory and record its checksum. Supply that snapshot explicitly:

```sh
./gradlew runGameTestServer -PlsPackaged \
  -PlsFourthDimensionJar=/absolute/path/to/copied-fourthdimension.jar \
  --no-daemon --max-workers=2
```

Only the Launch Systems namespace is enabled; five extra required integration tests are generated when Fourth Dimension is installed. Without it, those tests are absent, not successful skipped integration results. `-PlsPackaged` requires the already built Launch Systems binary. The saved evidence identifies both binary checksums and the actual test scope.

Open gates include loaded ordinary carts, linked loaded Arrow consists, rotating Arrow profile traversal during launch/rollback, equipment interfaces and controls, full/uneven loads, interrupted saves, unavailable bodies/chunks, third-party overlap control, rendered clients and multiplayer. All player equipment and the physical catch/release/reset mechanisms also remain required for 0.4.0.
