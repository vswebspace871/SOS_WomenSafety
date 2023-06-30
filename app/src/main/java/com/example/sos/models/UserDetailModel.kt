package com.example.sos.models

import java.sql.Timestamp

data class UserDetailModel(
    var fName: String,
    var lName: String,
    var email: String,
    var username: String,
    var timestamp: com.google.firebase.Timestamp
)