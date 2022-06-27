#import "FlutterAlarmNotificationPlugin.h"
#if __has_include(<flutter_alarm_notification/flutter_alarm_notification-Swift.h>)
#import <flutter_alarm_notification/flutter_alarm_notification-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_alarm_notification-Swift.h"
#endif

@implementation FlutterAlarmNotificationPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterAlarmNotificationPlugin registerWithRegistrar:registrar];
}
@end
