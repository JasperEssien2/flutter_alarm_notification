# flutter_alarm_notification

A flutter plugin that takes advantage of [AlarmManager](https://developer.android.com/reference/kotlin/android/app/AlarmManager)s, [BroadcastReceiver](https://developer.android.com/guide/components/broadcasts)s to bring these features to your flutter app:

- Set repeating alarms at an exact or inexact time.
- Control alarm intervals.
- Cancel all registered alarms.
- Triggers a high-priority notification with an alarm ringtone.

Notification actions can be set up, and code can be run from the flutter side of the application to handle these events.

Here's a screen record of [FlutterAlarmNotification] in action.
> You can find the sample example in `/example/lib` folder.

![Screen record of [FlutterAlarmNotification] in action](example/display/alarm_display.gif)

### Getting Started
To get started do the following:

1. Add the package to `pubspec.yaml` file.
2. In the `main()` function, initialise the plugin.
    ```dart
    void main() async {
        /// Make sure to add this line of code
        WidgetsFlutterBinding.ensureInitialized();

        /// This line of code initializes the [FlutterAlarmNotification]
        FlutterAlarmNotification.initialize();

        runApp(const MyApp());
    }
    ```
### Setting alarm
To register an alarm, use the `FlutterAlarmNotification.registerRepeatingAlarm()` passing in `alarmConfig` and `notificationBuilder`.

#### Parameter `alarmConfig`

The parameter `alarmConfig` is of type `AlarmConfig` and is responsible for configuring everything related to registering an alarm.

Below is the constructor for the `AlarmConfig` class.

```dart
AlarmConfig(
    /// Sets the interval of the alarm, can be either of the following:
    /// intervalDay, intervalHalfDay, intervalFifteenMinutes, intervalHalfHour,  intervalHour,
    interval: AlarmInterval.intervalFifteenMinutes,

    /// The hour to set the alarm for
    alarmHour: alarmTime!.hour,

    /// The minute to set the alarm for
    alarmMinute: alarmTime!.minute,

    /// The second to set the alarm for
    alarmSecond: 0,

    /// Set the type of the alarm, read on to know the options
    alarmType: AlarmType.rtcWakeup,

    /// A unique id has to be passed, note you will need this Id to cancel an existing alarm
    requestId: _alarmRequestId,

    /// Set [useExact] to true, for precise alarm, set to [false] if the alarm isn't meant to be precise.
    useExact: true,
)
```

The [AlarmType] is an enum with these options:
- *`Alarmtype.elaspedRealtime`*: Triggers the alarm based on how much time has passed since the device was booted. It does not wake up the device is asleep.
    
    > For example, setting `alarmMinute` to `20` will trigger the alarm *20 minutes* after the device is booted.
- *`Alarmtype.elaspedRealtimeWakeup`* : Is the same as `Alarmtype.elaspedRealtime`, the difference is that setting this wakes up the device is asleep.

- *`Alarmtype.rtc`* : Triggers the alarm according to the time of the clock. It does not wake the device up if asleep.
    > For example, setting `alarmHour` to `12`, will trigger the alarm by *12pm*.

- *`Alarmtype.elaspedRealtime`* : Is the same as `Alarmtype.rtc`, the difference is that it wakes the device is asleep.

#### Parameter `notificationBuilder`

The parameter `notificationBuilder` is of type `NotificationBuilder`, it configures what is displayed when the notification is shown.

Here's the constructor:

```dart
NotificationBuilder(
    notificationTitle: "An example notification",
    notificationDescription:
        "This is the notification description",
    actions: [
        NotificationAction(
            actionText: "Okay",
            data: {"positive": 'go'},
        ),
        NotificationAction(
            actionText: "Cancel",
            data: {"negative": 'go'},
        ),
    ],
)
```
To set notification actions, pass a list of `NotificationAction` setting the following parameters:
- `actionText` : The action displayed on the button.
- `data` Pass any extra data needed to handle that notification action.

#### Handling notification action
There are two ways of handling notification actions.

1. Pass a callback function during initialization, like this.
    ```dart
    FlutterAlarmNotification.initialize(
        actionCallback: (action, data) {
            ///TODO: Write code to handle notification action 
        },
    );
    ```

2. Listening to the notification actions [`Stream`](https://api.flutter.dev/flutter/dart-async/Stream-class.html), by calling `FlutterAlarmNotification.listenable`. 
    ```dart
    void _listenToAction() async {
        /// Gets the stream
        final stream = FlutterAlarmNotification.listenable;

        stream.listen((message) {
            /// Push new screen based on notification action
            _pushNewScreen(message);
        });

        final isEmpty = await stream.isEmpty;

        /// If there was a pending event before [HomeScreen] was created, handle it
        if (!isEmpty) {
            final last = await stream.last;

            if (last != null) {
                _pushNewScreen(last);
            }
        }
       
    }

    void _pushNewScreen(message) {
        Navigator.push(
            context,
            MaterialPageRoute(
                builder: (c) => SecondScreen(
                /// Gets the notification action text
                action: message['action'],
                /// Get the notification data
                data: message['data'].toString(),
                ),
            ),
        );
    }
    ```

This plugin is still in its early stages, as such is missing some implementations. Feel free to contribute to this project.


### Checklist
- [x] Implement repeating alarms for android.
- [x] Run flutter code when notification is interacted with.
- [ ] Write unit test cases.
- [ ] Implement one-shot alarms for android.
- [ ] Flexibility in setting alarm ringtone.
- [ ] Implement these features in IOS.
