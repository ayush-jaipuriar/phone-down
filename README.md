# Phone Down

Phone Down is a native Android focus timer where focus sessions only progress while the phone is placed face down and stable.

The product requirements and architecture live in [architecture.md](architecture.md). The full V1 roadmap lives in [v1-implementation-plan.md](v1-implementation-plan.md), and module boundary rules live in [docs/module-dependency-rules.md](docs/module-dependency-rules.md).

## Local Requirements

- Android Studio with Android SDK platform 36 installed.
- JDK 17 or newer available to Gradle.
- No global Gradle install is required; use the project Gradle wrapper.

## Setup

Open this repository directly in Android Studio and let Gradle sync.

For command-line builds, run:

```bash
./gradlew :app:assembleDebug
```

## Verification

Run the local check script before considering an implementation pass complete:

```bash
./scripts/check.sh
```

The check script runs Kotlin formatting checks, static analysis, Android lint, unit tests, and a debug build.

Phase 2 also adds Paparazzi screenshot verification and Compose UI-test APK compilation to the local check script. To run those checks directly:

```bash
./gradlew :feature:focus:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug
./gradlew :feature:focus:assembleDebugAndroidTest :feature:insights:assembleDebugAndroidTest :feature:settings:assembleDebugAndroidTest
```

Connected UI tests require an attached Android device or emulator:

```bash
adb devices
./gradlew :feature:focus:connectedDebugAndroidTest :feature:insights:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest
```

## Project Structure

- `:app` owns application startup, Hilt entry points, and Compose Navigation.
- `:feature:*` modules own screen composables and feature UI surfaces.
- `:domain:*` modules own testable product rules without Android UI dependencies.
- `:core:*` modules own shared models, design system code, platform integrations, and infrastructure.
- `build-logic` contains Gradle convention plugins used to keep module build files small and consistent.

## Secrets Safety

Do not commit local properties, environment files, signing keys, OAuth client secrets, service-account files, backup files, or generated credentials. Use placeholders in documentation and keep real credentials outside the repository.
