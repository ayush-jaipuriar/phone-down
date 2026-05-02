# Phase 6 - Foreground Service And Runtime Integration Plan

This document defines the implementation plan for Phase 6 of Phone Down V1.

Phase 6 is where the previously separate foundations become a real runtime system:

- the Phase 4 session engine
- the Phase 5 sensor engine
- foreground execution
- persistence and recovery
- notifications
- sound and haptic feedback
- first real screen-dimming behavior

This phase is not just “add a service.”
It is the point where Phone Down begins to behave like an always-honest focus companion instead of a collection of isolated modules.

## 1. Phase Goal

Phase 6 should produce a working runtime layer that:

- starts and maintains a foreground service for active focus sessions
- wires the live sensor validity stream into the Phase 4 session engine end to end
- persists session transitions safely enough for recovery without excessive write churn
- keeps sessions reliable when the app backgrounds or the screen turns off
- shows a calm persistent notification with an `End Session` action
- performs the first real screen-dimming behavior during active focus ritual flow
- executes real sound and haptic feedback respecting stored settings
- restores/reclassifies in-progress sessions on app relaunch and device reboot

At the end of this phase, the app should be able to run a real focus session outside the foreground UI and survive ordinary Android lifecycle pressure much more credibly.

## 2. Approved Planning Decisions

These decisions are locked in for this phase:

- [x] Phase 6 includes the full end-to-end runtime wiring between the Phase 5 sensor stream and the Phase 4 session engine.
- [x] The foreground service should start as soon as a session is created and enters waiting state.
- [x] Persistence should use a moderate cadence: always persist major state transitions and completion/breakage events, while throttling routine tick writes.
- [x] Phase 6 includes the first real screen-dimming behavior.
- [x] Phase 6 includes notification plus real sound and haptic execution.
- [x] Phase 6 includes boot/restart recovery wiring in addition to app relaunch/process-death recovery.

## 3. Recommendation Rationale Captured

### Service Start Timing

Recommended and approved behavior:

- start the service when the session enters `WaitingForPhoneDown`

Why:

- the app needs a durable runtime owner before arming/active states begin
- recovery becomes much simpler if a created session already has a service anchor
- this matches the product ritual: once the user taps Start, the system is responsible for honesty

### Persistence Cadence

Recommended and approved behavior:

- persist all major transitions immediately
- throttle routine active-tick persistence
- flush on service stop, completion, breakage, and manual end

Why:

- this gives strong recovery safety without turning every second into a database write
- most user-visible correctness comes from transition durability, not per-second writes

### Manual Device Restart Wiring

Recommended and approved behavior:

- include boot/restart awareness now

Why:

- restart behavior is explicitly important to the product’s honesty model
- postponing it would leave a glaring integrity hole in the first “real runtime” phase

## 4. In Scope

- [ ] Create a real foreground-service runtime in the app/runtime layer.
- [ ] Start service on session creation / waiting state.
- [ ] Wire `:core:sensors` validity stream into `:domain:session` input processing.
- [ ] Keep session runtime alive across backgrounding and screen-off conditions.
- [ ] Add moderate persistence policy for runtime progress.
- [ ] Add persistent foreground notification channel and notification updates.
- [ ] Add `End Session` notification action.
- [ ] Route runtime events back into persisted state and future UI entry points cleanly.
- [ ] Execute sound feedback when enabled.
- [ ] Execute haptic feedback when enabled.
- [ ] Add first real screen-dimming behavior when focus ritual begins.
- [ ] Recover in-progress sessions on app relaunch.
- [ ] Handle device reboot / boot-completed recovery path.
- [ ] Add targeted automated tests where practical.
- [ ] Define manual runtime validation checklist for this phase.
- [ ] Update roadmap and phase docs after implementation.

## 5. Out Of Scope

- [ ] Final polished Focus UI state wiring.
- [ ] Advanced notification actions beyond `End Session`.
- [ ] Backup sync integration.
- [ ] Billing/pro entitlement gating.
- [ ] User-visible notification customization.
- [ ] Complex battery optimization UX.
- [ ] Exported telemetry or analytics around runtime internals.

These should consume the runtime layer later rather than complicating Phase 6 prematurely.

## 6. Architectural Intent

Phase 6 should preserve the boundaries we have already created:

- `:core:sensors` owns posture validity detection
- `:domain:session` owns focus timing and classification rules
- the runtime/service layer coordinates them
- persistence remains behind repository interfaces
- notification and feedback execution remain implementation details, not business logic

The runtime layer should be an orchestrator, not a second rules engine.

## 7. Recommended Module Placement

Primary ownership is likely split between:

- `:app`
- `:core:notifications`
- existing `:core:sensors`
- existing `:domain:session`

Recommended Phase 6 responsibility split:

### `:app`

- foreground service class
- service registration
- boot receiver registration if placed at app edge
- service-level dependency injection entry points
- runtime coordinator glue if that coordinator must access Android lifecycle/service APIs

### `:core:notifications`

- notification channel setup
- foreground notification builder
- completion notification behavior if still needed after service ends
- notification action intent construction helpers

### `:domain:session`

- continues owning session progression rules only
- may receive small runtime-facing orchestration helpers if still pure and useful

### `:core:sensors`

- continues emitting semantic validity results only
- does not learn about session timing or notifications

## 8. Core Runtime Flow

The Phase 6 runtime should implement this end-to-end path:

1. User starts a session.
2. Session enters `WaitingForPhoneDown`.
3. Foreground service starts immediately.
4. Service subscribes to sensor validity stream.
5. Sensor updates are translated into session-engine inputs.
6. Session transitions are persisted with the approved cadence.
7. Notification updates as state changes.
8. Screen dims at the appropriate point in the ritual.
9. Sound/haptic feedback fires when enabled.
10. Service stops only when the session reaches a real terminal state or is explicitly ended.

That end-to-end flow is the heart of this phase.

## 9. Service Lifetime Policy

### Start Conditions

- [ ] Start foreground service as soon as session creation succeeds and runtime enters waiting state.

### Keep-Alive Conditions

- [ ] Keep service alive during `WaitingForPhoneDown`.
- [ ] Keep service alive during `Arming`.
- [ ] Keep service alive during `Active`.
- [ ] Keep service alive during `PausedByPickup`.
- [ ] Keep service alive during `PausedByCall`.
- [ ] Keep service alive during `Broken` if the product/runtime still allows continuing focus collection after breakage.

### Stop Conditions

- [ ] Stop service on `Completed`.
- [ ] Stop service on `Invalidated`.
- [ ] Stop service on `EndedEarly`.
- [ ] Stop service on `Abandoned`.
- [ ] Stop service after explicit recovery classification reaches a terminal non-running outcome.

Recommended policy:

- service lifetime should follow “session still operational” rather than “timer actively counting”

Why:

- waiting, arming, and interrupted states are still part of an in-progress focus attempt

## 10. Sensor To Session Wiring

Phase 6 should define the exact mapping from sensor output into Phase 4 inputs.

Recommended mapping:

- [ ] `FaceDownStable` / stable valid result -> `PhoneBecameValid`
- [ ] `FaceDownStabilizing` -> runtime remains aware but should not prematurely count focus
- [ ] any invalid reason (`FaceUp`, `Vertical`, `Moving`, `PocketLike`, `UnknownOrientation`) -> `PhoneBecameInvalid`
- [ ] service-owned timer/tick loop -> `Tick`
- [ ] telephony callback later in the same phase if included -> `CallStarted` / `CallEnded`

Important rule:

- the runtime should translate semantic validity to domain inputs
- it should not implement focus rules itself

## 11. Tick And Timing Strategy

The service needs an actual ticking mechanism for Phase 4 inputs.

Recommended approach:

- [ ] use a coroutine-driven or handler-driven tick loop owned by the service/runtime coordinator
- [ ] drive session progression with monotonic time
- [ ] keep tick frequency reasonable, likely around 1 second for session updates

Recommended persistence distinction:

- the engine may process ticks frequently
- persistence does not need to mirror every tick write one-for-one

This keeps behavior responsive while controlling write churn.

## 12. Persistence Policy

Approved direction: moderate cadence, transition-safe persistence.

### Persist Immediately

- [ ] session creation
- [ ] waiting -> arming
- [ ] arming -> active
- [ ] active -> paused by pickup
- [ ] active -> paused by call
- [ ] penalty event generation
- [ ] broken classification
- [ ] completion
- [ ] manual end
- [ ] recovery classification
- [ ] service shutdown / destruction cleanup pass

### Persist Throttled

- [ ] routine active-time progress
- [ ] non-terminal elapsed updates while focus is ongoing

Recommended throttle:

- no more than every few seconds, and/or only after meaningful second deltas

Exact cadence can be finalized during implementation, but the principle should remain:

- transition durability is mandatory
- per-second database churn is not

## 13. Foreground Notification

Phase 6 should add a real foreground notification via `:core:notifications`.

### Required V1 Notification Characteristics

- [ ] calm and minimal
- [ ] no playful or noisy wording
- [ ] persistent while runtime is active
- [ ] tap returns to app session surface later

### Content Examples

- [ ] `Waiting for phone down`
- [ ] `Hold still to begin`
- [ ] `Focus active - 18 min left`
- [ ] `Focus paused - return phone down`

### Actions

- [ ] include `End Session`
- [ ] do not include Pause
- [ ] do not include Add Time

Why:

- V1 should not encourage fiddling during focus

## 14. End Session Notification Action

This phase should plan the end-session action as a real runtime control path.

- [ ] notification action routes into the runtime/service layer
- [ ] runtime invokes `EndSessionUseCase`
- [ ] resulting transition is persisted
- [ ] service stops after terminal state and any final feedback/notification handoff completes

The action should feel explicit and controlled, not like an ad hoc kill switch.

## 15. Sound Feedback

Phase 6 should include real sound execution, respecting settings.

Expected events from architecture and V1 plan:

- [ ] phone down detected
- [ ] timer actually starts
- [ ] session completed

Recommended sound policy:

- [ ] use soft, minimal sounds only
- [ ] respect in-app `soundEnabled`
- [ ] respect system audio context as appropriate during implementation

Important product decision already captured:

- completion sound may play softly when sounds are enabled

Phase 6 should implement the real behavior path rather than leaving it as a future hook.

## 16. Haptic Feedback

Phase 6 should include real haptic execution, respecting settings.

Expected events:

- [ ] tiny haptic on valid phone-down detection
- [ ] subtle haptic when timer actually starts
- [ ] warning haptic on pickup / focus pause
- [ ] longer calm haptic on completion
- [ ] low soft haptic on broken session

Recommended policy:

- [ ] respect in-app `hapticsEnabled`
- [ ] keep haptics restrained and calm

## 17. Screen-Dimming Behavior

Phase 6 should include the first real dimming behavior.

Approved intent from earlier decisions:

- the screen should dim immediately enough to support the ritual, while still fitting the “put the phone face down” behavior

Recommended implementation direction:

- [ ] dim after valid face-down detection / arming start, not before the ritual begins
- [ ] use Android runtime-safe screen brightness or window-flag approach where technically appropriate
- [ ] restore normal brightness/flags when the session is no longer actively in the ritual flow

Recommended fallback behavior:

- if full deterministic dimming proves device-fragile, combine explicit dimming with normal Android screen-timeout behavior rather than faking certainty

The plan should call out that this needs real device validation, because dimming behavior is often device- and OEM-sensitive.

## 18. App Relaunch Recovery

Phase 6 should operationalize the Phase 4 recovery logic on normal app relaunch.

- [ ] detect recoverable sessions at app/service startup
- [ ] run `RecoverSessionsUseCase`
- [ ] restart or terminate runtime based on classified session outcomes
- [ ] ensure user does not see ghost-active state for already terminal sessions

Recommended behavior:

- if recovery yields terminal `Broken` or `Abandoned`, do not restart active runtime
- if later evidence suggests a still-running session can be resumed, document and constrain that behavior carefully

## 19. Boot / Restart Recovery Wiring

You explicitly approved including reboot handling now.

Recommended Phase 6 scope:

- [ ] add boot awareness entry point, likely a boot receiver at app edge
- [ ] on reboot, inspect persisted recoverable sessions
- [ ] apply the approved recovery classification flow
- [ ] do not silently resume a session as if uninterrupted after reboot

Recommended policy:

- device restart should feed the persistence/runtime recovery pipeline
- the system should classify honestly, not optimistically

This preserves the product promise around integrity.

## 20. Runtime Coordinator Design

Phase 6 likely benefits from a coordinator abstraction so service code stays thin.

Recommended shape:

- [ ] `ActiveSessionRuntimeCoordinator` or equivalent

Responsibilities:

- subscribe to sensor validity stream
- translate results to `SessionInput`
- own tick loop
- invoke session use cases
- update notification
- trigger feedback
- manage dimming hooks
- decide when service should stop

The coordinator may live in `:app` if it needs direct service/context access, or in a runtime-focused core module if that becomes cleaner. For this phase, prefer the smallest structure that preserves clarity.

## 21. Telephony / Call Path In Phase 6

Because this phase is the first real runtime integration layer, it is the natural place to wire actual call interruption signals if feasible within Android permission and API constraints.

Recommended plan scope:

- [ ] include runtime call interruption wiring if technically feasible within current permissions/API surface
- [ ] map real call state to `CallStarted` / `CallEnded`
- [ ] keep calls as separate non-broken interruption type unless session rules later classify otherwise

If full call-state integration proves riskier than expected, the implementation should document that clearly rather than partially faking support.

## 22. Testing Strategy

Phase 6 needs both targeted automation and manual runtime validation.

### Automated Tests

- [ ] unit tests for runtime coordinator translation logic
- [ ] fake sensor monitor + fake repository/service-adjacent tests
- [ ] notification-builder tests where practical
- [ ] recovery wiring tests
- [ ] boot/restart entry-path tests where practical

### High-Value Automated Scenarios

- [ ] service starts when session begins waiting
- [ ] sensor valid stream advances runtime into arming and active states
- [ ] invalid stream pauses runtime
- [ ] throttled persistence still persists major transitions
- [ ] notification text updates with runtime state
- [ ] `End Session` action reaches the use case correctly
- [ ] recovery path classifies unfinished sessions on startup
- [ ] reboot path does not incorrectly revive an already-broken session

### Manual Runtime Validation

Required scenarios:

- [ ] app backgrounded during waiting state
- [ ] app backgrounded during active focus
- [ ] screen off during active focus
- [ ] service survives normal app backgrounding
- [ ] notification remains calm and accurate
- [ ] `End Session` action works
- [ ] completion feedback fires when enabled
- [ ] sounds stay off when disabled
- [ ] haptics stay off when disabled
- [ ] screen dimming activates and restores correctly
- [ ] process death / app relaunch recovery path behaves honestly
- [ ] device reboot recovery path behaves honestly

## 23. Risks And Mitigations

### Risk: Service Becomes A Second Session Engine

- Mitigation: keep rules in `:domain:session`; the runtime only translates, schedules, and orchestrates.

### Risk: Persistence Is Too Frequent Or Too Sparse

- Mitigation: persist all major transitions immediately and throttle routine progress writes.

### Risk: Screen Dimming Behaves Inconsistently Across Devices

- Mitigation: keep dimming implementation explicit but validate on real devices and document fallbacks.

### Risk: Notification/Feedback Overstimulate The Experience

- Mitigation: keep all messaging and feedback calm, sparse, and settings-respecting.

### Risk: Boot Recovery Produces Dishonest Session States

- Mitigation: classify conservatively and avoid optimistic auto-resume after reboot.

### Risk: Service/OEM Behavior Differs Across Devices

- Mitigation: include manual runtime validation explicitly as part of the phase plan.

## 24. Documentation Updates Required During Implementation

During implementation, update:

- [ ] `v1-implementation-plan.md`
- [ ] this phase plan with implementation completion notes
- [ ] `docs/module-dependency-rules.md` if runtime ownership shifts meaningfully
- [ ] notification/runtime docs if new abstractions become substantial

Record:

- files touched
- service/runtime classes created
- notification classes created
- feedback execution path added
- tests run
- remaining manual validation gaps

## 25. Verification Plan For Implementation

After implementation, run:

- [ ] `git diff --check`
- [ ] targeted runtime/service unit tests
- [ ] targeted notification tests
- [ ] build/assemble tasks proving service code compiles cleanly
- [ ] broader verification if app/runtime integration reaches shared compile paths
- [ ] `./scripts/check.sh` if the implementation touches enough app paths that the broader suite becomes meaningful

And complete:

- [ ] manual runtime validation scenarios

If any category cannot run, document exactly why before calling the phase complete.

## 26. Acceptance Criteria

Phase 6 is complete only when:

- [ ] A real foreground service exists.
- [ ] The service starts on session creation / waiting state.
- [ ] Live sensor validity results drive real session-engine transitions end to end.
- [ ] Persistence cadence is moderated but safe for recovery.
- [ ] Notification channel and persistent notification work.
- [ ] `End Session` notification action works.
- [ ] Real sound and haptic feedback execute and respect settings.
- [ ] First real screen-dimming behavior works acceptably.
- [ ] App relaunch recovery is wired.
- [ ] Boot/restart recovery is wired.
- [ ] Automated verification passes for the implemented runtime layer.
- [ ] Manual runtime validation is completed or clearly documented as incomplete.
- [ ] Documentation is updated with actual implementation and verification status.
- [ ] The user approves this plan before implementation begins.

## 27. Recommended Implementation Order

1. Add runtime coordinator and service shell.
2. Wire sensor results into session-engine inputs.
3. Add notification channel and persistent notification updates.
4. Add moderate persistence policy.
5. Add `End Session` notification action.
6. Add sound/haptic execution paths.
7. Add screen-dimming behavior.
8. Add app relaunch recovery.
9. Add boot/restart recovery wiring.
10. Add targeted automated tests.
11. Complete manual runtime validation.
12. Update docs with exact behavior and remaining gaps.

This order keeps the always-on runtime path coherent while letting us verify one layer at a time.

## 28. Approval Gate

Implementation must not begin until this Phase 6 plan is approved.

Common next steps:

- approve this Phase 6 plan and start implementation
- request changes to the plan before implementation
- narrow or expand service/feedback/recovery scope before implementation
