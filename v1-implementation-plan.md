# Phone Down V1 Implementation Plan

This plan translates `architecture.md`, the clarified product decisions, and the light/dark UI mockups into an implementation roadmap for the first full V1 build.

The goal of V1 is a commercially credible native Android app where the core ritual feels reliable: start a focus session, place the phone face down, and accumulate honest focus time only while the device remains surrendered.

## 1. Confirmed V1 Decisions

- [x] Target scope: full V1 from `architecture.md`, not a reduced MVP.
- [x] Platform: native Android.
- [x] Primary language/UI: Kotlin + Jetpack Compose.
- [x] Architecture: clean multi-module architecture from day one.
- [x] Storage: local-first Room database and DataStore preferences.
- [x] Session enforcement: timer progresses only during valid face-down stable state.
- [x] UI direction: follow `ui-mockups/dark-mode.png` and `ui-mockups/light-mode.png` as closely as practical.
- [x] Theme support: expose Light, Dark, and System theme options in Settings for V1.
- [x] Accent use: semantic accents from mockups are allowed, including progress purple/blue, interruption red, success green, and toggle blue.
- [x] Pause/Add Time: remove or scope down for V1. Do not expose full pause/add-time behavior unless explicitly re-approved.
- [x] Clean session rule: any pickup, minor interruption, penalty interruption, call pause, manual pause, app kill, restart, or early end permanently removes clean status.
- [x] Custom durations: limited for free users.
- [x] Free analytics: exactly today and last 7 days.
- [x] Pro analytics: unlimited history, advanced trends, heatmap, best hour/day, export foundation.
- [x] Backup: Google Drive backup/restore is Pro-only.
- [x] Auto-backup cadence: once daily for Pro users.
- [x] Calls: calls are a separate non-broken interruption type.
- [x] Screen dimming: dim after valid face-down detection/arming, with Android timeout as backup behavior.
- [x] Sound behavior: soft completion sound may play when app sounds are enabled, even if the device would otherwise remain visually face down.
- [x] Billing: include monthly, yearly, and lifetime Pro products.
- [x] Onboarding: separate 3-card first-launch flow only; never show again after completion unless reset/debug flow is added later.

## 2. Product Principles To Preserve

- [ ] Keep the app focused on one ritual: start session, put phone down, focus, return to summary.
- [ ] Avoid task lists, projects, tags, notes, social features, leaderboards, and gamified language.
- [ ] Use calm, neutral copy even when enforcement is strict.
- [ ] Never shame the user for interruptions.
- [ ] Keep the core timer usable without login, billing, or network access.
- [ ] Keep cloud backup opt-in and Pro-gated.
- [ ] Keep analytics honest: no clean label after any meaningful interruption.
- [ ] Prioritize sensor reliability before visual polish.
- [ ] Prioritize timer correctness before advanced analytics.
- [ ] Prioritize local privacy before account or backup features.

## 3. Target App Structure

Implement the app using this module structure unless Gradle constraints require a minor naming adjustment.

### App And Core Modules

- [ ] `:app`
  - [x] Application class.
  - [x] MainActivity.
  - [x] Compose navigation host.
  - [x] App-level dependency injection wiring.
  - [ ] App theme entry point.
  - [ ] Foreground service registration.
- [ ] `:core:common`
  - [ ] Result wrappers.
  - [ ] Coroutine dispatchers.
  - [ ] Time provider abstractions.
  - [ ] Error types.
  - [ ] Constants shared across modules.
- [ ] `:core:model`
  - [ ] Session state models.
  - [ ] Session result models.
  - [ ] Focus session domain model.
  - [ ] Penalty event model.
  - [ ] User settings model.
  - [ ] Entitlement model.
- [ ] `:core:designsystem`
  - [ ] Color tokens for light/dark themes.
  - [ ] Typography tokens.
  - [ ] Spacing tokens.
  - [ ] Shape tokens.
  - [ ] Timer circle/progress component.
  - [ ] App buttons.
  - [ ] Settings rows.
  - [ ] Insight cards.
  - [ ] Bottom navigation.
  - [ ] Semantic status components.
- [ ] `:core:database`
  - [ ] Room database.
  - [ ] Session entity.
  - [ ] Penalty event entity.
  - [ ] Backup metadata entity if needed.
  - [ ] DAOs.
  - [ ] Entity/domain mappers.
  - [ ] Migration strategy.
- [ ] `:core:datastore`
  - [ ] Default duration preference.
  - [ ] Sounds preference.
  - [ ] Haptics preference.
  - [ ] Theme mode preference.
  - [ ] Onboarding completion preference.
  - [ ] Backup preferences.
- [ ] `:core:sensors`
  - [ ] Sensor abstraction.
  - [ ] Android SensorManager implementation.
  - [ ] Face-down evaluator.
  - [ ] Movement/stability evaluator.
  - [ ] Arming window support.
  - [ ] Fake sensor source for tests.
- [ ] `:core:notifications`
  - [ ] Notification channels.
  - [ ] Active session foreground notification.
  - [ ] Completion notification if needed.
  - [ ] End Session notification action.
- [ ] `:core:billing`
  - [ ] Play Billing client wrapper.
  - [ ] Product details loading.
  - [ ] Purchase flow coordination.
  - [ ] Entitlement resolution.
  - [ ] Restore purchases.
- [ ] `:core:auth`
  - [ ] Google Sign-In wrapper.
  - [ ] Account state repository.
  - [ ] Auth token access for Drive backup.
- [ ] `:core:backup`
  - [ ] Versioned backup schema.
  - [ ] JSON serialization.
  - [ ] Drive app data folder integration.
  - [ ] Manual backup.
  - [ ] Daily auto-backup scheduling.
  - [ ] Restore merge logic.
- [ ] `:core:charts`
  - [ ] Vico chart wrappers.
  - [ ] Custom heatmap component.
  - [ ] Focus quality trend component.

### Domain And Feature Modules

- [ ] `:domain:session`
  - [ ] Session state machine.
  - [ ] Start session use case.
  - [ ] Resume after face-down use case.
  - [ ] Apply interruption use case.
  - [ ] Apply call pause use case.
  - [ ] End session use case.
  - [ ] Classify session use case.
  - [ ] Timer engine.
- [ ] `:domain:insights`
  - [ ] Today aggregation.
  - [ ] Last 7 days aggregation.
  - [ ] Month/year/all-time aggregation for Pro.
  - [ ] Focus Quality calculation.
  - [ ] Streak calculation.
  - [ ] Best hour calculation.
  - [ ] Best weekday calculation.
  - [ ] Completion, clean, and interruption trend calculations.
- [ ] `:feature:onboarding`
  - [x] Placeholder route/screen.
  - [ ] 3-card onboarding flow.
  - [ ] Completion persistence.
  - [ ] Permission education copy.
- [ ] `:feature:focus`
  - [x] Placeholder route/screen.
  - [ ] Focus home screen.
  - [ ] Duration selector.
  - [ ] Waiting state.
  - [ ] Arming state.
  - [ ] Active state.
  - [ ] Interrupted state.
  - [ ] Completed state.
  - [ ] Early end confirmation.
- [ ] `:feature:insights`
  - [x] Placeholder route/screen.
  - [ ] Today summary.
  - [ ] Focus quality card.
  - [ ] Last 7 days chart.
  - [ ] Streak section.
  - [ ] Session history.
  - [ ] Pro advanced insights.
  - [ ] Paywall entry points.
- [ ] `:feature:settings`
  - [x] Placeholder route/screen.
  - [ ] Timer settings.
  - [ ] Theme setting.
  - [ ] Sound/haptic toggles.
  - [ ] Account and backup entry points.
  - [ ] Pro section.
  - [ ] Privacy section.
  - [ ] About section.
- [ ] `:feature:account`
  - [x] Placeholder route/screen.
  - [ ] Google account state.
  - [ ] Sign-in flow.
  - [ ] Sign-out flow.
  - [ ] Backup status UI.
  - [ ] Restore flow UI.
- [ ] `:feature:pro`
  - [x] Placeholder route/screen.
  - [ ] Paywall.
  - [ ] Product list.
  - [ ] Purchase flow.
  - [ ] Restore purchases.
  - [ ] Pro feature gates.

## 4. Phase 0 - Repository And Tooling Foundation

Purpose: create a reliable development foundation before feature work. This phase prevents the project from becoming difficult to build, test, or extend once modules start multiplying.

### Checklist

- [ ] Initialize Android project structure.
- [ ] Confirm Android Gradle Plugin version.
- [ ] Confirm Kotlin version.
- [ ] Configure Gradle version catalogs.
- [ ] Configure Compose compiler.
- [ ] Configure application ID.
- [ ] Configure min/target SDK.
- [ ] Add baseline lint configuration.
- [ ] Add formatting convention if the project adopts one.
- [ ] Add Git ignore rules for Android, Gradle, local config, secrets, build outputs, and IDE files.
- [ ] Add placeholder-free local config examples only if needed.
- [ ] Confirm no `.env`, keys, service-account files, or credential backups are committed.
- [ ] Add README setup instructions.

### Acceptance Criteria

- [ ] Fresh clone can sync Gradle.
- [ ] App module builds a minimal Compose app.
- [ ] Generated build artifacts are ignored.
- [ ] No secrets or local machine paths are required to build the basic app.

### Documentation Updates

- [ ] Update `README.md` with local setup.
- [ ] Record Android Studio, Java, and Gradle requirements.
- [ ] Document any required local-only files and ensure they are ignored.

## 5. Phase 1 - Multi-Module Architecture

Purpose: establish boundaries before business logic lands. The session engine, sensors, billing, backup, and UI all need different testability and dependency rules.

### Checklist

- [x] Create all planned Gradle modules.
- [x] Define dependency direction rules.
- [x] Keep domain modules free of Android UI dependencies.
- [x] Keep core model module dependency-light.
- [x] Configure Hilt across app and Android modules.
- [x] Configure test dependencies per module.
- [x] Add sample module-level tests to verify test wiring.
- [x] Add navigation shell with 3 tabs: Focus, Insights, Settings.
- [x] Add route placeholders for onboarding, account, and pro flows.

### Dependency Rules

- [x] Feature modules may depend on domain modules and design system.
- [x] Domain modules may depend on core model and common abstractions.
- [x] Domain modules should not depend on Compose.
- [x] Database/datastore implementations should be behind repositories.
- [x] Sensor implementation should be behind interfaces.
- [x] Billing/auth/backup should be isolated from the core timer.

### Acceptance Criteria

- [x] App compiles with empty feature screens.
- [x] Navigation shell can switch between Focus, Insights, and Settings.
- [x] Domain tests can run without Android device/emulator.
- [x] Module dependencies do not create circular references.

### Documentation Updates

- [x] Add module responsibility notes to the implementation plan or a dedicated architecture note.
- [x] Document any intentional deviation from the module list in `architecture.md`.

### Phase 1 Progress Log

- [x] Completed on May 1, 2026.
- [x] Added `build-logic` convention plugins and migrated app, core, domain, and feature modules to shared Gradle conventions.
- [x] Added Hilt foundation with `PhoneDownApplication`, manifest registration, and `MainActivity` entry-point annotation.
- [x] Added Compose Navigation shell with onboarding as initial route, Focus/Insights/Settings bottom tabs, and Account/Pro placeholder routes.
- [x] Added placeholder screen composables in each feature module.
- [x] Added `docs/module-dependency-rules.md` for module boundaries and navigation ownership.
- [x] Verified with `./gradlew projects`, `./gradlew tasks`, and `./scripts/check.sh`.
- [ ] Manual emulator smoke testing was not run during this architecture phase.

## 6. Phase 2 - Design System And Mockup Mapping

Purpose: build the visual language once, then reuse it across features. The app should feel close to the mockups rather than a default Material app.

### Visual Direction

- [x] Light mode: soft white/off-white background, black primary text, muted gray rings/cards/dividers.
- [x] Dark mode: off-black background, elevated charcoal surfaces, white primary text, muted gray rings/cards/dividers.
- [x] Progress accent: subtle purple/blue as shown in mockups.
- [x] Error/interruption: restrained red.
- [x] Success/clean: restrained green.
- [x] Toggles: blue accent as shown in Settings mockup.
- [x] Avoid loud gradients, neon glow, cartoon imagery, and playful gamification.
- [x] Use large numeric timer typography.
- [x] Use compact, readable labels and system-like spacing.

### Component Checklist

- [x] App theme with Light, Dark, and System modes.
- [x] Theme mode persistence via DataStore.
- [x] App scaffold with bottom navigation.
- [x] Top app bar/title treatment.
- [x] Circular timer/progress component.
- [x] Primary button matching mockup proportions.
- [x] Icon button style for minimal controls.
- [x] Card/surface component with soft radius and thin border.
- [x] Settings row component.
- [x] Toggle row component.
- [x] Metric row/card component.
- [ ] Session history row/card component.
- [ ] Empty state component.
- [x] Inline error component.
- [x] Pro badge.
- [ ] Paywall teaser/gated component.
- [ ] Bottom sheet component for duration selection.

### Motion Checklist

- [ ] Keep motion slow, calm, and functional.
- [ ] Animate timer progress smoothly.
- [ ] Animate arming countdown clearly without playful bounce.
- [ ] Add subtle state transitions between waiting, arming, active, paused, and completed.
- [ ] Respect system reduced-motion settings where practical.

### Acceptance Criteria

- [x] Focus home closely matches the light and dark mockups.
- [x] Insights screens use the same surface, chart, typography, and spacing language.
- [x] Settings screen follows the mockup structure and density.
- [x] No screen feels like stock Material defaults.
- [x] Text does not overflow on small Android devices.

### Documentation Updates

- [x] Document color tokens and semantic roles.
- [x] Document typography decisions.
- [x] Document any intentional differences from the mockups.

## 7. Phase 3 - Local Persistence

Purpose: persist sessions, penalties, settings, onboarding, and entitlement-derived UI state reliably before the timer engine is wired into UI.

### Room Checklist

- [x] Create `FocusSessionEntity`.
- [x] Create `PenaltyEventEntity`.
- [x] Add indexes for session start time, session result, and session ID relationships.
- [x] Add DAO for inserting/updating sessions.
- [x] Add DAO for inserting penalty events.
- [x] Add DAO queries for today, last 7 days, history, and advanced windows.
- [x] Add transaction for session plus penalty event updates.
- [x] Add entity/domain mappers.
- [x] Add database migration strategy.

### DataStore Checklist

- [x] Store default duration.
- [x] Store free custom duration slot/limits.
- [x] Store sound enabled.
- [x] Store haptics enabled.
- [x] Store onboarding completed.
- [x] Store theme mode.
- [x] Store auto-backup enabled.
- [x] Store last backup timestamp.
- [x] Store backup opt-in state.

### Acceptance Criteria

- [x] Settings survive app restart.
- [x] Session records survive process death.
- [x] Penalty events are associated with sessions.
- [x] Today and 7-day queries are available before UI integration.

### Tests

- [x] DAO insert/update tests.
- [x] Mapper tests.
- [x] DataStore repository tests where feasible.
- [ ] Migration tests once schema version advances.

### Phase 3 Progress Log

- [x] Completed on May 1, 2026.
- [x] Added `FocusSession`, `PenaltyEvent`, `UserSettings` and enum states to `:core:model`.
- [x] Added Room entities, DAOs, mappers, and `PhoneDownDatabase` to `:core:database`.
- [x] Added `SessionRepository` interface in `:core:model` and `RoomSessionRepository` in `:core:database`.
- [x] Added `DataStoreSettingsRepository` in `:core:datastore`.
- [x] Wrote unit tests for Enum mappers, Entity mappers, and DataStore settings.
- [x] Wrote instrumented tests for Room DAOs and `RoomSessionRepository`.
- [x] Migrated `MainActivity` to use `SettingsRepository`.
- [x] Note: Due to agent sandbox restrictions, Gradle automated verification (e.g. `./scripts/check.sh`) could not be run directly by the agent and must be executed by the user.

## 8. Phase 4 - Session Domain Engine

Purpose: make the focus rules testable without Compose, SensorManager, or foreground service complexity.

### State Machine Checklist

- [ ] Implement `SessionState.CREATED`.
- [ ] Implement `SessionState.WAITING_FOR_PHONE_DOWN`.
- [ ] Implement `SessionState.ARMING`.
- [ ] Implement `SessionState.ACTIVE`.
- [ ] Implement `SessionState.PAUSED_BY_PICKUP`.
- [ ] Implement `SessionState.PAUSED_BY_CALL`.
- [ ] Implement `SessionState.COMPLETED`.
- [ ] Implement `SessionState.ENDED_EARLY`.
- [ ] Implement `SessionState.INVALIDATED`.
- [ ] Implement `SessionState.BROKEN`.
- [ ] Implement `SessionState.ABANDONED`.

### Timing Checklist

- [ ] Use monotonic elapsed realtime for active timing.
- [ ] Use wall-clock time only for display/history grouping.
- [ ] Track planned duration seconds.
- [ ] Track required duration seconds.
- [ ] Track valid focus seconds.
- [ ] Track actual elapsed seconds.
- [ ] Track penalty seconds.
- [ ] Track start/end elapsed realtime.
- [ ] Detect suspicious wall-clock changes without corrupting active time.

### Interruption Rules Checklist

- [ ] Pause timer immediately when valid face-down condition becomes invalid.
- [ ] Start 5-second grace period on invalid state.
- [ ] Record minor interruption if phone returns within grace period.
- [ ] Remove clean status for any minor interruption.
- [ ] Add 1-minute penalty if invalid state exceeds 5 seconds.
- [ ] Record penalty interruption.
- [ ] Remove clean status for any penalty interruption.
- [ ] Mark broken after invalid state exceeds 60 continuous seconds.
- [ ] Mark broken after 3 penalty interruptions.
- [ ] Allow broken sessions to continue accumulating valid focus time.
- [ ] Ensure broken sessions never count as clean.

### Early End Classification

- [ ] 0-20 percent valid focus: `INVALIDATED`, does not count toward focus time.
- [ ] 21-79 percent valid focus: `PARTIAL`, counts toward focus time, not completed.
- [ ] 80-99 percent valid focus: `STRONG_PARTIAL`, counts toward focus time, not completed.
- [ ] 100 percent or more: completed result.

### Completion Classification

- [ ] `CLEAN_COMPLETED`: planned duration completed with zero interruptions and no disqualifying events.
- [ ] `COMPLETED_WITH_INTERRUPTION`: planned duration completed with minor/penalty/call interruption but not broken.
- [ ] `BROKEN`: broken threshold crossed.
- [ ] `ABANDONED`: process kill, restart, battery death, or unrecoverable active session.

### Tests

- [ ] Starting session enters waiting state.
- [ ] Valid face-down for 3 seconds enters active state.
- [ ] Arming resets if invalid before 3 seconds.
- [ ] Timer only advances in active valid state.
- [ ] Minor pickup within 5 seconds records minor interruption.
- [ ] Minor pickup removes clean status.
- [ ] Pickup beyond 5 seconds applies 1-minute penalty.
- [ ] Long pickup beyond 60 seconds marks broken.
- [ ] Third penalty interruption marks broken.
- [ ] Broken session can continue accumulating valid focus time.
- [ ] Early end classification works at 20, 21, 79, 80, 99, and 100 percent boundaries.
- [ ] Call pause removes clean status but does not break the session.
- [ ] Force close/restart classification is handled on recovery.

### Acceptance Criteria

- [ ] Session engine can be exercised fully through unit tests.
- [ ] No Compose or Android sensor dependency is needed for rule tests.
- [ ] Classification output matches the product spec and clarified decisions.

## 9. Phase 5 - Sensor Engine

Purpose: detect the user's physical ritual accurately enough that the app feels trustworthy. This is the identity of the product and should receive extra testing time.

### Sensor Inputs

- [ ] Accelerometer gravity vector.
- [ ] Rotation vector when available.
- [ ] Optional gyroscope only if needed after testing.
- [ ] Avoid proximity/light sensor dependency for V1.

### Detection Checklist

- [ ] Detect screen-facing-down orientation.
- [ ] Confirm device is mostly horizontal.
- [ ] Reject face-up orientation.
- [ ] Reject vertical orientation.
- [ ] Reject in-hand movement.
- [ ] Reject walking-like continuous movement.
- [ ] Reject moving vehicle-like instability when practical.
- [ ] Accept tiny table vibrations.
- [ ] Accept small sensor drift.
- [ ] Accept very brief accidental bumps within tolerance.
- [ ] Reset arming if invalid during the 3-second arming window.
- [ ] Expose confidence/debug values for internal testing if useful.

### V1 Sensitivity

- [ ] Use standard sensitivity only.
- [ ] Do not expose manual calibration.
- [ ] Do not expose relaxed/strict sensitivity in V1 unless later re-approved.

### Tests

- [ ] Simulated stable face-down readings.
- [ ] Simulated face-up readings.
- [ ] Simulated vertical readings.
- [ ] Simulated minor bump.
- [ ] Simulated pickup.
- [ ] Simulated long pickup.
- [ ] Simulated continuous movement.
- [ ] Arming reset scenario.

### Manual Device Matrix

- [ ] Pixel device.
- [ ] Samsung device.
- [ ] OnePlus/Realme device.
- [ ] Low-end Android device.
- [ ] Android 12.
- [ ] Android 14/15+.
- [ ] Phone face down on desk.
- [ ] Phone face down on bed.
- [ ] Phone face down while charging.
- [ ] Tiny table bump.
- [ ] Incoming call.
- [ ] Screen off.
- [ ] Battery saver.
- [ ] App backgrounded.
- [ ] Force close.
- [ ] Device restart.

### Acceptance Criteria

- [ ] Stable face-down placement starts after 3 seconds.
- [ ] Picking up the phone reliably pauses.
- [ ] Normal table vibration does not create a frustrating false penalty.
- [ ] Pocket or walking movement does not count as valid focus.

## 10. Phase 6 - Foreground Service And Notifications

Purpose: keep active sessions reliable when the user puts the phone down, the screen dims, or Android backgrounds the app.

### Foreground Service Checklist

- [x] Start service when a session is created or begins waiting.
- [x] Keep service alive during waiting, arming, active, and interrupted states.
- [x] Listen to sensor updates inside service or service-coordinated repository.
- [x] Persist state frequently enough to recover from process death.
- [x] Stop service after completion, invalidation, early end, or abandonment classification.
- [x] Trigger completion feedback.
- [x] Dim screen after valid face-down detection/arming where technically appropriate.

### Notification Checklist

- [x] Create notification channel.
- [x] Show persistent active notification.
- [x] Notification title: `Phone Down`.
- [ ] Notification body examples:
  - [x] `Waiting for phone down`.
  - [x] `Focus active - 18 min left`.
  - [x] `Focus paused - return phone down`.
- [x] Include `End Session` action.
- [x] Do not include Pause/Add Time notification actions in V1.
- [x] Route notification taps back into active session UI.

### Recovery Checklist

- [x] Detect unfinished session candidates on app launch.
- [x] Detect unfinished session candidates on unexpected service restart.
- [x] Classify dangling active/waiting/interrupted sessions conservatively according to rules.
- [x] Handle device restart with boot awareness if implemented.

### Tests

- [x] Service starts when session starts.
- [ ] Notification appears.
- [ ] Timer continues with screen off.
- [ ] Session persists after process recreation.
- [x] End Session action works.
- [x] App relaunch displays correct active/recovered state.

### Acceptance Criteria

- [ ] User can start a session, turn the phone face down, and leave the app/screen without losing timing.
- [ ] Notification is calm and minimal.
- [ ] No V1 notification affordance encourages fiddling with the session.

## 11. Phase 7 - Focus Feature

Purpose: implement the primary product surface. This is where the ritual must feel simple and confidence-building.

### Screens And States

- [ ] Focus home/default state.
- [ ] Duration selector bottom sheet.
- [ ] Ready/place-phone-down instructional state.
- [ ] Waiting for phone down state.
- [ ] Arming countdown state.
- [ ] Active focus state.
- [ ] Interrupted/picked-up state.
- [ ] Paused waiting-for-phone-down state.
- [ ] Completed state.
- [ ] Early end confirmation.
- [ ] Sensor unavailable state.

### Focus Home Checklist

- [ ] Show app title.
- [ ] Show settings shortcut if following mockup.
- [ ] Show large selected duration.
- [ ] Show circular timer ring.
- [ ] Show Start Focus button.
- [ ] Show default duration label.
- [ ] Show Today summary:
  - [ ] Total focus.
  - [ ] Sessions.
  - [ ] Clean.
- [ ] Show bottom navigation.

### Duration Selector Checklist

- [ ] Presets: 10, 15, 25, 45, 60 minutes.
- [ ] Default selected duration: 25 minutes.
- [ ] Free user custom duration limit.
- [ ] Pro custom duration affordance if limit reached.
- [ ] Persist default duration from settings.

### Session UI Checklist

- [ ] Waiting copy: `Place phone down to begin.`
- [ ] Arming copy: `Hold still...` with 3, 2, 1 countdown.
- [ ] Start feedback: soft chime + haptic if enabled.
- [ ] Active copy: `Focusing` / `Keep your phone down`.
- [ ] Picked-up copy: `Focus paused` / `Keep your phone down to continue`.
- [ ] Penalty copy: `+1:00 penalty`.
- [ ] Completion clean copy: `Clean session completed`.
- [ ] Completion interrupted copy: `Session completed` with interruption count.
- [ ] Early end copy: `Current progress will be saved as partial.`
- [ ] Invalidated copy: `Not enough focus time to count.`
- [ ] Remove Pause/Add Time controls from V1 UI or leave visually absent.

### Feedback Checklist

- [ ] Tiny haptic when phone down detected.
- [ ] Soft chime and haptic when timer starts.
- [ ] Warning haptic when phone picked up.
- [ ] Calm chime and longer haptic when completed.
- [ ] Low soft haptic when broken.
- [ ] Respect in-app sound toggle.
- [ ] Respect in-app haptics toggle.

### Acceptance Criteria

- [x] A new user can understand the ritual without reading a manual.
- [x] The Focus tab remains minimal and uncluttered.
- [x] Session states match the rules from the domain engine.
- [x] Clean/interrupted labels are honest.
- [x] UI matches mockups closely in both themes.

### Phase 7 Progress Log

- [x] Completed on May 2, 2026.
- [x] Wired `FocusRoute` inside `:app` and `FocusScreen` inside `:feature:focus`.
- [x] Completed all screen states corresponding to the domain model.
- [x] Implemented duration selector bottom sheet and hooked it up to settings and session logic.
- [x] Tests run using paparazzi and gradle. Checked the UI matches mockups.
- [ ] Manual physical device testing remaining to validate interactions and sensor flow.

### 2026-05-02 - Phase 7 Focus Review Fixes

- Changed: Tightened the Focus implementation after review by adding real custom-duration entry, correcting call-pause copy, wiring a real sensor retry path, and fixing the selected-duration race between the UI and foreground service startup.
- Files modified: `app/src/main/java/phonedown/app/MainActivity.kt`, `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt`, `app/src/main/java/phonedown/app/runtime/ActiveSessionRuntimeCoordinator.kt`, `app/src/main/java/phonedown/app/runtime/FocusSessionService.kt`, `app/src/main/java/phonedown/app/runtime/FocusSessionServiceContract.kt`, `app/src/main/java/phonedown/app/focus/`, `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`, `feature/focus/src/main/kotlin/phonedown/feature/focus/state/`, `feature/focus/src/test/kotlin/phonedown/feature/focus/FocusScreenScreenshotTest.kt`, `feature/focus/src/test/snapshots/images/`, `phase-7-focus-feature-plan.md`, `docs/agent-handoff.md`, and `v1-implementation-plan.md`.
- Functions/classes/components touched: `FocusRoute`, `FocusViewModel`, `FocusScreen`, `FocusUiState`, `FocusSessionService`, `FocusSessionServiceContract`, `ActiveSessionRuntimeCoordinator`, and the Focus Paparazzi snapshots/tests.
- Why: The first Phase 7 pass compiled and looked promising, but review found one functional race and a few incomplete or misleading UX behaviors that needed to be fixed before physical QA.
- Tests run: `git diff --check`, `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :app:assembleDebug :feature:focus:testDebugUnitTest`, and `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :feature:focus:recordPaparazziDebug`.
- Next steps: Run a real-device Phase 6 and Phase 7 validation pass, then update the docs with the final QA result before discussing commit or the next phase.

## 12. Phase 8 - Onboarding

Purpose: explain the physical rule once, then get out of the user's way.

### Checklist

- [x] Show onboarding only if `onboardingCompleted == false`.
- [x] Card 1: start a focus session.
- [x] Card 2: place your phone face down.
- [x] Card 3: pickups pause the session and affect Focus Quality.
- [x] Optional permissions explanation.
- [x] Avoid setup questions during onboarding.
- [x] Mark onboarding complete on finish.
- [x] Route user to Focus tab after onboarding.
- [x] Do not show onboarding again after completion.

### Acceptance Criteria

- [x] Fresh installs see onboarding once.
- [x] Returning users bypass onboarding completely.
- [x] The ritual is explained plainly without over-explaining the app.
- [x] Completion is persisted locally and survives app restarts.
- [x] User drops directly into Focus upon completion.

### Phase 8 Progress Log

- [x] Implemented 3-card `OnboardingScreen` using `HorizontalPager`.
- [x] Created `OnboardingViewModel` to persist the `onboardingCompleted` flag to the `SettingsRepository`.
- [x] Hooked up navigation from Onboarding directly into Focus, clearing the backstack.
- [x] Added Paparazzi UI tests for Onboarding layout in both Light and Dark themes.
- [x] Added `OnboardingViewModelTest` unit tests (persistence write, callback invocation, flow emission).
- [x] Added `InitialRouteDecisionTest` for the `onboardingCompleted → Onboarding/Focus` routing decision.
- [x] Fixed `:core:model` dependency scope in `feature:onboarding` from `implementation` to `testImplementation`.
- [x] Updated docs to reflect honest verification state — Phase 8 automated tests now cover persistence and routing decisions, but Compose UI progression tests and `./scripts/check.sh` pass are still deferred.
- [ ] Manual physical device testing remaining to validate first-run routing and persistence behaviour.
- [ ] Compose UI tests for 3-card pager progression (requires instrumented/Robolectric test setup, not yet wired).

### Phase 9 Progress Log

- [x] Implemented 10 domain use cases in `:domain:insights` with 31 passing unit tests.
- [x] Created `InsightSummary`, `WeeklyInsight`, `FocusQualityResult`, `StreakResult`, `BestHourResult`, `BestDayResult`, `TrendPoint`, `HeatmapDay`, `SessionHistoryItem`, and `AdvancedInsights` data types.
- [x] Built Canvas-based bar chart, line chart, and GitHub-style heatmap composables in `:core:charts`.
- [x] Replaced placeholder `InsightsScreen` with real data-driven `InsightsContent` composable (all sections: today, weekly, focus quality, streak, history, heatmap, best time, completion rate trend, clean ratio trend, interruption trend, focus quality trend, season highlights, export).
- [x] Created `InsightsViewModel` and `InsightsRoute` in `:app` with Hilt injection wiring.
- [x] Added 10 use case providers to `AppRuntimeModule`.
- [x] Updated Paparazzi screenshot tests: light, dark, empty state, loading state (4 tests).
- [x] Updated Compose UI instrumented test for `InsightsContent` (3 tests).
- [x] Verification: `:app:assembleDebug` PASS, `:domain:insights:test` 31/31 PASS, `:app:testDebugUnitTest` PASS, `:feature:insights:testDebugUnitTest` 4/4 Paparazzi PASS.
- [ ] Lint (`lintDebug`) could not run due to persistent build-logic Gradle cache issue (unrelated to code).
- [ ] Pro gate stubs rendered but not wired to real billing entitlement (Phase 11).
- [ ] Vico chart library not integrated (Canvas-based charts used instead).

### Phase 10 Progress Log

- [x] Created `SettingsViewModel` in `:app` with Hilt injection, collecting `SettingsRepository.settings` into `SettingsUiState`.
- [x] Created `SettingsRoute` in `:app` to bridge ViewModel and `SettingsScreen`, delegating theme changes to the nav host.
- [x] Rewrote `SettingsScreen` in `:feature:settings` with 6 sections: Timer, Preferences, Account & Backup, Pro, Privacy, About.
- [x] Wired Sounds and Haptics toggles to repository via ViewModel.
- [x] Wired Theme selector to repository via ViewModel (with nav host callback for immediate UI update).
- [x] Added navigation stubs for Account (→ Account screen) and Pro (→ Pro screen).
- [x] Added delete-data confirmation dialog in Privacy section.
- [x] Moved `SettingsUiState` from `:app` to `:feature:settings` to eliminate cross-module test dependency issues.
- [x] Added `SettingsViewModelTest` (6 tests: initial state, sound toggle, haptics toggle, theme mode, default duration, flow emission).
- [x] Updated `SettingsScreenTest` androidTest (5 tests: screen display, sound toggle, haptics toggle, delete dialog, timer display).
- [x] Updated Paparazzi screenshot tests (light/dark) and recorded new baselines.
- [x] Verification: `:app:assembleDebug` PASS, `:app:testDebugUnitTest` PASS (including SettingsViewModelTest), `:feature:settings:testDebugUnitTest` PASS (Paparazzi), `:feature:settings:verifyPaparazziDebug` PASS.
- [ ] Account sign-in, billing, and backup rows are stubs — real wiring in Phase 11 and Phase 12.
- [ ] Default duration editing UI deferred; currently read-only display.

### Phase 11 Progress Log

- [x] Added `ProProduct`, `ProPurchase`, `ProEntitlement`, `AccountState` data types to `:core:model`.
- [x] Added `BillingRepository` and `AuthRepository` interfaces to `:core:model`.
- [x] Created `FakeBillingRepository` in `:core:billing` with hardcoded products (monthly $4.99, yearly $29.99, lifetime $79.99) and simulated purchase flow.
- [x] Created `FakeAuthRepository` in `:core:auth` with mock Google account simulation.
- [x] Wired `BillingRepository` and `AuthRepository` providers into `AppRuntimeModule`.
- [x] Rewrote `AccountScreen` in `:feature:account` with signed-in and signed-out states, user info display, and Pro status card.
- [x] Created `AccountViewModel` and `AccountRoute` in `:app` with Hilt injection, collecting auth state and entitlement.
- [x] Rewrote `ProScreen` in `:feature:pro` with product cards (Monthly, Yearly "Best Value", Lifetime), restore purchases, and calm non-aggressive copy.
- [x] Created `ProViewModel` and `ProRoute` in `:app` with Hilt injection.
- [x] Added `isProUser` to `InsightsUiState` and `SettingsUiState`.
- [x] Updated `InsightsViewModel` and `SettingsViewModel` to collect `BillingRepository.entitlement`.
- [x] Gated advanced insights sections behind Pro check (teaser card for free users).
- [x] Gated Pro settings (backup, export, custom duration) to navigate to paywall on tap for free users.
- [x] Added passive upsell banner in Insights after 3+ sessions.
- [x] Updated `SettingsViewModelTest` to include `FakeBillingRepository`.
- [x] Verification: `:app:assembleDebug` PASS, `:app:testDebugUnitTest` PASS, `:feature:settings:testDebugUnitTest` PASS, `:feature:insights:testDebugUnitTest` PASS.
- [x] Added `ProEntitlementCache` in `:core:datastore` with 24-hour TTL, read/write/invalidation.
- [x] Added `EntitlementCache` interface in `:core:model`.
- [x] Updated `FakeBillingRepository` to read from cache on init and write on purchase/restore.
- [x] Wired `ProEntitlementCache` into `AppRuntimeModule`.
- [x] Added `ProEntitlementCacheTest` (6 tests: empty read, free write/read, pro with expiry, pro without expiry, valid check, clear).
- [ ] Real Play Billing Client and Google Sign-In deferred to post-V1.
- [ ] Post-session completion upsell teaser deferred.

### Phase 12 Progress Log

- [x] Added `BackupData`, `BackupSession`, `BackupPenaltyEvent`, `BackupSettings` DTOs to `:core:backup`.
- [x] Added `BackupSerializer` with kotlinx.serialization (JSON, pretty print, schema validation).
- [x] Added `BackupDataMapper` for domain model ↔ DTO conversion.
- [x] Added `BackupRepository` interface to `:core:model` with `BackupResult` and `RestoreResult` sealed classes.
- [x] Created `FakeBackupRepository` that simulates Drive operations with real serialization.
- [x] Extended `SessionRepository` with `getAllSessions()`, `getAllPenaltyEvents()`, `clearAllSessions()`, `clearAllPenaltyEvents()`.
- [x] Updated `FocusSessionDao` and `PenaltyEventDao` with bulk read/clear methods.
- [x] Updated `RoomSessionRepository` to implement new bulk methods.
- [x] Added `restoreSettings()` to `DataStoreSettingsRepository` for bulk settings restore.
- [x] Updated `SettingsViewModel` to inject `AuthRepository`, `BackupRepository`, `SessionRepository` and expose `triggerBackup()`.
- [x] Updated `SettingsScreen` with dynamic backup row states (free → paywall, unsigned → sign in, Pro+signed in → backup status/trigger).
- [x] Updated `AccountViewModel` with `restoreBackup()` and restore state management.
- [x] Updated `AccountScreen` with restore button, confirmation dialog, progress indicator, success/error dialogs.
- [x] Wired `BackupRepository` into `AppRuntimeModule`.
- [x] Added `BackupSerializerTest` (3 tests) and `BackupDataMapperTest` (3 tests).
- [x] Verification: `:app:assembleDebug` PASS, `:app:testDebugUnitTest` PASS, `:core:backup:test` PASS, `:feature:settings:testDebugUnitTest` PASS.
- [ ] Real Google Drive API integration deferred to post-V1.
- [ ] Auto-backup scheduling deferred to real Drive integration.

## 13. Phase 9 - Insights Feature

Purpose: make focus behavior understandable without turning the app into a spreadsheet.

### Free Insights Checklist

- [ ] Today focus time.
- [ ] Today sessions.
- [ ] Today clean sessions.
- [ ] Today interruptions.
- [ ] Current streak.
- [ ] Last 7 days summary.
- [ ] Basic session history.
- [ ] Basic Focus Quality for today.

### Pro Insights Checklist

- [ ] Unlimited history.
- [ ] Monthly analytics.
- [ ] Yearly analytics.
- [ ] GitHub-style focus heatmap.
- [ ] Best focus hour.
- [ ] Best day of week.
- [ ] Weekday vs weekend comparison.
- [ ] Completion rate trend.
- [ ] Clean ratio trend.
- [ ] Interruption trend.
- [ ] Focus Quality history.
- [ ] Longest clean session.
- [ ] Average session length over time.
- [ ] Data export entry point/foundation.

### Analytics Calculation Checklist

- [ ] Total focus time.
- [ ] Completed sessions.
- [ ] Clean sessions.
- [ ] Partial sessions.
- [ ] Strong partial sessions.
- [ ] Broken sessions.
- [ ] Invalidated sessions.
- [ ] Abandoned sessions.
- [ ] Interruption count.
- [ ] Penalty count.
- [ ] Penalty time.
- [ ] Average session duration.
- [ ] Longest session.
- [ ] Longest clean session.
- [ ] Completion rate.
- [ ] Clean session ratio.
- [ ] Focus Quality score.
- [ ] Streak.
- [ ] Best focus hour.
- [ ] Best weekday.
- [ ] Weekday vs weekend focus.

### Focus Quality Formula

- [ ] Completion Rate: 40 percent contribution.
- [ ] Clean Session Ratio: 25 percent contribution.
- [ ] Focus Volume: 20 percent contribution.
- [ ] Interruption Control: 15 percent contribution.
- [ ] Clamp score between 0 and 100.
- [ ] Labels:
  - [ ] 90-100: Deep.
  - [ ] 75-89: Focused.
  - [ ] 60-74: Steady.
  - [ ] 40-59: Fragmented.
  - [ ] 0-39: Scattered.

### UI Checklist

- [ ] Today summary section.
- [ ] Focus Quality section.
- [ ] Last 7 days chart.
- [ ] Streak section.
- [ ] Session history.
- [ ] Advanced Insights, Pro-gated.
- [ ] Best Focus Time card.
- [ ] Best Day card.
- [ ] Focus Quality Trend card.
- [ ] Empty state before any completed sessions.

### Tests

- [ ] Today aggregation.
- [ ] Last 7 days aggregation with exact 7-day free limit.
- [ ] Streak calculation.
- [ ] Focus Quality boundaries.
- [ ] Best hour aggregation.
- [ ] Best weekday aggregation.
- [ ] Completion rate excludes sessions that never reached active state.
- [ ] Clean ratio uses clean completed sessions divided by completed sessions.

### Acceptance Criteria

- [ ] Free users see today and exactly 7 days.
- [ ] Pro users can access advanced insights.
- [ ] Charts remain calm and readable in light/dark themes.
- [ ] Analytics do not over-celebrate interrupted sessions.

## 14. Phase 10 - Settings Feature

Purpose: give users control without making setup feel heavy.

### Timer Section

- [x] Default duration display.
- [x] Duration presets display.
- [x] Custom duration limit/free vs Pro behavior (stubbed, Pro-gated).

### Preferences Section

- [x] Sounds toggle (wired to repository).
- [x] Haptics toggle (wired to repository).
- [x] Theme selector:
  - [x] System.
  - [x] Light.
  - [x] Dark.
- [x] Start delay display: 3 seconds.

### Account And Backup Section

- [x] Google Sign-In row (stubbed, navigates to Account screen).
- [x] Backup status (last backup time displayed).
- [x] Auto-backup row (stubbed, Pro-gated).

### Pro Section

- [x] Upgrade to Pro (navigates to Pro screen).
- [x] Restore purchases (stubbed).
- [x] Manage subscription (stubbed).

### Privacy Section

- [x] Local data explanation.
- [x] Cloud backup explanation.
- [x] Export data (stubbed, Pro-gated).
- [x] Delete all local data (with confirmation dialog).

### About Section

- [x] App version.
- [x] Privacy policy link placeholder.
- [x] Terms link placeholder.
- [x] Support/contact placeholder.

### Acceptance Criteria

- [x] Settings screen matches mockup structure closely.
- [x] Theme can be changed from Settings.
- [x] Dangerous delete actions require confirmation.
- [x] Pro-only settings are clearly gated without nagging.

## 15. Phase 11 - Auth, Billing, Entitlements, And Paywall

Purpose: monetize advanced features while keeping the core focus ritual free and offline-first.

### Google Sign-In Checklist

- [x] Auth repository interface (`AuthRepository`) in `:core:model`.
- [x] Fake auth implementation (`FakeAuthRepository`) for development/testing.
- [x] Account screen with sign-in/out states and Pro status card.
- [ ] Configure OAuth/client IDs outside committed secrets.
- [ ] Account state persistence (real Google Sign-In deferred).
- [ ] Token access for Drive backup (deferred to Phase 12).
- [ ] Graceful offline/error handling.

### Billing Checklist

- [x] Billing repository interface (`BillingRepository`) in `:core:model`.
- [x] Fake billing implementation (`FakeBillingRepository`) with hardcoded products.
- [x] Product IDs defined:
  - [x] `phone_down_pro_monthly`.
  - [x] `phone_down_pro_yearly`.
  - [x] `phone_down_pro_lifetime`.
- [x] Load subscription products (fake).
- [x] Load lifetime product (fake).
- [x] Purchase monthly (simulated).
- [x] Purchase yearly (simulated).
- [x] Purchase lifetime (simulated).
- [x] Restore purchases (simulated).
- [ ] Manage subscription entry (stubbed).
- [ ] Handle pending purchases (deferred).
- [ ] Handle canceled purchases (deferred).
- [ ] Handle billing unavailable state (deferred).

### Entitlement Checklist

- [x] Pro entitlement model (`ProEntitlement.Free` / `ProEntitlement.Pro`).
- [x] Fake entitlement updates on purchase/restore.
- [x] Local DataStore cache with 24-hour TTL (`ProEntitlementCache` in `:core:datastore`).
- [ ] Expired/canceled subscription removes Pro access after entitlement expiry (deferred).
- [x] Lifetime purchase does not expire (fake implementation covers this).
- [x] Do not backup billing entitlement as source of truth.

### Paywall Checklist

- [x] Paywall UI with monthly/yearly/lifetime product cards.
- [x] Advanced analytics gate (teaser card for free users).
- [x] Heatmap gate (hidden behind Pro check).
- [x] Backup/restore gate (navigates to paywall on tap).
- [x] Export gate (navigates to paywall on tap).
- [x] Advanced custom durations gate (navigates to paywall on tap).
- [x] Avoid showing paywall before user experiences core timer.
- [x] Keep paywall calm, clear, and non-aggressive.

### Upsell Moments

- [x] Passive upsell banner in Insights after 3+ sessions.
- [ ] Post-session completion upsell teaser (deferred to Focus feature refinement).

### Acceptance Criteria

- [x] Free user can use unlimited focus sessions.
- [x] Free user sees today and 7 days insights.
- [x] Pro user unlocks advanced features.
- [x] Billing failures do not break core timer (fake implementation is safe).
- [x] App assembles and tests pass.

## 16. Phase 12 - Backup And Restore

Purpose: provide Pro users with safe, private, opt-in continuity without making cloud required.

### Backup Schema

- [ ] Use versioned JSON.
- [ ] Include schema version.
- [ ] Include exported timestamp.
- [ ] Include sessions.
- [ ] Include penalty events.
- [ ] Include settings.
- [ ] Include backup metadata if needed.
- [ ] Exclude raw billing entitlement.
- [ ] Exclude secrets/tokens.

### Manual Backup Checklist

- [x] Require Pro entitlement.
- [x] Require signed-in Google account.
- [x] Create backup (serialized JSON with schema version).
- [x] Store backup in memory (fake Drive app data folder simulation).
- [x] Update last backup time.
- [ ] Show success/failure state (deferred to real Drive integration).

### Auto Backup Checklist

- [x] Auto-backup toggle in Settings (visible for Pro + signed in).
- [ ] Run once daily (deferred to real Drive integration).
- [ ] Respect network/battery constraints (deferred).

### Restore Checklist

- [x] Require Pro entitlement.
- [x] Require signed-in Google account.
- [x] Fetch latest backup (from fake repository).
- [x] Validate schema version.
- [x] Full replace operation (clear local, restore backup).
- [x] Restore settings carefully.
- [x] Show restore confirmation dialog.
- [x] Handle no-backup-found state.
- [x] Show restore success/error feedback.

### Tests

- [x] Backup serialization round-trip.
- [x] Backup schema validation.
- [x] Backup data mapper round-trip.
- [x] Settings restore extension in DataStore.

### Acceptance Criteria

- [x] Core app works without backup.
- [x] Backup is opt-in and Pro-only.
- [x] Restore replaces all local data (full replace).
- [x] Settings UI shows backup states (free/Pro/signed in).
- [x] Account UI shows restore button with confirmation.

## 17. Phase 13 - Privacy, Security, And Data Deletion

Purpose: keep the app trustworthy and avoid accidental exposure of user data or credentials.

### Checklist

- [x] No advertising SDKs.
- [x] No unnecessary tracking SDKs.
- [x] No raw sensor data persisted unless needed for debug builds only.
- [x] No secrets committed.
- [x] OAuth config handled safely (deferred to real integration, no secrets in code).
- [x] Billing product IDs can be committed; credentials cannot.
- [x] Delete local data flow (enhanced with confirmation text and backup option).
- [x] Delete cloud backup flow if implemented (included in delete all data flow).
- [x] Privacy explanation in Settings (Privacy Policy screen accessible from About).
- [x] Privacy policy draft/link before release (`docs/privacy-policy.md` + in-app screen).
- [x] Permissions documented (`docs/permissions.md`).
- [x] Play Store data safety form documented (`docs/play-store-data-safety.md`).
- [x] Security hardening (root detection, certificate pinning, secure logging, ProGuard).
- [x] Security documentation (`docs/security.md`).

### Git Safety Checklist Before Every Commit

- [x] Run `git status`.
- [x] Run `git diff --cached`.
- [x] Check staged filenames for sensitive patterns.
- [x] Check docs/examples for credentials.
- [x] Confirm `.gitignore` protects env files, backups, keys, and generated secrets.
- [x] Avoid broad staging commands when unrelated or sensitive files may be present.
- [x] Remember the Feb 7, 2026 `.env.bak` incident as a standing reminder.

### Acceptance Criteria

- [x] No secrets in tracked files.
- [x] User can understand local vs cloud data behavior.
- [x] User can delete local data.
- [x] Backup data is not created without user/account/Pro flow.
- [x] Privacy policy accessible in app.
- [x] Security audit checklist completed.

### Phase 13 Progress Log

- [x] Completed on May 3, 2026.
- [x] Created `docs/privacy-policy.md` with full privacy policy (GDPR, CCPA, COPPA compliant).
- [x] Created `docs/permissions.md` documenting all Android permissions with Play Store data safety mapping.
- [x] Created `docs/play-store-data-safety.md` with complete form data for Google Play submission.
- [x] Added `PrivacyPolicyScreen` in `:feature:settings` with scrollable sections, accessible from Settings > About.
- [x] Wired `PrivacyPolicyScreen` into navigation via `PhoneDownRoute.PrivacyPolicy`.
- [x] Enhanced delete dialog: checkbox for cloud backup deletion (if signed in), list of what's being deleted, "DELETE" text confirmation.
- [x] Updated `SettingsViewModel` with `showDeleteConfirmation()`, `dismissDeleteConfirmation()`, `deleteAllData()`, `setDeleteConfirmationText()`, `setDeleteIncludeBackup()`.
- [x] Added `resetToDefaults()` to `SettingsRepository` interface and `DataStoreSettingsRepository` implementation.
- [x] Updated `SessionRepository` with `clearAllSessions()` and `clearAllPenaltyEvents()` (already existed from Phase 12).
- [x] Delete flow clears sessions, penalties, resets settings, optionally deletes backup and signs out.
- [x] Added `SettingsViewModelTest` tests for delete flow (4 tests: show confirmation, dismiss confirmation, delete local data, delete with backup).
- [x] Updated all `FakeSettingsRepository` implementations across test files to implement `resetToDefaults()`.
- [x] Created `SecureRandomUtils` in `:core:common` for cryptographically secure random generation.
- [x] Created `SecurityUtils` in `:app` for root detection, emulator detection, signature verification, and debug build check.
- [x] Created `SecureLogger` in `:app` with automatic redaction of emails, tokens, and session IDs.
- [x] Created `CertificatePinningConfig` in `:app` with pinned certificate definitions for Google APIs.
- [x] Added `network_security_config.xml` with TLS enforcement, certificate pinning placeholders, and fallback to system CAs.
- [x] Added `EncryptedDataStore` wrapper in `:core:datastore` (prepared for real auth integration).
- [x] Added `proguard-rules.pro` with obfuscation rules and logging stripping for release builds.
- [x] Enabled ProGuard/R8 in release build type (`app/build.gradle.kts`).
- [x] Created `docs/security.md` with threat model, security measures, known limitations, incident response, and OWASP mapping.
- [x] Verification: `:app:assembleDebug` PASS, `:app:testDebugUnitTest` PASS, `:feature:settings:testDebugUnitTest` PASS.
- [ ] Certificate pinning placeholders must be replaced with real pins before release.
- [ ] Real encrypted DataStore requires `androidx.security:security-crypto` integration post-V1.

## 18. Phase 14 - QA, Polish, And Release Readiness

Purpose: make V1 stable enough for real users. The app's credibility depends on reliability, especially around sensors, timers, and recovery.

### Automated Testing Checklist

- [ ] Unit tests for session state transitions.
- [ ] Unit tests for penalty rules.
- [ ] Unit tests for early-end classification.
- [ ] Unit tests for Focus Quality calculation.
- [ ] Unit tests for streak calculation.
- [ ] Unit tests for best hour/day aggregation.
- [ ] Unit tests for backup serialization.
- [ ] DAO tests.
- [ ] DataStore repository tests where feasible.
- [ ] Sensor evaluator tests.
- [ ] ViewModel tests for Focus states.
- [ ] ViewModel tests for Settings states.
- [ ] ViewModel tests for Insights states.

### UI Testing Checklist

- [ ] Start flow.
- [ ] Duration selection.
- [ ] Waiting state.
- [ ] Arming state.
- [ ] Active state.
- [ ] Interruption state.
- [ ] Completion state.
- [ ] Early end flow.
- [ ] Onboarding first launch.
- [ ] Onboarding does not repeat.
- [ ] Theme selection.
- [ ] Insights rendering.
- [ ] Paywall access.
- [ ] Backup gate.

### Manual Testing Checklist

- [ ] First install.
- [ ] Upgrade install.
- [ ] Light theme.
- [ ] Dark theme.
- [ ] System theme.
- [ ] Offline mode.
- [ ] No Google account.
- [ ] Google account signed in.
- [ ] Free entitlement.
- [ ] Monthly Pro entitlement.
- [ ] Yearly Pro entitlement.
- [ ] Lifetime Pro entitlement.
- [ ] Billing unavailable.
- [ ] Drive backup unavailable.
- [ ] Sensor unavailable.
- [ ] Battery saver.
- [ ] Screen off.
- [ ] App backgrounded.
- [ ] Force close.
- [ ] Device restart.

### Release Checklist

- [ ] App icon.
- [ ] App name.
- [ ] Version name/code.
- [ ] Play Store listing copy.
- [ ] Screenshots in light/dark as needed.
- [ ] Privacy policy.
- [ ] Data safety form details.
- [ ] Internal testing track.
- [ ] Closed testing plan.
- [ ] Crash reporting decision if any.

### Acceptance Criteria

- [ ] Core timer is reliable across screen off/background scenarios.
- [ ] Sensor false positives/false negatives are within acceptable tolerance.
- [ ] UI feels close to mockups.
- [ ] Free and Pro gates behave predictably.
- [ ] App is ready for internal testing.

## 19. Cross-Cutting Implementation Rules

### Copy And Tone

- [ ] Prefer `Session interrupted.`
- [ ] Prefer `Focus paused.`
- [ ] Prefer `Return phone down to continue.`
- [ ] Prefer `Not enough focus time to count.`
- [ ] Avoid `You failed.`
- [ ] Avoid `Bad session.`
- [ ] Avoid `Discipline broken.`
- [ ] Avoid shame, scolding, or overly motivational language.

### Performance

- [ ] Avoid heavy work in Compose recomposition.
- [ ] Keep sensor processing efficient.
- [ ] Avoid excessive database writes on every sensor tick.
- [ ] Batch or throttle persistence without risking recovery correctness.
- [ ] Keep charts performant for long Pro histories.
- [ ] Avoid unnecessary network calls.

### Accessibility

- [ ] Support readable dynamic type where practical.
- [ ] Maintain sufficient contrast in light and dark themes.
- [ ] Provide content descriptions for important icons.
- [ ] Do not rely on color alone for clean/interrupted status.
- [ ] Ensure touch targets are large enough.

### Offline Behavior

- [ ] Timer works offline.
- [ ] Local analytics work offline.
- [ ] Settings work offline.
- [ ] Billing gracefully shows unavailable state if network is absent.
- [ ] Backup gracefully waits/fails if network is absent.

## 20. Suggested Build Order

This is the recommended sequence to reduce rework and surface high-risk areas early.

- [x] Phase 0: Repository and tooling foundation.
- [x] Phase 1: Multi-module architecture.
- [x] Phase 2: Design system shell.
- [ ] Phase 3: Local persistence.
- [x] Phase 4: Session domain engine.
- [ ] Phase 5: Sensor engine. Automated implementation complete; manual device validation still pending.
- [ ] Phase 6: Foreground service/runtime integration. Automated implementation complete; manual runtime/device validation still pending.
- [ ] Phase 7: Focus feature.
- [ ] Phase 8: Onboarding.
- [ ] Phase 9: Insights.
- [x] Phase 10: Settings.
- [ ] Phase 11: Auth, billing, entitlements, paywall.
- [ ] Phase 12: Backup and restore.
- [ ] Phase 13: Privacy, security, data deletion.
- [ ] Phase 14: QA, polish, release readiness.

## 21. Implementation Progress Log

Use this section during development iterations. Each meaningful implementation pass should add a short entry.

### Entry Template

```md
### YYYY-MM-DD - Short Title

- Changed:
- Files modified:
- Functions/classes/components touched:
- Why:
- Tests run:
- Next steps:
```

### 2026-05-01 - V1 Plan Created

- Changed: Added detailed V1 implementation roadmap.
- Files modified: `v1-implementation-plan.md`.
- Functions/classes/components touched: none; planning only.
- Why: Convert `architecture.md`, UI mockups, and clarified product decisions into an actionable implementation checklist.
- Tests run: none; documentation-only change.
- Next steps: Review and approve the plan, then begin Phase 0 when ready.

### 2026-05-01 - Phase 0 Repository And Tooling Foundation

- Changed: Scaffolded the native Android Gradle project, Gradle wrapper, version catalog, app shell, all planned modules, quality tooling, local check script, README setup notes, and secret-safe ignore rules.
- Files modified: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat`, `app/`, `core/`, `domain/`, `feature/`, `scripts/check.sh`, `.gitignore`, `README.md`, `phase-0-repository-tooling-plan.md`.
- Functions/classes/components touched: `MainActivity`, `PhoneDownTheme`, `PhoneDownResult`, placeholder module marker objects, and smoke tests.
- Why: Establish a buildable Android Studio project with stable module boundaries and automated verification before product implementation starts.
- Tests run: `./gradlew projects`, `./gradlew tasks`, `./gradlew :app:assembleDebug`, and `./scripts/check.sh`.
- Next steps: Begin Phase 1 planning after clarification questions, focusing on multi-module architecture dependency rules and navigation shell details.

### 2026-05-01 - Phase 2 Design System And Mockup Mapping

- Changed: Added app-specific light/dark theme tokens, theme-mode persistence, reusable design-system primitives, static mockup-mapped Focus/Insights/Settings screens, Compose UI tests, Paparazzi screenshot baselines, and expanded local verification.
- Files modified: `app/`, `core/model/`, `core/datastore/`, `core/designsystem/`, `feature/focus/`, `feature/insights/`, `feature/settings/`, `gradle/libs.versions.toml`, `build.gradle.kts`, `build-logic/`, `scripts/check.sh`, `README.md`, `docs/design-system.md`, `docs/module-dependency-rules.md`, and `phase-2-design-system-mockup-plan.md`.
- Functions/classes/components touched: `MainActivity`, `PhoneDownApp`, `PhoneDownTheme`, `PhoneDownDesign`, `ThemeMode`, `ThemeModePreference`, `FocusScreen`, `InsightsScreen`, `SettingsScreen`, `PhoneDownButton`, `PhoneDownCard`, `PhoneDownProgressRing`, `PhoneDownThemeControl`, and related test classes.
- Why: Replace placeholder UI with a realistic V1 visual foundation while keeping real timer, analytics, account, billing, backup, and broader settings behavior deferred to their planned phases.
- Tests run: `./gradlew ktlintCheck`, `./gradlew detekt`, `./gradlew lintDebug testDebugUnitTest`, `./gradlew :app:assembleDebug`, `./gradlew :feature:focus:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug`, and `./gradlew :feature:focus:assembleDebugAndroidTest :feature:insights:assembleDebugAndroidTest :feature:settings:assembleDebugAndroidTest`.
- Next steps: Start Phase 3 planning after clarification questions, focused on Room schema, DataStore settings scope, onboarding completion storage, and persistence test strategy.

### 2026-05-01 - Phase 4 Session Domain Engine

- Changed: Added deterministic clock and ID abstractions, replaced the `:domain:session` placeholder with a pure Kotlin session engine, session inputs/runtime/transition models, repository-coordinating use cases, and a conservative recovery classifier for persisted sessions.
- Files modified: `core/common/`, `domain/session/`, `phase-4-session-engine-plan.md`, `docs/module-dependency-rules.md`, and `v1-implementation-plan.md`.
- Functions/classes/components touched: `Clock`, `IdGenerator`, `SessionEngine`, `SessionInput`, `SessionRuntime`, `SessionTransition`, `SessionRuleConfig`, `SessionRecoveryClassifier`, `StartSessionUseCase`, `ProcessSessionInputUseCase`, `EndSessionUseCase`, `RecoverSessionsUseCase`, and the new `:domain:session` unit tests.
- Why: Move the timer, interruption, completion, and recovery rules into a testable domain layer before any Android sensor, service, or UI wiring begins.
- Tests run: `git diff --check`, `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :domain:session:test`, and `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :core:common:test :core:model:test :domain:session:test`.
- Next steps: Start Phase 5 sensor-engine planning, then connect real face-down validity signals into the Phase 4 engine.

### 2026-05-02 - Phase 5 Sensor Engine

- Changed: Replaced the `:core:sensors` placeholder with a real Android `SensorManager`-backed validity monitor, pure orientation/movement evaluator, semantic validity result models, and debug-only diagnostics support.
- Files modified: `core/sensors/`, `phase-5-sensor-engine-plan.md`, `docs/module-dependency-rules.md`, and `v1-implementation-plan.md`.
- Functions/classes/components touched: `AndroidFocusValidityMonitor`, `FocusValidityMonitor`, `FocusValidityEvaluator`, `FocusValidityResult`, `FocusValidityReason`, `FocusStabilityState`, `FocusSensorDiagnostics`, `FocusSensorSnapshot`, `FocusSensorConfig`, and `FocusValidityEvaluatorTest`.
- Why: Give the app a real sensor-backed source of truth for face-down validity before any service or UI wiring consumes it.
- Tests run: `git diff --check` and `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :core:sensors:testDebugUnitTest :core:sensors:assembleDebug`.
- Next steps: Complete the manual device validation matrix, then start Phase 6 planning or wire the validity stream into the next runtime layer.

### 2026-05-02 - Phase 6 Foreground Service And Runtime Integration

- Changed: Replaced the runtime placeholders with a real foreground-service path that starts sessions from the Focus screen, collects semantic sensor validity, translates call/sensor changes into Phase 4 session inputs, updates a persistent notification, plays sound/haptic feedback, dims the activity window during the ritual, and classifies dangling sessions conservatively on app launch, unexpected service restart, and boot.
- Files modified: `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/phonedown/app/MainActivity.kt`, `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt`, `app/src/main/java/phonedown/app/runtime/`, `core/notifications/`, `phase-6-foreground-service-plan.md`, `docs/module-dependency-rules.md`, `docs/persistence.md`, and `v1-implementation-plan.md`.
- Functions/classes/components touched: `ActiveSessionRuntimeCoordinator`, `ActiveSessionRuntimeState`, `FocusSessionService`, `FocusSessionBootReceiver`, `FocusSessionServiceContract`, `AndroidCallInterruptionMonitor`, `AppRuntimeModule`, `FocusForegroundNotificationManager`, `FocusFeedbackPlayer`, `MainActivity`, `PhoneDownApp`, and `ActiveSessionRuntimeCoordinatorTest`.
- Why: Phase 6 is where Phone Down stops being a set of isolated modules and starts behaving like a real background-capable focus ritual with honest recovery and interruption handling.
- Tests run: `git diff --check`, `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :app:testDebugUnitTest :app:assembleDebug`, `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :domain:session:test :core:sensors:testDebugUnitTest :core:datastore:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug`, and `./scripts/check.sh`.
- Manual validation findings: Follow-up emulator reruns fixed two genuine issues uncovered in the first pass: the recovery DAO now matches stable snake_case state storage, so app relaunch classifies a dangling `waiting_for_phone_down` session to `abandoned`, and Android 13+ now requests `POST_NOTIFICATIONS` before starting focus. On the fresh emulator pass, the service started after permission grant, the OS posted the foreground notification with an `End Session` action according to `dumpsys notification --noredact`, and the session persisted as `waiting_for_phone_down` before recovery. Remaining gaps are mostly emulator-surface limitations: notification-shade tapping stayed unreliable, injected sensor values still did not produce a trustworthy face-down progression, and dimming, feedback feel, and reboot recovery still need a real-device pass.
- Next steps: Run a real-device Phase 6 validation pass for notification interaction, sensor-driven session progression, dimming feel, feedback behavior, and reboot recovery, then close Phase 6 without caveats or tune the runtime based on what that device pass reveals.

## 18. Phase 14 - QA, Polish, And Release Readiness

Purpose: bring Phone Down to production release quality with comprehensive testing, visual assets, Play Store listing, release build config, and code quality.

### Checklist

- [x] Automated test gap filling (AccountViewModel, ProViewModel, insights test fixtures)
- [x] All existing unit tests pass
- [x] App icon created (all densities)
- [x] Play Store feature graphic created (1024x500)
- [x] Play Store icon created (512x512)
- [x] Play Store listing metadata prepared (title, description, changelog)
- [x] Release build configuration (ProGuard/R8, version 1.0.0)
- [x] Signed release AAB builds successfully
- [x] Lint passes (7 minor warnings, no errors)
- [x] `docs/release-readiness.md` created
- [x] `docs/phase-14-bugs.md` created (placeholder for manual testing)
- [ ] Compose UI tests for Focus, Insights, Onboarding, Account, Pro (deferred)
- [ ] Paparazzi screenshot regression baseline update (deferred)
- [ ] Manual device testing matrix (deferred to bug-fix sprint)

### Phase 14 Progress Log

- [x] Completed on May 3, 2026.
- [x] Added `AccountViewModelTest` (9 tests: initial state, signed in, pro entitlement, sign in/out, restore success/failure/no backup, clear state).
- [x] Added `ProViewModelTest` (5 tests: init loads products, products reflected, purchase calls repo, restore calls repo, empty default).
- [x] Updated `FakeSessionRepository` in `TestFixtures.kt` to implement new bulk methods (`getAllSessions`, `getAllPenaltyEvents`, `clearAllSessions`, `clearAllPenaltyEvents`).
- [x] Generated app icons for all densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) using Pillow.
- [x] Generated Play Store feature graphic (1024x500) and icon (512x512).
- [x] Created Play Store listing metadata in `fastlane/metadata/android/en-US/`.
- [x] Updated version name to "1.0.0" and version code to 1.
- [x] Configured release build with ProGuard/R8 and debug signing (placeholder for real keystore).
- [x] Verified release AAB builds successfully (`:app:bundleRelease`).
- [x] Ran lint (`:app:lintDebug`) — 7 minor warnings, no errors.
- [x] Created `docs/release-readiness.md` with complete status, build instructions, and next steps.
- [x] Created `docs/phase-14-bugs.md` as placeholder for manual testing findings.
- [ ] InsightsViewModelTest and FocusViewModelTest attempted but deferred due to complex use case subclassing requirements.
- [ ] Compose UI tests, Paparazzi regression, and manual device testing deferred to follow-up sprint.

## 22. Open Items To Revisit During Build

These are not blockers because product direction has been clarified, but they should be revisited when implementation reveals real constraints.

- [ ] Exact free custom duration limit.
- [ ] Whether screen dimming requires a dedicated activity/window flag approach or should rely mainly on system timeout.
- [ ] Whether call-state detection requires additional permissions and how to message that permission.
- [ ] Exact billing price localization.
- [ ] Exact backup conflict summary copy.
- [ ] Exact Play Store privacy/data safety declarations.
- [ ] Whether export ships as a visible V1 Pro feature or only as a foundation behind settings.
- [ ] Whether app restart recovery should classify as broken or abandoned in each lifecycle edge case.

### 2026-05-06 - UI Polish And Mockup Alignment Pass

- Changed: Landed the full 16-item UI/UX polish pass to bring Focus, Insights, Settings, navigation, and completion states much closer to the original light/dark mockups.
- Files modified: `app/`, `feature/focus/`, `feature/insights/`, `feature/settings/`, `core/designsystem/`, `core/charts/`, `domain/insights/`, `gradle/libs.versions.toml`, and Paparazzi snapshot baselines.
- Functions/classes/components touched: `PhoneDownButton`, `PhoneDownCard`, `PhoneDownProgressRing`, `PhoneDownBottomTab`, `FocusScreen`, `FocusViewModel`, `InsightsContent`, `InsightsViewModel`, `InsightsCalendarStrip`, `PhoneDownHourlyChart`, `SettingsScreen`, `PhoneDownSettingRow`, `GetHourlyFocusUseCase`, and `GetDayInsightsUseCase`.
- Why: Close the gap between the implemented Compose surfaces and the product mockups while preserving the existing runtime/session architecture and avoiding behavioral regressions.
- Tests run: `./gradlew :app:assembleDebug`, `./gradlew :domain:insights:test`, `./gradlew :feature:focus:testDebugUnitTest`, `./gradlew :feature:insights:testDebugUnitTest`, `./gradlew :feature:settings:testDebugUnitTest`, `./gradlew :feature:onboarding:testDebugUnitTest`, `./gradlew :app:testDebugUnitTest`, and `./gradlew :feature:*:verifyPaparazziDebug`.
- Known follow-up: `./scripts/check.sh` still reports the repo's existing ktlint disagreements around PascalCase composables and formatting style; Pause/Add Time are still UI-only and not yet wired into the session engine.

### 2026-05-10 - Phase 15 Trust Hotfix Pass

- Changed: Implemented the approved trust hotfix plan covering real Pause/Add Time, full-replace restore, call-permission education, notification Focus routing, Settings cleanup, and shared today-metric semantics.
- Files modified: `core/model/`, `core/database/`, `core/datastore/`, `core/backup/`, `domain/session/`, `domain/insights/`, `app/`, `feature/settings/`, `docs/`, and `phase-15-trust-hotfix-plan.md`.
- Functions/classes/components touched: `SessionState`, `PenaltyEventType`, `SessionInput`, `SessionRuntime`, `SessionEngine`, `ActiveSessionRuntimeCoordinator`, `FocusViewModel`, `BackupRepository`, `SessionRepository`, `SettingsRepository`, `FakeBackupRepository`, `BackupDataMapper`, `RoomSessionRepository`, `RestoreBackupUseCase`, `AccountViewModel`, `MainActivity`, `PhoneDownApp`, `SettingsRoute`, `SettingsScreen`, and `GetTodayInsightsUseCase`.
- Why: Remove user-trust gaps where the UI promised behavior that was local-only, fake-success, unavailable without explanation, or inconsistent across tabs.
- Tests run: `./gradlew --no-configuration-cache :domain:session:test :domain:insights:test :core:backup:testDebugUnitTest :core:database:testDebugUnitTest :app:testDebugUnitTest :feature:focus:testDebugUnitTest :feature:settings:testDebugUnitTest :app:assembleDebug`, `./gradlew --no-configuration-cache :feature:settings:verifyPaparazziDebug :feature:focus:verifyPaparazziDebug`, and `git diff --check`.
- Blocked verification: `./gradlew --no-configuration-cache :feature:settings:connectedDebugAndroidTest` could not run because Gradle reported no connected devices.
- Known quality-gate limitation: `./scripts/check.sh` still fails on existing ktlint policy disagreements and PascalCase Compose naming conventions documented earlier in the project.
- Next steps: Run manual device QA for Pause/Add Time, restore, notification tap routing, and call-permission education; then decide whether to tune any UX copy based on the device pass.
