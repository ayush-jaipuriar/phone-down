# Phone-First Compatibility Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship Phone Down 1.0.6, version code 7, as a phone-first closed-test release with modernized native dependencies, verified 16 KB compatibility, protected portrait behavior, and complete release QA.

**Architecture:** Keep the supported phone experience portrait-only and add Android 16's temporary restricted-resizability compatibility property at application scope. Replace the two stale native-binary providers with current stable AndroidX releases, prove final APK/AAB alignment and toolchain provenance through a repository script, then promote the artifact only after automated, emulator-containment, physical-device, and Play-installed checks pass.

**Tech Stack:** Kotlin 2.2.21, Android Gradle Plugin 8.13.2, Jetpack Compose BOM 2026.04.01, AndroidX DataStore 1.2.1, AndroidX Graphics Path 1.1.0, Gradle, Bash, bundletool, Android SDK build-tools 36.0.0, NDK LLVM tools, ADB, Google Play Console.

**Spec:** `docs/superpowers/specs/2026-09-06-phone-first-compatibility-hardening-design.md`

## Global Constraints

- Public package remains `phonedown.app`.
- Candidate identity is version code `7`, version name `1.0.6`.
- Minimum SDK remains 26; compile and target SDK remain 36.
- Main phone experience remains portrait-only.
- Add `android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` only as temporary API 36 large-display protection.
- Do not add telephony, screen-size, or model filters to exclude tablets.
- Do not redesign Phone Down for tablets or landscape in this phase.
- Play Billing, purchase flows, prices, subscriptions, and billing permission remain absent from the public runtime.
- Existing focus, summary, duration, history, Insights, backup, export, privacy, and deletion contracts remain unchanged.
- Version 6 remains active until version 7 passes every pre-upload gate.
- Tester recruitment, 14-day eligibility, production application, and production rollout remain deferred.
- Use local builds and the connected RMX3853 before uploading to Play.
- Update implementation and release documentation after each meaningful task.
- Stage explicit paths only and run the complete git/secrets review before every commit.

---

### Task 1: Encode Native Compatibility Evidence

**Files:**
- Create: `scripts/check-native-release-compatibility.sh`
- Modify: `docs/release-readiness.md`

**Interfaces:**
- Consumes: signed APK path, signed AAB path, Android SDK build-tools, Gradle-cached bundletool, NDK `llvm-objdump` or system `objdump`.
- Produces: `scripts/check-native-release-compatibility.sh [apk] [aab]`, returning zero only when packaging alignment, ELF alignment, native-library inventory, and known stale-toolchain checks pass.

- [ ] **Step 1: Record the red baseline from version 6**

Run:

```bash
unzip -Z1 app/build/outputs/apk/release/app-release.apk | rg '^lib/.+\.so$'
```

Expected native inventory:

```text
lib/<abi>/libandroidx.graphics.path.so
lib/<abi>/libdatastore_shared_counter.so
```

Extract one copy per library and inspect its compiler strings:

```bash
work_dir="$(mktemp -d /tmp/phonedown-v6-native.XXXXXX)"
unzip -q app/build/outputs/apk/release/app-release.apk 'lib/arm64-v8a/*.so' -d "$work_dir"
strings "$work_dir/lib/arm64-v8a/libandroidx.graphics.path.so" | rg 'clang version|Linker:'
strings "$work_dir/lib/arm64-v8a/libdatastore_shared_counter.so" | rg 'clang version|Linker:'
```

Expected red evidence includes Graphics Path clang `14.0.7` and DataStore linker `LLD 8.0.7`, matching the pre-remediation version 6 bundle.

- [ ] **Step 2: Add the artifact verification script**

Implement `scripts/check-native-release-compatibility.sh` with this contract:

```bash
#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APK_PATH="${1:-$ROOT_DIR/app/build/outputs/apk/release/app-release.apk}"
AAB_PATH="${2:-$ROOT_DIR/app/build/outputs/bundle/release/app-release.aab}"
ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
BUILD_TOOLS="$ANDROID_HOME/build-tools/36.0.0"
NDK_OBJDUMP="$ANDROID_HOME/ndk/27.1.12297006/toolchains/llvm/prebuilt/darwin-x86_64/bin/llvm-objdump"

fail() {
    printf 'Native release compatibility check failed: %s\n' "$1" >&2
    exit 1
}

[[ -f "$APK_PATH" ]] || fail "APK not found: $APK_PATH"
[[ -f "$AAB_PATH" ]] || fail "AAB not found: $AAB_PATH"
[[ -x "$BUILD_TOOLS/zipalign" ]] || fail "zipalign 36.0.0 is unavailable"

if [[ -x "$NDK_OBJDUMP" ]]; then
    OBJDUMP="$NDK_OBJDUMP"
elif command -v objdump >/dev/null 2>&1; then
    OBJDUMP="$(command -v objdump)"
else
    fail "llvm-objdump or objdump is required"
fi

BUNDLETOOL_JAR="$(find \
    "$HOME/.gradle/caches/modules-2/files-2.1/com.android.tools.build/bundletool/1.18.1" \
    -type f -name 'bundletool-1.18.1.jar' 2>/dev/null | head -1)"
[[ -n "$BUNDLETOOL_JAR" && -f "$BUNDLETOOL_JAR" ]] || fail "bundletool jar is unavailable"

"$BUILD_TOOLS/zipalign" -c -P 16 -v 4 "$APK_PATH" >/dev/null
java -jar "$BUNDLETOOL_JAR" dump config --bundle="$AAB_PATH" \
    | grep -Fq 'PAGE_ALIGNMENT_16K' \
    || fail "AAB does not request 16 KB page alignment"

work_dir="$(mktemp -d /tmp/phonedown-native-check.XXXXXX)"
trap 'rm -rf "$work_dir"' EXIT
unzip -q "$APK_PATH" 'lib/*/*.so' -d "$work_dir"

mapfile_source="$work_dir/native-libraries.txt"
find "$work_dir/lib" -type f -name '*.so' | sort > "$mapfile_source"
[[ -s "$mapfile_source" ]] || fail "APK contains no native libraries"

while IFS= read -r library; do
    "$OBJDUMP" -p "$library" | awk '
        $1 == "LOAD" {
            count += 1
            split($NF, exponent, "\\*\\*")
            if (exponent[2] + 0 < 14) bad = 1
        }
        END {
            if (count == 0 || bad == 1) exit 1
        }
    ' || fail "ELF LOAD alignment below 16 KB: $library"
done < "$mapfile_source"

if find "$work_dir/lib" -name 'libandroidx.graphics.path.so' -exec strings {} \; \
    | grep -Fq 'clang version 14.0.7'; then
    fail "Graphics Path still uses the version 6 clang 14 binary"
fi

if find "$work_dir/lib" -name 'libdatastore_shared_counter.so' -exec strings {} \; \
    | grep -Fq 'Linker: LLD 8.0.7'; then
    fail "DataStore still uses the version 6 LLD 8 binary"
fi

printf 'Native release compatibility checks passed for %s and %s.\n' "$APK_PATH" "$AAB_PATH"
```

- [ ] **Step 3: Prove the script rejects version 6's stale binaries**

Run:

```bash
chmod +x scripts/check-native-release-compatibility.sh
./scripts/check-native-release-compatibility.sh
```

Expected: FAIL on either the Graphics Path clang 14 fingerprint or DataStore LLD 8 fingerprint. Packaging and ELF alignment may pass before that failure.

- [ ] **Step 4: Document the red baseline**

Add a version 7 compatibility section to `docs/release-readiness.md` recording:

- Play reports that version 6 supports 16 KB page size at the bundle level;
- version 6 still contains Graphics Path clang 14 and DataStore LLD 8 native binaries;
- the new script intentionally fails until those providers are upgraded;
- ZIP alignment alone is insufficient evidence.

- [ ] **Step 5: Verify formatting and script behavior**

Run:

```bash
bash -n scripts/check-native-release-compatibility.sh
git diff --check
./scripts/check-native-release-compatibility.sh
```

Expected: shell syntax and diff checks pass; compatibility script fails for the documented stale-toolchain reason.

- [ ] **Step 6: Preserve the red checkpoint without committing it**

Review the working diff and confirm only the new compatibility script and release-readiness evidence changed. Leave these files unstaged until Task 2 turns the compatibility gate green; do not commit a deliberately failing release check.

---

### Task 2: Replace Stale Native Dependency Artifacts

**Files:**
- Create: `scripts/check-native-release-compatibility.sh`
- Modify: `scripts/check.sh`
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `scripts/check-public-free-release.sh`
- Modify: `docs/release-readiness.md`

**Interfaces:**
- Consumes: Compose UI Graphics 1.11.0 transitive dependency and current DataStore API usage.
- Produces: resolved `androidx.graphics:graphics-path:1.1.0` and `androidx.datastore:datastore-*:1.2.1` in the release runtime without API changes to Phone Down code.

- [ ] **Step 1: Add failing dependency contract assertions**

Add to `scripts/check-public-free-release.sh`:

```bash
assert_contains gradle/libs.versions.toml 'datastore = "1.2.1"'
assert_contains gradle/libs.versions.toml 'graphicsPath = "1.1.0"'
assert_contains app/build.gradle.kts 'implementation(libs.androidx.graphics.path)'
```

Run:

```bash
./scripts/check-public-free-release.sh
```

Expected: FAIL because the catalog still declares DataStore 1.2.0 and has no Graphics Path override.

- [ ] **Step 2: Update the version catalog**

Change the DataStore version and add the Graphics Path alias:

```toml
datastore = "1.2.1"
graphicsPath = "1.1.0"
```

```toml
androidx-graphics-path = { module = "androidx.graphics:graphics-path", version.ref = "graphicsPath" }
```

- [ ] **Step 3: Override Graphics Path in the final app graph**

Add to the AndroidX dependency block in `app/build.gradle.kts`:

```kotlin
implementation(libs.androidx.graphics.path)
```

The direct app dependency raises Compose UI Graphics' transitive `graphics-path:1.0.1` to stable `1.1.0` across the final runtime graph without changing feature-module APIs.

- [ ] **Step 4: Prove dependency resolution**

Run:

```bash
./gradlew :app:dependencyInsight \
  --dependency androidx.graphics:graphics-path \
  --configuration releaseRuntimeClasspath
./gradlew :app:dependencyInsight \
  --dependency androidx.datastore:datastore-core-android \
  --configuration releaseRuntimeClasspath
```

Expected: Graphics Path resolves to 1.1.0 and DataStore resolves to 1.2.1. No 1.0.1 or 1.2.0 artifact remains selected.

- [ ] **Step 5: Run affected persistence and app tests**

Run:

```bash
./gradlew \
  :core:datastore:testDebugUnitTest \
  :core:auth:testDebugUnitTest \
  :core:backup:testDebugUnitTest \
  :app:testDebugUnitTest
./scripts/check-public-free-release.sh
```

Expected: all tests and release contracts pass.

- [ ] **Step 6: Add the green compatibility gate to the comprehensive suite**

Append to `scripts/check.sh` after the debug build:

```bash
if [[ -f app/build/outputs/apk/release/app-release.apk && \
      -f app/build/outputs/bundle/release/app-release.aab ]]; then
  run_step "Native release compatibility" ./scripts/check-native-release-compatibility.sh
fi
```

This keeps ordinary debug checks usable while automatically validating release artifacts whenever both exist.

- [ ] **Step 7: Build fresh signed artifacts and turn the native check green**

Run:

```bash
./gradlew clean :app:assembleRelease :app:bundleRelease
./scripts/check-native-release-compatibility.sh
```

Expected: release builds succeed and the compatibility script passes for every ABI and both native-library families.

- [ ] **Step 8: Record dependency evidence**

Update `docs/release-readiness.md` with:

- DataStore 1.2.1 replacing 1.2.0;
- Graphics Path 1.1.0 replacing 1.0.1;
- resolved dependency evidence;
- absence of version 6 clang 14 and LLD 8 fingerprints;
- successful 16 KB ZIP, AAB config, and ELF checks.

- [ ] **Step 9: Commit Tasks 1 And 2**

Stage only the Task 1 and Task 2 files. Run the complete pre-commit safety checklist, then commit:

```bash
git commit -m "fix: modernize and verify native AndroidX release binaries"
```

---

### Task 3: Protect Portrait Behavior On Android 16 Large Displays

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `scripts/check-public-free-release.sh`
- Modify: `docs/permissions.md`
- Modify: `docs/release-readiness.md`

**Interfaces:**
- Consumes: Android 16 application compatibility property and the existing portrait `MainActivity` declaration.
- Produces: application-scoped `android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY=true` while preserving `android:screenOrientation="portrait"`.

- [ ] **Step 1: Add failing manifest-source assertions**

Add these assertions to `scripts/check-public-free-release.sh`:

```bash
assert_contains app/src/main/AndroidManifest.xml \
  'android:name="android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY"'
assert_contains app/src/main/AndroidManifest.xml 'android:value="true"'
assert_contains app/src/main/AndroidManifest.xml 'android:screenOrientation="portrait"'
```

Run:

```bash
./scripts/check-public-free-release.sh
```

Expected: FAIL because the compatibility property does not exist yet.

- [ ] **Step 2: Add the application compatibility property**

Inside `<application>`, after the Crashlytics `<meta-data>` element, add:

```xml
<property
    android:name="android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY"
    android:value="true" />
```

Keep `MainActivity` portrait-only and do not add `resizeableActivity`, screen-size declarations, or required hardware features.

- [ ] **Step 3: Verify the merged manifests**

Run:

```bash
./gradlew :app:processDebugMainManifest :app:processReleaseMainManifest
rg -n \
  'PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY|screenOrientation' \
  app/build/intermediates/merged_manifests/debug/processDebugMainManifest/AndroidManifest.xml \
  app/build/intermediates/merged_manifests/release/processReleaseMainManifest/AndroidManifest.xml
```

Expected: both merged manifests contain the property set to `true` and the main activity remains portrait.

- [ ] **Step 4: Check for accidental device filtering**

Run:

```bash
rg -n \
  'uses-feature|compatible-screens|supports-screens|requiresSmallestWidthDp|resizeableActivity' \
  app/src/main/AndroidManifest.xml
```

Expected: no new tablet, telephony, screen-size, or resizability filter appears.

- [ ] **Step 5: Document the product boundary**

Update `docs/permissions.md` and `docs/release-readiness.md` to state:

- Phone Down supports phones and intentionally retains portrait behavior;
- the property is compatibility metadata, not a permission;
- API 36 large screens receive temporary compatibility protection;
- adaptive tablet support is not claimed;
- the restriction must be reassessed before target API 37.

- [ ] **Step 6: Run focused checks**

Run:

```bash
./scripts/check-public-free-release.sh
./gradlew :app:testDebugUnitTest :app:lintDebug
git diff --check
```

Expected: all checks pass.

- [ ] **Step 7: Commit Task 3**

Stage the four Task 3 files, perform the full safety review, then commit:

```bash
git commit -m "fix: preserve phone layout on Android 16 large displays"
```

---

### Task 4: Prepare Version 7 Release Identity And Notes

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `scripts/check-public-free-release.sh`
- Create: `fastlane/metadata/android/en-US/changelogs/7.txt`
- Modify: `fastlane/metadata/android/en-US/changelogs/default.txt`
- Modify: `docs/release-readiness.md`
- Modify: `docs/public-free-release-qa.md`
- Modify: `docs/phase-16-console-setup-info.md`

**Interfaces:**
- Consumes: completed compatibility changes and existing public-free store language.
- Produces: release identity `7 (1.0.6)` and user-facing release notes that describe compatibility and reliability without exposing internal implementation details.

- [ ] **Step 1: Make the release contract expect version 7**

Replace the version assertions in `scripts/check-public-free-release.sh` with:

```bash
assert_contains app/build.gradle.kts 'versionCode = 7'
assert_contains app/build.gradle.kts 'versionName = "1.0.6"'
assert_contains fastlane/metadata/android/en-US/changelogs/7.txt \
  "Improved compatibility with newer Android devices"
```

Add `fastlane/metadata/android/en-US/changelogs/7.txt` to `CURRENT_RELEASE_DOCS`.

Run the script and expect failure because version 7 metadata does not exist yet.

- [ ] **Step 2: Bump application identity**

In `app/build.gradle.kts`, set:

```kotlin
versionCode = 7
versionName = "1.0.6"
```

- [ ] **Step 3: Add version 7 release notes**

Create `fastlane/metadata/android/en-US/changelogs/7.txt`:

```text
- Improved compatibility with newer Android devices
- Strengthened focus-session reliability across system and display changes
- Preserved the complete free experience with no ads or purchases
```

Add the compatibility line to the default changelog while preserving its public-free feature list.

- [ ] **Step 4: Roll release documentation forward**

Update current-release headings and candidate fields in the three documentation files to version code 7 and version name 1.0.6. Preserve version 6 as historical evidence and state that version 7 has not reached Play until upload succeeds.

- [ ] **Step 5: Verify release consistency**

Run:

```bash
./scripts/check-public-free-release.sh
rg -n 'versionCode = 6|versionName = "1.0.5"' app scripts
git diff --check
```

Expected: release consistency passes and no executable release contract still targets version 6.

- [ ] **Step 6: Commit Task 4**

Stage only Task 4 paths, perform the complete safety review, then commit:

```bash
git commit -m "chore: prepare Phone Down 1.0.6 release identity"
```

---

### Task 5: Run Comprehensive Automated And Artifact Verification

**Files:**
- Modify: `docs/release-readiness.md`
- Modify: `docs/public-free-release-qa.md`
- Modify: `docs/superpowers/plans/2026-09-06-phone-first-compatibility-hardening.md`

**Interfaces:**
- Consumes: version 7 source tree and configured release signing outside Git.
- Produces: signed APK/AAB, complete test evidence, manifest/dependency proofs, and SHA-256 hashes.

- [ ] **Step 1: Run the comprehensive repository suite**

Run:

```bash
./scripts/check.sh
```

Expected: formatting, detekt, lint, public-free contracts, unit tests, screenshot tests, Compose test assembly, debug build, and native release checks pass.

- [ ] **Step 2: Run full connected-test assembly and release lint**

Run:

```bash
./gradlew \
  assembleDebugAndroidTest \
  testReleaseUnitTest \
  lintVitalRelease
```

Expected: all relevant Android-test APKs assemble and release JVM/lint gates pass.

- [ ] **Step 3: Build signed release artifacts from a clean graph**

Run:

```bash
./gradlew clean :app:assembleRelease :app:bundleRelease
```

Expected artifacts:

```text
app/build/outputs/apk/release/app-release.apk
app/build/outputs/bundle/release/app-release.aab
```

- [ ] **Step 4: Verify native, signing, package, and bundle evidence**

Run:

```bash
./scripts/check-native-release-compatibility.sh
$HOME/Library/Android/sdk/build-tools/36.0.0/apksigner verify --verbose --print-certs \
  app/build/outputs/apk/release/app-release.apk
unzip -t app/build/outputs/bundle/release/app-release.aab
./gradlew :app:dependencies --configuration releaseRuntimeClasspath \
  | rg 'billingclient|graphics-path|datastore-core-android'
```

Expected:

- signature verification succeeds with one signer;
- AAB ZIP integrity passes;
- Graphics Path is 1.1.0;
- DataStore is 1.2.1;
- BillingClient is absent.

- [ ] **Step 5: Verify merged release policy**

Run:

```bash
manifest=app/build/intermediates/merged_manifests/release/processReleaseMainManifest/AndroidManifest.xml
rg -n \
  'screenOrientation|PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY|foregroundServiceType|PROPERTY_SPECIAL_USE_FGS_SUBTYPE|firebase_crashlytics_collection_enabled' \
  "$manifest"
if rg -n 'com.android.vending.BILLING|foregroundServiceType="dataSync"' "$manifest"; then
  exit 1
fi
```

Expected: portrait, compatibility property, special-use foreground service, subtype, and release Crashlytics appear; billing and `dataSync` do not.

- [ ] **Step 6: Record hashes and pass counts**

Run:

```bash
shasum -a 256 \
  app/build/outputs/apk/release/app-release.apk \
  app/build/outputs/bundle/release/app-release.aab
```

Update the three Task 5 documentation files with exact commands, outcomes, test counts where reported, resolved versions, manifest proof, artifact paths, and hashes.

- [ ] **Step 7: Commit Task 5 evidence**

Stage documentation only; build artifacts remain ignored. Perform the complete safety review, then commit:

```bash
git commit -m "docs: record Phone Down 1.0.6 automated verification"
```

---

### Task 6: Validate Android 16 Large-Display Containment

**Files:**
- Modify: `docs/release-readiness.md`
- Modify: `docs/public-free-release-qa.md`
- Modify: `docs/superpowers/plans/2026-09-06-phone-first-compatibility-hardening.md`

**Interfaces:**
- Consumes: signed version 7 APK, Android 16 ARM64 Google APIs system image, Pixel Tablet AVD profile.
- Produces: large-display compatibility-mode evidence without claiming tablet support.

- [ ] **Step 1: Install missing local Android tooling**

If `sdkmanager`, `avdmanager`, or `emulator` is absent, install command-line tools with Homebrew and then install SDK packages into the existing Android SDK:

```bash
brew install --cask android-commandlinetools
SDKMANAGER=/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager
AVDMANAGER=/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/avdmanager
yes | "$SDKMANAGER" --sdk_root="$HOME/Library/Android/sdk" --licenses
"$SDKMANAGER" --sdk_root="$HOME/Library/Android/sdk" \
  'emulator' \
  'platform-tools' \
  'system-images;android-36;google_apis_playstore;arm64-v8a'
```

Expected: emulator and API 36 ARM64 system image are available locally. Do not remove existing SDK or NDK versions.

- [ ] **Step 2: Create a dedicated tablet containment AVD**

Run only if `PhoneDownApi36Tablet` does not already exist:

```bash
echo no | "$AVDMANAGER" --sdk_root="$HOME/Library/Android/sdk" create avd \
  --name PhoneDownApi36Tablet \
  --package 'system-images;android-36;google_apis_playstore;arm64-v8a' \
  --device pixel_tablet
```

- [ ] **Step 3: Boot the AVD and install version 7**

Run:

```bash
$HOME/Library/Android/sdk/emulator/emulator \
  -avd PhoneDownApi36Tablet \
  -no-snapshot-save
adb -e wait-for-device
adb -e shell getprop sys.boot_completed
adb -e install -r app/build/outputs/apk/release/app-release.apk
```

Expected: boot completes and installation succeeds.

- [ ] **Step 4: Verify compatibility containment**

Launch Phone Down, rotate the emulator, resize where supported, and inspect activity/window state:

```bash
adb -e shell am start -n phonedown.app/.MainActivity
adb -e shell settings put system accelerometer_rotation 1
adb -e shell settings put system user_rotation 1
adb -e shell dumpsys activity top | rg 'phonedown.app|mBounds|orientation|resize'
adb -e shell dumpsys window | rg 'phonedown.app|mCurrentFocus|mAppBounds'
```

Capture screenshots in portrait request and landscape device posture. Confirm:

- app remains in controlled compatibility behavior;
- no stretched or overlapping controls;
- onboarding and all main navigation remain reachable;
- starting and canceling a session does not duplicate or corrupt state;
- no crash or ANR appears in logcat.

- [ ] **Step 5: Record the containment result**

Document API level, AVD, compatibility behavior, screenshot observations, session-state result, and logcat result. Explicitly state that this is a safety check, not tablet support certification.

- [ ] **Step 6: Commit Task 6 evidence**

Stage the three documentation files only, run the complete safety review, then commit:

```bash
git commit -m "test: verify Android 16 large-display containment"
```

---

### Task 7: Complete Clean RMX3853 Device Regression

**Files:**
- Modify: `docs/phase-16-manual-qa.md`
- Modify: `docs/public-free-release-qa.md`
- Modify: `docs/release-readiness.md`
- Modify: `docs/superpowers/plans/2026-09-06-phone-first-compatibility-hardening.md`

**Interfaces:**
- Consumes: connected RMX3853, debug instrumentation APKs, signed version 7 APK, existing manual QA cases.
- Produces: clean-install phone evidence for core focus, recovery, export, privacy, and deletion behavior.

- [ ] **Step 1: Select one unambiguous ADB transport**

Run:

```bash
adb devices -l
```

Choose the currently responsive RMX3853 serial and export it for every remaining command:

```bash
export ANDROID_SERIAL="$(adb devices -l | awk '/model:RMX3853/{print $1; exit}')"
test -n "$ANDROID_SERIAL"
adb get-state
adb shell getprop ro.build.version.release
```

Expected: exactly one selected transport reports `device`; record the serial only outside Git.

- [ ] **Step 2: Run the complete connected instrumentation suite**

Run:

```bash
./gradlew --no-configuration-cache connectedDebugAndroidTest
```

Expected: database, account, Focus, Insights, Pro, and Settings instrumentation suites pass on the selected phone.

- [ ] **Step 3: Return to a clean release installation**

Run:

```bash
adb uninstall phonedown.app || true
adb install app/build/outputs/apk/release/app-release.apk
adb shell am start -n phonedown.app/.MainActivity
```

Expected: version 7 launches into first-run onboarding without retained Phone Down data.

- [ ] **Step 4: Complete first-run, duration, and focus checks**

Follow `docs/phase-16-manual-qa.md` Tests 9.1 through 9.8 and record pass/fail for:

- onboarding and permissions;
- editable default duration;
- one-time custom duration isolation;
- face-down arming;
- clean timing;
- pickup interruption;
- pause and resume;
- Add Time;
- notification tap and End action;
- completion summary and Done.

Physical phone placement is performed by the user when requested; ADB captures UI hierarchy, service state, notifications, logs, and persisted outcomes around those physical steps.

- [ ] **Step 5: Complete recovery, offline, export, privacy, and deletion checks**

Run the applicable manual cases for:

- app-process kill and relaunch recovery;
- reboot recovery;
- core timer offline;
- network-restoration behavior;
- history and Insights;
- CSV export and readable exported content;
- privacy-policy reachability and scrolling;
- local-data deletion;
- clean relaunch after deletion.

For each case, capture expected versus observed behavior and relevant logcat evidence without personal data.

- [ ] **Step 6: Apply the severity gate**

Use the existing P0-P3 model in `docs/phase-16-manual-qa.md`:

- P0/P1 blocks upload;
- P2 requires an explicit release decision and normally receives a forward fix;
- P3 is recorded and may be deferred.

Any code defect starts a focused test-first fix cycle, repeats affected automated tests, and reruns the failed device scenario before this task can pass.

- [ ] **Step 7: Record and commit device evidence**

Update all four Task 7 documents with device model, Android version, install source, covered scenarios, results, residual limits, and exact test commands. Do not record account identifiers or exported private data.

Stage only documentation and any separately reviewed defect-fix files. Run the complete safety review, then commit:

```bash
git commit -m "test: complete Phone Down 1.0.6 device regression"
```

---

### Task 8: Upload Version 7 To The Existing Closed Track

**Files:**
- Modify: `docs/public-free-release-qa.md`
- Modify: `docs/release-readiness.md`
- Modify: `docs/phase-16-console-setup-info.md`
- Modify: `docs/superpowers/plans/2026-09-06-phone-first-compatibility-hardening.md`

**Interfaces:**
- Consumes: validated signed version 7 AAB and existing closed-testing Alpha track.
- Produces: processed Play version 7 release without recreating, pausing, or changing tester eligibility configuration.

- [ ] **Step 1: Reconfirm the exact upload artifact**

Run:

```bash
shasum -a 256 app/build/outputs/bundle/release/app-release.aab
./scripts/check-native-release-compatibility.sh
git status --short --branch
```

Expected: hash matches Task 5 evidence, native checks pass, and source state is clean or contains only reviewed documentation progress.

- [ ] **Step 2: Create the replacement closed-test release**

In Play Console:

1. open Phone Down > Test and release > Closed testing > Alpha;
2. choose Create new release;
3. upload `app/build/outputs/bundle/release/app-release.aab`;
4. confirm Play reads version code 7 and version name 1.0.6;
5. paste the text from `fastlane/metadata/android/en-US/changelogs/7.txt`;
6. review the change summary;
7. submit the release to the same Alpha track.

Do not pause the track, create a second closed track, alter countries, remove tester configuration, or promote to production.

- [ ] **Step 3: Inspect Play processing results**

After processing, verify:

- release 7 is available to selected testers;
- memory page size says `Supports 16 KB`;
- the older-NDK crash warning no longer appears for version 7;
- the portrait/large-screen recommendation is understood as the accepted phone-only limitation;
- no new policy, permission, billing, foreground-service, or SDK warning appears.

If the 16 KB warning remains, open the affected-library detail, record its sanitized library/version evidence, stop promotion, and create a forward-fix candidate with the next unused version code.

- [ ] **Step 4: Review pre-launch results**

Open the pre-launch report for version 7 and inspect crashes, ANRs, accessibility, security, and device screenshots. Any P0/P1 finding blocks completion; P2 follows the explicit severity decision from Task 7.

- [ ] **Step 5: Record and commit Play evidence**

Document release state, processing date, 16 KB result, accepted orientation recommendation, new warnings, and pre-launch result without Console IDs or tester identities.

Stage only the four documentation files, run the complete safety review, then commit:

```bash
git commit -m "docs: record Play processing for Phone Down 1.0.6"
```

---

### Task 9: Verify The Play-Signed Installation

**Files:**
- Modify: `docs/phase-16-manual-qa.md`
- Modify: `docs/public-free-release-qa.md`
- Modify: `docs/release-readiness.md`
- Modify: `docs/superpowers/plans/2026-09-06-phone-first-compatibility-hardening.md`

**Interfaces:**
- Consumes: version 7 installed from the closed Play track and the configured Play-signing OAuth certificate.
- Produces: ecosystem evidence for Google sign-in, Drive, Crashlytics, and final smoke behavior.

- [ ] **Step 1: Install version 7 from Google Play**

Uninstall the local build, join/open the existing closed-test listing with the authorized owner/test account, and install Phone Down from Google Play. Confirm app details report version 1.0.6 before testing.

- [ ] **Step 2: Run the Play-installed smoke matrix**

Verify:

- onboarding and main navigation;
- default and custom duration;
- one short face-down session through summary and Done;
- history, Insights, Settings, and Phone Down Pro overview;
- absence of purchase, subscription, price, restore-purchase, and manage-subscription surfaces.

- [ ] **Step 3: Run Google sign-in and Drive checks**

Follow `docs/phase-16-manual-qa.md` Tests 10.1-10.2 and 11.1-11.4:

- Google sign-in succeeds with Play signing;
- manual backup succeeds;
- restore recovers the expected test data;
- auto-backup eligibility and execution are observable;
- cloud-backup deletion succeeds;
- sign-out returns to the correct local-first state.

Use synthetic focus-session data and avoid placing account identifiers or Drive contents in Git.

- [ ] **Step 4: Verify release Crashlytics**

Trigger only the existing approved non-sensitive release diagnostic path, then confirm the event appears under version 1.0.6 in Firebase without account, token, backup, or session-content leakage.

- [ ] **Step 5: Record and commit Play-installed evidence**

Update all four documents with install source, version, scenario results, Crashlytics receipt category, residual risks, and any defect references.

Stage explicit files, run the complete safety checklist, then commit:

```bash
git commit -m "test: verify Play-signed Phone Down 1.0.6"
```

---

### Task 10: Close The Compatibility Phase And Push

**Files:**
- Modify: `docs/release-readiness.md`
- Modify: `docs/public-free-release-qa.md`
- Modify: `docs/google-play-launch-report-2026-09-05.md`
- Modify: `docs/superpowers/plans/2026-09-06-phone-first-compatibility-hardening.md`

**Interfaces:**
- Consumes: all passing local, emulator, physical-device, Play-processing, and Play-installed evidence.
- Produces: final version 7 technical-readiness record and clean pushed repository state.

- [ ] **Step 1: Re-run final verification**

Run:

```bash
./scripts/check.sh
./gradlew testReleaseUnitTest lintVitalRelease
./scripts/check-native-release-compatibility.sh
git diff --check
```

Expected: every command passes against the exact source and artifacts represented by version 7.

- [ ] **Step 2: Reconcile all release records**

Update current-state sections to show:

- version 7 / 1.0.6 active in closed testing;
- 16 KB warning resolved;
- portrait-only phone boundary protected and documented;
- automated, tablet-containment, RMX3853, and Play-installed gates completed;
- tester recruitment and 14-day eligibility still deferred;
- API 37 adaptive-orientation migration remains future work.

Mark only evidence-backed checkboxes complete. Preserve historical version 6 information as history rather than silently rewriting it.

- [ ] **Step 3: Perform final Git and secrets audit**

Run:

```bash
git status --short --branch
git diff --cached
git diff --cached --name-only \
  | rg -i '(^|/)(\.env($|\.)|.*\.(bak|backup|key|pem|p12)$|credentials|service.?account)' \
  && exit 1 || true
git diff --cached \
  | rg -i '(api[_-]?key|client[_-]?secret|access[_-]?token|private[_-]?key|password\s*[:=]|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY)' \
  && exit 1 || true
rg -n '^\.env|\.bak|\.backup|\.pem|\.p12|google-services' .gitignore
git branch --show-current
```

Expected: no suspicious staged file or secret-like content; ignored local credentials remain protected; branch is `main` unless the execution workflow created and intentionally integrated a `codex/` branch.

- [ ] **Step 4: Commit final documentation**

Stage only the four Task 10 documents, inspect the staged diff, then commit:

```bash
git commit -m "docs: close Phone Down 1.0.6 compatibility phase"
```

- [ ] **Step 5: Push and verify remote state**

Run:

```bash
git push origin HEAD
git status --short --branch
git log --oneline --decorate -n 8
```

Expected: push succeeds, local branch matches its remote, and the version 7 compatibility commits are visible in history.

---

## End-To-End Acceptance Checklist

- [ ] `phonedown.app` builds as version code 7, version name 1.0.6.
- [ ] DataStore resolves to 1.2.1.
- [ ] Graphics Path resolves to 1.1.0.
- [ ] Version 6 clang 14 and LLD 8 native fingerprints are absent.
- [ ] APK 16 KB ZIP alignment passes.
- [ ] AAB requests `PAGE_ALIGNMENT_16K`.
- [ ] Every native ELF `LOAD` segment is aligned to at least `2**14`.
- [ ] Play reports `Supports 16 KB` and no older-NDK crash warning for version 7.
- [ ] Main activity remains portrait on supported phones.
- [ ] Android 16 restricted-resizability compatibility property is present in the release manifest.
- [ ] API 36 tablet containment test shows no crash, ANR, broken session, or unusable UI.
- [ ] Full automated suite passes.
- [ ] Full connected instrumentation suite passes on RMX3853.
- [ ] Clean local release-device matrix passes.
- [ ] Play-installed sign-in, Drive, Crashlytics, and smoke checks pass.
- [ ] Billing dependency, permission, purchase UI, and monetization references remain absent.
- [ ] Release artifact hashes and exact verification evidence are documented.
- [ ] Version 7 is active on the existing closed-testing track.
- [ ] Tester recruitment and production work remain explicitly deferred.
- [ ] Every commit passes status, staged-diff, sensitive-file, secret, ignore, and branch review.
- [ ] Repository is pushed and clean.
