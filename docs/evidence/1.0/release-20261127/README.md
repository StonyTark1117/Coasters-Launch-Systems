# Launch Systems 1.0 release qualification — 2026-11-27

Status: package qualified; tag/publication verification follows this release
source commit. LS1001 consumes accepted core outcomes without changing gameplay.
Existing checkout `/home/braydon/Documents/Coasters: Launch Systems`, branch
`codex/launch-systems-0-4-0`, starting source
`a97c63f224f4344f677080f9e937d05d47bd3e9c`. Root owns integration/publication;
foundation reviewed acceptance, bridge reviewed dependencies/licenses and rides
prepared the user-facing release notes. No new branch, dependency or optional
adapter is introduced.

## Artifact and validation

- `coasters-launch-systems-1.0.0.jar`, SHA-256
  `02573aeeab64bfa38da2ca41b2927f46649e2a4044264350f15f13f9c8e69176`.
- Java 21.0.13.1, Gradle 9.2.1, selected `fd-current` dependency profile without
  loading optional FD. Local `build` passed and executed all 31 unit tests with
  zero failures/errors/skips. See [build log](build.log.gz) and
  [unit results](unit-results.json).
- [Package audit](package-audit.json): all 409 ZIP entries match the accepted
  0.9 entry set. The sole changed entry is expanded NeoForge metadata, changing
  only `version="0.9.0"` to `version="1.0.0"`. All 183 compiled class entries
  (including 109 runtime classes), assets, recipes, templates and license are
  byte-identical. All 100 JSON documents parse, and no dependency JAR is bundled.
- [SHA256SUMS](SHA256SUMS) identifies the published binary. Persistent build and
  immutable upload copy are under
  `/mnt/NVME4/launchsystems-runs/ls10-release-20261127/`.

Build command (Java 21 selected through JAVA_HOME and PATH):

```sh
./gradlew build \
  -PlsBuildDirectory=/mnt/NVME4/launchsystems-runs/ls10-release-20261127/build \
  --project-cache-dir /mnt/NVME4/launchsystems-runs/ls10-release-20261127/cache \
  --no-daemon --max-workers=2
```

No graphical client or server was launched for this version-only promotion.
The single-use Gradle build exited successfully; accepted runtime cleanup remains
in the earlier receipts. No new physics or gameplay run is required by the
[validation policy](../../../VALIDATION-POLICY.md) because every compiled class
and non-version resource is unchanged.

## Required gates carried forward

| LS1001 prerequisite | Accepted evidence |
| --- | --- |
| LS001–LS005, LS101–LS106 | [Foundation, usable rides and lifecycle](../../0.1/integration-20261122/README.md) |
| LS201–LS205, LS301–LS303, LS401–LS403 | [Complete six-launch outcome](../../0.4/integration-20261122/README.md) |
| LS501–LS504 | [Finite FD family certification](../../0.5/integration-20261126/README.md) |
| LS601–LS602 | [Bounded Arrow operations and occupied recovery](../../0.6/integration-20261126/README.md) |
| LS901–LS904 | [Matrix, provider lifecycle, performance and occupied release gameplay](../../0.9/integration-20261127/README.md) |

The 0.9 input hash is
`693a955ed3855a2a4cfa212d562b01fcce270501db0868629029d42f2518adca`.
Exact class/resource parity carries these compatible results into 1.0; no closed
case is reset by the version label. The current queue identifies no remaining
core behavior criterion.

## Installation and publication boundaries

[Release notes](../../../releases/1.0.0.md) supply the exact public standalone
installation links and supported pins. The unchanged dependency/license audit
is retained in the [0.9 package record](../../0.9/integration-20261127/README.md).
The optional qualified FD input remains the unpublished local `d676d730…`
API1 snapshot; it is neither bundled nor replaceable by the published FD1.5.0
release. Optional LS801–LS807 adapters are not advertised. Existing-world provider
seeding and recovery requirements remain prominent in the notes/operator guide.

Publication uses tag `v1.0.0` and a stable GitHub release with the exact JAR and
checksum. The existing repository is private and its visibility is unchanged.
No Modrinth/CurseForge project or wider publication channel is configured here.
The GitHub Actions account cannot start jobs because of billing/spending limits;
this is disclosed, with no hosted-CI pass claimed. Sable's Maven endpoint returned
403 during the dependency audit, while its public binary download was available;
this cached local build does not establish fresh-network build availability.

After publication, the current branch's delivery receipt records the exact
release/tag/source, downloaded-asset verification and actual CI disposition.
