# Security Policy

## Reporting a vulnerability

Please report security issues privately through GitHub's
[private vulnerability reporting](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability)
on this repository, rather than opening a public issue.

Include the affected version, the device and Android version, and steps to reproduce. We aim
to acknowledge within 72 hours.

## Supported versions

Only the most recent release receives security fixes while the project is pre-1.0.

## Security properties this app claims

These are design invariants, not aspirations. If you find one of them broken, that is a
vulnerability and we want to hear about it.

### 1. No network access

The app declares **no `INTERNET` permission**, and none of the other network-capable
permissions. No data can leave the device, because the app cannot open a socket.

This is enforced at build time. `checkReleaseNoNetworkPermission` parses the **merged**
manifest and fails the build if a network permission appears — including one contributed by a
transitive dependency, which is the realistic failure mode. The build already relies on this:
`transport-backend-cct`, pulled in via ML Kit, declares `INTERNET`, and the manifest merger is
configured to strip it.

Verify it yourself on any release APK:

```bash
aapt2 dump permissions app-release.apk
```

### 2. Everything stays on the device

Image analysis, perceptual hashing, and OCR all run locally. There is no analytics, no
telemetry, and no crash reporting service.

### 3. Nothing is deleted without consent

Deletion goes through Android's own `MediaStore.createTrashRequest()` / `createDeleteRequest()`
consent dialogs. Trash-first is the default, and it is recoverable for roughly 30 days. The app
never deletes media silently.

### 4. Scan data cannot be extracted from the device

`android:allowBackup="false"`, and `data_extraction_rules.xml` excludes every domain from both
cloud backup and device transfer. This matters most for the OCR index, which contains text
lifted off the user's screen and is the most sensitive thing the app holds.

## Scope

In scope: anything that breaks the four properties above, leaks the OCR index, causes
unintended data loss, or escalates the app's access beyond its granted permissions.

Out of scope: findings that require a rooted device or physical access with an unlocked
bootloader; issues in Android itself; and the deliberate use of `QUERY_ALL_PACKAGES`, which is
the App Permission Drift Tracker's core mechanism and is documented in [PLAN.md](PLAN.md) §5.3.

## Development practices

- Code is reviewed against the [OWASP Secure Coding Practices checklist](https://owasp.org/www-project-secure-coding-practices-quick-reference-guide/stable-en/02-checklist/05-checklist);
  the mapping is in [PLAN.md](PLAN.md) §6.
- ktlint, detekt, and Android Lint block merges.
- Dependencies are pinned exactly in `gradle/libs.versions.toml` and scanned by Dependabot and
  OWASP Dependency-Check.
- CodeQL runs on every push to `main`.
