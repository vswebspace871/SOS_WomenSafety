package com.example.sos.fragments

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sos.*
import com.example.sos.activities.ChangePasswordActivity
import com.example.sos.activities.LoginActivity
import com.example.sos.databinding.FragmentPersonalBinding
import com.example.sos.entity.UserDetail
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.viewmodels.MainViewModel


class PersonalFragment : Fragment() {

    private lateinit var binding: FragmentPersonalBinding
    private lateinit var mainViewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.O)
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal, container, false)
        //binding = FragmentPersonalBinding.inflate(inflater, container, false)

        binding.editTextPhone3.afterTextChangedDelayed {
            val model = UserDetail(0, it)
            mainViewModel.insertNumber(model)
        }

        mainViewModel.isNumberAddedLiveData.observe(requireActivity(), Observer {
            if (it) {
                Toast.makeText(requireContext(), "Number Added", Toast.LENGTH_LONG).show()
            }
        })

        binding.viewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.checkBox.setOnCheckedChangeListener { p0, bool ->
            if (bool) {
                startTheLocationTrackService()
            } else {
                //stopLocationService()
            }
        }

        //showing firstname and lastname
        mainViewModel.getUserDataFromFirebase()
        binding.LL.visibility = View.VISIBLE
        return binding.root
    }

    private fun stopLocationService() {
        val intent = Intent(requireContext(), LocationService::class.java)
        if (isMyServiceRunning(LocationService::class.java)) {
            context?.stopService(intent)
        }
    }

    fun TextView.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            var timer: CountDownTimer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                timer?.cancel()
                timer = object : CountDownTimer(5000, 1500) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        afterTextChanged.invoke(editable.toString())
                    }
                }.start()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTheLocationTrackService() {
        val intent = Intent(requireContext(), LocationForeGroundService::class.java)
        if (!isMyServiceRunning(LocationForeGroundService::class.java)) {
            context?.startForegroundService(intent)
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.userDetailModelLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mainViewModel.fName.postValue(it.fName)
                mainViewModel.lName.postValue(it.lName)
            }
        })
        //change password
        binding.btnCHangePswd.setOnClickListener {
            activity?.startActivity(Intent(activity, ChangePasswordActivity::class.java))
        }

        //check if User logout ?
        mainViewModel.isUserSignOutLiveData.observe(viewLifecycleOwner, Observer { bool ->
            if (bool) {
                val i = Intent(requireContext(), LoginActivity::class.java)
                // set the new task and clear flags, Clear all activity from back Stack
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
            }
        })
    }
}