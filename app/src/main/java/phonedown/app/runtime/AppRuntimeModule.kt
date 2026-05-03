@file:Suppress("TooManyFunctions")

package phonedown.app.runtime

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import phonedown.core.common.Clock
import phonedown.core.common.IdGenerator
import phonedown.core.model.repository.SessionRepository
import phonedown.core.notifications.FocusFeedbackPlayer
import phonedown.core.notifications.FocusForegroundNotificationManager
import phonedown.core.sensors.AndroidFocusValidityMonitor
import phonedown.core.sensors.FocusValidityMonitor
import phonedown.domain.session.EndSessionUseCase
import phonedown.domain.session.RecoverSessionsUseCase
import phonedown.domain.session.SessionEngine
import phonedown.domain.session.SessionRecoveryClassifier
import phonedown.domain.session.StartSessionUseCase
import phonedown.domain.insights.GetTodayInsightsUseCase
import phonedown.domain.insights.GetWeeklyInsightsUseCase
import phonedown.domain.insights.GetFocusQualityUseCase
import phonedown.domain.insights.GetStreakUseCase
import phonedown.domain.insights.GetBestHourUseCase
import phonedown.domain.insights.GetBestWeekdayUseCase
import phonedown.domain.insights.GetTrendsUseCase
import phonedown.domain.insights.GetAdvancedInsightsUseCase
import phonedown.domain.insights.GetHeatmapDataUseCase
import phonedown.domain.insights.GetHistoryUseCase
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppRuntimeModule {
    @Provides
    @Singleton
    fun providesClock(): Clock =
        object : Clock {
            override fun currentTimeMillis(): Long = System.currentTimeMillis()

            override fun elapsedRealtimeMillis(): Long = android.os.SystemClock.elapsedRealtime()
        }

    @Provides
    @Singleton
    fun providesIdGenerator(): IdGenerator = IdGenerator { UUID.randomUUID().toString() }

    @Provides
    @Singleton
    fun providesSessionEngine(
        clock: Clock,
        idGenerator: IdGenerator,
    ): SessionEngine = SessionEngine(clock = clock, idGenerator = idGenerator)

    @Provides
    @Singleton
    fun providesSessionRecoveryClassifier(clock: Clock): SessionRecoveryClassifier = SessionRecoveryClassifier(clock)

    @Provides
    @Singleton
    fun providesStartSessionUseCase(
        sessionEngine: SessionEngine,
        sessionRepository: SessionRepository,
    ): StartSessionUseCase = StartSessionUseCase(sessionEngine, sessionRepository)

    @Provides
    @Singleton
    fun providesEndSessionUseCase(
        sessionEngine: SessionEngine,
        sessionRepository: SessionRepository,
    ): EndSessionUseCase {
        val processInputUseCase =
            phonedown.domain.session.ProcessSessionInputUseCase(
                sessionEngine = sessionEngine,
                sessionRepository = sessionRepository,
            )
        return EndSessionUseCase(processInputUseCase)
    }

    @Provides
    @Singleton
    fun providesRecoverSessionsUseCase(
        sessionRepository: SessionRepository,
        sessionRecoveryClassifier: SessionRecoveryClassifier,
    ): RecoverSessionsUseCase = RecoverSessionsUseCase(sessionRepository, sessionRecoveryClassifier)

    @Provides
    @Singleton
    fun providesFocusValidityMonitor(
        @ApplicationContext context: Context,
    ): FocusValidityMonitor = AndroidFocusValidityMonitor(context)

    @Provides
    @Singleton
    fun providesCallInterruptionMonitor(
        @ApplicationContext context: Context,
    ): CallInterruptionMonitor = AndroidCallInterruptionMonitor(context)

    @Provides
    @Singleton
    fun providesNotificationManager(
        @ApplicationContext context: Context,
    ): FocusForegroundNotificationManager = FocusForegroundNotificationManager(context)

    @Provides
    @Singleton
    fun providesFeedbackPlayer(
        @ApplicationContext context: Context,
    ): FocusFeedbackPlayer = FocusFeedbackPlayer(context)

    @Provides
    @Singleton
    fun providesGetTodayInsightsUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetTodayInsightsUseCase = GetTodayInsightsUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetWeeklyInsightsUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetWeeklyInsightsUseCase = GetWeeklyInsightsUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetFocusQualityUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetFocusQualityUseCase = GetFocusQualityUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetStreakUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetStreakUseCase = GetStreakUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetBestHourUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetBestHourUseCase = GetBestHourUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetBestWeekdayUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetBestWeekdayUseCase = GetBestWeekdayUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetTrendsUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetTrendsUseCase = GetTrendsUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetAdvancedInsightsUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetAdvancedInsightsUseCase = GetAdvancedInsightsUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetHeatmapDataUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetHeatmapDataUseCase = GetHeatmapDataUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetHistoryUseCase(
        sessionRepository: SessionRepository,
    ): GetHistoryUseCase = GetHistoryUseCase(sessionRepository)
}
