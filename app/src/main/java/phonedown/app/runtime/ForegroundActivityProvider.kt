package phonedown.app.runtime

import android.app.Activity
import android.app.Application
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import phonedown.core.billing.BillingActivityProvider
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundActivityProvider
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : BillingActivityProvider,
        Application.ActivityLifecycleCallbacks {
        private val application = context.applicationContext as Application
        private var currentActivityRef: WeakReference<Activity>? = null

        init {
            application.registerActivityLifecycleCallbacks(this)
        }

        override fun currentActivity(): Activity? = currentActivityRef?.get()

        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: android.os.Bundle?,
        ) = Unit

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityResumed(activity: Activity) {
            currentActivityRef = WeakReference(activity)
        }

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) {
            if (currentActivityRef?.get() === activity) {
                currentActivityRef = null
            }
        }

        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: android.os.Bundle,
        ) = Unit

        override fun onActivityDestroyed(activity: Activity) {
            if (currentActivityRef?.get() === activity) {
                currentActivityRef = null
            }
        }
    }
