# UI/UX Polish & Mockup Alignment Implementation Plan

## Overview

This plan addresses the gap between the current UI implementation and the original design mockups (`ui-mockups/dark-mode.png`, `ui-mockups/light-mode.png`). The goal is to bring the app from ~60-70% mockup fidelity to 90%+ while preserving the calm, premium aesthetic and fixing functional gaps.

### Status Update — 2026-05-06

- [x] All 16 scoped UI/UX polish items have been implemented across 4 execution phases.
- [x] Design-system foundation changes landed: pill buttons, borderless cards, larger timer type, progress-ring dot, bottom-nav icons, Focus settings gear.
- [x] High-impact Focus and Insights work landed: Ready-to-focus ritual, interruption redesign, pause/add-time UI, calendar strip, settings restructuring.
- [x] Rich session-complete screen and completion-copy refresh landed.
- [x] Remaining polish landed: arming countdown, hourly focus chart, historical calendar-day insights.
- [x] Verification completed: `:app:assembleDebug`, domain/app/feature unit tests, and feature Paparazzi verification.
- [ ] `./scripts/check.sh` is still not fully green because of pre-existing/project-convention ktlint findings documented in the handoff.

**Priority framework:**
- **P0** — Must fix before release. Core user flows are broken or incomplete without these.
- **P1** — High impact. Significantly improves UX and mockup alignment.
- **P2** — Polish. Visual refinements that elevate perceived quality.
- **P3** — Nice to have. Enhancements for future iterations.

---

## P0 — Must Fix Before Release

### 1. Add Rich Session Complete Summary Screen

**Current state:** A simple text result ("Clean session completed" / "✓") with a "Done" button.

**Mockup target:** A celebratory completion screen with a large green circle + checkmark, "Great focus!" title, detailed time breakdown rows, and a Clean Session badge.

**Why this matters:** The completion screen is the primary reward moment. A flat completion undermines the psychological payoff of finishing a focus session. The mockup explicitly designed this as a dopamine hit.

#### Implementation Steps

1. **Create new composable** `SessionCompleteScreen` in `:feature:focus` (or extend `ResultState` in `FocusScreen.kt`).
   - Add a large circular container (120dp) with success green background and white checkmark.
   - Add "Great focus!" title (or context-aware variants: "Session completed", "Focus session saved", "Session ended early").
   - Add time breakdown rows:
     - Focus Time: `validFocusSeconds` formatted as MM:SS
     - Penalty Time: `+${penaltySeconds / 60}:00` in danger red if > 0, else `+0:00` in muted gray
     - Total Time: `actualElapsedSeconds` formatted as MM:SS
   - Add "Clean Session" badge with checkmark icon if `clean == true`.
   - Add primary "Done" button (full width, pill-shaped).

2. **Map presentation states** in `FocusPresentationState`:
   - `CompletedClean` → Rich complete screen with "Great focus!", green circle, Clean badge.
   - `CompletedInterrupted` → Rich complete screen with "Session completed", yellow/gray circle, no Clean badge, show interruption count.
   - `EndedEarly` → Summary screen with "Session ended early", no celebration language, show partial stats.
   - `Broken` → Summary screen with "Session broken", red tint, honest messaging.
   - `Invalid` → Simple "Not enough focus time to count" with Done button.

3. **Update `FocusUiState`** to expose completion breakdown data:
   ```kotlin
   data class FocusUiState(
       // ... existing fields ...
       val completedFocusTimeSeconds: Long = 0,
       val completedPenaltySeconds: Long = 0,
       val completedTotalTimeSeconds: Long = 0,
   )
   ```

4. **Update `FocusViewModel`** to populate completion data from `runtimeCoordinator.state` when session enters a terminal state.

5. **Add Paparazzi screenshot tests** for each completion variant (Clean, Interrupted, Early, Broken, Invalid) in light and dark themes.

6. **Add Compose UI test** verifying that tapping "Done" returns to Idle state.

#### Acceptance Criteria
- [ ] Completion screen shows large green circle with white checkmark for clean sessions.
- [ ] "Great focus!" text appears for clean completions.
- [ ] Time breakdown rows (Focus Time, Penalty Time, Total Time) are visible and correctly formatted.
- [ ] Penalty Time is displayed in red/danger color if > 0.
- [ ] Clean Session badge with checkmark appears only when `clean == true`.
- [ ] Screen matches mockup proportions and spacing in both light and dark themes.
- [ ] Screenshot tests pass for all completion variants.

#### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — extend `ResultState`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusUiState.kt` — add completion fields
- `app/src/main/java/phonedown/app/focus/FocusViewModel.kt` — populate completion data
- `feature/focus/src/test/kotlin/phonedown/feature/focus/FocusScreenScreenshotTest.kt` — add completion screenshots
- `feature/focus/src/androidTest/kotlin/phonedown/feature/focus/FocusScreenTest.kt` — add completion UI tests

---

### 2. Fix Bottom Navigation (Icons Instead of Text)

**Current state:** Bottom nav uses text labels ("F", "I", "S" as the first letter of each tab).

**Mockup target:** Icon-based navigation with Focus circle, Insights bar chart, and Settings gear icons.

**Why this matters:** Text-only navigation is ambiguous (what does "I" mean?) and looks unfinished. Icons are universally understood and match the premium aesthetic.

#### Implementation Steps

1. **Add vector icons** to `:core:designsystem` or `:app/src/main/res/drawable/`:
   - `ic_focus.xml` — a simple circle or target icon.
   - `ic_insights.xml` — a bar chart icon.
   - `ic_settings.xml` — a gear icon.

2. **Update `PhoneDownBottomTab.kt`** to include icon references:
   ```kotlin
   data class PhoneDownBottomTab(
       val route: PhoneDownRoute,
       val label: String,
       val iconRes: Int, // or @DrawableRes
   )
   ```

3. **Update `PhoneDownBottomBar`** in `PhoneDownNavHost.kt`:
   - Replace `Text(tab.label.take(1))` with an `Icon` composable using the drawable resource.
   - Keep the text label below the icon.
   - Ensure selected/unselected colors match the mockup (white primary for selected, gray tertiary for unselected).

4. **Verify touch targets** meet 48dp minimum.

5. **Update Paparazzi tests** to reflect new navigation appearance.

#### Acceptance Criteria
- [ ] Bottom navigation displays icons (Focus, Insights, Settings) instead of text letters.
- [ ] Selected tab shows white/primary icon and label.
- [ ] Unselected tabs show muted gray icon and label.
- [ ] Touch targets are at least 48dp.
- [ ] Navigation behavior (tab switching, backstack) remains unchanged.
- [ ] Screenshot tests pass.

#### Files to Modify
- `app/src/main/java/phonedown/app/navigation/PhoneDownBottomTab.kt` — add icon field
- `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt` — update bottom bar composable
- Add 3 drawable XML files to `app/src/main/res/drawable/`
- Update existing Paparazzi baselines

---

### 3. Add Settings Gear Icon to Focus Home Top Bar

**Current state:** Focus home title bar shows only "Phone Down" text.

**Mockup target:** Top bar shows "Phone Down" title + a settings gear icon on the right.

**Why this matters:** Quick access to settings from the Focus tab reduces friction. Users expect a settings affordance on the primary screen.

#### Implementation Steps

1. **Add settings gear icon** to `app/src/main/res/drawable/ic_settings.xml` (or reuse from bottom nav work above).

2. **Update `FocusRoute`** in `:app` to accept an `onSettingsClick` callback.

3. **Update `FocusScreen`** composable to accept an optional `onSettingsClick` parameter.

4. **Update `PhoneDownTopBar`** in `FocusScreen` to show the gear icon button when `onSettingsClick` is provided:
   ```kotlin
   PhoneDownTopBar(
       title = topBarTitle(uiState.presentationState),
       trailing = {
           if (uiState.presentationState == FocusPresentationState.Idle) {
               IconButton(onClick = onSettingsClick) {
                   Icon(painter = painterResource(R.drawable.ic_settings), ...)
               }
           }
       }
   )
   ```

5. **Wire navigation** in `PhoneDownNavHost.kt`:
   - `FocusRoute(onSettingsClick = { navController.navigate(PhoneDownRoute.Settings.path) })`

6. **Add UI test** verifying gear icon navigates to Settings.

#### Acceptance Criteria
- [ ] Settings gear icon is visible on Focus home (Idle state) only.
- [ ] Tapping gear icon navigates to Settings screen.
- [ ] Gear icon is hidden during active session states.
- [ ] Icon color matches theme (white in dark, black in light).

#### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — accept settings callback
- `app/src/main/java/phonedown/app/focus/FocusRoute.kt` — wire callback
- `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt` — connect to nav
- `feature/focus/src/androidTest/kotlin/phonedown/feature/focus/FocusScreenTest.kt` — add test

---

## P1 — High Impact

### 4. Add "Ready to Focus?" Pre-Session Screen

**Current state:** Tapping "Start Focus" immediately creates a session and shows "Place phone down to begin."

**Mockup target:** A dedicated pre-session instructional screen with a 3D phone illustration, 3-step numbered list, and a "Phone Down to begin" progress indicator.

**Why this matters:** This screen teaches the physical ritual visually. First-time users see exactly what to do before the timer starts. It reduces confusion and abandonment.

#### Implementation Steps

1. **Create `PreSessionScreen` composable** in `:feature:focus`:
   - Title: "Ready to focus?"
   - Subtitle/instruction list:
     1. Tap Start Focus
     2. Place your phone face down
     3. Stay still and focus
   - Phone illustration (placeholder vector or image asset).
   - Bottom card: "Phone Down to begin" with a subtle progress line or text "We'll start in 3 seconds."
   - Back arrow (←) to cancel and return to Idle.

2. **Add `FocusPresentationState.ReadyToFocus`** enum value.

3. **Update session flow** in `FocusViewModel`:
   - `StartClicked` event should transition to `ReadyToFocus` state (local UI state only, do NOT start the session yet).
   - Auto-transition to `WaitingForPhoneDown` after a short delay (e.g., 500ms) or when the user places the phone down.
   - Alternatively, keep it simple: show Ready screen, then on next valid sensor reading or after 1s auto-advance to WaitingForPhoneDown.

4. **Wire to runtime coordinator**:
   - Only call `runtimeCoordinator.ensureSessionStarted()` when transitioning from Ready → WaitingForPhoneDown, not on StartClicked.

5. **Add illustration asset** — either a vector illustration of a phone face-down or a placeholder that can be replaced with final art later.

6. **Add Paparazzi and UI tests**.

#### Acceptance Criteria
- [ ] Tapping "Start Focus" shows the "Ready to focus?" screen instead of immediately starting the session.
- [ ] Screen displays a phone illustration and 3-step instruction list.
- [ ] Back arrow cancels and returns to Idle.
- [ ] Screen auto-advances to "Waiting for phone down" after a brief moment or upon phone-down detection.
- [ ] Session is not created until the Ready screen is dismissed.
- [ ] Matches mockup layout and typography in both themes.

#### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — add Ready state
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusUiState.kt` — add `ReadyToFocus`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusEvent.kt` — add `ReadyDismissed`, `ReadyBackClicked`
- `app/src/main/java/phonedown/app/focus/FocusViewModel.kt` — handle ready flow
- `app/src/main/java/phonedown/app/focus/FocusRoute.kt` — wire events
- Add illustration asset to `feature/focus/src/main/res/drawable/`
- Update tests

---

### 5. Add Pause & Add Time Controls During Active Session

**Current state:** Only "End" button is shown during active and paused states.

**Mockup target:** Three action buttons during active sessions: End (square), Pause (pause icon), Add Time (plus icon). During pause: End and Add Time.

**Why this matters:** Pause is a core Pomodoro affordance. Add Time lets users extend when they're in flow. Removing both forces users to end sessions prematurely, hurting retention.

**Decision note:** Per `v1-implementation-plan.md`, Pause/Add Time was scoped down. This plan **re-introduces Pause** as a first-class control. Add Time is included as a UI affordance that can be stubbed functionally if needed.

#### Implementation Steps

1. **Update `FocusPresentationState`**:
   - Active state keeps Pause button.
   - Paused states show Resume button instead of Pause.

2. **Update `InProgressActions` in `FocusScreen.kt`**:
   - Show 3 buttons in a row for Active state: End | Pause | Add Time.
   - Show 2 buttons for Paused state: End | Resume.
   - Use `PhoneDownIconButton` or create new circular icon buttons matching mockup style.

3. **Add new events** to `FocusEvent`:
   ```kotlin
   data object PauseClicked
   data object ResumeClicked
   data object AddTimeClicked
   ```

4. **Update `FocusViewModel`**:
   - `PauseClicked` → sends `ManualPauseRequested` to session engine (or maps to a new `SessionInput`).
   - `ResumeClicked` → transitions back to Active when phone is valid.
   - `AddTimeClicked` → shows a bottom sheet to add 5/10/15 minutes (or stubbed for V1).

5. **Update `SessionEngine`**:
   - Add `SessionInput.ManualPauseRequested`.
   - Handle pause: transition from Active → PausedByPickup (or new `PausedByUser`), mark `clean = false`.
   - Handle resume: transition back to Active if phone is still valid.

6. **Update `ActiveSessionRuntimeCoordinator`**:
   - Add `onManualPause()` and `onManualResume()` methods.

7. **Add interruption stats row** below action buttons:
   - Left: "Interruptions: ${interruptionCount}"
   - Right: "Clean" with checkmark (if clean) or hidden (if not)

8. **Add UI tests** verifying Pause/Resume cycle.

#### Acceptance Criteria
- [ ] Active session shows End, Pause, and Add Time buttons.
- [ ] Paused session shows End and Resume buttons.
- [ ] Tapping Pause transitions to paused state, timer stops, clean status is lost.
- [ ] Tapping Resume returns to Active if phone is face-down valid.
- [ ] Interruptions count and Clean status row is visible during active/paused states.
- [ ] Add Time shows a selection UI (even if stubbed).

#### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusEvent.kt` — add pause/resume/addTime events
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusUiState.kt` — add pause-related states
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — update InProgressActions
- `domain/session/src/main/kotlin/phonedown/domain/session/SessionInput.kt` — add ManualPauseRequested
- `domain/session/src/main/kotlin/phonedown/domain/session/SessionEngine.kt` — handle pause/resume
- `app/src/main/java/phonedown/app/runtime/ActiveSessionRuntimeCoordinator.kt` — add pause/resume methods
- `app/src/main/java/phonedown/app/focus/FocusViewModel.kt` — handle events
- `app/src/main/java/phonedown/app/focus/FocusRoute.kt` — wire callbacks
- Update tests across all affected modules

---

### 6. Improve Interruption Screen

**Current state:** Generic "Focus paused" text, no countdown, no illustration.

**Mockup target:** "Phone Picked Up" title in red, phone illustration with pickup arrow, red countdown timer showing grace period, penalty text.

**Why this matters:** The interruption screen must create urgency. Users need to know *why* focus stopped, *how long* they have before penalty, and *what* to do. The mockup's red countdown is the key visual anchor.

#### Implementation Steps

1. **Update `FocusScreen.kt` — PausedByPickup state**:
   - Change title from "Focus paused" to "Phone Picked Up" in red/danger color.
   - Add phone illustration (reuse from Ready screen or create new pickup variant).
   - Add red countdown timer showing grace period remaining.
   - Keep "Keep your phone down to continue" subtitle.
   - Show penalty text in red if grace period has been exceeded.

2. **Add grace period countdown logic** to `FocusViewModel`:
   - Track `interruptionStartedAt` and compute remaining grace seconds.
   - Expose `graceRemainingSeconds: Long` in `FocusUiState`.

3. **Style the countdown**:
   - Large red text (`00:08` format).
   - Color transitions from warning yellow (first 2s) to danger red (last 3s).

4. **Add phone pickup illustration** — a vector showing phone being lifted with an upward arrow.

5. **Add Paparazzi tests** for interruption state in both themes.

#### Acceptance Criteria
- [ ] Interruption screen shows "Phone Picked Up" title in red.
- [ ] Phone pickup illustration is visible.
- [ ] Red countdown timer shows grace period remaining (5 → 0).
- [ ] Countdown color transitions from yellow to red.
- [ ] Penalty text appears in red after grace period expires.
- [ ] Mockup proportions and spacing are matched.

#### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — enhance PausedByPickup UI
- `feature/focus/src/main/kotlin/phonedown/feature/focus/state/FocusUiState.kt` — add grace countdown field
- `app/src/main/java/phonedown/app/focus/FocusViewModel.kt` — compute grace countdown
- Add illustration asset
- Update screenshot tests

---

### 7. Add Calendar Strip to Insights Top

**Current state:** Insights starts with "Today" section directly.

**Mockup target:** A horizontal calendar strip (Mon-Sun with dates) sits at the top of Insights, with the current day highlighted.

**Why this matters:** Quick day navigation and week-at-a-glance is a standard analytics pattern. It gives users immediate temporal context.

#### Implementation Steps

1. **Create `InsightsCalendarStrip` composable** in `:feature:insights`:
   - Show 7 days: Mon, Tue, Wed, Thu, Fri, Sat, Sun.
   - Each day shows short day name (Mon) and date number (13).
   - Current day is highlighted with a filled circle or pill background.
   - Past days are clickable (tap to view that day's summary).
   - Future days are muted/disabled.

2. **Add to `InsightsContent.kt`**:
   - Place calendar strip as the first item in the LazyColumn, above "Today" section.
   - Or make it a sticky header if scrolling is long.

3. **Add interaction**:
   - Tapping a past day triggers `onDaySelected(date)` callback.
   - For V1, this can scroll the "Today" section to show that day's data, or simply highlight the selected day.

4. **Style**:
   - Compact height (~56dp).
   - Selected day: filled pill with progress/surface color.
   - Today indicator: small dot or bold text.

5. **Add UI tests**.

#### Acceptance Criteria
- [ ] Calendar strip shows current week (Mon-Sun) with dates.
- [ ] Current day is visually highlighted.
- [ ] Tapping a day updates the Today section to show that day's data.
- [ ] Strip is visible at the top of Insights in both themes.
- [ ] Matches mockup compact styling.

#### Files to Modify
- `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt` — add calendar strip item
- `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsCalendarStrip.kt` — new composable
- `app/src/main/java/phonedown/app/insights/InsightsViewModel.kt` — handle day selection
- `app/src/main/java/phonedown/app/insights/InsightsRoute.kt` — wire callback
- Update tests

---

### 8. Restructure Settings into 2-3 Clean Sections

**Current state:** 6 sections (Timer, Preferences, Account & Backup, Pro, Privacy, About) feel cluttered.

**Mockup target:** 2 primary sections: **Focus** (timer settings) and **Account** (sign-in, backup, Pro).

**Why this matters:** Fewer sections reduce cognitive load. The mockup's settings screen is scannable in 2 seconds. The current screen requires scrolling and parsing 6 headers.

#### Implementation Steps

1. **Restructure `SettingsScreen.kt`**:
   - **Section 1: Focus**
     - Default Duration (with chevron)
     - Duration Presets (display only)
     - Custom Duration (Pro-gated)
     - Sounds toggle
     - Haptics toggle
     - Theme selector
     - Start Delay (display only)
   - **Section 2: Account**
     - Google Account (sign-in/manage)
     - Phone Down Pro (upgrade/manage)
     - Backup & Restore (status/trigger)
     - Auto Backup toggle (Pro + signed in)
   - **Section 3: About** (or merge into Account)
     - Privacy Policy
     - Terms of Service
     - Support
     - Version
     - Delete All Data (moved to bottom as destructive action)

2. **Remove standalone Pro and Privacy sections** — fold their rows into Account and About.

3. **Update row styling**:
   - Add chevron (→) to navigable rows per mockup.
   - Keep toggle rows for Sounds/Haptics/Auto Backup.
   - Keep theme selector compact.

4. **Update Paparazzi baselines**.

#### Acceptance Criteria
- [ ] Settings screen has 2-3 clearly defined sections.
- [ ] Focus section contains all timer and preference settings.
- [ ] Account section contains auth, Pro, and backup rows.
- [ ] Navigable rows show chevron indicator.
- [ ] Screen is scannable without excessive scrolling.
- [ ] Screenshot tests pass.

#### Files to Modify
- `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsScreen.kt` — restructure sections
- `feature/settings/src/test/kotlin/phonedown/feature/settings/SettingsScreenScreenshotTest.kt` — update baselines

---

## P2 — Polish

### 9. Add Hourly Focus Chart to Insights

**Current state:** Insights shows daily totals but not *when* during the day the user focused.

**Mockup target:** An hourly bar chart ("Focus Time (h)") showing focus distribution across hours of the day.

#### Implementation Steps

1. **Create `PhoneDownHourlyChart` composable** in `:core:charts`:
   - X-axis: hours (12 AM, 6 AM, 12 PM, 6 PM, 12 AM).
   - Y-axis: focus minutes.
   - Bars for each hour block with focus time.
   - Lightweight Canvas implementation matching existing chart style.

2. **Add to `InsightsContent.kt`**:
   - Place below Today card, above or alongside Weekly chart.
   - Gate behind having at least one day's data.

3. **Add `GetHourlyFocusUseCase`** in `:domain:insights`:
   - Aggregate `validFocusSeconds` by hour of day from today's sessions.
   - Return `List<HourFocus>` with hour and minutes.

4. **Update `InsightsViewModel`** to collect hourly data.

5. **Add tests**.

#### Acceptance Criteria
- [ ] Hourly chart is visible in Insights when data exists.
- [ ] Chart shows focus time distributed across hours of the current day.
- [ ] Style matches existing bar chart (purple/blue bars, minimal axes).
- [ ] Empty state is handled gracefully.

#### Files to Modify
- `core/charts/src/main/kotlin/phonedown/core/charts/PhoneDownHourlyChart.kt` — new composable
- `domain/insights/src/main/kotlin/phonedown/domain/insights/GetHourlyFocusUseCase.kt` — new use case
- `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt` — integrate
- `app/src/main/java/phonedown/app/insights/InsightsViewModel.kt` — collect hourly data
- Update tests

---

### 10. Add Progress Ring Position Dot Indicator

**Current state:** Progress ring is a simple filled arc with no position marker.

**Mockup target:** A small dot or knob on the ring indicating the current progress position.

#### Implementation Steps

1. **Update `PhoneDownProgressRing` in `PhoneDownComponents.kt`**:
   - After drawing the arc, compute the angle of the progress tip.
   - Draw a small circle (dot) at the tip of the progress arc.
   - Dot color: same as progress color or slightly brighter.
   - Dot size: ~10-12dp.

2. **Animation**:
   - Dot should animate smoothly along with the progress arc.
   - No additional animation needed beyond existing `animateFloatAsState`.

#### Acceptance Criteria
- [ ] A small dot appears at the tip of the progress arc.
- [ ] Dot moves smoothly as progress updates.
- [ ] Dot is visible in all states where progress ring is shown.
- [ ] Dot color matches the progress accent.

#### Files to Modify
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownComponents.kt` — enhance `PhoneDownProgressRing`
- Update Paparazzi baselines for all screens using progress ring

---

### 11. Make Buttons More Pill-Shaped

**Current state:** Buttons use `shapes.large` = 8dp corner radius. They look like rectangles with slight rounding.

**Mockup target:** Buttons are distinctly pill-shaped (much more rounded, ~26dp+ radius or fully rounded).

#### Implementation Steps

1. **Update `PhoneDownShapes`** in `PhoneDownFoundation.kt`:
   - Change `large` and `extraLarge` shapes to use `RoundedCornerShape(26.dp)` or `CircleShape` for full pill.
   - Or create a dedicated `PhoneDownButtonShape = RoundedCornerShape(26.dp)`.

2. **Update `PhoneDownButton`** in `PhoneDownComponents.kt`:
   - Use the new pill shape.
   - Ensure height remains 52dp for comfortable touch target.

3. **Verify** that cards and other surfaces don't unintentionally become too rounded.

#### Acceptance Criteria
- [ ] Primary buttons (Start Focus, Done) are distinctly pill-shaped.
- [ ] Button height remains 52dp.
- [ ] No regression in card or surface corner radii.
- [ ] Matches mockup button proportions.

#### Files to Modify
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownFoundation.kt` — update shapes
- Update all Paparazzi baselines

---

### 12. Reduce Card Border Visibility

**Current state:** Cards have a 1dp border in `borderSubtle` color that is somewhat visible.

**Mockup target:** Cards appear to have no visible border or an extremely faint one. The separation comes from surface color difference alone.

#### Implementation Steps

1. **Update `PhoneDownCard`** in `PhoneDownComponents.kt`:
   - Reduce border width from 1.dp to 0.5.dp, or remove border entirely.
   - Ensure `surfaceRaised` color provides enough contrast against `background` to define card edges without borders.
   - In dark mode, the border is more acceptable; in light mode, it may be too prominent.

2. **Alternative:** Make border color even more subtle by blending it closer to the background color.

#### Acceptance Criteria
- [ ] Card borders are barely perceptible or completely absent.
- [ ] Card edges are still distinguishable via surface color contrast.
- [ ] Both light and dark themes look clean.

#### Files to Modify
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownComponents.kt` — adjust `PhoneDownCard`
- Optionally adjust `borderSubtle` colors in `PhoneDownTheme.kt`

---

### 13. Increase Timer Text Size

**Current state:** Timer uses `displayLarge` at 44sp.

**Mockup target:** Timer text appears larger and more prominent (~48-52sp).

#### Implementation Steps

1. **Add a dedicated timer text style** to `PhoneDownTypography`:
   ```kotlin
   val PhoneDownTimerText = TextStyle(
       fontSize = 52.sp,
       lineHeight = 60.sp,
       fontWeight = FontWeight.Light,
       letterSpacing = (-0.5).sp,
   )
   ```

2. **Apply** to timer text in `FocusRingSection` instead of `MaterialTheme.typography.displayLarge`.

3. **Verify** no overflow on small screens (test with 320dp width).

#### Acceptance Criteria
- [ ] Timer text is visibly larger than current 44sp.
- [ ] No text overflow on small devices.
- [ ] Timer remains perfectly centered in the ring.

#### Files to Modify
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownFoundation.kt` — add timer text style
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — apply to timer

---

### 14. Add Completion Celebration Copy

**Current state:** Completion uses neutral/functional language ("Clean session completed", "Session completed").

**Mockup target:** Celebratory language ("Great focus!", "Well done!", "Session complete").

#### Implementation Steps

1. **Create a completion copy map** in `FocusScreen.kt` or a dedicated strings resource:
   - `CompletedClean` → "Great focus!"
   - `CompletedInterrupted` → "Session complete"
   - `EndedEarly` → "Session ended early"
   - `Broken` → "Session broken"
   - `Invalid` → "Not enough focus time to count"

2. **Style the title**:
   - Clean completions: large, bold, success-colored text.
   - Others: appropriately muted but still prominent.

3. **Combine with P0 Item 1** (Rich Complete Screen) — this copy is part of that work.

#### Acceptance Criteria
- [ ] Clean completions show celebratory language.
- [ ] Copy remains honest and non-shaming for interrupted/broken sessions.
- [ ] Language matches the calm, premium brand voice.

#### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — update `ResultState` titles

---

## P3 — Nice to Have

### 15. Animate Timer Ring During Arming Countdown

**Current state:** Arming shows static "Hold still..." text.

**Mockup target:** A subtle progress indicator or countdown animation (3, 2, 1) during arming.

#### Implementation Steps
1. Add a 3-second animated countdown overlay or ring fill during arming.
2. Show "3", "2", "1" in large text or fill the ring progressively.

#### Acceptance Criteria
- [ ] Arming state has a clear visual countdown.
- [ ] Animation is calm, not frantic or game-like.

---

### 16. Calendar Day Selection for Historical Insights

**Current state:** Calendar strip is display-only.

**Mockup target:** Tapping a day shows that day's insights.

#### Implementation Steps
1. When a day is tapped in the calendar strip, fetch sessions for that date.
2. Update the "Today" section temporarily to show that day's data.
3. Add a "Back to Today" affordance.

#### Acceptance Criteria
- [ ] Tapping a past day updates insights to show that day's summary.
- [ ] Users can return to today's view.

---

## Implementation Order Recommendation

To minimize rework and merge conflicts, implement in this order:

1. **P0 Item 2** — Bottom navigation icons (isolated, no business logic changes).
2. **P0 Item 3** — Settings gear icon (isolated UI change).
3. **P2 Items 10-13** — Design system polish (shapes, borders, timer size, ring dot) — affects all screens, do early.
4. **P0 Item 1** — Rich completion screen (builds on design system changes).
5. **P1 Item 6** — Interruption screen improvements (uses same illustration assets as ready screen).
6. **P1 Item 4** — Ready to focus screen (shares illustration patterns with interruption screen).
7. **P1 Item 5** — Pause & Add Time (requires domain engine changes, most complex).
8. **P1 Item 7** — Calendar strip (isolated to Insights).
9. **P1 Item 8** — Settings restructuring (isolated to Settings).
10. **P2 Item 9** — Hourly chart (isolated to Insights).
11. **P2 Item 14** — Completion copy (text-only, can be done anytime).
12. **P3 Items** — Deferred to V1.1 or later sprints.

---

## Verification Checklist

After implementation, run:

```bash
# Build
./gradlew :app:assembleDebug

# Unit tests
./gradlew :domain:session:test :domain:insights:test :app:testDebugUnitTest

# Screenshot tests (record new baselines)
./gradlew :feature:focus:recordPaparazziDebug
./gradlew :feature:insights:recordPaparazziDebug
./gradlew :feature:settings:recordPaparazziDebug
./gradlew :feature:onboarding:recordPaparazziDebug

# Verify new baselines
./gradlew :feature:focus:verifyPaparazziDebug
./gradlew :feature:insights:verifyPaparazziDebug
./gradlew :feature:settings:verifyPaparazziDebug
./gradlew :feature:onboarding:verifyPaparazziDebug

# UI test APK compilation
./gradlew :feature:focus:assembleDebugAndroidTest
./gradlew :feature:insights:assembleDebugAndroidTest
./gradlew :feature:settings:assembleDebugAndroidTest

# Full check script
./scripts/check.sh
```

---

## Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Adding Pause changes session engine state machine, introducing regressions | Medium | High | Write comprehensive unit tests for pause/resume transitions before UI work. |
| Illustration assets delay delivery if final art is not ready | Medium | Medium | Use placeholder vector shapes that match proportions; swap in final art later. |
| Bottom nav icon colors don't pass contrast checks | Low | Medium | Test on accessibility scanner; adjust icon tints if needed. |
| Timer text size increase causes overflow on small screens | Low | Medium | Test on smallest supported device (320dp width) before merging. |
| Paparazzi baseline regeneration is time-consuming | High | Low | Batch all visual changes together to minimize regeneration cycles. |

---

## Documentation Updates

After implementation, update the following documents:

1. **`v1-implementation-plan.md`** — Mark P0 and P1 items as complete, document any intentional deviations from mockups.
2. **`docs/design-system.md`** — Document new components (`PhoneDownTimerText`, `InsightsCalendarStrip`, `SessionCompleteScreen`), updated shapes, and icon usage.
3. **`docs/agent-handoff.md`** — Summarize what changed, which files were modified, and any new patterns introduced.
4. This plan file (`ui-polish-implementation-plan.md`) — Mark checklist items complete as they are finished.

---

*Plan created: 2026-05-05*
*Scope: V1 UI/UX polish and mockup alignment*
*Target: 90%+ mockup fidelity before release*

## Implementation Outcome

Implemented in four phases:

1. Phase 1 — visual foundation and navigation polish.
2. Phase 2 — high-impact Focus, Insights, and Settings UX work.
3. Phase 3 — rich completion experience and celebratory copy.
4. Phase 4 — remaining polish and insight-depth features.

Intentional deviations from the original plan:

- Pause and Add Time remain UI-first controls; full domain-engine wiring is still deferred.
- Ktlint remains noisy in a few modules because the repo uses deliberate PascalCase composable naming and already had formatting-style disagreements before this polish pass.
