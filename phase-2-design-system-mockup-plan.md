# Phase 2 Plan - Design System And Mockup Mapping

This document is the detailed implementation plan for Phase 2 of Phone Down V1. It must be reviewed and approved before implementation begins.

Phase 2 turns the working Phase 1 navigation shell into a visually credible app foundation. The goal is not to complete business logic. The goal is to create reusable Compose design-system primitives, map the light/dark mockups into app tokens and components, and replace the Focus, Insights, and Settings placeholders with static but realistic UI surfaces.

## 0. Clarified Decisions

- [x] Plan all three primary tabs equally: Focus, Insights, and Settings.
- [x] Use my recommendation on implementation shape: build reusable primitives first, then compose realistic static screens from those primitives.
- [x] Include theme persistence if it can be scoped cleanly.
- [x] Add reusable design-system components now, even if some are lightly used at first.
- [x] Introduce screenshot/UI testing in Phase 2.
- [x] Keep implementation gated until this plan is approved.

## 1. Recommended Scope

Phase 2 should include both the reusable design system and the first realistic screen pass for the three primary tabs.

The reason is practical: a design system built without real usage often misses spacing, density, color, and state needs. By building Focus, Insights, and Settings as static UI clients of the design system, we validate the primitives against the mockups immediately while still avoiding premature session, analytics, billing, or persistence complexity.

### In Scope

- [ ] Define Phone Down color tokens for light and dark themes.
- [ ] Define semantic color roles beyond stock Material names.
- [ ] Define typography decisions for timer, headings, labels, metric values, and settings rows.
- [ ] Define spacing, shape, border, and surface tokens.
- [ ] Add reusable design-system components.
- [ ] Add theme mode model and persistence path if scoped cleanly.
- [ ] Add static realistic Focus screen matching the mockup direction.
- [ ] Add static realistic Insights screen matching the mockup direction.
- [ ] Add static realistic Settings screen matching the mockup direction.
- [ ] Preserve Phase 1 navigation behavior.
- [ ] Add Compose UI tests for key screen nodes and navigation shell presence.
- [ ] Add screenshot testing support and baseline screenshots for light/dark primary screens where tooling allows.
- [ ] Update implementation docs and progress logs.

### Out Of Scope

- [ ] Real session state machine integration.
- [ ] Real sensor state integration.
- [ ] Real timer countdown behavior beyond static/demo display values.
- [ ] Real Room-backed today/weekly analytics.
- [ ] Real settings persistence except theme mode if included cleanly.
- [ ] Real billing or entitlement state.
- [ ] Real account or backup behavior.
- [ ] Final onboarding card design.
- [ ] Final Account and Pro screen polish.
- [ ] Production chart library integration unless a simple native placeholder cannot meet the visual requirement.

## 2. Mockup Mapping Principles

The mockups should guide layout, density, contrast, and tone. The implementation should avoid blindly copying pixel positions if that would make the app brittle across Android screen sizes.

### Visual Direction Checklist

- [ ] Light mode uses a soft off-white app background.
- [ ] Dark mode uses an off-black app background.
- [ ] Text hierarchy is high contrast but calm.
- [ ] Surfaces are quiet and flat, with subtle border/elevation treatment.
- [ ] Timer typography is large, clear, and central.
- [ ] Progress accent uses restrained purple/blue.
- [ ] Completion/clean accent uses restrained green.
- [ ] Interruption/error accent uses restrained red.
- [ ] Toggle/accent controls use the blue tone visible in the Settings mockup.
- [ ] Bottom navigation feels integrated, not like a default pasted-on Material bar.
- [ ] Avoid gradients, decorative illustrations, neon glow, gamified badges, and oversized marketing-style cards.
- [ ] Maintain strong readability in both themes.

### Responsive Layout Checklist

- [ ] Support compact Android phones without text overflow.
- [ ] Support taller Android phones without awkward empty spacing.
- [ ] Use scroll containers where content may exceed viewport height.
- [ ] Use stable dimensions for fixed-format UI elements such as timer ring, metric cards, chart placeholders, and bottom navigation.
- [ ] Avoid cards nested inside cards.
- [ ] Avoid decorative page sections; screens should feel like app surfaces, not landing pages.

## 3. Design-System Package Shape

Recommended package under `:core:designsystem`:

```text
core/designsystem/src/main/kotlin/phonedown/core/designsystem/
  PhoneDownTheme.kt
  color/
    PhoneDownColors.kt
    PhoneDownThemeMode.kt
  component/
    PhoneDownButton.kt
    PhoneDownCard.kt
    PhoneDownIconButton.kt
    PhoneDownMetricCard.kt
    PhoneDownProgressRing.kt
    PhoneDownScaffold.kt
    PhoneDownSettingRow.kt
    PhoneDownSwitchRow.kt
    PhoneDownTopBar.kt
    PhoneDownProBadge.kt
    PhoneDownEmptyState.kt
  foundation/
    PhoneDownSpacing.kt
    PhoneDownShapes.kt
    PhoneDownTypography.kt
```

The exact package/file layout can be adjusted during implementation if Compose or ktlint patterns make a slightly different structure cleaner.

## 4. Theme And Token Plan

The current `PhoneDownTheme` wraps stock Material schemes. Phase 2 should replace that with an app-specific theme layer.

### Color Tokens

- [ ] Add named base palette values for light mode.
- [ ] Add named base palette values for dark mode.
- [ ] Add semantic roles:
  - [ ] `background`
  - [ ] `surface`
  - [ ] `surfaceRaised`
  - [ ] `borderSubtle`
  - [ ] `textPrimary`
  - [ ] `textSecondary`
  - [ ] `textTertiary`
  - [ ] `progress`
  - [ ] `success`
  - [ ] `warning`
  - [ ] `danger`
  - [ ] `toggle`
  - [ ] `inactive`
- [ ] Map semantic roles into Material 3 `ColorScheme` where appropriate.
- [ ] Expose non-Material semantic colors through a local design-system object or composition local.
- [ ] Keep color choices close to the mockups and avoid one-note purple/blue dominance.

### Typography Tokens

- [ ] Define timer display typography.
- [ ] Define screen title typography.
- [ ] Define section title typography.
- [ ] Define metric value typography.
- [ ] Define metric label typography.
- [ ] Define settings row title/body typography.
- [ ] Define button typography.
- [ ] Avoid viewport-scaled font sizes.
- [ ] Keep letter spacing at `0`.

### Spacing And Shape Tokens

- [ ] Define spacing scale for `4`, `8`, `12`, `16`, `20`, `24`, and `32` dp.
- [ ] Define screen horizontal padding.
- [ ] Define card padding.
- [ ] Define timer component sizing.
- [ ] Define small, medium, and large shape radii.
- [ ] Keep cards at `8.dp` radius or less unless mockup evidence clearly requires more.
- [ ] Define thin border treatment.

### Theme Mode Model

- [ ] Add `PhoneDownThemeMode` with `System`, `Light`, and `Dark`.
- [ ] Add a pure resolver from mode plus system dark flag to effective dark mode.
- [ ] Add unit tests for theme mode resolution.

## 5. Theme Persistence Recommendation

I recommend including narrowly scoped theme persistence in Phase 2.

The reason is that Settings must expose Light, Dark, and System in V1, and screenshot/UI testing benefits from deterministic theme selection. This should be scoped to theme mode only, not a full settings repository.

### DataStore Scope

- [ ] Add DataStore dependency if not already present.
- [ ] Add `ThemeModePreference` or equivalent in `:core:datastore`.
- [ ] Store one string/enum value for `System`, `Light`, or `Dark`.
- [ ] Expose a small flow for theme mode.
- [ ] Expose a suspend setter for theme mode.
- [ ] Add a fake/in-memory theme preference for tests/previews if helpful.
- [ ] Wire app-level theme mode into `PhoneDownTheme`.
- [ ] Keep other settings persistence for Phase 3.

### Acceptance Criteria

- [ ] App can render in system/default mode.
- [ ] Settings UI can display theme choices.
- [ ] Theme mode persistence has unit coverage.
- [ ] No broader settings persistence is accidentally implemented.

## 6. Component Plan

### App Surface Components

- [ ] `PhoneDownScaffold`
  - [ ] Provides background color.
  - [ ] Applies screen-safe padding defaults.
  - [ ] Supports optional bottom bar.
  - [ ] Avoids unnecessary nested cards.
- [ ] `PhoneDownTopBar`
  - [ ] Supports title.
  - [ ] Supports optional trailing icon/action.
  - [ ] Matches calm app density.

### Action Components

- [ ] `PhoneDownButton`
  - [ ] Primary style.
  - [ ] Secondary/quiet style.
  - [ ] Disabled state.
  - [ ] Loading state only if trivial; otherwise defer.
- [ ] `PhoneDownIconButton`
  - [ ] Stable square tap target.
  - [ ] Uses Material/Lucide-equivalent Android vector icon strategy if icons are added.
  - [ ] Content descriptions for accessibility.

### Surface And Metric Components

- [ ] `PhoneDownCard`
  - [ ] Standard surface color.
  - [ ] Optional subtle border.
  - [ ] No nested-card styling.
- [ ] `PhoneDownMetricCard`
  - [ ] Value.
  - [ ] Label.
  - [ ] Optional semantic accent.
  - [ ] Stable height.
- [ ] `PhoneDownHistoryRow`
  - [ ] Session duration.
  - [ ] Time/date label.
  - [ ] Clean/interrupted marker.
  - [ ] Deferred if Phase 2 screens do not need it.

### Focus Components

- [ ] `PhoneDownProgressRing`
  - [ ] Static progress fraction input.
  - [ ] Center content slot.
  - [ ] Light/dark colors.
  - [ ] Stable size constraints.
  - [ ] Preview in light and dark.
- [ ] `PhoneDownDurationChip`
  - [ ] Selected and unselected states.
  - [ ] Used by Focus screen static duration selector if included.

### Settings Components

- [ ] `PhoneDownSettingRow`
  - [ ] Leading optional icon.
  - [ ] Title.
  - [ ] Supporting text.
  - [ ] Trailing value/action.
  - [ ] Clickable and non-clickable variants.
- [ ] `PhoneDownSwitchRow`
  - [ ] Title.
  - [ ] Supporting text.
  - [ ] Switch state.
  - [ ] Disabled state.
- [ ] `PhoneDownSegmentedThemeControl`
  - [ ] System, Light, Dark.
  - [ ] Uses selected state with accessible labels.

### Status And Gating Components

- [ ] `PhoneDownProBadge`.
- [ ] `PhoneDownPaywallTeaser`.
- [ ] `PhoneDownEmptyState`.
- [ ] `PhoneDownInlineStatus`.

## 7. Screen Implementation Plan

These screens should use static/demo state. They should look real, but they should not pretend to have working session, analytics, billing, or account behavior.

### Focus Screen

Goal: make the primary ritual screen feel close to the mockups while preserving future room for real session state.

- [ ] Replace placeholder layout with mockup-inspired Focus home.
- [ ] Show app title/top treatment.
- [ ] Show large selected duration, recommended default `25:00`.
- [ ] Show circular timer/progress ring.
- [ ] Show primary `Start Focus` action.
- [ ] Show duration hint/default label.
- [ ] Show today summary metrics:
  - [ ] Total focus.
  - [ ] Sessions.
  - [ ] Clean.
- [ ] Use static values such as `1h 20m`, `3`, and `2`.
- [ ] Avoid Pause/Add Time controls.
- [ ] Avoid real timer countdown.
- [ ] Add Compose preview for light and dark.
- [ ] Add UI test tags for screen root, timer display, start button, and today metrics.

### Insights Screen

Goal: create a credible analytics surface using static data and reusable metric/chart-like components.

- [ ] Replace placeholder layout with mockup-inspired Insights screen.
- [ ] Show top title.
- [ ] Show today focus summary.
- [ ] Show Focus Quality card.
- [ ] Show last 7 days visual summary using simple custom bars or static chart placeholder.
- [ ] Show streak/clean-session summary.
- [ ] Show recent session rows if space allows.
- [ ] Show Pro-gated advanced insight teaser in a restrained way if useful.
- [ ] Use static data only.
- [ ] Avoid chart library dependency unless clearly needed.
- [ ] Add Compose preview for light and dark.
- [ ] Add UI test tags for screen root, quality card, weekly chart, and history/summary section.

### Settings Screen

Goal: make Settings match the mockup density and expose the V1 theme option while preserving Account and Pro navigation callbacks.

- [ ] Replace placeholder layout with structured Settings screen.
- [ ] Show Timer section.
- [ ] Show default duration row.
- [ ] Show theme mode segmented control or row.
- [ ] Show sound toggle.
- [ ] Show haptic toggle.
- [ ] Show Account row using existing `onAccountClick`.
- [ ] Show Pro row using existing `onProClick`.
- [ ] Show Backup row as Pro-gated/static, without real backup behavior.
- [ ] Show Privacy/About rows if space allows.
- [ ] Keep account/pro route behavior from Phase 1 intact.
- [ ] Use static values except theme mode if persistence is included.
- [ ] Add Compose preview for light and dark.
- [ ] Add UI test tags for screen root, theme control, account row, and pro row.

## 8. Navigation And App Shell Adjustments

Phase 1 navigation works. Phase 2 should refine appearance without breaking route ownership.

- [ ] Keep route definitions in `:app`.
- [ ] Keep feature modules route-string agnostic.
- [ ] Keep onboarding as initial route until onboarding persistence exists.
- [ ] Keep bottom tabs for Focus, Insights, and Settings.
- [ ] Style bottom navigation through design-system tokens.
- [ ] Keep Account and Pro reachable from Settings.
- [ ] Avoid adding backup route unless a later phase explicitly introduces it.

## 9. Screenshot And UI Testing Plan

Phase 2 should introduce two layers of UI verification:

1. Compose UI behavior/structure tests that assert important nodes exist and callbacks remain wired.
2. Screenshot tests that detect visual regressions against light/dark baselines.

### Tooling Recommendation

Use a screenshot testing library compatible with Compose and the current Android Gradle setup. Recommended first choice is Paparazzi if it works cleanly with AGP `8.13.2` and Kotlin `2.2.21`. If Paparazzi compatibility blocks progress, use Android instrumented screenshot tests with Compose test rule and emulator screenshots.

This should be validated during implementation before committing deeply to one approach.

### UI Test Checklist

- [ ] Add Compose UI test dependencies.
- [ ] Add test tags to Focus, Insights, and Settings screens.
- [ ] Add Focus screen UI test:
  - [ ] Screen root exists.
  - [ ] Timer display exists.
  - [ ] Start button exists.
  - [ ] Today metrics exist.
- [ ] Add Insights screen UI test:
  - [ ] Screen root exists.
  - [ ] Focus quality card exists.
  - [ ] Weekly visual exists.
- [ ] Add Settings screen UI test:
  - [ ] Screen root exists.
  - [ ] Theme control exists.
  - [ ] Account row triggers callback.
  - [ ] Pro row triggers callback.
- [ ] Keep tests deterministic and independent of real DataStore where possible.

### Screenshot Test Checklist

- [ ] Choose screenshot testing approach after compatibility check.
- [ ] Add screenshot test dependencies/configuration.
- [ ] Add light-mode screenshot coverage for:
  - [ ] Focus screen.
  - [ ] Insights screen.
  - [ ] Settings screen.
- [ ] Add dark-mode screenshot coverage for:
  - [ ] Focus screen.
  - [ ] Insights screen.
  - [ ] Settings screen.
- [ ] Store baselines in a predictable test artifact location.
- [ ] Document how to update screenshot baselines intentionally.
- [ ] Ensure screenshot outputs and generated diff artifacts are ignored when appropriate.

### Acceptance Criteria

- [ ] UI tests pass locally.
- [ ] Screenshot tests can be run locally.
- [ ] Screenshot baselines are deterministic enough to be useful.
- [ ] `./scripts/check.sh` includes stable UI/screenshot checks if runtime is reasonable.
- [ ] If screenshot checks are too slow or environment-sensitive, document the separate command and keep `scripts/check.sh` focused on stable checks.

## 10. Accessibility And Content Requirements

- [ ] Important buttons have clear accessible labels.
- [ ] Icon-only controls have content descriptions.
- [ ] Text contrast is acceptable in light and dark modes.
- [ ] Touch targets are at least Material-recommended minimums where practical.
- [ ] Text does not overflow at common font scales.
- [ ] Static demo copy remains calm and non-shaming.
- [ ] No in-app instructional text describes implementation details or shortcuts.

## 11. Build And Dependency Plan

### Dependencies To Consider

- [ ] Compose UI test dependencies.
- [ ] Compose UI tooling/test manifest dependencies if needed.
- [ ] DataStore preferences dependency if theme persistence is included.
- [ ] Paparazzi or equivalent screenshot test dependency after compatibility check.
- [ ] Optional icons dependency only if existing vector drawables or Material icons are insufficient.

### Build Logic Adjustments

- [ ] Add shared Compose UI test dependencies to the relevant convention plugin if broadly useful.
- [ ] Add screenshot test plugin/dependencies in a narrow module scope first.
- [ ] Avoid bloating every module with screenshot tooling if only feature/design-system modules need it.
- [ ] Ensure generated screenshot artifacts are ignored unless they are intentional baselines.

## 12. Documentation Updates

- [ ] Update this Phase 2 plan during implementation.
- [ ] Add Phase 2 progress log to `v1-implementation-plan.md`.
- [ ] Add or update design-system documentation.
- [ ] Document color tokens and semantic roles.
- [ ] Document typography decisions.
- [ ] Document screenshot/UI test commands.
- [ ] Document any intentional differences from the mockups.
- [ ] Update README if verification commands change.

Recommended new document:

```text
docs/design-system.md
```

## 13. Implementation Steps

Implementation should proceed in this order after approval.

### Step 1 - Compatibility And Baseline Audit

- [ ] Confirm Compose UI test dependency versions.
- [ ] Check screenshot testing compatibility with AGP/Kotlin.
- [ ] Confirm current `scripts/check.sh` runtime expectations.
- [ ] Inspect mockups for layout, color, and density notes.
- [ ] Record any screenshot-tooling decision in this plan.

### Step 2 - Theme Tokens

- [ ] Replace stock light/dark schemes with app-specific colors.
- [ ] Add semantic color access.
- [ ] Add typography tokens.
- [ ] Add spacing and shape tokens.
- [ ] Add previews/smoke composables if useful.
- [ ] Add theme mode model and resolver tests.

### Step 3 - Theme Persistence

- [ ] Add narrowly scoped DataStore preference for theme mode.
- [ ] Add repository/preference abstraction.
- [ ] Add fake or in-memory test helper if needed.
- [ ] Wire app shell to effective theme mode.
- [ ] Keep non-theme settings static.

### Step 4 - Reusable Components

- [ ] Add surface/scaffold components.
- [ ] Add button/icon button components.
- [ ] Add timer/progress ring components.
- [ ] Add metric/card components.
- [ ] Add settings row/switch/theme control components.
- [ ] Add status/pro/empty components.
- [ ] Add previews for key components in light and dark.

### Step 5 - Focus Screen

- [ ] Replace Focus placeholder with static mockup-mapped screen.
- [ ] Use design-system components only where practical.
- [ ] Add UI test tags.
- [ ] Add previews.

### Step 6 - Insights Screen

- [ ] Replace Insights placeholder with static mockup-mapped screen.
- [ ] Add simple weekly visual.
- [ ] Add UI test tags.
- [ ] Add previews.

### Step 7 - Settings Screen

- [ ] Replace Settings placeholder with static mockup-mapped screen.
- [ ] Add theme mode UI.
- [ ] Preserve Account and Pro callbacks.
- [ ] Add UI test tags.
- [ ] Add previews.

### Step 8 - UI And Screenshot Tests

- [ ] Add Compose UI tests for Focus.
- [ ] Add Compose UI tests for Insights.
- [ ] Add Compose UI tests for Settings.
- [ ] Add screenshot test setup.
- [ ] Add light/dark baselines for primary screens.
- [ ] Document update process.

### Step 9 - Verification And Docs

- [ ] Run `./gradlew ktlintCheck`.
- [ ] Run `./gradlew detekt`.
- [ ] Run `./gradlew lintDebug`.
- [ ] Run `./gradlew testDebugUnitTest`.
- [ ] Run Compose UI tests.
- [ ] Run screenshot tests.
- [ ] Run `./gradlew :app:assembleDebug`.
- [ ] Run `./scripts/check.sh`.
- [ ] Update this plan with completion notes.
- [ ] Update `v1-implementation-plan.md`.
- [ ] Update `docs/design-system.md`.
- [ ] Update README if verification commands change.

## 14. Risks And Mitigations

### Risk: Screenshot Tooling Compatibility

- Mitigation: validate screenshot tooling before broad UI implementation. If Paparazzi is blocked, use instrumented Compose screenshot tests or document screenshot testing as a separate emulator-backed command.

### Risk: Theme Persistence Expands Into Full Settings Scope

- Mitigation: persist theme mode only. Keep sound, haptics, default duration, backup, and account settings static until their planned phases.

### Risk: Static Screens Look Too Real

- Mitigation: use realistic demo data but do not wire fake business logic. Keep view state simple and explicit so future phases can replace static models with real state.

### Risk: Design System Over-Abstraction

- Mitigation: create components only when used by Focus, Insights, Settings, or clearly required by the mockups. Keep APIs small.

### Risk: Mockup Matching Breaks Responsiveness

- Mitigation: treat mockups as visual direction, then verify compact and tall Android screen sizes through tests/screenshots.

### Risk: Bottom Navigation Styling Conflicts With App Shell

- Mitigation: style the existing Phase 1 bottom navigation rather than replacing navigation architecture.

## 15. Completion Criteria

Phase 2 is complete only when:

- [x] Phone Down has app-specific light and dark theme tokens.
- [x] Theme mode model supports System, Light, and Dark.
- [x] Theme persistence is implemented or explicitly deferred with documented reason.
- [x] Reusable design-system components exist for the planned V1 UI primitives.
- [x] Focus screen is no longer a generic placeholder and follows the mockup direction.
- [x] Insights screen is no longer a generic placeholder and follows the mockup direction.
- [x] Settings screen is no longer a generic placeholder and follows the mockup direction.
- [x] Account and Pro navigation callbacks still work from Settings.
- [x] Compose UI tests cover primary screen structure and critical callbacks.
- [x] Screenshot tests or an approved screenshot-test fallback are introduced.
- [x] Full verification passes.
- [x] Documentation is updated.
- [ ] User is informed of verification results.

## 16. Approval Gate

Implementation must not begin until this Phase 2 plan is approved.

Approval options:

- Approve Phase 2 as written and begin implementation.
- Request lighter theme persistence scope.
- Request different screenshot testing tooling.
- Request changes to the component list.
- Request closer mockup matching for one screen before the others.

## 17. Implementation Completion Notes

### Completed Scope

- [x] Added `ThemeMode` with System, Light, and Dark modes in `:core:model`.
- [x] Added unit coverage for theme-mode dark resolution.
- [x] Added narrow theme persistence through `ThemeModePreference` in `:core:datastore`.
- [x] Wired persisted theme mode into `MainActivity`, `PhoneDownApp`, and `PhoneDownTheme`.
- [x] Added app-specific light and dark semantic color tokens.
- [x] Added spacing, sizing, shapes, and typography foundation tokens.
- [x] Added reusable buttons, cards, metric cards, timer ring, settings rows, switch rows, theme control, Pro badge, inline status, and screen/top-bar primitives.
- [x] Replaced Focus placeholder with static mockup-mapped Focus UI.
- [x] Replaced Insights placeholder with static mockup-mapped Insights UI and a simple weekly bar visual.
- [x] Replaced Settings placeholder with static mockup-mapped Settings UI.
- [x] Preserved Account and Pro callbacks from Settings.
- [x] Added Compose UI test coverage for Focus, Insights, and Settings.
- [x] Added Paparazzi screenshot tests and light/dark baselines for Focus, Insights, and Settings.
- [x] Added screenshot verification and UI-test APK compilation to `scripts/check.sh`.
- [x] Updated `README.md`, `v1-implementation-plan.md`, `docs/module-dependency-rules.md`, and added `docs/design-system.md`.

### Deliberate Deferrals

- [x] Real countdown/session state remains deferred to the session-engine and Focus feature phases.
- [x] Real analytics remain deferred to persistence/domain Insights phases.
- [x] Sound, haptic, account, Pro, backup, and duration settings remain static except for theme mode.
- [x] Connected instrumented UI execution requires a device/emulator and was not run when `adb devices` showed no attached devices.

### Verification Completed

- [x] `./gradlew ktlintCheck`
- [x] `./gradlew detekt`
- [x] `./gradlew lintDebug testDebugUnitTest`
- [x] `./gradlew :app:assembleDebug`
- [x] `./gradlew :feature:focus:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug`
- [x] `./gradlew :feature:focus:assembleDebugAndroidTest :feature:insights:assembleDebugAndroidTest :feature:settings:assembleDebugAndroidTest`
- [ ] Connected UI tests, pending attached device/emulator.
