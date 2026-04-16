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

    //TODO comment out this function
    /**
     * Allows us to keep track of the current user id of the logged inn user
     */
    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = userRepository.getUser().userID
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Gets the comments for a certain post
     * @param postId id of the post to get the comments for
     */
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

    /**
     * Allows a user to delete their own comment on any post
     * @param commentId the id of the comment to delete
     */
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

    /**
     * Allows the user to add a new comment to a post
     * @param postId id of the post the comment is for
     * @param text the content of the comment
     */
    fun addComment(postId: Int, text: String) {
        viewModelScope.launch {
            try {
                commentRepository.createComment(postId, text)
            } catch (e: Exception) {
                e.printStackTrace()
                _error.emit("Failed to post comment")
                return@launch
            }
            _commentAdded.emit(Unit)
            try {
                _comments.value = commentRepository.getComments(postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}