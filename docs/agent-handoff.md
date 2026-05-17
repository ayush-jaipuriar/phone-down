# Agent Handoff Summary

## Update - 2026-05-17 (Production Readiness Audit Before First Play Upload)

- A full pre-upload production-readiness audit was completed before attempting the first Play internal upload.
- Latest release-signing follow-up:
  - user created local ignored `keystore.properties`
  - `keystore.properties` is ignored by Git and contains all four required signing keys
  - the configured `storeFile` resolves to the expected `phone-down-upload.jks`
  - `./gradlew --no-daemon --no-configuration-cache :app:bundleRelease` now succeeds with real upload-key signing
  - fresh signed release bundle exists at:
    - [app-release.aab](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build/outputs/bundle/release/app-release.aab)
  - artifact details:
    - size: 6.2 MB
    - bundle SHA-256: `764667e43982c6d82d9726f5493a4a109112bdb22e10248b79276aa373ab9e85`
    - upload certificate SHA-1: `EE:FA:73:EF:A2:F0:6A:A1:8F:03:A8:0E:C4:A4:20:F7:65:33:A3:9C`
    - upload certificate SHA-256: `63:0E:62:5F:A1:14:13:C9:A0:FB:2B:53:E8:4B:5A:D2:B3:03:11:B5:0D:52:4F:42:B9:92:75:0E:2C:7E:F9:0A`
  - `jarsigner` reported `jar verified`
  - a safety scan of the AAB file list did not find bundled keystores, `google-services.json`, `client_secret*.json`, `.env`, `.pem`, `.p12`, or similar credential files; only normal AndroidX/Play Services credential library metadata appeared
  - `git diff --check` still passes
- Current interpretation:
  - release artifact mechanics are now good enough for the first internal testing upload
  - broader production release remains no-go until Play products and Play-installed QA are complete
- New audit document:
  - [docs/production-readiness-audit-2026-05-17.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/production-readiness-audit-2026-05-17.md)
- Important repo/build truths established during this audit:
  - `:app:bundleRelease` succeeded locally and produced:
    - `app/build/outputs/bundle/release/app-release.aab`
  - `:app:testDebugUnitTest` succeeded
  - `git diff --check` succeeded
- However, the broader production-readiness conclusion is still **do not promote beyond internal testing yet** because:
  - Play Billing products (`pro_monthly`, `pro_yearly`, `pro_lifetime`) still do not exist in Play Console
  - Play-installed QA for real auth, backup, and billing has not happened yet
  - `android:allowBackup="true"` remains unresolved relative to the app’s explicit Drive backup model
  - release/security/store docs are stale in places and could mislead Play Console setup
- The current AAB is therefore:
  - **build-valid**
  - **signed with the expected upload key**
  - **acceptable as the first internal-testing upload candidate**
- Recommended next order from the audit:
  1. create Play Billing products
  2. upload the signed AAB to internal testing
  3. run Play-installed QA for sign-in, Drive backup/restore, and billing flows
  4. fix any issues found before broader release
- Follow-up on the same day added a stricter pre-upload gate:
  - [docs/pre-upload-go-no-go-checklist-2026-05-17.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/pre-upload-go-no-go-checklist-2026-05-17.md)
- Security/secrets audit highlights from that follow-up:
  - no tracked secret-like files were found in git during the audit
  - `app/google-services.json` exists locally and is correctly ignored by `.gitignore`
  - built artifacts (`*.aab`, `*.apk`) are correctly ignored
  - **important gap:** `keystore.properties` is not currently ignored, which becomes a real leak risk once release signing is wired
  - no obvious committed tokens/keys were found via regex-based source/doc scanning; matches were explanatory mentions only
- Updated recommendation order after the security pass:
  1. add `keystore.properties` to `.gitignore`
  2. fix release signing
  3. rebuild a fresh signed AAB
  4. create Play products
  5. upload to internal testing
  6. run Play-installed QA
- Follow-up implementation completed:
  - `.gitignore` now ignores `keystore.properties`
  - `keystore.properties.example` was added with placeholders only
  - `app/build.gradle.kts` no longer points release builds at debug signing
  - release signing now reads either ignored local `keystore.properties` values or environment variables:
    - `PHONE_DOWN_STORE_FILE`
    - `PHONE_DOWN_STORE_PASSWORD`
    - `PHONE_DOWN_KEY_ALIAS`
    - `PHONE_DOWN_KEY_PASSWORD`
  - release bundle/assemble/sign tasks now fail fast if those signing values are missing, which prevents accidentally uploading an unsigned or debug-signed artifact
  - the stale unsigned/generated `app-release.aab` was removed from `app/build/outputs/bundle/release/`
- Remaining blocker before meaningful internal billing QA:
  - create Play Console products for `pro_monthly`, `pro_yearly`, and `pro_lifetime`

## Update - 2026-05-16 (Sprint 16.4 In Progress)

- Ignore older sections below that still describe Sprint 16.4 as "planning only" or describe icon-only local changes.
- Current truth:
  - Sprint 16.3 real Drive backup/restore is implemented and manually QA'd on device.
  - Sprint 16.4 real Play Billing is now **in implementation**, not just planned.
  - Local verification currently passing for the new billing slice:
    - `./gradlew --no-daemon --no-configuration-cache :app:assembleDebug`
    - `./gradlew --no-daemon --no-configuration-cache :app:testDebugUnitTest`
    - `git diff --check`
- The current dirty working tree is billing-sprint work, not launcher-icon work:
  - modified: `app/src/main/java/phonedown/app/MainActivity.kt`
  - modified: `app/src/main/java/phonedown/app/pro/ProRoute.kt`
  - modified: `app/src/main/java/phonedown/app/pro/ProViewModel.kt`
  - modified: `app/src/main/java/phonedown/app/runtime/AppRuntimeModule.kt`
  - modified: `app/src/test/java/phonedown/app/account/AccountViewModelTest.kt`
  - modified: `app/src/test/java/phonedown/app/pro/ProViewModelTest.kt`
  - modified: `app/src/test/java/phonedown/app/settings/SettingsViewModelTest.kt`
  - modified: `core/billing/build.gradle.kts`
  - modified: `core/billing/src/main/kotlin/phonedown/core/billing/FakeBillingRepository.kt`
  - modified: `core/model/src/main/kotlin/phonedown/core/model/repository/BillingRepository.kt`
  - modified: `feature/pro/src/main/kotlin/phonedown/feature/pro/ProScreen.kt`
  - modified: `gradle/libs.versions.toml`
  - modified: `phase-16-android-production-readiness-plan.md`
  - modified: `phase-16-sprint-16-4-real-play-billing-plan.md`
  - modified: `v1-implementation-plan.md`
  - untracked: `app/src/main/java/phonedown/app/runtime/ForegroundActivityProvider.kt`
  - untracked: `core/billing/src/main/kotlin/phonedown/core/billing/BillingActivityProvider.kt`
  - untracked: `core/billing/src/main/kotlin/phonedown/core/billing/RealBillingRepository.kt`
  - untracked: `core/model/src/main/kotlin/phonedown/core/model/BillingEvent.kt`
  - untracked: `core/model/src/main/kotlin/phonedown/core/model/ProCatalog.kt`
- Sprint 16.4 implementation completed so far:
  - real Play Billing dependency wiring
  - `BillingRepository` contract extended with event flow and `syncPurchases()`
  - new `BillingEvent` and `ProCatalog` models
  - real `RealBillingRepository`
  - runtime DI swap from fake billing to real billing
  - startup entitlement sync in `MainActivity`
  - new `ForegroundActivityProvider` for purchase launch context
  - paywall/viewmodel upgraded to honest loading/restore/purchase/subscription-management states
  - app billing tests updated and passing
- Immediate next step from this state:
  1. create Play Console billing products and tester setup for `pro_monthly`, `pro_yearly`, and `pro_lifetime`
  2. install a Play-distributed or tester-eligible build
  3. run real-device purchase/restore/cancel/recovery QA

## 1. Goal

- Build Phone Down into a fully production-ready Android Play Store app, not just a feature-complete app with fake external integrations.
- Continue `Phase 16 - Android Production Readiness`, which covers real Google Sign-In, real Google Drive backup/restore, real Play Billing, release signing, Crashlytics, Play policy readiness, and final QA.
- Preserve the project’s strict workflow: clarify if needed, plan in Markdown, get approval, implement, verify, then report honestly.
- The current immediate objective is to continue from Sprint `16.2` after the real Google Sign-In implementation is committed and a follow-up launcher-icon correction is now in local, uncommitted progress.
- The next meaningful product milestone is end-to-end device QA of real Google Sign-In, then moving into the next approved production-readiness sprint.
- Real-device QA for the current Google Sign-In flow has been executed successfully on May 16, 2026, and launcher-icon QA has now been re-run after a visual mismatch was reported by the user.

## 2. Context The Next Agent Must Know

- Read `AGENTS.md` first and follow it strictly.
- Project rules that matter most here:
  - ask clarifying questions before writing any new phase/sprint planning `.md` file
  - do not implement a new phase/sprint until the user approves the plan
  - update relevant Markdown docs during meaningful progress
  - prefer local Android builds and local QA over cloud-first workflows
  - do not commit unless the user explicitly asks
- Teaching mode is expected: explain what you are doing, why it matters, and the tradeoffs.
- Architecture:
  - `:app` owns runtime orchestration, Activities, DI, and platform-specific coordination
  - `:feature:*` modules stay UI-focused
  - `:core:model` owns platform-neutral contracts/models
  - `:core:auth` now has both fake and real auth-facing repository behavior
  - `:domain:session` and `:domain:insights` are already real and tested
- Important Phase 16 decisions already made:
  - use a dedicated Google Cloud/Firebase project for Phone Down
  - use Google Drive `appDataFolder` for backup
  - implement once-daily auto-backup with WorkManager
  - keep Room unencrypted for V1
  - add Firebase Crashlytics with minimal disclosed diagnostics
  - keep the 24-hour entitlement cache
  - India pricing recommendation: `INR 99/month`, `INR 799/year`, `INR 1,999 lifetime`
- Console/setup state already established:
  - Google Cloud project: `phone-down`
  - project ID: `phone-down-496414`
  - Firebase Android app exists for package `phonedown.app`
  - Drive API enabled
  - OAuth branding/audience/scopes/test-user setup completed
  - Android debug OAuth client created: `Phone Down Android Debug`
  - Web OAuth client created so `default_web_client_id` is available to Android runtime config
- Security note:
  - a downloaded `client_secret_...json` artifact was explicitly **not** placed in the repo
  - `google-services.json` is present locally and ignored by git

## 3. Work Completed

- Phase 15 trust fixes were completed earlier and are already in repo history:
  - real Pause/Add Time end to end
  - real full-replace restore
  - call permission education flow
  - notification tap-to-Focus routing
  - dead Settings row cleanup
  - consistent “today” metrics semantics
- Phase 16 planning and setup docs were created and expanded:
  - [phase-16-android-production-readiness-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-android-production-readiness-plan.md)
  - [phase-16-sprint-16-2-real-google-sign-in-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-sprint-16-2-real-google-sign-in-plan.md)
  - [docs/play-console-release-guide.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/play-console-release-guide.md)
  - [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md)
  - [docs/phase-16-manual-qa.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-manual-qa.md)
  - [docs/architecture-guide.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/architecture-guide.md)
- Sprint 16.2 implementation is committed in `HEAD` (`3c8cb1e`):
  - added real Google Sign-In dependencies in Gradle
  - added [GoogleSignInCoordinator.kt](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/java/phonedown/app/account/GoogleSignInCoordinator.kt)
  - added [DataStoreAuthRepository.kt](/Users/ayushjaipuriar/Documents/GitHub/phone-down/core/auth/src/main/kotlin/phonedown/core/auth/DataStoreAuthRepository.kt)
  - added [GoogleAccount.kt](/Users/ayushjaipuriar/Documents/GitHub/phone-down/core/model/src/main/kotlin/phonedown/core/model/GoogleAccount.kt)
  - updated `AuthRepository`, `AccountState`, `AccountRoute`, `AccountViewModel`, `AccountScreen`, `FakeAuthRepository`, and `AppRuntimeModule`
  - updated related account/settings tests
- UI/branding work that landed in the same latest commit:
  - productionized launcher icon assets across densities
  - added adaptive icon XMLs:
    - [ic_launcher.xml](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
    - [ic_launcher_round.xml](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
  - updated [AndroidManifest.xml](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/AndroidManifest.xml) to explicitly use launcher and round icons
  - included Focus/Insights/Settings typography/UI polish updates and snapshot baseline updates
- Commands inspected in this handoff pass:
  - `git status`
  - `git status --short --branch`
  - `git log --oneline --decorate -n 8`
  - `git show --stat --summary --name-only HEAD`
  - `sed -n ... docs/agent-handoff.md`
  - `sed -n ... phase-16-sprint-16-2-real-google-sign-in-plan.md`
- Important current outputs:
  - `git status`: working tree clean
  - `HEAD`: `3c8cb1e (HEAD -> main, origin/main, origin/HEAD) feat: Implement real Google Sign-In flow and UI updates`
- Verification known from recent implementation history:
  - Sprint 16.2 code was previously verified with targeted Gradle unit/build commands before commit
  - a later attempted `:app:assembleDebug` during icon packaging had once hit a build-logic parser error (`:build-logic:convention:compilePluginsBlocks`), but this was re-run successfully on May 16, 2026
- Fresh QA performed on May 16, 2026 against connected Android device `192.168.1.6:35045`:
  - `./gradlew --no-configuration-cache :app:assembleDebug` succeeded
  - installed `app/build/outputs/apk/debug/app-debug.apk` successfully via `adb install -r`
  - launched `phonedown.app/.MainActivity`
  - verified app opened correctly on Focus tab
  - verified Settings -> Account -> Google Account flow
  - verified real Google chooser appeared with `Phone Down` branding
  - verified consent screen appeared
  - verified successful return into `Phone Down` signed-in Account screen with account state rendered
  - earlier launcher/app-drawer QA showed a packaged icon that did not visually match the user-approved monogram reference closely enough
- Follow-up icon correction performed locally on May 16, 2026:
  - generated a new launcher master matching the user-approved dark monogram reference more closely
  - repackaged all density-specific assets:
    - `app/src/main/res/mipmap-*/ic_launcher.png`
    - `app/src/main/res/mipmap-*/ic_launcher_round.png`
    - `app/src/main/res/mipmap-*/ic_launcher_foreground.png`
    - `app/src/main/res/mipmap-*/ic_launcher_background.png`
  - re-ran `./gradlew --no-configuration-cache :app:assembleDebug` successfully after the asset swap
  - reinstalled on device and verified in launcher context that the shown icon now matches the intended monogram family much more closely

## 4. Current Workspace State

- Branch: `main`
- Remote state: branch is up to date with `origin/main`
- `git status`: dirty working tree with local icon-asset replacements plus this handoff update
- Modified files:
  - `app/src/main/res/mipmap-hdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-hdpi/ic_launcher_background.png`
  - `app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png`
  - `app/src/main/res/mipmap-hdpi/ic_launcher_round.png`
  - `app/src/main/res/mipmap-mdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-mdpi/ic_launcher_background.png`
  - `app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png`
  - `app/src/main/res/mipmap-mdpi/ic_launcher_round.png`
  - `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xhdpi/ic_launcher_background.png`
  - `app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png`
  - `app/src/main/res/mipmap-xhdpi/ic_launcher_round.png`
  - `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xxhdpi/ic_launcher_background.png`
  - `app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png`
  - `app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png`
  - `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xxxhdpi/ic_launcher_background.png`
  - `app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png`
  - `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png`
  - `docs/agent-handoff.md`
- Untracked files: none
- Staged files: none
- Latest commit at handoff time:
  - `3c8cb1e feat: Implement real Google Sign-In flow and UI updates`
- Treat the current uncommitted mipmap/icon changes as intentional in-progress work and do not overwrite or revert them unless explicitly asked.
- Sensitive-file posture:
  - no suspicious tracked secrets were visible in this inspection
  - `google-services.json` remains local/ignored
  - client-secret JSON files are expected to stay out of git and out of docs

## 5. Decisions And Rationale

- Real Google Sign-In uses Android Credential Manager plus a small app-layer coordinator:
  - keeps sign-in UI/platform handling in `:app`
  - keeps `:core:model` platform-neutral
  - avoids stuffing Android-specific auth flows directly into feature UI
- `DataStoreAuthRepository` persists minimal account display state instead of raw tokens:
  - enough for signed-in UI continuity
  - lower risk than token persistence
  - better fit for V1 scope
- Fake auth was preserved for tests:
  - keeps unit tests lightweight and deterministic
  - avoids coupling all test paths to Google runtime services
- Web OAuth client was required even for Android Credential Manager setup:
  - needed so Android gets `default_web_client_id` from Firebase/Google services config
- The selected final launcher icon direction was the top-left refined concept from the generated set:
  - strongest balance of distinctiveness, clarity at small sizes, and premium feel
- Adaptive icon packaging was added alongside legacy launcher sizes:
  - modern Android launchers mask icons differently
  - adaptive assets reduce cropping/masking inconsistencies across devices
- The initially committed packaged icon still drifted visually from the user's chosen reference on-device:
  - rather than assuming the generated concept had landed correctly, the follow-up fix replaced the actual rasterized launcher assets and revalidated on hardware
  - this is a good reminder that launcher icons need device-level QA because OEM masks, background treatments, and density-specific exports can change the perceived result

## 6. Known Issues / Blockers

- The old `docs/agent-handoff.md` was stale before this rewrite; it incorrectly described a clean tree and earlier icon-validation state. This new file replaces that stale picture.
- Real Google Sign-In/device QA is now working, but one notable follow-up surfaced in logs:
  - `Firebase Installations` emitted `403 PERMISSION_DENIED` / `API_KEY_SERVICE_BLOCKED` warnings during device QA
  - sign-in still succeeded, so this is not the auth blocker
  - this likely indicates Firebase-side API restriction/config cleanup still needed before broader production readiness
- The corrected launcher icon is verified on-device, but the icon asset changes are not committed yet.
- Follow-up investigation on May 16, 2026 refined the warning diagnosis:
  - `app` does not currently include any explicit Firebase runtime SDK dependency
  - `dependencyInsight` for `firebase-installations` and `com.google.firebase` on `:app:debugRuntimeClasspath` returned no matches
  - re-running the signed-out -> sign-in flow reproduced Google Play Services / auth noise for `com.android.vending` and `oauth2:https://www.googleapis.com/auth/googleplay`
  - the previously remembered `Firebase Installations` `API_KEY_SERVICE_BLOCKED` warning did not reproduce in the controlled retest
  - practical conclusion: this is not a current blocker for Sprint 16.3, but Firebase console/API restrictions should still be rechecked once actual Firebase runtime products like Crashlytics are added
- Sprint 16.3 is now implemented in code:
  - [phase-16-sprint-16-3-real-drive-backup-restore-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-sprint-16-3-real-drive-backup-restore-plan.md)
  - real Drive authorization layer, `DriveAppDataClient`, `DriveBackupRepository`, WorkManager auto-backup scheduling/worker, and Settings/Account UI wiring are all in place
- Sprint 16.3 manual device QA on May 16, 2026 is now partially completed and was materially useful:
  - manual backup initially failed with a permission-style network error
  - root causes found and fixed:
    - missing `android.permission.INTERNET` in `app/src/main/AndroidManifest.xml`
    - lost pending account email across the Drive authorization resolution flow in `GoogleDriveAuthorizationManager`
    - placeholder certificate pins in `app/src/main/res/xml/network_security_config.xml` that caused `SSLHandshakeException: Pin verification failed` against `www.googleapis.com`
  - after fixes, real manual backup succeeded on device
  - the Settings row updated to `Last backup: ...`
  - the once-daily `Auto Backup` toggle appeared and was manually toggled off as a restore test mutation
  - restore succeeded from the Account screen with `Restore Complete`
  - the restored settings reverted `Auto Backup` back to enabled, proving full settings replacement worked end to end
- Sprint 16.3 follow-up QA and fixes on May 16, 2026 closed the explicit empty-state gap too:
  - the current hidden Drive backup was deleted through the real `Delete All Data` + `Also delete cloud backup` flow
  - re-signing into the same Google account and restoring then reached the expected empty-state body: `No backup found for this account.`
  - along the way, another trust bug was fixed:
    - `deleteBackup()` no longer returns a lossy `Boolean`; it now uses `DeleteBackupResult`
    - the Settings delete flow now pre-authorizes Drive access, attempts cloud deletion before wiping local data, and surfaces a real failure instead of silently pretending the cloud backup was deleted
  - a final UX polish split `RestoreState.NoBackupFound` from generic restore failures so the dialog title can say `No Backup Found` instead of `Restore Failed`
- Phase 16 is not done:
  - real Play Billing setup/integration still pending
  - Crashlytics/release-signing/final Play readiness still pending
  - optional deeper transport/integration coverage is still worthwhile, but the core device QA paths for Sprint 16.3 are now covered
- Sprint 16.4 planning is now drafted and waiting for user review:
  - [phase-16-sprint-16-4-real-play-billing-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-sprint-16-4-real-play-billing-plan.md)
  - confirmed scope includes monthly/yearly/lifetime products, restore purchases, entitlement activation, paywall/listing copy refinement, final price-display QA, and cancellation/recovery QA
  - confirmed assumptions:
    - product IDs: `pro_monthly`, `pro_yearly`, `pro_lifetime`
    - Play Billing products are not yet created in Play Console
    - 24-hour entitlement cache remains the resilience rule
    - Play Billing plus local cache remains the entitlement authority, not Drive backup
- A local build-tooling workaround is currently present:
  - `build-logic/convention/build.gradle.kts` uses explicit plugin coordinates instead of version-catalog aliases because the local Gradle cache/tooling state broke accessor resolution during Sprint 16.3 implementation
  - the repo builds successfully with this workaround, but it should be revisited later if we want to restore the original catalog-based build-logic style
- Play Console/Play App Signing/Billing product state should be revalidated live before coding the next sprint, because some of that setup can drift outside the repo.

## 7. Exact Next Steps

1. Reconfirm repo/tooling state locally before new work:
   - run `git status --short --branch`
   - run `git log --oneline --decorate -n 3`
   - read [docs/agent-handoff.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/agent-handoff.md), [phase-16-sprint-16-2-real-google-sign-in-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-sprint-16-2-real-google-sign-in-plan.md), and [phase-16-sprint-16-3-real-drive-backup-restore-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-sprint-16-3-real-drive-backup-restore-plan.md)
2. Review the uncommitted launcher-icon asset diff before any other work:
   - inspect `git diff -- app/src/main/res/mipmap-* docs/agent-handoff.md`
   - keep these icon updates unless the user explicitly asks to regenerate again
3. Close Sprint 16.3 documentation and decide whether any optional extra QA is worth the time:
   - optionally inspect/capture WorkManager state for once-daily auto-backup scheduling evidence
   - otherwise move to Sprint 16.4 review and approval
4. Before any Sprint 16.4 implementation begins:
   - read [phase-16-sprint-16-4-real-play-billing-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-sprint-16-4-real-play-billing-plan.md)
   - confirm or revise the billing sprint plan with the user
   - do not start coding until the user approves the sprint plan
4. Re-run the verified local automation path before or after device QA if needed:
   - `./gradlew --no-daemon --no-configuration-cache :app:assembleDebug`
   - `./gradlew --no-daemon --no-configuration-cache :core:backup:testDebugUnitTest :app:testDebugUnitTest :feature:settings:testDebugUnitTest :feature:account:testDebugUnitTest`
5. After Sprint 16.3 QA, decide whether to:
   - tune backup UX/copy based on findings
   - or move into the next Phase 16 sprint, most likely real Play Billing

## 8. Suggested Prompt For The Next Agent

```text
Continue work in the Phone Down project. First, read `AGENTS.md`, `docs/agent-handoff.md`, and inspect the actual repo state with `git status --short --branch`, `git log --oneline --decorate -n 3`, and any relevant diffs. Treat the repo as source of truth. Do not commit anything unless I explicitly ask.

Current state:
- Branch `main` is up to date with `origin/main`, but the working tree is currently dirty with uncommitted launcher-icon asset replacements and an updated handoff doc.
- Latest commit is `3c8cb1e feat: Implement real Google Sign-In flow and UI updates`.
- Real Google Sign-In wiring for Sprint 16.2 is already implemented and committed.
- The originally committed launcher icon was visually corrected afterward; the corrected mipmap assets are local and uncommitted.
- Phase 16 is still in progress: Billing, Crashlytics, signing, final Play readiness, and Sprint 16.3 manual QA remain.
- Sprint 16.3 code is implemented and verified by local build/unit-test passes.
- `:app:assembleDebug` was re-run successfully on May 16, 2026.
- Real Google Sign-In was manually validated on a connected Android device.
- The corrected launcher icon was also revalidated on-device after the asset swap and now matches the chosen monogram direction much more closely.
- A previously observed warning was re-investigated:
  - controlled retest did not reproduce a Firebase Installations error
  - reproducible log noise instead came from Google Play Services / `com.android.vending` auth token fetches, while Credential Manager sign-in for `phonedown.app` still succeeded
  - treat Firebase Installations as a low-confidence/non-blocking concern until actual Firebase runtime SDKs are introduced
- Sprint 16.3 specifics now in repo:
  - `GoogleDriveAuthorizationManager` separates account sign-in from Drive scope authorization
  - `DriveAppDataClient` + `DriveBackupRepository` replace fake backup transport in normal runtime DI
  - `AutoBackupScheduler` + `AutoBackupWorker` implement once-daily Pro/sign-in-gated auto-backup behavior
  - targeted verification passed: `:app:assembleDebug`, `:core:backup:testDebugUnitTest`, `:app:testDebugUnitTest`, `:feature:settings:testDebugUnitTest`, `:feature:account:testDebugUnitTest`

Your first tasks:
1. Read `docs/agent-handoff.md`, `phase-16-android-production-readiness-plan.md`, `phase-16-sprint-16-2-real-google-sign-in-plan.md`, and `phase-16-sprint-16-3-real-drive-backup-restore-plan.md`.
2. Reconfirm the dirty repo state and inspect the uncommitted mipmap/icon diff before touching anything else.
3. Preserve the current icon asset changes unless the user explicitly asks to regenerate again.
4. Treat Sprint 16.3 implementation as already in place; focus first on manual device QA and any fixes it reveals.
5. Keep the build-logic workaround in mind before “cleaning up” Gradle files; it was added because local version-catalog accessor generation broke during implementation.
6. Update `docs/agent-handoff.md` with whatever you learn before moving deeper into Phase 16.

Preserve teaching mode, explain why each step matters, and ask clarifying questions before drafting any new sprint/phase plan.
```
