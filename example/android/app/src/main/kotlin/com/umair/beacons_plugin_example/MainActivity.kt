package com.umair.beacons_plugin_example

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.os.RemoteException
import android.util.Log
import com.umair.beacons_plugin.BeaconsPlugin
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import org.altbeacon.beacon.*
import java.util.ArrayList

class MainActivity : FlutterActivity(), EventChannel.StreamHandler, BeaconConsumer {

    private val TAG = "MainActivity"

    private lateinit var event_channel: EventChannel
    private var eventSink: EventChannel.EventSink? = null
    private lateinit var beaconManager: BeaconManager

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        event_channel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, "beacons_plugin_stream")
        event_channel.setStreamHandler(this)

        GeneratedPluginRegistrant.registerWith(flutterEngine)
        BeaconsPlugin.registerWith(flutterEngine.dartExecutor.binaryMessenger)

        setUpBLE(this)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.eventSink = events
        BeaconsPlugin.setEventSink(events)
    }

    override fun onCancel(arguments: Any?) {
        this.eventSink = null
        BeaconsPlugin.cancelEvents()
    }

    override fun onBeaconServiceConnect() {

        val region = Region("beacons", null, null, null)

        beaconManager.addMonitorNotifier(object : MonitorNotifier {

            override fun didEnterRegion(region: Region) {
                Log.i(TAG, "Entered Region: $region")
                try {
                    beaconManager.startRangingBeaconsInRegion(region)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun didExitRegion(region: Region) {
                Log.i(TAG, "Exited Region: $region")
                try {
                    beaconManager.stopRangingBeaconsInRegion(region)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun didDetermineStateForRegion(state: Int, region: Region) {
                Log.i(TAG, "didDetermineStateForRegion: $state")
            }
        })

        beaconManager.addRangeNotifier { beacons, region1 ->
            Log.i(TAG, "addRangeNotifier: ${beacons.size}")

            if (beacons.isNotEmpty()) {

                val arrayList = ArrayList<ArrayList<String>>()

                for (b in beacons) {

                    //UUID
                    val uuid = b.id1.toString()

                    //Major
                    val major = b.id2.toString()

                    //Minor
                    val minor = b.id3.toString()

                    //Distance
                    val distance1 = b.distance
                    val distance = (Math.round(distance1 * 100.0) / 100.0).toString()

                    val arr = ArrayList<String>()
                    arr.add(uuid)
                    arr.add(major)
                    arr.add(minor)
                    arr.add("$distance meters")

                    val message = "UUID: $uuid, MAJOR: $major, MINOR: $minor, Distance: $distance"
                    Log.i(TAG, message)
                    eventSink?.success(message)
                    arrayList.add(arr)
                }
            }
        }

        startMonitoringBeacons(region)
    }

    private fun startMonitoringBeacons(region: Region) {
        try {
            beaconManager.startMonitoringBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }

    private fun setUpBLE(activity: Activity) {
        hasBLEFeature(activity)
        isBluetoothEnabled(activity)
        checkPermissions(activity, ::isPermissionGranted)
    }

    private fun isPermissionGranted(isGranted: Boolean) {
        setUpBeaconManager()
    }

    private fun setUpBeaconManager() {
        if (PermissionsHelper.isPermissionGranted(this, ACCESS_FINE_LOCATION)
                || PermissionsHelper.isPermissionGranted(this, ACCESS_COARSE_LOCATION)) {
            BeaconsPlugin.setPermissionsFlag(true)

            beaconManager = BeaconManager.getInstanceForApplication(this)
            beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
            beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

            try {
                //Updates an already running scan
                beaconManager.updateScanPeriods()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            beaconManager.bind(this)
        }
    }
}
