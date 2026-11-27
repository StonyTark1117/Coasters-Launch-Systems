# Launch Systems 1.0.1 release — 2026-11-27

Status: release package verified; Git tag and publication verification pending.
The human explicitly authorized pushing Git and publishing 1.0.1 as the FD 1.6
companion. Existing private repository and branch `codex/launch-systems-0-4-0`
are retained. Starting clean HEAD `de3500944621ae487898638438e7cd3d0c0b161f`
was already equal to remote HEAD. This release preparation changes documentation
only; implementation remains `498f70ade44e46191df77d1b165b6a66ed274421`.

The Java 21.0.13.1 / Gradle 9.2.1 `jar` build passed. The output is byte-identical
to the accepted FD 1.6 combined candidate:

`66ec71f1990feb232715e8c692d81a778d28f7056dc2f0cf1e460218651fb723`

Files: `coasters-launch-systems-1.0.1.jar` and `SHA256SUMS`. The persistent build
and immutable upload copies are in
`/mnt/NVME4/launchsystems-runs/ls101-release-20261127/`. The selected XFS mount
had 144GiB available and host memory had 45GiB available before the build.
No Minecraft server, client or Xvfb display was launched.

Command (with Java 21 selected in JAVA_HOME and PATH):

```sh
./gradlew jar \
  -PlsBuildDirectory=/mnt/NVME4/launchsystems-runs/ls101-release-20261127/build \
  --project-cache-dir /mnt/NVME4/launchsystems-runs/ls101-release-20261127/cache \
  --no-daemon --max-workers=2
```

[Build log](build.log.gz), [package audit](package-audit.json) and
[checksum](SHA256SUMS) record the selected bytes and expanded metadata.
FD is optional and exact versions 1.1.0/1.6.0 are advertised; no FD classes are
embedded. JSON resources parse. The artifact identity permits reusing the
[accepted integration](../../fd16-integration-20261127/README.md): native
Arrow/Wing, two-client occupied power/cold restart, fresh receiving handoff,
downstream exit, dependency/controller loss, eight mounting unit tests and
seven adapter math tests. Earlier standalone/fixed-track acceptance remains
valid. No repeated native campaign is needed for this unchanged artifact.

[Release notes](../../../releases/1.0.1.md) explain installation and finite
support. Historical integration records that say unpublished describe their
original handoff; this receipt owns the subsequent publication status. Existing
1.0.0 assets and repository visibility are unchanged. Hosted CI was previously
blocked before job execution by account billing limits; no hosted pass claimed.
