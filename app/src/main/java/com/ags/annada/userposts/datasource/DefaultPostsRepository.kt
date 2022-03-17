package com.ags.annada.userposts.datasource

import androidx.lifecycle.LiveData
import com.ags.annada.userposts.datasource.Result.Success
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Default implementation of [PostsRepository]. Single entry point for managing posts' data.
 */
class DefaultPostsRepository(
    private val postsRemoteDataSource: PostsDataSource,
    private val postsLocalDataSource: PostsDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PostsRepository {

    override suspend fun getPosts(forceUpdate: Boolean): Result<List<Post>> {
        // Set app as busy while this function executes.
        wrapEspressoIdlingResource {

            if (forceUpdate) {
                try {
                    updatePostsFromRemoteDataSource()
                } catch (ex: Exception) {
                    return Result.Error(ex)
                }
            }
            return postsLocalDataSource.getPosts()
        }
    }

    override suspend fun refreshPosts() {
        updatePostsFromRemoteDataSource()
    }

    override fun observePosts(): LiveData<Result<List<Post>>> {
        return postsLocalDataSource.observePosts()
    }

    override suspend fun refreshPost(postId: Long) {
        updatePostFromRemoteDataSource(postId)
    }

    private suspend fun updatePostsFromRemoteDataSource() {
        val remotePosts = postsRemoteDataSource.getPosts()

        if (remotePosts is Success) {
            // Real apps might want to do a proper sync, deleting, modifying or adding each post.
            postsLocalDataSource.deleteAllPosts()
            remotePosts.data.forEach { post ->
                postsLocalDataSource.savePost(post)
            }
        } else if (remotePosts is Result.Error) {
            throw remotePosts.exception
        }
    }

    override fun observePost(postId: Long): LiveData<Result<Post>> {
        return postsLocalDataSource.observePost(postId)
    }

    private suspend fun updatePostFromRemoteDataSource(postId: Long) {
        val remotePost = postsRemoteDataSource.getPost(postId)

        if (remotePost is Success) {
            postsLocalDataSource.savePost(remotePost.data)
        }
    }

    /**
     * Relies on [getPosts] to fetch data and picks the post with the same ID.
     */
    override suspend fun getPost(postId: Long, forceUpdate: Boolean): Result<Post> {
        // Set app as busy while this function executes.
        wrapEspressoIdlingResource {
            if (forceUpdate) {
                updatePostFromRemoteDataSource(postId)
            }
            return postsLocalDataSource.getPost(postId)
        }
    }

    override suspend fun savePost(post: Post) {
        coroutineScope {
            launch { postsRemoteDataSource.savePost(post) }
            launch { postsLocalDataSource.savePost(post) }
        }
    }

    override suspend fun deleteAllPosts() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { postsRemoteDataSource.deleteAllPosts() }
                launch { postsLocalDataSource.deleteAllPosts() }
            }
        }
    }

    override suspend fun deletePost(postId: Long) {
        coroutineScope {
            launch { postsRemoteDataSource.deletePost(postId) }
            launch { postsLocalDataSource.deletePost(postId) }
        }
    }

    private suspend fun getPostWithId(id: Long): Result<Post> {
        return postsLocalDataSource.getPost(id)
    }
}
