package com.umair.beacons_plugin

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.RemoteException
import android.util.Log
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.altbeacon.beacon.*
import java.util.*


open class BeaconHelper(var context: Context) : BeaconConsumer, BeaconsPlugin.Companion.PluginImpl {

    override fun getApplicationContext(): Context {
        return context.applicationContext
    }

    override fun unbindService(p0: ServiceConnection) {
        context.unbindService(p0)
    }

    override fun bindService(p0: Intent?, p1: ServiceConnection, p2: Int): Boolean {
        return context.bindService(p0, p1, p2)
    }

    private var eventSink: EventChannel.EventSink? = null

    companion object {

        @JvmStatic
        private var listOfRegions = arrayListOf<Region>()
    }

    private val TAG = "BeaconHelper"

    private var beaconManager: BeaconManager? = null

    private fun setUpBLE(context: Context) {
        hasBLEFeature(context)
        isBluetoothEnabled(context)
    }

    override fun stopMonitoringBeacons() {
        Log.i(TAG, "stopMonitoringBeacons")

        beaconManager?.unbind(this)
        eventSink = null
    }

    override fun setEventSink(events: EventChannel.EventSink?) {
        this.eventSink = events
    }


    override fun onBeaconServiceConnect() {
        Log.i(TAG, "onBeaconServiceConnect")

        beaconManager?.removeAllMonitorNotifiers()

        beaconManager?.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region) {
                try {
                    Log.d(TAG, "didEnterRegion")
                    beaconManager?.startRangingBeaconsInRegion(region)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }

            override fun didExitRegion(region: Region) {
                try {
                    Log.d(TAG, "didExitRegion")
                    beaconManager?.stopRangingBeaconsInRegion(region)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }

            override fun didDetermineStateForRegion(i: Int, region: Region) {

            }
        })


        beaconManager?.addRangeNotifier { beacons, region1 ->
            if (beacons.isNotEmpty()) {
                for (b in beacons) {
                    sendBeaconData(b)
                }
            }
        }

        startMonitoringBeacons(Region("myBeacon", null, null, null))
    }

    private fun sendBeaconData(b: org.altbeacon.beacon.Beacon) {

        //Identifier
        val identifier = listOfRegions.find {
            it.id1.toString() == b.id1.toString()
        }?.uniqueId

        //MacAddress
        val address = b.bluetoothAddress

        //UUID
        val uuid = b.id1.toString()

        //Major
        val major = b.id2.toString()

        //Minor
        val minor = b.id3.toString()

        //RSSI
        val rssi = b.rssi.toString()

        //TxPower
        val txPower = b.txPower.toString()

        //Distance
        val distance1 = b.distance
        val distance = (Math.round(distance1 * 100.0) / 100.0).toString()

        val message = Beacon(
                name = identifier,
                uuid = uuid,
                major = major,
                minor = minor,
                macAddress = address,
                distance = distance,
                rssi = rssi,
                txPower = txPower,
                proximity = Beacon.getProximityOfBeacon(b).value,
                scanTime = getReadableTime(System.currentTimeMillis())
        ).toString()

        //Log.i(TAG, "sendBeaconData: $message")

        eventSink?.success(message)
    }

    private fun startMonitoringBeacons(region: Region) {
        try {
            Log.i(TAG, "startMonitoringBeacons: ${region.uniqueId}")
            beaconManager?.startMonitoringBeaconsInRegion(region)
            beaconManager?.startRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
            Log.e(TAG, e.message.toString())
        }
    }

    private fun setUpBeaconManager(context: Context) {
        if (BeaconsPlugin.permissionsGranted(context)) {

            Log.i(TAG, "setUpBeaconManager")
            beaconManager = BeaconManager.getInstanceForApplication(context)
            beaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
            beaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
            beaconManager?.backgroundBetweenScanPeriod = 0
            beaconManager?.backgroundScanPeriod = 1100
            beaconManager?.bind(this)
        } else {
            Log.e(TAG, "Location permissions are needed.")
        }
    }

    override fun addRegion(call: MethodCall, result: MethodChannel.Result) {

        var identifier = ""
        var uuid = ""

        call.argument<String>("identifier")?.let {
            identifier = it
        }
        call.argument<String>("uuid")?.let {
            uuid = it
        }

        val region = Region(identifier, Identifier.fromUuid(UUID.fromString(uuid)), null, null)

        listOfRegions.add(region)

        result.success("Region Added: ${region.uniqueId}, UUID: ${region.id1}")
        Log.i(TAG, "Region Added: ${region.uniqueId}, UUID: ${region.id1}")
    }

    override fun clearRegions(call: MethodCall, result: MethodChannel.Result) {
        listOfRegions = arrayListOf<Region>()

        result.success("Regions Cleared")
        Log.i(TAG, "Regions Cleared")
    }

    override fun startScanning() {

        setUpBLE(context)
        setUpBeaconManager(context)

        if (listOfRegions.isNotEmpty()) {
            Log.i(TAG, "Started Monitoring ${listOfRegions.size} regions.")
            listOfRegions.forEach {
                startMonitoringBeacons(it)
            }
        } else {
            Log.i(TAG, "startScanning: No regions added..")
        }
    }
}
