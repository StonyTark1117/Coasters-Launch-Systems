# CC0 replacement publication — 2026-11-27

The human authorized changing Launch Systems from MIT to CC0 and replacing
uploaded packages, including repairing the missing CurseForge 1.0.1 changelog.
Source code and original assets now use CC0 1.0 Universal (`CC0-1.0`), with the
same full legal text as Fourth Dimension. Third-party licenses remain intact.

## Package verification

The Java 21 Gradle JAR build passed for 1.0.1. The accepted 1.0.0 JAR was
repacked with the same two license changes. ZIP integrity and complete entry
comparisons passed for both: only `META-INF/neoforge.mods.toml` (the license
identifier) and `META-INF/LICENSE-coasterslaunchsystems.txt` differ. Every
other entry is byte-identical to the accepted release, so gameplay acceptance
carries forward without another runtime launch. No test runtimes were started.
See [artifact identities](artifacts.json) and [build log](build.log).

| Version | CC0 SHA-256 |
| --- | --- |
| 1.0.0 | `ac9ccdc01dc05b99f98c9996ccbb39de43f54aab4a1a5179e3e260927b9b5493` |
| 1.0.1 | `cf6581db4b7b12f59daa52d81d2a67bed21650fa28196f828568116da8a2014f` |

Artifacts and the original packages are preserved on persistent storage at the
paths in `artifacts.json`. Packaging source commit: `6bf06b4`.

## Published replacements

- GitHub: both existing releases have replacement JARs, checksums and updated
  notes linking the CC0 source commit. Downloaded JARs and checksum files match
  the local replacements byte for byte. Original release tags remain historical
  qualification snapshots; they were not force-moved. Repository stays private.
  [1.0.0 receipt](github-1.0.0.json), [1.0.1 receipt](github-1.0.1.json).
- Modrinth project `83qFweBK`: project license is `CC0-1.0`, description and both
  changelogs are updated. Existing version IDs `DcYKnKNH` and `x5GP1hOC` now each
  contain one CC0 JAR; old MIT files were removed after verifying the replacement
  download. Both final CDN downloads match local bytes, and dependency relations
  are unchanged. The current API ignores its legacy `primary_file` patch and
  reports `primary: false`; each version has exactly one downloadable file.
  [Readback](modrinth.json). Project remains processing.
- CurseForge project `1835112`: saved custom license explicitly names CC0-1.0,
  links the full legal text and identifies the packaged full license. Description
  now says CC0. New beta files **9849322 (1.0.0)** and **9849324 (1.0.1)** replace
  the old uploads, which were reversibly archived. The dashboard shows only the
  two CC0 replacements in its active file list, both Under Review. The existing
  Unlisted project setting was preserved. Upload receipts retain exact metadata:
  [1.0.0](curseforge-1.0.0.json), [1.0.1](curseforge-1.0.1.json).
- CurseForge 1.0.1's missing changelog was supplied on the original file
  `9848345` before archival. The replacement's saved Markdown and rendered
  preview were read back in its author editor: compatibility changes and CC0
  packaging notes are both present. Minecraft 1.21.1, NeoForge, Java 21,
  Client/Server and Beta metadata also read back correctly. CurseForge upload
  acceptance and author-dashboard readback are verified; a public CDN download
  is not claimed while initial project moderation is pending.

This supersedes the license and download identities in the
[initial marketplace receipt](../marketplaces-20261127/README.md) and original
GitHub publication evidence. Those records remain intact as historical evidence.
