# Design System

Phase 2 introduced the first Phone Down design-system pass. The later UI polish work extended it so the shipped surfaces track the mockups much more closely without changing the underlying app architecture.

## Theme

The theme entry point is `PhoneDownTheme` in `:core:designsystem`.

- Theme modes are represented by `ThemeMode.System`, `ThemeMode.Light`, and `ThemeMode.Dark` in `:core:model`.
- `ThemeMode.shouldUseDarkTheme(systemInDarkTheme)` keeps theme resolution pure and unit-testable.
- `MainActivity` reads the persisted theme mode from `ThemeModePreference` and passes it into the app navigation shell.
- `SettingsScreen` exposes the three-mode selector as the first V1 settings surface.

## Semantic Colors

`PhoneDownDesign.colors` exposes app-specific roles:

- `background`
- `surface`
- `surfaceRaised`
- `borderSubtle`
- `textPrimary`
- `textSecondary`
- `textTertiary`
- `progress`
- `progressTrack`
- `success`
- `warning`
- `danger`
- `toggle`
- `inactive`

The color values intentionally live as design tokens. Detekt magic-number suppression is scoped to the theme file because hex color literals are the token source of truth.

## Foundation Tokens

The foundation file defines:

- `PhoneDownSpacing` for `4`, `8`, `12`, `16`, `20`, `24`, and `32` dp spacing plus screen/card aliases.
- `PhoneDownSize` for stable touch targets and timer-ring sizing.
- `PhoneDownShapes` for compact Material 3 radii.
- `PhoneDownTypography` for calm app typography, including the larger timer display style used by the Focus ring.
- `PhoneDownButtonShape` for the pill/capsule primary-action treatment used throughout the polished Focus flow.
- `PhoneDownScreenTitleTextStyle` for stronger top-level screen titles like `Phone Down`, `Insights`, and `Settings`.
- `PhoneDownSectionHeaderTextStyle` for prominent section/state titles such as `Ready to focus?` and Settings section headers.
- `PhoneDownCardHeaderTextStyle` for bolder in-card labels like `7 Day Overview`, `Focus Quality`, and `Best Focus Time`.

## Components

Current reusable components:

- `PhoneDownScreen`
- `PhoneDownTopBar`
- `PhoneDownButton`
- `PhoneDownIconButton`
- `PhoneDownCard`
- `PhoneDownMetricCard`
- `PhoneDownProgressRing`
- `PhoneDownSettingRow`
- `PhoneDownSwitchRow`
- `PhoneDownThemeControl`
- `PhoneDownProBadge`
- `PhoneDownInlineStatus`
- `PhoneDownHourlyChart`

Component updates from the UI polish pass:

- `PhoneDownScreen` now supports a configurable top inset so headerless primary-tab screens can sit higher without affecting detail pages.
- `PhoneDownButton` now uses a pill silhouette to match the mockups.
- `PhoneDownCard` relies on surface contrast rather than explicit borders.
- `PhoneDownProgressRing` now includes a moving tip dot for active progress.
- `PhoneDownSettingRow` supports chevrons and destructive styling for clearer Settings grouping.

These components are intentionally small. Phase 2 only added primitives used by Focus, Insights, Settings, or clearly needed by the V1 mockups.

## Mockup Mapping

Implemented static surfaces:

- Focus: central timer ring, primary start action, default duration label, and today metric summary.
- Insights: today summary, seven-day bar visual, session summary, and focus-quality card.
- Settings: focus preferences, theme selector, sound/haptic toggles, account row, Pro row, and backup row.

Implemented polish layers beyond the original Phase 2 baseline:

- Focus: settings gear on Idle, ready-to-focus ritual screen, richer interruption state, pause/add-time controls, arming countdown, and richer completion summary.
- Insights: weekly calendar strip, hourly focus chart, and historical day switching.
- Settings: clearer section grouping, chevrons for navigable rows, and stronger destructive affordances.

Latest hierarchy refinement from the updated mockup:

- Screen headers are intentionally bolder and slightly larger than the earlier pass.
- Settings section headers now use primary text color instead of reading like muted captions.
- Insights card headers use stronger label styling so the cards scan more like the updated mock.
- These adjustments are token-driven, so light and dark mode inherit the same hierarchy without maintaining separate style branches.
- Primary tab roots now intentionally avoid redundant top bars when bottom navigation already provides location context.

Intentional current limits:

- Pause and Add Time are still UI-first controls; full domain behavior for them is deferred.
- Some screenshot baselines and styling conventions intentionally diverge from ktlint preferences because the repo keeps PascalCase composable naming and Compose-first formatting.

## Testing

Screenshot baselines live under each feature module's `src/test/snapshots/images` directory.

Run screenshot verification:

```bash
./gradlew :feature:focus:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug
```

Update screenshot baselines intentionally:

```bash
./gradlew :feature:focus:recordPaparazziDebug :feature:insights:recordPaparazziDebug :feature:settings:recordPaparazziDebug
```

Compile Compose UI tests:

```bash
./gradlew :feature:focus:assembleDebugAndroidTest :feature:insights:assembleDebugAndroidTest :feature:settings:assembleDebugAndroidTest
```

Run connected UI tests only when an Android device or emulator is attached:

```bash
./gradlew :feature:focus:connectedDebugAndroidTest :feature:insights:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest
```
