package com.ags.annada.userposts.utils

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ags.annada.userposts.comments.CommentsListAdapter
import com.ags.annada.userposts.datasource.room.entities.Comment
import com.ags.annada.userposts.datasource.room.entities.Post
import com.ags.annada.userposts.posts.PostsAdapter

@BindingAdapter("adapter")
fun setAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.adapter = adapter
}

@BindingAdapter("mutableVisibility")
fun setMutableVisibility(view: View, visibility: MutableLiveData<Int>?) {
    val parentActivity: AppCompatActivity? = view.getParentActivity()
    if (parentActivity != null && visibility != null) {
        visibility.observe(
            parentActivity,
            Observer { value -> view.visibility = value ?: View.VISIBLE })
    }
}

@BindingAdapter("mutableText")
fun setMutableText(view: TextView, text: MutableLiveData<String>?) {
    val parentActivity: AppCompatActivity? = view.getParentActivity()
    if (parentActivity != null && text != null) {
        text.observe(parentActivity, Observer { value -> view.text = value ?: "" })
    }
}

/**
 * [BindingAdapter]s for the [Post]s list.
 */
@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<Post>?) {
    items?.let {
        (listView.adapter as PostsAdapter).submitList(items)
    }
}

@BindingAdapter("app:items")
fun setComments(listView: RecyclerView, items: List<Comment>?) {
    items?.let {
        (listView.adapter as CommentsListAdapter).submitList(items)
    }
}