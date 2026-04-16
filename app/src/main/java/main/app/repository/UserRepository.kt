package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.dataModel.User
import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserRepository(private val context: Context? = null) {

    /**
     * Gets a sharedprefrence instance named user_cache that only the app can use
     */
    private val prefs by lazy {
        context?.getSharedPreferences("user_cache", Context.MODE_PRIVATE)
    }


    /**
     * stores a given user inn shared prefrences to be view offline.
     * @param user the user object to store.
     */
    private fun cacheUser(user: User) {
        prefs?.edit()?.putString("cached_user", Json.encodeToString(user))?.apply()
    }


    /**
     * Gets the cached user data and returns it as a user object that can be used to fill information
     * @return the cached user, if there is no user cached return null
     */
    fun getCachedUser(): User? {
        val json = prefs?.getString("cached_user", null) ?: return null
        return try {
            Json { ignoreUnknownKeys = true }.decodeFromString(json)
        } catch (e: Exception) {
            null
        }
    }



    /**
     * Allows us to get the follower count of a user
     * @return int count of followers
     */
    suspend fun getFollowerCount(): Int {
        return KtorClient.httpClient.get(HttpRoutes.FOLLOWER_COUNT).body()
    }


    /**
     * Gets the number of users following the logged inn user
     * @param int count amount of users that are folliwng the user
     */
    suspend fun getFollowingCount(): Int {
        return KtorClient.httpClient.get(HttpRoutes.FOLLOWING_COUNT).body()
    }


    /**
     * Allows us to get the information for the current user and caches it.
     * @return user object of the current user
     */
    suspend fun getUser(): User {
        val user = KtorClient.httpClient.get(HttpRoutes.GET_USER).body<User>()
        cacheUser(user)
        return user
    }


    /**
     * Allows us to find a user by userId
     * @param userId the id of the user to find
     * @return user object of the found user
     */
    suspend fun getUserById(userId: Int): User {
        return KtorClient.httpClient.get(HttpRoutes.PROFILE) {
            parameter("userId", userId)
        }.body()
    }

    /**
     * Updates the user's profile information
     * @param username the new username
     * @param email the new email
     * @param password the new password (empty string if unchanged)
     * @param bio the new bio
     * @return true if the update was successful, false otherwise
     */
    suspend fun updateUser(username: String, email: String, password: String, bio: String): Boolean {
        return try {
            val response: HttpResponse = KtorClient.httpClient.put(HttpRoutes.UPDATE_USER) {
                parameter("username", username)
                parameter("email", email)
                if (password.isNotEmpty()) {
                    parameter("password", password)
                }
                parameter("bio", bio)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /**
     * Allows us to get all the users that are following another user
     * @param userId id of the user we want to get the followers of
     * @return list of users that are following the user
     */
    suspend fun getFollowers(userId: Int): List<User> {
        return KtorClient.httpClient.get(HttpRoutes.GET_FOLLOWERS) {
            parameter("userId", userId)
        }.body()
    }


    /**
     * Allows us to get all the users that a user is following
     * @param userId id of the user we want to get the list for
     * @return a list of users that a user is following
     */
    suspend fun getFollowing(userId: Int): List<User> {
        return KtorClient.httpClient.get(HttpRoutes.GET_FOLLOWING) {
            parameter("userId", userId)
        }.body()
    }


    /**
     * Allows another user to block another user.
     * @param userId id of the user to be blocked
     */
    suspend fun blockUser(userId: Int) {
        KtorClient.httpClient.patch(HttpRoutes.BLOCK_USER) {
            parameter("userID", userId)
        }
    }


    /**
     * Allows a user to unblock another user
     * @param userId id of the user to unblock
     */
    suspend fun unblockUser(userId: Int) {
        KtorClient.httpClient.patch(HttpRoutes.UNBLOCK_USER) {
            parameter("userID", userId)
        }
    }


    /**
     *  Checks if a user is blocked by the current user
     *  @param userId id of the user to check if he is blocked or not
     *  @return true or false depending on if the user is blcoked or not
     */
    suspend fun isBlocked(userId: Int): Boolean {
        return KtorClient.httpClient.get(HttpRoutes.IS_BLOCKED) {
            parameter("userID", userId)
        }.body()
    }


    /**
     * Allows the active user to remove a follower from their account
     * @param userId the id of the user to be removed
     */
    suspend fun removeFollower(userId: Int) {
        KtorClient.httpClient.patch(HttpRoutes.REMOVE_FOLLOWER) {
            parameter("userID", userId)
        }
    }


    /**
     * Allows the active user to follow another user
     * @param userId the id of the user to follow
     */
    suspend fun followUser(userId: Int) {
        KtorClient.httpClient.patch(HttpRoutes.FOLLOW_USER) {
            parameter("userID", userId)
        }.body<Unit>()
    }


    /**
     *  Allows the active user to unfollow another user
     *  @param userId id of the user to unfollow
     */
    suspend fun unfollowUser(userId: Int) {
        KtorClient.httpClient.patch(HttpRoutes.UNFOLLOW_USER) {
            parameter("userID", userId)
        }.body<Unit>()
    }


    /**
     * Allows us to check if the current user is following another usre
     * @param userId the id of the user to check on
     * @return true or false depending of if the user is followed or not
     */
    suspend fun isFollowing(userId: Int): Boolean {
        return KtorClient.httpClient.get(HttpRoutes.IS_FOLLOWING) {
            parameter("userID", userId)
        }.body()
    }


    /**
     * Deletes the current user's account
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deleteUser(): Boolean {
        return try {
            KtorClient.httpClient.post(HttpRoutes.DELETE_USER).body<Unit>()
            KtorClient.resetClient()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /**
     * Allows the user to update their user profile pick
     * @param imageBytes the raw data of the image.
     * @param mimeType the format or type of the image for example jpeg
     */
    suspend fun updateProfilePicture(imageBytes: ByteArray, mimeType: String): Boolean {
        return try {
            val response: HttpResponse = KtorClient.httpClient.put(HttpRoutes.UPDATE_PROFILE_PICTURE) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("image", imageBytes, Headers.build {
                                append(HttpHeaders.ContentType, mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                            })
                        }
                    )
                )
            }

            println("updateProfilePicture status = ${response.status}")
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}