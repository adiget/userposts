package com.ags.annada.userposts.data.source

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.ags.annada.userposts.datasource.PostsRepository
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.datasource.Result
import com.ags.annada.userposts.datasource.Result.Error
import com.ags.annada.userposts.datasource.Result.Success
import kotlinx.coroutines.runBlocking
import java.util.LinkedHashMap
import javax.inject.Inject

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakePostsRepository @Inject constructor() : PostsRepository {

    var postsServiceData: LinkedHashMap<Long?, Post> = LinkedHashMap()

    private var shouldReturnError = false

    private val observablePosts = MutableLiveData<Result<List<Post>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun refreshPosts() {
        observablePosts.value = getPosts()
    }

    override suspend fun refreshPost(postId: Long) {
        refreshPosts()
    }

    override fun observePosts(): LiveData<Result<List<Post>>> {
        runBlocking { refreshPosts() }
        return observablePosts
    }

    override fun observePost(postId: Long): LiveData<Result<Post>> {
        runBlocking { refreshPosts() }
        return observablePosts.map { posts ->
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

    override suspend fun getPost(postId: Long, forceUpdate: Boolean): Result<Post> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        postsServiceData[postId]?.let {
            return Success(it)
        }
        return Error(Exception("Could not find post"))
    }

    override suspend fun getPosts(forceUpdate: Boolean): Result<List<Post>> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        return Success(postsServiceData.values.toList())
    }

    override suspend fun savePost(post: Post) {
        postsServiceData[post.id] = post
    }

    override suspend fun deletePost(postId: Long) {
        postsServiceData.remove(postId)
        refreshPosts()
    }

    override suspend fun deleteAllPosts() {
        postsServiceData.clear()
        refreshPosts()
    }

    @VisibleForTesting
    fun addPosts(vararg posts: Post) {
        for (post in posts) {
            postsServiceData[post.id] = post
        }
        runBlocking { refreshPosts() }
    }
}
