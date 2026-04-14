package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.dataModel.Comment
import main.app.dataModel.User
import main.app.repository.CommentRepository
import main.app.repository.PostRepository
import main.app.repository.UserRepository

class PostDetailViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val commentRepository = CommentRepository()
    private val userRepository = UserRepository()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _showEditButton = MutableStateFlow(false)
    val showEditButton: StateFlow<Boolean> = _showEditButton

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _likeCount = MutableStateFlow(0)
    val likeCount: StateFlow<Int> = _likeCount

    fun initComments(comments: List<Comment>) {
        _comments.value = comments
    }

    fun initLikeState(liked: Boolean, count: Int) {
        _isLiked.value = liked
        _likeCount.value = count
    }

    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            val currentlyLiked = _isLiked.value
            try {
                if (currentlyLiked) postRepository.unlikePost(postId)
                else postRepository.likePost(postId)
                _isLiked.value = !currentlyLiked
                _likeCount.value = _likeCount.value + if (currentlyLiked) -1 else 1
            } catch (e: Exception) {
                _error.emit("Failed to update like")
            }
        }
    }

    private val _likedUsers = MutableSharedFlow<List<User>>()
    val likedUsers: SharedFlow<List<User>> = _likedUsers

    private val _commentAdded = MutableSharedFlow<Unit>()
    val commentAdded: SharedFlow<Unit> = _commentAdded

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    fun checkEditButton(postUserId: Int?) {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getUser()
                _currentUserId.value = currentUser.userID
                _showEditButton.value = currentUser.userID == postUserId
            } catch (e: Exception) {
                // leave hidden
            }
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            try {
                commentRepository.deleteComment(commentId)
                _comments.value = _comments.value.filter { it.commentID != commentId }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.emit("Failed to delete comment")
            }
        }
    }

    fun loadLikes(postId: Int) {
        viewModelScope.launch {
            try {
                val users = postRepository.getLikes(postId)
                _likedUsers.emit(users)
            } catch (e: Exception) {
                _error.emit("Failed to load likes")
            }
        }
    }

    fun loadComments(postId: Int) {
        viewModelScope.launch {
            try {
                _comments.value = commentRepository.getComments(postId)
            } catch (e: Exception) {
                e.printStackTrace()
                _error.emit("Failed to load comments")
            }
        }
    }

    fun addComment(postId: Int, text: String) {
        viewModelScope.launch {
            try {
                commentRepository.createComment(postId, text)
                _comments.value = commentRepository.getComments(postId)
                _commentAdded.emit(Unit)
            } catch (e: Exception) {
                _error.emit("Failed to post comment")
            }
        }
    }
}