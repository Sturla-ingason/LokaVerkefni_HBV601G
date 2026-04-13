package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.SearchResults
import main.app.ViewModel.SearchViewModel
import main.app.adapters.Adapter
import main.app.adapters.UserAdapter

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()

    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var searchStatus: TextView
    private lateinit var searchRecyclerView: RecyclerView

    private lateinit var adapter: Adapter
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchButton)
        searchStatus = view.findViewById(R.id.searchStatus)
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView)

        adapter = Adapter(
            emptyList(),
            viewLifecycleOwner.lifecycleScope,
            childFragmentManager,
            onLikeToggle = { postId -> viewModel.toggleLike(postId) }
        )

        userAdapter = UserAdapter(emptyList()) { user ->
            user.userID?.let { openProfile(it) }
        }

        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchButton.setOnClickListener {
            viewModel.search(searchInput.text.toString().trim())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.results.collect { results ->
                when (results) {
                    is SearchResults.Posts -> {
                        searchRecyclerView.adapter = adapter
                        adapter.updateData(results.posts)
                    }
                    is SearchResults.Users -> {
                        searchRecyclerView.adapter = userAdapter
                        userAdapter.updateData(results.users)
                    }
                    is SearchResults.Idle -> Unit
                }
            }
        }
    }

    private fun openProfile(userId: Int) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.flFragment, ProfileFragment.newInstance(userId))
            .addToBackStack(null)
            .commit()
    }
}