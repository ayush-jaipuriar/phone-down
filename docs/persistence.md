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

The persistence layer does **not** decide recovery outcomes by itself. It persists session state and exposes `getRecoverableSessions()` for unfinished candidates such as `WaitingForPhoneDown`, `Arming`, `Active`, and paused states.

Phase 6 now operationalizes that recovery path conservatively:

- app launch recovery skips classification if a live in-memory runtime already exists
- unexpected foreground-service restarts classify dangling persisted sessions rather than silently creating a new one
- boot recovery classifies unfinished sessions rather than reviving them as if nothing happened

The current recovery posture is intentionally honesty-first, not resume-first. Unfinished sessions are classified as `Broken` or `Abandoned` according to the Phase 4 recovery rules instead of being optimistically resumed after process death or reboot.

## Backup Readiness

We store all required backup metadata (opt-in, auto-backup, last backup timestamp) in DataStore. The database is also structured such that when Google Drive backup is implemented in Phase 12, all sessions and penalty events can be cleanly exported via standard DAO queries, without needing a dedicated remote-sync ledger table right now.

## Schema Migrations

The database is at version 1.
The version 1 Room schema is exported under `core/database/schemas/`.
True migration tests should be added when version 2 is created.
