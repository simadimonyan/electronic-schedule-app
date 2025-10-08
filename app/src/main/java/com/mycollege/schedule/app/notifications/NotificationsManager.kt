package com.mycollege.schedule.app.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mycollege.schedule.R
import com.mycollege.schedule.core.cache.CacheManager
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@Immutable
class NotificationsManager {

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            "GROUP_SYNC_CHANNEL",
            "Schedule Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for schedule work"
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    fun createNotification(context: Context, message: String): Notification {
        return NotificationCompat.Builder(context, "GROUP_SYNC_CHANNEL")
            .setSmallIcon(R.drawable.notification, 0)
            .setContentTitle(context.getString(R.string.update_schedule))
            .setContentText(message)
            .setProgress(100, 0, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(false)
            .setSilent(true)
            .build()
    }

    fun createLessonAlertNotification(context: Context, message: String, lessonCount: Int, date: String): Notification {

        val notificationId = "${date}-${lessonCount}".hashCode()
        val deleteIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra("lesson", message)
            putExtra("date", date)
            putExtra("notificationId", notificationId)
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            deleteIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, "GROUP_SYNC_CHANNEL")
            .setSmallIcon(R.drawable.notification, 0)
            .setContentTitle(context.getString(R.string.lesson_alert))
            .setContentText(message)
            .setDeleteIntent(deletePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(false)
            .build()
    }

    fun updateProgressNotification(id: Int, context: Context, progress: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val updatedNotification = NotificationCompat.Builder(context, "GROUP_SYNC_CHANNEL")
            .setSmallIcon(R.drawable.notification, 0)
            .setContentTitle(context.getString(R.string.update_schedule))
            .setContentText(context.getString(R.string.get_data))
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSilent(true)
            .setOngoing(false)
            .build()

        notificationManager.notify(id, updatedNotification)
    }

    fun cancelNotification(id: Int, context: Context) {
        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
    }

}

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var cacheManager: CacheManager

    override fun onReceive(context: Context, intent: Intent) {
        val settings = cacheManager.loadLastSettings()
        if (settings == null || settings.notificationsEnabled) {

            val lesson = intent.getStringExtra("lesson")
            val timestamp = intent.getLongExtra("timestamp", 0L)
            val manager = NotificationsManager()
            val date = LocalDate.now(ZoneId.systemDefault())
            val notificationId = "${date}-${lesson}".hashCode()

            val notification = manager.createLessonAlertNotification(context, lesson.toString(), notificationId, date.toString())

            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            if (timestamp >= System.currentTimeMillis()) {
                notificationManager.notify(lesson.hashCode(), notification)
            }

        }
    }

}

@AndroidEntryPoint
class NotificationDismissReceiver : BroadcastReceiver() {

    @Inject lateinit var cacheManager: CacheManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationDismissReceiver", "Получено намерение смахивания: ${intent.extras?.keySet()?.joinToString()}")
        val lesson = intent.getStringExtra("lesson")
        val date = LocalDate.now(ZoneId.systemDefault()).dayOfWeek.toString()
        val notificationId = intent.getIntExtra("notificationId", -1)
        if (lesson != null) {
            cacheManager.saveDismissedNotification(date, lesson)
            Log.d("NotificationDismissReceiver", "Уведомление смахнуто: notificationId=$notificationId, lesson=$lesson, date=$date")
        } else {
            Log.e("NotificationDismissReceiver", "Неверные данные: lesson=$lesson, date=$date, notificationId=$notificationId")
        }
    }
}
