package com.umair.beacons_plugin

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val TAG = "AppUtils"
private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
internal val REQUEST_ENABLE_BT = 10323

fun hasBLEFeature(activity: Activity): Boolean {
    activity.packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }
            ?.also {
                return false
            }
    return true
}

fun setUpBlueToothAdapter(activity: Activity): BluetoothAdapter? {
    val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
                activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    return bluetoothAdapter
}

private val BluetoothAdapter.isDisabled: Boolean
    get() = !isEnabled

fun isBluetoothEnabled(activity: Activity) {

    setUpBlueToothAdapter(activity)?.takeIf { it.isDisabled }?.apply {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }
}

fun checkPermissions(activity: Activity, isGranted: (Boolean) -> Unit) {

    try {
        //Request for storage permissions
        PermissionsHelper.requestLocationPermissions(activity)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .debounce(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { callback ->
                            isGranted.invoke(callback)
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun getReadableTime(timestamp: Long): String {
    val TIME_FORMAT_READABLE = "dd MMMM yyyy hh:mm:ss a"
    if (timestamp != 0L) {
        val date1 = Date(timestamp)
        val outFormat = SimpleDateFormat(TIME_FORMAT_READABLE, Locale.ENGLISH)
        return outFormat.format(date1)
    } else
        return "No Time Provided!"
}