# Release Readiness - Phone Down 1.0.5

Date: September 4, 2026
Version code: 6
Target: public, completely free Google Play release

## Product status

- [x] Focus timer counts only while the phone is face down and stable.
- [x] Completion summary, clean/broken results, recovery, and explicit end flows are implemented.
- [x] Default duration is editable; one-time custom durations do not overwrite it.
- [x] History, advanced insights, custom durations, Drive backup, and CSV export are included without purchase.
- [x] Google sign-in and Drive backup are optional.
- [x] No ads, subscriptions, in-app purchases, upgrade actions, or purchase restoration are present in the release runtime.
- [x] Release Crashlytics is enabled; debug collection is disabled.

## Privacy and store status

- [x] In-app and hosted privacy policies describe Google profile data, Drive backup, Crashlytics, export, and deletion.
- [x] Data Safety answer sheet matches the free runtime.
- [x] Support email and public deletion instructions use real values.
- [x] Store listing describes all features as included.
- [x] Play listing icon and launcher icon use the intended Phone Down mark.
- [x] Closed-test draft contains only the public-free version-code 6 artifact; version code 5 was removed after replacement.

## Automated verification gates

- [x] `./scripts/check.sh`
- [x] Full JVM/unit regression suite
- [x] Compose Android-test APK assembly
- [x] All Paparazzi screenshot verification
- [x] Debug build
- [x] Signed release AAB build and bundle validation
- [x] Release dependency proof shows no BillingClient
- [x] Debug/release merged-manifest proof matches Crashlytics policy

Release evidence refreshed on 2026-09-04:

- Bundle: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`
- Bundletool 1.18.3 validation: pass
- Package/version: `phonedown.app`, version code 6, version name 1.0.5
- SDK boundary: minimum 26, target 36
- Release APK signature verification: pass, one signer
- Release runtime dependency query: no matching BillingClient dependency
- Release manifest: no `com.android.vending.BILLING` permission
- Foreground-service policy: active focus sessions use `specialUse` with the required subtype explanation; the incorrect `dataSync` declaration is absent
- AAB SHA-256: `7a72957ef8be4eb0c288e5017b3a343031c00996bddac3ff81d3696e97d2a8c5`
- APK SHA-256: `872f44893bc6e9064a56fa789f87ef0a211c877d40af92cd8a3582c4a2dbee1d`

## Physical-device and Play-installed gates

- [ ] Focus start, face-down arming, pause, resume, completion, summary, and Done.
- [ ] Clean, broken, abandoned, call-pause, and recovery paths.
- [ ] Default/custom duration behavior.
- [ ] Insights and CSV document export.
- [ ] Google sign-in, backup, restore, auto-backup, sign-out, and deletion.
- [ ] Privacy policy scrolling and links.
- [ ] Release Crashlytics test event appears without sensitive custom data.
- [ ] Play-installed sanity test passes from the closed-test track.

## External Play gates

- [x] Closed-test countries/regions, tester list, feedback path, and version-code 6 release draft are configured.
- [ ] Preview and confirm the closed-test release, then send it to Google for review.
- [ ] Required testers opt in and remain enrolled for Google's required duration.
- [ ] Production access becomes available and is approved.
- [ ] App content, Data Safety, content rating, target audience, ads, access, and privacy URL are complete.
- [ ] Production submission is reviewed immediately before the final consequential click.

## Current release boundary

Local engineering can produce and validate the release candidate. Public production access remains externally gated by Play's closed-testing requirements and cannot be claimed complete until the Console shows eligibility.

Physical-device instrumentation passed on 2026-09-01 using an RMX3853 running
Android 16. The full connected suite covered database, account, Focus, Insights,
Pro, and Settings behavior. A live smoke pass also covered onboarding and the
main public-free screens without exposing monetization actions. The broader
manual device matrix remains open, including timed sensor behavior, Google
sign-in and backup, process death, reboot recovery, and offline restoration.

Use `docs/public-free-release-qa.md` for evidence capture and `docs/phase-16-console-setup-info.md` for Console configuration history.
