package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.dataModel.Notification

class NotificationRepository {

    suspend fun getNotifications(): List<Notification> {
        return KtorClient.httpClient.get(HttpRoutes.GET_NOTIFICATIONS).body()
    }

    suspend fun getUnreadCount(): Int {
        return KtorClient.httpClient.get(HttpRoutes.GET_UNREAD_COUNT).body()
    }

    suspend fun markAsRead(notificationId: Int) {
        KtorClient.httpClient.post(HttpRoutes.MARK_NOTIFICATION_READ) {
            parameter("notificationId", notificationId)
        }
    }
}
