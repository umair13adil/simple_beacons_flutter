package com.umair.beacons_plugin_example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy


object PermissionsHelper {

    private val TAG = "PermissionsHelper"

    fun requestLocationPermissions(activity: Activity): Observable<Boolean> {
        val rxPermissions = RxPermissions(activity)

        return Observable.create { emitter ->
            rxPermissions
                    .request(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                    .compose(
                            rxPermissions.ensureEachCombined(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                    )
                    .subscribeBy(
                            onNext = { permission ->
                                emitter.onNext(permission.granted)
                            },
                            onError = {
                                it.printStackTrace()
                            }
                    )
        }
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission granted: $permission")
            return true
        }
        Log.i(TAG, "Permission NOT granted: $permission")
        return false
    }
}