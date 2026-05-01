package phonedown.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import phonedown.core.database.PhoneDownDatabase
import phonedown.core.database.dao.FocusSessionDao
import phonedown.core.database.dao.PenaltyEventDao
import phonedown.core.database.repository.RoomSessionRepository
import phonedown.core.model.repository.SessionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesPhoneDownDatabase(
        @ApplicationContext context: Context,
    ): PhoneDownDatabase = Room.databaseBuilder(
        context,
        PhoneDownDatabase::class.java,
        "phone_down_database",
    ).build()

    @Provides
    fun providesFocusSessionDao(
        database: PhoneDownDatabase,
    ): FocusSessionDao = database.focusSessionDao()

    @Provides
    fun providesPenaltyEventDao(
        database: PhoneDownDatabase,
    ): PenaltyEventDao = database.penaltyEventDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsSessionRepository(
        roomSessionRepository: RoomSessionRepository,
    ): SessionRepository
}
