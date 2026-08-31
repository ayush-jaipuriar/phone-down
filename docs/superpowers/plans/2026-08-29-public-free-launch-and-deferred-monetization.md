# Public Free Launch And Deferred Monetization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish Phone Down publicly on Google Play as a completely free app with all current Pro features available, no user-visible monetization, and a reversible path to resume Play Billing after address verification in Q1 2027.

**Architecture:** Public builds use a free-access `BillingRepository` implementation that grants the existing Pro entitlement without connecting to Google Play Billing. Existing real billing code remains isolated in `:core:billing` but is removed from the public app runtime and artifact. UI keeps the Phone Down Pro identity and feature surfaces while eliminating prices, purchases, subscriptions, restore-purchase controls, upgrade prompts, and paid/free tier language.

**Tech Stack:** Native Android, Kotlin, Jetpack Compose, Hilt, Coroutines/Flow, Gradle Kotlin DSL, Google Play Console, Firebase Crashlytics, Google Sign-In/Drive backup.

**Spec:** `docs/superpowers/plans/2026-08-29-public-free-launch-and-deferred-monetization.md` sections 1-8 capture the user-approved product and release specification.

## Global Constraints

- Public app is free to download and all current app features are available without payment.
- Phone Down Pro branding, screen, and feature set remain; monetization references are hidden.
- Public artifact must not initialize `BillingClient`, query products, launch purchases, restore purchases, or expose subscription management.
- Existing real Play Billing implementation remains in the repository for later reactivation.
- BillDesk/merchant Video KYC retries remain paused until the user moves and has a registered rent agreement for the actual residence.
- Mandatory Play developer identity, contact, device, and app-verification requirements are not deferred.
- No personal address, identity number, payment information, KYC document, application ID, token, or credential may be written into the repository.
- Implementation does not begin until this plan is explicitly approved.

---

## 1. Status And Approval Gate

- Planning status: Approved by user; execution began on 2026-08-29.
- Implementation status: Tasks 1-5 and 8 implemented; Task 6 automated gates pass, while physical-device QA remains open; Task 7 is externally gated.
- Approval required: Complete for implementation; consequential Play submission still requires action-time review.
- Recommended execution mode: Inline execution with review checkpoints because code, Play Console, device QA, and release evidence share state.
- Current branch at planning time: `main`, clean and aligned with `origin/main`.

Approval options:

- Approve plan and begin Task 1.
- Request plan changes before implementation.

## 2. Confirmed Decisions

| Decision | Confirmed direction |
|---|---|
| Move timing | Q1 2027 |
| New location | Undecided |
| Expected proof after move | Registered rent agreement |
| App distribution now | Public production, completely free |
| Pro presentation | Keep Pro UI and features |
| Monetization presentation | Hide all monetization references |
| Billing testing during free period | Not required |
| Plan scope | Code, Play Console, testing, release, KYC restart, and later monetization restart |
| Public launch target | Use engineering recommendation in section 7 |
| Durable plan document | Required |

## 3. Definitions And Copy Contract

### 3.1 What May Remain Visible

- `Phone Down Pro` as a product/feature-set name.
- Advanced insights, heatmaps, history, backup, restore, and customization features.
- A Pro overview screen describing available capabilities.
- Pro badges when they identify a feature family rather than a paid tier.

### 3.2 What Must Not Be Visible In Public Free Mode

- `Upgrade`, `buy`, `purchase`, `subscribe`, `subscription`, `billing`, `price`, `plan`, `monthly`, `yearly`, `lifetime purchase`, `restore purchases`, or `manage subscription`.
- Currency values or Google Play product cards.
- `Free tier`, `Free plan`, locked feature messages, usage caps, paywalls, or calls to unlock features.
- Errors saying Play Billing products have not been configured.
- Privacy-policy or store-listing statements claiming that users can purchase Pro.

### 3.3 Functional Meaning Of Free Mode

Free mode is not a cosmetic hide. Runtime behavior must satisfy all of these:

- Entitlement-dependent code receives `ProEntitlement.Pro(expiryDateMillis = null)`.
- Advanced insights always load.
- History and custom-duration limits do not apply.
- Google Drive backup/restore depends on sign-in and user opt-in, not payment.
- Automatic backup worker does not skip work due to commercial entitlement.
- Pro screen never loads products or observes billing events.
- No path can launch a Play purchase dialog.

## 4. Current Repository Baseline

### 4.1 Billing Runtime

- `app/src/main/java/phonedown/app/runtime/AppRuntimeModule.kt` binds `BillingRepository` to `RealBillingRepository`.
- `app/build.gradle.kts` packages `:core:billing`.
- `:core:billing` packages Google Play Billing KTX and owns `RealBillingRepository`.
- `RealBillingRepository` creates a `BillingClient` and supports product queries, purchase launch, purchase restore, acknowledgement, and entitlement caching.
- `ForegroundActivityProvider` implements `BillingActivityProvider` for purchase-flow activity access.

### 4.2 Monetization UI And Gates

- `ProViewModel` loads products during initialization and observes purchases/events.
- `ProScreen` is currently a paywall with product cards, prices, restore purchases, and subscription management.
- Settings contains `Free tier limited`, Pro-gated backup, and Pro navigation.
- Account contains `Free plan` and Google Play purchase language.
- Insights contains session-count limits, locked advanced sections, and upgrade prompts.
- `AutoBackupWorker` requires Pro entitlement.
- Privacy copy states that Google Play Billing is used for purchases.

### 4.3 Release Baseline

- Application ID: `phonedown.app`.
- Current version: `1.0.3` (`versionCode 4`).
- Release signing and Crashlytics are configured.
- Store listing has been saved.
- Closed testing and production-access work remain incomplete.
- Existing Play Billing products were previously documented as not created; live Console state must be rechecked before implementation.

## 5. Architecture Decision

### 5.1 Chosen Design: Runtime Replacement Plus Artifact Exclusion

Create `FreeAccessBillingRepository` in the app runtime. It preserves the existing `BillingRepository` contract and emits permanent Pro access, but owns no Google Play Billing types.

```kotlin
@Singleton
class FreeAccessBillingRepository @Inject constructor() : BillingRepository {
    override val products: Flow<List<ProProduct>> = flowOf(emptyList())
    override val purchases: Flow<List<ProPurchase>> = flowOf(emptyList())
    override val entitlement: Flow<ProEntitlement> =
        flowOf(ProEntitlement.Pro(expiryDateMillis = null))
    override val events: Flow<BillingEvent> = emptyFlow()

    override suspend fun loadProducts() = Unit
    override suspend fun restorePurchases() = Unit
    override suspend fun syncPurchases() = Unit
    override suspend fun acknowledgePurchase(purchaseToken: String) = Unit

    override suspend fun launchPurchaseFlow(product: ProProduct): Nothing =
        error("Unsupported operation in public free mode")
}
```

Production DI binds `BillingRepository` to this implementation. `app` removes its `:core:billing` dependency, so `RealBillingRepository`, `BillingActivityProvider`, and Google Play Billing KTX remain in source control but are absent from the public application dependency graph.

### 5.2 Why This Design

- Minimal change to existing entitlement consumers.
- Free access is enforced at source of truth, not scattered UI flags.
- Public artifact cannot accidentally connect to Play Billing.
- Real billing implementation and tests remain available for Q1 2027.
- Reactivation is explicit: dependency, DI binding, monetization UI, products, and QA all return together.

### 5.3 Rejected Alternatives

**Hide only paywall controls:** Rejected because background entitlement checks would still lock features and BillingClient could still run.

**Delete billing code:** Rejected because it discards tested work and makes Q1 2027 restart unnecessarily risky.

**Keep `RealBillingRepository` bound but never navigate to paywall:** Rejected because product queries, cached entitlements, and accidental purchase paths remain possible.

**Add a large product-flavor matrix now:** Rejected as premature. One public-free runtime plus dormant billing module meets current need with less Gradle complexity. A paid flavor can be reconsidered during monetization reactivation.

### 5.4 Invariant

The public app dependency graph must not contain `com.android.billingclient`. Verification:

```bash
./gradlew :app:dependencies --configuration releaseRuntimeClasspath
```

Expected: no `com.android.billingclient` entry and no `project :core:billing` entry.

## 6. File Map

### Files To Create

- `app/src/main/java/phonedown/app/runtime/FreeAccessBillingRepository.kt` - permanent free entitlement, no billing transport.
- `app/src/test/java/phonedown/app/runtime/FreeAccessBillingRepositoryTest.kt` - contract tests for free access and disabled purchases.
- `feature/pro/src/androidTest/kotlin/phonedown/feature/pro/ProScreenTest.kt` - Compose assertions for Pro overview and absent payment controls.
- `feature/pro/src/test/kotlin/phonedown/feature/pro/ProScreenScreenshotTest.kt` - Paparazzi light/dark visual baselines.
- `app/src/main/java/phonedown/app/backup/AutoBackupEligibility.kt` - pure sign-in/opt-in eligibility rule.
- `app/src/test/java/phonedown/app/backup/AutoBackupEligibilityTest.kt` - backup eligibility regression tests.
- `docs/public-free-release-qa.md` - device and Play-installed QA evidence template.
- `docs/monetization-restart-runbook.md` - trigger-based Q1 2027 KYC and billing restart procedure.

### Files To Modify

- `app/build.gradle.kts` - remove `implementation(project(":core:billing"))`; increment release version only in release task.
- `feature/pro/build.gradle.kts` - remove transitive `:core:billing` dependency and enable Paparazzi verification.
- `app/src/main/java/phonedown/app/runtime/AppRuntimeModule.kt` - bind `FreeAccessBillingRepository`.
- `app/src/main/java/phonedown/app/runtime/ForegroundActivityProvider.kt` - delete after billing binding removal because no other runtime consumes it.
- `app/src/main/java/phonedown/app/pro/ProRoute.kt` - remove purchase, restore, retry, and subscription callbacks.
- `app/src/main/java/phonedown/app/pro/ProViewModel.kt` - delete billing/paywall orchestration from the public app layer.
- `app/src/test/java/phonedown/app/pro/ProViewModelTest.kt` - delete obsolete paywall tests; free overview is covered in `:feature:pro`.
- `feature/pro/src/main/kotlin/phonedown/feature/pro/ProScreen.kt` - replace paywall with feature overview.
- `app/src/main/java/phonedown/app/insights/InsightsViewModel.kt` - preserve permanent feature access through repository entitlement.
- `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt` - remove caps, locked states, and upgrade prompts.
- `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsScreen.kt` - remove tier-limit and payment-oriented copy; keep Pro overview navigation.
- `feature/account/src/main/kotlin/phonedown/feature/account/AccountScreen.kt` - remove plan/purchase language and expose backup to signed-in users.
- `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt` - remove free-tier custom-duration rejection and paid-tier error copy.
- `core/model/src/main/kotlin/phonedown/core/model/UserSettings.kt` - remove obsolete free-tier duration-limit state if no remaining consumer needs it.
- `app/src/main/java/phonedown/app/backup/AutoBackupScheduler.kt` - schedule from sign-in/opt-in state without commercial entitlement.
- `app/src/main/java/phonedown/app/backup/AutoBackupWorker.kt` - remove payment entitlement as a worker precondition.
- `feature/settings/src/main/kotlin/phonedown/feature/settings/PrivacyPolicyScreen.kt` - remove current-use claim for Google Play Billing.
- Relevant unit and screenshot tests under `app/src/test`, `app/src/androidTest`, and feature test directories.
- `docs/play-store-data-safety.md` - ensure answers describe free runtime behavior.
- `docs/privacy-policy.md` - align hosted privacy copy with billing-free runtime.
- `docs/release-readiness.md` - replace stale version/fake-runtime claims with current verified release evidence.
- `fastlane/metadata/android/en-US/changelogs/default.txt` - remove upgrade language from public release notes.
- `scripts/check.sh` - include onboarding and Pro visual/test verification so the standard sanity gate covers every shipped screen.
- `docs/play-console-release-guide.md` - add free-public release path and merchant-KYC deferral boundary.
- `docs/phase-16-console-setup-info.md` - record current free-launch decision and distinguish developer verification from merchant onboarding.
- `phase-16-sprint-16-4-real-play-billing-plan.md` - mark monetization implementation dormant/deferred, not deleted.
- `phase-16-sprint-16-5-internal-testing-readiness-plan.md` - replace billing-product prerequisites with free-mode verification.
- `v1-implementation-plan.md` - add approved implementation progress only after execution begins.

## 7. Recommended Timeline

Assumption: plan approved by 2026-09-01. If approval is later, shift code and QA dates while preserving durations.

| Window | Milestone | Exit condition |
|---|---|---|
| Sep 1-2 | Console blocker audit | Merchant KYC separated from mandatory developer verification |
| Sep 2-6 | Free-runtime implementation | Unit/build/static copy checks pass |
| Sep 7-9 | Local physical-device QA | Core, Pro, insights, auth, backup, and regression checks pass |
| Sep 10 | Upload closed-test candidate | Play-installed build available to testers |
| Sep 10-24 | Closed test | At least 12 testers continuously opted in for 14 days, if account requirement applies |
| Sep 25-28 | Feedback fixes and production-access application | Material defects fixed; application answers evidence-backed |
| Sep 29-Oct 4 | Production candidate and staged rollout preparation | Release checklist green |
| Oct 5 | Recommended production submission | Free production release submitted |
| Oct 5-12 | Review and staged rollout observation | Crash/ANR and feedback remain acceptable |

Recommended public-launch target: **production submission on 2026-10-05**, with public availability treated as review-dependent rather than guaranteed on that date.

Why this target:

- It leaves one implementation week.
- It preserves the current Google requirement of 12 continuously opted-in closed testers for at least 14 days for affected new personal accounts.
- It leaves time to fix tester findings before production submission.
- It avoids tying launch to merchant KYC, while still respecting mandatory developer verification.

## 8. Google Play And KYC Policy Boundaries

Current official references checked on 2026-08-29:

- [Testing requirements for new personal accounts](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB): affected accounts need at least 12 continuously opted-in closed testers for 14 days before applying for production access.
- [Required developer-account information](https://support.google.com/googleplay/android-developer/answer/13628312?hl=en-IN): identity details must be verified before publishing; merchant setup is needed when monetizing.
- [Developer identity verification](https://support.google.com/googleplay/android-developer/answer/10841920?hl=en-IN): linked payments-profile identity may still be part of mandatory account verification.
- [Link a merchant payments profile](https://support.google.com/googleplay/android-developer/answer/3092739?hl=en): needed to sell paid apps or in-app purchases.
- [App pricing](https://support.google.com/googleplay/android-developer/answer/6334373?hl=en): once offered free, the app cannot later become a paid download. This does not prevent adding Play in-app products/subscriptions later.

Operational rule:

- Pause only merchant/KYC work whose purpose is receiving money.
- Complete any Play task marked as developer identity, contact, device, package, or account verification.
- If Play Console says production is blocked by identity verification, stop and inspect exact task wording; free distribution does not override that requirement.
- Phone Down remains free to download permanently under package `phonedown.app`; future revenue uses Play in-app products/subscriptions, not a paid-download conversion.

---

### Task 1: Audit Live Console Blockers And Freeze Merchant Work

**Files:**
- Modify: `docs/phase-16-console-setup-info.md`
- Modify: `docs/play-console-release-guide.md`
- Create: `docs/public-free-release-qa.md`

**Interfaces:**
- Consumes: current Play Console dashboard, Developer account verification page, App content status, Testing status, and Monetize status.
- Produces: a sanitized blocker matrix with no personal or financial data.

- [ ] **Step 1: Capture current Console task categories**

Record only status and category:

```markdown
| Area | Status | Required for free launch | Evidence date | Action |
| Developer identity | Not checked | Yes | YYYY-MM-DD | Complete or escalate |
| Contact verification | Not checked | Yes | YYYY-MM-DD | Complete if prompted |
| Device verification | Not checked | Yes | YYYY-MM-DD | Complete if prompted |
| Merchant onboarding | Paused | No | YYYY-MM-DD | Resume after move |
| Closed testing | Not checked | Yes if account is affected | YYYY-MM-DD | Recruit and run test |
| Production access | Not checked | Yes | YYYY-MM-DD | Apply after eligibility |
```

Do not record names, addresses, IDs, bank details, screenshots containing personal data, or KYC application numbers.

- [ ] **Step 2: Verify mandatory account health**

Open Play Console account-level tasks and confirm whether developer identity, email, phone, and physical Android device verification are complete. Treat any incomplete item as a launch blocker.

- [ ] **Step 3: Verify monetization state**

Confirm no in-app product or subscription is active. If a product exists, deactivate it; do not delete history or alter merchant identity data.

- [ ] **Step 4: Record BillDesk pause boundary**

Document:

```markdown
Merchant onboarding is intentionally paused. Do not retry Video KYC until actual residence, declared current address, supporting document, and device location are consistent.
```

- [ ] **Step 5: Verify documentation**

Run:

```bash
rg -n "Application ID|PAN|Aadhaar|bank account|current address" docs
```

Expected: no private KYC values or personal address data.

- [ ] **Step 6: Commit Task 1**

```bash
git add docs/phase-16-console-setup-info.md docs/play-console-release-guide.md docs/public-free-release-qa.md
git diff --cached
git commit -m "docs: define free launch console boundary"
```

**Acceptance criteria:**

- Merchant KYC is marked paused.
- Mandatory developer verification is explicitly tracked and not incorrectly deferred.
- Product/subscription state is known.
- No sensitive data enters git.

---

### Task 2: Add Permanent Free Entitlement Without Play Billing Runtime

**Files:**
- Create: `app/src/main/java/phonedown/app/runtime/FreeAccessBillingRepository.kt`
- Create: `app/src/test/java/phonedown/app/runtime/FreeAccessBillingRepositoryTest.kt`
- Modify: `app/src/main/java/phonedown/app/runtime/AppRuntimeModule.kt`
- Delete: `app/src/main/java/phonedown/app/runtime/ForegroundActivityProvider.kt`
- Modify: `app/build.gradle.kts`
- Modify: `feature/pro/build.gradle.kts`

**Interfaces:**
- Consumes: `BillingRepository` and `ProEntitlement` from `:core:model`.
- Produces: `FreeAccessBillingRepository` bound as the sole public runtime `BillingRepository`.

- [ ] **Step 1: Write failing repository contract tests**

```kotlin
@Test
fun `free release grants permanent pro entitlement`() = runTest {
    val repository = FreeAccessBillingRepository()
    assertEquals(ProEntitlement.Pro(expiryDateMillis = null), repository.entitlement.first())
}

@Test
fun `free release exposes no products or purchases`() = runTest {
    val repository = FreeAccessBillingRepository()
    assertTrue(repository.products.first().isEmpty())
    assertTrue(repository.purchases.first().isEmpty())
}

@Test
fun `purchase launch is impossible in free release`() = runTest {
    val sampleProduct = ProProduct(
        id = PRO_LIFETIME_PRODUCT_ID,
        type = ProProductType.Lifetime,
        priceAmountMicros = 0L,
        formattedPrice = "",
        billingPeriod = null,
    )
    assertFailsWith<IllegalStateException> {
        FreeAccessBillingRepository().launchPurchaseFlow(sampleProduct)
    }
}
```

- [ ] **Step 2: Run focused test and verify failure**

```bash
./gradlew :app:testDebugUnitTest --tests phonedown.app.runtime.FreeAccessBillingRepositoryTest
```

Expected: compile failure because `FreeAccessBillingRepository` does not exist.

- [ ] **Step 3: Implement free repository**

Implement the exact contract from section 5.1. Use immutable cold flows and no Android, BillingClient, cache, network, or Activity dependencies.

- [ ] **Step 4: Replace DI binding**

```kotlin
@Provides
@Singleton
fun providesBillingRepository(): BillingRepository = FreeAccessBillingRepository()
```

Remove `RealBillingRepository`, `BillingActivityProvider`, and entitlement-cache parameters from the public binding. Keep unrelated DataStore and runtime providers intact.

- [ ] **Step 5: Remove public app billing dependency**

Delete these two dependency declarations:

```kotlin
implementation(project(":core:billing"))
```

from `app/build.gradle.kts` and `feature/pro/build.gradle.kts`. The Pro screen uses models from `:core:model` and does not need the billing transport module. Do not remove `:core:billing` from `settings.gradle.kts`; dormant code and its tests remain buildable.

- [ ] **Step 6: Remove Activity billing role**

Delete `ForegroundActivityProvider` and its Hilt provider. Repository search confirms its only consumer is `RealBillingRepository` through `BillingActivityProvider`; Google Sign-In and Drive authorization use separate coordinators.

- [ ] **Step 7: Run focused and module tests**

```bash
./gradlew :app:testDebugUnitTest :core:billing:testDebugUnitTest
```

Expected: PASS. Dormant real billing tests still compile and pass independently.

- [ ] **Step 8: Prove artifact exclusion**

```bash
./gradlew :app:dependencies --configuration releaseRuntimeClasspath > build/reports/release-runtime-classpath.txt
rg "billingclient|project :core:billing" build/reports/release-runtime-classpath.txt
```

Expected: `rg` returns no matches.

- [ ] **Step 9: Commit Task 2**

Stage exact files, inspect staged diff, scan sensitive patterns, then commit:

```bash
git commit -m "feat: grant pro access in public free runtime"
```

**Acceptance criteria:**

- Every entitlement consumer receives permanent Pro access.
- Public app runtime has no Play Billing module or library.
- Purchase launch fails closed if called accidentally.
- Dormant real billing module remains intact and testable.

---

### Task 3: Convert Pro Paywall Into Feature Overview

**Files:**
- Modify: `app/src/main/java/phonedown/app/pro/ProRoute.kt`
- Delete: `app/src/main/java/phonedown/app/pro/ProViewModel.kt`
- Delete: `app/src/test/java/phonedown/app/pro/ProViewModelTest.kt`
- Modify: `feature/pro/src/main/kotlin/phonedown/feature/pro/ProScreen.kt`
- Modify: `feature/pro/build.gradle.kts`
- Create: `feature/pro/src/androidTest/kotlin/phonedown/feature/pro/ProScreenTest.kt`
- Create: `feature/pro/src/test/kotlin/phonedown/feature/pro/ProScreenScreenshotTest.kt`

**Interfaces:**
- Consumes: permanent Pro entitlement.
- Produces: `ProScreen` with overview-only callback `onBack`; dormant billing repository code remains untouched.

- [ ] **Step 1: Write failing UI assertions**

Test visible content:

```kotlin
composeRule.onNodeWithText("Phone Down Pro").assertIsDisplayed()
composeRule.onNodeWithText("Advanced insights").assertIsDisplayed()
composeRule.onNodeWithText("Backup and restore").assertIsDisplayed()
```

Test absent monetization content:

```kotlin
listOf("Restore Purchases", "Manage Subscription", "Monthly", "Yearly", "Lifetime")
    .forEach { composeRule.onNodeWithText(it).assertDoesNotExist() }
```

- [ ] **Step 2: Run focused tests and verify failure**

```bash
./gradlew :feature:pro:assembleDebugAndroidTest :feature:pro:verifyPaparazziDebug
```

Expected: failing assertions or missing tests/baselines because current paywall content violates the free-mode contract.

- [ ] **Step 3: Define overview state in feature module**

Replace product/purchase fields with overview state:

```kotlin
data class ProScreenState(
    val features: List<ProFeatureSummary> = defaultProFeatures,
)

data class ProFeatureSummary(
    val title: String,
    val description: String,
)

private val defaultProFeatures =
    listOf(
        ProFeatureSummary("Advanced insights", "Heatmaps, trends, quality patterns, and focus highlights."),
        ProFeatureSummary("Unlimited history", "Review your complete focus-session history."),
        ProFeatureSummary("Flexible focus controls", "Use custom durations and the complete focus toolkit."),
        ProFeatureSummary("Backup and restore", "Protect focus data with your Google account."),
    )
```

The default list must cover advanced insights, unlimited history, flexible focus controls, and Google backup/restore. Do not mention payment, tiers, or future monetization.

- [ ] **Step 4: Remove billing orchestration from active route**

Make `ProRoute` render the overview directly and remove its `hiltViewModel<ProViewModel>()`, product refresh, purchase, restore-purchase, and Play subscription URL wiring. Delete app-layer `ProViewModel` and its obsolete paywall tests so no billing event observer or product loader remains packaged in the public app. The transport implementation remains preserved in dormant `:core:billing`.

- [ ] **Step 5: Replace product cards with capability rows**

Use existing Phone Down components and compact feature rows. Keep `Phone Down Pro` as screen title. Do not add promotional hero copy or claims that conflict with shipped functionality.

- [ ] **Step 6: Run tests and screenshot checks**

```bash
./gradlew \
  :feature:pro:verifyPaparazziDebug \
  :feature:pro:assembleDebugAndroidTest \
  :feature:pro:connectedDebugAndroidTest \
  :app:testDebugUnitTest
```

Expected: PASS; screenshot has no clipped copy or payment controls.

- [ ] **Step 7: Commit Task 3**

```bash
git commit -m "feat: present pro as included feature set"
```

**Acceptance criteria:**

- Pro route remains reachable.
- Pro branding and feature descriptions remain.
- No product, price, billing error, purchase, restore-purchase, or subscription UI exists.

---

### Task 4: Remove Feature Gates And Monetization Copy Across App

**Files:**
- Modify: `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt`
- Modify: `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsTestTags.kt`
- Modify: `feature/insights/src/androidTest/kotlin/phonedown/feature/insights/InsightsScreenTest.kt`
- Modify: `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsScreen.kt`
- Modify: `feature/account/src/main/kotlin/phonedown/feature/account/AccountScreen.kt`
- Modify: `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
- Modify: `feature/focus/src/androidTest/kotlin/phonedown/feature/focus/FocusScreenTest.kt`
- Modify: `core/model/src/main/kotlin/phonedown/core/model/UserSettings.kt`
- Create: `app/src/main/java/phonedown/app/backup/AutoBackupEligibility.kt`
- Create: `app/src/test/java/phonedown/app/backup/AutoBackupEligibilityTest.kt`
- Modify: `app/src/main/java/phonedown/app/backup/AutoBackupScheduler.kt`
- Modify: `app/src/main/java/phonedown/app/backup/AutoBackupWorker.kt`
- Modify: `feature/settings/src/main/kotlin/phonedown/feature/settings/PrivacyPolicyScreen.kt`
- Modify: relevant unit, Compose, and screenshot tests.

**Interfaces:**
- Consumes: permanent Pro entitlement and signed-in state.
- Produces: no commercially gated path; backup gated only by account/opt-in prerequisites.

- [ ] **Step 1: Add failing regression tests for unlocked insights**

Extend `InsightsScreenTest` with the existing `sampleState` fixture:

```kotlin
@Test
fun advancedInsightsRemainAvailableWithoutPurchaseState() {
    composeRule.setContent {
        PhoneDownTheme(themeMode = ThemeMode.Light) {
            InsightsContent(
                uiState =
                    sampleState.copy(
                        isProUser = false,
                        today = sampleState.today.copy(sessionCount = 4),
                    ),
                onRefresh = {},
            )
        }
    }

    composeRule.onNodeWithText("Export Data").performScrollTo().assertIsDisplayed()
    composeRule.onNodeWithText("Upgrade to Pro").assertDoesNotExist()
}
```

Remove tests whose sole contract is a free-tier cap. Add stable test tags only where text assertions would be ambiguous.

- [ ] **Step 2: Add failing backup eligibility tests**

```kotlin
@Test
fun `signed in opted in user is eligible without purchase state`() =
    assertTrue(
        isAutoBackupEligible(
            backupOptIn = true,
            autoBackupEnabled = true,
            isSignedIn = true,
        ),
    )

@Test
fun `signed out user is not eligible`() =
    assertFalse(
        isAutoBackupEligible(
            backupOptIn = true,
            autoBackupEnabled = true,
            isSignedIn = false,
        ),
    )
```

- [ ] **Step 3: Remove Insights locks**

Eliminate session-count cap branches, `ProTeaserCard`, upgrade copy, and locked placeholders. Render advanced insights and heatmaps using existing loaded state.

- [ ] **Step 4: Normalize Settings copy**

Required outcomes:

- `Custom Duration` is a normal available setting, with no `Free tier` or `Pro` price-gate copy.
- `Phone Down Pro` row remains and opens overview.
- `Backup & Restore` routes to sign-in/account or backup based only on account state.
- No `trailing = "Pro"` indicates a lock.

- [ ] **Step 5: Normalize Account copy**

Replace `Free plan` and purchase-source explanations with neutral capability copy. Signed-in users can access backup/restore; signed-out users see sign-in guidance.

- [ ] **Step 5a: Remove custom-duration commercial enforcement**

Add a Compose regression test that enters a duration above the former `freeCustomDurationSeconds` value and confirms it is accepted without upgrade text. Remove the rejection branch and paid-tier error copy from `FocusScreen`; remove `freeCustomDurationSeconds` from `UserSettings` only after repository-wide search confirms no non-test consumer remains.

- [ ] **Step 6: Remove commercial check from auto backup**

Worker precondition becomes:

```kotlin
if (!settings.backupOptIn || !settings.autoBackupEnabled || !isSignedIn) {
    return Result.success()
}
```

Remove `BillingRepository` from `AutoBackupWorkerEntryPoint` if no longer used there.
Implement the condition through `isAutoBackupEligible` so the commercial-free rule is testable without WorkManager instrumentation.

Apply the same helper in `AutoBackupScheduler` so periodic work scheduling and worker execution share one eligibility rule. Add scheduler regression coverage proving a signed-in, opted-in user is scheduled without purchase state.

- [ ] **Step 7: Correct privacy copy**

Remove statements that current app uses Google Play Billing or ties exports/backups to Pro payment. Preserve accurate Google Sign-In, Drive app-data, Crashlytics, retention, deletion, and permission disclosures.

- [ ] **Step 8: Run static monetization scan**

```bash
rg -n -i 'upgrade|purchase|subscribe|subscription|billing|price|free tier|free plan|restore purchases|manage subscription' \
  app/src/main feature/*/src/main
```

Expected: no user-visible monetization copy. Technical identifiers in dormant `:core:billing` are outside this scan.

- [ ] **Step 9: Run affected tests**

```bash
./gradlew \
  :app:testDebugUnitTest \
  :feature:insights:testDebugUnitTest \
  :feature:settings:testDebugUnitTest \
  :feature:account:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 10: Commit Task 4**

```bash
git commit -m "feat: unlock all features for free launch"
```

**Acceptance criteria:**

- All advanced features work for a fresh install.
- No paid/free gate remains.
- Backup requires sign-in/consent, not a purchase.
- Privacy copy matches actual runtime.

---

### Task 5: Align Store Metadata, Data Safety, And Release Documentation

**Files:**
- Modify: Fastlane/store metadata files identified during execution.
- Modify: `docs/play-store-data-safety.md`
- Modify: `docs/privacy-policy.md`
- Modify: `docs/release-readiness.md`
- Modify: `docs/play-console-release-guide.md`
- Modify: `docs/phase-16-console-setup-info.md`
- Modify: `phase-16-sprint-16-4-real-play-billing-plan.md`
- Modify: `phase-16-sprint-16-5-internal-testing-readiness-plan.md`
- Modify: `v1-implementation-plan.md`
- Modify: `fastlane/metadata/android/en-US/changelogs/default.txt`

**Interfaces:**
- Consumes: verified free-runtime behavior.
- Produces: Play listing and policy answers that do not advertise unavailable purchases or locked features.

- [ ] **Step 1: Find commercial listing claims**

```bash
rg -n -i 'pro|premium|upgrade|purchase|subscription|paid|price|free tier' fastlane docs \
  --glob '*.txt' --glob '*.md'
```

Classify each match as Pro branding, historical documentation, current listing claim, or obsolete monetization statement.

- [ ] **Step 2: Update current listing metadata**

Describe shipped capabilities without paid-tier language. Keep app title and core focus proposition stable. Historical plans may retain billing detail when clearly marked dormant/deferred.

- [ ] **Step 3: Revalidate Data safety**

Confirm Google Sign-In, Drive backup, Crashlytics, and account deletion statements. Do not claim purchase-data collection in the free artifact if billing is absent. Apply the same correction to both in-app privacy copy and hosted `docs/privacy-policy.md`.

- [ ] **Step 4: Mark billing plan deferred**

Add a dated status note to Sprint 16.4:

```markdown
Public billing rollout deferred until post-move merchant verification in Q1 2027. Real billing source remains preserved but is excluded from the public free artifact.
```

- [ ] **Step 5: Replace billing prerequisites in testing plan**

Sprint 16.5 must require proof of no billing runtime and full free access instead of requiring active Play products.

- [ ] **Step 5a: Correct release-facing copy and stale readiness evidence**

Remove `Pro upgrade` or equivalent monetization language from the default Fastlane changelog. Refresh `docs/release-readiness.md` from current Gradle version/runtime evidence rather than retaining stale `1.0.2` or fake-service claims.

- [ ] **Step 6: Run docs/privacy scan**

```bash
rg -n -i 'PAN|Aadhaar|application ID|bank account|rent agreement|current address' . \
  --glob '*.md' --glob '!docs/superpowers/plans/2026-08-29-public-free-launch-and-deferred-monetization.md'
```

Expected: no private values; generic process wording is allowed.

- [ ] **Step 7: Commit Task 5**

```bash
git commit -m "docs: align play release with free access"
```

**Acceptance criteria:**

- Listing describes actual free app.
- Data safety matches packaged SDKs and behavior.
- Billing work is marked dormant, not falsely complete or deleted.
- No sensitive KYC data is committed.

---

### Task 6: Run Comprehensive Local Verification

**Files:**
- Modify: `docs/public-free-release-qa.md`
- Modify: `scripts/check.sh`
- Modify: plan checkboxes and `v1-implementation-plan.md` after evidence exists.

**Interfaces:**
- Consumes: Tasks 2-5 implementation.
- Produces: signed release candidate plus automated and physical-device evidence.

- [ ] **Step 1: Run formatting and static checks**

First update `scripts/check.sh` so its Paparazzi and Android-test compilation sections include `:feature:onboarding` and the new `:feature:pro` coverage. Then run the standard gate and explicit static tasks:

```bash
./scripts/check.sh
./gradlew ktlintCheck detekt lintDebug
```

Expected: no newly introduced findings. Existing repository-wide ktlint/detekt baseline failures must be recorded separately and proven unrelated with targeted module checks; do not add suppressions to hide new findings.

- [ ] **Step 2: Run JVM regression suite**

```bash
./gradlew testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 3: Run dormant billing module tests**

```bash
./gradlew :core:billing:testDebugUnitTest
```

Expected: PASS, proving deferred code has not rotted during extraction.

- [ ] **Step 4: Run instrumentation and screenshot tests on physical Android device**

```bash
adb devices
./gradlew connectedDebugAndroidTest
```

Expected: one authorized physical device and PASS. Update screenshot baselines only after visual review.

- [ ] **Step 5: Build signed release bundle locally**

Increment to the next unused `versionCode` and appropriate patch `versionName`, then run:

```bash
./gradlew clean bundleRelease
```

Expected: signed `.aab` generated locally; no cloud build needed.

- [ ] **Step 6: Inspect merged manifest and dependency graph**

```bash
rg -n 'com.android.vending.BILLING|billingclient' app/build/intermediates/merged_manifests app/build/reports
```

Expected: no matches in public release outputs.

- [ ] **Step 7: Install and run manual device matrix**

Verify:

- fresh install and onboarding
- editable default duration
- clean session completion summary and `Done`
- broken-session recovery and terminal summary
- all Insights sections on fresh account
- Pro overview with no purchase UI
- Google Sign-In success and failure guidance
- backup opt-in, manual backup, restore confirmation, and signed-out behavior
- settings, theme, sound, haptics, call permission, export, deletion, and privacy policy
- process death, reboot recovery, offline launch, and network restoration
- no billing UI, billing network call, or product-setup error

- [ ] **Step 8: Record evidence and blockers**

For each check, record build version, device model/Android version, pass/fail, date, and sanitized notes. Never paste account email, tokens, Drive IDs, or personal data.

- [ ] **Step 9: Commit Task 6 evidence**

```bash
git commit -m "test: verify public free release candidate"
```

**Acceptance criteria:**

- Automated suites pass.
- Signed bundle builds locally.
- Physical-device matrix passes or every failure has a blocking issue.
- Artifact contains no Play Billing SDK/permission.

---

### Task 7: Execute Closed Test And Production Release

**Files:**
- Modify: `docs/public-free-release-qa.md`
- Modify: `docs/play-console-release-guide.md`
- Modify: `v1-implementation-plan.md`

**Interfaces:**
- Consumes: signed bundle and completed local QA.
- Produces: production access, reviewed release, staged rollout, and monitoring evidence.

- [ ] **Step 1: Upload release candidate to closed testing**

Use the next unused version code. Release notes must describe user-visible fixes and free access; do not mention internal KYC or private merchant status.

- [ ] **Step 2: Verify Play processing**

Confirm no policy warning requests billing products, payment disclosure, or merchant completion. Resolve only actual required tasks.

- [ ] **Step 3: Maintain tester continuity**

For affected personal accounts, keep at least 12 testers opted in continuously for 14 full days. Recruit more than 12 to absorb dropouts. Record anonymized tester count and dates, not email addresses.

- [ ] **Step 4: Collect structured feedback**

Ask testers to exercise focus completion, duration editing, sign-in, backup, insights, and Pro overview. Record reproducible defects with app version and Android version.

- [ ] **Step 5: Apply for production access**

Answer Console questions from actual evidence: tester recruitment, engagement, feedback, changes made, app purpose, and production readiness. Do not invent usage or feedback.

- [ ] **Step 6: Build final candidate if fixes changed code**

Repeat Task 6 fully for any changed binary. A changed bundle requires a new version code.

- [ ] **Step 7: Submit staged production rollout**

Recommended rollout:

```text
Day 0: 10%
Day 2: 25% if crash/ANR and feedback are healthy
Day 4: 50%
Day 7: 100%
```

For a first release with low traffic, percentages may produce little data; manual smoke tests and tester evidence remain required.

- [ ] **Step 8: Monitor**

Check Play pre-launch report, Android vitals, Crashlytics, reviews, support email, sign-in failures, backup failures, and session-completion regressions daily during rollout.

- [ ] **Step 9: Complete release documentation**

Record public version, rollout dates, verification results, known limitations, and next technical backlog item. Do not record private Console identifiers.

**Acceptance criteria:**

- Production access is granted.
- Free release passes Play review.
- Staged rollout reaches 100% without an unresolved release-blocking defect.
- Store listing and app behavior agree.

---

### Task 8: Prepare Q1 2027 Monetization Restart Runbook

**Files:**
- Create: `docs/monetization-restart-runbook.md`
- Modify: `phase-16-sprint-16-4-real-play-billing-plan.md`

**Interfaces:**
- Consumes: actual post-move address and then-current Play/BillDesk requirements outside git.
- Produces: approved prerequisites for a separate monetization implementation plan.

- [ ] **Step 1: Define restart triggers**

All must be true:

- User has moved to the actual residence.
- Registered rent agreement is effective and names the user/address as required by the provider.
- Declared current address exactly matches acceptable proof.
- Device location during Video KYC reflects actual location.
- PAN/identity and bank details remain accurate.
- Current BillDesk invitation/application is confirmed usable or formally restarted.
- Play merchant-account requirement and accepted-document list are rechecked live.

- [ ] **Step 2: Define KYC preparation checklist**

Prepare outside repository:

- original PAN/identity document requested by provider
- registered rent agreement and any requested supporting document
- stable internet, camera, microphone, location permission, and clear background
- exact business/app description consistent with Play listing
- bank ownership details matching merchant profile

No document copies or extracted values go into git.

- [ ] **Step 3: Define failure handling**

- If address/document/location mismatch appears, stop and correct profile or evidence before retrying.
- If invitation expired, use official support/resume path rather than creating duplicate applications.
- If accepted-document policy changed, follow current provider list.
- Record only sanitized status in project docs.

- [ ] **Step 4: Gate monetization implementation behind a new plan**

After KYC approval, create a new sprint plan covering:

- re-adding `:core:billing` to public runtime
- replacing free entitlement DI with `RealBillingRepository`
- restoring paywall and billing state models
- creating/activating Play products
- backend purchase-verification decision
- purchase, pending, cancel, restore, renewal, expiry, and refund tests
- privacy/Data safety/store-listing updates
- new closed/internal release and staged production rollout

- [ ] **Step 5: Preserve free users' behavior until paid release approval**

Do not silently remove features from existing users. Monetization plan must explicitly choose grandfathering, trial, or transition rules and obtain user approval before changing entitlements.

- [ ] **Step 6: Commit Task 8**

```bash
git commit -m "docs: add monetization restart runbook"
```

**Acceptance criteria:**

- Restart is trigger-based, not merely calendar-based.
- Address, proof, and live location consistency are mandatory.
- Monetization cannot reactivate through one accidental DI change.
- Existing free-user transition receives an explicit product decision.

---

## 9. End-To-End Acceptance Criteria

### Code And Artifact

- [ ] Fresh install receives all Pro capabilities.
- [ ] `:app` release runtime classpath excludes `:core:billing` and `com.android.billingclient`.
- [ ] Merged public manifest contains no billing permission.
- [ ] No purchase flow can be launched.
- [ ] Dormant real billing module remains buildable and tested.

### User Experience

- [ ] Phone Down Pro overview remains accessible.
- [ ] No monetization term from section 3.2 is visible.
- [ ] No feature appears locked or capped by payment.
- [ ] Backup/restore depends only on Google account, permission, and opt-in requirements.
- [ ] Core focus-session fixes remain regression-free.

### Play Console

- [ ] Mandatory developer verification is complete.
- [ ] Merchant onboarding is not treated as a free-launch prerequisite unless Console explicitly links it to mandatory identity verification.
- [ ] No active paid product is advertised by the free build.
- [ ] Closed-test requirement is met where applicable.
- [ ] Production access and review are complete.

### Documentation And Security

- [ ] Privacy policy, Data safety, store metadata, and runtime agree.
- [ ] No secret or personal KYC information is committed.
- [ ] Plan/progress docs distinguish free launch from deferred monetization.
- [ ] Q1 2027 restart runbook exists and requires a new approved implementation plan.

## 10. Rollback Strategy

Before production:

- Stop rollout, fix defect, increment version code, rerun Task 6, and upload replacement.

During staged production:

- Halt rollout for crash, data-loss, session-state, sign-in, backup, or major feature-access regressions.
- Ship a forward fix; do not attempt destructive git history changes or downgrade installed user data.

Free-mode rollback boundaries:

- Do not rebind `RealBillingRepository` as an emergency fix.
- Do not expose paywall against inactive products.
- Do not remove user data when changing entitlement behavior.

## 11. Risks And Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| BillDesk KYC is actually tied to mandatory developer identity | Free launch remains blocked | Task 1 inspects exact Console category before assuming deferral |
| UI hides paywall but runtime still packages Billing | Policy/confusion risk | Remove app module dependency and inspect release dependency graph/manifest |
| Permanent Pro entitlement misses a separate hardcoded gate | Feature remains inaccessible | Static scan plus fresh-install device matrix |
| Backup becomes available but sign-in remains misconfigured | User-facing failure | Play-installed auth/Drive QA before production |
| Free listing later cannot become paid download | Business-model constraint | Commit to free download; future revenue uses in-app products/subscriptions |
| Closed-test tester drops below threshold | Clock resets or production access delays | Recruit buffer above 12 and monitor continuity |
| Existing free users lose features after monetization | Trust/review damage | Require explicit grandfathering/transition decision in future plan |
| KYC policy changes by Q1 2027 | Runbook becomes stale | Recheck official requirements before submitting documents |

## 12. Implementation Completion Checklist

- [x] User approved this plan.
- [x] Task 1 Console audit complete.
- [x] Task 2 free runtime complete.
- [x] Task 3 Pro overview complete.
- [x] Task 4 gates/copy cleanup complete.
- [x] Task 5 metadata/docs alignment complete.
- [ ] Task 6 local verification complete.
- [ ] Task 7 closed test and production release complete.
- [x] Task 8 restart runbook complete.
- [ ] All staged changes reviewed with `git status` and `git diff --cached` before every commit.
- [ ] Sensitive filename/content scan passed before every push.
- [ ] Final implementation summary records files, tests, release state, residual risks, and next backlog item.
