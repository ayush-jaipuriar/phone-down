# Technical Backlog Defect Analysis - 2026-07-04

## Scope

This report analyzes four defects found during multi-week device testing:

1. A completed focus session ends without leaving a visible completion summary.
2. Default duration appears editable but does not respond in Settings.
3. Google Sign-In does not work, and Pro/account behavior needs a clear model.
4. After `This session no longer counts as clean focus`, the `Done` button does not work.

The conclusions below combine source tracing, existing tests, configuration inspection, and a targeted JVM test run. No implementation changes were made during this analysis.

## Executive Summary

| ID | Finding | Severity | Confidence | Primary Cause |
|---|---|---:|---:|---|
| TB-01 | Completion summary is implemented but immediately discarded | High | Confirmed | Foreground service clears terminal runtime as soon as completion stops the service |
| TB-02 | Settings default-duration row is wired to a no-op callback | Medium | Confirmed | `SettingsRoute` never supplies `onDefaultDurationClick` |
| TB-03 | Sign-In trust configuration is incomplete; Pro and Google identity are not one account system | High | High | `google-services.json` has only a Web OAuth client; Android certificate-bound OAuth clients are absent, and Play signing fingerprints remain unresolved |
| TB-04 | Broken-session `Done` button is guaranteed to do nothing | High | Confirmed | UI treats `Broken` as terminal while runtime treats it as recoverable and refuses to clear it |

Recommended implementation order:

1. Fix terminal-session ownership and completion summary lifecycle (TB-01 and TB-04 together).
2. Wire a real default-duration editor (TB-02).
3. Repair Google Sign-In configuration and define account/Pro semantics (TB-03).

TB-01 and TB-04 should be implemented together because both come from disagreement about which layer owns terminal session dismissal.

## TB-01 - Completed Session Summary Disappears

### Observed Behavior

A focus session reaches its target and ends, but the user is returned to the idle experience instead of seeing a useful completed-session summary.

### Expected Behavior

After completion, the app should retain a completion result until the user dismisses it. At minimum it should show:

- outcome: clean or interrupted
- focused time
- planned duration
- penalty time
- interruption count
- total elapsed time
- a clear `Done` action

### Confirmed Cause

The summary UI already exists in `SessionCompleteContent`. `FocusViewModel` also maps `SessionState.Completed` to `CompletedClean` or `CompletedInterrupted`.

The failure is lifecycle ownership:

1. A completed session sets `shouldStopService = true`.
2. `FocusSessionService` observes that state.
3. It stops its loops and service.
4. It immediately calls `runtimeCoordinator.clearFinishedRuntime()`.
5. The coordinator replaces the terminal session with an empty state.
6. `FocusViewModel` maps the empty state to `Idle` before the user can dismiss the summary.

The service should own service shutdown, but it should not own dismissal of user-visible terminal state.

### Additional Data Correctness Concern

The summary calculates focus time as `selectedDurationSeconds - remainingSeconds` and displays total time as `selectedDurationSeconds`. That is not authoritative for every result:

- `actualElapsedSeconds` can exceed planned duration because of interruptions.
- `validFocusSeconds` is the actual earned focus amount.
- `requiredDurationSeconds` can change through Add Time.
- an early-ended or broken session needs its final persisted values, not a reconstructed duration.

The UI state should expose final session metrics directly instead of recomputing them from display fields.

### Recommended Design

- Stop the foreground service when a session becomes terminal, but preserve terminal `FocusSession` state.
- Clear terminal state only from the user-driven `Done` action.
- Model a dedicated immutable completion payload in `FocusUiState`, sourced from the final `FocusSession`.
- Ensure terminal state survives activity recreation long enough to be shown. A robust implementation can reload the latest undismissed result or retain a persisted presentation marker rather than depending only on singleton memory.
- Distinguish natural completion, manual early end, invalidation, and recovered broken results in copy and metrics.

### Required Tests

- Coordinator test: natural completion sets terminal state and service-stop flag but remains visible until explicit dismissal.
- Service test: stopping service does not clear terminal runtime.
- ViewModel test: completed clean/interrupted sessions map to a completion payload.
- UI test: summary displays authoritative focus, elapsed, penalty, and interruption values.
- UI test: `Done` clears summary and returns to idle exactly once.
- Recreation test: completion remains visible after activity recreation.

## TB-02 - Default Duration Is Not Editable

### Observed Behavior

Settings shows `Default Duration` with a chevron, but tapping it does nothing.

### Expected Behavior

Tapping the row should open a duration selector. Saving a value should update DataStore and become the default for the next session.

### Confirmed Cause

The lower layers already support this feature:

- `SettingsViewModel.setDefaultDuration(seconds)` persists through `SettingsRepository`.
- `DataStoreSettingsRepository.setDefaultDurationSeconds(seconds)` writes the preference.
- tests confirm repository and ViewModel updates.

The UI route is incomplete:

- `SettingsScreen` declares `onDefaultDurationClick`, defaulting it to `{}`.
- the row calls that callback.
- `SettingsRoute` does not pass the callback at all.
- no duration dialog or sheet exists in the Settings route.

The default lambda hid the integration mistake from the compiler and existing tests only assert that the row is displayed.

### Product Consistency Concern

The Focus screen's duration selector currently persists every selection as the new default. This means choosing a one-off session duration silently changes future sessions. That conflicts with the distinction between `default duration` and `session duration`.

Recommended behavior:

- Focus screen duration selection changes the upcoming session only.
- Settings `Default Duration` changes the persistent default.
- Optional explicit `Set as default` action can bridge both flows without hidden persistence.

### Recommended Design

- Add a Settings-owned modal bottom sheet using the same duration-selector component or shared duration model as Focus.
- Wire selection to `SettingsViewModel.setDefaultDuration`.
- Remove the default no-op callback from production-facing composable APIs so missing wiring becomes a compile error.
- Decide supported presets and custom-duration entitlement once, then use the same source in Settings and Focus.
- Stop persisting one-off Focus selections unless the user explicitly chooses to make them default.

### Required Tests

- Settings route/component test: tapping `Default Duration` opens selector.
- Selection test: choosing a preset invokes `setDefaultDuration` and updates displayed value.
- Persistence test: selected default survives ViewModel/app recreation.
- Focus test: one-off duration does not mutate persistent default.
- Navigation/regression test: next session starts with the configured default when no override is supplied.

## TB-03 - Google Sign-In And Pro Account Model

### Observed Behavior

Google Sign-In does not complete. The product also needs a coherent way for a Pro user to identify their status and recover access.

### Current Architecture

Two independent systems exist:

- Google Sign-In stores a Google account identity locally and enables Drive backup authorization.
- Play Billing determines Pro entitlement from purchases and caches the entitlement locally.

`AccountViewModel` displays both account state and Pro state, but there is no backend account, entitlement server, or cross-platform Phone Down login.

### Configuration Evidence

The app has real Credential Manager / Google ID code and resolves `default_web_client_id` from generated resources. The checked-in `google-services.json` matches package `phonedown.app`, but it currently contains only OAuth client type `3` (Web client).

It does not contain Android OAuth client entries bound to package plus certificate fingerprint. Existing project documentation also leaves Play App Signing SHA-1 and SHA-256 unresolved.

This makes certificate/OAuth trust the leading cause of Sign-In failure:

- debug install needs an Android OAuth client for the debug SHA fingerprint
- locally signed release needs one for the upload/release fingerprint when applicable
- Play-installed build needs one for the Play App Signing fingerprint

Exact runtime failure should still be captured from the affected device because Credential Manager currently collapses most `GetCredentialException` variants into one generic message.

### Recommended Identity Model

Do not add a separate `Pro login` password/account system for V1.

Recommended V1 contract:

- `Google Account`: identity for backup/restore and account display.
- `Google Play Account`: purchase owner and source of Pro entitlement.
- `Restore purchases`: recovery path for Pro on another Android device using the purchasing Play account.
- Account screen: show both statuses clearly, such as `Signed in as ...` and `Pro active / Free plan`.

These accounts may differ on a device. UI copy must not promise that signing into Google automatically transfers Pro. Without a trusted backend, binding Play purchase tokens to a Google identity would be insecure and misleading.

If future requirements include web/iOS access, family/team plans, or entitlement tied to a Phone Down account, that requires a backend entitlement service and server-side Play Developer API verification. It should be a separate architecture phase.

### Recommended Sign-In Repair

1. Capture exact exception type and diagnostic code without logging tokens or personal data.
2. Register Android OAuth clients for all supported signing identities.
3. Add debug, upload/release, and Play App Signing SHA fingerprints to Firebase/Google Cloud as appropriate.
4. Download refreshed `google-services.json` after configuration changes.
5. Verify OAuth consent-screen/test-user state if the project is restricted to test users.
6. Test both local debug and Play-installed builds because they use different certificates.
7. Verify Drive authorization separately after base identity sign-in succeeds.

### Required Tests

- Unit tests for cancellation, missing config, unsupported credentials, and typed Credential Manager failures.
- Device test: debug-signed Sign-In succeeds with registered debug fingerprint.
- Play test: Play-installed Sign-In succeeds with Play App Signing fingerprint.
- Sign-out/re-sign-in test.
- Account UI test showing signed-in identity and independent Pro status.
- Billing restore test using the Play account that owns the purchase.
- Drive backup authorization test after Sign-In.

## TB-04 - Broken Session `Done` Does Nothing

### Observed Behavior

After the message `This session no longer counts as clean focus`, tapping `Done` has no effect.

### Confirmed Cause

The domain and UI disagree about `SessionState.Broken`:

- Domain engine treats `Broken` as recoverable. Returning the phone face down transitions it back toward arming, and its `result` remains null.
- Runtime therefore keeps the foreground service active and sets `shouldStopService = false`.
- UI groups `Broken` with terminal states and renders `SessionCompleteContent` with `Done`.
- `Done` calls `clearFinishedRuntime()`.
- `clearFinishedRuntime()` only clears when `shouldStopService` is true.
- For `Broken`, that condition is false, so the button is a deterministic no-op.

Existing tests verify only that the broken message renders; no test clicks `Done` or validates the cross-layer state contract.

### Product Decision

Recommended behavior: `Broken` should mean `not clean, but session can continue`.

Rationale:

- Domain code and notification copy already describe this behavior.
- Earned focus time can still be meaningful even after clean status is lost.
- A separate `End session` action already supports explicit termination.

Therefore, the screen should not show a terminal `Done` button for a live broken session.

### Recommended Design

- Present a recoverable interruption state:
  - title: `Clean status lost`
  - explanation: session can continue but will not count as clean
  - primary guidance: place phone down to continue
  - secondary action: end session
- Continue sensor processing and service operation.
- When the phone becomes valid, return through arming to active while preserving `clean=false` and `broken=true`.
- If the user ends the session, classify it into a terminal result and then show the shared completion summary.
- Reserve `Done` for terminal states only.

Alternative behavior is to make `Broken` immediately terminal, but that requires changing domain rules, persistence semantics, notification behavior, and recovery expectations. It is not recommended without a deliberate product-policy change.

### Required Tests

- Domain test: broken session can re-arm and continue while remaining non-clean.
- UI test: live broken state shows continue guidance and `End session`, not `Done`.
- End-flow test: ending broken session produces a terminal result and completion summary.
- Contract test: every state rendered with `Done` must satisfy the runtime clear condition.
- Regression test: no terminal button is wired to a guarded no-op.

## Cross-Cutting Cause

Three defects share one architectural pattern: modules expose valid lower-level behavior, but route/service integration does not enforce the same state contract.

- TB-01: service shutdown also dismisses UI result.
- TB-02: composable's default no-op callback hides missing route wiring.
- TB-04: UI terminal-state grouping conflicts with domain/runtime semantics.

Recommended engineering rule: important user actions should not default to no-op, and terminal-state ownership should be represented by one shared contract rather than repeated state lists in UI, runtime, and service code.

## Verification Performed

- Inspected focus UI, Focus ViewModel, runtime coordinator, service, session engine, settings route/ViewModel/repository, account Sign-In flow, Pro billing flow, and OAuth configuration.
- Confirmed no Android emulator/device was connected during this analysis, so device reproduction and logcat capture were unavailable.
- Ran:
  - `./gradlew :app:testDebugUnitTest :domain:session:test --console=plain`
- Result: successful.
- Existing tests passing does not invalidate these findings; current coverage misses service-to-summary lifecycle, Settings callback integration, and broken-state dismissal behavior.

## Proposed Delivery Slices

1. **Session terminal-state correction**: TB-01 + TB-04, including tests and completion-summary metric cleanup.
2. **Duration editing correction**: TB-02, including shared selector behavior and persistence semantics.
3. **Identity and entitlement correction**: TB-03, including diagnostic improvements, OAuth fingerprint setup, and account/Pro UI contract.

Before implementation, each slice should be added to the approved technical execution plan with acceptance criteria and then implemented in that order.

## Implementation Update - 2026-07-04

- TB-01 implemented locally: service shutdown preserves terminal result state, and summary metrics now use authoritative session fields.
- TB-02 implemented locally: Settings provides an explicit default-duration picker; Focus duration overrides no longer mutate the saved default.
- TB-03 local implementation completed: safe typed Sign-In messages and separate Google identity / Play Pro status are present. Android OAuth certificate registration and device verification remain external prerequisites.
- TB-04 implemented locally: recoverable broken sessions no longer render a terminal `Done` action; they provide continue guidance and an explicit end flow.

### Post-Implementation Verification

- Passed app, session, insights-domain, backup, and database JVM test suites.
- Passed Focus and Settings Paparazzi screenshot verification.
- Android UI test sources for Focus and Settings compile successfully; no connected device was available to execute instrumentation tests.
- Signed release bundle completed successfully with `:app:bundleRelease`.
- Insights Paparazzi verification remains unstable because its calendar header uses the current date: retained snapshots show May dates while the 2026-07-04 run rendered July dates. The two failures differed by approximately 0.31% and 0.51%; no Insights or chart source files changed in this iteration.
- Global ktlint and detekt remain blocked by existing repository-wide baseline violations, including unchanged build-script formatting, route complexity, and older test formatting. New code was compiled and exercised by the targeted suites; the one newly introduced coordinator-test formatting violation reported by ktlint was corrected.
