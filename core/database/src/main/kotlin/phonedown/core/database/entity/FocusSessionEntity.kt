package phonedown.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity backing the `focus_sessions` table.
 *
 * All enum fields are stored as stable [String] values via [phonedown.core.database.converter.SessionConverters].
 * Do not use enum ordinals for storage.
 */
@Entity(
    tableName = "focus_sessions",
    indices = [
        Index("started_at_epoch_millis"),
        Index("ended_at_epoch_millis"),
        Index("state"),
        Index("result"),
        Index("clean"),
        Index("broken"),
        Index("updated_at_epoch_millis"),
    ],
)
data class FocusSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "planned_duration_seconds")
    val plannedDurationSeconds: Long,

    @ColumnInfo(name = "required_duration_seconds")
    val requiredDurationSeconds: Long,

    @ColumnInfo(name = "valid_focus_seconds")
    val validFocusSeconds: Long,

    @ColumnInfo(name = "actual_elapsed_seconds")
    val actualElapsedSeconds: Long,

    @ColumnInfo(name = "penalty_seconds")
    val penaltySeconds: Long,

    @ColumnInfo(name = "interruption_count")
    val interruptionCount: Int,

    @ColumnInfo(name = "minor_interruption_count")
    val minorInterruptionCount: Int,

    @ColumnInfo(name = "penalty_interruption_count")
    val penaltyInterruptionCount: Int,

    @ColumnInfo(name = "started_at_epoch_millis")
    val startedAtEpochMillis: Long,

    @ColumnInfo(name = "ended_at_epoch_millis")
    val endedAtEpochMillis: Long?,

    @ColumnInfo(name = "start_elapsed_realtime")
    val startElapsedRealtime: Long,

    @ColumnInfo(name = "end_elapsed_realtime")
    val endElapsedRealtime: Long?,

    /** Stored as stable string; see [phonedown.core.database.converter.SessionConverters]. */
    @ColumnInfo(name = "state")
    val state: String,

    /** Stored as stable string; null if session has no terminal result yet. */
    @ColumnInfo(name = "result")
    val result: String?,

    @ColumnInfo(name = "clean")
    val clean: Boolean,

    @ColumnInfo(name = "broken")
    val broken: Boolean,

    @ColumnInfo(name = "call_interrupted")
    val callInterrupted: Boolean,

    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,

    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
