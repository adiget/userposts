package com.ags.annada.userposts.datasource.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.ags.annada.userposts.datasource.PostsDataSource
import com.ags.annada.userposts.datasource.Result
import com.ags.annada.userposts.datasource.Result.Error
import com.ags.annada.userposts.datasource.Result.Success
import com.ags.annada.userposts.datasource.api.ApiService
import com.ags.annada.userposts.datasource.room.entities.Post
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostsRemoteDataSource internal constructor(
    private val remoteService: ApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PostsDataSource {

    private val _observablePosts = MutableLiveData<Result<List<Post>>>()

    override suspend fun refreshPosts() {
        _observablePosts.value = getPosts()!!
    }

    override suspend fun refreshPost(postId: Long) {
        refreshPosts()
    }

    override fun observePosts(): LiveData<Result<List<Post>>> {
        return _observablePosts
    }

    override fun observePost(postId: Long): LiveData<Result<Post>> {
        return _observablePosts.map { posts ->
            when (posts) {
                is Result.Loading -> Result.Loading
                is Error -> Error(posts.exception)
                is Success -> {
                    val post = posts.data.firstOrNull() { it.id == postId }
                        ?: return@map Error(Exception("Not found"))
                    Success(post)
                }
            }
        }
    }

    override suspend fun getPosts(): Result<List<Post>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(remoteService.getPosts())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getPost(postId: Long): Result<Post> {
        withContext(ioDispatcher) {
            remoteService.getPost(postId).let { return@withContext Success(it) }
        }

        return Error(Exception("Post not found"))
    }

    override suspend fun savePost(post: Post) {
        /* No Op */
    }

    override suspend fun deleteAllPosts() {
        /* No Op */
    }

    override suspend fun deletePost(postId: Long) {
        /* No Op */
    }
}
