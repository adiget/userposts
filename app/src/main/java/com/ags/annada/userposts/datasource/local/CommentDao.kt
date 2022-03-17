package com.ags.annada.userposts.datasource.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ags.annada.userposts.datasource.room.entities.Comment
import com.ags.annada.userposts.datasource.room.entities.Post

@Dao
interface CommentDao {
    @Query("SELECT * FROM comment_table")
    fun observeComments(): LiveData<List<Comment>>

    /**
     * Observes a single comment.
     *
     * @param commentId the comment id.
     * @return the comment with commentId.
     */
    @Query("SELECT * FROM comment_table WHERE id = :commentId")
    fun observeCommentById(commentId: Int): LiveData<Comment>

    /**
     * Select all comments from the comment table.
     *
     * @return all comments.
     */
    @Query("SELECT * FROM comment_table")
    suspend fun getComments(): List<Comment>

    /**
     * Select comments from the comment table with postId.
     *
     * @return all comments with postId.
     */
    @Query("SELECT * FROM comment_table WHERE postId = :postId")
    suspend fun getCommentsWithPostId(postId: Long): List<Comment>


    /**
     * Select a comment by id.
     *
     * @param commentId the comment id.
     * @return the comment with commentId.
     */
    @Query("SELECT * FROM comment_table WHERE id = :commentId")
    suspend fun getCommentById(commentId: Int): Comment?

    /**
     * Insert a comment in the database. If the comment already exists, replace it.
     *
     * @param comment the comment to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    /**
     * Delete a comment by id.
     *
     * @return the number of comments deleted. This should always be 1.
     */
    @Query("DELETE FROM comment_table WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: Int): Int

    /**
     * Delete all comments.
     */
    @Query("DELETE FROM comment_table")
    suspend fun deleteComments()


    @get:Query("SELECT * from comment_table")
    val all: LiveData<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: Comment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg comment: Comment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertComments(vararg comment: Comment)

    @Query("SELECT * from comment_table WHERE postId = :key")
    fun getCommentWithId(key: Long): LiveData<List<Comment>>

    @Query("DELETE from comment_table")
    suspend fun deleteAll()
}