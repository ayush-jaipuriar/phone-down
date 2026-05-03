# Phase 14 Bug Documentation

## Instructions

This file documents bugs discovered during Phase 14 code review and manual device testing.

## Fixed Issues (Code Review)

### Issue 1: Potential NPE in FakeBackupRepository
- **Severity**: Low
- **Location**: `core/backup/src/main/kotlin/phonedown/core/backup/FakeBackupRepository.kt:36`
- **Issue**: Used `!!` operator on `lastBackupTime` which was set on the previous line. While safe in this specific case, it's bad practice.
- **Fix**: Extracted timestamp to a local variable before assignment and return.
- **Status**: ✅ Fixed in commit

### Issue 2: Potential NPE in InsightsContent
- **Severity**: Low
- **Location**: `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt:346`
- **Issue**: Used `!!` operator after null check. Kotlin's smart cast should handle this, but `!!` is unnecessary.
- **Fix**: Removed `!!` operator - Kotlin smart cast handles it after the null check.
- **Status**: ✅ Fixed in commit

### Issue 3: Potential NPE in FocusScreen
- **Severity**: Low
- **Location**: `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt:578`
- **Issue**: Used `!!` operator on `customDurationSeconds` inside button click. Button is disabled when null, but `!!` is still risky.
- **Fix**: Replaced with safe call `customDurationSeconds?.let { onSelect(it) }`.
- **Status**: ✅ Fixed in commit

## Known Issues (Pending Manual Testing)

### Issue 4: runBlocking in Service onDestroy
- **Severity**: Medium
- **Location**: `app/src/main/java/phonedown/app/runtime/FocusSessionService.kt:135-137`
- **Issue**: `runBlocking` is used in `onDestroy()` to flush session state. While the operation is a quick DB write, blocking the main thread during service destruction is not ideal.
- **Suggested Fix**: Consider using `serviceScope.launch { }.join()` instead, though this requires careful handling since the scope is cancelled right after.
- **Status**: ⏳ Pending manual testing validation

### Issue 5: System Icons in Notifications
- **Severity**: Low
- **Location**: `core/notifications/src/main/kotlin/phonedown/core/notifications/FocusForegroundNotificationManager.kt:42,50`
- **Issue**: Uses system drawables (`android.R.drawable.ic_lock_idle_alarm`, `android.R.drawable.ic_menu_close_clear_cancel`) which may not be available or consistent across all devices/Android versions.
- **Suggested Fix**: Create custom notification icons in the app's drawable resources.
- **Status**: ⏳ Pending manual testing validation

### Issue 6: Missing Room Migration Strategy
- **Severity**: Medium
- **Location**: `core/database/src/main/kotlin/phonedown/core/database/di/DatabaseModule.kt:26-31`
- **Issue**: Room database is built without `.fallbackToDestructiveMigration()` or any migration strategy. When schema version increases, the app will crash on upgrade.
- **Suggested Fix**: Add `.fallbackToDestructiveMigration()` for V1 (acceptable since no production users yet) or implement proper migrations for V2+.
- **Status**: ⏳ To be addressed before V2 schema changes

### Issue 7: allowBackup in Manifest
- **Severity**: Low
- **Location**: `app/src/main/AndroidManifest.xml:10`
- **Issue**: `android:allowBackup="true"` allows Android's auto-backup to save app data. For a privacy-focused app, this might not be desired.
- **Suggested Fix**: Consider setting `android:allowBackup="false"` or explicitly defining backup rules with `android:fullBackupContent` to exclude sensitive data.
- **Status**: ⏳ Pending product decision

### Issue 8: Notification Permission Edge Case
- **Severity**: Medium
- **Location**: `app/src/main/java/phonedown/app/MainActivity.kt:96-105`
- **Issue**: If user denies notification permission, returns from `startFocusSession()` early. However, if user later grants permission through system settings and returns to the app, `pendingStartDurationSeconds` might be stale or null.
- **Suggested Fix**: Clear `pendingStartDurationSeconds` after use or when activity is destroyed/recreated.
- **Status**: ⏳ Pending manual testing validation

### Issue 9: Screen Dimming Brightness Value
- **Severity**: Low
- **Location**: `app/src/main/java/phonedown/app/MainActivity.kt:71`
- **Issue**: Screen brightness is set to `0.02f` during dimming. On some devices, this might be too dim or not dim at all depending on the screen's minimum brightness.
- **Suggested Fix**: Test on multiple devices and consider using `WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE` with a system brightness reduction or a dim overlay instead.
- **Status**: ⏳ Pending manual device testing

## Manual Testing Checklist

### Critical Paths to Test
- [ ] Start session → place phone down → complete naturally
- [ ] Start session → pick up within 5s → minor interruption recorded
- [ ] Start session → pick up for > 5s → penalty applied
- [ ] Start session → pick up for > 60s → session broken
- [ ] Receive call during session → call pause → resume after call
- [ ] Force close app during active session → relaunch → recovery
- [ ] Device restart during active session → relaunch → recovery
- [ ] End session early → correct classification
- [ ] Notification "End Session" action works
- [ ] Permission denial and re-grant flows

### Device Matrix
- [ ] Android 12 (API 31)
- [ ] Android 13 (API 33) - notification permission
- [ ] Android 14 (API 34)
- [ ] Android 15 (API 35)
- [ ] Samsung device
- [ ] Pixel device
- [ ] Low-end device

### Edge Cases
- [ ] Airplane mode
- [ ] Battery saver
- [ ] Do Not Disturb
- [ ] Low storage
- [ ] No Google account
- [ ] Rapid start/stop sessions
- [ ] Background the app during session
- [ ] Screen rotation during session

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
