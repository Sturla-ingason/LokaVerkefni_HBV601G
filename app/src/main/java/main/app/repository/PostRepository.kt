package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import main.app.dataModel.Post

class PostRepository {
    suspend fun getPosts(): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.GET_FEED).body()
    }

    suspend fun getPostByUser(): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.GET_USERS_POSTS).body()
    }

}
