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

/**
 * ViewModel for SettingsFragment.
 * Handles loading user data, saving profile changes, logout, and account deletion.
 * All results use nullable LiveData so they are consumed once and don't replay on rotation.
 */
class SettingsViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    /** true = success, false = failure, null = not triggered yet / already consumed */
    private val _updateResult = MutableLiveData<Boolean?>()
    val updateResult: LiveData<Boolean?> = _updateResult

    /** Set to true once logout finishes, null otherwise */
    private val _logoutComplete = MutableLiveData<Boolean?>()
    val logoutComplete: LiveData<Boolean?> = _logoutComplete

    /** true = deleted, false = failed, null = not triggered yet / already consumed */
    private val _deleteResult = MutableLiveData<Boolean?>()
    val deleteResult: LiveData<Boolean?> = _deleteResult

    private val _loadError = MutableLiveData<Boolean?>()
    val loadError: LiveData<Boolean?> = _loadError

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

    fun logout() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { authRepository.logout() }
            } catch (_: Exception) { }
            _logoutComplete.value = true
        }
    }

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

    /** Call after consuming a result to prevent it replaying on rotation. */
    fun clearUpdateResult() { _updateResult.value = null }
    fun clearLogoutComplete() { _logoutComplete.value = null }
    fun clearDeleteResult() { _deleteResult.value = null }
    fun clearLoadError() { _loadError.value = null }
}