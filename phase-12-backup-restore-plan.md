# Phase 12 — Backup And Restore Plan

## Scope

Implement Google Drive backup and restore for Pro users. This phase makes focus data portable: sessions, penalties, and settings can be backed up to the user's Google Drive app data folder and restored on a new device or after reinstall. Backup is opt-in, Pro-gated, and privacy-respecting.

## Architecture

### Module Placement

| Concern | Module | Rationale |
|---|---|---|
| Backup schema & serialization | `:core:backup` | Isolated from UI, testable |
| Drive API client | `:core:backup` | Wraps Google Drive REST API |
| Backup/restore use cases | `:domain:session` | Domain logic for serialization |
| Backup UI (settings integration) | `:feature:settings` | Settings screen owns backup rows |
| Restore UI | `:feature:account` | Account screen owns restore flow |

### Dependency Direction

```
:core:backup → :core:model (for data types)
:core:backup → :core:auth (for auth token)
:core:backup → :core:datastore (for settings)
:feature:settings → :core:backup (for manual backup trigger)
:feature:account → :core:backup (for restore flow)
```

### Backup Schema

```json
{
  "schemaVersion": 1,
  "exportedAtMillis": 1717420800000,
  "sessions": [
    {
      "id": "uuid",
      "plannedDurationSeconds": 1500,
      "startTimeMillis": 1717420800000,
      "endTimeMillis": 1717422300000,
      "validFocusSeconds": 1500,
      "penaltySeconds": 0,
      "result": "CLEAN_COMPLETED",
      "state": "COMPLETED",
      "interruptions": 0,
      "penalties": 0
    }
  ],
  "penaltyEvents": [
    {
      "id": "uuid",
      "sessionId": "uuid",
      "type": "MINOR_PICKUP",
      "timestampMillis": 1717421000000,
      "penaltySeconds": 0
    }
  ],
  "settings": {
    "defaultDurationSeconds": 1500,
    "soundEnabled": true,
    "hapticsEnabled": true,
    "themeMode": "System",
    "onboardingCompleted": true,
    "backupOptIn": true,
    "autoBackupEnabled": true,
    "freeCustomDurationSeconds": null
  }
}
```

### Key Interfaces

#### `BackupRepository` (`:core:model`)
```kotlin
interface BackupRepository {
    suspend fun createBackup(): BackupResult
    suspend fun restoreBackup(): RestoreResult
    suspend fun getLastBackupTime(): Long?
    suspend fun deleteBackup(): Boolean
}

sealed class BackupResult {
    data class Success(val backupId: String, val timestampMillis: Long) : BackupResult()
    data class Failure(val reason: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(
        val sessionsRestored: Int,
        val settingsRestored: Boolean,
    ) : RestoreResult()
    data class Failure(val reason: String) : RestoreResult()
    data object NoBackupFound : RestoreResult()
}
```

## Implementation Steps

### Step 1 — Backup Schema & Serialization (2 hours)
- [ ] Create `BackupData` data class (schema version, timestamp, sessions, penalties, settings)
- [ ] Create `BackupSerializer` with kotlinx.serialization (JSON)
- [ ] Add schema version constant (v1)
- [ ] Add `BackupDataMapper` to convert between domain models and backup DTOs
- [ ] Unit tests: serialization round-trip, schema version validation

### Step 2 — Google Drive Client (3 hours)
- [ ] Add Google Drive API dependency (google-api-services-drive)
- [ ] Create `DriveBackupClient` class
  - Authenticate with auth token from `AuthRepository`
  - Upload file to Drive app data folder
  - Download file from Drive app data folder
  - List/delete files in app data folder
- [ ] Handle network errors gracefully (timeout, no connection, auth failure)
- [ ] Unit tests: mock Drive API responses

### Step 3 — Backup Repository Implementation (2 hours)
- [ ] Create `DriveBackupRepository` implementing `BackupRepository`
- [ ] `createBackup()`:
  - Collect all sessions from `SessionRepository`
  - Collect all penalty events
  - Collect current settings from `SettingsRepository`
  - Serialize to JSON
  - Upload to Drive app data folder
  - Update `lastBackupEpochMillis` in settings
  - Return success/failure
- [ ] `restoreBackup()`:
  - Download latest backup from Drive
  - Validate schema version
  - Deserialize sessions, penalties, settings
  - **Full replace**: clear local database, insert restored data
  - Restore settings to DataStore
  - Return success with count
- [ ] `getLastBackupTime()`: return cached timestamp from settings
- [ ] `deleteBackup()`: remove file from Drive, clear local timestamp
- [ ] Add `BackupRepository` provider to `AppRuntimeModule`

### Step 4 — Settings UI Integration (2 hours)
- [ ] Update `SettingsScreen` Backup & Restore row:
  - If not Pro: show "Pro" label, navigate to paywall on tap
  - If Pro but not signed in: show "Sign in to backup", navigate to account
  - If Pro and signed in: show last backup time, trigger manual backup on tap
- [ ] Add backup progress indicator (simple loading state)
- [ ] Add backup success/failure snackbar/toast
- [ ] Update `SettingsViewModel` to expose `triggerBackup()` method
- [ ] Add `SettingsScreenTest` for backup row states

### Step 5 — Account UI Integration (1 hour)
- [ ] Update `AccountScreen` with restore section:
  - Show "Restore from Backup" button (Pro + signed in only)
  - Show last backup time if available
  - Show restore confirmation dialog ("This will replace all local data with your backup from [date]")
- [ ] Add restore progress indicator
- [ ] Add restore success/failure feedback
- [ ] Update `AccountViewModel` to expose `restoreBackup()` method

### Step 6 — Auto-Backup (2 hours)
- [ ] Add `AutoBackupManager` in `:core:backup`
  - Check if auto-backup is enabled (Pro + opt-in)
  - Check if enough time has passed since last backup (24 hours)
  - Check network availability
  - Trigger backup if all conditions met
- [ ] Hook auto-backup check into app launch flow
  - Check in `MainActivity.onCreate` or `PhoneDownApp`
  - Run in background coroutine, don't block UI
- [ ] Add auto-backup toggle in Settings (only visible for Pro + signed in)
- [ ] Update `SettingsRepository` with `setAutoBackupEnabled`

### Step 7 — Datastore Backup Extension (1 hour)
- [ ] Extend `DataStoreSettingsRepository` to support bulk read for backup
- [ ] Add `restoreSettings(settings: UserSettings)` method
- [ ] Ensure settings restore doesn't trigger infinite loops with observers

### Step 8 — Verification (2 hours)
- [ ] Unit tests:
  - `BackupSerializerTest`: round-trip serialization, schema validation
  - `DriveBackupRepositoryTest`: create/restore/delete backup (mocked Drive)
  - `BackupDataMapperTest`: domain ↔ DTO mapping
- [ ] Compose UI tests:
  - `SettingsScreenTest`: backup row state changes (free/Pro/signed in)
  - `AccountScreenTest`: restore button visibility
- [ ] Paparazzi tests:
  - Settings with backup status
  - Account with restore section
- [ ] Build check: `:app:assembleDebug`, all unit tests, all screenshot tests

## Tradeoffs

### Full Replace vs Merge
- **Decision**: Full replace operation.
- **Why**: Simpler to implement, less error-prone, matches user mental model ("restore my backup"). Merge logic with conflict resolution adds complexity and potential data loss ambiguity.
- **Risk**: User loses any sessions created after the backup. Mitigation: show backup date prominently in confirmation dialog.

### WorkManager vs App Launch Check
- **Decision**: App launch check for auto-backup (user preference).
- **Why**: Simpler to implement, no additional dependencies, sufficient for daily cadence. WorkManager is more robust but adds complexity.
- **Risk**: If user never opens app, auto-backup won't run. Mitigation: backup is explicitly opt-in; user understands the app needs to be opened.

### No Additional Encryption
- **Decision**: Rely on Drive's app data folder isolation.
- **Why**: App data folder is only accessible to the app that created it. Adding client-side encryption requires key management and increases complexity.
- **Risk**: If user's Google account is compromised, backup data is accessible. Mitigation: this is standard for most apps; user controls account security.

### Preserve Newer Local on Conflict
- **Decision**: Not applicable (full replace).
- **Why**: Full replace is an explicit user action with a confirmation dialog. The user chooses to overwrite local data.

## Acceptance Criteria

- [ ] Pro user can trigger manual backup from Settings
- [ ] Backup includes all sessions, penalty events, and settings
- [ ] Backup is stored in Google Drive app data folder
- [ ] Last backup time is displayed in Settings
- [ ] Pro user can restore from backup in Account screen
- [ ] Restore replaces all local data (full replace)
- [ ] Restore validates schema version and handles version mismatch gracefully
- [ ] Auto-backup runs on app launch if enabled (24h cadence)
- [ ] Free user sees Pro gate for backup features
- [ ] Unsigned-in user sees sign-in prompt for backup features
- [ ] Backup works offline (queues until network available)
- [ ] All unit tests pass
- [ ] All Compose UI tests pass
- [ ] All Paparazzi screenshot tests pass
- [ ] `:app:assembleDebug` succeeds

## Residual Risks

- Google Drive API rate limits (unlikely for personal use)
- Large backup files for long-term Pro users (may need pagination in future)
- Schema version migration when adding new fields (v2+ will need migration logic)
- Google Sign-In token expiry during backup/restore (need token refresh handling)

## Checklist

- [ ] Step 1: Backup schema & serialization
- [ ] Step 2: Google Drive client
- [ ] Step 3: Backup repository implementation
- [ ] Step 4: Settings UI integration
- [ ] Step 5: Account UI integration
- [ ] Step 6: Auto-backup
- [ ] Step 7: Datastore backup extension
- [ ] Step 8: Verification
- [ ] Update `v1-implementation-plan.md`
- [ ] Update `docs/agent-handoff.md`
