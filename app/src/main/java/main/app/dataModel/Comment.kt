package main.app.dataModel

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val commentID: Int? = null,
    val comment: String? = null,
    val userId: Int? = null,
    val username: String? = null,
): java.io.Serializable