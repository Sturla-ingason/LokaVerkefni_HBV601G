package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import main.app.dataModel.Post

class SearchRepository {

    suspend fun searchUsers(username: String): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.USER_SEARCH) {
            parameter("username", username)
        }.body()
    }

    suspend fun searchHashtags(hashtag: String): List<Post> {
        val cleanTag = hashtag.removePrefix("#")

        return KtorClient.httpClient.get(HttpRoutes.HASHTAG_SEARCH) {
            parameter("hastag", cleanTag)
        }.body()
    }
}
