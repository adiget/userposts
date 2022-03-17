package com.ags.annada.userposts.datasource.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.ags.annada.userposts.datasource.PostsDataSource
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.datasource.Result
import com.ags.annada.userposts.datasource.Result.Error
import com.ags.annada.userposts.datasource.Result.Success
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostsLocalDataSource internal constructor(
    private val postsDao: PostDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PostsDataSource {

    override fun observePosts(): LiveData<Result<List<Post>>> {
        return postsDao.observePosts().map {
            Success(it)
        }
    }

    override fun observePost(postId: Long): LiveData<Result<Post>> {
        return postsDao.observePostById(postId).map {
            Success(it)
        }
    }

    override suspend fun refreshPost(postId: Long) {
        // NO-OP
    }

    override suspend fun refreshPosts() {
        // NO-OP
    }

    override suspend fun getPosts(): Result<List<Post>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(postsDao.getPosts())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getPost(postId: Long): Result<Post> = withContext(ioDispatcher) {
        try {
            val post = postsDao.getPostById(postId)
            if (post != null) {
                return@withContext Success(post)
            } else {
                return@withContext Error(Exception("Post not found!"))
            }
        } catch (e: Exception) {
            return@withContext Error(e)
        }
    }

    override suspend fun savePost(post: Post) = withContext(ioDispatcher) {
        postsDao.insertPost(post)
    }

    override suspend fun deleteAllPosts() = withContext(ioDispatcher) {
        postsDao.deletePosts()
    }

    override suspend fun deletePost(postId: Long) = withContext<Unit>(ioDispatcher) {
        postsDao.deletePostById(postId)
    }
}
