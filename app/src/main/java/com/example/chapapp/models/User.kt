package com.example.chapapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, val profileImageUrL: String): Parcelable{
    constructor() : this("","","")
}