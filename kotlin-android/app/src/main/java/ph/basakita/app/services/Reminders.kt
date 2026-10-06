package ph.basakita.app.services

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ph.basakita.app.MainActivity
import ph.basakita.app.R
import ph.basakita.app.state.AppState
import ph.basakita.app.state.PrefsStore
import java.util.Calendar

private const val CHANNEL = "reading-reminder"
private const val ID = 4001

/**
 * The daily 4:00 PM reading reminder. These are real notifications that arrive even when
 * the app is closed, and they come back after the phone restarts.
 */
object Reminders {
    private fun pending(context: Context) = PendingIntent.getBroadcast(
        context, ID, Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun schedule(context: Context) {
        val at = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val alarms = context.getSystemService(AlarmManager::class.java)
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, at.timeInMillis, AlarmManager.INTERVAL_DAY, pending(context))
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context))
    }

    /** Turns the reminder on or off. Returns false when the phone blocked notifications. */
    suspend fun set(context: Context, on: Boolean): Boolean {
        if (!on) {
            cancel(context)
            return true
        }
        if (Build.VERSION.SDK_INT >= 33 && !Permissions.request(context, Manifest.permission.POST_NOTIFICATIONS)) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        schedule(context)
        return true
    }

    fun show(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Daily reading reminder", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to read! 📚")
            .setContentText("Your BASA-KITA stories are waiting for you.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        if (Build.VERSION.SDK_INT < 33 || Permissions.has(context, Manifest.permission.POST_NOTIFICATIONS)) nm.notify(ID, n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Reminders.show(context)
}

/** Puts the reminder back after the phone restarts or the app is updated. */
class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (AppState(PrefsStore(context)).settings.reminder) Reminders.schedule(context)
    }
}
