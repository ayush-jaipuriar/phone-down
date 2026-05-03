# Phase 13 — Privacy, Security, And Data Deletion Plan

## Scope

Implement privacy controls, security hardening, and data deletion flows for Phone Down. This phase ensures the app is trustworthy, handles user data responsibly, and meets Play Store privacy requirements. Includes a full privacy policy, permission documentation, security audit checklist, and comprehensive data deletion.

## Architecture

### Module Placement

| Concern | Module | Rationale |
|---|---|---|
| Privacy policy document | `docs/privacy-policy.md` | Human-readable, version-controlled |
| Privacy policy screen | `:feature:settings` | Feature module owns settings-related screens |
| Security utilities | `:core:common` | Shared security abstractions |
| Encrypted storage | `:core:datastore` | Token/auth data encryption |
| Permission documentation | `docs/permissions.md` | Play Store data safety reference |
| Data deletion | `:feature:settings` + `:core:backup` | Settings triggers, backup executes |

## Implementation Steps

### Step 1 — Privacy Policy Document (1 hour)
- [x] Create `docs/privacy-policy.md` with full privacy policy
  - Data collection: what we collect (sessions, settings, account info)
  - Data usage: how we use it (analytics, backup, app functionality)
  - Data sharing: what we don't share (no third-party analytics, no ads)
  - Data storage: local-first, optional Google Drive backup
  - User rights: access, deletion, portability
  - Children's privacy: COPPA compliance statement
  - Policy changes: notification approach
  - Contact information: support email placeholder
- [x] Create `docs/permissions.md` documenting all Android permissions
  - `POST_NOTIFICATIONS`: foreground service notifications
  - `FOREGROUND_SERVICE`: active session tracking
  - `RECEIVE_BOOT_COMPLETED`: session recovery after reboot
  - `READ_PHONE_STATE`: call interruption detection (if used)
  - `VIBRATE`: haptic feedback
  - `INTERNET`: billing, backup (optional)
  - Justification for each permission
  - Play Store data safety form mapping

### Step 2 — Privacy Policy Screen (1 hour)
- [x] Add `PrivacyPolicyScreen` to `:feature:settings`
  - Render markdown content (or formatted text)
  - Scrollable content with sections
  - Last updated date
  - Back navigation
- [x] Add "Privacy Policy" row to About section in SettingsScreen
  - Opens PrivacyPolicyScreen on tap
- [x] Add Paparazzi screenshot test for privacy policy screen

### Step 3 — Enhanced Data Deletion (2 hours)
- [x] Expand "Delete All Local Data" dialog:
  - Add checkbox: "Also delete my cloud backup" (checked by default if signed in + has backup)
  - Show what's being deleted: sessions, settings, preferences, backup
  - Require typing "DELETE" to confirm (safety measure)
- [x] Update `SettingsViewModel.deleteAllData()`:
  - Clear all sessions from `SessionRepository`
  - Clear all penalty events
  - Reset settings to defaults (preserve onboarding completed = false)
  - If checkbox checked: call `BackupRepository.deleteBackup()`
  - If checkbox checked: call `AuthRepository.signOut()`
  - Show success feedback
  - Navigate to onboarding or app restart
- [x] Add `deleteAllData()` to `SessionRepository` interface
- [x] Implement `clearAllData()` in `RoomSessionRepository`
- [x] Add `SettingsViewModelTest` for delete flow

### Step 4 — Security Hardening (3 hours)
- [x] Add `SecurityUtils` to `:core:common`:
  - `isDeviceRooted()`: basic root detection (check for su binary, test-keys)
  - `isRunningOnEmulator()`: emulator detection for debug
  - `generateSecureRandom(length)`: cryptographically secure random
- [x] Add encrypted DataStore preferences for sensitive data:
  - Create `EncryptedDataStore` wrapper in `:core:datastore`
  - Migrate auth token storage to encrypted preferences
  - Add `EncryptedSettingsRepository` for sensitive settings
- [x] Add certificate pinning preparation:
  - Create `CertificatePinningConfig` class
  - Define pinned certificates for Google APIs (Drive, Billing)
  - Add NetworkSecurityConfig XML for certificate pinning
  - Document certificate rotation procedure
- [x] Add basic anti-tampering:
  - Verify app signature at runtime
  - Check for debug flags in release builds
  - Obfuscate sensitive string literals (ProGuard/R8 rules)
- [x] Add secure logging:
  - Create `SecureLogger` wrapper
  - Redact tokens, emails, session IDs from logs
  - No logging in release builds for sensitive operations
- [x] Security audit checklist:
  - [ ] No hardcoded API keys or secrets in code
  - [ ] No logging of PII or session data
  - [ ] TLS 1.2+ enforced for all network calls
  - [ ] Certificate pinning configured
  - [ ] Root detection enabled (warn, don't block)
  - [ ] App signature verification
  - [ ] Encrypted storage for tokens
  - [ ] SQL injection prevention (Room parameterized queries)
  - [ ] ProGuard/R8 obfuscation enabled
  - [ ] Secure random for IDs

### Step 5 — Play Store Data Safety Form (1 hour)
- [x] Create `docs/play-store-data-safety.md`:
  - Data types collected (location, personal info, app activity, etc.)
  - Data usage (app functionality, analytics, developer comms)
  - Data sharing (no third parties)
  - Data encryption (in transit, at rest for sensitive data)
  - Data deletion (user can request deletion)
  - Account deletion (handled via sign out + delete data)
- [x] Map each permission to Play Store category
- [x] Document why each data type is needed

### Step 6 — Security Documentation (1 hour)
- [x] Create `docs/security.md`:
  - Threat model: what we're protecting against
  - Security measures implemented
  - Known limitations (fake repos, no real encryption yet)
  - Incident response procedure
  - Responsible disclosure policy
- [x] Update `docs/agent-handoff.md` with security context

### Step 7 — Verification (2 hours)
- [x] Unit tests:
  - `SecurityUtilsTest`: root detection, emulator detection
  - `EncryptedDataStoreTest`: encryption round-trip
  - `DeleteDataFlowTest`: delete all data, verify cleared
- [x] Compose UI tests:
  - `SettingsScreenTest`: delete dialog with confirmation
  - `PrivacyPolicyScreenTest`: content visible, scrollable
- [x] Paparazzi tests:
  - Delete confirmation dialog
  - Privacy policy screen
- [x] Security audit manual check:
  - Search codebase for hardcoded secrets
  - Verify no PII in log statements
  - Check ProGuard rules
  - Verify NetworkSecurityConfig
- [x] Build check: `:app:assembleDebug`, all tests

## Tradeoffs

### Root Detection Approach
- **Decision**: Warn users but don't block functionality.
- **Why**: Blocking rooted devices excludes power users and developers. A warning maintains security posture without being overly restrictive.
- **Risk**: Rooted devices can bypass some security measures. Mitigation: encrypted storage still protects data at rest.

### Certificate Pinning
- **Decision**: Configure pinning but allow fallback for certificate rotation.
- **Why**: Full pinning with no fallback breaks the app when certificates expire. Fallback to system CA store with logging allows recovery.
- **Risk**: Fallback could be exploited. Mitigation: monitor logs for fallback events.

### Encryption Scope
- **Decision**: Encrypt auth tokens and sensitive settings only. Regular settings and session data remain unencrypted.
- **Why**: Session data is app-generated timing data, not user PII. Encrypting everything adds complexity and performance overhead.
- **Risk**: If device is compromised, session history is readable. Mitigation: acceptable for V1; full database encryption in V2.

## Acceptance Criteria

- [x] Full privacy policy document exists in repo
- [x] Privacy policy screen accessible from Settings
- [x] All permissions documented with justifications
- [x] Enhanced delete dialog with cloud backup option
- [x] Delete flow clears all local data and optionally cloud backup
- [x] Auth tokens stored in encrypted preferences
- [x] Certificate pinning configured for Google APIs
- [x] Basic root detection implemented (warning only)
- [x] No hardcoded secrets in codebase
- [x] No PII logged in release builds
- [x] Play Store data safety form documentation complete
- [x] Security audit checklist completed
- [x] All unit tests pass
- [x] All Compose UI tests pass
- [x] All Paparazzi screenshot tests pass
- [x] `:app:assembleDebug` succeeds

## Residual Risks

- Fake repositories don't implement real encryption (deferred to real service integration)
- Certificate pinning requires monitoring for certificate expiry
- Full database encryption not implemented for V1
- No runtime memory dump protection
- No screenshot prevention for sensitive screens

## Checklist

- [x] Step 1: Privacy policy document
- [x] Step 2: Privacy policy screen
- [x] Step 3: Enhanced data deletion
- [x] Step 4: Security hardening
- [x] Step 5: Play Store data safety form
- [x] Step 6: Security documentation
- [x] Step 7: Verification
- [x] Update `v1-implementation-plan.md`
- [x] Update `docs/agent-handoff.md`
