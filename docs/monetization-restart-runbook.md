# Monetization Restart Runbook

Status: dormant until Q1 2027 prerequisites are satisfied

## Purpose

This runbook prevents Phone Down monetization from being re-enabled through a
partial code or Console change. The public app remains free. Restart work begins
only after the address, document, location, merchant, product, policy, and user
transition decisions below are current and approved.

Do not store identity documents, addresses, bank details, merchant references,
application identifiers, screenshots containing personal data, tokens, or
credentials in this repository.

## Restart Triggers

All conditions must be true before merchant onboarding resumes:

- The user has moved to the actual residence.
- A registered rent agreement is effective and acceptable for that residence.
- The declared current address exactly matches accepted proof.
- Device location during Video KYC reflects the actual residence.
- Identity and bank details remain current and consistent.
- BillDesk confirms whether the existing invitation can resume or a new one is required.
- Current Google Play merchant and BillDesk document requirements are rechecked from official sources.
- A separate monetization implementation plan is approved.

The Q1 2027 date is a review window, not permission to proceed when any trigger
is false.

## Private Preparation Checklist

Prepare outside git:

- Original identity and PAN documents requested by the provider.
- Registered rent agreement and any currently accepted supporting proof.
- Bank ownership evidence matching the merchant profile.
- Stable internet, camera, microphone, and location permission.
- Clear background and sufficient light for Video KYC.
- Business and app description consistent with the live Play listing.

Stop before submission if an address, document, location, name, or bank-owner
value conflicts. Correct the source profile or obtain accepted evidence before
retrying. If an invitation has expired, use the official support or restart
path instead of creating duplicate applications.

## Engineering Restart Gate

The new approved plan must cover, together:

- Re-adding `:core:billing` to the public app dependency graph.
- Replacing `FreeAccessBillingRepository` with `RealBillingRepository` through explicit DI.
- Restoring a truthful paywall, product state, purchase, restore, and subscription-management UX.
- Creating and activating the corresponding Play products.
- Deciding whether server-side purchase verification is required.
- Testing purchase, pending, cancel, acknowledgment, restore, renewal, expiry, refund, offline, and account-switch behavior.
- Updating privacy, Data Safety, listing, release notes, support, and account-deletion documentation.
- Running local, physical-device, internal/closed-track, Play-installed, regression, and staged-rollout QA.
- Incrementing the version code and repeating artifact/dependency/manifest validation.

One dependency or DI change is never sufficient authorization to reactivate
monetization.

## Existing User Transition

Do not silently remove features that users received in the public-free app. The
future plan must explicitly choose and obtain approval for one transition:

- grandfather existing installs/accounts,
- provide a defined trial or transition window, or
- introduce a different paid feature boundary that preserves existing core behavior.

The decision must include entitlement migration, offline behavior, backup and
export access, communication, refund/support handling, and rollback behavior.

## Failure Handling

- Address/document/location mismatch: stop and correct before retrying KYC.
- Expired or invalid invitation: use official support; do not duplicate applications blindly.
- Changed document policy: follow the current official accepted-document list.
- Failed test purchase or entitlement migration: halt rollout and ship a forward fix.
- Console or policy uncertainty: preserve the free release and request clarification from the provider.

Only sanitized status, dates, and pass/fail evidence belong in project docs.
