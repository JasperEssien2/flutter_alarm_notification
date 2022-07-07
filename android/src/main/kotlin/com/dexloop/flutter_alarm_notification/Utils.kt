package com.dexloop.flutter_alarm_notification

/*
 * MIT License
 *
 * Copyright (c) 2020 Giorgos Neokleous
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

const val NOTIFICATION_ID = 456

fun Context.showNotification(
    alarmConfig: AlarmConfig
) {
    val appInfo =
        this.packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA)


    val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    val builder = NotificationCompat.Builder(this, alarmConfig.channelId ?: CHANNEL_ID)
        .setSmallIcon(appInfo.icon)
        .setContentTitle(alarmConfig.notificationTitle)
        .setLargeIcon((packageManager.getApplicationIcon(appInfo) as BitmapDrawable).bitmap)
        .setContentText(alarmConfig.notificationDescription)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setDefaults(Notification.DEFAULT_VIBRATE)
        .setSound(notificationSound)
        .setLights(Color.RED, 3000, 3000)
        .setVibrate(
            longArrayOf(
                0, 100, 200, 300
            )
        ).setSilent(false)
        .setFullScreenIntent(
            pendingIntent(
                requestCode = 35,
                action = "notification_tapped",
                intentData =  hashMapOf(),
                callbackHandle = alarmConfig.callbackHandle,
                callbackDispatcherHandle = alarmConfig.callbackDispatcherHandle,
                launchAppOnTap = true,
            ), true
        )

    if (alarmConfig.actions != null)
        for (action in alarmConfig.actions) {

            val actionBtn = NotificationCompat.Action.Builder(
                0, action.actionText,
                pendingIntent(
                    requestCode = alarmConfig.actions.indexOf(action) + 5,
                    action = "fire-callback",
                    intentData = action.data,
                    callbackHandle = alarmConfig.callbackHandle,
                    callbackDispatcherHandle = alarmConfig.callbackDispatcherHandle,
                    launchAppOnTap = action.launchAppOnTap,
                ),
            ).build()

            builder.addAction(actionBtn)

        }


    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    with(notificationManager) {
        buildChannel(
            channelName = alarmConfig.channelName,
            channelId = alarmConfig.channelId,
            channelDescription = alarmConfig.channelDescription,
        )

        val notification = builder.build()

        notify(NOTIFICATION_ID, notification)
    }
}

fun Context.dismissNotification() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    with(notificationManager) {
        cancel(NOTIFICATION_ID)
    }
}

private fun NotificationManager.buildChannel(
    channelName: String?, channelId: String?, channelDescription: String?
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = channelName ?: "Notification Channel"
        val descriptionText = channelDescription ?: "This is used to open a full screen intent"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId ?: CHANNEL_ID, name, importance).apply {
            description = descriptionText
            lightColor = Color.RED
        }

        channel.enableLights(true)

        val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setLegacyStreamType(AudioManager.STREAM_ALARM)
            .build()
        channel.setSound(notificationSound, audioAttributes)

        createNotificationChannel(channel)
    }
}

private fun Context.pendingIntent(

    requestCode: Int,
    action: String?,
    intentData: HashMap<String, String>?,
    callbackDispatcherHandle: Long,
    callbackHandle: Long,
    launchAppOnTap: Boolean,
): PendingIntent {

    val intent = Intent(this, AlarmReceiver::class.java)
    intent.action = action
    intent.putExtra("intentData", intentData)
    intent.putExtra("packageName", packageName)
    intent.putExtra("callbackDispatcherHandle", callbackDispatcherHandle)
    intent.putExtra("callbackHandle", callbackHandle)
    intent.putExtra("launchAppOnTap", launchAppOnTap)


    // flags and request code are 0 for the purpose of demonstration
    return PendingIntent
        .getBroadcast(
            this,
            requestCode,
            intent,
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                    or PendingIntent.FLAG_UPDATE_CURRENT
        )
}

private const val CHANNEL_ID = "channelId"
