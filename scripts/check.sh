#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -z "${ANDROID_HOME:-}" && -d "$HOME/Library/Android/sdk" ]]; then
  export ANDROID_HOME="$HOME/Library/Android/sdk"
fi

run_step() {
  local title="$1"
  shift
  printf '\n==> %s\n' "$title"
  "$@"
}

run_step "Kotlin formatting check" ./gradlew ktlintCheck
run_step "Static analysis" ./gradlew detekt
run_step "Android lint" ./gradlew lintDebug
run_step "Public-free release consistency" ./scripts/check-public-free-release.sh
run_step "Unit tests" ./gradlew testDebugUnitTest
run_step "Screenshot tests" ./gradlew \
  :feature:focus:verifyPaparazziDebug \
  :feature:insights:verifyPaparazziDebug \
  :feature:onboarding:verifyPaparazziDebug \
  :feature:pro:verifyPaparazziDebug \
  :feature:settings:verifyPaparazziDebug
run_step "Compose UI test compile" ./gradlew \
  :feature:account:assembleDebugAndroidTest \
  :feature:focus:assembleDebugAndroidTest \
  :feature:insights:assembleDebugAndroidTest \
  :feature:pro:assembleDebugAndroidTest \
  :feature:settings:assembleDebugAndroidTest
run_step "Debug build" ./gradlew :app:assembleDebug
