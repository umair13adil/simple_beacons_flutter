package com.umair.beacons_plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class RebootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.e("GEOFENCING REBOOT", "Reregistering geofences!")
            //TODO: Listen to beacons on Reboot
        }
    }
}