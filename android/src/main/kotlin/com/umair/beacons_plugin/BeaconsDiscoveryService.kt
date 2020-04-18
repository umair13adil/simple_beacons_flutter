package com.umair.beacons_plugin

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import io.flutter.view.FlutterNativeView
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class BeaconsDiscoveryService : JobIntentService() {
    private val queue = ArrayDeque<List<Any>>()

    private lateinit var mContext: Context

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
}
