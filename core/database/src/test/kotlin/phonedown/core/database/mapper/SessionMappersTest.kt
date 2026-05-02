package phonedown.core.database.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionState

class SessionMappersTest {
    @Test
    fun focusSessionMapsToEntityAndBack() {
        val domain =
            FocusSession(
                id = "session-123",
                plannedDurationSeconds = 1500,
                requiredDurationSeconds = 1500,
                validFocusSeconds = 1000,
                actualElapsedSeconds = 1000,
                penaltySeconds = 0,
                interruptionCount = 0,
                minorInterruptionCount = 0,
                penaltyInterruptionCount = 0,
                startedAtEpochMillis = 10000L,
                endedAtEpochMillis = null,
                startElapsedRealtime = 5000L,
                endElapsedRealtime = null,
                state = SessionState.Active,
                result = null,
                clean = true,
                broken = false,
                callInterrupted = false,
                createdAtEpochMillis = 9000L,
                updatedAtEpochMillis = 10000L,
            )

        val entity = domain.toEntity()
        assertEquals("session-123", entity.id)
        assertEquals("active", entity.state)

        val restored = entity.toDomainModel()
        assertEquals(domain, restored)
    }

    @Test
    fun penaltyEventMapsToEntityAndBack() {
        val domain =
            PenaltyEvent(
                id = "penalty-456",
                sessionId = "session-123",
                type = PenaltyEventType.MinorPickup,
                startedAtEpochMillis = 11000L,
                endedAtEpochMillis = 11005L,
                durationSeconds = 5,
                penaltySeconds = 0,
            )

        val entity = domain.toEntity()
        assertEquals("penalty-456", entity.id)
        assertEquals("minor_pickup", entity.type)

        val restored = entity.toDomainModel()
        assertEquals(domain, restored)
    }
}
