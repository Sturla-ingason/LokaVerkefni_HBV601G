package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.repository.PostRepository

sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    object Success : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}

class CreatePostViewModel : ViewModel() {

    private val postRepository = PostRepository()

    private val _state = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val state: StateFlow<CreatePostState> = _state

    fun createPost(description: String, imageBytes: ByteArray?, mimeType: String?) {
        viewModelScope.launch {
            _state.value = CreatePostState.Loading
            try {
                postRepository.createPost(description, imageBytes, mimeType)
                _state.value = CreatePostState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = CreatePostState.Error("Failed to create post")
            }
        }
    }

    fun resetState() {
        _state.value = CreatePostState.Idle
    }
}