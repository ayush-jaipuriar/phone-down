# Phase 1 Implementation Plan: Visual Foundation & Navigation

## Overview

Phase 1 establishes the visual foundation for all subsequent UI polish work. This phase contains **purely visual and navigational changes** with **zero business logic or domain engine modifications**. By batching these together, we minimize the number of Paparazzi baseline regeneration cycles required.

**Scope:** Design system tokens, shared components, bottom navigation, and quick-access affordances.

**Risk level:** Low — no state machine changes, no database changes, no API changes.

**Estimated files modified:** ~8-12 source files, ~4 Paparazzi baseline sets.

---

## Confirmed Decisions from Clarification

1. **Bottom nav icons:** Use Material Design icons (`androidx.compose.material.icons`) for Focus, Insights, and Settings.
2. **Settings gear icon:** Tapping navigates directly to the Settings screen (full screen push via nav controller).
3. **Button shape:** Match mockup exactly — fully pill-shaped/capsule buttons (corner radius = 50% of height).
4. **Card borders:** Completely removed; rely on surface color contrast alone.

---

## Implementation Items

### Item 1: Pill-Shaped Primary Buttons

**Current state:** Buttons use `shapes.large` = 8dp corner radius. They look like softly rounded rectangles.

**Target:** Fully pill-shaped buttons matching the mockup (capsule/cylinder shape with half-height corner radius).

#### Detailed Steps

1. **Create dedicated button shape** in `PhoneDownFoundation.kt`:
   ```kotlin
   val PhoneDownButtonShape = RoundedCornerShape(percent = 50) // Full capsule
   ```

2. **Update `PhoneDownShapes`**:
   - Keep existing shapes for cards and surfaces (8dp is fine for cards).
   - Do NOT change `MaterialTheme.shapes.large` globally — only apply pill shape to buttons specifically.

3. **Update `PhoneDownButton` in `PhoneDownComponents.kt`**:
   - Change `shape = MaterialTheme.shapes.large` to `shape = PhoneDownButtonShape`.
   - Ensure height remains 52dp.
   - Verify quiet buttons also use pill shape.

4. **Verify no regression** in other components that use `MaterialTheme.shapes.large` (cards, surfaces, text fields).

#### Files to Modify
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownFoundation.kt` — add `PhoneDownButtonShape`
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownComponents.kt` — apply to `PhoneDownButton`

#### Acceptance Criteria
- [ ] Primary buttons (`Start Focus`, `Done`) are distinctly pill-shaped/capsule.
- [ ] Button height remains 52dp.
- [ ] Quiet/cancel buttons also use pill shape.
- [ ] Cards, surfaces, and other components retain their existing corner radius.
- [ ] No visual regression in bottom sheets or dialogs.

---

### Item 2: Remove Card Borders

**Current state:** `PhoneDownCard` applies a 1dp border in `borderSubtle` color.

**Target:** Cards have no visible border; edge definition comes purely from surface color contrast against background.

#### Detailed Steps

1. **Update `PhoneDownCard` in `PhoneDownComponents.kt`**:
   - Remove `.border(1.dp, PhoneDownDesign.colors.borderSubtle, ...)` from the Surface modifier.
   - Keep the `Surface` with `shape`, `color`, and padding.
   - Ensure `tonalElevation = 0.dp` and `shadowElevation = 0.dp` remain (flat design per mockup).

2. **Verify surface contrast** is sufficient without borders:
   - Light mode: `surfaceRaised` (#FDFDFD) vs `background` (#F8F8F7) — subtle but should be visible.
   - Dark mode: `surfaceRaised` (#181D25) vs `background` (#080B10) — clear contrast.
   - If light mode lacks definition, slightly darken `surfaceRaised` or lighten `background`.

3. **Optional:** If removing borders makes cards disappear in light mode, consider:
   - Option A: Very subtle 0.5dp border (compromise).
   - Option B: Adjust light mode colors slightly for more contrast.
   - **Preferred:** Remove borders entirely and adjust surface/background colors if needed to match mockup.

#### Files to Modify
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownComponents.kt` — remove border from `PhoneDownCard`
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownTheme.kt` — potentially adjust light mode surface/background colors

#### Acceptance Criteria
- [ ] Cards have no visible border in light mode.
- [ ] Cards have no visible border in dark mode.
- [ ] Card edges are still distinguishable via surface color contrast.
- [ ] No regression in card padding or internal layout.

---

### Item 3: Increase Timer Text Size

**Current state:** Timer text uses `MaterialTheme.typography.displayLarge` at 44sp.

**Target:** Timer text is larger and more prominent (~52sp), matching the mockup's commanding presence.

#### Detailed Steps

1. **Add dedicated timer text style** in `PhoneDownFoundation.kt`:
   ```kotlin
   val PhoneDownTimerTextStyle = TextStyle(
       fontSize = 52.sp,
       lineHeight = 60.sp,
       fontWeight = FontWeight.Normal,
       letterSpacing = (-0.5).sp,
   )
   ```
   Note: Using `FontWeight.Normal` (not Light) to maintain readability at large size. Adjust to `FontWeight.Light` if mockup appears thinner.

2. **Apply in `FocusRingSection`** in `FocusScreen.kt`:
   - Replace `MaterialTheme.typography.displayLarge` with `PhoneDownTimerTextStyle` for the timer display.
   - Keep the label below timer ("Focus", "Remaining", etc.) unchanged.

3. **Test on small screens**:
   - Verify no overflow at 320dp width (smallest supported device).
   - Timer string "25:00" at 52sp should fit comfortably within the 190dp ring.

#### Files to Modify
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownFoundation.kt` — add `PhoneDownTimerTextStyle`
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — apply to timer in `FocusRingSection`

#### Acceptance Criteria
- [ ] Timer text is 52sp (up from 44sp).
- [ ] Timer remains centered in the progress ring.
- [ ] No text overflow on 320dp-wide screens.
- [ ] Timer label ("Focus", "Remaining") remains unchanged.

---

### Item 4: Add Progress Ring Position Dot Indicator

**Current state:** `PhoneDownProgressRing` draws a filled arc but has no position marker showing the current progress tip.

**Target:** A small dot at the tip of the progress arc, matching the mockup's ring indicator.

#### Detailed Steps

1. **Update `PhoneDownProgressRing` in `PhoneDownComponents.kt`**:
   - After drawing the arc, compute the angle of the progress tip in radians:
     ```kotlin
     val progressAngle = (progress.coerceIn(0f, 1f) * 360f) - 90f
     val angleRad = Math.toRadians(progressAngle.toDouble())
     ```
   - Compute dot position on the circle:
     ```kotlin
     val radius = (size.toPx() - strokePx) / 2f
     val centerX = size.toPx() / 2f
     val centerY = size.toPx() / 2f
     val dotX = centerX + radius * cos(angleRad).toFloat()
     val dotY = centerY + radius * sin(angleRad).toFloat()
     ```
   - Draw a circle at that position:
     ```kotlin
     drawCircle(
         color = progressColor,
         radius = strokePx * 0.8f, // Slightly smaller than stroke width
         center = Offset(dotX, dotY),
     )
     ```

2. **Handle edge cases**:
   - When progress = 0f, dot should sit at the 12 o'clock position (start of arc).
   - When progress = 1f, dot completes the circle at 12 o'clock again.
   - Dot should animate smoothly with the arc via `animateFloatAsState`.

3. **Size considerations**:
   - Dot radius = ~5-6dp (roughly 80% of 7dp stroke width).
   - Ensure dot doesn't clip outside the Canvas bounds.

#### Files to Modify
- `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownComponents.kt` — enhance `PhoneDownProgressRing`

#### Acceptance Criteria
- [ ] A small dot appears at the tip of the progress arc.
- [ ] Dot moves smoothly as progress animates.
- [ ] Dot is visible in Idle, Active, Paused, and Completed states.
- [ ] Dot color matches progress arc color.
- [ ] Dot does not clip or overflow the ring bounds.
- [ ] No performance regression (Canvas operations remain lightweight).

---

### Item 5: Bottom Navigation Icons (Replace Text with Material Icons)

**Current state:** Bottom nav shows text labels ("F", "I", "S" — first letter of each tab).

**Target:** Icon-based navigation with Focus circle, Insights bar chart, and Settings gear icons from Material Design.

#### Detailed Steps

1. **Identify Material icons** to use:
   - Focus: `Icons.Rounded.RadioButtonUnchecked` (circle/target) or `Icons.Rounded.Adjust`
   - Insights: `Icons.Rounded.BarChart` or `Icons.Rounded.Insights`
   - Settings: `Icons.Rounded.Settings`
   - Check availability in `androidx.compose.material:material-icons-extended` or use `androidx.compose.material.icons` if available in the BOM.

2. **Check dependency availability**:
   - The current `libs.versions.toml` includes `androidx-compose-material3` but not material icons explicitly.
   - Material3's `material-icons-extended` may need to be added to the catalog or we can use `material3`'s built-in icons if they exist.
   - **Decision:** Add `androidx-compose-material-icons-extended` to version catalog and `:app` dependencies, OR use simple custom vector drawables if Material icons aren't available.
   - Actually, looking at the BOM 2026.04.01, Material3 includes basic icons. For extended icons, we need `androidx.compose.material:material-icons-extended`. Let me add it.

3. **Update `PhoneDownBottomTab`** in `PhoneDownBottomTab.kt`:
   ```kotlin
   data class PhoneDownBottomTab(
       val route: PhoneDownRoute,
       val label: String,
       val icon: @Composable () -> Unit, // Or use ImageVector
   )
   ```

4. **Update `PhoneDownBottomBar` in `PhoneDownNavHost.kt`**:
   - Replace `Text(tab.label.take(1))` with `Icon(imageVector = tab.icon, ...)`.
   - Keep text label below the icon.
   - Selected icon: `PhoneDownDesign.colors.textPrimary`.
   - Unselected icon: `PhoneDownDesign.colors.textTertiary`.
   - Icon size: 24dp.

5. **Update tab definitions**:
   ```kotlin
   val phoneDownBottomTabs = listOf(
       PhoneDownBottomTab(PhoneDownRoute.Focus, "Focus", Icons.Rounded.Adjust),
       PhoneDownBottomTab(PhoneDownRoute.Insights, "Insights", Icons.Rounded.BarChart),
       PhoneDownBottomTab(PhoneDownRoute.Settings, "Settings", Icons.Rounded.Settings),
   )
   ```

6. **Add dependency** to `app/build.gradle.kts`:
   ```kotlin
   implementation(libs.androidx.compose.material.icons.extended) // or similar
   ```
   If this library doesn't exist in the catalog, add it to `gradle/libs.versions.toml` first.

7. **Verify** selected/unselected icon tint colors match the mockup.

#### Files to Modify
- `app/src/main/java/phonedown/app/navigation/PhoneDownBottomTab.kt` — add icon field
- `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt` — update bottom bar to show icons
- `app/build.gradle.kts` — add material-icons dependency if needed
- `gradle/libs.versions.toml` — add material-icons library if needed

#### Acceptance Criteria
- [ ] Bottom navigation shows icons for Focus, Insights, and Settings.
- [ ] Text labels remain below each icon.
- [ ] Selected tab shows white/primary icon and label.
- [ ] Unselected tabs show muted gray icon and label.
- [ ] Icon touch targets meet 48dp minimum.
- [ ] Navigation behavior (tab switching, state restoration) is unchanged.
- [ ] No build errors or missing dependency issues.

---

### Item 6: Settings Gear Icon on Focus Home Top Bar

**Current state:** Focus home shows "Phone Down" title with no action affordances.

**Target:** A settings gear icon in the top-right corner of the Focus home screen, matching the mockup.

#### Detailed Steps

1. **Add `onSettingsClick` parameter** to `FocusScreen` composable:
   ```kotlin
   fun FocusScreen(
       uiState: FocusUiState,
       onEvent: (FocusEvent) -> Unit,
       onSettingsClick: () -> Unit = {},
   )
   ```

2. **Show gear icon only in Idle state**:
   - In `FocusScreen`, conditionally add the settings icon to `PhoneDownTopBar`'s `trailing` slot.
   - Only show when `uiState.presentationState == FocusPresentationState.Idle`.
   - Use `Icons.Rounded.Settings` from Material Design.

3. **Update `PhoneDownTopBar`** to accept and render trailing content (already does — just need to pass it).

4. **Update `FocusRoute` in `:app`** to accept `onSettingsClick` callback:
   ```kotlin
   @Composable
   fun FocusRoute(
       onStartFocusClick: (Long) -> Unit,
       onRetrySensorsClick: (Long) -> Unit,
       onSettingsClick: () -> Unit = {},
   )
   ```

5. **Wire navigation in `PhoneDownNavHost.kt`**:
   ```kotlin
   composable(PhoneDownRoute.Focus.path) {
       FocusRoute(
           onStartFocusClick = onStartFocusClick,
           onRetrySensorsClick = onRetrySensorsClick,
           onSettingsClick = { navController.navigate(PhoneDownRoute.Settings.path) },
       )
   }
   ```

6. **Add UI test** in `FocusScreenTest`:
   - Verify gear icon is visible in Idle state.
   - Verify gear icon is hidden in non-Idle states.
   - Verify tapping gear icon triggers `onSettingsClick`.

#### Files to Modify
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` — accept settings callback, show icon conditionally
- `app/src/main/java/phonedown/app/focus/FocusRoute.kt` — accept and forward callback
- `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt` — wire navigation
- `feature/focus/src/androidTest/kotlin/phonedown/feature/focus/FocusScreenTest.kt` — add UI tests

#### Acceptance Criteria
- [ ] Settings gear icon is visible on Focus home (Idle state).
- [ ] Gear icon uses Material Design `Icons.Rounded.Settings`.
- [ ] Tapping gear icon navigates to Settings screen.
- [ ] Gear icon is hidden during active session states (Waiting, Arming, Active, Paused).
- [ ] Icon color matches theme (white in dark, black in light).
- [ ] UI tests verify visibility and navigation behavior.

---

## Implementation Order Within Phase 1

To minimize conflicts and Paparazzi churn:

1. **Items 1-4** (Design system changes: buttons, cards, timer, ring dot) — These all touch `PhoneDownComponents.kt` and `PhoneDownFoundation.kt`. Make all changes to design system first.
2. **Item 5** (Bottom nav icons) — Isolated to navigation files.
3. **Item 6** (Settings gear icon) — Isolated to Focus feature + navigation.
4. **Regenerate all Paparazzi baselines** — Since items 1-4 affect every screen, do one single regeneration at the end.

---

## Test Strategy

### Unit Tests
- No new unit tests needed — this phase has no business logic.

### Screenshot Tests (Paparazzi)
All existing Paparazzi tests must be re-recorded since design system changes affect every screen:
- `:feature:focus` — Idle, Active, Paused states (light + dark)
- `:feature:insights` — Insights screen (light + dark)
- `:feature:settings` — Settings screen (light + dark)
- `:feature:onboarding` — Onboarding screen (light + dark)

### UI Tests (Compose / AndroidTest)
- `FocusScreenTest` — Verify settings gear icon visibility in Idle, hidden in Active.
- `FocusScreenTest` — Verify settings gear icon tap triggers callback.
- Navigation tests — Verify bottom nav icons are tappable and switch tabs correctly.

### Manual Verification Checklist
- [ ] Light mode: buttons are pill-shaped, cards have no borders, timer is larger.
- [ ] Dark mode: same as above.
- [ ] Bottom nav: icons visible, labels below, selection state clear.
- [ ] Focus home: gear icon visible in Idle, hidden when session starts.
- [ ] Progress ring: dot visible at arc tip in all progress states.
- [ ] No visual glitches on small screens (320dp width).

---

## Files to Modify (Complete List)

### Design System (4 files)
1. `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownFoundation.kt`
2. `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownComponents.kt`
3. `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownTheme.kt` (if color adjustment needed)

### Navigation (2 files)
4. `app/src/main/java/phonedown/app/navigation/PhoneDownBottomTab.kt`
5. `app/src/main/java/phonedown/app/navigation/PhoneDownNavHost.kt`

### Focus Feature (2 files)
6. `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
7. `app/src/main/java/phonedown/app/focus/FocusRoute.kt`

### Build Configuration (2 files)
8. `gradle/libs.versions.toml` (if material-icons dependency needed)
9. `app/build.gradle.kts` (if material-icons dependency needed)

### Tests (1 file)
10. `feature/focus/src/androidTest/kotlin/phonedown/feature/focus/FocusScreenTest.kt`

### Paparazzi Baselines (~8-12 image files)
11. `feature/focus/src/test/snapshots/images/*`
12. `feature/insights/src/test/snapshots/images/*`
13. `feature/settings/src/test/snapshots/images/*`
14. `feature/onboarding/src/test/snapshots/images/*`

---

## Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Material icons dependency not available in BOM | Medium | Medium | Fallback to custom vector drawables in `app/src/main/res/drawable/`. |
| Removing card borders makes cards invisible in light mode | Medium | Medium | Adjust light mode `surfaceRaised`/`background` colors for more contrast. |
| Timer text overflow on small screens | Low | Medium | Test at 320dp width before merging. Reduce to 48sp if needed. |
| Progress ring dot clips at canvas edges | Low | Low | Compute dot position with stroke offset padding. |
| Paparazzi baseline regeneration reveals unrelated drift | Medium | Low | Review diffs carefully; only approve intentional changes. |

---

## Post-Implementation Documentation

After Phase 1 completes, update:
1. `v1-implementation-plan.md` — Mark design system and navigation items as complete.
2. `docs/design-system.md` — Document new `PhoneDownButtonShape`, `PhoneDownTimerTextStyle`, removed card borders, progress ring dot.
3. `ui-polish-implementation-plan.md` — Check off completed items.
4. This file — Mark all checklist items complete.

---

## Checklist (Track Implementation Progress)

### Preparation
- [ ] Clarification questions answered and documented above.
- [ ] This plan approved by user.

### Implementation
- [ ] Item 1: Pill-shaped buttons — code changes complete.
- [ ] Item 2: Remove card borders — code changes complete.
- [ ] Item 3: Increase timer text size — code changes complete.
- [ ] Item 4: Progress ring position dot — code changes complete.
- [ ] Item 5: Bottom navigation icons — code changes complete.
- [ ] Item 6: Settings gear icon — code changes complete.

### Build & Dependencies
- [ ] Material icons dependency added (if needed) and builds successfully.
- [ ] No compilation errors across all modules.

### Automated Testing
- [ ] `./gradlew :app:assembleDebug` passes.
- [ ] `./gradlew :feature:focus:testDebugUnitTest` passes.
- [ ] `./gradlew :feature:insights:testDebugUnitTest` passes.
- [ ] `./gradlew :feature:settings:testDebugUnitTest` passes.
- [ ] `./gradlew :feature:onboarding:testDebugUnitTest` passes.
- [ ] `./gradlew :feature:focus:recordPaparazziDebug` executed.
- [ ] `./gradlew :feature:insights:recordPaparazziDebug` executed.
- [ ] `./gradlew :feature:settings:recordPaparazziDebug` executed.
- [ ] `./gradlew :feature:onboarding:recordPaparazziDebug` executed.
- [ ] `./gradlew :feature:focus:verifyPaparazziDebug` passes.
- [ ] `./gradlew :feature:insights:verifyPaparazziDebug` passes.
- [ ] `./gradlew :feature:settings:verifyPaparazziDebug` passes.
- [ ] `./gradlew :feature:onboarding:verifyPaparazziDebug` passes.
- [ ] `./gradlew :feature:focus:connectedDebugAndroidTest` passes (if emulator available).

### Manual Verification
- [ ] Light mode visual inspection: buttons, cards, timer, ring dot, nav icons, gear icon.
- [ ] Dark mode visual inspection: same elements.
- [ ] Small screen (320dp) check: no overflow, no clipping.
- [ ] Navigation flow: gear → Settings, bottom tabs → correct screens.

### Documentation
- [ ] `v1-implementation-plan.md` updated.
- [ ] `docs/design-system.md` updated.
- [ ] `ui-polish-implementation-plan.md` updated.
- [ ] This plan file checklist marked complete.

---

*Phase 1 Plan Created: 2026-05-05*
*Status: Pending Approval*
