package phonedown.app.runtime

import android.content.pm.ServiceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusForegroundServicePolicyTest {
    @Test
    fun `android 14 and newer use special-use foreground service type`() {
        assertEquals(
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            focusForegroundServiceType(sdkInt = 34),
        )
    }

    @Test
    fun `older Android versions use legacy foreground service start`() {
        assertNull(focusForegroundServiceType(sdkInt = 33))
    }
}
