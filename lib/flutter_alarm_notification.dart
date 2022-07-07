import 'dart:convert';
import 'dart:isolate';
import 'dart:ui';
import 'package:flutter/foundation.dart';
import 'export.dart';
import 'flutter_alarm_notification_platform_interface.dart';

typedef NotificationActionCallback = Function(Map? message);

const String _buttonActionIsolateName = "button-action-isolate";

class FlutterAlarmNotification {
  static final _uiReceivePort = ReceivePort();

  static Future<Map?> get cachedMessage async {
    final String? message =
        await FlutterAlarmNotificationPlatform.instance.retrieveCachedMessage();
    debugPrint(
        "Message json ============= $message == TIME : ${DateTime.now().millisecondsSinceEpoch}");
    if (message == null) return null;
    final messageJson = json.decode(message..replaceAll("\n", ""));

    return messageJson;
  }

  static Stream<dynamic> get listenable => _uiReceivePort.asBroadcastStream();

  static Future<void> initialize(
      {NotificationActionCallback? actionCallback}) async {
    registerPort(callback: actionCallback);

    await FlutterAlarmNotificationPlatform.initialize();
  }

  static registerPort({NotificationActionCallback? callback}) {
    IsolateNameServer.registerPortWithName(
      _uiReceivePort.sendPort,
      _buttonActionIsolateName,
    );

    if (callback != null) {
      listenable.listen((message) {
        callback(message);
      });
    }
  }

  @visibleForTesting
  static void onAction(bool fromForeground, Map message) async {
    final uiSendport =
        IsolateNameServer.lookupPortByName(_buttonActionIsolateName);

    uiSendport?.send(message);
    FlutterAlarmNotificationPlatform.instance.dismissNotification();
  }

  /// Set a recursive alarms that sets of at a particular time
  /// and repeats at an interval
  /// To set configurations for this alram use [alarmConfig]
  /// [notificationBuilder] accepts fields that's pertaining to the notification
  /// if you need to pass data to the intent set [intentBuilder] parameter .
  static Future<void> registerRepeatingAlarm({
    required AlarmConfig alarmConfig,
    required NotificationBuilder notificationBuilder,
  }) async {
    return FlutterAlarmNotificationPlatform.instance.registerRepeatingAlarm(
      alarmConfig: alarmConfig,
      notificationBuilder: notificationBuilder,
      onAction: onAction,
    );
  }

  /// Cancel an existing alarm with [requestId] by calling this method
  /// Returns [true] if successful, otherwise [false]
  static Future<bool> cancelAlarm(int requestId) async {
    return FlutterAlarmNotificationPlatform.instance.cancelAlarm(requestId);
  }

  /// Cancel all existing alarms by calling this method
  static Future<void> cancelAllAlarm() async {
    return FlutterAlarmNotificationPlatform.instance.cancelAllAlarm();
  }
}
