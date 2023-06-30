package com.example.sos

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.sos.database.UserDatabase
import com.example.sos.utils.SharedPrefManager
import com.example.sos.utils.Utility
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.*
import kotlinx.coroutines.*

class SensorService : Service() {

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        // start the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
        )

        // ShakeDetector initialization
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()
        mShakeDetector!!.setOnShakeListener(object : ShakeDetector.OnShakeListener {
            @SuppressLint("MissingPermission")
            override fun onShake(count: Int) {
                // check if the user has shacked
                // the phone for 3 time in a row
                if (count == 3) {

                    // vibrate the phone
                    //vibrate()

                    // create FusedLocationProviderClient to get the user location
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                        applicationContext
                    )

                    // use the PRIORITY_BALANCED_POWER_ACCURACY
                    // so that the service doesn't use unnecessary power via GPS
                    // it will only use GPS at this very moment
                    fusedLocationClient.getCurrentLocation(
                        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                        object : CancellationToken() {
                            override fun isCancellationRequested(): Boolean {
                                return false
                            }

                            override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                                CancellationTokenSource().token

                        })
                        //fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            // check if location is null
                            // for both the cases we will
                            // create different messages
                            if (location != null) {
                                val db = UserDatabase.getDatabase(this@SensorService)

                                val email = SharedPrefManager(this@SensorService).getUserEmail()

                                var contact1: String? = null
                                var contact2: String? = null
                                var name1: String? = null
                                var name2: String? = null
                                var UserMsg : String? = null
                                fun main() = runBlocking {
                                    launch(Dispatchers.IO) {
                                        val list =
                                            db.contactDao().getContacts(email, Utility.SMS_TOKEN)
                                        if (list != null && list.isNotEmpty()) {
                                            contact1 = list[0].contact.toString()
                                            name1 = list[0].name.toString()
                                            contact2 = list[1].contact.toString()
                                            name2 = list[1].name.toString()
                                        }
                                        val msg = db.messageDao().getMessage(email, Utility.SMS_TOKEN)
                                        if (msg != null){
                                            UserMsg = msg.message
                                        }
                                    }.join()
                                }

                                main() // calling the main function which create with Run Blocking


                                // get the SMSManager

                                val smsManager = SmsManager.getDefault()
                                var message : String? = null
                                if (contact1 != null && UserMsg != null && name1 != null){
                                    message =
                                        "Hello $name1, $UserMsg and Here are my coordinates.\n" +
                                                " http://maps.google.com/?q=${location.latitude},${location.longitude}"
                                    smsManager.sendTextMessage(
                                        contact1,
                                        null,
                                        message,
                                        null,
                                        null
                                    )
                                }

                                if (contact2 != null && UserMsg != null && name2 != null){
                                    message =
                                        "Hello $name2, $UserMsg and Here are my coordinates.\n" +
                                                " http://maps.google.com/?q=${location.latitude},${location.longitude}"
                                    smsManager.sendTextMessage(
                                        contact2,
                                        null,
                                        message,
                                        null,
                                        null
                                    )
                                }


                                // get the list of all the contacts in Database
                                /*val db = DbHelper(this@SensorService)
                                val list: List<ContactModel> = db.getAllContacts()

                                // send SMS to each contact
                                for (c in list) {
                                    val message =
                                        """Hey, ${c.getName()}I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.
     http://maps.google.com/?q=${location.latitude},${location.longitude}"""
                                    smsManager.sendTextMessage(
                                        c.getPhoneNo(),
                                        null,
                                        message,
                                        null,
                                        null
                                    )
                                }*/
                            } else {
                                /*val message =
                                    """
                                I am in DANGER, i need help. Please urgently reach me out.
                                GPS was turned off.Couldn't find location. Call your nearest Police Station.
                                """.trimIndent()
                                val smsManager = SmsManager.getDefault()
                                // val db = DbHelper(this@SensorService)
                                //val list: List<ContactModel> = db.getAllContacts()
                                *//* for (c in list) {
                                 smsManager.sendTextMessage(
                                     c.getPhoneNo(),
                                     null,
                                     message,
                                     null,
                                     null
                                 )
                             }*/

                                // get the SMSManager
                                val smsManager = SmsManager.getDefault()
                                val message =
                                    "Hellooooooo"
                                smsManager.sendTextMessage(
                                    "8287012854",
                                    null,
                                    message,
                                    null,
                                    null
                                )
                            }
                        }.addOnFailureListener {
                            /* Log.d("Check: ", "OnFailure")
                             val message =
                                 """
                             I am in DANGER, i need help. Please urgently reach me out.
                             GPS was turned off.Couldn't find location. Call your nearest Police Station.
                             """.trimIndent()
                             val smsManager = SmsManager.getDefault()
                             //val db = DbHelper(this@SensorService)
                             //val list: List<ContactModel> = db.getAllContacts()
                             *//*for (c in list) {
                            smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null)
                        }*/
                        }
                }
            }
        })
        // register the listener
        mSensorManager!!.registerListener(
            mShakeDetector,
            mAccelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    // method to vibrate the phone
    /* fun vibrate() {
         val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
         val vibEff: VibrationEffect

         // Android Q and above have some predefined vibrating patterns
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
             vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
             vibrator.cancel()
             vibrator.vibrate(vibEff)
         } else {
             vibrator.vibrate(500)
         }
     }*/

    // For Build versions higher than Android Oreo, we launch
    // a foreground service in a different way. This is due to the newly
    // implemented strict notification rules, which require us to identify
    // our own notification channel in order to view them correctly.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("You are protected.")
            .setContentText("We are there for you") // this is important, otherwise the notification will show the way
            // you want i.e. it will show some default notification
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        // create an Intent to call the Broadcast receiver
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ReactivateService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }
}