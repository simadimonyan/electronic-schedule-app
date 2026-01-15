package com.mycollege.schedule.app.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import com.mycollege.schedule.feature.widgets.ui.ScheduleLargeWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleLargeWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = ScheduleLargeWidget()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        scheduleUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when(intent.action) {
            "android.appwidget.action.APPWIDGET_UPDATE" -> {
                Log.i("WIDGET_UPDATE", "UPDATE")
                coroutineScope.launch {
                    ScheduleLargeWidget().updateAll(context)
                }
                scheduleUpdate(context)
            }
        }
    }

    private fun scheduleUpdate(context: Context?) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ScheduleLargeWidgetReceiver::class.java).apply {
            action = "android.appwidget.action.APPWIDGET_UPDATE"
        }

        val pendingBroadcast = PendingIntent.getBroadcast(
            context,
            2024,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = SystemClock.elapsedRealtime() + 60_000L

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            pendingBroadcast
        )
    }

}