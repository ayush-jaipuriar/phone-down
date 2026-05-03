# Phase 11 — Auth, Billing, Entitlements, And Paywall Plan

## Scope

Implement the monetization and identity layer for Phone Down. This phase makes Pro features real: users can purchase monthly, yearly, or lifetime Pro, see their entitlement status, and sign in with Google for backup and identity. Free users continue to have unlimited core focus sessions; Pro unlocks advanced insights, backup, and unlimited history.

## Architecture

### Module Placement

| Concern | Module | Rationale |
|---|---|---|
| Billing client wrapper | `:core:billing` | Isolated from UI, testable with fake implementation |
| Auth wrapper | `:core:auth` | Isolated from UI, testable with fake implementation |
| Entitlement resolution | `:core:billing` | Lives with billing logic; consumes auth state only for backup |
| Paywall UI | `:feature:pro` | Feature module owns the paywall surface |
| Account UI | `:feature:account` | Feature module owns account management surface |
| Pro gates in other features | `:feature:insights`, `:feature:settings` | Feature modules check entitlement via repository |

### Dependency Direction

```
:feature:pro → :core:billing
:feature:account → :core:auth, :core:billing (for Pro status display)
:feature:insights → :core:billing (entitlement checks)
:feature:settings → :core:billing (Pro status display)
:core:billing → :core:model (entitlement types)
:core:auth → :core:model (account types)
```

### Key Interfaces

#### `BillingRepository` (`:core:model`)
```kotlin
interface BillingRepository {
    val products: Flow<List<ProProduct>>
    val purchases: Flow<List<ProPurchase>>
    val entitlement: Flow<ProEntitlement>
    
    suspend fun loadProducts()
    suspend fun launchPurchaseFlow(activity: Activity, product: ProProduct)
    suspend fun restorePurchases()
    suspend fun acknowledgePurchase(purchaseToken: String)
}
```

#### `AuthRepository` (`:core:model`)
```kotlin
interface AuthRepository {
    val accountState: Flow<AccountState>
    
    suspend fun signIn(activity: Activity)
    suspend fun signOut()
    fun getAuthToken(): String?
}
```

#### `ProEntitlement` (`:core:model`)
```kotlin
sealed class ProEntitlement {
    data object Free : ProEntitlement()
    data class Pro(val expiryDate: Instant? = null) : ProEntitlement()
}
```

## Implementation Steps

### Step 1 — Core Model Types (1 hour)
- [ ] Add `ProProduct` (id, type: monthly/yearly/lifetime, price, formattedPrice, billingPeriod)
- [ ] Add `ProPurchase` (productId, purchaseToken, state: pending/completed/acknowledged, purchaseTime)
- [ ] Add `ProEntitlement` sealed class
- [ ] Add `AccountState` sealed class (SignedOut, SignedIn(displayName, email, photoUrl))
- [ ] Add `BillingRepository` interface to `:core:model`
- [ ] Add `AuthRepository` interface to `:core:model`

### Step 2 — Fake Implementations (2 hours)
- [ ] Create `FakeBillingRepository` in `:core:billing` test fixtures
  - Returns hardcoded products (monthly $4.99, yearly $29.99, lifetime $79.99)
  - Simulates purchase flow with 2-second delay
  - Updates entitlement after simulated purchase
- [ ] Create `FakeAuthRepository` in `:core:auth` test fixtures
  - Simulates sign-in with mock Google account
  - Returns test auth token
- [ ] Add `BillingRepository` and `AuthRepository` providers to `AppRuntimeModule`
  - Bind to fake implementations for now (flag: `BuildConfig.DEBUG || BuildConfig.ENABLE_BILLING_STUB`)

### Step 3 — Auth Feature UI (2 hours)
- [ ] Rewrite `AccountScreen` in `:feature:account`
  - Signed-out state: "Sign in with Google" button, explanation text
  - Signed-in state: avatar, display name, email, "Sign out" button
  - Pro status card (if Pro, show expiry/manage subscription)
- [ ] Create `AccountViewModel` in `:app`
  - Collect `AuthRepository.accountState`
  - Expose `signIn()`, `signOut()`
- [ ] Create `AccountRoute` in `:app`
- [ ] Wire `AccountRoute` in `PhoneDownNavHost`
- [ ] Add `AccountScreenTest` (androidTest): sign-in button visible, sign-out flow
- [ ] Add Paparazzi screenshots for signed-in and signed-out states

### Step 4 — Paywall Feature UI (3 hours)
- [ ] Rewrite `ProScreen` in `:feature:pro`
  - Pro value proposition header
  - Product cards: Monthly, Yearly ("Best Value"), Lifetime
  - Price display, billing period, savings calculation
  - "Restore Purchases" button
  - "Maybe Later" dismiss button
  - Clean, non-aggressive copy
- [ ] Create `ProViewModel` in `:app`
  - Collect `BillingRepository.products` and `entitlement`
  - Expose `purchase(product)`, `restorePurchases()`
- [ ] Create `ProRoute` in `:app`
- [ ] Add `ProScreenTest` (androidTest): product cards visible, purchase button tap
- [ ] Add Paparazzi screenshots for paywall

### Step 5 — Entitlement Caching (2 hours)
- [ ] Add `ProEntitlementPreference` to DataStore
  - Store entitlement type + expiry timestamp
  - Cache invalidation: 24 hours or on purchase flow completion
- [ ] Add `DataStoreEntitlementCache` in `:core:datastore`
  - Read/write cached entitlement
  - Expose `isCacheValid(): Boolean`
- [ ] Update `BillingRepository` fake to read/write cache
- [ ] Add unit tests for cache read/write/invalidation

### Step 6 — Pro Gates In Insights (2 hours)
- [ ] Add `isProUser: Boolean` to `InsightsUiState`
- [ ] Update `InsightsViewModel` to collect `BillingRepository.entitlement`
- [ ] Gate advanced insights sections:
  - Heatmap: show 7-day preview, blur beyond + "Upgrade to Pro" teaser
  - Best Focus Time: fully gated with teaser
  - Advanced Trends: fully gated with teaser
  - Export Data: fully gated with teaser
- [ ] Teaser design: subtle card with lock icon, brief benefit description, "Upgrade" CTA
- [ ] Update `InsightsContentTest` to verify gated states
- [ ] Regenerate Paparazzi baselines

### Step 7 — Pro Gates In Settings (1 hour)
- [ ] Add `isProUser: Boolean` to `SettingsUiState`
- [ ] Update `SettingsViewModel` to collect `BillingRepository.entitlement`
- [ ] Gate Backup & Restore: show row, but manual backup button shows paywall if not Pro
- [ ] Gate Auto Backup: disabled switch for free users, enabled for Pro
- [ ] Gate Export Data: show paywall on tap for free users
- [ ] Gate Custom Duration: show paywall on tap for free users
- [ ] Update `SettingsViewModelTest` to verify entitlement state

### Step 8 — Passive Upsell Moments (2 hours)
- [ ] Add upsell banner to Insights screen
  - Show after 3+ sessions if not Pro
  - Dismissible, reappears weekly
  - Copy: "See your focus patterns over time with Pro"
- [ ] Add upsell teaser after session completion
  - Show "Your focus quality was 85/100" → "See detailed trends with Pro"
  - Non-blocking, dismissible
- [ ] Add `UpsellBanner` component to `:core:designsystem`
- [ ] Add `SessionCompletionUpsell` component to `:feature:focus`

### Step 9 — Integration & Navigation (1 hour)
- [ ] Update `PhoneDownNavHost` to pass `BillingRepository` and `AuthRepository` where needed
- [ ] Ensure paywall can be triggered from any feature via navigation
- [ ] Add deep link or shared action for "showPaywall"
- [ ] Verify back navigation from paywall returns to previous screen

### Step 10 — Verification (2 hours)
- [ ] Unit tests:
  - `FakeBillingRepositoryTest`: product loading, purchase flow, entitlement update
  - `FakeAuthRepositoryTest`: sign-in, sign-out, token access
  - `ProEntitlementCacheTest`: cache read/write/invalidation
  - `InsightsViewModelTest`: entitlement reflects in UI state
  - `SettingsViewModelTest`: entitlement reflects in UI state
- [ ] Compose UI tests:
  - `AccountScreenTest`: sign-in/out flow
  - `ProScreenTest`: product display, purchase tap
  - `InsightsContentTest`: gated sections show teasers
- [ ] Paparazzi tests:
  - Account signed-in/out
  - Pro paywall
  - Insights with Pro gates
- [ ] Build check: `:app:assembleDebug`, all unit tests, all screenshot tests

## Tradeoffs

### Fake vs Real BillingClient
- **Decision**: Fake for now, real SKUs later.
- **Why**: Real BillingClient requires Play Console setup, test accounts, and signed APKs. Fake lets us build the full UX and entitlement flow now, then swap in real client later with minimal changes.
- **Risk**: We must ensure the fake API surface matches BillingClient closely to reduce swap friction.

### Entitlement Caching Strategy
- **Decision**: Local DataStore cache with 24-hour TTL, revalidated on app launch and after purchase.
- **Why**: Offline access is critical for a focus app. Users shouldn't lose Pro features mid-session due to network issues. Periodic revalidation catches subscription expiry.
- **Risk**: Cache could be stale if subscription expires between checks. Mitigation: revalidate on app launch (users restart app daily).

### Upsell Aggressiveness
- **Decision**: Passive, dismissible banners only. No modal paywalls on app open.
- **Why**: The product principle is "keep the core timer usable without login, billing, or network access." Aggressive upsells violate this.
- **Risk**: Lower conversion. Mitigation: show value in the teaser (e.g., actual focus quality score).

## Acceptance Criteria

- [ ] Free user sees paywall when tapping Pro-gated feature
- [ ] Free user sees dismissible upsell banner in Insights after 3 sessions
- [ ] Free user sees dismissible upsell teaser after session completion
- [ ] Pro user sees all advanced insights without gates
- [ ] Pro user sees backup/export features enabled
- [ ] Google Sign-In shows account info in Account screen
- [ ] Sign-out clears account state and returns to signed-out UI
- [ ] Entitlement survives app restart (cached)
- [ ] Entitlement revalidates on app launch
- [ ] Purchase flow updates entitlement immediately
- [ ] Restore purchases works
- [ ] Paywall is calm, clear, and non-aggressive
- [ ] All unit tests pass
- [ ] All Compose UI tests pass
- [ ] All Paparazzi screenshot tests pass
- [ ] `:app:assembleDebug` succeeds

## Residual Risks

- Real BillingClient swap will require Play Console configuration and testing
- Subscription expiry edge cases (grace period, account hold) not handled in fake
- Google Sign-In configuration requires OAuth client IDs (not committed)
- Pro product pricing is placeholder ($4.99/$29.99/$79.99)

## Checklist

- [ ] Step 1: Core model types
- [ ] Step 2: Fake implementations
- [ ] Step 3: Auth feature UI
- [ ] Step 4: Paywall feature UI
- [ ] Step 5: Entitlement caching
- [ ] Step 6: Pro gates in Insights
- [ ] Step 7: Pro gates in Settings
- [ ] Step 8: Passive upsell moments
- [ ] Step 9: Integration & navigation
- [ ] Step 10: Verification
- [ ] Update `v1-implementation-plan.md`
- [ ] Update `docs/agent-handoff.md`
