package phonedown.core.common

import java.security.SecureRandom

object SecureRandomUtils {
    private val secureRandom = SecureRandom()

    fun generateSecureId(length: Int = 16): String {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateSecureToken(length: Int = 32): String {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
