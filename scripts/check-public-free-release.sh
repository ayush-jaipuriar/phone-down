#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  printf 'Public-free release check failed: %s\n' "$1" >&2
  exit 1
}

assert_contains() {
  local file="$1"
  local text="$2"
  grep -Fq "$text" "$file" || fail "$file must contain: $text"
}

assert_absent() {
  local text="$1"
  shift
  if grep -Fq "$text" "$@"; then
    fail "stale text found in current release surfaces: $text"
  fi
}

CURRENT_RELEASE_DOCS=(
  docs/privacy-policy.md
  docs/account-deletion.md
  docs/play-store-data-safety.md
  docs/release-readiness.md
  fastlane/metadata/android/en-US/full_description.txt
  fastlane/metadata/android/en-US/changelogs/default.txt
  fastlane/metadata/android/en-US/changelogs/5.txt
)

assert_absent "Phone Down Pro users" "${CURRENT_RELEASE_DOCS[@]}"
assert_absent "support@phonedown.app" "${CURRENT_RELEASE_DOCS[@]}" \
  feature/settings/src/main/kotlin/phonedown/feature/settings/PrivacyPolicyScreen.kt
assert_absent "Purchase history is handled" "${CURRENT_RELEASE_DOCS[@]}"
assert_absent "Auth tokens are stored in encrypted preferences" "${CURRENT_RELEASE_DOCS[@]}"
assert_absent "Certificate pinning" "${CURRENT_RELEASE_DOCS[@]}"

assert_contains docs/privacy-policy.md "jaipuriar.ayush@gmail.com"
assert_contains docs/privacy-policy.md "automatically send crash reports"
assert_contains docs/privacy-policy.md "export your focus history as a CSV file"
assert_contains docs/play-store-data-safety.md "Device or other IDs | Yes | Required in release builds"
assert_contains docs/play-store-data-safety.md "Financial or payment data"
assert_contains docs/play-store-data-safety.md "Purchase history"
assert_contains docs/account-deletion.md "disconnect the Google account from Phone Down"
assert_contains feature/settings/src/main/kotlin/phonedown/feature/settings/PrivacyPolicyScreen.kt \
  "Settings > About > Send Feedback"
assert_contains app/build.gradle.kts 'versionCode = 5'
assert_contains app/build.gradle.kts 'versionName = "1.0.4"'
assert_contains fastlane/metadata/android/en-US/full_description.txt "<b>Everything included</b>"
assert_contains fastlane/metadata/android/en-US/changelogs/5.txt "Removed purchase, upgrade, restore, and subscription flows"

if grep -R -Fq "libs.play.billing" app feature/pro --include='*.kts'; then
  fail "billing dependency is present in the public app or Pro feature"
fi

printf 'Public-free release surfaces are consistent.\n'
