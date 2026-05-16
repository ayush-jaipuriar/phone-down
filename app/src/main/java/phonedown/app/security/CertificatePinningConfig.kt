package phonedown.app.security

object CertificatePinningConfig {
    val pinnedCertificates =
        emptyMap<String, List<String>>()

    fun getPinsForHost(host: String): List<String> = pinnedCertificates[host] ?: emptyList()

    fun isPinnedHost(host: String): Boolean = pinnedCertificates.containsKey(host)
}
