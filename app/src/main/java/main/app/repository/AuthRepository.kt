package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient

class AuthRepository {

    /**
     * Allows a user to login to their account
     * @param email the users email
     * @param password the password of the user
     * @return true if login was successful, false if otherwise
     */
    suspend fun login(email: String, password: String): Boolean {
        return try {
            val response: HttpResponse = KtorClient.httpClient.post(HttpRoutes.LOGIN) {
                parameter("email", email)
                parameter("password", password)
            }
            val body = response.body<String>()
            val success = response.status.isSuccess() && !body.contains("not found", ignoreCase = true)
            if (!success) KtorClient.resetClient()
            success
        } catch (e: Exception) {
            e.printStackTrace()
            KtorClient.resetClient()
            false
        }
    }


    /**
     * Allows a user to logout of their account
     * @return true if logedout successfully, false if otherwise
     */
    suspend fun logout(): Boolean {
        return try {
            val response: HttpResponse = KtorClient.httpClient.post(HttpRoutes.LOGOUT)
            KtorClient.resetClient()
            response.status.isSuccess()
        } catch (e: Exception) {
            KtorClient.resetClient()
            false
        }
    }

    /**
     * Allows a user to signup for a new account
     * @param email the users email
     * @param username the users username
     * @param password the users password
     * @return true if signup was successful, false if otherwise
     */
    suspend fun signup(email: String, username: String, password: String): Boolean {
        return try {
            val response: HttpResponse = KtorClient.httpClient.post(HttpRoutes.SIGNUP) {
                parameter("email", email)
                parameter("username", username)
                parameter("password", password)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
