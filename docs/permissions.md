# Android Permissions Documentation

This document lists all Android permissions requested by Phone Down, their purposes, and how they map to Play Store data safety categories.

## Permissions Overview

| Permission | Required? | Purpose | Play Store Category |
|---|---|---|---|
| `POST_NOTIFICATIONS` (Android 13+) | Yes | Show foreground service notification during active focus sessions | App functionality |
| `FOREGROUND_SERVICE` | Yes | Keep focus session running when app is backgrounded | App functionality |
| `RECEIVE_BOOT_COMPLETED` | Yes | Recover interrupted sessions after device reboot | App functionality |
| `VIBRATE` | Yes | Haptic feedback during session start/end and interruptions | App functionality |
| `INTERNET` | Yes | Play Billing verification, optional cloud backup | App functionality |
| `ACCESS_NETWORK_STATE` | No | Check network availability before backup operations | App functionality |
| `WAKE_LOCK` | No | Keep CPU awake during active focus sessions | App functionality |

## Detailed Permission Descriptions

### `POST_NOTIFICATIONS` (Android 13+)
**Purpose**: Display a persistent notification when a focus session is active.
**Why**: Required by Android 13+ for all notifications. The foreground service notification shows session status and provides quick access to end the session.
**User Control**: Can be disabled in Android system settings, but doing so will hide the active session notification.
**Data Access**: No personal data accessed.

### `FOREGROUND_SERVICE`
**Purpose**: Run the focus session service in the background.
**Why**: Required by Android to keep the session timer running when the app is not visible. The service monitors phone orientation and tracks focus time.
**Data Access**: No personal data accessed.

### `RECEIVE_BOOT_COMPLETED`
**Purpose**: Check for interrupted sessions after device restart.
**Why**: If the device restarts during an active session, this permission allows the app to classify the session appropriately on next launch.
**Data Access**: No personal data accessed.

### `VIBRATE`
**Purpose**: Provide haptic feedback during sessions.
**Why**: Optional haptic feedback when starting a session, detecting interruptions, or completing a session. Can be disabled in app settings.
**Data Access**: No personal data accessed.

### `INTERNET`
**Purpose**: Network connectivity for optional features.
**Why**: Used for:
- Google Play Billing (purchase verification)
- Google Sign-In (authentication)
- Google Drive backup (if enabled)
**Data Access**: No browsing history or network traffic monitored.

### `ACCESS_NETWORK_STATE`
**Purpose**: Check network availability.
**Why**: Determine if backup operations can proceed. Prevents failed backup attempts when offline.
**Data Access**: No personal data accessed.

### `WAKE_LOCK`
**Purpose**: Prevent device sleep during active sessions.
**Why**: Ensures the session timer continues accurately even if the device's screen times out.
**Data Access**: No personal data accessed.

## Permissions NOT Requested

Phone Down does NOT request the following permissions:

| Permission | Why Not Needed |
|---|---|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Location is not relevant to focus sessions |
| `READ_CONTACTS` | No social features or contact access needed |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | App uses internal storage only; exports use Sharesheet |
| `CAMERA` | No camera features |
| `RECORD_AUDIO` | No audio recording features |
| `READ_PHONE_STATE` | Call detection uses system broadcasts, not phone state |
| `SEND_SMS` / `READ_SMS` | No messaging features |

## Play Store Data Safety Form Mapping

### Data Types Collected

| Data Type | Collected? | Shared? | Purpose |
|---|---|---|---|
| **App activity** (session data) | Yes | No | App functionality (insights, history) |
| **App info and performance** (crash logs) | Optional | No | App functionality (if user opts in) |
| **Account information** (Google) | Optional | No | App functionality (backup, identity) |
| **Purchase history** | Yes (Google handles) | No | App functionality (billing) |
| **Location** | No | No | N/A |
| **Personal info** | No | No | N/A |
| **Photos and videos** | No | No | N/A |
| **Files and docs** | No | No | N/A |
| **Contacts** | No | No | N/A |
| **Calendar** | No | No | N/A |
| **SMS** | No | No | N/A |
| **Call logs** | No | No | N/A |

### Data Usage

| Usage | Applies To |
|---|---|
| App functionality | Session data, settings, account info |
| Analytics | Session statistics (aggregated locally) |
| Developer communications | Purchase receipt (Google Play) |
| Fraud prevention | Purchase verification (Google Play) |

### Data Sharing

Phone Down does not share user data with third parties. All data remains:
- On the user's device (local storage)
- In the user's personal Google Drive (if backup enabled)
- Handled by Google Play Billing (for purchases)

### Data Encryption

| State | Encryption |
|---|---|
| In transit | TLS 1.2+ for all network communications |
| At rest | Auth tokens encrypted; session data unencrypted (V1) |

### Data Deletion

Users can delete their data through:
- **Local data**: Settings > Privacy > Delete All Local Data
- **Cloud backup**: Included in delete all data flow (if enabled)
- **Account**: Sign out + delete data

## Permission Rationale UI

When Android requests dangerous permissions, the app shows:
- **POST_NOTIFICATIONS**: "Phone Down needs notification permission to show your active focus session status."

All other permissions are normal permissions that don't require runtime user approval.
