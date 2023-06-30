package com.example.sos.utils

import android.text.format.DateFormat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

object Utility {

    val SMS_TOKEN = "sms"
    val EMAIL_TOKEN = "email"

    val EMERGENCY_PHONE_NO = "911"

    fun getCollectionReference(): CollectionReference {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return FirebaseFirestore.getInstance()
            .collection("User_Details")
            .document(currentUser!!.uid)
            .collection("details")
    }

    fun getLocationCollectionReference(): CollectionReference {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return FirebaseFirestore.getInstance()
            .collection("User_Details")
            .document(currentUser!!.uid)
            .collection("locations")
    }

    fun timeStampToString(timestamp: Timestamp): String? {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp.toDate())
    }

    fun LongtimeStampToString(timestamp: Long): String? {
        return DateFormat.format("E, MMM d, yyyy HH:mm a", Date(timestamp)).toString()
    }


}