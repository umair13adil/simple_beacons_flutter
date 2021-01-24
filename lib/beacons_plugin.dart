import 'dart:async';

import 'package:flutter/services.dart';

class BeaconsPlugin {
  static const MethodChannel channel = const MethodChannel('beacons_plugin');
  static const event_channel = EventChannel('beacons_plugin_stream');

  // 0 = no messages, 1 = only errors, 2 = all
  static int _debugLevel = 0;

  /// Set the message level value [value] for debugging purpose. 0 = no messages, 1 = errors, 2 = all
  static void setDebugLevel(int value) {
    _debugLevel = value;
  }

  // Send the message [msg] with the [msgDebugLevel] value. 1 = error, 2 = info
  static void printDebugMessage(String msg, int msgDebugLevel) {
    if (_debugLevel >= msgDebugLevel) {
      print('beacons_plugin: $msg');
    }
  }

  static Future<String> get startMonitoring async {
    final String result = await channel.invokeMethod('startMonitoring');
    printDebugMessage(result, 2);
    return result;
  }

  static Future<String> get stopMonitoring async {
    final String result = await channel.invokeMethod('stopMonitoring');
    printDebugMessage(result, 2);
    return result;
  }

  static Future<String> addRegion(String identifier, String uuid) async {
    final String result = await channel.invokeMethod(
        'addRegion', <String, dynamic>{'identifier': identifier, 'uuid': uuid});
    printDebugMessage(result, 2);
    return result;
  }

  static Future<String> clearRegions() async {
    final String result = await channel.invokeMethod('clearRegions');
    printDebugMessage(result, 2);
    return result;
  }

  static Future<String> runInBackground(bool runInBackground) async {
    final String result = await channel.invokeMethod(
      'runInBackground',
      <String, dynamic>{'background': runInBackground},
    );
    printDebugMessage(result, 2);
    return result;
  }

  static Future<String> clearDisclosureDialogShowFlag(bool clearFlag) async {
    final String result = await channel.invokeMethod(
      'clearDisclosureDialogShowFlag',
      <String, dynamic>{'clearFlag': clearFlag},
    );
    printDebugMessage(result, 2);
    return result;
  }

  static Future<String> setDisclosureDialogMessage(
      {String title, String message}) async {
    final String result = await channel.invokeMethod(
        'setDisclosureDialogMessage',
        <String, dynamic>{'title': title, 'message': message});
    printDebugMessage(result, 2);
    return result;
  }

  static Future<String> addRegionForIOS(
      String uuid, int major, int minor, String name) async {
    final String result = await channel.invokeMethod(
      'addRegionForIOS',
      <String, dynamic>{
        'uuid': uuid,
        'major': major,
        'minor': minor,
        'name': name
      },
    );
    printDebugMessage(result, 2);
    return result;
  }

  static listenToBeacons(StreamController controller) async {
    event_channel.receiveBroadcastStream().listen((dynamic event) {
      printDebugMessage('Received: $event', 2);
      controller.add(event);
    }, onError: (dynamic error) {
      printDebugMessage('Received error: ${error.message}', 1);
    });
  }
}
