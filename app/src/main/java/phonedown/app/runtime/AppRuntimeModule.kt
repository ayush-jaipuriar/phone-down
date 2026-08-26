@file:Suppress("TooManyFunctions")

package phonedown.app.runtime

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import phonedown.app.BuildConfig
import phonedown.app.account.BackupRestorer
import phonedown.app.account.RestoreBackupUseCase
import phonedown.app.backup.AutoBackupScheduler
import phonedown.app.backup.AutoBackupScheduling
import phonedown.app.backup.DriveAuthorizationCoordinator
import phonedown.app.backup.GoogleDriveAuthorizationManager
import phonedown.core.auth.DataStoreAuthRepository
import phonedown.core.backup.DriveAppDataClient
import phonedown.core.backup.DriveBackupRepository
import phonedown.core.billing.BillingActivityProvider
import phonedown.core.billing.RealBillingRepository
import phonedown.core.common.Clock
import phonedown.core.common.IdGenerator
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.DriveAccessTokenProvider
import phonedown.core.model.repository.SessionRepository
import phonedown.core.notifications.FocusFeedbackPlayer
import phonedown.core.notifications.FocusForegroundNotificationManager
import phonedown.core.sensors.AndroidFocusValidityMonitor
import phonedown.core.sensors.FocusSensorConfig
import phonedown.core.sensors.FocusValidityMonitor
import phonedown.domain.insights.GetAdvancedInsightsUseCase
import phonedown.domain.insights.GetBestHourUseCase
import phonedown.domain.insights.GetBestWeekdayUseCase
import phonedown.domain.insights.GetDayInsightsUseCase
import phonedown.domain.insights.GetFocusQualityUseCase
import phonedown.domain.insights.GetHeatmapDataUseCase
import phonedown.domain.insights.GetHistoryUseCase
import phonedown.domain.insights.GetHourlyFocusUseCase
import phonedown.domain.insights.GetStreakUseCase
import phonedown.domain.insights.GetTodayInsightsUseCase
import phonedown.domain.insights.GetTrendsUseCase
import phonedown.domain.insights.GetWeeklyInsightsUseCase
import phonedown.domain.session.EndSessionUseCase
import phonedown.domain.session.RecoverSessionsUseCase
import phonedown.domain.session.SessionEngine
import phonedown.domain.session.SessionRecoveryClassifier
import phonedown.domain.session.StartSessionUseCase
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
    fun providesBackupRestorer(restoreBackupUseCase: RestoreBackupUseCase): BackupRestorer = restoreBackupUseCase

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
    ): FocusValidityMonitor =
        AndroidFocusValidityMonitor(
            context = context,
            config = FocusSensorConfig(),
            debugDiagnosticsEnabled = BuildConfig.DEBUG,
        )

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
    fun providesGetHistoryUseCase(sessionRepository: SessionRepository): GetHistoryUseCase = GetHistoryUseCase(sessionRepository)

    @Provides
    @Singleton
    fun providesEntitlementCache(
        dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>,
    ): phonedown.core.model.repository.EntitlementCache =
        phonedown.core.datastore.cache
            .ProEntitlementCache(dataStore)

    @Provides
    @Singleton
    fun providesBillingActivityProvider(foregroundActivityProvider: ForegroundActivityProvider): BillingActivityProvider =
        foregroundActivityProvider

    @Provides
    @Singleton
    fun providesBillingRepository(
        @ApplicationContext context: Context,
        cache: phonedown.core.model.repository.EntitlementCache,
        billingActivityProvider: BillingActivityProvider,
    ): BillingRepository = RealBillingRepository(context, cache, billingActivityProvider)

    @Provides
    @Singleton
    fun providesAuthRepository(dataStore: DataStore<Preferences>): AuthRepository = DataStoreAuthRepository(dataStore)

    @Provides
    @Singleton
    fun providesDriveAccessTokenProvider(googleDriveAuthorizationManager: GoogleDriveAuthorizationManager): DriveAccessTokenProvider =
        googleDriveAuthorizationManager

    @Provides
    @Singleton
    fun providesDriveAuthorizationCoordinator(
        googleDriveAuthorizationManager: GoogleDriveAuthorizationManager,
    ): DriveAuthorizationCoordinator = googleDriveAuthorizationManager

    @Provides
    @Singleton
    fun providesAutoBackupScheduling(autoBackupScheduler: AutoBackupScheduler): AutoBackupScheduling = autoBackupScheduler

    @Provides
    @Singleton
    fun providesBackupRepository(
        driveAccessTokenProvider: DriveAccessTokenProvider,
        settingsRepository: phonedown.core.model.repository.SettingsRepository,
        clock: Clock,
    ): phonedown.core.model.repository.BackupRepository =
        DriveBackupRepository(
            driveAppDataClient = DriveAppDataClient(),
            driveAccessTokenProvider = driveAccessTokenProvider,
            settingsRepository = settingsRepository,
            clock = clock,
        )

    @Provides
    @Singleton
    fun providesGetHourlyFocusUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetHourlyFocusUseCase = GetHourlyFocusUseCase(sessionRepository, clock)

    @Provides
    @Singleton
    fun providesGetDayInsightsUseCase(
        sessionRepository: SessionRepository,
        clock: Clock,
    ): GetDayInsightsUseCase = GetDayInsightsUseCase(sessionRepository, clock)
}
