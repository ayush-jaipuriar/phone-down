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
run_step "Unit tests" ./gradlew testDebugUnitTest
run_step "Debug build" ./gradlew :app:assembleDebug
