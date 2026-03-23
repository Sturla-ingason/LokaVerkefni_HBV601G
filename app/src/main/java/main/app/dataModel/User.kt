package main.app.dataModel

import kotlinx.serialization.Serializable

/**
 * User data object. Stores user data from the Api
 */
@Serializable
data class User (
    val userID: Int? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val bio: String? = null,
    val imageId: Int? = null,
    val following: Int? = null,
    val followers: Int? = null,
):java.io.Serializable
