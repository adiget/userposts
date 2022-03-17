package com.ags.annada.userposts.posts

import androidx.lifecycle.*
import com.ags.annada.userposts.R
import com.ags.annada.userposts.datasource.PostsRepository
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.datasource.Result
import com.ags.annada.userposts.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostsRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData(false)

    private val _items: LiveData<List<Post>> = _forceUpdate.switchMap { forceUpdate ->
        if (forceUpdate) {
            _dataLoading.value = true
            viewModelScope.launch {
                postRepository.refreshPosts()
                _dataLoading.value = false
            }
        }

        postRepository.observePosts().distinctUntilChanged().switchMap { buildPostsLiveData(it) }
    }

    val items: LiveData<List<Post>> = _items

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _openPostEvent = MutableLiveData<Event<Long>>()
    val openPostEvent: LiveData<Event<Long>> = _openPostEvent

    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    init {
        // Set initial state
        loadPosts(true)
    }

    /**
     * @param forceUpdate Pass in true to refresh the data in the [PostsDataSource]
     */
    fun loadPosts(forceUpdate: Boolean) {
        _forceUpdate.value = forceUpdate
    }

    private fun buildPostsLiveData(postsResult: Result<List<Post>>): LiveData<List<Post>> {
        val result = MutableLiveData<List<Post>>()

        if (postsResult is Result.Success) {
            isDataLoadingError.value = false
            result.value = postsResult.data!!
        } else {
            result.value = emptyList()
            showSnackbarMessage(R.string.loading_posts_error)
            isDataLoadingError.value = true
        }

        return result
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    fun refresh() {
        _forceUpdate.value = true
    }

    /**
     * Called by Data Binding.
     */
    fun openPost(postId: Long) {
        _openPostEvent.value = Event(postId)
    }
}