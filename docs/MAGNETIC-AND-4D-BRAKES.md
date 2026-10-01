# Magnetic and Fourth Dimension brake checkpoint: 0.0.1-dev.6

This advances LS003, LS004, LS102 and LS103. It is a development checkpoint toward 0.4.0; full release gates remain open.

## Passive magnetic equipment

Install the **Chassis Conductive Brake Fin** at the representative bearing's local west/below cell, `(-1, -1, 0)`. This copper fin is separate from the friction fin at `(1, -1, 0)` and the tire strip directly below the bearing. The installed block supplies its own magnetic interface certificate; other strips do not substitute for it.

Place, bind and arm a Magnetic Brake Module using the span tool. The cabinet owns at most two blocks around its rail projection, with a swept 0.6-block bearing half-footprint. It supports fixed unbanked rail through a 1-in-4 grade and a transverse horizontal shaft. These geometry limits match the friction cabinet. Contact length still determines where force can act; this is not a stopping-distance guarantee.

The armed module is passive. It needs no shaft rotation, consumes zero Create stress and keeps working after shaft power loss. Manual disarming removes its effect. Its original game-scale curve is `F = -64 v / (1 + (v/4)^2)`, multiplied by the thermal force fraction and capped by the provider's brake rating and a shared 128-unit force budget per cabinet. Here `v` is the actual conductive-fin point speed, including chassis rotation. A further half-no-reversal impulse bound makes each free-body request dissipative near rest. The native guide and articulated joints can exchange energy with the chassis; this bound does not prescribe the train's resulting speed or prevent natural carrier motion.

Actual incremental interface work includes the native fin lever arm and effective mass. Negative work heats the cabinet; it never becomes shaft or FE credit. Heat capacity is 4096 work units with cooling at 8 units/second while loaded. Force stays full through capacity, then falls linearly to a minimum fraction of 0.1 at 1.9 times capacity. Heat can exceed capacity. Schema 2 persists heat and module identity; schema 1 migrates with zero heat while preserving its binding and identity. Unknown schemas reject loading. This checkpoint verifies NBT reconstruction, not active server restart or chunk reload.

Magnets approach zero force at rest and cannot provide rated static hold. Use a friction fin clamp for final stop and hold. The ordinary slope fixture starts at rest after a temporary fixture-only preparation constraint is removed: actual magnetic force dissipates work while the car rolls downhill. A separate flat disturbance produces motion despite the active magnet. Neither fixture uses a preparation constraint during measured braking.

## Independent brake authority

Provider API 3 adds `maximumBrakeForce` independently of propulsive `maximumForce` and `maximumAcceleration`. API-1 and API-2 snapshot overloads grant no API-3 brake authority because they lack an explicit brake rating. A positive finite rating is required when braking is permitted.

The isolated Fourth Dimension adapter recognizes the same four exact schema-4 variants as the tire checkpoint: Arrow, legacy ZacSpin, Free Spin and revised ZacSpin. It derives fin/magnetic certificates from physically installed chassis hardware. Its conservative brake rating is `min(256, loaded articulated mass * 2)` per chassis, retaining the acceleration scale of the earlier native articulated-force proofs. Cabinet allocation can reduce that rating further. PAX has no certificate or external launch/brake authority while its winch owns the vehicle.

Support, closed restraints, released capture, maintenance, faults, stopping and the whole linked consist remain authoritative. Missing or invalid carrier bodies deny operation without ordinary-provider fallback. Unavailable carrier testing removes one body with Sable's `UNLOADED` reason while keeping the vehicle record and chassis; permanent destruction correctly invokes Fourth Dimension's rider recovery and whole-vehicle removal instead. This fixture does not prove real chunk scheduling or later reload.

## Native evidence and limits

Nine new standalone equipment fixtures cover cold forward/reverse trim, mass 64, rest/disturbance, slope, missing fin, competing owners, seeded overheat and shaft power loss. Mass-26 cold cars enter at about ±1.9955 blocks/s and reach about ±0.01421 after two seconds. Their peak force is about 102.40, dissipated work 42.53 and remaining heat 26.73 after cooling. Mass 64 reaches about 1.00277 blocks/s under the same entry. A seeded heat of 8192 reduces peak force to 10.24 and leaves speed about 1.7589. Each physics step reconciles observed impulse work with heat and cooling. Missing hardware and conflicts produce no partial force.

The slope car moves downhill at about 1.1214 blocks/s despite magnetic work of about 85.03. The flat disturbance moves about 0.356 blocks and reaches 0.3064 blocks/s while the magnet dissipates work. This distinguishes trim from static hold. Removing the physical copper fin revokes its certificate and ends force. Invalid/extrapolated native contact parameters (`t < 0`, `t > 1`, NaN and infinity) are rejected instead of clamped to a rail endpoint. The actual slope fixture exposed this guard requirement by rolling beyond a native edge; the previous unchecked distance lookup could crash the server.

Thirty-eight new combined fixtures cover friction and magnetic equipment on all four families with full/uneven passengers in both directions, plus fully occupied six-block native coupled Arrow pairs and PAX vetoes. Physical entry forces end before measured braking. Each passenger contributes exactly 0.8 native mass. Friction equipment uses real finite native constraints; magnetic equipment queues observed point impulses. Veto checks observe actual brake impulses and live native clamp handles. Maintenance interrupts braking, and an unloaded carrier denies operation throughout the consist for at least ten native steps. Free families retain independent rotor response; no angle, angular velocity, train velocity or pose is set by the brake implementation.

These are straight-rail, loaded equipment fixtures against the copied upstream JAR with SHA-256 `027ed99261ec8ce45a7916fcaf6d65f252f9d359421a384c44f861217169af16`. The declared `0.9.0-dev.1` version alone does not identify another upstream build. No Fourth Dimension checkout or process was changed. They do not certify 4D slope hold, profile traversal, arbitrary entry speeds, emergency stopping envelopes, rendered physical reach, active restart/chunk lifecycle or multiplayer.

Twenty unit checks, twenty-seven packaged standalone native tests and eighty-nine packaged combined native tests passed with the same Launch Systems JAR, SHA-256 `89eb76642bc9e3b188d683611bf018c90d5b5fe7d0a341ca167464d2e411b079`. Maximum loaded joint separation across the new brake fixtures was 0.001681 blocks for Arrow, 0.005045 for legacy, 0.001288 for Free Spin and 0.001633 for revised. Arrow coupler separation error stayed below 0.000088 blocks. Maximum final friction speed was about 0.005225 blocks/s. All passengers remained seated through the measured braking phase; later carrier unavailability intentionally removes its seat bodies and is a separate ownership-veto check.

Evidence: [unit results](evidence/brakes-unit.txt), [packaged standalone](evidence/brakes-packaged-standalone.txt), [packaged combined](evidence/brakes-packaged-combined.txt), [artifact checksums](evidence/brakes-artifacts.sha256), [pre-fix endpoint diagnostic](evidence/brakes-contact-diagnostic.txt), and [carrier fixture correction](evidence/brakes-carrier-diagnostic.txt). Native clamp impulses are not included in the queued-force sensor; fin evidence measures actual handles, loaded stopping/hold and joint response. The recorded zero queued fin impulse/work is not a claim of zero constraint force or zero brake energy.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer -PlsPackaged --no-daemon --max-workers=2
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PlsPackaged \
  -PlsFourthDimensionJar=/absolute/path/to/copied/fourthdimension-schema4.jar --no-daemon --max-workers=2
```

Run sequentially; each recreates the disposable native test world. Complete controller, stopping envelopes, ordinary passengers, rendered client/multiplayer lifecycle, Ponder and remaining launch mechanisms stay on the [roadmap](../ROADMAP.md).
