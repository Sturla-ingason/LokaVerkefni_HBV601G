package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.dataModel.User

class UserRepository {

    suspend fun getFollowerCount(): Int {
        return KtorClient.httpClient.get(HttpRoutes.FOLLOWER_COUNT).body()
    }


    suspend fun getFollowingCount(): Int {
        return KtorClient.httpClient.get(HttpRoutes.FOLLOWING_COUNT).body()
    }


    suspend fun getUser(): User {
        return KtorClient.httpClient.get(HttpRoutes.GET_USER).body()
    }

    suspend fun getUserById(userId: Int): User {
        return KtorClient.httpClient.get(HttpRoutes.PROFILE) {
            parameter("userId", userId)
        }.body()
    }

    /**
     * Updates the user's profile information
     * @param username the new username
     * @param email the new email
     * @param password the new password (empty string if unchanged)
     * @param bio the new bio
     * @return true if the update was successful, false otherwise
     */
    suspend fun updateUser(username: String, email: String, password: String, bio: String): Boolean {
        return try {
            val response: HttpResponse = KtorClient.httpClient.post(HttpRoutes.UPDATE_USER) {
                parameter("username", username)
                parameter("email", email)
                if (password.isNotEmpty()) {
                    parameter("password", password)
                }
                parameter("bio", bio)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes the current user's account
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deleteUser(): Boolean {
        return try {
            val response: HttpResponse = KtorClient.httpClient.post(HttpRoutes.DELETE_USER)
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}