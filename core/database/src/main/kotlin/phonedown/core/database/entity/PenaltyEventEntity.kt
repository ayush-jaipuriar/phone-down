package phonedown.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity backing the `penalty_events` table.
 *
 * Foreign key to `focus_sessions` with CASCADE DELETE ensures penalty events
 * are automatically removed when their parent session is deleted.
 */
@Entity(
    tableName = "penalty_events",
    foreignKeys = [
        ForeignKey(
            entity = FocusSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("session_id"),
        Index("type"),
        Index("started_at_epoch_millis"),
    ],
)
data class PenaltyEventEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    /** Stored as stable string; see [phonedown.core.database.converter.SessionConverters]. */
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "started_at_epoch_millis")
    val startedAtEpochMillis: Long,
    @ColumnInfo(name = "ended_at_epoch_millis")
    val endedAtEpochMillis: Long?,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long,
    @ColumnInfo(name = "penalty_seconds")
    val penaltySeconds: Long,
)
