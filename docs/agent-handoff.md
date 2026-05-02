# Agent Handoff Summary

## 1. Goal
- Finish Phase 7 of Phone Down by making the Focus experience honest, polished, and aligned with the approved mockups.
- Keep Phase 6 and Phase 7 grounded in real validation: automated checks are passing, but physical-device QA is still required before we call the runtime and Focus ritual fully closed.
- Preserve the repo’s phase workflow: no new phase implementation until the current phase is honestly validated and the user explicitly wants to move on.

## 2. Context The Next Agent Must Know
- Read `AGENTS.md` first. This repo requires:
  - clarification questions before writing any new phase plan
  - user approval before implementing a new phase plan
  - documentation updates during meaningful progress
  - comprehensive verification before claiming completion
- Architectural direction:
  - runtime/service orchestration lives in `app/runtime`
  - Focus route/viewmodel wiring lives in `:app`
  - `feature:focus` is a UI-first module that consumes mapped presentation state
  - domain timing/classification lives in `:domain:session`
  - sensors live in `:core:sensors`
- Important product decisions already reflected in the current Phase 7 work:
  - Focus is the only scope of Phase 7
  - Insights and Settings remain placeholder routes
  - Focus uses one primary stateful surface, not many separate screens
  - duration selector supports presets plus custom duration entry
  - sensor-unavailable remains a blocked state; no fake manual fallback
- Repo-sequence note:
  - `v1-implementation-plan.md` still defines **Phase 8 = Onboarding** and **Phase 9 = Insights**
  - any older mention of “Phase 8 Insights” is stale and should not be followed

## 3. Work Completed
- Reviewed the Phase 7 implementation that another agent started and verified the real repo state before editing.
- Confirmed the Focus-route architecture:
  - `app/src/main/java/phonedown/app/focus/FocusRoute.kt`
  - `app/src/main/java/phonedown/app/focus/FocusViewModel.kt`
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusUiState.kt`
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusEvent.kt`
- Fixed the main review issues in the current Phase 7 work:
  - added real custom duration entry to the Focus duration sheet
  - added limit-aware custom-duration messaging using `UserSettings.freeCustomDurationSeconds`
  - corrected `PausedByCall` UI copy so it is no longer rendered like a pickup interruption
  - made sensor-unavailable `Retry` trigger a real sensor-monitor restart path through the service
  - fixed the selected-duration start race by threading the chosen duration through `MainActivity` and `FocusSessionService` instead of relying on an async DataStore write to win the race
- Updated runtime/service plumbing:
  - `app/src/main/java/phonedown/app/runtime/FocusSessionServiceContract.kt`
  - `app/src/main/java/phonedown/app/runtime/ActiveSessionRuntimeCoordinator.kt`
  - `app/src/main/java/phonedown/app/runtime/FocusSessionService.kt`
  - `app/src/main/java/phonedown/app/MainActivity.kt`
  - `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt`
- Updated Focus UI and screenshot coverage:
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - `feature/focus/src/test/kotlin/phonedown/feature/focus/FocusScreenScreenshotTest.kt`
  - Paparazzi snapshots under `feature/focus/src/test/snapshots/images/`
- Verification run in this pass:
  - `git diff --check`
  - `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :app:assembleDebug :feature:focus:testDebugUnitTest`
  - `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :feature:focus:recordPaparazziDebug`

## 4. Current Workspace State
- Branch: `main`
- `git status` at this handoff includes uncommitted changes
- Modified tracked files:
  - `app/src/main/java/phonedown/app/MainActivity.kt`
  - `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt`
  - `app/src/main/java/phonedown/app/runtime/ActiveSessionRuntimeCoordinator.kt`
  - `app/src/main/java/phonedown/app/runtime/FocusSessionService.kt`
  - `app/src/main/java/phonedown/app/runtime/FocusSessionServiceContract.kt`
  - `docs/agent-handoff.md`
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - `feature/focus/src/test/kotlin/phonedown/feature/focus/FocusScreenScreenshotTest.kt`
  - `phase-7-focus-feature-plan.md`
  - `v1-implementation-plan.md`
- Untracked paths:
  - `.vscode/`
  - `app/src/main/java/phonedown/app/focus/`
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/state/`
  - Paparazzi snapshot images under `feature/focus/src/test/snapshots/images/`
- Staged files: none
- Treat all of the above as user/previous-agent work. Do not overwrite or revert it unless explicitly asked.
- Safety note: `.vscode/` is untracked and should be reviewed carefully before any future staging; do not use broad staging commands.

## 5. Decisions And Rationale
- Duration-selection race fix:
  - rationale: starting a session using an async DataStore write for the selected duration is unreliable because the service can read the old default before the write completes
  - fix: pass the intended duration through `MainActivity` into `FocusSessionService`, then into `ActiveSessionRuntimeCoordinator.ensureSessionStarted(plannedDurationSeconds)`
- Retry path fix:
  - rationale: a `Retry` CTA that does nothing is worse than no CTA
  - fix: add a real retry action that restarts sensor monitoring via the service
- Distinct call UI:
  - rationale: calls are a product-distinct interruption type and should not be mislabeled as physical pickup
- Custom duration support:
  - rationale: the phase plan and approved product direction require custom duration entry in Phase 7, even if full billing/entitlement behavior remains for a later phase
- Phase ordering truth:
  - rationale: older handoffs drifted toward “Phase 8 Insights,” but the repo plan still says Onboarding comes first

## 6. Known Issues / Blockers
- Physical-device QA is still missing for both Phase 6 runtime behavior and Phase 7 Focus behavior.
- Emulators are still not trustworthy for validating:
  - real face-down sensor progression
  - notification-shade interaction
  - dimming feel
  - feedback feel
  - reboot behavior
- The untracked `.vscode/` directory may be purely local/editor state, but it should not be staged casually.
- `phase-7-focus-feature-plan.md` and `v1-implementation-plan.md` still need a follow-up doc pass to reflect this review/fix iteration cleanly if the next agent continues working in this area.

## 7. Exact Next Steps
1. Re-inspect the current state before more edits:
   - `sed -n '1,220p' AGENTS.md`
   - `git status`
   - `sed -n '1,260p' docs/agent-handoff.md`
2. Run the app on a physical Android device and validate the real Focus ritual end to end:
   - onboarding -> Focus
   - change duration, including custom duration
   - start focus
   - place phone face down and confirm waiting/arming/active progression
   - pick up the phone and confirm paused/penalty behavior
   - test call interruption behavior if feasible
   - test notification interaction, including `End Session`
3. If physical QA surfaces issues, fix those before talking about a commit or the next phase.
4. If physical QA passes, update:
   - `phase-7-focus-feature-plan.md`
   - `v1-implementation-plan.md`
   with the final validation status
5. Only after that, ask the user whether they want to commit the Phase 7 work.
6. When the project is ready to move on, remember that the repo plan says **Phase 8 is Onboarding**, not Insights.

## 8. Suggested Prompt For The Next Agent
```text
Continue work in the Phone Down project. Start by reading `AGENTS.md` and `docs/agent-handoff.md`, then inspect `git status` so you are anchored to the real workspace. Do not overwrite existing uncommitted work, do not commit unless explicitly asked, and do not use broad staging commands.

The current Phase 7 Focus implementation has already been reviewed and patched for:
- custom duration entry
- a real retry path for sensor-unavailable state
- distinct paused-by-call copy
- a start-duration race between UI selection and service startup

Your next job is not a new phase yet. First help validate Phase 6 and Phase 7 on a physical Android device. If QA fails, fix those issues and update the docs. If QA passes, update the phase docs and only then discuss commit or the next phase. Also note that `v1-implementation-plan.md` still defines Phase 8 as Onboarding and Phase 9 as Insights.
```
