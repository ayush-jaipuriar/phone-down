package phonedown.app.onboarding

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import phonedown.feature.onboarding.OnboardingScreen

@Composable
@Suppress("FunctionName")
fun OnboardingRoute(
    onContinue: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingScreen(
        onContinue = {
            viewModel.completeOnboarding(onCompleted = onContinue)
        },
    )
}
