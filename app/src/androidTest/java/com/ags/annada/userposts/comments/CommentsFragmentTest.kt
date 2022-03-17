package com.ags.annada.userposts.comments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.ags.annada.userposts.R
import com.ags.annada.userposts.datasource.CommentsRepository
import com.ags.annada.userposts.datasource.room.entities.Comment
import com.ags.annada.userposts.launchFragmentInHiltContainer
import com.ags.annada.userposts.util.saveCommentBlocking
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration test for the Comments List screen.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
class CommentsFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: CommentsRepository

    @Before
    fun init() {
        // Populate @Inject fields in test class
        hiltRule.inject()
    }

    @Test
    fun displayComments_whenRepositoryHasData() {
        // GIVEN - Add comment to the DB
        val comment = Comment(1, "body1", "email1", "name1", 1)
        repository.saveCommentBlocking(comment)

        // WHEN - Comments fragment launched to display comments
        val bundle = CommentsFragmentArgs(1).toBundle()
        launchFragmentInHiltContainer<CommentsFragment>(bundle, R.style.AppTheme)

        // THEN - Verify comment is displayed on screen
        // make sure that the body/email/name are shown and correct
        onView(withText("body1")).check(matches(isDisplayed()))
        onView(withText("email1")).check(matches(isDisplayed()))
        onView(withText("name1")).check(matches(isDisplayed()))
    }

    @Test
    fun showAllComments() {
        repository.saveCommentBlocking(Comment(1, "body1", "email1", "name1", 1))
        repository.saveCommentBlocking(Comment(2, "body2", "email2", "name2", 1))

        // WHEN - Comments fragment launched to display comments
        val bundle = CommentsFragmentArgs(1).toBundle()
        launchFragmentInHiltContainer<CommentsFragment>(bundle, R.style.AppTheme)

        // Verify that both of our comments are shown
        onView(withText("name1")).check(matches(isDisplayed()))
        onView(withText("name2")).check(matches(isDisplayed()))
    }

    @Test
    fun noComments_NoCommentViewVisible() {
        // WHEN - Comments fragment launched to display comments
        val bundle = CommentsFragmentArgs(1).toBundle()
        launchFragmentInHiltContainer<CommentsFragment>(bundle, R.style.AppTheme)

        // Verify the "You have no comments!" text is shown
        onView(withText("You have no comments!")).check(matches(isDisplayed()))
    }
}
