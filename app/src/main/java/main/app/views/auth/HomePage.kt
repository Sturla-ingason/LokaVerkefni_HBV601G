package main.app.views.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.adapters.Adapter
import main.app.repository.PostRepository

class HomePage : Fragment() {

    private val postRepository = PostRepository()
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

        adapter = Adapter(emptyList(), viewLifecycleOwner.lifecycleScope, childFragmentManager)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        fetchPosts()
    }


    /**
     *  Fetches all the posts that are supposed to be on the users homepage.
     */
    private fun fetchPosts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val posts = postRepository.getPosts()
                adapter.updateData(posts)
            } catch (e: Exception) {
                // Handle error (e.g., show a Toast or an error message in the UI)
                e.printStackTrace()
            }
        }
    }
}
