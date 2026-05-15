# Agent Handoff Summary

## 1. Goal

- Build Phone Down into a real production-ready Android Play Store app, not just a feature-complete app with fake external integrations.
- Current production-readiness phase is `Phase 16 - Android Production Readiness`, covering real Google Sign-In, real Google Drive backup/restore, real Play Billing, release signing, Crashlytics, Play policy readiness, and final release QA.
- Sprint `16.1` is the current active sprint: release infrastructure, console setup, and documentation handholding for a first-time Play publisher.
- The user explicitly wants beginner-friendly, theory-backed guides so they can learn the release stack while doing it.
- There is also uncommitted UI polish work in the tree for Focus/Insights/Settings typography and screenshot baselines; that work must not be overwritten while Phase 16 continues.
- User has now completed the Play Console/account/app-shell setup path through Step 2 and has generated the upload keystore with public SHA fingerprints recorded.
- The local repo is now wired with the Google services Gradle plugin so `app/google-services.json` can be consumed by Firebase-aware builds, but no Firebase SDKs have been intentionally added yet.
- Firebase project/app setup is now confirmed complete enough for the next console steps: the `phone-down` Firebase project exists, the Android app `phonedown.app` exists, and `google-services.json` is already placed locally.
- Google Drive API is now enabled for project `phone-down-496414`, so the next required browser-side step is OAuth consent configuration.
- OAuth consent configuration is now effectively complete: branding, external audience, testing status, required scopes, and at least one test user are all in place.
- The first Android OAuth client now exists for debug testing: `Phone Down Android Debug`.
- Sprint 16.2 focused implementation plan has been drafted in `phase-16-sprint-16-2-real-google-sign-in-plan.md`; implementation has not started yet and still needs user approval.
- Sprint 16.2 implementation is now code-complete for debug Google Sign-In wiring, and the Firebase config now generates `default_web_client_id`, so manual sign-in QA can proceed.

## 2. Context The Next Agent Must Know

- Read `AGENTS.md` first and follow it strictly.
- Repo workflow rules:
  - ask clarifying questions before writing any new phase/sprint plan
  - do not implement a phase until the user approves the plan
  - update relevant Markdown docs during meaningful progress
  - run comprehensive verification before claiming implementation completion
- Architecture:
  - `:app` owns navigation, Activities, runtime/service orchestration, permission flows, and eventually real auth/billing launch coordination
  - `:feature:*` modules are UI-focused
  - `:core:auth`, `:core:backup`, and `:core:billing` still need real production implementations in Phase 16
  - `:domain:session` and `:domain:insights` are already real and tested
- Key Phase 16 product decisions already made:
  - create a dedicated Google Cloud/Firebase project for Phone Down instead of reusing `only-yours`
  - use Google Drive `appDataFolder` for backup
  - implement once-daily auto-backup with WorkManager
  - keep Room database unencrypted for V1
  - add Firebase Crashlytics with minimal disclosed diagnostics
  - keep the 24-hour entitlement cache
  - recommended India launch pricing: `INR 99/month`, `INR 799/year`, `INR 1,999 lifetime`
- New personal Play accounts may require closed testing with at least 12 opted-in testers for 14 continuous days before production access.
- The user is a complete beginner to Play publishing and wants step-by-step guidance plus theory.
- Do not overwrite existing uncommitted changes unless the user explicitly asks.
- Do not commit anything unless the user explicitly asks.

## 3. Work Completed

- Launcher icon productionization work has now started:
  - selected the final app icon direction from generated concept variations
  - replaced the placeholder launcher assets with new density-specific `ic_launcher.png` and `ic_launcher_round.png` files derived from the approved master icon
  - regenerated adaptive-icon layer assets `ic_launcher_foreground.png` and `ic_launcher_background.png` across mipmap densities
  - added `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`
  - updated `AndroidManifest.xml` to explicitly use `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`
- Phase 15 was already implemented before this handoff cycle:
  - real Pause/Add Time end to end
  - real full-replace restore
  - call permission education flow
  - notification tap-to-Focus warm-start routing
  - Settings cleanup of dead rows
  - consistent “today” metrics semantics
- Phase 16 planning was completed and approved:
  - created [phase-16-android-production-readiness-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-android-production-readiness-plan.md)
  - plan covers Play Console, Google Cloud/Firebase, real Google Sign-In, real Drive backup, auto-backup, real Play Billing, Crashlytics, signing, policy docs, and QA gates
- Sprint 16.1 repo-side documentation work was completed:
  - created [docs/play-console-release-guide.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/play-console-release-guide.md)
  - created [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md)
  - created [docs/phase-16-manual-qa.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-manual-qa.md)
  - updated [docs/architecture-guide.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/architecture-guide.md) so the real/deferred matrix marks auth, billing, Drive, Crashlytics, auto-backup, and release signing as active Phase 16 targets
  - updated the Phase 16 plan progress checklists and theory sections
- The three new Phase 16 docs were expanded significantly in this session into combined guide/study docs:
  - `docs/play-console-release-guide.md` now includes mental models, glossary-style explanations, release-identity theory, common misunderstandings, why each step exists, and checkpoint questions
  - `docs/phase-16-console-setup-info.md` now explains what each field means, what is safe vs unsafe to share, and how values map back to implementation
  - `docs/phase-16-manual-qa.md` now explains QA philosophy, evidence capture, severity thinking, what failures usually imply architecturally, and why each test exists
- Current uncommitted UI polish work already present in the tree and reviewed during this session:
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
    - added a `FocusTodayMetric` helper and centered the Today metrics with slightly larger values
  - `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt`
    - added local Insights typography helpers, centered Today metrics, and increased emphasis/card typography
  - `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownSettingsComponents.kt`
    - increased Settings row title/support/trailing typography
  - `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsScreen.kt`
    - increased section-header sizing
  - related Paparazzi baseline PNGs for Focus, Insights, and Settings are modified
- Commands run in this session:
  - `sed -n '1,260p' AGENTS.md`
  - `git status --short --branch`
  - `git diff --stat`
  - `git diff -- <changed Kotlin files>`
  - reads of `docs/agent-handoff.md`, `docs/play-console-release-guide.md`, `phase-16-android-production-readiness-plan.md`
  - `git diff --check -- ...` on the doc files
  - `wc -l` on the new guide docs
- Verification performed in this session:
  - `git diff --check` passed for the updated Phase 16 docs
- Verification not performed in this session:
  - no Gradle build/test run was needed because this session’s actual edits were documentation-only
- Additional user-provided console progress captured:
  - upload keystore alias: `phone-down-upload`
  - upload key SHA-1: `EE:FA:73:EF:A2:F0:6A:A1:8F:03:A8:0E:C4:A4:20:F7:65:33:A3:9C`
  - upload key SHA-256: `63:0E:62:5F:A1:14:13:C9:A0:FB:2B:53:E8:4B:5A:D2:B3:03:11:B5:0D:52:4F:42:B9:92:75:0E:2C:7E:F9:0A`
  - Google Cloud project name: `phone-down`
  - Google Cloud project ID: `phone-down-496414`
  - `google-services.json` downloaded locally and placed at `app/google-services.json` (ignored by git)
- Local Gradle integration completed in this session:
  - added Google services plugin version to `gradle/libs.versions.toml`
  - added root plugin declaration in `build.gradle.kts`
  - applied `com.google.gms.google-services` in `app/build.gradle.kts`
  - intentionally did not add Firebase Analytics or Firebase BoM yet

## 4. Current Workspace State

- Branch: `main`
- `git status --short --branch` shows: `## main...origin/main`
- No staged files
- Modified files:
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
  - `app/src/main/res/mipmap-*/ic_launcher.png`
  - `app/src/main/res/mipmap-*/ic_launcher_round.png`
  - `app/src/main/res/mipmap-*/ic_launcher_foreground.png`
  - `app/src/main/res/mipmap-*/ic_launcher_background.png`
  - `core/designsystem/src/main/kotlin/phonedown/core/designsystem/PhoneDownSettingsComponents.kt`
  - `docs/agent-handoff.md`
  - `feature/focus/src/main/kotlin/phonedown/feature/focus/FocusScreen.kt`
  - `feature/focus/src/test/snapshots/images/phonedown.feature.focus_FocusScreenScreenshotTest_idleState_Dark.png`
  - `feature/focus/src/test/snapshots/images/phonedown.feature.focus_FocusScreenScreenshotTest_idleState_Light.png`
  - `feature/insights/src/main/kotlin/phonedown/feature/insights/InsightsContent.kt`
  - `feature/insights/src/test/snapshots/images/phonedown.feature.insights_InsightsScreenScreenshotTest_insightsContentDark.png`
  - `feature/insights/src/test/snapshots/images/phonedown.feature.insights_InsightsScreenScreenshotTest_insightsContentLight.png`
  - `feature/settings/src/main/kotlin/phonedown/feature/settings/SettingsScreen.kt`
  - `feature/settings/src/test/snapshots/images/phonedown.feature.settings_SettingsScreenScreenshotTest_settingsScreenDark.png`
  - `feature/settings/src/test/snapshots/images/phonedown.feature.settings_SettingsScreenScreenshotTest_settingsScreenLight.png`
- Untracked files:
  - `docs/phase-16-console-setup-info.md`
  - `docs/phase-16-manual-qa.md`
  - `docs/play-console-release-guide.md`
- These uncommitted changes include both:
  - real current work from this session on Phase 16 docs
  - existing UI polish changes that must not be overwritten
- No staged files were present.
- No obvious secrets or suspicious files were noticed in the current workspace listing.
- `.gitignore` already protects important release-sensitive patterns including `.env`, `.bak`, `.key`, `.pem`, `.p12`, `service-account*.json`, `client_secret*.json`, `oauth*.json`, `*.jks`, `*.keystore`, and `google-services.json`.

## 5. Decisions And Rationale

- Dedicated Google Cloud/Firebase project for Phone Down:
  - avoids identity/scope/quota confusion with `only-yours`
  - cleaner OAuth branding, Firebase ownership, and future maintenance
- Google Drive `appDataFolder` for backup:
  - narrowest useful Drive scope
  - keeps backups hidden and implementation-focused
  - aligns with the app’s privacy/minimalism posture
- Once-daily auto-backup with WorkManager:
  - practical V1 feature
  - low cost for a small app
  - matches the product promise
- Keep Room database unencrypted for V1:
  - session data is not highly sensitive enough to justify Phase 16 complexity
  - better to ship real auth/billing/backup reliably first
- Add Firebase Crashlytics:
  - needed for production visibility
  - must be privacy-scoped and disclosed properly
- 24-hour entitlement cache retained:
  - good offline/resilience tradeoff for V1
- India pricing recommendation:
  - `INR 99/month`, `INR 799/year`, `INR 1,999 lifetime`
  - chosen to be accessible in India while still commercially credible
- Documentation-first Sprint 16.1:
  - the user is a beginner to Play publishing
  - auth/billing/Drive implementation should not start before package/fingerprint/project/test-track setup is clear
- Recent UI polish changes were kept scoped:
  - Focus Today card got a custom local metric layout instead of changing shared metric-card behavior globally
  - Insights and Settings typography changes were localized to preserve existing architecture while improving visual hierarchy

## 6. Known Issues / Blockers

- Biggest current blocker: browser-side console setup has not been completed yet by the user.
- Biggest current blocker: Google Cloud/Firebase/OAuth setup is still pending.
  - Dedicated Google Cloud project now exists, but Firebase-specific setup is still pending
  - No Play App Signing SHA values yet
  - No Billing products created yet
  - Firebase project/app status still needs explicit confirmation even though `google-services.json` now exists locally
- Because of that, real Phase 16 code integration has not started.
- The workspace is dirty with uncommitted UI typography polish plus doc work. The next agent must preserve that state.
- No code verification was run in this session because the session work was documentation-only.
- Existing historical constraint from previous work:
  - `./scripts/check.sh` may still fail on known ktlint convention disagreements around PascalCase Compose naming and some project formatting conventions
- The current handoff file itself is modified in this session and not committed.

## 7. Exact Next Steps

1. Inspect the current repo state before doing anything else:
   - run `git status --short --branch`
   - read [docs/agent-handoff.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/agent-handoff.md)
   - read [phase-16-android-production-readiness-plan.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/phase-16-android-production-readiness-plan.md)
2. Do not start real auth/billing/Drive code until the user completes the remaining console setup using:
   - [docs/play-console-release-guide.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/play-console-release-guide.md)
   - [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md)
3. Ask the user to send back the remaining safe values from section `14` of [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md):
   - developer account type / Play app status
   - project ID / Firebase/Drive/OAuth status
   - Play App Signing SHA values if available
   - Billing product IDs and activation status
   - whether `google-services.json` is downloaded locally
4. Once those values exist, begin Sprint `16.2`:
   - inspect `:core:auth`, `:core:model` auth repository contracts, and existing Account route/ViewModel files
   - implement real Google Sign-In behind `AuthRepository`
   - keep fake implementations available for tests if still useful
5. Preserve the current uncommitted UI polish files unless the user explicitly asks to revert or commit them.

## 8. Suggested Prompt For The Next Agent

```text
Continue work in the Phone Down project. First, read `AGENTS.md`, `docs/agent-handoff.md`, and inspect the actual repo state with `git status --short --branch` and relevant diffs. Treat all uncommitted changes as user/previous-agent work and do not overwrite or revert them unless explicitly asked. Do not commit anything unless the user explicitly asks.

Current situation:
- Phase 15 is already implemented.
- Phase 16 (Android Production Readiness) is approved and Sprint 16.1 documentation is complete.
- The user is a complete beginner to Play publishing and wants step-by-step, theory-backed guidance.
- Real production integrations have NOT started yet because console setup is still pending.
- The user should complete the browser-side setup using:
  - `docs/play-console-release-guide.md`
  - `docs/phase-16-console-setup-info.md`
- There are also uncommitted UI typography polish changes in Focus/Insights/Settings plus Paparazzi baseline updates; preserve them.

Your first tasks:
1. Read `docs/agent-handoff.md`, `phase-16-android-production-readiness-plan.md`, and the three Phase 16 docs.
2. Reconfirm current git state and do not disturb existing uncommitted UI/doc changes.
3. Ask the user to send back only the safe values from section 14 of `docs/phase-16-console-setup-info.md` if they have completed console setup.
4. If the user has completed console setup, begin Sprint 16.2 by inspecting `:core:auth`, auth repository contracts in `:core:model`, and the account/auth UI wiring in `:app`, then implement real Google Sign-In.

Keep the repo as source of truth, update docs during meaningful progress, and avoid secrets at all times.
```
