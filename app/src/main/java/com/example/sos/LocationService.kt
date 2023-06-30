package com.example.sos

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.sos.models.LocationModel
import com.example.sos.utils.Utility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.Timestamp

class LocationService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("TAG", "onStartCommand: Location Service Started")

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(
            applicationContext
        )

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                getLocation(fusedLocationClient)
                handler.postDelayed(this, 1000*60)
            }
        }, 1000)

        //getLocation(fusedLocationClient)
        return START_STICKY
    }

    private fun getLocation(fusedLocationClient: FusedLocationProviderClient) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                object : CancellationToken() {
                    override fun isCancellationRequested(): Boolean {
                        return false
                    }

                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                })
                .addOnSuccessListener { location ->
                    // check if location is null
                    // for both the cases we will
                    // create different messages
                    if (location != null) {
                        var locationModel  = LocationModel(
                            location.latitude.toString(),
                            location.longitude.toString(),
                            Timestamp.now().seconds
                        )

                        Log.d("TAG", "Latitude Run Once :  ${location.latitude} ")

                        val docref = Utility.getLocationCollectionReference().document()
                        docref.set(locationModel).addOnCompleteListener(OnCompleteListener {

                        })
                    }
                }.addOnFailureListener(OnFailureListener {
                    stopSelf()
                })
        }
    }

    override fun onDestroy() {
        // create an Intent to call the Broadcast receiver
        //Restart Service
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ReactivateLocationService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }
}