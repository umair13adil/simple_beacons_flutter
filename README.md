# beacons_plugin

[![pub package](https://img.shields.io/pub/v/beacons_plugin)](https://pub.dev/packages/beacons_plugin)


This plugin is developed to scan nearby iBeacons on both Android iOS. This library makes it easier to scan & range nearby BLE beacons and read their proximity values.

## Android
For Android change min *SDK version*:

```groovy
defaultConfig {
  ...
  minSdkVersion 18
  ...
}
```

Change your Android Project's *MainActivity* class to following:

```kotlin
import com.umair.beacons_plugin.BeaconsActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity : BeaconsActivity(){

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        GeneratedPluginRegistrant.registerWith(flutterEngine)
    }
}
```

Add following dependency on *build.gradle* file:

```groovy
    implementation 'org.altbeacon:android-beacon-library:2.16.4'
```

Add following in *Android Manifest* file:

```xml
    
        <uses-permission android:name="android.permission.BLUETOOTH" />
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
        <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
        <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
        <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
        <uses-permission android:name="android.permission.WAKE_LOCK" />
    
        <uses-feature
            android:name="android.hardware.bluetooth_le"
            android:required="true" />
            
        <application
                android:icon="@mipmap/ic_launcher"
                android:label="beacons_plugin_example">
                <receiver
                    android:name="com.umair.beacons_plugin.RebootBroadcastReceiver"
                    android:enabled="true">
                    <intent-filter>
                        <action android:name="android.intent.action.BOOT_COMPLETED"></action>
                    </intent-filter>
                </receiver>
        
                <service
                    android:name="com.umair.beacons_plugin.BeaconsDiscoveryService"
                    android:exported="true" />
        
                <service
                    android:name="org.altbeacon.beacon.service.BeaconService"
                    android:enabled="true"
                    android:isolatedProcess="false"
                    android:label="beacon" />
                <service
                    android:name="org.altbeacon.beacon.BeaconIntentProcessor"
                    android:enabled="true" />
        </application>
```

*That's it for Android.*

## iOS

In your *AppDelegate.swift* file change it to like this:

```swift
    
    import UIKit
    import Flutter
    import CoreLocation
    
    @UIApplicationMain
    @objc class AppDelegate: FlutterAppDelegate {
    
        let locationManager = CLLocationManager()
    
        override func application(
            _ application: UIApplication,
            didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
        ) -> Bool {
    
            locationManager.requestAlwaysAuthorization()
            GeneratedPluginRegistrant.register(with: self)
    
            return super.application(application, didFinishLaunchingWithOptions: launchOptions)
        }
    }
```

In your *Info.plist* file add following lines:

```swift
    <dict>
    	<key>UIBackgroundModes</key>
        <string>location</string>
        <key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
    	<string>App needs location permissions to scan nearby beacons.</string>
    	<key>NSLocationWhenInUseUsageDescription</key>
    	<string>App needs location permissions to scan nearby beacons.</string>
    	<key>NSLocationAlwaysUsageDescription</key>
    	<string>App needs location permissions to scan nearby beacons.</string>
    </dict>
```


## Install
In your pubspec.yaml

```yaml
dependencies:
  beacons_plugin: [Latest_Version]
```

```dart
import 'dart:io' show Platform;
import 'package:flutter/services.dart';
import 'package:beacons_plugin/beacons_plugin.dart';
```

## Ranging Beacons

```dart
      static const MethodChannel methodChannel = const MethodChannel('beacons_plugin');
    
    static Future<String> addRegion(String identifier, String uuid) async {
        final String result = await methodChannel.invokeMethod(
            'addRegion', <String, dynamic>{'identifier': identifier, 'uuid': uuid});
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
      await BeaconsPlugin.addRegion(
               "Beacon1", "fda50693-a4e2-4fb1-afcf-c6eb07647825");
    } else if (Platform.isIOS) {
      await addRegionForIOS(
          "01022022-f88f-0000-00ae-9605fd9bb620", 1, 1, "BeaconName");
    }
    
    //Send 'true' to run in background
    static Future<String> runInBackground(bool runInBackground) async {
        final String result = await methodChannel.invokeMethod(
            'runInBackground', <String, dynamic>{'background': runInBackground});
        print(result);
        return result;
    }

    //Run once Scanner is setup & ready
    methodChannel.setMethodCallHandler((call) async {
      if (call.method == 'scannerReady') {
        await BeaconsPlugin.startMonitoring;
      }
    });
```

## Listen To Beacon Scan Results

```dart
    static const eventChannel = EventChannel('beacons_plugin_stream');
    
    static listenToBeacons() async {
        eventChannel.receiveBroadcastStream().listen((dynamic event) {
          print('Received: $event');
        }, onError: (dynamic error) {
          print('Received error: ${error.message}');
        });
    }
```

## Scan Results

| Data | Android | iOS |
| ------------- | ------------- | ------------- |
| name | Yes  |  Yes |
| uuid | Yes  |  Yes |
| major | Yes  |  Yes |
| minor | Yes  |  Yes |
| distance | Yes  |  Yes |
| proximity | Yes  |  Yes |
| rssi | Yes  |  Yes |
| macAddress | Yes  |  No |
| txPower | Yes  |  No |


## Native Libraries

* For iOS: [CoreLocation](https://developer.apple.com/documentation/corelocation/)
* For Android: [Android-Beacon-Library](https://github.com/AltBeacon/android-beacon-library) 

# Author

Flutter Beacons plugin is developed by Umair Adil. You can email me at <m.umair.adil@gmail.com> for any queries.