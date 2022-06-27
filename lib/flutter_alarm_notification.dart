import 'dart:developer';
import 'dart:isolate';
import 'dart:ui';

import 'package:flutter/foundation.dart';

import 'export.dart';
import 'flutter_alarm_notification_platform_interface.dart';

typedef NotificationActionCallback = Function(dynamic action, Map? data);

const String _buttonActionIsolateName = "button-action-isolate";

class FlutterAlarmNotification {
  static final _uiReceivePort = ReceivePort();
  static final Map _cache = {};

  static Map get cache {
    final cache = Map.from(_cache);

    _cache.clear();

    return cache;
  }

  static Stream<dynamic> get listenable => _uiReceivePort.asBroadcastStream();

  static registerPort({NotificationActionCallback? callback}) {
    IsolateNameServer.registerPortWithName(
        _uiReceivePort.sendPort, _buttonActionIsolateName);

    if (callback != null) {
      listenable.listen((message) {
        log("UI RECEIVE PORT GOT A MESSAGE ====== $message");
        callback(message['action'], message['data']);
      });
    }
  }

  static Future<void> initialize(
      {NotificationActionCallback? actionCallback}) async {

    registerPort(callback: actionCallback);

    await FlutterAlarmNotificationPlatform.initialize();
  }

  @visibleForTesting
  static void onAction(dynamic action, Map data) {
    log("FlutterSide === onAction($action, $data)");

    final uiSendport =
        IsolateNameServer.lookupPortByName(_buttonActionIsolateName);

    final message = {
      'action': action,
      'data': data,
    };
    _cache.addAll(message);
    uiSendport?.send(message);
    FlutterAlarmNotificationPlatform.instance.dismissNotification();
  }

  /// Set a recursive alarms that sets of at a particular time
  /// and repeats at an interval
  /// To set configurations for this alram use [alarmConfig]
  /// [nottificationBuilder] accepts fields that's pertaining to the notification
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
