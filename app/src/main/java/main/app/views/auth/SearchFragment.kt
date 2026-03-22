package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.dataModel.Post
import main.app.repository.SearchRepository

class SearchFragment : Fragment() {

    private val searchRepository = SearchRepository()

    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var searchStatus: TextView
    private lateinit var searchRecyclerView: RecyclerView

    private lateinit var adapter: Adapter

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

        adapter = Adapter(emptyList())
        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerView.adapter = adapter

        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            adapter.updateData(emptyList())
            showStatus("Enter a username or hashtag")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results: List<Post> = if (query.startsWith("#")) {
                    searchRepository.searchHashtags(query)
                } else {
                    searchRepository.searchUsers(query)
                }

                adapter.updateData(results)

                if (results.isEmpty()) {
                    showStatus("No results found")
                } else {
                    showStatus("${results.size} result(s) found")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                adapter.updateData(emptyList())
                showStatus("Search failed")
            }
        }
    }

    private fun showStatus(message: String) {
        searchStatus.visibility = View.VISIBLE
        searchStatus.text = message
    }
}