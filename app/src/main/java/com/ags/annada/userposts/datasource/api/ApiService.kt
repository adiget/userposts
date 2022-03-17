package com.ags.annada.userposts.datasource.api

import com.ags.annada.userposts.datasource.room.entities.Comment
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.datasource.room.entities.User
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts")
    suspend fun getPost(@Query("postId") postId: Long): Post

    @GET("comments")
    suspend fun getComments(): List<Comment>

    @GET("comments")
    suspend fun getComment(@Query("commentId") commentId: Int): Comment

    @GET("comments")
    suspend fun getCommentsByPostId(@Query("postId") postId: Long): List<Comment>

    @GET("users")
    suspend fun getUsers(): List<User>
}