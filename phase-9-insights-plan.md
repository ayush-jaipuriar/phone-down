# Phase 9 - Insights Plan

This document defines the implementation plan for Phase 9 of Phone Down V1.

Phase 9 turns the placeholder Insights tab into a real data-driven analytics surface, with free today/7-day insights and Pro-gated advanced metrics, heatmap, and trends.

## 1. Phase Goal

Produce a calm, honest Insights tab that:

- shows real today and 7-day analytics derived from persisted sessions
- respects the free-vs-Pro boundary clearly
- uses Vico charts for 7-day overview and Focus Quality trend
- adds a GitHub-style heatmap for Pro users
- calculates Focus Quality, streaks, best hour, and best weekday correctly
- keeps the UI premium, minimal, and aligned with the existing design system
- covers all calculations with unit tests before wiring UI

## 2. Approved Planning Decisions

- [x] Full V1 insights scope: free + Pro, not incremental phases.
- [x] Standalone `:domain:insights` module with proper use cases; thin ViewModel in `:app`.
- [x] Use Vico for chart rendering (bar charts, line/trend charts).
- [x] Custom Compose Canvas heatmap for Pro GitHub-style contribution view.
- [x] Implement all Pro insights now (heatmap, best hour, best day, trends, export foundation).
- [x] Pro gate via paywall entry points; entitlement checking deferred to Phase 11 billing wiring.

## 3. In Scope

### Free Insights
- [ ] Today summary: total focus time, sessions count, clean sessions count, interruptions count.
- [ ] 7-day focus overview with Vico bar chart.
- [ ] Focus Quality score + label (Deep/Focused/Steady/Fragmented/Scattered).
- [ ] Current streak (consecutive days with at least one session).
- [ ] Session history list (scrollable, showing session date, duration, result, clean status).

### Pro Insights
- [ ] GitHub-style contribution heatmap (day-level focus minutes, up to 1 year).
- [ ] Best focus hour (by valid focus time).
- [ ] Best day of week (by valid focus time).
- [ ] Weekday vs weekend comparison.
- [ ] Completion rate trend (Vico line chart).
- [ ] Clean session ratio trend.
- [ ] Interruption count trend.
- [ ] Focus Quality history trend.
- [ ] Longest clean session.
- [ ] Average session length over time.
- [ ] Data export entry point (foundation; actual file export deferred to Phase 12).

### Charts Module
- [ ] Vico bar chart wrapper (7-day overview).
- [ ] Vico line chart wrapper (trends).
- [ ] Custom heatmap composable (year view, day-level tiles).

### Domain Logic
- [ ] Today aggregation use case.
- [ ] 7-day aggregation use case.
- [ ] Focus Quality calculation use case.
- [ ] Streak calculation use case.
- [ ] Best hour aggregation use case.
- [ ] Best weekday aggregation use case.
- [ ] Completion rate trend use case.
- [ ] Clean ratio trend use case.
- [ ] Interruption trend use case.
- [ ] Focus Quality trend use case.
- [ ] Advanced aggregation use case (longest clean, average session, weekday vs weekend).

### UI Surface
- [ ] Replace placeholder `InsightsScreen` with real data-driven UI.
- [ ] Create `InsightsViewModel` in `:app` wiring domain use cases.
- [ ] Create `InsightsRoute` in `:app` for Hilt injection entry.
- [ ] Free/Pro sectioning with Pro gate cards (non-functional paywall stubs).
- [ ] Empty state when no completed sessions exist yet.
- [ ] Dark/light theme fidelity.

## 4. Out Of Scope

- [ ] Billing entitlement enforcement (Pro gates show stub paywall entry; real enforcement in Phase 11).
- [ ] Actual data export file generation (Phase 12).
- [ ] Pull-to-refresh or live-update during active session.
- [ ] Notification integration for insights.
- [ ] Social/sharing of insights.

## 5. Architectural Intent

```
:domain:insights (pure Kotlin, no Android/Compose)
  ├── GetTodayInsightsUseCase
  ├── GetWeeklyInsightsUseCase
  ├── GetFocusQualityUseCase
  ├── GetStreakUseCase
  ├── GetBestHourUseCase
  ├── GetBestWeekdayUseCase
  ├── GetTrendsUseCase (completion rate, clean ratio, interruptions, FQ)
  ├── GetAdvancedInsightsUseCase (longest clean, avg session, weekday vs weekend)
  ├── GetHeatmapDataUseCase (daily focus minutes, 1 year)
  └── GetHistoryUseCase (paginated session list)
  └── Types: InsightSummary, WeeklyInsight, FocusQualityResult, StreakResult, 
             BestHourResult, BestDayResult, TrendPoint, HeatmapDay, SessionHistoryItem

:core:charts (Compose, depends on vico)
  ├── PhoneDownBarChart (7-day overview wrapper)
  ├── PhoneDownLineChart (trends wrapper)
  └── FocusHeatmap (custom Canvas composable)

:feature:insights (Compose, depends on :core:designsystem, :core:charts, :domain:insights)
  ├── InsightsContent (screen layout, free/pro sections)
  ├── TodaySection (metric cards)
  ├── WeeklyChartSection (Vico bar chart)
  ├── FocusQualitySection (score + label)
  ├── StreakSection
  ├── SessionHistorySection
  ├── HeatmapSection (Pro)
  ├── BestTimeSection (Pro)
  ├── TrendsSection (Pro)
  └── ProGateCard (teaser for non-Pro sections)

:app
  ├── InsightsRoute (Hilt-powered entry)
  └── InsightsViewModel (thin wrapper; delegates to domain use cases)
```

## 6. Domain Model Types

These live in `:domain:insights` as pure Kotlin data classes:

```kotlin
data class InsightSummary(
    val totalFocusSeconds: Long,
    val sessionCount: Int,
    val cleanSessionCount: Int,
    val interruptionCount: Int,
    val penaltyCount: Int,
    val penaltySeconds: Long,
    val incompleteSessionCount: Int,
    val brokenSessionCount: Int,
    val invalidatedSessionCount: Int,
    val abandonedSessionCount: Int,
)

data class WeeklyInsight(
    val days: List<DayInsight>,  // exactly 7 entries, newest first
    val totalFocusSeconds: Long,
    val changePercent: Float,    // vs previous 7 days, or null if not available
)

data class DayInsight(
    val dateEpochDay: Long,       // LocalDate.toEpochDay()
    val focusSeconds: Long,
    val sessionCount: Int,
    val cleanSessionCount: Int,
)

data class FocusQualityResult(
    val score: Int,               // 0-100
    val label: FocusQualityLabel,
    val completionRate: Float,    // 0-1
    val cleanRatio: Float,        // 0-1
    val focusVolumeScore: Float,  // 0-1 (normalized)
    val interruptionScore: Float, // 0-1 (inverted)
)

enum class FocusQualityLabel { Deep, Focused, Steady, Fragmented, Scattered }

data class StreakResult(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
)

data class BestHourResult(
    val hour: Int,                // 0-23
    val focusSeconds: Long,
)

data class BestDayResult(
    val dayOfWeek: java.time.DayOfWeek,  // or Int 1-7
    val focusSeconds: Long,
)

data class TrendPoint(
    val label: String,            // "Mon", "Tue", or "W1", "W2" etc.
    val value: Float,
)

data class HeatmapDay(
    val dateEpochDay: Long,
    val focusMinutes: Int,        // 0-* 
    val level: Int,               // 0-4 for color intensity
)

data class SessionHistoryItem(
    val sessionId: String,
    val startedAtEpochMillis: Long,
    val plannedDurationSeconds: Long,
    val validFocusSeconds: Long,
    val result: SessionResult?,
    val clean: Boolean,
    val broken: Boolean,
)

data class AdvancedInsights(
    val longestCleanFocusSeconds: Long,
    val averageSessionSeconds: Long,
    val weekdayFocusSeconds: Long,
    val weekendFocusSeconds: Long,
)
```

## 7. Focus Quality Formula (per V1 spec)

```
Completion Rate = completedSessions / totalSessionsThatReachedActiveState
Clean Ratio      = cleanCompletedSessions / completedSessions
Focus Volume     = normalized(totalFocusSeconds across window)
Interruption     = 1 - (totalInterruptions / maxInterruptionsNormalized)

Score = (CompletionRate * 40) + (CleanRatio * 25) + (FocusVolume * 20) + (Interruption * 15)
Score clamped to [0, 100]

Labels: 90-100: Deep, 75-89: Focused, 60-74: Steady, 40-59: Fragmented, 0-39: Scattered
```

## 8. UI Layout (Top To Bottom)

```
┌─────────────────────────┐
│  Insights (Top Bar)     │
├─────────────────────────┤
│  ┌─────────────────────┐│
│  │ Today               ││
│  │ [1h20m] [3] [2]    ││  ← Metric cards: Total Focus, Sessions, Clean
│  └─────────────────────┘│
│                         │
│  ┌─────────────────────┐│
│  │ 7 Day Overview      ││
│  │ 8h 45m    +12%      ││  ← Total + change%; Vico bar chart below
│  │ [████ ███ ████ ███ ]││
│  └─────────────────────┘│
│                         │
│  ┌─────────────────────┐│
│  │ Focus Quality  78   ││
│  │ Focused             ││
│  └─────────────────────┘│
│                         │
│  ┌─────────────────────┐│
│  │ Streak          5   ││
│  │ current / 12 longest││
│  └─────────────────────┘│
│                         │
│  ┌─────────────────────┐│
│  │ Session History     ││
│  │ Date  Dur  Result   ││
│  │ ...                 ││
│  └─────────────────────┘│
│                         │
│  ── Pro Insights ──    │
│  ┌─────────────────────┐│
│  │ Focus Heatmap       ││
│  │ [GitHub-style tiles]││
│  └─────────────────────┘│
│  ┌─────────────────────┐│
│  │ Best Focus Time     ││
│  │ 9 AM / Tuesday      ││
│  └─────────────────────┘│
│  ┌─────────────────────┐│
│  │ Trends              ││
│  │ [Vico line charts]  ││
│  └─────────────────────┘│
│  ┌─────────────────────┐│
│  │ Season Highlights   ││
│  │ Longest clean: 2h   ││
│  │ Avg session: 28m    ││
│  └─────────────────────┘│
│  ┌─────────────────────┐│
│  │ Export Data    [Pro]││  ← Stub; real export in Phase 12
│  └─────────────────────┘│
└─────────────────────────┘
```

## 9. Heatmap Design

- GitHub contribution graph style: 7 rows (days of week) × 52+ columns (weeks).
- Each tile = one calendar day. Color intensity = focus minutes that day.
- 5-level scale: 0 min (empty), 1-15m, 16-30m, 31-60m, 60+m.
- Rendered with Compose Canvas for performance.
- Scrollable horizontally to show full year.
- Day-of-week labels on left, month labels on top.
- On long-press or tap: show tooltip with date + exact minutes.

## 10. Vico Chart Strategy

- 7-day bar chart: `CartesianChartHost` with `ColumnCartesianLayer` (bar).
  - X-axis: day labels (Mon-Sun or date abbrev).
  - Y-axis: focus hours.
  - Color: `PhoneDownDesign.colors.progress`.
  - Respects dark/light theme.
- Trends line charts: `CartesianChartHost` with `LineCartesianLayer`.
  - X-axis: week labels.
  - Y-axis: percentage (0-100%) or count.
  - Color: `PhoneDownDesign.colors.progress` with subtle area fill.

## 11. Dependency Plan

### `:domain:insights` (build.gradle.kts)
```
implementation(project(":core:common"))
implementation(project(":core:model"))
testImplementation(libs.junit)
testImplementation(libs.kotlinx.coroutines.test)
```

### `:core:charts` (build.gradle.kts)
```
implementation(project(":core:designsystem"))
implementation(project(":core:model"))
implementation(libs.vico.compose.m3)       // or vico.compose depending on catalog
testImplementation(libs.junit)
```

### `:feature:insights` (build.gradle.kts)
Already wired; may need to add Vico dependency if charts are rendered here.

## 12. ViewModel And Route Design

```kotlin
// In :app
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getTodayInsights: GetTodayInsightsUseCase,
    private val getWeeklyInsights: GetWeeklyInsightsUseCase,
    private val getFocusQuality: GetFocusQualityUseCase,
    private val getStreak: GetStreakUseCase,
    private val getHistory: GetHistoryUseCase,
    private val getHeatmapData: GetHeatmapDataUseCase,
    private val getBestHour: GetBestHourUseCase,
    private val getBestDay: GetBestDayUseCase,
    private val getTrends: GetTrendsUseCase,
    private val getAdvancedInsights: GetAdvancedInsightsUseCase,
) : ViewModel() {
    val uiState: StateFlow<InsightsUiState>
}

data class InsightsUiState(
    val today: InsightSummary = InsightSummary(),
    val weekly: WeeklyInsight? = null,
    val focusQuality: FocusQualityResult? = null,
    val streak: StreakResult? = null,
    val history: List<SessionHistoryItem> = emptyList(),
    val heatmap: List<HeatmapDay> = emptyList(),
    val bestHour: BestHourResult? = null,
    val bestDay: BestDayResult? = null,
    val trends: List<TrendPoint> = emptyList(), // or structured per-metric
    val advanced: AdvancedInsights? = null,
    val isEmpty: Boolean = true,
)
```

## 13. Module Creation

### New use cases in `:domain:insights`

Each use case takes a `SessionRepository` via constructor injection and returns computed results:

| Use Case | Input | Output | Notes |
|---|---|---|---|
| `GetTodayInsightsUseCase` | `Clock` | `InsightSummary` | Filters sessions where `startedAtEpochMillis` is in today's window |
| `GetWeeklyInsightsUseCase` | `Clock` | `WeeklyInsight` | Last 7 complete days + optional previous 7 for change% |
| `GetFocusQualityUseCase` | `Clock` | `FocusQualityResult` | Applies FQ formula over last 7 days |
| `GetStreakUseCase` | (sessions) | `StreakResult` | Scans backward from today for consecutive days with ≥1 non-abandoned session |
| `GetBestHourUseCase` | (sessions) | `BestHourResult` | Groups sessions by start hour, picks max valid focus |
| `GetBestWeekdayUseCase` | (sessions) | `BestDayResult` | Groups by day of week, picks max valid focus |
| `GetTrendsUseCase` | `Clock` | `Map<String, List<TrendPoint>>` | Weekly buckets for completion rate, clean ratio, interruptions, FQ |
| `GetAdvancedInsightsUseCase` | (sessions) | `AdvancedInsights` | Longest clean, avg session, weekday vs weekend |
| `GetHeatmapDataUseCase` | `Clock` | `List<HeatmapDay>` | 365 days of daily focus minutes, plus 5-level intensity |
| `GetHistoryUseCase` | `page`, `size` | `List<SessionHistoryItem>` | Paginated session list, newest first |

Each use case should be a single-responsibility class (not a god object) so they can be tested independently.

## 14. Testing Strategy

### Domain Tests (`:domain:insights`)

- [ ] `GetTodayInsightsUseCase` — verified counts, sums, correct day boundary.
- [ ] `GetWeeklyInsightsUseCase` — 7-day window, change% calculation, edge of week boundaries.
- [ ] `GetFocusQualityUseCase` — all 5 label boundaries (0, 39, 40, 59, 60, 74, 75, 89, 90, 100), edge cases on division by zero.
- [ ] `GetStreakUseCase` — consecutive days, gaps, edge of today, longest streak tracking.
- [ ] `GetBestHourUseCase` — ties, empty sessions.
- [ ] `GetBestWeekdayUseCase` — ties, empty sessions.
- [ ] `GetTrendsUseCase` — correct weekly buckets, rate calculations.
- [ ] `GetAdvancedInsightsUseCase` — longest clean, avg session, weekday/weekend split.
- [ ] `GetHeatmapDataUseCase` — correct day count (365), intensity levels, leap year.
- [ ] `GetHistoryUseCase` — pagination boundaries, sort order.

### UI Tests (`:feature:insights`)

- [ ] Compose UI test: all free-tier sections render with data.
- [ ] Compose UI test: Pro-gated sections show paywall stubs.
- [ ] Compose UI test: empty state when no sessions exist.
- [ ] Paparazzi screenshot tests: Light + Dark themes with sample data.
- [ ] Paparazzi screenshot tests: Light + Dark themes in empty state.

### Integration Tests (`:app`)

- [ ] `InsightsViewModel` processes use case outputs correctly into UiState.
- [ ] Verify `insights` tab navigation still works after wiring.

## 15. Implementation Order

1. **`:domain:insights` data types** — define `InsightSummary`, `WeeklyInsight`, `DayInsight`, `FocusQualityResult`, `StreakResult`, etc.
2. **`:domain:insights` use cases** — implement all 10 use cases, one by one, with unit tests.
3. **`:core:charts` Vico wrappers** — `PhoneDownBarChart`, `PhoneDownLineChart`.
4. **`:core:charts` heatmap** — `FocusHeatmap` custom Canvas composable.
5. **`:feature:insights` UI** — replace placeholder with real data-driven composables.
6. **`:app` InsightsViewModel + InsightsRoute** — Hilt wiring from use cases to UI state.
7. **`:feature:insights` UI tests** — Compose UI + Paparazzi screenshot tests.
8. **Docs update** — `phase-9-insights-plan.md`, `v1-implementation-plan.md`, `docs/agent-handoff.md`.

## 16. Acceptance Criteria

Phase 9 is complete when:

- [ ] Free users see today + 7 days insights with real session data.
- [ ] Focus Quality score is calculated correctly per the formula and shows the right label.
- [ ] Streak calculation correctly identifies consecutive days.
- [ ] Session history is scrollable and shows real sessions with correct metadata.
- [ ] Pro sections (heatmap, best time, trends, season highlights, export) render with real data behind a non-functional Pro gate stub.
- [ ] Heatmap shows 365 days of focus data with correct 5-level intensity coloring.
- [ ] Best hour and best day aggregations are correct.
- [ ] All 10 domain use cases have passing unit tests (at minimum: core calculation logic).
- [ ] Vico charts render correctly in both light and dark themes.
- [ ] Empty state renders cleanly when no sessions exist.
- [ ] Paparazzi screenshots pass for representative Insight states in both themes.
- [ ] Documentation updated with implementation details and honest verification state.

## 17. Risks And Watchouts

### Pro Gate Dependency
- The Pro paywall and entitlement system doesn't exist yet (Phase 11). We'll render Pro cards with a `[Pro]` badge and stub click handlers. No billing integration in this phase.

### Vico Integration
- Vico has its own theme/model system. We must bridge `PhoneDownDesign.colors` into Vico's `ChartModel` color scheme.
- Vico may require additional dependencies beyond `vico-compose-m3`. Verify the version catalog entry.

### Performance
- Heatmap rendering 365+ tiles on Canvas should be efficient if done in a single draw call.
- Session history should be lazy-loaded (scroll-based pagination via use case parameter).

### Over-Scoping
- Resist adding settings-like configuration to the Insights screen. Keep it read-only analytics.
- Export foundation = a labeled card with Pro badge; no actual file creation in this phase.

## 18. Documentation Updates Required

During implementation, update:

- [ ] `phase-9-insights-plan.md`
- [ ] `v1-implementation-plan.md`
- [ ] `docs/agent-handoff.md`

## 19. Approval Gate

Implementation must not begin until this Phase 9 plan is approved.

Common next steps:

- approve this Phase 9 plan and start implementation
- request updates to the plan before implementation
