import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'export.dart';
import 'flutter_alarm_notification_method_channel.dart';

void callbackDispatcher() {
  const backgroundChannel = MethodChannel("background-channel");

  debugPrint("AlarmReceiver FLUTTER SIDE ====== callbackDispatcher ");
  WidgetsFlutterBinding.ensureInitialized();

  backgroundChannel.setMethodCallHandler(
    (call) async {
      final args = call.arguments;

      final callbackThis = PluginUtilities.getCallbackFromHandle(
          CallbackHandle.fromRawHandle(args[0]));

      assert(callbackThis != null);

      String action = args[1];
      Map data = args[2];

      callbackThis!(action, data);
    },
  );
}

abstract class FlutterAlarmNotificationPlatform extends PlatformInterface {
  /// Constructs a FlutterAlarmNotificationPlatform.
  FlutterAlarmNotificationPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterAlarmNotificationPlatform _instance =
      MethodChannelFlutterAlarmNotification();

  /// The default instance of [FlutterAlarmNotificationPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterAlarmNotification].
  static FlutterAlarmNotificationPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterAlarmNotificationPlatform] when
  /// they register themselves.
  static set instance(FlutterAlarmNotificationPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  static Future<void> initialize() async {
    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);

    var raw = callback!.toRawHandle();

    instance.initializeService(raw);
  }

  Future<void> registerRepeatingAlarm({
    required AlarmConfig alarmConfig,
    required NotificationBuilder notificationBuilder,
    required Function(dynamic action, Map data) onAction,
  });

  Future<bool> cancelAlarm(int requestId);

  Future<void> cancelAllAlarm();

  Future<void> dismissNotification();

  Future<void> initializeService(int callbackRaw);
}
