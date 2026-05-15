package phonedown.app.account

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import phonedown.core.model.GoogleAccount
import java.security.SecureRandom
import java.util.Base64

class GoogleSignInCoordinator(
    private val applicationContext: Context,
) {
    suspend fun signIn(activity: Activity): GoogleAccount {
        val serverClientId = applicationContext.findDefaultWebClientId()
        val credentialManager = CredentialManager.create(activity)
        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(
                    GetSignInWithGoogleOption
                        .Builder(serverClientId)
                        .setNonce(generateNonce())
                        .build(),
                ).build()

        val response =
            try {
                credentialManager.getCredential(
                    context = activity,
                    request = request,
                )
            } catch (exception: GetCredentialCancellationException) {
                throw GoogleSignInCancelledException()
            } catch (exception: GetCredentialException) {
                throw GoogleSignInFailedException("Google Sign-In is unavailable right now.")
            }

        val credential = response.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw GoogleSignInFailedException("Google returned an unsupported sign-in response.")
        }

        val googleCredential =
            try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (exception: GoogleIdTokenParsingException) {
                throw GoogleSignInFailedException("Google returned a sign-in response this app could not read.")
            }

        return GoogleAccount(
            accountId = googleCredential.id,
            displayName = googleCredential.displayName,
            email = googleCredential.id,
            photoUrl = googleCredential.profilePictureUri?.toString(),
        )
    }

    suspend fun clearCredentialState(context: Context) {
        CredentialManager
            .create(context)
            .clearCredentialState(ClearCredentialStateRequest())
    }

    private fun Context.findDefaultWebClientId(): String {
        val resourceId =
            resources.getIdentifier(
                "default_web_client_id",
                "string",
                packageName,
            )
        if (resourceId == 0) {
            throw GoogleSignInMissingConfigException()
        }
        return getString(resourceId)
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(NONCE_BYTE_LENGTH)
        SecureRandom().nextBytes(bytes)
        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    private companion object {
        const val NONCE_BYTE_LENGTH = 32
    }
}

sealed class GoogleSignInException(
    message: String,
) : Exception(message)

class GoogleSignInCancelledException : GoogleSignInException("Sign-in was cancelled.")

class GoogleSignInMissingConfigException : GoogleSignInException("Google Sign-In needs a Web OAuth client before it can run.")

class GoogleSignInFailedException(
    message: String,
) : GoogleSignInException(message)
