package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.dataModel.User
import main.app.repository.UserRepository

class FollowListViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    /**
     * Returns either the list of the Followers of the account or a list of
     * who the user is following
     * @param userId the owner of the two lists
     * @param mode determens if it is followers or following that we are geting
     */
    fun loadList(userId: Int, mode: String) {
        viewModelScope.launch {
            try {
                _users.value = if (mode == "Followers") {
                    userRepository.getFollowers(userId)
                } else {
                    userRepository.getFollowing(userId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.emit("Failed to load $mode")
            }
        }
    }


    /**
     * Allows a user to either remove a user so they stop following their account
     * or allows the user to unfollow a user that they are following
     * @param user the user that is performing the action
     * @param mode determins if we are removing a user or unfollowing them
     */
    fun removeUser(user: User, mode: String) {
        viewModelScope.launch {
            try {
                val uid = user.userID ?: return@launch
                if (mode == "Following") userRepository.unfollowUser(uid)
                else userRepository.removeFollower(uid)
                _users.value = _users.value.filter { it.userID != uid }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.emit("Action failed")
            }
        }
    }
}