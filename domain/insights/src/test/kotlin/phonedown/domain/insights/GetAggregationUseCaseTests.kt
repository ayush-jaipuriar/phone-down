package phonedown.domain.insights

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import phonedown.core.model.SessionResult

@OptIn(ExperimentalCoroutinesApi::class)
class GetBestHourUseCaseTest {
    @Test
    fun `no sessions returns null`() =
        runTest {
            val repo = FakeSessionRepository(emptyList())
            val clock = TestClock(TestFixtures.jan15_2026_10am())
            val useCase = GetBestHourUseCase(repo, clock)

            assertEquals(null, useCase())
        }

    @Test
    fun `single session returns its hour`() =
        runTest {
            val session =
                TestFixtures.testSession(
                    id = "s1",
                    startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
                    validFocusSeconds = 1500,
                    result = SessionResult.CleanCompleted,
                )
            val repo = FakeSessionRepository(listOf(session))
            val clock = TestClock(TestFixtures.jan15_2026_10am() + 7200_000)
            val useCase = GetBestHourUseCase(repo, clock)

            val result = useCase()!!

            assertNotNull(result)
            assertEquals(1500, result.focusSeconds)
        }

    @Test
    fun `abandoned sessions are excluded`() =
        runTest {
            val session =
                TestFixtures.testSession(
                    id = "s1",
                    startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
                    result = SessionResult.Abandoned,
                    validFocusSeconds = 0,
                )
            val repo = FakeSessionRepository(listOf(session))
            val clock = TestClock(TestFixtures.jan15_2026_10am() + 3600_000)
            val useCase = GetBestHourUseCase(repo, clock)

            assertEquals(null, useCase())
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetBestWeekdayUseCaseTest {
    @Test
    fun `no sessions returns null`() =
        runTest {
            val repo = FakeSessionRepository(emptyList())
            val clock = TestClock(TestFixtures.jan15_2026_10am())
            val useCase = GetBestWeekdayUseCase(repo, clock)

            assertEquals(null, useCase())
        }

    @Test
    fun `single session returns its weekday`() =
        runTest {
            val session =
                TestFixtures.testSession(
                    id = "s1",
                    startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
                    validFocusSeconds = 1500,
                    result = SessionResult.CleanCompleted,
                )
            val repo = FakeSessionRepository(listOf(session))
            val clock = TestClock(TestFixtures.jan15_2026_10am() + 7200_000)
            val useCase = GetBestWeekdayUseCase(repo, clock)

            val result = useCase()!!

            assertEquals(1500, result.focusSeconds)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetHistoryUseCaseTest {
    @Test
    fun `empty sessions returns empty list`() =
        runTest {
            val repo = FakeSessionRepository(emptyList())
            val useCase = GetHistoryUseCase(repo)

            val result = useCase()

            assertEquals(0, result.size)
        }

    @Test
    fun `pagination works correctly`() =
        runTest {
            val sessions =
                (1..5).map { i ->
                    TestFixtures.testSession(
                        id = "s$i",
                        startedAtEpochMillis = TestFixtures.jan15_2026_10am() - i * 3600_000,
                        validFocusSeconds = 1500,
                        result = SessionResult.CleanCompleted,
                    )
                }
            val repo = FakeSessionRepository(sessions)
            val useCase = GetHistoryUseCase(repo)

            val page1 = useCase(page = 0, pageSize = 2)
            val page2 = useCase(page = 1, pageSize = 2)
            val page3 = useCase(page = 2, pageSize = 2)

            assertEquals(2, page1.size)
            assertEquals(2, page2.size)
            assertEquals(1, page3.size)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetAdvancedInsightsUseCaseTest {
    @Test
    fun `no meaningful sessions returns null`() =
        runTest {
            val repo = FakeSessionRepository(emptyList())
            val clock = TestClock(TestFixtures.jan15_2026_10am())
            val useCase = GetAdvancedInsightsUseCase(repo, clock)

            assertEquals(null, useCase())
        }

    @Test
    fun `longest clean session is found correctly`() =
        runTest {
            val s1 =
                TestFixtures.testSession(
                    id = "s1",
                    startedAtEpochMillis = TestFixtures.jan15_2026_10am(),
                    validFocusSeconds = 900,
                    clean = true,
                    result = SessionResult.CleanCompleted,
                )
            val s2 =
                TestFixtures.testSession(
                    id = "s2",
                    startedAtEpochMillis = TestFixtures.jan15_2026_10am() - 3600_000,
                    validFocusSeconds = 3600,
                    clean = true,
                    result = SessionResult.CleanCompleted,
                )
            val repo = FakeSessionRepository(listOf(s1, s2))
            val clock = TestClock(TestFixtures.jan15_2026_10am() + 7200_000)
            val useCase = GetAdvancedInsightsUseCase(repo, clock)

            val result = useCase()!!

            assertEquals(3600, result.longestCleanFocusSeconds)
            assertEquals(2250, result.averageSessionSeconds)
        }
}
