package phonedown.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import phonedown.app.navigation.PhoneDownApp
import phonedown.core.model.ThemeMode
import phonedown.core.model.repository.SettingsRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settingsRepository.settings
                .map { it.themeMode }
                .collectAsStateWithLifecycle(initialValue = ThemeMode.System)

            val scope = rememberCoroutineScope()

            PhoneDownApp(
                themeMode = themeMode,
                onThemeModeSelected = { mode ->
                    scope.launch {
                        settingsRepository.setThemeMode(mode)
                    }
                },
            )
        }
    }
}
