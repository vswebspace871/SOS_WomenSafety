package com.example.sos.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MyApplication
import com.example.sos.activities.MapsActivity
import com.example.sos.databinding.FragmentLocationBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.models.LocationModel
import com.example.sos.viewmodels.MainViewModel
import com.google.firebase.Timestamp
import com.google.firestore.v1.DocumentTransform.FieldTransform.ServerValue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class LocationFragment : Fragment() {

    private lateinit var binding: FragmentLocationBinding
    private lateinit var mainViewModel: MainViewModel

    //for first Calender Object
    private var YEAR: Int = 0
    private var MONTH: Int = 0
    private var DAYOFMONTH: Int = 0
    private var HOUR: Int = 0
    private var SECOND: Int = 0

    //for Second Calender Object
    private var YEAR1: Int = 0
    private var MONTH1: Int = 0
    private var DAYOFMONTH1: Int = 0
    private var HOUR1: Int = 0
    private var SECOND1: Int = 0

    private var TIMESTAMP1 : Long = 0
    private var TIMESTAMP2 : Long = 0




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                MyApplication.firebaseCredential(requireContext()),
                MyApplication.getSharedPrefManager(requireContext())
            )
        )[MainViewModel::class.java]
        // Inflate the layout for this fragment
        binding = FragmentLocationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TAG", "CURRENT TIMESTAMP = ${Timestamp.now()} ")

        binding.etDate.setOnClickListener {
            fromDateOpnCalender()
        }

        binding.etTimeFrom.setOnClickListener {
            fromTimePicker()
        }

        binding.etTimeTo.setOnClickListener {
            toTimePicker()
        }

        binding.btnSelect.setOnClickListener {
            createTimeStamp()
            createTimeStamp2()

            if (TIMESTAMP2 > TIMESTAMP1){
                //Fetch Data from Firebase
                var intent = Intent(requireContext(), MapsActivity::class.java)
               /* intent.putExtra("data1", TIMESTAMP1)
                intent.putExtra("data2", TIMESTAMP2)*/

                intent.putExtra("fromTime", TIMESTAMP1)
               intent.putExtra("toTime", TIMESTAMP2)
                requireActivity().startActivity(intent)

              /*  mainViewModel.fetchLatLongFromFirebase(TIMESTAMP1, TIMESTAMP2)
                mainViewModel.listOfLatLongLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                   if (it != null){
                       for (list in it){
                           val list1 : ArrayList<LocationModel> = it as ArrayList<LocationModel>
                           Log.d("TAG", "LatLong List item: ${list.latitude}, ${list.longitude}, ${list.timestamp} \n\n")

                           var intent = Intent(requireContext(), MapsActivity::class.java)
                           intent.putParcelableArrayListExtra("list", list1)
                           requireActivity().startActivity(Intent(requireContext(), MapsActivity::class.java))
                       }
                   }
                })*/
            }else {
                Toast.makeText(requireContext(), "Select proper Date And Time", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createTimeStamp2() {
        val calendar = Calendar.getInstance()
        calendar.set(YEAR1, MONTH1, DAYOFMONTH1, HOUR1, SECOND1, 0)
        val time2 = calendar.timeInMillis
        TIMESTAMP2 = time2
        Log.d("TAG", "SELECTED TIMESTAMP 2  ${time2}")
    }

    private fun createTimeStamp() {
        val calendar = Calendar.getInstance()
        calendar.set(YEAR, MONTH, DAYOFMONTH, HOUR, SECOND, 0)
        val time1 = calendar.timeInMillis
        TIMESTAMP1 = time1
        Log.d("TAG", "SELECTED TIMESTAMP 1  ${time1}")
    }

    private fun toTimePicker() {
        val calendar = Calendar.getInstance()
        var hour = calendar[Calendar.HOUR_OF_DAY]
        var minute = calendar[Calendar.MINUTE]

        var setListener = TimePickerDialog.OnTimeSetListener { timePicker, i, i2 ->
            calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
            calendar.set(0, 0, 0, i, i2)

            HOUR1 = i
            SECOND1 = i2

            var timeInMillis = calendar.timeInMillis

            val formattedTime = getFormattedTime(timeInMillis)
            binding.etTimeTo.setText(formattedTime)
        }

        var timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.THEME_HOLO_LIGHT,
            setListener,
            hour,
            minute,
            false
        )
        timePickerDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        timePickerDialog.show()
    }

    private fun fromTimePicker() {
        val calendar = Calendar.getInstance()
        var hour = calendar[Calendar.HOUR_OF_DAY]
        var minute = calendar[Calendar.MINUTE]

        var setListener = TimePickerDialog.OnTimeSetListener { timePicker, i, i2 ->
            calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
            calendar.set(0, 0, 0, i, i2)

            HOUR = i
            SECOND = i2

            var timeInMillis = calendar.timeInMillis
            val formattedTime = getFormattedTime(timeInMillis)
            binding.etTimeFrom.setText(formattedTime)
        }

        var timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.THEME_HOLO_LIGHT,
            setListener,
            hour,
            minute,
            false
        )

        timePickerDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        timePickerDialog.show()
    }

    private fun fromDateOpnCalender() {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]

        var setListener: DatePickerDialog.OnDateSetListener? =
            DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
                var mon = m + 1
                calendar.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
                calendar.set(y, m, d, 0, 0)

                YEAR = y
                MONTH = m
                DAYOFMONTH = d

                YEAR1 = y
                MONTH1 = m
                DAYOFMONTH1 = d


                val timeInMillis = calendar.timeInMillis
                val formattedDate = getFormattedDateFromTimestamp(timeInMillis)
                binding.etDate.text = formattedDate

            }
        var datePickerDialog = DatePickerDialog(
            requireContext(),
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            setListener,
            year,
            month,
            day
        )
        datePickerDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        datePickerDialog.show()
    }

    fun getFormattedDateFromTimestamp(timestampInMilliSeconds: Long): String? {
        val date = Date()
        date.time = timestampInMilliSeconds
        return SimpleDateFormat("E, MMM d, yyyy").format(date)
    }

    fun getFormattedTime(timestampInMilliSeconds: Long): String? {
        val date = Date()
        date.time = timestampInMilliSeconds
        return SimpleDateFormat("HH:mm a").format(date)
    }


}