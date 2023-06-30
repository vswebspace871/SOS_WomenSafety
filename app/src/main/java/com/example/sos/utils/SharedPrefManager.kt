package com.example.sos.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SharedPrefManager(val context: Context) {
    var LOGGED_IN_USERNAME = "LOGGED_IN_USEREMAIL"
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "MySharedPreference",
        MODE_PRIVATE
    )

    fun getUserEmail(): String {
        return sharedPreferences.getString(LOGGED_IN_USERNAME, "")!!
    }

    fun setUserEmail(username: String) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(LOGGED_IN_USERNAME, username)
        editor.apply()
    }
}