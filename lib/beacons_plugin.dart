import 'dart:async';
import 'dart:ffi';

import 'package:flutter/services.dart';

class BeaconsPlugin {
  static const MethodChannel channel = const MethodChannel('beacons_plugin');
  static const event_channel = EventChannel('beacons_plugin_stream');

  static Future<String> get startMonitoring async {
    final String result = await channel.invokeMethod('startMonitoring');
    print(result);
    return result;
  }

  static Future<String> get stopMonitoring async {
    final String result = await channel.invokeMethod('stopMonitoring');
    print(result);
    return result;
  }

  static Future<String> addRegion(String identifier, String uuid) async {
    final String result = await channel.invokeMethod(
        'addRegion', <String, dynamic>{'identifier': identifier, 'uuid': uuid});
    print(result);
    return result;
  }

  static Future<Void> runInBackground(bool runInBackground) async {
    return await channel.invokeMethod(
        'runInBackground', <String, dynamic>{'background': runInBackground});
  }

  static Future<String> addRegionForIOS(
      String uuid, int major, int minor, String name) async {
    final String result = await channel.invokeMethod(
        'addRegionForIOS', <String, dynamic>{
      'uuid': uuid,
      'major': major,
      'minor': minor,
      'name': name
    });
    print(result);
    return result;
  }

  static listenToBeacons(StreamController controller) async {
    event_channel.receiveBroadcastStream().listen((dynamic event) {
      print('Received: $event');
      controller.add(event);
    }, onError: (dynamic error) {
      print('Received error: ${error.message}');
    });
  }
}
