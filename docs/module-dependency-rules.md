# Module Dependency Rules

This document defines the intended dependency direction for the Phone Down Android codebase.

The goal is to keep product behavior testable, UI modules focused, and Android implementation details from leaking into pure domain logic.

## Module Categories

### `:app`

The app module owns process-level Android wiring:

- Application class.
- Main activity.
- App navigation host.
- Top-level dependency injection entry points.
- App-wide route coordination.

The app module may depend on feature modules and shared core modules. Feature, domain, and core modules must not depend on `:app`.

### `:feature:*`

Feature modules own user-facing screens and feature-level UI state.

Examples:

- `:feature:focus`
- `:feature:insights`
- `:feature:settings`
- `:feature:onboarding`
- `:feature:account`
- `:feature:pro`

Feature modules may depend on:

- `:core:common`
- `:core:model`
- `:core:designsystem`
- relevant `:domain:*` modules
- relevant core implementation modules when the feature directly owns that integration surface

Feature modules should not depend on other feature modules unless a shared UI contract is intentionally extracted first.

### `:domain:*`

Domain modules own product rules and use cases.

Examples:

- `:domain:session`
- `:domain:insights`

Domain modules may depend on:

- `:core:common`
- `:core:model`

Domain modules must not depend on:

- `:app`
- `:feature:*`
- Compose
- Android UI classes
- Android services
- Room, DataStore, Billing, Drive, or SensorManager implementations

### `:core:model`

The model module owns shared data models and enums.

It should stay dependency-light and should not depend on Android UI, feature modules, or domain modules.

### `:core:common`

The common module owns tiny shared primitives such as result types, dispatchers, time abstractions, and reusable error types.

It should stay small. If a helper grows product-specific behavior, move it into the relevant domain module.

### `:core:designsystem`

The design system module owns shared Compose theme and reusable UI primitives.

It may depend on Compose, Material 3, and `:core:model` for cross-app UI enums such as `ThemeMode`.
It must not depend on feature modules or product domain use cases.

### Android Implementation Core Modules

These modules own platform or integration details:

- `:core:database`
- `:core:datastore`
- `:core:sensors`
- `:core:notifications`
- `:core:billing`
- `:core:auth`
- `:core:backup`
- `:core:charts`

They may depend on `:core:common` and `:core:model` as needed.

They should not depend on feature modules.

## Allowed Examples

- `:app` depends on `:feature:focus`.
- `:app` depends on `:feature:settings`.
- `:feature:focus` depends on `:domain:session`.
- `:feature:focus` depends on `:core:designsystem`.
- `:domain:session` depends on `:core:model`.
- `:core:database` depends on `:core:model`.
- `:core:backup` depends on `:core:database` and `:core:datastore`.

## Disallowed Examples

- `:domain:session` depending on `:feature:focus`.
- `:domain:insights` depending on Compose.
- `:core:database` depending on `:feature:insights`.
- `:core:designsystem` depending on `:domain:session`.
- `:feature:settings` depending on `:app`.

## Navigation Ownership

Navigation is coordinated by `:app`.

Feature modules should expose screen composables with callbacks such as `onContinue`, `onAccountClick`, or `onBack`. They should not know route strings or directly own the app-level navigation graph.

This keeps feature modules reusable and prevents route definitions from spreading across the codebase.

## Dependency Enforcement

Automated dependency-boundary enforcement is intentionally deferred. Phase 1 documents the rules and keeps the actual module graph aligned with them.

Once the architecture stabilizes, the project can add automated checks such as a custom Gradle validation task or a dependency-analysis plugin.

## Current Phase 1 Notes

- `:app` owns `PhoneDownNavHost`, `PhoneDownRoute`, and `PhoneDownBottomTab`.
- Placeholder screens live in their owning feature modules.
- Hilt is wired at the application and activity level only.
- Real repository/use-case bindings are intentionally deferred until those implementations exist.

## Current Phase 2 Notes

- `:core:designsystem` now owns app-specific light/dark color tokens, typography, spacing, shapes, cards, buttons, timer ring, settings rows, theme selector, inline status, and Pro badge primitives.
- `:core:model` owns `ThemeMode` and its pure dark-theme resolver.
- `:core:datastore` owns the narrow theme-mode DataStore preference. Broader settings persistence remains deferred to Phase 3.
- Focus, Insights, and Settings feature modules use `:core:designsystem` and `:core:model` directly for static mockup-mapped UI and test tags.

## Current Phase 4 Notes

- `:core:common` now owns pure `Clock` and `IdGenerator` abstractions used by domain logic for deterministic tests and platform-independent runtime behavior.
- `:domain:session` owns the session state machine, interruption rules, early-end classification, and conservative persistence recovery classification.
- `:domain:session` depends only on `:core:common` and `:core:model`, including the `SessionRepository` contract from `:core:model`.
- Repository orchestration for session and penalty-event writes lives in `:domain:session` use cases rather than in UI or service layers.

## Current Phase 5 Notes

- `:core:sensors` now owns Android `SensorManager` integration, semantic focus-validity results, pocket/movement rejection heuristics, and debug-only diagnostics data.
- `:core:sensors` keeps Android APIs at the edge through `AndroidFocusValidityMonitor`, while `FocusValidityEvaluator` contains the threshold and classification logic in a unit-testable form.
- Later layers should consume semantic validity output from `:core:sensors` and map it into `:domain:session` inputs rather than reading raw sensor events directly.
