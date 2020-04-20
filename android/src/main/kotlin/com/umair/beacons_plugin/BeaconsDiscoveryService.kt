package com.umair.beacons_plugin

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments
import java.util.concurrent.atomic.AtomicBoolean

class BeaconsDiscoveryService : Service() {

    private lateinit var beaconScannerImplActivity: BeaconScannerImplActivity

    companion object {
        @JvmStatic
        private val TAG = "BeaconsDiscoveryService"

        @JvmStatic
        val sServiceStarted = AtomicBoolean(false)
    }

    override fun onCreate() {
        super.onCreate()
        //beaconScannerImplActivity = BeaconScannerImplActivity(this as Activity)
        Log.i(TAG, "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startBLEDiscoveryService(mContext: Context) {
        try {
            synchronized(sServiceStarted) {
                Log.i(TAG, "startBLEDiscoveryService")
                val callbackHandle = mContext.getSharedPreferences(
                        BeaconsPlugin.SHARED_PREFERENCES_KEY,
                        Context.MODE_PRIVATE)
                        .getLong(BeaconsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, 0)

                val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

                Log.i(TAG, "Starting BLEDiscoveryService...")
                BeaconsPlugin.sBackgroundFlutterView = FlutterNativeView(mContext, true)

                val args = FlutterRunArguments()
                args.bundlePath = FlutterMain.findAppBundlePath()
                args.entrypoint = callbackInfo.callbackName
                args.libraryPath = callbackInfo.callbackLibraryPath

                BeaconsPlugin.sBackgroundFlutterView!!.runFromBundle(args)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "$TAG: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification("${TAG}_ID", TAG, "${TAG}::WAKE_LOCK", "Beacons Service", "Looking for nearby beacons")
        aquireWakeLock(intent, "${TAG}::WAKE_LOCK")

        //startBLEDiscoveryService(this)

        val callbackHandle = intent?.getLongExtra(BeaconsPlugin.CALLBACK_HANDLE_KEY, 0)

        //TODO: Send Callback Handle with Result
        synchronized(sServiceStarted) {
            if (!sServiceStarted.get()) {
                // Queue up geofencing events while background isolate is starting
                //queue.add()
            } else {
                // Callback method name is intentionally left blank.
                Handler(mainLooper).post { BeaconsPlugin.mBackgroundChannel?.invokeMethod("", "onHandleWork") }
            }
        }
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        //beaconScannerImplActivity.stopMonitoringBeacons()
        Log.i(TAG, "onDestroy")
    }
}
