package com.umair.beacons_plugin

import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener
import io.flutter.plugin.common.BinaryMessenger


open class BeaconsActivity : FlutterActivity() {

    private val TAG = "BeaconsActivity"

    private lateinit var beaconHelper: BeaconHelper

    companion object {

        @JvmStatic
        var mFlutterEngine: FlutterEngine? = null

        @JvmStatic
        var binaryMessenger: BinaryMessenger? = null
    }

    private fun isPermissionGranted() {
        Log.i(TAG, "Location permissions are granted.")

        attachMethodChannels()

        BeaconsPlugin.sendBLEScannerReadyCallback()
    }

    private fun attachMethodChannels() {
        Log.i(TAG, "attachMethodChannels")

        mFlutterEngine?.dartExecutor?.binaryMessenger?.let { messenger ->
            binaryMessenger = messenger
            BeaconsPlugin.registerWith(messenger, beaconHelper, this)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        if (!BeaconsPlugin.runInBackground)
            beaconHelper.stopMonitoringBeacons()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flutterEngine?.let {

            mFlutterEngine = flutterEngine

            beaconHelper = BeaconHelper(this)

            attachMethodChannels()
        }
    }
}
