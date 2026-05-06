# Phase 4 UI/UX Polish Plan

## Overview

This plan implements the three remaining items from the master UI/UX polish roadmap: the Hourly Focus Chart, the Arming Countdown Animation, and Calendar Day Historical Insights.

**Scope:** P2 #9 + P3 #15 + P3 #16 from `ui-polish-implementation-plan.md`  
**Approach:** Use case-driven data layer for hourly chart and day insights; animation overlay for arming.

---

## Item 1 — Hourly Focus Chart (P2 #9)

### Problem
Insights shows daily/weekly totals but not *when* during the day the user focused. No hourly distribution.

### Solution
Add an hourly bar chart below the Today card showing focus minutes per hour.

### Architecture

**New use case:** `GetHourlyFocusUseCase` in `:domain:insights`
- Input: `LocalDate` (target day)
- Output: `List<HourFocus>` — `data class HourFocus(val hour: Int, val focusSeconds: Long)`
- Query sessions for the given day, aggregate `validFocusSeconds` by hour of day
- Hours 0–23, zero-filled for hours with no data

**New chart composable:** `PhoneDownHourlyChart` in `:core:charts`
- X-axis: hours (0, 6, 12, 18)
- Y-axis: focus minutes
- Bars for each hour, style matching existing `PhoneDownBarChart`
- Height: ~120dp
- Call existing bar chart internally with formatted data

**ViewModel changes:** `InsightsViewModel`
- Call `getHourlyFocus()` on refresh
- Expose `hourlyFocus: List<HourFocus>` in `InsightsUiState`

**InsightsContent integration:**
- Place `HourlyChartSection` below `TodaySection` and above `WeeklyChartSection`
- Only show if Pro user AND hourly data is non-empty (or gate behind having at least one data point)

### Acceptance Criteria
- [ ] Hourly chart visible in Insights when data exists
- [ ] Chart shows focus minutes distributed across 24 hours
- [ ] Style matches existing bar chart (progress/primary colors, minimal axes)
- [ ] Empty state handled gracefully (hidden)
- [ ] Screenshot tests pass

### Files to Modify
- `domain/insights/src/main/kotlin/.../GetHourlyFocusUseCase.kt` — new
- `domain/insights/src/main/kotlin/.../HourFocus.kt` — new data class (or inline in UseCase)
- `core/charts/src/main/kotlin/.../PhoneDownHourlyChart.kt` — new composable
- `feature/insights/src/main/kotlin/.../InsightsUiState.kt` — add `hourlyFocus` field
- `feature/insights/src/main/kotlin/.../InsightsContent.kt` — add `HourlyChartSection`
- `app/src/main/java/.../InsightsViewModel.kt` — collect hourly data

---

## Item 2 — Arming Countdown Animation (P3 #15)

### Problem
Arming shows static "Hold still..." text. No visual countdown feedback.

### Solution
Show a 3-second animated countdown overlay during the Arming state. Numbers "3", "2", "1" fade in/out in the progress ring center.

### Architecture

**FocusRingSection change:**
- When `presentationState == Arming`:
  - Show large countdown text (3, 2, 1) in the ring center
  - Use `Animatable` or keyframe animation for fade + scale
  - Each number visible for ~1 second
- No domain/ViewModel changes needed — pure UI animation

**Animation approach:**
- Use `LaunchedEffect` with `delay(1000)` to cycle through numbers
- `AnimatedContent` with fade transition between numbers
- Style: same as timer text (progress color, large)

### Acceptance Criteria
- [ ] Arming state shows animated countdown (3 → 2 → 1)
- [ ] Each number visible for ~1 second
- [ ] Animation is calm, not frantic
- [ ] Transitions to "Hold still" label after countdown completes

### Files to Modify
- `feature/focus/src/main/kotlin/.../FocusScreen.kt` — update `FocusRingSection` arming logic

---

## Item 3 — Calendar Day Historical Insights (P3 #16)

### Problem
Calendar strip is display-only. Tapping a day doesn't fetch that day's data.

### Solution
Create `GetDayInsightsUseCase` to fetch session summaries for any given date, and wire it to the calendar strip's `onDaySelected`.

### Architecture

**New use case:** `GetDayInsightsUseCase` in `:domain:insights`
- Input: `LocalDate`
- Output: `InsightSummary` (reuse existing data class)
- Query sessions within the date window, compute same summary as today

**ViewModel changes:** `InsightsViewModel`
- Add `onDaySelected(epochDay: Long)` — calls `getDayInsights()`, updates `selectedDaySummary`
- Add `selectedDaySummary: InsightSummary?` to `InsightsUiState`
- `TodaySection` label and data switch to selected day's summary when available

**InsightsContent changes:**
- `TodaySection` shows `selectedDaySummary` when non-null, otherwise `today`
- Label shows day name when a different day is selected (already implemented in Phase 2)

### Acceptance Criteria
- [ ] Tapping a past calendar day fetches and shows that day's summary
- [ ] Today card updates with selected day's data
- [ ] "Back to Today" restores today's data
- [ ] Future days are not interactive (already implemented)
- [ ] Empty days show zero values gracefully

### Files to Modify
- `domain/insights/src/main/kotlin/.../GetDayInsightsUseCase.kt` — new
- `feature/insights/src/main/kotlin/.../InsightsUiState.kt` — add `selectedDaySummary`
- `feature/insights/src/main/kotlin/.../InsightsContent.kt` — use `selectedDaySummary` in `TodaySection`
- `app/src/main/java/.../InsightsViewModel.kt` — add `onDaySelected` logic

---

## Cross-Cutting Concerns

### Dependency Flow
- Item 3 (Calendar Day) depends on Phase 2's calendar strip being in place (it is).
- Item 1 (Hourly Chart) is independent of Items 2 and 3.
- Item 2 (Arming Animation) is purely cosmetic, independent of everything.

### Testing Strategy

1. **Unit tests:**
   - `GetHourlyFocusUseCase` — test hourly aggregation with sample sessions
   - `GetDayInsightsUseCase` — test day summary calculation
2. **Paparazzi:**
   - Insights: with hourly chart
   - Focus: with arming countdown
3. **Compose UI tests:**
   - Calendar day selection fetches data

---

## Implementation Order

1. **Item 2 — Arming Countdown** (simplest, 1 file, no dependencies)
2. **Item 1 — Hourly Chart** (new use case + chart + integration, ~5 files)
3. **Item 3 — Calendar Day Insights** (new use case + ViewModel wiring, ~3 files, depends on Phase 2 calendar)

---

## Verification Checklist

```bash
# Build
./gradlew :app:assembleDebug

# Domain tests
./gradlew :domain:insights:test

# Feature tests
./gradlew :feature:insights:testDebugUnitTest \
    :feature:focus:testDebugUnitTest \
    :app:testDebugUnitTest

# Screenshot tests
./gradlew :feature:insights:recordPaparazziDebug \
    :feature:focus:recordPaparazziDebug

# Verify
./gradlew :feature:insights:verifyPaparazziDebug \
    :feature:focus:verifyPaparazziDebug
```

---

*Plan created: 2026-05-05*  
*Scope: P2 #9 + P3 #15 + P3 #16 — Final remaining UI/UX polish items*  
*Prerequisite: Phase 3 complete*
