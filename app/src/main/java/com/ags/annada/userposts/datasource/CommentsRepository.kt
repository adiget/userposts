package com.ags.annada.userposts.datasource

import androidx.lifecycle.LiveData
import com.ags.annada.userposts.datasource.room.entities.Comment

/**
 * Interface to the data layer.
 */
interface CommentsRepository {

    fun observeComments(): LiveData<Result<List<Comment>>>

    suspend fun getComments(forceUpdate: Boolean = false): Result<List<Comment>>

    suspend fun refreshComments()

    suspend fun refreshCommentsByPostId(postId: Long)

    fun observeComment(commentId: Int): LiveData<Result<Comment>>

    suspend fun getComment(commentId: Int, forceUpdate: Boolean = false): Result<Comment>

    suspend fun refreshComment(commentId: Int)

    suspend fun saveComment(comment: Comment)

    suspend fun deleteAllComments()

    suspend fun deleteComment(commentId: Int)
}
