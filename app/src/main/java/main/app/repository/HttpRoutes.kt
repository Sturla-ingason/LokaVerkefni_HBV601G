package main.app.repository

import main.app.BuildConfig

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

    // Posts
    val CREATE_POST = "$BASE_URL/post/create"
    val GET_FEED = "$BASE_URL/feed/getfeed"
}
