package com.example.sos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ReactivateService : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        Log.d("Check: ", "Receiver Started")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(Intent(context, SensorService::class.java))
        } else {
            context?.startService(Intent(context, SensorService::class.java))
        }
    }
}