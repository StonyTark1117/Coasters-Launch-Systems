# Equipment checkpoint: 0.0.1-dev.3

This historical dev.3 checkpoint advances LS005/LS101 toward the full 0.4.0 goal. The later [dev.4 tire checkpoint](FOURTH-DIMENSION-TIRES.md) certifies specified Fourth Dimension variants and occupied linked Arrow tires. It does not complete either package or a release gate.

## Current tire prototype

1. Include a **Chassis Tire Drive Strip** immediately underneath the representative native Coasters bearing in the assembled ordinary chassis. Removing it revokes tire eligibility.
2. Build level, unbanked fixed rail within 1024 blocks of origin. Place a tire cabinet within three blocks of its rail contact, with its shaft transverse to the rail. Moving rail, slopes, banks and ambiguous edges are rejected by this prototype.
3. Use the span tool on the first anchor, the second anchor, then the disarmed cabinet. Anchor order defines positive travel. Shift-use clears the tool's selection.
4. Connect Create shaft power. Shift-use the cabinet with an empty hand to arm/disarm it; normal use reports status. Rated RPM magnitude is 1–128. Zero/unrated RPM coasts; negative RPM reverses the wheel surface direction.

The cabinet owns at most two blocks of rail centered on its placement contact. Runtime uses the native accepted guide frame and a swept 0.6-block half-footprint; it never drives the nearest free cart. Binding stores UUID, endpoints, shaft axis, geometry fingerprint and distance interval, without native body/constraint handles. A missing/changed support, rotated shaft, missing interface or ownership conflict latches a fault and disarms the cabinet. Inspect and rebind before arming again.

Original game-scale ratings: wheel radius 0.4, traction force 20, torque 8, acceleration demand ramp 4 units/second, fixed Create stress consumption 16. Provider force/acceleration ratings can further limit operation. Create stress units are not real watts. Available shaft work is torque × angular shaft speed × timestep, shared across every contacted consist. Current point speed and effective impulse mass include the drive strip's angular lever arm; dissipation grants no extra shaft credit. A reduced slip/traction envelope overrides stored ramp demand immediately.

Forces act at the installed chassis strip. Native rail constraints retain guidance; the addon does not set cart velocity, pose, carrier rotation or angular velocity. At dev.3, Fourth Dimension's provider returned no certified equipment interfaces, while preserving its existing retention/readiness/maintenance/PAX vetoes. Its raw-force regression tests do not establish tire compatibility.

Engineered (`coastersengineered`) 1.1.1 uses a version-pinned public registry bridge: disjoint Launch Systems equipment can run, while an active Engineered launch, trim or stop command at the same cart contact faults the Launch Systems module before it adds force. Additions (`coasterfins`) still disables cabinet operation until an ownership adapter exists. Native chain-span ownership is checked across every loaded consist member. Other force addons are not yet coordinated or advertised as supported. Concurrent cabinets claiming one consist are conservatively rejected; intentional distributed drive groups need LS104.

## Validation and reproduction

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PlsPackaged \
  -PlsFourthDimensionJar=/absolute/path/to/copied/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

GameTests recreate only the disposable validation world. Combined validation uses the copied schema-4 artifact identified in [checksums](evidence/equipment-artifacts.sha256); no sibling source or process is modified.

Eight standalone native tests cover the earlier point-force foundation, real tool/placement/persistence/recipe interaction, powered forward and reverse transport, negative shaft RPM, ballasted transport, missing hardware and overlapping ownership. Eighteen mathematical tests include the regression preventing actuator history from driving against physical tire slip. The combined installation additionally runs the existing five Fourth Dimension raw-force/ownership fixtures.

Evidence: [unit results](evidence/equipment-unit.txt), [packaged standalone](evidence/equipment-packaged-standalone.txt), [packaged combined](evidence/equipment-packaged-combined.txt), [checksums](evidence/equipment-artifacts.sha256).

[CI run 36804981215](https://github.com/StonyTark1117/Coasters-Launch-Systems/actions/runs/36804981215) passed for implementation commit `c2d192390c24f199b19ce6b40b7a96b42ccdce73`. It ran a clean build and the eight packaged standalone native tests. The downloaded CI JAR exactly matched the local SHA-256 `bda99f6f836a5a6fefaad926954609ce79997763fa9774544bf8b72159af3b49`. The combined Fourth Dimension evidence remains local against the identified copied artifact. See [CI excerpt](evidence/equipment-ci.txt).

## Remaining acceptance

Fin and passive-magnetic cabinets are registered scaffolds with original models/recipes, and reject operation. They do not yet brake or hold a vehicle. Full obstruction/clearance checks, tire wheel animation/rendered QA, dynamic stress, rolling entry, linked consists, ordinary passengers, Fourth Dimension tire certification, controllers, reservations, Ponder and multiplayer lifecycle evidence remain. Save/load reconstruction was tested; active server restart/chunk loss is not yet accepted. LIM/LSM, skid, catch, physical reset, flywheel, hydraulic and pneumatic equipment remain on the roadmap.
