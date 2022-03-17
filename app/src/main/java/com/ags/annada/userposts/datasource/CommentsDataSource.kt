package com.ags.annada.userposts.datasource

import androidx.lifecycle.LiveData
import com.ags.annada.userposts.datasource.room.entities.Comment

interface CommentsDataSource {

    fun observeComments(): LiveData<Result<List<Comment>>>

    suspend fun getComments(): Result<List<Comment>>

    suspend fun getCommentsByPostId(postId: Long): Result<List<Comment>>

    suspend fun refreshComments()

    fun observeComment(commentId: Int): LiveData<Result<Comment>>

    suspend fun getComment(commentId: Int): Result<Comment>

    suspend fun refreshComment(commentId: Int)

    suspend fun saveComment(comment: Comment)

    suspend fun deleteAllComments()

    suspend fun deleteComment(commentId: Int)
}
