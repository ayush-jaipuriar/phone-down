package phonedown.app.pro

import androidx.compose.runtime.Composable
import phonedown.feature.pro.ProScreen

@Composable
@Suppress("FunctionName")
fun ProRoute(onBack: () -> Unit) {
    ProScreen(onBack = onBack)
}
