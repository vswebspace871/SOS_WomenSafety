package com.example.sos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MyApplication
import com.example.sos.R
import com.example.sos.databinding.ActivityResetPwdBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.viewmodels.MainViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class ResetPwdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPwdBinding

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_pwd)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MyApplication.firebaseCredential(this), MyApplication.getSharedPrefManager(this))
        )[MainViewModel::class.java]

        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this


        binding.button2.setOnClickListener {
            startActivity(Intent(this@ResetPwdActivity, LoginActivity::class.java))
            finish()
        }

        mainViewModel.isPswdResetLiveData.observe(this, Observer { bool ->
            if (bool) {
                binding.tvMailSent.visibility = View.VISIBLE
                binding.etEmailId.setText("")
            } else {
                Toast.makeText(this, "Enter Valid Email id", Toast.LENGTH_LONG).show()
            }
        })

    }
}