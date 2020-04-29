package com.umair.beacons_plugin

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterNativeView


/** BeaconsPlugin */
class BeaconsPlugin : FlutterPlugin, ActivityAware {

    private var context: Context? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.i(TAG, "onAttachedToEngine")
        context = flutterPluginBinding.applicationContext

        context?.let {
            stopBackgroundService(it)
        }
    }

    companion object {

        private lateinit var channel: MethodChannel
        private lateinit var event_channel: EventChannel

        private val TAG = "BeaconsPlugin"

        @JvmStatic
        var runInBackground = false

        @JvmStatic
        var stopService = false

        interface PluginImpl {
            fun startScanning()
            fun stopMonitoringBeacons()
            fun addRegion(call: MethodCall, result: MethodChannel.Result)
            fun setEventSink(events: EventChannel.EventSink?)
        }

        private var callBack: PluginImpl? = null

        @JvmStatic
        fun registerWith(messenger: BinaryMessenger, callBack: PluginImpl?, context: Context) {
            this.callBack = callBack

            channel = MethodChannel(messenger, "beacons_plugin")
            channel.setMethodCallHandler { call, result ->
                when {
                    call.method == "startMonitoring" -> {
                        stopService = false
                        callBack?.startScanning()
                        result.success("Started scanning Beacons.")
                    }
                    call.method == "stopMonitoring" -> {

                        if (runInBackground) {
                            stopService = true
                            context.let {
                                stopBackgroundService(it)
                            }
                        }

                        callBack?.stopMonitoringBeacons()
                        result.success("Stopped scanning Beacons.")
                    }
                    call.method == "addRegion" -> {
                        callBack?.addRegion(call, result)
                    }
                    call.method == "runInBackground" -> {
                        call.argument<Boolean>("background")?.let {
                            runInBackground = it
                        }
                        result.success("App will run in background? $runInBackground")
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

        fun sendBLEScannerReadyCallback() {
            channel.invokeMethod("scannerReady", "")
        }

        fun startBackgroundService(context: Context) {
            if (runInBackground && !stopService) {
                val serviceIntent1 = Intent(context, BeaconsDiscoveryService::class.java)
                context.startService(serviceIntent1)
            }
        }

        fun stopBackgroundService(context: Context) {
            if (runInBackground && !stopService) {
                val serviceIntent = Intent(context, BeaconsDiscoveryService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Log.i(TAG, "onDetachedFromEngine")
        channel.setMethodCallHandler(null)
        event_channel.setStreamHandler(null)
    }

    override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        Log.i(TAG, "onDetachedFromEngine")
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.i(TAG, "onDetachedFromActivityForConfigChanges")
    }

    override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
        Log.i(TAG, "onReattachedToActivityForConfigChanges")
    }

    override fun onDetachedFromActivity() {
        Log.i(TAG, "onDetachedFromActivity")

        context?.let {
            startBackgroundService(it)
        }
    }
}
