package com.ags.annada.userposts.datasource

import androidx.lifecycle.LiveData
import com.ags.annada.userposts.datasource.Result.Success
import com.ags.annada.userposts.datasource.room.entities.Comment
import com.ags.annada.userposts.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.*

/**
 * Default implementation of [CommentsRepository]. Single entry point for managing comments' data.
 */
class DefaultCommentsRepository(
    private val commentsRemoteDataSource: CommentsDataSource,
    private val commentsLocalDataSource: CommentsDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CommentsRepository {

    override suspend fun getComments(forceUpdate: Boolean): Result<List<Comment>> {
        // Set app as busy while this function executes.
        wrapEspressoIdlingResource {

            if (forceUpdate) {
                try {
                    updateCommentsFromRemoteDataSource()
                } catch (ex: Exception) {
                    return Result.Error(ex)
                }
            }
            return commentsLocalDataSource.getComments()
        }
    }

    override suspend fun refreshComments() {
        updateCommentsFromRemoteDataSource()
    }

    override suspend fun refreshCommentsByPostId(postId: Long) {
        updateCommentsFromRemoteDataSourceByPostId(postId)
    }

    override fun observeComments(): LiveData<Result<List<Comment>>> {
        return commentsLocalDataSource.observeComments()
    }

    override suspend fun refreshComment(commentId: Int) {
        updateCommentFromRemoteDataSource(commentId)
    }

    private suspend fun updateCommentsFromRemoteDataSource() {
        val remoteComments = commentsRemoteDataSource.getComments()

        if (remoteComments is Success) {
            // Real apps might want to do a proper sync, deleting, modifying or adding each comment.
            commentsLocalDataSource.deleteAllComments()
            remoteComments.data.forEach { comment ->
                commentsLocalDataSource.saveComment(comment)
            }
        } else if (remoteComments is Result.Error) {
            throw remoteComments.exception
        }
    }

    private suspend fun updateCommentsFromRemoteDataSourceByPostId(postId: Long) {
        val remoteComments = commentsRemoteDataSource.getCommentsByPostId(postId)

        if (remoteComments is Success) {
            // Real apps might want to do a proper sync, deleting, modifying or adding each comment.
            commentsLocalDataSource.deleteAllComments()
            remoteComments.data.forEach { comment ->
                commentsLocalDataSource.saveComment(comment)
            }
        } else if (remoteComments is Result.Error) {
            throw remoteComments.exception
        }
    }

    override fun observeComment(commentId: Int): LiveData<Result<Comment>> {
        return commentsLocalDataSource.observeComment(commentId)
    }

    private suspend fun updateCommentFromRemoteDataSource(commentId: Int) {
        val remoteComment = commentsRemoteDataSource.getComment(commentId)

        if (remoteComment is Success) {
            commentsLocalDataSource.saveComment(remoteComment.data)
        }
    }

    /**
     * Relies on [getComments] to fetch data and picks the comment with the same ID.
     */
    override suspend fun getComment(commentId: Int, forceUpdate: Boolean): Result<Comment> {
        // Set app as busy while this function executes.
        wrapEspressoIdlingResource {
            if (forceUpdate) {
                updateCommentFromRemoteDataSource(commentId)
            }
            return commentsLocalDataSource.getComment(commentId)
        }
    }

    override suspend fun saveComment(comment: Comment) {
        coroutineScope {
            launch { commentsRemoteDataSource.saveComment(comment) }
            launch { commentsLocalDataSource.saveComment(comment) }
        }
    }

    override suspend fun deleteAllComments() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { commentsRemoteDataSource.deleteAllComments() }
                launch { commentsLocalDataSource.deleteAllComments() }
            }
        }
    }

    override suspend fun deleteComment(commentId: Int) {
        coroutineScope {
            launch { commentsRemoteDataSource.deleteComment(commentId) }
            launch { commentsLocalDataSource.deleteComment(commentId) }
        }
    }

    private suspend fun getCommentWithId(id: Int): Result<Comment> {
        return commentsLocalDataSource.getComment(id)
    }
}
