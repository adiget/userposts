package com.ags.annada.userposts.datasource.room.entities

import androidx.room.Embedded
import com.ags.annada.userposts.datasource.room.entities.Geo

class Address(
    val street: String,

    val suite: String?,

    val city: String?,

    val zipcode: String?,

    @Embedded
    val geo: Geo?
)