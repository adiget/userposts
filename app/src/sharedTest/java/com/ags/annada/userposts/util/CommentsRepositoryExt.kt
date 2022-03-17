package com.ags.annada.userposts.util

import com.ags.annada.userposts.datasource.CommentsRepository
import com.ags.annada.userposts.datasource.room.entities.Comment
import kotlinx.coroutines.runBlocking

/**
 * A blocking version of CommentsRepository.saveComment to minimize the number of times we have to
 * explicitly add <code>runBlocking { ... }</code> in our tests
 */
fun CommentsRepository.saveCommentBlocking(comment: Comment) = runBlocking {
    this@saveCommentBlocking.saveComment(comment)
}

fun CommentsRepository.getCommentsBlocking(forceUpdate: Boolean) = runBlocking {
    this@getCommentsBlocking.getComments(forceUpdate)
}

fun CommentsRepository.deleteAllCommentsBlocking() = runBlocking {
    this@deleteAllCommentsBlocking.deleteAllComments()
}
