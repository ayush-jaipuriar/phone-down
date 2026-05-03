@file:Suppress("MagicNumber")

package phonedown.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import phonedown.app.navigation.PhoneDownApp
import phonedown.app.navigation.PhoneDownRoute
import phonedown.app.runtime.ActiveSessionRuntimeCoordinator
import phonedown.app.runtime.FocusSessionService
import phonedown.app.runtime.FocusSessionServiceContract
import phonedown.core.model.repository.SettingsRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var runtimeCoordinator: ActiveSessionRuntimeCoordinator

    private var pendingStartDurationSeconds: Long? = null

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                FocusSessionService.start(this, pendingStartDurationSeconds)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            runtimeCoordinator.recoverFromAppLaunch()
        }
        setContent {
            val settings by settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = phonedown.core.model.UserSettings())
            val runtimeState by runtimeCoordinator.state
                .collectAsStateWithLifecycle(initialValue = phonedown.app.runtime.ActiveSessionRuntimeState())
            val shouldDimScreen = runtimeState.shouldDimScreen

            val scope = rememberCoroutineScope()
            val initialRoute =
                when {
                    intent?.getBooleanExtra(FocusSessionServiceContract.EXTRA_OPEN_FOCUS, false) == true ->
                        PhoneDownRoute.Focus
                    settings.onboardingCompleted -> PhoneDownRoute.Focus
                    else -> PhoneDownRoute.Onboarding
                }

            SideEffect {
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
}
