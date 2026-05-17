# Pre-Upload Go / No-Go Checklist - 2026-05-17

## Purpose

This checklist is the stricter gate we should use **before uploading the first Android App Bundle to Google Play**.

It exists for one reason:

> A bundle that builds is not automatically a bundle that should be uploaded.

For Phone Down, the pre-upload gate has to cover three layers:

1. **release mechanics** - signing, artifacts, Play prerequisites
2. **product truthfulness** - auth, backup, billing, store copy
3. **security/secrets hygiene** - no accidental key, token, or credential leakage

This checklist is based on:

- direct local repo inspection
- direct build verification
- the production-readiness audit in [docs/production-readiness-audit-2026-05-17.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/production-readiness-audit-2026-05-17.md)

## Current Go / No-Go Status

### Current status

- **GO for first internal-test artifact upload mechanics**
- **NO-GO for broader production release**

### Why

The repo is close, and the local signing/artifact mechanics are now proven. The current state still fails a few non-negotiable gates for a complete production release:

- Play Billing products are not created yet
- Play-installed QA for auth/backup/billing has not happened yet
- backup policy is still ambiguous because `allowBackup` remains enabled

## Section A - Release Artifact Gate

### A1. Does a release AAB exist?

- [x] Yes

### Why

A fresh signed release bundle now exists at:

- [app-release.aab](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build/outputs/bundle/release/app-release.aab)

It was generated after real local signing was configured through ignored `keystore.properties`.

### A2. Did the release build path succeed locally?

- [x] Release build path was proven before the signing guard
- [x] Release build path now intentionally fails without signing secrets
- [x] Release build path succeeds with real local upload-key signing configured
- Verified with:

```bash
./gradlew --no-daemon --no-configuration-cache :app:bundleRelease :app:assembleRelease
./gradlew --no-daemon --no-configuration-cache :app:bundleRelease
```

### A3. Is the current AAB the one we should upload?

- [x] Yes, for the first internal testing release upload

### Why

- the previous stale artifact was removed
- the current artifact was rebuilt after real local signing values were configured
- the bundle signature verifies successfully
- the upload certificate fingerprint matches the expected upload key

### Verified artifact

- Path: [app-release.aab](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build/outputs/bundle/release/app-release.aab)
- Size: 6.2 MB
- Bundle SHA-256: `764667e43982c6d82d9726f5493a4a109112bdb22e10248b79276aa373ab9e85`
- Upload certificate SHA-1: `EE:FA:73:EF:A2:F0:6A:A1:8F:03:A8:0E:C4:A4:20:F7:65:33:A3:9C`
- Upload certificate SHA-256: `63:0E:62:5F:A1:14:13:C9:A0:FB:2B:53:E8:4B:5A:D2:B3:03:11:B5:0D:52:4F:42:B9:92:75:0E:2C:7E:F9:0A`

## Section B - Play Console Prerequisite Gate

### B1. Payments profile exists

- [x] Yes

### B2. Internal testing track is accessible

- [x] Yes

### B3. Play Billing products exist

- [ ] Not yet

### Required products

- [ ] `pro_monthly`
- [ ] `pro_yearly`
- [ ] `pro_lifetime`

### Why this matters

The app now uses real Billing runtime behavior. If products do not exist in Play Console, billing cannot truthfully load and the paywall cannot be considered release-ready.

### B4. Play App Signing release fingerprints captured

- [ ] Not yet confirmed complete

### Why this matters

Google Sign-In can work in local/debug testing and still fail on Play-distributed installs if Play signing fingerprints are not correctly captured in the Google/Firebase setup.

## Section C - Product Truthfulness Gate

### C1. Real Google Sign-In works on debug/device install

- [x] Yes

### C2. Real Drive backup and restore work on device

- [x] Yes

### C3. Real Billing logic is wired in code

- [x] Yes

### C4. Real Billing is validated on a Play-installed build

- [ ] Not yet

### Must validate after internal upload

- [ ] product catalog loads
- [ ] monthly purchase works
- [ ] yearly purchase works
- [ ] lifetime purchase works
- [ ] restore purchases works
- [ ] cancellation/relaunch/recovery works

### C5. Manage Subscription path is validated

- [ ] Not yet

## Section D - Core Experience QA Gate

### D1. Core focus-session behavior is manually validated on real hardware

- [x] Substantially validated on debug/device path

### D2. Core focus-session behavior is validated on the release / Play-installed path

- [ ] Not yet

### Must validate

- [ ] timer honesty while face down
- [ ] pickup interruption behavior
- [ ] call pause behavior
- [ ] reboot recovery
- [ ] dimming / service / notification behavior

## Section E - Security and Secrets Gate

This section is specifically about preventing accidental credential leakage or unsafe release configuration.

### E1. Tracked secret-like files in git

- [x] None found in tracked files during audit

### Verified with

```bash
git ls-files | rg '(^|/)(\.env(\..*)?$|.*\.bak$|.*\.backup$|.*\.pem$|.*\.key$|.*\.p12$|.*service.*account.*\.json$|google-services\.json$|client_secret.*\.json$|keystore\.properties$|.*\.jks$|.*\.keystore$)'
```

### E2. Local ignored sensitive files

- [x] `app/google-services.json` exists locally and is ignored
- [x] build artifacts (`*.aab`, `*.apk`) are ignored

### Verified with

```bash
git check-ignore -v app/google-services.json app/build/outputs/bundle/release/app-release.aab app/build/outputs/apk/debug/app-debug.apk
```

### E3. `keystore.properties` ignore protection

- [x] Added to `.gitignore`

### Why this matters

The moment we wire real release signing, a file like `keystore.properties` usually contains:

- keystore path
- store password
- key alias
- key password

If that file is not ignored, it becomes one of the highest-probability accidental secret leaks in the repo.

### Required before wiring release signing

- [x] Add `keystore.properties` to `.gitignore`
- [x] Add a safe placeholder-only `keystore.properties.example`

### E4. Suspicious local secret files in repo root or nearby paths

- [x] No stray local secret files were found in the repo scan except the expected ignored `app/google-services.json`

### E5. Regex-based repo scan for likely tokens/keys

- [x] No actual secret values found in tracked source/docs during audit
- [x] Matches were documentation mentions and explanatory text only

### Caveat

This does **not** prove the app has perfect security. It only proves we did not detect obvious committed credentials in the repo contents scanned.

### E6. Network security posture

- [x] Cleartext traffic disabled
- [x] No active placeholder certificate pins currently shipping

### Evidence

- [network_security_config.xml](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/res/xml/network_security_config.xml)
- [CertificatePinningConfig.kt](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/java/phonedown/app/security/CertificatePinningConfig.kt)

### Interpretation

This is better than shipping broken placeholder pins, but it also means **real pinning is not active yet**. That is acceptable for internal testing, but should be a conscious release-hardening decision later.

### E7. Logging/redaction posture

- [x] `SecureLogger` redacts email/token/session-like strings
- [x] ProGuard strips `Log` and `SecureLogger` calls in release builds

### Evidence

- [SecureLogger.kt](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/java/phonedown/app/security/SecureLogger.kt)
- [proguard-rules.pro](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/proguard-rules.pro)

### E8. Stale security wording that could mislead release decisions

- [ ] Needs cleanup

### Why

Some docs still describe older assumptions such as fake integrations, placeholder pinning, or placeholder support/security contacts. That is not a code leak, but it is a release-governance risk.

## Section F - Backup and Privacy Gate

### F1. Is Android system backup policy explicitly decided?

- [ ] Not yet

### Current state

- `android:allowBackup="true"` in [AndroidManifest.xml](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/AndroidManifest.xml:11)

### Why this matters

Phone Down now has a real app-managed Google Drive backup system. Leaving OS backup enabled without explicit rules creates ambiguity around:

- restore behavior
- delete behavior
- privacy guarantees

### Required decision before broader release

- [ ] set `allowBackup=false`
  - or
- [ ] keep it on, but define explicit backup/data extraction rules

## Section G - Store and Policy Truthfulness Gate

### G1. Release-readiness doc matches reality

- [ ] Not yet

### G2. Security doc matches reality

- [ ] Not yet

### G3. Privacy/store contact details are real, not placeholders

- [ ] Not yet

### Examples needing cleanup

- `support@phonedown.app (placeholder)`
- `security@phonedown.app (placeholder)`

## Section H - Operational Recovery Gate

### H1. Crash reporting operational

- [ ] Not yet

### Current interpretation

Crashlytics is still a production-readiness gap, but it is **not** a blocker for the first internal upload if we are disciplined about internal QA.

## Pre-Upload Decision Rules

## Rule 1 - Absolute no-go items

Do **not** upload if any of these are still false:

- [x] release signing is real, not debug
- [x] fresh AAB rebuilt after signing fix
- [ ] Play Billing products created
- [x] no secret-like signing/config files are accidentally trackable

## Rule 2 - Okay to proceed to internal testing if these are true

Internal testing is acceptable once:

- [x] Section A absolute items are done
- [ ] Section B Play Console prerequisites are done enough
- [x] Section E secrets/config hygiene is clean
- [ ] we understand that billing/auth/backup still need Play-installed QA

## Rule 3 - Not required before first internal upload, but should happen soon after

- [ ] Crashlytics
- [ ] final backup policy cleanup
- [ ] doc truthfulness cleanup
- [ ] deeper billing lifecycle QA

## Recommended Next Order

1. **Create Play products**
2. **Upload the signed AAB to internal testing**
3. **Run Play-installed QA**
4. **Validate billing catalog, purchases, restore, cancellation, and recovery**
5. **Then decide whether the app is ready for broader release work**

## Final Decision Today

- **Current state:** signed AAB is ready for internal testing upload mechanics
- **Still pending before meaningful billing QA:** Play products must be created
- **Still no-go for production release:** Play-installed QA, backup policy cleanup, Crashlytics, and final policy/doc cleanup remain
