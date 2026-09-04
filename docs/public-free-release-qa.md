# Public-Free Release QA

## Purpose

Use this checklist to capture only release evidence for Phone Down's target
public-free launch. Do not describe the current artifact as fully free until
every applicable local release-candidate and Play-installed QA gate below
passes. Do not record personal, financial, merchant, account, contact,
application, or KYC-reference values in this file.

## Console Gate - 2026-09-04

| Area | Observed status | Required before production | Evidence to retain outside git |
|---|---|---|---|
| App state | Closed-test draft `6 (1.0.5) - Public Free` | Yes | Console task state without personal details |
| Developer identity | Registered; completion state not observed | Yes | Completion state or unresolved task wording |
| Contact verification | Email and phone verified | Yes | Completion state only |
| Device verification | Not observed | Yes if prompted | Completion state or unresolved task wording |
| Merchant onboarding | Paused | No | None for free launch |
| Monetization products | No active product or subscription observed | No | Product-state summary only |
| Closed testing | 3 of 4 track tasks complete; version code 6, 177 countries/regions, 4-user tester list, and feedback path saved | Yes if account is affected | Tester opt-in count and testing-duration evidence |
| Production access | Not eligible until testing prerequisites finish | Yes | Eligibility or approval state |

## Local Release Candidate

- [x] Release build completes.
- [x] Public artifact has no billing runtime dependency.
- [x] Automated and screenshot tests cover app features without a purchase, restore, upgrade, price, or subscription surface.
- [x] Unit tests prove sign-in and opt-in backup behavior is independent of entitlement.
- [x] Privacy policy and data-safety declarations describe the free runtime.
- [ ] No crash, ANR, or material usability regression is found in device QA.

Automated and device evidence, 2026-09-01:

- `./scripts/check.sh`: pass.
- Full debug/release JVM unit and Paparazzi suites: pass.
- Compose Android-test APK assembly: pass.
- `lintDebug`, release lint-vital, ktlint, and detekt: pass.
- Signed APK and AAB builds: pass.
- AAB ZIP integrity, JAR signature, and APK signature verification: pass.
- Package/version: `phonedown.app` / `6 (1.0.5)`.
- Foreground-service declaration uses `specialUse` for the user-started active
  focus session, with the required subtype explanation; `dataSync` is absent.
- Release graph and manifest: no BillingClient or billing permission.
- CSV export regression coverage proves recreation-safe document-result
  coordination, cancellation, success/failure feedback, truncating writes,
  complete older-history visibility, background load/open/write execution, and
  provider/write failure handling.
- Account-deletion cloud-failure coverage proves sessions, penalties, settings,
  token, sign-in state, and backup schedule remain unchanged.
- Final AAB SHA-256: `7a72957ef8be4eb0c288e5017b3a343031c00996bddac3ff81d3696e97d2a8c5`.
- Final APK SHA-256: `872f44893bc6e9064a56fa789f87ef0a211c877d40af92cd8a3582c4a2dbee1d`.
- Play Console accepted the final AAB in closed-test draft `6 (1.0.5) - Public Free`.
- Version code 5 was removed after version code 6 replaced it in the draft.
- Play's native-code debug-symbol warning remains non-blocking for this draft.
- All 177 available countries/regions and the existing 4-user tester list were
  selected; the feedback path was configured without recording its contact
  value in git.
- Preview, review submission, and rollout were intentionally not performed.
- Physical device: RMX3853 on Android 16, authorized over wireless ADB.
- Full `connectedDebugAndroidTest`: pass across 860 Gradle tasks. Named suites
  covered database (6), account (2), focus (13), insights (5), Pro (2), and
  settings (8) tests; modules without instrumentation tests also installed and
  launched their generated test APKs successfully.
- Live debug smoke: onboarding, Focus, empty Insights, Settings, and Pro overview
  rendered without a crash. No purchase, upgrade, subscription, price, billing,
  restore-purchase, or manage-subscription text was present in the UI hierarchy.
- Manual timed-session, sensor, sign-in/backup, permission, process-death,
  reboot, and offline/network-restoration checks remain open.

## Play-Installed QA

- [ ] Internal build installs through the Play testing path.
- [ ] Core focus flow, history, insights, settings, and Pro overview work.
- [ ] No purchase dialog, product query, restore-purchase action, or subscription-management action is reachable.
- [ ] Closed-test preview/submission, tester opt-in, and required duration are complete when required by the account.
- [ ] Mandatory identity, contact, device, package, account, and app-content tasks are complete.
- [ ] Production access is granted before production submission.

## Evidence Rules

- Record status, date, and category only in git.
- Keep personal or financial evidence outside the repository.
- Do not retry merchant onboarding while the public-free release is in effect.
- Re-audit live Console state immediately before production submission.
