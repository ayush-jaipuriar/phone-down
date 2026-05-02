package phonedown.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat

data class FocusForegroundNotificationState(
    val title: String,
    val body: String,
    val ongoing: Boolean = true,
)

class FocusForegroundNotificationManager(
    private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Phone Down Focus",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Foreground notification for active Phone Down sessions"
                setShowBadge(false)
            }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildForegroundNotification(
        state: FocusForegroundNotificationState,
        contentIntent: PendingIntent,
        endSessionIntent: PendingIntent,
    ): Notification =
        NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(state.title)
            .setContentText(state.body)
            .setContentIntent(contentIntent)
            .setOngoing(state.ongoing)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "End Session",
                endSessionIntent,
            ).build()

    companion object {
        const val CHANNEL_ID = "phone_down_focus_runtime"
        const val NOTIFICATION_ID = 1001
    }
}
