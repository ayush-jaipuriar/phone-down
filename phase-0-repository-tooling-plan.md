# Phase 0 Plan - Repository And Tooling Foundation

This document is the detailed implementation plan for Phase 0 of Phone Down V1. It must be reviewed and approved before implementation begins.

Phase 0 establishes the native Android project foundation directly inside this repository. It should leave the repo in a state where Android Studio can open and sync the project, Gradle can build a minimal app, quality tooling is wired, all planned module folders exist, and future phases can proceed without revisiting base project structure.

## 1. Confirmed Decisions

- [x] Scaffold a standard Android Studio Gradle project directly in this repo.
- [x] Use package/application ID: `phonedown.app`.
- [x] Use Gradle Kotlin DSL.
- [x] Use recommended SDK baseline.
- [x] Create all planned module folders/modules during Phase 0.
- [x] Configure quality tooling during Phase 0.
- [x] Add a local CI-style verification script.

## 2. Recommended SDK And Tooling Baseline

The exact versions should be chosen from stable Android tooling available during implementation. The implementation should avoid preview/canary tooling unless required.

### Android SDK Recommendation

- [x] `minSdk`: 26.
- [x] `targetSdk`: latest stable installed/available at implementation time.
- [x] `compileSdk`: latest stable installed/available at implementation time.

### Why `minSdk 26`

`minSdk 26` is a conservative modern baseline for V1:

- It keeps compatibility broad enough for many Android users.
- It avoids some older Android background-service and notification complexity.
- It supports a more predictable foreground service and notification foundation.
- It is compatible with a modern Compose-first app.

If a dependency or Play policy requires a higher target/compile SDK, target/compile should move up while keeping `minSdk 26` unless there is a concrete reason to raise it.

## 3. Phase 0 Scope

Phase 0 should create the project skeleton and development rails only. It should not implement product behavior such as session timing, sensor logic, billing, backup, onboarding, or production UI screens.

### In Scope

- [ ] Android Gradle project scaffold.
- [ ] Kotlin DSL Gradle files.
- [ ] Version catalog.
- [ ] Minimal Compose app entry point.
- [ ] All planned modules registered in Gradle.
- [ ] Basic module dependency conventions.
- [ ] Basic dependency injection setup only if needed for compilation skeleton.
- [ ] Quality tooling setup.
- [ ] Local verification script.
- [ ] `.gitignore` hardening.
- [ ] README setup instructions.
- [ ] Documentation progress-log updates.

### Out Of Scope

- [ ] Real session engine implementation.
- [ ] Sensor detection implementation.
- [ ] Room schema implementation.
- [ ] DataStore preferences implementation.
- [ ] Billing implementation.
- [ ] Google Sign-In implementation.
- [ ] Google Drive backup implementation.
- [ ] Production Focus/Insights/Settings UI.
- [ ] Navigation beyond placeholder shells if needed to prove compilation.

## 4. Target Repository Shape

The implementation should create a normal Android project layout at the repository root.

```text
phone-down/
  AGENTS.md
  README.md
  architecture.md
  v1-implementation-plan.md
  phase-0-repository-tooling-plan.md
  settings.gradle.kts
  build.gradle.kts
  gradle.properties
  gradlew
  gradlew.bat
  gradle/
    wrapper/
    libs.versions.toml
  app/
  core/
    common/
    model/
    designsystem/
    database/
    datastore/
    sensors/
    notifications/
    billing/
    auth/
    backup/
    charts/
  domain/
    session/
    insights/
  feature/
    onboarding/
    focus/
    insights/
    settings/
    account/
    pro/
  scripts/
    check.sh
  ui-mockups/
```

## 5. Gradle And Build Setup

### Root Gradle Files

- [ ] Create `settings.gradle.kts`.
- [ ] Create root `build.gradle.kts`.
- [ ] Create `gradle.properties`.
- [ ] Create or install Gradle wrapper.
- [ ] Create `gradle/libs.versions.toml`.

### Version Catalog

Add stable aliases for the categories below. Exact versions should be selected during implementation based on current stable compatibility.

- [ ] Android Gradle Plugin.
- [ ] Kotlin.
- [ ] Compose BOM.
- [ ] AndroidX Core.
- [ ] Activity Compose.
- [ ] Lifecycle.
- [ ] Navigation Compose.
- [ ] Hilt.
- [ ] Room.
- [ ] DataStore.
- [ ] Kotlin Coroutines.
- [ ] WorkManager.
- [ ] Play Billing.
- [ ] Google Sign-In/Auth dependencies.
- [ ] Google Drive API dependencies.
- [ ] Vico charts.
- [ ] JUnit.
- [ ] AndroidX Test.
- [ ] Turbine or equivalent Flow testing helper if adopted.
- [ ] ktlint.
- [ ] detekt.

### Gradle Plugin Strategy

- [ ] Use Kotlin DSL throughout.
- [ ] Prefer version catalog aliases for plugins and libraries.
- [ ] Keep common Android config centralized where practical.
- [ ] Avoid over-engineered convention plugins unless they clearly reduce duplication in this initial setup.
- [ ] If convention plugins are introduced, document why and keep them minimal.

### Acceptance Criteria

- [ ] `./gradlew tasks` runs.
- [ ] `./gradlew projects` shows all planned modules.
- [ ] `./gradlew :app:assembleDebug` builds a minimal app.
- [ ] Android Studio can open and sync the project.

## 6. Module Creation Plan

Create all modules listed in the V1 architecture during Phase 0 so later phases can work inside stable boundaries.

### App Module

- [ ] `:app`
  - [ ] Android application module.
  - [ ] Application ID `phonedown.app`.
  - [ ] Minimal `MainActivity`.
  - [ ] Minimal Compose content.
  - [ ] App name `Phone Down`.
  - [ ] Basic manifest.

### Core Modules

- [ ] `:core:common`
- [ ] `:core:model`
- [ ] `:core:designsystem`
- [ ] `:core:database`
- [ ] `:core:datastore`
- [ ] `:core:sensors`
- [ ] `:core:notifications`
- [ ] `:core:billing`
- [ ] `:core:auth`
- [ ] `:core:backup`
- [ ] `:core:charts`

### Domain Modules

- [ ] `:domain:session`
- [ ] `:domain:insights`

### Feature Modules

- [ ] `:feature:onboarding`
- [ ] `:feature:focus`
- [ ] `:feature:insights`
- [ ] `:feature:settings`
- [ ] `:feature:account`
- [ ] `:feature:pro`

### Module Type Guidance

- [ ] `:app` should be an Android application module.
- [ ] UI-related modules should be Android library modules with Compose enabled where needed.
- [ ] Domain modules should be Kotlin/JVM modules if feasible.
- [ ] Pure model/common modules should be Kotlin/JVM modules if feasible.
- [ ] Android-dependent core modules should be Android library modules.

### Module Dependency Rules For Phase 0

Phase 0 should keep dependencies intentionally sparse:

- [ ] `:app` may depend on feature modules and `:core:designsystem`.
- [ ] Feature modules may depend on `:core:designsystem`, `:core:model`, and relevant domain modules.
- [ ] Domain modules may depend on `:core:model` and `:core:common`.
- [ ] Android implementation modules may depend on `:core:model` and `:core:common`.
- [ ] Avoid adding dependencies that are not needed for the skeleton to compile.

### Acceptance Criteria

- [ ] All modules are included in `settings.gradle.kts`.
- [ ] All modules compile.
- [ ] Placeholder source files exist where needed to avoid empty-module confusion.
- [ ] No module contains product logic yet.

## 7. Minimal App Shell

Phase 0 should include only enough UI to prove Compose, theme wiring, and app launch behavior.

### Checklist

- [ ] Create `MainActivity`.
- [ ] Set Compose content.
- [ ] Display a minimal `Phone Down` placeholder screen.
- [ ] Use Material 3 dependency if selected.
- [ ] Avoid building production Focus/Insights/Settings UI in Phase 0.
- [ ] Avoid mockup implementation in Phase 0.

### Acceptance Criteria

- [ ] App launches to a simple placeholder.
- [ ] Placeholder confirms the Android/Compose foundation works.
- [ ] No user-facing feature behavior is implied to be complete.

## 8. Quality Tooling Plan

Quality tooling belongs in Phase 0 so later feature work immediately benefits from consistent checks.

### Android Lint

- [ ] Ensure Android lint can run.
- [ ] Configure lint behavior conservatively.
- [ ] Avoid suppressing real issues globally.
- [ ] Document any initial suppressions if required.

### ktlint

- [ ] Add ktlint plugin or Gradle integration.
- [ ] Configure Kotlin formatting rules.
- [ ] Add ktlint check task to local verification script.
- [ ] Add ktlint format task availability for developer use.

### detekt

- [ ] Add detekt plugin.
- [ ] Add baseline config only if necessary.
- [ ] Prefer a reasonable default config at first.
- [ ] Add detekt task to local verification script.
- [ ] Avoid making Phase 0 fail on rules that are unreasonable for generated/skeleton code.

### Unit Test Foundation

- [ ] Add JUnit test dependencies.
- [ ] Add at least one tiny test in a pure module or app module to prove test execution.
- [ ] Ensure `./gradlew test` or equivalent runs.

### Acceptance Criteria

- [ ] Lint task runs.
- [ ] ktlint task runs.
- [ ] detekt task runs.
- [ ] Unit test task runs.
- [ ] Quality tasks are included in `scripts/check.sh`.

## 9. Local Verification Script

Create `scripts/check.sh` as the local CI-style entrypoint.

### Script Requirements

- [ ] Use `bash`.
- [ ] Use `set -euo pipefail`.
- [ ] Run from repo root reliably.
- [ ] Execute Gradle wrapper commands.
- [ ] Include build check.
- [ ] Include unit tests.
- [ ] Include lint.
- [ ] Include ktlint.
- [ ] Include detekt.
- [ ] Print clear section headings.

### Initial Command Set

The exact tasks depend on chosen plugin names, but the script should aim to run the equivalent of:

```bash
./gradlew \
  ktlintCheck \
  detekt \
  lintDebug \
  testDebugUnitTest \
  assembleDebug
```

If some tasks have different names because of project structure, use the correct generated tasks and document them.

### Acceptance Criteria

- [ ] `scripts/check.sh` is executable.
- [ ] `scripts/check.sh` runs all Phase 0 verification tasks.
- [ ] Any skipped/unavailable task is documented with a reason.

## 10. Gitignore And Secret Safety

The existing `.gitignore` already covers common Android/Gradle/IDE/build outputs. Phase 0 should harden it for upcoming auth, billing, backup, and signing work.

### Existing Coverage To Preserve

- [x] `.gradle/`
- [x] `build/`
- [x] `local.properties`
- [x] Android Studio files.
- [x] APK/AAB outputs.
- [x] keystore patterns.
- [x] `google-services.json`
- [x] logs.

### Add Or Confirm Coverage

- [ ] `.env`
- [ ] `.env.*`
- [ ] `*.bak`
- [ ] `*.backup`
- [ ] `*.key`
- [ ] `*.pem`
- [ ] `*.p12`
- [ ] `credentials/`
- [ ] `service-account*.json`
- [ ] `client_secret*.json`
- [ ] Any local OAuth/testing config files created during implementation.

### Acceptance Criteria

- [ ] No generated secret/config file is tracked.
- [ ] No backup files are tracked.
- [ ] Git status remains understandable after Gradle sync/build.

## 11. README Updates

The current README is only a title. Phase 0 should expand it enough for a developer to set up the project locally.

### Checklist

- [ ] Add project description.
- [ ] Add requirements:
  - [ ] Android Studio.
  - [ ] JDK version.
  - [ ] Android SDK/compile SDK.
  - [ ] Gradle wrapper usage.
- [ ] Add setup steps.
- [ ] Add build command.
- [ ] Add test/check command.
- [ ] Add note that secrets/local properties must not be committed.
- [ ] Add pointer to `architecture.md`.
- [ ] Add pointer to `v1-implementation-plan.md`.

### Acceptance Criteria

- [ ] A new developer can run the basic project from README instructions.
- [ ] README does not include real credentials.
- [ ] README uses placeholders only where config is discussed.

## 12. Documentation Updates During Phase 0

### Required Updates

- [ ] Update this Phase 0 plan checklist as implementation progresses.
- [ ] Add a progress-log entry to `v1-implementation-plan.md`.
- [ ] Update README setup/build instructions.
- [ ] Document any deviations from this Phase 0 plan.

### Progress Log Entry Should Include

- [ ] Date.
- [ ] Summary.
- [ ] Files modified.
- [ ] Modules created.
- [ ] Tooling configured.
- [ ] Tests/checks run.
- [ ] Remaining risks or next steps.

## 13. Phase 0 Implementation Steps

Implementation should proceed in this order after plan approval.

### Step 1 - Scaffold Gradle Project

- [x] Create Gradle wrapper.
- [x] Create root Gradle files.
- [x] Create version catalog.
- [x] Configure repositories.
- [x] Configure plugin aliases.
- [x] Verify Gradle task listing.

### Step 2 - Create App Module

- [x] Create `app/build.gradle.kts`.
- [x] Create manifest.
- [x] Create `MainActivity`.
- [x] Add minimal Compose placeholder.
- [x] Configure app ID `phonedown.app`.
- [x] Configure SDK values.
- [x] Verify `:app:assembleDebug`.

### Step 3 - Create All Planned Modules

- [x] Create core modules.
- [x] Create domain modules.
- [x] Create feature modules.
- [x] Register modules in settings.
- [x] Add minimal build files.
- [x] Add placeholder source where appropriate.
- [x] Verify all modules compile.

### Step 4 - Add Quality Tooling

- [x] Add ktlint.
- [x] Add detekt.
- [x] Confirm Android lint.
- [x] Add sample unit test.
- [x] Verify each quality task independently.

### Step 5 - Add Local Check Script

- [x] Create `scripts/check.sh`.
- [x] Make executable.
- [x] Add build/test/lint/format checks.
- [x] Run script.
- [x] Fix any failures.

### Step 6 - Harden Ignore Rules

- [x] Update `.gitignore`.
- [x] Run build/check to reveal generated files.
- [x] Confirm generated files are ignored.
- [x] Confirm no sensitive files are present.

### Step 7 - Update Documentation

- [x] Update README.
- [x] Update this plan's checklist.
- [x] Update `v1-implementation-plan.md` progress log.

## 14. Verification Plan

After implementation, run a comprehensive Phase 0 verification suite.

### Required Commands

- [x] `./gradlew projects`
- [x] `./gradlew tasks`
- [x] `./gradlew :app:assembleDebug`
- [x] `./gradlew testDebugUnitTest` or project-equivalent unit test command.
- [x] `./gradlew lintDebug`
- [x] `./gradlew ktlintCheck`
- [x] `./gradlew detekt`
- [x] `./scripts/check.sh`
- [x] `git status --short`

### Expected Result

- [x] All Gradle commands pass.
- [x] Local check script passes.
- [x] Git status shows only intentional source/documentation changes.
- [x] No untracked generated secrets or backup files appear.

## 15. Risks And Mitigations

### Risk: Module Setup Becomes Too Heavy Too Early

- Mitigation: create modules and compile skeletons only; defer real dependencies and product logic to later phases.

### Risk: Quality Tooling Blocks Skeleton Work With Noisy Rules

- Mitigation: start with practical defaults, avoid excessive strictness, and document any temporary baseline/config choices.

### Risk: Android Tooling Version Conflicts

- Mitigation: use stable compatible AGP/Kotlin/Compose versions and verify with Gradle sync/build before adding extra dependencies.

### Risk: Generated Files Pollute Git Status

- Mitigation: harden `.gitignore`, run checks, and inspect `git status` before reporting completion.

### Risk: Secrets Enter The Repo During Auth/Billing Prep

- Mitigation: Phase 0 should not require real credentials. Add ignore rules now for future local config files.

## 16. Completion Criteria

Phase 0 is complete only when:

- [x] Android Studio project exists directly in this repo.
- [x] Kotlin DSL Gradle build is configured.
- [x] Application ID is `phonedown.app`.
- [x] Recommended SDK baseline is applied.
- [x] All planned modules exist and compile.
- [x] Minimal app launches/builds.
- [x] Quality tooling is configured.
- [x] Local check script exists and passes.
- [x] README is updated.
- [x] Progress documentation is updated.
- [x] No secrets or suspicious generated files are tracked.
- [x] User is informed of verification results.

## 17. Approval Gate

Implementation must not begin until this Phase 0 plan is approved.

Approval options:

- Approve Phase 0 as written and begin implementation.
- Request edits to SDK/tooling/module choices.
- Request a lighter Phase 0 scope.
- Request stricter quality/tooling requirements.
