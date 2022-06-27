import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_alarm_notification/flutter_alarm_notification_method_channel.dart';

void main() {
  MethodChannelFlutterAlarmNotification platform = MethodChannelFlutterAlarmNotification();
  const MethodChannel channel = MethodChannel('flutter_alarm_notification');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
