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

    /**
     * Get's all the comments under a post
     * @param postId the id of the post to get the comment's from
     * @return a list of comments under a post
     */
    suspend fun getComments(postId: Int): List<Comment> {
        return KtorClient.httpClient.get(HttpRoutes.GET_COMMENTS) {
            parameter("postId", postId)
        }.body()
    }

    /**
     * Calls the api to create a new comment and save it to the database
     * @param postId the id of the post to create the comment under
     * @param text the content of the comment
     */
    suspend fun createComment(postId: Int, text: String) {
        KtorClient.httpClient.post(HttpRoutes.CREATE_COMMENT) {
            parameter("postId", postId)
            parameter("text", text)
        }.body<Unit>()
    }


    /**
     * Calles the api and deletes a comment from the database
     * @param commentId the id of the comment to delete
     */
    suspend fun deleteComment(commentId: Int) {
        KtorClient.httpClient.delete(HttpRoutes.DELETE_COMMENT) {
            parameter("commentId", commentId)
        }
    }
}