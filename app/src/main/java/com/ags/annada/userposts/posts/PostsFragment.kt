package com.ags.annada.userposts.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ags.annada.userposts.databinding.PostsFragBinding
import com.ags.annada.userposts.utils.EventObserver
import com.ags.annada.userposts.utils.setupRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Display a list of [Post]s.
 */
@AndroidEntryPoint
class PostsFragment : Fragment() {

    private val viewModel by viewModels<PostsViewModel>()

    private lateinit var viewDataBinding: PostsFragBinding

    private lateinit var listAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = PostsFragBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setupListAdapter()
        setupRefreshLayout(viewDataBinding.refreshLayout, viewDataBinding.postsList)
        setupNavigation()
    }

    private fun setupNavigation() {
        viewModel.openPostEvent.observe(viewLifecycleOwner, EventObserver {
            openPostDetails(it)
        })
    }


    private fun openPostDetails(postId: Long) {
        val action = PostsFragmentDirections.actionPostsFragmentToCommentsFragment(postId)
        findNavController().navigate(action)
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = PostsAdapter(viewModel)
            viewDataBinding.postsList.adapter = listAdapter
        } else {
            Timber.w("ViewModel not initialized when attempting to set up adapter.")
        }
    }
}
