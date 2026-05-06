package phonedown.app.security

import android.util.Log
import phonedown.app.BuildConfig

object SecureLogger {
    private const val TAG = "PhoneDown"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, redactSensitiveData(message))
        }
    }

    fun i(message: String) {
        Log.i(TAG, redactSensitiveData(message))
    }

    fun w(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            Log.w(TAG, redactSensitiveData(message), throwable)
        } else {
            Log.w(TAG, redactSensitiveData(message))
        }
    }

    fun e(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            Log.e(TAG, redactSensitiveData(message), throwable)
        } else {
            Log.e(TAG, redactSensitiveData(message))
        }
    }

    private fun redactSensitiveData(message: String): String =
        message
            .replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), "[REDACTED_EMAIL]")
            .replace(Regex("ya29\\.[a-zA-Z0-9_-]+", RegexOption.IGNORE_CASE), "[REDACTED_TOKEN]")
            .replace(Regex("Bearer\\s+[a-zA-Z0-9_-]+", RegexOption.IGNORE_CASE), "Bearer [REDACTED_TOKEN]")
            .replace(Regex("session_[a-f0-9]{16,}", RegexOption.IGNORE_CASE), "[REDACTED_SESSION_ID]")
}
