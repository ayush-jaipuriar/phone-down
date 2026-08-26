# Phase 17: In-Depth Functional and Regression Bug Audit & Remediation Plan

## Executive Summary

A comprehensive, line-by-line audit across all 16 modules in the Phone Down codebase was performed, examining:
1. **Core Domain & Session Engine**: Session lifecycle, clock monotonic calculations, recovery across device reboots.
2. **Sensors & Detection**: Low-pass filtering, gravity normalization, rotation vector handling, stability evaluation.
3. **Runtime & Foreground Service**: Foreground service lifecycle, audio/haptic feedback player singleton management, shutdown vs UI dismissal decoupling.
4. **Data Layer (Room & DataStore)**: Transactions, DAOs, schema versions, migrations, entity mappings.
5. **Auth & Google Drive Backup / Restore**: Google ID Sign-In, Credential Manager, Drive AppData REST API, multipart uploads, Android API level compatibility.
6. **Billing & Pro Entitlement**: In-app purchase flows, subscription offer tokens, acknowledgment, entitlement caching, threading.
7. **Insights & Analytics**: Streak calculation, hourly distributions, date windowing, calendar locale awareness, snapshot test determinism.
8. **Settings & Customization**: DataStore preference persistence, ViewModel state updates, transient dialog preservation, email intent dispatch.
9. **Focus UI & Navigation**: Screen brightness / wake-lock coordination, timer format edge cases, back stack navigation, notification permission handling.

---

## Detailed Findings & Defects

### Defect 1: API Level 33 Compatibility Crash in `DriveAppDataClient` (Severity: Critical)
- **Location**: `core/backup/src/main/kotlin/phonedown/core/backup/DriveAppDataClient.kt` line 184
- **Issue**: `URLEncoder.encode(value, StandardCharsets.UTF_8)` was introduced in Java 10 / Android API 33 (Tiramisu). The app's `minSdk` is 26 (Android 8.0). On devices running Android 8.0 through Android 12L (API 26–32), attempting a Google Drive backup or query causes an immediate `NoSuchMethodError` runtime crash.
- **Root Cause**: Passing `java.nio.charset.Charset` directly to `URLEncoder.encode` rather than the `String` charset name (`StandardCharsets.UTF_8.name()` or `"UTF-8"`).
- **Remediation**: Update `urlEncode` to `URLEncoder.encode(value, StandardCharsets.UTF_8.name())`.

---

### Defect 2: Singleton `FocusFeedbackPlayer` SoundPool Resource Destruction (Severity: High)
- **Location**: `core/notifications/src/main/kotlin/phonedown/core/notifications/FocusFeedbackPlayer.kt` and `app/src/main/java/phonedown/app/runtime/FocusSessionService.kt`
- **Issue**: `FocusFeedbackPlayer` is provided as `@Singleton` in `AppRuntimeModule.kt`. Its `init` block initializes `SoundPool` and loads `focus_start_chime` and `focus_complete_chime`. When a focus session finishes, `FocusSessionService.onDestroy()` calls `feedbackPlayer.release()`, which destroys the `SoundPool`, clears `soundIds`, and clears `loadedSoundEvents`. On all subsequent sessions in the same app process, the singleton `FocusFeedbackPlayer` has a destroyed `SoundPool` and never plays custom chimes again.
- **Root Cause**: Lifecycle mismatch between singleton dependency injection scope and service component teardown.
- **Remediation**: Make `FocusFeedbackPlayer` lazily (re-)initialize its `SoundPool` and loaded sounds on demand if released, or manage resource lifecycle cleanly so subsequent sessions retain audio chime capability.

---

### Defect 3: `SettingsViewModel` Transient UI State Clobbering (Severity: High)
- **Location**: `app/src/main/java/phonedown/app/settings/SettingsViewModel.kt` lines 45–65
- **Issue**: In `SettingsViewModel`, the `init` block collects a `combine` of `settings`, `entitlement`, and `accountState`. Whenever any of these flows emit, it instantiates a brand new `SettingsUiState(...)` with default values, completely resetting `showDeleteConfirmation`, `deleteConfirmationText`, `deleteIncludeBackup`, `isDeleting`, `deleteError`, `isBackingUp`, and `backupError`. If a user is actively typing in the delete dialog or waiting on a backup operation, background emissions discard user input and close dialogs unexpectedly.
- **Root Cause**: Recreating full UI state with default values instead of updating only the repository-sourced fields of `_uiState.value`.
- **Remediation**: Refactor the collector to use `_uiState.update { current -> current.copy(defaultDurationSeconds = ..., soundEnabled = ..., ...) }`.

---

### Defect 4: Insights Calendar Non-Observable Locale & Paparazzi Flakiness (Severity: Medium)
- **Location**: `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsCalendarStrip.kt` line 69 and `InsightsContent.kt` lines 221, 417
- **Issue**: 
  1. `Locale.getDefault()` inside composable functions violates Compose best practices (`NonObservableLocale` Android lint failure).
  2. `InsightsCalendarStrip` hardcodes `LocalDate.now()` inside the composable, which changes the displayed day numbers dynamically at runtime, causing Paparazzi screenshot tests to fail whenever run on dates different from the original golden snapshots.
- **Root Cause**: Uninjected calendar reference date and unobservable locale access in Compose.
- **Remediation**: Use `LocalConfiguration.current.locales[0]` or `androidx.compose.ui.text.intl.Locale.current.platformLocale`, and accept `today: LocalDate = LocalDate.now()` in `InsightsCalendarStrip` so tests and previews can supply fixed reference dates.

---

### Defect 5: Morning Streak Reset Bug in `GetStreakUseCase` (Severity: Medium)
- **Location**: `domain/insights/src/main/kotlin/phonedown/domain/insights/GetStreakUseCase.kt` lines 47–55
- **Issue**: `computeCurrentStreak(todayEpochDay, activeDays)` checks `day in activeDays` starting strictly from `todayEpochDay`. If a user had a 10-day streak yesterday and opens the app in the morning (having not yet completed a session today), `todayEpochDay in activeDays` is false, and the streak immediately evaluates to 0 until they complete a session.
- **Root Cause**: Failing to check if yesterday has an active session before declaring the streak broken.
- **Remediation**: If `todayEpochDay !in activeDays`, start checking from `todayEpochDay - 1`. If yesterday has an active session, the streak remains intact (e.g. 10 days). If neither today nor yesterday has a session, the streak is 0.

---

### Defect 6: Stale Today Window in `FocusViewModel` Across Midnight (Severity: Medium)
- **Location**: `app/src/main/java/phonedown/app/focus/FocusViewModel.kt` lines 38–45
- **Issue**: `startOfDayMillis` and `endOfDayMillis` are evaluated once as `val` properties during ViewModel initialization. If the app is left open or backgrounded across midnight, the Focus screen continues querying yesterday's window, showing stale "Today" summary metrics.
- **Root Cause**: Static timestamp initialization in a persistent ViewModel.
- **Remediation**: Recompute the day window dynamically or reactively anchor it to the current date.

---

### Defect 7: Unsynchronized Hourly Focus on Day Selection (Severity: Medium)
- **Location**: `app/src/main/java/phonedown/app/insights/InsightsViewModel.kt` lines 55–65
- **Issue**: When `onDaySelected(epochDay)` is called in `InsightsViewModel`, `selectedDaySummary` updates, but `hourlyFocus` remains set to today's distribution.
- **Root Cause**: Missing invocation of `getHourlyFocus(LocalDate.ofEpochDay(epochDay))` during day selection.
- **Remediation**: Update `onDaySelected` to also fetch and assign `hourlyFocus` for the selected date, and restore today's hourly distribution on `onBackToToday()`.

---

### Defect 8: Notification Permission Permanent Denial Flow in `MainActivity` (Severity: Medium)
- **Location**: `app/src/main/java/phonedown/app/MainActivity.kt` lines 129–138
- **Issue**: If `POST_NOTIFICATIONS` is denied on Android 13+, `startFocusSession` returns without starting `FocusSessionService`. On subsequent taps, if the permission was permanently denied, the session never starts, locking out users who prefer not to grant notifications.
- **Root Cause**: Coupling session start strictly to notification permission approval.
- **Remediation**: Start `FocusSessionService` even if notification permission is declined by the user.

---

### Defect 9: Monotonic Clock Reset on System Reboot in `SessionRecoveryClassifier` (Severity: Low)
- **Location**: `domain/session/src/main/kotlin/phonedown/domain/session/SessionRecoveryClassifier.kt` lines 16–17
- **Issue**: `actualElapsedSeconds = ((nowElapsed - session.startElapsedRealtime).coerceAtLeast(0L) / 1000L)` uses `SystemClock.elapsedRealtime()`, which resets to 0 after device reboot. When recovering unfinalized sessions on boot, `actualElapsedSeconds` evaluates to 0.
- **Root Cause**: `elapsedRealtime` is non-continuous across reboots.
- **Remediation**: Use `maxOf(session.actualElapsedSeconds, (nowWall - session.startedAtEpochMillis) / 1000L)` when classifying recovered sessions.

---

### Defect 10: Unused Variables & Static Analysis Violations (Severity: Low)
- **Location**: `domain/insights/src/main/kotlin/phonedown/domain/insights/GetTrendsUseCase.kt` line 87 (`startDate`), detekt rules, ktlint style inconsistencies.
- **Root Cause**: Leftover debug variables and style drift.
- **Remediation**: Clean up unused variables and align formatting with ktlint/detekt standards.

---

## Implementation Plan & Checklist

- [x] **Step 1: Fix API Level 33 Compatibility in `DriveAppDataClient`**
  - [x] Replace `URLEncoder.encode(value, StandardCharsets.UTF_8)` with `URLEncoder.encode(value, StandardCharsets.UTF_8.name())`.
  - [x] Verify `core:backup:lintDebug` passes without NewApi errors.
- [x] **Step 2: Fix `FocusFeedbackPlayer` Singleton Lifecycle & SoundPool Persistence**
  - [x] Implement lazy re-initialization of `SoundPool` and audio cues in `FocusFeedbackPlayer`.
  - [x] Verify multiple sequential focus sessions play start and completion chimes consistently.
- [x] **Step 3: Fix `SettingsViewModel` State Clobbering**
  - [x] Update `SettingsViewModel.init` combine collector to use `_uiState.update { current -> current.copy(...) }`.
  - [x] Add unit test ensuring dialog open state and typed delete confirmation text survive settings repository emissions.
- [x] **Step 4: Fix `InsightsCalendarStrip` Locale Lint and Paparazzi Determinism**
  - [x] Fix `Locale.getDefault()` to use Compose-observable locale.
  - [x] Inject `today: LocalDate = LocalDate.now()` into `InsightsCalendarStrip`.
  - [x] Pass fixed reference date in `InsightsScreenScreenshotTest` and verify `:feature:insights:verifyPaparazziDebug` passes.
- [x] **Step 5: Fix Streak Calculation in `GetStreakUseCase`**
  - [x] Update `computeCurrentStreak` to retain yesterday's streak when today has no sessions yet.
  - [x] Add unit test verifying morning streak retention before first daily session.
- [x] **Step 6: Fix `FocusViewModel` Today Window Refresh & `InsightsViewModel` Day Hourly Sync**
  - [x] Compute today window dynamically on collection in `FocusViewModel`.
  - [x] Update `InsightsViewModel.onDaySelected` to load hourly distribution for the selected date.
- [x] **Step 7: Fix Notification Permission Flow in `MainActivity` & System Recovery in `SessionRecoveryClassifier`**
  - [x] Ensure `FocusSessionService` starts even when notification permission is denied.
  - [x] Use wall clock fallback in `SessionRecoveryClassifier` when elapsed monotonic clock resets.
- [x] **Step 8: Run Full Automated Verification Suite**
  - [x] `./gradlew ktlintCheck`
  - [x] `./gradlew detekt`
  - [x] `./gradlew lintDebug`
  - [x] `./gradlew testDebugUnitTest`
  - [x] `./gradlew :feature:focus:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug :feature:onboarding:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug`
  - [x] `./gradlew :app:assembleDebug`
  - [x] `./scripts/check.sh`

---

## Verification Plan

### Automated Tests
1. **Lint Check**: `./gradlew lintDebug --console=plain` (Must pass with 0 errors across all modules).
2. **Unit Tests**: `./gradlew testDebugUnitTest --console=plain` (All unit tests pass across all modules).
3. **Paparazzi Snapshot Verification**: `./gradlew verifyPaparazziDebug --console=plain` (Focus, Settings, Onboarding, and Insights screenshot tests all pass).
4. **App Build Compilation**: `./gradlew :app:assembleDebug --console=plain` (Debug APK compiles cleanly).
