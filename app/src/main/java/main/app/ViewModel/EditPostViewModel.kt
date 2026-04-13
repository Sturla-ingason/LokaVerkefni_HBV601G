package main.app.ViewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import main.app.repository.PostRepository

/**
 * ViewModel for EditPostFragment.
 * Handles saving edits and deleting posts.
 * Uses AndroidViewModel so it can access contentResolver for reading image bytes.
 */
class EditPostViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository = PostRepository()

    /** The updated description returned by the server after a successful save. Null = not triggered / consumed. */
    private val _saveResult = MutableLiveData<String?>()
    val saveResult: LiveData<String?> = _saveResult

    /** True once delete succeeds. Null = not triggered / consumed. */
    private val _deleteComplete = MutableLiveData<Boolean?>()
    val deleteComplete: LiveData<Boolean?> = _deleteComplete

    /** Error message to show as a toast. Null = no error / consumed. */
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun savePost(
        postId: Int,
        description: String,
        removeImageIds: List<Long>?,
        newImageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                var imageBytes: ByteArray? = null
                var mimeType: String? = null
                newImageUri?.let { uri ->
                    val contentResolver = getApplication<Application>().contentResolver
                    val stream = contentResolver.openInputStream(uri)
                    imageBytes = stream?.readBytes()
                    stream?.close()
                    mimeType = contentResolver.getType(uri)
                }
                val updatedPost = withContext(Dispatchers.IO) {
                    postRepository.editPost(postId, description, removeImageIds, imageBytes, mimeType)
                }
                _saveResult.value = updatedPost.description ?: description
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to save changes"
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    postRepository.deletePost(postId)
                }
                _deleteComplete.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to delete post"
            }
        }
    }

    fun clearSaveResult() { _saveResult.value = null }
    fun clearDeleteComplete() { _deleteComplete.value = null }
    fun clearError() { _error.value = null }
}