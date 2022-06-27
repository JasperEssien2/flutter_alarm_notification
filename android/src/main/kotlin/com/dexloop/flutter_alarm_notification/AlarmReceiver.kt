package com.dexloop.flutter_alarm_notification

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.flutter.FlutterInjector
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Type


class AlarmReceiver : BroadcastReceiver(), MethodChannel.MethodCallHandler {

    companion object {
        const val BACKGROUND_CHANNEL = "background-channel"
    }


    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent!!.action.equals("android.intent.action.BOOT_COMPLETED") || intent.action.equals(
                AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
            )
        ) {
            reRegisterAllAlarm(context)
        } else if (intent.action.equals("com.dexloop.flutter_alarm_notification")) {

            val alarmConfig =
                Gson().fromJson(intent.getStringExtra("data"), AlarmConfig::class.java)

            context!!.showNotification(
                alarmConfig = alarmConfig,
            )
        } else if (intent.action.equals("fire-callback") || intent.action.equals("notification_tapped")) {

            invokeDartCallbackFunction(intent, context)
        }
    }

    private fun reRegisterAllAlarm(context: Context?) {

        SharedPreferenceHelper.retrieveAllAlarmSettings(context!!).forEach {
            val gson = Gson()

            val type: Type = object : TypeToken<Map<String?, String?>?>() {}.type
            val myMap: Map<String, String> = gson.fromJson(gson.toJson(it), type)

            AlarmManagerHelper.setRepeatingAlarm(
                context,
                MethodCall("", myMap),
                null,
                it.callbackDispatcherHandle,
            )
        }
    }


    private fun invokeDartCallbackFunction(
        intent: Intent,
        context: Context?
    ) {
        val flutterEngine = FlutterEngine(context!!)
        val callbackDispatcherHandle = intent.getLongExtra("callbackDispatcherHandle", 0)

        val appOnForeground = runBlocking {

            isAppOnForeground(context)
        }

        Log.i("AlarmReceiver", "isAppOnForeground ==== $appOnForeground")

        if (!appOnForeground) {
            val launchIntent: Intent = FlutterActivity
                .createDefaultIntent(context)

            launchIntent.flags = FLAG_ACTIVITY_NEW_TASK

            startActivity(
                context,
                launchIntent,
                null,
            )
        }
        val flutterLoader = FlutterInjector.instance().flutterLoader()

        val backgroundMethodChannel =
            MethodChannel(flutterEngine.dartExecutor, BACKGROUND_CHANNEL)

        backgroundMethodChannel.setMethodCallHandler(this)

        val flutterCallbackInformation =
            FlutterCallbackInformation.lookupCallbackInformation(callbackDispatcherHandle)

        flutterEngine.dartExecutor.executeDartCallback(
            DartExecutor.DartCallback(
                context.assets,
                flutterLoader.findAppBundlePath(),
                flutterCallbackInformation,
            )
        )

        val args = listOf<Any?>(
            intent.getLongExtra("callbackHandle", 0),
            intent.action!!,
            intent.getSerializableExtra("intentData"),
        )

        backgroundMethodChannel.invokeMethod("", args)
    }

    /**
     * Checks the state of the app
     * @return true if on foreground else false
     */
    private fun isAppOnForeground(context: Context): Boolean {

        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {

    }

}