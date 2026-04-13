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

/**
 * ViewModel for CommentFragment.
 * Handles loading comments and adding new ones.
 */
class CommentViewModel : ViewModel() {

    private val commentRepository = CommentRepository()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    /** Emits once after a comment is successfully added so the fragment can scroll and clear input. */
    private val _commentAdded = MutableSharedFlow<Unit>()
    val commentAdded: SharedFlow<Unit> = _commentAdded

    /** Emits an error message to show as a toast. */
    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

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
                e.printStackTrace()
                _error.emit("Failed to post comment")
            }
        }
    }
}