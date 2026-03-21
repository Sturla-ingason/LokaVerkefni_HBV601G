package main.app.dataModel

import kotlinx.serialization.SerialName
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
): java.io.Serializable


/**
 * Auka shit sem ég tók út til að prófa annað.
 * // Potential alternative names from server
 *     @SerialName("postText") val postText: String? = null,
 *     @SerialName("description") val description: String? = null,
 *     @SerialName("content") val content: String? = null,
 *     @SerialName("username") val username: String? = null,
 *     @SerialName("_id") val mongoId: String? = null
 */