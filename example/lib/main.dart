import 'package:flutter/material.dart';
import 'package:flutter_alarm_notification/export.dart';

final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  FlutterAlarmNotification.initialize();

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: navigatorKey,
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  TimeOfDay? alarmTime;

  static const _alarmRequestId = 10;

  @override
  void initState() {
    super.initState();
    _listenToAction();
  }

  void _listenToAction() async {
    final stream = FlutterAlarmNotification.listenable;
    stream.listen((message) {
      _pushNewScreen(message);
    });

    final cachedMessage = await FlutterAlarmNotification.cachedMessage;

    if (cachedMessage != null) {
      _pushNewScreen(cachedMessage);
    }
  }

  void _pushNewScreen(message) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (c) => SecondScreen(
          data: message.toString(),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          children: [
            InkWell(
              onTap: () async {
                final time = await showTimePicker(
                  context: context,
                  initialTime: TimeOfDay.now(),
                );

                setState(() => alarmTime = time);

                if (alarmTime != null) {
                  await FlutterAlarmNotification.registerRepeatingAlarm(
                    alarmConfig: AlarmConfig(
                      interval: AlarmInterval.intervalFifteenMinutes,
                      alarmHour: alarmTime!.hour,
                      alarmMinute: alarmTime!.minute,
                      alarmSecond: 0,
                      alarmType: AlarmType.rtcWakeup,
                      requestId: _alarmRequestId,
                      useExact: true,
                    ),
                    notificationBuilder: NotificationBuilder(
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
                          launchAppOnTap: false,
                        ),
                      ],
                    ),
                  );
                }
              },
              child: Text(
                alarmTime == null
                    ? 'Tap to set alarm'
                    : "Alarm set for: ${alarmTime!.format(context)}",
                style: const TextStyle(fontSize: 20),
              ),
            ),
            const SizedBox(height: 32),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: TextButton(
                style: ButtonStyle(
                  elevation: MaterialStateProperty.all(5),
                  padding: MaterialStateProperty.all(
                    const EdgeInsets.symmetric(horizontal: 32, vertical: 8),
                  ),
                  fixedSize: MaterialStateProperty.all(const Size(300, 50)),
                  backgroundColor: MaterialStateProperty.all(
                    Colors.red[300],
                  ),
                ),
                onPressed: () async {
                  final successful = await FlutterAlarmNotification.cancelAlarm(
                      _alarmRequestId);
                  _showSnackbar(successful);
                },
                child: const Text(
                  "Cancel",
                  style: TextStyle(color: Colors.white),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  _showSnackbar(bool successful) {
    return ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(successful
            ? "Successfully cancelled alarm"
            : "An error occurred canceling alarms"),
      ),
    );
  }
}

class SecondScreen extends StatelessWidget {
  const SecondScreen({
    Key? key,
    required this.data,
  }) : super(key: key);

  final String data;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('From Notification'),
      ),
      body: Center(
        child: Text(
          "This Notification is with data $data",
          style: const TextStyle(fontSize: 20),
        ),
      ),
    );
  }
}
