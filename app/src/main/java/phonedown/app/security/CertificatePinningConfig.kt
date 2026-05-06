package phonedown.app.security

object CertificatePinningConfig {
    val pinnedCertificates =
        mapOf(
            "accounts.google.com" to
                listOf(
                    "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // Placeholder - replace with real cert pin
                ),
            "www.googleapis.com" to
                listOf(
                    "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // Placeholder - replace with real cert pin
                ),
            "android.clients.google.com" to
                listOf(
                    "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // Placeholder - replace with real cert pin
                ),
        )

    fun getPinsForHost(host: String): List<String> = pinnedCertificates[host] ?: emptyList()

    fun isPinnedHost(host: String): Boolean = pinnedCertificates.containsKey(host)
}
