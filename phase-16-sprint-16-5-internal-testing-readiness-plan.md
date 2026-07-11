# Sprint 16.5 - Internal Testing Readiness Completion Plan

## Status

- Planning status: Approved by user on 2026-06-12
- Implementation status: Phase 1 store listing saved; Play review send is blocked by remaining closed-testing dashboard steps
- Approval required before implementation: Complete
- Phase: Phase 16 - Android Production Readiness
- Scope target: Internal testing readiness only
- Production scope: Deferred until after internal-test QA evidence exists
- Current live Play Console baseline: Internal testing release `3 (1.0.2)` is active, but billing products, closed-test release, and production access are not ready

## 1. Purpose

Sprint 16.5 finishes the work needed to make the current Play internal testing track meaningful.

The app is already buildable, signed, uploaded, and available to internal testers. That is not the same as being ready for real internal QA. Real internal QA requires the Play Console setup and app runtime to agree with each other:

- Play must expose the billing products that the app queries.
- Google Sign-In must trust the signing identity used by Play-installed builds.
- Store setup must be complete enough that testers are not using an obviously unfinished app shell.
- Crash reporting must exist before broader closed testing so failures from testers can be recovered.
- Backup behavior must be unambiguous before testers exercise Drive backup/restore.
- QA must be run from the Play-installed app, not only local debug installs.

The sprint outcome is not "production launch." The sprint outcome is:

> Phone Down can be honestly tested by internal testers through Google Play, including billing, auth, backup, and core focus behavior, with enough diagnostics and documentation to fix what testers find.

## 2. Confirmed Scope Decisions

- [x] Target internal testing readiness first.
- [x] Keep billing product IDs exactly:
  - `pro_monthly`
  - `pro_yearly`
  - `pro_lifetime`
- [x] Include Crashlytics before closed testing.
- [x] Use the engineering recommendation for Android backup policy.
- [x] Include hands-on Play Console steps, not only code work.
- [x] Do not start implementation until this plan is approved.

## 3. Current Ground Truth

### 3.1 Repo And Build Truth

- Branch is clean on `main`.
- App identity in code:
  - application ID: `phonedown.app`
  - version code: `3`
  - version name: `1.0.2`
- Release signing is configured with ignored local `keystore.properties` or environment variables.
- Release signing fails fast if signing values are missing.
- Existing release artifact:
  - `app/build/outputs/bundle/release/PhoneDown-1.0.2-3/PhoneDown-1.0.2-3-release.aab`
- Real Play Billing code is wired through `RealBillingRepository`.
- Real Billing repository queries:
  - subscriptions: `pro_monthly`, `pro_yearly`
  - one-time product: `pro_lifetime`
- If Play products are missing, the app intentionally fails product loading with a setup message.
- Crashlytics is not implemented in app code yet.
- Android manifest still has `android:allowBackup="true"`.

### 3.2 Live Play Console Truth

- Developer account exists.
- Play app exists:
  - app: `Phone Down`
  - package: `phonedown.app`
  - status: `Draft`
- Internal testing:
  - active
  - latest release: `3 (1.0.2)`
  - available to internal testers
  - not reviewed
  - tester list: `internal_release_testers`
  - users in tester list: `4`
- Dashboard setup:
  - 10 of 11 setup tasks complete
  - remaining visible task: store listing setup
  - internal testers still see temporary app name `phonedown.app (unreviewed)`
- Billing console:
  - no one-time products exist
  - no subscriptions exist
- Closed testing:
  - `Closed testing - Alpha` track exists
  - no release on closed testing
- Production:
  - inactive
  - production access disabled
  - 0 testers currently opted in for production-access requirement
- Protected with Play:
  - automatic protection active
  - Play Integrity API not integrated
  - Play Billing protection inactive

## 4. Recommended Logical Order

### Why This Order

The order follows dependency direction:

1. Console products and signing identity must exist before Play-installed billing/auth QA can be trusted.
2. Store listing setup should be finished before testers install, otherwise feedback is polluted by temporary/unreviewed presentation.
3. Backup policy and Crashlytics should be fixed before broader tester feedback, because they affect trust and recovery.
4. Only then should manual QA be run through the Play-distributed build.
5. Closed testing should wait until internal QA passes.

## 5. Implementation Phase Map

Use this section as the top-level progress tracker. Each phase below has its own detailed checklist and acceptance criteria.

### Approved Technical Defect Track - 2026-07-04

The user approved the recommendations in `docs/technical-backlog-defect-analysis-2026-07-04.md` and requested technical backlog completion before remaining Console, rollout, and tester-administration work.

Implementation order:

- [x] Technical Slice A - Session terminal-state correction:
  - preserve completed-session state after foreground-service shutdown
  - show authoritative completion metrics until user taps `Done`
  - treat live `Broken` state as recoverable instead of terminal
  - ensure ending a broken session produces a terminal summary
- [x] Technical Slice B - Duration editing correction:
  - wire Settings default-duration editing
  - persist only explicit default changes
  - keep Focus-screen one-time duration selection temporary
  - remove production no-op callback wiring
- [~] Technical Slice C - Identity and entitlement correction:
  - improve typed Google Sign-In diagnostics without exposing sensitive data
  - present Google identity and Play Pro entitlement as separate statuses
  - retain Play `Restore purchases` as the V1 Pro recovery mechanism
  - document and complete external Android OAuth/fingerprint configuration

Technical dependency rules:

- Slice A comes first because it affects the core focus-session workflow and currently contains two high-severity state-contract defects.
- Slice B follows because it is locally implementable and changes session-start defaults.
- Slice C follows because code improvements can be completed locally, while final Sign-In proof also depends on Firebase/Google Cloud and Play signing configuration.
- Remaining country, tester, review-submission, and production-access administration stays deferred until these technical slices pass verification.

Implementation progress - 2026-07-04:

- Slice A code complete:
  - foreground-service shutdown no longer dismisses terminal session state
  - completion summary uses persisted focus, elapsed, planned, penalty, and interruption values
  - live broken sessions show recoverable `Clean status lost` guidance and an explicit end flow instead of a no-op `Done`
- Slice B code complete:
  - Settings now opens a default-duration picker and persists explicit selection
  - Focus-screen duration selection is now a one-session override and no longer silently changes the default
  - required callback wiring replaces the production no-op default
- Slice C local code complete, external trust setup pending:
  - Credential Manager failures now map to safe, actionable categories
  - Account screen distinguishes Google backup identity from Google Play Pro entitlement
  - separate Pro username/password login remains intentionally out of scope for V1
  - Firebase/Google Cloud still needs Android OAuth clients for supported signing fingerprints, followed by refreshed config and device verification
- Verification completed:
  - app/domain/core JVM test suites passed
  - Focus and Settings screenshot verification passed
  - signed release bundle passed
  - Android instrumentation execution remains pending because no device was connected
  - Insights screenshot verification exposed an unrelated date-dependent baseline: expected May calendar dates versus current July dates; no Insights source changed

| Phase | Status | Primary Owner | Blocks | Outcome |
|---|---|---|---|---|
| Phase 1 - Store Listing And Console Setup | Complete for store listing; review submission blocked by closed-testing dashboard steps | Play Console | Phase 7 QA quality | Store setup is complete enough for internal testers |
| Phase 2 - Billing Product Creation | Not started | Play Console | Billing QA | Required products exist and are active |
| Phase 3 - Signing And Firebase Trust | Console setup complete; Play-installed sign-in QA pending | Play Console/Firebase | Auth and Drive QA | Play-installed app can be trusted by Google services |
| Phase 4 - Backup Policy Cleanup | Complete | Code/docs | Backup/privacy QA | One clear backup authority for V1 |
| Phase 5 - Crashlytics Integration | Code/build complete; Firebase console/device event verification pending | Code/docs/Firebase | Closed testing readiness | Release/internal crashes are diagnosable |
| Phase 6 - Internal Release Sync | Not started | Code/Play Console | Play-installed QA | Current internal release contains required changes |
| Phase 7 - Play-Installed Internal QA | Not started | Device QA | Sprint completion | Internal testing readiness is proven or blockers are logged |

### Master Tracking Checklist

- [x] Phase 1 store listing complete: listing text/assets saved and no longer the visible dashboard setup blocker.
- [ ] Phase 2 complete: `pro_monthly`, `pro_yearly`, and `pro_lifetime` active in Play Console.
- [~] Phase 3 complete: Play signing fingerprints verified and trusted by Firebase/Google Cloud; Play-installed sign-in QA pending.
- [x] Phase 4 complete: Android OS backup disabled for V1 and docs updated.
- [~] Phase 5 complete: Crashlytics integrated and privacy docs updated; Firebase console/device event verification pending.
- [ ] Phase 6 complete: fresh internal release uploaded if code/config changed.
- [ ] Phase 7 complete: Play-installed QA run and documented.
- [ ] Sprint completion review done.

### Phase Dependency Rules

- Phase 1 and Phase 2 can run in parallel because both are console setup tasks.
- Phase 3 should happen before auth/Drive QA, but can run while billing products are being created.
- Phase 4 and Phase 5 are code phases and should happen before uploading a fresh internal release.
- Phase 6 should happen after all required code/config changes are complete.
- Phase 7 should start only after Phases 1-6 are complete or explicitly marked as deferred with a reason.

## 6. Phase 1 - Play Console Store Setup Completion

### Goal

Complete the remaining Play Console setup item so internal testers see a coherent app identity and the dashboard no longer blocks later closed-testing steps.

### Phase Checklist

- [x] Confirm current Play dashboard setup state.
- [x] Complete missing store listing fields.
- [x] Validate listing copy against actual app behavior.
- [x] Save Console changes.
- [x] Update repo docs with safe public status.
- [x] Confirm phase acceptance criteria after Play quick checks and review-send state settle.

### Phase 1 Progress - 2026-06-12

- Play Console Dashboard initially showed `Provide app information and create your store listing` as `10 of 11 complete`.
- The visible remaining action was `Set up your store listing`.
- Default store listing page opened for `Default - English (United Kingdom) - en-GB`.
- App name field already contains `Phone Down`.
- Short description and full description were filled in the browser from existing Fastlane metadata and saved.
- Upload-ready public store assets now exist in the repo:
  - `fastlane/metadata/android/en-US/images/icon.png` - 512x512
  - `fastlane/metadata/android/en-US/images/featureGraphic.png` - 1024x500
  - `fastlane/metadata/android/en-US/images/phoneScreenshots/*.png` - 1080x1920
  - `fastlane/metadata/android/en-US/images/sevenInchScreenshots/*.png` - 1080x1920
  - `fastlane/metadata/android/en-US/images/tenInchScreenshots/*.png` - 1080x1920
- Browser automation reached the Play Console asset side panel, but Chrome blocked local file upload with a file-access permission error.
- Retried after the user enabled `Allow access to file URLs`, but Chrome still returned the same file upload permission error and did not expose a native file picker.
- Switched to Computer Use, which opened the native macOS file picker and attached all required assets:
  - app icon: 1 / 1
  - feature graphic: 1 / 1
  - phone screenshots: 4 / 8
  - 7-inch tablet screenshots: 4 / 8
  - 10-inch tablet screenshots: 4 / 8
- Play Console saved the default store listing and showed `Change saved. Send for review in Publishing overview.`
- Publishing overview now lists the saved store-listing change under `Changes not yet sent for review`.
- Play quick checks finished, but `Send app for review` remains disabled because required dashboard steps are still incomplete.
- Dashboard no longer shows the store-listing setup card; the remaining visible required work is closed testing:
  - select countries and regions
  - select testers
  - preview and confirm the release
  - send the release to Google for review
- Production access still shows 0 opted-in testers, so the 12 tester / 14 day requirement is not started.

### Phase 1 Icon Correction - 2026-06-13

- User noticed the Play Console store-listing app icon did not match the intended Phone Down app / launcher icon.
- Replaced `fastlane/metadata/android/en-US/images/icon.png` with a 512x512 resized copy of the intended P/D launcher-style icon from `app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png`.
- Removed the stale Play Console app icon and uploaded the corrected 6/13/2026 `icon.png`.
- Play Console now shows the corrected P/D mark in the App icon slot and still reports `1 / 1`.
- Saved the corrected listing change. Play Console confirmed `Change saved. Send for review in Publishing overview.`
- Review submission was not triggered; this saved draft should be sent together with the remaining closed-testing setup changes.

### Hands-On Play Console Steps

- [x] Open Play Console > Phone Down > Dashboard.
- [x] Confirm `Provide app information and create your store listing` still shows 10 of 11 complete.
- [x] Open `Set up your store listing`.
- [x] Complete missing listing fields.
- [x] Confirm app name is `Phone Down`, not only temporary `phonedown.app (unreviewed)`.
- [x] Confirm short description matches current product.
- [x] Confirm full description does not overclaim:
  - no fake billing claims
  - no untested production claims
  - no unsupported platform claims
- [x] Confirm screenshots/icons/feature graphic requirements are satisfied if requested by Console.
- [x] Save changes.
- [x] Return to Dashboard and confirm setup checklist no longer shows store listing as incomplete.
- [x] Wait for Play quick checks to finish.
- [ ] Send pending listing change for review after required closed-testing dashboard steps are complete.

### Repo Documentation Steps

- [x] Update `docs/phase-16-console-setup-info.md` with:
  - store listing completed: Yes
  - any public non-secret app listing values
  - date completed
- [ ] Update `docs/play-console-release-guide.md` if the actual console flow differs from existing guide.

### Acceptance Criteria

- [x] Play dashboard no longer shows store listing as remaining setup work.
- [ ] Internal testing page no longer warns testers about only a temporary unreviewed app name, or the warning is understood as review-only and not setup-blocking.
- [ ] Store text matches actual app behavior.

## 7. Phase 2 - Play Billing Product Setup

### Goal

Create real Play Billing products that match the app's hardcoded product contract.

### Phase Checklist

- [ ] Create monthly subscription.
- [ ] Create yearly subscription.
- [ ] Create lifetime one-time product.
- [ ] Activate required base plans/offers/purchase options.
- [ ] Confirm product tables show all required product IDs.
- [ ] Update repo docs with product status.
- [ ] Confirm phase acceptance criteria.

### Product Contract

| Product ID | Play Type | App Meaning | Required State |
|---|---|---|---|
| `pro_monthly` | Subscription | Monthly Pro | Active with purchasable base plan/offer |
| `pro_yearly` | Subscription | Yearly Pro | Active with purchasable base plan/offer |
| `pro_lifetime` | One-time product | Lifetime Pro | Active purchase option |

### Pricing Recommendation

Use the Phase 16 launch pricing unless changed deliberately:

| Product | India Price | Default/US Reference |
|---|---:|---:|
| Monthly Pro | INR 99/month | USD 1.99/month |
| Yearly Pro | INR 799/year | USD 14.99/year |
| Lifetime Pro | INR 1,999 one-time | USD 39.99 one-time |

### Hands-On Play Console Steps - Subscriptions

- [x] Open Play Console > Phone Down > Monetize with Play > Products > Subscriptions.
- [x] Create subscription `pro_monthly`.
- [ ] Add clear name, for example `Phone Down Pro Monthly`.
- [x] Add monthly base plan.
- [ ] Set base plan active.
- [~] Set regional prices, starting with India and default prices.
- [ ] Confirm at least one purchasable offer/base plan exists.
- [ ] Save/activate.
- [ ] Create subscription `pro_yearly`.
- [ ] Add clear name, for example `Phone Down Pro Yearly`.
- [ ] Add yearly base plan.
- [ ] Set base plan active.
- [ ] Set regional prices.
- [ ] Confirm at least one purchasable offer/base plan exists.
- [ ] Save/activate.

### Hands-On Play Console Steps - One-Time Product

- [ ] Open Play Console > Phone Down > Monetize with Play > Products > One-time products.
- [ ] Create one-time product `pro_lifetime`.
- [ ] Add clear name, for example `Phone Down Pro Lifetime`.
- [ ] Add purchase option.
- [ ] Set price.
- [ ] Activate product.
- [ ] Confirm it appears in the one-time products table.

### Repo Documentation Steps

- [ ] Update `docs/phase-16-console-setup-info.md` with product status:
  - `pro_monthly`: created/active
  - `pro_yearly`: created/active
  - `pro_lifetime`: created/active
- [ ] Record only public product IDs and status.
- [ ] Do not record revenue reports, payment details, tax details, or private account information.

### Acceptance Criteria

- [ ] Play Console subscriptions page lists `pro_monthly`.
- [ ] Play Console subscriptions page lists `pro_yearly`.
- [ ] Play Console one-time products page lists `pro_lifetime`.
- [ ] Each product is active or otherwise available to the internal testing build.
- [ ] App paywall can load real localized product details on Play-installed build.

### 2026-07-09 Console Attempt Notes

- `pro_monthly` subscription shell was created in Play Console as `Phone Down Pro Monthly`.
- Monthly base-plan draft was configured with:
  - base plan ID `monthly`
  - type `Auto-renewing`
  - billing period `Monthly`
  - default grace/account-hold settings
- Bulk regional pricing was attempted for all 177 countries/regions from INR 99.
  - Play normalized India to INR 100.00.
  - Play generated United States as USD 0.99, below the launch reference of USD 1.99.
  - Manual US override to USD 1.99 did not persist in the inline editor.
- Saving the monthly base-plan draft failed with Play Console message: `Your changes couldn't be saved`.
- Next retry should use the narrower path first:
  - set only India availability/price to create a valid internal-testing product
  - save/activate monthly base plan
  - then add US/default international prices after the product exists
  - repeat for yearly and lifetime only after monthly save path is proven

### 2026-07-09 Recovery Attempt Notes

- Cleared the missing subscription-benefits task on `pro_monthly`.
  - Added benefit text: `Unlimited focus sessions and Pro tools`.
  - Play Console confirmed: `Your changes have been saved`.
- Retried monthly base-plan creation from a clean create form.
  - base plan ID: `monthly`
  - type: `Auto-renewing`
  - billing period: `Monthly`
  - availability: India only
  - entered price: INR 99
  - Play-normalized price: INR 100.00
- Final base-plan save still failed with Play Console message: `Your changes couldn't be saved`.
- Conclusion: blocker is not all-region pricing and not missing benefits. It is likely a Play Console/backend/account-state validation issue around base-plan creation.
- Next recommended action:
  - manually retry base-plan creation in Play Console after a refresh/new browser session
  - if it still fails, capture screenshot and contact Play Console support with product ID `pro_monthly`, base plan ID `monthly`, India-only INR 100.00, and error `Your changes couldn't be saved`

### 2026-07-11 Merchant Verification Diagnosis

- Inspected `Settings > Payments profile` in Play Console.
- Confirmed PA-CB merchant verification was `Not started`; Play stated that selling is disabled until verification completes.
- Initiated BillDesk merchant verification; Play Console now shows `In progress`.
- BillDesk instructions are delivered by email from `onboarding@billdesk.com` to the primary payments-profile contact.
- Immediate Gmail search found no matching message, so delivery may still be pending; Inbox and Spam must be checked again.
- A separate Google Payments identity-verification email requests organization details and a readable government photo ID, with PAN named as an accepted example.
- Play Console payment-account details still say to await BillDesk; no identity-upload action is exposed there yet.
- Phase 2 is externally blocked until BillDesk KYC is completed and approved.
- After approval, retry `pro_monthly` base-plan creation before creating yearly or lifetime products.

## 8. Phase 3 - Play Signing And Firebase/OAuth Fingerprint Verification

### Goal

Ensure Google Sign-In and Drive authorization work on the Play-installed app, not only debug installs.

### Phase Checklist

- [x] Find Play App Signing certificate fingerprints.
- [x] Compare upload-key fingerprints against local docs.
- [x] Add Play signing fingerprints to Firebase/Google Cloud if missing.
- [x] Refresh ignored `google-services.json` only if required.
- [x] Update repo docs with public fingerprint status.
- [~] Confirm phase acceptance criteria.

### Why This Matters

Google auth often trusts the tuple:

`package name + signing certificate fingerprint`

Debug builds use the debug certificate. Play-installed builds use Play signing. If Firebase/Google Cloud lacks the Play signing fingerprint, auth can fail even when code is correct.

### Hands-On Play Console Steps

- [x] Open Play Console > Phone Down > test/release signing or app integrity signing details.
- [x] Locate Play App Signing certificate fingerprints.
- [x] Copy public SHA-1 and SHA-256 fingerprints only.
- [x] Confirm upload key SHA-1/SHA-256 still match local docs:
  - SHA-1: `EE:FA:73:EF:A2:F0:6A:A1:8F:03:A8:0E:C4:A4:20:F7:65:33:A3:9C`
  - SHA-256: `63:0E:62:5F:A1:14:13:C9:A0:FB:2B:53:E8:4B:5A:D2:B3:03:11:B5:0D:52:4F:42:B9:92:75:0E:2C:7E:F9:0A`

### Firebase/Google Cloud Steps

- [x] Open Firebase project for Phone Down.
- [x] Add Play signing SHA-1 to Android app `phonedown.app` if missing.
- [x] Add Play signing SHA-256 if available/needed.
- [x] Download refreshed `google-services.json` only if Firebase config changes require it.
- [x] Place `google-services.json` at `app/google-services.json`.
- [x] Confirm it remains ignored by Git.
- [x] Confirm no OAuth client secret JSON is added to repo.

### Repo Documentation Steps

- [x] Update `docs/phase-16-console-setup-info.md`:
  - Play App Signing enabled: Yes/No
  - Play signing SHA-1
  - Play signing SHA-256
  - date verified
- [x] Keep only public fingerprints in docs.

### Acceptance Criteria

- [x] Play signing fingerprints are recorded.
- [x] Firebase/Google Cloud trusts Play signing fingerprints.
- [~] Play-installed Google Sign-In can be tested meaningfully after Google OAuth propagation.

### Phase 3 Progress - 2026-07-09

- Verified local debug and upload-key fingerprints with `./gradlew --no-daemon --no-configuration-cache :app:signingReport --console=plain`.
- Verified Play App Signing certificate in Play Console:
  - SHA-1: `3D:57:D0:6B:3F:38:A6:D7:40:4F:48:F1:9E:4C:68:07:55:30:D0:25`
  - SHA-256: `E2:EA:8A:C4:27:76:1F:66:3F:96:9F:D7:5E:F6:45:3E:A8:93:17:10:21:CE:20:9F:D7:78:4A:DC:60:D7:95:66`
- Added debug, upload, and Play App Signing SHA-1/SHA-256 values to the Firebase Android app.
- Google Cloud Credentials initially had only `Phone Down Web Client` and `Phone Down Android Debug`.
- Created missing Android OAuth clients:
  - `Phone Down Android Upload`
  - `Phone Down Android Play Signing`
- Downloaded refreshed `app/google-services.json`; it remains ignored by Git.
- Refreshed config now contains three Android OAuth clients and one Web client.
- Verification passed:
  - `./gradlew --no-daemon --no-configuration-cache :app:processDebugGoogleServices :app:processReleaseGoogleServices :app:assembleDebug --console=plain`
  - generated debug and release resources include `default_web_client_id`.
- Remaining proof:
  - wait for Google OAuth propagation if needed, then test Google Sign-In from the Play-installed app.

## 9. Phase 4 - Android Backup Policy Cleanup

### Goal

Make backup behavior unambiguous before testers exercise Drive backup/restore.

### Phase Checklist

- [x] Change manifest backup policy.
- [x] Update privacy/data-safety/readiness docs.
- [x] Run manifest/build verification.
- [x] Confirm no OS backup rules are needed after disabling OS backup.
- [x] Confirm phase acceptance criteria.

### Recommendation

Set `android:allowBackup="false"` for V1.

### Why This Is Recommended

Phone Down now has explicit app-managed Google Drive backup/restore. Keeping Android OS backup enabled creates two restore paths:

- OS-level automatic backup/restore
- in-app Google Drive appDataFolder backup/restore

That ambiguity can confuse:

- delete-all-data expectations
- restore correctness
- privacy policy wording
- tester bug reports

For V1, one backup authority is simpler and safer: the in-app Drive backup feature.

### Code Steps

- [x] Update `app/src/main/AndroidManifest.xml`.
- [x] Change `android:allowBackup="true"` to `android:allowBackup="false"`.
- [x] Confirm no backup rules are required after disabling OS backup.

### Documentation Steps

- [x] Update `docs/privacy-policy.md` to reflect app-managed backup only.
- [x] Update `docs/play-store-data-safety.md` if backup behavior language needs adjustment.
- [x] Update `docs/pre-upload-go-no-go-checklist-2026-05-17.md` or add a newer readiness checklist entry marking backup policy resolved.
- [x] Update `docs/phase-16-console-setup-info.md` with backup policy decision.

### Verification

- [x] Run `./gradlew --no-daemon --no-configuration-cache :app:processDebugMainManifest`.
- [x] Run `./gradlew --no-daemon --no-configuration-cache :app:assembleDebug`.
- [x] Inspect merged manifest if needed.

### Acceptance Criteria

- [x] Manifest has `android:allowBackup="false"`.
- [x] Policy docs no longer imply OS-level backup is part of Phone Down's restore model.
- [x] Manual Drive backup/restore remains the explicit backup path.

## 10. Phase 5 - Crashlytics Before Closed Testing

### Goal

Add privacy-conscious Crashlytics before moving from internal QA into closed testing.

### Phase Checklist

- [x] Add Crashlytics Gradle/plugin dependencies.
- [x] Configure debug disabled/release enabled collection behavior.
- [x] Add privacy-safe runtime setup.
- [x] Update privacy/security/data-safety/manual-QA docs.
- [x] Run build/test verification.
- [ ] Verify safe release/internal Crashlytics event.
- [ ] Confirm phase acceptance criteria.

### Why Before Closed Testing

Closed testing introduces more people, more devices, and less direct observation. Without crash reporting, failures become slower to diagnose. Crashlytics gives a recovery loop while keeping analytics minimal.

### Code Steps

- [x] Add Firebase Crashlytics Gradle plugin to version catalog/build setup.
- [x] Apply Crashlytics plugin to `:app`.
- [x] Add Firebase Crashlytics dependency.
- [x] Ensure `google-services.json` remains ignored.
- [x] Configure Crashlytics collection:
  - disabled for debug builds
  - enabled for release/internal testing builds
- [x] Add app startup initialization if needed.
- [x] Add a debug-only or internal-only manual test hook only if safe and clearly removed/guarded.
- [x] Do not log:
  - email
  - Google account ID
  - access tokens
  - purchase token
  - backup payload
  - raw session database content

### Documentation Steps

- [x] Update `docs/privacy-policy.md` to disclose crash diagnostics if not already accurate.
- [x] Update `docs/play-store-data-safety.md` if Crashlytics changes data collection declarations.
- [x] Update `docs/security.md` with Crashlytics redaction rules.
- [x] Update `docs/phase-16-manual-qa.md` Crashlytics QA section with exact verification steps.

### Verification

- [x] Run `./gradlew --no-daemon --no-configuration-cache :app:assembleDebug`.
- [x] Run `./gradlew --no-daemon --no-configuration-cache :app:testDebugUnitTest`.
- [x] Run `./gradlew --no-daemon --no-configuration-cache :app:bundleRelease`.
- [x] Confirm debug builds do not send Crashlytics events.
- [ ] Confirm release/internal test crash event appears in Firebase Crashlytics using a safe test event.

### Acceptance Criteria

- [x] Crashlytics integrated in release build.
- [x] Debug collection disabled.
- [x] No sensitive values are logged as keys, messages, or custom data.
- [x] Privacy/data-safety docs match actual Crashlytics behavior.

### Phase 4/5 Progress - 2026-07-07

- Changed `app/src/main/AndroidManifest.xml` to disable Android OS automatic backup for V1.
- Added Firebase Crashlytics through the version catalog, root Gradle plugin list, and app module dependencies.
- Configured Crashlytics collection via manifest placeholders:
  - debug build: `firebase_crashlytics_collection_enabled=false`
  - release build: `firebase_crashlytics_collection_enabled=true`
- Kept Crashlytics passive; no custom keys, user identifiers, or manual crash trigger were added.
- Updated privacy, Data Safety, security, manual QA, console setup, and handoff docs.
- Verification passed:
  - `./gradlew --no-daemon --no-configuration-cache :app:processDebugMainManifest :app:assembleDebug :app:testDebugUnitTest --console=plain`
  - `./gradlew --no-daemon --no-configuration-cache :app:bundleRelease --console=plain`
  - merged debug/release manifest inspection confirmed `android:allowBackup="false"`
  - merged debug manifest confirmed Crashlytics collection `false`
  - merged release manifest confirmed Crashlytics collection `true`
- Remaining Phase 5 proof:
  - verify a safe release/internal Crashlytics event appears in Firebase after the next Play/internal build is installed.

## 11. Phase 6 - Internal Test Build And Console Sync

### Goal

Produce and, if needed, upload a fresh internal testing build after console/code changes.

### Phase Checklist

- [ ] Decide whether new version code/name is required.
- [x] Build fresh signed release AAB if code/config changed.
- [x] Verify signing and artifact contents.
- [ ] Upload to internal testing if needed.
- [ ] Confirm latest internal release is active.
- [ ] Update docs with exact release version/date.
- [ ] Confirm phase acceptance criteria.

### When A Fresh Build Is Required

A fresh build is required if any code/config changes land, including:

- backup policy manifest change
- Crashlytics integration
- refreshed Firebase config that changes runtime behavior
- billing code fixes from QA
- version bump

### Code Steps

- [x] Bump version if uploading a new AAB:
  - `versionCode = 4`
  - `versionName = "1.0.3"`
- [x] Build fresh release AAB:

```bash
./gradlew --no-daemon --no-configuration-cache :app:bundleRelease
```

- [x] Create version-labeled copy under release output if needed.
- [x] Verify AAB signing.
- [x] Scan AAB file list for forbidden bundled files:
  - `.env`
  - `.pem`
  - `.p12`
  - `.jks`
  - `.keystore`
  - `keystore.properties`
  - `google-services.json`
  - `client_secret*.json`
  - service-account JSON

### Version 1.0.3 Artifact Record - 2026-07-04

- Play Console artifact:
  - `app/build/outputs/bundle/release/PhoneDown-1.0.3-4/PhoneDown-1.0.3-4-release.aab`
  - SHA-256: `8cd1d06c6bea4b37679f8b97764af282dcf3039ddfd355b54391301b379ae1a0`
- Direct-install artifact:
  - `app/build/outputs/apk/release/PhoneDown-1.0.3-4/PhoneDown-1.0.3-4-release.apk`
  - SHA-256: `db644ea8bfe541309a59650443d597999244acfa18f2bb2c9c159ff9423994bb`
- APK metadata verified:
  - package: `phonedown.app`
  - version code: `4`
  - version name: `1.0.3`
  - min SDK: `26`
  - target SDK: `36`
- APK signature verified with the expected upload certificate SHA-256:
  - `63:0E:62:5F:A1:14:13:C9:A0:FB:2B:53:E8:4B:5A:D2:B3:03:11:B5:0D:52:4F:42:B9:92:75:0E:2C:7E:F9:0A`
- AAB signature verification returned `jar verified`; expected self-signed upload-key and no-timestamp warnings remain.

### Hands-On Play Console Steps

- [ ] Open Play Console > Phone Down > Internal testing.
- [ ] Create new release if version code changed.
- [ ] Upload fresh AAB.
- [ ] Add release notes focused on tester-visible changes.
- [ ] Review warnings.
- [ ] Roll out to internal testing.
- [ ] Confirm latest internal release is active and available.
- [ ] Confirm tester list still includes `internal_release_testers`.

### Acceptance Criteria

- [ ] Latest internal testing release matches the intended version.
- [ ] Release is active and available to internal testers.
- [ ] No Play Console warnings remain unexplained.
- [ ] Local docs record exact release version/date.

## 12. Phase 7 - Internal Play-Installed QA

### Goal

Run the QA that proves internal testing is meaningful.

### Phase Checklist

- [ ] Confirm pre-QA checklist.
- [ ] Install/update from Play internal testing link.
- [ ] Run auth and Drive QA.
- [ ] Run billing purchase/restore QA.
- [ ] Run core focus/session QA.
- [ ] Run system behavior QA.
- [ ] Verify Crashlytics signal.
- [ ] Document results and blockers.
- [ ] Confirm phase acceptance criteria.

### Pre-QA Checklist

- [ ] Store listing setup complete.
- [ ] Billing products active.
- [ ] Play signing fingerprints trusted by Firebase/Google Cloud.
- [ ] Backup policy resolved.
- [ ] Crashlytics integrated or intentionally deferred with a written reason.
- [ ] Latest internal testing release active.
- [ ] Tester account is in `internal_release_testers`.
- [ ] Tester has joined through internal test link.

### QA Matrix

| Area | Required Tests |
|---|---|
| Install/update | Install from Play internal link, update over prior build if available |
| Auth | Sign in, sign out, relaunch, token refresh path |
| Drive backup | Manual backup, restore, delete cloud backup if exposed |
| Auto-backup | Confirm scheduled worker path does not error visibly |
| Billing catalog | Paywall loads all 3 real products with localized prices |
| Monthly purchase | Purchase, entitlement unlock, relaunch, restore |
| Yearly purchase | Purchase, entitlement unlock, relaunch, restore |
| Lifetime purchase | Purchase, entitlement unlock, reinstall/restore if practical |
| Cancel/recovery | Cancel subscription in Play, relaunch, manage subscription link |
| Core focus | Start, face-down detect, pickup interrupt, complete, early end |
| System behavior | Notification, foreground service, boot/recovery, permissions |
| Settings | Feedback intent, portfolio link, version text |
| Crash recovery | Verify Crashlytics receives safe test event from release/internal build |

### Documentation Steps

- [ ] Update `docs/phase-16-manual-qa.md` with:
  - device
  - Android version
  - install source
  - app version
  - tester account type
  - test results
  - bugs found
- [ ] Add bug entries to `docs/phase-14-bugs.md` or a new Phase 16 bug log if needed.
- [ ] Update `docs/agent-handoff.md` with final sprint state.

### Acceptance Criteria

- [ ] Play-installed app can sign in.
- [ ] Play-installed app can backup/restore.
- [ ] Paywall loads real products.
- [ ] Purchases unlock Pro.
- [ ] Restore purchases works.
- [ ] Core focus behavior passes on real hardware.
- [ ] Crash diagnostics are visible for release/internal build.
- [ ] Any blockers are documented with reproduction steps.

## 13. Out Of Scope For This Sprint

- Production rollout.
- Production access application.
- Closed testing execution.
- 12-tester/14-day closed testing clock.
- Play Integrity API integration unless Play requires it immediately.
- Server-side purchase verification.
- RevenueCat or third-party billing abstraction.
- Major UI redesign.

## 14. Sprint Acceptance Criteria

Sprint 16.5 is complete when:

- [ ] Store listing setup is complete enough for internal testers.
- [ ] `pro_monthly`, `pro_yearly`, and `pro_lifetime` exist and are active in Play Console.
- [ ] Play signing fingerprints are verified and trusted by Firebase/Google Cloud.
- [ ] Android backup policy is resolved in code and docs.
- [ ] Crashlytics is integrated before closed testing.
- [ ] Latest internal testing release includes any required code/config changes.
- [ ] Play-installed QA has been run and recorded.
- [ ] Internal-test blockers are either fixed or explicitly logged.
- [ ] Docs reflect the current truth rather than older May 17 assumptions.
- [ ] No secrets or credential files are tracked or bundled.

## 15. Required Verification Commands

Run these after code changes:

```bash
./gradlew --no-daemon --no-configuration-cache :app:assembleDebug
./gradlew --no-daemon --no-configuration-cache :app:testDebugUnitTest
./gradlew --no-daemon --no-configuration-cache :domain:session:test :domain:insights:test
./gradlew --no-daemon --no-configuration-cache :core:backup:testDebugUnitTest :core:database:testDebugUnitTest
./gradlew --no-daemon --no-configuration-cache :feature:focus:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug
./gradlew --no-daemon --no-configuration-cache :app:bundleRelease
git diff --check
```

If any command is skipped, record why in the sprint report.

## 16. Git And Secrets Safety

Before any commit:

- [ ] Run `git status`.
- [ ] Run `git diff --cached`.
- [ ] Stage explicit files only.
- [ ] Check staged filenames for:
  - `.env`
  - `.bak`
  - `.backup`
  - `.key`
  - `.pem`
  - `.p12`
  - `.jks`
  - `.keystore`
  - `keystore.properties`
  - `google-services.json`
  - `client_secret*.json`
  - service-account JSON
- [ ] Confirm examples use placeholders only.
- [ ] Confirm `.gitignore` protects local secrets and build outputs.

## 17. Next Step After Plan Approval

Recommended implementation sequence:

1. Complete Play Console store listing.
2. Create billing products.
3. Verify Play signing fingerprints in Firebase/Google Cloud.
4. Implement backup policy cleanup.
5. Implement Crashlytics.
6. Build/upload fresh internal test release if code changed.
7. Run Play-installed QA and document results.

After this sprint passes, create the next plan for closed testing and production-access readiness.
