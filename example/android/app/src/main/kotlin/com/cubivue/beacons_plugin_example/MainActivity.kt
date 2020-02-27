package com.cubivue.beacons_plugin_example

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.annotation.NonNull;
import com.cubivue.beacons_plugin.BeaconsPlugin
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity : FlutterActivity(), PluginRegistry.PluginRegistrantCallback {

    private val TAG = "MainActivity"

    override fun registerWith(registry: PluginRegistry?) {
        registry?.let {
            BeaconsPlugin.registerWith(registry.registrarFor("com.cubivue.beacons_plugin.BeaconsPlugin"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        Log.i(TAG,"Flutter Activity Started")
    }
}
