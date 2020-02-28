import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
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
  StreamController<String> beaconRegionController = new StreamController();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  @override
  void dispose() {
    beaconEventsController.close();
    beaconRegionController.close();
    super.dispose();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    BeaconsPlugin.listenToBeacons(beaconEventsController);
    BeaconsPlugin.listenToRegionEvents(beaconRegionController);

    await BeaconsPlugin.addRegion("Beacon1");
    await BeaconsPlugin.addRegion("Beacon2");

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

    beaconRegionController.stream.listen(
        (data) {
          if (data.isNotEmpty) {
            setState(() {
              _regionResult = data;
            });
            print("Regions Events: " + data);
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
