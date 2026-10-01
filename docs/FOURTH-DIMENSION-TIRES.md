# Fourth Dimension tire checkpoint: 0.0.1-dev.4

This extends the [dev.3 equipment prototype](EQUIPMENT-PROTOTYPE.md) with actual tire-equipment evidence for specific Fourth Dimension schema-4 vehicles. It advances LS003/LS004/LS101; it does not complete the 0.4.0 release gate.

## Certified scope

Install the chassis tire drive strip beneath the native representative bearing and use the existing two-block cabinet setup on fixed, level/unbanked rail. Known family/variant pairs are `ARROW/arrow_four_rail_v1`, `ZACSPIN/zacspin_legacy_v1`, `FREE_SPIN/sns_magnetic_v1`, and `ZACSPIN_REVISED/zacspin_revised_v1`. Other variants receive no certificate. Every native consist member needs its own installed strip and valid accepted support; a contacted strip receives the physical chassis force.

The adapter reads authoritative Fourth Dimension state. Restraints, capture, retention/maintenance, fault, stopping, support eligibility and PAX exclusive ownership remain authoritative. Missing/removed/invalid-mass carrier bodies veto the whole consist. The ordinary fallback never replaces an explicit Fourth Dimension veto. No rotor angles, velocities or vehicle poses are set by Launch Systems.

Validation used a copied Fourth Dimension artifact declaring `0.9.0-dev.1`, schema 4, SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`. Its source commit is not established. A matching development version string does not prove a different build matches this adapter. No Fourth Dimension source, checkout or running process was modified.

## Native validation

Eight required standalone tests and thirty-two required combined tests passed with the same packaged Launch Systems artifact. Nineteen new combined equipment tests cover four eligible families × full/uneven loads × forward/reverse, a powered PAX veto, and fully occupied native six-block Arrow pairs in both directions. Without Fourth Dimension installed, these combined fixtures are absent rather than counted as skipped successes.

Fixtures assemble vehicles through the pinned public vehicle/track factories, mount real pig passengers on Create seats, and use public restraint/retention/dispatch operations. Passenger mass increases by exactly 0.8 per rider. A separate fault/maintenance interruption checks authoritative state vetoes. The observer reads the real queued cabinet impulses before integration and measures joints afterward. It never injects launch forces. Positive incremental interface work includes angular effective mass and remains within one shared shaft budget; dissipation earns no shaft credit.

| Loaded fixture | Native mass | Speed magnitude after tire phase (blocks/s) |
| --- | ---: | ---: |
| Arrow, full | 19.2 | About 1.38 |
| Legacy ZacSpin, full | 26.4 | About 1.07–1.11 |
| Free Spin, full | 44.4 | About 0.67 |
| Revised ZacSpin, full | 46.4 | About 0.61 |
| Native Arrow pair, eight riders | 38.4 | About 0.69 |

All riders remain seated. Maximum native carrier joint separation is below 0.00031 blocks; the linked Arrow pair's maximum six-block coupler distance error is below 0.00007 blocks. Both linked parents retain engagement and common translation. Removing an authoritative carrier afterward vetoes both parents. PAX receives zero tire impulse.

Full revised loads rotate about 0.042 radians under this low-force tire drive. The copied raw-force fixture's 0.05-radian threshold was inappropriate for this much slower acceleration, so full-load equipment fixtures require a measurable response above 0.02 radians; uneven fixtures retain a 0.05-radian threshold and require carrier-group divergence. These measurements verify free response, not manufacturer fidelity or arbitrary profile traversal.

## Reproduction and evidence

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PlsPackaged \
  -PlsFourthDimensionJar=/absolute/path/to/copied/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

Run these sequentially: each test-server run recreates its disposable world. Preserve the copied upstream artifact and identify its hash. Evidence: [18 unit checks](evidence/fourthdimension-tire-unit.txt), [8 packaged standalone tests](evidence/fourthdimension-tire-packaged-standalone.txt), [32 packaged combined tests](evidence/fourthdimension-tire-packaged-combined.txt), [both JAR hashes](evidence/fourthdimension-tire-artifacts.sha256).

Fin and magnetic cabinets still reject operation. Rolling-entry/contact-envelope limits, ordinary passenger mass, controlled Arrow profile traversal, slopes/banks, rendered clients, multiplayer/restart/chunk interruptions, controller/reservations, Ponder and all other launch/brake mechanisms remain pending. Existing global Engineered/Additions ownership vetoes remain until optional adapters are validated.

Implementation commit `929ca5981de96986501e5d925de7e12dfa4a8ab8` was pushed to the private remote. [CI run 36808157969](https://github.com/StonyTark1117/Coasters-Launch-Systems/actions/runs/36808157969) passed a clean build and eight packaged standalone native tests. Its downloaded JAR exactly matched local SHA-256 `b1269e64d715d8abcf94e1d077bbfe167a6d3fff6464fb542ff44a69c5caecd5`. [CI evidence](evidence/fourthdimension-tire-ci.txt) identifies the source and artifact; the thirty-two-test Fourth Dimension matrix remains local against the pinned copied artifact.
