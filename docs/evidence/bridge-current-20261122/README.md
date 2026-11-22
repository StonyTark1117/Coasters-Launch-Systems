# Current Fourth Dimension authority bridge — 2026-11-22

Owner `/root/bridge`, LS worktree `/home/braydon/Documents/Coasters: Launch Systems`, branch `codex/launch-systems-0-4-0`, base `1ac51d7`. Evidence records the named uncommitted bridge overlay, not the base alone. Current changes select the schema-8 registered external-authority adapter; the unchanged schema-4 implementation remains a separate legacy class/profile.

## Native acceptance

`native.log`: 3/3 required tests pass. `CurrentAuthorityGameTests` delegates existing actual-Sable Arrow and Free Spin forward tests plus the Arrow circuit, with assertions for actual native provider/controller identity. Arrow circuit reports six departures, two completed laps, capture/unload/reboard and a fresh departure; the whole train stops before atomic source-to-target authority transfer. Arrow loaded mass 20.2, Free Spin loaded mass 45.4, measured Free Spin rotor response 0.803769 rad. These synthetic occupants are not the connected-player proof.

`recovery.log`: 1/1 required test passes. `CurrentAuthorityRecoveryGameTests` serializes/deserializes native FoundationData and installs its restored authority record. The old live token cannot dispatch; the LS reservation remains for inspection. Explicit off/on rearming acknowledges the restored ownership but stays held until a fresh request. A subsequent native hold increments the sequence, rejects the cached token, and leaves the stopped car under retained ownership. This is serializer/token restoration, not a separate-JVM world reopen.

The first recovery fixture sampled `EngagedContacts` outside its same-physics-tick lifetime; it was corrected to assert persisted native hold state, actual chassis speed, exact token `permits`, then the next runtime controller fault. No production authority guard was relaxed. Earlier native attempts selected the wrong template namespace and out-of-bounds fixture slots; corrected slots 0–2 remain inside the hardware ±1024 guard. Only successful logs above earn acceptance.

## Contract and bounded support

- `FourthDimensionAuthority` requires `EXTERNAL_API == 1` and registration of `coasterslaunchsystems:local_controller`; schema-8 without that facade fails initialization. API-5 defaults cannot silently stand in for this provider. `LaunchSystems` chooses the legacy adapter only for schema 4.
- `supported` permits only the original exact Arrow, Zacspin, Free Spin and revised Zacspin variants, without recovery cart, alignment-required state, or managed FD route. PAX and later variants are excluded. These exclusion boundaries are source-reviewed; this record does not claim a new PAX/managed-route gameplay matrix.
- `FourthDimensionMotionProvider` retains primary ownership of unsupported FD snapshots; rejected FD bodies do not fall back to ordinary support. Unclaimed FD hardware cannot propel before a legitimate controller reservation.
- FD `RideAuthority.validateExternal` rechecks registration/predicate/managed-route eligibility on claim, refresh, transfer and explicit resume. Provider-specific controller resolution prevents external registrations from intercepting built-in controllers. `ExternalRideAuthorityGameTests` tests duplicate registration, second owner, stale transfer/release, capture, restored hold/resume, missing/replaced controller, predicate changes and native-controller isolation. Unknown persisted provider rejection is fail-closed source behavior; no claim of removing a registration in a live JVM is made.
- FD native tests and logs are committed in `8cebab779da3311b39e3471bbfb41ebf1dafcd19`, integrated by the FD owner as `235255cf21845cda0e4921b5d036425c55a9a66b`. That evidence is native-only and reused here.

## Inputs and resources

Java 21; profile `fd-current`; per-owner cache and output rooted at `/mnt/NVME4/launchsystems-runs/bridge-20261122`. Pinned FD JAR SHA-256 `df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e`. Both native servers saved all dimensions and exited. No display/client was needed for these checks.

Occupied Arrow continuation uses `arrow/`, display `:393`, game/RCON ports 25751/25752, owner `/root/bridge`, units `fd-ls-arrow-20261122-{server,xvfb,client}.service`. Packaged LS SHA-256 `67b2b08f571c78224be87b421010518c4cdc812362a1addf6b47ec68043b1d98`. This continuation passed Survival construction, empty/occupied circuit, normal exit/reboard/fresh departure, and separate-JVM world restoration. See [Arrow evidence](arrow/README.md). Its server, client, and Xvfb are now stopped, with saved world retained.
