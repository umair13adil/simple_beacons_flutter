package com.umair.beacons_plugin_example

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.RemoteException
import android.util.Log
import com.umair.beacons_plugin.BeaconsPlugin
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import org.altbeacon.beacon.*
import java.util.*


class MainActivity : FlutterActivity(), MethodChannel.MethodCallHandler, BeaconConsumer {

    private val TAG = "MainActivity"

    private lateinit var channel: MethodChannel
    private lateinit var event_channel: EventChannel
    private lateinit var region_event_channel: EventChannel
    private var eventSink: EventChannel.EventSink? = null
    private var regionEventSink: EventChannel.EventSink? = null
    private lateinit var beaconManager: BeaconManager
    private val listOfRegions = arrayListOf<Region>()
    private var isBinded = false

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "beacons_plugin")
        channel.setMethodCallHandler(this)

        event_channel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, "beacons_plugin_stream")
        event_channel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                eventSink = events
            }

            override fun onCancel(arguments: Any?) {
                eventSink = null
            }
        })

        region_event_channel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, "beacons_region_stream")
        region_event_channel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                regionEventSink = events
            }

            override fun onCancel(arguments: Any?) {
                regionEventSink = null
            }
        })

        GeneratedPluginRegistrant.registerWith(flutterEngine)
        BeaconsPlugin.registerWith(flutterEngine.dartExecutor.binaryMessenger, this)

        setUpBLE(this)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.removeAllMonitorNotifiers()
        isBinded = true

        beaconManager.addMonitorNotifier(object : MonitorNotifier {

            override fun didEnterRegion(region: Region) {
                regionEventSink?.success("Entered Region: ${region.uniqueId}")
                startMonitoringBeacons(region)
            }

            override fun didExitRegion(region: Region) {
                regionEventSink?.success("Exited Region: ${region.uniqueId}")
                startMonitoringBeacons(region)
            }

            override fun didDetermineStateForRegion(state: Int, region: Region) {
                if (state == 1) {
                    regionEventSink?.success("Found ${region.uniqueId}")
                } else
                    regionEventSink?.success("No beacons found!")
            }
        })

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
                address = address,
                identifier = identifier,
                uuid = uuid,
                major = major,
                minor = minor,
                distance = distance,
                time = System.currentTimeMillis()
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

        val intent = Intent(this, MainActivity::class.java)
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

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when {
            call.method == "startMonitoring" -> {
                if (isBinded) {
                    startScanning()
                    result.success("Started scanning Beacons.")
                } else {
                    result.success("Beacon manager hasn't bind yet.")
                }
            }
            call.method == "stopMonitoring" -> {
                stopMonitoringBeacons()
                result.success("Stopped scanning Beacons.")
            }
            call.method == "addRegion" -> {
                addRegion(call, result)
            }
            else -> result.notImplemented()
        }
    }

    private fun addRegion(call: MethodCall, result: MethodChannel.Result) {
        var identifier = ""
        call.argument<String>("identifier")?.let {
            identifier = it
            listOfRegions.add(Region(it, null, null, null))
        }
        result.success("Region Added: $identifier")
    }

    private fun startScanning() {
        if (listOfRegions.isNotEmpty()) {
            Log.i(TAG, "Started Monitoring ${listOfRegions.size} regions.")
            listOfRegions.forEach {
                startMonitoringBeacons(it)
            }
        }
    }

    private fun stopMonitoringBeacons() {
        beaconManager.unbind(this)
        channel.setMethodCallHandler(null)
        event_channel.setStreamHandler(null)
        region_event_channel.setStreamHandler(null)
    }
}
