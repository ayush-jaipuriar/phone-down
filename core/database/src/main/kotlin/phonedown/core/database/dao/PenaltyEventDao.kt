@file:Suppress("MaxLineLength")

package phonedown.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import phonedown.core.database.entity.PenaltyEventEntity

@Dao
interface PenaltyEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenaltyEvent(entity: PenaltyEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPenaltyEvent(entity: PenaltyEventEntity)

    @Query("SELECT * FROM penalty_events WHERE session_id = :sessionId ORDER BY started_at_epoch_millis ASC")
    fun observePenaltyEventsForSession(sessionId: String): Flow<List<PenaltyEventEntity>>

    @Query("SELECT * FROM penalty_events WHERE session_id = :sessionId ORDER BY started_at_epoch_millis ASC")
    suspend fun getPenaltyEventsForSession(sessionId: String): List<PenaltyEventEntity>

    @Query("SELECT * FROM penalty_events WHERE started_at_epoch_millis >= :startEpochMillis AND started_at_epoch_millis <= :endEpochMillis")
    suspend fun getPenaltyEventsUpdatedWindow(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): List<PenaltyEventEntity>

    @Query("DELETE FROM penalty_events WHERE session_id = :sessionId")
    suspend fun deletePenaltyEventsForSession(sessionId: String)

    @Query("SELECT * FROM penalty_events")
    suspend fun getAllPenaltyEvents(): List<PenaltyEventEntity>

    @Query("DELETE FROM penalty_events")
    suspend fun deleteAllPenaltyEvents()
}
