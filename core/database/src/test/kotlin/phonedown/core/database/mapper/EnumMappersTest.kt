package phonedown.core.database.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionResult
import phonedown.core.model.SessionState

class EnumMappersTest {
    @Test
    fun sessionStateMapsCorrectly() {
        SessionState.entries.forEach { state ->
            val stored = state.toStorageString()
            assertEquals(state, stored.toSessionState())
        }
    }

    @Test
    fun sessionStateUsesExplicitStableStorageStrings() {
        assertEquals("created", SessionState.Created.toStorageString())
        assertEquals("waiting_for_phone_down", SessionState.WaitingForPhoneDown.toStorageString())
        assertEquals("arming", SessionState.Arming.toStorageString())
        assertEquals("active", SessionState.Active.toStorageString())
        assertEquals("paused_by_pickup", SessionState.PausedByPickup.toStorageString())
        assertEquals("paused_by_call", SessionState.PausedByCall.toStorageString())
        assertEquals("paused_by_user", SessionState.PausedByUser.toStorageString())
        assertEquals("completed", SessionState.Completed.toStorageString())
        assertEquals("ended_early", SessionState.EndedEarly.toStorageString())
        assertEquals("invalidated", SessionState.Invalidated.toStorageString())
        assertEquals("broken", SessionState.Broken.toStorageString())
        assertEquals("abandoned", SessionState.Abandoned.toStorageString())
    }

    @Test
    fun unknownSessionStateMapsToFallback() {
        assertEquals(SessionState.Broken, "SomeFutureState".toSessionState())
    }

    @Test
    fun sessionResultMapsCorrectly() {
        SessionResult.entries.forEach { result ->
            val stored = result.toStorageString()
            assertEquals(result, stored.toSessionResult())
        }
    }

    @Test
    fun sessionResultUsesExplicitStableStorageStrings() {
        assertEquals("clean_completed", SessionResult.CleanCompleted.toStorageString())
        assertEquals(
            "completed_with_interruption",
            SessionResult.CompletedWithInterruption.toStorageString(),
        )
        assertEquals("partial", SessionResult.Partial.toStorageString())
        assertEquals("strong_partial", SessionResult.StrongPartial.toStorageString())
        assertEquals("invalidated", SessionResult.Invalidated.toStorageString())
        assertEquals("broken", SessionResult.Broken.toStorageString())
        assertEquals("abandoned", SessionResult.Abandoned.toStorageString())
    }

    @Test
    fun unknownSessionResultMapsToFallback() {
        assertEquals(SessionResult.Broken, "SomeFutureResult".toSessionResult())
    }

    @Test
    fun penaltyEventTypeMapsCorrectly() {
        PenaltyEventType.entries.forEach { type ->
            val stored = type.toStorageString()
            assertEquals(type, stored.toPenaltyEventType())
        }
    }

    @Test
    fun penaltyEventTypeUsesExplicitStableStorageStrings() {
        assertEquals("minor_pickup", PenaltyEventType.MinorPickup.toStorageString())
        assertEquals("penalty_pickup", PenaltyEventType.PenaltyPickup.toStorageString())
        assertEquals("long_pickup", PenaltyEventType.LongPickup.toStorageString())
        assertEquals("call_pause", PenaltyEventType.CallPause.toStorageString())
        assertEquals("force_close", PenaltyEventType.ForceClose.toStorageString())
        assertEquals("device_restart", PenaltyEventType.DeviceRestart.toStorageString())
        assertEquals("manual_end", PenaltyEventType.ManualEnd.toStorageString())
        assertEquals("manual_pause", PenaltyEventType.ManualPause.toStorageString())
    }

    @Test
    fun unknownPenaltyEventTypeMapsToFallback() {
        assertEquals(PenaltyEventType.MinorPickup, "SomeFuturePenalty".toPenaltyEventType())
    }
}
