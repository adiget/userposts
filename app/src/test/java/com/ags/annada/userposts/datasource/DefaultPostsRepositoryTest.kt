package com.ags.annada.userposts.datasource

import com.ags.annada.userposts.MainCoroutineRule
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.datasource.Result.Success
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultPostsRepositoryTest {

    private val post1 = Post(1L, 1, "Title1", "Body1")
    private val post2 = Post(2L, 1, "Title2", "Body2")
    private val post3 = Post(3L, 1, "Title3", "Body3")
    private val newPost = Post(4L, 1, "Title new", "Body new")
    private val remotePosts = listOf(post1, post2).sortedBy { it.id }
    private val localPosts = listOf(post3).sortedBy { it.id }
    private val newPosts = listOf(post3).sortedBy { it.id }
    private lateinit var postsRemoteDataSource: FakeDataSource
    private lateinit var postsLocalDataSource: FakeDataSource

    // Class under test
    private lateinit var postsRepository: DefaultPostsRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        postsRemoteDataSource = FakeDataSource(remotePosts.toMutableList())
        postsLocalDataSource = FakeDataSource(localPosts.toMutableList())
        // Get a reference to the class under test
        postsRepository = DefaultPostsRepository(
            postsRemoteDataSource, postsLocalDataSource, Dispatchers.Main
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getPosts_emptyRepositoryAndUninitializedCache() = mainCoroutineRule.runBlockingTest {
        val emptySource = FakeDataSource()
        val postsRepository = DefaultPostsRepository(
            emptySource, emptySource, Dispatchers.Main
        )

        assertThat(postsRepository.getPosts() is Success).isTrue()
    }

    @Test
    fun getPosts_repositoryCachesAfterFirstApiCall() = mainCoroutineRule.runBlockingTest {
        // Trigger the repository to load data, which loads from remote and caches
        val initial = postsRepository.getPosts()

        postsRemoteDataSource.posts = newPosts.toMutableList()

        val second = postsRepository.getPosts()

        // Initial and second should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getPosts_requestsAllPostsFromRemoteDataSource() = mainCoroutineRule.runBlockingTest {
        // When posts are requested from the posts repository
        val posts = postsRepository.getPosts(true) as Success

        // Then posts are loaded from the remote data source
        assertThat(posts.data).isEqualTo(remotePosts)
    }

    @Test
    fun savePost_savesToLocalAndRemote() = mainCoroutineRule.runBlockingTest {
        // Make sure newPost is not in the remote or local datasources
        assertThat(postsRemoteDataSource.posts).doesNotContain(newPost)
        assertThat(postsLocalDataSource.posts).doesNotContain(newPost)

        // When a post is saved to the posts repository
        postsRepository.savePost(newPost)

        // Then the remote and local sources are called
        assertThat(postsRemoteDataSource.posts).contains(newPost)
        assertThat(postsLocalDataSource.posts).contains(newPost)
    }

    @Test
    fun getPosts_WithDirtyCache_postsAreRetrievedFromRemote() = mainCoroutineRule.runBlockingTest {
        // First call returns from REMOTE
        val posts = postsRepository.getPosts()

        // Set a different list of posts in REMOTE
        postsRemoteDataSource.posts = newPosts.toMutableList()

        // But if posts are cached, subsequent calls load from cache
        val cachedPosts = postsRepository.getPosts()
        assertThat(cachedPosts).isEqualTo(posts)

        // Now force remote loading
        val refreshedPosts = postsRepository.getPosts(true) as Success

        // Posts must be the recently updated in REMOTE
        assertThat(refreshedPosts.data).isEqualTo(newPosts)
    }

    @Test
    fun getPosts_WithDirtyCache_remoteUnavailable_error() = mainCoroutineRule.runBlockingTest {
        // Make remote data source unavailable
        postsRemoteDataSource.posts = null

        // Load posts forcing remote load
        val refreshedPosts = postsRepository.getPosts(true)

        // Result should be an error
        assertThat(refreshedPosts).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getPosts_WithRemoteDataSourceUnavailable_postsAreRetrievedFromLocal() =
        mainCoroutineRule.runBlockingTest {
            // When the remote data source is unavailable
            postsRemoteDataSource.posts = null

            // The repository fetches from the local source
            assertThat((postsRepository.getPosts() as Success).data).isEqualTo(localPosts)
        }

    @Test
    fun getPosts_WithBothDataSourcesUnavailable_returnsError() = mainCoroutineRule.runBlockingTest {
        // When both sources are unavailable
        postsRemoteDataSource.posts = null
        postsLocalDataSource.posts = null

        // The repository returns an error
        assertThat(postsRepository.getPosts()).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getPosts_refreshesLocalDataSource() = mainCoroutineRule.runBlockingTest {
        val initialLocal = postsLocalDataSource.posts

        // First load will fetch from remote
        val newPosts = (postsRepository.getPosts(true) as Success).data

        assertThat(newPosts).isEqualTo(remotePosts)
        assertThat(newPosts).isEqualTo(postsLocalDataSource.posts)
        assertThat(postsLocalDataSource.posts).isEqualTo(initialLocal)
    }

    @Test
    fun getPost_repositoryCachesAfterFirstApiCall() = mainCoroutineRule.runBlockingTest {
        // Trigger the repository to load data, which loads from remote
        postsRemoteDataSource.posts = mutableListOf(post1)
        post1.id?.let { postsRepository.getPost(it, true) }

        // Configure the remote data source to store a different post
        postsRemoteDataSource.posts = mutableListOf(post2)

        val post1SecondTime = post1.id?.let { postsRepository.getPost(it, true) } as Success
        val post2SecondTime = post2.id?.let { postsRepository.getPost(it, true) } as Success

        // Both work because one is in remote and the other in cache
        assertThat(post1SecondTime.data.id).isEqualTo(post1.id)
        assertThat(post2SecondTime.data.id).isEqualTo(post2.id)
    }

    @Test
    fun getPost_forceRefresh() = mainCoroutineRule.runBlockingTest {
        // Trigger the repository to load data, which loads from remote and caches
        postsRemoteDataSource.posts = mutableListOf(post1)
        post1.id?.let { postsRepository.getPost(it) }

        // Configure the remote data source to return a different post
        postsRemoteDataSource.posts = mutableListOf(post2)

        // Force refresh
        val post1SecondTime = post1.id?.let { postsRepository.getPost(it, true) }
        val post2SecondTime = post2.id?.let { postsRepository.getPost(it, true) }

        // Only post2 works because the cache and local were invalidated
        assertThat((post1SecondTime as? Success)?.data?.id).isNull()
        assertThat((post2SecondTime as? Success)?.data?.id).isEqualTo(post2.id)
    }

    @Test
    fun deleteAllPosts() = mainCoroutineRule.runBlockingTest {
        val initialPosts = (postsRepository.getPosts() as? Success)?.data

        // Delete all posts
        postsRepository.deleteAllPosts()

        // Fetch data again
        val afterDeletePosts = (postsRepository.getPosts() as? Success)?.data

        // Verify posts are empty now
        assertThat(initialPosts).isNotEmpty()
        assertThat(afterDeletePosts).isEmpty()
    }

    @Test
    fun deleteSinglePost() = mainCoroutineRule.runBlockingTest {
        val initialPosts = (postsRepository.getPosts(true) as? Success)?.data

        // Delete first post
        post1.id?.let { postsRepository.deletePost(it) }

        // Fetch data again
        val afterDeletePosts = (postsRepository.getPosts(true) as? Success)?.data

        // Verify only one post was deleted
        assertThat(afterDeletePosts?.size).isEqualTo(initialPosts!!.size - 1)
        assertThat(afterDeletePosts).doesNotContain(post1)
    }
}
