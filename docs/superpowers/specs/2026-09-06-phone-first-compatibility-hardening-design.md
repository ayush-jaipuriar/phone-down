# Phone-First Compatibility Hardening Design

Date: September 6, 2026
Target release: Phone Down 1.0.6, version code 7

## 1. Purpose

Prepare Phone Down version 7 as a technically hardened closed-test release while preserving the product's phone-first behavior. This phase resolves the Google Play 16 KB native-library warning, protects the existing portrait-only experience on Android 16 large displays, completes automated and physical-device regression testing, and produces a validated signed release bundle.

Tester recruitment, the continuous closed-test eligibility period, production-access application, and production rollout are outside this phase.

## 2. Product Boundary

Phone Down is designed for phones. Its central interaction requires the user to place a phone face down while a sensor-backed foreground session measures valid focus time. Tablet-specific navigation, expanded layouts, and landscape experiences do not advance that product purpose and are not part of version 7.

Version 7 will:

- continue to support Android phones from API 26 through API 36;
- retain portrait orientation for the main activity;
- protect large Android 16 displays with the platform's temporary restricted-resizability compatibility mechanism;
- keep focus-session behavior unchanged on ordinary phones;
- avoid false hardware requirements, such as declaring cellular telephony mandatory only to filter tablets;
- document large-screen use as unsupported rather than claim adaptive tablet support.

The Android 16 compatibility mechanism is temporary. Before Phone Down targets API 37, the project must reassess orientation and resizability because Android 17 removes this opt-out for large displays.

## 3. Current Technical Evidence

The current release is version code 6, version name 1.0.5, and is active on the closed-testing track. Google Play reports:

1. the app could crash on devices using 16 KB memory pages because one or more bundled native libraries may have been built with an affected older Android NDK; and
2. orientation and resizability restrictions can be ignored on Android 16 large-screen devices.

The repository currently uses:

- Android Gradle Plugin 8.13.2;
- compile SDK 36 and target SDK 36;
- `android:screenOrientation="portrait"` on `MainActivity`;
- AndroidX DataStore 1.2.0;
- Compose BOM 2026.04.01;
- `androidx.graphics:graphics-path:1.0.1`, transitively supplied by Compose UI Graphics 1.11.0.

The release artifact contains native libraries including:

- `libandroidx.graphics.path.so`;
- `libdatastore_shared_counter.so`.

Local 16 KB ZIP-alignment verification of the release APK succeeded. That result proves packaging alignment only; it does not prove that every native library's ELF load segments are 16 KB aligned or that a prebuilt library avoids the older-NDK runtime defect reported by Play.

## 4. Architecture

### 4.1 Native-Library Compatibility

Compatibility analysis works from the final release artifacts rather than inferring safety from source dependencies alone.

For every ABI and `.so` file in the release AAB or APK:

1. identify the Maven dependency that contributed the binary;
2. verify AAB page-alignment configuration;
3. verify APK ZIP alignment;
4. inspect ELF `LOAD` segment alignment;
5. inspect available build metadata for NDK/toolchain provenance;
6. confirm the supplying SDK version is documented or demonstrated as 16 KB compatible.

Remediation must remain narrow. Upgrade the dependency that owns an incompatible prebuilt binary and any versions required to maintain its AndroidX atomic/version constraints. Do not perform unrelated dependency refreshes.

If the latest stable dependency still ships an incompatible binary, the phase stops for a documented dependency decision. Phone Down will not patch opaque third-party native binaries or suppress the warning without compatibility evidence.

### 4.2 Portrait And Large-Display Protection

The main activity remains portrait-only for the supported phone experience. The release manifest adds Android 16's `android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` compatibility property at the narrowest effective scope.

This property must:

- have no effect on supported phone layouts;
- place affected API 36 large-display execution into the platform's compatibility behavior;
- coexist with the existing portrait declaration;
- appear in the merged release manifest;
- avoid introducing device filters or unrelated manifest capabilities.

The Google Play recommendation to remove orientation restrictions may remain because the product intentionally retains the restriction. The release is acceptable only if large-display testing shows controlled compatibility behavior rather than stretched, rotated, overlapping, or state-corrupting UI.

### 4.3 Runtime Invariants

Compatibility work must not change Phone Down's product contracts:

- focus time advances only while the phone is face down and stable;
- picking up or invalidating the phone pauses or breaks focus according to existing rules;
- foreground-service timing survives expected app backgrounding;
- completion always reaches a usable summary and Done exits correctly;
- default duration remains editable and one-time custom durations remain independent;
- all current Pro-labelled capabilities remain free and available;
- Play Billing remains absent from the public runtime and release manifest;
- Google sign-in and Drive backup remain optional;
- privacy, deletion, export, Crashlytics, and foreground-service declarations remain accurate.

## 5. Verification Strategy

### 5.1 Automated Regression

Run the repository's comprehensive check suite plus focused checks for changed dependencies and manifest behavior. Required categories are:

- compilation and static checks;
- unit and JVM regression tests;
- Compose UI and Android-test assembly;
- screenshot verification;
- debug and signed release builds;
- release dependency inspection;
- merged-manifest assertions;
- bundle validation and signing verification;
- billing and monetization absence checks;
- secret and sensitive-file checks.

Any failure blocks artifact promotion until its cause is understood and fixed.

### 5.2 Artifact Verification

The candidate AAB and APK must prove:

- package name `phonedown.app`;
- version code 7;
- version name 1.0.6;
- expected min and target SDK values;
- valid signing configuration;
- 16 KB AAB and APK packaging alignment;
- compatible ELF alignment for every bundled native library and ABI;
- intended portrait and Android 16 compatibility declarations;
- no Play Billing dependency or billing permission;
- reproducible hashes recorded in release documentation.

Google Play's processed-bundle result is the final external confirmation that the 16 KB warning has been resolved.

### 5.3 Clean Physical-Device QA

The connected RMX3853 is available with Phone Down uninstalled, providing a clean-install starting point. Device validation covers:

- installation and first launch;
- onboarding and permissions;
- default and one-time custom duration behavior;
- focus start and face-down arming;
- valid timing, pickup, pause, resume, and penalty behavior;
- clean, broken, abandoned, and completed outcomes;
- completion summary and Done;
- foreground notification and service lifecycle;
- process recovery where reproducible;
- history, insights, CSV export, settings, and privacy surfaces;
- local-data deletion and a second clean-launch check.

Google sign-in, Drive backup/restore, release Crashlytics delivery, and final install-path behavior require a Play-signed closed-track installation. Local signing is insufficient evidence because Google OAuth identity depends on signing-certificate configuration.

### 5.4 Large-Display Safety Check

Run version 7 on at least one API 36 tablet or foldable emulator configuration. This is a containment test, not tablet feature certification. It must confirm:

- the compatibility mechanism takes effect;
- the main activity is not forced into an untested adaptive layout;
- visible content remains reachable;
- rotation or window changes do not corrupt or duplicate an active session;
- no crash, ANR, overlapping controls, or unusable completion path occurs.

## 6. Release Flow

Version 6 remains active while version 7 is built and validated.

Promotion order:

1. automated regression passes;
2. local artifact inspection passes;
3. clean physical-device QA passes;
4. signed version 7 artifacts are generated and hashed;
5. version 7 is uploaded to the existing closed-testing track;
6. Play processing and compatibility results are reviewed;
7. version 7 is installed from Google Play;
8. Play-signed sign-in, Drive, Crashlytics, and smoke checks pass;
9. release documentation is finalized.

Uploading version 7 must not stop or recreate the existing closed-testing track. Tester recruitment remains deferred, but the track must stay ready for later opt-in.

## 7. Failure And Rollback Rules

- Do not upload a build that fails automated, artifact, or local-device validation.
- If dependency remediation introduces regression, keep version 6 active while revising the version 7 candidate.
- If Google Play still reports the 16 KB defect, inspect Play's affected-library evidence and repeat remediation with a new version code only after the rejected or superseded artifact state is understood.
- Do not conceal an unresolved native compatibility problem through warning suppression.
- Do not remove portrait protection merely to clear a recommendation.
- Do not reactivate billing or expose monetization while fixing compatibility.
- Use forward fixes; do not rewrite shared Git history or erase release evidence.

## 8. Documentation And Git Safety

During implementation, update the phase plan and release-readiness documentation after each meaningful work unit. Record:

- files and components changed;
- dependency ownership and version changes;
- reason for each change;
- commands and test categories run;
- physical-device and Play-installed evidence;
- artifact hashes;
- Play processing results;
- accepted limitations and future API 37 work.

Before every commit:

- inspect repository status;
- stage explicit paths only;
- inspect the staged diff;
- scan filenames and staged content for credentials, private KYC data, environment files, keys, and backups;
- confirm `.gitignore` coverage;
- verify the branch and push target.

## 9. Acceptance Criteria

The phase is complete when:

- version 7 builds as `1.0.6` with package `phonedown.app`;
- every bundled native library passes 16 KB packaging and ELF compatibility checks;
- Google Play no longer reports the 16 KB crash warning for version 7;
- phone portrait behavior and all existing focus contracts pass regression testing;
- Android 16 large-display execution uses controlled compatibility behavior without session or UI breakage;
- the comprehensive automated suite passes;
- the clean RMX3853 device matrix passes;
- Play-installed sign-in, Drive, Crashlytics, and smoke checks pass;
- billing and monetization remain absent from the public release;
- signed AAB/APK hashes and verification evidence are documented;
- changes are reviewed, committed, and pushed without secrets or unrelated files.

## 10. Deferred Work

The following remain outside this phase:

- recruiting 15-20 closed-test participants;
- achieving 12 continuously opted-in testers for 14 days;
- collecting cohort feedback;
- applying for production access;
- production rollout and launch monitoring;
- adaptive tablet and unfolded-foldable product design;
- API 37 orientation/resizability migration;
- BillDesk KYC and monetization restart work.
