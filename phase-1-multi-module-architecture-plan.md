# Phase 1 Plan - Multi-Module Architecture

This document is the detailed implementation plan for Phase 1 of Phone Down V1. It must be reviewed and approved before implementation begins.

Phase 1 turns the Phase 0 skeleton into a more durable architecture foundation. The goal is to reduce Gradle duplication, formalize module dependency direction, add Hilt wiring, and create a real Compose Navigation shell with placeholder routes before product features begin.

## 0. Implementation Status

- [x] Phase 1 plan approved by user.
- [x] Implementation completed.
- [x] Verification completed successfully on May 1, 2026.
- [x] Progress documentation updated.

### Implementation Notes

- Added `build-logic` as an included build with lightweight convention plugins for Android application modules, Android library modules, Compose library modules, Kotlin JVM modules, and Hilt-enabled modules.
- Migrated app, core, domain, and feature module Gradle files to convention plugins while keeping module-specific namespaces and dependencies local to each module.
- Added Hilt foundation with `PhoneDownApplication`, manifest registration, and `MainActivity` entry-point annotation.
- Added a real Compose Navigation shell starting at onboarding, bottom tabs for Focus, Insights, and Settings, and placeholder Account/Pro flows from Settings.
- Added placeholder screen composables in their owning feature modules.
- Added route smoke tests and module dependency documentation.

### Verification Evidence

- [x] `./gradlew projects`
- [x] `./gradlew tasks`
- [x] `./scripts/check.sh`
- [x] `./scripts/check.sh` covered `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, and `:app:assembleDebug`.

### Manual Smoke Status

- [ ] Emulator/device manual smoke test not run in this phase. Automated build, lint, static analysis, and unit checks passed.

## 1. Confirmed Decisions

- [x] Include Hilt dependency injection wiring in Phase 1.
- [x] Create a real Compose Navigation shell with placeholder routes.
- [x] Include routes for `Onboarding`, `Focus`, `Insights`, `Settings`, `Account`, and `Pro`.
- [x] Start the app at onboarding as the initial route.
- [x] Add lightweight Gradle convention plugins under `build-logic`.
- [x] Document dependency rules now.
- [x] Defer automated dependency-boundary enforcement until the architecture settles.
- [x] Keep placeholder screens plain but routed through the design system.

## 2. Phase 1 Purpose

Phase 1 exists to make the project pleasant and safe to extend.

Without this phase, later implementation would repeat Gradle configuration across many modules, create inconsistent dependency patterns, and mix navigation decisions into feature work. By doing this now, later phases can focus on behavior.

## 3. Phase 1 Scope

### In Scope

- [x] Add `build-logic` included build.
- [x] Add convention plugins for Android app, Android library, Android Compose library, Kotlin JVM library, and Hilt-enabled modules.
- [x] Migrate modules from repeated Gradle config to convention plugins.
- [x] Add Hilt dependencies and plugins.
- [x] Create an application class annotated for Hilt.
- [x] Annotate `MainActivity` as an Android entry point.
- [x] Add basic app-level navigation host.
- [x] Add typed or centralized route definitions.
- [x] Add placeholder screens for onboarding, focus, insights, settings, account, and pro.
- [x] Add bottom navigation for Focus, Insights, and Settings.
- [x] Start at onboarding.
- [x] Add simple navigation tests or unit-level route tests where feasible.
- [x] Document module dependency rules.
- [x] Update README and implementation progress logs.

### Out Of Scope

- [ ] Real onboarding persistence.
- [ ] Real first-launch/returning-user routing logic.
- [ ] Real Focus UI matching mockups.
- [ ] Real Insights UI.
- [ ] Real Settings UI.
- [ ] Real Hilt modules for repositories/use cases.
- [ ] Room, DataStore, sensors, billing, auth, backup implementation.
- [ ] Automated dependency graph enforcement.

## 4. Target Repository Additions

```text
phone-down/
  build-logic/
    settings.gradle.kts
    build.gradle.kts
    convention/
      build.gradle.kts
      src/main/kotlin/
        phonedown.android.application.gradle.kts
        phonedown.android.library.gradle.kts
        phonedown.android.compose.library.gradle.kts
        phonedown.kotlin.library.gradle.kts
        phonedown.hilt.gradle.kts
  app/src/main/java/phonedown/app/
    PhoneDownApplication.kt
    MainActivity.kt
    navigation/
      PhoneDownNavHost.kt
      PhoneDownRoute.kt
      PhoneDownBottomTab.kt
  feature/*/
    ... placeholder screen composables
  docs/
    module-dependency-rules.md
```

If `docs/` does not exist, Phase 1 should create it.

## 5. Build Logic Plan

### Why Add Convention Plugins

The project has many modules. Repeating `compileSdk`, `minSdk`, Kotlin toolchain, Compose compiler, lint defaults, and test dependencies across modules creates drift. Lightweight convention plugins let future phases add modules and dependencies with less boilerplate.

### Included Build Checklist

- [x] Create `build-logic/settings.gradle.kts`.
- [x] Create `build-logic/build.gradle.kts`.
- [x] Create `build-logic/convention/build.gradle.kts`.
- [x] Add `includeBuild("build-logic")` to root `settings.gradle.kts`.
- [x] Confirm Gradle can resolve convention plugin IDs.

### Convention Plugin Checklist

- [x] `phonedown.android.application`
  - [x] Applies Android application plugin.
  - [x] Applies Kotlin Android plugin.
  - [x] Configures `compileSdk = 36`.
  - [x] Configures `minSdk = 26`.
  - [x] Configures `targetSdk = 36`.
  - [x] Configures Kotlin JVM toolchain 17.
  - [x] Configures common Android test runner if needed.
- [x] `phonedown.android.library`
  - [x] Applies Android library plugin.
  - [x] Applies Kotlin Android plugin.
  - [x] Configures `compileSdk = 36`.
  - [x] Configures `minSdk = 26`.
  - [x] Configures Kotlin JVM toolchain 17.
- [x] `phonedown.android.compose.library`
  - [x] Applies Android library convention.
  - [x] Applies Compose compiler plugin.
  - [x] Adds Compose BOM/runtime/ui baseline dependencies.
- [x] `phonedown.kotlin.library`
  - [x] Applies Kotlin JVM plugin.
  - [x] Configures JVM toolchain 17.
  - [x] Adds common unit test dependency if appropriate.
- [x] `phonedown.hilt`
  - [x] Applies Hilt plugin.
  - [x] Applies KSP or KAPT support depending on selected Hilt setup.
  - [x] Adds Hilt Android and compiler dependencies.

### Migration Checklist

- [x] Migrate `:app` to `phonedown.android.application`.
- [x] Migrate pure JVM modules to `phonedown.kotlin.library`.
- [x] Migrate Android non-Compose modules to `phonedown.android.library`.
- [x] Migrate Compose-capable library modules to `phonedown.android.compose.library`.
- [x] Apply `phonedown.hilt` to `:app`.
- [x] Keep module-specific namespace and dependencies in each module file.
- [x] Remove repeated SDK/toolchain boilerplate from module files.
- [x] Keep version catalog as the single version source.

### Acceptance Criteria

- [x] Build files are shorter and easier to scan.
- [x] All modules still compile.
- [x] Convention plugins do not hide important module-specific dependencies.
- [x] Future modules can adopt a plugin with minimal copy/paste.

## 6. Hilt Wiring Plan

Phase 1 should establish Hilt infrastructure only. Real modules and bindings should wait until repositories and use cases exist.

### Dependency Checklist

- [x] Add Hilt plugin alias to version catalog.
- [x] Add KSP or KAPT plugin alias to version catalog.
- [x] Add Hilt runtime dependency alias.
- [x] Add Hilt compiler dependency alias.
- [x] Add Hilt navigation Compose dependency only if needed in this phase.

### Implementation Checklist

- [x] Create `PhoneDownApplication`.
- [x] Annotate application with `@HiltAndroidApp`.
- [x] Register application class in `AndroidManifest.xml`.
- [x] Annotate `MainActivity` with `@AndroidEntryPoint`.
- [x] Keep `MainActivity` free of injected dependencies for now.
- [x] Add placeholder DI package only if needed for build structure.

### Tests And Verification

- [x] Confirm app compiles with Hilt.
- [x] Confirm KSP/KAPT generated code does not enter git.
- [x] Confirm no real bindings are added prematurely.

### Acceptance Criteria

- [x] Hilt is available to later feature ViewModels and repositories.
- [x] App shell builds with Hilt enabled.
- [x] No production behavior depends on Hilt yet.

## 7. Navigation Shell Plan

Phase 1 should create the real route skeleton so later feature work slots into known destinations.

### Route Definitions

Create centralized route definitions for:

- [x] `Onboarding`.
- [x] `Focus`.
- [x] `Insights`.
- [x] `Settings`.
- [x] `Account`.
- [x] `Pro`.

The implementation may use sealed interfaces/classes, objects, or a simple enum-like structure. The priority is clarity and type-safe-ish reuse, not over-abstraction.

### NavHost Checklist

- [x] Create `PhoneDownNavHost`.
- [x] Use `NavHost` from Navigation Compose.
- [x] Start destination is `Onboarding`.
- [x] Add composable destination for onboarding.
- [x] Add composable destination for focus.
- [x] Add composable destination for insights.
- [x] Add composable destination for settings.
- [x] Add composable destination for account.
- [x] Add composable destination for pro.
- [x] Keep route arguments out of Phase 1 unless needed.

### Bottom Navigation Checklist

- [x] Bottom tabs: Focus, Insights, Settings.
- [x] Do not show bottom navigation on onboarding.
- [x] Show bottom navigation on Focus, Insights, and Settings.
- [x] Decide whether Account/Pro show bottom navigation based on route context.
- [x] Use design system placeholder styling.
- [x] Avoid production polish in Phase 1.

### Navigation Behavior

- [x] Onboarding placeholder includes a way to continue to Focus for manual testing.
- [x] Focus placeholder can navigate to Settings if needed.
- [x] Settings placeholder can navigate to Account and Pro.
- [x] Bottom tab reselection should avoid building a large back stack.
- [x] Back behavior should be simple and predictable.

### Acceptance Criteria

- [x] App launches to onboarding placeholder.
- [x] User can navigate from onboarding to Focus.
- [x] User can switch between Focus, Insights, and Settings.
- [x] User can reach Account and Pro placeholders from Settings.
- [x] Bottom navigation is hidden during onboarding.

## 8. Placeholder Screen Plan

Placeholder screens should prove routing and module ownership without pretending to be final UI.

### Design Direction

- [x] Plain but clean.
- [x] Use `PhoneDownTheme`.
- [x] Use simple text labels.
- [x] Use minimal buttons for navigation.
- [x] Avoid mockup-specific timer/charts/settings UI until later phases.

### Feature Module Screen Ownership

- [x] `:feature:onboarding` owns `OnboardingRoute` or `OnboardingScreen`.
- [x] `:feature:focus` owns `FocusRoute` or `FocusScreen`.
- [x] `:feature:insights` owns `InsightsRoute` or `InsightsScreen`.
- [x] `:feature:settings` owns `SettingsRoute` or `SettingsScreen`.
- [x] `:feature:account` owns `AccountRoute` or `AccountScreen`.
- [x] `:feature:pro` owns `ProRoute` or `ProScreen`.

### Acceptance Criteria

- [x] App module coordinates navigation.
- [x] Feature modules own screen composables.
- [x] Placeholder screens compile independently through module dependencies.
- [x] No feature module depends on `:app`.

## 9. Dependency Rules Documentation

Create `docs/module-dependency-rules.md`.

### Document Checklist

- [x] Explain module categories.
- [x] Explain dependency direction.
- [x] Explain app module responsibilities.
- [x] Explain feature module responsibilities.
- [x] Explain domain module responsibilities.
- [x] Explain core module responsibilities.
- [x] Explain what should not cross boundaries.
- [x] Include examples of allowed dependencies.
- [x] Include examples of disallowed dependencies.
- [x] Note that automated enforcement is deferred.

### Initial Rules

- [x] `:app` may depend on feature modules and app-level core modules.
- [x] Feature modules may depend on domain modules, `:core:model`, `:core:common`, and `:core:designsystem`.
- [x] Domain modules may depend on `:core:model` and `:core:common`.
- [x] Core implementation modules may depend on `:core:model` and `:core:common`.
- [x] Core modules should not depend on feature modules.
- [x] Domain modules should not depend on Android UI or Compose.
- [x] Feature modules should not depend on `:app`.
- [x] Navigation is coordinated by `:app`.

### Acceptance Criteria

- [x] Dependency rules are documented in a standalone file.
- [x] Documentation reflects actual Phase 1 module dependencies.
- [x] Any intentional exception is documented.

## 10. Testing Plan

Phase 1 is architecture-heavy, so tests focus on build integrity, route constants, and smoke validation.

### Automated Checks

- [x] `./gradlew projects`.
- [x] `./gradlew tasks`.
- [x] `./gradlew :app:assembleDebug`.
- [x] `./gradlew testDebugUnitTest`.
- [x] `./gradlew ktlintCheck`.
- [x] `./gradlew detekt`.
- [x] `./gradlew lintDebug`.
- [x] `./scripts/check.sh`.

### Test Additions

- [x] Add route constant tests if route definitions are pure enough to test.
- [x] Keep existing smoke tests passing.
- [x] Add navigation-related unit test only if feasible without heavy instrumentation.

### Manual Smoke Check

If an emulator/device is available:

- [ ] Launch app.
- [ ] Confirm onboarding is first screen.
- [ ] Tap through to Focus placeholder.
- [ ] Switch tabs.
- [ ] Navigate to Account and Pro from Settings.

If no emulator/device is available, document that manual smoke testing was not run.

## 11. Documentation Updates

### Required Updates

- [x] Update this Phase 1 plan checklist during implementation.
- [x] Add progress entry to `v1-implementation-plan.md`.
- [x] Update README if commands, requirements, or project structure change.
- [x] Add `docs/module-dependency-rules.md`.
- [x] Document any deviation from this plan.

## 12. Implementation Steps

Implementation should proceed in this order after approval.

### Step 1 - Add Build Logic

- [x] Create `build-logic`.
- [x] Add convention plugin project.
- [x] Add convention plugin scripts.
- [x] Include build logic from root settings.
- [x] Verify Gradle resolves plugin IDs.

### Step 2 - Migrate Build Files

- [x] Migrate `:app`.
- [x] Migrate core modules.
- [x] Migrate domain modules.
- [x] Migrate feature modules.
- [x] Run Gradle sync/build command.
- [x] Fix convention gaps.

### Step 3 - Add Hilt Foundation

- [x] Add Hilt/KSP dependencies.
- [x] Apply Hilt convention to app.
- [x] Create `PhoneDownApplication`.
- [x] Register application class.
- [x] Annotate `MainActivity`.
- [x] Verify generated code is ignored.

### Step 4 - Add Navigation Dependencies

- [x] Add Navigation Compose dependency.
- [x] Add route definitions.
- [x] Add navigation host.
- [x] Add bottom tab model.
- [x] Wire `MainActivity` to app shell.

### Step 5 - Add Placeholder Screens

- [x] Add onboarding placeholder.
- [x] Add focus placeholder.
- [x] Add insights placeholder.
- [x] Add settings placeholder.
- [x] Add account placeholder.
- [x] Add pro placeholder.
- [x] Wire screen callbacks through navigation host.

### Step 6 - Add Dependency Rules Documentation

- [x] Create docs directory.
- [x] Add module dependency rules doc.
- [x] Cross-check rules with actual dependencies.

### Step 7 - Verify And Update Docs

- [x] Run full verification suite.
- [x] Update Phase 1 checklists.
- [x] Update V1 progress log.
- [x] Report results.

## 13. Risks And Mitigations

### Risk: Convention Plugins Hide Too Much

- Mitigation: keep convention plugins small and leave namespace/dependencies in module build files.

### Risk: Hilt Adds Build Complexity Too Early

- Mitigation: wire only app/application/activity Hilt setup. Do not add unnecessary modules or bindings.

### Risk: Navigation Shell Becomes Premature Product UI

- Mitigation: keep placeholders plain. Real UI arrives in later phases.

### Risk: Onboarding Initial Route Conflicts With Future Returning-User Logic

- Mitigation: start at onboarding now for shell proof. Later onboarding phase will add completion persistence and conditional start routing.

### Risk: Dependency Rules Drift From Reality

- Mitigation: document rules after implementation and note any intentional exceptions.

## 14. Completion Criteria

Phase 1 is complete only when:

- [x] `build-logic` exists and is used by modules.
- [x] Repeated Gradle boilerplate is reduced.
- [x] Hilt application/activity foundation compiles.
- [x] Real Compose Navigation shell exists.
- [x] App starts at onboarding placeholder.
- [x] Focus, Insights, and Settings bottom tabs work.
- [x] Account and Pro placeholders are reachable.
- [x] Dependency rules are documented.
- [x] Full verification suite passes.
- [x] Progress documentation is updated.
- [x] User is informed of verification results.

## 15. Approval Gate

Implementation must not begin until this Phase 1 plan is approved.

Approval options:

- Approve Phase 1 as written and begin implementation.
- Request edits to build-logic scope.
- Request edits to Hilt setup.
- Request edits to navigation behavior.
- Request stricter or lighter dependency documentation.
