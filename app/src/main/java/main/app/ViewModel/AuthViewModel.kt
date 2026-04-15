package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.repository.AuthRepository

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _signupState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signupState: StateFlow<AuthState> = _signupState

    /**
     * handles log inn for the user
     * @param email for the acccount of the user
     * @param password of the account of the user
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val success = authRepository.login(email, password)
            _loginState.value = if (success) AuthState.Success
            else AuthState.Error("Login failed. Please check your credentials.")
        }
    }

    /**
     * Allows a new user to sign upp to the service
     * @param email the email for the new user
     * @param username the name of the new user
     * @param password for the account that the user is creating
     */
    fun signup(email: String, username: String, password: String) {
        viewModelScope.launch {
            _signupState.value = AuthState.Loading
            val success = authRepository.signup(email, username, password)
            _signupState.value = if (success) AuthState.Success
            else AuthState.Error("Signup failed. Please try again.")
        }
    }
}