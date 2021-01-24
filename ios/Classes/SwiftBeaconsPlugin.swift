import Flutter
import UIKit
import CoreLocation

public class SwiftBeaconsPlugin: NSObject, FlutterPlugin {

    var eventSink: FlutterEventSink?
    let locationManager = CLLocationManager()
    var runInBackground = false

    var listOfRegions = [Item]()

    init(eventSink: FlutterEventSink?) {
        self.eventSink = eventSink
    }

    public static func register(with registrar: FlutterPluginRegistrar) {
        let fChannel = FlutterMethodChannel(name: "beacons_plugin", binaryMessenger: registrar.messenger())

        let eventChannel = FlutterEventChannel(name: "beacons_plugin_stream", binaryMessenger: registrar.messenger())

        let eventHandler = EventsStreamHandler(channel: fChannel, registrar: registrar)
        eventChannel.setStreamHandler(eventHandler)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {

        // flutter cmds dispatched on iOS device :
        if call.method == "addRegionForIOS" {
            guard let args = call.arguments else {
                return
            }
            if let myArgs = args as? [String: Any],
                let uuid = myArgs["uuid"] as? String,
                // let major = myArgs["major"] as? Int,
                // let minor = myArgs["minor"] as? Int,
                let name = myArgs["name"] as? String
            {
                // addRegion(uuid: uuid, major: major, minor: minor, name: name)
                addRegion(uuid: uuid, name: name)
                result("Region Added.")
            } else {
                result("iOS could not extract flutter arguments in method: (addRegion)")
            }
        } else if call.method == "addRegion" {
            guard let args = call.arguments else {
                return
            }
            if let myArgs = args as? [String: Any],
                let name = myArgs["identifier"] as? String,
                let uuid = myArgs["uuid"] as? String
            {
                // addRegion(uuid: uuid, major: major, minor: minor, name: name)
                addRegion(uuid: uuid, name: name)
                result("Region Added.")
            } else {
                result("iOS could not extract flutter arguments in method: (addRegion)")
            }
        } else if call.method == "clearRegions"{
            clearRegions()
            result("Regions cleared.")
        } else if call.method == "startMonitoring"{
            locationManager.delegate = self
            startScanning()
            result("Started scanning Beacons.")
        }else if call.method == "stopMonitoring"{
            stopScanning()
            result("Stopped scanning Beacons.")
        }else if call.method == "runInBackground"{
            runInBackground = true
            result("App will run in background? \(runInBackground)")
        }else {
            result("Flutter method not implemented on iOS")
        }
    }

    // func addRegion(uuid:String?, major:Int, minor:Int, name:String){
    func addRegion(uuid:String?, name:String){
        guard let uuid = UUID(uuidString: uuid ?? "") else { return; }
        // let major = major
        // let minor = minor
        let name = name

        // let newItem = Item(name: name, uuid: uuid, majorValue: major, minorValue: minor)
        let newItem = Item(name: name, uuid: uuid)
        listOfRegions.append(newItem)
    }

    func clearRegions() {
        listOfRegions = [Item]()
    }

    func startMonitoringItem(_ item: Item) {
        let beaconRegion = item.asBeaconRegion()
        locationManager.startMonitoring(for: beaconRegion)
        locationManager.startRangingBeacons(in: beaconRegion)
    }

    func stopMonitoringItem(_ item: Item) {
        let beaconRegion = item.asBeaconRegion()
        locationManager.stopMonitoring(for: beaconRegion)
        locationManager.stopRangingBeacons(in: beaconRegion)
    }

    func startScanning() {
        if (!listOfRegions.isEmpty) {
            for item in listOfRegions {
                startMonitoringItem(item)
            }
        }
    }

    func stopScanning() {
        if (!listOfRegions.isEmpty) {
            for item in listOfRegions {
                stopMonitoringItem(item)
            }
        }
    }
}

// MARK: CLLocationManagerDelegate
extension SwiftBeaconsPlugin: CLLocationManagerDelegate {

    public func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        guard region is CLBeaconRegion else { return }
    }

    public func locationManager(_ manager: CLLocationManager, monitoringDidFailFor region: CLRegion?, withError error: Error) {
        print("Failed monitoring region: \(error.localizedDescription)")
    }

    public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Location manager failed: \(error.localizedDescription)")
    }

    public func locationManager(_ manager: CLLocationManager, didRangeBeacons beacons: [CLBeacon], in region: CLBeaconRegion) {

        for beacon in beacons {
          for row in 0..<listOfRegions.count {
            if listOfRegions[row] == beacon {
                  listOfRegions[row].beacon = beacon
                  let data = "{\n" +
                  "  \"name\": \"\(listOfRegions[row].name)\",\n" +
                  "  \"uuid\": \"\(beacon.proximityUUID)\",\n" +
                  "  \"major\": \"\(beacon.major)\",\n" +
                  "  \"minor\": \"\(beacon.minor)\",\n" +
                  "  \"rssi\": \"\(beacon.rssi)\",\n" +
                  "  \"distance\": \"\(listOfRegions[row].locationString())\",\n" +
                  "  \"proximity\": \"\(listOfRegions[row].nameForProximity(beacon.proximity))\"\n" +
                  "}"

                  //Send data to flutter
                  eventSink?("\(data)")
            }
          }
        }
    }
}


class EventsStreamHandler: NSObject, FlutterStreamHandler {

    private var eventSink: FlutterEventSink?

    private var fChannel:FlutterMethodChannel
    private var fRegistrar: FlutterPluginRegistrar?

    init(channel:FlutterMethodChannel,registrar: FlutterPluginRegistrar) {
        self.fChannel = channel
        self.fRegistrar = registrar
    }

    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        eventSink = events
        let instance = SwiftBeaconsPlugin(eventSink: eventSink)
        fRegistrar?.addMethodCallDelegate(instance, channel: fChannel)
        return nil
    }

    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        return nil
    }
}
