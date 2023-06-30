package com.example.sos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MyApplication
import com.example.sos.R
import com.example.sos.databinding.ActivityRegisterBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.models.UserDetailModel
import com.example.sos.utils.SharedPrefManager
import com.example.sos.utils.Utility
import com.example.sos.viewmodels.MainViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mainViewModel: MainViewModel
    lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MyApplication.firebaseCredential(this), MyApplication.getSharedPrefManager(this))
        )[MainViewModel::class.java]

        binding.viewmodel = mainViewModel
        binding.lifecycleOwner = this

        sharedPrefManager = SharedPrefManager(this)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.etPassword.afterTextChangedDelayed { password ->
            mainViewModel.checkPasswordLength(password)
        }

        mainViewModel.isSmallPassword.observe(this, Observer { isSmallPassword ->
            if (isSmallPassword) {
                binding.etPassword.setBackgroundResource(R.drawable.edittext_red_border)
                binding.etPassword.error = "Password length should exact 6 or more than 6 Char"
            } else {
                binding.etPassword.setBackgroundResource(R.drawable.edittext_white_border)
            }
        })

        /** if userCreated Successfully it will take to Registered activity*/

        mainViewModel.isUserloggedInLive.observe(this, Observer { bool ->
            if (bool) {
                binding.progressBar.visibility = View.VISIBLE
                binding.buttonRegister.visibility = View.GONE
            }else {
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
            }
        })


        mainViewModel.userSavedLivedata.observe(this, Observer { bool ->
            if (bool) {
                sharedPrefManager.setUserEmail(mainViewModel.email.value.toString())
                Log.d("TAG", "onCreate: Email Saved =${mainViewModel.email.value.toString()} ")
                binding.progressBar.visibility = View.GONE
                binding.buttonRegister.visibility = View.VISIBLE
                //mainViewModel.insertUserDetailInFireStore()
                startActivity(Intent(this@RegisterActivity, RegisteredActivity::class.java))
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
            }
        })


        /** back arrow on toolbar , onBackPressed will function */
        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.addCallback()
        }

        binding.btnBackToLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }
    }

    private fun OnBackPressedDispatcher.addCallback() {
        onBackPressed()
    }

    fun TextView.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            var timer: CountDownTimer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                timer?.cancel()
                timer = object : CountDownTimer(1000, 1500) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        afterTextChanged.invoke(editable.toString())
                    }
                }.start()
            }
        })
    }
}