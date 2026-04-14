package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.dataModel.Post
import main.app.dataModel.User
import main.app.repository.PostRepository
import main.app.repository.UserRepository

class ProfileViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val userRepository = UserRepository()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    fun loadPosts(userId: Int?) {
        viewModelScope.launch {
            try {
                _posts.value = if (userId != null) postRepository.getPostsByUserId(userId)
                               else postRepository.getPostByUser()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadProfileData(userId: Int?) {
        viewModelScope.launch {
            try {
                _user.value = if (userId != null) userRepository.getUserById(userId)
                              else userRepository.getUser()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadFollowBlockState(userId: Int) {
        viewModelScope.launch {
            try {
                _isFollowing.value = userRepository.isFollowing(userId)
                _isBlocked.value = userRepository.isBlocked(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            val post = _posts.value.find { it.postID == postId } ?: return@launch
            val currentlyLiked = post.likedByCurrentUser ?: false
            try {
                if (currentlyLiked) postRepository.unlikePost(postId)
                else postRepository.likePost(postId)
                _posts.value = _posts.value.map {
                    if (it.postID == postId) it.copy(
                        likedByCurrentUser = !currentlyLiked,
                        likeCount = (it.likeCount ?: 0) + if (currentlyLiked) -1 else 1
                    ) else it
                }
            } catch (e: Exception) {
                _error.emit("Failed to update like")
            }
        }
    }

    fun toggleFollow(userId: Int) {
        viewModelScope.launch {
            try {
                if (_isFollowing.value) userRepository.unfollowUser(userId)
                else userRepository.followUser(userId)
                _isFollowing.value = !_isFollowing.value
                loadProfileData(userId)
            } catch (e: Exception) {
                _error.emit("Failed to update follow")
            }
        }
    }

    fun toggleBlock(userId: Int) {
        viewModelScope.launch {
            try {
                if (_isBlocked.value) userRepository.unblockUser(userId)
                else userRepository.blockUser(userId)
                _isBlocked.value = !_isBlocked.value
                if (_isBlocked.value) _isFollowing.value = false
            } catch (e: Exception) {
                _error.emit("Failed to update block")
            }
        }
    }
}