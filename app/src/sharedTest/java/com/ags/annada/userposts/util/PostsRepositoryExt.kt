package com.ags.annada.userposts.util

import com.ags.annada.userposts.datasource.PostsRepository
import com.ags.annada.userposts.datasource.room.entities.Post
import kotlinx.coroutines.runBlocking

/**
 * A blocking version of PostsRepository.savePost to minimize the number of times we have to
 * explicitly add <code>runBlocking { ... }</code> in our tests
 */
fun PostsRepository.savePostBlocking(post: Post) = runBlocking {
    this@savePostBlocking.savePost(post)
}

fun PostsRepository.getPostsBlocking(forceUpdate: Boolean) = runBlocking {
    this@getPostsBlocking.getPosts(forceUpdate)
}

fun PostsRepository.deleteAllPostsBlocking() = runBlocking {
    this@deleteAllPostsBlocking.deleteAllPosts()
}
