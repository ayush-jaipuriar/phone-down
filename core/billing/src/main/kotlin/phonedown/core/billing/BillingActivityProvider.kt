package phonedown.core.billing

import android.app.Activity

fun interface BillingActivityProvider {
    fun currentActivity(): Activity?
}
