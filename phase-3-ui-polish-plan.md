# Phase 3 UI/UX Polish Plan

## Overview

This plan implements the highest-priority remaining item from the master UI/UX polish roadmap: the **Rich Session Complete Summary Screen** (P0 #1) and the associated **Completion Celebration Copy** (P2 #14).

These two items are naturally combined since they both affect the same screen state.

**Scope:** P0 #1 + P2 #14 from `ui-polish-implementation-plan.md`  
**Approach:** Replace the current minimal `ResultState` composable with a rich, celebratory completion screen inline within `FocusScreen`. Canvas-drawn graphics, no new assets.

---

## Item — Rich Session Complete Summary + Celebration Copy

### Problem

The current completion screen is a flat text result ("Clean session completed" / "✓") with a "Done" button. This undermines the psychological reward of finishing a focus session — the primary dopamine moment in the app.

### Solution

Replace `ResultState` with a rich `SessionCompleteContent` composable that:
- Shows a large green success circle with white checkmark (Canvas-drawn)
- Uses celebratory language ("Great focus!", "Session complete", etc.)
- Displays detailed time breakdown rows
- Shows a "Clean Session" badge when appropriate
- Retains the "Done" button for dismissal

### Architecture

#### 1. New Composable: `SessionCompleteContent`

Replaces the current `ResultState` in `FocusScreen`'s `AnimatedContent` for all terminal presentation states: `CompletedClean`, `CompletedInterrupted`, `EndedEarly`, `Broken`, `Invalid`.

```
┌──────────────────────────────┐
│         ✓ (green circle)     │  ← Canvas-drawn: 96dp circle, green fill, white checkmark
│                              │     Only for CompletedClean
│       "Great focus!"         │  ← titleMedium, celebratory
│                              │
│  Focus Time     25:00        │  ← bodyMedium label + trailing value
│  Penalty Time   +0:00        │  ← danger red if > 0, tertiary if 0
│  Total Time     25:00        │  ← bodyMedium
│                              │
│  ✓ Clean Session             │  ← badge with checkmark, only if clean == true
│                              │
│  ┌────────────────────────┐  │
│  │         Done           │  │  ← pill-shaped primary button
│  └────────────────────────┘  │
└──────────────────────────────┘
```

#### 2. Copy Map (Celebration Language)

| Presentation State | Title | Body | Circle |
|---|---|---|---|
| CompletedClean | "Great focus!" | — | Green with checkmark (✓) |
| CompletedInterrupted | "Session complete" | "Focus session saved." | Gray/surface with checkmark |
| EndedEarly | "Session ended early" | "Current progress was saved as partial." | None (or yellow) |
| Broken | "Session broken" | "This session no longer counts as clean focus." | None (or red X) |
| Invalid | "Not enough focus time to count" | — | None |

#### 3. Data Fields

Existing `FocusUiState` already has:
- `penaltySeconds` — used for "Penalty Time" row
- `interruptionCount` — used for body text in CompletedInterrupted
- `clean` — used for Clean Session badge visibility

Need to compute or expose:
- `completedFocusTimeSeconds` — `validFocusSeconds` from the completed session
- `completedTotalTimeSeconds` — `actualElapsedSeconds` from the completed session

These can be derived from existing `FocusUiState` fields (`selectedDurationSeconds - penaltySeconds` for focus time, `selectedDurationSeconds` for total time) or added as explicit fields.

**Decision:** Use existing `remainingSeconds`, `penaltySeconds`, `selectedDurationSeconds` to derive:
- Focus Time = `selectedDurationSeconds - remainingSeconds` (valid focus time)
- Penalty Time = `penaltySeconds`
- Total Time = `selectedDurationSeconds` (planned duration)

This avoids adding new domain fields to `FocusUiState`.

#### 4. Canvas Checkmark Circle

```kotlin
@Composable
private fun CompletionCircle(clean: Boolean) {
    Canvas(modifier = Modifier.size(96.dp)) {
        if (clean) {
            // Green filled circle
            drawCircle(color = successColor, radius = size.minDimension / 2)
            // White checkmark
            // Draw a simple path: two lines forming a checkmark
        } else {
            // Gray circle with subtle checkmark or no circle for broken/invalid
        }
    }
}
```

#### 5. Clean Session Badge

A small chip/badge row:
```
✓ Clean Session
```
Only shown when `uiState.clean == true`.

### Acceptance Criteria
- [ ] Clean completions show a large green circle with white checkmark.
- [ ] Title uses celebratory language ("Great focus!") for clean sessions.
- [ ] Time breakdown rows (Focus Time, Penalty Time, Total Time) are visible and correctly formatted.
- [ ] Penalty Time displayed in red/danger color if > 0, gray otherwise.
- [ ] Clean Session badge with checkmark appears only when `clean == true`.
- [ ] Broken/Invalid states show appropriate messaging (no celebration).
- [ ] Screen matches existing dark/light theme.
- [ ] Screenshot tests pass for all completion variants.
- [ ] Tapping "Done" returns to Idle state (existing behavior preserved).

### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - Replace `ResultState` composable with `SessionCompleteContent`
  - Add `CompletionCircle` Canvas composable
  - Add `TimeBreakdownRow` composable
  - Update `AnimatedContent` block to use new composable
- `feature/focus/src/test/kotlin/phonedown/feature/focus/FocusScreenScreenshotTest.kt`
  - Add completion variant screenshots if not already covered
  - Update baselines

### Implementation Order
1. Create the `CompletionCircle` Canvas composable (green circle + checkmark)
2. Create the `SessionCompleteContent` composable (title + breakdown rows + badge + Done button)
3. Update copy for all presentation states (celebration language)
4. Replace `ResultState` usage in `FocusScreen`
5. Re-record Paparazzi baselines

### Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Time breakdown calculation is incorrect for aborted sessions | Low | Low | Use `coerceAtLeast(0L)` on all derived values |
| Canvas checkmark path looks wrong at different DPIs | Low | Medium | Use relative coordinates (fraction of circle size) |
| "Great focus!" text doesn't fit in some languages | Low | Low | Use `titleSmall` with wrapping |

### Verification Checklist

```bash
# Build
./gradlew :app:assembleDebug

# Unit tests
./gradlew :feature:focus:testDebugUnitTest

# Screenshot tests
./gradlew :feature:focus:recordPaparazziDebug
./gradlew :feature:focus:verifyPaparazziDebug

# Full check
./scripts/check.sh
```

---

*Plan created: 2026-05-05*  
*Scope: P0 #1 — Rich Session Complete Summary + P2 #14 — Completion Celebration Copy*  
*Prerequisite: Phase 2 complete (ReadyToFocus, Pause/AddTime, Interruption redesign, Calendar strip, Settings restructuring)*
