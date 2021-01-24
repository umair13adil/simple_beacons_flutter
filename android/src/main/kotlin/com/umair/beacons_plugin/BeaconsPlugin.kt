package com.umair.beacons_plugin

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.*
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
            BeaconPreferences.init(it)
            stopBackgroundService(it)
        }
    }

    companion object {
        private val TAG = "BeaconsPlugin"
        private var REQUEST_LOCATION_PERMISSIONS = 1890
        private var PERMISSION_REQUEST_BACKGROUND_LOCATION = 1891
        private var channel: MethodChannel? = null
        private var event_channel: EventChannel? = null
        private var currentActivity: Activity? = null
        private var beaconHelper: BeaconHelper? = null

        private var defaultPermissionDialogTitle = "This app needs background location access"
        private var defaultPermissionDialogMessage = "Please grant location access so this app can detect beacons in the background."

        @JvmStatic
        internal var messenger: BinaryMessenger? = null

        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            Timber.i("registerWith: registrar")
            BeaconPreferences.init(registrar.context())
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
            BeaconPreferences.init(context)
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
            BeaconPreferences.init(context)
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
                    call.method == "clearRegions" -> {
                        callBack?.clearRegions(call, result)
                    }
                    call.method == "runInBackground" -> {
                        call.argument<Boolean>("background")?.let {
                            runInBackground = it
                        }
                        result.success("App will run in background? $runInBackground")
                    }
                    call.method == "clearDisclosureDialogShowFlag" -> {
                        call.argument<Boolean>("clearFlag")?.let {
                            if (it) {
                                clearPermissionDialogShownFlag()
                                result.success("clearDisclosureDialogShowFlag: Flag cleared!")
                            } else {
                                setPermissionDialogShown()
                                result.success("clearDisclosureDialogShowFlag: Flag Set!")
                            }
                        }
                    }
                    call.method == "setDisclosureDialogMessage" -> {
                        call.argument<String>("title")?.let {
                            defaultPermissionDialogTitle = it
                        }
                        call.argument<String>("message")?.let {
                            defaultPermissionDialogMessage = it
                        }
                        result.success("Disclosure message Set: $defaultPermissionDialogMessage")
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
            if (areBackgroundScanPermissionsGranted()) {
                requestLocationPermissions()
            } else {
                requestBackgroundPermission()
            }
        }

        private fun requestLocationPermissions() {
            if (!arePermissionsGranted()) {
                Timber.i(String.format("requestLocationPermissions, %s", "Requesting location permissions.."))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        currentActivity?.let {
                            ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_LOCATION_PERMISSIONS)
                        }
                    } else {
                        currentActivity?.let {
                            ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSIONS)
                        }
                    }
                            ?: Timber.e(String.format("requestLocationPermissions, %s", "Unable to request location permissions."))
                } else {
                    doIfPermissionsGranted()
                }
            } else {
                doIfPermissionsGranted()
            }
        }

        @JvmStatic
        private fun requestBackgroundPermission() {
            if (!isPermissionDialogShown()) {
                currentActivity?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //if (it.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        Timber.i(String.format("requestBackgroundPermission, %s", "Requesting background location permissions.."))
                        val builder: AlertDialog.Builder = AlertDialog.Builder(it)
                        builder.setTitle(defaultPermissionDialogTitle)
                        builder.setMessage(defaultPermissionDialogMessage)
                        builder.setPositiveButton("Ok", null)
                        builder.setOnDismissListener {
                            requestLocationPermissions()
                        }
                        builder.show()
                        /*} else {
                        Timber.i(String.format("requestBackgroundPermission, %s", "Background location permissions are not granted!"))
                        val builder: AlertDialog.Builder = AlertDialog.Builder(it)
                        builder.setTitle("Functionality limited")
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                        builder.setPositiveButton("OK", null)
                        builder.setOnDismissListener {
                            currentActivity?.let {
                                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), PERMISSION_REQUEST_BACKGROUND_LOCATION)
                            }
                                    ?: Timber.e(String.format("requestBackgroundPermission, %s", "Unable to request background location permissions."))
                        }
                        builder.show()
                    }*/
                    }
                }
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
        private fun areBackgroundScanPermissionsGranted(): Boolean {
            currentActivity?.let {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                } else {
                    return true
                }
            }
            return true
        }

        @JvmStatic
        var runInBackground = false

        @JvmStatic
        var stopService = false

        interface PluginImpl {
            fun startScanning()
            fun stopMonitoringBeacons()
            fun addRegion(call: MethodCall, result: MethodChannel.Result)
            fun clearRegions(call: MethodCall, result: MethodChannel.Result)
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
        BeaconPreferences.init(currentActivity)
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
            setPermissionDialogShown()
            doIfPermissionsGranted()
            return true
        }
        if (requestCode == PERMISSION_REQUEST_BACKGROUND_LOCATION && grantResults?.isNotEmpty()!! && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setPermissionDialogShown()
            requestPermission()
            return true
        }
        return false
    }
}
