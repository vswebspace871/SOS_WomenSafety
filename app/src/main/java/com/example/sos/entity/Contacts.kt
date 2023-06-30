package com.example.sos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("contacts")
data class Contacts(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,

    var user_email: String,

    var token: String,

    var name: String?,

    var contact: String?
)