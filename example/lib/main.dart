import 'dart:isolate';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:io' show Platform;
import 'package:beacons_plugin/callback_dispatcher.dart';
import 'package:beacons_plugin/beacons_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _beaconResult = 'Not Scanned Yet.';
  String _regionResult = 'No Results Available.';

  StreamController<String> beaconEventsController = new StreamController();
  ReceivePort port = ReceivePort();

  @override
  void initState() {
    super.initState();
    IsolateNameServer.registerPortWithName(
        port.sendPort, 'geofencing_send_port');
    port.listen((dynamic data) {
      print('IsolateNameServer Event: $data');
    });
    initPlatformState();
  }

  @override
  void dispose() {
    beaconEventsController.close();
    super.dispose();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {

    final CallbackHandle callback =
    PluginUtilities.getCallbackHandle(callbackDispatcher);
    await BeaconsPlugin.background.invokeMethod('initializeService',
        <dynamic>[callback.toRawHandle()]);

    BeaconsPlugin.listenToBeacons(beaconEventsController);

    if (Platform.isAndroid) {
      await BeaconsPlugin.addRegion("Beacon1");
      await BeaconsPlugin.addRegion("Beacon2");
    } else if (Platform.isIOS) {
      await BeaconsPlugin.addRegionForIOS(
          "fda50693-a4e2-4fb1-afcf-c6eb07647825", 10035, 56498, "WGX_iBeacon");
      await BeaconsPlugin.addRegionForIOS(
          "01022022-f88f-0000-00ae-9605fd9bb620", 1, 1, "BV5500Pro");
    }

    beaconEventsController.stream.listen(
        (data) {
          if (data.isNotEmpty) {
            setState(() {
              _beaconResult = data;
            });
            print("Beacons DataReceived: " + data);
          }
        },
        onDone: () {},
        onError: (error) {
          print("Error: $error");
        });

    await BeaconsPlugin.startMonitoring;

    if (!mounted) return;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Monitoring Beacons'),
        ),
        body: Center(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Text('$_beaconResult'),
              Padding(
                padding: EdgeInsets.all(10.0),
              ),
              Text('$_regionResult')
            ],
          ),
        ),
      ),
    );
  }
}
