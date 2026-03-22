package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import main.app.dataModel.Post

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

}
