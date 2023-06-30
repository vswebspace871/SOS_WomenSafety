package com.example.sos

import android.app.Application
import android.content.Context
import com.example.sos.database.UserDatabase
import com.example.sos.repository.MainRepository
import com.example.sos.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth

class MyApplication: Application() {

    init {
        instance = this
    }

    companion object {
        lateinit var database: UserDatabase
        //private lateinit var appContext: Context
        lateinit var instance: MyApplication


        fun getSharedPrefManager(context: Context): SharedPrefManager {
            return SharedPrefManager(context)
        }

        fun firebaseCredential(context: Context?): MainRepository {
            database = UserDatabase.getDatabase(context!!.applicationContext)
            return MainRepository(database)
        }
    }

}