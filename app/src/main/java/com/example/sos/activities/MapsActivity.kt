package com.example.sos.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MyApplication
import com.example.sos.R
import com.example.sos.databinding.ActivityMapsBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.models.LocationModel
import com.example.sos.utils.Utility
import com.example.sos.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mainViewModel: MainViewModel

    var markersArray: List<LocationModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                MyApplication.firebaseCredential(this@MapsActivity),
                MyApplication.getSharedPrefManager(this@MapsActivity)
            )
        )[MainViewModel::class.java]

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.addCallback()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {


        val fromTime = intent.getLongExtra("fromTime", 0)
        val toTime = intent.getLongExtra("toTime", 0)

        mainViewModel.fetchLatLongFromFirebase(fromTime, toTime)

        mainViewModel.listOfLatLongLiveData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                /* for (list in it) {
                     Log.d(
                         "TAG",
                         "LatLong List item: ${list.latitude}, ${list.longitude}, ${list.timestamp} \n\n"
                     )

                 }*/

                markersArray = it

                for (i in markersArray.indices) {
                    createMarker(
                        markersArray[i].latitude,
                        markersArray[i].longitude,
                        markersArray[i].timestamp,
                        googleMap
                    )
                }
            }
        })


        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap.isMyLocationEnabled = true


        mMap = googleMap
        // Add a marker in Sydney and move the camera
        //val myLocation = LatLng(39.1864814, -96.5749807)
        //mMap.addMarker(MarkerOptions().position(myLocation).title("My Location"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
//        mMap.uiSettings.isCompassEnabled = true
//        mMap.uiSettings.isMyLocationButtonEnabled = true
//        mMap.uiSettings.isZoomControlsEnabled = true
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17f))
    }

    private fun createMarker(
        latitude: String,
        longitude: String,
        timestamp: Long,
        googleMap: GoogleMap,
    ): Marker? {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null
        }
        googleMap.isMyLocationEnabled = true

        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        val myLocation = LatLng(latitude.toDouble(), longitude.toDouble())

        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17f))
        val geocoder = Geocoder(this, Locale.getDefault())
        val list: List<Address> =
            geocoder.getFromLocation(
                latitude.toDouble(),
                longitude.toDouble(),
                1
            ) as List<Address>
        val addr = list[0].getAddressLine(0)
        return mMap.addMarker(
            MarkerOptions()
                .position(LatLng(latitude.toDouble(), longitude.toDouble()))
                .anchor(0.5f, 0.5f)
                .title(addr)
                .snippet(Utility.LongtimeStampToString(timestamp))
        )

    }

    private fun OnBackPressedDispatcher.addCallback() {
        onBackPressed()
    }
}