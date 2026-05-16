package phonedown.app.backup

import android.accounts.Account
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.first
import phonedown.core.model.AccountState
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.DriveAccessTokenProvider
import phonedown.core.model.repository.DriveAccessTokenResult
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleDriveAuthorizationManager
    @Inject
    constructor(
        @ApplicationContext private val applicationContext: Context,
        private val authRepository: AuthRepository,
    ) : DriveAccessTokenProvider, DriveAuthorizationCoordinator {
        private val authorizationClient by lazy {
            Identity.getAuthorizationClient(applicationContext)
        }

        private var cachedAccessToken: String? = null
        private var cachedAccountEmail: String? = null
        private var pendingAuthorizationEmail: String? = null

        override suspend fun getAccessToken(): DriveAccessTokenResult {
            val email = currentSignedInEmail() ?: return DriveAccessTokenResult.SignedOut
            val cachedToken = cachedAccessToken
            if (cachedToken != null && cachedAccountEmail == email) {
                return DriveAccessTokenResult.Success(cachedToken)
            }

            return when (val step = beginAuthorization()) {
                is DriveAuthorizationUiStep.AccessToken -> DriveAccessTokenResult.Success(step.token)
                DriveAuthorizationUiStep.Cancelled -> DriveAccessTokenResult.Failure("Google Drive authorization was cancelled.")
                is DriveAuthorizationUiStep.Error -> DriveAccessTokenResult.Failure(step.message)
                is DriveAuthorizationUiStep.LaunchResolution -> DriveAccessTokenResult.RequiresUserAction
            }
        }

        override suspend fun beginAuthorization(): DriveAuthorizationUiStep {
            val email = currentSignedInEmail()
            if (email.isNullOrBlank()) {
                return DriveAuthorizationUiStep.Error("Sign in to Google before using backup.")
            }

            return try {
                val authorizationResult =
                    authorizationClient
                        .authorize(
                            AuthorizationRequest
                                .builder()
                                .setRequestedScopes(listOf(Scope(DRIVE_APPDATA_SCOPE)))
                                .setAccount(Account(email, GOOGLE_ACCOUNT_TYPE))
                                .build(),
                        ).await()

                when {
                    authorizationResult.hasResolution() && authorizationResult.pendingIntent != null -> {
                        pendingAuthorizationEmail = email
                        DriveAuthorizationUiStep.LaunchResolution(authorizationResult.pendingIntent!!)
                    }

                    authorizationResult.hasResolution() -> {
                        DriveAuthorizationUiStep.Error("Google Drive authorization requires confirmation, but no resolution was returned.")
                    }

                    !authorizationResult.accessToken.isNullOrBlank() -> {
                        cacheAccessToken(email, authorizationResult.accessToken!!)
                        DriveAuthorizationUiStep.AccessToken(authorizationResult.accessToken!!)
                    }

                    else -> {
                        DriveAuthorizationUiStep.Error("Google Drive authorization did not return an access token.")
                    }
                }
            } catch (_: Exception) {
                DriveAuthorizationUiStep.Error("Google Drive authorization is unavailable right now.")
            }
        }

        override fun completeAuthorization(
            resultCode: Int,
            data: Intent?,
        ): DriveAuthorizationUiStep {
            if (resultCode != Activity.RESULT_OK) {
                pendingAuthorizationEmail = null
                return DriveAuthorizationUiStep.Cancelled
            }

            val email = pendingAuthorizationEmail ?: cachedAccountEmail
            return try {
                val result = authorizationClient.getAuthorizationResultFromIntent(data)
                val accessToken = result.accessToken
                if (email.isNullOrBlank() || accessToken.isNullOrBlank()) {
                    DriveAuthorizationUiStep.Error("Google Drive authorization did not return an access token.")
                } else {
                    pendingAuthorizationEmail = null
                    cacheAccessToken(email, accessToken)
                    DriveAuthorizationUiStep.AccessToken(accessToken)
                }
            } catch (_: Exception) {
                pendingAuthorizationEmail = null
                DriveAuthorizationUiStep.Error("Google Drive authorization could not be completed.")
            }
        }

        override fun clearCachedAccessToken() {
            cachedAccessToken = null
            cachedAccountEmail = null
            pendingAuthorizationEmail = null
        }

        private suspend fun currentSignedInEmail(): String? =
            when (val state = authRepository.accountState.first()) {
                is AccountState.SignedIn -> state.email
                AccountState.SignedOut -> null
            }

        private fun cacheAccessToken(
            email: String,
            accessToken: String,
        ) {
            cachedAccountEmail = email
            cachedAccessToken = accessToken
        }

        private suspend fun <T> Task<T>.await(): T =
            suspendCancellableCoroutine { continuation ->
                addOnSuccessListener { continuation.resume(it) }
                addOnFailureListener { continuation.resumeWithException(it) }
                addOnCanceledListener { continuation.cancel() }
            }

        private companion object {
            const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
            const val GOOGLE_ACCOUNT_TYPE = "com.google"
        }
    }

interface DriveAuthorizationCoordinator {
    suspend fun beginAuthorization(): DriveAuthorizationUiStep

    fun completeAuthorization(
        resultCode: Int,
        data: Intent?,
    ): DriveAuthorizationUiStep

    fun clearCachedAccessToken()
}

sealed class DriveAuthorizationUiStep {
    data class AccessToken(
        val token: String,
    ) : DriveAuthorizationUiStep()

    data class LaunchResolution(
        val pendingIntent: PendingIntent,
    ) : DriveAuthorizationUiStep()

    data class Error(
        val message: String,
    ) : DriveAuthorizationUiStep()

    data object Cancelled : DriveAuthorizationUiStep()
}
