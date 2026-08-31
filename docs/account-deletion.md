---
title: Account and Data Deletion for Phone Down
permalink: /account-deletion/
---

# Account and Data Deletion for Phone Down

Last updated: August 30, 2026

Phone Down does not create a standalone username/password account. Google sign-in is optional and is used only for cloud backup and restore.

## Delete local data and disconnect Google

1. Open `Phone Down`.
2. Go to `Settings > Privacy`.
3. Tap `Delete All Local Data`.
4. Turn on `Also delete cloud backup` if you want to remove the Drive backup too.
5. Confirm the deletion flow.

The completed flow will:

- remove local focus sessions and interruption events
- reset app settings
- clear Phone Down's cached Drive authorization
- disconnect the Google account from Phone Down
- delete the app's backup from the Google Drive app data folder if you selected cloud-backup deletion

If cloud-backup deletion fails, Phone Down stops before deleting local data so you can retry without losing the connection needed to remove the backup.

## Data that can remain

- If you do not select `Also delete cloud backup`, the hidden backup can remain in your Google Drive app data folder until you reconnect the same account and delete it.
- Firebase Crashlytics can retain release crash reports and associated installation identifiers according to Firebase's retention policy. These reports are not part of your Phone Down account connection or Drive backup.

You can also revoke Phone Down's access from your Google Account settings.

## Need help

Email `jaipuriar.ayush@gmail.com` for help with account access or data deletion.
