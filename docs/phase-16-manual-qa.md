# Phase 16 Manual QA Guide

## 1. Purpose

This guide is the manual QA playbook for Phone Down’s production-readiness phase.

It is written for two kinds of readers at once:

- someone testing the app carefully for the first time
- someone trying to understand why each test exists

Automated tests already protect a lot of the codebase:

- pure session-engine rules
- insight calculations
- mapper correctness
- some UI regressions

Manual QA still matters because the hardest Android production bugs often live in places that are only partly visible to unit tests:

- sensors
- notifications
- system permissions
- foreground service lifecycle
- Google Sign-In
- Play Billing
- Google Drive backup/restore
- release build behavior from Play-distributed installs

The theory here is simple:

- automated tests protect logic
- manual QA protects reality

## 1.1 What Good QA Looks Like In This Project

Good QA here does not mean tapping around randomly until something feels off.

Good QA means:

- testing one hypothesis at a time
- knowing why the test matters
- recording the environment precisely
- being clear about expected vs actual behavior

That precision matters because Android bugs are often environment-sensitive.

## 2. How To Use This Guide

Use this guide in three stages:

1. before release integration is complete
   - mainly core focus/session QA on debug builds
2. after real Google services are wired
   - sign-in, backup, billing, and release behavior QA
3. before production submission
   - final regression sweep across real devices and Play tracks

For each test:

- read the `why` section first
- run the steps exactly
- record the result honestly
- capture any bug using the template at the end

## 2.1 A Useful Mental Model

Each serious QA test in this guide is checking one of four trust layers:

1. product truth
2. system truth
3. platform truth
4. release truth

Product truth means the app behaves according to its promise.

System truth means Android permissions, notifications, and services behave as expected.

Platform truth means Google systems like Sign-In, Drive, and Billing work correctly.

Release truth means the Play-distributed build behaves like the thing users will actually install.

## 3. QA Philosophy for Phone Down

Phone Down is not a conventional CRUD app. It makes a physical promise:

> focus time only counts while the phone is actually down and stable

That means QA has to validate more than “the screen looks right.”

We are testing trust:

- does the timer count honestly?
- do interruptions behave honestly?
- does backup really restore what it claims?
- do purchases really unlock Pro?
- do release builds behave the same way users will experience them?

If any of those are misleading, the app loses credibility quickly.

## 3.1 Thinking Like A Product QA Reviewer

A product QA reviewer is not just looking for crashes. They are also looking for honesty.

Examples:

- backup saying “success” without a meaningful restore path is a trust bug
- the timer display disagreeing with persisted session state is a trust bug
- Pro appearing unlocked unreliably is a trust bug

## 4. QA Status

- Current phase: Phase 16 - Android Production Readiness
- Current sprint: Sprint 16.1 - Release Infrastructure and Docs
- Manual QA status: In progress
- Last updated: 2026-05-12

### Latest Evidence Snapshot

- Date: 2026-05-12
- Device: RMX3686
- Android version: 15
- Install source: local debug install over wireless `adb`
- QA mode: automated physical-device smoke pass plus connected-test harness check
- Outcome:
  - latest debug build installed successfully on the connected device
  - app launched successfully and remained stable during navigation
  - automated adb-driven smoke pass verified the Focus, Insights, and Settings tabs
  - starting a focus session reached the waiting state correctly
  - canceling from the waiting state returned to Focus idle state correctly
  - Insights reflected the canceled session as an invalidated `0m` session, confirming persistence and navigation wiring
  - connected Android instrumentation tests initially failed because library modules were missing the AndroidX test runner and Compose host Activity wiring
  - the connected test harness was repaired and then passed on the physical device for Focus, Insights, and Settings
  - the latest debug app build was assembled and installed successfully after the test fixes
- Verification commands:
  - `ANDROID_SERIAL='192.168.1.14:44625' ./gradlew --no-configuration-cache :feature:focus:connectedDebugAndroidTest :feature:insights:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest`
  - `ANDROID_SERIAL='192.168.1.14:44625' ./gradlew --no-configuration-cache :app:assembleDebug :app:installDebug`
  - `git diff --check`
- Harness fixes made during this QA pass:
  - Android library modules now use `androidx.test.runner.AndroidJUnitRunner`
  - Android library modules now target SDK 36 so Android 15 does not show deprecated-target behavior for test APKs
  - Compose feature instrumentation tests now launch with a real `ComponentActivity`
  - stale Focus, Insights, and Settings test expectations were updated to match the current polished UI
  - Settings is now vertically scrollable so lower About actions are reachable on real phone screens
- Important limitation:
  - full end-to-end automation of the core face-down timing promise is still incomplete on a real device because adb can drive UI, but it cannot truthfully reproduce the physical phone-down and stability conditions that this app depends on
- Device-specific note:
  - `adb shell pm clear phonedown.app` failed with a device-side `SecurityException`, so clean-state retesting on this ROM should use uninstall/reinstall or in-app reset flows instead

## 5. Test Environments

### Device Matrix

| Device | Android Version | Install Source | Account Type | Tester | Status | Notes |
|---|---:|---|---|---|---|---|
| TBD | TBD | Local debug | Owner | Ayush | Not started | Use for sensor and core focus checks |
| TBD | TBD | Play internal test | License tester | TBD | Not started | Use for billing/auth/Drive checks |
| TBD | TBD | Play closed test | Closed tester | TBD | Not started | Required if Google requires closed testing before production |

### Build Matrix

| Build | Purpose | Status | Notes |
|---|---|---|---|
| Debug APK | Fast local QA | Not started | Good for feature work and adb installs |
| Release AAB | Play internal testing | Not started | Needed before Play-distributed QA |
| Play internal install | Billing/auth/release-signing QA | Not started | Installed from Play opt-in link |
| Play closed test install | Production-access evidence | Not started | Needed if Google enforces tester requirement |

### Theory

Why multiple build/install paths exist:

- debug APK proves local engineering behavior
- Play-installed builds prove production ecosystem behavior

Some failures only show up on Play-installed builds because:

- Play signs the app differently
- Billing requires Play context
- Sign-In can depend on the correct certificate fingerprints

### Study Note

If something works locally but fails only on a Play install, suspect release identity and console configuration early, not only app code.

## 6. Required Accounts

| Account | Purpose | Status | Notes |
|---|---|---|---|
| Play Console owner | Publishing | Not started | Must have 2-Step Verification |
| Google Cloud/Firebase owner | OAuth, Drive, Crashlytics | Not started | Recommended dedicated `phone-down` project |
| Play license tester | Billing tests | Not started | Added in Play Console |
| Google Sign-In tester | Auth/Drive tests | Not started | Added to OAuth test users if app is in testing mode |
| Closed test users | Production-access evidence | Not started | 12 opted-in testers for 14 days if required |

### Theory

These are not redundant accounts. Each one unlocks a different Google-controlled workflow:

- license testers let you safely simulate Play purchases
- OAuth test users let sign-in work before general publication
- closed testers satisfy Play’s trust process for new publishers if required

## 7. Automated Verification Before Manual QA

Run these before each serious manual QA round:

```bash
./gradlew --no-configuration-cache :app:assembleDebug
./gradlew --no-configuration-cache :domain:session:test :domain:insights:test
./gradlew --no-configuration-cache :core:backup:testDebugUnitTest :core:database:testDebugUnitTest
./gradlew --no-configuration-cache :app:testDebugUnitTest
./gradlew --no-configuration-cache :feature:focus:verifyPaparazziDebug :feature:settings:verifyPaparazziDebug :feature:insights:verifyPaparazziDebug
git diff --check
```

Before release upload:

```bash
./gradlew --no-configuration-cache :app:bundleRelease
```

Known caveat:

- `./scripts/check.sh` may still fail on existing ktlint disagreements around PascalCase Compose naming and project formatting conventions. If it fails, record the exact failure and confirm whether it is pre-existing or newly introduced.

### Theory

Manual QA should not be used as a substitute for obvious automated breakage.

Run automation first because:

- it catches basic regressions cheaply
- it prevents you from wasting device-testing time on something already broken

## 7.1 How To Capture Evidence Well

For every meaningful QA run, try to capture:

- device model
- Android version
- install source
- signed-in state
- whether the build came from Play or local debug
- screenshots or short screen recordings when useful

## 8. Severity Model

Use this when recording bugs.

| Severity | Meaning |
|---|---|
| P0 | Release-blocking crash, data loss, broken purchase, broken restore, privacy/security issue |
| P1 | Major user flow broken or misleading |
| P2 | Important papercut, confusing state, non-critical edge case |
| P3 | Minor polish, copy, or low-risk UX issue |

### Theory

Not every bug should block release. Severity helps us decide what must be fixed before production and what can wait for follow-up.

## 8.1 How Severity Maps To Release Decisions

### P0

Usually blocks any release candidate.

Examples:

- broken purchase flow
- restore causing data loss
- crash on startup
- privacy or credential leak

### P1

Usually blocks production, even if internal testing can continue.

Examples:

- Sign-In broken on Play-installed build
- timer counting incorrectly
- notification actions failing consistently

### P2

Important but not always production-blocking by itself.

Examples:

- confusing state copy
- one-device inconsistency
- non-fatal edge-case mismatch

### P3

Minor polish issue.

## 9. Core Focus Session QA

These tests are the heart of the app. Even if billing and backup are perfect, the release is not good if focus timing is not trustworthy.

## 9.0 Before You Start Core Focus QA

Before running these tests, check:

- sound enabled
- haptics enabled
- battery saver off unless the test explicitly involves system constraints
- screen timeout not set to something extreme
- the phone surface/case is not obviously interfering with face-down detection

Why this matters:

- sensor-heavy apps can look broken when the environment is the real variable

### Test 9.1 - First Launch and Onboarding

Why this test exists:

- onboarding is the first impression
- it gates the initial navigation flow
- bugs here can make the app feel broken before the user even starts a session

Preconditions:

- fresh install
- app data cleared

Steps:

1. Launch the app.
2. Verify onboarding appears.
3. Complete the 3-card onboarding flow.
4. Relaunch the app.

Expected:

- onboarding appears only on first launch
- after completion, app opens to Focus
- relaunch does not show onboarding again

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.2 - Notification Permission Flow

Why this test exists:

- Android 13+ requires runtime notification permission
- the foreground service depends on notifications
- a bad permission flow can prevent sessions from starting cleanly

Preconditions:

- Android 13+ device
- notification permission not yet granted

Steps:

1. Open Focus.
2. Select a short duration.
3. Tap Start Focus.
4. Respond to the notification permission prompt.

Expected:

- permission prompt appears before foreground notification is needed
- if granted, session starts normally
- if denied, app does not crash and user can retry later

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.3 - Face-Down Detection and Arming

Why this test exists:

- this is the app’s defining behavior
- it validates the sensor pipeline, arming logic, and feedback cues together

Preconditions:

- notification permission granted
- sounds and haptics enabled

Steps:

1. Start a 1-minute or 5-minute session.
2. Place the phone face down and keep it stable.
3. Wait through the arming countdown.
4. Pick the phone up after 10-20 seconds.

Expected:

- phone-down is detected
- haptic cue occurs when the phone-down condition is recognized
- start sound plays when the timer truly starts
- timer accumulates valid focus time
- after pickup, remaining time reflects actual elapsed focus

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.4 - Clean Completion

Why this test exists:

- verifies the happy path
- validates completion sound/haptics, final classification, and metrics updates

Steps:

1. Start a short session.
2. Place phone face down and stable.
3. Do not pick it up until completion.
4. Return to the app after completion.

Expected:

- session completes
- completion sound/haptic plays if enabled
- summary shows clean completion
- Focus and Insights today metrics increment consistently

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.5 - Pickup Interruption

Why this test exists:

- interruptions are where trust can quietly break
- the user needs the app to be strict without being confusing

Steps:

1. Start a short session.
2. Let the timer run for at least 10 seconds.
3. Pick up the phone.
4. Wait through the interruption state.
5. Put the phone down again.

Expected:

- app shows paused/interrupted state
- clean status is lost
- interruption and penalty behavior matches the rules
- resume requires phone down and arming again

What a failure here usually points to:

- sensor-validity edge detection issue
- runtime coordinator transition issue
- session-engine interruption-rule issue

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.6 - Manual Pause and Resume

Why this test exists:

- manual pause is now a real engine behavior, not UI-only
- it must not silently keep counting time in the background

Steps:

1. Start a session and reach Active state.
2. Tap Pause.
3. Wait 15 seconds.
4. Tap Resume.
5. Put phone face down again.

Expected:

- focus time stops during pause
- session becomes non-clean
- resume returns to waiting/arming
- paused time is not counted as focus time

What a failure here usually points to:

- engine transition bug
- persistence drift
- UI reading stale state instead of real state

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.7 - Add Time

Why this test exists:

- add-time used to be cosmetic only
- we now need to prove it changes real domain behavior

Steps:

1. Start a short session.
2. Reach Active state.
3. Tap Add Time.
4. Add 1 minute.
5. Observe remaining time and completion threshold.

Expected:

- required duration increases
- remaining time updates from real session state
- session does not complete at the original duration

What a failure here usually points to:

- add-time not reaching the session engine
- timer math still being partially UI-derived

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.8 - Notification Tap and End Action

Why this test exists:

- notification routing is part of the foreground-service contract
- this was previously a trust gap

Steps:

1. Start a session.
2. Navigate away from Focus or background the app.
3. Open the notification shade.
4. Tap the notification body.
5. Repeat and tap the End action.

Expected:

- notification body always routes to Focus
- End action ends the session safely
- no duplicate or phantom sessions appear

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.9 - App Kill Recovery

Why this test exists:

- apps die in the real world
- recovery behavior must be honest, not optimistic

Steps:

1. Start a session and reach Active.
2. Kill the app from recents or force stop for the specific test variant.
3. Relaunch the app.

Expected:

- app stays stable
- it does not falsely pretend uninterrupted focus continued
- dangling sessions are classified according to recovery rules

What a failure here usually points to:

- recovery classification bug
- persistence flush issue
- startup coordination issue

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 9.10 - Device Reboot Recovery

Why this test exists:

- reboot is the harshest realistic interruption path
- if this is wrong, session history can become dishonest

Steps:

1. Start a session and reach Active.
2. Reboot the device.
3. Unlock and relaunch Phone Down.

Expected:

- no crash on startup
- reboot recovery behaves conservatively
- session is not falsely completed as clean

Result:

- Status:
- Device:
- Build:
- Notes:

## 10. Google Sign-In QA

These tests become active after real auth implementation lands.

## 10.0 Why Auth QA Is Different

Auth bugs often look like app bugs when they are really configuration bugs.

Examples:

- missing SHA fingerprint
- test user not added
- wrong Firebase project
- wrong package registration

### Test 10.1 - Sign In Success

Why this test exists:

- verifies OAuth, SHA fingerprint setup, and account UI wiring all at once

Preconditions:

- Firebase and OAuth configured
- tester account added if OAuth app is still in testing mode

Steps:

1. Open Account.
2. Tap Sign in with Google.
3. Select a valid test account.
4. Complete sign-in.

Expected:

- account screen shows the signed-in account
- name/email are correct
- no sensitive values are exposed in logs or UI

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 10.2 - Sign Out

Why this test exists:

- sign-out must reliably clear user-owned account state
- backup actions should not remain misleadingly enabled

Steps:

1. Sign in.
2. Tap Sign out.
3. Return to Account.

Expected:

- account state clears
- backup actions disable or require sign-in
- repeated sign-in/sign-out does not break state

Result:

- Status:
- Device:
- Build:
- Notes:

## 11. Google Drive Backup and Restore QA

These tests validate whether backup is genuinely real, not just UI-complete.

## 11.0 Why Backup QA Needs Extra Skepticism

Backup systems can look successful while still failing the real user promise.

That is why backup QA should always test both halves:

- can we save data?
- can we truly restore meaningful state later?

### Test 11.1 - Manual Backup

Why this test exists:

- validates the real Drive transport path
- checks the narrow Drive scope and account prerequisites

Preconditions:

- signed in with Google
- Pro entitlement active
- network available

Steps:

1. Complete at least one session.
2. Open Account or Backup area.
3. Tap Backup.
4. Wait for completion.

Expected:

- backup succeeds
- timestamp updates
- backup is stored in Drive app data
- no broad Drive permission is requested

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 11.2 - Full Restore

Why this test exists:

- restore is one of the easiest places for false-success UX
- we need proof that local persistence is actually replaced

Steps:

1. Create backup with known data.
2. Delete local data or install fresh app.
3. Sign in.
4. Restore backup.
5. Check restored sessions/settings/insights.

Expected:

- restore replaces local data
- restored data appears in app surfaces
- success is shown only after persistence succeeds
- restore is blocked during an active session

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 11.3 - Auto-Backup

Why this test exists:

- once-daily auto-backup is a V1 production promise
- WorkManager constraints must behave sanely

Steps:

1. Sign in.
2. Enable backup.
3. Ensure Pro is active.
4. Complete a session.
5. Wait for eligible WorkManager execution under network-connected conditions.

Expected:

- auto-backup runs no more than once daily
- last backup timestamp updates
- auto-backup does not run during active focus session

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 11.4 - Delete Cloud Backup

Why this test exists:

- data deletion must be honest and complete
- cloud backup is part of that trust story

Steps:

1. Ensure a cloud backup exists.
2. Open delete-data flow.
3. choose delete cloud backup if offered.
4. confirm delete.

Expected:

- local data is deleted
- cloud backup is deleted if selected
- any failure is surfaced honestly

Result:

- Status:
- Device:
- Build:
- Notes:

## 12. Play Billing QA

These tests must be done on Play-distributed builds, not just local debug installs.

## 12.0 Why Billing QA Must Use Play Builds

Play Billing is part of the Play ecosystem, not just a normal library call inside the app.

That means local debug installs are not enough to prove:

- real product loading
- real purchase flow
- restore purchases behavior

### Test 12.1 - Product Loading

Why this test exists:

- proves the app is loading real Play products, not placeholders

Preconditions:

- build installed from Play internal testing
- products active in Play Console
- tester account is a license tester

Steps:

1. Open the Pro screen.
2. Wait for products to load.

Expected:

- monthly, yearly, and lifetime products load
- displayed prices match Play Console configuration
- release flow does not fall back to fake/static products

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 12.2 - Monthly Purchase

Why this test exists:

- confirms the most common recurring Pro path

Steps:

1. Tap monthly Pro.
2. Complete the test purchase.
3. Return to app.

Expected:

- purchase is acknowledged
- Pro entitlement activates
- gated features unlock

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 12.3 - Yearly Purchase

Why this test exists:

- yearly products sometimes behave differently because of product setup/base plans

Steps:

1. Use a tester account without active Pro.
2. Tap yearly Pro.
3. Complete purchase.

Expected:

- yearly purchase grants Pro
- Pro state persists
- restore later finds the entitlement

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 12.4 - Lifetime Purchase

Why this test exists:

- lifetime is a different Play product type than subscriptions
- entitlement merge logic must treat it correctly

Steps:

1. Use a tester account without active Pro.
2. Tap lifetime Pro.
3. Complete purchase.

Expected:

- lifetime entitlement activates
- reinstall and restore-purchases still recover Pro
- lifetime beats expired subscription state

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 12.5 - Restore Purchases

Why this test exists:

- users expect reinstall or app reset not to destroy paid access

Steps:

1. Complete a purchase.
2. Reinstall or clear app data.
3. Open Pro screen.
4. Tap Restore Purchases.

Expected:

- previous purchases are found
- Pro is restored without repurchase

Result:

- Status:
- Device:
- Build:
- Notes:

## 13. Crashlytics QA

These tests become relevant after Crashlytics integration lands.

## 13.0 Why Crashlytics QA Matters

Crash reporting is one of those systems you hope not to need, but when you need it, you need it immediately.

If we ship without proving it works:

- the first production crash may be much harder to diagnose

If we ship with overly broad diagnostics:

- we risk collecting more than we intended

### Test 13.1 - Debug Build Does Not Report Crashes

Why this test exists:

- debug noise should not pollute production crash monitoring

Steps:

1. Install debug build.
2. Confirm manifest/BuildConfig uses debug collection disabled.
3. Trigger a controlled test crash only if a debug-only trigger exists.

Expected:

- no debug crash appears in Firebase Crashlytics
- debug builds do not upload Crashlytics reports

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 13.2 - Release/Internal Build Sends Crash

Why this test exists:

- confirms release diagnostics are live before production

Steps:

1. Install Play internal or release-equivalent build.
2. Confirm the Firebase project is the intended Phone Down project.
3. Trigger a controlled test crash only from a tester build where this is safe.
4. Reopen the app once after the crash so Crashlytics can upload the report.
5. Check Firebase Crashlytics console.

Expected:

- crash appears in Crashlytics
- no sensitive user values are attached
- no Google email, Google ID, access token, purchase token, backup payload, or raw session database content appears in keys/logs

Result:

- Status:
- Device:
- Build:
- Notes:

## 14. Privacy and Policy QA

### Test 14.1 - Privacy Policy Access

Why this test exists:

- review and trust depend on discoverable, accurate policy information

Steps:

1. Open Settings.
2. Open Privacy Policy.
3. Read the relevant production sections.

Expected:

- policy is reachable
- policy mentions Google Sign-In, Drive backup, Billing, Crashlytics if enabled, permissions, and deletion

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 14.2 - Data Deletion

Why this test exists:

- deletion is both a privacy requirement and a trust requirement

Steps:

1. create sessions/settings
2. sign in and create backup if available
3. open delete-data flow
4. provide required confirmation
5. delete data

Expected:

- local sessions are removed
- settings reset appropriately
- cloud backup deletion works if selected
- app returns to a coherent default state

Result:

- Status:
- Device:
- Build:
- Notes:

## 15. No-Network QA

### Test 15.1 - Core Timer Offline

Why this test exists:

- local-first is a core product principle

Steps:

1. turn on airplane mode
2. start and complete a focus session

Expected:

- core timer works offline
- local persistence still works
- auth/billing/backup unavailability does not block focus

Result:

- Status:
- Device:
- Build:
- Notes:

### Test 15.2 - Backup/Billing Offline Handling

Why this test exists:

- online-only features should fail gracefully, not poison the whole app

Steps:

1. turn off network
2. attempt backup
3. open Pro screen / attempt product load

Expected:

- backup shows clear network-related failure
- billing handles unavailable product data gracefully
- cached Pro remains active for the current entitlement cache window if applicable

Result:

- Status:
- Device:
- Build:
- Notes:

## 16. Bug Report Template

Use this for every manual QA bug:

```text
### Bug ID: P16-

Severity: P0 / P1 / P2 / P3
Area:
Device:
Android version:
Build:
Install source:
Account:

Why this matters:

Steps:
1.
2.
3.

Expected:

Actual:

Logs/screenshots:

Suspected layer:

Status:
```

## 17. Closed Testing Feedback Log

| Tester | Device | Android | Day | Completed Core Focus? | Tested Billing? | Tested Backup? | Bugs/Feedback |
|---|---|---:|---:|---|---|---|---|
| TBD | TBD | TBD | 1 | No | No | No | Not started |

## 18. Sprint 16.1 Setup Checklist

This checklist covers the prerequisites before service-integration QA begins.

- [ ] Play Console developer account created
- [ ] Phone Down app shell created
- [ ] Internal testing track created
- [ ] Closed testing track created if required
- [ ] Dedicated Google Cloud/Firebase project created
- [ ] Debug SHA added to Firebase/OAuth
- [ ] Upload keystore generated outside repo
- [ ] Upload SHA added to Firebase/OAuth
- [ ] Drive API enabled
- [ ] OAuth consent screen configured
- [ ] Billing products drafted or created
- [ ] Tester list started

## 18.1 Study Questions For QA

Use these while testing or reviewing results:

- Which failures here are most likely caused by code, and which by console configuration?
- Which tests prove product truth, and which prove ecosystem truth?
- If a test passes on debug but fails on a Play install, which identity or signing assumptions should we question first?

## 19. What “Ready For Real Integration QA” Means

We can move from setup work into real auth/billing/backup QA once these are true:

- Play Console app exists
- Firebase project exists
- OAuth consent is configured
- Drive API is enabled
- debug and upload SHA values are registered
- billing product IDs are decided
- `google-services.json` is downloaded locally

## 20. Current Next Step

The automated physical-device smoke and connected test harness pass is now complete for the current debug build.

Next steps:

- run one short human-in-the-loop physical sensor check for the real face-down timing promise
- then continue console setup using [docs/play-console-release-guide.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/play-console-release-guide.md) and [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md)
- once console setup is complete, move into real Google Sign-In implementation and the first genuine production-integration QA pass
