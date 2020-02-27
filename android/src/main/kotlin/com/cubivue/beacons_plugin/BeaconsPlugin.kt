package com.cubivue.beacons_plugin

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/** BeaconsPlugin */
class BeaconsPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {

    private val TAG = "BeaconsPlugin"
    private var eventSink: EventChannel.EventSink? = null
    private lateinit var channel: MethodChannel
    private lateinit var event_channel: EventChannel
    private var connectivityDisposable: Disposable? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "beacons_plugin")
        channel.setMethodCallHandler(this)


        event_channel = EventChannel(flutterPluginBinding.flutterEngine.dartExecutor, "beacons_plugin_stream")
        event_channel.setStreamHandler(this)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        this.eventSink = null
    }

    companion object {

        val bleScannerHelper = BLEScanHelper()

        fun setRxBLEClient(rxBleClient: RxBleClient, registrar: Registrar) {
            bleScannerHelper.rxBleClient = rxBleClient

            checkPermissions(registrar.activity())
            hasBLEFeature(registrar.activity())
            isBluetoothEnabled(registrar.activity())
        }

        @JvmStatic
        fun registerWith(registrar: Registrar) {

            val channel = MethodChannel(registrar.messenger(), "beacons_plugin")
            channel.setMethodCallHandler(BeaconsPlugin())

            RxBleClient.updateLogOptions(
                    LogOptions.Builder()
                            .setLogLevel(LogConstants.INFO)
                            .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                            .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                            .setShouldLogAttributeValues(true)
                            .build()
            )
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when {
            call.method == "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            call.method == "startMonitoringItem" -> {
                connectivityDisposable = bleScannerHelper.scanBleDevices(ScanSettings.SCAN_MODE_BALANCED)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(Schedulers.newThread())
                        ?.subscribeBy(
                                onNext = { scanResult ->
                                    doOnScanned(scanResult)
                                },
                                onError = {
                                    it.printStackTrace()
                                }
                        )
                result.success("Started scanning BLE devices.")
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
        val message = "RSSI: ${scanResult.rssi}, Power: ${scanResult.scanRecord.txPowerLevel}, Distance: $distance, Approx: ${bleScannerHelper.getDistance(distance)}"
        Log.i(TAG, "doOnScanned: $message")
        eventSink?.success("Beacon: $message)")
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
