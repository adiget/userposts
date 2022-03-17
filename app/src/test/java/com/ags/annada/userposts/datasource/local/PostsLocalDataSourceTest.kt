package com.ags.annada.userposts.datasource.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.ags.annada.userposts.MainCoroutineRule
import com.ags.annada.userposts.datasource.PostsDataSource
import com.ags.annada.userposts.datasource.room.PostDatabase
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.datasource.Result.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the [PostsDataSource].
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class PostsLocalDataSourceTest {

    private lateinit var localDataSource: PostsLocalDataSource
    private lateinit var database: PostDatabase

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PostDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource = PostsLocalDataSource(database.postDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun savePost_retrievesPost() = runBlockingTest {
        // GIVEN - a new post saved in the database
        val newPost = Post(1L, 1, "Title", "Body")
        localDataSource.savePost(newPost)

        // WHEN  - Task retrieved by ID
        val result = newPost.id?.let { localDataSource.getPost(it) }

        // THEN - Same task is returned
        result as Success
        assertThat(result.data.title, `is`("Title"))
        assertThat(result.data.body, `is`("Body"))
    }

    @Test
    fun deleteAllPosts_emptyListOfRetrievedPost() = runBlockingTest {
        // Given a new post in the persistent repository and a mocked callback
        val newPost = Post(1L, 1, "Title", "Body")

        localDataSource.savePost(newPost)

        // When all posts are deleted
        localDataSource.deleteAllPosts()

        // Then the retrieved posts is an empty list
        val result = localDataSource.getPosts() as Success
        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun getPosts_retrieveSavedPosts() = runBlockingTest {
        // Given 2 new posts in the persistent repository
        val newPost1 = Post(1L, 1, "Title1", "Body1")
        val newPost2 = Post(2L, 1, "Title2", "Body2")

        localDataSource.savePost(newPost1)
        localDataSource.savePost(newPost2)
        // Then the posts can be retrieved from the persistent repository
        val results = localDataSource.getPosts() as Success<List<Post>>
        val posts = results.data
        assertThat(posts.size, `is`(2))
    }
}
