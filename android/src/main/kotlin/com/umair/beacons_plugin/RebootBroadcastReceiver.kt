package com.umair.beacons_plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class RebootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {

            //Start Background service to scan BLE devices
            BeaconsPlugin.startBackgroundService(context)
        }
    }
}