package main.app.dataModel

import kotlinx.serialization.Serializable

/**
 * Post Data object. Stores Post data that comes from the api
 */
@Serializable
data class Post(
    val postID: Int? = null,
    val userId: Int? = null,
    val username: String? = null,
    val description: String? = null,
    val likeCount: Int? = null,
    val likedByCurrentUser: Boolean? = null,
    val comments: List<Comment>? = null,
    val imageIds: List<Long>? = null,
): java.io.Serializable
