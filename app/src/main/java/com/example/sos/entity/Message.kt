package com.example.sos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("message")
data class Message(

    @PrimaryKey(autoGenerate = true)
    var id: Int?,

    var user_email: String,

    var token: String,

    var message: String
)