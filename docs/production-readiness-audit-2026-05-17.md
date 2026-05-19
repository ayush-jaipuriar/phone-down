# Production Readiness Audit - 2026-05-17

## Purpose

This audit answers one practical question:

> Is Phone Down ready to upload a real Android App Bundle to Google Play right now?

The review was done against the current working tree, which includes in-progress Sprint 16.4 real Play Billing changes. It combines:

- direct local repo inspection
- direct build/output verification
- parallel subagent audits for architecture, release engineering, and QA/release risk

This is intentionally stricter than a normal code review. The standard here is not "the app builds" but "the first Play upload is unlikely to create avoidable release problems."

## Executive Summary

### Short answer

- **We can generate a signed release AAB locally.**
- **The current AAB is acceptable for internal testing upload mechanics.**
- **We should not treat the app as production-release-ready yet.**

### Why not yet

The original release artifact produced during the audit was blocked by a few launch-critical realities:

1. **Release builds were still using the debug signing config.**
2. **Play Billing console products do not exist yet.**
3. **Play-installed QA for auth, backup, and billing has not happened yet.**
4. **Release/security/store docs are materially stale in places and could mislead Play setup decisions.**
5. **Android system backup is still enabled while the app also has its own custom Drive backup system.**

After the audit, release signing was hardened:

- release builds no longer fall back to debug signing
- signing values come from ignored `keystore.properties` or environment variables
- release bundle tasks fail fast if signing is missing
- a fresh signed AAB was generated and verified locally

### What changed from the initial assumption

At the start of the audit, no `.aab` had been found on disk. During the audit, a fresh release build completed and produced:

- `app/build/outputs/bundle/release/app-release.aab`

So the question is no longer "can we build an AAB?" The question is now:

> Is this particular AAB production-trustworthy?

For internal testing upload mechanics, the answer is now **yes**. For full production release readiness, the answer is still **no** until Play products and Play-installed QA are complete.

## What Was Verified Directly

### Repo/build state

- Current branch: `main`
- Working tree: dirty with in-progress Sprint 16.4 billing work
- Existing artifact at audit start:
  - `app/build/outputs/apk/debug/app-debug.apk`
- Release artifact generated during audit:
  - `app/build/outputs/bundle/release/app-release.aab`

### Commands run

```bash
find . -type f \( -name '*.aab' -o -name '*.apk' \) | sort
./gradlew --no-daemon --no-configuration-cache :app:bundleRelease :app:assembleRelease
./gradlew --no-daemon --no-configuration-cache :app:testDebugUnitTest
git diff --check
```

### Outcomes

- `:app:bundleRelease` succeeded
- `:app:assembleRelease` succeeded as part of the release run
- `:app:testDebugUnitTest` succeeded
- `git diff --check` succeeded

## Hard Blockers Before First Play Upload

### 1. Release builds were signed with the debug key at audit time

**Severity:** Resolved for current internal-test artifact

**Evidence:** [`app/build.gradle.kts`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build.gradle.kts:21)

At the time of this audit, the `release` build type did this:

- sets `signingConfig = signingConfigs.getByName("debug")`
- includes a comment explicitly saying real release signing is still pending

**Why this matters**

This is the most important blocker in the whole audit.

The first Play upload should not establish its release path using debug-signing assumptions. Even if a bundle is technically accepted, it creates avoidable risk around:

- upload-key hygiene
- future release continuity
- Google Sign-In / Billing fingerprint alignment
- general release trust

### Follow-up

After this audit, release signing was changed so the app no longer points release builds at the debug signing config. It now expects real local signing values from ignored `keystore.properties` or environment variables. A fresh signed AAB still must be generated after local signing secrets are provided.

That follow-up is now complete:

- `./gradlew --no-daemon --no-configuration-cache :app:bundleRelease` succeeded
- [app-release.aab](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build/outputs/bundle/release/app-release.aab) was generated
- `jarsigner` reported `jar verified`
- the AAB upload certificate fingerprint matches the expected upload key:
  - SHA-1: `EE:FA:73:EF:A2:F0:6A:A1:8F:03:A8:0E:C4:A4:20:F7:65:33:A3:9C`
  - SHA-256: `63:0E:62:5F:A1:14:13:C9:A0:FB:2B:53:E8:4B:5A:D2:B3:03:11:B5:0D:52:4F:42:B9:92:75:0E:2C:7E:F9:0A`

### 2. Play Billing products do not exist yet

**Severity:** Critical

**Evidence:**

- [`phase-16-sprint-16-4-real-play-billing-plan.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-sprint-16-4-real-play-billing-plan.md:63)
- [`RealBillingRepository.kt`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/core/billing/src/main/kotlin/phonedown/core/billing/RealBillingRepository.kt:95)

The runtime is now wired to real Play Billing behavior. If product lookup returns nothing, the app throws a user-facing failure:

- `"No Play Billing products are available yet. Finish Play Console product setup and try again."`

**Why this matters**

Once Play-distributed testing begins, the app is no longer protected by fake pricing/product fallback. That is good product honesty, but it means the console side must now be real before billing can be considered launch-ready.

**Decision**

- Before meaningful internal testing, create:
  - `pro_monthly`
  - `pro_yearly`
  - `pro_lifetime`

### 3. Play-installed validation is still missing for the real external-service stack

**Severity:** Critical

**Evidence:** [`docs/phase-16-manual-qa.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-manual-qa.md:160)

The app has strong debug/device validation for several flows now, but the following are still not proven on a Play-installed build:

- Google Sign-In
- Drive backup and restore
- monthly purchase
- yearly purchase
- lifetime purchase
- restore purchases
- cancellation / relaunch / recovery

**Why this matters**

Auth, Billing, and Play-side entitlement behavior can differ between:

- adb-installed debug builds
- local release installs
- Play-distributed internal-test installs

This is one of the most common "it worked locally, but failed in Play testing" traps.

## Medium-Risk Gaps

### 4. Android system backup is still enabled alongside app-managed Drive backup

**Severity:** Medium

**Evidence:** [`AndroidManifest.xml`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/AndroidManifest.xml:11)

The app still sets:

- `android:allowBackup="true"`

There are no explicit backup rules visible alongside it.

**Why this matters**

Phone Down now has a real, explicit backup and restore product surface. If Android system backup also restores app data independently, the user can end up with two overlapping restore channels:

- app-managed Google Drive backup
- Android OS auto-backup/restore

That creates ambiguity for:

- "Delete All Data" expectations
- backup ownership
- restore correctness
- privacy claims

**Decision needed**

Pick one of these explicitly before broader release:

1. set `allowBackup=false`
2. keep OS backup enabled but define explicit backup rules

### 5. Crash reporting is not operational yet

**Severity:** Medium

**Evidence:**

- [`app/build.gradle.kts`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build.gradle.kts:1)
- [`docs/release-readiness.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/release-readiness.md:83)

The app is wired for Google services config, but there is still no active Firebase Crashlytics plugin/runtime integration in the shipping app.

**Why this matters**

This is not a blocker for uploading to internal testing, but it is a real production-readiness gap:

- if early Play testers hit crashes
- there is no first-party crash/ANR visibility loop

That slows recovery and makes real-device failures harder to debug.

### 6. Auto-backup is real, but not fully "fire-and-forget" forever

**Severity:** Medium

**Evidence:** [`AutoBackupWorker.kt`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/java/phonedown/app/backup/AutoBackupWorker.kt:48)

When Drive authorization expires or needs user resolution, the background worker can skip backup without surfacing a strong user-visible signal in that moment.

**Why this matters**

This does not invalidate the feature. Manual backup/restore is real and already QA'd. But the product promise of "daily auto backup" is only as reliable as long-lived Drive authorization remains healthy.

This is acceptable for internal testing, but worth hardening before calling the feature fully production-polished.

### 7. Manage Subscription path is not yet deeply validated

**Severity:** Medium

**Evidence:** [`ProRoute.kt`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/java/phonedown/app/pro/ProRoute.kt:56)

The current subscription management path opens the Play subscription management URL.

**Why this matters**

That may be sufficient, but it has not yet been proven as part of an end-to-end Play-distributed purchase/cancel/recovery flow. This becomes important once the real monthly/yearly subscriptions are active.

## Low-Risk But Should-Fix Items

### 8. Release-facing docs are stale and can mislead console work

**Severity:** Low to Medium operational risk

**Evidence:**

- [`docs/release-readiness.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/release-readiness.md:3)
- [`docs/security.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/security.md:61)
- [`docs/architecture-guide.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/architecture-guide.md:121)

Several docs still describe old realities such as:

- auth/billing/backup being fake
- certificate pinning being configured when it is now intentionally relaxed after the Google TLS issue
- readiness status being higher than the current repo truth

**Why this matters**

Stale docs are not just cosmetic here. They can directly cause wrong Play Console answers, wrong testing assumptions, or the false belief that a release-hardening step is already complete.

### 9. Placeholder support/security contact info still exists

**Severity:** Low, but not shippable

**Evidence:**

- [`docs/privacy-policy.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/privacy-policy.md:104)
- [`docs/security.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/security.md:92)
- [`docs/play-store-data-safety.md`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/play-store-data-safety.md:9)

Store- and policy-facing content still contains placeholder contact identity such as:

- `support@phonedown.app`
- `security@phonedown.app`

These need to be replaced before anything resembling real external distribution.

### 10. Settings version label appears out of sync

**Severity:** Low

**Evidence:**

- build version in [`app/build.gradle.kts`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build.gradle.kts:13) is `1.0.2`
- Settings UI now receives the live app version from [`SettingsRoute.kt`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/src/main/java/phonedown/app/settings/SettingsRoute.kt:75), which keeps the visible version aligned with release metadata.

This is small, but it makes internal testing slightly more confusing because testers can no longer trust the displayed app version.

## Interpretation of the Current AAB

### Current artifact

- [`app/build/outputs/bundle/release/app-release.aab`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build/outputs/bundle/release/app-release.aab)

### What this proves

- The repo can now produce a release bundle locally.
- The release pipeline is not fundamentally broken.

### What it does **not** prove

- That the signing identity is correct for Play upload
- That Play-installed auth/billing/backup will work
- That the store/policy/release paperwork matches the codebase
- That the current AAB is the right candidate for the first internal Play track

## Go / No-Go Decision

### Upload current AAB to Play internal track right now?

- **No-go**

### Why

Because the current artifact is still being built with debug signing and the release-side environment has not yet been validated in the exact places that matter most:

- Play Billing products
- Play-installed auth
- Play-installed Drive backup
- purchase / restore / cancellation / recovery

### Is the repo close?

- **Yes**

This is not a "weeks away" situation. It is more like:

- a few real blockers
- a handful of medium-risk release decisions
- and then a real internal-testing candidate

## Recommended Pre-Upload Order

### Must fix before first internal Play upload

1. Configure real release/upload signing in [`app/build.gradle.kts`](/Users/ayushjaipuriar/Documents/GitHub/phone-down/app/build.gradle.kts:21)
2. Rebuild the release bundle after signing is corrected
3. Create Play Billing products:
   - `pro_monthly`
   - `pro_yearly`
   - `pro_lifetime`
4. Refresh the stale release/security/store docs so Play Console answers match actual app behavior

### Must validate immediately after first internal Play upload

1. Google Sign-In on Play-installed build
2. manual backup
3. restore
4. monthly purchase
5. yearly purchase
6. lifetime purchase
7. restore purchases
8. cancellation and relaunch recovery
9. core focus/session/device-behavior regression pass

### Can wait until after internal testing if needed

1. Crashlytics integration
2. auto-backup auth-expiry hardening
3. Settings version/polish cleanup
4. deeper docs cleanup beyond the release-critical set

## Final Recommendation

Phone Down is **not yet production-ready for Play upload**, but it is **close to a safe internal-testing candidate**.

The key distinction is:

- **buildable**: yes
- **uploadable without avoidable risk**: not yet

The single most important next step is:

> Fix release signing, then generate a fresh signed AAB and use that as the real first internal candidate.

After that, the truth-telling step is Play-distributed QA of auth, backup, and billing.
