package phonedown.domain.insights

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.core.model.SessionResult

@OptIn(ExperimentalCoroutinesApi::class)
class GetFocusQualityUseCaseTest {
    @Test
    fun `no sessions returns null`() =
        runTest {
            val repo = FakeSessionRepository(emptyList())
            val clock = TestClock(TestFixtures.jan15_2026_10am())
            val useCase = GetFocusQualityUseCase(repo, clock)

            val result = useCase()

            assertEquals(null, result)
        }

    @Test
    fun `all clean completed sessions produce Focused label with moderate volume`() =
        runTest {
            val session =
                TestFixtures.testSession(
                    id = "s1",
                    startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
                    validFocusSeconds = 3600,
                    clean = true,
                    result = SessionResult.CleanCompleted,
                    interruptionCount = 0,
                )
            val repo = FakeSessionRepository(listOf(session))
            val clock = TestClock(TestFixtures.jan15_2026_10am() + 3600_000)
            val useCase = GetFocusQualityUseCase(repo, clock)

            val result = useCase()!!

            assertEquals(FocusQualityLabel.Focused, result.label)
            assertTrue(result.score in 75..89)
            assertEquals(1f, result.completionRate)
            assertEquals(1f, result.cleanRatio)
        }

    @Test
    fun `completed with interruption lowers score`() =
        runTest {
            val session =
                TestFixtures.testSession(
                    id = "s1",
                    startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
                    validFocusSeconds = 3600,
                    clean = false,
                    result = SessionResult.CompletedWithInterruption,
                    interruptionCount = 3,
                    penaltyInterruptionCount = 1,
                    penaltySeconds = 60,
                )
            val repo = FakeSessionRepository(listOf(session))
            val clock = TestClock(TestFixtures.jan15_2026_10am() + 3600_000)
            val useCase = GetFocusQualityUseCase(repo, clock)

            val result = useCase()!!

            assertEquals(1f, result.completionRate)
            assertEquals(0f, result.cleanRatio)
            assertTrue(result.score < 90)
        }

    @Test
    fun `label boundaries are correct`() =
        runTest {
            assertLabel(95, FocusQualityLabel.Deep)
            assertLabel(90, FocusQualityLabel.Deep)
            assertLabel(89, FocusQualityLabel.Focused)
            assertLabel(75, FocusQualityLabel.Focused)
            assertLabel(74, FocusQualityLabel.Steady)
            assertLabel(60, FocusQualityLabel.Steady)
            assertLabel(59, FocusQualityLabel.Fragmented)
            assertLabel(40, FocusQualityLabel.Fragmented)
            assertLabel(39, FocusQualityLabel.Scattered)
            assertLabel(0, FocusQualityLabel.Scattered)
        }

    @Test
    fun `normalizeFocusVolume is 0 for 0 seconds`() {
        assertEquals(0f, GetFocusQualityUseCase.normalizeFocusVolume(0))
    }

    @Test
    fun `normalizeFocusVolume is 1 for 14 hours`() {
        assertEquals(1f, GetFocusQualityUseCase.normalizeFocusVolume(14 * 3600))
    }

    @Test
    fun `normalizeFocusVolume clamped at 1`() {
        assertEquals(1f, GetFocusQualityUseCase.normalizeFocusVolume(100 * 3600))
    }

    @Test
    fun `normalizeInterruptions is 1 when no interruptions`() {
        assertEquals(1f, GetFocusQualityUseCase.normalizeInterruptions(0f, 5f))
    }

    @Test
    fun `normalizeInterruptions is 0 at 5 avg interruptions`() {
        assertEquals(0f, GetFocusQualityUseCase.normalizeInterruptions(25f, 5f))
    }

    private fun assertLabel(
        score: Int,
        expected: FocusQualityLabel,
    ) {
        val label =
            when (score) {
                in 90..100 -> FocusQualityLabel.Deep
                in 75..89 -> FocusQualityLabel.Focused
                in 60..74 -> FocusQualityLabel.Steady
                in 40..59 -> FocusQualityLabel.Fragmented
                else -> FocusQualityLabel.Scattered
            }
        assertEquals(expected, label)
    }
}
