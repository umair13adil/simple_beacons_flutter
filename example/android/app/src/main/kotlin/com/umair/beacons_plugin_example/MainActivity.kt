package com.umair.beacons_plugin_example

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.umair.beacons_plugin.BeaconsPlugin
import com.umair.beacons_plugin.PermissionsHelper
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity : FlutterActivity() , EventChannel.StreamHandler{

    private val TAG = "MainActivity"
    private lateinit var event_channel: EventChannel

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        Log.i(TAG, "Flutter Activity Started")
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        Log.i(TAG, "configureFlutterEngine")
        super.configureFlutterEngine(flutterEngine)

        event_channel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, "beacons_plugin_stream")
        event_channel.setStreamHandler(this)

        GeneratedPluginRegistrant.registerWith(flutterEngine)
        BeaconsPlugin.setRxBLEClient(Application.rxBleClient, this)
        BeaconsPlugin.registerWith(flutterEngine.dartExecutor.binaryMessenger)

        if (PermissionsHelper.isPermissionGranted(this, ACCESS_FINE_LOCATION)
                || PermissionsHelper.isPermissionGranted(this, ACCESS_COARSE_LOCATION)) {
            BeaconsPlugin.setPermissionsFlag(true)
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Log.i(TAG, "onListen: EventChannel.EventSink set")
        BeaconsPlugin.setEventSink(events)
    }

    override fun onCancel(arguments: Any?) {
        Log.i(TAG, "onCancel: EventChannel.EventSink cancelled")
        BeaconsPlugin.cancelEvents()
    }
}
