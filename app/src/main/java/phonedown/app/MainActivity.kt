@file:Suppress("MagicNumber")

package phonedown.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import phonedown.app.backup.AutoBackupScheduler
import phonedown.app.navigation.PhoneDownApp
import phonedown.app.navigation.PhoneDownRoute
import phonedown.app.runtime.ActiveSessionRuntimeCoordinator
import phonedown.app.runtime.FocusSessionService
import phonedown.app.runtime.FocusSessionServiceContract
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.SettingsRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var runtimeCoordinator: ActiveSessionRuntimeCoordinator

    @Inject
    lateinit var autoBackupScheduler: AutoBackupScheduler

    @Inject
    lateinit var billingRepository: BillingRepository

    private var pendingStartDurationSeconds: Long? = null
    private val openFocusRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var callPermissionGranted by mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val duration = pendingStartDurationSeconds
            pendingStartDurationSeconds = null
            if (granted && duration != null) {
                FocusSessionService.start(this, duration)
            }
        }

    private val callPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            callPermissionGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            runtimeCoordinator.recoverFromAppLaunch()
            autoBackupScheduler.refreshSchedule()
            billingRepository.syncPurchases()
        }
        callPermissionGranted = hasCallPermission()
        setContent {
            val settings by settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = phonedown.core.model.UserSettings())
            val runtimeState by runtimeCoordinator.state
                .collectAsStateWithLifecycle(initialValue = phonedown.app.runtime.ActiveSessionRuntimeState())
            val shouldDimScreen = runtimeState.shouldDimScreen
            val shouldKeepScreenAwake = runtimeState.shouldKeepScreenAwake

            val scope = rememberCoroutineScope()
            val initialRoute =
                when {
                    intent?.getBooleanExtra(FocusSessionServiceContract.EXTRA_OPEN_FOCUS, false) == true ->
                        PhoneDownRoute.Focus
                    settings.onboardingCompleted -> PhoneDownRoute.Focus
                    else -> PhoneDownRoute.Onboarding
                }

            SideEffect {
                if (shouldKeepScreenAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                window.attributes =
                    window.attributes.apply {
                        screenBrightness =
                            if (shouldDimScreen) {
                                0.02f
                            } else {
                                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                            }
                    }
            }

            PhoneDownApp(
                themeMode = settings.themeMode,
                initialRoute = initialRoute,
                onThemeModeSelected = { mode ->
                    scope.launch {
                        settingsRepository.setThemeMode(mode)
                    }
                },
                onStartFocusClick = { durationSeconds ->
                    startFocusSession(durationSeconds)
                },
                onRetrySensorsClick = { durationSeconds ->
                    retrySensors(durationSeconds)
                },
                openFocusRequests = openFocusRequests,
                callPausePermissionGranted = callPermissionGranted,
                onCallPausePermissionRequested = ::requestCallPermission,
            )
        }
    }

    private fun startFocusSession(durationSeconds: Long) {
        pendingStartDurationSeconds = durationSeconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        FocusSessionService.start(this, durationSeconds)
    }

    private fun retrySensors(durationSeconds: Long) {
        FocusSessionService.retrySensors(this, durationSeconds)
    }

    private fun requestCallPermission() {
        if (!hasCallPermission()) {
            callPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        } else {
            callPermissionGranted = true
        }
    }

    private fun hasCallPermission(): Boolean =
        checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(FocusSessionServiceContract.EXTRA_OPEN_FOCUS, false)) {
            openFocusRequests.tryEmit(Unit)
        }
    }

    override fun onDestroy() {
        pendingStartDurationSeconds = null
        super.onDestroy()
    }
}
