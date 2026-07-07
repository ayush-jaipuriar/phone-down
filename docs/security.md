# Security Documentation for Phone Down

## Threat Model

### Assets We Protect
1. **User session data**: Focus session history, penalty events, and insights
2. **User settings**: App preferences and configuration
3. **Authentication tokens**: Google Sign-In tokens (when integrated)
4. **Purchase data**: Pro subscription status
5. **Crash diagnostics**: Release crash logs and basic stability metadata

### Threats We Address
- **Data at rest**: Device theft or unauthorized access
- **Data in transit**: Network interception during backup/sync
- **Tampering**: Modified APK or debugging attempts
- **Information disclosure**: Sensitive data in logs or memory

### Threats We Acknowledge But Don't Block
- **Rooted devices**: We warn users but don't block functionality
- **Emulators**: Detected but allowed for development
- **Memory dumps**: No runtime memory protection in V1
- **Screenshots**: No screenshot prevention for sensitive screens

## Security Measures Implemented

### Data Storage
- **Local-first**: All session data stored locally on device
- **Encrypted preferences**: Auth tokens use encrypted DataStore (prepared for V1, active when real auth integrated)
- **Database**: Room database with parameterized queries (SQL injection prevention)

### Network Security
- **TLS 1.2+**: All network communications use HTTPS
- **Certificate pinning**: Configured for Google APIs with fallback to system CAs
- **Network security config**: XML-based configuration in `res/xml/network_security_config.xml`
- **Cleartext disabled**: No HTTP traffic allowed

### Anti-Tampering
- **Root detection**: Warns users if device is rooted (`SecurityUtils.isDeviceRooted()`)
- **Emulator detection**: Identifies emulator environments (`SecurityUtils.isRunningOnEmulator()`)
- **Signature verification**: Verifies app signature at runtime (`SecurityUtils.verifyAppSignature()`)
- **Debug flag check**: `SecurityUtils.isDebugBuild()` distinguishes debug/release

### Code Obfuscation
- **ProGuard/R8**: Enabled for release builds
- **Model classes preserved**: For serialization and DI
- **Logging removed**: Debug logs stripped in release builds

### Secure Logging
- **Redaction**: Emails, tokens, and session IDs automatically redacted
- **No sensitive data**: Auth tokens, PII never logged in plaintext
- **Release stripping**: All debug logging removed by ProGuard

### Crash Reporting
- **Crashlytics release-only collection**: Firebase Crashlytics collection is disabled for debug builds and enabled for release/internal testing builds.
- **No sensitive keys**: Crash reports must not attach Google account email, Google account ID, access tokens, purchase tokens, backup payloads, or raw session database content.
- **Purpose limitation**: Crash diagnostics are used only for stability diagnosis.

### Data Deletion
- **Complete local wipe**: Sessions, penalties, settings all cleared
- **Cloud backup deletion**: Optional removal of Google Drive backup
- **Account sign-out**: Automatic disconnection when deleting data
- **Confirmation required**: User must type "DELETE" to confirm
- **Android OS backup disabled**: App-managed Google Drive backup is the only restore path for V1.

## Known Limitations (V1)

### External Configuration Still Required
- Google Sign-In, Drive backup, Play Billing, and Crashlytics depend on correct Play Console, Firebase, OAuth, and signing-fingerprint setup.
- Play-installed QA remains required before broader release because external trust configuration can fail even when local code builds.

### Certificate Pinning
- Pins are placeholders (`AAAAAAAA...`) and must be replaced before release
- Fallback to system CAs is enabled to prevent certificate expiry outages
- Certificate rotation monitoring required

### Database Encryption
- Session database is not encrypted in V1
- Acceptable risk: session data is app-generated timing data, not user PII
- Full database encryption planned for V2

### No Runtime Protection
- No anti-debugging measures
- No memory dump protection
- No screenshot prevention
- No certificate transparency checks

## Incident Response Procedure

### If a Security Issue is Discovered
1. **Assess severity**: Data breach vs. local-only vulnerability
2. **Isolate**: Disable affected feature if possible
3. **Fix**: Implement patch with minimal changes
4. **Test**: Verify fix doesn't introduce regressions
5. **Deploy**: Release update through Google Play
6. **Notify**: Update users if data was potentially exposed

### Responsible Disclosure
- Report security issues to: security@phonedown.app (placeholder)
- Allow 90 days for fix before public disclosure
- Acknowledge reporters in release notes (with permission)

## Security Audit Checklist

- [x] No hardcoded API keys or secrets in code
- [x] No logging of PII or session data in plaintext
- [x] TLS 1.2+ enforced for all network calls
- [x] Certificate pinning configured (with fallback)
- [x] Root detection enabled (warning only)
- [x] App signature verification implemented
- [x] Encrypted storage prepared for tokens
- [x] SQL injection prevention (Room parameterized queries)
- [x] ProGuard/R8 obfuscation enabled for release
- [x] Secure random for IDs (`SecureRandomUtils`)
- [x] Network security config XML defined
- [x] Cleartext traffic disabled
- [x] Debug logs stripped in release builds
- [x] Android OS automatic backup disabled
- [x] Crashlytics collection disabled for debug builds

## Files Related to Security

| File | Purpose |
|---|---|
| `app/src/main/java/phonedown/app/security/SecurityUtils.kt` | Root detection, emulator detection, signature verification |
| `app/src/main/java/phonedown/app/security/SecureLogger.kt` | Redacted logging, no PII in logs |
| `app/src/main/java/phonedown/app/security/CertificatePinningConfig.kt` | Certificate pin definitions |
| `app/src/main/res/xml/network_security_config.xml` | Android network security configuration |
| `app/proguard-rules.pro` | ProGuard/R8 obfuscation rules |
| `app/src/main/AndroidManifest.xml` | Backup policy and Crashlytics collection flag |
| `core/common/src/main/kotlin/phonedown/core/common/SecureRandomUtils.kt` | Cryptographically secure random generation |
| `core/datastore/src/main/kotlin/phonedown/core/datastore/security/EncryptedDataStore.kt` | Encrypted preferences wrapper (prepared) |

## Compliance

### Google Play Security Requirements
- [x] Target SDK 36 (latest)
- [x] Network security config defined
- [x] No cleartext traffic
- [x] Permissions justified in `docs/permissions.md`
- [x] Data safety form documented in `docs/play-store-data-safety.md`

### OWASP Mobile Top 10 (2024)
| Risk | Status | Mitigation |
|---|---|---|
| M1: Improper Credential Usage | N/A | No credentials stored (fake repos) |
| M2: Inadequate Supply Chain Security | Partial | ProGuard enabled, signature verification |
| M3: Insecure Authentication/Authorization | N/A | OAuth via Google (not yet integrated) |
| M4: Insufficient Input/Validation | Mitigated | Room parameterized queries |
| M5: Insecure Communication | Mitigated | TLS 1.2+, certificate pinning |
| M6: Inadequate Privacy Controls | Mitigated | Privacy policy, data deletion, local-first |
| M7: Binary Protection Issues | Partial | ProGuard, signature verification |
| M8: Security Misconfiguration | Mitigated | Network security config, no debug in release |
| M9: Insecure Data Storage | Partial | Encrypted prefs prepared, DB encryption deferred |
| M10: Insufficient Cryptography | Partial | SecureRandom for IDs, full crypto deferred |

## Next Steps for V2

1. Replace fake repositories with real integrations
2. Implement actual encrypted DataStore using `androidx.security:security-crypto`
3. Add SQLCipher for full database encryption
4. Replace certificate pinning placeholders with real pins
5. Add certificate transparency checks
6. Implement anti-debugging measures
7. Add screenshot prevention for sensitive screens
8. Enable biometric authentication for sensitive operations
