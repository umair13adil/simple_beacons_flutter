import 'dart:async';

import 'package:flutter/services.dart';

class BeaconsPlugin {
  static const MethodChannel _channel =
      const MethodChannel('beacons_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> get startMonitoringItem async {
    final String version = await _channel.invokeMethod('startMonitoringItem');
    return version;
  }

  static Future<String> get sendParamsTest async {
    final String version = await _channel.invokeMethod('sendParams',<String, dynamic>{
      'someInfo1': "test123",
      'someInfo2': 3.22,
    });
    return version;
  }
}
