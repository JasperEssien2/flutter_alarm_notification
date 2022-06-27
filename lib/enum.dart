
enum AlarmType {
  ///Fires the pending intent based on the amount of time since the device was booted,
  /// but doesn't wake up the device. The elapsed time includes any time
  ///  during which the device was asleep
  elaspedRealtime,
  elaspedRealtimeWakeup,
  rtc,
  rtcWakeup,

 
}

enum AlarmInterval{
  intervalDay,
  intervalHalfDay,
  intervalFifteenMinutes,
  intervalHalfHour,
  intervalHour,

}
