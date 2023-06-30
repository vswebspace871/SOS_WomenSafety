package com.example.sos.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MyApplication
import com.example.sos.R
import com.example.sos.databinding.ActivityLoginBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.utils.SharedPrefManager
import com.example.sos.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var mainViewModel: MainViewModel
    lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        sharedPrefManager = SharedPrefManager(this)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MyApplication.firebaseCredential(this), MyApplication.getSharedPrefManager(this))
        )[MainViewModel::class.java]

        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        binding.btnForgotPsw.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ResetPwdActivity::class.java))
        }

        binding.buttonReg.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        mainViewModel.isUserloggedInLive.observe(this, Observer { bool ->
            if (bool) {
                sharedPrefManager.setUserEmail(mainViewModel.email.value.toString())
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Invalid Username/Password", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }
}