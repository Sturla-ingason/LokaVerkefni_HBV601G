package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.dataModel.Post
import main.app.dataModel.User

class SearchRepository {


    /**
     * Calles the API and searches for a user
     * @param username the name of the user to search for
     */
    suspend fun searchUsers(username: String): List<User> {
        return KtorClient.httpClient.get(HttpRoutes.USER_SEARCH) {
            parameter("username", username)
        }.body()
    }


    /**
     * Calles the API and searches for a hashtag on a post
     * @param hashtag the hashtag to search for.
     */
    suspend fun searchHashtags(hashtag: String): List<Post> {
        val cleanTag = hashtag.removePrefix("#")

        return KtorClient.httpClient.get(HttpRoutes.HASHTAG_SEARCH) {
            parameter("hastag", cleanTag)
        }.body()
    }
}
