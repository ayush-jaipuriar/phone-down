# Phase 8 - Onboarding Plan

This document defines the implementation plan for Phase 8 of Phone Down V1.

Phase 8 is not about teaching every feature.
It is about giving a new user just enough context to succeed in the core ritual once, then getting out of the way permanently.

## 1. Phase Goal

Phase 8 should produce a polished first-run onboarding flow that:

- appears only when `onboardingCompleted == false`
- explains the physical rule clearly in three cards
- visually matches the app’s mockup language closely
- persists completion end to end so it never reappears after the first successful pass
- routes the user directly into the real Focus home after completion
- stays calm, minimal, and free of setup friction

At the end of this phase, a first-time user should understand the ritual in under a minute and land in Focus ready to start without feeling like they just passed through a setup wizard.

## 2. Approved Planning Decisions

These decisions are locked in for this phase:

- [x] The onboarding remains a separate 3-card first-time-only flow.
- [x] Completion persistence and first-run routing should be fully implemented in this phase.
- [x] Onboarding should visually match the mockups’ style language very closely.
- [x] After completion, the app should route directly to the Focus idle home.

Recommended-and-approved decisions captured from clarification:

- [x] Copy and visual treatment may be refined if it improves clarity and feel.
- [x] Permission education should stay minimal and contextual rather than becoming a permission gate inside onboarding.
- [x] The parked Phase 6/7 physical-device QA should remain a documented risk and should be revisited before moving deeper into later phases.

## 3. Recommendation Rationale Captured

### Copy Refinement

Recommended and approved behavior:

- refine the onboarding copy where needed, while preserving the 3-card structure and the already-approved meaning

Why:

- onboarding copy has to carry real instructional weight without becoming verbose
- tiny wording improvements can make the physical rule feel calmer and more obvious

### Permission Strategy

Recommended and approved behavior:

- do not turn onboarding into a permission-request ceremony
- keep any permission explanation lightweight and optional
- let actual OS prompts happen contextually when the user starts focus or hits a real runtime need

Why:

- the product should feel low-friction and confidence-building
- asking for permissions before the user understands the value creates avoidable drop-off

### QA Reminder Strategy

Recommended and approved behavior:

- document the parked physical-device QA as an open risk
- do not block onboarding implementation on it
- explicitly revisit that QA before confidently advancing beyond the current user-facing runtime stack

Why:

- we should keep momentum
- we should also avoid accidentally forgetting that the runtime and Focus interaction layer still need real-device proof

## 4. In Scope

- [x] Replace the onboarding placeholder UI with a real 3-card experience.
- [x] Persist onboarding completion using the existing settings repository.
- [x] Ensure app launch routing respects `onboardingCompleted`.
- [x] Route users directly to Focus after onboarding completion.
- [x] Keep onboarding out of the way for returning users.
- [x] Refine onboarding copy for clarity and tone.
- [x] Keep permission messaging optional and lightweight.
- [x] Add screenshot/UI coverage for the onboarding states.
- [x] Update planning/progress docs after implementation and verification.

## 5. Out Of Scope

- [ ] Multi-step setup questionnaire.
- [ ] Early billing/paywall exposure.
- [ ] Deep permission walkthroughs.
- [ ] Account creation/login during onboarding.
- [ ] Advanced personalization.
- [ ] Feature-tour overlays after onboarding completion.

These are intentionally deferred because the first-run experience should stay narrow and calm.

## 6. Architectural Intent

Phase 8 should preserve the current app boundaries:

- onboarding UI remains in `:feature:onboarding`
- first-run routing continues to be owned by the app/navigation layer
- completion persistence goes through `SettingsRepository`
- onboarding should not grow its own domain logic layer

This phase should feel like glue plus polish, not a new business subsystem.

## 7. Recommended Module Placement

Recommended ownership split:

### `:feature:onboarding`

- onboarding composables
- pager/card content
- local animation/presentation logic
- onboarding callbacks such as `onContinue`, `onSkip` if used, and `onFinish`

### `:app`

- initial-route decision using persisted settings
- navigation transition from onboarding to Focus
- any app-level coordination needed for first-run completion persistence

### `:core:datastore` / `:core:model`

- existing `onboardingCompleted` setting and persistence interface remain the source of truth

## 8. Product Shape

The onboarding should remain exactly three cards:

1. Start a focus session
2. Place your phone face down
3. Pickups pause your session and affect Focus Quality

Recommended refinement:

- keep the ideas the same
- tighten the phrasing to feel calm, premium, and instructional
- avoid sounding punitive or overexplained

## 9. Screen Strategy

Phase 8 should feel like a guided introduction, not a wizard.

Recommended layout approach:

- one screen with a card/pager progression
- stable layout skeleton across all three cards
- subtle motion between cards
- one clear primary action per step
- visible progress indicator that stays quiet and minimal

Recommended visual rule:

- the first viewport should feel unmistakably like Phone Down, not a generic mobile onboarding template

## 10. Card-by-Card Intent

### 10.1 Card 1 - Start A Focus Session

- [x] Explain the user starts a session intentionally.
- [x] Keep the message short and concrete.
- [x] Establish the tone of the app as calm and focused.

Recommended copy direction:

- introduce the ritual, not the whole product

### 10.2 Card 2 - Place Your Phone Face Down

- [x] Explain the physical rule plainly.
- [x] Make it obvious that focus begins only while the phone is face down and stable.
- [x] Avoid technical sensor language.

Recommended copy direction:

- concrete physical instruction beats abstraction here

### 10.3 Card 3 - Pickups Pause Your Session

- [x] Explain that pickups pause the session.
- [x] Introduce Focus Quality gently.
- [x] Avoid making the feature sound punitive or gamified.

Recommended copy direction:

- “honest tracking” tone rather than “you are being judged” tone

## 11. Permission Messaging Strategy

Recommended behavior:

- keep permission messaging lightweight and optional
- mention that the app may ask for notifications when a session starts
- do not force a permission explanation step unless the design still feels clear

Recommended implementation shapes:

- a short supportive line on the final card, or
- a subtle inline note near the action, or
- no explicit mention if the flow already feels cleaner without it

Success condition:

- a first-time user should not feel blocked by permissions during onboarding

## 12. Routing And Persistence Plan

This phase must fully wire first-run behavior.

Required behavior:

- [x] On fresh install, `onboardingCompleted == false` routes to onboarding.
- [x] Finishing onboarding sets `onboardingCompleted = true`.
- [x] After finish, navigate directly to Focus.
- [x] On subsequent launches, skip onboarding entirely and route to Focus.

Recommended implementation detail:

- the persistence write should happen before or atomically with the route transition so the app cannot accidentally re-show onboarding on next launch

## 13. Interaction Model

Recommended interaction shape:

- [x] one primary CTA per card
- [x] optional back affordance only if it improves usability without adding clutter
- [x] no freeform inputs
- [x] no setup toggles
- [x] no branching logic

Recommended CTA language:

- simple progression language such as `Continue`
- final CTA should clearly imply landing in the app, such as `Start` or `Go to Focus`, if it feels better than another `Continue`

## 14. Mockup Fidelity Requirements

Phase 8 should match the app’s visual language very closely.

Implementation expectations:

- [x] typography hierarchy should feel aligned with Focus
- [x] spacing should feel premium and restrained
- [x] card treatment should align with the existing design system
- [x] dark and light modes should both feel intentional
- [x] onboarding should not look like a default template bolted onto the product

Recommended design note:

- even though onboarding is not shown often, it still shapes first trust in the product

## 15. Testing And Verification Plan

### Automated

- [x] onboarding ViewModel unit tests (persistence + callback behavior)
- [ ] Compose UI tests for progression through the 3 cards (deferred — requires instrumented test setup; Paparazzi screenshot tests provide visual coverage in both themes)
- [x] screenshot/Paparazzi coverage for at least one representative state in both themes
- [x] app compile/build verification
- [x] initial-route decision tests (onboardingCompleted → Onboarding/Focus routing)
- [ ] broader `./scripts/check.sh` if the changes touch shared app/navigation paths meaningfully

### Manual

- [ ] fresh-install first-run routing
- [ ] card progression flow
- [ ] onboarding completion persistence
- [ ] relaunch skip behavior
- [ ] dark/light visual review

## 16. Documentation Updates Required During Implementation

During implementation, update:

- [x] `phase-8-onboarding-plan.md`
- [x] `v1-implementation-plan.md`
- [x] `docs/agent-handoff.md` if continuity materially changes

Record:

- files changed
- screens/components added or replaced
- persistence/routing changes
- tests run
- remaining gaps
- next steps

## 17. Risks And Watchouts

### Overexplaining

Risk:

- onboarding becomes too wordy and dilutes the product’s calm feel

Mitigation:

- prefer fewer words and more direct instruction

### Setup Friction

Risk:

- permissions or optional notes turn onboarding into a hurdle

Mitigation:

- keep permission messaging contextual and lightweight

### Persistence Mismatch

Risk:

- onboarding completion writes but routing still replays onboarding incorrectly

Mitigation:

- verify the full first-run -> relaunch loop, not just the write itself

### Mockup Drift

Risk:

- onboarding feels visually disconnected from the now-polished Focus screen

Mitigation:

- use screenshot review in both themes and tune spacing/typography intentionally

### Parked QA Drift

Risk:

- the team forgets that Phase 6/7 physical-device validation is still pending

Mitigation:

- keep it documented as an open risk in the progress log and handoff

## 18. Acceptance Criteria

Phase 8 is complete only when:

- [x] Fresh installs see onboarding once.
- [x] Returning users bypass onboarding completely.
- [x] Onboarding completion persists reliably.
- [x] The user lands directly in Focus after onboarding completion.
- [x] The three cards clearly explain the ritual without extra setup friction.
- [x] Permission messaging, if present, is minimal and non-blocking.
- [x] The onboarding visuals match the app’s style language closely in both themes.
- [x] Automated verification passes for the implemented onboarding layer.
- [ ] Manual first-run persistence/routing validation is completed.
- [x] Documentation is updated with the real implementation and verification state.
- [x] The parked Phase 6/7 physical QA remains documented until revisited.

## 19. Recommended Implementation Order

1. Replace the onboarding placeholder screen with the real 3-card structure.
2. Refine the copy and CTA wording.
3. Wire onboarding completion persistence through `SettingsRepository`.
4. Connect finish routing back into the existing app navigation flow.
5. Verify relaunch skip behavior.
6. Add screenshot/UI tests.
7. Update docs with actual implementation and verification results.

## 20. Approval Gate

Implementation must not begin until this Phase 8 plan is approved.

Common next steps:

- approve this Phase 8 plan and start implementation
- request updates to the plan before implementation
