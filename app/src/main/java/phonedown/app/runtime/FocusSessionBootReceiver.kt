package phonedown.app.runtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FocusSessionBootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var runtimeCoordinator: ActiveSessionRuntimeCoordinator

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                runtimeCoordinator.recoverFromBoot()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
