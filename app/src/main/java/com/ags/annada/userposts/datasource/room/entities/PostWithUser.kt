package com.ags.annada.userposts.datasource.room.entities

data class PostWithUser(
    val id: Long,
    val title: String,
    val body: String,
    val name: String,
    val username: String
)