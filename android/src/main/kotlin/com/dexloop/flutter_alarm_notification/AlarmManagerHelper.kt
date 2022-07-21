package com.dexloop.flutter_alarm_notification

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.security.InvalidKeyException
import java.util.Calendar as javaCalendar


class AlarmManagerHelper {


    companion object {
        private var alarmMgr: AlarmManager? = null
        private lateinit var alarmIntent: PendingIntent

        fun setRepeatingAlarm(
            context: Context,
            @NonNull call: MethodCall,
            @NonNull result: MethodChannel.Result?,
            callbackDispatcherHandle: Long,
        ) {

            enableAlarmReceiverBroadcast(context)

            val actions = mutableListOf<Action>()

            call.argument<List<Map<String, Any>>>("actions")?.forEach {
                actions.add(
                    Action(
                        it["action_text"] as String,
                        it["data"] as HashMap<String, String>,
                    )
                )
            }


            val repeatingModel = AlarmConfig(
                interval = call.argument<String>("interval")!!,
                alarmHour = call.argument<Int>("alarm_hour")!!,
                alarmMinute = call.argument<Int?>("alarm_minute"),
                alarmSecond = call.argument<Int?>("alarm_second"),
                requestId = call.argument<Int>("request_id")!!,
                alarmType = call.argument<String>("alarm_type")!!,
                alarmPermissionReason = call.argument("alarm_permission_reason"),
                useExact = call.argument<Boolean>("use_exact") ?: false,
                notificationDescription = call.argument<String>("notification_description")!!,
                notificationTitle = call.argument<String>("notification_title")!!,
                notificationBackgroundColor = call.argument<String>("notification_background_color"),
                channelName = call.argument<String>("channel_name"),
                channelId = call.argument<String>("channel_id"),
                channelDescription = call.argument<String>("channel_description"),
                actions = actions,
                callbackDispatcherHandle = callbackDispatcherHandle,
                callbackHandle = call.argument<Long>("on_action_handle")!!,
            )


            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


            initAlarmIntent(context, repeatingModel)

            setAlarm(context, repeatingModel)

            SharedPreferenceHelper.cacheAlarmSettings(
                context = context,
                alarmConfig = repeatingModel
            )

            result?.success(mapOf("success" to "Successfully set alarm"))
        }

        private fun initAlarmIntent(
            context: Context,
            repeatingModel: AlarmConfig
        ) {
            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                intent.action = "com.dexloop.flutter_alarm_notification"
                intent.putExtra("data", Gson().toJson(repeatingModel))
                PendingIntent.getBroadcast(
                    context, repeatingModel.requestId, intent,
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                            or FLAG_UPDATE_CURRENT
                )
            }
        }

        private fun enableAlarmReceiverBroadcast(context: Context) {
            val receiver = ComponentName(context, AlarmReceiver::class.java)

            context.packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        private fun setAlarm(context: Context, config: AlarmConfig) {
            when (config.useExact) {
                true -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val doesNotHavePermission: Boolean = !alarmMgr!!.canScheduleExactAlarms()
                        Log.i(
                            "AlarmManagerHelper",
                            "DOES NOT HAVE PERMISSION: ${doesNotHavePermission}"
                        )

                        if (doesNotHavePermission)
                            requestPermissionToUseExact(context, alarmConfig = config)
                        else {
                            alarmMgr?.setRepeating(
                                config.alarmType(),
                                config.timeMilliSeconds(),
                                config.repeatIntervalType(),
                                alarmIntent
                            )
                        }
                    } else {
                        alarmMgr?.setRepeating(
                            config.alarmType(),
                            config.timeMilliSeconds(),
                            config.repeatIntervalType(),
                            alarmIntent
                        )
                        Log.i(AlarmManagerHelper.toString(), "Alarm set config $config")
                    }
                }
                false -> {
                    alarmMgr?.setInexactRepeating(
                        config.alarmType(),
                        config.timeMilliSeconds(),
                        config.repeatIntervalType(),
                        alarmIntent
                    )
                }
            }

        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun requestPermissionToUseExact(context: Context, alarmConfig: AlarmConfig) {

            if (context is Activity) {
                val builder = AlertDialog.Builder(context).setTitle("Permission to set alarm")

                    .setMessage(alarmConfig.alarmPermissionReason)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Accept") { dialogInterface, which ->
                        requestPermissions(
                            context,
                            arrayOf(
                                Manifest.permission.SCHEDULE_EXACT_ALARM
                            ), 1
                        )
                    }
                    .setNegativeButton("Reject") { dialogInterface, which ->
                        Toast.makeText(context, "Alarm cannot be set", Toast.LENGTH_LONG).show()
                    }

                val alertDialog: AlertDialog = builder.create()

                alertDialog.setCancelable(false)
                alertDialog.show()

            }
        }

        fun cancelAlarm(context: Context, requestId: Int, @NonNull result: MethodChannel.Result) {

            try {
                alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val config = SharedPreferenceHelper.retrieveAlarmSettings(context, requestId)
                if (config != null)
                    initAlarmIntent(
                        context,
                        SharedPreferenceHelper.retrieveAlarmSettings(context, requestId)!!
                    )


                alarmMgr!!.cancel(alarmIntent)
                SharedPreferenceHelper.removeAlarmSettingsCache(context, requestId)

                result.success(mapOf("success" to "Cancel alarm with requestId: $requestId"))
            } catch (e: Exception) {
                result.error(
                    e.toString(),
                    "An error occurred while trying to cancel alarm with requestId: $requestId",
                    null,
                )
            }
        }
    }
}

data class AlarmConfig(
    @SerializedName("alarm_hour")
    val alarmHour: Int,
    @SerializedName("alarm_minute")
    val alarmMinute: Int?,
    @SerializedName("alarm_second")
    val alarmSecond: Int?,
    @SerializedName("interval")
    val interval: String,
    @SerializedName("request_id")
    val requestId: Int,
    @SerializedName("alarm_type")
    val alarmType: String,
    @SerializedName("alarm_permission_reason")
    val alarmPermissionReason: String?,
    @SerializedName("use_exact")
    val useExact: Boolean = false,
    @SerializedName("notification_title")
    val notificationTitle: String,
    @SerializedName("notification_description")
    val notificationDescription: String,
    @SerializedName("notification_background_color")
    val notificationBackgroundColor: String?,

    @SerializedName("channel_name")
    val channelName: String?,

    @SerializedName("channel_id")
    val channelId: String?,

    @SerializedName("channel_description")
    val channelDescription: String?,

    @SerializedName("actions")
    val actions: List<Action>?,

    @SerializedName("callback_dispatcher_handle")
    val callbackDispatcherHandle: Long,

    @SerializedName("callback_handle")
    val callbackHandle: Long,
) {


    fun repeatIntervalType(): Long {
        return when (interval) {
            "intervalDay" -> AlarmManager.INTERVAL_DAY
            "intervalHalfDay" -> AlarmManager.INTERVAL_HALF_DAY
            "intervalFifteenMinutes" -> AlarmManager.INTERVAL_FIFTEEN_MINUTES
            "intervalHalfHour" -> AlarmManager.INTERVAL_HOUR
            "intervalHour" -> AlarmManager.INTERVAL_HALF_HOUR
            else -> {
                throw InvalidKeyException("$interval NOT RECOGNISED")
            }
        }
    }

    fun alarmType(): Int {

        return when (alarmType) {
            "elaspedRealtime" -> AlarmManager.ELAPSED_REALTIME
            "elaspedRealtimeWakeup" -> AlarmManager.ELAPSED_REALTIME_WAKEUP
            "rtc" -> AlarmManager.RTC
            "rtcWakeup" -> AlarmManager.RTC_WAKEUP
            else -> {
                throw  InvalidKeyException("$alarmType NOT RECOGNISED")
            }
        }
    }

    fun timeMilliSeconds(): Long {
        val timeMilliSeconds: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Calendar.getInstance().apply {

                set(Calendar.HOUR_OF_DAY, alarmHour)
                if (alarmMinute != null)
                    set(Calendar.MINUTE, alarmMinute)

                set(Calendar.SECOND, alarmSecond ?: 0)

                if(this <= Calendar.getInstance()){
                    // Add an additional day, if alarm is set before "now" time
                    // to prevent alarm from getting triggered
                    set(Calendar.DATE, this.get(Calendar.DATE) + 1)
                }


            }.timeInMillis

        } else {
            javaCalendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(javaCalendar.HOUR_OF_DAY, alarmHour)
                if (alarmMinute != null)
                    set(javaCalendar.MINUTE, alarmMinute)
                if (alarmSecond != null)
                    set(javaCalendar.SECOND, alarmSecond)

            }.timeInMillis
        }

        return timeMilliSeconds
    }
}

data class Action(

    @SerializedName("action_text")
    val actionText: String,

    @SerializedName("data")
    val data: HashMap<String, String>,
)
