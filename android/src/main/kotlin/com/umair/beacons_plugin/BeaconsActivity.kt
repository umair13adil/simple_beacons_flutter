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

        attachMethodChannels()
    }

    private fun isPermissionGranted() {
        Log.i(TAG, "Location permissions are granted.")

        attachMethodChannels()

        BeaconsPlugin.sendBLEScannerReadyCallback()
    }

    private fun attachMethodChannels() {
        mFlutterEngine?.dartExecutor?.binaryMessenger?.let { messenger ->
            BeaconsPlugin.registerWith(messenger, this, this)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        if (!BeaconsPlugin.runInBackground)
            stopMonitoringBeacons()
    }


    override fun onPause() {
        super.onPause()

        //Start Background service to scan BLE devices
        BeaconsPlugin.startBackgroundService(this)
    }

    override fun onResume() {
        super.onResume()

        checkPermissions(this, ::isPermissionGranted)

        //Stop Background service, app is in foreground
        BeaconsPlugin.stopBackgroundService(this)
    }
}
