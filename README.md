# beacons_plugin

[![pub package](https://img.shields.io/pub/v/beacons_plugin)](https://pub.dev/packages/beacons_plugin)


This plugin is developed to scan nearby iBeacons on both Android iOS. This library makes it easier to scan & range nearby BLE beacons and read their proximity values.

## Android
For Android change min SDK version:

```groovy
defaultConfig {
  ...
  minSdkVersion 18
  ...
}
```

## Install
In your pubspec.yaml

```yaml
dependencies:
  beacons_plugin: ^1.0.4
```

```dart
import 'dart:io' show Platform;
import 'package:flutter/services.dart';
import 'package:beacons_plugin/beacons_plugin.dart';
```

## Ranging Beacons

```dart
    static const MethodChannel methodChannel = const MethodChannel('com.umair.beacons_plugin_example/beacons_plugin');
    
    static Future<String> addRegion(String identifier) async {
        final String result = await methodChannel.invokeMethod('addRegion', <String, dynamic>{'identifier': identifier});
        print(result);
        return result;
    }
    
    static Future<String> addRegionForIOS(String uuid, int major, int minor, String name) async {
        final String result = await methodChannel.invokeMethod('addRegionForIOS', <String, dynamic>{
          'uuid': uuid,
          'major': major,
          'minor': minor,
          'name': name
        });
        print(result);
        return result;
    }

    if (Platform.isAndroid) {
      await addRegion("Beacon1");
    } else if (Platform.isIOS) {
      await addRegionForIOS(
          "01022022-f88f-0000-00ae-9605fd9bb620", 1, 1, "BeaconName");
    }
```

## Listen To Beacon Scan Results

```dart
    static const eventChannel = EventChannel('com.umair.beacons_plugin_example/beacons_plugin_stream');
    
    static listenToBeacons() async {
        eventChannel.receiveBroadcastStream().listen((dynamic event) {
          print('Received: $event');
        }, onError: (dynamic error) {
          print('Received error: ${error.message}');
        });
    }
```

## Native Libraries

* For iOS: [CoreLocation](https://developer.apple.com/documentation/corelocation/)
* For Android: [Android-Beacon-Library](https://github.com/AltBeacon/android-beacon-library) 

# Author

Flutter Beacons plugin is developed by Umair Adil. You can email me at <m.umair.adil@gmail.com> for any queries.