package com.cubivue.beacons_plugin_example

import android.util.Log
import com.cubivue.beacons_plugin.BeaconsPlugin
import com.polidea.rxandroidble2.RxBleClient

import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback
import io.flutter.plugins.GeneratedPluginRegistrant
import io.flutter.view.FlutterMain

class Application : FlutterApplication(), PluginRegistrantCallback {

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

    override fun registerWith(registry: PluginRegistry) {
        Log.i(TAG, "registerWith")
        val registrar = registry.registrarFor("com.cubivue.beacons_plugin.BeaconsPlugin")
        BeaconsPlugin.setRxBLEClient(rxBleClient, registrar)
        BeaconsPlugin.registerWith(registrar)
    }
}
