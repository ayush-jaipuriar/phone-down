# Public-Free Release QA

## Purpose

Use this checklist to capture only release evidence for Phone Down's target
public-free launch. Do not describe the current artifact as fully free until
every applicable local release-candidate and Play-installed QA gate below
passes. Do not record personal, financial, merchant, account, contact,
application, or KYC-reference values in this file.

## Console Gate - 2026-08-29

| Area | Observed status | Required before production | Evidence to retain outside git |
|---|---|---|---|
| App state | Draft / internal testing | Yes | Console task state without personal details |
| Developer identity | Registered; completion state not observed | Yes | Completion state or unresolved task wording |
| Contact verification | Email and phone verified | Yes | Completion state only |
| Device verification | Not observed | Yes if prompted | Completion state or unresolved task wording |
| Merchant onboarding | Paused | No | None for free launch |
| Monetization products | No active product or subscription observed | No | Product-state summary only |
| Closed testing | 1 of 5 setup tasks complete; 0 opted-in testers | Yes if account is affected | Tester count and testing-duration evidence |
| Production access | Not eligible until testing prerequisites finish | Yes | Eligibility or approval state |

## Local Release Candidate

- [x] Release build completes.
- [x] Public artifact has no billing runtime dependency.
- [x] Automated and screenshot tests cover app features without a purchase, restore, upgrade, price, or subscription surface.
- [x] Unit tests prove sign-in and opt-in backup behavior is independent of entitlement.
- [x] Privacy policy and data-safety declarations describe the free runtime.
- [ ] No crash, ANR, or material usability regression is found in device QA.

Automated evidence, 2026-08-31:

- `./scripts/check.sh`: pass.
- Full debug/release JVM unit and Paparazzi suites: pass.
- Compose Android-test APK assembly: pass.
- `lintDebug`, release lint-vital, ktlint, and detekt: pass.
- Signed APK and AAB builds: pass.
- Bundletool validation and APK signature verification: pass.
- Package/version: `phonedown.app` / `5 (1.0.4)`.
- Release graph and manifest: no BillingClient or billing permission.
- CSV export regression coverage proves recreation-safe document-result
  coordination, cancellation, success/failure feedback, truncating writes,
  complete older-history visibility, background load/open/write execution, and
  provider/write failure handling.
- Account-deletion cloud-failure coverage proves sessions, penalties, settings,
  token, sign-in state, and backup schedule remain unchanged.
- Final AAB SHA-256: `57386468f03d3d9271e52502cb7894f6b93dbf1b42cc45793fb202b0a444d4e6`.
- Final APK SHA-256: `ec56626c29e57e1b20f1e7c8a69cfb77accba8bb3878d26dd40a7ca682d2e4a9`.
- Physical device: not connected; connected and manual QA remain open.

## Play-Installed QA

- [ ] Internal build installs through the Play testing path.
- [ ] Core focus flow, history, insights, settings, and Pro overview work.
- [ ] No purchase dialog, product query, restore-purchase action, or subscription-management action is reachable.
- [ ] Closed-testing setup, tester opt-in, and required duration are complete when required by the account.
- [ ] Mandatory identity, contact, device, package, account, and app-content tasks are complete.
- [ ] Production access is granted before production submission.

## Evidence Rules

- Record status, date, and category only in git.
- Keep personal or financial evidence outside the repository.
- Do not retry merchant onboarding while the public-free release is in effect.
- Re-audit live Console state immediately before production submission.
