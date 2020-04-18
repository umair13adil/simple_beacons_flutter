package com.umair.beacons_plugin

import android.util.Log
import io.flutter.app.FlutterApplication
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap

open class BeaconApplication : FlutterApplication(), BootstrapNotifier {

    private val TAG = "BeaconApplication"
    private var regionBootstrap: RegionBootstrap? = null

    override fun onCreate() {
        super.onCreate()

        val beaconManager = BeaconManager.getInstanceForApplication(this)
        val region = Region("myBeacon", null, null, null)

        //TODO Add Regions Here
        regionBootstrap = RegionBootstrap(this, region)
    }

    override fun didDetermineStateForRegion(p0: Int, p1: Region?) {
        Log.i(TAG, "didDetermineStateForRegion: ${p0} ${p1.toString()}")
    }

    override fun didEnterRegion(p0: Region?) {
        Log.i(TAG, "didEnterRegion: ${p0.toString()}")
        regionBootstrap?.disable()
    }

    override fun didExitRegion(p0: Region?) {
        Log.i(TAG, "didExitRegion: ${p0.toString()}")
    }
}