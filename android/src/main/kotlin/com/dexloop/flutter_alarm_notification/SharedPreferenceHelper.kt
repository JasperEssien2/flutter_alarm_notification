package com.dexloop.flutter_alarm_notification

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SharedPreferenceHelper {


    companion object {
        private const val PREFERENCE_KEY: String = "fullScreenAlarm";

        private lateinit var sharedPref: SharedPreferences

        fun cacheAlarmSettings(context: Context, alarmConfig: AlarmConfig) {
            sharedPref = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);

            val jsonString = Gson().toJson(alarmConfig)

            sharedPref.edit().putString(alarmConfig.requestId.toString(), jsonString).apply()
        }

        fun retrieveAllAlarmSettings(context: Context): List<AlarmConfig> {
            val list = mutableListOf<AlarmConfig>()

            sharedPref = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)

            sharedPref.all.forEach {
                list.add(Gson().fromJson(it.value as String, AlarmConfig::class.java))
            }

            return list.toList()
        }

        fun retrieveAlarmSettings(context: Context, requestId: Int): AlarmConfig? {
            sharedPref = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
            val json = sharedPref.getString(requestId.toString(), null)

            return Gson().fromJson(json, AlarmConfig::class.java)

        }

        fun removeAllAlarmSettings(context: Context) {
            sharedPref = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)

            sharedPref.all.forEach {
                removeAlarmSettingsCache(context = context, requestId = it.key.toInt())
            }
        }

        fun removeAlarmSettingsCache(context: Context, requestId: Int) {
            sharedPref = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
            sharedPref.edit().remove(requestId.toString()).apply()

        }
    }


}