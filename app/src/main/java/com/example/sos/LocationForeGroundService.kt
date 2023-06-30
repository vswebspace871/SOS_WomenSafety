package com.example.sos

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sos.models.LocationModel
import com.example.sos.utils.Utility
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp

class LocationForeGroundService : Service() {

    companion object {

        const val CHANNEL_ID = "12345"
        const val NOTIFICATION_ID = "12345"
    }

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null

    private var notificationManager: NotificationManager? = null

    private var location: Location? = null

    override fun onCreate() {
        super.onCreate()

        // start the foreground service
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else {
            startForeground(1, Notification())
        }
*/
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000 )
            .setIntervalMillis(1000* 60 * 15)
            .setMinUpdateDistanceMeters(100F)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)
            }
        }

        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, "locations",
                NotificationManager.IMPORTANCE_MIN
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }

    }

    // For Build versions higher than Android Oreo, we launch
    // a foreground service in a different way. This is due to the newly
    // implemented strict notification rules, which require us to identify
    // our own notification channel in order to view them correctly.
    /*@RequiresApi(Build.VERSION_CODES.O)
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
    }*/


    @SuppressLint("MissingPermission")
    fun createLocationRequest() {
        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }


    private fun onNewLocation(locationResult: LocationResult) {
        location = locationResult.lastLocation
        Log.d("TAG", "onNewLocation: latLong = ${location?.latitude}")


        if (location != null) {
            var locationModel  = LocationModel(
                location?.latitude.toString(),
                location?.longitude.toString(),
                System.currentTimeMillis()
            )

            Log.d("TAG", "Millisec :  ${System.currentTimeMillis()} ")

            val docref = Utility.getLocationCollectionReference().document()
            docref.set(locationModel).addOnCompleteListener(OnCompleteListener {
            })
        }
        /*  org.greenrobot.eventbus.EventBus.getDefault().post(LocationEvent(
              latitude = location?.latitude,
              longitude = location?.longitude
          ))
  */
        /** Send Notification */
        startForeground(NOTIFICATION_ID.toInt(), getNotification())
    }



    //create notification
    fun getNotification(): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SOS Service")
            .setContentText("Service Running")
            .setSmallIcon(R.drawable.siren)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setChannelId(CHANNEL_ID)
        }
        return notification.build()
    }


    // ILocationListener is a way for the Service to subscribe for updates
    // from the System location Service

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createLocationRequest()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }


}