package com.ags.annada.userposts.comments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.ags.annada.userposts.*
import com.ags.annada.userposts.data.source.FakeCommentsRepository
import com.ags.annada.userposts.datasource.room.entities.Comment
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [CommentsViewModel]
 */
@ExperimentalCoroutinesApi
class CommentsViewModelTest {

    // Subject under test
    private lateinit var commentsViewModel: CommentsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var commentsPostsRepository: FakeCommentsRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the comments to 3
        commentsPostsRepository = FakeCommentsRepository()
        val comment1 = Comment(1, "Body1", "Email1", "Name1", 1)
        val comment2 = Comment(2, "Body2", "Email2", "Name2", 1)
        val comment3 = Comment(3, "Body3", "Email3", "Name3", 1)
        commentsPostsRepository.addComments(comment1, comment2, comment3)

        commentsViewModel = CommentsViewModel(SavedStateHandle(), commentsPostsRepository)
    }

    @Test
    fun loadAllCommentsFromRepository_loadingTogglesAndDataLoaded() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Trigger loading of comments
        commentsViewModel.loadComments(true)
        // Observe the items to keep LiveData emitting
        commentsViewModel.items.observeForTesting {

            // Then progress indicator is shown
            assertThat(commentsViewModel.dataLoading.getOrAwaitValue()).isTrue()

            // Execute pending coroutines actions
            mainCoroutineRule.resumeDispatcher()

            // Then progress indicator is hidden
            assertThat(commentsViewModel.dataLoading.getOrAwaitValue()).isFalse()

            // And data correctly loaded
            assertThat(commentsViewModel.items.getOrAwaitValue()).hasSize(3)
        }
    }

    @Test
    fun loadComments_error() {
        // Make the repository return errors
        commentsPostsRepository.setReturnError(true)

        // Load comments
        commentsViewModel.loadComments(true)
        // Observe the items to keep LiveData emitting
        commentsViewModel.items.observeForTesting {

            // Then progress indicator is hidden
            assertThat(commentsViewModel.dataLoading.getOrAwaitValue()).isFalse()

            // And the list of items is empty
            assertThat(commentsViewModel.items.getOrAwaitValue()).isEmpty()

            // And the snackbar updated
            assertSnackbarMessage(commentsViewModel.snackbarText, R.string.loading_comments_error)
        }
    }
}
