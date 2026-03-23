package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.dataModel.Post
import main.app.dataModel.User

class PostRepository {
    suspend fun getPosts(): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.GET_FEED).body()
    }

    suspend fun getPostByUser(): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.GET_USERS_POSTS).body()
    }

    suspend fun likePost(postId: Int) {
        KtorClient.httpClient.post(HttpRoutes.LIKE_POST) {
            parameter("postId", postId)
        }
    }

    suspend fun unlikePost(postId: Int) {
        KtorClient.httpClient.patch(HttpRoutes.UNLIKE_POST) {
            parameter("postId", postId)
        }
    }

    suspend fun getLikes(postId: Int): List<User> {
        return try {
            KtorClient.httpClient.get(HttpRoutes.GET_LIKES) {
                parameter("postId", postId)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}
