package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.dataModel.Notification

class NotificationRepository {

    /**
     * Get's all the notification for a user form the API
     * @return a list of notifications
     */
    suspend fun getNotifications(): List<Notification> {
        return KtorClient.httpClient.get(HttpRoutes.GET_NOTIFICATIONS).body()
    }


    /**
     * get's all the unread notificaitons from the API
     * @return number of unread notifications
     */
    suspend fun getUnreadCount(): Int {
        return KtorClient.httpClient.get(HttpRoutes.GET_UNREAD_COUNT).body()
    }


    /**
     * Marks a notificaiton as read
     * @param notificationId the id of the notification to mark
     */
    suspend fun markAsRead(notificationId: Int) {
        KtorClient.httpClient.post(HttpRoutes.MARK_NOTIFICATION_READ) {
            parameter("notificationId", notificationId)
        }
    }
}
