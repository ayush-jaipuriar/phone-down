# Phase 10 — Settings Plan

## 1. Phase Goal

Turn the existing settings placeholder into a fully wired settings screen that persists user preferences through `SettingsRepository`, organizes controls into the 6 planned sections, and provides navigation entry points to Account and Pro flows.

The settings screen should feel like a calm, premium control panel — not a rebuild. Most structure already exists; the work is wiring, filling gaps, and making values dynamic.

## 2. Approved Planning Decisions

- [x] Use existing V1 plan scope (v1-implementation-plan.md:813-871).
- [x] Keep the existing `SettingsScreen` composable structure — refactor, don't rebuild.
- [x] Add `SettingsViewModel` in `:app` for persistence wiring (sounds, haptics, theme, default duration).
- [x] Stub sections that depend on Phase 11 (Auth/Billing) and Phase 12 (Backup) rather than blocking.
- [x] Keep navigation entry points (Account click, Pro click) handled by the parent nav host.

## 3. In Scope

### Wiring (Real Data)
- [ ] Sounds toggle → read/write `SettingsRepository.setSoundEnabled()`.
- [ ] Haptics toggle → read/write `SettingsRepository.setHapticsEnabled()`.
- [ ] Theme control → already wired via `onThemeModeSelected` in nav host; confirm persistence works.
- [ ] Default Duration display → read from `SettingsRepository.settings.defaultDurationSeconds`.
- [ ] Duration Presets display → show presets {10, 15, 25, 45, 60} in a compact format.

### Section Organization (per V1 Plan)
- [ ] **Timer Section**: Default Duration (functional), Duration Presets (display), Custom Duration limit (Pro stub).
- [ ] **Preferences Section**: Sounds toggle (functional), Haptics toggle (functional), Theme (functional), Start Delay display ("3 seconds" — static, no backend yet).
- [ ] **Account & Backup Section**: Google Sign-In row (navigation stub), Backup status (stub), Manual backup (Pro stub), Restore (Pro stub), Auto-backup toggle (Pro stub).
- [ ] **Pro Section**: Upgrade to Pro (navigation stub), Restore purchases (stub), Manage subscription (stub), Lifetime recognition (stub).
- [ ] **Privacy Section**: Local data explanation (text), Cloud backup explanation (text), Export data (Pro stub), Delete local data (action stub with confirmation dialog), Delete cloud backup (stub).
- [ ] **About Section**: App version (hardcoded for now), Privacy policy link (stub), Terms link (stub), Support/contact (stub).

### ViewModel
- [ ] `SettingsViewModel` in `:app` — collects `SettingsRepository.settings` flow, exposes UI state.
- [ ] Actions: `setSoundEnabled`, `setHapticsEnabled`, `setThemeMode`, `setDefaultDuration`.

### Route Wiring
- [ ] `SettingsRoute` in `:app` — Hilt-powered, connects `SettingsViewModel` to `SettingsScreen`.
- [ ] Update `PhoneDownNavHost` to use `SettingsRoute` instead of calling `SettingsScreen` directly.

### Testing
- [ ] `SettingsViewModel` unit tests — verify toggle persistence and state emission.
- [ ] Update Paparazzi screenshot tests for all settings sections in light/dark.
- [ ] Update Compose UI instrumented test for wired settings.

### Docs
- [ ] Update `v1-implementation-plan.md` Phase 10 progress log.
- [ ] Update `docs/agent-handoff.md`.

## 4. Out Of Scope

- [ ] Google Sign-In implementation (Phase 11).
- [ ] Billing purchase flow (Phase 11).
- [ ] Backup/Restore implementation (Phase 12).
- [ ] Custom duration limit enforcement (requires billing/Pro gating, Phase 11).
- [ ] Data deletion actual execution (requires DB/DataStore clear logic, Phase 13).
- [ ] Privacy policy / terms URLs (placeholder links only).
- [ ] Delete confirmation UX beyond a simple dialog stub.

## 5. Architectural Intent

```
:app
  ├── SettingsRoute (Hilt entry, connects ViewModel to Screen)
  └── SettingsViewModel (collects SettingsRepository flow, exposes actions)

:feature:settings
  └── SettingsScreen (UI only, stateless, receives state and callbacks)
      ├── TimerSection
      ├── PreferencesSection
      ├── AccountBackupSection
      ├── ProSection
      ├── PrivacySection
      └── AboutSection

:core:designsystem
  └── Existing components reused: PhoneDownSettingRow, PhoneDownSwitchRow,
       PhoneDownThemeControl, PhoneDownCard, PhoneDownScreen, PhoneDownTopBar,
       PhoneDownProBadge
```

The screen remains stateless — all state flows from the ViewModel. Callbacks are passed up to the ViewModel (or nav host for navigation).

## 6. Data Flow

```
SettingsRepository (DataStore)
        │
        ▼  Flow<UserSettings>
SettingsViewModel
        │
        ▼  StateFlow<SettingsUiState>
SettingsRoute
        │
        ▼  props + callbacks
SettingsScreen
```

## 7. SettingsUiState

```kotlin
data class SettingsUiState(
    val defaultDurationSeconds: Long = 1500,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val autoBackupEnabled: Boolean = false,
    val lastBackupEpochMillis: Long? = null,
    val backupOptIn: Boolean = false,
)
```

## 8. Implementation Order

1. Create `SettingsViewModel` in `:app/settings/` — wire to `SettingsRepository`.
2. Create `SettingsRoute` in `:app/settings/` — Hilt-powered composable entry.
3. Update `PhoneDownNavHost` to use `SettingsRoute` and pass theme mode through ViewModel.
4. Refactor `SettingsScreen` into the 6 sections per the plan.
5. Wire functional toggles (sounds, haptics, theme) to ViewModel actions.
6. Wire default duration display from ViewModel state.
7. Add stub sections: Privacy, About.
8. Add `SettingsViewModel` unit tests.
9. Update Paparazzi + Compose UI tests.
10. Update docs.

## 9. Acceptance Criteria

- [ ] Sounds and Haptics toggles persist across app restarts.
- [ ] Theme control persists and reflects correctly.
- [ ] Default duration shows the actual stored value.
- [ ] All 6 sections render in the correct order.
- [ ] Navigation entry points (Account, Pro) work via existing nav host wiring.
- [ ] Privacy and About sections render with stub content (no functional backend).
- [ ] `SettingsViewModel` tests pass for toggle + theme persistence.
- [ ] Paparazzi screenshots pass for light/dark.
- [ ] App assembles without errors.

## 10. Risks

- **Theme already wired externally**: The `onThemeModeSelected` callback is currently handled in `MainActivity` directly. The ViewModel needs to coexist with this or absorb it. The simplest approach: let the ViewModel handle the persistence, and pass `onThemeModeSelected` through the route for the nav host to still call.
- **Pro/Account stubs**: Keep them visually present but non-functional to avoid confusion.
- **Delete data confirmation**: Use a simple AlertDialog; don't implement actual deletion (Phase 13).

## 11. Approval Gate

Implementation must not begin until this plan is approved.

Common next steps:
- Approve and start implementation
- Request updates to the plan
