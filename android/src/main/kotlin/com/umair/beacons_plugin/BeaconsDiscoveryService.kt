package com.umair.beacons_plugin

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class BeaconsDiscoveryService : JobIntentService() {
    private val queue = ArrayDeque<List<Any>>()

    companion object {
        @JvmStatic
        private val TAG = "GeofencingService"
        @JvmStatic
        private val JOB_ID = UUID.randomUUID().mostSignificantBits.toInt()
        @JvmStatic
        val sServiceStarted = AtomicBoolean(false)

        @JvmStatic
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, BeaconsDiscoveryService::class.java, JOB_ID, work)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        startBLEDiscoveryService(this)
    }

    private fun startBLEDiscoveryService(mContext: Context) {
        synchronized(sServiceStarted) {
            Log.i(TAG,"startBLEDiscoveryService")
            //if (BeaconsPlugin.sBackgroundFlutterView == null) {
                val callbackHandle = mContext.getSharedPreferences(
                        BeaconsPlugin.SHARED_PREFERENCES_KEY,
                        Context.MODE_PRIVATE)
                        .getLong(BeaconsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, 0)

                val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
                if (callbackInfo == null) {
                    Log.e(TAG, "Fatal: failed to find callback")
                    return
                }
                Log.i(TAG, "Starting BLEDiscoveryService...")
                BeaconsPlugin.sBackgroundFlutterView = FlutterNativeView(mContext, true)

                val args = FlutterRunArguments()
                args.bundlePath = FlutterMain.findAppBundlePath()
                args.entrypoint = callbackInfo.callbackName
                args.libraryPath = callbackInfo.callbackLibraryPath

                BeaconsPlugin.sBackgroundFlutterView!!.runFromBundle(args)
                IsolateHolderService.setBackgroundFlutterView(BeaconsPlugin.sBackgroundFlutterView)
           /* }else{
                Log.i(TAG,"sBackgroundFlutterView != null")
            }*/
        }
    }

    override fun onHandleWork(intent: Intent) {
        val callbackHandle = intent.getLongExtra(BeaconsPlugin.CALLBACK_HANDLE_KEY, 0)

        //TODO: Send Callback Handle with Result
        synchronized(sServiceStarted) {
            if (!sServiceStarted.get()) {
                // Queue up geofencing events while background isolate is starting
                //queue.add()
            } else {
                // Callback method name is intentionally left blank.
                //Handler(mContext.mainLooper).post { mBackgroundChannel.invokeMethod("", geofenceUpdateList) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }
}
