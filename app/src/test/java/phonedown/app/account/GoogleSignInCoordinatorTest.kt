package phonedown.app.account

import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleSignInCoordinatorTest {
    @Test
    fun `provider configuration failure gives signing guidance`() {
        assertEquals(
            SIGN_IN_CONFIGURATION_ERROR,
            credentialFailureMessage(GetCredentialProviderConfigurationException()),
        )
    }

    @Test
    fun `missing credential gives account guidance`() {
        assertEquals(
            "No eligible Google account was available. Add or select a Google account on this device and try again.",
            credentialFailureMessage(NoCredentialException()),
        )
    }

    @Test
    fun `unsupported device gives play services guidance`() {
        assertEquals(
            "Google Sign-In is not supported by this device configuration. Update Google Play services and try again.",
            credentialFailureMessage(GetCredentialUnsupportedException()),
        )
    }

    @Test
    fun `interrupted request can be retried`() {
        assertEquals(
            "Google Sign-In was interrupted. Please try again.",
            credentialFailureMessage(GetCredentialInterruptedException()),
        )
    }

    @Test
    fun `unknown failure remains safe and actionable`() {
        assertEquals(
            "Google Sign-In could not start. Check your connection and try again.",
            credentialFailureMessage(GetCredentialUnknownException("sensitive provider detail")),
        )
    }
}
