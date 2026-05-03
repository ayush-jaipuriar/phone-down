# Release Readiness Report — Phone Down V1

## Status: Ready for Internal Testing

Date: May 3, 2026
Version: 1.0.0
Version Code: 1

## What's Complete

### Core Features
- [x] Focus session timer that only counts when phone is face-down
- [x] Sensor-based face-down detection with arming window
- [x] Interruption handling (minor, penalty, broken)
- [x] Call pause support
- [x] Session classification (clean, interrupted, broken, partial, invalidated)
- [x] Foreground service with notification
- [x] Session recovery after app kill/restart

### UI/UX
- [x] Focus screen with all states (waiting, arming, active, paused, completed)
- [x] Insights screen with today, weekly, focus quality, streak, history
- [x] Settings screen with theme, sound, haptics
- [x] Onboarding flow (3 cards)
- [x] Light and dark themes
- [x] Pro paywall with monthly/yearly/lifetime options
- [x] Privacy Policy screen

### Data & Persistence
- [x] Room database for sessions and penalty events
- [x] DataStore for settings
- [x] Backup/restore with JSON schema (v1)
- [x] Enhanced data deletion with confirmation

### Monetization
- [x] Pro entitlement system
- [x] Fake billing implementation (ready for real Play Billing)
- [x] Pro gates on advanced insights, backup, export
- [x] Passive upsell banner

### Security & Privacy
- [x] Privacy policy (GDPR/CCPA/COPPA compliant)
- [x] Permissions documentation
- [x] Play Store data safety form
- [x] Security hardening (root detection, certificate pinning, secure logging)
- [x] ProGuard/R8 obfuscation enabled
- [x] No secrets in code

### Testing
- [x] Session engine unit tests
- [x] Sensor evaluator tests
- [x] Insights use case tests (31 tests)
- [x] Database mapper and DAO tests
- [x] Settings ViewModel tests
- [x] Account ViewModel tests
- [x] Pro ViewModel tests
- [x] Paparazzi screenshot tests for all screens
- [x] Compose UI tests for Settings
- [x] Release build verification (AAB builds successfully)
- [x] Lint passes

### Assets
- [x] App icon (all densities)
- [x] Play Store feature graphic (1024x500)
- [x] Play Store icon (512x512)
- [x] Play Store listing metadata

## What's Deferred to Post-V1

### Real Service Integration
- [ ] Google Play Billing Client (fake implementation used)
- [ ] Google Sign-In (fake implementation used)
- [ ] Google Drive API for backup (fake implementation used)
- [ ] Auto-backup scheduling

### Security Enhancements
- [ ] Real encrypted DataStore (using `androidx.security:security-crypto`)
- [ ] Full database encryption (SQLCipher)
- [ ] Real certificate pinning (placeholders currently used)
- [ ] Anti-debugging measures
- [ ] Screenshot prevention for sensitive screens

### Missing Features
- [ ] Compose UI tests for Focus, Insights, Onboarding, Account, Pro
- [ ] Manual device testing matrix execution
- [ ] Post-session completion upsell teaser
- [ ] Subscription expiry handling
- [ ] Crash reporting integration
- [ ] Data export functionality (UI prepared, implementation deferred)

## Known Issues / Risks

1. **Fake repositories**: All external services use fake implementations. Swapping to real ones is the highest priority post-V1 task.
2. **Certificate pinning**: Placeholder pins must be replaced before production release.
3. **No manual device testing**: Physical device validation (sensor reliability, battery, etc.) has not been performed.
4. **Build-logic Gradle cache**: Intermittent hash mismatch issues require occasional cache cleaning.
5. **ProGuard**: Release build compiles but hasn't been tested on a device with obfuscation.

## Build Instructions

### Debug Build
```bash
export ANDROID_HOME=/Users/$USER/Library/Android/sdk
./gradlew :app:assembleDebug
```

### Release AAB
```bash
export ANDROID_HOME=/Users/$USER/Library/Android/sdk
./gradlew :app:bundleRelease
```

### Run Tests
```bash
./gradlew :app:testDebugUnitTest
./gradlew :domain:insights:test
./gradlew :feature:settings:testDebugUnitTest
```

### Lint
```bash
./gradlew :app:lintDebug
```

## Next Steps for Production Release

1. **Configure release signing**:
   - Generate/upload keystore
   - Create `keystore.properties` in project root
   - Update `app/build.gradle.kts` signing config
   - Never commit keystore or passwords

2. **Replace certificate pinning placeholders**:
   - Generate real SHA-256 pins for Google APIs
   - Update `CertificatePinningConfig.kt`
   - Update `network_security_config.xml`

3. **Integrate real services**:
   - Google Play Billing
   - Google Sign-In
   - Google Drive API

4. **Manual device testing**:
   - Test on 2+ physical devices
   - Document bugs in `docs/phase-14-bugs.md`
   - Fix critical bugs in follow-up sprint

5. **Play Store submission**:
   - Upload signed AAB to Play Console
   - Complete data safety form
   - Upload screenshots
   - Set content rating
   - Configure pricing and distribution

## Bug List

See `docs/phase-14-bugs.md` for documented bugs discovered during testing.

**Note**: No bugs have been documented yet because manual testing has not been performed.
