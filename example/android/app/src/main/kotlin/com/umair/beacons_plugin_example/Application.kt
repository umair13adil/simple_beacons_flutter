package com.umair.beacons_plugin_example

import android.util.Log
import com.polidea.rxandroidble2.RxBleClient
import io.flutter.app.FlutterApplication

class Application : FlutterApplication() {

    private val TAG = "Application"

    companion object {
        lateinit var rxBleClient: RxBleClient
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        rxBleClient = RxBleClient.create(this)
    }
}
