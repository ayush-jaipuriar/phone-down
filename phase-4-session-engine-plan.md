# Phase 4 - Session Domain Engine Plan

This document defines the implementation plan for Phase 4 of Phone Down V1.

Phase 4 builds the first real session engine in `:domain:session`.
Its job is to make the timer and interruption rules correct, deterministic, and thoroughly testable before any Android sensor wiring, foreground service lifecycle handling, or feature UI integration is introduced.

The guiding principle for this phase is simple:

- persistence stores durable facts
- the domain engine owns session rules
- platform modules provide inputs later

That separation matters because the product promise depends on behavioral correctness more than on UI polish. If the timer rules drift or interruption handling is inconsistent, everything above it becomes untrustworthy.

## 1. Phase Goal

Phase 4 should produce a pure Kotlin session engine that:

- starts and advances sessions using the persisted Phase 3 models
- applies arming, active, pause, interruption, penalty, broken, and completion rules from `architecture.md`
- classifies early ends and recovery outcomes consistently
- coordinates repository writes where domain progress needs to be persisted
- exposes a deterministic API that can be fully exercised with unit tests

At the end of this phase, we should have a trustworthy session state machine that is independent of sensors, Compose, services, and Android lifecycle wiring.

## 2. Approved Planning Decisions

These decisions are locked in for this phase based on the current recommendation path:

- [x] Phase 4 stays pure Kotlin and domain-first.
- [x] No Android sensor APIs, phone-state APIs, notifications, or foreground service work in this phase.
- [x] Process-death recovery logic is included as domain classification and repository-backed recovery coordination.
- [x] Full interruption policy is implemented now in the engine.
- [x] Call pauses remain a distinct interruption category in the engine, even though call detection wiring arrives later.
- [x] Time is injected through abstractions so tests can simulate elapsed time deterministically.
- [x] Repository orchestration is included where the engine needs to persist state transitions and penalty events.

## 3. In Scope

- [x] Create the domain session engine in `:domain:session`.
- [x] Define domain-facing engine input models and command/event types.
- [x] Define injected time abstractions required for deterministic timing.
- [x] Implement start-session flow.
- [x] Implement arming-window behavior.
- [x] Implement active-timing accumulation.
- [x] Implement invalid-state grace-period behavior.
- [x] Implement interruption classification into minor, penalty, long, call, manual-end, recovery outcomes.
- [x] Implement penalty application rules.
- [x] Implement broken-session thresholds.
- [x] Implement clean-session disqualification rules.
- [x] Implement completion and early-end classification rules.
- [x] Implement process-death recovery classification rules using persisted state plus current monotonic time.
- [x] Persist session changes through `SessionRepository`.
- [x] Persist penalty events through `SessionRepository`.
- [x] Add exhaustive unit tests for the state machine and classification boundaries.
- [x] Update V1 planning/progress documentation after implementation.

## 4. Out Of Scope

- [ ] Real sensor ingestion from `:core:sensors`.
- [ ] Telephony/call-state integration.
- [ ] Foreground service orchestration.
- [ ] Notifications, sounds, or haptics execution.
- [ ] Compose UI wiring to live engine state.
- [ ] ViewModel integration.
- [ ] Alarm scheduling or screen-dimming execution.
- [ ] Google Drive backup integration.
- [ ] Analytics aggregation in `:domain:insights`.

Those pieces should consume this engine later, not contaminate it now.

## 5. Why This Phase Exists

Phone Down is not a generic timer app.
Its defining behavior is that focus only counts when the phone is genuinely put down and remains valid.

That means the domain engine needs to answer hard questions consistently:

- when does a session actually begin
- when does valid time count
- when is a pickup “minor” versus “penalty”
- when does a session become broken
- what result should an early end produce
- how should a previously active session be classified after process death

If these answers live partly in UI, partly in sensors, and partly in service code, the product becomes fragile.
This phase prevents that by centralizing the behavior in one deterministic, testable engine.

## 6. Module Placement

Primary module:

- `:domain:session`

Allowed dependencies:

- `:core:model`
- `:core:common`

Optional dependency:

- `:core:model` repository contracts already exist and should be consumed from there

Disallowed dependencies:

- `:app`
- `:feature:*`
- `:core:database`
- `:core:datastore`
- `:core:sensors`
- `:core:notifications`
- Compose
- Android framework APIs

This keeps the engine reusable by later service, sensor, and UI layers.

## 7. Architecture Shape

Recommended internal structure for `:domain:session`:

- [ ] `SessionEngine`
- [ ] `SessionRecoveryClassifier`
- [ ] `SessionRuleConfig`
- [ ] `SessionInput` or equivalent command/event models
- [ ] `SessionTickResult` / `SessionTransitionResult` models
- [ ] use cases wrapping engine operations where useful

Recommended high-level split:

1. `SessionEngine`
   Handles session lifecycle progression from start through completion or breakage.

2. `SessionRecoveryClassifier`
   Handles persisted-session recovery decisions on app relaunch or service restart.

3. `SessionRepositoryCoordinator` behavior
   May live inside use cases or engine wrapper methods.
   Its job is to write updated sessions and penalty events through `SessionRepository`.

The engine should be the place where rules live.
Use cases should orchestrate I/O around those rules.

## 8. Recommended Public Surface

The exact API can change during implementation, but the public shape should support the following operations cleanly:

- [ ] create/start a session from a selected duration
- [ ] enter waiting state
- [ ] consume “phone became valid” signal
- [ ] consume arming progress/tick
- [ ] consume “phone became invalid” signal
- [ ] consume “phone became valid again” signal
- [ ] consume active timer ticks
- [ ] consume call started
- [ ] consume call ended
- [ ] end session manually
- [ ] recover unfinished sessions from persistence

Recommended pattern:

- the engine consumes domain inputs
- the engine returns updated `FocusSession` plus any generated `PenaltyEvent`s plus metadata about what happened
- repository-facing use cases persist the returned changes

This pattern keeps logic pure while still making persistence integration straightforward.

## 9. Time Abstractions

Phase 4 should introduce explicit time abstractions in domain or common code.

Recommended abstractions:

- [ ] wall-clock provider for `currentTimeMillis`
- [ ] monotonic clock provider for `elapsedRealtimeMillis`

Recommended names:

- `WallClock`
- `MonotonicClock`

or a small combined abstraction if the codebase stays simpler that way.

Why inject time:

- deterministic test control
- no hidden calls to system time inside rules
- clean recovery reasoning using persisted monotonic values
- easier later integration with service and sensors

The engine should use:

- monotonic time for active timing and interruption durations
- wall-clock time for record timestamps and history metadata

## 10. Domain Inputs

Because sensors are not wired yet, the engine should operate on abstract domain signals rather than platform callbacks.

Recommended input vocabulary:

- [ ] `StartRequested`
- [ ] `PhoneBecameValid`
- [ ] `PhoneBecameInvalid`
- [ ] `Tick`
- [ ] `CallStarted`
- [ ] `CallEnded`
- [ ] `ManualEndRequested`
- [ ] `RecoverRequested`

The actual naming is flexible, but the concept matters:
the engine should not know where a signal came from, only what it means.

That lets later phases map:

- sensor validation -> `PhoneBecameValid` / `PhoneBecameInvalid`
- telephony callback -> `CallStarted` / `CallEnded`
- foreground service timer -> `Tick`

## 11. Session Lifecycle To Implement

### Start Flow

- [ ] Starting a session creates a `FocusSession` in `Created` or directly `WaitingForPhoneDown`, depending on the final implementation simplification.
- [ ] Planned duration and required duration start equal.
- [ ] Valid focus seconds start at `0`.
- [ ] Actual elapsed seconds start at `0`.
- [ ] Penalty seconds start at `0`.
- [ ] Clean starts as `true`.
- [ ] Broken starts as `false`.
- [ ] Call interrupted starts as `false`.
- [ ] Start timestamps are initialized consistently.

Recommendation:

- create the persisted record immediately
- transition into `WaitingForPhoneDown`

Why:
it gives later service/UI layers something durable to resume and matches the architecture direction.

### Waiting State

- [ ] No valid focus time accumulates.
- [ ] No arming time counts toward session progress.
- [ ] Session remains ready for valid face-down input.

### Arming State

- [ ] Valid face-down condition starts a 3-second arming countdown.
- [ ] If validity is lost before arming completes, arming resets.
- [ ] Arming should not add valid focus time yet.
- [ ] Once arming completes, session enters `Active`.

### Active State

- [ ] Valid focus time accumulates only while active and valid.
- [ ] Actual elapsed time accumulates while the session exists after start, including pauses, according to the chosen implementation policy.
- [ ] Completion occurs once valid focus reaches or exceeds required duration.

Recommendation for elapsed tracking:

- keep `actualElapsedSeconds` aligned with real elapsed session lifetime from session start
- keep `validFocusSeconds` as the true earned focus metric

This preserves meaningful analytics later.

## 12. Interruption Rules To Implement

### Invalid State Grace Window

- [ ] When validity is lost during `Active`, session pauses immediately.
- [ ] Session enters `PausedByPickup`.
- [ ] A 5-second grace window starts.
- [ ] If the phone becomes valid again within 5 seconds, record a minor interruption.
- [ ] Minor interruption does not add penalty seconds.
- [ ] Minor interruption still removes clean status.

### Penalty Threshold

- [ ] If invalid state exceeds 5 continuous seconds, add 60 penalty seconds.
- [ ] Increase `penaltySeconds` by 60.
- [ ] Increase `penaltyInterruptionCount` by 1.
- [ ] Increase `interruptionCount`.
- [ ] Persist a `PenaltyPickup` event.
- [ ] Increase `requiredDurationSeconds` by 60.
- [ ] Clean becomes `false`.

### Long Invalid / Broken Threshold

- [ ] If invalid state continues beyond 60 continuous seconds, mark the session `Broken`.
- [ ] Persist a `LongPickup` event if needed by the chosen event model.
- [ ] Set `broken = true`.
- [ ] Result remains terminal only when the engine finalizes that outcome.

### Repeated Penalty Threshold

- [ ] If the user accumulates 3 penalty interruptions, mark the session `Broken`.
- [ ] Broken sessions must never be marked clean.

### Broken Session Continuation

Architecture indicates broken sessions may still continue accumulating valid focus time for honest analytics, but they must never recover into clean completion.

Recommended behavior for Phase 4:

- [ ] broken state is terminal in quality/result terms
- [ ] later valid focus may still accumulate if the engine intentionally supports post-break continuation
- [ ] final result remains `Broken` even if required duration is later reached

If implementation complexity spikes, prefer explicit terminal broken handling and document any deferred continuation nuance before code lands.

## 13. Call Pause Rules

Phase 4 should model calls abstractly without telephony integration.

- [ ] `CallStarted` pauses an active session into `PausedByCall`.
- [ ] No direct 60-second or 3-strike penalty is applied just because a call is ongoing.
- [ ] `callInterrupted` becomes `true`.
- [ ] Clean becomes `false`.
- [ ] A `CallPause` event is recorded when appropriate.
- [ ] `CallEnded` allows the session to return to waiting-for-valid or arming flow depending on the chosen resume rule.

Recommended resume behavior:

- after call ends, require the phone to re-enter valid face-down flow
- do not jump straight back to active

Why:
it keeps the ritual honest and avoids resuming active focus while the phone may still be in-hand.

## 14. Manual End Rules

- [ ] Manual end is available from waiting, arming, active, and paused states.
- [ ] Manual end records a `ManualEnd` penalty/interruption event when appropriate.
- [ ] Manual end always removes clean status unless the product rules explicitly allow clean completion after manual end, which they currently do not.
- [ ] Final classification follows the early-end thresholds below.

## 15. Completion And Early-End Classification

### Full Completion

- [ ] If `validFocusSeconds >= requiredDurationSeconds` and the session is not broken:
  completion result is `CleanCompleted` when no minor interruption, penalty interruption, call pause, manual end, or other disqualifier occurred.
- [ ] Otherwise result is `CompletedWithInterruption`.

### Early End Thresholds

Implement the thresholds from `architecture.md`:

- [ ] `0-20%` valid focus -> `Invalidated`
- [ ] `21-79%` valid focus -> `Partial`
- [ ] `80-99%` valid focus -> `StrongPartial`
- [ ] `100%+` -> completion result path

Recommended implementation detail:

- compute percentage against `plannedDurationSeconds`, not `requiredDurationSeconds`, unless architecture language or later user clarification says otherwise

Why:
these thresholds are about “how much of the intended session was meaningfully achieved,” while `requiredDurationSeconds` grows when penalties happen.

This assumption should be called out again during implementation notes.

## 16. Recovery Rules

Phase 4 should implement the missing decision layer on top of Phase 3 persistence.

Inputs:

- persisted `FocusSession`
- persisted penalty events if needed
- current monotonic time
- current wall-clock time

Recommended recoverable states from Phase 3:

- `Created`
- `WaitingForPhoneDown`
- `Arming`
- `Active`
- `PausedByPickup`
- `PausedByCall`

Recommended Phase 4 recovery behavior:

- [ ] classify sessions that never meaningfully started as `Abandoned`
- [ ] classify interrupted active sessions according to architecture recovery rules
- [ ] classify force-close or unrecoverable active sessions as `Broken` or `Abandoned` per product rules
- [ ] persist the final recovery classification back to `SessionRepository`

Specific recommendation:

- `Created`, `WaitingForPhoneDown`, and incomplete `Arming` sessions recover as `Abandoned`
- `Active` session after process death recovers as `Broken` unless future service-state evidence allows something smarter
- `PausedByPickup` and `PausedByCall` recover as `Abandoned` unless there is a clear architecture rule requiring `Broken`

This is intentionally conservative and honest.
If future service integration gives us stronger evidence, later phases can refine the classification with more context.

## 17. Repository Coordination

Phase 4 should not dump persistence responsibility onto UI or service layers.

Recommended repository-facing use cases:

- [ ] `StartSessionUseCase`
- [ ] `ProcessSessionInputUseCase` or equivalent
- [ ] `EndSessionUseCase`
- [ ] `RecoverSessionsUseCase`

Repository responsibilities in this phase:

- [ ] persist newly created/started sessions
- [ ] persist every session state transition that changes durable state
- [ ] persist penalty events when generated
- [ ] persist recovery classification outcomes

Do not add Android-specific repository types here.
Use the Phase 3 repository contracts as the persistence boundary.

## 18. Data Model Additions Or Helpers

Phase 4 may require small pure-model additions if implementation becomes awkward.

Allowed additions if needed:

- [ ] a domain-only transition result model
- [ ] a session progress snapshot model
- [ ] a rule config object
- [ ] lightweight engine event metadata

Avoid:

- [ ] duplicating `FocusSession` with a second session model unless absolutely necessary
- [ ] embedding Android or Room concerns into domain models

## 19. Testing Strategy

This phase should be unit-test heavy.

### Core Testing Goals

- [ ] prove every state transition
- [ ] prove every threshold boundary
- [ ] prove persistence orchestration behavior with fake repositories
- [ ] prove recovery outcomes from persisted states
- [ ] prove time handling does not depend on real clocks

### Test Types

- [ ] pure engine unit tests
- [ ] use-case tests with fake repositories
- [ ] recovery-classifier tests
- [ ] boundary tests for percentages and interruption thresholds

### Minimum Test Matrix

- [ ] starting a session enters waiting state correctly
- [ ] valid signal begins arming
- [ ] invalid signal during arming resets arming
- [ ] arming completes after exactly 3 seconds
- [ ] active ticks increase valid focus
- [ ] active ticks do not increase valid focus when invalid
- [ ] actual elapsed tracks monotonic time correctly
- [ ] invalid state under 5 seconds records minor interruption
- [ ] invalid state exactly at 5-second threshold behaves as intended
- [ ] invalid state over 5 seconds adds one-minute penalty
- [ ] required duration increases after penalty
- [ ] third penalty interruption marks broken
- [ ] invalid state over 60 seconds marks broken
- [ ] call pause marks clean false without penalty
- [ ] call end requires valid re-entry flow
- [ ] manual end at 0, 20, 21, 79, 80, 99, and 100 percent yields correct result
- [ ] completed clean session yields `CleanCompleted`
- [ ] completed interrupted session yields `CompletedWithInterruption`
- [ ] recovery of waiting/arming session yields `Abandoned`
- [ ] recovery of previously active unrecoverable session yields conservative terminal result
- [ ] repository writes happen exactly once for each expected durable transition

### Test Tooling

- [ ] JUnit tests in `:domain:session`
- [ ] coroutine test library if suspend use cases are added
- [ ] fake repository implementations in test source
- [ ] fake clock implementations in test source

## 20. Documentation Updates Required During Implementation

During implementation, update:

- [ ] `v1-implementation-plan.md`
- [ ] this phase plan with implementation completion notes
- [ ] `docs/module-dependency-rules.md` if dependency boundaries change
- [ ] any domain-specific documentation added during implementation

Record:

- files touched
- major engine/use case classes created
- tests added
- verification commands run
- residual risks or deferred nuances

## 21. Verification Plan For Implementation

After implementation, run:

- [x] `git diff --check`
- [x] `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :domain:session:test`
- [x] `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :core:model:test :core:common:test`
- [x] targeted repository/build checks if `:domain:session` adds new dependencies
- [ ] `ANDROID_HOME="$HOME/Library/Android/sdk" ./scripts/check.sh` if Phase 4 integration reaches app-wide compile paths cleanly

If any test category cannot run, document exactly why before calling the phase complete.

## 22. Risks And Mitigations

### Risk: State Machine Becomes Too Implicit

- Mitigation: make transitions explicit and test each one independently.

### Risk: Engine Leaks Platform Assumptions

- Mitigation: consume abstract validity/call/tick signals only.

### Risk: Recovery Rules Become Guessy

- Mitigation: choose conservative classifications, document them, and defer smarter heuristics until later lifecycle/service phases.

### Risk: Timing Bugs Hide In Real-Time Code

- Mitigation: inject time sources and use deterministic fake clocks in tests.

### Risk: Repositories Become Passive Data Dumps

- Mitigation: keep orchestration use cases in `:domain:session` so durable writes happen at the same layer where rules are enforced.

### Risk: Threshold Interpretation Drifts

- Mitigation: codify the 3-second, 5-second, 60-second, and percent boundaries with exact unit tests.

## 23. Acceptance Criteria

Phase 4 is complete only when:

- [x] `:domain:session` contains a real session engine, not a placeholder.
- [x] The engine is pure Kotlin with no Android framework dependency.
- [x] The engine implements start, arming, active, interruption, penalty, call-pause, manual-end, completion, and recovery flows.
- [x] Time is injected and deterministic in tests.
- [x] Repository orchestration exists for durable session and penalty-event writes.
- [x] Recovery classification uses Phase 3 persisted sessions and writes terminal outcomes back when required.
- [x] Boundary tests cover all important thresholds.
- [x] Unit tests for the engine and use cases pass.
- [x] Documentation is updated with actual implementation and verification status.
- [x] The user has reviewed the plan and approved implementation before coding starts.

## 24. Recommended Implementation Order

1. Add time abstractions and any small domain helper models.
2. Build the core engine transition logic without persistence.
3. Add classification logic for completion and early end.
4. Add interruption and penalty-event generation.
5. Add recovery classifier behavior.
6. Wrap engine operations in repository-coordinating use cases.
7. Add exhaustive unit tests.
8. Run targeted verification.
9. Update docs and progress logs.

This order keeps the critical thinking in the smallest possible loop before integration concerns are layered in.

## 25. Approval Gate

Implementation must not begin until this Phase 4 plan is approved.

Common next steps:

- approve this Phase 4 plan and start implementation
- request changes to the plan before implementation
- narrow or expand recovery behavior before implementation

---

### Implementation Completion Note
**Phase 4 implementation was completed on May 1, 2026.**
The `:domain:session` module now contains a pure Kotlin session engine, repository-coordinating use cases, and a recovery classifier built on top of the Phase 3 persistence contracts. Shared clock and ID abstractions were added in `:core:common`.

Verification completed for this phase:

- `git diff --check`
- `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :domain:session:test`
- `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew --no-configuration-cache :core:common:test :core:model:test :domain:session:test`

Deferred verification:

- `./scripts/check.sh` was not run in this pass because Phase 4 has not yet been wired into the app/UI/service layers, so the highest-signal verification was the targeted domain and adjacent core test suite.
