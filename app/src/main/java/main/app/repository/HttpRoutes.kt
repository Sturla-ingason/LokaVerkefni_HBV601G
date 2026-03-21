package main.app.repository

import main.app.BuildConfig

/**
 * determines all the https routs for the api
 */
object HttpRoutes {
    private val BASE_URL = BuildConfig.BASE_URL

    // Auth
    val SIGNUP = "$BASE_URL/auth/signup"
    val LOGIN = "$BASE_URL/auth/login"
    val LOGOUT = "$BASE_URL/auth/logout"

    // User
    val GET_USER = "$BASE_URL/user/getuser"
    val PROFILE = "$BASE_URL/user/profile"
    val PROFILE_BY_USERNAME = "$BASE_URL/user/profile/by-username"
    val FOLLOWING_COUNT = "$BASE_URL/user/followingcount"
    val FOLLOWER_COUNT = "$BASE_URL/user/followercxount"

    // Posts
    val CREATE_POST = "$BASE_URL/post/create"
    val GET_USERS_POSTS = "$BASE_URL/post/userposts"
    val GET_FEED = "$BASE_URL/feed/getfeed"

}
