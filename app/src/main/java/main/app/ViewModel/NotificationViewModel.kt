package main.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import main.app.dataModel.Notification
import main.app.repository.NotificationRepository

class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    fun loadNotifications() {
        viewModelScope.launch {
            try {
                _notifications.value = repository.getNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                _unreadCount.value = repository.getUnreadCount()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            try {
                repository.markAsRead(notificationId)
                _notifications.value = _notifications.value.map {
                    if (it.id == notificationId) it.copy(read = true) else it
                }
                if (_unreadCount.value > 0) _unreadCount.value -= 1
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
