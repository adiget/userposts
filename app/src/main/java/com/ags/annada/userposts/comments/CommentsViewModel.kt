package com.ags.annada.userposts.comments

import androidx.lifecycle.*
import com.ags.annada.userposts.R
import com.ags.annada.userposts.datasource.CommentsRepository
import com.ags.annada.userposts.datasource.Result
import com.ags.annada.userposts.datasource.room.entities.Comment
import com.ags.annada.userposts.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val commentsRepository: CommentsRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData(false)

    private val _items: LiveData<List<Comment>> = _forceUpdate.switchMap { forceUpdate ->
        if (forceUpdate) {
            _dataLoading.value = true
            viewModelScope.launch {
                commentsRepository.refreshCommentsByPostId(getSavedPostId())
                _dataLoading.value = false
            }
        }
        commentsRepository.observeComments().distinctUntilChanged()
            .switchMap { buildCommentsLiveData(it) }
    }

    val items: LiveData<List<Comment>> = _items

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val isDataLoadingError = MutableLiveData<Boolean>()

    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    init {
        loadComments(true)
    }

    fun loadComments(forceUpdate: Boolean) {
        _forceUpdate.value = forceUpdate
    }

    fun refresh() {
        _forceUpdate.value = true
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    private fun buildCommentsLiveData(commentsResult: Result<List<Comment>>): LiveData<List<Comment>> {
        val result = MutableLiveData<List<Comment>>()

        if (commentsResult is Result.Success) {
            isDataLoadingError.value = false
            result.value = commentsResult.data!!
        } else {
            result.value = emptyList()
            showSnackbarMessage(R.string.loading_comments_error)
            isDataLoadingError.value = true
        }

        return result
    }

    private fun getSavedPostId(): Long {
        return savedStateHandle.get(POST_ID_SAVED_STATE_KEY) ?: 1
    }
}

// Used to save the selected post in SavedStateHandle.
const val POST_ID_SAVED_STATE_KEY = "POST_ID_SAVED_STATE_KEY"
