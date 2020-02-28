package com.umair.beacons_plugin

import android.app.Activity
import android.util.Log
import androidx.annotation.NonNull
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/** BeaconsPlugin */
class BeaconsPlugin : FlutterPlugin, MethodCallHandler{

    private val TAG = "BeaconsPlugin"
    private lateinit var channel: MethodChannel
    private var connectivityDisposable: Disposable? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "beacons_plugin")
        channel.setMethodCallHandler(this)
    }

    companion object {

        private var eventSink: EventChannel.EventSink? = null
        private var allPermissionsGranted = false
        val bleScannerHelper = BLEScanHelper()

        fun setEventSink(sink: EventChannel.EventSink?) {
            this.eventSink = sink
        }

        fun cancelEvents() {
            this.eventSink = null
        }

        fun setPermissionsFlag(areGranted: Boolean) {
            this.allPermissionsGranted = areGranted
        }

        fun setRxBLEClient(rxBleClient: RxBleClient, activity: Activity) {
            bleScannerHelper.rxBleClient = rxBleClient

            checkPermissions(activity)
            hasBLEFeature(activity)
            isBluetoothEnabled(activity)
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
                    connectivityDisposable = bleScannerHelper.scanBleDevices(ScanSettings.SCAN_MODE_BALANCED)
                            ?.subscribeOn(Schedulers.io())
                            ?.observeOn(AndroidSchedulers.mainThread())
                            ?.subscribeBy(
                                    onNext = { scanResult ->
                                        doOnScanned(scanResult)
                                    },
                                    onError = {
                                        it.printStackTrace()
                                    }
                            )
                    result.success("Started scanning BLE devices.")
                } else {
                    result.success("Location permissions are not allowed.")
                }
            }
            call.method == "stopScan" -> {
                bleScannerHelper.unRegisterListeners(connectivityDisposable)
                result.success("Stopped scanning BLE devices.")
            }
            call.method == "sendParams" -> {
                result.success("sendParams")
            }
            else -> result.notImplemented()
        }
    }

    private fun doOnScanned(scanResult: ScanResult) {
        val distance = bleScannerHelper.calculateDistance(scanResult.scanRecord.txPowerLevel, scanResult.rssi.toDouble())
        val message = Beacon(
                identifier = scanResult.bleDevice.name,
                uuid = scanResult.bleDevice.bluetoothDevice?.uuids?.first().toString(),
                distance = distance,
                time = System.currentTimeMillis()
        ).toString()
        //Log.i(TAG, message)
        eventSink?.success(message)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Log.i(TAG, "onDetachedFromEngine")
        channel.setMethodCallHandler(null)
    }
}
