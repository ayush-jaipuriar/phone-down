# Google Play Launch Report: Requirements and Phone Down Progress

Date researched: September 5, 2026

App: Phone Down

Package: `phonedown.app`

Current candidate: version code `6`, version name `1.0.5`

Target path: closed testing, production-access application, then public free production release

## 1. Purpose and Evidence Boundary

This report explains the complete Google Play launch path from developer-account creation through post-release monitoring. It deliberately separates:

1. **General Google Play requirements**: current rules documented by Google and Android.
2. **Phone Down-specific work**: actions and evidence found in this repository, its Git history, and the Play Console submission record from this release session.

Google changes Play requirements and Console navigation over time. The official links in this report are the authority for general requirements; repository records describe what was done for Phone Down. Live Play Console state remains authoritative for whether a task is currently accepted, under review, rejected, or complete.

## 2. The End-to-End Launch Model

A Google Play launch is not a single upload. It is a chain of trust:

```text
Developer identity
  -> Play Console app identity
  -> Android package and signing identity
  -> Release artifact and technical checks
  -> Store listing and policy declarations
  -> Test-track release and review
  -> 12 testers for 14 continuous days, when applicable
  -> Production-access application
  -> Production release and review
  -> Monitoring, updates, and policy maintenance
```

Each layer answers a different question:

- **Who is publishing?** Developer-account verification.
- **Which app is this?** Package name and Play Console app record.
- **Can updates be trusted?** Cryptographic signing.
- **What will users receive?** The Android App Bundle and generated APKs.
- **What does the app do?** Listing, screenshots, declarations, and reviewer access.
- **Has it been tested by real users?** Testing tracks and production-eligibility evidence.
- **Does it remain healthy?** Android vitals, pre-launch reports, crash reporting, reviews, and support.

## 3. Developer and Account Setup

### 3.1 General Google Play requirements

A personal Play Console account requires a developer name, legal name, legal address, contact email, contact phone number, public developer email, and the current Console may also require a developer website. The legal identity is tied to the linked Google Payments profile and must be verified before publishing. Contact details must be verified and kept operational. Google explains the identity fields and which personal-account details may appear publicly in [Required information to create a Play Console developer account](https://support.google.com/googleplay/android-developer/answer/13628312?hl=en) and [Contact information requirements for developer accounts](https://support.google.com/googleplay/android-developer/answer/10840893?hl=en).

The normal full-distribution registration path includes accepting the applicable terms and paying the one-time USD 25 registration fee. Google summarizes this account path in [General conditions of access for Google Play in the EEA](https://support.google.com/googleplay/android-developer/answer/14659200?hl=en) and its newer Android developer distribution model in [Choose a distribution](https://support.google.com/android-developer-console/answer/16640817?hl=en).

New personal accounts may also have to prove access to a real Android phone. Google requires the account owner to use the Play Console mobile app on a non-rooted physical phone running Android 10 or later when that task appears. See [Device verification requirements for new developer accounts](https://support.google.com/googleplay/android-developer/answer/14316361?hl=en).

These account checks are independent of monetization. Publishing a completely free app does not remove identity, contact, device, or testing requirements.

### 3.2 Phone Down-specific actions and state

- A personal Play Console developer account was created and the Phone Down app record exists.
- Repository records say the contact email and phone were verified.
- The app is now intentionally public and completely free. Merchant onboarding and payment-gateway KYC were paused; neither is required for this free launch.
- Repository snapshots did not conclusively record the final developer-identity or physical-device verification completion state. Before production submission, the Play Console account-level dashboard must show no unresolved identity or device-verification task.
- No identity document, legal address, payment information, receipt, or other private verification material is stored in this repository.

### 3.3 Why this stage was required

Google needs a legally accountable publisher and reliable contact path before it gives an app public distribution. This layer protects users and separates publisher verification from app-level technical review.

## 4. Creating the Play Console App

### 4.1 General Google Play requirements

When creating an app, the developer chooses a default language, public app name, app or game classification, free or paid status, and user-facing support email; accepts policy and export-law declarations; and accepts Play App Signing terms. Google documents the workflow in [Create and set up your app](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en).

The package name becomes the technical identity of the Play app after an artifact is uploaded. It must match the Android application ID exactly. Package identity and signing identity are long-lived, so they should be treated as permanent design decisions rather than editable marketing fields.

### 4.2 Phone Down-specific actions

- Public name: `Phone Down`.
- App type: app, not game.
- Price: free.
- Ads: no.
- Package/application ID: `phonedown.app`.
- Default listing and support details were added in Play Console.
- The app was converted to a permanently free runtime for this launch: all current capabilities are included, and purchase, price, subscription, restore-purchase, and upgrade surfaces are removed from the active artifact.

### 4.3 Why this stage was required

The Play Console app is the container that joins the package, signing keys, bundles, store listing, policy answers, testers, countries, and releases. Code alone cannot create that distribution identity.

## 5. Technical Android Readiness

### 5.1 General Google Play requirements

#### Target API level

As of August 31, 2026, new phone/tablet apps and updates submitted to Google Play must target Android 16, API level 36, or higher. Google maintains the current deadline and form-factor exceptions in [Meet Google Play's target API level requirement](https://developer.android.com/google/play/requirements/target-sdk).

Targeting a current API does more than satisfy an upload check. It opts the app into newer security, background-execution, permissions, and user-experience rules. Those behavior changes must be tested, not merely compiled.

#### Release quality

Before upload, a release should:

- use a unique application ID;
- increment its integer version code for every Play update;
- use an understandable version name for humans;
- remove debug-only behavior and secrets;
- shrink/optimize release code where appropriate;
- request only permissions needed by actual features;
- pass unit, integration, UI, lint, release-build, and physical-device checks appropriate to its risk;
- remain functional when backgrounded, interrupted, restored, updated, and installed through Play.

Google's release preparation overview explains that a release-ready app must be configured, built, tested, and signed before distribution: [Prepare your app for release](https://developer.android.com/studio/publish/preparing).

### 5.2 Phone Down-specific technical evidence

The current source and release records show:

| Item | Phone Down evidence |
|---|---|
| Application ID | `phonedown.app` |
| Version | code `6`, name `1.0.5` |
| SDK range | minimum 26, compile/target 36 |
| Release optimization | code minification and resource shrinking enabled |
| Signing | release build requires external keystore configuration; no key material is committed |
| Runtime model | native Android, Kotlin, Jetpack Compose, foreground service, local persistence |
| Public-free boundary | no BillingClient runtime dependency and no Play Billing permission in release artifact |
| Crash diagnostics | Crashlytics disabled for debug and enabled for release |
| Local verification | project checks, unit/regression tests, screenshot tests, lint, signed AAB build, bundle validation, and signature verification recorded as passing |
| Physical-device evidence | connected Android test suite and live smoke tests were run on a physical RMX3853 device |

Recorded final artifacts for 1.0.5:

- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB SHA-256: `7a72957ef8be4eb0c288e5017b3a343031c00996bddac3ff81d3696e97d2a8c5`
- APK SHA-256: `872f44893bc6e9064a56fa789f87ef0a211c877d40af92cd8a3582c4a2dbee1d`

Google Play displayed a native debug-symbol warning for version 6. Repository evidence treats it as non-blocking because Phone Down is primarily Kotlin/Java and no unresolved native-code failure was identified. It should still be revisited if native libraries begin producing unsymbolicated crashes.

### 5.3 Why this stage was required

Play review is not a substitute for engineering QA. Review checks policy and broad behavior; it cannot prove every timer, sensor, persistence, interruption, sign-in, backup, or device-specific path. Phone Down particularly needed real-device testing because its core promise depends on physical orientation sensors and a background focus session.

## 6. Signing and the Android App Bundle

### 6.1 General Google Play requirements

All Android apps must be digitally signed. New Play apps must use Play App Signing. With the recommended split:

- the developer signs the `.aab` with an **upload key**;
- Google verifies that upload and signs device-delivered APKs with the **app signing key**;
- the app signing key establishes update continuity for installed users;
- certificate fingerprints are registered with services such as Google Sign-In.

Google documents the roles and sequence in [Sign your app](https://developer.android.com/studio/publish/app-signing). The upload flow and current bundle-size boundary are documented in [Upload your app to the Play Console](https://developer.android.com/studio/publish/upload-bundle).

Google Play uses the Android App Bundle to generate optimized APKs for each supported device. Every subsequent release must use a higher version code; reusing a consumed version code is not allowed.

### 6.2 Phone Down-specific actions

- An upload keystore was created outside the repository.
- Gradle reads signing values from an ignored local properties file or environment variables and fails release tasks when signing is absent.
- Play App Signing is enabled for the Play app.
- The signed version-code 6 AAB passed local bundle and signature validation and was accepted by Play Console.
- Version 5 was removed from the active closed-test draft when version 6 replaced it, preventing an obsolete foreground-service declaration from being submitted.
- Play signing SHA fingerprints were relevant to Phone Down's Google Sign-In configuration because Play-installed builds are signed by Play, not by the local debug certificate.

### 6.3 Why APK testing alone was insufficient

A locally installed APK can prove core Android behavior, but only a Play-installed build proves the final package/signing identity and Play delivery path. That difference is especially important for Google Sign-In and any API client restricted by package name plus certificate fingerprint.

## 7. Store Listing and Visual Assets

### 7.1 General Google Play requirements

The main listing includes an app name of at most 30 characters, a short description of at most 80 characters, and a full description of at most 4,000 characters. It also includes categorization, contact details, and localized listing content. See [Create and set up your app](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en).

For phone apps, key preview-asset requirements include:

- Play icon: 512 x 512, 32-bit PNG, at most 1,024 KB;
- feature graphic: 1,024 x 500, JPEG or 24-bit PNG without alpha;
- at least two screenshots across supported device types;
- screenshots between 320 px and 3,840 px, with the longest side no more than twice the shortest side.

Google lists current formats, dimensions, and usage in [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en). Listing text and imagery must accurately describe the shipped app and avoid misleading claims, rankings, prices, or implied affiliations; see [Best practices for your store listing](https://support.google.com/googleplay/android-developer/answer/13393723?hl=en).

### 7.2 Phone Down-specific actions

- App name: `Phone Down`.
- Short description: `Focus by putting your phone face down`.
- Full description was updated to describe all capabilities as included, with no paid-tier claim.
- A 512 x 512 Play icon was prepared and uploaded.
- A 1,024 x 500 feature graphic was prepared and uploaded.
- Four 1,080 x 1,920 phone screenshots were prepared, alongside tablet screenshot sets.
- The Play listing icon was corrected to match the intended Phone Down launcher identity. This mattered because the Play listing icon and installed launcher icon are separate assets; changing one does not update the other.
- The store listing, privacy link, account/data-deletion page, and support path were saved in Play Console.

### 7.3 Why this stage was required

The listing is both a product page and a policy promise. Reviewers compare its claims and screenshots with the submitted artifact. A technically correct app can still be rejected if its listing is inaccurate or incomplete.

## 8. App-Content and Policy Declarations

### 8.1 General Google Play requirements

The App content area gathers information Google needs for policy, safety, age, and legal review. Depending on app behavior, this includes:

- privacy-policy URL;
- ads declaration;
- app-access or sign-in instructions;
- target audience and content;
- content rating questionnaire;
- Data safety form and data-deletion answers;
- news, health, financial, government, or other category declarations when applicable;
- high-risk permission and API declarations;
- foreground-service declarations for apps targeting Android 14 or later.

Google describes the core App content workflow in [Prepare your app for review](https://support.google.com/googleplay/android-developer/answer/9859455?hl=en).

#### Privacy policy and Data safety

The privacy policy and Data safety answers must match the behavior of the active artifact, including third-party SDK behavior. Apps on closed, open, or production tracks must complete Data safety; apps exclusively on internal testing are exempt. Even an app that collects no data must complete the form and provide a privacy policy. See [Provide information for Google Play's Data safety section](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en).

#### App access

If any reviewable functionality is gated by login, membership, location, OTP, two-factor authentication, or another restriction, the reviewer must receive durable instructions and reusable access. Google gives the detailed standard in [Requirements for providing sign in details for review](https://support.google.com/googleplay/android-developer/answer/15748846?hl=en-EN).

#### Account and data deletion

If an app enables creation of an app account, Google requires an in-app account-deletion path and an external web resource. All apps must answer the Data safety deletion questions. The distinction matters: signing into a third-party service is not automatically the same as the developer creating a separate app account, so the declaration must describe the real identity model. See [Understanding Google Play's app account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en).

### 8.2 Phone Down-specific declarations

Phone Down's repository answer sheets and policies establish the following release position:

- **Ads:** No ads.
- **Pricing:** Free download; no active subscription, purchase, price, or paywall path.
- **Access:** Core focus functionality is usable without sign-in. Google Sign-In is optional and supports Drive backup/restore rather than creating a separate Phone Down credential system.
- **Data:** Focus sessions and settings are primarily local. Optional Google profile data and Drive backup are used only after user action. Firebase Crashlytics receives release diagnostics.
- **Privacy:** A public privacy policy is hosted at `https://ayush-jaipuriar.github.io/phone-down/privacy-policy/` and corresponding in-app information exists.
- **Deletion:** Local data can be deleted in the app; Drive authorization can be disconnected and an optional backup can be deleted. Public instructions are hosted at `https://ayush-jaipuriar.github.io/phone-down/account-deletion/`.
- **Target audience:** Must match the adult/general-audience productivity positioning actually selected in Console; including children would trigger Families obligations.
- **Content rating:** The questionnaire must remain completed and consistent with the app's non-violent focus/productivity content.
- **Permissions:** Notification, foreground-service, optional phone-state, boot recovery, vibration, and internet uses must match the manifest and disclosures.
- **Financial data:** The active version 6 artifact contains no billing runtime. This is why old billing-enabled artifacts had to be removed before submitting a no-financial-data release declaration.

The authoritative Phone Down answers are maintained in `docs/play-store-data-safety.md`, `docs/privacy-policy.md`, `docs/account-deletion.md`, and `docs/permissions.md`. They must be updated whenever SDKs or runtime data flows change.

### 8.3 Why this stage was required

Declarations are not administrative guesses. They are testable representations of the active bundle. Removing monetization from UI while leaving BillingClient in the artifact, or claiming no crash data while shipping Crashlytics, would create a mismatch and review risk.

## 9. Foreground-Service `specialUse` Declaration

### 9.1 Why Phone Down needs a foreground service

During a user-started focus session, Phone Down must continue monitoring device orientation, tracking valid focus and penalty time, updating its ongoing notification, and completing the session while the app is backgrounded or the screen is off. This is immediate, user-visible work rather than deferrable batch work.

Android foreground services continue noticeable work while the user is not interacting with the app and must display a notification. See [Services overview](https://developer.android.com/develop/background-work/services).

### 9.2 General Android and Play requirements

Apps targeting Android 14 or later must declare an appropriate foreground-service type for every foreground service and the matching type-specific permission. Starting a service without the required type can cause `MissingForegroundServiceTypeException`; missing the required permission can cause `SecurityException`. Google explains the platform enforcement in [Foreground service types are required](https://developer.android.com/about/versions/14/changes/fgs-types-required).

`specialUse` is for a valid foreground-service case not covered by another defined type. It requires:

- `android:foregroundServiceType="specialUse"` on the service;
- `android.permission.FOREGROUND_SERVICE`;
- `android.permission.FOREGROUND_SERVICE_SPECIAL_USE`;
- a manifest `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` value explaining the use case;
- `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` when starting the service;
- a corresponding Play Console declaration reviewed by Google.

See [Foreground service types: Special use](https://developer.android.com/develop/background-work/services/fgs/service-types#special-use).

For every foreground-service type declared in Play Console, Google requires a functionality description, the impact if work is delayed or interrupted, and a video showing how the user triggers the feature. See [Understanding foreground service and full-screen intent requirements](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en). The broader policy requires foreground work to be user-initiated or user-perceptible, stoppable by the user, and necessary for the stated task; see [Device and network abuse](https://support.google.com/googleplay/android-developer/answer/16559646?rd=2).

### 9.3 Phone Down correction and implementation

Version 5 incorrectly classified the focus timer as `dataSync`. That type is for data transfer, not continuous local sensor monitoring and session timing. Version 6 corrected all three required layers:

1. **Manifest:** declares `FOREGROUND_SERVICE_SPECIAL_USE`, sets the service type to `specialUse`, and includes this subtype explanation: `Maintains a user-started focus session by monitoring face-down sensor state and tracking elapsed focus time while the app is not visible.`
2. **Runtime:** starts the service with `ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE` on supported Android versions.
3. **Test and release evidence:** a policy test verifies the manifest/runtime contract, and the signed version 6 AAB was rebuilt and accepted by Play.

The Play Console category was completed using the applicable **Other** use case for the `specialUse` service. The submitted explanation states, in substance:

> Phone Down uses a foreground service during a user-initiated focus session to monitor orientation, track focused and penalty time, update the ongoing notification, and complete the session accurately while the app is in the background or the screen is off. It must start immediately so no focus or pickup time is missed, and system interruption would lose the continuous timing and sensor state needed for an accurate result. The user can pause or end the session.

### 9.4 Demonstration video

Google requires the demonstration to show the actual path that triggers the foreground-service feature. The Phone Down reviewer video shows:

1. selecting a one-minute focus session;
2. starting focus;
3. placing the phone face down;
4. the countdown continuing;
5. backgrounding the app;
6. the persistent `Phone Down - Focus active` notification;
7. returning to the app;
8. clean session completion and summary.

Private, unrelated notifications were removed from the reviewer-safe edit. The final file was uploaded to Google Drive and made available to reviewers using an anyone-with-link viewer URL:

[Phone Down foreground-service demonstration](https://drive.google.com/file/d/1F0nMKzUbw3SkgY4UXQxAymERgKDd3-fb/view?usp=sharing)

Google accepts a YouTube link or a cloud-storage link to a common video format for permission demonstrations. See [Declare permissions for your app](https://support.google.com/googleplay/android-developer/answer/9214102?hl=en).

### 9.5 Why this stage was required

The code declaration tells Android how to enforce the service. The Console declaration tells Play why that use is policy-valid. The video lets a reviewer reproduce and observe the behavior. All three must agree.

## 10. Test-Track Configuration

### 10.1 General Google Play model

Google provides three pre-production tracks:

| Track | Purpose | Important behavior |
|---|---|---|
| Internal | Fast trusted QA, up to 100 testers | Can begin before full app setup; useful for Play signing and distribution checks |
| Closed | Controlled pre-release testing | Required production-eligibility route for affected new personal accounts |
| Open | Public opt-in testing | For affected new personal accounts, available after production access |

For closed testing, the publisher configures a release, countries/regions, an email list or Google Group, a feedback channel, and an opt-in link. A person's email being on the tester list is not enough: the person must open the opt-in link with an eligible Google account and actively join the test. Google documents setup and eligibility in [Set up an open, closed, or internal test](https://support.google.com/googleplay/android-developer/answer/9845334?hl=en).

An important track interaction is that an internal tester is not eligible to receive an open or closed version until they opt out of the internal test and opt into the intended track. The delivered build is the highest compatible version code among tracks for which that user is eligible.

### 10.2 Phone Down-specific test setup

- Internal testing was used earlier for Play-distributed builds and account-level integration work.
- The current closed-test release is `6 (1.0.5) - Public Free`.
- Version 5 was removed when version 6 replaced it.
- All 177 countries/regions offered in that Console configuration were selected.
- Tester list `internal_release_testers` contained four addresses when last recorded.
- A tester feedback path was configured.
- The release, country, tester, listing, app-content, and foreground-service changes were previewed and sent for Google review in the current release session.
- The latest session record showed 15 changes in review, with automated checks running. That state is time-sensitive and must be rechecked in Play Console rather than treated as permanently true.

### 10.3 Why this stage was required

The test track makes the release available through the same signing and delivery system used for production, while limiting exposure. For Phone Down it also creates the eligibility period Google requires from this new personal account.

## 11. Review and Publishing Flow

### 11.1 General Google Play flow

The practical sequence is:

1. Save app setup, listing, and App content changes.
2. Create the track release and upload the signed AAB.
3. Resolve errors and assess warnings.
4. Add release notes, countries, testers, and feedback details.
5. Preview/review the release.
6. Send all pending changes for Google review from Publishing overview.
7. Wait for automated processing and policy review.
8. If rejected, correct the cited artifact, listing, declaration, or access issue and resubmit.
9. If approved, the release becomes available according to its track and publishing mode.

Google distinguishes app, update, and individual-item statuses in [Publish your app](https://support.google.com/googleplay/android-developer/answer/9859751?hl=en_EN). Reviews can take up to seven days or longer in exceptional cases, especially for some new accounts.

With **standard publishing**, approved changes publish automatically. With **managed publishing**, approved changes wait in `Changes ready to publish` until the developer deliberately publishes them. See [Control when app changes are reviewed and published](https://support.google.com/googleplay/android-developer/answer/9859654?hl=en).

### 11.2 Phone Down-specific state

- The version 6 closed-test release and related changes were sent for review.
- Managed publishing was observed as off, so approved changes should become available to the configured closed-test audience automatically.
- The Console's missing-native-debug-symbol warning did not block submission.
- Review approval will publish the **closed test**, not the public production app.
- The next operational checkpoint is to wait until the closed release is published, obtain the opt-in link, and verify installation from Play using eligible tester accounts.

## 12. The 12-Testers / 14-Days Production Gate

### 12.1 Exact general rule

For a **personal developer account created after November 13, 2023**, an app must run a closed test with at least 12 testers continuously opted in for at least the preceding 14 days before the developer can apply for production access. If a tester opts out and later rejoins, that tester's continuity restarts. Production and open-testing features remain unavailable until the account/app meets the applicable gate. See [App testing requirements for new personal developer accounts](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB).

This means:

- `12` is a minimum, not a recommended recruitment target;
- being listed is not the same as opting in;
- installing alone is not the same as remaining opted in;
- the 14 days must be continuous;
- the clock starts only after the closed test is published and each tester actively opts in;
- it is safer to recruit more than 12 people to absorb dropouts;
- Google can require more testing if the production-access application does not demonstrate meaningful testing and readiness.

After the threshold is met, the developer selects **Apply for production** and answers three groups of questions:

1. about the closed test;
2. about the app;
3. about production readiness.

Answers must reflect real recruitment, usage, feedback, defects, and changes. The numerical threshold creates eligibility to apply; it does not guarantee production approval.

### 12.2 Phone Down-specific gap

The last repository snapshot showed:

- tester list size: four;
- opted-in testers: zero;
- required minimum: 12 continuously opted-in testers;
- production access: not yet eligible.

Therefore, the immediate Phone Down work after closed-release approval is:

1. expand the closed-test list or Google Group to at least 12 eligible testers, preferably 15-20;
2. send the official opt-in link;
3. have each tester opt out of conflicting internal testing if necessary;
4. have each tester join the closed test with the same Google account used on Play Store;
5. confirm the Console opted-in count reaches at least 12;
6. maintain at least 12 continuously for 14 full days;
7. collect genuine feedback throughout the period;
8. record anonymized testing evidence, defects, fixes, app/Android versions, and dates;
9. apply for production access only when Play enables the action.

Recommended tester coverage for Phone Down:

- start, pause, resume, cancel, and explicitly end a focus session;
- face-down arming and face-up penalty behavior;
- clean and interrupted completion summaries;
- editable default and one-time custom durations;
- notification permission denied and granted paths;
- active session while backgrounded and with screen off;
- recovery after process death or reboot;
- history, insights, CSV export, and included Pro feature screens;
- optional Google Sign-In, Drive backup, restore, sign-out, and data deletion;
- different Android versions and manufacturers.

## 13. From Production Eligibility to Public Launch

Once the 12/14 gate is complete:

1. Open the Dashboard and select **Apply for production**.
2. Answer from the actual closed-test evidence; do not invent daily activity, feedback, or fixes.
3. Submit the production-access application.
4. If Google requests more testing, follow the exact feedback and preserve tester continuity.
5. When production access is granted, create the production release.
6. Reuse or promote the verified version only if the Console allows it and the same artifact remains the intended candidate; otherwise build a higher version code.
7. Recheck the AAB, countries, listing, App content, Data safety, privacy URL, reviewer access, and release notes.
8. Decide whether to enable managed publishing for a controlled launch time.
9. Send the production release for review.
10. After approval and publication, verify the public listing and install path from a non-tester account/device where practical.

For Phone Down, public availability must not be claimed when only the closed track is approved. The app reaches the general Play audience only when production access is granted and a production release is separately reviewed and published.

## 14. Post-Release Monitoring

### 14.1 General Google Play monitoring

Play automatically produces pre-launch reports for eligible uploaded artifacts and checks stability, Android compatibility, performance, and accessibility on lab devices. Review the overview, details, screenshots, warnings, and stack traces before expanding distribution. See [Use a pre-launch report to identify issues](https://support.google.com/googleplay/android-developer/answer/9842757?hl=en).

After testers or production users install through Play, Android vitals reports stability, performance, battery, memory, and permission problems. Core metrics include user-perceived crash rate, user-perceived ANR rate, excessive partial wake locks, memory usage, and bitmap memory usage. These can affect store visibility. See [Android vitals](https://developer.android.com/topic/performance/vitals).

Monitoring also includes:

- Play review and policy inbox;
- acquisition and install metrics;
- ratings and reviews;
- support email and tester feedback;
- device-catalog compatibility;
- crash reporting such as Firebase Crashlytics;
- sign-in, backup, session-completion, and notification regressions;
- policy and target-API deadline changes.

### 14.2 Phone Down-specific monitoring plan

During closed testing:

- check the closed-test status and opted-in count daily;
- ensure the count never falls below 12 once the eligibility clock is intended to run;
- inspect pre-launch report findings;
- inspect Crashlytics for version 1.0.5 crashes and non-fatal patterns;
- ask testers for reproducible steps, device model, Android version, and session state;
- prioritize failures in focus timing, foreground notification, completion summary, persistence, and Drive restore;
- issue fixes with higher version codes while keeping the closed track active.

After production:

- monitor Android vitals, Crashlytics, reviews, and support daily during the initial rollout;
- verify the live listing, icon, screenshots, privacy links, and install/update path;
- watch foreground-service policy feedback and background reliability across OEMs;
- keep Data safety and privacy disclosures synchronized with every SDK or data-flow change;
- retain the public-free boundary until a separately planned and verified monetization restart is ready.

## 15. Current Phone Down Status

| Launch area | Current state | What remains |
|---|---|---|
| Developer account | Account and app exist; contact verification recorded | Confirm no live identity/device-verification task remains before production |
| App identity | `Phone Down`, `phonedown.app`, free, no ads | Keep immutable package identity and accurate profile data |
| Technical candidate | Version 6 / 1.0.5, target 36, signed AAB validated | Complete remaining Play-installed manual QA and respond to findings |
| Signing | Upload signing and Play App Signing configured | Protect upload key; retain Play signing fingerprints for Google APIs |
| Store listing | Text and assets saved; icon corrected | Recheck live rendering after review |
| App content | Privacy/Data safety/access declarations prepared for free runtime | Keep answers aligned; respond to reviewer questions |
| Foreground service | Corrected to `specialUse`; description and demo submitted | Await policy review result |
| Closed test | Version 6 submitted; 177 countries and four-address list recorded | Wait for publication, expand recruitment, distribute opt-in link |
| Production eligibility | Not yet met | At least 12 continuously opted-in testers for 14 days, then apply |
| Public production | Not yet submitted | Obtain production access, create/review/publish production release |
| Monitoring | Local QA and Crashlytics infrastructure exist | Operate tester feedback, pre-launch report, vitals, reviews, and support loops |

## 16. What We Did, in Order, and Why

1. **Created and verified the developer environment** so Google could associate releases with an accountable publisher.
2. **Created the Phone Down Play app** so package, listing, policies, artifacts, and tracks had one distribution identity.
3. **Established package and signing identities** so Play could verify uploads and safely deliver updates.
4. **Built the Android product and production integrations** including optional Google Sign-In/Drive and release crash reporting.
5. **Fixed functional defects and completed real-device QA** because sensor timing and background behavior cannot be proven from listing forms.
6. **Changed the launch product to completely free** because merchant verification was not required for a non-monetized release and the user wanted publication to proceed.
7. **Removed monetization behavior and claims from the active artifact and listing** so code, UI, Data safety, privacy copy, and Play declarations agreed.
8. **Prepared and uploaded accurate store assets** so users and reviewers see the real product; corrected the mismatched Play icon.
9. **Configured closed testing** with version, countries, testers, and feedback path because this account must build production-eligibility evidence.
10. **Rebuilt as version 6 after correcting the foreground-service type** because `dataSync` did not describe Phone Down's sensor/timer work.
11. **Recorded `specialUse` in manifest, runtime, test, and Play Console** so Android enforcement and Play policy review use one consistent explanation.
12. **Recorded and shared a reviewer-safe demonstration video** so Google can see the foreground service being triggered, continuing in background, notifying the user, and completing.
13. **Previewed and sent the closed-test changes for review** so the release can become installable to eligible closed testers.
14. **Reached the current waiting state**: Google is reviewing the closed-test submission; tester recruitment and the continuous 14-day eligibility period come next.

## 17. Immediate Next-Step Checklist

- [ ] Wait for Play Console to mark the version 6 closed release published or identify a rejection/action item.
- [ ] Review automated checks and the pre-launch report.
- [ ] Expand the closed-test cohort to at least 15-20 reliable people.
- [ ] Share the closed-test opt-in link, not only the app URL or email-list invitation.
- [ ] Confirm at least 12 people are shown as opted in.
- [ ] Preserve at least 12 continuously opted in for 14 full days.
- [ ] Collect and document genuine feedback and fixes during the test.
- [ ] Complete Play-installed checks for focus lifecycle, notifications, summaries, sign-in, Drive, export, recovery, and deletion.
- [ ] Reconfirm account identity and physical-device verification tasks are complete.
- [ ] Apply for production access using real test evidence.
- [ ] After access is granted, prepare and submit the separate production release.
- [ ] Verify public publication and begin daily launch monitoring.

## 18. Evidence Used

### 18.1 Official Google and Android primary sources

- [Required information to create a Play Console developer account](https://support.google.com/googleplay/android-developer/answer/13628312?hl=en)
- [Contact information requirements for developer accounts](https://support.google.com/googleplay/android-developer/answer/10840893?hl=en)
- [Device verification requirements for new developer accounts](https://support.google.com/googleplay/android-developer/answer/14316361?hl=en)
- [Create and set up your app](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en)
- [Meet Google Play's target API level requirement](https://developer.android.com/google/play/requirements/target-sdk)
- [Prepare your app for release](https://developer.android.com/studio/publish/preparing)
- [Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Upload your app to the Play Console](https://developer.android.com/studio/publish/upload-bundle)
- [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en)
- [Prepare your app for review](https://support.google.com/googleplay/android-developer/answer/9859455?hl=en)
- [Provide information for Google Play's Data safety section](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en)
- [Requirements for providing sign in details for review](https://support.google.com/googleplay/android-developer/answer/15748846?hl=en-EN)
- [Understanding Google Play's app account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en)
- [Foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Understanding foreground service and full-screen intent requirements](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en)
- [Device and network abuse](https://support.google.com/googleplay/android-developer/answer/16559646?rd=2)
- [Set up an open, closed, or internal test](https://support.google.com/googleplay/android-developer/answer/9845334?hl=en)
- [App testing requirements for new personal developer accounts](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB)
- [Publish your app](https://support.google.com/googleplay/android-developer/answer/9859751?hl=en_EN)
- [Control when app changes are reviewed and published](https://support.google.com/googleplay/android-developer/answer/9859654?hl=en)
- [Use a pre-launch report to identify issues](https://support.google.com/googleplay/android-developer/answer/9842757?hl=en)
- [Android vitals](https://developer.android.com/topic/performance/vitals)

### 18.2 Repository and release-history evidence

- `app/build.gradle.kts`: package, version, release signing, shrinking, Crashlytics build policy.
- `app/src/main/AndroidManifest.xml`: permissions, launcher identity, service and receiver declarations, `specialUse` subtype.
- `app/src/main/java/phonedown/app/runtime/FocusSessionService.kt`: foreground-service start and special-use runtime type.
- `app/src/test/java/phonedown/app/runtime/FocusForegroundServicePolicyTest.kt`: policy-contract regression test.
- `build-logic/convention/src/main/kotlin/phonedown.android.application.gradle.kts`: minimum, compile, and target SDK levels.
- `docs/release-readiness.md`: final 1.0.5 artifact, QA, signing, SDK, and hash evidence.
- `docs/public-free-release-qa.md`: public-free artifact checks, device evidence, and Console gates.
- `docs/phase-16-console-setup-info.md`: Play App Signing, listing, privacy URLs, closed-track, country, tester, icon, and foreground-service records.
- `docs/play-store-data-safety.md`, `docs/privacy-policy.md`, `docs/account-deletion.md`, and `docs/permissions.md`: app-behavior and policy mapping.
- `fastlane/metadata/android/en-US/`: listing text, icon, feature graphic, screenshots, and version 6 release notes.
- Commit `374dddd`: aligned the Play listing icon with the launcher identity.
- Commit `652d5bc`: prepared the permanent-free release candidate and policy boundary.
- Commit `8621086`: recorded closed-test release, country, tester, and feedback setup.
- Commit `f384867`: corrected the foreground service to `specialUse`, added policy regression coverage, and prepared version 6 / 1.0.5.
- Current release-session evidence: foreground-service demo recording and upload, declaration save, release preview, review submission, 15 changes entering review, and managed publishing observed off.

## 19. Evidence Limitations

- Play Console status is live and can change after this report. Recheck the Console before relying on any `in review`, tester-count, or eligibility statement.
- Repository documents intentionally omit private account, tester, contact, KYC, and signing-secret values.
- The repository proves build configuration and recorded verification results; it cannot independently prove current Play account identity/device-verification status.
- Google can apply account-specific review or testing requirements beyond the published minimum and can change policy deadlines.
- Closed-test approval is not production approval, and meeting 12 testers for 14 days creates the right to apply rather than an automatic public launch.
