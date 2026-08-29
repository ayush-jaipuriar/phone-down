# Public-Free Release QA

## Purpose

Use this checklist to capture only release evidence for Phone Down's public-free
launch. Do not record personal, financial, merchant, account, contact,
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

- [ ] Release build completes.
- [ ] Public artifact has no billing runtime dependency.
- [ ] All app features work without a purchase, restore, upgrade, price, or subscription surface.
- [ ] Sign-in and opt-in backup behavior work independently of entitlement.
- [ ] Privacy policy and data-safety declarations describe the free runtime.
- [ ] No crash, ANR, or material usability regression is found in device QA.

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
