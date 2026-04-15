package main.app.repository

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.database.AppDatabase
import main.app.database.toEntity
import main.app.database.toPost
import main.app.dataModel.Post
import main.app.dataModel.User

class PostRepository(private val context: Context? = null) {

    private val postDao by lazy {
        context?.let { AppDatabase.getDatabase(it).postDao() }
    }

    private fun currentUserId(): Int? =
        context?.let { UserRepository(it).getCachedUser()?.userID }

    private suspend fun cachePosts(posts: List<Post>) {
        val userId = currentUserId() ?: return
        postDao?.let { dao ->
            dao.deleteAllForUser(userId)
            dao.insertAll(posts.map { it.toEntity(userId) })
        }
    }

    suspend fun getCachedPosts(): List<Post>? {
        val userId = currentUserId() ?: return null
        return postDao?.getPostsForUser(userId)?.map { it.toPost() }?.ifEmpty { null }
    }

    suspend fun getPosts(): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.GET_FEED).body<List<Post>>()
            .sortedByDescending { it.postID }
    }

    suspend fun getPostByUser(): List<Post> {
        val posts = KtorClient.httpClient.get(HttpRoutes.GET_USERS_POSTS).body<List<Post>>()
            .sortedByDescending { it.postID }
        cachePosts(posts)
        return posts
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

    suspend fun createPost(description: String, imageBytes: ByteArray?, mimeType: String?): Post {
        return KtorClient.httpClient.post(HttpRoutes.CREATE_POST) {
            setBody(MultiPartFormDataContent(
                formData {
                    append("description", description)
                    if (imageBytes != null && mimeType != null) {
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                        })
                    }
                }
            ))
        }.body()
    }

    suspend fun deletePost(postId: Int) {
        KtorClient.httpClient.delete(HttpRoutes.DELETE_POST) {
            parameter("postID", postId)
        }
    }

    suspend fun editPost(
        postId: Int,
        description: String,
        removeImageIds: List<Long>? = null,
        imageBytes: ByteArray? = null,
        mimeType: String? = null
    ): Post {
        return if (imageBytes != null && mimeType != null) {
            KtorClient.httpClient.put(HttpRoutes.EDIT_POST) {
                parameter("postId", postId)
                parameter("description", description)
                removeImageIds?.forEach { id -> parameter("removeImageIds", id) }
                setBody(MultiPartFormDataContent(formData {
                    append("image", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                    })
                }))
            }.body()
        } else {
            KtorClient.httpClient.put(HttpRoutes.EDIT_POST) {
                parameter("postId", postId)
                parameter("description", description)
                removeImageIds?.forEach { id -> parameter("removeImageIds", id) }
            }.body()
        }
    }

    suspend fun getPostsByUserId(userId: Int): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.GET_PROFILE_POSTS) {
            parameter("userId", userId)
        }.body<List<Post>>().sortedByDescending { it.postID }
    }

}
