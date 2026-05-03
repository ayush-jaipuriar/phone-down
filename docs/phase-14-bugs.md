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
| | | | | |

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
