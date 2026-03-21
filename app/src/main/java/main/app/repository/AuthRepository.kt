package main.app.repository

import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class AuthRepository {
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

    suspend fun logout(): Boolean {
        return try {
            val response: HttpResponse = KtorClient.httpClient.post(HttpRoutes.LOGOUT)
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}
