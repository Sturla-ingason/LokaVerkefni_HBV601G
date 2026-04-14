package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.dataModel.Comment
import main.app.repository.CommentRepository
import main.app.repository.UserRepository


class CommentViewModel : ViewModel() {

    private val commentRepository = CommentRepository()
    private val userRepository = UserRepository()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    private val _commentAdded = MutableSharedFlow<Unit>()
    val commentAdded: SharedFlow<Unit> = _commentAdded

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = userRepository.getUser().userID
            } catch (e: Exception) {
                e.printStackTrace()
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

    fun addComment(postId: Int, text: String) {
        viewModelScope.launch {
            try {
                commentRepository.createComment(postId, text)
                _comments.value = commentRepository.getComments(postId)
                _commentAdded.emit(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                _error.emit("Failed to post comment")
            }
        }
    }
}