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


class EditPostViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository = PostRepository()


    private val _saveResult = MutableLiveData<String?>()
    val saveResult: LiveData<String?> = _saveResult


    private val _deleteComplete = MutableLiveData<Boolean?>()
    val deleteComplete: LiveData<Boolean?> = _deleteComplete


    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Allows a user to save any changes he has made to the post
     * @param postId the id of the post to change the information for
     * @param description the text content of the post
     * @param removeImageIds the id of the images to be removed from the post
     * @param newImageUri the bytearay of the new image ot be added to the post
     */
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


    /**
     * Allows the user to delete there own post
     * @param postId the id of the post to be deleted
     */
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

    //TODO finish commenting this out
    /**
     *
     */
    fun clearSaveResult() { _saveResult.value = null }

    //TODO finish commenting this out
    /**
     *
     */
    fun clearDeleteComplete() { _deleteComplete.value = null }

    //TODO finish commenting this out
    /**
     *
     */
    fun clearError() { _error.value = null }
}