package com.cubivue.beacons_plugin

import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.disposables.Disposable


class BLEScanHelper() {

    private val TAG = "BLEScanHelper"
    var rxBleClient: RxBleClient? = null

    fun scanBleDevices(scanMode: Int): Observable<ScanResult>? {
        val scanSettings = ScanSettings.Builder()
                .setScanMode(scanMode)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

        val scanFilter = ScanFilter.Builder()
                .build()

        return rxBleClient?.scanBleDevices(scanSettings, scanFilter)
    }

    fun unRegisterListeners(disposable: Disposable?) {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    fun getDistance(accuracy: Double): String {
        return if (accuracy.toDouble() == -1.0) {
            "Unknown"
        } else if (accuracy < 1) {
            "Immediate"
        } else if (accuracy < 3) {
            "Near"
        } else {
            "Far"
        }
    }

    fun calculateDistance(txPower: Int, rssi: Double): Double {
        if (rssi == 0.0) {
            return -1.0 // if we cannot determine accuracy, return -1.
        }
        val ratio = rssi * 1.0 / txPower
        return if (ratio < 1.0) {
            Math.pow(ratio, 10.0)
        } else {
            0.89976 * Math.pow(ratio, 7.7095) + 0.111
        }
    }
}