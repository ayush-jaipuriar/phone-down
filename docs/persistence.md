# Local Persistence

This document explains the local persistence foundation implemented in Phase 3 of Phone Down V1.

## Architecture

The persistence layer is strictly local and consists of two parts:
1. **Room Database (`:core:database`)**: Stores session history and penalty events.
2. **DataStore Preferences (`:core:datastore`)**: Stores user settings, onboarding state, and backup preferences.

Both implementations are hidden behind pure Kotlin interfaces in `:core:model` (`SessionRepository` and `SettingsRepository`). This allows domain and feature modules to use persistence without knowing about Room or DataStore.

## Database Entities

### `focus_sessions`

Stores every session that was ever started.
- All enum fields (`SessionState`, `SessionResult`) are stored as stable strings.
- Timing fields include both wall-clock time (`startedAtEpochMillis`) for display and monotonic time (`startElapsedRealtime`) to allow reliable resumption across process death.
- Sessions are inserted and updated continuously as they progress from `WaitingForPhoneDown` to `Active` and eventually `Completed` or `Broken`.

### `penalty_events`

Stores interruption events (pickups, calls, etc).
- Linked to a session via a foreign key (`sessionId`) with `CASCADE DELETE`.
- Enums are stored as stable strings.

## DataStore Preferences

A single DataStore file (named `phone_down_theme_mode` for backward compatibility with pre-Phase 3 builds) stores:
- Timer settings (duration, limits)
- Hardware preferences (sound, haptics)
- UI preferences (theme mode)
- App state (onboarding completed)
- Backup metadata (opt-in, auto-backup, last backup timestamp)

## Process-Death Recovery Strategy

The persistence layer does **not** classify whether a paused or interrupted session is broken or abandoned. It simply persists the state of the session at any given time. 
The DAO exposes a `getRecoverableSessions()` query which returns candidates based on their state (e.g. `Active`, `PausedByPickup`). The domain layer (Session Engine / Foreground Service, to be built in later phases) is responsible for taking these candidates, analyzing the current `elapsedRealtime`, and classifying them as resumed, abandoned, or broken.

## Backup Readiness

We store all required backup metadata (opt-in, auto-backup, last backup timestamp) in DataStore. The database is also structured such that when Google Drive backup is implemented in Phase 12, all sessions and penalty events can be cleanly exported via standard DAO queries, without needing a dedicated remote-sync ledger table right now.

## Schema Migrations

The database is at version 1.
The version 1 Room schema is exported under `core/database/schemas/`.
True migration tests should be added when version 2 is created.
