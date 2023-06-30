package com.example.sos.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.sos.*
import com.example.sos.databinding.ActivityMainBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.utils.SharedPrefManager
import com.example.sos.utils.Utility
import com.example.sos.viewmodels.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import okhttp3.internal.Version
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {


    private var savedMsg: String? = null
    private var contact2: String? = null
    private var contact1: String? = null

    private lateinit var binding: ActivityMainBinding
    var isButtonEnabled = false
    private lateinit var mainViewModel: MainViewModel

    /** */
    private lateinit var lastlocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionId: Int = 201

    /** */


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                MyApplication.firebaseCredential(this),
                MyApplication.getSharedPrefManager(this)
            )
        )[MainViewModel::class.java]

        /** SMS CODE */
        //checkSMSPermission()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        checkAndRequestPermissions()

        /** SMS CODE */


        /** */

        /** */


        //getLocation()

        startTheServiceSOS()

        /*val foregroundService = LocationForeGroundService()
        val intent = Intent(this, ForegroundService::class.java)
        if (!isMyServiceRunning(foregroundService::class.java)) {
            startService(intent)
        }*/

        binding.imageView4.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }

        binding.switch2.setOnCheckedChangeListener { btn, bool ->
            if (bool) {
                isButtonEnabled = true

                binding.ivPanic.setImageResource(R.drawable.panic)
                binding.ivOk.setImageResource(R.drawable.ook)
                binding.ivEmer.setImageResource(R.drawable.emer)

                checkCallPermission()

                enablePanicButton()
                enableOkButton()
                enableEmerButton()


            } else {
                isButtonEnabled = false

                binding.ivPanic.setImageResource(R.drawable.panic1)
                binding.ivOk.setImageResource(R.drawable.ook1)
                binding.ivEmer.setImageResource(R.drawable.emer1)

                disablePanicButton()
                disableOkButton()
                disableEmerButton()
            }
        }

        binding.ivPanic.setOnClickListener {
            if (isButtonEnabled) {
                pressedAnimation(it)
            }
        }
        binding.ivOk.setOnClickListener {
            if (isButtonEnabled) {
                pressedAnimation(it)
            }
        }
        binding.ivEmer.setOnClickListener {
            if (isButtonEnabled) {
                pressedAnimation(it)
            }
        }
    }

    private fun startTheServiceSOS() {

        // start the service
        val intent = Intent(this, SensorService::class.java)
        if (!isMyServiceRunning(SensorService::class.java)) {
            startService(intent)
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkSMSPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) +
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
            != PackageManager.PERMISSION_GRANTED
        ) {

            // SMS Permission is not granted
            // Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    "Manifest.permission.READ_SMS"
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    "Manifest.permission.SEND_SMS"
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    101
                )

                // REQUEST_CODE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkAndRequestPermissions(): Boolean {
        var permissionList = mutableListOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        /*if (VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionList = mutableListOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }*/
        //check if permissions are already granted or not in loop
        val permissionsNotGranted: MutableList<String> = ArrayList()
        for (permission in permissionList) {
            //check if permission is already granted or not
            val result = ContextCompat.checkSelfPermission(applicationContext, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                //permission not granted
                permissionsNotGranted.add(permission)
            }
        }

        //check if permissionsNotGranted list is not empty
        if (permissionsNotGranted.isNotEmpty()) {
            /*val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if ()*/
            ActivityCompat.requestPermissions(
                this@MainActivity,
                permissionsNotGranted.toTypedArray(),
                permissionId
            )
            return false
        } else {
            checkAndRequestBgLocationPermission()
            return true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkAndRequestBgLocationPermission() {
        var permissionList = mutableListOf(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        //check if permissions are already granted or not in loop
        val permissionsNotGranted: MutableList<String> = ArrayList()
        for (permission in permissionList) {
            //check if permission is already granted or not
            val result = ContextCompat.checkSelfPermission(applicationContext, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                //permission not granted
                permissionsNotGranted.add(permission)
            }
        }

        //check if permissionsNotGranted list is not empty
        if (permissionsNotGranted.isNotEmpty()) {
            /*val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if ()*/
            ActivityCompat.requestPermissions(
                this@MainActivity,
                permissionsNotGranted.toTypedArray(),
                permissionId
            )
            return
        } else {
            getLocation()
        }
    }


    private fun checkCallPermission() {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS),
                101
            )
        }
    }

    private fun pressedAnimation(view: View) {
        val animation: Animation =
            TranslateAnimation(0f, 0f, 0f, 20f)
        //AlphaAnimation(1f, 0.5f) //to change visibility from visible to half-visible

        animation.duration = 50 // 100 millisecond duration for each animation cycle

        animation.repeatMode =
            Animation.REVERSE //animation will start from end point once ended.

        view.startAnimation(animation) //to start animation
    }

    private fun disableOkButton() {
        binding.ivOk.isClickable = false
    }

    private fun disableEmerButton() {
        binding.ivEmer.isClickable = false
    }

    private fun enableEmerButton() {
        binding.ivEmer.isClickable = true
        binding.ivEmer.setOnClickListener {
            pressedAnimation(it)
            makeCall()
        }
    }

    private fun makeCall() {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:${Utility.EMERGENCY_PHONE_NO}")
        startActivity(callIntent)
    }

    private fun enableOkButton() {
        binding.ivOk.isClickable = true
        binding.ivOk.setOnClickListener {
            pressedAnimation(it)
            //sendSMS("I am Ok")
        }
    }

    private fun disablePanicButton() {
        binding.ivPanic.isClickable = false
    }

    private fun enablePanicButton() {
        var isSMSSent1 = false
        var isSMSSent2 = false

        binding.ivPanic.isClickable = true
        binding.ivPanic.setOnClickListener {
            //sendWhtsappMsg()
            pressedAnimation(it)

            //getting Contact from Db
            mainViewModel.getContacts(SharedPrefManager(this).getUserEmail(), Utility.SMS_TOKEN)
            mainViewModel.listOfContact.observe(this, androidx.lifecycle.Observer { list ->
                if (list != null) {
                    contact1 = list[0].contact.toString()
                    contact2 = list[1].contact.toString()
                }
            })

            //getting msg from Db
            mainViewModel.getMessage(SharedPrefManager(this).getUserEmail(), Utility.SMS_TOKEN)
            mainViewModel.msgModel.observe(this, androidx.lifecycle.Observer { msg ->
                if (msg != null) {
                    savedMsg = msg.message
                }
            })
            if (contact1 != null && savedMsg != null) {
                sendSMS(contact1, savedMsg)
                isSMSSent1 = true
            }

            if (contact1 != null && savedMsg != null) {
                sendSMS(contact2, savedMsg)
                isSMSSent2 = true
            }

            if (!isSMSSent1 && !isSMSSent2) {
                Toast.makeText(this, "No Contact Saved", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun sendWhtsappMsg() {
        var phone = "8287012854"
        var message = "Hello"

        /** This code OPENED WHTSAPP APP */
        /*val uri = Uri.parse("smsto:$phone")
        val i = Intent(Intent.ACTION_SENDTO, uri)
        i.putExtra("sms_body", message)
        i.setPackage("com.whatsapp")
        startActivity(i)*/
        /** This code OPENED WHTSAPP APP */

        /** CODE WORKS 100 % */

        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
        sendIntent.putExtra("jid", "$phone@s.whatsapp.net") //phone number without "+" prefix

        sendIntent.setPackage("com.whatsapp")
        if (intent.resolveActivity(this.packageManager) == null) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(sendIntent)

        /** CODE WORKS 100 % */
    }

    private fun sendSMS(contact: String?, msg: String?) {
        val smsManager: SmsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contact, null, msg, null, null)
        Toast.makeText(this@MainActivity, "Sent", Toast.LENGTH_LONG).show()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        //checkAndRequestBgLocationPermission()
        if (checkLocationPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            ) as List<Address>

                        binding.progressBarLoading.visibility = View.GONE
                        binding.tvLocation.visibility = View.VISIBLE

                        binding.tvLocation.text = list[0].getAddressLine(0)
                        //binding.tvLocation.text = "lattitude = ${location.latitude}"
                    }
                }
            }
        } else {
            //requestLocationPermissions()
            //checkAndRequestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            permissionId
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        /*if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }*/
        when (requestCode) {
            permissionId -> {
                var allPermissionsGranted = true

                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                    }
                }

                //check if any permission is denied
                if (!allPermissionsGranted) {
                    //checkAndRequestPermissions()
                    //show a dialog and inform user that please grant all permissions
                    //so that app can run smoothly
                    //show dialog function here
                    checkAndRequestPermissions()//
                } else {
                    //getLocation()
                    //checkAndRequestBgLocationPermission()
                    checkAndRequestPermissions()
                }
            }
        }
    }

    override fun onDestroy() {
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ReactivateService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }
}
