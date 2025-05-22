package com.example.myapplication.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.myapplication.receivers.AlarmReceiver

// AI PROMPT: HELP ME TO CREATE AN ALARM WHICH STARTS SOUNDING AFTER 30 SEGS

object AlarmManagerUtil {
    private const val ALARM_REQUEST_CODE = 0

    fun setOneTimeAlarm(context: Context, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)


        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)


        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}


object AlarmSoundPlayer {
    private var ringtone: Ringtone? = null

    fun playAlarm(context: Context) {
        val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(context, notificationSound)
        ringtone?.play()
    }

    fun stopAlarm() {
        ringtone?.stop()
        ringtone = null
    }
}
