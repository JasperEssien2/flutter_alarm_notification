import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_alarm_notification/flutter_alarm_notification.dart';
import 'package:flutter_alarm_notification/flutter_alarm_notification_platform_interface.dart';
import 'package:flutter_alarm_notification/flutter_alarm_notification_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterAlarmNotificationPlatform 
    with MockPlatformInterfaceMixin
    implements FlutterAlarmNotificationPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterAlarmNotificationPlatform initialPlatform = FlutterAlarmNotificationPlatform.instance;

  test('$MethodChannelFlutterAlarmNotification is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterAlarmNotification>());
  });

  test('getPlatformVersion', () async {
    FlutterAlarmNotification flutterAlarmNotificationPlugin = FlutterAlarmNotification();
    MockFlutterAlarmNotificationPlatform fakePlatform = MockFlutterAlarmNotificationPlatform();
    FlutterAlarmNotificationPlatform.instance = fakePlatform;
  
    expect(await flutterAlarmNotificationPlugin.getPlatformVersion(), '42');
  });
}
