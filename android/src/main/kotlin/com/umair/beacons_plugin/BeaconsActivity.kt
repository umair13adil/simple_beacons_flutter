package com.umair.beacons_plugin

import android.util.Log
import io.flutter.embedding.engine.FlutterEngine


open class BeaconsActivity : BeaconScannerImplActivity() {

    private val TAG = "BeaconsActivity"

    companion object {

        @JvmStatic
        var mFlutterEngine: FlutterEngine? = null
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        mFlutterEngine = flutterEngine
    }

    private fun isPermissionGranted() {
        Log.i(TAG, "isPermissionGranted")
        
        mFlutterEngine?.dartExecutor?.binaryMessenger?.let { messenger ->
            BeaconsPlugin.registerWith(messenger, this, this)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        stopMonitoringBeacons()
    }


    override fun onPause() {
        super.onPause()

        BeaconsDiscoveryService.sServiceStarted.set(true)

        //Start Background service to scan BLE devices
        BeaconsPlugin.startBackgroundService(this)
    }

    override fun onResume() {
        super.onResume()

        checkPermissions(this, ::isPermissionGranted)

        BeaconsDiscoveryService.sServiceStarted.set(false)

        //Stop Background service, app is in foreground
        BeaconsPlugin.stopBackgroundService(this)
    }
}
