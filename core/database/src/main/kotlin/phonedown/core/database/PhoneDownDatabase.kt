package phonedown.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import phonedown.core.database.dao.FocusSessionDao
import phonedown.core.database.dao.PenaltyEventDao
import phonedown.core.database.entity.FocusSessionEntity
import phonedown.core.database.entity.PenaltyEventEntity

@Database(
    entities = [
        FocusSessionEntity::class,
        PenaltyEventEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PhoneDownDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun penaltyEventDao(): PenaltyEventDao
}
