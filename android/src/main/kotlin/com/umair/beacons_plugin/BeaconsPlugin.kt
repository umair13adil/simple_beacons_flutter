package com.umair.beacons_plugin

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments

/** BeaconsPlugin */
class BeaconsPlugin : FlutterPlugin {

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

    }

    companion object {

        private lateinit var channel: MethodChannel
        private lateinit var event_channel: EventChannel
        private lateinit var mBackgroundChannel: MethodChannel
        private var sBackgroundFlutterView: FlutterNativeView? = null
        private var context: Context? = null

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

            mBackgroundChannel = MethodChannel(messenger,
                    "beacons_plugin_background")
            mBackgroundChannel.setMethodCallHandler { call, result ->
                when {
                    call.method == "initializeService" -> {
                        Log.i(TAG, "initializeService")
                        synchronized(BeaconsDiscoveryService.sServiceStarted) {
                            /*while (!queue.isEmpty()) {
                                mBackgroundChannel.invokeMethod("", queue.remove())
                            }*/
                            startBLEDiscoveryService()
                            BeaconsDiscoveryService.sServiceStarted.set(true)
                        }
                    }
                    call.method == "initialized" -> {
                        Log.i(TAG, "initialized")
                        synchronized(BeaconsDiscoveryService.sServiceStarted) {
                            /*while (!queue.isEmpty()) {
                                mBackgroundChannel.invokeMethod("", queue.remove())
                            }*/
                            BeaconsDiscoveryService.sServiceStarted.set(true)
                        }
                    }
                    call.method == "promoteToForeground" -> {
                        Log.i(TAG, "promoteToForeground")
                        //mContext.startForegroundService(Intent(mContext, IsolateHolderService::class.java))
                        try {
                            val serviceIntent = Intent(context, IsolateHolderService::class.java)
                            context?.startService(serviceIntent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, "promoteToForeground")
                        }
                    }
                    call.method == "demoteToBackground" -> {
                        Log.i(TAG, "demoteToBackground")
                        val intent = Intent(context, IsolateHolderService::class.java)
                        intent.setAction(IsolateHolderService.ACTION_SHUTDOWN)
                        //mContext.startForegroundService(intent)
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
        private fun startBLEDiscoveryService() {
            context?.let { context ->
                synchronized(BeaconsDiscoveryService.sServiceStarted) {
                    if (sBackgroundFlutterView == null) {
                        val callbackHandle = context.getSharedPreferences(
                                BeaconsPlugin.SHARED_PREFERENCES_KEY,
                                Context.MODE_PRIVATE)
                                .getLong(BeaconsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, 0)

                        val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
                        if (callbackInfo == null) {
                            Log.e(TAG, "Fatal: failed to find callback")
                            return
                        }
                        Log.i(TAG, "Starting BLEDiscoveryService...")
                        sBackgroundFlutterView = FlutterNativeView(context, true)

                        val args = FlutterRunArguments()
                        args.bundlePath = FlutterMain.findAppBundlePath()
                        args.entrypoint = callbackInfo.callbackName
                        args.libraryPath = callbackInfo.callbackLibraryPath

                        sBackgroundFlutterView!!.runFromBundle(args)
                        IsolateHolderService.setBackgroundFlutterView(sBackgroundFlutterView)
                    }
                }
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        mBackgroundChannel.setMethodCallHandler(null)
        event_channel.setStreamHandler(null)
        context = binding.applicationContext
        sBackgroundFlutterView = FlutterNativeView(binding.applicationContext, true)
    }
}
