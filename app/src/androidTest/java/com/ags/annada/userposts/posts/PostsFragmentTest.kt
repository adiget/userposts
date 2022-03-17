package com.ags.annada.userposts.posts

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.ags.annada.userposts.R
import com.ags.annada.userposts.datasource.PostsRepository
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.ui.MainActivity
import com.ags.annada.userposts.util.savePostBlocking
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration test for the Post List screen.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
@HiltAndroidTest
class PostsFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: PostsRepository

    @Before
    fun init() {
        // Populate @Inject fields in test class
        hiltRule.inject()
    }

    @Test
    fun displayPost_whenRepositoryHasData() {
        // GIVEN - One post already in the repository
        repository.savePostBlocking(Post(1L, 1, "TITLE1", "BODY1"))

        // WHEN - On startup
        launchActivity()

        // THEN - Verify task is displayed on screen
        onView(withText("TITLE1")).check(matches(isDisplayed()))
    }

    @Test
    fun showAllPosts() {
        repository.savePostBlocking(Post(1L, 1, "TITLE1", "BODY1"))
        repository.savePostBlocking(Post(2L, 1, "TITLE2", "BODY2"))

        launchActivity()

        // Verify that both of our posts are shown
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("TITLE2")).check(matches(isDisplayed()))
    }

    @Test
    fun noPosts_NoPostViewVisible() {
        launchActivity()

        // Verify the "You have no posts!" text is shown
        onView(withText("You have no posts!")).check(matches(isDisplayed()))
    }

    private fun launchActivity(): ActivityScenario<MainActivity>? {
        val activityScenario = launch(MainActivity::class.java)
        activityScenario.onActivity { activity ->
            // Disable animations in RecyclerView
            (activity.findViewById(R.id.posts_list) as RecyclerView).itemAnimator = null
        }
        return activityScenario
    }
}
