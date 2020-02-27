import 'dart:async';

import 'package:flutter/services.dart';

class BeaconsPlugin {
  static const MethodChannel _channel = const MethodChannel('beacons_plugin');
  static const channel = EventChannel('beacons_plugin_stream');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> get startMonitoringItem async {
    final String version = await _channel.invokeMethod('startMonitoringItem');
    return version;
  }

  static Future<String> get sendParamsTest async {
    final String version =
        await _channel.invokeMethod('sendParams', <String, dynamic>{
      'someInfo1': "test123",
      'someInfo2': 3.22,
    });
    return version;
  }

  static listenToBeacons() async {
    channel.receiveBroadcastStream().listen((dynamic event) {
      print('Received event: $event');
    }, onError: (dynamic error) {
      print('Received error: ${error.message}');
    });
  }
}
