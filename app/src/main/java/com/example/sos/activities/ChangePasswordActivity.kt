package com.example.sos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MyApplication
import com.example.sos.R
import com.example.sos.databinding.ActivityChangePasswordBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.viewmodels.MainViewModel

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MyApplication.firebaseCredential(this), MyApplication.getSharedPrefManager(this))
        )[MainViewModel::class.java]

        binding.viewmodel = mainViewModel
        binding.lifecycleOwner = this

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.addCallback()
        }

        binding.btnCancel.setOnClickListener {
            startActivity(Intent(this@ChangePasswordActivity, SettingsActivity::class.java))
        }

        binding.btnChangePswd.setOnClickListener {
            val pwd = binding.editTextTextPassword.text.toString().trim()
            if (pwd.isEmpty()) {
                Toast.makeText(this, "Enter Correct password", Toast.LENGTH_LONG).show()
            } else if (pwd.length < 6) {
                Toast.makeText(
                    this,
                    "Password length should be more than six char",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


        /** observe password updated Value from viewModel */
        mainViewModel.isPswdUpdatedLiveData.observe(this, Observer { bool ->
            if (bool) {
                Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    this,
                    "Failed / Logout then Login again and Then Change Password",
                    Toast.LENGTH_LONG
                ).show()
            }
        })


    }

    private fun OnBackPressedDispatcher.addCallback() {
        onBackPressed()
    }
}