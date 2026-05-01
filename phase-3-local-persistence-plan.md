# Phase 3 Plan - Local Persistence

This document is the detailed implementation plan for Phase 3 of Phone Down V1. It must be reviewed and approved before implementation begins.

Phase 3 creates the local persistence foundation for sessions, penalty events, user settings, onboarding completion, and backup metadata. The goal is to make the data layer real and testable before wiring the live timer engine, sensor engine, foreground service, insights UI, onboarding flow, or backup transport.

## 0. Clarified Decisions

- [x] Implement the full persistence foundation in this phase.
- [x] Include Room sessions and penalty events.
- [x] Include DataStore settings, onboarding completion, and backup preference state.
- [x] Include backup metadata placeholders needed by later Google Drive backup work.
- [x] Add real repository interfaces and implementations.
- [x] Use recommendation for process-death recovery: persist enough data now, but defer final recovery classification execution to the session-engine/foreground-service phases.
- [x] Use recommendation for settings: persist all planned V1 settings that are stable now, not only theme mode.
- [x] Use recommendation for migration strategy: export schema from version 1, validate DB open/create now, and add true migration tests when version 2 exists.
- [x] Use recommendation for seed/demo data: no production seed data; use test fixtures/fakes only.
- [x] Keep implementation gated until this plan is approved.

## 1. Purpose And Boundaries

Phase 3 should answer one question: can the app store and retrieve the core V1 data model reliably, locally, and without UI or network dependencies?

It should not try to make sessions run yet. That distinction matters because storage is about durable facts, while the session engine is about state transitions over time and sensors.

### In Scope

- [ ] Add Room dependencies and configuration.
- [ ] Add database schema export path.
- [ ] Add `PhoneDownDatabase`.
- [ ] Add `FocusSessionEntity`.
- [ ] Add `PenaltyEventEntity`.
- [ ] Add backup metadata entity or DataStore-backed metadata, depending on best fit during implementation.
- [ ] Add DAOs for sessions and penalty events.
- [ ] Add transaction helpers for session-plus-penalty writes.
- [ ] Add domain models/enums in `:core:model`.
- [ ] Add mapper functions between entities and domain models.
- [ ] Add repository interfaces.
- [ ] Add repository implementations.
- [ ] Add DataStore-backed settings repository.
- [ ] Consolidate existing theme preference into the broader settings persistence surface.
- [ ] Add tests for DAOs, mappers, repositories, DataStore flows, and DB creation.
- [ ] Update docs and progress logs.

### Out Of Scope

- [ ] Live timer countdown.
- [ ] Session state-machine rules.
- [ ] Face-down sensor evaluation.
- [ ] Foreground service active-session recovery.
- [ ] UI wiring to real repositories.
- [ ] Real analytics aggregation UI.
- [ ] Google Drive backup transport.
- [ ] Play Billing entitlement source of truth.
- [ ] Production seed/demo data.
- [ ] True Room migration from version 1 to version 2, because version 2 does not exist yet.

## 2. Recommended Architecture

Use persistence modules as implementation owners, and expose small repository contracts that later domain/features can consume.

### Module Responsibility

- [ ] `:core:model`
  - [ ] Pure Kotlin models and enums.
  - [ ] No Android, Room, DataStore, or Compose dependencies.
- [ ] `:core:database`
  - [ ] Room database, entities, DAOs, database callbacks if needed, and entity mappers.
  - [ ] Android implementation detail stays here.
- [ ] `:core:datastore`
  - [ ] DataStore preference keys, preference-backed settings implementation, and backup preference metadata when key-value storage is sufficient.
  - [ ] Keep existing `ThemeModePreference` behavior, but avoid duplicated theme APIs after broader settings repository lands.
- [ ] `:core:common`
  - [ ] Optional time-provider or dispatcher abstractions only if needed for repository tests.
- [ ] `:domain:*`
  - [ ] No direct Room or DataStore dependencies in this phase.
- [ ] `:feature:*`
  - [ ] No direct Room or DataStore dependencies in this phase.
- [ ] `:app`
  - [ ] Owns Hilt bindings if repository injection is introduced now.
  - [ ] Keeps current theme wiring working.

### Repository Placement Recommendation

Use repository interfaces near the implementation boundary for now:

- [ ] `SessionRepository` in `:core:database` or `:core:model` only if it has no Android/Room types.
- [ ] `SettingsRepository` in `:core:datastore` or a small `:core:model` contract package only if needed by multiple modules.
- [ ] Avoid creating a new `:core:data` module in Phase 3 unless implementation friction proves it is clearly better.

Rationale: adding `:core:data` too early can become a generic bucket. Phase 3 can keep contracts small and move them later if cross-module pressure appears.

## 3. Data Model Plan

The Phase 3 models should mirror `architecture.md` closely while staying practical for Room and future analytics.

### Session State Enums

Add to `:core:model`:

- [ ] `SessionState`
  - [ ] `Created`
  - [ ] `WaitingForPhoneDown`
  - [ ] `Arming`
  - [ ] `Active`
  - [ ] `PausedByPickup`
  - [ ] `PausedByCall`
  - [ ] `Completed`
  - [ ] `EndedEarly`
  - [ ] `Invalidated`
  - [ ] `Broken`
  - [ ] `Abandoned`
- [ ] `SessionResult`
  - [ ] `CleanCompleted`
  - [ ] `CompletedWithInterruption`
  - [ ] `Partial`
  - [ ] `StrongPartial`
  - [ ] `Invalidated`
  - [ ] `Broken`
  - [ ] `Abandoned`
- [ ] `PenaltyEventType`
  - [ ] `MinorPickup`
  - [ ] `PenaltyPickup`
  - [ ] `LongPickup`
  - [ ] `CallPause`
  - [ ] `ForceClose`
  - [ ] `DeviceRestart`
  - [ ] `ManualEnd`

Recommended Kotlin naming uses idiomatic enum entries instead of screaming snake case. Room stores enum values as stable strings through explicit mapper functions.

### Focus Session Domain Model

Add `FocusSession` to `:core:model`:

- [ ] `id: String`
- [ ] `plannedDurationSeconds: Long`
- [ ] `requiredDurationSeconds: Long`
- [ ] `validFocusSeconds: Long`
- [ ] `actualElapsedSeconds: Long`
- [ ] `penaltySeconds: Long`
- [ ] `interruptionCount: Int`
- [ ] `minorInterruptionCount: Int`
- [ ] `penaltyInterruptionCount: Int`
- [ ] `startedAtEpochMillis: Long`
- [ ] `endedAtEpochMillis: Long?`
- [ ] `startElapsedRealtime: Long`
- [ ] `endElapsedRealtime: Long?`
- [ ] `state: SessionState`
- [ ] `result: SessionResult?`
- [ ] `clean: Boolean`
- [ ] `broken: Boolean`
- [ ] `callInterrupted: Boolean`
- [ ] `createdAtEpochMillis: Long`
- [ ] `updatedAtEpochMillis: Long`

### Penalty Event Domain Model

Add `PenaltyEvent` to `:core:model`:

- [ ] `id: String`
- [ ] `sessionId: String`
- [ ] `type: PenaltyEventType`
- [ ] `startedAtEpochMillis: Long`
- [ ] `endedAtEpochMillis: Long?`
- [ ] `durationSeconds: Long`
- [ ] `penaltySeconds: Long`

### User Settings Domain Model

Add or expand `UserSettings` in `:core:model`:

- [ ] `defaultDurationSeconds: Long`
- [ ] `soundEnabled: Boolean`
- [ ] `hapticsEnabled: Boolean`
- [ ] `themeMode: ThemeMode`
- [ ] `onboardingCompleted: Boolean`
- [ ] `backupOptIn: Boolean`
- [ ] `autoBackupEnabled: Boolean`
- [ ] `lastBackupEpochMillis: Long?`
- [ ] `freeCustomDurationSeconds: Long?`

Recommended defaults:

- [ ] Default duration: `25 * 60` seconds.
- [ ] Sounds enabled: `true`.
- [ ] Haptics enabled: `true`.
- [ ] Theme mode: `System`.
- [ ] Onboarding completed: `false`.
- [ ] Backup opt-in: `false`.
- [ ] Auto-backup enabled: `false`.
- [ ] Last backup timestamp: `null`.
- [ ] Free custom duration: `null`.

## 4. Room Schema Plan

### Database Configuration

- [ ] Add Room runtime/compiler/testing dependencies to version catalog.
- [ ] Configure KSP if Room compiler uses KSP.
- [ ] Configure Room schema export directory, recommended:

```text
core/database/schemas/
```

- [ ] Commit schema JSON for version 1.
- [ ] Ensure generated build artifacts remain ignored.
- [ ] Add `PhoneDownDatabase` with `version = 1`.
- [ ] Keep destructive migrations disabled for normal app usage.
- [ ] Use in-memory Room database for DAO tests.

### `FocusSessionEntity`

Recommended table name:

```text
focus_sessions
```

Fields:

- [ ] `id` primary key.
- [ ] `plannedDurationSeconds`.
- [ ] `requiredDurationSeconds`.
- [ ] `validFocusSeconds`.
- [ ] `actualElapsedSeconds`.
- [ ] `penaltySeconds`.
- [ ] `interruptionCount`.
- [ ] `minorInterruptionCount`.
- [ ] `penaltyInterruptionCount`.
- [ ] `startedAtEpochMillis`.
- [ ] `endedAtEpochMillis`.
- [ ] `startElapsedRealtime`.
- [ ] `endElapsedRealtime`.
- [ ] `state`.
- [ ] `result`.
- [ ] `clean`.
- [ ] `broken`.
- [ ] `callInterrupted`.
- [ ] `createdAtEpochMillis`.
- [ ] `updatedAtEpochMillis`.

Indexes:

- [ ] `startedAtEpochMillis`
- [ ] `endedAtEpochMillis`
- [ ] `state`
- [ ] `result`
- [ ] `clean`
- [ ] `broken`
- [ ] `updatedAtEpochMillis`

### `PenaltyEventEntity`

Recommended table name:

```text
penalty_events
```

Fields:

- [ ] `id` primary key.
- [ ] `sessionId`, foreign key to `focus_sessions.id`.
- [ ] `type`.
- [ ] `startedAtEpochMillis`.
- [ ] `endedAtEpochMillis`.
- [ ] `durationSeconds`.
- [ ] `penaltySeconds`.

Indexes:

- [ ] `sessionId`
- [ ] `type`
- [ ] `startedAtEpochMillis`

Foreign key behavior:

- [ ] Delete penalty events when their session is deleted.
- [ ] Do not allow orphan penalty events in normal DAO writes.

### Backup Metadata

Recommended Phase 3 storage:

- [ ] Store backup preference state in DataStore:
  - [ ] Backup opt-in.
  - [ ] Auto-backup enabled.
  - [ ] Last backup timestamp.
- [ ] Add a small Room metadata table only if implementation needs record-level backup sync metadata.

Recommended deferral:

- [ ] Do not add a per-session remote-sync table unless backup implementation proves it needs one.

## 5. DAO Plan

### `FocusSessionDao`

- [ ] `upsertSession(entity: FocusSessionEntity)`.
- [ ] `getSession(id: String): Flow<FocusSessionEntity?>`.
- [ ] `getSessionOnce(id: String): FocusSessionEntity?`.
- [ ] `observeLatestSessions(limit: Int): Flow<List<FocusSessionEntity>>`.
- [ ] `observeSessionsInWindow(startEpochMillis: Long, endEpochMillis: Long): Flow<List<FocusSessionEntity>>`.
- [ ] `getSessionsUpdatedSince(updatedAtEpochMillis: Long): List<FocusSessionEntity>`.
- [ ] `getRecoverableSessions(): List<FocusSessionEntity>`.
- [ ] `deleteSession(id: String)`.
- [ ] `deleteAllSessions()` for tests only if kept internal/test-visible.

Recoverable states should include:

- [ ] `Created`
- [ ] `WaitingForPhoneDown`
- [ ] `Arming`
- [ ] `Active`
- [ ] `PausedByPickup`
- [ ] `PausedByCall`

The DAO should only retrieve recoverable candidates. It should not decide whether they become abandoned or broken; that belongs to the session engine/recovery phase.

### `PenaltyEventDao`

- [ ] `insertPenaltyEvent(entity: PenaltyEventEntity)`.
- [ ] `upsertPenaltyEvent(entity: PenaltyEventEntity)`.
- [ ] `observePenaltyEventsForSession(sessionId: String): Flow<List<PenaltyEventEntity>>`.
- [ ] `getPenaltyEventsForSession(sessionId: String): List<PenaltyEventEntity>`.
- [ ] `getPenaltyEventsUpdatedWindow(startEpochMillis: Long, endEpochMillis: Long): List<PenaltyEventEntity>` if useful for backup.
- [ ] `deletePenaltyEventsForSession(sessionId: String)`.

### Transaction DAO/Store

Room transactions should support:

- [ ] Upsert session only.
- [ ] Upsert session plus one penalty event.
- [ ] Upsert session plus multiple penalty events.
- [ ] Delete session plus related penalty events through foreign key.

## 6. Repository Plan

Repository interfaces should make later phases easier to test. They should return domain models, not entities.

### `SessionRepository`

Recommended API:

- [ ] `upsertSession(session: FocusSession)`.
- [ ] `observeSession(id: String): Flow<FocusSession?>`.
- [ ] `getSession(id: String): FocusSession?`.
- [ ] `observeLatestSessions(limit: Int): Flow<List<FocusSession>>`.
- [ ] `observeSessionsInWindow(startEpochMillis: Long, endEpochMillis: Long): Flow<List<FocusSession>>`.
- [ ] `getRecoverableSessions(): List<FocusSession>`.
- [ ] `recordPenaltyEvent(event: PenaltyEvent)`.
- [ ] `upsertSessionWithPenaltyEvent(session: FocusSession, event: PenaltyEvent)`.
- [ ] `observePenaltyEvents(sessionId: String): Flow<List<PenaltyEvent>>`.
- [ ] `getPenaltyEvents(sessionId: String): List<PenaltyEvent>`.

Implementation:

- [ ] `RoomSessionRepository`.
- [ ] Maps all entities to domain models.
- [ ] Uses transaction helpers for atomic writes.
- [ ] Does not classify session results.
- [ ] Does not access sensors, notifications, billing, or UI.

### `SettingsRepository`

Recommended API:

- [ ] `settings: Flow<UserSettings>`.
- [ ] `setDefaultDurationSeconds(seconds: Long)`.
- [ ] `setSoundEnabled(enabled: Boolean)`.
- [ ] `setHapticsEnabled(enabled: Boolean)`.
- [ ] `setThemeMode(themeMode: ThemeMode)`.
- [ ] `setOnboardingCompleted(completed: Boolean)`.
- [ ] `setBackupOptIn(enabled: Boolean)`.
- [ ] `setAutoBackupEnabled(enabled: Boolean)`.
- [ ] `setLastBackupEpochMillis(epochMillis: Long?)`.
- [ ] `setFreeCustomDurationSeconds(seconds: Long?)`.

Implementation:

- [ ] `DataStoreSettingsRepository`.
- [ ] Keep preference keys private.
- [ ] Preserve current theme mode behavior.
- [ ] Replace direct `ThemeModePreference` usage where practical.
- [ ] Validate obvious values such as duration bounds at repository boundary if simple.

### Backup Readiness

Phase 3 should expose enough data for Phase 12 backup:

- [ ] Read all sessions for export.
- [ ] Read all penalty events for export.
- [ ] Read settings for export.
- [ ] Read and update backup metadata.
- [ ] Upsert imported sessions by ID.
- [ ] Upsert imported penalty events by ID.

Actual JSON serialization and Drive upload remain out of scope.

## 7. Hilt And App Wiring Plan

### Database Wiring

- [ ] Add a Hilt module for `PhoneDownDatabase`.
- [ ] Provide `FocusSessionDao`.
- [ ] Provide `PenaltyEventDao`.
- [ ] Provide `SessionRepository`.
- [ ] Scope database singleton.

### DataStore Wiring

- [ ] Add/provide a single app settings DataStore.
- [ ] Provide `SettingsRepository`.
- [ ] Keep `MainActivity` theme collection working.
- [ ] Prefer collecting theme through `SettingsRepository.settings.map { it.themeMode }` if this does not complicate Phase 3.

### Risk Control

- [ ] Do not inject repositories into feature screens yet unless needed to preserve current theme behavior.
- [ ] Keep UI static after Phase 3 unless the change is strictly necessary for app startup or theme persistence.

## 8. Testing Plan

Phase 3 should be heavy on automated tests because persistence mistakes are expensive later.

### Unit Tests

- [ ] Enum storage mapper tests:
  - [ ] `SessionState` to/from stored string.
  - [ ] `SessionResult` to/from stored string.
  - [ ] `PenaltyEventType` to/from stored string.
  - [ ] Unknown stored values fail safely or map according to documented behavior.
- [ ] Entity/domain mapper tests:
  - [ ] Full session round trip.
  - [ ] Session with `null` result/end fields.
  - [ ] Full penalty event round trip.
  - [ ] Penalty event with `null` end.
- [ ] Settings default tests.
- [ ] Settings update tests.

### Room Tests

- [ ] Create in-memory database.
- [ ] Insert and retrieve session.
- [ ] Update session.
- [ ] Insert penalty event linked to session.
- [ ] Observe penalty events for session.
- [ ] Delete session cascades penalty events.
- [ ] Query latest sessions in expected order.
- [ ] Query sessions by time window.
- [ ] Query recoverable sessions.
- [ ] Verify abandoned/completed sessions are not recoverable.

### Repository Tests

- [ ] `RoomSessionRepository` maps DAO output to domain models.
- [ ] `upsertSessionWithPenaltyEvent` writes atomically.
- [ ] `getRecoverableSessions` returns domain models.
- [ ] `DataStoreSettingsRepository` emits defaults.
- [ ] Updating each setting emits new `UserSettings`.
- [ ] Theme mode still persists correctly.

### Migration/Schema Tests

- [ ] Export Room schema version 1.
- [ ] Add a database creation/open smoke test.
- [ ] Add migration-test dependency and test scaffold if useful.
- [ ] Defer true `1 -> 2` migration test until schema version 2 exists.

### Verification Commands

After implementation, run:

- [ ] `./gradlew ktlintCheck`
- [ ] `./gradlew detekt`
- [ ] `./gradlew lintDebug`
- [ ] `./gradlew testDebugUnitTest`
- [ ] `./gradlew :app:assembleDebug`
- [ ] `./gradlew :feature:focus:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug`
- [ ] `./gradlew :feature:focus:assembleDebugAndroidTest :feature:insights:assembleDebugAndroidTest :feature:settings:assembleDebugAndroidTest`
- [ ] `./scripts/check.sh`

Connected UI tests remain optional unless an emulator/device is available:

- [ ] `adb devices`
- [ ] `./gradlew :feature:focus:connectedDebugAndroidTest :feature:insights:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest`

## 9. Implementation Steps

### Step 1 - Dependency And Build Setup

- [ ] Add Room dependencies to `gradle/libs.versions.toml`.
- [ ] Add Room testing dependency.
- [ ] Add any needed coroutine test/Turbine dependency if missing.
- [ ] Configure KSP for `:core:database`.
- [ ] Configure Room schema export arguments.
- [ ] Confirm `scripts/check.sh` still covers the right tasks.

### Step 2 - Core Models

- [ ] Add `FocusSession`.
- [ ] Add `PenaltyEvent`.
- [ ] Add `SessionState`.
- [ ] Add `SessionResult`.
- [ ] Add `PenaltyEventType`.
- [ ] Add `UserSettings`.
- [ ] Add default settings constants/helper if useful.
- [ ] Add pure model tests.

### Step 3 - Room Entities And Mappers

- [ ] Add `FocusSessionEntity`.
- [ ] Add `PenaltyEventEntity`.
- [ ] Add explicit enum string mappers.
- [ ] Add entity/domain mappers.
- [ ] Add mapper tests.

### Step 4 - DAOs And Database

- [ ] Add `FocusSessionDao`.
- [ ] Add `PenaltyEventDao`.
- [ ] Add transaction support.
- [ ] Add `PhoneDownDatabase`.
- [ ] Add database version 1 schema export.
- [ ] Add DAO tests.

### Step 5 - Session Repository

- [ ] Add `SessionRepository` interface.
- [ ] Add `RoomSessionRepository`.
- [ ] Add repository tests.
- [ ] Add Hilt binding if app-level injection is introduced now.

### Step 6 - Settings Repository

- [ ] Add/expand DataStore preferences for all V1 settings.
- [ ] Add `SettingsRepository`.
- [ ] Add `DataStoreSettingsRepository`.
- [ ] Migrate current theme preference usage to the settings repository if low risk.
- [ ] Keep backward-compatible theme key so existing local installs do not lose theme preference.
- [ ] Add DataStore repository tests.

### Step 7 - Backup Metadata Readiness

- [ ] Add backup opt-in and auto-backup preference methods.
- [ ] Add last backup timestamp support.
- [ ] Add repository methods needed by later export/restore.
- [ ] Document backup transport as deferred.

### Step 8 - Documentation

- [ ] Update `v1-implementation-plan.md`.
- [ ] Update `docs/module-dependency-rules.md` if repository placement changes.
- [ ] Add or update `docs/persistence.md`.
- [ ] Update `README.md` if verification/setup commands change.
- [ ] Mark this plan with implementation completion notes after implementation.

### Step 9 - Full Verification

- [ ] Run all checks listed in the testing section.
- [ ] Run `git diff --check`.
- [ ] Run sensitive-file scan.
- [ ] Report any connected UI test limitation if no device/emulator is available.

## 10. Acceptance Criteria

Phase 3 is complete only when:

- [ ] Room database exists and builds.
- [ ] Version 1 schema is exported.
- [ ] Session and penalty entities match the V1 architecture model.
- [ ] Session and penalty DAOs support required insert/update/query flows.
- [ ] Penalty events are associated with sessions.
- [ ] Session deletion cascades penalty events.
- [ ] Recoverable session query exists but does not perform final recovery classification.
- [ ] Repository interfaces and real implementations exist.
- [ ] Settings DataStore stores all planned V1 settings.
- [ ] Existing theme persistence still works.
- [ ] Onboarding completion can be persisted.
- [ ] Backup opt-in, auto-backup, and last backup metadata can be persisted.
- [ ] Unit, DAO, repository, and DataStore tests pass.
- [ ] Full local verification passes.
- [ ] Documentation is updated.
- [ ] User is informed of verification results.

## 11. Recommended Answers Captured As Implementation Policy

### Process Death And Recovery

Recommended behavior for Phase 3:

- [ ] Persist recoverable session state and monotonic timing fields.
- [ ] Add DAO/repository access to recoverable sessions.
- [ ] Do not finalize abandoned vs broken classification in Phase 3.

Recommended later behavior:

- [ ] Phase 4 session engine decides classification using the persisted session state and architecture rules.
- [ ] Phase 6 foreground service/app launch recovery applies the classification on startup.

Why: persistence should store durable facts. Classification depends on elapsed realtime, lifecycle signals, service state, and session rules that belong to later phases.

### Settings Scope

Recommended behavior for Phase 3:

- [ ] Persist all stable V1 settings now.
- [ ] Keep UI wiring mostly static except existing theme behavior.

Why: DataStore is low-risk here, and persisting all settings early prevents repeated preference-file churn across onboarding, settings, backup, and focus phases.

### Migration Scope

Recommended behavior for Phase 3:

- [ ] Treat version 1 schema export as mandatory.
- [ ] Add DB create/open tests.
- [ ] Add true migration tests when version 2 exists.

Why: there is no historical schema to migrate from yet. The important V1 discipline is exporting schema 1 and making future migrations testable.

### Seed Data Scope

Recommended behavior for Phase 3:

- [ ] No production seed/demo data.
- [ ] Use test fixtures and fake repositories for tests only.

Why: production seed data can pollute analytics and backup behavior. Static UI demo data already exists in Phase 2 and should remain UI-local until real data wiring starts.

## 12. Risks And Mitigations

### Risk: Repository Contracts Become Too Broad

- Mitigation: expose methods required by V1 persistence and near-future backup/analytics only. Avoid generic query builders.

### Risk: Session Engine Logic Leaks Into Persistence

- Mitigation: repositories store and retrieve sessions. They do not classify, apply penalties, or advance timers.

### Risk: Enum String Values Become Migration Hazards

- Mitigation: use explicit mapper functions and tests. Do not rely on `enum.name` as an implicit long-term storage contract.

### Risk: DataStore Settings Expand Into Business Rules

- Mitigation: DataStore stores preferences. Free-vs-Pro enforcement for duration limits and backup gates remains in domain/billing phases.

### Risk: Backup Metadata Is Overbuilt

- Mitigation: store opt-in, auto-backup, and last backup timestamp now. Defer remote revision IDs or sync ledgers until backup implementation proves they are needed.

### Risk: Room Tests Become Slow Or Flaky

- Mitigation: use in-memory databases and targeted DAO tests. Keep emulator/device tests separate from JVM/local tests unless AndroidX runner is required.

## 13. Approval Gate

Implementation must not begin until this Phase 3 plan is approved.

Approval options:

- Approve Phase 3 as written and begin implementation.
- Request a smaller repository scope.
- Request a different repository placement strategy, such as adding `:core:data`.
- Request schema/model changes before implementation.
- Request broader or narrower settings persistence.
