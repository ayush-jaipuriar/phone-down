@file:Suppress("MaxLineLength")

package phonedown.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import phonedown.core.database.entity.FocusSessionEntity

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(entity: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    fun getSession(id: String): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getSessionOnce(id: String): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions ORDER BY started_at_epoch_millis DESC LIMIT :limit")
    fun observeLatestSessions(limit: Int): Flow<List<FocusSessionEntity>>

    @Query(
        "SELECT * FROM focus_sessions WHERE started_at_epoch_millis >= :startEpochMillis AND started_at_epoch_millis <= :endEpochMillis ORDER BY started_at_epoch_millis DESC",
    )
    fun observeSessionsInWindow(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE updated_at_epoch_millis >= :updatedAtEpochMillis")
    suspend fun getSessionsUpdatedSince(updatedAtEpochMillis: Long): List<FocusSessionEntity>

    // Gets candidates for process-death recovery. State strings must match the stable storage strings.
    @Query(
        """
        SELECT * FROM focus_sessions 
        WHERE state IN ('created', 'waiting_for_phone_down', 'arming', 'active', 'paused_by_pickup', 'paused_by_call')
    """,
    )
    suspend fun getRecoverableSessions(): List<FocusSessionEntity>

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT * FROM focus_sessions")
    suspend fun getAllSessions(): List<FocusSessionEntity>
}
