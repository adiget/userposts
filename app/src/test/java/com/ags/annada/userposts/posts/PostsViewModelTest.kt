package com.ags.annada.userposts.posts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ags.annada.userposts.*
import com.ags.annada.userposts.data.source.FakePostsRepository
import com.ags.annada.userposts.datasource.room.entities.Post
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [PostsViewModel]
 */
@ExperimentalCoroutinesApi
class PostsViewModelTest {

    // Subject under test
    private lateinit var postsViewModel: PostsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var postsPostsRepository: FakePostsRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the posts to 3
        postsPostsRepository = FakePostsRepository()
        val post1 = Post(1L, 1, "Title1", "Body1")
        val post2 = Post(2L, 1, "Title2", "Body2")
        val post3 = Post(3L, 1, "Title3", "Body3")
        postsPostsRepository.addPosts(post1, post2, post3)

        postsViewModel = PostsViewModel(postsPostsRepository)
    }

    @Test
    fun loadAllPostsFromRepository_loadingTogglesAndDataLoaded() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Trigger loading of posts
        postsViewModel.loadPosts(true)
        // Observe the items to keep LiveData emitting
        postsViewModel.items.observeForTesting {

            // Then progress indicator is shown
            assertThat(postsViewModel.dataLoading.getOrAwaitValue()).isTrue()

            // Execute pending coroutines actions
            mainCoroutineRule.resumeDispatcher()

            // Then progress indicator is hidden
            assertThat(postsViewModel.dataLoading.getOrAwaitValue()).isFalse()

            // And data correctly loaded
            assertThat(postsViewModel.items.getOrAwaitValue()).hasSize(3)
        }
    }

    @Test
    fun loadPosts_error() {
        // Make the repository return errors
        postsPostsRepository.setReturnError(true)

        // Load posts
        postsViewModel.loadPosts(true)
        // Observe the items to keep LiveData emitting
        postsViewModel.items.observeForTesting {

            // Then progress indicator is hidden
            assertThat(postsViewModel.dataLoading.getOrAwaitValue()).isFalse()

            // And the list of items is empty
            assertThat(postsViewModel.items.getOrAwaitValue()).isEmpty()

            // And the snackbar updated
            assertSnackbarMessage(postsViewModel.snackbarText, R.string.loading_posts_error)
        }
    }

    @Test
    fun clickOnOpenPost_setsEvent() {
        // When opening a post
        val postId = 1L
        postsViewModel.openPost(postId)

        // Then the event is triggered
        assertLiveDataEventTriggered(postsViewModel.openPostEvent, postId)
    }
}
