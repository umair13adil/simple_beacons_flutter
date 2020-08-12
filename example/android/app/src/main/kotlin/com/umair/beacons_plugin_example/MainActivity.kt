package com.umair.beacons_plugin_example

import com.umair.beacons_plugin.BeaconsPlugin
import io.flutter.embedding.android.FlutterActivity

class MainActivity : FlutterActivity(){

    override fun onPause() {
        super.onPause()

        //Start Background service to scan BLE devices
        BeaconsPlugin.startBackgroundService(this)
    }

    override fun onResume() {
        super.onResume()

        //Stop Background service, app is in foreground
        BeaconsPlugin.stopBackgroundService(this)
    }
}
