package phonedown.domain.insights

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import phonedown.core.model.SessionResult
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GetStreakUseCaseTest {

    @Test
    fun `no sessions returns zero streak`() = runTest {
        val repo = FakeSessionRepository(emptyList())
        val clock = TestClock(TestFixtures.jan15_2026_10am())
        val useCase = GetStreakUseCase(repo, clock)

        val result = useCase()!!

        assertEquals(0, result.currentStreakDays)
        assertEquals(0, result.longestStreakDays)
    }

    @Test
    fun `single session today gives streak of 1`() = runTest {
        val session = TestFixtures.testSession(
            id = "s1",
            startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
            validFocusSeconds = 1500,
            result = SessionResult.CleanCompleted,
        )
        val repo = FakeSessionRepository(listOf(session))
        val clock = TestClock(TestFixtures.jan15_2026_10am() + 3600_000)
        val useCase = GetStreakUseCase(repo, clock)

        val result = useCase()!!

        assertEquals(1, result.currentStreakDays)
        assertEquals(1, result.longestStreakDays)
    }

    @Test
    fun `abandoned sessions do not count for streak`() = runTest {
        val session = TestFixtures.testSession(
            id = "s1",
            startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
            validFocusSeconds = 0,
            result = SessionResult.Abandoned,
        )
        val repo = FakeSessionRepository(listOf(session))
        val clock = TestClock(TestFixtures.jan15_2026_10am() + 3600_000)
        val useCase = GetStreakUseCase(repo, clock)

        val result = useCase()!!

        assertEquals(0, result.currentStreakDays)
        assertEquals(0, result.longestStreakDays)
    }

    @Test
    fun `streak broken by a missing day`() = runTest {
        val today = TestFixtures.jan15_2026_10am()
        val yesterday = today - 24 * 3600_000
        val threeDaysAgo = today - 3 * 24 * 3600_000

        val s1 = TestFixtures.testSession(id = "s3", startedAtEpochMillis = threeDaysAgo, validFocusSeconds = 1500, result = SessionResult.CleanCompleted)
        val s2 = TestFixtures.testSession(id = "s2", startedAtEpochMillis = yesterday, validFocusSeconds = 1500, result = SessionResult.CleanCompleted)
        val s3 = TestFixtures.testSession(id = "s1", startedAtEpochMillis = today, validFocusSeconds = 1500, result = SessionResult.CleanCompleted)
        val repo = FakeSessionRepository(listOf(s1, s2, s3))
        val clock = TestClock(today + 3600_000)
        val useCase = GetStreakUseCase(repo, clock)

        val result = useCase()!!

        assertEquals(2, result.currentStreakDays)
    }

    @Test
    fun `longest streak is computed correctly`() = runTest {
        val today = TestFixtures.jan15_2026_10am()
        val sessions = (0..4).map { dayOffset ->
            TestFixtures.testSession(
                id = "s$dayOffset",
                startedAtEpochMillis = today - dayOffset * 24L * 3600_000,
                validFocusSeconds = 1500,
                result = SessionResult.CleanCompleted,
            )
        }
        val repo = FakeSessionRepository(sessions)
        val clock = TestClock(today + 3600_000)
        val useCase = GetStreakUseCase(repo, clock)

        val result = useCase()!!

        assertEquals(5, result.currentStreakDays)
        assertEquals(5, result.longestStreakDays)
    }

    @Test
    fun `computeCurrentStreak with empty set returns 0`() {
        assertEquals(0, GetStreakUseCase.computeCurrentStreak(100, emptySet()))
    }

    @Test
    fun `computeLongestStreak with empty set returns 0`() {
        assertEquals(0, GetStreakUseCase.computeLongestStreak(emptySet()))
    }

    @Test
    fun `computeLongestStreak with consecutive days`() {
        val days = setOf(1L, 2L, 3L, 5L, 6L, 7L, 8L)
        assertEquals(4, GetStreakUseCase.computeLongestStreak(days))
    }
}
