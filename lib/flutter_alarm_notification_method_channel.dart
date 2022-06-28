import 'dart:developer';
import 'dart:ui';

import 'package:flutter/services.dart';

import 'export.dart';
import 'flutter_alarm_notification_platform_interface.dart';

/// An implementation of [FlutterAlarmNotificationPlatform] that uses method channels.
class MethodChannelFlutterAlarmNotification
    extends FlutterAlarmNotificationPlatform {
  static const _registerAlarm = "registerRepeatingAlarm";
  static const _cancelAlarm = "cancelAlarm";
  static const _cancelAllAlarm = "cancelAllAlarm";
  static const _initialiseService = 'initializeService';
  static const _dismissNotification = 'dismissNotification';
  static const _cachedMessage = 'retrieveCacheActionMessage';

  /// The method channel used to interact with the native platform.
  static const methodChannel = MethodChannel('flutter_alarm_notification');

  @override
  Future<void> registerRepeatingAlarm({
    required AlarmConfig alarmConfig,
    required NotificationBuilder notificationBuilder,
    required Function(bool fromForeground, Map data) onAction,
  }) async {
    var raw = PluginUtilities.getCallbackHandle(onAction);

    await methodChannel.invokeMapMethod(
      _registerAlarm,
      alarmConfig.toMap
        ..addAll(notificationBuilder.toMap)
        ..addAll({'on_action_handle': raw!.toRawHandle()}),
    );
  }

  @override
  Future<bool> cancelAlarm(int requestId) async {
    try {
      await methodChannel.invokeMethod(_cancelAlarm, {"request_id": requestId});
      return true;
    } catch (e) {
      log(e.toString());
      return false;
    }
  }

  @override
  Future<void> cancelAllAlarm() async {
    await methodChannel.invokeMapMethod(_cancelAllAlarm);
  }

  @override
  Future<void> initializeService(int callbackRaw) async {
    await methodChannel
        .invokeMethod(_initialiseService, <dynamic>[callbackRaw]);
  }

  @override
  Future<void> dismissNotification() async {
    await methodChannel.invokeMapMethod(_dismissNotification);
  }

  @override
  Future<String?> retrieveCachedMessage() async {
    return await methodChannel.invokeMethod(_cachedMessage);
  }
}
