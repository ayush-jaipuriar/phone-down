# Phase 5 - Sensor Engine Plan

This document defines the implementation plan for Phase 5 of Phone Down V1.

Phase 5 builds the real face-down detection engine in `:core:sensors` and wires it to actual Android motion/orientation sensors.
Its purpose is to decide, as reliably and conservatively as possible, whether the phone is in a valid focus posture for the Phase 4 session engine.

This phase is where the product starts to earn or lose trust.
If the detector feels noisy, inconsistent, or easy to fool, the app loses its identity.
If it feels calm, stable, and predictable, the rest of the focus experience has something solid to stand on.

## 1. Phase Goal

Phase 5 should produce an Android-backed sensor engine that:

- reads real device motion/orientation signals
- evaluates whether the device is valid for focus timing
- rejects face-up, vertical, pocket-like, in-hand, walking-like, and unstable cases conservatively
- tolerates tiny table vibrations and brief harmless drift
- exposes a richer domain-friendly validity result, not just a boolean
- supports debug-only diagnostics for internal validation
- is testable with simulated readings plus manual real-device validation

At the end of this phase, the app should have a trustworthy validity signal source that later phases can connect to the session engine, foreground service, and focus UI.

## 2. Approved Planning Decisions

These decisions are locked in for this phase:

- [x] Actual Android sensor integration is included in this phase.
- [x] Detection should be conservative: prefer a few extra pauses over false valid-focus credit.
- [x] A debug-only diagnostics surface is planned now.
- [x] Manual real-device validation is a required exit criterion, not just a recommendation.
- [x] Pocket rejection and moving-vehicle / walking rejection are best-effort heuristics with conservative fallback to invalid.
- [x] The sensor engine should expose rich output: validity, reason, stability state, and optional debug confidence/metrics.

## 3. In Scope

- [ ] Replace the `:core:sensors` placeholder with real Android sensor-engine code.
- [ ] Define sensor-facing abstractions and validity result models.
- [ ] Integrate Android `SensorManager`.
- [ ] Use accelerometer gravity vector and/or rotation vector as primary orientation inputs.
- [ ] Optionally use gyroscope only if implementation/testing shows it is needed.
- [ ] Detect face-down horizontal placement.
- [ ] Reject face-up and vertical orientation.
- [ ] Evaluate movement stability over time, not just a single instant.
- [ ] Reject in-hand / continuously moving states.
- [ ] Add pocket-rejection heuristics.
- [ ] Add best-effort walking / moving-vehicle rejection heuristics.
- [ ] Expose a stream/callback surface consumable by later app layers.
- [ ] Expose a debug-only diagnostics model for internal testing.
- [ ] Add local unit tests for math/classification logic.
- [ ] Add integration-ish tests where practical for Android-facing glue.
- [ ] Define manual device validation checklist and exit criteria.
- [ ] Update project docs and progress logs after implementation.

## 4. Out Of Scope

- [ ] Full UI wiring to Focus screen.
- [ ] Foreground-service orchestration.
- [ ] Notification behavior.
- [ ] Screen-dimming behavior.
- [ ] Production-visible debug tools.
- [ ] User-exposed sensitivity settings.
- [ ] Manual calibration flow.
- [ ] iOS support.
- [ ] Analytics derived from sensor confidence.

Those should consume this engine later rather than shaping it prematurely.

## 5. Why This Phase Exists

The session engine already knows what to do with “valid” and “invalid” focus posture.
What it does not yet have is a trustworthy source of truth for whether the phone is truly face down and stable.

This phase exists to solve the core physical ritual of the app:

- is the phone actually face down
- is it flat enough
- is it stable enough
- is it likely on a surface rather than in a pocket or hand
- is the movement harmless drift or a real pickup

The detector must feel strict but fair.
That means the implementation should lean conservative when evidence is ambiguous.
It is better for V1 to pause when uncertain than to silently grant false focus time.

## 6. Module Placement

Primary module:

- `:core:sensors`

Allowed dependencies:

- `:core:common`
- `:core:model` only if a shared model truly belongs there

Disallowed dependencies:

- `:app`
- `:feature:*`
- `:domain:session`
- `:core:database`
- `:core:datastore`
- Compose UI

Design intent:

- `:core:sensors` should publish validity information
- `:domain:session` should consume that information later
- neither module should collapse into the other

## 7. Recommended Public Surface

Phase 5 should not expose raw sensor listeners directly to feature code.

Recommended public surface:

- [ ] `FocusValidityMonitor` or similar facade
- [ ] `FocusValidityResult`
- [ ] `FocusValidityReason`
- [ ] `FocusStabilityState`
- [ ] debug-only diagnostics model

Recommended output model shape:

- `isValid: Boolean`
- `reason: FocusValidityReason`
- `stabilityState: FocusStabilityState`
- `orientationConfidence: Float?`
- `movementScore: Float?`
- `isDebugOnlyDetailAvailable: Boolean`

The exact field names can change, but the important design choice is:

- later layers should receive a semantic result
- not a raw stream of accelerometer values

## 8. Data And Signal Model

Recommended domain-facing validity result categories:

- [ ] `ValidFaceDownStable`
- [ ] `InvalidFaceUp`
- [ ] `InvalidVertical`
- [ ] `InvalidMoving`
- [ ] `InvalidPocketLike`
- [ ] `InvalidUnknown`
- [ ] `Unavailable`

Recommended stability states:

- [ ] `Unstable`
- [ ] `Stabilizing`
- [ ] `Stable`

Recommended debug metrics:

- [ ] current tilt angle
- [ ] face-down confidence / orientation confidence
- [ ] movement variance / movement score
- [ ] last valid duration
- [ ] current sampling frequency if useful
- [ ] active sensor source list

These debug metrics should be gated so they can exist for development without becoming a production UX commitment.

## 9. Android Sensor Inputs

From `architecture.md`, the preferred inputs are:

- [ ] accelerometer gravity vector
- [ ] rotation vector when available
- [ ] optional gyroscope only if needed after testing

Recommended Phase 5 plan:

1. Start with accelerometer + rotation vector.
2. Use gravity/orientation math first.
3. Add gyroscope only if walking/continuous-motion discrimination clearly needs it.

Why this recommendation:

- keeps V1 simpler
- avoids overfitting too early
- reduces battery and implementation complexity
- still gives enough signal richness for posture and movement checks

## 10. Sensor Sampling Strategy

The implementation plan should explicitly choose sampling and smoothing behavior rather than leaving it implicit.

Recommended direction:

- [ ] use a moderate sensor sampling rate appropriate for near-real-time posture validation
- [ ] smooth over a short rolling window
- [ ] classify based on recent stability, not single-frame changes
- [ ] avoid ultra-high-rate processing unless testing proves it necessary

Target qualities:

- quick enough to feel responsive
- stable enough not to flap on minor vibrations
- light enough not to create unnecessary battery cost

## 11. Orientation Rules

### Valid Face-Down Requirements

Use the product guidance from `architecture.md`:

- [ ] screen facing down
- [ ] device mostly horizontal
- [ ] movement variance below threshold

Recommended policy:

- require all three
- if any one signal is ambiguous, treat as invalid

Why:
the session engine already supports arming and grace periods, so the sensor engine does not need to be permissive to keep the experience humane.

### Explicit Invalid Cases

- [ ] face-up
- [ ] vertical / near-vertical
- [ ] large tilt away from horizontal
- [ ] strong or repeated movement
- [ ] continuously moving but roughly face-down

## 12. Stability Rules

The detector should separate “orientation looks right” from “orientation is valid and stable.”

Recommended behavior:

- [ ] detect raw face-down orientation
- [ ] compute stability over a recent time window
- [ ] expose `Stabilizing` before declaring truly stable validity
- [ ] allow tiny harmless vibrations to remain valid/stabilizing rather than immediately invalid

The session engine already has a 3-second arming period.
That means Phase 5 does not need to duplicate session timing logic, but it should still expose stability clearly enough that later integration can behave well.

Recommended split:

- sensor engine owns physical validity and stability
- session engine owns timing consequences of that validity

## 13. Movement Rejection Rules

Phase 5 should treat movement conservatively.

Required behaviors:

- [ ] reject obvious pickup motion
- [ ] reject repeated strong tilting
- [ ] reject walking-like movement patterns
- [ ] reject obvious continuous movement even when face-down orientation appears roughly correct

Recommended heuristic approach:

- use rolling movement variance and tilt variance
- require sustained low-motion stability for valid classification
- if uncertain whether movement is harmless or active handling, fall back to invalid

This is the right tradeoff for V1 because false-valid focus time harms trust more than a few extra pauses.

## 14. Pocket Rejection

`architecture.md` explicitly says pocket mode should not count as valid focus in V1.

Recommended implementation approach:

- [ ] treat “face-down-ish but not horizontally surface-stable” as invalid
- [ ] reject clusters of readings that indicate device motion with no stable resting signature
- [ ] do not require proximity/light sensor support in V1

Recommended policy:

- pocket rejection is best-effort
- ambiguous pocket-like states should be classified invalid

This avoids blocking on extra sensor dependencies while still honoring the product rule.

## 15. Walking And Moving-Vehicle Rejection

This is harder than simple face-down detection and should be planned honestly.

Recommended V1 stance:

- [ ] implement best-effort heuristics for walking-like and continuously moving scenarios
- [ ] do not promise perfect activity classification
- [ ] fall back to invalid when movement looks non-resting

What Phase 5 should not do:

- [ ] over-promise exact activity recognition
- [ ] require a dedicated activity-recognition system for V1

This keeps the product behavior honest while still aiming for strong practical rejection.

## 16. Unavailable Sensor Handling

From `architecture.md`:

- if required sensors are unavailable, session cannot start

Phase 5 should plan for:

- [ ] explicit “sensor unavailable” state/result
- [ ] ability for app layers to show a clear user-facing message later
- [ ] no silent fallback that pretends validity can still be determined reliably

## 17. Debug-Only Diagnostics Surface

This phase should deliberately plan a debug-only diagnostics surface for internal testing.

Recommended scope:

- [ ] current validity result
- [ ] current reason code
- [ ] live orientation angle / confidence
- [ ] movement score / stability score
- [ ] active sensor list
- [ ] last transition timestamps

Recommended constraints:

- [ ] no production-visible diagnostics UI in V1 by default
- [ ] debug data may be surfaced to logs, developer overlays, or test-only screens later
- [ ] diagnostics API should be structured enough to support later tooling if needed

Why include this now:

- sensors are notoriously device-specific
- diagnostics reduce guesswork during calibration and QA
- it is much cheaper to design for observability now than bolt it on after false-positive reports begin

## 18. Integration Shape With Phase 4

Although Phase 4 is already built, Phase 5 should only plan the integration boundary, not fully implement all later UI/service behavior.

Recommended boundary:

- sensor engine emits semantic validity results
- adapter layer later maps those results into Phase 4 `SessionInput` transitions

Recommended future mapping examples:

- valid stable -> `PhoneBecameValid`
- invalid moving / invalid orientation -> `PhoneBecameInvalid`

This keeps the session engine and sensor engine independently testable.

## 19. Implementation Structure

Recommended internal structure for `:core:sensors`:

- [ ] `FocusValidityMonitor`
- [ ] `AndroidSensorSource` or equivalent Android glue
- [ ] `OrientationEvaluator`
- [ ] `MovementClassifier`
- [ ] `PocketRejectionHeuristics`
- [ ] `FocusValidityEvaluator`
- [ ] `FocusSensorDiagnostics`

Suggested responsibility split:

1. Android glue:
   reads sensor events, lifecycle hooks, registration/unregistration

2. math/evaluation layer:
   computes orientation, movement, validity, and reasons

3. diagnostics layer:
   packages internal metrics safely for debugging

Keep Android APIs at the edges so the posture/motion math can be unit tested.

## 20. Testing Strategy

This phase should combine local deterministic tests with required manual device validation.

### Automated Tests

- [ ] unit tests for orientation classification
- [ ] unit tests for movement/stability thresholds
- [ ] unit tests for pocket-like invalid classification
- [ ] unit tests for conservative fallback behavior
- [ ] unit tests for rich result mapping
- [ ] Android-facing tests for registration lifecycle where practical

### Minimum Automated Test Matrix

- [ ] simulated stable face-down readings -> valid
- [ ] simulated face-up readings -> invalid
- [ ] simulated vertical readings -> invalid
- [ ] simulated small vibration on stable surface -> remains valid or stabilizing
- [ ] simulated brief accidental bump -> does not immediately become confidently valid if unstable
- [ ] simulated pickup -> invalid
- [ ] simulated long continuous movement -> invalid
- [ ] simulated walking-like variance -> invalid
- [ ] simulated ambiguous pocket-like readings -> invalid
- [ ] rotation-vector missing fallback path still behaves sensibly
- [ ] unavailable required sensors -> unavailable result

### Manual Real-Device Validation

This is a required exit criterion for the implementation plan.

Required device matrix target:

- [ ] Pixel device
- [ ] Samsung device
- [ ] OnePlus / Realme or equivalent
- [ ] lower-end Android device

Required OS coverage target:

- [ ] Android 12
- [ ] Android 13
- [ ] Android 14+

Required manual behavior checklist:

- [ ] stable face-down placement becomes valid reliably
- [ ] face-up never becomes valid
- [ ] vertical/in-hand posture never becomes valid
- [ ] minor table vibration does not create frustrating flapping
- [ ] walking with phone does not count as valid
- [ ] pocket-like carrying does not count as valid
- [ ] returning phone to table re-establishes stable validity cleanly
- [ ] notification vibration does not create unfair pauses

Recommendation:

- manual device validation is required before calling Phase 5 complete
- if the full matrix is not available immediately, document the missing devices explicitly rather than silently treating the phase as done

## 21. Battery And Performance Considerations

The plan should explicitly acknowledge runtime cost.

Recommended constraints:

- [ ] avoid unnecessarily high sampling rates
- [ ] unregister listeners when not needed
- [ ] structure evaluation logic to avoid excessive allocations
- [ ] prefer simple rolling-window math over heavy signal processing for V1

Success criterion:

- the detector feels responsive without obviously wasteful background cost

## 22. Risks And Mitigations

### Risk: Detector Feels Noisy

- Mitigation: conservative validity rules, stability windows, and movement smoothing.

### Risk: Detector Grants False Focus Time

- Mitigation: ambiguous states should resolve invalid, not valid.

### Risk: Device Fragmentation Causes Inconsistent Behavior

- Mitigation: required diagnostics surface plus required manual device matrix.

### Risk: Pocket/Walking Rejection Becomes Over-Ambitious

- Mitigation: treat those as best-effort heuristics and use conservative invalid fallback.

### Risk: Android Glue Becomes Untestable

- Mitigation: keep sensor math and classification in pure, unit-testable classes with Android APIs at the edges.

### Risk: Debug Tooling Leaks Into Production UX

- Mitigation: plan diagnostics as debug-only and keep them out of normal user flows.

## 23. Documentation Updates Required During Implementation

During implementation, update:

- [ ] `v1-implementation-plan.md`
- [ ] this phase plan with implementation completion notes
- [ ] `docs/module-dependency-rules.md` if module responsibilities shift
- [ ] sensor-specific documentation if heuristics become complex enough to merit a dedicated doc

Record:

- files touched
- major classes created
- test coverage added
- manual device validation completed or still pending
- residual detector limitations

## 24. Verification Plan For Implementation

After implementation, run:

- [ ] `git diff --check`
- [ ] targeted `:core:sensors` unit tests
- [ ] targeted `:core:sensors` Android/instrumentation tests if added
- [ ] build/assemble tasks needed to prove Android sensor code compiles cleanly
- [ ] broader app verification only if Phase 5 wiring reaches compile paths outside `:core:sensors`

And complete:

- [ ] required manual device validation checklist

If device coverage is incomplete, document that explicitly before calling the phase complete.

## 25. Acceptance Criteria

Phase 5 is complete only when:

- [ ] `:core:sensors` contains a real Android-backed sensor engine, not a placeholder.
- [ ] Face-down validity uses real Android sensor data.
- [ ] The detector distinguishes valid, invalid, and unstable states semantically.
- [ ] Conservative fallback behavior is implemented for ambiguous states.
- [ ] Face-up, vertical, obvious movement, and unavailable-sensor cases are rejected correctly.
- [ ] Pocket-like and walking-like states are treated as invalid on a best-effort basis.
- [ ] Debug-only diagnostics are available for internal testing.
- [ ] Automated tests cover the core classification rules.
- [ ] Required manual device validation is completed or clearly documented as incomplete.
- [ ] Documentation is updated with actual implementation and verification status.
- [ ] The user approves this plan before implementation begins.

## 26. Recommended Implementation Order

1. Define validity result models and diagnostics surface.
2. Build pure orientation and movement evaluation helpers.
3. Add Android sensor registration and event ingestion.
4. Build the validity monitor facade.
5. Add debug diagnostics output.
6. Add automated tests for classification logic.
7. Run device-level manual validation and tune thresholds.
8. Update docs with final behavior and residual limitations.

This order reduces the risk of burying threshold decisions inside hard-to-test Android glue.

## 27. Approval Gate

Implementation must not begin until this Phase 5 plan is approved.

Common next steps:

- approve this Phase 5 plan and start implementation
- request changes to the plan before implementation
- narrow or expand the diagnostics or device-validation scope before implementation
