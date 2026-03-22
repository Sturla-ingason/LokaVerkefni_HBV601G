package main.app.dataModel

import kotlinx.serialization.Serializable

/**
 * User data object. Stores user data from the Api
 */
@Serializable
data class User (
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val following: Int? = null,
    val followers: Int? = null,
):java.io.Serializable
