package phonedown.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PhoneDownLightColors = lightColorScheme()
private val PhoneDownDarkColors = darkColorScheme()

@Composable
@Suppress("FunctionName")
fun PhoneDownTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PhoneDownDarkColors else PhoneDownLightColors,
        content = content,
    )
}
