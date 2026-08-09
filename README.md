# Storage Bundle

**See what has piled up.**

An Android app that surfaces the things that accumulate on your phone without you noticing —
old screenshots, near-duplicate photos, and permissions that quietly widened after an app
update.

It is not a "cleaner". Deletion is an option it offers, not the point of it.

> **Status: Phase 0.** The foundation — build, module structure, DI, persistence, design
> system, permission gateway, CI — is complete and verified. The features themselves land in
> v0.1–v0.3. See [PLAN.md](PLAN.md) §10.

## Why it is different

**It has no network access at all.** The app declares no `INTERNET` permission, so nothing it
sees can leave your device. That is not a promise in a privacy policy — it is a property you
can verify yourself:

```bash
aapt2 dump permissions app-release.apk
```

The build enforces it too: a Gradle task parses the merged manifest and fails the build if any
network permission appears, including one contributed by a dependency. See [SECURITY.md](SECURITY.md).

## The three tools

| Tool | What it does | Release |
|---|---|---|
| **Screenshot Sweeper** | Finds and clusters screenshots, and — the part that makes it worth keeping — indexes their text on-device so you can search for "receipt" or "boarding pass" and actually find it | v0.1 |
| **Duplicate Photo Auditor** | Finds *near*-duplicates using perceptual hashing, so burst shots cluster together instead of only byte-identical copies | v0.2 |
| **App Permission Drift Tracker** | A timeline of permissions your installed apps gained after an update | v0.3 |

## Building

Requirements: **JDK 17+**, **Android SDK with API 36**, Android Studio Panda 4 or newer.

```bash
git clone <repository-url> && cd Storage_MVP_Bundle
```

Create `local.properties` pointing at your SDK. Note the escaping — in a Java properties file
both `\` and the drive-letter `:` must be escaped:

```properties
sdk.dir=C\:/Users/you/AppData/Local/Android/Sdk
```

Then:

```bash
./gradlew assembleDebug
```

Run every quality gate the CI runs — detekt, Android Lint, unit tests, and the no-network
manifest check:

```bash
./gradlew verifyAll
```

## Project layout

```
app                    Nav host, permission gateway, settings, dashboard
core/ui                Design system, theme, shared composables
core/common            Dispatchers, Outcome type, logging front door
core/data              Room database, DataStore, repositories
core/media             MediaStore facade, trash/delete consent flows
core/hashing           Perceptual hashing + BK-tree  (pure JVM, no Android deps)
core/ocr               On-device text recognition behind our own interface
core/testing           Test fixtures and fakes
feature/screenshots    Screenshot Sweeper       (v0.1)
feature/dupes          Duplicate Photo Auditor  (v0.2)
feature/permissions    Permission Drift Tracker (v0.3)
build-logic            Gradle convention plugins shared by all modules
```

`core/hashing` is deliberately free of Android dependencies: the similarity algorithm is the
riskiest correctness surface in the app, and this keeps it covered by unit tests that run in
milliseconds without an emulator.

## Documentation

| Document | Contents |
|---|---|
| [PLAN.md](PLAN.md) | Product positioning, verified platform constraints, feature designs, security mapping, roadmap, risk register |
| [SECURITY.md](SECURITY.md) | Security properties, how to verify them, how to report a vulnerability |
| [VERSIONING.md](VERSIONING.md) | Versioning scheme, branching, release process, signing |

## Distribution

GitHub Releases and F-Droid. Not Google Play — a Play Console account carries a one-time
US$25 fee, and this project runs on free tooling end to end.

## Licence

Apache License 2.0 — see [LICENSE](LICENSE).
