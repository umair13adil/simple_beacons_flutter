import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void callbackDispatcher() {
  const MethodChannel _backgroundChannel =
      MethodChannel('beacons_plugin_background');
  WidgetsFlutterBinding.ensureInitialized();

  _backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final List<dynamic> args = call.arguments;
    final Function callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));
    assert(callback != null);
    final List<String> triggeringGeofences = args[1].cast<String>();
    final List<double> locationList = <double>[];
    // 0.0 becomes 0 somewhere during the method call, resulting in wrong
    // runtime type (int instead of double). This is a simple way to get
    // around casting in another complicated manner.
    args[2]
        .forEach((dynamic e) => locationList.add(double.parse(e.toString())));

    callback(triggeringGeofences);
  });
  print("callbackDispatcher: _backgroundChannel.invokeMethod('initialized')");
  _backgroundChannel.invokeMethod('initialized');
}
