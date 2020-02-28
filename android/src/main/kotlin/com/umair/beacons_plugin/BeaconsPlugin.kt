package com.umair.beacons_plugin

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

/** BeaconsPlugin */
class BeaconsPlugin : FlutterPlugin{

    private val TAG = "BeaconsPlugin"

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

    }

    companion object {

        @JvmStatic
        fun registerWith(messenger: BinaryMessenger, handler:MethodChannel.MethodCallHandler) {

            val channel = MethodChannel(messenger, "beacons_plugin")
            channel.setMethodCallHandler(handler)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {

    }
}
