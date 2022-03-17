package com.ags.annada.userposts.datasource.room.entities

import com.google.gson.annotations.SerializedName

class Company(
    @SerializedName("name")
    val companyName: String?,

    val catchPhrase: String?,

    val bs: String?
)