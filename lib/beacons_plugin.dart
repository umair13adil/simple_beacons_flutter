import 'dart:async';
import 'package:background_fetch/background_fetch.dart';
import 'package:flutter/services.dart';

class BeaconsPlugin {
  static const TAG = 'BeaconsPlugin';
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

  static Future<String> runInBackground(bool runInBackground) async {
    if (runInBackground) {
      BackgroundFetch.start().then((int status) {
        print('[$TAG] [BackgroundFetch] start success: $status');
      }).catchError((e) {
        print('[$TAG] [BackgroundFetch] start FAILURE: $e');
      });
    } else {
      BackgroundFetch.stop().then((int status) {
        print('[$TAG] [BackgroundFetch] stop success: $status');
      });
    }

    final String result = await channel.invokeMethod(
        'runInBackground', <String, dynamic>{'background': runInBackground});
    print(result);
    return result;
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
      print('[$TAG] Received: $event');
      controller.add(event);
    }, onError: (dynamic error) {
      print('[$TAG] Received error: ${error.message}');
    });
  }

  static Future<String> scanPeriodically({int delayInMilliseconds}) async {
    final String result = await channel.invokeMethod('scanPeriodically',
        <String, dynamic>{'delayInMilliseconds': delayInMilliseconds});
    print(result);
    return result;
  }

  static Future<int> setupBackgroundFetch(Function callback) async {
    return BackgroundFetch.configure(
        BackgroundFetchConfig(
            minimumFetchInterval: 15,
            stopOnTerminate: false,
            enableHeadless: true,
            requiresBatteryNotLow: false,
            requiresCharging: false,
            requiresStorageNotLow: false,
            requiresDeviceIdle: false,
            requiredNetworkType: NetworkType.NONE),
        callback);
  }

  /// This "Headless Task" is run when app is terminated.
  static void backgroundFetchHeadlessTask(String taskId) async {
    print('[$TAG] [BackgroundFetch] Headless event received.');

    //Start Monitoring
    await startMonitoring;

    BackgroundFetch.finish(taskId);
  }
}
