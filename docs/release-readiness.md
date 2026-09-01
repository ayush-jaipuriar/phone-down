# Release Readiness - Phone Down 1.0.4

Date: August 31, 2026
Version code: 5
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
- [ ] Replace every active billing-enabled Play artifact with version code 5 before submitting Data Safety answers that declare no financial data.

## Automated verification gates

- [x] `./scripts/check.sh`
- [x] Full JVM/unit regression suite
- [x] Compose Android-test APK assembly
- [x] All Paparazzi screenshot verification
- [x] Debug build
- [x] Signed release AAB build and bundle validation
- [x] Release dependency proof shows no BillingClient
- [x] Debug/release merged-manifest proof matches Crashlytics policy

Release evidence captured on 2026-08-31:

- Bundle: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`
- Bundletool 1.18.3 validation: pass
- Package/version: `phonedown.app`, version code 5, version name 1.0.4
- SDK boundary: minimum 26, target 36
- Release APK signature verification: pass, one signer
- Release runtime dependency query: no matching BillingClient dependency
- Release manifest: no `com.android.vending.BILLING` permission
- AAB SHA-256: `57386468f03d3d9271e52502cb7894f6b93dbf1b42cc45793fb202b0a444d4e6`
- APK SHA-256: `ec56626c29e57e1b20f1e7c8a69cfb77accba8bb3878d26dd40a7ca682d2e4a9`

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

- [ ] Closed-test countries/regions, tester list, feedback path, and version-code 5 release are configured.
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
