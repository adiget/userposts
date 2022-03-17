package com.ags.annada.userposts.datasource.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.ags.annada.userposts.datasource.CommentsDataSource
import com.ags.annada.userposts.datasource.Result
import com.ags.annada.userposts.datasource.Result.Error
import com.ags.annada.userposts.datasource.Result.Success
import com.ags.annada.userposts.datasource.room.entities.Comment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentsLocalDataSource internal constructor(
    private val commentsDao: CommentDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CommentsDataSource {

    override fun observeComments(): LiveData<Result<List<Comment>>> {
        return commentsDao.observeComments().map {
            Success(it)
        }
    }

    override fun observeComment(commentId: Int): LiveData<Result<Comment>> {
        return commentsDao.observeCommentById(commentId).map {
            Success(it)
        }
    }

    override suspend fun refreshComment(commentId: Int) {
        // NO-OP
    }

    override suspend fun refreshComments() {
        // NO-OP
    }

    override suspend fun getComments(): Result<List<Comment>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(commentsDao.getComments())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getCommentsByPostId(postId: Long): Result<List<Comment>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(commentsDao.getCommentsWithPostId(postId))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getComment(commentId: Int): Result<Comment> = withContext(ioDispatcher) {
        try {
            val comment = commentsDao.getCommentById(commentId)
            if (comment != null) {
                return@withContext Result.Success(comment)
            } else {
                return@withContext Error(Exception("Comment not found!"))
            }
        } catch (e: Exception) {
            return@withContext Error(e)
        }
    }

    override suspend fun saveComment(comment: Comment) = withContext(ioDispatcher) {
        commentsDao.insertComment(comment)
    }

    override suspend fun deleteAllComments() = withContext(ioDispatcher) {
        commentsDao.deleteComments()
    }

    override suspend fun deleteComment(commentId: Int) = withContext<Unit>(ioDispatcher) {
        commentsDao.deleteCommentById(commentId)
    }
}
