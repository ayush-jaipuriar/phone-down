# Google Play Data Safety Declaration

This is the implementation-backed answer sheet for Phone Down's public free release. Re-check it against every active artifact before submitting the Play Console form because Google Play requires one global declaration covering all versions currently distributed.

Official references:

- [Google Play Data safety form guidance](https://support.google.com/googleplay/android-developer/answer/10787469)
- [Firebase Android data-disclosure guidance](https://firebase.google.com/docs/android/play-data-disclosure)

## App information

- **App name:** Phone Down
- **Support email:** `jaipuriar.ayush@gmail.com`
- **Privacy policy:** `https://ayush-jaipuriar.github.io/phone-down/privacy-policy/`
- **Release model:** public and completely free; no ads, paid app price, subscriptions, or in-app purchases

## Top-level answers

| Play Console question | Answer | Basis |
|---|---|---|
| Does the app collect or share required user-data types? | Yes | Optional Google account/Drive data and automatic release Crashlytics data leave the device. |
| Is collected data encrypted in transit? | Yes | Google Sign-In, Drive, and Firebase use HTTPS/TLS. |
| Does the app share user data with third parties? | No | Google services process data to provide app functionality or crash reporting; no data is sold or transferred for independent advertising/marketing use. Confirm this classification against the final Console wording. |
| Can users request data deletion? | Yes | In-app local/cloud deletion plus the public account-deletion page. |
| Does the app create a developer account? | No | Google sign-in connects an existing Google account; Phone Down creates no standalone account. |

## Data types to declare

Only off-device transmission counts as collection in this form. Local-only focus/session/settings data is not collection until the user enables Drive backup.

| Category | Data type | Collected | Required or optional | Purpose | Runtime source |
|---|---|---:|---|---|---|
| Personal info | Name | Yes | Optional | App functionality | Optional Google sign-in profile |
| Personal info | Email address | Yes | Optional | App functionality | Optional Google sign-in profile |
| Personal info | User IDs | Yes | Optional | App functionality | Google account identifier for optional account connection |
| Personal info | Other personal info | Yes | Optional | App functionality | Google profile-picture URL |
| App activity | App interactions | Yes | Optional | App functionality | Focus-session history included only when Drive backup is enabled |
| App activity | Other user-generated content | Yes | Optional | App functionality | App preferences included only when Drive backup is enabled |
| App info and performance | Crash logs | Yes | Required in release builds | Analytics | Firebase Crashlytics automatic crash reporting |
| App info and performance | Diagnostics | Yes | Required in release builds | Analytics | Crashlytics app/device/OS diagnostic metadata |
| Device or other IDs | Device or other IDs | Yes | Required in release builds | Analytics | Crashlytics installation UUID and Firebase installation ID |

For Play's form, **Analytics** includes monitoring app health and diagnosing crashes. Crashlytics collection is required for users of release artifacts because the release manifest enables it automatically; debug builds are irrelevant to the public declaration.

## Data types not collected

- Approximate or precise location
- Address or phone number
- Contacts
- Messages
- Photos or videos as user files
- Audio files
- Calendar data
- Health or fitness data
- Financial or payment data
- Purchase history
- Web-browsing history
- Installed-app inventory
- Advertising identifiers

## Handling details

### Google account and Drive

- Sign-in and Drive backup are optional.
- Backup uses the user's personal Google Drive app data folder.
- Phone Down requests the Drive app-data scope, not general access to the user's Drive files.
- Focus history and settings are transmitted only when the user invokes or enables backup.
- Drive access tokens are held in memory and cleared on sign-out or local-data deletion.

### Firebase Crashlytics

- Enabled automatically in release builds and disabled in debug builds.
- Automatically collects crash stack traces, relevant app state, relevant device metadata, a Crashlytics installation UUID, and data from transitive Firebase Installations/Sessions dependencies.
- Used only for app stability and crash diagnosis.
- Phone Down does not deliberately attach Google profile details, access tokens, raw backup payloads, or full database contents as custom Crashlytics data.

### Deletion

- `Settings > Privacy > Delete All Local Data` removes local focus/session data, resets settings, clears Drive authorization, and disconnects Google.
- The same flow can optionally delete the Drive backup.
- A cloud-deletion failure stops the operation before local data is cleared.
- Public instructions: `https://ayush-jaipuriar.github.io/phone-down/account-deletion/`
- Crash diagnostics follow Firebase's retention and deletion practices; they are not stored in the user's Drive backup.

## Submission verification

- [ ] No active Play artifact contains Google Play Billing or paid features.
- [ ] Release dependency graph contains no BillingClient.
- [ ] Release manifest enables Crashlytics; debug manifest disables it.
- [ ] Google Sign-In fields and Drive backup payload still match this declaration.
- [ ] In-app and hosted privacy policies match this declaration.
- [ ] Support email and deletion URL open successfully.
- [ ] Console preview is reviewed before submission.

## Explicit non-claims

Do not claim encrypted preferences, certificate pinning, payment security, purchase verification, advertising-ID collection, or a standalone Phone Down account. Those claims do not describe this release.
