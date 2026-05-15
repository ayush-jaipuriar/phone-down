# Phase 16 Sprint 16.2 - Real Google Sign-In Plan

## Status

- Planning status: Drafted
- Implementation status: Implemented, pending Web OAuth client config for manual sign-in QA
- Approval required before implementation: Completed
- Parent phase: Phase 16 - Android Production Readiness
- Sprint goal: Replace the fake debug sign-in path with real Google Sign-In for local/debug builds while preserving clean module boundaries and testability

## 1. Purpose

Sprint 16.2 turns Phone Down's account sign-in from a simulated development behavior into a real Google identity flow.

The goal is intentionally narrow:

- let the user sign in with a real Google account
- show the real account identity in the Account and Settings surfaces
- support real sign-out
- keep fake auth available for tests
- avoid storing raw tokens unnecessarily

This sprint does not implement real Drive backup yet. Drive backup needs Google authorization/access-token work, which belongs in Sprint 16.3.

## 2. Current Setup State

The browser-side setup is now far enough for debug Google Sign-In implementation:

- Firebase project: `phone-down`
- Google Cloud project ID: `phone-down-496414`
- Android package: `phonedown.app`
- `app/google-services.json` exists locally and is ignored by git
- Google services Gradle plugin is wired in the repo
- Google Drive API is enabled
- OAuth consent is configured
- OAuth audience is `External`
- OAuth publishing status is `Testing`
- required scopes are configured:
  - `openid`
  - `email`
  - `profile`
  - `https://www.googleapis.com/auth/drive.appdata`
- at least one OAuth test user is configured
- Android OAuth debug client exists:
  - name: `Phone Down Android Debug`
  - type: Android
  - package: `phonedown.app`
  - debug SHA-1 registered

## 3. Mental Model

Google identity work has three layers:

1. Firebase/Google project identity
2. OAuth consent policy
3. Android runtime sign-in code

The console work completed so far covers the first two layers. Sprint 16.2 implements the third layer.

Think of it this way:

- Firebase app registration says "this Android app belongs to this project."
- OAuth consent says "this app may ask approved test users for these identity permissions."
- Credential Manager says "show the real sign-in UI on the Android device and return the selected account identity."

## 4. Recommended Technical Approach

Use Android Credential Manager with Sign in with Google.

Reasoning:

- Android's current official Sign in with Google guidance points developers toward Credential Manager.
- It keeps sign-in UI in the Android-native account chooser flow.
- It avoids directly building an Activity-result-based legacy sign-in path.
- It fits our existing ViewModel/repository architecture if we add a small app-layer launcher/coordinator.

Important distinction:

- Credential Manager gives us the signed-in Google identity and ID token.
- Google Drive backup will still need authorization for Drive access in Sprint 16.3.
- We should not treat an ID token as a Drive API access token.

Reference docs:

- Android Sign in with Google via Credential Manager: https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation
- Android authorization for Google user data: https://developer.android.com/identity/authorization
- Google Play services `AuthorizationClient`: https://developers.google.com/android/reference/com/google/android/gms/auth/api/identity/AuthorizationClient

## 5. Architecture Boundaries

### `:core:model`

Owns stable contracts and models:

- `AuthRepository`
- `AccountState`
- any auth result/error model if needed

The model module must not depend on Android Credential Manager, Google Play services, Firebase, or Hilt.

### `:core:auth`

Owns auth implementation details:

- existing `FakeAuthRepository`
- new real Google auth implementation
- auth-specific mapping from Google credential payload to `AccountState`

This module may depend on Android Credential Manager / Google Identity libraries if we keep the implementation here.

### `:app`

Owns Android runtime coordination:

- Hilt binding selection
- Activity/context-aware sign-in trigger if needed
- route/ViewModel integration
- error display and manual QA hooks

### `:feature:account`

Remains UI-only:

- renders signed-out, loading, signed-in, and error states
- does not know about Credential Manager or Google APIs

## 6. Contract Design Decision

The current `AuthRepository` has:

```kotlin
interface AuthRepository {
    val accountState: Flow<AccountState>
    suspend fun signIn()
    suspend fun signOut()
    fun getAuthToken(): String?
}
```

This is too simple for real Google Sign-In because sign-in needs an Android UI/context boundary.

Recommended change:

- keep `accountState`
- keep `signOut()`
- remove or deprecate direct token usage from account UI
- introduce an app-layer sign-in coordinator for the actual Credential Manager call
- let the repository expose methods that can accept a verified Google identity result

Recommended shape:

```kotlin
interface AuthRepository {
    val accountState: Flow<AccountState>
    suspend fun applyGoogleAccount(account: GoogleAccount)
    suspend fun signOut()
}
```

Potential supporting model:

```kotlin
data class GoogleAccount(
    val id: String?,
    val displayName: String?,
    val email: String,
    val photoUrl: String?,
)
```

Why this is cleaner:

- `:core:model` stays platform-neutral.
- the Android UI sign-in mechanism stays outside pure model contracts.
- tests can apply fake account results without launching Google UI.

Implementation may keep compatibility helpers if many callers currently invoke `signIn()` directly, but release code should not depend on fake `signIn()` behavior.

## 7. Implementation Steps

### 7.1 Dependency Setup

- [x] Add Credential Manager versions to `gradle/libs.versions.toml`.
- [x] Add Google Identity / `googleid` dependency.
- [x] Add Credential Manager Play Services bridge dependency.
- [x] Add dependencies only to the module that truly needs them.
- [x] Avoid Firebase Auth unless we explicitly decide to use Firebase Auth as a separate account system.

Recommended initial dependencies:

- `androidx.credentials:credentials`
- `androidx.credentials:credentials-play-services-auth`
- `com.google.android.libraries.identity.googleid:googleid`
- possibly `com.google.android.gms:play-services-auth` for Sprint 16.3 authorization

### 7.2 Model Updates

- [x] Review `AccountState`.
- [x] Add account ID.
- [x] Add auth error/loading state where UI needs it.
- [x] Add a platform-neutral Google account result model.
- [x] Remove token-first language from auth interfaces where practical.

### 7.3 Real Auth Implementation

- [x] Add a real repository implementation in `:core:auth`.
- [x] Persist minimal signed-in account display state.
- [x] Do not persist ID tokens.
- [x] Do not log ID tokens, email in debug logs, or raw credential payloads.
- [x] Preserve `FakeAuthRepository` for unit tests and preview/test injection.

### 7.4 App-Layer Sign-In Coordinator

- [x] Add a small app-layer coordinator that invokes Credential Manager.
- [x] Request Sign in with Google with the configured OAuth setup.
- [x] Map `GoogleIdTokenCredential` to the platform-neutral account model.
- [x] Handle cancellation as a non-error user action.
- [x] Handle missing configuration with user-friendly UI copy.
- [x] Handle Google Play services/device errors without crashing.

### 7.5 ViewModel and UI Wiring

- [x] Update Account route/ViewModel to use the real sign-in coordinator path.
- [x] Add a loading state while sign-in is in progress.
- [x] Add an error state for failed sign-in.
- [x] Keep account feature UI free of Google API types.
- [x] Ensure Settings reflects signed-in/signed-out state correctly through `AuthRepository.accountState`.

### 7.6 DI Wiring

- [x] Bind real auth implementation for normal app builds.
- [x] Keep fake auth available for unit tests.
- [x] Confirm normal app DI no longer binds `FakeAuthRepository`.
- [x] Avoid creating a hidden debug-only behavior that makes manual QA misleading.

### 7.7 Docs Updates

- [x] Update this plan as implementation progresses.
- [x] Update `phase-16-android-production-readiness-plan.md`.
- [x] Update `docs/phase-16-console-setup-info.md` if any new safe values are needed.
- [x] Update `docs/agent-handoff.md`.
- [ ] Update `docs/architecture-guide.md` if auth architecture changes materially.

## 8. Testing Plan

### Unit Tests

- [x] `:core:auth:testDebugUnitTest`
  - fake repository still works for tests
  - real repository maps account results correctly
  - sign-out clears account state
- [x] `:app:testDebugUnitTest`
  - `AccountViewModel` loading/error/success state mapping
  - sign-out behavior
  - Settings signed-in state mapping

### Build Checks

- [x] `./gradlew --no-configuration-cache :core:auth:testDebugUnitTest`
- [x] `./gradlew --no-configuration-cache :app:testDebugUnitTest`
- [x] `./gradlew --no-configuration-cache :feature:account:testDebugUnitTest`
- [x] `./gradlew --no-configuration-cache :feature:settings:testDebugUnitTest`
- [x] `./gradlew --no-configuration-cache :app:assembleDebug`
- [x] `git diff --check`

### Manual Device QA

- [ ] Install debug app on physical Android device.
- [ ] Open Account screen.
- [ ] Tap Sign in with Google.
- [ ] Confirm Google account chooser appears.
- [ ] Sign in with the OAuth test user account.
- [ ] Confirm Account screen shows real account email/name.
- [ ] Confirm Settings reflects signed-in account state.
- [ ] Sign out.
- [ ] Confirm backup/restore actions return to signed-out gated state.
- [ ] Reinstall app and confirm account state behavior is acceptable.

Manual QA is currently blocked by console/config state: the local `google-services.json` does not generate `default_web_client_id`. Credential Manager Sign in with Google requires a Web OAuth client ID, so the next browser-side step is to create/download config that includes that client before physical-device sign-in QA.

## 9. Acceptance Criteria

- [ ] User can sign in with a real Google account on a debug build.
- [ ] Account screen shows real signed-in account identity.
- [ ] Settings screen reflects signed-in state.
- [ ] User can sign out.
- [ ] Sign-out clears local signed-in state.
- [x] Fake auth is no longer used for normal app runtime DI.
- [x] Unit tests still use controllable fake auth.
- [x] No raw tokens, OAuth credentials, or sensitive payloads are logged or committed.
- [x] Build and focused test suite pass.
- [x] Residual release-signing fingerprint work is documented for later Play/internal testing.

## 10. Known Follow-Ups

These are intentionally not part of Sprint 16.2:

- real Drive `appDataFolder` backup and restore
- Google authorization access-token retrieval for Drive
- auto-backup WorkManager integration
- Play Billing implementation
- Firebase Crashlytics runtime setup
- Play App Signing SHA validation for Play-distributed builds
- Web OAuth client creation / updated `google-services.json` if Firebase config still lacks `default_web_client_id`

## 11. Open Questions

No user clarification is currently needed before approving this sprint plan.

Implementation details to resolve while coding:

- whether `AccountState` needs a stable Google subject/account ID
- whether signed-in display state should survive process death before Drive auth exists
- whether Credential Manager should live in `:core:auth` or be coordinated from `:app` with only mapping in `:core:auth`

Recommended default:

- keep Credential Manager invocation in `:app`
- keep persistent auth/account state and fake/test implementations behind `AuthRepository`
- defer Drive token authorization to Sprint 16.3
