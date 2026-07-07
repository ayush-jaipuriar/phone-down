# Play Store Data Safety Form

This document provides the information needed to complete the Google Play Data Safety form for Phone Down.

## App Information

- **App Name**: Phone Down
- **Developer**: Phone Down Team
- **Contact**: support@phonedown.app
- **Privacy Policy**: See `docs/privacy-policy.md`

## Data Collection and Sharing

### Does your app collect or share any of the required user data types?

**Answer**: Yes

### Is all of the user data collected by your app encrypted in transit?

**Answer**: Yes

### Do you provide a way for users to request that their data is deleted?

**Answer**: Yes

## Data Types

### Location

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Approximate location | No | N/A | N/A | N/A |
| Precise location | No | N/A | N/A | N/A |

### Personal Info

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Name | No | N/A | N/A | N/A |
| Email address | Yes (optional) | No | App functionality, Account management | Optional |
| User IDs | Yes | No | App functionality, Account management | Required |
| Address | No | N/A | N/A | N/A |
| Phone number | No | N/A | N/A | N/A |
| Race and ethnicity | No | N/A | N/A | N/A |
| Political or religious beliefs | No | N/A | N/A | N/A |
| Sexual orientation | No | N/A | N/A | N/A |
| Other personal info | No | N/A | N/A | N/A |

**Notes**: Email address is only collected if user opts into Google Sign-In for backup. User ID is the app's internal session identifier.

### Financial Info

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| User payment info | No | N/A | N/A | N/A |
| Purchase history | Yes | No | App functionality | Required |
| Credit score | No | N/A | N/A | N/A |
| Other financial info | No | N/A | N/A | N/A |

**Notes**: Purchase history is handled entirely by Google Play Billing. The app only knows whether a purchase was successful.

### Health and Fitness

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Health info | No | N/A | N/A | N/A |
| Fitness info | No | N/A | N/A | N/A |

### Messages

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Emails | No | N/A | N/A | N/A |
| SMS or MMS | No | N/A | N/A | N/A |
| Other in-app messages | No | N/A | N/A | N/A |

### Photos and Videos

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Photos | No | N/A | N/A | N/A |
| Videos | No | N/A | N/A | N/A |

### Audio Files

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Voice or sound recordings | No | N/A | N/A | N/A |
| Music files | No | N/A | N/A | N/A |
| Other audio files | No | N/A | N/A | N/A |

### Files and Docs

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Files and docs | No | N/A | N/A | N/A |

### Calendar

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Calendar events | No | N/A | N/A | N/A |

### Contacts

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Contacts | No | N/A | N/A | N/A |

### App Activity

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| App interactions | Yes | No | Analytics, App functionality | Required |
| In-app search history | No | N/A | N/A | N/A |
| Installed apps | No | N/A | N/A | N/A |
| Other user-generated content | Yes | No | App functionality | Required |
| Other actions | No | N/A | N/A | N/A |

**Notes**: App interactions include focus session data (duration, completion status, interruptions). Other user-generated content refers to user settings and preferences.

### Web Browsing

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Web browsing history | No | N/A | N/A | N/A |

### App Info and Performance

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Crash logs | Optional | No | Analytics | Optional |
| Diagnostics | Optional | No | Analytics | Optional |
| Other app performance data | No | N/A | N/A | N/A |

**Notes**: Release builds use Firebase Crashlytics for crash logs and basic diagnostics. Debug builds disable Crashlytics collection. Crash diagnostics are used only for app stability and must not include direct personal contact details, Google access tokens, purchase tokens, raw backup payloads, or full session database contents.

### Device or Other IDs

| Data Type | Collected? | Shared? | Purpose | Required? |
|---|---|---|---|---|
| Device or other IDs | No | N/A | N/A | N/A |

## Data Handling

### Data Encryption in Transit

**Answer**: Yes

**Details**: All network communications use HTTPS with TLS 1.2+. This includes:
- Google Play Billing API
- Google Sign-In OAuth
- Google Drive API (for backup)
- Firebase Crashlytics (for release crash diagnostics)

### Data Deletion Request Mechanism

**Answer**: Yes

**Details**: Users can delete their data through the app:
- **Settings > Privacy > Delete All Local Data**: Removes all sessions, settings, and preferences
- **Account > Sign Out**: Disconnects Google account
- **Delete All Local Data** includes option to delete cloud backup if present

### Review Data Safety Practices

**Answer**: Yes

**Details**: 
- All data collection is documented in this form
- Privacy policy is available in the app and online
- Users are informed about data usage through onboarding
- Backup is opt-in, not opt-out

## Security Practices

### Account Security
- Google Sign-In uses OAuth 2.0 with PKCE
- Auth tokens stored in encrypted preferences
- Tokens are scoped to Drive app data folder only

### Payment Security
- All purchases handled by Google Play Billing
- No payment information stored by the app
- Purchase verification through Google Play server

### Backup Security
- Backups stored in user's personal Google Drive app data folder
- Only accessible by Phone Down app
- Encrypted in transit using TLS
- Android OS automatic backup is disabled; Phone Down uses explicit in-app Google Drive backup and restore.

## Compliance

### GDPR Compliance
- Data minimization: only necessary data collected
- Purpose limitation: data used only for stated purposes
- Storage limitation: data deleted upon user request
- Transparency: privacy policy clearly explains data usage
- User rights: access, deletion, and portability supported

### CCPA Compliance
- Users can request deletion of their data
- Users can opt out of data collection (by not using optional features)
- Privacy policy explains what data is collected and why

### COPPA Compliance
- App is not directed at children under 13
- No personal information collected from children
- If inadvertent collection discovered, data will be deleted promptly
