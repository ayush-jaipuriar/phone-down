# Phase 14 Bug Documentation

## Instructions

This file documents bugs discovered during Phase 14 code review and manual device testing.

## Fixed Issues (Code Review + Fixes)

### Issue 1: Potential NPE in FakeBackupRepository
- **Severity**: Low
- **Location**: `core/backup/src/main/kotlin/phonedown/core/backup/FakeBackupRepository.kt:36`
- **Issue**: Used `!!` operator on `lastBackupTime` which was set on the previous line. While safe in this specific case, it's bad practice.
- **Fix**: Extracted timestamp to a local variable before assignment and return.
- **Status**: ✅ Fixed

### Issue 2: Potential NPE in InsightsContent
- **Severity**: Low
- **Location**: `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt:346`
- **Issue**: Used `!!` operator after null check. Smart cast was blocked by module boundary.
- **Fix**: Used safe call `?.name ?: "Active"` instead of `!!`.
- **Status**: ✅ Fixed

### Issue 3: Potential NPE in FocusScreen
- **Severity**: Low
- **Location**: `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt:578`
- **Issue**: Used `!!` operator on `customDurationSeconds` inside button click. Button is disabled when null, but `!!` is still risky.
- **Fix**: Replaced with safe call `customDurationSeconds?.let { onSelect(it) }`.
- **Status**: ✅ Fixed

### Issue 4: runBlocking in Service onDestroy
- **Severity**: Medium
- **Location**: `app/src/main/java/phonedown/app/runtime/FocusSessionService.kt:133-140`
- **Issue**: `runBlocking` used in `onDestroy()` to flush session state without timeout or error handling.
- **Fix**: Added `withTimeout(2_000L)` and try-catch to prevent ANR if DB write hangs. Added explanatory comment.
- **Status**: ✅ Fixed

### Issue 5: System Icons in Notifications
- **Severity**: Low
- **Location**: `core/notifications/src/main/kotlin/phonedown/core/notifications/FocusForegroundNotificationManager.kt:42,50`
- **Issue**: Used system drawables (`android.R.drawable.ic_lock_idle_alarm`, `android.R.drawable.ic_menu_close_clear_cancel`) which may not be available or consistent across all devices/Android versions.
- **Fix**: Created custom vector drawables (`ic_notification_focus.xml`, `ic_notification_end.xml`) in `core:notifications` module.
- **Status**: ✅ Fixed

### Issue 6: Missing Room Migration Strategy
- **Severity**: Medium
- **Location**: `core/database/src/main/kotlin/phonedown/core/database/di/DatabaseModule.kt:26-31`
- **Issue**: Room database built without `.fallbackToDestructiveMigration()` or any migration strategy. App would crash on schema upgrade.
- **Fix**: Added `.fallbackToDestructiveMigration()` to Room builder. Acceptable for V1 (no production users).
- **Status**: ✅ Fixed

### Issue 8: Notification Permission Edge Case
- **Severity**: Medium
- **Location**: `app/src/main/java/phonedown/app/MainActivity.kt:38-43,96-105`
- **Issue**: `pendingStartDurationSeconds` could become stale if user granted permission through system settings later.
- **Fix**: Clear `pendingStartDurationSeconds` immediately after reading in permission callback and in `onDestroy()`.
- **Status**: ✅ Fixed

## Known Issues (Pending Manual Testing / Product Decision)

### Issue 7: allowBackup in Manifest
- **Severity**: Low
- **Location**: `app/src/main/AndroidManifest.xml:10`
- **Issue**: `android:allowBackup="true"` allows Android's auto-backup to save app data. For a privacy-focused app, this might not be desired.
- **Suggested Fix**: Consider setting `android:allowBackup="false"` or explicitly defining backup rules with `android:fullBackupContent` to exclude sensitive data.
- **Status**: ⏳ Pending product decision

### Issue 9: Screen Dimming Brightness Value
- **Severity**: Low
- **Location**: `app/src/main/java/phonedown/app/MainActivity.kt:71`
- **Issue**: Screen brightness is set to `0.02f` during dimming. On some devices, this might be too dim or not dim at all depending on the screen's minimum brightness.
- **Suggested Fix**: Test on multiple devices and consider using `WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE` with a system brightness reduction or a dim overlay instead.
- **Status**: ⏳ Pending manual device testing

## Detailed Manual Testing Procedures

### Preparation
1. Install debug build: `./gradlew :app:installDebug`
2. Clear app data before each test: Settings > Apps > Phone Down > Storage > Clear Data
3. Have a stopwatch ready for timing verification

### Test 1: Basic Focus Session
**Purpose**: Verify core ritual works end-to-end
**Steps**:
1. Open app (should show Focus tab)
2. Tap "Start Focus" (default 25 min)
3. Place phone face down on stable surface
4. Wait for "Hold still..." countdown (3 seconds)
5. Verify notification shows "Focus active"
6. Wait for session to complete (or test with 1 min duration)
7. Verify completion sound plays
8. Verify session appears in Insights

**Expected**: Timer counts only while face down, notification updates correctly

### Test 2: Minor Interruption
**Purpose**: Verify 5-second grace period
**Steps**:
1. Start a session
2. Place phone face down
3. Wait for active state
4. Pick up phone for 3 seconds
5. Place phone face down again

**Expected**: Session continues, clean status removed, minor interruption recorded

### Test 3: Penalty Interruption
**Purpose**: Verify penalty after grace period
**Steps**:
1. Start a session (use 10 min for faster testing)
2. Place phone face down
3. Wait for active state
4. Pick up phone for 10 seconds
5. Place phone face down again

**Expected**: +1:00 penalty added, required duration increases, clean status removed

### Test 4: Broken Session
**Purpose**: Verify broken threshold (60s or 3 penalties)
**Steps**:
1. Start a session
2. Place phone face down
3. Pick up phone for 65 seconds

**Expected**: Session marked as broken, can continue but never clean

### Test 5: Call Interruption
**Purpose**: Verify call pause behavior
**Steps**:
1. Start a session
2. Place phone face down
3. Have someone call the test device
4. Answer call
5. End call
6. Place phone face down again

**Expected**: Session pauses during call, resumes after, clean status removed

### Test 6: App Kill Recovery
**Purpose**: Verify session recovery after force close
**Steps**:
1. Start a session
2. Place phone face down
3. Wait for active state (30+ seconds)
4. Force close app (swipe away from recents)
5. Reopen app

**Expected**: Session recovered, classified appropriately (likely broken if active for a while)

### Test 7: Device Restart Recovery
**Purpose**: Verify boot recovery
**Steps**:
1. Start a session
2. Place phone face down
3. Wait for active state
4. Restart device
5. Unlock device and open app

**Expected**: Session recovered from boot

### Test 8: Notification End Action
**Purpose**: Verify notification action works
**Steps**:
1. Start a session
2. Place phone face down
3. Pull down notification shade
4. Tap "End Session" action

**Expected**: Service stops, session ends, app shows completed/ended state

### Test 9: Permission Flows
**Purpose**: Verify notification permission handling
**Steps**:
1. Fresh install on Android 13+ device
2. Tap "Start Focus"
3. Deny notification permission
4. Verify session doesn't start
5. Go to Settings > Apps > Phone Down > Permissions
6. Grant notification permission
7. Return to app
8. Tap "Start Focus"

**Expected**: Session starts after permission granted, no stale duration issues

### Test 10: Early End Classification
**Purpose**: Verify end session classifications
**Steps**:
1. Start a 10-minute session
2. Place phone face down
3. Wait 1 minute (10%)
4. End session
5. Check classification (should be Invalidated)
6. Repeat with 3 minutes (30%) → Partial
7. Repeat with 8 minutes (80%) → Strong Partial

**Expected**: Correct classification based on completion percentage

## Device Matrix

| Device | Android Version | Tester | Date | Notes |
|---|---|---|---|---|
| Pixel_8 AVD | Android 14 (API 34) | Codex | 2026-05-03 | Onboarding completed successfully; relaunch skipped onboarding; notification permission deny/allow flows worked; start focus entered waiting state and foreground service/notification were posted; force-stop during waiting relaunched to idle and recorded an `Abandoned` session; sensor-dependent tests remain emulator-limited; notification shade action could not be exercised reliably; waiting-state controls appeared unresponsive to emulator tap/back input |
| RMX3686 | Android 15 | Codex + User | 2026-05-06 | Real-device QA confirmed fresh-install onboarding, relaunch-to-Focus, Insights/Settings surface rendering, notification permission deny/allow behavior, no foreground service before permission grant, waiting-state foreground service start, posted notification with `End Session`, waiting-state `Cancel` returning to idle, background-home/relaunch continuity back into waiting state, force-stop/relaunch recovery back to idle, successful face-down progression through to a completed focus session with correct elapsed-time pickup reporting after the gravity-based flatness fix, notification shade `End Session` action working, and dimming feel working acceptably on-device. `pm clear` was blocked by OEM shell restrictions, so clean-state resets used uninstall/reinstall. PSTN call interruption is still unverified because the test device has no SIM. |

## Edge Cases Checklist

- [ ] Airplane mode during session
- [ ] Battery saver mode
- [ ] Do Not Disturb enabled
- [ ] Low storage (< 500MB free)
- [ ] No Google account signed in
- [ ] Rapid start/stop 5 sessions in a row
- [ ] Background app during session (switch to another app)
- [ ] Screen rotation during session
- [ ] Incoming notification during session
- [ ] Device locked during session
- [ ] Different surfaces: desk, bed, couch, charging pad
- [ ] Walking with phone in pocket (should not count as valid)
- [ ] Tiny table bump (should not trigger penalty)

## Automated Tests Added

### FocusScreen Instrumented Tests
- `focusScreenIdleStateShowsStartButton` - Verify idle UI
- `focusScreenWaitingStateShowsGuidance` - Verify waiting state
- `focusScreenActiveStateShowsTimerAndEndButton` - Verify active state
- `focusScreenCompletedCleanStateShowsSuccess` - Verify completion UI
- `focusScreenBrokenStateShowsBrokenMessage` - Verify broken state
- `focusScreenStartButtonTriggersEvent` - Verify start interaction
- `focusScreenEndButtonShowsConfirmation` - Verify end confirmation
- `focusScreenDurationSelectorShowsPresets` - Verify duration picker
- `focusScreenSensorUnavailableShowsRetry` - Verify sensor error
- `focusScreenPausedStateShowsPenalty` - Verify penalty display
- `focusScreenTodayMetricsDisplay` - Verify today summary

**Location**: `feature/focus/src/androidTest/kotlin/phonedown/feature/focus/FocusScreenTest.kt`

## Bug Template for New Findings

```
### Bug #N: [Title]
- **Device**: [Device model]
- **Android Version**: [API level]
- **Severity**: [Critical / High / Medium / Low]
- **Reproduction Steps**:
  1. Step one
  2. Step two
- **Expected Behavior**:
- **Actual Behavior**:
- **Screenshots/Logs**:
- **Suggested Fix**:
```

## Emulator QA Notes (2026-05-03)

### Confirmed Passes
- First-run onboarding advanced through all 3 cards and routed to Focus home.
- Relaunch after onboarding skipped onboarding and landed directly on Focus.
- Notification permission flow worked:
  - denying permission kept the app on idle Focus and did not start the foreground session
  - granting permission allowed session start

## Real Device QA Notes (RMX3686, 2026-05-06)

### Confirmed Passes
- Fresh uninstall/reinstall reproduced first-run onboarding and advanced through all 3 cards into Focus home.
- Relaunch after onboarding skipped onboarding and returned directly to Focus.
- Insights tab rendered correctly with the weekly strip, Today summary, 7 Day Overview, and Pro upsell surface.
- Settings tab rendered correctly with Focus and Account sections, theme segmented control, toggles, and navigable rows.
- Notification permission gating behaved correctly:
  - before grant, `dumpsys activity services phonedown.app` showed no running service
  - denying permission returned the app to idle Focus and did not start the foreground session
  - granting permission started the focus runtime and moved the app into the waiting state
- Waiting-state runtime wiring behaved correctly:
  - foreground `FocusSessionService` started
  - `phone_down_focus_runtime` notification was posted with the `End Session` action present in `dumpsys notification`
  - waiting-state UI showed `Ready to focus?`, `Focused 00:00`, `Remaining 25:00`, `State Waiting`, and `Cancel`
- Waiting-state `Cancel` returned the app to idle and stopped the foreground service.
- Background continuity behaved correctly:
  - pressing Home left the foreground service running
  - relaunching the app returned it to the same waiting state instead of dropping the session
- Force-stop recovery behaved predictably:
  - `am force-stop phonedown.app` removed the service
  - relaunch returned the app to idle Focus rather than a stuck intermediate state

### Still Pending Direct Physical Interaction
- Incoming-call interruption handling (currently blocked because the test device has no SIM)

### Notable Observation
- Session totals increased after waiting-state cancel / force-stop recovery flows. This may be acceptable if abandoned or invalid attempts are intentionally counted in aggregate session metrics, but it should be sanity-checked against the intended product semantics for the `Sessions` counter in Insights and Focus home.
- Starting focus with permission granted entered the waiting state:
  - heading: `Ready to focus?`
  - body: `Place phone down to begin.`
- Foreground runtime service was confirmed via `dumpsys activity services phonedown.app`.
- Foreground notification was confirmed via `dumpsys notification --noredact` with:
  - title `Phone Down`
  - text `Waiting for phone down`
  - `End Session` action present
- Force-stop/relaunch recovery from the waiting state returned to idle Focus and the abandoned session appeared in Insights history as `Abandoned`.

### Emulator Limitations / Inconclusive
- Face-down sensor validity progression could not be validated honestly in the emulator.
- Notification shade expansion was not reliable enough to validate tapping the notification `End Session` action.
- Screenshot capture occasionally lagged the current Compose state, so UI tree dumps were more reliable than screenshots during transitions.

### Bug #1: Waiting-State Controls Appear Unresponsive On Emulator
- **Device**: Pixel_8 AVD
- **Android Version**: 14 / API 34
- **Severity**: Medium
- **Reproduction Steps**:
  1. Fresh install / clear app data
  2. Complete onboarding
  3. Start focus and grant notification permission
  4. Wait for the `Ready to focus?` waiting state
  5. Attempt to tap `Cancel`, `Insights`, `Settings`, or press system Back
- **Expected Behavior**:
  - `Cancel` should end the waiting session and stop the service
  - bottom navigation and/or Back behavior should follow the intended runtime UX
- **Actual Behavior**:
  - multiple adb tap attempts derived from the UI tree produced no visible state change
  - system Back also left the screen unchanged
- **Screenshots/Logs**:
  - UI tree continued to show the same waiting state after repeated interactions
  - no app crash was observed
- **Suggested Fix**:
  - verify on a physical device first to rule out emulator-only input handling
  - if reproducible on device, inspect the waiting-state composable and interaction layers for an input blocker or disabled click handlers during active runtime

### Follow-up Investigation (2026-05-03)
- Added focused automated coverage for the waiting-state end path:
  - `feature/focus/src/androidTest/kotlin/phonedown/feature/focus/FocusScreenTest.kt`
    - `focusScreenWaitingCancelTriggersEndEvent`
  - `app/src/test/java/phonedown/app/runtime/ActiveSessionRuntimeCoordinatorTest.kt`
    - `endSessionFromWaitingStateInvalidatesSessionAndRequestsShutdown`
- Result:
  - the runtime unit test passed, confirming that ending from `WaitingForPhoneDown` transitions to `Invalidated` and requests service shutdown
  - static code inspection also confirmed the waiting-state `Cancel` button is wired to emit `FocusEvent.EndClicked`
- Remaining uncertainty:
  - a connected emulator instrumentation rerun was inconclusive because one attempt used an unsupported test filter and a later run failed after the emulator went offline
  - current best hypothesis is that the observed manual-QA issue is either emulator-input-specific or lives in a UI integration layer above the tested event/runtime logic

## Real Device QA Notes (RMX3686 / Android 15, 2026-05-05)

### Confirmed Passes
- Fresh-install onboarding advanced through all 3 cards and routed to Focus home successfully.
- Relaunch after onboarding skipped onboarding and opened Focus directly.
- Android 15 notification permission flow behaved correctly:
  - denying permission kept the app on idle Focus and did not start the foreground service
  - allowing permission on the next attempt started the focus runtime successfully
- Starting focus with notification permission granted entered the waiting state:
  - UI changed to `Ready to focus?`
  - `FocusSessionService` was present in `dumpsys activity services`
  - the foreground notification was present in `dumpsys notification --noredact`
- Waiting-state `Cancel` worked on the real device:
  - UI returned to idle Focus
  - `dumpsys activity services phonedown.app` showed no running service afterward
- Force-stop / relaunch recovery behaved coherently:
  - after force-stopping from the waiting state, relaunch returned to idle Focus
  - Insights session history recorded the interrupted waiting session as `Abandoned`
  - the prior waiting-state cancel path appeared in Insights as `Invalidated`

### Operational Notes
- `adb shell pm clear phonedown.app` failed on this OEM device with a shell `CLEAR_APP_USER_DATA` restriction.
- Clean-state resets for QA were performed with uninstall/reinstall instead of `pm clear`.
- `adb shell monkey -p phonedown.app -c android.intent.category.LAUNCHER 1` brought the app to foreground more reliably than `am start` on this device.

### Still Pending
- True sensor-driven face-down progression and active-session timing validation
- Notification shade tap on the `End Session` action
- Call interruption behavior
- Screen dimming feel / hardware-specific brightness behavior

### Active Investigation (2026-05-05)
- Real-device testing suggests the session is often stuck in `WaitingForPhoneDown` with `0` progress rather than transitioning into active focus.
- Current working theory:
  - the sensor gate is too strict for at least some real hardware
  - the app effectively had two hold-still gates: a sensor stabilization delay and the domain arming countdown
  - the user-visible result is "phone times out and relocks before focus ever truly starts"
- Mitigations now being implemented:
  - expose waiting-state sensor rejection diagnostics in the Focus UI
  - loosen sensor thresholds and shorten pre-arming sensor stabilization so the domain `Arming` state remains the primary ritual countdown
  - add proximity as a supporting face-down hint rather than a replacement for orientation/motion logic
  - keep the screen awake during waiting/arming/active states so timeout does not sabotage startup
- Follow-up UX fix:
  - waiting, arming, active, pickup-paused, and call-paused states now show explicit `Focused`, `Remaining`, and `State` metrics
  - this makes it clear after pickup whether real focus time counted, for example `Focused 00:30` and `Remaining 04:30` for a 5-minute session
  - haptic feedback patterns were strengthened and the reusable tone generator now recreates itself after service cleanup

### Additional Sensor Fix (2026-05-05)
- User retested on RMX3686 and the app still remained in `Waiting` after roughly 1 minute face down:
  - `Focused` stayed `00:00`
  - `Remaining` stayed at the full selected duration
  - after pickup, the UI showed `Screen is facing up.`
- Root cause identified in code review:
  - `AndroidFocusValidityMonitor` was feeding rotation-vector pitch/roll into the flatness check when available
  - some devices can report a near-180-degree roll when the phone is perfectly face down
  - this made a true face-down posture look "not flat" to the validity evaluator, preventing transition into `Arming` or `Active`
- Fix implemented:
  - `FocusValidityEvaluator` now uses gravity-derived tilt as the source of truth for face-down flatness and clamps any supplemental sensor tilt to that gravity value
  - added a regression test for the inverted-roll case
  - added debug-only `PhoneDownSensors` logcat transition logging so blind face-down QA can verify whether `FaceDownStabilizing` and `FaceDownStable` were reached
- Retest result:
  - user confirmed the updated APK successfully registered face-down focus on RMX3686
  - a full focus session completed successfully
  - midway pickup showed elapsed focus time progressing correctly
  - this validates the gravity-derived flatness fix for the primary sensor-start blocker

### Focus UI Cleanup (2026-05-05)
- Removed the temporary in-UI sensor diagnosis copy that was added during hardware debugging:
  - waiting and arming states no longer render temporary sensor-status text
  - debug-only raw sensor traces remain in logcat under `PhoneDownSensors`
  - user-facing progress metrics (`Focused`, `Remaining`, `State`) remain in place because they proved useful during real-world pickup validation

### Notification Action Follow-up (2026-05-05)
- The `End Session` notification action is wired to a non-exported `FocusSessionService` start request using `ACTION_END`.
- `dumpsys notification --noredact` confirmed the notification action is posted correctly on-device.
- A direct `adb shell am start-foreground-service ... -a phonedown.app.action.END_FOCUS_SESSION` attempt was rejected with `Requires permission not exported from uid 10087`, which is expected and desirable.
- Remaining gap:
  - the exact notification-shade tap interaction on RMX3686 still needs a literal manual device tap because adb cannot legitimately invoke that private entrypoint from outside the app.
