# Hydraulic and pneumatic cable launch

Both cabinets drive the physical carriage described in [cable mechanics](CABLE-MECHANICS.md). They use the same straight, level, unbanked guide, certified catch socket, 12-block pull, 20-block guide, downstream friction-fin bank, 64-unit loaded mass ceiling and physical return. A pressure label does not grant a vehicle a catch certificate. Ordinary carts and the explicitly certified original Arrow use the shared catch contract; other articulated families remain excluded.

## Stores and charging

These are bounded game-unit models, not real-world pressure-vessel specifications. A Create shaft supplies charging work; no external pneumatic mod, fluid-network mod or FE converter is required. The pump, return reservoir and accumulator form the hydraulic cabinet's internal closed circuit. The pneumatic cabinet contains its compressor, receiver and valve/actuator supply.

| Behavior | Hydraulic | Pneumatic |
| --- | --- | --- |
| Stored work | Gas-backed accumulator with polytropic exponent 1.4 | Finite isothermal receiver exergy relative to ambient |
| Native parameters | Gas volume 4, minimum volume 2, precharge 250, ambient 10 | Receiver volume 4, maximum pressure 200, ambient 10 |
| Shaft charging efficiency | 0.72 of the shared charging budget | 0.55 of the shared charging budget |
| Force | Pressure above ambient times piston area 0.7 | Pressure after expansion into `0.1 × travel` volume, above ambient, times area 0.4 |
| Ready threshold | 80% of capacity | 25% of capacity |
| Discharge | Oil leaves the accumulator and returns to the internal reservoir; total circulating oil remains 2 volume units | Finite stored work and expanding volume reduce available force |

The span-tool status reports work, charge readiness, physical phase and pressure. Hydraulic status also reports charged and returned oil; pneumatic status explains expansion. Readiness permits partial charge above the mechanism's threshold, not an unlimited-energy launch. The native rig charges positive impulse work and conversion loss to the selected store. It never credits braking as new pressure.

## Player operation and service

1. Place one pressure cabinet and bind it to the same directed edge as the controller, catch-home fin and uninterrupted downstream fin bank. Install the certified socket and continuous guide. Link the disarmed modules, then arm only after inspecting support and clearance.
2. Power the charging shaft and catch-home fin. Check the displayed work and readiness; dispatch requires a fresh redstone request after the controller reports ready. An undercharged cabinet cannot dispatch.
3. During a pressure-funded pull, stopping only the charging shaft does not remove stored pressure. The launch fin must still have release power. Guide/support loss or a broken ownership/catch contract faults the retained installation and stops further launch work.
4. The catch physically releases before the guide end. The released carriage brakes and returns using shaft power. Removing that shaft during return leaves the carriage short of home and prevents premature reset; restore power and wait for actual home. Pressure charging resumes when the catch is no longer in use.
5. After an interrupted or restarted installation, inspect the saved carriage and guide. At home, active-catch release requires the exact retained controller reservation and applied linked fin clamps: use the span tool on the cabinet with a redstone torch offhand. A compass requests idle retirement. Rebind/rearm explicitly after inspection; restored power or an old high signal cannot substitute for a fresh dispatch.

The recipes use Create iron/brass sheets, a shaft and precision mechanism. Hydraulic adds a copper block; pneumatic adds light-blue concrete. Those ingredients create distinct self-contained cabinets; the recipe does not promise an external oil tank, fluid pipe or compressor network. The shared Ponder cable guide teaches the real socket, guide, finite-store and downstream-hold workflow.

## Upstream hydraulic re-audit — 2026-11-22

[Create Coasters Simulated Additions 0.2.5, file 9243346](https://www.curseforge.com/minecraft/mc-mods/create-coasters-simulated-additions/files/9243346) advertises a staff-configured hydraulic/flywheel/tire force span. [Create Coasters Extras/Tracks and Balloons 1.2, file 8718286](https://www.curseforge.com/minecraft/mc-mods/create-coasters-extras/files/8718286) advertises a hydraulic-style boost track. These published descriptions do not establish a finite closed-oil-loop store or a physically returning catch. Neither artifact is a required dependency or an accepted force owner for this release. This re-audit preserves LS401's original finite mechanism; optional force-overlap integration remains the separately scoped LS803 work in [optional integrations](OPTIONAL-INTEGRATIONS.md). Do not combine unqualified active force spans with this cable section.

## Qualification

The unchanged model, pressure shaft-loss, physical return interruption, retained-world and guide-loss claims reuse dev.29–35, dev.53 and dev.62 records in [validation](VALIDATION.md). Current adapter and player/service closure is recorded in the [0.4 pressure evidence](evidence/ls04-pressure-20261122/README.md); that record distinguishes historical pins from the current artifact and does not inherit obsolete historical "still open" lists as new gates.
