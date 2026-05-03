package phonedown.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import phonedown.core.designsystem.PhoneDownButton
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing

@Composable
@Suppress("FunctionName")
fun OnboardingScreen(onContinue: () -> Unit) {
    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    PhoneDownScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.weight(0.2f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            OnboardingCard(page = page)
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.lg))

        // Pager Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pageCount) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    label = "IndicatorWidth",
                )
                Box(
                    modifier =
                        Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    PhoneDownDesign.colors.textPrimary
                                } else {
                                    PhoneDownDesign.colors.inactive
                                },
                            ),
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.8f))

        val isLastPage = pagerState.currentPage == pageCount - 1

        PhoneDownButton(
            text = if (isLastPage) "Start" else "Continue",
            onClick = {
                if (isLastPage) {
                    onContinue()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(PhoneDownSpacing.xl))
    }
}

@Composable
@Suppress("FunctionName")
private fun OnboardingCard(page: Int) {
    PhoneDownCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = PhoneDownSpacing.screen),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
            modifier = Modifier.padding(PhoneDownSpacing.md),
        ) {
            when (page) {
                0 -> {
                    Text(
                        text = "Start a focus session",
                        style = MaterialTheme.typography.titleLarge,
                        color = PhoneDownDesign.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Take control of your time by starting a session intentionally. Choose your duration and commit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhoneDownDesign.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                1 -> {
                    Text(
                        text = "Place your phone face down",
                        style = MaterialTheme.typography.titleLarge,
                        color = PhoneDownDesign.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text =
                            "Your session begins only when your phone is resting face " +
                                "down and stable. Remove the distraction completely.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhoneDownDesign.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                2 -> {
                    Text(
                        text = "Pickups pause your session",
                        style = MaterialTheme.typography.titleLarge,
                        color = PhoneDownDesign.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text =
                            "Picking up your phone will pause the session and affect your Focus " +
                                "Quality. We may ask for notification permissions to keep tracking " +
                                "running in the background.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhoneDownDesign.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
