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
        if #available(iOS 10.0, *) {
          UNUserNotificationCenter.current().delegate = self as? UNUserNotificationCenterDelegate
        }
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
}
