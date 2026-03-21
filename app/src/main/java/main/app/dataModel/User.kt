package main.app.dataModel

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val email: String,
    val password: String
):java.io.Serializable
