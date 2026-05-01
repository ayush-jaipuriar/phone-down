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

- [ ] Light mode: soft white/off-white background, black primary text, muted gray rings/cards/dividers.
- [ ] Dark mode: off-black background, elevated charcoal surfaces, white primary text, muted gray rings/cards/dividers.
- [ ] Progress accent: subtle purple/blue as shown in mockups.
- [ ] Error/interruption: restrained red.
- [ ] Success/clean: restrained green.
- [ ] Toggles: blue accent as shown in Settings mockup.
- [ ] Avoid loud gradients, neon glow, cartoon imagery, and playful gamification.
- [ ] Use large numeric timer typography.
- [ ] Use compact, readable labels and system-like spacing.

### Component Checklist

- [ ] App theme with Light, Dark, and System modes.
- [ ] Theme mode persistence via DataStore.
- [ ] App scaffold with bottom navigation.
- [ ] Top app bar/title treatment.
- [ ] Circular timer/progress component.
- [ ] Primary button matching mockup proportions.
- [ ] Icon button style for minimal controls.
- [ ] Card/surface component with soft radius and thin border.
- [ ] Settings row component.
- [ ] Toggle row component.
- [ ] Metric row/card component.
- [ ] Session history row/card component.
- [ ] Empty state component.
- [ ] Inline error component.
- [ ] Pro badge.
- [ ] Paywall teaser/gated component.
- [ ] Bottom sheet component for duration selection.

### Motion Checklist

- [ ] Keep motion slow, calm, and functional.
- [ ] Animate timer progress smoothly.
- [ ] Animate arming countdown clearly without playful bounce.
- [ ] Add subtle state transitions between waiting, arming, active, paused, and completed.
- [ ] Respect system reduced-motion settings where practical.

### Acceptance Criteria

- [ ] Focus home closely matches the light and dark mockups.
- [ ] Insights screens use the same surface, chart, typography, and spacing language.
- [ ] Settings screen follows the mockup structure and density.
- [ ] No screen feels like stock Material defaults.
- [ ] Text does not overflow on small Android devices.

### Documentation Updates

- [ ] Document color tokens and semantic roles.
- [ ] Document typography decisions.
- [ ] Document any intentional differences from the mockups.

## 7. Phase 3 - Local Persistence

Purpose: persist sessions, penalties, settings, onboarding, and entitlement-derived UI state reliably before the timer engine is wired into UI.

### Room Checklist

- [ ] Create `FocusSessionEntity`.
- [ ] Create `PenaltyEventEntity`.
- [ ] Add indexes for session start time, session result, and session ID relationships.
- [ ] Add DAO for inserting/updating sessions.
- [ ] Add DAO for inserting penalty events.
- [ ] Add DAO queries for today, last 7 days, history, and advanced windows.
- [ ] Add transaction for session plus penalty event updates.
- [ ] Add entity/domain mappers.
- [ ] Add database migration strategy.

### DataStore Checklist

- [ ] Store default duration.
- [ ] Store free custom duration slot/limits.
- [ ] Store sound enabled.
- [ ] Store haptics enabled.
- [ ] Store onboarding completed.
- [ ] Store theme mode.
- [ ] Store auto-backup enabled.
- [ ] Store last backup timestamp.
- [ ] Store backup opt-in state.

### Acceptance Criteria

- [ ] Settings survive app restart.
- [ ] Session records survive process death.
- [ ] Penalty events are associated with sessions.
- [ ] Today and 7-day queries are available before UI integration.

### Tests

- [ ] DAO insert/update tests.
- [ ] Mapper tests.
- [ ] DataStore repository tests where feasible.
- [ ] Migration tests once schema version advances.

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

- [ ] Start service when a session is created or begins waiting.
- [ ] Keep service alive during waiting, arming, active, and interrupted states.
- [ ] Listen to sensor updates inside service or service-coordinated repository.
- [ ] Persist state frequently enough to recover from process death.
- [ ] Stop service after completion, invalidation, early end, or abandonment classification.
- [ ] Trigger completion feedback.
- [ ] Dim screen after valid face-down detection/arming where technically appropriate.

### Notification Checklist

- [ ] Create notification channel.
- [ ] Show persistent active notification.
- [ ] Notification title: `Phone Down`.
- [ ] Notification body examples:
  - [ ] `Waiting for phone down`.
  - [ ] `Focus active - 18 min left`.
  - [ ] `Focus paused - return phone down`.
- [ ] Include `End Session` action.
- [ ] Do not include Pause/Add Time notification actions in V1.
- [ ] Route notification taps back into active session UI.

### Recovery Checklist

- [ ] Detect active session on app launch.
- [ ] Recover waiting/active/interrupted session when possible.
- [ ] Classify unrecoverable active sessions as abandoned or broken according to rules.
- [ ] Handle device restart with boot awareness if implemented.

### Tests

- [ ] Service starts when session starts.
- [ ] Notification appears.
- [ ] Timer continues with screen off.
- [ ] Session persists after process recreation.
- [ ] End Session action works.
- [ ] App relaunch displays correct active/recovered state.

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

- [ ] A new user can understand the ritual without reading a manual.
- [ ] The Focus tab remains minimal and uncluttered.
- [ ] Session states match the rules from the domain engine.
- [ ] Clean/interrupted labels are honest.
- [ ] UI matches mockups closely in both themes.

## 12. Phase 8 - Onboarding

Purpose: explain the physical rule once, then get out of the user's way.

### Checklist

- [ ] Show onboarding only if `onboardingCompleted == false`.
- [ ] Card 1: start a focus session.
- [ ] Card 2: place your phone face down.
- [ ] Card 3: pickups pause the session and affect Focus Quality.
- [ ] Optional permissions explanation.
- [ ] Avoid setup questions during onboarding.
- [ ] Mark onboarding complete on finish.
- [ ] Route user to Focus tab after onboarding.
- [ ] Do not show onboarding again after completion.

### Acceptance Criteria

- [ ] Fresh install sees onboarding once.
- [ ] Returning user goes directly to app.
- [ ] Onboarding copy stays calm and concise.

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

- [ ] Default duration.
- [ ] Duration presets display.
- [ ] Custom duration limit/free vs Pro behavior.

### Preferences Section

- [ ] Sounds toggle.
- [ ] Haptics toggle.
- [ ] Theme selector:
  - [ ] System.
  - [ ] Light.
  - [ ] Dark.
- [ ] Start delay display: 3 seconds.

### Account And Backup Section

- [ ] Google Sign-In row.
- [ ] Signed-in account display.
- [ ] Backup status.
- [ ] Last backup time.
- [ ] Manual backup.
- [ ] Restore from backup.
- [ ] Auto-backup toggle, Pro only.

### Pro Section

- [ ] Upgrade to Pro.
- [ ] Restore purchases.
- [ ] Manage subscription.
- [ ] Lifetime Pro recognition.

### Privacy Section

- [ ] Local data explanation.
- [ ] Cloud backup explanation.
- [ ] Export data, Pro.
- [ ] Delete all local data.
- [ ] Delete cloud backup if implemented.

### About Section

- [ ] App version.
- [ ] Privacy policy link placeholder.
- [ ] Terms link placeholder if needed.
- [ ] Support/contact placeholder if needed.

### Acceptance Criteria

- [ ] Settings screen matches mockup structure closely.
- [ ] Theme can be changed from Settings.
- [ ] Dangerous delete actions require confirmation.
- [ ] Pro-only settings are clearly gated without nagging.

## 15. Phase 11 - Auth, Billing, Entitlements, And Paywall

Purpose: monetize advanced features while keeping the core focus ritual free and offline-first.

### Google Sign-In Checklist

- [ ] Add Google Sign-In dependency.
- [ ] Configure OAuth/client IDs outside committed secrets.
- [ ] Sign-in from Settings/Account.
- [ ] Sign-out.
- [ ] Account state persistence.
- [ ] Token access for Drive backup.
- [ ] Graceful offline/error handling.

### Billing Checklist

- [ ] Add Play Billing Library.
- [ ] Product IDs:
  - [ ] `phone_down_pro_monthly`.
  - [ ] `phone_down_pro_yearly`.
  - [ ] `phone_down_pro_lifetime`.
- [ ] Load subscription products.
- [ ] Load lifetime product.
- [ ] Purchase monthly.
- [ ] Purchase yearly.
- [ ] Purchase lifetime.
- [ ] Restore purchases.
- [ ] Manage subscription entry.
- [ ] Handle pending purchases.
- [ ] Handle canceled purchases.
- [ ] Handle billing unavailable state.

### Entitlement Checklist

- [ ] User is Pro with active monthly subscription.
- [ ] User is Pro with active yearly subscription.
- [ ] User is Pro with owned lifetime purchase.
- [ ] Expired/canceled subscription removes Pro access after entitlement expiry.
- [ ] Lifetime purchase does not expire.
- [ ] Do not backup billing entitlement as source of truth.

### Paywall Checklist

- [ ] Advanced analytics gate.
- [ ] Heatmap gate.
- [ ] Backup/restore gate.
- [ ] Export gate.
- [ ] Advanced custom durations gate.
- [ ] Avoid showing paywall before user experiences core timer.
- [ ] Keep paywall calm, clear, and non-aggressive.

### Acceptance Criteria

- [ ] Free user can use unlimited focus sessions.
- [ ] Free user sees today and 7 days insights.
- [ ] Pro user unlocks advanced features.
- [ ] Billing failures do not break core timer.

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

- [ ] Require Pro entitlement.
- [ ] Require signed-in Google account.
- [ ] Write backup to Google Drive app data folder.
- [ ] Show success state.
- [ ] Show failure state.
- [ ] Update last backup time.

### Auto Backup Checklist

- [ ] Require Pro entitlement.
- [ ] Require user opt-in.
- [ ] Run once daily.
- [ ] Prefer WorkManager.
- [ ] Avoid backing up more frequently after every session.
- [ ] Respect network/battery constraints as appropriate.
- [ ] Show last successful backup time.

### Restore Checklist

- [ ] Require Pro entitlement.
- [ ] Require signed-in Google account.
- [ ] Fetch latest backup.
- [ ] Validate schema version.
- [ ] Merge sessions by ID.
- [ ] Avoid duplicates.
- [ ] Preserve newer local records on conflict.
- [ ] Restore settings carefully.
- [ ] Show restore summary.
- [ ] Handle no-backup-found state.

### Tests

- [ ] Backup serialization.
- [ ] Backup deserialization.
- [ ] Schema validation.
- [ ] Duplicate merge.
- [ ] Newer local record preservation.
- [ ] Missing/corrupt backup handling.

### Acceptance Criteria

- [ ] Core app works without backup.
- [ ] Backup is opt-in and Pro-only.
- [ ] Restore does not duplicate sessions.
- [ ] Restore does not override newer local data.

## 17. Phase 13 - Privacy, Security, And Data Deletion

Purpose: keep the app trustworthy and avoid accidental exposure of user data or credentials.

### Checklist

- [ ] No advertising SDKs.
- [ ] No unnecessary tracking SDKs.
- [ ] No raw sensor data persisted unless needed for debug builds only.
- [ ] No secrets committed.
- [ ] OAuth config handled safely.
- [ ] Billing product IDs can be committed; credentials cannot.
- [ ] Delete local data flow.
- [ ] Delete cloud backup flow if implemented.
- [ ] Privacy explanation in Settings.
- [ ] Privacy policy draft/link before release.

### Git Safety Checklist Before Every Commit

- [ ] Run `git status`.
- [ ] Run `git diff --cached`.
- [ ] Check staged filenames for sensitive patterns.
- [ ] Check docs/examples for credentials.
- [ ] Confirm `.gitignore` protects env files, backups, keys, and generated secrets.
- [ ] Avoid broad staging commands when unrelated or sensitive files may be present.
- [ ] Remember the Feb 7, 2026 `.env.bak` incident as a standing reminder.

### Acceptance Criteria

- [ ] No secrets in tracked files.
- [ ] User can understand local vs cloud data behavior.
- [ ] User can delete local data.
- [ ] Backup data is not created without user/account/Pro flow.

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

- [ ] Phase 0: Repository and tooling foundation.
- [ ] Phase 1: Multi-module architecture.
- [ ] Phase 2: Design system shell.
- [ ] Phase 3: Local persistence.
- [ ] Phase 4: Session domain engine.
- [ ] Phase 5: Sensor engine.
- [ ] Phase 6: Foreground service.
- [ ] Phase 7: Focus feature.
- [ ] Phase 8: Onboarding.
- [ ] Phase 9: Insights.
- [ ] Phase 10: Settings.
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
- [ ] Whether debug-only sensor diagnostics should be included for internal testing builds.
