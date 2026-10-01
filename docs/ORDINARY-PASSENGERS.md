# Occupied ordinary cart checkpoint: 0.0.1-dev.7

This advances LS003, LS004 and the loaded tire/brake evidence for LS101–LS103. It does not complete the 0.1 or 0.4 release gate.

## Native seat loading

The pinned Sable 2.0.3 stack accounts for blocks and merged contraptions, but the initial ordinary fixture proved that a real Create seat passenger contributed no native mass: boarding left the chassis at 22.0. Launch Systems now adds an equal declared load of **0.8 native mass units per seated rider** to ordinary owned bodies. This is an original game-scale convention, matching the existing Fourth Dimension load scale; player characteristics are never inspected.

The contribution acts at the seat's declared torso location, 1.5 blocks above its origin, with fixed torso inertia. It updates native self/merged mass and inertia before equipment force collection. Actual passengers, real seat blocks, plot membership and rider identities determine contributions. Dismount, removal of a seat or transfer to a primary provider removes ordinary contributions; returning ordinary ownership restores them once. The ordinary provider's rigid rivet descendants are included in its ownership set, but this checkpoint's equipment fixtures place seats on chassis bodies. A separate occupied rivet-child fixture remains open.

Fourth Dimension owns its own articulated seat loading. The ordinary loader acts only when the resolved motion provider is `simulatedcoasters:ordinary_0.1.5`; it neither supplies ordinary fallback over a primary veto nor adds mass to Fourth Dimension bodies. Existing combined fixtures continue to require exactly 0.8 added mass per 4D passenger.

## Recovery ordering

Passenger contributions are transient tracker data. A Sable native-body recovery rebuilds block mass and re-adds the body to its pipeline. Waiting until the next ordinary loading tick restored the correct total mass but moved the guide point by **0.0625 blocks**, because the body had already been initialized with its unloaded center of mass.

The new `ServerSubLevel.buildMassTracker` return hook reapplies the previously owned contribution to the fresh tracker before Sable adds that recovered body to its pipeline. This hook performs pure tracker work; it calls no JNI, writes no pose/velocity and creates no replacement solver or guide. The later loading tick sees the same tracker/contributions and does not replay them. Sable's merged tracker owns its normal center-of-mass frame bookkeeping.

The corrected recovery fixture retains native engagement and 25.6 total mass for its two riders. Guide-point displacement is about **0.000000004 blocks**, compared with the failing 0.0625 diagnostic. This is a real public `recoverSubLevel` operation, not an isolated internal tracker call. Calling `buildMassTracker` alone while a live body/constraints remain attached produced an invalid fixture lifecycle and is not advertised as supported recovery.

This evidence covers recovery of a still-loaded body with its tracked riders. Server restart, body/chunk reload, rider reconnect and saved center-of-mass reconstruction still require their own acceptance; the in-memory contributions are not saved as fabricated blocks or replayed launches.

## Occupied equipment evidence

Twelve new native equipment fixtures use four real seated pigs per chassis and fully occupied native six-block coupled pairs, in both travel directions. A continuous wooden/iron deck supports the actual Create seat blocks. Empty chassis mass is 24; four riders produce 27.2, and the eight-rider pair totals 54.4. Public native coupling establishes the pair; no fixture imposes a prescribed train path or velocity. Physical entry forces end before measured equipment operation.

| Equipment | Occupied single cart | Occupied coupled pair |
| --- | --- | --- |
| Tire, forward/reverse | Enters at about ±0.998 blocks/s; actual cabinet reaches about +2.144 / −1.823 | Reaches about +1.684 / −1.727; shared shaft work reconciles each physics step |
| Friction fin, forward/reverse | Enters at about ±1.996; stops near zero and keeps riders seated during the final hold | Same loaded stop/hold; native coupler error remains below 0.000001 blocks |
| Passive magnet, forward/reverse | Enters at about ±1.996; trims to about ±0.000140 and dissipates about 52.26 work units | Forward trims to about +0.0192; reverse exits the short contact and coasts near −0.7836 after dissipating about 87.31 work units |

Every tire request shares one actual shaft-work budget per cabinet; magnetic work opposes fin point motion and the cabinet's total force remains bounded. Fin sensors count actual live native clamps and measure stopping/hold, not queued point impulses. Recorded zero queued fin impulse/work is not zero constraint force or zero brake energy. All riders remain seated through measured equipment operation, native engagement remains valid, and coupled cars retain common translation.

The reverse magnetic pair demonstrates the existing contact-length limit: a two-block module can trim a loaded train and then lose contact while it still moves. This is expected passive trim, not a guaranteed final stop. The later controller must validate sufficient stopping distance and combine magnets with rated friction hold. These fixtures certify the measured straight-rail layout, not arbitrary entry speed, train length, slope hold or station/block control.

The separate passenger lifecycle fixture verifies boarding, a second rider, native body recovery, dismount/reboarding, empty seats, primary-provider handoff/return and occupied seat removal. The synthetic primary provider is an API ownership test only; it does not certify an external addon. Seat removal subtracts the native 0.5 block mass and the departed 0.8 rider contribution, leaving 23.5 from the empty 24.0 baseline.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PlsPackaged \
  -PlsFourthDimensionJar=/absolute/path/to/copied/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

Run sequentially; each recreates the disposable native world. The copied Fourth Dimension schema-4 JAR retains SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`. No upstream checkout or process was changed. Native fixtures use the [NeoForge 1.21.1 GameTest framework](https://docs.neoforged.net/docs/1.21.1/misc/gametest/); the native physics APIs are verified against the exact pinned Sable artifacts.

Full stopping envelopes, local arm/hold/launch/fault control, obstructed interface envelopes, adjacent-edge traversal, occupied rivet children, rendered geometry/animation, packaged client/dedicated gameplay, multiplayer and active save/chunk lifecycle remain open. LIM/LSM, skid, physical catch/reset, flywheel, hydraulic and pneumatic equipment remain on the [release roadmap](../ROADMAP.md).

Twenty unit checks, forty packaged standalone native tests and 102 packaged combined native tests passed with the same Launch Systems JAR, SHA-256 `d9974501e81216974c13b8d39c5c0e07af8526cf552e78ad7094b27d6639b692`. Evidence: [unit results](evidence/passengers-unit.txt), [packaged standalone](evidence/passengers-packaged-standalone.txt), [packaged combined](evidence/passengers-packaged-combined.txt), [artifact hashes](evidence/passengers-artifacts.sha256) and [pre-fix diagnostics](evidence/passengers-diagnostics.txt). Combined checks retain the earlier 4D rider/joint/coupler, hardware and PAX-veto evidence against the copied pinned upstream artifact.

Implementation commit `1691482d7bf4e020c16ebf00e706208015669f5e` was pushed to the private remote. [CI run 36817913452](https://github.com/StonyTark1117/Coasters-Launch-Systems/actions/runs/36817913452) passed a clean build, twenty unit checks and all forty packaged standalone native tests. Its downloaded JAR exactly matched local SHA-256 `d9974501e81216974c13b8d39c5c0e07af8526cf552e78ad7094b27d6639b692`. [CI evidence](evidence/passengers-ci.txt) records the source/artifact verification. Combined evidence remains local against the copied pinned Fourth Dimension JAR. The full 0.4.0 goal remains active.
