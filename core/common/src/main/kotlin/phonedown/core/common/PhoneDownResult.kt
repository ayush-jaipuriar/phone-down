package phonedown.core.common

sealed interface PhoneDownResult<out T> {
    data class Success<T>(
        val value: T,
    ) : PhoneDownResult<T>

    data class Failure(
        val throwable: Throwable,
    ) : PhoneDownResult<Nothing>
}
