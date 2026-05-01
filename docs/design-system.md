# Design System

Phase 2 introduces the first Phone Down design-system pass. It maps the light and dark mockups into reusable Compose primitives without implementing the real session engine, analytics repositories, billing, or backup behavior.

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
- `PhoneDownTypography` for calm app typography, including the large timer display style.

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

These components are intentionally small. Phase 2 only added primitives used by Focus, Insights, Settings, or clearly needed by the V1 mockups.

## Mockup Mapping

Implemented static surfaces:

- Focus: central timer ring, primary start action, default duration label, and today metric summary.
- Insights: today summary, seven-day bar visual, session summary, and focus-quality card.
- Settings: focus preferences, theme selector, sound/haptic toggles, account row, Pro row, and backup row.

Intentional Phase 2 limits:

- The timer does not count down yet.
- Analytics values are static demo values.
- Sounds, haptics, account, Pro, and backup rows are UI-only except for preserved Account/Pro callbacks.
- Only theme mode is persisted.

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
