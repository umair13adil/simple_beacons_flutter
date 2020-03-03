package com.umair.beacons_plugin

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/** BeaconsPlugin */
class BeaconsPlugin : FlutterPlugin {

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

    }

    companion object {

        private lateinit var channel: MethodChannel
        private lateinit var event_channel: EventChannel

        private val TAG = "BeaconsPlugin"

        interface PluginImpl {
            fun startScanning()
            fun stopMonitoringBeacons()
            fun addRegion(call: MethodCall, result: MethodChannel.Result)
            fun setEventSink(events: EventChannel.EventSink?)
        }

        private var callBack: PluginImpl? = null

        @JvmStatic
        fun registerWith(messenger: BinaryMessenger, callBack: PluginImpl?) {
            this.callBack = callBack

            channel = MethodChannel(messenger, "beacons_plugin")
            channel.setMethodCallHandler { call, result ->
                when {
                    call.method == "startMonitoring" -> {
                        callBack?.startScanning()
                        result.success("Started scanning Beacons.")
                    }
                    call.method == "stopMonitoring" -> {
                        callBack?.stopMonitoringBeacons()
                        result.success("Stopped scanning Beacons.")
                    }
                    call.method == "addRegion" -> {
                        callBack?.addRegion(call, result)
                    }
                    else -> result.notImplemented()
                }
            }

            event_channel = EventChannel(messenger, "beacons_plugin_stream")
            event_channel.setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    callBack?.setEventSink(events)
                }

                override fun onCancel(arguments: Any?) {

                }
            })

        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        event_channel.setStreamHandler(null)
    }
}
