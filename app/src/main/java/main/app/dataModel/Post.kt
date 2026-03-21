package main.app.dataModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val userId: Int? = null,
    val id: Int? = null,
    val title: String? = null,
    val body: String? = null,
    
    // Potential alternative names from server
    @SerialName("postText") val postText: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("_id") val mongoId: String? = null
): java.io.Serializable
