package com.umair.beacons_plugin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.*
import io.flutter.plugin.platform.PlatformViewsController
import io.flutter.view.FlutterNativeView
import timber.log.Timber


/** BeaconsPlugin */
class BeaconsPlugin : FlutterPlugin, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

    private var context: Context? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Timber.i("onAttachedToEngine")
        messenger = flutterPluginBinding.binaryMessenger
        setUpPluginMethods(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)
        context = flutterPluginBinding.applicationContext
        beaconHelper = BeaconHelper(flutterPluginBinding.applicationContext)
        context?.let {
            stopBackgroundService(it)
        }
    }

    companion object {
        private val TAG = "BeaconsPlugin"
        private var REQUEST_LOCATION_PERMISSIONS = 1890
        private var channel: MethodChannel? = null
        private var event_channel: EventChannel? = null
        private var currentActivity: Activity? = null
        private var beaconHelper: BeaconHelper? = null

        @JvmStatic
        internal var messenger: BinaryMessenger? = null

        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            Timber.i("registerWith: registrar")
            if (beaconHelper == null) {
                this.beaconHelper = BeaconHelper(registrar.context())
            }
            val instance = BeaconsPlugin()
            registrar.addRequestPermissionsResultListener(instance)
            requestPermission()
            setUpPluginMethods(registrar.activity(), registrar.messenger())
        }

        @JvmStatic
        fun registerWith(messenger: BinaryMessenger, context: Context) {
            Timber.i("registerWith: messenger")
            if (beaconHelper == null) {
                this.beaconHelper = BeaconHelper(context)
            }
            val instance = BeaconsPlugin()
            requestPermission()
            setUpPluginMethods(context, messenger)
        }

        @JvmStatic
        fun registerWith(messenger: BinaryMessenger, beaconHelper: BeaconHelper, context: Context) {
            Timber.i("registerWith: messenger background")
            this.beaconHelper = beaconHelper
            val instance = BeaconsPlugin()
            requestPermission()
            setUpPluginMethods(context, messenger)
        }

        @JvmStatic
        private fun setUpPluginMethods(context: Context, messenger: BinaryMessenger) {
            Timber.i("setUpPluginMethods")
            Timber.plant(Timber.DebugTree())

            channel = MethodChannel(messenger, "beacons_plugin")
            notifyIfPermissionsGranted(context)
            channel?.setMethodCallHandler { call, result ->
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
            event_channel?.setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    callBack?.setEventSink(events)
                }

                override fun onCancel(arguments: Any?) {

                }
            })
        }

        @JvmStatic
        private fun notifyIfPermissionsGranted(context: Context) {
            Timber.i("notifyIfPermissionsGranted")
            if (permissionsGranted(context)) {
                doIfPermissionsGranted()
            }
        }

        @JvmStatic
        fun permissionsGranted(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        @JvmStatic
        private fun doIfPermissionsGranted() {
            Timber.i("doIfPermissionsGranted")

            if (beaconHelper == null) {
                Timber.i(String.format("doIfPermissionsGranted, %s", "Unable to start beacons plugin service."))
                return
            }

            this.callBack = beaconHelper
        }

        @JvmStatic
        private fun requestPermission() {
            if (!arePermissionsGranted()) {
                Timber.i(String.format("requestPermission, %s", "Requesting location permissions.."))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    currentActivity?.let {
                        ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSIONS)
                    }
                            ?: Timber.e(String.format("requestPermission, %s", "Unable to request location permissions."))
                } else {
                    doIfPermissionsGranted()
                }
            } else {
                doIfPermissionsGranted()
            }
        }

        @JvmStatic
        private fun arePermissionsGranted(): Boolean {
            currentActivity?.let {
                return ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
            return false
        }

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

        fun sendBLEScannerReadyCallback() {
            Timber.i("sendBLEScannerReadyCallback")
            channel?.invokeMethod("scannerReady", "")
        }

        fun startBackgroundService(context: Context) {
            Timber.i("startBackgroundService")
            if (runInBackground && !stopService) {
                val serviceIntent1 = Intent(context, BeaconsDiscoveryService::class.java)
                context.startService(serviceIntent1)
            }
        }

        fun stopBackgroundService(context: Context) {
            Timber.i("stopBackgroundService")
            if (runInBackground && !stopService) {
                val serviceIntent = Intent(context, BeaconsDiscoveryService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Timber.i("onDetachedFromEngine")
        currentActivity = null
        channel?.setMethodCallHandler(null)
        event_channel?.setStreamHandler(null)

        if (!BeaconsPlugin.runInBackground)
            beaconHelper?.stopMonitoringBeacons()

        context?.let {
            stopBackgroundService(it)
        }

        context = null
    }

    override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        Timber.i("onAttachedToActivity")
        currentActivity = activityPluginBinding.activity
        activityPluginBinding.addRequestPermissionsResultListener(this)
        requestPermission()

        if (arePermissionsGranted()) {
            sendBLEScannerReadyCallback()
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Timber.i("onDetachedFromActivityForConfigChanges")
    }

    override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
        Timber.i("onReattachedToActivityForConfigChanges")
        currentActivity = activityPluginBinding.activity
        activityPluginBinding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {
        Timber.i("onDetachedFromActivity")
        currentActivity = null

        context?.let {
            startBackgroundService(it)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?): Boolean {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS && grantResults?.isNotEmpty()!! && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doIfPermissionsGranted()
            return true
        }
        return false
    }
}
