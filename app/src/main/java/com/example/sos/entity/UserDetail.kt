package com.example.sos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_detail")
data class UserDetail(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var number : String
)