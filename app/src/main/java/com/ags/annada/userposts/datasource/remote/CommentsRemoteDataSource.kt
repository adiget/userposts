package com.ags.annada.userposts.datasource.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.ags.annada.userposts.datasource.CommentsDataSource
import com.ags.annada.userposts.datasource.Result
import com.ags.annada.userposts.datasource.Result.Error
import com.ags.annada.userposts.datasource.Result.Success
import com.ags.annada.userposts.datasource.api.ApiService
import com.ags.annada.userposts.datasource.room.entities.Comment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class CommentsRemoteDataSource internal constructor(
    private val remoteService: ApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CommentsDataSource {

    private val _observableComments = MutableLiveData<Result<List<Comment>>>()

    override suspend fun refreshComments() {
        _observableComments.value = getComments()!!
    }

    override suspend fun refreshComment(commentId: Int) {
        refreshComments()
    }

    override fun observeComments(): LiveData<Result<List<Comment>>> {
        return _observableComments
    }

    override fun observeComment(commentId: Int): LiveData<Result<Comment>> {
        return _observableComments.map { comments ->
            when (comments) {
                is Result.Loading -> Result.Loading
                is Error -> Error(comments.exception)
                is Success -> {
                    val comment = comments.data.firstOrNull() { it.id == commentId }
                        ?: return@map Error(Exception("Not found"))
                    Success(comment)
                }
            }
        }
    }

    override suspend fun getComments(): Result<List<Comment>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(remoteService.getComments())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getCommentsByPostId(postId: Long): Result<List<Comment>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(remoteService.getCommentsByPostId(postId))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getComment(commentId: Int): Result<Comment> {
        withContext(ioDispatcher) {
            remoteService.getComment(commentId).let { return@withContext Success(it) }
        }
        return Error(Exception("Comment not found"))
    }

    override suspend fun saveComment(comment: Comment) {
        /* No Op */
    }

    override suspend fun deleteAllComments() {
        /* No Op */
    }

    override suspend fun deleteComment(commentId: Int) {
        /* No Op */
    }
}
