# Phase 16 Console Setup Info

## 1. Purpose

This file is the safe bridge between browser-console setup and app implementation.

As we wire real Google Sign-In, Google Drive backup, Play Billing, Crashlytics, and release signing into Phone Down, we need certain public configuration facts. This file is where we collect those facts in one place.

The idea is simple:

- we need a few configuration values from Google/Play
- we do not want secrets in the repo
- we do not want you to wonder what is safe to send back

So this file records only non-secret values and statuses.

## 1.1 How To Use This While You Learn

Treat this file like a release lab notebook.

For each field you fill, ask:

1. which system gave me this value?
2. what future app behavior depends on it?

That habit turns setup from rote clicking into understanding.

## 2. What This File Is For

Use this file to record:

- project IDs
- package names
- public SHA fingerprints
- product IDs
- setup yes/no statuses
- test-user and testing-track status

Use this file to avoid recording:

- passwords
- private keys
- service-account credentials
- OAuth client secrets
- access tokens
- recovery codes

Why this matters:

- we need enough information to wire the app correctly
- we do not need anything sensitive to do that work

## 2.1 Mental Model

This file is the public configuration map of Phone Down’s production identity.

It tells us:

- how Google knows the app
- how Play knows the app
- what purchase IDs the app expects
- which test environments are truly ready

## 3. Safe vs Unsafe Information

### Safe To Record Here

- package name
- application ID
- project ID
- SHA-1 fingerprint
- SHA-256 fingerprint
- product IDs
- tester group names
- whether Play App Signing is enabled
- whether OAuth consent is configured

### Not Safe To Record Here

- keystore password
- key password
- `.jks` or `.keystore` file contents
- service-account JSON
- OAuth client secret
- access token
- refresh token
- identity verification docs
- recovery codes

Theory:

Public certificate fingerprints are like a public label for a key. They help Google identify the app, but they do not let anyone sign as you.

Passwords, key files, and tokens are different. They are actual secrets that can give someone control or access.

## 4. App Identity

These values define the technical Android identity of Phone Down.

| Field | Value | Why It Matters |
|---|---|---|
| App name | Phone Down | User-facing app name in Play and consent screens |
| Android package / application ID | `phonedown.app` | Permanent Android identity used by Play, Firebase, and Google APIs |
| Android namespace | `phonedown.app` | Build/config namespace in the app module |
| Version name | `1.0.3` | Human-readable release version |
| Version code | `4` | Internal monotonically increasing Android release number |
| App type | Free app with in-app purchases | Tells Play we monetize via Billing, not paid install |
| Ads | No ads | Must match Play Console declaration and app behavior |
| Support email | `jaipuriar.ayush@gmail.com` | Public support contact used in console and policy flows |

### Theory

The package name is one of the most important identities in Android. Google services often trust the combination of:

- package name
- signing certificate fingerprint

That is why we treat `phonedown.app` as stable.

### Study Note

## 4.1 Current Backup And Crash Reporting Policy

| Field | Value | Why It Matters |
|---|---|---|
| Android OS backup | Disabled with `android:allowBackup="false"` | Keeps restore behavior under Phone Down's explicit Google Drive backup flow |
| App-managed backup | Google Drive `appDataFolder`, opt-in | User-controlled backup and restore path |
| Crash reporting | Firebase Crashlytics for release/internal builds | Lets tester crashes become diagnosable before closed testing |
| Debug crash collection | Disabled | Keeps development crashes out of production diagnostics |

No secrets belong in this section. Firebase/Play console values needed for this policy are public configuration status only.

If you remember only one Android identity rule, remember this:

`package name + signing certificate` is one of the most important trust combinations in the Android ecosystem.

## 5. Local Debug Certificate

These fingerprints are safe to use in Firebase and Google Cloud for local debug testing.

| Fingerprint | Value |
|---|---|
| Debug SHA-1 | `6F:55:47:BF:27:95:9E:D4:5E:34:BE:54:5B:8C:4A:E3:C8:C9:97:39` |
| Debug SHA-256 | `A3:4D:4C:62:AF:41:1A:4E:EF:5A:BE:86:6E:54:E3:EB:59:41:B9:4C:17:DD:2C:34:E5:46:87:68:78:5A:EC:81` |

Command used:

```bash
keytool -list -v \
  -alias androiddebugkey \
  -keystore "$HOME/.android/debug.keystore" \
  -storepass android \
  -keypass android
```

### Why These Matter

When we test Google Sign-In locally, the installed app is signed with the Android debug certificate, not the Play certificate.

If these fingerprints are not added in Firebase/Google config:

- local sign-in can fail even though the app code is correct

### Why These Are Safe

These fingerprints are public identifiers, not secrets.

### Study Note

A fingerprint is like the public label on a lock. It helps identify the lock, but it does not let someone unlock it.

## 6. Play Console Setup

Fill these after creating the Play Console account and Phone Down app.

| Field | Value | Why It Matters |
|---|---|---|
| Developer account type | TODO: Personal / Organization | Affects verification and sometimes release requirements |
| Developer name | TODO | User-facing publisher name |
| Play Console app created | Yes | Confirms Play-side app identity exists |
| Play App Signing enabled | Yes | Play-installed builds use Google's app signing certificate |
| Play app package | `phonedown.app` | Must exactly match Android application ID |
| Play App Signing enabled | TODO: Yes / No | Needed for recommended release flow |
| Internal testing track created | TODO: Yes / No | Needed for early Play-distributed testing |
| Closed testing required by Google | TODO: Yes / No / Unknown | Determines whether 12 testers / 14 days is part of the release path |
| Closed testing track created | TODO: Yes / No | Needed if required by Google or useful for broader testing |
| 12 tester list started | TODO: Yes / No | Helps avoid release delays if production access requires it |

### Sprint 16.5 Store Listing Status - 2026-06-12

| Field | Value | Why It Matters |
|---|---|---|
| Dashboard store setup status | Store listing setup no longer appears as the visible dashboard blocker | Confirms Phase 1 store-listing setup is done |
| Remaining visible task | Closed testing setup: countries/regions, testers, preview/confirm release, send release for review | Next blocker moved from store listing to testing release workflow |
| Default listing locale | English (United Kingdom) - `en-GB` | Store text is being entered into the default listing shown by Play Console |
| App name field | `Phone Down` | Confirms the listing uses the intended public app name |
| Short description prepared | `Focus by putting your phone face down` | Existing Fastlane metadata reused for Play Console consistency |
| Full description prepared | Existing Fastlane full description | Reuses current product copy and avoids production overclaims |
| App icon asset prepared | `fastlane/metadata/android/en-US/images/icon.png` - 512x512 | Matches Play's required app-icon size |
| Feature graphic asset prepared | `fastlane/metadata/android/en-US/images/featureGraphic.png` - 1024x500 | Matches Play's required feature-graphic size |
| Phone screenshots prepared | `fastlane/metadata/android/en-US/images/phoneScreenshots/*.png` - 1080x1920 | Provides 4 compliant phone screenshots |
| 7-inch tablet screenshots prepared | `fastlane/metadata/android/en-US/images/sevenInchScreenshots/*.png` - 1080x1920 | Provides required tablet screenshot assets if Play asks for them |
| 10-inch tablet screenshots prepared | `fastlane/metadata/android/en-US/images/tenInchScreenshots/*.png` - 1080x1920 | Provides required tablet screenshot assets if Play asks for them |
| Store listing saved in Play Console | Yes | Play Console confirmed `Change saved. Send for review in Publishing overview.` |
| Current blocker | Required closed-testing dashboard steps are incomplete | Select countries/regions, select testers, preview/confirm release, then send release for review |
| Production-access tester status | 0 opted-in testers | The 12 tester / 14 day clock has not started |

Why this matters:

- Browser automation could not attach local files through the Chrome extension, even after file URL access was enabled.
- Computer Use succeeded through the native macOS file picker and attached all required public assets: icon, feature graphic, phone screenshots, 7-inch tablet screenshots, and 10-inch tablet screenshots.
- The listing is now saved. Play quick checks finished, but review submission is still blocked by required dashboard setup for the closed testing release.
- The next Play Console work is not a store-listing fix; it is closed-test release setup and tester recruitment.

### Sprint 16.5 Store Icon Correction - 2026-06-13

| Field | Value | Why It Matters |
|---|---|---|
| Issue found | Play Console store-listing app icon did not match the intended app / launcher icon | Store presence should use one consistent visual identity |
| Corrected local asset | `fastlane/metadata/android/en-US/images/icon.png` - 512x512 | Keeps Fastlane metadata aligned with the Play Console asset |
| Source asset | `app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png` | Uses the same P/D mark family as the launcher icon |
| Play Console status | Corrected icon uploaded and saved on 2026-06-13 | Confirms the live draft listing now uses the intended icon |
| Review status | Saved to Publishing overview, not sent for review | Avoids submitting before closed-testing setup is ready |

Why this matters:

- Google Play uses the store-listing icon, not the Android launcher icon, for the public store front.
- Android launcher icons and Play store icons are separate assets, so changing one does not automatically update the other.
- The repo metadata and Play Console draft now point at the same intended P/D visual direction, reducing the risk of a future upload reintroducing the stale icon.

### Theory

These values tell us where you are in the Play release lifecycle. They are not just admin details; they affect what engineering can test next.

For example:

- if Play App Signing is enabled, we later need Play signing SHA fingerprints
- if internal testing exists, we can test Billing and Play-installed Sign-In properly

### What To Watch For

If the Play app exists but the testing tracks do not, we are still not ready for realistic Play-distributed behavior testing.

## 6.1 Privacy Policy Hosting

| Field | Value | Why It Matters |
|---|---|---|
| Planned public host | GitHub Pages from this repository | Fastest way to produce a stable public privacy-policy URL |
| Planned site URL | `https://ayush-jaipuriar.github.io/phone-down/` | Base URL for the public Pages site |
| Privacy policy URL | `https://ayush-jaipuriar.github.io/phone-down/privacy-policy/` | Public URL used in the Play Console privacy policy field |
| Account deletion URL | `https://ayush-jaipuriar.github.io/phone-down/account-deletion/` | Public URL used for Play account deletion requirements in Data safety |
| Current status | Published and live on GitHub Pages | Public compliance URLs are now available for Play Console forms |

### Theory

Play does not care where the privacy policy is hosted, but it does care that:

- the URL is public
- the URL is stable
- the page content matches real app behavior

GitHub Pages works well because it gives us a low-friction public URL while keeping the policy text in the same repository as the app and release docs.

## 7. Upload Keystore

Keep the keystore outside this repo.

Recommended location:

```text
$HOME/.android/phone-down-release/phone-down-upload.jks
```

Recommended alias:

```text
phone-down-upload
```

### What This Is

This is the key you use to upload builds to Play Console.

It is not the same thing as the Play App Signing key. Think of it as “your upload identity,” while Google Play uses its own managed signing identity to deliver builds to users.

### Why We Need It

Without an upload key:

- you cannot upload signed release bundles properly
- we cannot get the upload SHA fingerprints
- Google/Firebase setup stays incomplete for release-side testing

### Record Only These Public Values

| Fingerprint | Value | Why It Matters |
|---|---|---|
| Upload key SHA-1 | `EE:FA:73:EF:A2:F0:6A:A1:8F:03:A8:0E:C4:A4:20:F7:65:33:A3:9C` | Needed for Google/Firebase app trust configuration |
| Upload key SHA-256 | `63:0E:62:5F:A1:14:13:C9:A0:FB:2B:53:E8:4B:5A:D2:B3:03:11:B5:0D:52:4F:42:B9:92:75:0E:2C:7E:F9:0A` | Needed for stronger certificate matching and some API config |

### Generation Command

```bash
mkdir -p "$HOME/.android/phone-down-release"

keytool -genkeypair \
  -v \
  -keystore "$HOME/.android/phone-down-release/phone-down-upload.jks" \
  -alias phone-down-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### Fingerprint Command

```bash
keytool -list -v \
  -keystore "$HOME/.android/phone-down-release/phone-down-upload.jks" \
  -alias phone-down-upload
```

### Do Not Record

- keystore password
- key password
- keystore file contents

### Study Note

The upload keystore is not an ordinary project file. It is long-lived release infrastructure.

## 7.1 Play App Signing Certificate

Google Play uses this certificate to sign builds delivered to users from Play. These values are public certificate fingerprints, not private keys.

Verified in Play Console on July 9, 2026.

| Fingerprint | Value | Why It Matters |
|---|---|---|
| Play App Signing SHA-1 | `3D:57:D0:6B:3F:38:A6:D7:40:4F:48:F1:9E:4C:68:07:55:30:D0:25` | Needed for Play-installed Google Sign-In and Drive trust |
| Play App Signing SHA-256 | `E2:EA:8A:C4:27:76:1F:66:3F:96:9F:D7:5E:F6:45:3E:A8:93:17:10:21:CE:20:9F:D7:78:4A:DC:60:D7:95:66` | Needed for stronger certificate matching and Android app links/trust surfaces |

### Current Progress

Recorded from local `keytool` output on `2026-05-15`:

- alias: `phone-down-upload`
- creation date: `2026-05-15`
- upload SHA-1 recorded
- upload SHA-256 recorded

Sensitive values such as the keystore password were intentionally not recorded here.

## 8. Play App Signing Certificate

Fill these after Play App Signing is enabled and the values become visible in Play Console.

| Fingerprint | Value | Why It Matters |
|---|---|---|
| Play App Signing SHA-1 | TODO | Needed because Play-distributed installs are signed by Google |
| Play App Signing SHA-256 | TODO | Same reason, stronger fingerprint |

### Theory

This is the part many Android beginners miss.

A debug-installed build and a Play-installed build can both be “the same app” from your perspective but appear different to Google APIs because they are signed differently.

If we forget to add Play App Signing fingerprints:

- Google Sign-In may work locally
- then fail on internal testing or production

### Shortcut Memory Aid

Debug install:

- debug SHA

Your upload artifact:

- upload SHA

Play-installed build:

- Play signing SHA

## 8.1 Android OAuth Clients

These are the Android-specific OAuth client records that bind package name plus certificate fingerprint to Google Sign-In trust.

| Field | Value | Why It Matters |
|---|---|---|
| Android OAuth debug client created | Yes | Needed for real debug-build Google Sign-In |
| Android OAuth debug client name | `Phone Down Android Debug` | Human-readable client label in Google Cloud |
| Android OAuth debug client type | Android | Confirms this is the correct client platform |
| Android OAuth debug client ID | Created | Actual runtime client identity exists in Google Cloud |
| Android OAuth upload client created | Yes | Needed for locally signed release/upload-key installs |
| Android OAuth upload client name | `Phone Down Android Upload` | Human-readable client label in Google Cloud |
| Android OAuth Play Signing client created | Yes | Needed for Play-installed builds |
| Android OAuth Play Signing client name | `Phone Down Android Play Signing` | Human-readable client label in Google Cloud |
| Web OAuth client for Credential Manager | Yes | Needed for `default_web_client_id` used by Sign in with Google |

### Theory

The OAuth consent setup defines what the app is allowed to ask for.

The Android OAuth client defines which installed Android app is allowed to ask for it.

That is why both are needed:

- consent config = policy and trust UI
- Android OAuth client = package/fingerprint identity binding

### Current Progress

Recorded from user console progress on `2026-05-15`:

- Android OAuth client created for debug testing
- client name is `Phone Down Android Debug`
- client type is `Android`

Updated on `2026-07-09`:

- Added Firebase Android app fingerprints for debug, upload, and Play App Signing SHA-1/SHA-256 values.
- Created missing Google Cloud Android OAuth clients:
  - `Phone Down Android Upload`
  - `Phone Down Android Play Signing`
- Downloaded refreshed local `app/google-services.json`.
- Refreshed config now contains Android OAuth clients for debug, upload, and Play App Signing SHA-1 plus the existing Web client.

Important operational note:

- do not download the OAuth client JSON for this Android mobile flow
- we only need the client to exist in Google Cloud for the Android sign-in path

Current implementation note:

- the Android app now uses Credential Manager for Google Sign-In
- refreshed Firebase config now generates `default_web_client_id`
- debug, upload-key release, and Play-installed Google Sign-In are ready for manual device QA after Google propagation

## 9. Google Cloud / Firebase Project

Recommendation: use a dedicated project for Phone Down.

| Field | Value | Why It Matters |
|---|---|---|
| Google Cloud project name | `phone-down` | Human-readable project identity |
| Google Cloud project ID | `phone-down-496414` | Technical project identifier used in config and APIs |
| Firebase project created | Yes | Needed for Crashlytics and app config |
| Firebase Android app package | `phonedown.app` | Must match the app exactly |
| Firebase Android app nickname | `Phone Down Android` | Helps distinguish the app in Firebase |
| `google-services.json` downloaded locally | Yes | Needed later when we wire Firebase into the Android app |
| Drive API enabled | Yes | Required for cloud backup/restore |

### Theory

Google Cloud is the API/security identity layer. Firebase is the mobile-app convenience layer on top of it.

We care about both because:

- Google Sign-In and Drive permissions depend on Cloud/OAuth config
- Crashlytics and Android app registration depend on Firebase

### Current Progress

Recorded from user console progress on `2026-05-15`:

- Google Cloud project name: `phone-down`
- Google Cloud project ID: `phone-down-496414`
- Firebase project exists
- Firebase Android app for `phonedown.app` exists
- Firebase Android app nickname is `Phone Down Android`
- Google Drive API enabled in the correct project

Current local config status:

- `google-services.json` has been placed at `app/google-services.json`
- refreshed on July 9, 2026 after Firebase/Google Cloud OAuth updates
- ignored by Git
- the file remains ignored by git
- contents were intentionally not copied into docs

### Important Safety Note

`google-services.json` is currently ignored by `.gitignore`.

That is good for now because it reduces the chance of accidental configuration drift or secret confusion while we are still setting things up.

Also:

- do not download service-account private key JSON
- do not create or share OAuth client secrets for this mobile-app flow unless we explicitly add a server-side feature later

### Practical Rule

If a downloaded file or console action mentions `secret`, `private key`, or `service account`, pause and double-check before using it in this mobile-app release flow.

## 10. OAuth Consent Screen

| Field | Value | Why It Matters |
|---|---|---|
| OAuth app name | Phone Down | User-visible name during Google consent |
| User type | External | Should usually be `External` for a consumer app |
| Support email | Configured | User-facing trust and support contact |
| Developer contact email | Configured | Google’s contact path for issues or verification |
| Privacy policy URL | TODO | Required trust and review document |
| Authorized domain | TODO | Needed if domain-backed links are used |
| Test users added | Yes | Required while the app is in testing mode |

### Required Scopes

| Scope | Purpose | Why We Need It |
|---|---|---|
| `openid` | identity foundation | standard Google identity flow |
| `email` | account email display | show signed-in account in UI |
| `profile` | account profile display | show name/avatar if desired |
| `https://www.googleapis.com/auth/drive.appdata` | hidden app-specific backup storage | backup/restore without broad Drive access |

### Theory

The OAuth consent screen is not just bureaucracy. It is the exact moment a user decides whether your app looks trustworthy enough to connect to their Google account.

### Current Progress

Recorded from user console progress on `2026-05-15`:

- OAuth app name is `Phone Down`
- user type is `External`
- publishing status remains `Testing`
- support email is configured
- developer contact email is configured
- required scopes have been added:
  - `openid`
  - `email`
  - `profile`
  - `https://www.googleapis.com/auth/drive.appdata`
- at least one test user has been added

Using the smallest necessary scopes helps because:

- consent looks cleaner
- users feel safer
- review risk stays lower

### What Not To Do

Do not request broad Drive scopes unless the product truly needs file-browser-style access. Phone Down does not.

### Study Note

Scope choice is both a security decision and a product-trust decision. Asking for less access usually makes review and user trust easier.

## 11. Play Billing Products

Recommended product setup:

| Plan | Product ID | India Price | Default International Price | Status |
|---|---|---:|---:|---|
| Monthly Pro | `pro_monthly` | INR 99/month | USD 1.99/month | Blocked: BillDesk merchant verification in progress |
| Yearly Pro | `pro_yearly` | INR 799/year | USD 14.99/year | TODO |
| Lifetime Pro | `pro_lifetime` | INR 1,999 | USD 39.99 | TODO |

Confirmed contract:

- Use separate subscription product IDs `pro_monthly` and `pro_yearly`.
- Use one-time product ID `pro_lifetime`.
- Do not switch to a shared `pro` subscription unless app code and docs are deliberately changed together.

### Theory

Product IDs are effectively API identifiers. Once code depends on them, changing them later is annoying.

That is why we want to choose clean, stable names now.

These product entries also matter for testing because:

- the app queries them from Play
- the Pro screen shows their real prices/details
- entitlement restoration depends on the same IDs

### 2026-07-09 Billing Console Status

- Created `pro_monthly` subscription shell as `Phone Down Pro Monthly`.
- Started monthly auto-renewing base plan with base plan ID `monthly`.
- Confirmed Play Console showed billing period `Monthly` after selecting `Auto-renewing`.
- Attempted 177-country bulk pricing from INR 99.
  - Play rounded India to INR 100.00.
  - United States generated as USD 0.99, not the USD 1.99 launch reference.
  - Manual United States USD 1.99 inline override did not persist.
- Play Console rejected the monthly base-plan draft with `Your changes couldn't be saved`.
- Current recommended recovery: create monthly with India-only pricing first for internal billing QA, then expand and override international prices after the base plan saves.

### 2026-07-09 Billing Console Recovery Status

- Added and saved subscription benefit for `pro_monthly`:
  - `Unlimited focus sessions and Pro tools`
- Retried `pro_monthly` monthly base plan after benefits were saved.
- Minimal retry used:
  - base plan ID `monthly`
  - auto-renewing monthly billing period
  - India-only availability
  - INR 99 entered, normalized by Play to INR 100.00
- Result:
  - base-plan save still failed with `Your changes couldn't be saved`.
- Current diagnosis:
  - all-region pricing was not the blocker
  - missing subscription benefits was not the blocker
  - likely remaining issue is Play Console/backend/account-state validation for base-plan creation
- Current next step:
  - manually retry base-plan creation after refresh/new browser session
  - escalate to Play Console support if the same save failure repeats

### 2026-07-11 Merchant Verification Status

- Account-level diagnosis confirmed PA-CB merchant verification was the billing blocker.
- Play Console stated that an India payments-profile developer cannot sell until BillDesk verification completes.
- Verification was initiated from `Settings > Payments profile`.
- Current status is `In progress`.
- Next action is user completion of the BillDesk KYC instructions sent from `onboarding@billdesk.com`; check Inbox and Spam if delivery is delayed.
- Google Payments may also send a separate identity-verification notice requesting organization details and a readable government photo ID; PAN is listed as an accepted example.
- The Play Console payments view does not expose an upload form yet and continues to direct the user to the BillDesk email.
- Do not retry product creation until verification is approved. Then retry `pro_monthly` first and create `pro_yearly` / `pro_lifetime` only after monthly saves.

### What Makes A Good Product ID

A good product ID is:

- stable
- readable
- boring

That is a compliment. Boring IDs age well.

## 12. Tester Setup

| Group | Status | Why It Exists |
|---|---|---|
| Internal testers | TODO | Needed for quick Play-distributed app testing |
| License testers | TODO | Needed for Play Billing test purchases |
| OAuth test users | TODO | Needed while Google OAuth app is still in testing mode |
| Closed testers | TODO | Needed if Google requires 12 testers / 14 days before production |

### Theory

Testing in Google ecosystems is not one single thing.

Different tester roles unlock different systems:

- internal testers test Play distribution
- license testers test billing safely
- OAuth test users test Google sign-in before the app is broadly published

## 13. Status Tracker

Use this section as a quick human-readable checkpoint.

| Area | Status | Notes |
|---|---|---|
| Play Console account | TODO | |
| Phone Down Play app shell | TODO | |
| Play App Signing | TODO | |
| Upload keystore generated | TODO | |
| Firebase project | TODO | |
| Firebase Android app | TODO | |
| Google Drive API enabled | TODO | |
| OAuth consent configured | TODO | |
| Billing products created | TODO | |
| Internal track created | TODO | |
| Closed testing requirement known | TODO | |

### How To Read This Tracker

If something is still `TODO`, ask:

- does this block real code integration now?
- or does it only block later release submission?

That helps us move forward without pretending every release task has to be finished before any implementation can start.

## 14. Exactly What To Send Back

After you finish the console work, send back only this safe information:

```text
Play Console:
- Developer account type:
- Developer name:
- Play app created: yes/no
- Play App Signing enabled: yes/no
- Internal testing track created: yes/no
- Closed testing required: yes/no/unknown

Google Cloud/Firebase:
- Project ID:
- Firebase project created: yes/no
- Firebase Android app added for phonedown.app: yes/no
- Drive API enabled: yes/no
- OAuth consent configured: yes/no
- Test users added: yes/no

Fingerprints:
- Upload key SHA-1:
- Upload key SHA-256:
- Play App Signing SHA-1, if available:
- Play App Signing SHA-256, if available:

Billing:
- Product IDs chosen:
- Products created/active: yes/no

Files:
- google-services.json downloaded locally: yes/no
```

## 15. What Not To Send Back

Do not send:

- keystore password
- key password
- keystore file
- service-account JSON
- OAuth client secret
- access tokens
- refresh tokens
- private identity documents

## 16. Why This File Helps The Code Phase

Once you send back the safe values above, I can move into real implementation with much less friction.

Specifically, I can:

- wire Firebase using the correct project identity
- implement Google Sign-In knowing which SHA contexts are already configured
- implement Drive backup against the right OAuth scope
- wire Play Billing against stable product IDs
- reason about whether we are testing local-debug installs or Play-distributed installs

That is why this file exists. It turns a vague “I think I set the consoles up” feeling into a concrete, implementation-ready handoff.

## 17. Reflection Questions

Use these as mini study prompts while filling the sheet:

- Why is the package name recorded here even though it already exists in code?
- Why can the same app need multiple SHA fingerprints?
- Why do Billing product IDs belong in release setup rather than only in code?
- Why is `drive.appdata` enough for Phone Down’s backup goal?
