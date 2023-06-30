package com.example.sos.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MyApplication
import com.example.sos.R
import com.example.sos.databinding.ActivityRegisteredBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.models.UserDetailModel
import com.example.sos.utils.Utility
import com.example.sos.viewmodels.MainViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject


class RegisteredActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisteredBinding

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_registered)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MyApplication.firebaseCredential(this), MyApplication.getSharedPrefManager(this))
        )[MainViewModel::class.java]

        binding.viewmodel = mainViewModel
        binding.lifecycleOwner = this

        mainViewModel.getUserDataFromFirebase()

        binding.backLogin.setOnClickListener {
            mainViewModel.signOutUser()
            startActivity(Intent(this@RegisteredActivity, LoginActivity::class.java))
        }

        mainViewModel.userDetailModelLiveData.observe(this) {
            if (it != null)
                mainViewModel.fName.postValue(it.fName)
            mainViewModel.lName.postValue(it.lName)
            mainViewModel.email.postValue(it.email)
            mainViewModel.username.postValue(it.username)
            mainViewModel.timestamp.postValue(Utility.timeStampToString(it.timestamp))

        }

    }


}