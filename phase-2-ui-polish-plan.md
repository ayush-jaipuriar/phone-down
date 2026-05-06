# Phase 2 UI/UX Polish Plan

## Overview

This plan implements all five P1 (High Impact) items from the master UI/UX polish roadmap. These are the next highest-priority improvements after Phase 1's foundational visual system changes.

**Scope:** P1 Items 4–8 from `ui-polish-implementation-plan.md`  
**Approach:** Inline state changes within existing screens; no new screens or navigation routes.  
**Aesthetic:** Consistent with existing dark/light theme system (no new colors or typography).  

---

## Item 1 — "Ready to Focus?" Pre-Session Screen (P1 #4)

### Problem
Tapping "Start Focus" immediately transitions to "Waiting for phone down," giving users no moment to prepare or understand the physical ritual.

### Solution
Add a `ReadyToFocus` presentation state that displays inline within `FocusScreen` before the session officially begins.

### Architecture
- **New enum value:** `FocusPresentationState.ReadyToFocus`
- **New UI section in `FocusScreen`:** `ReadyToFocusContent()`
  - Title: "Ready to focus?"
  - Numbered instruction list:
    1. Tap Start Focus
    2. Place your phone face down
    3. Stay still and focus
  - Bottom action: "Phone Down to begin" subtitle (no button — purely instructional)
  - Back arrow (top-left or inline) to cancel → `Idle`
- **ViewModel flow:**
  - `StartClicked` → sets `presentationState = ReadyToFocus` (no session created yet)
  - After 2-second delay OR on first valid sensor face-down reading → auto-advance to `WaitingForPhoneDown`, then call `runtimeCoordinator.ensureSessionStarted()`
  - `ReadyBackClicked` → returns to `Idle`
- **Illustration:** Placeholder vector (phone face-down silhouette) in `feature/focus/src/main/res/drawable/`. Final art can be swapped later without code changes.

### Acceptance Criteria
- [ ] Tapping "Start Focus" shows Ready state inline (not a new screen or route).
- [ ] "Ready to focus?" title + 3-step numbered list is visible.
- [ ] Phone illustration placeholder is displayed.
- [ ] Back/cancel affordance returns to Idle without creating a session.
- [ ] Auto-advances to WaitingForPhoneDown after ~2s or on valid sensor reading.
- [ ] Session is NOT created until auto-advance occurs.
- [ ] Matches dark/light theme styling.

### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — add `ReadyToFocusContent()`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusPresentationState.kt` — add `ReadyToFocus`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusEvent.kt` — add `ReadyBackClicked`, `ReadyAutoAdvance`
- `app/src/main/java/phonedown/app/focus/FocusViewModel.kt` — handle ready flow, delay, sensor wiring
- `app/src/main/java/phonedown/app/focus/FocusRoute.kt` — wire `ReadyBackClicked`
- Add illustration asset to `feature/focus/src/main/res/drawable/`

---

## Item 2 — Pause & Add Time Controls (P1 #5)

### Problem
Active sessions only show an "End" button. Users cannot pause intentionally or extend a session when in flow.

### Solution
Add Pause and Add Time icon buttons to the `InProgressActions` row during active sessions.

### Architecture
- **Active state action row:** 3 circular icon buttons horizontally centered:
  - **End** — square stop icon (existing, styled as circular icon button)
  - **Pause** — pause icon (two vertical bars)
  - **Add Time** — plus icon
- **Paused state action row:** 2 circular icon buttons:
  - **End** — square stop icon
  - **Resume** — play icon
- **New events:**
  - `PauseClicked` → transitions UI to paused state, marks `clean = false`
  - `ResumeClicked` → returns to Active if phone is valid face-down
  - `AddTimeClicked` → shows inline options (e.g., "+5 min", "+10 min", "+15 min") as a small chip row below the buttons
- **ViewModel:**
  - `PauseClicked` → sets `presentationState = PausedByUser`, updates `clean = false`
  - `ResumeClicked` → checks sensor validity; if valid → `Active`, else → `PausedByPickup`
  - `AddTimeClicked` → exposes `addTimeOptions: List<Int>` in `FocusUiState`; selecting one adds minutes to `remainingSeconds`
- **No domain engine changes in Phase 2** — Add Time is UI-only (adjusts local timer). Pause is a UI state toggle. Domain engine integration deferred to avoid scope creep.

### Acceptance Criteria
- [ ] Active session shows 3 circular icon buttons: End, Pause, Add Time.
- [ ] Paused session shows 2 circular icon buttons: End, Resume.
- [ ] Tapping Pause sets state to paused, timer stops, clean status becomes false.
- [ ] Tapping Resume returns to Active if phone is face-down valid.
- [ ] Add Time shows inline chip options (+5, +10, +15 min) when tapped.
- [ ] Selecting an add-time option increases remaining time and dismisses options.
- [ ] Buttons match pill/circular styling from design system.

### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — update `InProgressActions()`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusEvent.kt` — add `PauseClicked`, `ResumeClicked`, `AddTimeClicked`, `AddTimeSelected(minutes: Int)`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusUiState.kt` — add `addTimeOptions`, `isAddTimeVisible`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusPresentationState.kt` — ensure `PausedByUser` exists
- `app/src/main/java/phonedown/app/focus/FocusViewModel.kt` — handle pause/resume/add-time logic
- `app/src/main/java/phonedown/app/focus/FocusRoute.kt` — wire new events

---

## Item 3 — Interruption Screen Redesign (P1 #6)

### Problem
"Focus paused" is generic and lacks urgency. No countdown, no visual explanation of *why* focus stopped.

### Solution
Redesign the `PausedByPickup` state to show "Phone Picked Up" in danger red, a countdown timer, and a phone illustration.

### Architecture
- **Title change:** "Focus paused" → "Phone Picked Up" in `MaterialTheme.colorScheme.error` (red)
- **Countdown timer:**
  - Large red text showing grace period remaining (e.g., "00:05")
  - `FocusUiState.graceRemainingSeconds` updated every second by ViewModel
  - Color: starts `errorContainer` (yellow-orange) at 5s, transitions to `error` (red) at 2s
- **Subtitle:** "Keep your phone down to continue" (existing, kept)
- **Penalty text:** Shows "+1:00 penalty" in red once grace expires (existing logic, restyled)
- **Illustration:** Placeholder vector (phone being lifted with upward arrow) in `feature/focus/src/main/res/drawable/`
- **Layout:** Title + illustration + countdown stacked vertically, replacing the current minimal text layout

### Acceptance Criteria
- [ ] Interruption screen shows "Phone Picked Up" title in red.
- [ ] Phone pickup illustration is visible.
- [ ] Red countdown timer shows grace period remaining (5 → 0).
- [ ] Countdown color transitions from yellow-orange to red.
- [ ] Penalty text appears in red after grace expires.
- [ ] Existing sensor auto-resume logic remains unchanged.
- [ ] Matches dark/light theme.

### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — enhance `PausedByPickupContent()`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusUiState.kt` — add `graceRemainingSeconds`
- `app/src/main/java/phonedown/app/focus/FocusViewModel.kt` — compute and emit grace countdown
- Add illustration asset to `feature/focus/src/main/res/drawable/`

---

## Item 4 — Calendar Strip in Insights (P1 #7)

### Problem
Insights starts with "Today" section. No week-at-a-glance or quick day navigation.

### Solution
Add a horizontal 7-day calendar strip at the top of `InsightsContent`.

### Architecture
- **New composable:** `InsightsCalendarStrip()`
  - Shows Mon–Sun with date numbers
  - Each day: short name + date number in a compact vertical layout (~48dp wide)
  - Current day: filled pill background (`surfaceRaised`) + bold text
  - Selected day (tapped): accent border or dot indicator
  - Past days: fully interactive
  - Future days: muted/disabled
- **Integration:** Placed as first item in `InsightsContent` LazyColumn, above "Today" section
- **Interaction:**
  - `onDaySelected(date)` callback → `InsightsViewModel` updates `selectedDate`
  - "Today" section re-renders with selected day's data
  - Show "Back to Today" text button when a non-today date is selected
- **Data:** Reuse existing `GetDailyFocusSummaryUseCase` with a date parameter

### Acceptance Criteria
- [ ] Calendar strip shows current week (Mon–Sun) with dates.
- [ ] Current day is visually highlighted.
- [ ] Tapping a past day updates the "Today" section to show that day's data.
- [ ] "Back to Today" affordance appears when a different day is selected.
- [ ] Strip is visible at top of Insights in both themes.
- [ ] Compact styling (~72dp total height).

### Files to Modify
- `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsCalendarStrip.kt` — new composable
- `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt` — integrate strip
- `app/src/main/java/phonedown/app/insights/InsightsViewModel.kt` — handle `selectedDate`, fetch day data
- `app/src/main/java/phonedown/app/insights/InsightsRoute.kt` — wire `onDaySelected`

---

## Item 5 — Settings Restructuring (P1 #8)

### Problem
6 sections feel cluttered and require excessive scrolling. Cognitive load is high.

### Solution
Collapse into 3 clean sections with clear hierarchy and chevron indicators for navigable rows.

### Architecture
- **Section 1: Focus**
  - Default Duration (→ chevron, navigable)
  - Duration Presets (display only)
  - Custom Duration (Pro-gated)
  - Sounds (toggle)
  - Haptics (toggle)
  - Theme selector (compact pill toggle)
  - Start Delay (display only)
- **Section 2: Account**
  - Google Account (→ chevron)
  - Phone Down Pro (→ chevron)
  - Backup & Restore (→ chevron)
  - Auto Backup (toggle, Pro-gated)
- **Section 3: About**
  - Privacy Policy (→ chevron)
  - Terms of Service (→ chevron)
  - Support (→ chevron)
  - Version (display only)
  - Delete All Data (destructive, at bottom, red text)
- **Removed standalone sections:** Pro and Privacy folded into Account and About.
- **Row styling updates:**
  - Navigable rows: title + value + trailing chevron (→)
  - Toggle rows: title + Switch
  - Display-only rows: title + value text aligned right
  - Destructive row: red text, no chevron

### Acceptance Criteria
- [ ] Settings has 3 clearly defined sections: Focus, Account, About.
- [ ] Focus section contains all timer and preference settings.
- [ ] Account section contains auth, Pro, and backup rows.
- [ ] About section contains legal, support, version, and delete action.
- [ ] Navigable rows show trailing chevron (→).
- [ ] Screen is scannable with minimal scrolling.
- [ ] Pro gating remains on appropriate rows.
- [ ] Matches dark/light theme.

### Files to Modify
- `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsScreen.kt` — restructure sections
- `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsSection.kt` — update or replace section composables
- `feature/settings/src/test/kotlin/phonedown/feature/settings/SettingsScreenScreenshotTest.kt` — re-record baselines

---

## Cross-Cutting Concerns

### Shared Illustration Assets
Both Ready screen and Interruption screen need phone illustration vectors. To avoid duplication:
- Create a single `ic_phone_face_down.xml` placeholder in `feature/focus/src/main/res/drawable/`
- Interruption screen can rotate/flip the same asset or add an arrow overlay via Compose
- If time permits, create a second `ic_phone_picked_up.xml` for interruption specificity

### State Machine Impact
- **ReadyToFocus** is a pure UI state — no domain engine changes.
- **Pause/Resume** is a UI state toggle in Phase 2 — domain engine integration (ManualPauseRequested) is deferred.
- **Add Time** is UI-only (local timer adjustment) in Phase 2.
- **Interruption redesign** is purely visual — no domain changes.

### Testing Strategy
1. **Paparazzi screenshots** for each new/modified state:
   - Focus: ReadyToFocus, Active with Pause/Add Time, PausedByUser, PausedByPickup (redesigned)
   - Insights: with calendar strip, with selected day
   - Settings: restructured layout
2. **Compose UI tests:**
   - Ready → cancel returns to Idle
   - Ready → auto-advance triggers session start
   - Pause button shows, Resume returns to Active
   - Add Time chips appear and adjust timer
   - Calendar strip day selection updates data
3. **Unit tests:**
   - ViewModel grace countdown logic
   - ViewModel ready state transitions

---

## Implementation Order

1. **Settings restructuring** — Isolated, no dependencies, easiest win.
2. **Calendar strip** — Isolated to Insights module.
3. **Interruption redesign** — Visual only, builds on existing paused state.
4. **Ready to Focus screen** — Adds new presentation state, moderate complexity.
5. **Pause & Add Time** — Most complex (new events, ViewModel logic, inline chips).

This order minimizes merge conflicts and allows early verification of isolated items.

---

## Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Ready screen auto-advance delay conflicts with sensor events | Medium | Medium | Debounce: if sensor fires during delay, cancel delay and advance immediately. |
| Add Time UI-only approach creates UX confusion (not persisted) | Low | Medium | Add a subtle note "Adds time to current session only" or implement domain hook if trivial. |
| Calendar strip date formatting edge cases (week boundaries) | Low | Low | Use `java.time` APIs; test with Monday and Sunday start-of-week. |
| Settings restructure breaks existing UI tests | Medium | Low | Update test tags/semantics in parallel with layout changes. |
| Illustration placeholders look too crude | Medium | Low | Use clean geometric vectors (Material Design-style); avoid complex art. |

---

## Verification Checklist

After implementation, run:

```bash
# Build
./gradlew :app:assembleDebug

# Unit tests — all affected modules
./gradlew :app:testDebugUnitTest
./gradlew :feature:focus:testDebugUnitTest
./gradlew :feature:insights:testDebugUnitTest
./gradlew :feature:settings:testDebugUnitTest

# Screenshot tests (record new baselines)
./gradlew :feature:focus:recordPaparazziDebug
./gradlew :feature:insights:recordPaparazziDebug
./gradlew :feature:settings:recordPaparazziDebug

# Verify baselines
./gradlew :feature:focus:verifyPaparazziDebug
./gradlew :feature:insights:verifyPaparazziDebug
./gradlew :feature:settings:verifyPaparazziDebug

# Full check (expect pre-existing ktlint failures)
./scripts/check.sh
```

---

## Documentation Updates

After implementation, update:

1. **`v1-implementation-plan.md`** — Mark P1 items complete.
2. **`ui-polish-implementation-plan.md`** — Check off items 4–8.
3. **`docs/design-system.md`** — Document new components (`ReadyToFocusContent`, `InsightsCalendarStrip`, icon button row patterns).
4. Create `phase-2-completion-report.md` summarizing changes, test results, and any deviations.

---

*Plan created: 2026-05-05*  
*Scope: P1 High Impact UI/UX items*  
*Prerequisite: Phase 1 complete (design system shapes, borders, timer, ring dot, nav icons, gear icon)*
