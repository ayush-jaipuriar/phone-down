package phonedown.app.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import phonedown.app.BuildConfig
import java.io.File

object SecurityUtils {

    fun isDeviceRooted(): Boolean {
        val testKeys = android.os.Build.TAGS?.contains("test-keys") ?: false
        val superuserApk = File("/system/app/Superuser.apk").exists()
        val suBinary = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
        ).any { File(it).exists() }

        return testKeys || superuserApk || suBinary
    }

    fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("google/sdk_gphone") ||
            Build.FINGERPRINT.lowercase().contains("generic") ||
            Build.FINGERPRINT.lowercase().contains("emulator") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.lowercase().contains("emulator") ||
            Build.MODEL.contains("Android SDK built for") ||
            Build.MANUFACTURER.contains("Google") && Build.BRAND.contains("google") && Build.DEVICE.startsWith("generic") ||
            Build.PRODUCT == "google_sdk" ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu"))
    }

    fun verifyAppSignature(context: Context): Boolean {
        return try {
            val packageName = context.packageName
            val packageManager = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                ).signingInfo?.hasMultipleSigners() == false
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures != null
            }
        } catch (e: Exception) {
            false
        }
    }

    fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }
}
