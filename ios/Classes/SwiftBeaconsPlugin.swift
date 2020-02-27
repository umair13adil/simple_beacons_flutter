import Flutter
import UIKit
import CoreLocation

public class SwiftBeaconsPlugin: NSObject, FlutterPlugin,FlutterStreamHandler {
    
    private var eventSink: FlutterEventSink?
    let locationManager = CLLocationManager()
    var eventChannel:FlutterEventChannel? = nil
    
    init(eventChannel: FlutterEventChannel) {
      self.eventChannel = eventChannel
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "beacons_plugin", binaryMessenger: registrar.messenger())
        
        let eventChannel = FlutterEventChannel(name: "beacons_plugin_stream", binaryMessenger: registrar.messenger())
        
        let instance = SwiftBeaconsPlugin(eventChannel: eventChannel)
        eventChannel.setStreamHandler(instance)
        
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        // flutter cmds dispatched on iOS device :
        if call.method == "sendParams" {
            guard let args = call.arguments else {
                return
            }
            if let myArgs = args as? [String: Any],
                let someInfo1 = myArgs["someInfo1"] as? String,
                let someInfo2 = myArgs["someInfo2"] as? Double {
                result("Params received on iOS = \(someInfo1), \(someInfo2)")
            } else {
                result("iOS could not extract flutter arguments in method: (sendParams)")
            }
        } else if call.method == "getPlatformVersion" {
            result("Now Running on: iOS " + UIDevice.current.systemVersion)
        } else if call.method == "startMonitoringItem"{
            locationManager.delegate = self
            
            let uuid = UUID(uuidString: "fda50693-a4e2-4fb1-afcf-c6eb07647825")
            let major = 10035
            let minor = 56498
            let name = "WGX_iBeacon"
            
            let newItem = Item(name: name, uuid: uuid!, majorValue: major, minorValue: minor)
            startMonitoringItem(newItem)
            result("startMonitoringItem started.")
        }else {
            result("Flutter method not implemented on iOS")
        }
    }
    
    func startMonitoringItem(_ item: Item) {
        let beaconRegion = item.asBeaconRegion()
        locationManager.startMonitoring(for: beaconRegion)
        locationManager.startRangingBeacons(in: beaconRegion)
        print("startMonitoringItem")
    }
    
    func stopMonitoringItem(_ item: Item) {
        let beaconRegion = item.asBeaconRegion()
        locationManager.stopMonitoring(for: beaconRegion)
        locationManager.stopRangingBeacons(in: beaconRegion)
        print("stopMonitoringItem")
    }
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.eventSink = events
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        return nil
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
            eventSink!("Beacon: \(beacon)")
        }
    }
}
