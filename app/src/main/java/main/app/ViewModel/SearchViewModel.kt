package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.dataModel.Post
import main.app.dataModel.User
import main.app.repository.PostRepository
import main.app.repository.SearchRepository

sealed class SearchResults {
    object Idle : SearchResults()
    data class Posts(val posts: List<Post>) : SearchResults()
    data class Users(val users: List<User>) : SearchResults()
}

class SearchViewModel : ViewModel() {

    private val searchRepository = SearchRepository()
    private val postRepository = PostRepository()

    private val _results = MutableStateFlow<SearchResults>(SearchResults.Idle)
    val results: StateFlow<SearchResults> = _results

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            try {
                if (query.startsWith("#")) {
                    _results.value = SearchResults.Posts(searchRepository.searchHashtags(query))
                } else {
                    _results.value = SearchResults.Users(searchRepository.searchUsers(query))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleLike(postId: Int) {
        val current = (_results.value as? SearchResults.Posts)?.posts ?: return
        val post = current.find { it.postID == postId } ?: return
        val currentlyLiked = post.likedByCurrentUser ?: false
        viewModelScope.launch {
            try {
                if (currentlyLiked) postRepository.unlikePost(postId)
                else postRepository.likePost(postId)
                _results.value = SearchResults.Posts(current.map {
                    if (it.postID == postId) it.copy(
                        likedByCurrentUser = !currentlyLiked,
                        likeCount = (it.likeCount ?: 0) + if (currentlyLiked) -1 else 1
                    ) else it
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}