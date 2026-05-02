# Phase 7 - Focus Feature Plan

This document defines the implementation plan for Phase 7 of Phone Down V1.

Phase 7 is where the core runtime becomes a product experience that feels calm, obvious, and trustworthy.
The Phase 4 domain engine, Phase 5 sensor validity layer, and Phase 6 runtime/service work already give us behavior.
This phase is about turning that behavior into a Focus surface that matches the mockups closely and makes the ritual feel natural.

## 1. Phase Goal

Phase 7 should produce a polished Focus tab that:

- matches the approved dark and light mockups closely
- presents the full focus ritual through one calm primary surface
- reflects live runtime/session state honestly
- supports duration selection without clutter
- shows real completion, interruption, and invalidation outcomes
- uses polished timer-ring and state-transition animation without compromising clarity
- keeps Insights and Settings as placeholder routes for now

At the end of this phase, a user should be able to open the Focus tab, understand what to do, start a session, and follow the session’s progress through clear visual state changes without needing onboarding text to prop the experience up.

## 2. Approved Planning Decisions

These decisions are locked in for this phase:

- [x] Phase 7 covers the Focus tab and focus-session experience only.
- [x] Insights and Settings remain placeholder routes during this phase.
- [x] The implementation should follow the provided mockups as closely as practical.
- [x] The first Phase 7 pass should already include a polished timer ring and animated state transitions.

Recommended-and-approved decisions captured from the clarification round:

- [x] Include presets plus custom duration entry in this phase.
- [x] Use full Focus-screen state transitions for ongoing and terminal session states.
- [x] Use a confirmation surface only when the user would lose meaningful progress.
- [x] `Sensor unavailable` should be a blocked state with retry guidance and no manual fallback mode.
- [x] The Focus home “Today” summary should use real repository-backed data in this phase.

## 3. Recommendation Rationale Captured

### Duration Strategy

Recommended and approved behavior:

- include preset durations and custom duration entry in Phase 7

Why:

- duration choice is part of the main Focus ritual, not a side feature
- leaving custom duration out would make the first real Focus experience feel artificially incomplete
- the implementation can stay honest by enforcing the current free-user custom-duration limit without needing billing flow completion in this phase

### State Presentation Strategy

Recommended and approved behavior:

- use one primary Focus screen that morphs across session states
- use transient overlays only where the user is making a confirmation decision

Why:

- the product should feel like one ritual, not many disconnected screens
- a single stateful surface makes the timer ring, copy, and motion feel coherent
- confirmation is a separate task, so it deserves a sheet/dialog rather than a full screen replacement

### Manual End Strategy

Recommended and approved behavior:

- allow immediate end while still in `waiting_for_phone_down`
- require confirmation once the session has meaningfully started (`arming`, `active`, interrupted, or paused states with recorded progress)

Why:

- before the ritual begins, accidental starts should be cheap to undo
- once progress exists, the app should protect the user from casual mis-taps

### Sensor Unavailable Strategy

Recommended and approved behavior:

- show a blocked Focus state with retry guidance
- do not offer a fake manual fallback

Why:

- Phone Down’s trust depends on the phone-state rule remaining honest
- a manual fallback would weaken the core promise right at the main product surface

### Today Summary Strategy

Recommended and approved behavior:

- back the Focus home summary with real persistence now

Why:

- the summary already exists in the mockup and influences the feel of the home surface
- the required data is already local and should be cheap to query
- deferring this would leave the Focus home looking polished but hollow

## 4. In Scope

- [ ] Build the real Focus tab UI in the app module.
- [ ] Replace the placeholder Focus home with a production-ready layout.
- [ ] Add a real duration selector bottom sheet.
- [ ] Show preset durations and custom duration entry.
- [ ] Persist and display selected/default duration appropriately.
- [ ] Integrate real runtime/session state into the Focus surface.
- [ ] Show all major session states through one animated Focus experience.
- [ ] Add timer ring rendering and state-aware animation.
- [ ] Add progress/feedback copy for waiting, arming, active, interrupted, paused, completed, broken/invalid, and ended-early outcomes.
- [ ] Add confirmation UI for ending in-progress sessions from the Focus screen.
- [ ] Add `sensor unavailable` blocked-state UX.
- [ ] Show real repository-backed “Today” summary stats on the home state.
- [ ] Keep Insights and Settings as placeholder routes, but ensure the bottom navigation and settings affordance feel consistent with the mockups.
- [ ] Add screenshot/UI tests for key Focus states.
- [ ] Update docs after implementation and verification.

## 5. Out Of Scope

- [ ] Insights feature implementation.
- [ ] Settings feature implementation beyond navigation affordances already present.
- [ ] Billing/paywall implementation.
- [ ] Deep custom-duration entitlement flow.
- [ ] Export/history analytics.
- [ ] Manual fallback mode when sensors are unavailable.
- [ ] Advanced celebratory animations, confetti, or marketing-style motion.

These are intentionally deferred so Phase 7 can stay focused on the primary ritual surface.

## 6. Architectural Intent

Phase 7 should preserve the boundaries established earlier:

- `:domain:session` continues owning the session rules
- `:core:sensors` continues owning semantic validity detection
- Phase 6 runtime remains the source of live in-progress state
- persistence stays behind repository interfaces
- Focus UI consumes state and sends user intents; it should not re-implement timing or interruption logic

The Focus screen should feel rich, but it still needs to be a thin client over the engine/runtime behavior we already built.

## 7. Recommended Module Placement

Current app structure is still small, so the cleanest recommendation for this phase is:

- keep the implementation inside `:app`
- introduce a dedicated package structure for Focus UI instead of expanding `navigation` or `runtime`

Recommended package split inside `app/src/main/java/phonedown/app/`:

- `focus/`
- `focus/components/`
- `focus/model/`
- `focus/state/`
- `focus/preview/` if preview helpers become useful

Recommended responsibility split:

### `navigation/`

- route declarations
- top-level tab shell integration
- route arguments if needed

### `focus/`

- Focus screen entry composable
- Focus view-model/state holder
- UI state mapping from repositories/runtime/session snapshots
- user actions such as start session, select duration, retry sensor check, request end-session confirmation

### `focus/components/`

- timer ring
- duration chips/selector trigger
- home summary cards/rows
- state copy block
- session action buttons
- confirmation sheet content

### `runtime/`

- remains the execution owner for active sessions
- may expose read-only flows/state adapters needed by the Focus screen

This keeps the runtime layer operational and the Focus layer experiential.

## 8. Core Product Surface Strategy

Phase 7 should treat Focus as one continuous surface with multiple visual modes.

Recommended macro states:

1. Idle home
2. Waiting for phone down
3. Arming countdown
4. Active focus
5. Interrupted / paused by pickup
6. Paused by call if surfaced distinctly
7. Completion result
8. Broken / invalid result
9. Sensor unavailable blocked state

Recommended UX rule:

- keep layout structure mostly stable across states
- change ring treatment, emphasis, copy, small action affordances, and summary visibility rather than hard-switching to unrelated layouts

Why:

- stable structure makes the app feel calm
- stable structure also makes animation significantly cleaner and easier to reason about

## 9. Screen And State Plan

### 9.1 Idle Home State

- [ ] Show title matching mockup hierarchy.
- [ ] Show settings shortcut if present in mockup.
- [ ] Show selected duration prominently.
- [ ] Show timer ring in idle mode.
- [ ] Show primary `Start Focus` CTA.
- [ ] Show supporting default-duration text.
- [ ] Show real Today summary.
- [ ] Show bottom navigation.

### 9.2 Waiting For Phone Down State

- [ ] Keep same overall screen skeleton.
- [ ] Replace idle supporting copy with `Place phone down to begin.`
- [ ] Shift ring styling into waiting mode.
- [ ] Keep end/escape affordance minimal and honest.
- [ ] Hide or de-emphasize Today summary while a session is in progress.

### 9.3 Arming Countdown State

- [ ] Show `Hold still...`
- [ ] Show 3, 2, 1 countdown clearly inside or tightly associated with the timer ring.
- [ ] Animate state transition from waiting to arming.
- [ ] Prevent clutter during countdown.

### 9.4 Active Focus State

- [ ] Show active countdown prominently.
- [ ] Show active support copy such as `Focusing` / `Keep your phone down`.
- [ ] Reflect live remaining time honestly from runtime/session state.
- [ ] Animate timer ring progress continuously but calmly.
- [ ] Keep destructive actions secondary.

### 9.5 Interrupted / Pickup State

- [ ] Show `Focus paused`.
- [ ] Show return guidance: `Keep your phone down to continue`.
- [ ] If penalty is applied, show `+1:00 penalty` as a transient but readable event.
- [ ] Visually signal pause without making the screen feel alarming.

### 9.6 Call Pause State

- [ ] Surface calls as a distinct interruption state if exposed by runtime.
- [ ] Keep copy concise and non-judgmental.
- [ ] Resume normal state presentation when the runtime exits the call interruption.

### 9.7 Completion State

- [ ] Show clean completion variant: `Clean session completed`.
- [ ] Show interrupted completion variant: `Session completed` with interruption count / quality context.
- [ ] Preserve the sense of completion without introducing celebratory excess.
- [ ] Offer a clear path back to idle/start next session.

### 9.8 Early End / Broken / Invalid States

- [ ] Early end should route through confirmation, then show honest saved-partial outcome if needed.
- [ ] Broken/invalid results should explain the outcome simply.
- [ ] Use `Not enough focus time to count.` for invalidated sessions where appropriate.
- [ ] Avoid making failure states punitive in tone.

### 9.9 Sensor Unavailable State

- [ ] Show a blocked state when required sensors are unavailable.
- [ ] Explain briefly that Phone Down needs the device sensors to run honestly.
- [ ] Provide retry guidance/action.
- [ ] Do not offer a manual start fallback.

## 10. Duration Selector Strategy

Phase 7 should include a real bottom-sheet duration selector.

Recommended contents:

- preset chips/buttons for `10`, `15`, `25`, `45`, and `60` minutes
- one custom-duration affordance
- indication of the current default/selected duration

Recommended custom-duration behavior:

- [ ] Allow custom duration entry within the free-user limit.
- [ ] If the entered duration exceeds the free limit, block confirmation cleanly.
- [ ] Show a calm locked/pro message rather than implementing purchase flow here.
- [ ] Persist a valid confirmed duration back to settings/repository.

Recommended UX shape:

- presets should be fastest
- custom entry should be available but visually secondary
- the sheet should feel precise and utility-first, not gamified

## 11. Timer Ring And Motion Plan

Phase 7 explicitly includes a polished timer ring and state transitions.

Recommended ring responsibilities:

- [ ] idle visualization of selected duration
- [ ] countdown/progress visualization during active sessions
- [ ] subtle state cues for waiting, arming, paused, and completed states
- [ ] smooth transitions between states without re-layout jumps

Recommended motion approach:

- [ ] use restrained Compose animation APIs
- [ ] animate progress, emphasis, opacity, and scale carefully
- [ ] avoid flashy springiness or celebratory over-animation
- [ ] keep text legible and stable during animation

Recommended visual rule:

- motion should reinforce certainty, not excitement

This matters because Phone Down is a ritual product, not a game.

## 12. State Mapping Layer

The Focus UI should not bind directly to low-level runtime internals if it can avoid it.

Recommended approach:

- [ ] create a Focus-specific UI model/state mapping layer
- [ ] map persisted session + live runtime status + settings + summary stats into a single render model
- [ ] separate raw domain states from user-facing presentation states

Suggested presentation-state families:

- `Idle`
- `WaitingForPhoneDown`
- `Arming`
- `Active`
- `PausedByPickup`
- `PausedByCall`
- `CompletedClean`
- `CompletedInterrupted`
- `EndedEarly`
- `Broken`
- `Invalid`
- `SensorUnavailable`

Why:

- the domain model is optimized for correctness
- the UI model should be optimized for rendering clarity and copy decisions

## 13. End Session UX

Recommended end-session behavior:

- [ ] if the session is still effectively pre-start (`waiting_for_phone_down` with no meaningful progress), allow immediate end
- [ ] once the session is arming/active/paused or has meaningful recorded focus, require confirmation
- [ ] use a bottom sheet or dialog for confirmation, not a full screen replacement
- [ ] use honest copy: `Current progress will be saved as partial.`

Recommended design rule:

- ending focus should feel available but not inviting

## 14. Today Summary Integration

Phase 7 should back the home summary with real data.

Recommended summary fields for the idle Focus home:

- [ ] total focus today
- [ ] session count today
- [ ] clean sessions today

Recommended implementation approach:

- [ ] query local persistence through repository-backed flows/use cases
- [ ] keep the aggregation lightweight and local
- [ ] show stable placeholders/loading states rather than flashing values

Recommended visibility rule:

- show the summary mainly in the idle home state
- de-emphasize or hide it during an active ritual so the screen does not become busy

## 15. Navigation And Route Behavior

Phase 7 only owns the Focus experience, but navigation still matters.

Recommended navigation behavior:

- [ ] Focus remains the primary tab
- [ ] Insights and Settings routes remain placeholders
- [ ] tapping notification content should land the user back in Focus with active session state visible
- [ ] onboarding exit should still land here naturally

Recommended route policy:

- the Focus route should be able to render both idle and live-session states without requiring multiple distinct nav destinations

## 16. Sensor Unavailable And Capability Checks

Recommended behavior:

- [ ] detect and surface unavailable sensor capability before a session gets into a confusing runtime loop
- [ ] provide retry action
- [ ] optionally deep-link or guide toward system-level remedies only if the reason is truly actionable

Important rule:

- do not pretend the session can run without the required signal fidelity

## 17. Mockup Fidelity Requirements

This phase should follow the dark and light mockups closely.

Implementation expectations:

- [ ] typography hierarchy should match the mockups closely
- [ ] spacing and density should feel near-identical
- [ ] component proportions should stay stable across themes
- [ ] the timer ring should visually anchor the screen
- [ ] the Focus tab should remain sparse and premium, not dashboard-like
- [ ] avoid introducing extra explanatory text beyond what the mockups imply

Recommended verification:

- capture before/after screenshots in both themes
- compare layout proportions, not just rough component presence

## 18. Testing And Verification Plan

Phase 7 needs both logic-level and UI-level validation.

### Automated

- [ ] Focus state-mapper unit tests
- [ ] duration-selector logic tests
- [ ] summary aggregation tests if new aggregation code is introduced
- [ ] Compose UI tests for key Focus states
- [ ] screenshot/Paparazzi coverage for dark and light themes
- [ ] app compile/build verification
- [ ] broader `./scripts/check.sh` if Phase 7 touches enough shared app paths

### Key UI States To Snapshot/Test

- [ ] idle home
- [ ] duration sheet
- [ ] waiting state
- [ ] arming state
- [ ] active state
- [ ] paused/interrupted state
- [ ] completion state
- [ ] sensor unavailable state

### Manual

- [ ] theme comparison against both mockups
- [ ] start session from Focus UI
- [ ] duration change flow
- [ ] end-session confirmation behavior
- [ ] notification -> Focus re-entry behavior
- [ ] active-session state coherence during runtime changes

## 19. Documentation Updates Required During Implementation

During implementation, update:

- [ ] `phase-7-focus-feature-plan.md`
- [ ] `v1-implementation-plan.md`
- [ ] any runtime/UI docs that become materially outdated

Record:

- files changed
- composables/view-models/state mappers added or modified
- why the change was made
- tests run
- remaining gaps
- next steps

## 20. Risks And Watchouts

### UI / Runtime Drift

Risk:

- the Focus UI could accidentally duplicate runtime logic or drift from domain truth

Mitigation:

- keep a strong mapping layer between runtime/domain state and presentation state

### Animation Noise

Risk:

- polished motion could become busy or reduce legibility

Mitigation:

- animate fewer things more carefully
- favor continuity over spectacle

### Duration Scope Creep

Risk:

- custom-duration support could drag billing/paywall complexity into Phase 7

Mitigation:

- enforce the free-user limit locally
- keep over-limit behavior informational and non-purchasing for now

### Summary Data Complexity

Risk:

- Today summary implementation could expand into premature Insights work

Mitigation:

- keep the summary narrow and only for the Focus home

### Mockup Fidelity Drift

Risk:

- a technically correct UI may still miss the feel of the provided designs

Mitigation:

- verify with screenshots in both themes
- tune spacing, hierarchy, and ring prominence intentionally

## 21. Acceptance Criteria

Phase 7 is complete only when:

- [ ] The Focus tab is no longer a placeholder and matches the mockups closely in both themes.
- [ ] The Focus home state feels minimal, clear, and premium.
- [ ] The duration selector works with presets and custom entry within the current product constraints.
- [ ] Live session states are rendered honestly from runtime/domain state.
- [ ] Waiting, arming, active, paused, completed, and invalid/broken outcomes all have coherent UI states.
- [ ] The timer ring and animated transitions feel polished without harming clarity.
- [ ] Ending a session behaves safely and predictably.
- [ ] Sensor unavailable state blocks misleading use and provides retry guidance.
- [ ] The Today summary uses real data and supports the home screen without turning it into Insights.
- [ ] Automated verification passes for the implemented Focus layer.
- [ ] Manual theme/state validation is completed.
- [ ] Documentation is updated with real implementation and verification status.

## 22. Recommended Implementation Order

1. Create the Focus package structure and top-level Focus screen/state holder.
2. Build the idle Focus home to match the mockups closely.
3. Add real Today summary data integration.
4. Add duration selector bottom sheet with presets first, then custom entry and validation.
5. Build the Focus presentation-state mapping layer.
6. Wire live runtime/session state into the Focus screen.
7. Add waiting, arming, active, and paused states.
8. Add completion, broken/invalid, and sensor unavailable states.
9. Add end-session confirmation behavior.
10. Polish timer ring and animated transitions across all key states.
11. Add UI tests, screenshot tests, and verification.
12. Update docs with the actual implementation result.

## 23. Approval Gate

Implementation must not begin until this Phase 7 plan is approved.

Common next steps:

- approve this Phase 7 plan and start implementation
- request updates to the plan before implementation
