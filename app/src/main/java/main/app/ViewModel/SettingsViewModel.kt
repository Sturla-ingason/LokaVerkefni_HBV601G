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


    fun clearUpdateResult() { _updateResult.value = null }
    fun clearLogoutComplete() { _logoutComplete.value = null }
    fun clearDeleteResult() { _deleteResult.value = null }
    fun clearLoadError() { _loadError.value = null }
}