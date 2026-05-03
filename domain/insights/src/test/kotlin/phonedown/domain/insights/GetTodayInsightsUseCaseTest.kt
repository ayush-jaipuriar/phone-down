package phonedown.domain.insights

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.core.model.SessionResult

@OptIn(ExperimentalCoroutinesApi::class)
class GetTodayInsightsUseCaseTest {

    @Test
    fun `empty sessions returns zero summary`() = runTest {
        val repo = FakeSessionRepository(emptyList())
        val clock = TestClock(TestFixtures.jan15_2026_10am())
        val useCase = GetTodayInsightsUseCase(repo, clock)

        val result = useCase()

        assertEquals(0, result.sessionCount)
        assertEquals(0, result.totalFocusSeconds)
        assertEquals(0, result.cleanSessionCount)
    }

    @Test
    fun `single clean session in window`() = runTest {
        val session = TestFixtures.testSession(
            id = "s1",
            startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
            validFocusSeconds = 1500,
            clean = true,
            result = SessionResult.CleanCompleted,
        )
        val repo = FakeSessionRepository(listOf(session))
        val clock = TestClock(TestFixtures.jan15_2026_10am() + 3600_000)
        val useCase = GetTodayInsightsUseCase(repo, clock)

        val result = useCase()

        assertEquals(1, result.sessionCount)
        assertEquals(1500, result.totalFocusSeconds)
        assertEquals(1, result.cleanSessionCount)
    }

    @Test
    fun `abandoned sessions are excluded from meaningful count`() = runTest {
        val abandoned = TestFixtures.testSession(
            id = "s1",
            startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
            result = SessionResult.Abandoned,
            validFocusSeconds = 0,
        )
        val repo = FakeSessionRepository(listOf(abandoned))
        val clock = TestClock(TestFixtures.jan15_2026_10am() + 3600_000)
        val useCase = GetTodayInsightsUseCase(repo, clock)

        val result = useCase()

        assertEquals(0, result.sessionCount)
        assertEquals(1, result.abandonedSessionCount)
    }

    @Test
    fun `session outside today window is excluded`() = runTest {
        val yesterday = TestFixtures.jan15_2026_10am() - 24 * 3600_000
        val oldSession = TestFixtures.testSession(
            id = "old",
            startedAtEpochMillis = yesterday,
        )
        val repo = FakeSessionRepository(listOf(oldSession))
        val clock = TestClock(TestFixtures.jan15_2026_10am())
        val useCase = GetTodayInsightsUseCase(repo, clock)

        val result = useCase()

        assertEquals(0, result.sessionCount)
    }

    @Test
    fun `multiple sessions aggregate correctly`() = runTest {
        val s1 = TestFixtures.testSession(
            id = "s1",
            startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
            validFocusSeconds = 1500,
            clean = true,
            result = SessionResult.CleanCompleted,
        )
        val s2 = TestFixtures.testSession(
            id = "s2",
            startedAtEpochMillis = TestFixtures.jan15_2026_10am() + 1800_000,
            validFocusSeconds = 900,
            clean = false,
            interruptionCount = 2,
            penaltyInterruptionCount = 1,
            penaltySeconds = 60,
            result = SessionResult.CompletedWithInterruption,
        )
        val repo = FakeSessionRepository(listOf(s1, s2))
        val clock = TestClock(TestFixtures.jan15_2026_10am() + 7200_000)
        val useCase = GetTodayInsightsUseCase(repo, clock)

        val result = useCase()

        assertEquals(2, result.sessionCount)
        assertEquals(2400, result.totalFocusSeconds)
        assertEquals(1, result.cleanSessionCount)
        assertEquals(2, result.interruptionCount)
        assertEquals(1, result.penaltyCount)
        assertEquals(60, result.penaltySeconds)
    }
}
