//
//  BeaconsPlugin.swift
//  Runner
//
//  Created by Umair Adil on 26/02/2020.
//

import Flutter

import UIKit

public class BeaconsPlugin: NSObject, FlutterPlugin {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "beacons_plugin", binaryMessenger: registrar.messenger())
        
        let instance = BeaconsPlugin()
        
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
        } else {
          result("Flutter method not implemented on iOS")
        }
    }
}
