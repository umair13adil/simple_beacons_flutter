import Flutter
import UIKit
import CoreLocation

public class SwiftBeaconsPlugin: NSObject, FlutterPlugin {
    
    var eventSink: FlutterEventSink?
    var regionEventSink: FlutterEventSink?
    let locationManager = CLLocationManager()
    
    var listOfRegions = [Item]()
    
    init(eventSink: FlutterEventSink?,regionEventSink: FlutterEventSink?) {
        self.eventSink = eventSink
        self.regionEventSink = regionEventSink
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "beacons_plugin", binaryMessenger: registrar.messenger())
        
        let eventChannel = FlutterEventChannel(name: "beacons_plugin_stream", binaryMessenger: registrar.messenger())
        
        let regionEventChannel = FlutterEventChannel(name: "beacons_region_stream", binaryMessenger: registrar.messenger())
        
        let eventHandler = EventsStreamHandler()
        eventChannel.setStreamHandler(eventHandler)
        
        let regionHandler = RegionStreamHandler()
        regionEventChannel.setStreamHandler(regionHandler)
        
        let instance = SwiftBeaconsPlugin(eventSink: eventHandler.eventSink,regionEventSink: regionHandler.regionEventSink)
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        // flutter cmds dispatched on iOS device :
        if call.method == "addRegionForIOS" {
            guard let args = call.arguments else {
                return
            }
            if let myArgs = args as? [String: Any],
                let uuid = myArgs["uuid"] as? String,
                let major = myArgs["major"] as? Int,
                let minor = myArgs["minor"] as? Int,
                let name = myArgs["name"] as? String
            {
                addRegion(uuid: uuid, major: major, minor: minor, name: name)
                result("Region Added.")
            } else {
                result("iOS could not extract flutter arguments in method: (addRegion)")
            }
        } else if call.method == "startMonitoring"{
            locationManager.delegate = self
            startScanning()
            result("Started scanning Beacons.")
        }else if call.method == "stopMonitoring"{
            startScanning()
            result("Stopped scanning Beacons.")
        }else {
            result("Flutter method not implemented on iOS")
        }
    }
    
    func addRegion(uuid:String, major:Int, minor:Int, name:String){
        let uuid = UUID(uuidString: uuid)
        let major = major
        let minor = minor
        let name = name
        
        let newItem = Item(name: name, uuid: uuid!, majorValue: major, minorValue: minor)
        listOfRegions.append(newItem)
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
            print("Beacon: \(beacon)")
            eventSink?("Beacon: \(beacon)")
        }
    }
}


class EventsStreamHandler: NSObject, FlutterStreamHandler {
    
    var eventSink: FlutterEventSink?
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        eventSink = events
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        return nil
    }
}

class RegionStreamHandler: NSObject, FlutterStreamHandler {
    
    var regionEventSink: FlutterEventSink?
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        regionEventSink = events
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        regionEventSink = nil
        return nil
    }
}
