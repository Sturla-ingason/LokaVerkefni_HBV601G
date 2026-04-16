package main.app.dataModel

import kotlinx.serialization.Serializable

/**
 * This is a notificaiton object that we use to store data of notifications that
 * we get from the API
 */

@Serializable
data class Notification(
    val id: Int? = null,
    val type: String? = null,
    val message: String? = null,
    val read: Boolean? = null,
    val actorId: Int? = null,
    val actorUsername: String? = null,
    val postId: Int? = null,
    val createdAt: String? = null
)
