# Friction fin checkpoint: 0.0.1-dev.5

This implements an ordinary-cart fin-clamp prototype and advances LS102. It does not complete the 0.1 or 0.4 release gate.

## Setup and ratings

Install the **Chassis Friction Brake Fin** in the assembled chassis at the representative native bearing's local east/below cell, `(1, -1, 0)` relative to the bearing. It is separate from the tire strip directly below the bearing. A tire strip never supplies a brake-fin certificate. Fin geometry runs along chassis-local Z.

Place the Fin Brake Module within three blocks of one fixed rail edge, with its shaft transverse to the rail. Bind it with the existing span tool, then arm it. Its contact interval covers at most two blocks around the cabinet's rail projection; runtime uses the accepted native bearing and a swept 0.6-block half-footprint. Fixed unbanked slopes up to a 1-in-4 grade are allowed. Tires remain level-only. Moving rail, banks and unsupported binding geometry are rejected.

An armed fin cabinet applies its spring pads below 32 RPM shaft magnitude, including zero power, and releases them at 32 RPM or above. Manual disarming removes the prototype clamp. It has a 256-unit native force cap shared across every contacted chassis, and a maximum native consist mass of 64. Provider force limits can reduce that cap. Gravity projected along the rail must consume no more than 80% of the available force; shared contacts are checked again after allocation. These are original game-scale ratings, not real manufacturer specifications.

The native joint applies longitudinal damping toward zero slip, with a finite force limit, no position spring and no hard axis locks. Rail and carrier joints own the other degrees of freedom. The implementation never sets train velocity/pose or carrier rotation. Handles exist only in the runtime, not in saved block entities. Loss of the installed fin, valid contact, cabinet or permitted ownership removes the handle. A missing fin or invalid rating faults/disarms the cabinet; inspect and rebind before reuse.

Provider API 2 requires explicit `brakingAllowed` authority. The acceleration rating bounds propulsion; the force rating also bounds brakes. The legacy motion-snapshot constructor intentionally grants no new brake authority. Ordinary carts supply the fin certificate from installed hardware. Fourth Dimension currently grants no fin authority: its existing tire variants and all vehicle/PAX vetoes remain in place until loaded brake equipment is validated.

## Contact length matters

A finite clamp only acts while its fin occupies the cabinet's physical contact zone. It cannot stop a train after the fin has passed the pads. The maximum-load transition initially failed when the held car lay near the lower end of the zone: after half a second of powered release, the remaining contact was too short to arrest the rollback before it exited. The fixture now places the cabinet around the loaded car's measured holding position, providing the contact needed for reapplication. Both the failed contact diagnostic and corrected packaged results are preserved below.

This is a demonstrated layout limit. The later controller/stopping-envelope work must reserve and validate sufficient loaded braking distance before release. This checkpoint does not guarantee a stop for arbitrary entry speed, train length or power-loss timing, and is not a completed block system.

## Native evidence

Four constraint proofs use a ballasted native chassis of mass 25. Forward/reverse physical entry forces produce motion; the 128-unit proof clamp stops both without measurable reversal. It holds a 1-in-4 slope for ten seconds with zero measured creep, then releases into downhill motion. A 256-unit external force produces about 5.109 blocks/s after one second against the 128-unit clamp, consistent with bounded slipping rather than an unlimited lock.

Six actual cabinet fixtures cover forward, reverse, slope, missing fin, overlap and excessive mass. Native mass-26 carts enter at about 2 blocks/s and stop within about 0.202 blocks in both directions. The maximum mass-64 chassis holds the 1-in-4 slope for ten seconds with zero measured creep, releases at shaft power, and stops again on power loss while contact remains available. The corrected reapplication speed is about 0.0053 blocks/s. Removing the actual fin revokes its certificate and releases the native clamp, verified by subsequent physical acceleration. Missing fins, native mass 82 and a tire/fin ownership conflict fault without a partial brake or drive.

The complete package passed eighteen unit checks and eighteen packaged standalone native tests. With the copied pinned Fourth Dimension artifact, forty-two required native tests passed, retaining the prior tire/rider/coupler/PAX evidence. The combined run does not certify Fourth Dimension brakes. Both runs used the same Launch Systems JAR; the upstream copy retains SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`. No Fourth Dimension source or process was changed.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PlsPackaged \
  -PlsFourthDimensionJar=/absolute/path/to/copied/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

Run sequentially; each recreates the disposable native test world. Evidence: [unit checks](evidence/fin-unit.txt), [standalone](evidence/fin-packaged-standalone.txt), [combined](evidence/fin-packaged-combined.txt), [artifact hashes](evidence/fin-artifacts.sha256), [failed short-contact diagnostic](evidence/fin-contact-diagnostic.txt).

The constraint API/force-limit implementation was inspected in pinned Sable 2.0.3 source, commit `17d29f5b99d50e3cc13b536b23f9181ade99f14f`: [native joint motors](https://github.com/ryanhcode/sable/blob/17d29f5b99d50e3cc13b536b23f9181ade99f14f/sable_rapier/src/main/rust/rapier/src/joints.rs). Runtime fixtures establish the measured behavior above.

## Remaining acceptance

Ordinary passengers, occupied/linked brake consists, Fourth Dimension brake interfaces and restraint/capture/stopping ownership, wrong-interface orientation/obstruction envelopes, complete stopping-distance/controller checks, rendered pad animation, heat, Ponder and active restart/chunk/multiplayer evidence remain. The magnetic cabinet still rejects operation. LIM/LSM, skid, physical catch/reset, flywheel, hydraulic and pneumatic equipment remain on the roadmap.

Implementation commit `a5f92af89cda8ef0a4300ca6a32f224e219c9ee9` was pushed to the private remote. [CI run 36810768572](https://github.com/StonyTark1117/Coasters-Launch-Systems/actions/runs/36810768572) passed a clean build and all eighteen packaged standalone native tests. Its downloaded JAR exactly matched local SHA-256 `001c41686ddeb8e1cc1317cc6bd9d7e321d6ee352e4c76f56cc1edcaced7f8d4`. [CI evidence](evidence/fin-ci.txt) identifies the source and artifact. Combined Fourth Dimension evidence remains local against the copied pinned upstream JAR; the full 0.4.0 goal remains active.
