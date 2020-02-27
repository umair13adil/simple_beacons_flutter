package com.cubivue.beacons_plugin

import android.Manifest
import android.app.Activity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy


object PermissionsHelper {

    fun requestStoragePermissions(activity: Activity): Observable<Boolean> {
        val rxPermissions = RxPermissions(activity)

        return Observable.create { emitter ->
            rxPermissions
                .request(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .compose(
                    rxPermissions.ensureEachCombined(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
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

    fun requestLocationPermissions(activity: Activity): Observable<Boolean> {
        val rxPermissions = RxPermissions(activity)

        return Observable.create { emitter ->
            rxPermissions
                .request(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .compose(
                    rxPermissions.ensureEachCombined(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
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
}