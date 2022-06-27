import 'enum.dart';

class AlarmConfig {
  AlarmConfig({
    required this.interval,
    required this.alarmHour,
    this.alarmMinute = 0,
    this.alarmSecond,
    required this.alarmType,
    required this.requestId,
    this.alarmPermissionReason = "Needs permission to show a fullscreen alarm",
    this.useExact = false,
  });

  /// Sets the interval at which alarm is triggered
  final AlarmInterval interval;

  /// Set hour to trigger alarm
  final int alarmHour;

  /// Set minute to trigger alarm, defaults to [0]
  final int alarmMinute;

  /// Set second to trigger alarm, defaults to [0]
  final int? alarmSecond;

  /// Set hour to trigger alarm
  final AlarmType alarmType;

  /// For each alarm you set, a unique requestId is needed
  final int requestId;

  /// When requesting permission to set exact alarms,
  /// this sets the reason to be shown to the user
  final String? alarmPermissionReason;

  /// To schedule an alarm at a precise time in future set this [true],
  /// if your alarm doesn't depend on exact time set to [false]
  final bool useExact;

  Map<String, dynamic> get toMap => {
        'alarm_hour': alarmHour,
        'alarm_minute': alarmMinute,
        if (alarmSecond != null) 'alarm_second': alarmSecond,
        'interval': interval.toString().split('.').last,
        'request_id': requestId,
        'alarm_type': alarmType.toString().split('.').last,
        'alarm_permission_reason': alarmPermissionReason,
        'use_exact': useExact,
      };
}

class NotificationBuilder {
  NotificationBuilder({
    required this.notificationTitle,
    required this.notificationDescription,
    this.notificationBackgroundColor,
    this.channelName,
    this.channelId,
    this.channelDescription,
    this.actions,
  });

  /// When the notification view is shown, this is the title that will be shown
  final String notificationTitle;

  /// When the notification view is shown, this is the message that will be shown
  final String notificationDescription;

  final String? notificationBackgroundColor;

  ///From Android API level 26 upward, a channel must be created for
  ///notification, This sets the channel name.
  final String? channelName;

  ///From Android API level 26 upward, a channel must be created for
  ///notification, This sets the channel Id.
  final String? channelId;

  ///From Android API level 26 upward, a channel must be created for
  ///notification, This sets the channel description.
  final String? channelDescription;

  final List<NotificationAction>? actions;

  Map<String, dynamic> get toMap {
    return {
      'notification_title': notificationTitle,
      'notification_description': notificationDescription,
      'notification_background_color': notificationBackgroundColor,
      'channel_name': channelName,
      'channel_id': channelId,
      'channel_description': channelDescription,
      if (actions != null) 'actions': actions!.map((e) => e.toMap).toList(),
    };
  }
}

class NotificationAction {
  NotificationAction({
    required this.actionText,
    required this.data,
  });

  final Map<String, String> data;
  final String actionText;

  Map<String, dynamic> get toMap => {
        'action_text': actionText,
        'data': data,
      };
}
