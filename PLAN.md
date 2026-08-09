# Storage MVP Bundle — Implementation Plan

Native Android app covering three storage-hygiene tools:
**Screenshot Sweeper** (lead) · Duplicate Photo Auditor · App Permission Drift Tracker

> **Scope note:** the source brief listed a fourth tool, *Download Folder Butler*. It has been
> **removed from scope** at the user's direction. §12 records why that turns out to be a
> significant simplification rather than just a subtraction.

---

## 0. Read this first: platform facts that shape the design

I verified these against official Android documentation before planning, because each one decides
whether a requested feature is buildable as written.

| # | Finding | Consequence |
|---|---|---|
| 1 | `MediaStore.createTrashRequest()` / `createDeleteRequest()` / `createWriteRequest()` (API 30+) **do** let us trash, delete, or modify media owned by *other* apps, via a system consent dialog. | The safe deletion path for photos and screenshots is solved, and needs no invasive storage permission. |
| 2 | `PackageInfo.requestedPermissionsFlags` + `REQUESTED_PERMISSION_GRANTED` reports whether a permission "is currently granted to the application" — for **any** queried package, when fetched with `GET_PERMISSIONS`. | Permission Drift Tracker is viable. But it needs `QUERY_ALL_PACKAGES`; `<queries>` cannot substitute, because enumerating *all* apps is the feature. |

Two secondary findings that change the background architecture:

- **`ACTION_PACKAGE_ADDED` and `ACTION_PACKAGE_REPLACED` are *not* exempt** from implicit-broadcast
  restrictions (only `ACTION_PACKAGE_FULLY_REMOVED` is). So we **cannot** reliably wake on app
  update. Drift detection must be a scheduled `WorkManager` scan, not an event listener. Expect
  *daily* detection latency, not real-time — this must be stated honestly in the UI, and it is the
  main reason that feature is deprioritized in §1.
- **`dataSync` foreground services are capped at 6 hours per 24h** on Android 15+ (shared across all
  the app's dataSync services), with `Service.onTimeout()` fired at the limit. Long library scans
  must be checkpointed and resumable, never a single unbounded service.

---

## 1. Product positioning

This section drives the roadmap in §10. Read it before the feature designs, because it changes
which feature is the product and which are supporting.

### 1.1 It is an awareness app, not a cleaner

"Bundles multiple tools" is an architecture, not a proposition. And *storage cleaner* is one of the
most scam-saturated categories on Android — competing for that label means inheriting its
reputation and fighting uphill on trust from day one.

The genuine thread across all three tools is: **things accumulate on your phone without you
noticing** — duplicate shots, dead screenshots, permissions that quietly widened after an update.
That framing is an *awareness* product, not a cleanup utility. Same code, materially better story,
and it explains why permission tracking sits next to photo tools instead of looking like an
unrelated fourth thing bolted on.

All user-facing copy should follow from this: **surface what you didn't know was there**, with
deletion as the optional consequence rather than the purpose.

### 1.2 Screenshot Sweeper leads, and OCR search is the headline

Two reasons, one commercial and one structural.

**Commercial:** it's the only one of the three without a serious incumbent. Google Photos and Files
by Google already ship duplicate detection; the near-duplicate gap named in the brief is real but
narrow, and it means competing for trust on ground someone else already holds. Nobody owns
screenshots.

**Structural — this is the more important one:** every other feature here is *destructive*. Its
payoff is deletion, which asks the user to trust the app with something irreversible before they
have seen any benefit at all. Search-your-screenshots-by-text inverts that. It is *additive*:
value on first launch, zero risk, and cleanup follows naturally once the user can finally see what
is in the pile. It earns the trust the other two features need in order to be used.

**Consequence:** OCR is no longer an optional extra (it was a clarifying question in the previous
revision). It is committed, and it is the headline. See §5.1.

### 1.3 "No network access" is the pitch

The app declares **no `INTERNET` permission at all**. That is unusual in this category and, crucially,
*independently verifiable* — anyone can inspect the manifest and confirm it. In a market defined by
user suspicion, that is worth more than any individual feature.

Make it the lead marketing claim, and back it structurally:

- **Open-source the repository.** The F-Droid distribution route in §1.4 effectively requires it
  anyway, so the marginal cost is zero and the credibility gain is large.
- **Use F-Droid's anti-features metadata** to let the store confirm the no-tracking claim on our
  behalf, rather than asking users to take our word for it.
- Reinforce in-app: a plain-language "what this app can and cannot see" screen in onboarding.

### 1.4 Distribution: the cost restriction decides it

The brief says *no paid services for any part of the project*. A Google Play Console developer
account is a **one-time US$25 registration fee**. That is a cost.

**Free alternative:** distribute via **GitHub Releases** (signed APK) and **F-Droid**, with
**Obtainium** for user-side auto-updates. All free, all open source — and consistent with §1.3.

This also sidesteps the Play policy reviews that `QUERY_ALL_PACKAGES` and `READ_MEDIA_IMAGES` would
each otherwise require. **The plan below assumes non-Play distribution.**

### 1.5 Permission Drift Tracker is deliberately deprioritized

The problem it addresses is real. The platform simply will not let the solution be good:

- Detection is **daily, not real-time** — package-update broadcasts are unavailable to us (§0).
- The app **cannot revoke anything**, only deep-link the user to Settings.
- The result is a notification the user dismisses.

Build it — it's cheap, and it shares no code with the media stack so it parallelizes for free — but
**do not let it drive the roadmap or the marketing.** It ships last (§10) and is feature-flagged so
a future Play build can omit it along with `QUERY_ALL_PACKAGES`.

---

## 2. Questions I need answered

I've made a defensible call on each so work isn't blocked, but these are worth your confirmation:

1. **Distribution** — GitHub Releases / F-Droid (assumed per §1.4), or do you already hold a Play
   Console account? Changes whether we write policy declarations for `QUERY_ALL_PACKAGES` and
   `READ_MEDIA_IMAGES`.
2. **`minSdk` 30 (Android 11)?** Assumed. Trash/delete requests and the package visibility model
   both land at API 30. Supporting 26–29 means maintaining a second legacy storage path for a
   shrinking device slice. I'd skip it for an MVP.

*(The previous OCR question is resolved — it's in, and it's the headline. See §1.2.)*

---

## 3. Stack

Every item is free and open source. No paid tiers, no cloud services, no network calls at runtime.

| Layer | Choice | Note |
|---|---|---|
| Language | Kotlin 2.3 | AGP 8.13.2's R8 (8.13.19) supports it |
| UI | Jetpack Compose + Material 3 | Single activity, type-safe Navigation |
| Build | AGP 8.13.2, Gradle version catalogs, KSP | `compileSdk`/`targetSdk` **36** — AGP 8.13 caps at API 36.1, so Android 17 would need an AGP upgrade |
| DI | Hilt | |
| Persistence | Room (KSP) + DataStore (Preferences) | Room **FTS4** for the OCR index |
| Background | WorkManager | Expedited + periodic; survives process death |
| Images | Coil 3 | Downsampled decode only |
| Async | Coroutines + Flow | |
| **OCR** | **ML Kit Text Recognition v2, bundled model** | **Committed, not optional.** Bundled rather than Play-Services-dependent, so it works on GMS-free devices — which is also the F-Droid requirement |
| Test | JUnit, Robolectric, MockK, Turbine, Compose UI Test, Macrobenchmark | |
| Static analysis | ktlint, detekt, Android Lint, OWASP Dependency-Check, MobSF | All free |
| CI | GitHub Actions | Free for public repos — and §1.3 makes the repo public |

---

## 4. Module structure

```
:app                    Nav host, onboarding, permission gateway, settings, dashboard
:core:ui                Design system, theme, shared composables, a11y helpers
:core:common            Dispatchers, Result types, logging
:core:data              Room DB, DataStore, repository contracts
:core:media             MediaStore facade, trash/delete request flows
:core:hashing           Perceptual hashing + BK-tree index — pure Kotlin/JVM, no Android deps
:core:ocr               ML Kit wrapper + FTS index — isolated so the model is swappable
:core:testing           Fakes, fixtures, deterministic test image corpus
:feature:screenshots    Screenshot Sweeper  (lead feature)
:feature:dupes          Duplicate Photo Auditor
:feature:permissions    App Permission Drift Tracker
```

**Architecture:** MVVM + unidirectional data flow. Compose screen → ViewModel exposing immutable
`UiState` via `StateFlow` → UseCase → Repository → MediaStore/Room/PackageManager. Domain layer
holds no Android framework types.

Two deliberate isolation choices:

- `:core:hashing` has **no Android dependencies** — the similarity algorithm is the riskiest
  correctness surface in the app, and this makes it testable with fast JVM unit tests against a
  fixed corpus, no emulator required.
- `:core:ocr` wraps ML Kit behind our own interface so the recognizer can be replaced (e.g. with
  Tesseract) without touching feature code, and so v0.1 isn't welded to one vendor's model.

**Important:** v0.1 ships only `:feature:screenshots` (§10), but the full module structure is
established in Phase 0 regardless. The later features fold in without restructuring.

---

## 5. Feature designs

Ordered by product priority, per §1.

### 5.1 Screenshot Sweeper — lead feature

**Detection** — no official "is a screenshot" flag exists, so fuse signals and expose a confidence:
- `RELATIVE_PATH` matching `Pictures/Screenshots`, `DCIM/Screenshots`, plus OEM variants
- Dimensions matching device display metrics
- Absent EXIF camera model (real photos have one)
- Filename patterns (`Screenshot_*`, `Screenshot from *`)

**Access:** `READ_MEDIA_IMAGES` (33+) / `READ_EXTERNAL_STORAGE` (30–32).

**OCR search — the headline (§1.2).** On-device ML Kit text extraction, indexed into Room FTS4:

- Runs as a checkpointed `WorkManager` job after the first scan; incremental thereafter.
- Index stores `(mediaId, extractedText, confidence, indexedAt)`. Text is stored, thumbnails are not.
- Search UI is the screen the app opens on — type "receipt", "boarding pass", "wifi password",
  get the screenshot back. This is the reason to keep the app installed.
- Suggested queries seeded from the most frequent extracted terms, so the search box isn't a blank
  prompt on first run.
- **Framing matters:** results are presented as *found*, not as *deletion candidates*. Cleanup is a
  secondary action available from a result, never the default.

**Clustering (secondary):** by age bucket, by folder, and by visual similarity via `:core:hashing` —
catching the common case of five near-identical screenshots of the same screen.

**Cleanup UI:** dense grid, multi-select, "select all older than X", running total of reclaimable
bytes, trash-first deletion via `createTrashRequest()`.

**Privacy obligation:** the OCR index contains arbitrary text lifted off the user's screen —
plausibly including 2FA codes, messages, and banking details. It never leaves the device, is never
logged, and `allowBackup="false"` keeps it out of adb backups. See §6.

### 5.2 Duplicate Photo Auditor

The stated gap is real — near-duplicates from burst shots, not just hash-identical files — but note
§1.2: this competes with established incumbents, so it is v0.2, not the pitch.

**Access:** `READ_MEDIA_IMAGES` (33+) / `READ_EXTERNAL_STORAGE` (30–32). The Photo Picker is *not*
usable here — auditing requires whole-library visibility by definition.

**Pipeline:**
1. Query `MediaStore.Images` for `_ID`, `SIZE`, `DATE_TAKEN`, `WIDTH`, `HEIGHT`, `RELATIVE_PATH`.
2. **Exact pass** — group by size, then SHA-256 only within same-size groups. Cheap, catches true copies.
3. **Perceptual pass** — decode to 32×32 grayscale via `ImageDecoder` with a target sample size
   (never allocate a full-resolution bitmap); compute a 64-bit **dHash** plus a secondary
   colour-moment signature.
4. **Index** hashes in a **BK-tree** over Hamming distance — turns clustering from O(n²) pairwise
   comparison into a tractable radius query. This is what makes a 20,000-photo library feasible.
5. **Cluster** with union-find at a user-facing *strictness* slider (default Hamming ≤ 10 of 64).
6. **Burst detection** — same dimensions + capture timestamps within N seconds is a strong
   independent prior; fuse with visual distance so burst sequences cluster confidently.
7. **Keeper heuristic** — highest resolution → largest file → oldest. Always user-overridable.

**Incremental rescan:** persist `(mediaId, dateModified, size) → hash` in Room; process only deltas.

**Performance:** WorkManager, parallel decode bounded to `cores - 1`, checkpointed every N images so
a killed process resumes rather than restarts. Respects the Android 15 dataSync cap by construction.

**Safety (non-negotiable):** nothing is ever auto-deleted. Side-by-side comparison with pinch-zoom
before any destructive action. Default is `createTrashRequest()` (recoverable ~30 days); permanent
`createDeleteRequest()` sits behind an explicit toggle.

### 5.3 App Permission Drift Tracker

Deprioritized per §1.5 — built, but not marketed and not on the critical path.

**Mechanism:** `getInstalledPackages(GET_PERMISSIONS)` → for each package, zip `requestedPermissions`
against `requestedPermissionsFlags` and test `REQUESTED_PERMISSION_GRANTED`. Requires
`QUERY_ALL_PACKAGES`, behind a feature flag.

**Baseline & diff:** snapshot `(package, versionCode, permission, granted, protectionLevel)` into
Room on first scan. Subsequent scans emit typed drift events:

- `NEW_PERMISSION_REQUESTED` — app now asks for something it didn't before
- `PERMISSION_NEWLY_GRANTED`
- `DANGEROUS_ADDED_AFTER_UPDATE` — the headline case from the brief
- `SPECIAL_ACCESS_ADDED` — All Files, accessibility, notification listener, overlay

Severity-ranked by permission group: location, microphone, camera, SMS, contacts at the top.

**Scheduling:** daily `WorkManager` periodic scan (user-configurable) plus an opportunistic scan on
app open. **The UI must say "checked daily," not "real time."** Overpromising here would be a
correctness bug, not just marketing spin.

**Notifications:** `POST_NOTIFICATIONS` (33+), grouped by severity, deep-linking to
`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`. We navigate the user to revoke — no app can revoke
another app's permission programmatically, and the UI must not imply otherwise.

**Fit with §1.1:** presented as a *timeline of what changed*, not an alert feed. That framing suits
what the platform actually permits, and matches the awareness positioning.

---

## 6. Security

Mapped to the OWASP Secure Coding Practices checklist (14 sections). The sections that bite for a
fully offline, no-backend Android app:

| OWASP section | Applied here |
|---|---|
| **Input Validation** | Validate every value read from MediaStore, filesystem, and PackageManager as untrusted. Filenames, package labels, and **OCR-extracted text** are attacker-influenced — treat as hostile strings, never as format specifiers or query fragments. |
| **Access Control** | Least privilege — each permission requested *only* when its feature is first used, never bundled at launch. Fail securely: no permission ⇒ feature disabled, never a silent partial action. |
| **Error Handling & Logging** | No file paths, package names, or **any OCR text** in logs at any build type; logging stripped via R8 in release. Errors surface as actionable UI text, never raw stack traces. |
| **Data Protection** | Room DB app-private. No analytics, no telemetry, no network permission at all — `INTERNET` deliberately **not** declared, making exfiltration structurally impossible and trivially auditable. This is also the §1.3 marketing claim, so it is a hard architectural invariant: **any PR adding a network dependency fails CI.** |
| **Database Security** | Room parameterised queries throughout; no string-concatenated SQL. Critical for the FTS index — user search terms and stored OCR text are both untrusted. FTS `MATCH` queries must be parameterised and the query syntax escaped. |
| **File Management** | MIME sniffed from headers, not trusted from extension. No symlink following. No execution of anything on disk. All media access mediated by the MediaStore facade, so there is no direct-path write surface to get wrong. |
| **Cryptographic Practices** | Only hashing (SHA-256, dHash) — no home-rolled crypto. Release keystore never in the repo; injected from CI secrets. |
| **General Coding Practices** | No dynamic code loading, no reflection into hidden APIs, no `WebView`. Dependencies pinned in the version catalog and scanned by OWASP Dependency-Check. |

**The OCR index is the app's most sensitive asset** — it may contain 2FA codes, message content, and
banking details lifted from the user's own screen. Treat it accordingly:

- `allowBackup="false"` — keeps it out of adb backups
- `FLAG_SECURE` on every screen rendering screenshot thumbnails or OCR text
- A user-facing "clear index" action in settings, and index deletion on permission revocation
- Never included in any diagnostic export or bug report

Plus the standard hardening: `android:exported="false"` on every component that doesn't need
otherwise, no exported `ContentProvider`, R8 full-mode obfuscation, MobSF scan gating each release.

**Open-source consequence (§1.3):** the code is public, so security review is public too. Enable
GitHub Dependabot and CodeQL (both free for public repos), and add a `SECURITY.md` with a
disclosure contact before the repo goes public.

---

## 7. Coding standards

The linked GeeksforGeeks standards give us: limited globals, standard file headers, consistent
naming, consistent indentation, documented error returns, short single-purpose functions,
single-purpose identifiers, thorough commenting, no GOTO.

**One conflict, and how I propose to resolve it.** That article's *specific* conventions are
C-family: globals capitalised, constants ALL-CAPS, braces on their own line. Kotlin's official style
guide says otherwise — `PascalCase` types, `camelCase` properties, K&R braces on the same line —
and Android Studio, ktlint, and detekt all enforce the Kotlin form. Applying the C conventions
literally would mean fighting the toolchain on every save for no safety benefit.

**Recommendation:** adopt the article's *principles* — sound and language-independent — expressed
through the official Kotlin style guide, and machine-enforced rather than review-enforced:

- **Limited globals** → no mutable top-level state; state scoped in ViewModels or repositories,
  injected via Hilt. `detekt` flags violations.
- **Standard headers** → KDoc on every public class and function: purpose, params, returns, throws.
  Enforced via detekt's `UndocumentedPublicClass`/`UndocumentedPublicFunction`.
- **Naming** → Kotlin official style, `ktlint` enforced, zero exceptions.
- **Indentation** → 4 spaces, 120-col limit, `.editorconfig` + ktlint. Never hand-formatted.
- **Error returns** → *not* magic 0/1 ints. Kotlin's `Result<T>` / sealed `Outcome` hierarchies are
  strictly better: they carry the failure reason and the compiler enforces handling. This honours
  the intent — unambiguous, debuggable error signalling — far better than the letter.
- **Short functions** → detekt `LongMethod` at 40 lines, `CyclomaticComplexMethod` at 12, hard-fail CI.
- **No GOTO** → n/a in Kotlin; the analogue is banning non-local returns from nested lambdas and
  unlabelled `break` in complex loops.

CI hard-fails on ktlint, detekt, Android Lint, and any new compiler warning. Standards that aren't
automated are standards that don't survive contact with a deadline. This matters more than usual
here: the repo is public (§1.3), so code quality is part of the trust argument.

---

## 8. Testing & QA

| Level | Scope | Tooling |
|---|---|---|
| **Unit (JVM)** | Hash correctness, BK-tree radius queries, clustering, keeper heuristic, drift diffing, FTS query escaping | JUnit, MockK, Turbine |
| **Algorithm accuracy** | Fixed corpus: burst sequences, resized/recompressed/rotated pairs, visually-similar-but-distinct pairs. Assert precision/recall thresholds and **fail CI on regression** | Custom harness in `:core:testing` |
| **OCR accuracy** | Fixed screenshot corpus — chat, receipt, browser, dark mode, small text, non-Latin script. Assert recall on known search terms | `:core:testing` |
| **Instrumented** | MediaStore queries, trash/delete consent flows, WorkManager scheduling, FTS search | AndroidX Test, WorkManager `TestDriver` |
| **UI** | Compose screens, state restoration, permission-denied paths | Compose UI Test |
| **Performance** | Scan + OCR throughput and memory ceiling at 1k / 10k / 50k images; search latency at 50k indexed; startup and frame timing | Macrobenchmark |
| **Accessibility** | TalkBack traversal, 48dp touch targets, contrast, dynamic type to 200% | Accessibility Scanner + manual |
| **Security** | Dependency CVEs, APK static analysis, **assert no `INTERNET` permission in the merged manifest** | OWASP Dependency-Check, MobSF, custom Gradle check |

**Manual QA matrix:** Android 11 / 13 / 14 / 15 / 16 × {each permission granted, denied, revoked
mid-session} × {Pixel stock, Samsung One UI — its Screenshots path and storage behaviour differ
meaningfully from AOSP}.

**Highest-risk QA areas, ranked:** (1) false-positive duplicate detection causing real photo loss,
(2) OCR index leaking into logs, backups, or diagnostics, (3) OEM screenshot path variance,
(4) scan/OCR behaviour under process death, (5) drift baseline correctness across app updates.

**Destructive-operation rule:** every delete path must have an automated test proving trash recovery
works *before* that path ships enabled.

---

## 9. DevOps (free tier only)

GitHub Actions, on a public repo per §1.3:

- **PR:** ktlint → detekt → Android Lint → unit tests → **no-`INTERNET` manifest assertion** →
  assemble debug. All required to merge.
- **Nightly:** instrumented tests on an emulator matrix (API 30/33/36) via
  `reactivecircus/android-emulator-runner`, plus OWASP Dependency-Check.
- **Tag `v*`:** assemble release, sign from repo secrets, MobSF scan, publish to GitHub Releases
  with generated notes.
- **Always on (free for public repos):** Dependabot, CodeQL.

Also required for F-Droid: **reproducible builds** where achievable, and an
`fastlane/metadata/android` directory for store listing text, so the F-Droid submission is a
metadata PR rather than a scramble.

Gradle remote build cache off (paid); local build cache and configuration cache on. Release keystore
lives only in GitHub Secrets and a local password manager — never in git. `.gitignore` covers
`*.jks`, `local.properties`, `keystore.properties` from commit one.

---

## 10. Roadmap — ship v0.1 standalone

The central scheduling recommendation: **do not build all three, then release.** Ship Screenshot
Sweeper alone, validate it, then fold the rest in.

| Release | Contents | Rationale |
|---|---|---|
| **Phase 0 — Foundation** *(not shipped)* | Repo public, version catalog, **full** module skeleton, Hilt/Room/DataStore/WorkManager wiring, design system, permission gateway, CI green, `SECURITY.md` | Full structure now so v0.2/v0.3 fold in without restructuring |
| **v0.1 — Screenshot Sweeper + OCR search** | Detection, OCR index + search UI, visual clustering, bulk review, trash-first delete, onboarding incl. "what this app can see" | A few weeks past Phase 0. Validates the destructive-action UX on the lowest-stakes data, and tests the actual product hypothesis (§1.2) before the expensive work starts. **If nobody wants this, that is learned before building the BK-tree and the drift engine.** |
| **v0.2 — Duplicate Photo Auditor** | dHash + BK-tree, burst detection, incremental rescan, side-by-side comparison UI | Scales the hashing v0.1 already uses for screenshot clustering |
| **v0.3 — Permission Drift Tracker** | Baseline, diff engine, daily worker, change timeline | Shares no code with the media stack — **can be built in parallel at any point**, but ships last per §1.5 |
| **v1.0 — Hardening** | Perf tuning, a11y, OEM matrix, MobSF, security review, F-Droid submission | |

**Cut list under schedule pressure**, in order: the "granted but never used" `PACKAGE_USAGE_STATS`
extension, then v0.3 entirely, then duplicate-detection strictness tuning. Never cut trash-first
deletion, the side-by-side comparison, or the OCR privacy controls — those are load-bearing for trust.

Because v0.3 shares no code with v0.1–v0.2, a two-track team finishes in roughly the time of the
media track alone. But per §1.5, resist the temptation to start it early just because it's
parallelizable — v0.1 validation is what the schedule should be optimising for.

---

## 11. Risk register

| Risk | Severity | Mitigation |
|---|---|---|
| False-positive duplicate → user loses a real photo | **Critical** | Trash-first (recoverable), mandatory side-by-side review, conservative default threshold, precision/recall regression gate in CI |
| OCR index exposure (2FA codes, messages, banking) | **Critical** | Never logged, `allowBackup="false"`, `FLAG_SECURE`, user-clearable, excluded from diagnostics; QA rank #2 |
| Category is scam-saturated — users distrust on sight | High | §1.1 awareness framing, §1.3 verifiable no-network claim, open source, F-Droid anti-features metadata |
| **v0.1 hypothesis is wrong — nobody wants screenshot search** | High | This is precisely what the v0.1 standalone release exists to discover, cheaply, before v0.2–v0.3 are built |
| `READ_MEDIA_IMAGES` denied | Medium | Both media features degrade to an explanatory empty state; no partial-access illusion |
| OEM screenshot paths differ | Medium | Multi-signal detection, not path-only; test on Samsung + Pixel |
| Scan/OCR OOM or ANR on large libraries | Medium | Never decode full bitmaps; bounded parallelism; checkpointed resumable work |
| OCR accuracy poor on dark mode / small text / non-Latin | Medium | Dedicated accuracy corpus in CI; `:core:ocr` isolation allows swapping the recognizer |
| Bundled ML Kit inflates APK — **measured at 44 MB universal, not the ~4 MB first estimated** | Medium | The text model is only 1.2 MB; the cost is ML Kit's native engine duplicated per ABI. Mitigated in Phase 0 with ABI splits: 16 MB (arm64-v8a), 11 MB (armeabi-v7a). Requires per-ABI versionCodes before v0.1 — see VERSIONING.md |
| `QUERY_ALL_PACKAGES` blocks a future Play listing | Low (non-Play) | v0.3 feature-flagged so a Play build ships without it |
| AGP 8.13.2 caps at API 36.1 | Low | Fine for targetSdk 36; Android 17 targeting requires an AGP bump |

---

## 12. What dropping Download Folder Butler bought us

Worth recording, because the simplification is larger than "one fewer feature":

- **`MANAGE_EXTERNAL_STORAGE` is gone entirely.** It was required solely by that feature — Android
  11+ blocks `ACTION_OPEN_DOCUMENT_TREE` from granting the `Download` directory, and
  `MediaStore.Downloads` only exposes files the app itself created, so All Files Access was the only
  route. The app now requests no broad-filesystem permission at all — which is a materially better
  privacy story, removes the most likely reason a user would abandon onboarding, and directly
  strengthens the §1.3 trust pitch.
- **No direct file-path writes anywhere.** All media access is mediated by MediaStore and its
  consent dialogs, deleting the path-traversal and symlink-following attack surface rather than
  defending it.
- **The undo journal and rules engine are no longer needed** — both substantial, correctness-critical
  subsystems whose failure modes involved moving users' files to the wrong place.
- **The roadmap shortens by a full phase**, and it was the phase with the highest trust cost.

If the feature is ever revived, the binding constraint to re-check first is whether the platform
still blocks SAF access to the `Download` tree; nothing else about the design was the blocker.

---

## Sources

- [Access media files from shared storage](https://developer.android.com/training/data-storage/shared/media)
- [Package visibility filtering on Android](https://developer.android.com/training/package-visibility)
- [PackageInfo (API reference)](https://developer.android.com/reference/android/content/pm/PackageInfo)
- [Implicit broadcast exceptions](https://developer.android.com/guide/components/broadcast-exceptions)
- [Behavior changes: Android 15](https://developer.android.com/about/versions/15/behavior-changes-15)
- [Storage updates in Android 11](https://developer.android.com/about/versions/11/privacy/storage) — basis for the §12 note
- [Android Gradle plugin 8.13.0 release notes](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [Target API level requirements for Google Play](https://developer.android.com/google/play/requirements/target-sdk)
- [OWASP Secure Coding Practices — Checklist](https://owasp.org/www-project-secure-coding-practices-quick-reference-guide/stable-en/02-checklist/05-checklist)
- [Coding Standards and Guidelines — GeeksforGeeks](https://www.geeksforgeeks.org/software-engineering/coding-standards-and-guidelines/)
