package main.app.views.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.HomeViewModel
import main.app.adapters.Adapter

class HomePage : Fragment() {

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: Adapter


    /**
     * Called when the view is created. Inflates the view with fragment_home_page
     * @return the inflated view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }


    /**
     * Sets upp the recicle viewer for the posts.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = Adapter(
            emptyList(),
            viewLifecycleOwner.lifecycleScope,
            childFragmentManager,
            onLikeToggle = { postId -> viewModel.toggleLike(postId) }
        )
        val recyclerView: RecyclerView = view.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                adapter.updateData(posts)
            }
        }

        childFragmentManager.setFragmentResultListener("post_detail_dismissed", viewLifecycleOwner) { _, _ ->
            viewModel.loadPosts()
        }

        viewModel.loadPosts()
    }
}