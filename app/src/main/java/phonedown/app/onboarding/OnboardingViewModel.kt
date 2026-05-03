package phonedown.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import phonedown.core.model.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        fun completeOnboarding(onCompleted: () -> Unit) {
            viewModelScope.launch {
                settingsRepository.setOnboardingCompleted(true)
                onCompleted()
            }
        }
    }
