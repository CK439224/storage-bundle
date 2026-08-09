# Changelog

Notable changes per release. Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/);
versioning follows [VERSIONING.md](VERSIONING.md).

## [Unreleased]

### Added — Phase 0 foundation

- Gradle build on AGP 8.13.2 / Kotlin 2.2.21 / KSP 2.2.21-2.0.5, wrapper pinned to Gradle
  8.14.3, with every dependency pinned exactly in a version catalog.
- Convention plugins in `build-logic/` shared across eleven modules.
- `NoNetworkPermissionCheckTask`: parses the merged manifest and fails the build if any
  network permission appears. Verified working — `transport-backend-cct` (via ML Kit) does
  declare `INTERNET`, and it is stripped at merge time.
- Room database with the shared `media_signature` table, schema export, and DataStore
  preferences.
- Hilt wiring including WorkManager's injected worker factory.
- Material 3 design system: theme with dynamic colour, type scale, spacing tokens.
- Permission gateway enforcing request-at-first-use and fail-secure behaviour.
- Perceptual hashing (dHash + Hamming distance) in an Android-free module, with 10 unit tests.
- detekt with the ktlint rule set; Android Lint; CI, CodeQL, Dependabot and a release
  workflow that verifies the tag matches `versionName`.
- ABI splits, cutting the download from 44 MB universal to 16 MB (arm64-v8a).

### Known gaps

- Split APKs share one `versionCode`; per-ABI offsets are required before v0.1
  (see [VERSIONING.md](VERSIONING.md)).
- Feature modules ship placeholder routes only — the features themselves are v0.1–v0.3.
