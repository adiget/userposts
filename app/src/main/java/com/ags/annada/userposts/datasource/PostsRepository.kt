package com.ags.annada.userposts.datasource

import androidx.lifecycle.LiveData
import com.ags.annada.userposts.datasource.room.entities.Post

/**
 * Interface to the data layer.
 */
interface PostsRepository {

    fun observePosts(): LiveData<Result<List<Post>>>

    suspend fun getPosts(forceUpdate: Boolean = false): Result<List<Post>>

    suspend fun refreshPosts()

    fun observePost(postId: Long): LiveData<Result<Post>>

    suspend fun getPost(postId: Long, forceUpdate: Boolean = false): Result<Post>

    suspend fun refreshPost(postId: Long)

    suspend fun savePost(post: Post)

    suspend fun deleteAllPosts()

    suspend fun deletePost(postId: Long)
}
