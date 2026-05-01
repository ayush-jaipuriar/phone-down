package phonedown.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import phonedown.core.designsystem.PhoneDownTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhoneDownApp()
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun PhoneDownApp() {
    PhoneDownTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Phone Down",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
    }
}
