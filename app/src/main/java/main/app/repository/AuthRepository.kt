package main.app.repository

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
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
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
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}
