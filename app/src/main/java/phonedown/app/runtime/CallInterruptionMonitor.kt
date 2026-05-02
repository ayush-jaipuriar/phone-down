@file:Suppress("ReturnCount", "MaxLineLength")

package phonedown.app.runtime

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface CallInterruptionMonitor {
    val isInCall: StateFlow<Boolean>

    fun start()

    fun stop()
}

@Singleton
class AndroidCallInterruptionMonitor
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
    ) : CallInterruptionMonitor {
        private val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        private val _isInCall = MutableStateFlow(false)
        override val isInCall: StateFlow<Boolean> = _isInCall.asStateFlow()

        private var started = false

        private val callback =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        _isInCall.value = state != TelephonyManager.CALL_STATE_IDLE
                    }
                }
            } else {
                null
            }

        @Suppress("DEPRECATION")
        private val legacyListener =
            object : PhoneStateListener() {
                override fun onCallStateChanged(
                    state: Int,
                    phoneNumber: String?,
                ) {
                    _isInCall.value = state != TelephonyManager.CALL_STATE_IDLE
                }
            }

        override fun start() {
            if (started) return
            started = true
            if (!supportsTelephonyCalls() || !hasRequiredPermission()) {
                _isInCall.value = false
                return
            }
            val manager = telephonyManager ?: return
            _isInCall.value = currentCallState(manager)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val listener = callback ?: return
                manager.registerTelephonyCallback(ContextCompat.getMainExecutor(context), listener)
            } else {
                @Suppress("DEPRECATION")
                manager.listen(legacyListener, PhoneStateListener.LISTEN_CALL_STATE)
            }
        }

        override fun stop() {
            if (!started) return
            started = false
            val manager = telephonyManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val listener = callback ?: return
                manager.unregisterTelephonyCallback(listener)
            } else {
                @Suppress("DEPRECATION")
                manager.listen(legacyListener, PhoneStateListener.LISTEN_NONE)
            }
        }

        private fun currentCallState(manager: TelephonyManager): Boolean =
            if (hasRequiredPermission()) {
                manager.callState != TelephonyManager.CALL_STATE_IDLE
            } else {
                false
            }

        private fun hasRequiredPermission(): Boolean =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE,
            ) == PackageManager.PERMISSION_GRANTED

        private fun supportsTelephonyCalls(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CALLING)
    }
