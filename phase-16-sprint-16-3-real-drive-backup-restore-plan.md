# Sprint 16.3 - Real Google Drive Backup And Restore Plan

## Status

- Planning status: Drafted
- Implementation status: Not started
- Approval required before implementation: Yes
- Phase: Phase 16 - Android Production Readiness
- Primary goal: Replace fake in-memory backup behavior with real Google Drive `appDataFolder` backup/restore and add real once-daily auto-backup

## 1. Sprint Purpose

Sprint 16.3 makes Phone Down's backup system real.

Today, the app already has:

- backup and restore UI
- backup DTO/schema/serializer logic
- a fake backup repository
- Pro/sign-in backup gating
- restore confirmation flow
- settings fields for backup opt-in, auto-backup enabled, and last backup time

What it does **not** yet have is a production transport and scheduling layer.

This sprint closes that gap by implementing:

- real manual backup
- real manual restore
- real daily auto-backup
- Google Drive hidden storage using `appDataFolder`
- one-latest-backup semantics

This sprint matters because backup is a trust feature, not just a nice-to-have utility. If users are asked to sign in and pay for Pro in part because of cloud backup, the backup path must be real, predictable, and recoverable.

## 2. Confirmed Product Decisions

- [x] Include all three flows in this sprint:
  - manual backup
  - manual restore
  - once-daily auto-backup
- [x] Restore remains **full replace**, not merge.
- [x] Backup/restore actions remain blocked until Google sign-in is complete.
- [x] Store only **one latest backup file** in Drive `appDataFolder`.
- [x] Daily auto-backup runs via periodic background work when constraints allow.
- [x] If user is signed out or offline, auto-backup should skip and retry later.
- [x] Backup remains **Pro-only**.
- [x] No-backup-found restore should show a simple, friendly message.
- [x] Backup authoritative contents:
  - sessions
  - penalty events
  - settings/preferences
- [x] Pro entitlement cache should **not** be treated as restore-authoritative backup data.

## 3. Existing Baseline We Will Build On

### 3.1 Already Present In Repo

- `:core:backup`
  - `BackupData`
  - `BackupSerializer`
  - `BackupDataMapper`
  - `FakeBackupRepository`
- `:core:model`
  - `BackupRepository`
  - `BackupResult`
  - `RestoreResult`
  - `RestorePayload`
  - `SettingsRepository`
- `:app`
  - `RestoreBackupUseCase`
  - `SettingsViewModel.triggerBackup()`
  - `AccountViewModel.restoreBackup()`
- UI
  - Account restore flow
  - Settings backup row
  - Pro/sign-in gating copy

### 3.2 What This Means Architecturally

We are **not** redesigning the backup product surface from scratch.

We are upgrading:

- transport: fake memory -> real Drive API
- restore data source: fake payload -> real downloaded payload
- scheduling: manual only -> manual plus WorkManager auto-backup
- operational rules: optimistic local dev behavior -> real auth/network/background constraints

That is a much safer sprint shape than broad backup feature churn.

## 4. Product Behavior Contract

### 4.1 Manual Backup

If the user is:

- Pro
- signed in
- and taps the backup action

Then the app should:

1. collect local backup-authoritative data
2. serialize it into schema-versioned backup JSON
3. upload it into Drive `appDataFolder`
4. replace any existing prior backup file with the new latest backup
5. persist `lastBackupEpochMillis`
6. show success/failure feedback

### 4.2 Manual Restore

If the user is:

- Pro
- signed in
- and confirms restore

Then the app should:

1. fetch the latest backup file from Drive `appDataFolder`
2. fail clearly if no backup exists
3. validate schema version
4. deserialize payload
5. block restore if an active focus runtime exists
6. replace local sessions/penalties
7. restore settings
8. return success with restored-session count

### 4.3 Auto-Backup

If the user is:

- Pro
- signed in
- opted into backup
- auto-backup enabled

Then the app should schedule periodic background work that:

- runs approximately once every 24 hours
- requires network connectivity
- skips gracefully if the user is signed out when work executes
- uploads the latest backup payload to the same single Drive file slot
- updates `lastBackupEpochMillis` on success

## 5. Technical Design

## 5.1 Drive Storage Model

### Decision

Use Google Drive `appDataFolder` with a single logical backup file.

### Why

- hidden from users, which matches “app backup” mental model
- narrow permission scope: `drive.appdata`
- avoids cluttering user-visible Drive
- simpler restore selection logic
- simpler retention logic

### V1 File Strategy

Recommendation for implementation:

- logical app file name: `phone-down-backup-v1.json`
- if a previous backup exists, replace it
- if multiple stale files somehow exist, choose latest modified one and optionally clean extras during successful backup

This gives us deterministic restore behavior.

## 5.2 Repository Split

### Keep The Existing Contract

We should keep `BackupRepository` as the main product-facing abstraction.

That is good because:

- UI and ViewModels already depend on it
- fake repository can remain available for tests
- real implementation can be swapped in through DI

### Add A Real Production Implementation

Planned new implementation:

- `DriveBackupRepository`

Probable responsibility split:

- `DriveBackupRepository`
  - orchestrates backup/restore semantics
  - knows how to create or fetch latest backup payload
- `DriveAppDataClient`
  - raw Drive file operations
  - list/create/update/download/delete
- small auth/access-token provider abstraction if needed
  - converts signed-in Google account state into an authorized Drive client request path

## 5.3 Authentication For Drive Access

### Important Theory

Google Sign-In identity and Drive API authorization are related but not identical.

We already completed Sprint 16.2 for sign-in identity. Sprint 16.3 now needs actual Drive-authorized requests with the `drive.appdata` scope.

This means the sprint must solve:

- how the app obtains the right authorized Google account context
- how it requests Drive access cleanly
- how it fails if that authorization is missing or expired

### Working Recommendation

Use the signed-in Google account as the identity source and add a Drive-authorized access path specifically for backup operations.

Implementation detail may land as either:

- a Play Services / Google auth helper that fetches an access token for `drive.appdata`
- or another Google-recommended Android authorization path compatible with the current sign-in stack

We should choose the narrowest path that:

- reuses the current signed-in account
- avoids persisting raw tokens
- keeps account/Drive logic out of feature modules

### Constraint

Do **not** make restored billing/pro entitlement depend on Drive data.

Drive backup is user data transport, not account authority.

## 5.4 Restore Safety Rules

Restore is destructive at the local-data level, so we need explicit guardrails.

### Required Rules

- Block restore when a focus runtime is active
  - existing `RestoreBackupUseCase` already checks this
- Require explicit confirmation
  - already present in UI
- Show “No backup found” clearly
- Fail on schema mismatch
- Never partially merge data in V1

### Why This Matters

Full replace is simpler and less bug-prone, but only if it is treated as an intentional destructive operation with clear user messaging.

## 5.5 Auto-Backup Scheduling

### Decision

Use `WorkManager`.

### Why

For daily background work on Android, WorkManager is the correct production primitive because it:

- persists across process death
- survives app restarts better than in-memory timers
- lets us specify network constraints
- is OS-friendly for deferred periodic work

### Planned Work Characteristics

- periodic work request
- repeat interval: 24 hours
- network required
- likely battery-not-low optional, but not mandatory for V1
- unique work name to avoid duplicate scheduling

### Important Tradeoff

Periodic work timing is approximate, not exact.

That is okay here because “daily backup” is a resilience feature, not a precision-timer feature. We want eventual daily backup behavior, not exact-at-midnight semantics.

## 5.6 Settings State Semantics

The settings layer already contains:

- `backupOptIn`
- `autoBackupEnabled`
- `lastBackupEpochMillis`

We should preserve this model.

### Proposed Semantics

- `backupOptIn`
  - user has explicitly agreed to use Drive backup
- `autoBackupEnabled`
  - periodic work should be scheduled when eligible
- `lastBackupEpochMillis`
  - updated only after successful backup completion

### Eligibility Rule

Auto-backup should only actually schedule/run if all are true:

- Pro user
- signed in
- `backupOptIn == true`
- `autoBackupEnabled == true`

## 6. Implementation Workstreams

## 6.1 Workstream A - Drive Transport Layer

### Goal

Build the real file transport layer for `appDataFolder`.

### Planned Tasks

- [ ] Add Drive API dependencies required for Android-side file operations
- [ ] Introduce `DriveAppDataClient` (name can vary if a better repo-local naming pattern appears)
- [ ] Implement:
  - [ ] list backup files in `appDataFolder`
  - [ ] fetch latest backup file metadata
  - [ ] download latest backup file content
  - [ ] create new backup file
  - [ ] update/replace existing backup file
  - [ ] optionally clean duplicate stale files after successful write
- [ ] Handle transport failures:
  - [ ] no network
  - [ ] unauthorized / missing Drive permission
  - [ ] malformed response
  - [ ] empty file
  - [ ] API/server failure

### Acceptance Notes

At the end of this workstream, we should be able to upload and download raw backup JSON bytes outside the UI.

## 6.2 Workstream B - Real Backup Repository

### Goal

Replace fake in-memory backup semantics with production Drive-backed semantics.

### Planned Tasks

- [ ] Create `DriveBackupRepository`
- [ ] Keep `FakeBackupRepository` for tests/dev-only scenarios if still useful
- [ ] Update DI so normal runtime uses `DriveBackupRepository`
- [ ] `createBackup(...)` should:
  - [ ] map domain data -> backup DTO
  - [ ] serialize JSON
  - [ ] write to Drive
  - [ ] update timestamp result
- [ ] `fetchRestorePayload()` should:
  - [ ] fetch latest backup file
  - [ ] return `NoBackupFound` when absent
  - [ ] deserialize
  - [ ] validate schema
  - [ ] map DTO -> domain payload
- [ ] `restoreBackup()` should remain consistent with repository contract
- [ ] `getLastBackupTime()` should align with settings-cached time
- [ ] `deleteBackup()` should remove latest backup from Drive

### Design Constraint

Do not bury local database replacement logic inside the transport layer. Keep restore orchestration aligned with the existing `RestoreBackupUseCase` ownership where possible.

## 6.3 Workstream C - Auto-Backup Scheduling

### Goal

Implement real periodic backup execution.

### Planned Tasks

- [ ] Add WorkManager dependency and app initialization setup if missing
- [ ] Create `AutoBackupWorker`
- [ ] Inject needed collaborators into worker
- [ ] Create scheduling helper/coordinator:
  - [ ] schedule unique periodic auto-backup work
  - [ ] cancel work when user disables auto-backup
  - [ ] cancel work when user signs out or loses Pro eligibility if appropriate
- [ ] Worker should:
  - [ ] verify eligibility again at runtime
  - [ ] load sessions, penalties, settings
  - [ ] call real backup repository
  - [ ] update last-backup time on success
  - [ ] return retry/failure appropriately

### Retry Strategy

Recommended V1:

- retry on transient network/server issues
- finish without noisy failure on signed-out / ineligible state

That keeps the worker polite and avoids endless useless retries.

## 6.4 Workstream D - UI And ViewModel Integration

### Goal

Keep the current product surface, but ensure it is powered by real repository behavior and scheduling state.

### Planned Tasks

- [ ] Update `SettingsViewModel.triggerBackup()` to work with real Drive repository
- [ ] Ensure last backup timestamp remains single source of truth
- [ ] Decide where `backupOptIn` becomes true:
  - [ ] likely first successful manual backup
  - [ ] or explicit toggle path if already present
- [ ] Ensure backup row behavior is correct for:
  - [ ] free users
  - [ ] Pro signed-out users
  - [ ] Pro signed-in users with no backup
  - [ ] Pro signed-in users with prior backup
- [ ] Update restore messaging if needed to mention latest hidden Google Drive backup
- [ ] Ensure “No backup found for this account” copy path is implemented cleanly
- [ ] Ensure auto-backup toggle only appears when useful and real

### UX Principle

Hide or disable dead states. If a control is visible in Sprint 16.3, it should reflect a real, functioning backend path.

## 6.5 Workstream E - Restore Robustness

### Goal

Make restore trustworthy, not just nominally functional.

### Planned Tasks

- [ ] Reconfirm `RestoreBackupUseCase` remains the orchestration owner
- [ ] Ensure full replace uses current repository/database bulk replacement path
- [ ] Validate no active runtime before restore
- [ ] Ensure settings restore does not loop into accidental extra writes or bad scheduling side effects
- [ ] Decide whether restore should reschedule auto-backup work after settings are restored
  - [ ] recommended: yes, based on restored backup settings and current account eligibility

## 7. Data And State Rules

## 7.1 What Gets Backed Up

- [x] Focus sessions
- [x] Penalty events
- [x] User settings/preferences

## 7.2 What Does Not Become Backup Authority

- [x] Pro entitlement state
- [x] billing ownership
- [x] auth identity authority
- [x] ephemeral runtime/session-in-progress state

## 7.3 File Retention Rule

- [x] One latest backup only

## 7.4 Restore Rule

- [x] Full replace only

## 8. Error Handling Matrix

| Scenario | Expected Result |
|---|---|
| User not signed in | Show sign-in CTA, block backup/restore actions |
| User not Pro | Show Pro gate, block backup/restore actions |
| No network during manual backup | Show clear backup failure, preserve local data |
| No backup found during restore | Show friendly “No backup found for this account” |
| Schema version mismatch | Show restore failure, preserve local data |
| Active focus runtime during restore | Block restore with explanatory message |
| Worker runs while signed out | Exit gracefully without noisy user-facing failure |
| Worker runs while free-tier | Exit gracefully and cancel/respect ineligibility rules |
| Duplicate backup files exist | Use latest file; optionally clean extras on successful write |

## 9. Testing And Verification Plan

## 9.1 Unit Tests

- [ ] `DriveBackupRepositoryTest`
  - [ ] create backup success
  - [ ] no backup found
  - [ ] schema mismatch failure
  - [ ] duplicate-file selection chooses latest
- [ ] `DriveAppDataClientTest` or equivalent mock transport tests
- [ ] `AutoBackupWorkerTest`
  - [ ] eligible success path
  - [ ] signed-out skip path
  - [ ] network/transient retry path
- [ ] `RestoreBackupUseCaseTest`
  - [ ] active runtime blocked
  - [ ] payload replace success
  - [ ] no-backup-found message

## 9.2 Integration / Repository Tests

- [ ] serializer round-trip still passes with production repository
- [ ] repository + mapper path preserves stable schema strings
- [ ] settings restore + repository interaction preserves backup fields correctly

## 9.3 UI Tests

- [ ] settings backup row states still render correctly
- [ ] account restore section states still render correctly
- [ ] backup in-progress / failure / success states verified
- [ ] restore in-progress / failure / success states verified

## 9.4 Build / Regression Checks

- [ ] `./gradlew --no-configuration-cache :app:assembleDebug`
- [ ] targeted unit tests for `:core:backup`, `:app`, `:feature:settings`, `:feature:account`
- [ ] relevant screenshot/UI tests
- [ ] `git diff --check`

## 9.5 Manual QA

- [ ] signed-in Pro manual backup succeeds
- [ ] signed-in Pro manual restore succeeds on a second device / after data reset
- [ ] free-tier user cannot access real backup actions
- [ ] signed-out Pro user gets sign-in gating
- [ ] no-backup-found path behaves correctly
- [ ] auto-backup schedules and executes under eligible conditions
- [ ] restored data actually reappears in Focus/Insights/Settings surfaces

## 10. Risks And Tradeoffs

### 10.1 Drive Authorization Complexity

This is the main engineering risk.

Why:

- Sprint 16.2 solved identity sign-in
- Sprint 16.3 adds actual Drive-scoped data access
- Android-side Google auth flows can be subtle when you separate account identity from API authorization

Mitigation:

- isolate auth-to-Drive authorization in a small app/core integration layer
- keep repository and UI logic independent from token-fetch details

### 10.2 WorkManager Timing Expectations

Users may imagine exact daily timing.

Reality:

- WorkManager is deferred and approximate

Mitigation:

- product copy should imply periodic automatic backup, not exact scheduled backup time

### 10.3 Destructive Restore

Full replace is operationally safer for implementation, but user-riskier if messaging is weak.

Mitigation:

- keep explicit confirmation
- mention overwrite clearly
- show no-backup-found clearly

### 10.4 Billing/Auth Drift

Because backup is Pro-only and sign-in-dependent, sprint behavior depends on both auth and entitlement being stable.

Mitigation:

- gate eligibility in both UI and worker runtime
- do not restore entitlement from backup

## 11. Acceptance Criteria

- [ ] Manual backup writes real data to Google Drive `appDataFolder`
- [ ] Manual restore reads real data from Google Drive `appDataFolder`
- [ ] Restore remains full replace
- [ ] Only the latest backup is kept logically authoritative
- [ ] Backup remains Pro-only
- [ ] Backup/restore remain sign-in-gated
- [ ] “No backup found for this account” path works
- [ ] Once-daily auto-backup is scheduled with WorkManager
- [ ] Worker skips gracefully when signed out/ineligible/offline
- [ ] Last backup timestamp updates only on successful backup
- [ ] Existing settings/account UI continues to function with real repository behavior
- [ ] Automated verification passes for the touched modules
- [ ] Manual device QA confirms real backup/restore behavior

## 12. Progress Checklist

- [x] Workstream A - Drive transport layer
- [x] Workstream B - Real backup repository
- [x] Workstream C - Auto-backup scheduling
- [x] Workstream D - UI and ViewModel integration
- [x] Workstream E - Restore robustness
- [x] Unit tests
- [ ] Integration/repository tests
- [x] UI tests
- [x] Build/regression verification
- [x] Manual QA
- [x] Update `phase-16-android-production-readiness-plan.md`
- [ ] Update `docs/architecture-guide.md` if architecture changes materially
- [x] Update `docs/agent-handoff.md`

## 12.1 Implementation Progress Update - 2026-05-16

- [x] Added a real Drive-scoped authorization layer in `:app`:
  - `GoogleDriveAuthorizationManager`
  - `DriveAuthorizationCoordinator`
  - `DriveAccessTokenProvider`
- [x] Added a real Drive transport/repository path in `:core:backup`:
  - `DriveAppDataClient`
  - `DriveBackupRepository`
- [x] Replaced fake runtime backup DI with the real Drive-backed repository in `AppRuntimeModule`.
- [x] Added WorkManager-based auto-backup scheduling and worker execution:
  - `AutoBackupScheduler`
  - `AutoBackupScheduling`
  - `AutoBackupWorker`
- [x] Wired manual backup authorization and auto-backup toggle behavior into Settings flow.
- [x] Wired restore authorization into Account flow.
- [x] Preserved the one-latest-backup and full-replace restore model.
- [x] Added/updated test scaffolding to keep screenshot and ViewModel tests aligned with the new constructor and callback contracts.
- [x] Verification completed:
  - `./gradlew --no-daemon --no-configuration-cache :app:assembleDebug`
  - `./gradlew --no-daemon --no-configuration-cache :core:backup:testDebugUnitTest :app:testDebugUnitTest :feature:settings:testDebugUnitTest :feature:account:testDebugUnitTest`
- [x] Real device/manual QA completed on a connected Android device:
  - manual backup initially failed because `android.permission.INTERNET` was missing from `AndroidManifest.xml`
  - backup then failed again because the Drive authorization resolution path lost the pending account email before `completeAuthorization()`
  - backup then failed again because the app-wide `network_security_config.xml` pinned Google hosts with placeholder pins, causing TLS handshake failures for `www.googleapis.com`
  - after fixing those three issues, manual backup succeeded and the Settings row updated to `Last backup: ...`
  - the first successful real backup also exposed the once-daily `Auto Backup` toggle exactly as designed
  - restore was manually confirmed through the Account screen and completed successfully with `Restore Complete` / restored-session feedback
  - a visible mutated setting (`Auto Backup` toggled off locally) returned to the backed-up enabled state after restore, proving full settings replacement worked end to end
- [x] Explicit no-backup-found device QA completed through the real destructive path:
  - `Delete All Data` with `Also delete cloud backup` now pre-authorizes Drive access and deletes cloud state before wiping local data
  - the delete flow no longer treats cloud-delete uncertainty as success; `DeleteBackupResult` now distinguishes deleted, no-backup, and real failure states
  - after deleting the current hidden Drive backup, re-signing in to the same Google account, and confirming restore, the app showed the expected empty-state body: `No backup found for this account.`
  - a small UX polish followed so this branch now renders as `No Backup Found` instead of the generic `Restore Failed`
- [ ] Still pending before closing the sprint:
  - optional deeper integration tests around transport behavior if we want broader regression coverage

## 13. Recommended Implementation Order

1. Drive transport layer
2. real repository swap in DI
3. restore path verification first
4. manual backup path verification
5. WorkManager auto-backup
6. UI/state cleanup and test expansion

Why this order:

- restore and backup depend on the transport layer
- auto-backup depends on real repository behavior
- UI cleanup is safer after the real backend semantics are stable

## 14. Review Request

Please review this sprint plan for:

- backup/restore product behavior
- auto-backup expectations
- one-latest-backup retention model
- any restore messaging you want refined before implementation

Common next steps:

1. approve and start implementation
2. request changes to the sprint plan
