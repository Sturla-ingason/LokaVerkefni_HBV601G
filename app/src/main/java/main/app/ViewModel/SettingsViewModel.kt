package main.app.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import main.app.dataModel.User
import main.app.repository.AuthRepository
import main.app.repository.UserRepository


class SettingsViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user


    private val _updateResult = MutableLiveData<Boolean?>()
    val updateResult: LiveData<Boolean?> = _updateResult


    private val _logoutComplete = MutableLiveData<Boolean?>()
    val logoutComplete: LiveData<Boolean?> = _logoutComplete


    private val _deleteResult = MutableLiveData<Boolean?>()
    val deleteResult: LiveData<Boolean?> = _deleteResult

    private val _loadError = MutableLiveData<Boolean?>()
    val loadError: LiveData<Boolean?> = _loadError


    /**
     * Allows us to load all the information for the current user
     * so that he can edit it.
     */
    fun loadUser() {
        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) { userRepository.getUser() }
                _user.value = user
            } catch (e: Exception) {
                e.printStackTrace()
                _loadError.value = true
            }
        }
    }


    /**
     * Allows a user to updated their settings for the account
     * @param username the user name the user wants's to use
     * @param email the email the account sholud be connected to
     * @param password the password to the account
     * @param bio the bio/description of the user
     */
    fun saveSettings(username: String, email: String, password: String, bio: String) {
        viewModelScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    userRepository.updateUser(username, email, password, bio)
                }
                _updateResult.value = success
            } catch (e: Exception) {
                e.printStackTrace()
                _updateResult.value = false
            }
        }
    }


    /**
     * Allows a user to logout of their account
     */
    fun logout() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { authRepository.logout() }
            } catch (_: Exception) { }
            _logoutComplete.value = true
        }
    }


    /**
     * Allows the user to delete their account
     */
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val success = withContext(Dispatchers.IO) { userRepository.deleteUser() }
                _deleteResult.value = success
            } catch (e: Exception) {
                e.printStackTrace()
                _deleteResult.value = false
            }
        }
    }


    //TODO comment this out
    /**
     * Allows the user to updated their profile picture
     * @param imageBytes
     * @param mimeType
     */
    fun updateProfilePicture(imageBytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    userRepository.updateProfilePicture(imageBytes, mimeType)
                }
                _updateResult.value = success
            } catch (e: Exception) {
                e.printStackTrace()
                _updateResult.value = false
            }
        }
    }


    /**
     * Clears the updated resaults back to null after we have updated
     * so that we dont get a retriger for update complete when we recreate
     * the fragment
     */
    fun clearUpdateResult() { _updateResult.value = null }


    /**
     * clears the logout complete value back to null so that we don't
     * geta retrigger of it if we create the fragment again
     */
    fun clearLogoutComplete() { _logoutComplete.value = null }


    /**
     * clears the delete resault back to null so that we dont get a
     * retrigger of it when we recreate the fragment
     */
    fun clearDeleteResult() { _deleteResult.value = null }


    /**
     * clears the load erro value back to null so that we dont get a retrigger
     * of it when we load the fragment again.
     */
    fun clearLoadError() { _loadError.value = null }
}