package com.ags.annada.userposts.datasource

import androidx.lifecycle.LiveData
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.datasource.Result.Error
import com.ags.annada.userposts.datasource.Result.Success

class FakeDataSource(var posts: MutableList<Post>? = mutableListOf()) : PostsDataSource {
    override suspend fun getPosts(): Result<List<Post>> {
        posts?.let { return Success(ArrayList(it)) }
        return Error(
            Exception("Posts not found")
        )
    }

    override suspend fun getPost(postId: Long): Result<Post> {
        posts?.firstOrNull { it.id == postId }?.let { return Success(it) }
        return Error(
            Exception("Post not found")
        )
    }

    override suspend fun savePost(post: Post) {
        posts?.add(post)
    }

    override suspend fun deleteAllPosts() {
        posts?.clear()
    }

    override suspend fun deletePost(postId: Long) {
        posts?.removeIf { it.id == postId }
    }

    override fun observePosts(): LiveData<Result<List<Post>>> {
        TODO("not implemented")
    }

    override suspend fun refreshPosts() {
        TODO("not implemented")
    }

    override fun observePost(postId: Long): LiveData<Result<Post>> {
        TODO("not implemented")
    }

    override suspend fun refreshPost(postId: Long) {
        TODO("not implemented")
    }
}
