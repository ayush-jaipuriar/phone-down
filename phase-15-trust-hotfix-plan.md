# Phase 15 - Trust Hotfix Implementation Plan

## Status

- Planning status: Approved
- Implementation status: Implemented
- Approval required before implementation: Completed
- Source audit: `docs/product-qa-audit-2026-05-10.md`

## Purpose

Phase 15 is a release-trust stabilization pass. The goal is to remove the biggest gaps between what the app currently promises in the UI and what it actually does end to end. These are not broad new product features; they are fixes for flows where a user could reasonably feel the app is misleading, incomplete, or unreliable.

This phase focuses on:

- Making Pause and Add Time real session-engine behavior instead of local UI-only behavior.
- Making Restore actually replace local sessions, penalties, and settings from backup data.
- Adding an educational call-permission flow so call interruptions can work without surprising users.
- Ensuring notification taps always return users to the Focus tab, including when the app is already running.
- Hiding or disabling settings rows that are not backed by real behavior.
- Making today's Focus summary use the same session-counting semantics as Insights.

## Confirmed Product Decisions

- Pause and Add Time must be real, full end-to-end behavior.
- Restore should be a full replacement, not a merge.
- Call-state permission should be requested, but only after educating the user because it is sensitive.
- Notification taps should always route to Focus.
- Settings rows that are not real should be hidden or disabled.
- Session count semantics should count completed, ended early, invalidated, and broken sessions, while excluding abandoned and pre-start waiting sessions.

## Out Of Scope

- Real Google Drive API integration. This phase makes the local restore path real using the existing backup abstraction; production Drive remains a later integration.
- Real Play Billing or Google Sign-In replacement.
- Auto-backup scheduling.
- Subscription expiry edge cases.
- New visual redesign beyond removing misleading/dead controls.
- New analytics dashboards beyond consistent existing metrics.

## Current Evidence From Code Review

### Pause And Add Time Are UI-Only

Current behavior in `FocusViewModel`:

- `FocusEvent.PauseClicked` only sets `localViewState.pausedByUser = true`.
- `FocusEvent.ResumeClicked` only clears that local flag.
- `FocusEvent.AddTimeSelected` only increments `localViewState.addTimeSeconds`.
- The domain engine never receives a pause, resume, or add-time input.

Impact:

- The UI can show paused while the domain session continues changing.
- Add Time can make the displayed timer diverge from the saved session.
- Process death or service recovery loses these local-only choices.

### Restore Does Not Write Restored Data Locally

Current behavior:

- `FakeBackupRepository.restoreBackup()` deserializes JSON and returns a count.
- `AccountViewModel.restoreBackup()` displays success based on that count.
- No sessions, penalty events, or settings are written back to Room/DataStore.

Impact:

- A user can see "restore successful" while their app data is unchanged.
- This is the highest-trust issue in the current app.

### Call Permission Is Not Requested

Current behavior:

- `AndroidCallInterruptionMonitor` silently returns if `READ_PHONE_STATE` is missing.
- `MainActivity` only requests notification permission.
- There is no education UI explaining why call-state access exists.

Impact:

- Call interruption handling appears supported but will not work on a fresh install.
- Requesting this permission without context would feel invasive.

### Notification Tap Routing Is Cold-Start Only

Current behavior:

- `MainActivity` computes `initialRoute` from the launch `Intent`.
- There is no `onNewIntent()` handling.
- Compose navigation is not driven by a runtime notification-open event.

Impact:

- Tapping the foreground notification may fail to return to Focus if the app is already alive on another tab or detail screen.

### Settings Contains Dead Or Misleading Rows

Current examples:

- Terms of Service and Support default to no-op callbacks.
- Export Data routes to backup-like behavior rather than a real file export.
- Auto Backup is displayed even though scheduling is not implemented.
- Some rows use chevrons even when there is no real destination.

Impact:

- The Settings screen can overpromise.
- Users may tap rows that appear interactive but do not complete a real workflow.

### Focus And Insights Can Disagree On Today Metrics

Current behavior:

- `FocusViewModel` computes today's count and clean count locally.
- `GetTodayInsightsUseCase` owns a separate summary rule.
- `FocusViewModel` computes the day window once at ViewModel initialization.

Impact:

- Today cards in Focus and Insights can drift in edge cases.
- The Focus tab may be stale across midnight without ViewModel recreation.

## Architecture Plan

### Guiding Rule

Each fix should move behavior into the correct layer:

- `:domain:session` owns session state transitions.
- `:app` owns ViewModels, runtime coordinators, permissions, service wiring, and navigation events.
- `:core:model` owns repository contracts and shared domain models.
- `:core:database` owns Room persistence implementation.
- `:core:backup` owns backup serialization/deserialization.
- `:core:datastore` owns settings persistence.
- `:feature:*` modules stay UI-focused and should not depend on runtime coordinators or Android services.

## Workstream 1 - Real Pause And Add Time

### Desired Product Behavior

- Pause should stop focus progress immediately.
- Resume should require the phone-down ritual again before progress resumes.
- Pausing should make the session non-clean.
- Pausing should be persisted, survive process death, and appear consistently after app relaunch.
- Add Time should increase the actual required session duration, not just the UI display.
- Add Time should be persisted and reflected in completion criteria.

### Domain Changes

- [ ] Add `SessionState.PausedByUser` to `core:model`.
- [ ] Update stable Room enum mapping for `PausedByUser`.
- [ ] Add `PausedByUser` to recoverable-session query.
- [ ] Add session inputs:
  - [ ] `SessionInput.ManualPauseRequested`
  - [ ] `SessionInput.ManualResumeRequested`
  - [ ] `SessionInput.AddTimeRequested(additionalSeconds: Long)`
- [ ] Add a `manualPauseStartedAtElapsedMillis` field to `SessionRuntime`.
- [ ] Add `PenaltyEventType.ManualPause` or equivalent non-penalty event if the existing analytics model needs an explicit event.
- [ ] Define pause semantics in `SessionEngine`:
  - [ ] If active, first apply active progress, then enter `PausedByUser`.
  - [ ] If arming or waiting, enter `PausedByUser` without adding focus progress.
  - [ ] Mark `clean = false`.
  - [ ] Clear arming/active timing anchors.
  - [ ] Preserve `phoneIsValid` only as sensor context, not as active progress permission.
- [ ] Define resume semantics:
  - [ ] From `PausedByUser`, return to `WaitingForPhoneDown`.
  - [ ] Require a fresh `PhoneBecameValid` and arming countdown before active timing resumes.
  - [ ] Persist a manual pause event if the event model is updated.
- [ ] Define add-time semantics:
  - [ ] Reject non-positive values.
  - [ ] Add to `requiredDurationSeconds`.
  - [ ] Preserve `plannedDurationSeconds` as the user's original planned duration for analytics.
  - [ ] Update timestamps so the change persists.

### Runtime Coordinator Changes

- [ ] Add `pauseSession()`.
- [ ] Add `resumeSession()`.
- [ ] Add `addTime(additionalSeconds: Long)`.
- [ ] Route those methods through `SessionEngine.processInput()`.
- [ ] Force persistence for manual pause/resume/add-time transitions so UI and database do not drift.
- [ ] Add feedback behavior:
  - [ ] Pause: subtle haptic only.
  - [ ] Resume: no completion-like sound; normal arming/start cues should continue to work.
  - [ ] Add Time: subtle haptic or no feedback, depending existing feedback patterns.

### Focus ViewModel/UI Changes

- [ ] Remove `LocalViewState.pausedByUser`.
- [ ] Remove `LocalViewState.addTimeSeconds` from timer math.
- [ ] Map `SessionState.PausedByUser` to `FocusPresentationState.PausedByUser`.
- [ ] Route Pause/Resume/Add Time events to `ActiveSessionRuntimeCoordinator`.
- [ ] Keep only temporary UI sheet state locally, such as `showAddTime`.
- [ ] Ensure remaining time is derived from persisted session fields.

### Tests

- [ ] Add pure session engine tests for pause while active.
- [ ] Add pure session engine tests for pause during arming/waiting.
- [ ] Add pure session engine tests for resume requiring phone-down again.
- [ ] Add pure session engine tests for add-time changing completion threshold.
- [ ] Add repository/database tests for `PausedByUser` enum persistence.
- [ ] Add ViewModel tests proving Pause/Add Time call coordinator methods or change state through fake runtime.
- [ ] Update Focus UI tests for `PausedByUser`.

## Workstream 2 - Real Full-Replace Restore

### Desired Product Behavior

- Restore should deserialize backup data and replace local sessions, penalties, and settings.
- The success message should only appear after local persistence succeeds.
- Restore failure should leave the user with an honest error state.
- The existing fake backup should still support real round-trip restore in development.

### Repository Contract Changes

- [ ] Replace or supplement `BackupRepository.restoreBackup()` so app code can access the restored payload.
- [ ] Recommended model:
  - [ ] `RestorePayload(sessions, penaltyEvents, settings)`
  - [ ] `RestoreResult.Success(payload)` or a new `BackupRepository.fetchLatestBackup()`.
- [ ] Keep UI-facing restored counts in a use case rather than fake repository logic.

### Persistence Changes

- [ ] Add bulk insert methods to `SessionRepository`:
  - [ ] `replaceAllData(sessions: List<FocusSession>, penaltyEvents: List<PenaltyEvent>)`
  - [ ] or explicit `insertSessions()` / `insertPenaltyEvents()` plus a transaction helper.
- [ ] Add bulk DAO inserts:
  - [ ] `FocusSessionDao.upsertSessions(...)`
  - [ ] `PenaltyEventDao.upsertPenaltyEvents(...)`
- [ ] Implement full replacement in a Room transaction:
  - [ ] Clear penalties first.
  - [ ] Clear sessions second.
  - [ ] Insert sessions.
  - [ ] Insert penalties.
- [ ] Restore settings after Room replacement using `SettingsRepository`.
- [ ] Document the transaction limitation:
  - [ ] Room and DataStore cannot be committed in one atomic transaction.
  - [ ] The recommended order is restore Room first, then settings, because losing settings is less damaging than showing restored settings over missing session history.

### App Use Case

- [ ] Create `RestoreBackupUseCase` in an appropriate module, likely `:app` initially because it coordinates repositories.
- [ ] Responsibilities:
  - [ ] Fetch and validate backup payload.
  - [ ] Full-replace Room session data.
  - [ ] Restore settings.
  - [ ] Return restored counts and any failure reason.
- [ ] Update `AccountViewModel.restoreBackup()` to call the use case.
- [ ] Ensure restore does not run while a focus session is active.
  - [ ] Recommended behavior: block restore and show "End your current focus session before restoring data."

### Backup Mapper Stability

- [ ] Stop using enum `.name` in backup JSON for session/result/event/theme values.
- [ ] Reuse stable mapper constants where available.
- [ ] Add explicit parser failures with readable error messages for unsupported values.

### Tests

- [ ] Add backup serializer round-trip test that restores sessions, penalties, and settings.
- [ ] Add use-case test proving local repositories are replaced.
- [ ] Add test proving old local sessions are removed during restore.
- [ ] Add test proving restore blocks during active session.
- [ ] Add AccountViewModel test for success/failure/no-backup states.

## Workstream 3 - Call Permission Education And Request Flow

### Desired Product Behavior

- Users should understand why call-state permission is requested before seeing the OS prompt.
- If permission is granted, call interruptions should pause sessions.
- If permission is denied, the app should keep working and clearly treat call pause as unavailable.
- The app should avoid nagging repeatedly.

### UX Flow

- [ ] Add a small education surface before requesting `READ_PHONE_STATE`.
- [ ] Recommended placement:
  - [ ] Settings row: "Pause for calls" with status "Off", "On", or "Permission needed".
  - [ ] Tapping the row opens an explanation dialog/sheet.
  - [ ] The dialog explains that Phone Down only needs call state to pause focus sessions during phone calls and does not read call contents.
- [ ] Include actions:
  - [ ] "Allow call pause" triggers OS permission request.
  - [ ] "Not now" dismisses.
- [ ] Add a lightweight setting flag to avoid repeated automatic prompts.
- [ ] Do not ask for this permission automatically on first launch.

### Android Wiring

- [ ] Add `READ_PHONE_STATE` permission to `AndroidManifest.xml`.
- [ ] Add a permission launcher in `MainActivity`.
- [ ] Expose permission request callbacks to Settings through the app layer.
- [ ] Update `CallInterruptionMonitor` to expose capability/permission state if needed.
- [ ] Start or restart monitor behavior after permission grant.
- [ ] Handle denied and permanently-denied states gracefully.

### Tests

- [ ] Add Settings UI tests for call permission education copy and disabled/enabled states.
- [ ] Add ViewModel tests for permission education visibility.
- [ ] Add unit tests around call monitor fallback behavior if practical.
- [ ] Add manual QA checklist for a device with SIM/call capability.

## Workstream 4 - Notification Tap Always Routes To Focus

### Desired Product Behavior

- Tapping the active focus notification should always land on the Focus tab.
- This should work from cold start, warm start, and while the app is already open on another screen.
- Repeated notification taps should not duplicate navigation stack entries.

### Android/App Wiring

- [ ] Handle `MainActivity.onNewIntent(intent)`.
- [ ] Detect `FocusSessionServiceContract.EXTRA_OPEN_FOCUS`.
- [ ] Expose a one-shot navigation event from `MainActivity` or an app-level navigator.
- [ ] Update `PhoneDownApp` to observe the event and navigate to `PhoneDownRoute.Focus`.
- [ ] Use `launchSingleTop` and appropriate `popUpTo` behavior to avoid duplicate Focus destinations.
- [ ] Preserve onboarding behavior:
  - [ ] If onboarding is not complete, do not bypass required onboarding unless a real active session exists and the notification is the entry point.

### Tests

- [ ] Add app/navigation test for open-focus command.
- [ ] Add unit test for intent parsing if extracted.
- [ ] Add manual QA:
  - [ ] Start a session.
  - [ ] Navigate to Settings.
  - [ ] Tap foreground notification.
  - [ ] Confirm Focus tab is shown with current session state.

## Workstream 5 - Settings Honesty Cleanup

### Desired Product Behavior

- Every tappable row should either perform a real action or visibly communicate that it is unavailable.
- Prefer hiding dead rows over leaving no-op rows.
- Keep V1 settings useful and honest.

### Proposed Row Treatment

- [ ] Keep real rows:
  - [ ] Default Duration
  - [ ] Sounds
  - [ ] Haptics
  - [ ] Theme
  - [ ] Google Account
  - [ ] Phone Down Pro
  - [ ] Backup & Restore where it actually creates/restores backup data through fake/dev repositories
  - [ ] Privacy Policy
  - [ ] Version
  - [ ] Delete All Data
- [ ] Disable or hide rows that are not real:
  - [ ] Terms of Service: hide until document/screen exists.
  - [ ] Support: hide until email/deep link exists.
  - [ ] Export Data: hide unless a real file export/share flow is implemented.
  - [ ] Auto Backup: hide or mark disabled until scheduling exists.
  - [ ] Duration Presets: keep as static informational text only if not tappable.
  - [ ] Start Delay: keep static only if genuinely fixed, otherwise hide.
- [ ] Add Call Pause row as part of Workstream 3.
- [ ] Replace hardcoded version string with BuildConfig version if available from `:app` route state; otherwise leave as a tracked follow-up.

### Tests

- [ ] Update Settings UI tests for removed/disabled rows.
- [ ] Update Paparazzi baselines for Settings light/dark.
- [ ] Add tests that no-op rows are absent or disabled.

## Workstream 6 - Consistent Today Metrics

### Desired Product Behavior

- Focus and Insights should use the same definition of today's sessions.
- Count completed, ended early, invalidated, and broken sessions.
- Exclude abandoned sessions and pre-start/waiting sessions.
- Clean count should match the definition used in Insights.
- The day window should not silently stay on yesterday across midnight.

### Implementation Plan

- [ ] Extract session-summary classification into a shared helper or use case.
- [ ] Recommended location: `:domain:insights`, because it already owns insight summary rules.
- [ ] Reuse the same helper in:
  - [ ] `GetTodayInsightsUseCase`
  - [ ] Focus today's mini-summary path
- [ ] Replace ad hoc `FocusViewModel` counting with the shared summary.
- [ ] Refresh day boundaries:
  - [ ] Minimum: recompute when ViewModel is recreated.
  - [ ] Better: expose a small ticker/date provider so the flow switches windows at midnight.
  - [ ] Recommended for this phase: use a lightweight daily boundary flow only if simple; otherwise document as a follow-up because the main trust issue is semantic mismatch.

### Tests

- [ ] Add tests for session inclusion/exclusion rules.
- [ ] Add FocusViewModel test with mixed completed/ended/broken/abandoned/waiting sessions.
- [ ] Add Insights use-case test to prove the same helper is used.

## Verification Plan

### Automated Verification

- [ ] `./gradlew --no-configuration-cache :domain:session:test`
- [ ] `./gradlew --no-configuration-cache :domain:insights:test`
- [ ] `./gradlew --no-configuration-cache :core:backup:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :core:database:connectedDebugAndroidTest` if a device/emulator is available.
- [ ] `./gradlew --no-configuration-cache :app:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :feature:focus:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :feature:settings:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :app:assembleDebug`
- [ ] `./gradlew --no-configuration-cache :feature:focus:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug`
- [ ] `./scripts/check.sh`
- [ ] `git diff --check`

### Manual QA

- [ ] Install on attached Android device.
- [ ] Start a focus session and confirm progress starts only after face-down/arming.
- [ ] Pause during an active session.
  - [ ] Confirm timer stops.
  - [ ] Confirm state persists after app background/foreground.
  - [ ] Confirm resume requires phone-down again.
- [ ] Add 1 minute during an active session.
  - [ ] Confirm remaining time increases.
  - [ ] Confirm completion waits for the extended duration.
- [ ] Create backup, delete local data, restore backup.
  - [ ] Confirm sessions return in Insights.
  - [ ] Confirm settings return.
- [ ] Tap active notification from Focus, Insights, Settings, and Account.
  - [ ] Confirm app returns to Focus.
- [ ] Open Settings and verify no dead/no-op rows remain tappable.
- [ ] Test call permission education:
  - [ ] Permission denied path.
  - [ ] Permission granted path on a SIM/call-capable device when available.

## Documentation Updates

- [x] Update `docs/product-qa-audit-2026-05-10.md` with Phase 15 status.
- [x] Update `docs/agent-handoff.md` after implementation progress.
- [ ] Update `v1-implementation-plan.md` progress log after completion.
- [x] Add any permission explanation changes to `docs/permissions.md`.
- [ ] Update release readiness notes if any user-facing trust issue remains.

## Risks And Tradeoffs

- Pause semantics affect analytics. Marking pause as non-clean is conservative and honest, but users may expect a manual pause not to "punish" them. For V1, non-clean is safer because a clean session should mean uninterrupted phone-down focus.
- Restore cannot atomically commit Room and DataStore together. The implementation should restore Room first, then settings, and show failure if either step fails.
- `READ_PHONE_STATE` is sensitive. The app should ask only after a user explicitly opts into call pause from an explanation surface.
- Notification routing from Android intents into Compose navigation needs one-shot handling to avoid repeated navigation after configuration changes.
- Adding `SessionState.PausedByUser` changes persisted enum mapping. Because this is still pre-release/V1, a direct mapping addition is acceptable; migration behavior must still be tested.

## Acceptance Criteria

- [x] Pause changes the persisted session state and stops timer progress.
- [x] Resume from pause requires phone-down arming before progress continues.
- [x] Add Time changes `requiredDurationSeconds` and is reflected in persistence, UI, and completion.
- [x] Restore fully replaces local Room session/penalty data and restores DataStore settings.
- [x] Restore success is not shown unless local persistence succeeds.
- [x] Call permission is requested only after an education screen/dialog.
- [x] Denying call permission leaves the app functional and understandable.
- [x] Notification taps always return to Focus from cold and warm app states.
- [x] Dead/no-op Settings rows are hidden or disabled.
- [x] Focus and Insights today metrics use consistent session inclusion rules.
- [x] Automated verification commands pass or any blocked command is documented with a concrete reason.
- [ ] Manual device QA checklist is updated with results.

## Implementation Progress Log

### 2026-05-10

- Implemented real Pause/Add Time in `:domain:session`, `ActiveSessionRuntimeCoordinator`, and `FocusViewModel`.
- Added persisted `SessionState.PausedByUser` and `PenaltyEventType.ManualPause` stable storage mappings.
- Implemented real full-replace restore through `RestoreBackupUseCase`, `RestorePayload`, Room bulk replacement, and `SettingsRepository.restoreSettings`.
- Updated backup JSON enum strings to use stable storage values instead of Kotlin enum names.
- Added warm-start notification routing to Focus via `MainActivity.onNewIntent()` and a Compose navigation event flow.
- Added Settings education dialog before requesting `READ_PHONE_STATE`.
- Removed dead Settings rows for Terms, Support, Export Data, and Auto Backup.
- Aligned Focus today metrics with `GetTodayInsightsUseCase.summarize()`.
- Verification passed:
  - `./gradlew --no-configuration-cache :domain:session:test :domain:insights:test :core:backup:testDebugUnitTest :core:database:testDebugUnitTest :app:testDebugUnitTest :feature:focus:testDebugUnitTest :feature:settings:testDebugUnitTest :app:assembleDebug`
  - `./gradlew --no-configuration-cache :feature:settings:verifyPaparazziDebug :feature:focus:verifyPaparazziDebug`
  - `git diff --check`
- Verification blocked:
  - `./gradlew --no-configuration-cache :feature:settings:connectedDebugAndroidTest` could not run because Gradle reported `No connected devices!`.
  - `./scripts/check.sh` still fails on existing ktlint policy disagreements in modules outside the trust-hotfix scope plus the repo's known PascalCase Compose naming convention.

## Implementation Order

1. Real Pause/Add Time domain and runtime behavior.
2. Focus ViewModel/UI cleanup for Pause/Add Time.
3. Real restore payload and full-replace persistence.
4. Notification open-to-Focus routing.
5. Settings honesty cleanup plus call-permission education UI.
6. Call permission Android wiring.
7. Today metrics consistency.
8. Tests, Paparazzi updates, docs, and device QA.

This order prioritizes the highest user-visible trust gaps first while keeping risky Android permission work behind a clear UI explanation.
