package com.example.sos

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class ForegroundService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        // start the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else {
            startForeground(1, Notification())
        }

        connectionTimer()
    }

    private fun connectionTimer() {
        // Don't start the timer multiple times
        /*MyApplication.ensureCoreExists(applicationContext)
        if (MyApplication.timer != null) {
            return
        }

        MyApplication.timer = Timer()
        //Set the schedule function and rate
        MyApplication.timer!!.scheduleAtFixedRate(timerTask {
            //reRegister()
            Log.d("abhinavS", "connectionTimer running")
            if (MientryManager==null || MientryManager.getCore()==null) {
                MyApplication.ensureCoreExists(applicationContext)
            } else {
                //if (MientryManager.getCore()?.)
                MientryManager.getCore()?.refreshRegisters()
            }
        }, 0, 60000)*/
    }

    // For Build versions higher than Android Oreo, we launch
    // a foreground service in a different way. This is due to the newly
    // implemented strict notification rules, which require us to identify
    // our own notification channel in order to view them correctly.
    @RequiresApi(Build.VERSION_CODES.O)
    fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "sos.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(chan)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("MiEntry Service")
            .setContentText("Receives SIP calls")
            .setSmallIcon(R.drawable.siren)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(2, notification)
    }

    override fun onDestroy() {
        // create an Intent to call the Broadcast receiver
        /*val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, MyBroadcastReceiver::class.java)
        this.sendBroadcast(broadcastIntent)*/
        super.onDestroy()
    }
}