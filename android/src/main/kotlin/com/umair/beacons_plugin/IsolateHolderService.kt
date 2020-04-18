package com.umair.beacons_plugin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import io.flutter.view.FlutterNativeView

class IsolateHolderService : Service() {

    private val CHANNEL_ID = "ForegroundService Kotlin"

    companion object {
        @JvmStatic
        val ACTION_SHUTDOWN = "SHUTDOWN"
        @JvmStatic
        private val WAKELOCK_TAG = "IsolateHolderService::WAKE_LOCK"
        @JvmStatic
        private val TAG = "IsolateHolderService"
        @JvmStatic
        private var sBackgroundFlutterView: FlutterNativeView? = null

        @JvmStatic
        fun setBackgroundFlutterView(view: FlutterNativeView?) {
            sBackgroundFlutterView = view
        }
    }

    override fun onBind(p0: Intent) : IBinder? {
        return null;
    }

    override fun onCreate() {
        super.onCreate()
        val CHANNEL_ID = "beacons_plugin_channel"
        createNotificationChannel()
        val imageId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName())

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Almost home!")
                .setContentText("Within 1KM of home. Fine location tracking enabled.")
                .setSmallIcon(imageId)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                setReferenceCounted(false)
                acquire()
            }
        }
        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) : Int {
        if (intent.getAction() == ACTION_SHUTDOWN) {
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                    if (isHeld()) {
                        release()
                    }
                }
            }
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY;
    }
}
