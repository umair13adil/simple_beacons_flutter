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
class BeaconsPlugin : FlutterPlugin , ActivityAware {

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.i(TAG, "onAttachedToEngine")
    }

    companion object {

        private lateinit var channel: MethodChannel
        private lateinit var event_channel: EventChannel
        lateinit var mBackgroundChannel: MethodChannel
        var sBackgroundFlutterView: FlutterNativeView? = null

        private val TAG = "BeaconsPlugin"

        @JvmStatic
        val CALLBACK_DISPATCHER_HANDLE_KEY = "callback_dispatch_handler"

        @JvmStatic
        val CALLBACK_HANDLE_KEY = "callback_handle"

        @JvmStatic
        val SHARED_PREFERENCES_KEY = "geofencing_plugin_cache"

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
            sBackgroundFlutterView = FlutterNativeView(context, true)

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

            mBackgroundChannel = MethodChannel(messenger,
                    "beacons_plugin_background")
            mBackgroundChannel.setMethodCallHandler { call, result ->
                val args = call.arguments<ArrayList<*>>()
                when {
                    call.method == "initializeService" -> {
                        context.let { context ->
                            Log.i(TAG, "initializeService")
                            initializeService(context, args)
                            //BeaconsDiscoveryService.enqueueWork(context, Intent())
                            val serviceIntent = Intent(context, BeaconsDiscoveryService::class.java)
                            context.startService(serviceIntent)
                            result.success(true)
                        }
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

        @JvmStatic
        private fun initializeService(context: Context, args: ArrayList<*>?) {
            val callbackHandle = args!![0] as Long
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(CALLBACK_DISPATCHER_HANDLE_KEY, callbackHandle)
                    .apply()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Log.i(TAG, "onDetachedFromEngine")
        channel.setMethodCallHandler(null)
        mBackgroundChannel.setMethodCallHandler(null)
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
    }
}
