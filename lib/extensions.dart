
extension DurationExt on Duration {
  Map<String, dynamic> get toMap => {
        'hour': inHours,
        'minutes': inMinutes,
        'seconds': inSeconds,
        'milliseconds': inMilliseconds,
        'microseconds': inMicroseconds,
      };
}
