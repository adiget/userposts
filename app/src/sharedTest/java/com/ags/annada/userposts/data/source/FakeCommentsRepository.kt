package com.ags.annada.userposts.data.source

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.ags.annada.userposts.datasource.CommentsRepository
import com.ags.annada.userposts.datasource.Result
import com.ags.annada.userposts.datasource.Result.Error
import com.ags.annada.userposts.datasource.Result.Success
import com.ags.annada.userposts.datasource.room.entities.Comment
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeCommentsRepository @Inject constructor() : CommentsRepository {

    var commentsServiceData: LinkedHashMap<Int, Comment> = LinkedHashMap()

    private var shouldReturnError = false

    private val observableComments = MutableLiveData<Result<List<Comment>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun refreshComments() {
        observableComments.value = getComments()
    }

    override suspend fun refreshCommentsByPostId(postId: Long) {
        observableComments.value = getComments()
    }

    override suspend fun refreshComment(commentId: Int) {
        refreshComments()
    }

    override fun observeComments(): LiveData<Result<List<Comment>>> {
        runBlocking { refreshComments() }
        return observableComments
    }

    override fun observeComment(commentId: Int): LiveData<Result<Comment>> {
        runBlocking { refreshComments() }
        return observableComments.map { comments ->
            when (comments) {
                is Result.Loading -> Result.Loading
                is Error -> Error(comments.exception)
                is Success -> {
                    val post = comments.data.firstOrNull() { it.id == commentId }
                        ?: return@map Error(Exception("Not found"))
                    Success(post)
                }
            }
        }
    }

    override suspend fun getComment(commentId: Int, forceUpdate: Boolean): Result<Comment> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        commentsServiceData[commentId]?.let {
            return Success(it)
        }
        return Error(Exception("Could not find comment"))
    }

    override suspend fun getComments(forceUpdate: Boolean): Result<List<Comment>> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        return Success(commentsServiceData.values.toList())
    }

    override suspend fun saveComment(comment: Comment) {
        commentsServiceData[comment.id] = comment
    }

    override suspend fun deleteComment(commentId: Int) {
        commentsServiceData.remove(commentId)
        refreshComments()
    }

    override suspend fun deleteAllComments() {
        commentsServiceData.clear()
        refreshComments()
    }

    @VisibleForTesting
    fun addComments(vararg comments: Comment) {
        for (comment in comments) {
            commentsServiceData[comment.id] = comment
        }
        runBlocking { refreshComments() }
    }
}
