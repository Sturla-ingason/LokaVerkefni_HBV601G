package main.app.dataModel

import kotlinx.serialization.Serializable

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
