package com.umair.beacons_plugin_example

import io.flutter.app.FlutterApplication
import org.altbeacon.beacon.powersave.BackgroundPowerSaver


class MainApplication : FlutterApplication() {
    private var backgroundPowerSaver: BackgroundPowerSaver? = null

    override fun onCreate() {
        super.onCreate()
        backgroundPowerSaver = BackgroundPowerSaver(this)
    }
}