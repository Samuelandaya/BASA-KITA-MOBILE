import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_timezone/flutter_timezone.dart';
import 'package:timezone/data/latest_all.dart' as tzdata;
import 'package:timezone/timezone.dart' as tz;

final FlutterLocalNotificationsPlugin _plugin = FlutterLocalNotificationsPlugin();
bool _ready = false;
const _id = 4001;

Future<void> _init() async {
  if (_ready) return;
  tzdata.initializeTimeZones();
  try {
    tz.setLocalLocation(tz.getLocation((await FlutterTimezone.getLocalTimezone()).identifier));
  } catch (_) {
    tz.setLocalLocation(tz.getLocation('Asia/Manila'));
  }
  await _plugin.initialize(
    settings: const InitializationSettings(
      android: AndroidInitializationSettings('@mipmap/ic_launcher'),
      iOS: DarwinInitializationSettings(requestAlertPermission: false, requestBadgePermission: false, requestSoundPermission: false),
    ),
  );
  _ready = true;
}

/// Asks for permission when needed. Returns true when reminders are allowed.
Future<bool> _allowed() async {
  final android = _plugin.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
  final ios = _plugin.resolvePlatformSpecificImplementation<IOSFlutterLocalNotificationsPlugin>();
  if (android != null) {
    final ok = await android.requestNotificationsPermission();
    return ok ?? false;
  }
  if (ios != null) return await ios.requestPermissions(alert: true, badge: true, sound: true) ?? false;
  return false;
}

/// Turns the daily 4:00 PM reading reminder on or off. These are real notifications that
/// arrive even when the app is closed. Returns false when the phone blocked notifications.
Future<bool> setDailyReminder(bool on) async {
  try {
    await _init();
    if (!on) {
      await _plugin.cancel(id: _id);
      return true;
    }
    if (!await _allowed()) return false;
    final now = tz.TZDateTime.now(tz.local);
    var at = tz.TZDateTime(tz.local, now.year, now.month, now.day, 16);
    if (at.isBefore(now)) at = at.add(const Duration(days: 1));
    await _plugin.zonedSchedule(
      id: _id,
      title: 'Time to read! 📚',
      body: 'Your BASA-KITA stories are waiting for you.',
      scheduledDate: at,
      notificationDetails: const NotificationDetails(
        android: AndroidNotificationDetails('reading-reminder', 'Daily reading reminder', importance: Importance.defaultImportance),
        iOS: DarwinNotificationDetails(),
      ),
      androidScheduleMode: AndroidScheduleMode.inexactAllowWhileIdle,
      matchDateTimeComponents: DateTimeComponents.time,
    );
    return true;
  } catch (e) {
    debugPrint('Reminder failed: $e');
    return false;
  }
}
