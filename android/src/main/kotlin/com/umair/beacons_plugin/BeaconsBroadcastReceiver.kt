package com.umair.beacons_plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.flutter.view.FlutterMain


class BeaconsBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BeaconsBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        FlutterMain.ensureInitializationComplete(context, null)
        BeaconsPlugin.startBackgroundService(context)
    }
}