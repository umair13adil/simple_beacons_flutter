package com.umair.beacons_plugin

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BeaconsPlugin */
class BeaconsPlugin : FlutterPlugin, MethodCallHandler {

    private val TAG = "BeaconsPlugin"
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "beacons_plugin")
        channel.setMethodCallHandler(this)
    }

    companion object {

        private var eventSink: EventChannel.EventSink? = null
        private var allPermissionsGranted = false

        fun setEventSink(sink: EventChannel.EventSink?) {
            this.eventSink = sink
        }

        fun cancelEvents() {
            this.eventSink = null
        }

        fun setPermissionsFlag(areGranted: Boolean) {
            this.allPermissionsGranted = areGranted
        }

        @JvmStatic
        fun registerWith(messenger: BinaryMessenger) {

            val channel = MethodChannel(messenger, "beacons_plugin")
            channel.setMethodCallHandler(BeaconsPlugin())
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when {
            call.method == "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            call.method == "startMonitoringItem" -> {
                if (allPermissionsGranted) {
                    result.success("Started scanning BLE devices.")
                } else {
                    result.success("Location permissions are not allowed.")
                }
            }
            call.method == "stopScan" -> {
                result.success("Stopped scanning BLE devices.")
            }
            call.method == "sendParams" -> {
                result.success("sendParams")
            }
            else -> result.notImplemented()
        }
    }

    /*private fun doOnScanned(scanResult: ScanResult) {
        scanResult.scanRecord.deviceName?.let {name->
            if (name.toLowerCase(Locale.US).contains("beacon")) {
                val distance = bleScannerHelper.calculateDistance(scanResult.scanRecord.txPowerLevel, scanResult.rssi.toDouble())
                val message = Beacon(
                        identifier = name,
                        uuid = scanResult.bleDevice.bluetoothDevice?.uuids?.first().toString(),
                        distance = distance,
                        time = System.currentTimeMillis()
                ).toString()
                scanResult.bleDevice?.bluetoothDevice?.uuids?.forEach {
                    Log.i(TAG, "UUID: ${it.toString()}")
                }
                scanResult.scanRecord.serviceUuids?.forEach {
                    it.uuid?.let {
                        if (it.toString() == "fda50693-a4e2-4fb1-afcf-c6eb07647825")
                            Log.i(TAG, "UUID: ${it.toString()}")
                    }
                }
                scanResult.scanRecord.deviceName?.let {
                    Log.i(TAG, "Name: ${it.toString()}")
                }
                eventSink?.success(message)
            }
        }
    }*/

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
