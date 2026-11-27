# First marketplace uploads — 2026-11-27

The human authorized creation of Launch Systems projects and upload of both
1.0.0 and 1.0.1 as betas using the existing Zen browser. All four uploads are
confirmed. Both new projects await moderation; public availability is not yet
confirmed. Existing GitHub releases and JAR version strings are unchanged.

## Platforms

- [Modrinth project](https://modrinth.com/mod/coasters-launch-systems): both
  versions show the Beta badge, Minecraft 1.21.1 and NeoForge. Project submission
  succeeded and displays **Under review**. [Versions proof](modrinth-betas.png),
  [submission proof](modrinth-review.png).
- [CurseForge project](https://www.curseforge.com/minecraft/mc-mods/coasters-launch-systems):
  project **1835112**. The author dashboard shows both files as **Beta / Under
  Review** with Minecraft 1.21.1, Java 21 and NeoForge metadata.
  [Author dashboard](https://authors.curseforge.com/#/projects/1835112/files),
  [upload proof](curseforge-betas.png). The project preview confirms its name,
  slug, MIT license, icon and description; public downloads await moderation.

Both versions require client and server installation, Create, Create: Coasters
Simulated and Sable. Tested versions are Create 6.0.10, Coasters Simulated 0.1.5
and Sable 2.0.6, together with their dependencies. Platform relationships link
the three required projects; they do not pin exact dependency versions.

Fourth Dimension remains optional. **Use LS 1.0.1 for FD 1.6.0 integration.**
CurseForge 1.0.1 has an optional relation to FD project 1809752. Modrinth's picker
did not resolve the pending FD project by name or verified ID `4eicZbtp`, so no
Modrinth FD relationship was added. Instead, the description states the version
requirement and the 1.0.1 changelog links directly to
[FD 1.6.0](https://modrinth.com/mod/coasters-fourth-dimension/version/pPt5lvrN).
This is a marketplace relationship limitation, not a hard dependency or a code
compatibility failure. The direct optional relation can be added when the picker
resolves the project.

## Artifacts and scope

| Version | Uploaded artifact SHA-256 |
| --- | --- |
| 1.0.0 | `02573aeeab64bfa38da2ca41b2927f46649e2a4044264350f15f13f9c8e69176` |
| 1.0.1 | `66ec71f1990feb232715e8c692d81a778d28f7056dc2f0cf1e460218651fb723` |

Upload sources:

- `/mnt/NVME4/launchsystems-runs/ls10-release-20261127/artifacts/coasters-launch-systems-1.0.0.jar`
- `/mnt/NVME4/launchsystems-runs/ls101-release-20261127/upload/coasters-launch-systems-1.0.1.jar`

The existing [1.0.0](../../evidence/1.0/release-20261127/README.md) and
[1.0.1](../../evidence/1.0.1/release-20261127/README.md) artifact verification and
acceptance carry forward. No code, embedded assets or packaged metadata changed,
and no runtime was launched. Marketplace upload results were visually verified;
marketplace download hashes have not been independently checked while review is
pending. The GitHub release downloads were already verified in those receipts.

The listings distinguish the standalone/fixed-track 1.0.0 baseline from the
accepted original Arrow/Wing moving handoffs in 1.0.1. Tire/LSM/LIM and fin
brake integration preserves native retention during movement and requires
destination release, receiving-side arming and a fresh request before departure.
Other moving families, moving cable/swing and managed circuits are not advertised
as certified.

## Listing assets and disclosures

Both projects use the MIT license and the same original LS icon. CurseForge uses
Technology and Addons; Modrinth uses Technology and Transportation. Modrinth's
AI-content disclosure is enabled for code, assets and text, with an explanation
of AI coding assistance, the generated icon, and assisted documentation. The
mod does not offer generative AI functionality.

The [PNG icon](project-icon.png) was generated with the built-in image generation
tool: silver LS lettering, an orange launch arrow and coaster rails against a
dark industrial background. The [WebP icon](project-icon.webp) is a lossless
encoding of the same pixels to meet Modrinth's 2 MiB upload limit. These are
listing assets only and are not modifications to the released JARs.

Work continued in `/home/braydon/Documents/Coasters: Launch Systems` on
`codex/launch-systems-0-4-0`, starting at
`fb543e105f5de4a12c98417a2d300c309c4f9148`. This publication record and listing
assets are the only new work. The repository remains private.
