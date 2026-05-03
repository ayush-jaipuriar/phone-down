# Phase 14 — QA, Polish, And Release Readiness Plan

## Scope

Bring Phone Down to production release quality. This phase includes comprehensive automated testing, manual device validation, visual asset creation, Play Store listing preparation, release build configuration, and final code quality passes. Any bugs discovered during manual testing will be documented for a follow-up bug-fix sprint rather than fixed in this phase.

## Clarifications Applied

| Question | Answer | Plan Impact |
|---|---|---|
| QA focus | Both automated and manual equally | Split time 50/50 between test gap filling and device validation |
| Bug handling | Separate bug-fix sprint after Phase 14 | Document bugs in a `phase-14-bugs.md` file; do not fix in this phase |
| Release target | Production release | Need signed release APK, Play Store listing, screenshots, privacy policy compliance |
| App icon | Create one | Include icon design/generation in plan |
| App name | "Phone Down" final | Lock name in all user-facing strings and metadata |
| Screenshots | Yes | Generate light + dark screenshots for phone + tablet form factors |
| Feature graphic | Needed | Create Play Store feature graphic |
| Crash reporting | Not specified | Defer to post-V1 to avoid third-party SDK complexity |
| Code quality | Include lint/dead code/perf audit | Add lint pass, unused code scan, basic Compose perf check |

## Architecture

### Module Placement

| Concern | Module | Rationale |
|---|---|---|
| Unit tests | Per-module test directories | Follow existing pattern in `:app/src/test`, `:domain:insights/src/test`, etc. |
| UI tests | `:feature:*` androidTest | Feature modules own their Compose UI tests |
| Screenshot tests | Paparazzi in `:feature:*` | Existing Paparazzi setup for visual regression |
| App icon | `app/src/main/res/mipmap-*` | Standard Android icon placement |
| Play Store assets | `fastlane/metadata/android/` or `play-store-assets/` | Fastlane-compatible structure for screenshots and listing |
| Release build config | `:app/build.gradle.kts` | Signing config, version code, ProGuard |

## Implementation Steps

### Step 1 — Automated Test Gap Analysis and Filling (4 hours)

#### Session Domain Engine Tests (`:domain:session`)
- [ ] `SessionStateMachineTest`: test all state transitions
  - CREATED → WAITING_FOR_PHONE_DOWN
  - WAITING → ARMING (valid face-down)
  - ARMING → ACTIVE (after 3s)
  - ARMING → WAITING (invalid during arming)
  - ACTIVE → PAUSED_BY_PICKUP
  - PAUSED → ACTIVE (return to face-down)
  - ACTIVE → PAUSED_BY_CALL
  - Any state → COMPLETED (timer reaches planned duration)
  - Any state → ENDED_EARLY (user taps end)
  - INVALIDATED (0-20% valid focus)
  - PARTIAL (21-79%)
  - STRONG_PARTIAL (80-99%)
- [ ] `PenaltyRulesTest`:
  - 5-second grace period for minor interruption
  - Clean status removed on any interruption
  - 1-minute penalty after grace period exceeded
  - Broken after 60s continuous invalid state
  - Broken after 3 penalty interruptions
  - Broken session can continue accumulating valid focus
- [ ] `EarlyEndClassificationTest`:
  - Boundary tests at 20%, 21%, 79%, 80%, 99%, 100%
  - Verify INVALIDATED does not count toward focus time
  - Verify PARTIAL/STRONG_PARTIAL count but are not completed
- [ ] `CallPauseTest`:
  - Call pause removes clean status
  - Call pause does not break session
  - Call end resumes session if face-down

#### Sensor Engine Tests (`:core:sensors`)
- [ ] `FaceDownEvaluatorTest`:
  - Stable face-down orientation detected
  - Face-up rejected
  - Vertical rejected
  - Horizontal threshold acceptance
- [ ] `MovementStabilityTest`:
  - Tiny table vibration accepted
  - Small bump within tolerance accepted
  - Walking movement rejected
  - Vehicle movement rejected
  - In-hand movement rejected
- [ ] `ArmingWindowTest`:
  - 3-second arming countdown
  - Reset on invalid during arming
  - Success after 3 seconds stable

#### Insights Domain Tests (expand `:domain:insights`)
- [ ] `FocusQualityCalculationTest`: boundary tests for score labels (Deep, Focused, Steady, Fragmented, Scattered)
- [ ] `StreakCalculationTest`: streak breaks, multiple streaks, edge cases
- [ ] `BestHourCalculationTest`: tie handling, no data, single session
- [ ] `BestDayCalculationTest`: tie handling, weekday vs weekend
- [ ] `TrendCalculationTest`: completion rate, clean ratio, interruption trends
- [ ] `HeatmapAggregationTest`: empty days, partial weeks, month boundaries

#### Database Tests (expand `:core:database`)
- [ ] `FocusSessionDaoTest`: bulk insert, update, delete
- [ ] `PenaltyEventDaoTest`: foreign key constraints, cascade delete
- [ ] `MigrationTest`: schema version 1 → 2 (when needed)
- [ ] `RoomSessionRepositoryTest`: observe flows, error handling

#### ViewModel Tests
- [ ] `FocusViewModelTest`: state transitions, user actions, error states
- [ ] `InsightsViewModelTest`: loading states, empty states, Pro gates
- [ ] `AccountViewModelTest`: sign in/out, restore states, error handling
- [ ] `ProViewModelTest`: product loading, purchase flow, restore flow

### Step 2 — Compose UI Tests (3 hours)
- [ ] `FocusScreenTest`: start flow, duration selection, state visibility
- [ ] `InsightsScreenTest`: scroll through sections, Pro teaser visibility
- [ ] `SettingsScreenTest`: toggle interactions, navigation, delete dialog (expand existing)
- [ ] `OnboardingScreenTest`: pager progression, completion persistence
- [ ] `AccountScreenTest`: signed in/out states, restore button
- [ ] `ProScreenTest`: product cards, purchase button states

### Step 3 — Paparazzi Screenshot Regression (2 hours)
- [ ] Record/update baselines for all screens in light + dark
- [ ] Add screenshot tests for any new screens since last baseline update
- [ ] Verify no visual regressions across feature modules
- [ ] Screenshots to test:
  - Focus (all states: home, waiting, arming, active, interrupted, completed)
  - Insights (with data, empty, loading)
  - Settings (all sections visible)
  - Account (signed in, signed out)
  - Pro (product list)
  - Onboarding (all 3 cards)
  - Privacy Policy
  - Delete confirmation dialog

### Step 4 — Manual Device Testing Matrix (4 hours)

#### Devices to Test
- [ ] High-end device (Pixel 7/8 or equivalent)
- [ ] Samsung device (different sensor calibration)
- [ ] Low-end device (< 4GB RAM, older Android)
- [ ] Tablet (if targeting tablets)

#### Android Versions
- [ ] Android 12 (API 31)
- [ ] Android 13 (API 33)
- [ ] Android 14 (API 34)
- [ ] Android 15 (API 35)

#### Focus Ritual Scenarios
- [ ] Start session, place phone down, complete naturally
- [ ] Pick up phone during session (minor interruption within 5s)
- [ ] Pick up phone for > 5s (penalty applied)
- [ ] Pick up phone for > 60s (session broken)
- [ ] Receive phone call during session (call pause)
- [ ] End session early (test all classification boundaries)
- [ ] Force close app during active session (recovery on relaunch)
- [ ] Device restart during active session (recovery on boot)
- [ ] Battery saver mode during session
- [ ] Screen off during session (timer continues)
- [ ] App backgrounded during session (notification shows)
- [ ] Tap notification "End Session" action
- [ ] Place phone on desk, bed, and charging pad
- [ ] Tiny table bump (should not trigger penalty)
- [ ] Walk with phone in pocket (should not count as valid)

#### Settings & Account Scenarios
- [ ] Change theme (Light → Dark → System)
- [ ] Toggle sounds/haptics
- [ ] Sign in with Google account (fake flow)
- [ ] Trigger backup (fake flow)
- [ ] Restore backup (fake flow)
- [ ] Delete all data with confirmation
- [ ] Delete all data including cloud backup

#### Edge Cases
- [ ] First install → onboarding → focus flow
- [ ] Upgrade install (if previous version exists)
- [ ] Offline mode (airplane mode)
- [ ] No Google account (free user flow)
- [ ] Pro entitlement (fake purchase flow)
- [ ] Low storage (near-full device)
- [ ] Low battery (< 15%)
- [ ] Notification permission denied (Android 13+)

### Step 5 — Bug Documentation (1 hour)
- [ ] Create `docs/phase-14-bugs.md`
- [ ] Document each bug with:
  - Device/Android version
  - Reproduction steps
  - Expected vs actual behavior
  - Severity (Critical / High / Medium / Low)
  - Suggested fix approach
- [ ] Do NOT fix bugs in this phase

### Step 6 — Visual Asset Creation (3 hours)

#### App Icon
- [ ] Create adaptive icon foreground (vector drawable)
- [ ] Create adaptive icon background (color or simple pattern)
- [ ] Generate all mipmap densities:
  - mdpi (48x48)
  - hdpi (72x72)
  - xhdpi (96x96)
  - xxhdpi (144x144)
  - xxxhdpi (192x192)
- [ ] Create round icon variant (if desired)
- [ ] Create Play Store icon (512x512 PNG)
- [ ] Verify icon visibility on light and dark device wallpapers

#### Play Store Screenshots
- [ ] Generate screenshots for phone form factor (light theme):
  - Focus home screen
  - Active session screen
  - Completed session screen
  - Insights screen (with data)
  - Settings screen
  - Pro paywall
- [ ] Generate screenshots for phone form factor (dark theme):
  - Same 6 screens as above
- [ ] Generate tablet screenshots (if targeting tablets)
- [ ] Ensure screenshots show realistic data (not empty states)
- [ ] Ensure no debug UI, fake data labels, or placeholder text visible
- [ ] Store in `fastlane/metadata/android/en-US/images/phoneScreenshots/` and `sevenInchScreenshots/`

#### Feature Graphic
- [ ] Create 1024x500 PNG feature graphic for Play Store
- [ ] Include app name, tagline, and key visual
- [ ] Ensure text is readable at small sizes
- [ ] Store in `fastlane/metadata/android/en-US/images/featureGraphic.png`

### Step 7 — Play Store Listing Preparation (2 hours)
- [ ] Create `fastlane/metadata/android/en-US/title.txt` — "Phone Down"
- [ ] Create `fastlane/metadata/android/en-US/short_description.txt` — 80 char max
- [ ] Create `fastlane/metadata/android/en-US/full_description.txt` — 4000 char max
- [ ] Create `fastlane/metadata/android/en-US/changelogs/default.txt` — initial release notes
- [ ] Verify privacy policy URL (use GitHub Pages or similar for `docs/privacy-policy.md`)
- [ ] Complete data safety form using `docs/play-store-data-safety.md`
- [ ] Define content rating (likely "Everyone" or "Teen")
- [ ] Define app category (Productivity or Health & Fitness)
- [ ] Prepare contact email and website

### Step 8 — Release Build Configuration (2 hours)
- [ ] Configure release signing in `app/build.gradle.kts`:
  - Create or reference keystore properties
  - Do NOT commit keystore file or passwords
  - Add keystore path to `.gitignore`
- [ ] Verify version code and version name:
  - Version code: 1 (or appropriate number)
  - Version name: "1.0.0" (first production release)
- [ ] Verify ProGuard/R8 rules are comprehensive:
  - No runtime crashes in release build
  - No missing classes warnings
  - Logging properly stripped
- [ ] Build signed release APK/AAB:
  - `bundleRelease` for Play Store (AAB preferred)
  - Verify build succeeds with no errors
- [ ] Test release build on device:
  - Install signed release APK
  - Verify core flow works
  - Verify ProGuard didn't break reflection/DI

### Step 9 — Code Quality Pass (3 hours)

#### Lint and Static Analysis
- [ ] Run `./gradlew lintDebug` and review all warnings
- [ ] Fix or suppress legitimate lint issues
- [ ] Document suppressed lint rules with justification
- [ ] Check for unused resources (`lint --check UnusedResources`)
- [ ] Check for unused imports and code (`detekt` or IDE analysis)

#### Dead Code Removal
- [ ] Remove unused imports across all modules
- [ ] Remove unused string resources
- [ ] Remove unused drawable/color resources
- [ ] Remove commented-out code blocks
- [ ] Remove placeholder/empty composables that are no longer needed
- [ ] Remove unused dependencies from `build.gradle.kts` files

#### Performance Audit
- [ ] Compose recomposition check:
  - Use `LayoutInspector` or recomposition counts to find hot paths
  - Ensure `SessionScreen` doesn't recompose on every sensor tick
  - Ensure `InsightsScreen` charts don't recompose unnecessarily
- [ ] Database query check:
  - Review all DAO queries for N+1 patterns
  - Ensure `observeLatestSessions` and `observeSessionsInWindow` use indexes
  - Verify bulk operations are efficient
- [ ] Memory check:
  - Ensure large session histories don't cause OOM in Insights
  - Ensure bitmaps (if any) are properly recycled
- [ ] Battery check:
  - Verify sensor sampling rate is reasonable (not too aggressive)
  - Verify foreground service doesn't hold unnecessary wakelocks

#### Accessibility Check
- [ ] Verify all interactive elements have minimum 48dp touch target
- [ ] Verify color contrast meets WCAG AA (4.5:1 for text)
- [ ] Add content descriptions for critical icons
- [ ] Test with TalkBack on at least one screen
- [ ] Respect system font scaling

### Step 10 — Final Verification and Documentation (2 hours)
- [ ] Run full test suite: `:app:testDebugUnitTest`, all module tests
- [ ] Run Paparazzi verification: `:feature:*:verifyPaparazziDebug`
- [ ] Build debug and release variants successfully
- [ ] Update `v1-implementation-plan.md` Phase 14 checklist
- [ ] Update `docs/agent-handoff.md` with Phase 14 status
- [ ] Create `docs/release-readiness.md` summarizing:
  - What's complete
  - What's deferred to post-V1
  - Known issues and limitations
  - Bug list link (`docs/phase-14-bugs.md`)
  - Release build instructions

## Tradeoffs

### Manual Testing Scope vs. Time
- **Decision**: Cover 4 devices × 4 Android versions with core ritual scenarios only, not exhaustive edge cases.
- **Why**: Full matrix testing (all devices × all versions × all scenarios) would take weeks. Core ritual + key edge cases gives 80% confidence.
- **Risk**: Some device-specific sensor quirks may be missed. Mitigation: beta testing with wider device pool post-release.

### App Icon Design
- **Decision**: Create a simple, text-based or geometric icon rather than commissioning custom illustration.
- **Why**: Production-ready illustration takes days and may need revision cycles. A clean geometric icon (e.g., downward arrow, phone silhouette) is fast and professional.
- **Risk**: Less distinctive than a custom illustration. Mitigation: plan icon redesign in V2 based on user feedback.

### Crash Reporting
- **Decision**: Defer Firebase Crashlytics or similar to post-V1.
- **Why**: Adds third-party SDK, privacy policy implications, and network dependency. For V1, rely on Google Play Console crash reports.
- **Risk**: Less detailed crash diagnostics. Mitigation: add comprehensive logging (redacted) for manual debugging.

### Screenshot Generation
- **Decision**: Use Paparazzi or Compose Preview screenshots rather than manual device screenshots.
- **Why**: Faster, consistent, and works in CI. Device screenshots are only needed if Play Store requires them.
- **Risk**: May not reflect actual device appearance perfectly. Mitigation: generate a few manual device screenshots for key screens as backup.

## Acceptance Criteria

- [ ] All automated test gaps filled (session engine, sensors, insights, database, ViewModels)
- [ ] Compose UI tests cover all major screens
- [ ] Paparazzi baselines updated with no regressions
- [ ] Manual testing matrix completed on at least 2 physical devices
- [ ] All discovered bugs documented in `docs/phase-14-bugs.md`
- [ ] App icon created and visible at all densities
- [ ] Play Store screenshots generated for light + dark themes
- [ ] Feature graphic created
- [ ] Play Store listing metadata prepared
- [ ] Release signing configured (keystore not committed)
- [ ] Signed release AAB builds successfully
- [ ] Release build tested on device
- [ ] Lint warnings reviewed and addressed
- [ ] Dead code removed
- [ ] Performance audit completed with no critical issues
- [ ] Accessibility checks passed
- [ ] `v1-implementation-plan.md` updated
- [ ] `docs/agent-handoff.md` updated
- [ ] `docs/release-readiness.md` created

## Residual Risks

- Manual testing may reveal critical bugs requiring Phase 14 scope expansion (per plan, these go to bug-fix sprint)
- Some device-specific sensor behavior may only surface in beta testing
- Play Store review may require additional assets or changes
- Keystore management is manual — ensure secure backup of signing key
- ProGuard may cause subtle runtime issues not caught by tests

## Checklist

- [ ] Step 1: Automated test gap filling
- [ ] Step 2: Compose UI tests
- [ ] Step 3: Paparazzi screenshot regression
- [ ] Step 4: Manual device testing
- [ ] Step 5: Bug documentation
- [ ] Step 6: Visual asset creation
- [ ] Step 7: Play Store listing preparation
- [ ] Step 8: Release build configuration
- [ ] Step 9: Code quality pass
- [ ] Step 10: Final verification and documentation
- [ ] Update `v1-implementation-plan.md`
- [ ] Update `docs/agent-handoff.md`
- [ ] Create `docs/release-readiness.md`
