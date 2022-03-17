package com.ags.annada.userposts.datasource.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.datasource.room.entities.PostWithUser


@Dao
interface PostDao {
    @Query("SELECT * FROM post_table")
    fun observePosts(): LiveData<List<Post>>

    /**
     * Observes a single post.
     *
     * @param postId the post id.
     * @return the post with postId.
     */
    @Query("SELECT * FROM post_table WHERE id = :postId")
    fun observePostById(postId: Long): LiveData<Post>

    /**
     * Select all posts from the post table.
     *
     * @return all posts.
     */
    @Query("SELECT * FROM post_table")
    suspend fun getPosts(): List<Post>

    /**
     * Select a post by id.
     *
     * @param postId the post id.
     * @return the post with postId.
     */
    @Query("SELECT * FROM post_table WHERE id = :postId")
    suspend fun getPostById(postId: Long): Post?

    /**
     * Insert a post in the database. If the post already exists, replace it.
     *
     * @param post the post to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    /**
     * Delete a post by id.
     *
     * @return the number of posts deleted. This should always be 1.
     */
    @Query("DELETE FROM post_table WHERE id = :postId")
    suspend fun deletePostById(postId: Long): Int

    /**
     * Delete all posts.
     */
    @Query("DELETE FROM post_table")
    suspend fun deletePosts()
}