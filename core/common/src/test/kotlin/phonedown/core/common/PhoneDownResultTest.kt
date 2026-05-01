package phonedown.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneDownResultTest {
    @Test
    fun successStoresValue() {
        val result = PhoneDownResult.Success("ready")

        assertEquals("ready", result.value)
    }
}
