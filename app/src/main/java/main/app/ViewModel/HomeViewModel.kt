package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.dataModel.Post
import main.app.repository.PostRepository

class HomeViewModel : ViewModel() {

    private val postRepository = PostRepository()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    /**
     * Loads the posts of the home feed
     */
    fun loadPosts() {
        viewModelScope.launch {
            try {
                _posts.value = postRepository.getPosts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * Allows a user to like or unlike a post
     * @param postId the id of the post to like
     */
    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            val post = _posts.value.find { it.postID == postId } ?: return@launch
            val currentlyLiked = post.likedByCurrentUser ?: false
            try {
                if (currentlyLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }
                _posts.value = _posts.value.map {
                    if (it.postID == postId) it.copy(
                        likedByCurrentUser = !currentlyLiked,
                        likeCount = (it.likeCount ?: 0) + if (currentlyLiked) -1 else 1
                    ) else it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}