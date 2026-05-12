# Phone Down Play Console Release Guide

## 1. Purpose

This guide explains, step by step, how to take Phone Down from a local Android project to a real Google Play production app.

It is written for a complete beginner. That means this document does two jobs at the same time:

- it tells you exactly what to click or configure
- it explains the theory behind each step so the process feels understandable instead of mysterious

This guide is focused on browser-console and release-operations work:

- Google Play Console
- Google Cloud
- Firebase
- OAuth consent
- Google Drive API setup
- Billing product setup
- upload signing setup
- testing tracks
- policy and listing readiness

Use [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md) beside this guide to record safe non-secret values while you work.

## 1.1 How To Use This As Both A Guide And A Study Document

This document is intentionally written in two layers.

### The first layer is execution

Use it when you are actively working through browser tabs and setup screens.

In that mode:

- follow the numbered steps in order
- treat each checklist as a completion gate
- record values in the setup-info file as you go

### The second layer is understanding

Use it when you want to learn what Android release infrastructure is actually doing.

In that mode:

- read the theory sections slowly
- pay attention to the “why this matters” subsections
- use the glossary-like explanations as anchors

Why this is useful:

- beginners often get stuck when a guide tells them what to click without explaining why
- the goal here is for you to be able to reason about the system, not just survive it once

## 1.2 Learning Goals

By the end of this guide, you should understand:

- why Play Console, Firebase, and Google Cloud are separate but connected
- why package name and signing fingerprints both matter
- why debug builds and Play-installed builds can behave differently
- why narrow OAuth scopes are better for trust and review
- why Billing products live in Play Console rather than in code alone
- why internal and closed testing exist before production

## 2. What We Are Actually Building

Phone Down is not just an APK you upload somewhere. A production Android app is really a bundle of connected systems:

1. the app code in this repo
2. a Play Console app listing and release pipeline
3. a Google/Firebase project for sign-in, Drive access, and crash reporting
4. billing products in Play Console
5. signing keys that prove builds really came from you

Why this matters:

- the app code alone cannot do real Google Sign-In
- the app code alone cannot sell subscriptions
- the app code alone cannot upload to the Play Store
- the app code alone cannot safely identify a release build to Google services

So the theory is simple: code gives the app behavior, and console configuration gives the app identity, trust, and permissions in the Google ecosystem.

## 2.1 The Five Identities Of A Production Android App

One of the cleanest ways to understand Android release work is to realize that a real app has several identities at once.

### Product identity

This is what users see:

- app name
- icon
- screenshots
- developer name

### Android identity

This is the app’s technical identity:

- package name
- version code
- version name

### Signing identity

This proves which builds really belong to the app:

- debug certificate
- upload key
- Play App Signing key

### Google API identity

This tells Google services which app is asking for sign-in or Drive access:

- Google Cloud project
- Firebase app registration
- OAuth consent screen
- enabled APIs and scopes

### Commerce identity

This tells Play what the app can sell:

- subscription product IDs
- base plans
- one-time products

Most release confusion comes from mixing these identities up. This guide is really about keeping them aligned.

## 3. The Big Picture Release Flow

This is the full production path we are following:

1. create Play Console developer account
2. create the Phone Down app shell in Play Console
3. create a dedicated Google Cloud and Firebase project
4. configure Android app identity, OAuth, and Drive API
5. generate an upload keystore outside the repo
6. create billing products in Play Console
7. integrate real Google services into the app code
8. upload an internal testing build
9. test sign-in, billing, backup, notifications, and crashes on real devices
10. run closed testing if Google requires it
11. complete store listing and policy declarations
12. release to production

Why this order matters:

- the package name and SHA fingerprints must exist before Google Sign-In works correctly
- billing products are easiest to verify once a Play build exists
- release signing must be in place before Play-distributed testing behaves like the final product

## 3.1 How This Connects Back To Phone Down’s Codebase

This release work is not separate from the app architecture. It feeds directly into the modules we already built.

- `:core:auth` will depend on the OAuth and Firebase setup from this guide
- `:core:backup` will depend on Drive API setup and the `drive.appdata` scope
- `:core:billing` will query the exact product IDs created in Play Console
- `:app` will later initialize Firebase/Crashlytics and coordinate Activity-based auth and billing flows

That is why this phase starts with console setup before real production integration code. The code needs real external identities to attach to.

## 4. Important Concepts Before You Start

These terms come up constantly. Understanding them will make the rest of the setup much less intimidating.

### 4.1 Play Console

Play Console is Google’s dashboard for publishing Android apps.

You use it to:

- create the app listing
- upload builds
- define billing products
- add testers
- fill policy forms
- release new versions

Think of it as the distribution and monetization control panel for Android.

### 4.2 Google Cloud Project

A Google Cloud project is the identity container for APIs and OAuth configuration.

You use it to:

- enable Google APIs like Drive
- manage OAuth consent
- see project IDs and service configuration

Think of it as the technical backend identity for your app’s Google integrations.

### 4.3 Firebase Project

Firebase sits on top of Google Cloud and provides mobile-app-focused tooling.

For Phone Down, we mainly want it for:

- Crashlytics
- easier Android app registration
- `google-services.json`

Think of Firebase as the mobile-friendly face of Google Cloud for apps.

### 4.4 Package Name

The package name is the app’s permanent technical identity on Android.

For Phone Down it is:

```text
phonedown.app
```

Why it matters:

- Play Console app identity depends on it
- Firebase Android app registration depends on it
- Google Sign-In and API access are bound to it

Do not casually change it later.

### 4.5 SHA Fingerprints

SHA fingerprints are public fingerprints of the certificate used to sign your Android app.

Google uses them to trust that your installed app really belongs to the registered app identity.

You will deal with three certificate contexts:

- debug key: used for local development installs
- upload key: used by you to upload builds to Play
- Play App Signing key: used by Google Play to distribute production builds

Why this matters:

- Google Sign-In may work on a local debug install but fail on a Play install if the Play signing SHA is missing
- many Android beginners think “it worked on my device once, so setup is done,” but signed builds can behave differently

### 4.6 OAuth Consent Screen

OAuth consent is the Google screen users see when your app asks for account-related permissions.

For Phone Down, this will explain that the app wants:

- basic Google account identity
- hidden app-specific Drive backup access

Why it matters:

- it is the user-facing trust layer for Google Sign-In
- if configured badly, the app can look suspicious or confusing

### 4.7 Drive `appDataFolder`

This is a hidden area of the user’s Google Drive reserved for app-owned data.

Why we want it:

- it keeps backups private and implementation-focused
- the user does not need to manage files manually
- the permission scope is narrower than broad Drive access

Think of it as “private cloud save storage for the app,” not “a file browser.”

### 4.8 Play Billing Products

These are the things a user buys through Google Play.

For Phone Down we need:

- monthly Pro
- yearly Pro
- lifetime Pro

Why Play Billing exists:

- Google requires digital in-app subscriptions and purchases to go through Play Billing on Android
- it handles purchase UI, payment methods, tax/compliance infrastructure, and entitlement recovery

### 4.9 Internal Testing vs Closed Testing vs Production

These are Play release tracks:

- `Internal testing`: fastest, small group, ideal for us while wiring real billing/auth
- `Closed testing`: larger controlled group, often required before production for new personal accounts
- `Production`: public release

Why not jump straight to production:

- billing and sign-in often need Play-distributed builds to behave exactly like production
- Google may block new publishers from production until testing evidence exists

## 4.10 Short Glossary

| Term | Meaning |
|---|---|
| `AAB` | Android App Bundle, the release artifact uploaded to Play |
| `APK` | The installable Android package generated from a bundle or built locally |
| `OAuth` | Google’s user-consent and account-access mechanism |
| `Scope` | A permission boundary requested from the user during OAuth |
| `SHA-1 / SHA-256` | Public certificate fingerprints used for trust/config matching |
| `License tester` | A Google account allowed to test Play Billing safely |
| `Crashlytics` | Firebase’s crash and ANR reporting tool |
| `App Data Folder` | Hidden Drive storage reserved for app-specific files |

## 4.11 Common Beginner Misunderstandings

### “If it works in debug, it will work in production.”

Not necessarily.

Why:

- debug installs use the debug certificate
- Play installs use the Play signing certificate
- Google APIs may reject one while accepting the other if fingerprint setup is incomplete

### “Firebase and Google Cloud are the same thing.”

Not quite.

Better mental model:

- Google Cloud is the broader infrastructure project
- Firebase is the mobile-focused layer on top of it

### “`google-services.json` is a secret key.”

No.

It is app configuration, not a service-account private key. It still deserves care, but it is not the same class of secret as a private credential export.

## 5. Recommended Ownership Setup

Use one Google account that you are comfortable treating as the long-term owner of Phone Down.

Best practice:

- enable 2-Step Verification
- add recovery options
- avoid temporary accounts
- use an email you will still control a year from now

Why this matters:

- Play Console ownership is operationally important
- losing access can become a business problem, not just a technical problem

## 6. Personal vs Organization Developer Account

Google Play lets you register as either a personal developer or an organization.

### Recommendation for You

If you are publishing as an individual right now, use a personal account.

Why:

- faster setup
- fewer business-verification requirements up front
- enough for a solo launch

Tradeoff:

- Google may require a closed test with at least 12 opted-in testers for 14 continuous days before production access

Use an organization account only if:

- you already operate under a real company identity
- you want the publisher name to be that organization
- you are ready for organization verification

## 7. Step 1: Create the Play Console Developer Account

Open:

[Google Play Console signup](https://play.google.com/console/signup)

### What To Do

1. Sign in with the Google account you want as the long-term owner.
2. Choose `Personal` or `Organization`.
3. Pay the one-time developer registration fee.
4. Complete identity verification if prompted.
5. Fill in developer profile details.
6. Confirm the public-facing developer name.
7. Add a reliable developer contact email.
8. Finish any device or security verification Google asks for.

### Why Each Part Exists

- account type: determines legal identity and sometimes release path
- fee: reduces spam and fake publishers
- identity verification: helps Google trust the publisher
- developer profile: gives users and Google a support contact
- public developer name: this is what users may see in the Play Store

### What To Keep Private

Never put any of these into the repo:

- payment receipts
- recovery codes
- identity documents
- screenshots of verification info

### Completion Checklist

- [ ] Play Console account exists
- [ ] registration fee paid
- [ ] account verified
- [ ] developer profile complete

### What Success Looks Like

At the end of this step, you should be able to open Play Console and see the main dashboard without any blocking prompts telling you to finish account verification or payment setup.

## 8. Step 2: Create the Phone Down App Shell in Play Console

Inside Play Console:

1. Click `Create app`.
2. Use these values:

| Field | Value |
|---|---|
| App name | Phone Down |
| Default language | English |
| App or game | App |
| Free or paid | Free |
| Contains ads | No |

Important package identity:

```text
phonedown.app
```

### Why These Values Matter

`Free`:

- correct because the app itself is free to install
- subscriptions and lifetime Pro happen via in-app purchases

`No ads`:

- must match real app behavior
- this affects policy declarations

`App`:

- ensures the app is categorized correctly for Play flows

### Theory: Why “Free” Is Still Right If We Charge for Pro

Google Play distinguishes between:

- the price to install the app
- digital products sold inside the app

Phone Down should be free to install because:

- the core ritual should be accessible
- Pro is an upgrade, not the install price

### Completion Checklist

- [ ] Phone Down app created in Play Console
- [ ] app marked free
- [ ] ads set to no

### What This Unlocks Later

Once the Play app shell exists:

- billing products can be attached to the app
- testing tracks can be created
- release bundles can be uploaded later
- policy forms now have a real app home inside Play Console

## 9. Step 3: Enable Play App Signing and Understand Keys

Google Play App Signing should be enabled for Phone Down.

### The Theory

Android apps are signed. Signing proves the app really came from the same publisher identity as earlier versions.

There are two practical keys in our setup:

1. `Upload key`
   - your local key
   - used only to upload bundles to Play
2. `Play App Signing key`
   - managed by Google Play
   - used by Google when distributing builds to users

Why this split exists:

- Google can manage final distribution signing securely
- you can still upload builds safely
- if your upload key is ever lost, Google has recovery processes

### Why This Is Needed for Sign-In

Google Sign-In and other Google APIs validate the package name plus certificate fingerprint.

That means we must eventually register:

- debug key fingerprints
- upload key fingerprints
- Play App Signing fingerprints

Otherwise a Play-installed build can fail even if the local debug build works.

## 10. Step 4: Generate the Upload Keystore

This happens on your machine, not inside the repo.

### Recommended Location

```bash
mkdir -p "$HOME/.android/phone-down-release"
```

### Command

```bash
keytool -genkeypair \
  -v \
  -keystore "$HOME/.android/phone-down-release/phone-down-upload.jks" \
  -alias phone-down-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### What the Fields Mean

- `keystore`: the file containing the signing key
- `alias`: the name of the specific key entry inside the keystore
- `RSA 2048`: standard modern signing strength
- `validity 10000`: long-lived key validity so it remains useful for years

### What You Will Be Asked

`keytool` may ask for:

- keystore password
- key password
- name and organization details

Use strong passwords and store them in your password manager.

### Critical Safety Rule

Do not:

- put the `.jks` file in the repo
- paste passwords into chat
- commit a `keystore.properties` file with real secrets

### Why Password Hygiene Matters Here

The upload keystore is long-lived release infrastructure, not a throwaway convenience file.

If you lose it or lose the password:

- future uploads become much harder

If you leak it along with the password:

- someone could potentially upload builds as you

### Next Step After Generation

Run:

```bash
keytool -list -v \
  -keystore "$HOME/.android/phone-down-release/phone-down-upload.jks" \
  -alias phone-down-upload
```

Record only:

- SHA-1
- SHA-256

in [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md).

## 11. Step 5: Create a Dedicated Google Cloud and Firebase Project

Recommendation:

- create a new project for Phone Down
- do not reuse `only-yours`

### Suggested Names

| Item | Recommended Value |
|---|---|
| Google Cloud project name | `phone-down` |
| Firebase project name | `phone-down` |
| Android app nickname | `Phone Down Android` |
| OAuth app name | `Phone Down` |

### Why a Dedicated Project Is Better

It keeps:

- OAuth branding clean
- API scopes product-specific
- quotas and logs easier to reason about
- Firebase crashes isolated to this app
- future maintenance less tangled

### Beginner Mental Model

Think of this project as the “Google-side home” for Phone Down. It is where Google learns:

- what this app is
- what APIs it may call
- what user consent screen it should show
- what signed Android builds belong to it

## 12. Step 6: Set Up Firebase

Open:

[Firebase Console](https://console.firebase.google.com/)

### What To Do

1. Create project `phone-down`.
2. Decide whether to enable Google Analytics.

Recommendation:

- leave Analytics disabled for now unless you have a clear analytics plan

Why:

- Crashlytics does not require product analytics to be useful
- keeping the data footprint smaller matches the app’s privacy posture

3. Add Android app.
4. Use package name:

```text
phonedown.app
```

5. Use nickname:

```text
Phone Down Android
```

6. Add the debug SHA-1 and SHA-256 from [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md).
7. Later add upload key SHA values.
8. After the first Play upload, add Play App Signing SHA values from Play Console.
9. Download `google-services.json`.

### What `google-services.json` Is

It is a Firebase configuration file that tells the Android app which Firebase/Google project it belongs to.

Important:

- it is configuration, not a service-account private key
- it is still safer to treat as local config until we intentionally decide how to manage it

### Safety Rules

- `google-services.json` is ignored by the repo right now
- do not download service-account private key JSON
- do not create a server-style credential flow for this mobile app unless we explicitly need one later

### Completion Checklist

- [ ] Firebase project created
- [ ] Android app `phonedown.app` added
- [ ] debug SHA values added
- [ ] `google-services.json` downloaded locally

### Common Mistakes In This Step

- adding the wrong package name
- forgetting the debug SHA values
- downloading the wrong kind of credential file
- assuming Firebase setup alone automatically enables Drive API or OAuth consent

## 13. Step 7: Enable Google Drive API

Open the Google Cloud project:

[Google Cloud Console](https://console.cloud.google.com/)

Then:

1. open `APIs & Services`
2. open `Library`
3. search for `Google Drive API`
4. click `Enable`

### Why This Is Needed

Even if the app has Google Sign-In, it cannot call Drive unless the Drive API is enabled in the project.

Think of this as turning on a capability inside Google Cloud.

### Which Drive Access We Want

Only:

```text
https://www.googleapis.com/auth/drive.appdata
```

Why:

- it gives access only to hidden app data
- it avoids frightening users with broad Drive access
- it matches the product need exactly

## 14. Step 8: Configure OAuth Consent Screen

Still in Google Cloud:

1. open `APIs & Services`
2. open `OAuth consent screen`
3. configure the app

### Recommended Values

| Field | Recommended Value |
|---|---|
| App name | Phone Down |
| User type | External |
| Support email | your real support/contact email |
| Developer contact email | your real developer/contact email |

### Why “External”

`External` means people outside a private organization can use the sign-in flow.

Phone Down is a public consumer app, so `External` is the right direction.

### App Status: Testing vs Published

Before production, OAuth apps often remain in testing mode.

If the consent screen is in testing mode:

- only listed test users can sign in
- this is normal during development

That is why the guide also asks you to add test users.

### Scopes To Add

- `openid`
- `email`
- `profile`
- `https://www.googleapis.com/auth/drive.appdata`

### Why Each Scope Exists

`openid`:

- standard identity foundation

`email`:

- lets us show the signed-in Google email in the account UI

`profile`:

- lets us show display name/avatar if desired

`drive.appdata`:

- lets the app write and read hidden backup data

### Why We Avoid Broad Drive Scopes

Broad scopes increase:

- user concern
- consent friction
- review risk
- blast radius if the app is misconfigured

The narrowest scope that does the job is the best product and security choice.

### How To Think About OAuth As Product Design

From the user’s point of view, the consent screen is a trust moment, not just a technical permission prompt.

The user is silently asking:

- do I understand what this app wants?
- does this request feel proportional?
- does the app look legitimate?

That is why good OAuth setup is partly security work and partly product design.

### Test Users

If OAuth is still in testing mode:

1. add your own Google account
2. add any other real testers who need sign-in

Why this is needed:

- otherwise Google blocks those accounts from completing sign-in

## 15. Step 9: Add SHA Fingerprints in the Right Places

This part trips up many Android releases, so it is worth being very explicit.

### Add These in Firebase / Google Config

1. Debug SHA values
   - for local Android Studio / adb installs
2. Upload key SHA values
   - for your signed uploads
3. Play App Signing SHA values
   - for builds downloaded from Play tracks

### Why Three Contexts Exist

Your local device and Google Play do not sign the app with the same certificate.

So the same codebase can behave like three slightly different installed apps from Google’s perspective.

If debug works but Play internal testing fails, missing Play signing SHA values are a common culprit.

### Quick Mental Model

Think of fingerprint setup as a matrix:

| Install Context | Certificate Used | Must Be Registered? |
|---|---|---|
| Local debug install | Debug certificate | Yes |
| Locally signed upload artifact | Upload certificate | Yes |
| Play-installed app | Play App Signing certificate | Yes |

This one table explains a huge amount of Android auth confusion.

## 16. Step 10: Set Up Play Billing Products

Inside Play Console, billing setup may be under monetization/in-app products/subscriptions depending on the UI version.

### Recommended Product Structure

Preferred:

| Type | Product ID | Price |
|---|---|---:|
| Subscription | `pro` with base plan `pro-monthly` | INR 99/month |
| Subscription | `pro` with base plan `pro-yearly` | INR 799/year |
| One-time product | `pro_lifetime` | INR 1,999 |

Fallback if separate subscription products are easier:

| Type | Product ID | Price |
|---|---|---:|
| Subscription | `pro_monthly` | INR 99/month |
| Subscription | `pro_yearly` | INR 799/year |
| One-time product | `pro_lifetime` | INR 1,999 |

### Why This Structure Makes Sense

Monthly:

- low-friction entry
- good for students and people testing the app

Yearly:

- better long-term value
- encourages commitment without being too expensive in India

Lifetime:

- strong option for users who dislike subscriptions
- helps conversion for users who want a one-time unlock

### Why We Still Need Play Billing Even for Lifetime

The lifetime plan is still a digital in-app product, so it should go through Google Play Billing on Android.

### Billing Setup Checklist

- [ ] developer payment profile ready
- [ ] product IDs created
- [ ] products active
- [ ] prices set
- [ ] test accounts prepared

## 17. Step 11: Create Testing Tracks

### Internal Testing

Create this first.

Why:

- fastest to distribute
- best for us while wiring billing, sign-in, and backup
- usually enough to test Play-distributed behavior quickly

Use internal testing for:

- verifying Play installation
- product loading
- test purchases
- sign-in under Play signing
- crash reporting from release builds

### Closed Testing

Create this next, especially if your Play account is new and personal.

Why:

- Google may require proof of real testing before granting production access
- it is part of the trust-building path for new publishers

If required:

- recruit at least 12 opted-in testers
- keep the test live for at least 14 continuous days

## 18. Step 12: Store Listing and Policy Work

Google Play does not only check whether the app works. It also checks whether the listing and policy declarations match reality.

### Listing Assets We Need

- app icon
- feature graphic
- screenshots
- short description
- full description
- privacy policy URL
- support email

### Policy Areas We Need To Complete

- Data Safety
- content rating
- target audience
- app access instructions if needed
- ads declaration
- permissions explanation
- subscription disclosures

### Why This Matters

Even a technically good app can be delayed or rejected if:

- permissions are unclear
- billing disclosures are incomplete
- the privacy policy does not mention real data flows
- the listing says one thing and the app does another

### What Google Review Is Really Looking For

A helpful way to understand review is this: Google is trying to filter out apps that are misleading, unsafe, or hard for users to understand.

That means reviewers care a lot about alignment:

- does the permission request match the feature?
- does the privacy policy match actual behavior?
- do the billing surfaces clearly explain what is being sold?
- do store listing claims match what the app can really do?

## 19. Step 13: Internal Release Upload

After code integration begins and release signing is wired:

1. build a signed release AAB
2. upload it to internal testing
3. add testers
4. install from the Play opt-in link

### Why We Need a Play-Installed Build

Some behaviors only truly prove themselves when the app is installed through Play:

- billing products
- Play signing SHA behavior
- release Crashlytics
- subscription restore behavior

Local debug builds are necessary, but not enough.

### Why Internal Testing Comes Before Closed Testing

Internal testing is our fast feedback loop.

It is where we catch issues like:

- Billing products do not load
- Sign-In works in debug but fails in Play
- Crashlytics is not receiving release crashes

Closed testing is broader and slower. Internal testing should give us confidence before we ask more people to spend time testing.

## 20. Local Commands You Will Use Later

Debug build:

```bash
./gradlew --no-configuration-cache :app:assembleDebug
```

Release bundle:

```bash
./gradlew --no-configuration-cache :app:bundleRelease
```

Key fingerprints:

```bash
keytool -list -v \
  -keystore "$HOME/.android/phone-down-release/phone-down-upload.jks" \
  -alias phone-down-upload
```

## 21. Secrets Safety Rules

This matters enough to say plainly.

Safe to share back with me:

- project ID
- yes/no setup statuses
- SHA-1 and SHA-256 fingerprints
- product IDs
- whether `google-services.json` is downloaded locally

Not safe to share:

- keystore passwords
- keystore files
- service-account JSON
- OAuth client secrets
- access tokens
- recovery codes
- private identity verification docs

### Why We Are Strict About This

Release work tends to generate exactly the kinds of files people accidentally leak:

- credential exports
- keystores
- copied setup files
- screenshots containing private information

The repo already ignores many risky patterns, but process still matters. If a file could let someone impersonate you or access private data, keep it out of git and out of chat.

## 22. Exactly What To Send Back After Setup

After you complete the console steps, reply with only this:

```text
Play Console:
- Developer account type:
- Play app created: yes/no
- Play App Signing enabled: yes/no
- Closed testing required: yes/no/unknown

Google Cloud/Firebase:
- Project ID:
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

## 23. Fastest Path For You Right Now

If you want the shortest “do this now” version, do these in order:

1. Create the Play Console account.
2. Create the Phone Down app shell.
3. Create the `phone-down` Firebase/Google Cloud project.
4. Add Android app `phonedown.app`.
5. Add the debug SHA values from [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md).
6. Enable Drive API.
7. Configure OAuth consent and add test users.
8. Generate the upload keystore outside the repo.
9. Record upload SHA values in [docs/phase-16-console-setup-info.md](/Users/ayushjaipuriar/Documents/GitHub/phone-down/docs/phase-16-console-setup-info.md).
10. Create the Play Billing products.

That gets us to the point where I can safely start wiring real Google Sign-In in code.

## 24. Checkpoint Questions

Use these to test your understanding while you work.

### After Play Console App Creation

- Why is Phone Down a free app even though Pro is paid?
- Why does the Play app shell need to exist before billing products can be attached cleanly?

### After Keystore Generation

- What is the difference between the upload key and the Play App Signing key?
- Why are SHA fingerprints safe to record while passwords are not?

### After Firebase and OAuth Setup

- Why do we need both the package name and the signing fingerprints for Google Sign-In?
- Why is `drive.appdata` a better scope for Phone Down than broad Drive access?

### After Billing Setup

- Why do product IDs live in Play Console instead of only in code?
- Why do we still need Play-installed builds after local debug testing?
