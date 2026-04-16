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


    /**
     * Post data acces object
     */
    private val postDao by lazy {
        context?.let { AppDatabase.getDatabase(it).postDao() }
    }

    /**
     * Adds a user id when we cach data so users dont see each others cashed data.
     */
    private fun currentUserId(): Int? =
        context?.let { UserRepository(it).getCachedUser()?.userID }


    /**
     * caches all the posts of the user inn the local dtabase
     */
    private suspend fun cachePosts(posts: List<Post>) {
        val userId = currentUserId() ?: return
        postDao?.let { dao ->
            dao.deleteAllForUser(userId)
            dao.insertAll(posts.map { it.toEntity(userId) })
        }
    }


    /**
     * Gets all the cached posts by a user (the user has to be logged inn)
     */
    suspend fun getCachedPosts(): List<Post>? {
        val userId = currentUserId() ?: return null
        return postDao?.getPostsForUser(userId)?.map { it.toPost() }?.ifEmpty { null }
    }


    /**
     * Get's the posts for the feed and sorts them inn decending order by time.
     * So get's all posts from users that the logged inn user is following.
     * @return a list of posts from the followed users.
     */
    suspend fun getPosts(): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.GET_FEED).body<List<Post>>()
            .sortedByDescending { it.postID }
    }


    /**
     * Gets all the posts from a user. Also caches them to be seen offline
     * @return list of users posts.
     */
    suspend fun getPostByUser(): List<Post> {
        val posts = KtorClient.httpClient.get(HttpRoutes.GET_USERS_POSTS).body<List<Post>>()
            .sortedByDescending { it.postID }
        cachePosts(posts)
        return posts
    }


    /**
     * Updates the like counter on a post when a user likes a post
     * @param postId the id of the post to update the likes on
     */
    suspend fun likePost(postId: Int) {
        KtorClient.httpClient.post(HttpRoutes.LIKE_POST) {
            parameter("postId", postId)
        }
    }


    /**
     * Updates the like count when a user unlikes a post
     * @param postId the id of the post to update the like counter on
     */
    suspend fun unlikePost(postId: Int) {
        KtorClient.httpClient.patch(HttpRoutes.UNLIKE_POST) {
            parameter("postId", postId)
        }
    }


    /**
     * Get a list of users that have liked the post
     * @param postId id of the post to get the list from
     * @return list of users that have liked the post
     */
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


    /**
     * Allows a user to create a new post
     * @param description the string content of the post
     * @param imageBytes the raw data of the image
     * @param mimeType the format the images is inn for example jpeg
     */
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


    /**
     * Allows a user to delete a post that they have created
     * @param postId id of the post to delete
     */
    suspend fun deletePost(postId: Int) {
        KtorClient.httpClient.delete(HttpRoutes.DELETE_POST) {
            parameter("postID", postId)
        }
    }


    /**
     * Allows a user to edit a post that they have already created
     * @param postId the id of the post to edit
     * @param description the text content of the post
     * @param removeImageIds the id's of the images to remove from the posts.
     * @param imageBytes the raw data of the image
     * @param mimeType the format of the image.
     */
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


    /**
     * Get a post by a user id
     * @param userId the user id to get a post by
     */
    suspend fun getPostsByUserId(userId: Int): List<Post> {
        return KtorClient.httpClient.get(HttpRoutes.GET_PROFILE_POSTS) {
            parameter("userId", userId)
        }.body<List<Post>>().sortedByDescending { it.postID }
    }

}
