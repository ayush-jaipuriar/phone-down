# Agent Handoff Summary

## 1. Goal

- **Objective:** Execute a UI/UX polish pass on the Phone Down Android app to align the current UI with original design mockups (`ui-mockups/dark-mode.png`, `ui-mockups/light-mode.png`).
- **Scope:** 16 improvement items across 4 priority tiers (P0–P3) defined in `ui-polish-implementation-plan.md`.
- **Current Status:** All 16 items implemented and verified across 4 phases. Zero behavioral regressions.

## 2. Context The Next Agent Must Know

- **Project:** Android app built with Jetpack Compose, Hilt DI, Kotlin coroutines, Paparazzi screenshot testing, ktlint.
- **Architecture:** Modular Gradle project — `:app`, `:feature:*`, `:core:*`, `:domain:*` modules. Feature modules are UI-only; domain modules contain use cases; core modules contain shared design system, charts, sensors, etc.
- **Design System:** `PhoneDownDesign` object in `core/designsystem` provides all colors. `PhoneDownComponents.kt` provides reusable composables (`PhoneDownButton`, `PhoneDownCard`, `PhoneDownProgressRing`, `PhoneDownIconButton`, etc.).
- **Navigation:** Bottom-tab navigation (Focus, Insights, Settings) via Jetpack Navigation Compose. Icons from `material.icons.extended`.
- **FocusScreen State Machine:** `FocusPresentationState` enum drives all UI states (Idle, ReadyToFocus, WaitingForPhoneDown, Arming, Active, PausedByPickup, PausedByUser, PausedByCall, CompletedClean, CompletedInterrupted, EndedEarly, Broken, Invalid, SensorUnavailable).
- **Pause/Add Time** are UI-only toggles in Phase 2 — domain engine integration (`ManualPauseRequested`) is deferred. Pause sets a local flag; timer display still runs.
- **Composable naming:** All composables use PascalCase (e.g., `FocusScreen`, `SessionCompleteContent`). ktlint flags this as `function-naming` violations project-wide — this is a deliberate convention, not a bug.
- **Paparazzi:** Screenshot testing uses `recordPaparazziDebug` / `verifyPaparazziDebug` tasks. Baselines in `feature/*/src/test/snapshots/images/`. Device config: `DeviceConfig.PIXEL_5`.

## 3. Work Completed

### Phase 1 — Design System Foundation (6 items)
- **Pill-shaped buttons:** `PhoneDownButtonShape = RoundedCornerShape(percent=50)` in `PhoneDownFoundation.kt`
- **Card borders removed:** border call removed from `PhoneDownCard` in `PhoneDownComponents.kt`
- **Timer text enlarged:** `PhoneDownTimerTextStyle` at 52sp added to `PhoneDownFoundation.kt`
- **Progress ring dot:** `drawCircle()` at arc tip in `PhoneDownProgressRing`
- **Bottom nav icons:** Replaced text "F/I/S" with `Icons.Rounded.Adjust/BarChart/Settings`. Added `icon: ImageVector` to `PhoneDownBottomTab`.
- **Settings gear icon:** Conditional `IconButton` in `FocusScreen` top bar (Idle state only), wired to Settings route.
- **Dependency:** Added `androidx-compose-material-icons-extended` to `gradle/libs.versions.toml`, `:app`, and `:feature:focus`.

### Phase 2 — P1 High-Impact Features (5 items)
- **Settings restructuring:** 6 sections collapsed into 3 (Focus / Account / About). Chevrons (→) on navigable rows via new `showChevron` param on `PhoneDownSettingRow`. Destructive "Delete All Data" row in red via new `destructive` param.
- **Calendar strip:** New `InsightsCalendarStrip` composable — Mon–Sun week row with today highlight. Day selection changes "Today" label. "← Back to Today" affordance. Added `selectedDateEpochDay` to `InsightsUiState`.
- **Interruption redesign:** New `PausedByPickupActions` composable — "Phone Picked Up" title in danger red, countdown timer (5→0) with yellow→red transition, Canvas-drawn phone pickup illustration. `graceRemainingSeconds` tracked in `FocusViewModel` via `interruptionStartTime`.
- **Ready to Focus screen:** New `ReadyToFocusContent` composable — "Ready to focus?" title, 3-step instruction list, Canvas-drawn phone illustration, Cancel button. `SessionState.Created` now maps to `ReadyToFocus` in `mapToPresentationState()`. Session starts on next sensor face-down reading.
- **Pause & Add Time:** New `PausedByUser` state. Active state shows End/Pause/Add Time icon buttons. Paused state shows End/Resume. Add Time shows +1m/+5m/+15m chip row. Local-only `addTimeSeconds` in `LocalViewState`.

### Phase 3 — Rich Session Complete Screen + Celebration Copy (P0)
- **SessionCompleteContent:** Replaced flat `ResultState` composable. Features 96dp Canvas-drawn green circle + white checkmark for clean completions, "Great focus!" title, time breakdown rows (Focus Time / Penalty Time / Total Time), "✓ Clean Session" badge.
- **Copy map:** CompletedClean→"Great focus!", CompletedInterrupted→"Session complete", EndedEarly→"Session ended early", Broken→"Session broken", Invalid→"Not enough focus time to count".
- **CompletionCircle:** Canvas composable drawing filled circle + clean stroked checkmark path with `StrokeCap.Round`/`StrokeJoin.Round`.

### Phase 4 — Remaining P2/P3 Items (3 items)
- **Arming countdown:** `ArmingCountdown` composable — "3, 2, 1" animated countdown in progress ring center using `AnimatedContent` + `LaunchedEffect` with 1s `delay`. Shows "Get ready..." / "Hold still" label.
- **Hourly focus chart:** New `GetHourlyFocusUseCase` aggregates `validFocusSeconds` by hour. New `PhoneDownHourlyChart` Canvas composable (24 bars, labels at 0/6/12/18). Integrated as `HourlyChartSection` in `InsightsContent` below Today card. New `HourFocus` data class in `InsightModels.kt`.
- **Calendar day historical data:** New `GetDayInsightsUseCase` fetches `InsightSummary` for any `LocalDate`. `InsightsViewModel.onDaySelected()` now fetches and stores `selectedDaySummary`. `TodaySection` uses `selectedDaySummary ?: today`. "Back to Today" resets to null.
- **DI:** Added `@Provides` methods for `GetHourlyFocusUseCase` and `GetDayInsightsUseCase` in `AppRuntimeModule.kt`.

### Verification (all phases)
- `./gradlew :app:assembleDebug` — ✅ PASS (clean build)
- `./gradlew :domain:insights:test` — ✅ PASS
- `./gradlew :feature:focus:testDebugUnitTest` — ✅ PASS
- `./gradlew :feature:insights:testDebugUnitTest` — ✅ PASS
- `./gradlew :feature:settings:testDebugUnitTest` — ✅ PASS
- `./gradlew :feature:onboarding:testDebugUnitTest` — ✅ PASS
- `./gradlew :app:testDebugUnitTest` — ✅ PASS
- `./gradlew :feature:*:verifyPaparazziDebug` (all 4 modules) — ✅ PASS
- `./scripts/check.sh` — ⚠️ 7 ktlint failures, all pre-existing or following project-wide conventions (see Known Issues)

## 4. Current Workspace State

- **Branch:** `main`
- **Commits:** 7bd57a1 is HEAD ("Refactor Focus Feedback and Sensor Validity Logic"). All Phase 1–4 changes are UNCOMMITTED.
- **Modified files (41):** 32 source files, 5 plan `.md` files, 4 new files untracked. See `git status --short` for full list.
- **Untracked files:** `phase-{1,2,3,4}-ui-polish-plan.md`, `ui-polish-implementation-plan.md`, `PhoneDownHourlyChart.kt`, `GetDayInsightsUseCase.kt`, `GetHourlyFocusUseCase.kt`, `InsightsCalendarStrip.kt`, 4 Paparazzi baseline PNGs.
- **Sensitive files:** None detected. No `.env`, `.key`, `.pem`, or credential files in workspace.
- **NOTE:** Several non-UI files (AccountRoute.kt, AccountViewModel.kt, ProViewModel.kt, SettingsViewModel.kt, SecurityUtils.kt, SecureLogger.kt, CertificatePinningConfig.kt, test files) appear modified but changes are formatting-only from `ktlintFormat` auto-format. These should ideally be reverted before committing UI changes unless explicitly wanted.

### 2026-05-06 Workspace Recheck
- The real workspace on 2026-05-06 does **not** exactly match the earlier count in this handoff:
  - `git status --short` currently shows 42 tracked modifications plus 11 untracked files
  - several Paparazzi PNG baselines are already tracked and modified, not newly untracked
  - `app/build.gradle.kts`, `app/src/main/java/phonedown/app/focus/FocusRoute.kt`, and `app/src/main/java/phonedown/app/insights/InsightsRoute.kt` are also modified and were not called out in the earlier summary
- Review result for the likely formatting-only bucket:
  - `app/src/main/java/phonedown/app/account/`
  - `app/src/main/java/phonedown/app/pro/`
  - `app/src/main/java/phonedown/app/security/`
  - `app/src/test/`
  - These diffs currently read as formatting/reflow-only changes rather than substantive product logic changes.
  - No reverts have been performed yet; treat them as deliberate workspace changes until the user explicitly asks to clean them up.

## 5. Decisions And Rationale

| Decision | Rationale |
|----------|-----------|
| Batch all design system changes (Phase 1 items 1-4) before navigation changes | Minimize Paparazzi regeneration cycles |
| `RoundedCornerShape(percent=50)` for pill buttons vs fixed radius | Matches mockup exactly; scales correctly across button sizes |
| Canvas-drawn illustrations (phone, checkmark) vs XML drawables | No external asset files needed; fully theme-aware; simpler |
| `SessionState.Created` → `ReadyToFocus` mapping | Reuses existing session lifecycle; no new domain states needed |
| Pause is UI-state toggle only (no domain engine changes) | Deferred `ManualPauseRequested` to avoid scope creep in Phase 2 |
| Chevron as text "→" vs Material icon in Settings | Avoids adding `material-icons-extended` dependency to `:feature:settings` |
| Time breakdowns derived from existing `FocusUiState` fields | `selectedDurationSeconds`, `remainingSeconds`, `penaltySeconds` already available; no new data layer fields |

## 6. Known Issues / Blockers

- **ktlint:** 7 modules fail `ktlintMainSourceSetCheck` in `./scripts/check.sh`. All failures are pre-existing or follow project-wide conventions:
  - `function-naming` — PascalCase composables (affects all feature modules + charts). Deliberate convention.
  - `chain-method-continuation` / `multiline-expression-wrapping` — `android.graphics.Paint().apply {}` pattern in Canvas code. Pre-existing in all chart files.
  - `indent` — Continuation indentation inconsistencies in ViewModels. Pre-existing in all ViewModels.
  - No new violations were introduced beyond the project's existing style patterns.
- **Chromium rendering artifacts:** Paparazzi screenshots occasionally show minor font-rendering differences on different machines. Baselines recorded on mac OS.
- **Test assertion mismatch:** `SettingsScreenTest.kt` has a pre-existing broken assertion in `deleteDataDialogShowsAndDismisses()` — dialog body text doesn't match test expectation. Not addressed in this scope.
- **Add Time is cosmetic only:** Selecting time adds to local display timer but doesn't extend the actual domain session. Full integration deferred.

## 7. Exact Next Steps

1. **Review auto-formatted files:** Run `git diff -- app/src/main/java/phonedown/app/account/ app/src/main/java/phonedown/app/pro/ app/src/main/java/phonedown/app/security/ app/src/test/` to check formatting-only changes. Revert if unwanted: `git checkout -- <file>`.

2. **Commit Phase 1–4 changes:** Stage and commit UI polish changes (excluding auto-formatted files if reverted). Suggested message: "feat: implement full UI/UX polish across all 16 items (Phases 1-4). Design system updates, bottom nav icons, settings gear, settings restructuring, calendar strip, interruption redesign, ready screen, pause/add-time, rich completion screen, arming countdown, hourly chart, calendar day insights."

3. **Update documentation:** Refresh `v1-implementation-plan.md`, `ui-polish-implementation-plan.md`, and `docs/design-system.md` to mark completed items and document new components.

4. **Run final check:** `./gradlew :app:assembleDebug && ./gradlew :feature:*:verifyPaparazziDebug && ./scripts/check.sh`

5. **Optional — Domain engine integration:** Wire Pause/Add Time to actual session engine (`ManualPauseRequested` input, `addTime()` method).

6. **Optional — Deferred items:** The Phase 2 ktlint-flagged ktlintFormat-on-modified files should be reviewed and either committed or reverted before merging.

## 8. Suggested Prompt For The Next Agent

```
Read docs/agent-handoff.md to understand the current state of the Phone Down UI/UX polish work. Then inspect the repo: run `git status`, `git diff --stat`, and read any untracked phase plan .md files. All 16 UI/UX polish items have been implemented and verified across 4 phases — they are uncommitted on branch `main`.

Your first tasks:
1. Review the auto-formatted files (AccountRoute, AccountViewModel, ProViewModel, SettingsViewModel, SecurityUtils, SecureLogger, CertificatePinningConfig, test files) — these may have ktlint formatting changes only. Decide whether to revert them.
2. Stage and commit the Phase 1-4 changes using explicit paths (not `git add .`).
3. Update `v1-implementation-plan.md` and `ui-polish-implementation-plan.md` to mark all items complete.
4. Run `./gradlew :app:assembleDebug && ./gradlew :feature:*:verifyPaparazziDebug` to confirm no regressions.

Do NOT expand scope or implement new features without asking the user. Follow the AGENTS.md workflow for any new phases.
```

## 9. 2026-05-06 Device QA Progress

- Real-device adb QA continued on serial `192.168.1.14:40565` (RMX3686, Android 15).
- Verified from a fresh uninstall/reinstall state:
  - onboarding completes and routes to Focus home
  - relaunch skips onboarding
  - Insights and Settings surfaces render correctly
  - notification permission deny path returns to idle and does not start the service
  - notification permission allow path starts `FocusSessionService` and enters waiting state
  - waiting-state `Cancel` returns to idle and stops the service
  - Home/background + relaunch returns to the same waiting state while service stays alive
  - `am force-stop` kills the service and relaunch returns to idle cleanly
- `dumpsys notification --noredact` confirmed the `phone_down_focus_runtime` foreground notification includes the `End Session` action and `Waiting for phone down` body text.
- Remaining literal finger-on-device checks:
  - incoming-call interruption behavior only
- User subsequently verified on-device that:
  - the notification shade `End Session` action works
  - dimming feel / brightness quality is acceptable on RMX3686
- Current blocker for finishing the last device-only QA item:
  - the test device has no SIM, so PSTN call interruption could not be exercised yet
- Important observation: the home/insights session count increased after waiting-state cancel / force-stop flows. Treat this as a product-semantics question to verify, not yet as a confirmed bug.

## 10. 2026-05-06 Small UI Cleanup

- Removed the redundant top-right Settings gear from the Focus screen now that Settings is already a bottom-tab destination.
- Cleaned up the now-unused callback plumbing in:
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - `app/src/main/java/phonedown/app/focus/FocusRoute.kt`
  - `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt`
- Verification note:
  - initial `./gradlew --no-configuration-cache :app:assembleDebug` attempt hit the pre-existing build-logic Kotlin DSL parsing issue
  - cleaned generated build-logic artifacts at `build-logic/convention/build` and `build-logic/.gradle`
  - reran `./gradlew --no-configuration-cache :app:assembleDebug` successfully
  - installed updated debug APK on RMX3686 and visually confirmed on-device that the Focus top bar no longer shows the gear icon while the bottom `Settings` tab remains available

## 11. 2026-05-06 Updated Mock Hierarchy Pass

- Applied the newer mockup hierarchy pass focused on stronger headers across Focus, Insights, and Settings.
- Key interpretation of the new mock:
  - top-level screen titles should read more prominently
  - section headers like `Focus` / `Account` should feel like real headings, not subdued labels
  - Insights card headers such as `7 Day Overview`, `Focus Quality`, and `Best Focus Time` should carry more emphasis
  - the same hierarchy shift should apply in dark mode without separate one-off styling
- Implemented via shared design tokens plus targeted screen usage:
  - added `PhoneDownScreenTitleTextStyle`
  - added `PhoneDownSectionHeaderTextStyle`
  - added `PhoneDownCardHeaderTextStyle`
  - updated `PhoneDownTopBar` to use the stronger title token
  - updated Settings section headers and row-title weight
  - updated Focus state titles like `Ready to focus?`, `Phone Picked Up`, completion titles, and the `Phone Down to begin` label
  - updated Insights card/header labels to use stronger card-header styling
- Files touched for this pass:
  - `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownFoundation.kt`
  - `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownComponents.kt`
  - `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownSettingsComponents.kt`
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt`
  - `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsScreen.kt`
  - `docs/design-system.md`
- Verification completed:
  - `./gradlew --no-configuration-cache :app:assembleDebug`
  - `./gradlew --no-configuration-cache :feature:focus:recordPaparazziDebug :feature:insights:recordPaparazziDebug :feature:settings:recordPaparazziDebug`
  - `./gradlew --no-configuration-cache :feature:focus:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug`
- Live device spot-check completed on RMX3686:
  - installed the updated debug APK
  - verified on-device dark theme captures for Focus, Insights, and Settings
  - confirmed the stronger header hierarchy is visible in the real app, not just Paparazzi output

## 12. 2026-05-06 Focus Idle Polish And Feedback Tuning

- Applied one additional polish pass on the Focus idle screen to better match the updated mock:
  - made the duration chooser feel more interactive by adding a downward chevron next to the preset label
  - strengthened the `TODAY` summary label hierarchy so it reads less like faint metadata and more like a card heading
- Implemented a more noticeable and more polished audio path for focus session feedback:
  - replaced the quiet `ToneGenerator`-only start/end behavior with short packaged chime assets loaded through `SoundPool`
  - retained tone fallback behavior in case the custom sounds fail to load on a device
  - slightly strengthened the corresponding haptic waveforms for start and completion so the full feedback package feels more intentional
- Files touched for this pass:
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - `core/notifications/src/main/kotlin/phonedown/core/notifications/FocusFeedbackPlayer.kt`
  - `core/notifications/src/main/res/raw/focus_start_chime.wav`
  - `core/notifications/src/main/res/raw/focus_complete_chime.wav`
- Verification completed:
  - `./gradlew --no-configuration-cache :app:assembleDebug`
  - `./gradlew --no-configuration-cache :feature:focus:recordPaparazziDebug`
  - `./gradlew --no-configuration-cache :feature:focus:verifyPaparazziDebug`
- Device update completed:
  - installed updated debug APK on `192.168.1.14:40565`
  - next manual check needed from the user: judge start/end sound audibility and feel on real hardware

## 13. 2026-05-06 Focus Header Removal And Premium Sound Revision

- Refined the Focus screen further based on live-feedback review:
  - removed the redundant top-left `Phone Down` header from the Focus screen entirely instead of leaving an empty top bar
  - this intentionally shifts the ring/content upward and makes the screen feel cleaner and less repetitive now that bottom navigation already anchors context
- Reworked the focus start/end cues to feel louder, warmer, and more premium:
  - regenerated both packaged chime assets with fuller lower-register tones, staggered harmonic layers, gentler fades, and light echo for a more polished feel
  - kept `SoundPool` for low-latency playback, but raised the playback priority and strengthened fallback behavior
  - increased start/completion haptic confidence so the audio and touch feedback feel like one designed gesture
  - switched the custom audio content type to `CONTENT_TYPE_MUSIC` while keeping sonification usage, which helps the cues feel less like sterile system beeps
- Files touched for this pass:
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - `core/notifications/src/main/kotlin/phonedown/core/notifications/FocusFeedbackPlayer.kt`
  - `core/notifications/src/main/res/raw/focus_start_chime.wav`
  - `core/notifications/src/main/res/raw/focus_complete_chime.wav`
  - `feature/focus/src/test/snapshots/images/phonedown.feature.focus_FocusScreenScreenshotTest_idleState_Light.png`
  - `feature/focus/src/test/snapshots/images/phonedown.feature.focus_FocusScreenScreenshotTest_idleState_Dark.png`
  - `feature/focus/src/test/snapshots/images/phonedown.feature.focus_FocusScreenScreenshotTest_activeState_Dark.png`
  - `feature/focus/src/test/snapshots/images/phonedown.feature.focus_FocusScreenScreenshotTest_pausedState_Dark.png`
- Verification completed:
  - `./gradlew --no-configuration-cache :app:assembleDebug`
  - `./gradlew --no-configuration-cache :feature:focus:recordPaparazziDebug`
  - `./gradlew --no-configuration-cache :feature:focus:verifyPaparazziDebug`
  - `git diff --check`
- Device update completed:
  - installed updated debug APK on `192.168.1.14:40565`
  - next manual check needed from the user: evaluate whether the start/end sound is now loud enough and premium enough on real hardware

## 14. 2026-05-06 Audible Cue Cleanup And Portrait Lock

- Investigated user-reported mismatch where the app still played a generic beep even after the premium start/end chimes were added.
- Root cause:
  - the runtime emits a distinct `PhoneDownDetected` feedback event before `TimerStarted`
  - that early event still used the generic tone path, so users could hear a stray system-like beep before the nicer focus-start cue
- Fix applied:
  - `PhoneDownDetected` is now haptic-only and no longer plays an audible cue
  - meaningful milestone sounds remain on the richer custom path for `TimerStarted` and `SessionCompleted`
- Locked the app to portrait orientation at the Android activity level:
  - `MainActivity` now declares `android:screenOrientation="portrait"`
  - this keeps the app in portrait even when device auto-rotate is enabled
- Files touched for this pass:
  - `core/notifications/src/main/kotlin/phonedown/core/notifications/FocusFeedbackPlayer.kt`
  - `app/src/main/AndroidManifest.xml`
- Verification completed:
  - `./gradlew --no-configuration-cache :app:assembleDebug`
  - `git diff --check`
- Next manual check needed from the user:
  - confirm the stray beep is gone and only the intended premium cue remains
  - confirm rotating the device no longer rotates the app UI

## 15. 2026-05-06 Headerless Tab Shell Refinement

- Removed the redundant top header bars from the primary tab-root screens:
  - Focus already had its top header removed in the earlier pass
  - Insights no longer renders a duplicate `Insights` title above the content
  - Settings no longer renders a duplicate `Settings` title above the content
- To keep the result feeling intentional rather than abruptly cropped, generalized `PhoneDownScreen`:
  - added a `topPadding` parameter with a default value so secondary/detail screens can keep the fuller header-led spacing
  - tab-root screens (`Focus`, `Insights`, `Settings`) now opt into a slightly tighter top inset for a smoother, more modern feel
- Files touched for this pass:
  - `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownComponents.kt`
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt`
  - `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsScreen.kt`
- Verification completed:
  - `./gradlew --no-configuration-cache :app:assembleDebug`
  - `./gradlew --no-configuration-cache :feature:focus:recordPaparazziDebug :feature:insights:recordPaparazziDebug :feature:settings:recordPaparazziDebug`
  - `./gradlew --no-configuration-cache :feature:focus:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug`
  - `git diff --check`

## 16. 2026-05-10 Product QA Audit And Phase 15 Trust Hotfix Plan

- Completed a PM/QA-style audit of the current Phone Down codebase and captured the findings in `docs/product-qa-audit-2026-05-10.md`.
- Highest-priority trust gaps identified:
  - Pause and Add Time are currently UI-only and do not affect the session engine.
  - Restore reports success after deserializing/counting backup data but does not write restored sessions/settings locally.
  - Call interruption support silently no-ops without `READ_PHONE_STATE` permission.
  - Notification open-to-Focus routing only works reliably for cold start, not warm `onNewIntent` cases.
  - Some Settings rows are tappable but not backed by real behavior.
  - Focus and Insights can use different "today sessions" counting rules.
- User clarified Phase 15 decisions:
  - Make Pause/Add Time real end to end.
  - Restore should fully replace local data.
  - Request call permission only after educating the user.
  - Notification taps should always route to Focus.
  - Hide or disable dead Settings rows unless they are real.
  - Count completed, ended early, invalidated, and broken sessions; exclude abandoned and pre-start waiting sessions.
- Created `phase-15-trust-hotfix-plan.md` with detailed workstreams, checklists, verification plan, risks, and acceptance criteria.
- User approved the plan and Phase 15 implementation is now complete.
- Implementation highlights:
  - real Pause/Add Time in `SessionEngine`, `ActiveSessionRuntimeCoordinator`, and `FocusViewModel`
  - persisted `PausedByUser` session state and `ManualPause` event type with stable storage strings
  - real full-replace restore through `RestoreBackupUseCase`, `RestorePayload`, Room bulk replacement, and `SettingsRepository.restoreSettings`
  - backup JSON enum storage switched from Kotlin enum names to stable strings
  - notification taps now route to Focus for warm-start intents via `MainActivity.onNewIntent()` and a Compose flow
  - Settings now educates before requesting `READ_PHONE_STATE`
  - dead Settings rows for Terms, Support, Export Data, and Auto Backup were removed
  - Focus today's metrics now reuse `GetTodayInsightsUseCase.summarize()`
- Verification passed:
  - `./gradlew --no-configuration-cache :domain:session:test :domain:insights:test :core:backup:testDebugUnitTest :core:database:testDebugUnitTest :app:testDebugUnitTest :feature:focus:testDebugUnitTest :feature:settings:testDebugUnitTest :app:assembleDebug`
  - `./gradlew --no-configuration-cache :feature:settings:verifyPaparazziDebug :feature:focus:verifyPaparazziDebug`
  - `git diff --check`
- Verification blocked:
  - `./gradlew --no-configuration-cache :feature:settings:connectedDebugAndroidTest` could not run because Gradle reported `No connected devices!`.
  - `./scripts/check.sh` still fails on existing ktlint policy disagreements and PascalCase Compose naming conventions documented earlier in the project.
- Next step: install on a real attached device and manually test Pause/Add Time, full restore, notification tap-to-Focus, and call-permission education.

## 17. 2026-05-10 Phase 16 Android Production Readiness Plan

- User requested a plan to complete all fake/deferred production items and reach Android Play Store production readiness.
- User confirmed Phase 16 should include:
  - real Google Sign-In
  - real Google Drive backup/restore
  - real Google Play Billing for monthly, yearly, and lifetime Pro
  - release signing and Play Store upload readiness
  - certificate pin/security cleanup
  - production privacy/security cleanup
  - final Play Store metadata/assets review
- Research completed against current official/commercial references:
  - Play Console has a one-time US$25 registration fee.
  - New personal Play developer accounts may require closed testing with at least 12 opted-in testers for 14 continuous days before production access.
  - Drive `appDataFolder` is the recommended hidden app-specific backup store and requires the narrow `drive.appdata` scope.
  - Play Billing supports recurring subscriptions and one-time products.
  - Comparable focus pricing supports an India launch recommendation of INR 99/month, INR 799/year, and INR 1,999 lifetime.
- Created `phase-16-android-production-readiness-plan.md`.
- Key recommendations in the plan:
  - create a dedicated Google Cloud/Firebase project for Phone Down rather than reusing `only-yours`
  - use Drive `appDataFolder`
  - implement once-daily auto-backup with WorkManager
  - keep Room database unencrypted for V1
  - add Firebase Crashlytics with minimal disclosed diagnostics
  - keep 24-hour entitlement cache
  - use internal testing first, then closed testing, then production rollout
- No production implementation has started yet.
- Next step: user should review and approve `phase-16-android-production-readiness-plan.md`; after approval, begin Sprint 16.1 with Play Console/Firebase/OAuth/signing setup docs and account handholding.
