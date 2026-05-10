# Product QA Audit - 2026-05-10

This audit reviews the current Phone Down repository from a product manager and QA tester perspective. It focuses on user-visible correctness, trust, release readiness, and small papercuts that would affect perceived quality.

## Snapshot

- Workspace was clean before this audit document was added.
- Core focus flow has previously passed real-device QA for face-down progression, elapsed-time reporting, notification end action, and dimming on RMX3686.
- The main remaining risks are not visual polish. They are trust gaps: flows that appear real but are still fake, UI controls that do not affect domain behavior, analytics inconsistencies, and release-readiness placeholders.

## P0 - Must Fix Before Any Serious Beta Or Release

### 1. Backup Restore Reports Success Without Restoring Data

**User symptom:** A user taps restore, sees a success dialog, but their sessions/settings do not actually come back.

**Evidence:**
- `app/src/main/java/phonedown/app/account/AccountViewModel.kt`
  - `restoreBackup()` calls `backupRepository.restoreBackup()` and only maps the result to UI state.
  - It never calls `SessionRepository` or `SettingsRepository` to replace local data.
- `core/backup/src/main/kotlin/phonedown/core/backup/FakeBackupRepository.kt`
  - `restoreBackup()` deserializes backup JSON and counts restored sessions.
  - It discards the sessions, penalty events, and settings after mapping.

**Why this matters:** Restore is a high-trust feature. A false success state is worse than no restore feature because it makes users believe recovery worked.

**Recommended fix:**
- Change `BackupRepository.restoreBackup()` to return restored domain payload, not only counts.
- Move full-replace restore orchestration into an app/domain use case:
  - clear penalty events
  - clear sessions
  - insert restored sessions
  - insert restored penalty events
  - restore settings
  - update last restore/backup metadata
- Add unit tests for successful restore, unsupported schema, no backup, and partial failure rollback.

### 2. Pause And Add Time Are Visible But Not Real

**User symptom:** The Focus UI offers Pause and Add Time. Pause only changes local UI state, and Add Time only changes displayed remaining time. The actual session engine keeps its own timing rules.

**Evidence:**
- `app/src/main/java/phonedown/app/focus/FocusViewModel.kt`
  - `PauseClicked` sets `pausedByUser = true`.
  - `ResumeClicked` sets `pausedByUser = false`.
  - `AddTimeSelected` only increments `localViewState.addTimeSeconds`.
- `domain/session` has no `ManualPauseRequested`, `ManualResumeRequested`, or `AddTimeRequested` input.

**Why this matters:** This is a direct user-trust problem. The app can show a paused or extended session while the persistent domain session does not reflect that.

**Recommended fix options:**
- Short-term: hide Pause and Add Time from V1 until the engine supports them.
- Better: add domain inputs for manual pause/resume and add-time; persist manual pause penalty/clean-status semantics; update tests.

**Recommendation:** Hide them for the next stabilization pass unless you explicitly want to implement full domain behavior now.

### 3. Notification Tap May Not Navigate Existing App Back To Focus

**User symptom:** User taps the foreground service notification while already inside the app on Insights/Settings and may not be routed back to Focus.

**Evidence:**
- `MainActivity.kt` computes `initialRoute` from `intent?.getBooleanExtra(...)`.
- `PhoneDownApp` creates a remembered `NavController`; `initialRoute` only configures `NavHost` start destination.
- There is no `onNewIntent()` handling or navigation command for a later notification tap.

**Why this matters:** During active sessions, the notification is the user’s lifeline back to the session. It should be deterministic.

**Recommended fix:**
- Add `onNewIntent()` in `MainActivity`.
- Expose a navigation event into `PhoneDownApp`, or route notification opens through a shared state/event channel.
- Add an automated test around notification intent handling if feasible.

### 4. Call Interruption Feature Has No Permission Flow

**User symptom:** Calls are supposed to pause sessions, but the app never requests `READ_PHONE_STATE`. On most devices, call monitoring silently stays inactive.

**Evidence:**
- Manifest declares `READ_PHONE_STATE`.
- `AndroidCallInterruptionMonitor.start()` returns early if permission is missing.
- `MainActivity` only requests notification permission.

**Why this matters:** The product decision says calls are a separate non-broken interruption type. Today that is only true if the permission somehow exists.

**Recommended fix:**
- Add a calm permission education step for phone-state access.
- Request permission only when needed and make the value clear: "Pause focus during calls."
- If denied, show call interruption as unavailable rather than silently failing.

### 5. Real Auth/Billing/Backup Are Still Fake

**User symptom:** Google sign-in, Pro purchase, restore purchases, and backup appear real but are in-memory/simulated.

**Evidence:**
- `FakeAuthRepository` returns `Test User` and fake token.
- `FakeBillingRepository` returns hardcoded products and simulated purchases.
- `FakeBackupRepository` stores JSON in memory.

**Why this matters:** This is fine for development, but not for a credible beta unless clearly labeled. It also means backup disappears after process death.

**Recommended fix:**
- Decide if the next milestone is "internal demo" or "external beta."
- For internal demo: visually mark account/billing/backup as demo/fake or hide the flows.
- For beta/release: integrate real Google Sign-In, Play Billing, and Drive backup before shipping.

## P1 - High-Impact Trust And Quality Fixes

### 6. Focus Today Metrics Can Disagree With Insights

**User symptom:** The Focus tab and Insights tab can show different session counts for the same day.

**Evidence:**
- `FocusViewModel` computes today counts inline.
- `GetTodayInsightsUseCase.summarize()` has its own filtering logic.
- Focus counts `result != null && result != Broken`; Insights excludes `Abandoned` but includes other non-abandoned result types.
- `FocusViewModel` captures `startOfDayMillis` once at ViewModel creation, so it can go stale after midnight.

**Recommended fix:**
- Reuse `GetTodayInsightsUseCase` or shared summary logic for Focus tab metrics.
- Make the day window reactive to clock/date changes or refresh on resume.
- Define product semantics for what "Sessions" means.

### 7. End Session Classification Copy Is Too Generic

**User symptom:** Early ending always says "Current progress will be saved as partial" before confirmation, even when the result may become invalidated or broken.

**Evidence:**
- `EndConfirmationSheet` always uses the same copy.
- Domain classification has richer result states.

**Recommended fix:**
- Show contextual copy using current progress percentage:
  - under minimum threshold: "This will not count as a focus session."
  - partial threshold: "This will save as an early-ended session."
  - completed state: do not show early-end copy.

### 8. Session Complete Time Breakdown Can Mislead

**User symptom:** Completion screen displays `Total Time` as selected duration, not actual total required duration with penalties or actual elapsed time.

**Evidence:**
- `SessionCompleteContent` calculates `Total Time` from `selectedDurationSeconds`.
- Penalty time is shown separately, but total does not include it.

**Recommended fix:**
- Rename rows to avoid ambiguity, or compute:
  - Focus Time
  - Penalty Added
  - Target Time
  - Actual Elapsed
- For completed sessions, show required duration including penalties.

### 9. Insights Refresh Is One-Shot

**User symptom:** User completes a session, switches to Insights, and may not see fresh values if the ViewModel already loaded. There is an `onRefresh` callback but no UI gesture uses it.

**Evidence:**
- `InsightsViewModel.refresh()` snapshots multiple use cases once.
- `InsightsContent` accepts `onRefresh` but does not use it.

**Recommended fix:**
- Observe repository flows or refresh when the tab resumes.
- Add pull-to-refresh if keeping explicit refresh.

### 10. Settings Has Dead Or Misleading Rows

**User symptom:** Several rows look actionable but do nothing or do the wrong thing.

**Evidence:**
- Terms and Support default to empty callbacks.
- Export Data triggers backup for Pro users.
- Auto Backup displays On/Off but is not toggleable.
- Start Delay displays "3 seconds" but is not configurable.
- Duration Presets displays values but cannot be edited.

**Recommended fix:**
- Either wire the rows or visibly make them non-actionable.
- For V1, prefer smaller honest settings over rich dead settings.

## P2 - Release Readiness And Platform Papercuts

### 11. Android Auto Backup Conflicts With Privacy Positioning

**Evidence:** `android:allowBackup="true"` in the manifest.

**Risk:** OS-level backups may copy local focus data even if the product says cloud backup is opt-in.

**Recommended fix:** Either set `allowBackup=false`, or define explicit backup rules that exclude sensitive/local session data.

### 12. Certificate Pinning And "Encrypted" DataStore Are Placeholders

**Evidence:**
- Placeholder pins in `CertificatePinningConfig`.
- Placeholder pin in `network_security_config.xml`.
- `EncryptedDataStore` uses normal preferences DataStore.

**Recommended fix:** Before real service integration, either remove placeholder pinning from release builds or replace it with real pins and a rotation strategy. Use real encrypted storage for tokens if tokens are ever persisted.

### 13. Billing Fake Semantics Are Too Loose For QA

**Evidence:** Monthly and yearly fake purchases both grant 365-day entitlement.

**Risk:** QA cannot validate subscription expiry, restore, or entitlement edge cases.

**Recommended fix:** Even before real Play Billing, fake monthly should expire in about 30 days and yearly in about 365 days.

### 14. Call QA Is Still Device-Setup Blocked

**Evidence:** Prior RMX3686 QA could not test PSTN call interruption because the device had no SIM.

**Recommended fix:** Use a SIM-enabled device or decide whether VoIP/audio-focus interruption should also count as call-like pause.

### 15. Boot Recovery May Be Too Quiet

**Evidence:** Boot receiver classifies recoverable sessions but does not notify the user that an active session was classified as broken/abandoned.

**Recommended fix:** On next app open, show a calm recovery banner: "Your previous session ended when the device restarted."

## P3 - UX Papercuts And Nice-To-Haves

### 16. Focus Ring Shows "Remaining" During Active While Mock Shows Focus Time

Current implementation shows remaining time as the primary active number. The mock variants have used both patterns in conversation. Product should decide one and keep it consistent across ring, progress summary, and notification.

### 17. Ready-To-Focus Copy Still Says "Tap Start Focus"

After the user has already tapped Start, the ready screen repeats "Tap Start Focus" as step 1. This can feel chronologically odd.

Suggested copy:
- "Place your phone face down"
- "Keep it still"
- "Focus begins after 3 seconds"

### 18. Debug-Like Progress Card May Be Too Operational

The `Focused / Remaining / State` card is useful for QA, but may feel a little diagnostic for a premium focus app.

Suggestion:
- Keep it during active QA builds.
- In production UI, make it subtler or remove the explicit `State` metric.

### 19. Paywall Needs Purchase Loading/Error States

`ProViewModel.purchase()` launches the fake flow without loading/error/success UI state. Users can tap repeatedly and get no clear progress.

### 20. Account Sign-In Needs Loading/Error States

Fake sign-in has delay but the Account screen has no signing-in state, so the button can feel unresponsive.

### 21. Empty Insights State Could Be More Actionable

Current empty copy is clear but passive. Add a single action or hint that routes back to Focus.

### 22. Privacy/Support Contact Placeholders Need Real Values

Docs still include placeholder support/security addresses. That is fine internally but should not ship.

### 23. Version Is Hardcoded

Settings shows `0.1.0` rather than reading from `BuildConfig.VERSION_NAME`.

### 24. Screen Brightness Value Should Be Device-Tuned

`0.02f` worked on RMX3686, but should be tested across OLED/LCD devices and battery saver modes.

### 25. Sensor State Needs Production Diagnostics Path

The temporary UI debug text was removed, which is correct. But support/debug builds still need a non-user-facing diagnostic path for "why did this not start?"

## Recommended Implementation Order

1. **Trust hotfix pack**
   - Hide or properly wire Pause/Add Time.
   - Fix restore so success means actual local restore.
   - Fix notification tap routing to Focus.
   - Fix call permission flow or explicitly mark it unavailable.

2. **Analytics consistency pack**
   - Unify Focus and Insights "today" metrics.
   - Decide session counting semantics.
   - Refresh Insights reactively.

3. **Settings cleanup pack**
   - Remove or wire dead Settings rows.
   - Fix Export Data behavior.
   - Read version from BuildConfig.

4. **Release readiness pack**
   - Decide Android auto-backup behavior.
   - Remove/replace placeholder certificate pins.
   - Replace fake auth/billing/backup or mark them internal-only.

5. **Polish pack**
   - Tune ready-screen copy.
   - Decide active timer primary number.
   - Add loading/error states for Paywall and Account.
   - Add user-friendly recovery messaging.

## Phase 15 Implementation Status

Implemented on 2026-05-10:

- Pause and Add Time now route through the session engine/runtime coordinator instead of local-only UI state.
- Restore now fetches backup payloads and fully replaces local Room session/penalty data, then restores settings.
- Notification taps emit a warm-start navigation event and route back to Focus.
- Call-state permission is requested only after a Settings education dialog.
- Dead Settings rows for Terms, Support, Export Data, and Auto Backup were removed.
- Focus today's metrics now use the same summary helper as Insights.

Verification:

- Passed: `./gradlew --no-configuration-cache :domain:session:test :domain:insights:test :core:backup:testDebugUnitTest :core:database:testDebugUnitTest :app:testDebugUnitTest :feature:focus:testDebugUnitTest :feature:settings:testDebugUnitTest :app:assembleDebug`
- Passed: `./gradlew --no-configuration-cache :feature:settings:verifyPaparazziDebug :feature:focus:verifyPaparazziDebug`
- Passed: `git diff --check`
- Blocked: `./gradlew --no-configuration-cache :feature:settings:connectedDebugAndroidTest` because Gradle reported no connected devices.
- Known quality-gate limitation: `./scripts/check.sh` still fails on existing ktlint policy disagreements and PascalCase Compose naming conventions documented earlier in the project.
