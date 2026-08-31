---
title: Privacy Policy for Phone Down
permalink: /privacy-policy/
---

# Privacy Policy for Phone Down

**Last Updated:** August 30, 2026

## Introduction

Phone Down ("we", "our", or "the app") is a focus and productivity application designed to help you stay present by encouraging you to place your phone face down during focus sessions.

This policy explains what data the app handles, why it is used, and the choices available to you.

## Data We Handle

### Data generated on your device

- **Focus session data:** session times, planned and focused duration, interruption events, completion result, and clean/broken status.
- **App settings:** theme, sound and haptic preferences, default timer duration, onboarding state, and backup preferences.

Focus session data and app settings are stored locally unless you choose to use Google Drive backup.

### Optional Google account data

If you choose to sign in with Google for backup and restore, Phone Down accesses and stores your Google display name, email address, profile-picture URL, and Google account identifier. Google handles authentication.

If you enable backup, Phone Down sends your backup data to the app data folder in your personal Google Drive. The app requests access only to its own Drive app data folder.

### Release crash diagnostics

Release builds automatically send crash reports and basic diagnostic metadata to Firebase Crashlytics. This can include crash stack traces, relevant app state, device and operating-system information, a Crashlytics installation identifier, and a Firebase installation identifier. We use this information only to diagnose crashes, ANRs, and stability problems.

Phone Down does not deliberately attach your Google account details, access tokens, raw Drive backup, or full session database to Crashlytics reports.

### Data we do not collect

- Location
- Contacts
- Call content, caller phone numbers, or call history
- Advertising identifiers
- Web-browsing history or activity outside Phone Down
- Payment information or purchase history

## How We Use Data

- Display focus history, summaries, streaks, and insights.
- Apply your settings and preferences.
- Provide optional backup and restore through your Google account.
- Diagnose and fix release crashes and stability problems.

We do not sell data, use it for advertising, or profile you for marketing.

## Storage and Security

- Focus sessions and app settings are stored locally on your device.
- Optional backups are stored in your personal Google Drive app data folder.
- Google account profile details are stored in app preferences.
- Google Drive access tokens are kept in memory and cleared when you sign out or delete local data.
- Network traffic to Google services uses HTTPS.
- Android OS automatic app-data backup is disabled; backup and restore happen only through Phone Down's explicit Google Drive feature.

Firebase states that Crashlytics retains crash stack traces and associated identifiers for 90 days before removal from live and backup systems begins. See [Privacy and Security in Firebase](https://firebase.google.com/support/privacy/).

## Optional Permissions

- **Notifications:** allows Phone Down to show the active focus-session notification.
- **Phone state:** allows Phone Down to pause focus automatically during a phone call. The app does not read or store caller numbers, call content, or call history.

## Your Choices

### Export

You can export your focus history as a CSV file from the Insights screen. You choose where the file is saved.

### Delete local data

Use `Settings > Privacy > Delete All Local Data` to remove local focus sessions and interruption events, reset settings, clear the in-memory Drive access token, and disconnect the Google account from Phone Down. You can also choose to delete the cloud backup during this flow.

If you do not choose cloud-backup deletion, the backup can remain in your Google Drive app data folder until you reconnect the same account and delete it.

### Revoke Google access

You can revoke Phone Down's access from your Google Account settings. Public deletion instructions are available at:

`https://ayush-jaipuriar.github.io/phone-down/account-deletion/`

## Children's Privacy

Phone Down is not intended for children under 13. We do not knowingly collect personal information from children under 13. Contact us if you believe this has occurred.

## Third-Party Services

- Google Sign-In for optional account access
- Google Drive for optional backup and restore
- Firebase Crashlytics for automatic release crash diagnostics

These services are governed by Google's privacy terms. Phone Down does not include advertising or Google Play Billing in this free release.

## Policy Changes

We may update this policy as the app changes. The current version will be posted here and reflected in the app with an updated date.

## Contact

- Email: `jaipuriar.ayush@gmail.com`
- In app: `Settings > About > Send Feedback`
