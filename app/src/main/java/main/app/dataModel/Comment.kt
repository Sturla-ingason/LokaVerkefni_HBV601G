package main.app.dataModel

import kotlinx.serialization.Serializable

/**
 * this is a comment data object used to store comment data that we
 * get from the API
 */

@Serializable
data class Comment(
    val commentID: Int? = null,
    val comment: String? = null,
    val userId: Int? = null,
    val username: String? = null,
): java.io.Serializable