package phonedown.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import phonedown.app.navigation.PhoneDownApp
import phonedown.core.datastore.ThemeModePreference
import phonedown.core.datastore.phoneDownThemeModeDataStore
import phonedown.core.model.ThemeMode

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeModePreference =
                remember {
                    ThemeModePreference(applicationContext.phoneDownThemeModeDataStore)
                }
            val themeMode by themeModePreference.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.System,
            )

            PhoneDownApp(
                themeMode = themeMode,
                onThemeModeSelected = themeModePreference::setThemeMode,
            )
        }
    }
}
