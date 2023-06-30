package com.example.sos.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationModel(
    var latitude: String,
    var longitude: String,
    var timestamp: Long
) : Parcelable