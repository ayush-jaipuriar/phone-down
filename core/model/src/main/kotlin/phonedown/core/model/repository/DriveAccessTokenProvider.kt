package phonedown.core.model.repository

sealed class DriveAccessTokenResult {
    data class Success(
        val accessToken: String,
    ) : DriveAccessTokenResult()

    data object SignedOut : DriveAccessTokenResult()

    data object RequiresUserAction : DriveAccessTokenResult()

    data class Failure(
        val reason: String,
    ) : DriveAccessTokenResult()
}

interface DriveAccessTokenProvider {
    suspend fun getAccessToken(): DriveAccessTokenResult

    fun clearCachedAccessToken()
}
