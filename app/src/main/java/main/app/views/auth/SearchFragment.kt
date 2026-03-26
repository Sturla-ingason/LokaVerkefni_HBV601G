package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.adapters.Adapter
import main.app.adapters.UserAdapter
import main.app.repository.SearchRepository

class SearchFragment : Fragment() {

    private val searchRepository = SearchRepository()

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

        adapter = Adapter(emptyList(), viewLifecycleOwner.lifecycleScope, childFragmentManager)
        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerView.adapter = adapter

        userAdapter = UserAdapter(emptyList()) { user ->
            user.userID?.let { openProfile(it) }
        }

        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (query.startsWith("#")) {
                    val posts = searchRepository.searchHashtags(query)
                    searchRecyclerView.adapter = adapter
                    adapter.updateData(posts)
                } else {
                    val users = searchRepository.searchUsers(query)
                    searchRecyclerView.adapter = userAdapter
                    userAdapter.updateData(users)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openProfile(userId: Int) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.flFragment, ProfileFragment.newInstance(userId))
            .addToBackStack(null)
            .commit()
    }

    private fun showStatus(message: String) {
        searchStatus.visibility = View.VISIBLE
        searchStatus.text = message
    }
}
