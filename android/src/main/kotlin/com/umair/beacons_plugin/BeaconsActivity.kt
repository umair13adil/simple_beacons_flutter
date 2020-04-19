package com.umair.beacons_plugin

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.RemoteException
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import java.util.*


open class BeaconsActivity : FlutterActivity(), BeaconConsumer, BeaconsPlugin.Companion.PluginImpl {

    private val TAG = "MainActivity"

    private lateinit var beaconManager: BeaconManager
    private val listOfRegions = arrayListOf<Region>()
    private var eventSink: EventChannel.EventSink? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val messenger = flutterEngine.dartExecutor.binaryMessenger
        BeaconsPlugin.registerWith(messenger, this, this)

        setUpBLE(this)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.removeAllMonitorNotifiers()

        beaconManager.addRangeNotifier { beacons, region1 ->
            if (beacons.isNotEmpty()) {
                for (b in beacons) {
                    sendBeaconData(b)
                }
            }
        }

        startMonitoringBeacons(Region("myBeacon", null, null, null))
    }

    private fun sendBeaconData(b: org.altbeacon.beacon.Beacon) {
        //MacAddress
        val name = b.bluetoothName

        //MacAddress
        val address = b.bluetoothAddress

        //Identifier
        val identifier = b.parserIdentifier

        //UUID
        val uuid = b.id1.toString()

        //Major
        val major = b.id2.toString()

        //Minor
        val minor = b.id3.toString()

        //Distance
        val distance1 = b.distance
        val distance = (Math.round(distance1 * 100.0) / 100.0).toString()

        val message = Beacon(
                name = name,
                uuid = uuid,
                major = major,
                minor = minor,
                distance = distance,
                proximity = Beacon.getProximityOfBeacon(b).value
        ).toString()

        eventSink?.success(message)
    }

    private fun startMonitoringBeacons(region: Region) {
        try {
            beaconManager.startMonitoringBeaconsInRegion(region)
            beaconManager.startRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        stopMonitoringBeacons()
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

            beaconManager = BeaconManager.getInstanceForApplication(this)
            beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
            beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
            beaconManager.backgroundBetweenScanPeriod = 0
            beaconManager.backgroundScanPeriod = 1100
            beaconManager.bind(this)
        } else {
            Log.e(TAG, "Location permissions are needed.")
        }
    }

    private fun startForeGroundScanning() {
        val builder = Notification.Builder(this)
        builder.setSmallIcon(R.drawable.launch_background)
        builder.setContentTitle("Content Title")
        builder.setContentText("Content Text")

        val intent = Intent(this, BeaconsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ChannelId", "ChannelName", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel)
            builder.setChannelId(channel.id)
        }

        beaconManager.enableForegroundServiceScanning(builder.build(), 12345)
        beaconManager.setEnableScheduledScanJobs(false)
        beaconManager.backgroundBetweenScanPeriod = 0
        beaconManager.backgroundScanPeriod = 1100
    }

    override fun addRegion(call: MethodCall, result: MethodChannel.Result) {
        var identifier = ""
        call.argument<String>("identifier")?.let {
            identifier = it
            listOfRegions.add(Region(it, null, null, null))
        }
        result.success("Region Added: $identifier")
    }

    override fun startScanning() {
        if (listOfRegions.isNotEmpty()) {
            Log.i(TAG, "Started Monitoring ${listOfRegions.size} regions.")
            listOfRegions.forEach {
                startMonitoringBeacons(it)
            }
        }
    }

    override fun stopMonitoringBeacons() {
        beaconManager.unbind(this)
        eventSink = null
    }

    override fun setEventSink(events: EventChannel.EventSink?) {
        this.eventSink = events
    }
}
