package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.delete
import io.ktor.client.request.post
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.dataModel.Comment

class CommentRepository {
    suspend fun getComments(postId: Int): List<Comment> {
        return KtorClient.httpClient.get(HttpRoutes.GET_COMMENTS) {
            parameter("postId", postId)
        }.body()
    }

    suspend fun createComment(postId: Int, text: String) {
        KtorClient.httpClient.post(HttpRoutes.CREATE_COMMENT) {
            parameter("postId", postId)
            parameter("text", text)
        }
    }

    suspend fun deleteComment(commentId: Int) {
        KtorClient.httpClient.delete(HttpRoutes.DELETE_COMMENT) {
            parameter("commentId", commentId)
        }
    }
}