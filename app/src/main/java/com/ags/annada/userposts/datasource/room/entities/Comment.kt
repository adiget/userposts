package com.ags.annada.userposts.datasource.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ags.annada.userposts.datasource.room.entities.Post

@Entity(tableName = "comment_table", foreignKeys = [
    ForeignKey(entity = Post::class, parentColumns = ["id"], childColumns = ["postId"], onDelete = ForeignKey.CASCADE)])
data class Comment(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "body") var body: String,
    @ColumnInfo(name = "email") var email: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "postId") var postId: Int
) {
    constructor() : this(0, "", "", "", 0)
}