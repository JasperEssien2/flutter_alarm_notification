package com.dexloop.flutter_alarm_notification

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FullScreenAlarmPlugin */
class FlutterAlarmNotificationPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var context: Context

    private var callbackDispatcherHandle: Long? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_alarm_notification")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

        when (call.method) {
            "initializeService" -> {
                val args = call.arguments as ArrayList<*>

                callbackDispatcherHandle = (args[0] as Long)


                result.success(null)
            }
            "registerRepeatingAlarm" -> {

                AlarmManagerHelper.setRepeatingAlarm(
                    context, call, result,
                    callbackDispatcherHandle!!,

                )
            }

            "cancelAlarm" -> {
                AlarmManagerHelper.cancelAlarm(context, call.argument<Int>("request_id")!!, result)
            }

            "cancelAllAlarm" -> {
                for (alarmConfig in SharedPreferenceHelper.retrieveAllAlarmSettings(context)) {
                    AlarmManagerHelper.cancelAlarm(context, alarmConfig.requestId, result)
                }
            }

            "dismissNotification" -> {
                context.dismissNotification()
            }

            "retrieveCacheActionMessage" -> {
                result.success(SharedPreferenceHelper.retrieveActionMessage(context))
            }
        }

    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


}

