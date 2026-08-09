# Versioning and release process

## Scheme

Releases follow [Semantic Versioning](https://semver.org/) with the pre-release suffixes the
roadmap in [PLAN.md](PLAN.md) §10 uses.

| Field | Source of truth | Rule |
|---|---|---|
| `versionName` | `app/build.gradle.kts` | `MAJOR.MINOR.PATCH[-alphaNN\|-betaNN]`. Must equal the git tag without its leading `v`. |
| `versionCode` | `app/build.gradle.kts` | A monotonically increasing integer. **Never** reset, reused, or decreased — Android refuses to install an APK whose `versionCode` is lower than the installed one. |
| Git tag | `git tag` | `v<versionName>`, annotated, on `main`. |

Because distribution is via GitHub Releases and F-Droid rather than Play (PLAN.md §1.4), the
`versionCode` is ours alone to manage; nothing regenerates it.

### Planned line

| Version | Contents |
|---|---|
| `0.1.0-alphaNN` | Phase 0 foundation — current |
| `0.1.0` | v0.1: Screenshot Sweeper with OCR search |
| `0.2.0` | v0.2: Duplicate Photo Auditor |
| `0.3.0` | v0.3: App Permission Drift Tracker |
| `1.0.0` | Hardening, accessibility, OEM matrix, F-Droid submission |

## Branching

`main` is always releasable and protected. Work happens on short-lived branches named
`feat/…`, `fix/…`, `chore/…`, merged by pull request once CI is green.

CI runs on every pull request and blocks the merge on: ktlint/detekt, Android Lint, unit
tests, and the no-network manifest gate.

## Commit messages

[Conventional Commits](https://www.conventionalcommits.org/), so release notes can be
generated from history:

```
feat(screenshots): add OCR text index
fix(hashing): correct Hamming distance for inverted hashes
chore(build): bump AGP to 8.13.2
```

## Cutting a release

1. Update `versionName` and increment `versionCode` in `app/build.gradle.kts`.
2. Update the changelog entry.
3. Merge to `main` with CI green.
4. Tag and push:

```bash
git tag -a v0.1.0 -m "v0.1.0 — Screenshot Sweeper with OCR search" && git push origin v0.1.0
```

5. The `release` workflow builds, signs, scans, and publishes the APK to GitHub Releases.

## Signing

The release keystore is **never** committed. CI reconstructs `keystore.properties` and the
keystore from repository secrets:

| Secret | Contents |
|---|---|
| `KEYSTORE_BASE64` | The `.jks` file, base64-encoded |
| `KEYSTORE_PASSWORD` | Store password |
| `KEY_ALIAS` | Signing key alias |
| `KEY_PASSWORD` | Key password |

`app/build.gradle.kts` configures release signing only when `keystore.properties` exists, so
a plain `git clone` still builds an unsigned release without any secrets present.

**Keep a backup of the keystore.** Losing it means no existing installation can ever be
updated — every user would have to uninstall and reinstall, losing their data.
