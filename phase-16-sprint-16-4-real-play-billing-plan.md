# Sprint 16.4 - Real Play Billing, Entitlements, And Paywall Readiness Plan

## Status

- Planning status: Drafted
- Implementation status: Not started
- Approval required before implementation: Yes
- Phase: Phase 16 - Android Production Readiness
- Primary goal: Replace fake billing with real Google Play Billing across monthly, yearly, and lifetime Pro, then verify the full purchase, restore, cancellation, and paywall experience on real devices and Play test tracks

## 1. Sprint Purpose

Sprint 16.4 makes Phone Down's Pro system real.

Today, the app already has:

- a Pro paywall UI
- fake billing products
- fake purchase and restore behavior
- Pro gating across settings, insights, and backup features
- entitlement caching logic and Pro-aware UI states

What it does **not** yet have is a production billing authority.

That means the app currently looks purchasable, but Google Play is not yet the real source of truth for:

- what products exist
- what they cost
- whether the user owns Pro
- whether a subscription has been canceled, restored, or expired

This sprint closes that gap by implementing:

- real monthly subscription
- real yearly subscription
- real lifetime one-time purchase
- real restore purchases behavior
- real entitlement activation and downgrade rules
- billing failure and unavailable-product UX
- paywall copy/price-display refinement
- cancellation/recovery QA in a Play testing environment

This sprint matters because billing is not just another API integration. It is a trust, policy, and revenue system. If users can buy Pro, then:

- the paywall must be honest
- Play must be the authority
- entitlement must survive reinstall and app restarts
- cancellation and restore behavior must feel predictable

## 2. Confirmed Product Decisions

- [x] Include full billing scope in this sprint:
  - monthly subscription
  - yearly subscription
  - lifetime purchase
  - restore purchases
  - entitlement activation
  - end-to-end paywall wiring
- [x] Use these product IDs unless Play Console forces a naming adjustment:
  - `pro_monthly`
  - `pro_yearly`
  - `pro_lifetime`
- [x] Play Billing products are **not yet created** in Play Console.
- [x] Recommended sequencing is accepted:
  - repo-side billing integration first
  - then Play Console product setup/testing
  - then device QA with license testers
- [x] Entitlement rules:
  - active monthly subscription grants Pro
  - active yearly subscription grants Pro
  - acknowledged lifetime purchase grants Pro permanently
  - restore purchases should re-enable Pro if Google Play reports entitlement
- [x] Keep the existing **24-hour entitlement cache** for resilience.
- [x] If products fail to load, show a friendly retry/error state and do not invent fake prices.
- [x] Proper acknowledgment is required:
  - subscriptions acknowledged
  - lifetime purchase acknowledged
  - no consumables in this app
- [x] Source of truth:
  - Play Billing purchase state on device
  - cached locally for resilience
  - not restored from Drive backup
- [x] Sprint also includes:
  - Play Store listing/paywall copy refinement
  - final price-display QA
  - subscription cancellation/recovery QA

## 3. Existing Baseline We Will Build On

### 3.1 Already Present In Repo

- `:core:billing`
  - fake billing implementation
  - repository contract already used by the app
- `:core:model`
  - `BillingRepository`
  - `ProProduct`
  - `ProPurchase`
  - `ProEntitlement`
- `:app`
  - `ProViewModel`
  - `SettingsViewModel` and `AccountViewModel` already respond to entitlement state
- `:feature:pro`
  - paywall screen
  - product card rendering
  - restore-purchases entry point
- broader app
  - Pro gates already exist in settings, insights, backup, and certain customization surfaces

### 3.2 Why This Baseline Is Good

We are **not** inventing a billing UX from scratch.

We are upgrading:

- product loading: fake hardcoded list -> real Play product details
- purchase authority: fake local mutation -> Play purchase state
- entitlement resolution: fake repo -> real merged Play state + cached fallback
- restore behavior: fake repo callback -> `queryPurchasesAsync` / Play-side restore

That is the right sprint shape because it minimizes UI churn while hardening the real source of truth.

## 4. Product Behavior Contract

### 4.1 Paywall Product Display

When the paywall opens, the app should:

1. load real Play products
2. render real localized prices
3. distinguish monthly, yearly, and lifetime clearly
4. show a retry/failure state if products cannot be loaded
5. never display fake fallback prices in production runtime

### 4.2 Purchase Flow

When a user taps a purchasable product, the app should:

1. launch the real Play Billing purchase UI
2. listen for purchase updates
3. acknowledge the purchase when required
4. resolve entitlement from the purchase state
5. update the app UI so Pro gates open naturally without requiring app restart

### 4.3 Restore Purchases

When the user taps `Restore Purchases`, the app should:

1. query existing Play purchases
2. recompute entitlement
3. update local cached entitlement
4. refresh paywall/account/settings UI state
5. clearly communicate success, no-purchase, or failure states

### 4.4 Cancellation And Recovery

If a user cancels a subscription:

- Pro should remain active until Play says the entitlement period is over
- cached entitlement should prevent momentary false downgrades during query hiccups
- after expiry and cache expiry, Pro-only surfaces should downgrade gracefully
- local user data should remain intact

### 4.5 Lifetime Product Behavior

If the user purchases `pro_lifetime`:

- entitlement should remain Pro permanently
- restore purchases should re-detect it after reinstall/sign-in
- subscription cancellation concepts should not affect the lifetime state

## 5. Console And Operational Model

## 5.1 Product Strategy

### Chosen Product IDs

- `pro_monthly`
- `pro_yearly`
- `pro_lifetime`

### Product Types

- `pro_monthly`: subscription
- `pro_yearly`: subscription
- `pro_lifetime`: one-time in-app product

### Why This Shape

This is the simplest V1 mental model.

The alternative would be:

- one subscription product with multiple base plans/offers

That can be elegant, but it also introduces more console complexity for a beginner setup. Since the user asked for handholding and is just getting started with Play Console, separate product IDs are a pragmatic first production pass unless Play Console setup naturally nudges us to a base-plan model we can document clearly.

## 5.2 Testing Model

We should plan for the real-world billing testing ladder:

1. repo-side code integration
2. Play Console product creation
3. license tester setup
4. internal testing track build distribution
5. real purchase/cancel/restore QA on device

Why this matters:

- Billing often behaves differently in plain local installs versus Play-distributed installs
- a build that compiles locally is not enough to prove the billing system works
- Play testing environment setup is part of the implementation, not an afterthought

## 5.3 Entitlement Authority

### Production Rule

Pro entitlement should be derived from:

1. active acknowledged lifetime purchase
2. active acknowledged subscription purchase
3. cached entitlement if billing cannot be queried temporarily and cache age is within 24 hours

It should **not** be derived from:

- Drive backup contents
- hand-authored local flags
- UI-only purchase optimism

### Why This Matters

Backups move user data.

Billing defines commercial access.

Those are different authority domains and should remain separate.

## 6. Technical Design

## 6.1 Keep The Existing BillingRepository Contract Shape Where Reasonable

Current contract:

- `products: Flow<List<ProProduct>>`
- `purchases: Flow<List<ProPurchase>>`
- `entitlement: Flow<ProEntitlement>`
- `loadProducts()`
- `launchPurchaseFlow(product)`
- `restorePurchases()`
- `acknowledgePurchase(token)`

### Recommendation

Keep the app-facing abstraction but refine the implementation model behind it.

That is good because:

- viewmodels already use it
- fake billing can remain for unit tests
- swapping DI from fake to real is low-risk

Likely contract refinement needed:

- inject or coordinate an Activity-bound purchase launcher path from `:app`
- possibly add explicit initialization / connection lifecycle methods if the real client needs them

## 6.2 Real Billing Implementation

Planned production implementation:

- `RealBillingRepository`

Likely supporting helpers:

- Play Billing client wrapper
- product-details mapper
- purchase-state mapper
- entitlement resolver
- cached entitlement persistence helper

### Responsibility Split

`RealBillingRepository`
- product query orchestration
- purchase flow orchestration
- restore purchases orchestration
- entitlement publishing

Billing client wrapper
- manage `BillingClient` connection
- abstract callbacks into suspending/flow-friendly APIs

Entitlement resolver
- determine Pro from current purchases + cached fallback rules

This keeps business semantics out of the raw Play callback layer.

## 6.3 Activity / Purchase Launch Coordination

### Important Theory

Play Billing purchase launch usually needs an Activity context.

That means we should not pretend the repository can launch billing in a vacuum from deep inside `:core:billing` unless we have a clean coordination path.

### Recommendation

Keep the repo as the authority for billing state, but let `:app` own the actual purchase-launch coordination if needed.

Possible shape:

- `ProRoute` / `ProViewModel` asks repository for launch data or triggers a launcher method through an app-layer coordinator

Why:

- consistent with how sign-in and Drive authorization are already handled
- preserves module boundaries
- avoids pushing Android UI context through the core layer

## 6.4 Cached Entitlement Rules

### Rule

If billing queries fail temporarily:

- keep the last-known Pro entitlement for up to 24 hours

If cache is older than 24 hours and billing still cannot verify:

- downgrade gracefully to free
- do not delete local user data
- keep paywall honest about temporary unavailability if relevant

### Why This Rule Is Good

Phone Down is a daily-use focus app.

We do not want:

- airplane mode
- flaky Play Services
- temporary network outages

to immediately strip features from a legitimately paying user.

But we also do not want indefinite Pro based on stale local cache alone.

24 hours is a reasonable resilience window for V1.

## 6.5 Pending / Unavailable / Failure States

We should explicitly model:

- product load in progress
- products unavailable
- purchase in progress
- pending purchase
- purchase acknowledged and active
- restore success
- no purchases to restore
- billing service unavailable

### Why

Billing bugs often come from pretending the world has only:

- success
- failure

Play actually has a richer state machine, and the UI should reflect that without feeling noisy.

## 7. Console Setup Workstream

## 7.1 Play Console Product Creation

This sprint should explicitly cover beginner-friendly Play Console work:

- create `pro_monthly`
- create `pro_yearly`
- create `pro_lifetime`
- configure localized INR pricing
- set subscription details, renewal behavior, and standard disclosures

### Why This Belongs In The Sprint Plan

Billing is half code, half console.

If the sprint plan ignores Play Console setup, implementation will look “done” in git while still being impossible to verify end to end.

## 7.2 License Tester And Internal Track Setup

We should include:

- Play license tester configuration
- internal testing track upload flow
- test-account instructions
- purchase/cancel/restore QA procedure

This should end up documented in Markdown, not left as tribal knowledge.

## 8. UI And Product Refinement Workstream

## 8.1 Paywall Copy Refinement

The paywall should clearly communicate:

- what Pro includes
- what is monthly vs yearly vs lifetime
- that subscriptions renew automatically
- where subscription management/cancellation happens
- what restore purchases means

### Copy Quality Rules

- calm, direct, non-manipulative
- no fake urgency
- real localized price display
- no stale hardcoded dollar pricing

## 8.2 Final Price Display QA

We should verify:

- INR price strings render correctly
- product ordering is stable
- yearly plan presentation makes sense next to monthly
- lifetime plan does not visually overwhelm everything else
- unavailable products do not leave broken holes in the layout

## 8.3 Manage Subscription UX

For active subscribers, expose a clear path to:

- `Manage Subscription`

using the Play subscription center.

This matters because users should not have to guess how to cancel or modify their subscription.

## 9. Implementation Checklist

## 9.1 Repo And Dependency Layer

- [ ] Add real Play Billing library dependency.
- [ ] Inspect current `:core:billing` fake implementation and preserve test-only path.
- [ ] Add `RealBillingRepository`.
- [ ] Add a small Billing client wrapper for connection/query/purchase callback flow.
- [ ] Add product-details mapping from Play objects to `ProProduct`.
- [ ] Add purchase mapping from Play objects to `ProPurchase`.
- [ ] Add entitlement resolver that merges:
  - [ ] active subscription
  - [ ] lifetime purchase
  - [ ] 24-hour cache fallback
- [ ] Keep fake billing available for tests and non-Play test environments where needed.

## 9.2 Runtime Wiring

- [ ] Switch normal runtime DI from fake billing to real billing.
- [ ] Keep fake billing explicitly for unit-test DI and previews if needed.
- [ ] Decide whether purchase launch coordination stays inside repository or is hoisted to an app-layer coordinator.
- [ ] Ensure billing queries happen on app launch / relevant screen entry / reconnect points as needed.

## 9.3 Product Loading And Paywall

- [ ] Query real product details for:
  - [ ] `pro_monthly`
  - [ ] `pro_yearly`
  - [ ] `pro_lifetime`
- [ ] Update paywall to display real localized prices.
- [ ] Add loading state for product fetch.
- [ ] Add retry/error state for unavailable products.
- [ ] Ensure fake placeholder prices are not shown in production runtime when real data is missing.
- [ ] Refine paywall copy so it matches real billing behavior and Play policy expectations.

## 9.4 Purchase Flow

- [ ] Launch purchase flow for tapped product.
- [ ] Handle purchase updates.
- [ ] Handle user cancellation cleanly.
- [ ] Handle pending purchase state if encountered.
- [ ] Acknowledge purchases where required.
- [ ] Update entitlement immediately after successful purchase processing.
- [ ] Verify Pro-gated UI unlocks without restart.

## 9.5 Restore And Existing Purchase Sync

- [ ] Implement `Restore Purchases` via Play purchase query.
- [ ] Re-query existing purchases on app start and/or resume as appropriate.
- [ ] Recompute entitlement after restore.
- [ ] Surface no-purchases-to-restore state clearly.
- [ ] Keep restore behavior idempotent and safe after reinstall.

## 9.6 Cancellation And Recovery

- [ ] Document test path for cancellation and resubscription.
- [ ] Verify canceled subscription remains active until actual expiry.
- [ ] Verify expired subscription downgrades after Play says entitlement ended and cache fallback is exhausted.
- [ ] Verify lifetime purchase remains Pro regardless of subscription cancellation scenarios.
- [ ] Ensure local app data is never deleted during downgrade.

## 9.7 Settings / Account Integration

- [ ] Replace any stubbed `Manage Subscription` behavior with real Play subscription management entry.
- [ ] Ensure Settings and Account reflect real entitlement state.
- [ ] Ensure backup/restore Pro gates respond to billing changes.
- [ ] Ensure Insights/other Pro-teased surfaces respond to real entitlement changes.

## 9.8 Console And Testing Setup Docs

- [ ] Update Play Console setup docs for billing product creation.
- [ ] Add license tester instructions.
- [ ] Add internal-track billing QA instructions.
- [ ] Add cancellation/recovery QA instructions.
- [ ] Record any safe-to-share console values/IDs needed later in implementation docs.

## 10. Error-Handling Matrix

| Scenario | Expected behavior |
|---|---|
| Products still loading | Show loading state |
| Billing unavailable | Show retry/error state, no fake prices |
| User cancels purchase sheet | Return cleanly, no entitlement change |
| Pending purchase | Show waiting/pending messaging |
| Purchase succeeds but ack not done yet | Process and acknowledge before final entitlement success |
| Restore finds no purchases | Show friendly no-purchases state |
| Temporary billing query failure | Use cached entitlement up to 24 hours |
| Cache expired + billing still unavailable | Downgrade gracefully |
| Lifetime + subscription both present | Lifetime wins semantically for long-term entitlement confidence |

## 11. Tests And Verification

## 11.1 Automated Verification

- [ ] `./gradlew --no-configuration-cache :core:billing:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :app:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :feature:pro:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :feature:settings:testDebugUnitTest`
- [ ] `./gradlew --no-configuration-cache :app:assembleDebug`

## 11.2 Unit Tests

- [ ] product mapping tests
- [ ] entitlement resolver tests
- [ ] cached entitlement age-window tests
- [ ] lifetime vs subscription precedence tests
- [ ] purchase-state handling tests
- [ ] restore-purchases tests

## 11.3 ViewModel / UI Tests

- [ ] `ProViewModel` tests for:
  - [ ] products loaded
  - [ ] unavailable products
  - [ ] purchase trigger
  - [ ] restore purchases
- [ ] paywall UI tests for:
  - [ ] loading state
  - [ ] real product state
  - [ ] failure/retry state
  - [ ] localized price rendering

## 11.4 Manual QA

- [ ] Internal test build installs from Play-distributed path.
- [ ] Monthly purchase succeeds.
- [ ] Yearly purchase succeeds.
- [ ] Lifetime purchase succeeds.
- [ ] Restore purchases works after reinstall.
- [ ] Subscription cancellation flow tested.
- [ ] Subscription recovery / resubscribe flow tested.
- [ ] Final price display QA completed on real device.
- [ ] Manage subscription deep link/path validated.

## 12. Acceptance Criteria

- [ ] Real Play products load from Google Play on a test device.
- [ ] Monthly purchase grants Pro.
- [ ] Yearly purchase grants Pro.
- [ ] Lifetime purchase grants Pro.
- [ ] Restore purchases re-enables Pro after reinstall.
- [ ] Entitlement cache works for temporary billing outages up to 24 hours.
- [ ] Expired/canceled subscriptions downgrade gracefully.
- [ ] Paywall shows real prices and honest fallback states.
- [ ] Manage subscription path is real, not stubbed.
- [ ] Final copy/pricing QA is complete.

## 13. Risks And Tradeoffs

### Risk 1: Billing behaves differently in plain local installs vs Play-distributed installs

Mitigation:

- plan for internal track and license tester validation explicitly

### Risk 2: Product-loading or purchase-launch architecture leaks Android activity concerns into core modules

Mitigation:

- keep launch coordination in `:app` if needed, while preserving `BillingRepository` as the business-facing abstraction

### Risk 3: Cached entitlement logic creates accidental indefinite access

Mitigation:

- centralize the resolver
- test cache age explicitly
- keep 24-hour window hard and documented

### Risk 4: Console setup drags implementation late because products do not exist yet

Mitigation:

- treat Play Console creation and license tester setup as sprint deliverables, not side chores

## 14. Recommended Implementation Order

1. Real billing repository and Play client wrapper
2. Product-details query and paywall loading/error states
3. Purchase flow and acknowledgment
4. Restore purchases and entitlement resolver
5. Settings/account/manage-subscription integration
6. Console product setup and license tester docs
7. Internal-track/device QA for purchase, restore, cancel, and recovery
8. Final copy and price-display refinement

Why this order:

- the paywall is meaningless until real products load
- purchase flow depends on product details and launch coordination
- entitlement logic must be stable before cancellation/recovery QA
- final copy and price QA should happen after the real Play responses are visible

## 15. Review Request

Please review this sprint plan for:

- product ID assumptions
- billing/test-track scope
- entitlement rules
- cancellation/recovery expectations
- whether the sprint boundary feels right before implementation

Common next steps:

1. approve and start implementation
2. request changes to the sprint plan
