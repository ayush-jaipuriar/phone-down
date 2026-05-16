# Phase 16 - Android Production Readiness Plan

## Status

- Planning status: Drafted and expanded through Sprint 16.4 planning
- Implementation status:
  - Sprint 16.1 setup/docs: completed
  - Sprint 16.2 real Google Sign-In: implemented
  - Sprint 16.3 real Google Drive backup/restore: implemented in code, manual QA still pending
- Approval required before implementation: Per-sprint approval required
- Scope owner: Phone Down Android V1 production release
- Target outcome: Replace fake/deferred production dependencies and prepare the app for Google Play production release

## 1. Purpose

Phase 16 turns Phone Down from a feature-complete Android app with fake external integrations into a production-ready Play Store release candidate.

This phase is intentionally larger than earlier feature phases because it crosses product, engineering, release operations, privacy, billing, account setup, and Play policy readiness.

The goal is not only to make the app compile with real services. The goal is to make the app trustworthy enough for a real user to:

- install it from Google Play
- buy Pro through Google Play Billing
- sign in with Google
- back up and restore through Google Drive
- recover from crashes with production diagnostics
- understand privacy and permissions clearly
- pass Google Play review and testing requirements

## 2. Confirmed Product Decisions

- [x] Include all production-readiness work in one phase.
- [x] Replace fake/deferred Google Sign-In, Google Drive backup, Play Billing, release security, and Play Store readiness.
- [x] User does not currently have a Play Console developer account and needs beginner-friendly handholding from scratch.
- [x] User has an existing Google Cloud project named `only-yours`, but project reuse is undecided.
- [x] Product should support monthly, yearly, and lifetime Pro.
- [x] Pricing should be recommended after competitor research, with India as the primary pricing market.
- [x] Backup location follows engineering recommendation.
- [x] Production release is the final goal, with intermediate testing tracks used only because Play requires or strongly recommends them.
- [x] Full Room database encryption is not required for V1 production unless later risk review changes this.
- [x] Once-daily auto-backup should be implemented if it is not materially costly.
- [x] Crash reporting follows engineering recommendation.
- [x] Keep the 24-hour entitlement cache for now.
- [x] Include full final Play Store metadata/assets review.

## 3. Research Summary

### 3.1 Google Play Account and Testing

Google Play requires a Play Console developer account before publishing. The official Play Console setup documentation says there is a one-time US$25 registration fee.

For newly-created personal developer accounts, Google says production access requires a closed test with at least 12 opted-in testers for at least 14 continuous days before applying for production access.

Practical implication:

- We can still aim for production release.
- But the release path should be: developer account -> app setup -> internal testing -> closed testing -> production access request -> production rollout.
- If the user creates a personal account, we should plan for the 12 tester / 14 day requirement.

Sources:

- Google Play Console setup: https://support.google.com/googleplay/android-developer/answer/6112435
- Google Play personal account testing requirements: https://support.google.com/googleplay/android-developer/answer/14151465

### 3.2 Google Cloud Project Recommendation

Recommendation: create a new Google Cloud/Firebase project for Phone Down instead of reusing `only-yours`.

Reasoning:

- OAuth consent screen, package name, SHA fingerprints, Drive API scopes, Firebase config, and quota visibility should be isolated by product.
- Reusing another app's project can create confusing consent branding, support, quota, analytics, and security boundaries.
- A dedicated project makes future ownership transfer, Play Console linking, Firebase Crashlytics, and OAuth verification cleaner.

Acceptable exception:

- Reuse `only-yours` only if it is already a general company-level project and the OAuth consent screen can correctly represent `phonedown.app`.

Recommended project name:

- Google Cloud project: `phone-down`
- Firebase project: `phone-down`
- OAuth app name: `Phone Down`
- Android package name: `phonedown.app`

### 3.3 Google Drive Backup Location

Recommendation: use Google Drive `appDataFolder`.

The Drive application data folder is hidden from the user and intended for app-specific data that the user should not directly interact with. It requires the `drive.appdata` scope.

Why this fits Phone Down:

- backup files are implementation data, not user-facing documents
- users should not accidentally delete or edit backup JSON
- the scope is narrower than broad Drive access
- the UX remains calm: "Back up to Google Drive" without exposing file management complexity

Tradeoff:

- users cannot browse backup files directly in Drive UI
- explicit export can be added later as a separate feature

Source:

- Google Drive app data folder docs: https://developers.google.com/workspace/drive/api/guides/appdata

### 3.4 Billing Model

Google Play Billing supports:

- recurring subscriptions for monthly/yearly Pro
- one-time products for lifetime Pro

Implementation implication:

- monthly and yearly should be one subscription product with base plans, or two subscription products if the first implementation is simpler
- lifetime should be a non-consumable one-time product
- entitlement resolver should merge active subscription entitlement and lifetime entitlement into one `isPro` result

Sources:

- Play Billing subscriptions: https://developer.android.com/google/play/billing/subscriptions
- Play Billing one-time products: https://developer.android.com/google/play/billing/one-time-products

### 3.5 Pricing Research and Recommendation

Observed comparable pricing:

- Focus Now: EUR 0.99/month, EUR 9.99/year, EUR 19.99 lifetime.
- Focus To-Do reference: USD 1.99/month, USD 3.99/three months, USD 11.99 lifetime.
- Focusmo: USD 4.92/month, USD 39/year, USD 99 lifetime, but it is a broader blocker/productivity app.
- India-relevant research snapshot lists Forest at about INR 399 one-time and Freedom at USD 3.3-8.99/month, with premium productivity often feeling expensive in India.

Recommended India launch pricing:

| Plan | Recommended Launch Price | Rationale |
|---|---:|---|
| Monthly Pro | INR 99/month | Low-friction, similar to low-end focus timer pricing, accessible for students and young professionals |
| Yearly Pro | INR 799/year | About 33% discount vs monthly, strong enough to nudge yearly without feeling manipulative |
| Lifetime Pro | INR 1,999 one-time | Roughly 2.5x yearly, attractive to users who dislike subscriptions while still preserving subscription economics |

Recommended US/default international pricing:

| Plan | Recommended Price |
|---|---:|
| Monthly Pro | USD 1.99/month |
| Yearly Pro | USD 14.99/year |
| Lifetime Pro | USD 39.99 one-time |

Optional launch experiment:

- first 30 days: INR 699/year and INR 1,499 lifetime
- after launch validation: move to INR 799/year and INR 1,999 lifetime

Recommendation for V1:

- Use the standard launch prices immediately: INR 99/month, INR 799/year, INR 1,999 lifetime.
- Avoid temporary discounts until the product has enough users to interpret conversion data.

Sources:

- Focus Now pricing: https://focus-now.app/en/pricing/
- Focusmo pricing: https://focusmo.app/pricing
- Focus To-Do pricing reference: https://www.howtogeek.com/productivity-apps-that-are-worth-the-premium-subscription/
- India productivity pricing snapshot: https://assets.nextleap.app/submissions/NLStride-047b23a9-189b-410d-9622-79b5bed73346.pdf

### 3.6 Crash Reporting Recommendation

Recommendation: add Firebase Crashlytics with privacy-conscious controls.

V1 posture:

- enabled for release builds
- disabled for debug builds
- no user email, Google ID, session ID, or raw backup payload in crash keys/logs
- document crash reporting in privacy policy and Play Data Safety
- provide a Settings privacy toggle if practical in this phase

Why:

- production app quality is hard to maintain without crash and ANR visibility
- Crashlytics is lightweight and standard for Android
- the app already has a privacy-first posture, so collection must be minimal and disclosed

Source:

- Firebase Crashlytics docs: https://firebase.google.com/docs/crashlytics

## 4. Out of Scope

- iOS release readiness.
- Web app or desktop app.
- AI coaching, tasks, tags, calendars, widgets, or social features.
- Full SQLCipher database encryption for Room.
- User-visible Drive file export.
- Backend server for Play purchase verification.
- Server-side Real-time Developer Notifications.
- RevenueCat or third-party subscription abstraction unless Play Billing complexity becomes a blocker.
- Major UI redesign beyond production compliance and polish fixes.

## 5. Phase Architecture Strategy

The production version should preserve existing module boundaries.

### 5.1 App Layer

`:app` owns:

- route/ViewModel wiring
- permission flows
- Play Billing purchase launch coordination if Activity context is required
- Google Sign-In launcher coordination
- production DI binding selection
- Crashlytics initialization policy
- release build configuration

### 5.2 Core Integration Modules

Production implementations should live behind existing repository contracts:

- `:core:auth` gets real Google Sign-In/Auth implementation
- `:core:billing` gets real Play Billing implementation
- `:core:backup` gets real Drive API backup client/repository implementation
- `:core:datastore` keeps entitlement cache and backup settings
- `:core:model` keeps contracts and shared entitlement/account/backup models

### 5.3 Feature Modules

Feature modules remain UI-focused:

- `:feature:pro` renders product cards and purchase states
- `:feature:account` renders sign-in, backup, restore, and account state
- `:feature:settings` exposes account, Pro, crash reporting, privacy, and permissions controls

Feature modules should not directly know about BillingClient, GoogleSignInClient, Drive clients, Firebase, or Play Console.

## 6. Workstream 1 - Play Console and Developer Account Setup

### Goal

Create the release infrastructure needed to publish Phone Down.

### User-Owned Console Steps

- [ ] Create Google Play Console developer account.
- [ ] Pay the one-time US$25 registration fee.
- [ ] Choose personal vs organization account.
- [ ] Complete identity verification.
- [ ] Verify Android device access if Play Console requests it.
- [ ] Create a new app in Play Console:
  - [ ] App name: `Phone Down`
  - [ ] Default language: English
  - [ ] App or game: App
  - [ ] Free or paid: Free with in-app purchases
  - [ ] Declarations accepted
- [ ] Link app package name: `phonedown.app`
- [ ] Enable Play App Signing.
- [ ] Create internal testing track.
- [ ] Create closed testing track.
- [ ] Prepare 12 testers for 14 continuous days if account is personal and new.

### Repo-Owned Steps

- [ ] Document Play Console setup in `docs/play-console-release-guide.md`.
- [ ] Add release checklist for screenshots, Data Safety, content rating, target audience, ads declaration, and testing.
- [ ] Add tester recruitment checklist and feedback template to `docs/phase-16-manual-qa.md`.
- [ ] Confirm package name, app label, version code, version name, and release signing expectations.

### Acceptance Criteria

- [ ] Play Console account exists and is verified.
- [ ] Phone Down app exists in Play Console.
- [ ] Internal and closed testing tracks are available.
- [ ] Required tester plan is documented.
- [ ] Release guide is beginner-friendly enough to follow step-by-step.

## 7. Workstream 2 - Google Cloud, Firebase, and OAuth Setup

### Goal

Create a dedicated production Google/Firebase project for Phone Down and wire the app to it without committing secrets.

### Recommended Console Setup

- [ ] Create Google Cloud project: `phone-down`.
- [ ] Create Firebase project linked to the same Google Cloud project.
- [ ] Register Android app with package `phonedown.app`.
- [ ] Add debug SHA-1/SHA-256 for local testing.
- [ ] Add upload/release SHA-1/SHA-256 after release key is created.
- [ ] Configure OAuth consent screen:
  - [ ] App name: `Phone Down`
  - [ ] Support email
  - [ ] Developer contact email
  - [ ] Privacy policy URL
  - [ ] App domain if available
- [ ] Enable Google Drive API.
- [ ] Configure Drive scope: `https://www.googleapis.com/auth/drive.appdata`.
- [ ] Download `google-services.json` for Firebase.

### Repo Safety Requirements

- [ ] Confirm `google-services.json` contains no private keys.
- [ ] Decide whether `google-services.json` is committed or supplied locally.
- [ ] If committed, verify it contains only standard Firebase app config and no service-account credentials.
- [ ] Ensure service-account JSON, private keys, `.p12`, `.pem`, and credential exports are ignored.
- [ ] Update `.gitignore` if needed.

### Implementation Steps

- [ ] Add Google Services Gradle plugin if missing.
- [ ] Add Firebase BoM.
- [ ] Add Firebase Crashlytics plugin/dependency.
- [ ] Add Google Identity / Credential Manager dependency.
- [ ] Add Drive REST client dependencies or a small HTTP client wrapper.
- [ ] Add build config flags for real vs fake integrations if needed.

### Acceptance Criteria

- [ ] App can compile with Firebase/Google config in place.
- [ ] Debug sign-in can be tested locally.
- [ ] Release SHA is planned and documented.
- [ ] No private keys or service-account credentials are committed.

## 8. Workstream 3 - Real Google Sign-In

### Goal

Replace `FakeAuthRepository` with a real Google account implementation.

### Architecture

- Keep `AuthRepository` contract in `:core:model`.
- Add real implementation in `:core:auth`.
- Use app-layer launcher/context handling where Android Activity APIs require it.
- Persist only minimal account display state needed by UI.
- Do not store raw access tokens unless absolutely required.

### Implementation Checklist

- [ ] Review current `AuthRepository` contract.
- [ ] Extend account model if needed:
  - [ ] account ID
  - [ ] email
  - [ ] display name
  - [ ] avatar URL
  - [ ] signed-in state
  - [ ] auth error state
- [ ] Add real Google sign-in client.
- [ ] Request only scopes needed:
  - [ ] basic profile/email
  - [ ] Drive app data scope when enabling backup
- [ ] Route sign-in launch through `AccountRoute` / app layer safely.
- [ ] Update `AccountViewModel` to use real repository results.
- [ ] Preserve fake implementation for unit tests or debug fallback.
- [ ] Add sign-out behavior.
- [ ] Ensure backup features require signed-in account.

### Tests

- [ ] Unit tests for auth state mapping.
- [ ] ViewModel tests for sign-in success/failure/sign-out.
- [ ] Manual test with debug OAuth SHA.
- [ ] Manual test after release/internal track upload with Play signing SHA.

### Acceptance Criteria

- [ ] User can sign in with Google on a physical device.
- [ ] Account screen shows real signed-in account state.
- [ ] Sign-out clears account state and disables backup actions.
- [ ] No token or sensitive auth values are logged.

## 9. Workstream 4 - Real Google Drive Backup and Restore

### Goal

Replace fake backup transport with real Google Drive `appDataFolder` backup/restore.

### Recommended Backup Model

Use one canonical backup file:

- filename: `phone_down_backup_v1.json`
- location: Drive `appDataFolder`
- format: existing versioned backup JSON schema
- write mode: upload/update latest backup
- restore mode: download latest backup and full-replace local data

Optional metadata:

- backup version
- created timestamp
- app version
- schema version
- device model hash or label if needed later

### Implementation Checklist

- [ ] Create `DriveBackupClient` in `:core:backup`.
- [ ] Authenticate requests with the signed-in Google account.
- [ ] Request only `drive.appdata`.
- [ ] Implement upload:
  - [ ] find existing backup file in `appDataFolder`
  - [ ] create if missing
  - [ ] update content if present
  - [ ] record backup timestamp in DataStore
- [ ] Implement download:
  - [ ] list files in `appDataFolder`
  - [ ] select canonical backup file
  - [ ] download content
  - [ ] deserialize using existing `BackupSerializer`
- [ ] Implement delete cloud backup for data deletion flow.
- [ ] Preserve current full-replace local restore semantics.
- [ ] Surface clear errors:
  - [ ] not signed in
  - [ ] network unavailable
  - [ ] no backup found
  - [ ] Drive permission denied
  - [ ] backup version unsupported
  - [ ] restore blocked during active session
- [ ] Update Account UI states for real in-progress/success/failure.

### Auto-Backup Recommendation

Implement once-daily auto-backup using WorkManager.

Reasoning:

- data payload is small
- cost is minimal for a personal focus app
- WorkManager handles device constraints and retries
- it fulfills the original V1 promise

Recommended constraints:

- network connected
- user signed in
- user is Pro
- backup enabled
- no active focus session
- at least 24 hours since last successful auto-backup

### Tests

- [ ] Unit tests for backup file selection.
- [ ] Unit tests for Drive client error mapping using fake HTTP/Drive layer.
- [ ] Serializer round-trip tests stay passing.
- [ ] Restore use case tests for full replacement.
- [ ] WorkManager scheduling tests where practical.
- [ ] Manual test with real Google account.

### Acceptance Criteria

- [ ] Manual backup writes to Drive app data folder.
- [ ] Restore downloads real remote backup and replaces local data.
- [ ] Delete data can delete cloud backup when requested.
- [ ] Auto-backup runs once daily under constraints.
- [ ] Backup/restore UI never claims success before persistence succeeds.

### Progress Update - 2026-05-16

- [x] Added real Drive-backed repository wiring and removed fake backup runtime DI for normal app usage.
- [x] Added a dedicated Google Drive authorization layer that separates account identity from Drive scope authorization.
- [x] Added once-daily WorkManager auto-backup scheduling and worker runtime eligibility checks.
- [x] Updated Settings and Account flows to pre-authorize Drive access before manual backup/restore.
- [x] Verified `:app:assembleDebug` and targeted unit tests for `:core:backup`, `:app`, `:feature:settings`, and `:feature:account`.
- [x] Manual physical-device QA uncovered and fixed three real Sprint 16.3 blockers:
  - missing `android.permission.INTERNET` in `AndroidManifest.xml`
  - lost pending account email across the Drive authorization resolution flow in `GoogleDriveAuthorizationManager`
  - placeholder certificate pins in `network_security_config.xml` that incorrectly blocked Google Drive TLS handshakes
- [x] Manual backup now succeeds on device, updates the last-backup timestamp, and reveals the real Auto Backup toggle after first success.
- [x] Manual restore now succeeds on device and re-applies backed-up settings in a true full-replace flow.
- [x] Explicit no-backup-found device QA now passes after deleting the current hidden backup and re-signing into the same Google account.
- [x] Delete-all-data with cloud backup is now trust-preserving:
  - it pre-authorizes Drive access
  - deletes cloud backup before wiping local state
  - and surfaces a real failure instead of silently pretending cloud deletion succeeded

## 10. Workstream 5 - Real Play Billing and Entitlements

### Goal

Replace fake billing with real Google Play Billing for subscriptions and lifetime Pro.

### Play Console Product Setup

Recommended product IDs:

- Subscription: `pro`
- Monthly base plan: `pro-monthly`
- Yearly base plan: `pro-yearly`
- Lifetime one-time product: `pro_lifetime`

If Play Console requires separate subscription product IDs for implementation simplicity:

- `pro_monthly`
- `pro_yearly`
- `pro_lifetime`

Recommended India launch prices:

- monthly: INR 99
- yearly: INR 799
- lifetime: INR 1,999

Recommended default international prices:

- monthly: USD 1.99
- yearly: USD 14.99
- lifetime: USD 39.99

### App Implementation Checklist

- [ ] Add Play Billing Library dependency.
- [ ] Create `RealBillingRepository` in `:core:billing`.
- [ ] Connect `BillingClient` lifecycle safely.
- [ ] Query product details for subscription and lifetime products.
- [ ] Map product details to existing Pro UI models.
- [ ] Launch purchase flow from app/activity layer.
- [ ] Handle purchase updates.
- [ ] Acknowledge purchases.
- [ ] Query existing purchases on app launch/resume.
- [ ] Merge entitlement sources:
  - [ ] active monthly/yearly subscription
  - [ ] purchased lifetime product
  - [ ] 24-hour cached entitlement
- [ ] Cache entitlement in DataStore.
- [ ] Add "Manage subscription" link to Play subscription center.
- [ ] Add "Restore purchases" behavior using `queryPurchasesAsync`.
- [ ] Handle pending purchases.
- [ ] Handle canceled/expired subscriptions.
- [ ] Keep fake billing for tests.

### Entitlement Rules

- Lifetime Pro always wins if purchased and acknowledged.
- Active subscription grants Pro.
- If billing query temporarily fails, use cached entitlement for up to 24 hours.
- If cache expires and billing cannot verify entitlement, Pro-only remote features should be disabled gracefully.
- Local data should never be deleted because entitlement expires.

### Tests

- [ ] Unit tests for entitlement resolver.
- [ ] Unit tests for product mapping.
- [ ] ViewModel tests for purchase states.
- [ ] Manual license tester purchase flow.
- [ ] Manual pending purchase flow if available.
- [ ] Manual subscription cancellation/restore flow in Play test environment.

### Acceptance Criteria

- [ ] Real products load on device from Play test track.
- [ ] Monthly purchase grants Pro.
- [ ] Yearly purchase grants Pro.
- [ ] Lifetime purchase grants Pro.
- [ ] Restore purchases works after reinstall.
- [ ] Pro gates respond to real entitlement changes.

## 11. Workstream 6 - Crash Reporting and Production Diagnostics

### Goal

Add production-grade crash visibility without undermining privacy.

### Implementation Checklist

- [ ] Add Firebase Crashlytics.
- [ ] Disable Crashlytics in debug builds.
- [ ] Decide release default:
  - [ ] recommended: enabled in release and disclosed in privacy policy
  - [ ] optional: user-facing diagnostic toggle in Settings
- [ ] Ensure SecureLogger remains the default for app logs.
- [ ] Do not attach raw session IDs, emails, Google IDs, token values, or backup contents to crash reports.
- [ ] Add safe custom keys only:
  - [ ] app version
  - [ ] build type
  - [ ] current screen category
  - [ ] active session state category
  - [ ] sensor unavailable flag
- [ ] Add non-fatal logging only for production-critical recoverable errors:
  - [ ] billing query failure
  - [ ] Drive backup failure
  - [ ] restore validation failure
  - [ ] service recovery failure

### Documentation Updates

- [ ] Update `docs/privacy-policy.md`.
- [ ] Update `docs/play-store-data-safety.md`.
- [ ] Update `docs/security.md`.
- [ ] Update Settings privacy copy if a diagnostics toggle is added.

### Acceptance Criteria

- [ ] Test crash appears in Firebase for release/internal build.
- [ ] Debug builds do not send crash reports.
- [ ] Privacy docs accurately disclose diagnostics.
- [ ] No sensitive values appear in Crashlytics keys/logs.

## 12. Workstream 7 - Release Signing, Build Hardening, and Security

### Goal

Prepare the build for Play Store upload and remove release-blocking placeholders.

### Signing Checklist

- [ ] Enable Play App Signing in Play Console.
- [ ] Generate upload keystore outside the repo.
- [ ] Store upload keystore securely outside git.
- [ ] Add local signing properties file outside git or in ignored file.
- [ ] Verify `.gitignore` blocks keystores and signing configs.
- [ ] Configure release signing in `app/build.gradle.kts`.
- [ ] Document keystore backup and recovery steps.
- [ ] Record SHA-1/SHA-256 fingerprints for Google APIs.

### Security Checklist

- [ ] Replace certificate pinning placeholders or disable pinning until real pins are known.
- [ ] Confirm cleartext traffic is disabled.
- [ ] Confirm release logs are stripped.
- [ ] Confirm no debug-only sensor diagnostics are visible in release.
- [ ] Confirm root/emulator warnings are not disruptive.
- [ ] Confirm DataStore token storage is secure enough for real auth.
- [ ] Keep Room database unencrypted for V1 unless a later review changes the risk posture.

### Secrets Checklist

- [ ] Scan for `.env`, `.bak`, `.backup`, `.key`, `.pem`, `.p12`, `credentials`, and service-account JSON files.
- [ ] Confirm no service-account private keys are present.
- [ ] Confirm `google-services.json`, if committed, contains no private keys.
- [ ] Confirm no real tokens are in docs, logs, tests, or examples.

### Acceptance Criteria

- [ ] Release AAB builds locally.
- [ ] Release AAB can be uploaded to internal testing.
- [ ] No signing keys or secrets are committed.
- [ ] Security docs accurately reflect production posture.

## 13. Workstream 8 - Play Store Metadata and Policy Readiness

### Goal

Prepare the store listing and policy declarations for review.

### Store Listing Checklist

- [ ] Final app name.
- [ ] Short description.
- [ ] Full description.
- [ ] Feature graphic.
- [ ] App icon.
- [ ] Phone screenshots.
- [ ] Tablet screenshots if required or useful.
- [ ] Privacy policy URL.
- [ ] Support email.
- [ ] Website URL if available.
- [ ] Category selection.
- [ ] Tags.

### Policy Checklist

- [ ] Complete Data Safety form.
- [ ] Complete content rating questionnaire.
- [ ] Complete target audience declaration.
- [ ] Complete ads declaration: no ads.
- [ ] Complete app access instructions for reviewer.
- [ ] Complete financial features declaration if Play asks due to subscriptions.
- [ ] Complete health/medical declaration carefully: this is productivity/wellness, not medical.
- [ ] Complete permissions declaration if any sensitive permission requires it.
- [ ] Confirm subscription disclosure copy:
  - [ ] billing period
  - [ ] price
  - [ ] cancellation path
  - [ ] auto-renewal behavior
  - [ ] lifetime plan terms

### Documentation Checklist

- [ ] Update `docs/play-store-listing.md`.
- [ ] Update `docs/play-store-data-safety.md`.
- [ ] Update `docs/privacy-policy.md`.
- [ ] Add `docs/play-console-release-guide.md`.
- [ ] Add `docs/phase-16-manual-qa.md`.

### Acceptance Criteria

- [ ] Store listing draft is complete.
- [ ] Policy declarations match actual app behavior.
- [ ] Privacy policy covers Google Sign-In, Drive backup, billing, diagnostics, permissions, and data deletion.
- [ ] App review instructions explain how to test free and Pro flows.

## 14. Workstream 9 - Production QA and Release Gates

### Goal

Verify the app as a release candidate on real hardware and Play tracks.

### Automated Verification

- [ ] `./gradlew --no-configuration-cache :app:assembleDebug`
- [ ] `./gradlew --no-configuration-cache :app:bundleRelease`
- [ ] `./gradlew --no-configuration-cache :domain:session:test`
- [ ] `./gradlew --no-configuration-cache :domain:insights:test`
- [ ] `./gradlew --no-configuration-cache :core:backup:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :core:database:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :core:billing:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :core:auth:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :app:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :feature:focus:verifyPaparazziDebug`
- [ ] `./gradlew --no-configuration-cache :feature:settings:verifyPaparazziDebug`
- [ ] `./gradlew --no-configuration-cache :feature:insights:verifyPaparazziDebug`
- [ ] `git diff --check`
- [ ] `./scripts/check.sh` or documented exception for known ktlint convention disagreements

### Manual QA Matrix

Run on at least:

- [ ] one physical Android 13+ device
- [ ] one physical Android 14/15/16 device if available
- [ ] one emulator for basic navigation/regression

Critical paths:

- [ ] first launch onboarding
- [ ] notification permission education and request
- [ ] start focus session
- [ ] face-down detection
- [ ] arming countdown
- [ ] active timer progress
- [ ] pickup interruption
- [ ] manual pause/resume
- [ ] add time
- [ ] clean completion
- [ ] interrupted completion
- [ ] early end
- [ ] notification tap routes to Focus
- [ ] notification end action
- [ ] app kill recovery
- [ ] device reboot recovery
- [ ] Google sign-in
- [ ] Google sign-out
- [ ] manual backup
- [ ] restore
- [ ] auto-backup
- [ ] delete local data
- [ ] delete cloud backup
- [ ] monthly purchase
- [ ] yearly purchase
- [ ] lifetime purchase
- [ ] restore purchases after reinstall
- [ ] subscription cancellation behavior
- [ ] no-network behavior

### Release Gates

The app cannot be considered production-ready until:

- [ ] release AAB builds locally
- [ ] internal testing upload succeeds
- [ ] billing products load in Play test environment
- [ ] Google Sign-In works with release/internal signing certificate
- [ ] Drive backup/restore works with real account
- [ ] Crashlytics test event appears from release/internal build
- [ ] 12 testers / 14 days requirement is complete if applicable
- [ ] production access request is approved if applicable
- [ ] privacy/data safety docs match actual app behavior
- [ ] no secrets are staged or committed

## 15. Proposed Implementation Order

The order matters because some pieces depend on console setup.

### Sprint 16.1 - Release Infrastructure and Docs

- [x] Create `docs/play-console-release-guide.md`.
- [x] Create `docs/phase-16-manual-qa.md`.
- [x] Create `docs/phase-16-console-setup-info.md`.
- [x] Update `docs/architecture-guide.md` real/deferred matrix for Phase 16 target.
- [x] Set up Play Console account and app shell.
- [ ] Set up dedicated Google Cloud/Firebase project.
- [ ] Configure package, SHA fingerprints, OAuth consent, Drive API, Firebase app.
- [x] Confirm release signing approach.

### Sprint 16.2 - Real Google Sign-In

- [x] Draft focused Sprint 16.2 implementation plan.
- [x] Add dependencies.
- [x] Implement real auth repository.
- [x] Wire app-layer sign-in launcher.
- [x] Update Account screen states.
- [~] Add tests and manual QA.

Manual QA is pending a Web OAuth client/default web client ID in Firebase config.

### Sprint 16.3 - Real Drive Backup and Auto-Backup

- [ ] Implement Drive appDataFolder client.
- [ ] Implement manual backup.
- [ ] Implement manual restore.
- [ ] Implement cloud backup deletion.
- [ ] Add WorkManager once-daily auto-backup.
- [ ] Add tests and manual QA.

### Sprint 16.4 - Real Play Billing

- Detailed sprint plan drafted:
  - [phase-16-sprint-16-4-real-play-billing-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-sprint-16-4-real-play-billing-plan.md)
- Clarified scope for Sprint 16.4:
  - monthly subscription
  - yearly subscription
  - lifetime purchase
  - restore purchases
  - entitlement activation and downgrade rules
  - paywall copy refinement
  - final price-display QA
  - subscription cancellation and recovery QA
- Product ID assumption for implementation planning:
  - `pro_monthly`
  - `pro_yearly`
  - `pro_lifetime`
- Console state at planning time:
  - Play Billing products not yet created in Play Console
  - implementation should therefore include both repo-side integration and beginner-friendly Play Console/testing-track setup guidance
- Recommended source of truth retained:
  - Play Billing purchase state plus 24-hour local entitlement cache
  - not Drive backup data

- [ ] Create Play products.
- [ ] Implement real billing repository.
- [ ] Wire purchase flow.
- [ ] Wire restore purchases.
- [ ] Wire entitlement resolver.
- [ ] Add tests and Play license tester QA.

### Sprint 16.5 - Crash Reporting, Security, and Release Build

- [ ] Add Firebase Crashlytics.
- [ ] Update privacy/security docs.
- [ ] Configure release signing.
- [ ] Replace or disable placeholder certificate pins.
- [ ] Build release AAB.
- [ ] Upload internal test build.

### Sprint 16.6 - Store Listing, Closed Testing, and Production Access

- [ ] Finalize store listing.
- [ ] Complete policy declarations.
- [ ] Run internal testing.
- [ ] Run closed testing.
- [ ] Collect tester feedback.
- [ ] Fix release blockers.
- [ ] Apply for production access if required.
- [ ] Prepare staged production rollout.

## 16. Recommended Final Phase Acceptance Criteria

Phase 16 is complete only when:

- [ ] Fake auth is no longer used in release builds.
- [ ] Fake billing is no longer used in release builds.
- [ ] Fake backup transport is no longer used in release builds.
- [ ] Monthly/yearly/lifetime Pro products are configured in Play Console.
- [ ] Real purchases grant and restore entitlement.
- [ ] Real Google Sign-In works on debug and Play-signed builds.
- [ ] Real Drive backup/restore works through `appDataFolder`.
- [ ] Once-daily auto-backup works under constraints.
- [ ] Crashlytics is configured according to privacy decision.
- [ ] Release AAB builds locally.
- [ ] Internal testing upload succeeds.
- [ ] Store listing, privacy policy, data safety, and content rating are complete.
- [ ] Required closed testing is complete if the Play account requires it.
- [ ] Production access is available or the app is ready to request it.
- [ ] All critical QA paths pass on a real Android device.
- [ ] No secrets, keystores, service-account files, or tokens are committed.

## 17. Key Risks

### 17.1 Play Console Timeline Risk

If a new personal developer account is created, Google may require 12 testers for 14 continuous days before production access.

Mitigation:

- start Play Console setup early
- recruit testers while engineering work proceeds
- use internal testing first for quick sanity checks

### 17.2 Billing Test Complexity

Real Billing behavior often differs between local install, internal test, license testers, and production.

Mitigation:

- implement fake billing for unit tests and real billing for release/debug integration
- test through Play internal track before trusting billing
- document license tester setup

### 17.3 Google Sign-In Certificate Risk

Debug SHA and Play App Signing SHA are different.

Mitigation:

- register debug SHA for local testing
- register upload/release/Play signing SHA for Play testing
- document where each fingerprint comes from

### 17.4 Drive Scope / OAuth Review Risk

Even `drive.appdata` may require consent screen configuration and potentially verification depending on app publishing state and scopes.

Mitigation:

- use the narrowest scope
- keep consent copy clear
- avoid broad Drive scopes

### 17.5 Secrets Risk

Release signing and Google config introduce new files that can accidentally leak.

Mitigation:

- explicitly scan before staging
- keep keystore outside repo
- update `.gitignore`
- never use broad `git add .` for release files

## 18. Product Recommendation Summary

Recommended choices for V1 production:

- Create a dedicated Google Cloud/Firebase project for Phone Down.
- Use Drive `appDataFolder` for backup.
- Implement once-daily auto-backup with WorkManager.
- Keep Room database unencrypted for V1.
- Add Crashlytics with minimal disclosed diagnostics.
- Keep 24-hour entitlement cache.
- Use India launch pricing:
  - INR 99/month
  - INR 799/year
  - INR 1,999 lifetime
- Use default international pricing:
  - USD 1.99/month
  - USD 14.99/year
  - USD 39.99 lifetime
- Use internal testing first, then closed testing, then production rollout.

## 19. Immediate Next Step After Approval

Sprint 16.1 has started after approval. Repo-side setup documentation is now in place:

1. `docs/play-console-release-guide.md`
2. `docs/phase-16-manual-qa.md`
3. `docs/phase-16-console-setup-info.md`
4. `docs/architecture-guide.md` Phase 16 target matrix

The next hands-on step is user/browser-side console setup:

1. create the Play Console developer account
2. create the Phone Down app shell
3. create the dedicated Google Cloud/Firebase project
4. configure OAuth, Drive API, Firebase Android app, and fingerprints
5. generate the upload keystore outside the repo

No production code should be started until the console/account foundation is clear, because auth, Drive, Billing, signing, and Crashlytics all depend on correct project/package/fingerprint setup.
