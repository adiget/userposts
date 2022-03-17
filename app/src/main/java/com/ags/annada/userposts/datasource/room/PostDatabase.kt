package com.ags.annada.userposts.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ags.annada.userposts.datasource.local.CommentDao
import com.ags.annada.userposts.datasource.local.PostDao
import com.ags.annada.userposts.datasource.room.entities.Comment
import com.ags.annada.userposts.datasource.room.entities.Post

@Database(entities = [Post::class, Comment::class], version = 1)
@TypeConverters(RoomTypeConverters::class)
abstract class PostDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
}