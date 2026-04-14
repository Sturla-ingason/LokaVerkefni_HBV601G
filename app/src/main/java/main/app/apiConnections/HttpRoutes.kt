package main.app.apiConnections

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
    val UPDATE_USER = "$BASE_URL/user/update"
    val DELETE_USER = "$BASE_URL/user/delete"
    val FOLLOW_USER = "$BASE_URL/user/follow"
    val UNFOLLOW_USER = "$BASE_URL/user/unfollow"
    val IS_FOLLOWING = "$BASE_URL/user/isfollowing"
    val GET_FOLLOWERS = "$BASE_URL/user/allfollowers"
    val GET_FOLLOWING = "$BASE_URL/user/allfollowing"
    val REMOVE_FOLLOWER = "$BASE_URL/user/removefollower"
    val BLOCK_USER = "$BASE_URL/user/block"
    val UNBLOCK_USER = "$BASE_URL/user/unblock"
    val IS_BLOCKED = "$BASE_URL/user/isblocked"
    val UPDATE_PROFILE_PICTURE = "$BASE_URL/user/update-picture"

    // Posts
    val CREATE_POST = "$BASE_URL/post/create"
    val GET_USERS_POSTS = "$BASE_URL/post/userposts"
    val GET_FEED = "$BASE_URL/feed/getfeed"
    val LIKE_POST = "$BASE_URL/post/like"
    val UNLIKE_POST = "$BASE_URL/post/unlike"
    val GET_COMMENTS = "$BASE_URL/post/comment"
    val CREATE_COMMENT = "$BASE_URL/post/comment/create"
    val DELETE_COMMENT = "$BASE_URL/post/comment/delete"
    val GET_LIKES = "$BASE_URL/post/likes"
    val GET_PROFILE_POSTS = "$BASE_URL/post/profileposts"
    val EDIT_POST = "$BASE_URL/post/edit"
    val DELETE_POST = "$BASE_URL/post/delete"

    // Images
    val GET_IMAGE = "$BASE_URL/image"

    // Search
    val USER_SEARCH = "$BASE_URL/usersearch"
    val HASHTAG_SEARCH = "$BASE_URL/hashtagsearch"

    // Notifications
    val GET_NOTIFICATIONS = "$BASE_URL/notification/get"
    val GET_UNREAD_COUNT = "$BASE_URL/notification/unread/count"
    val MARK_NOTIFICATION_READ = "$BASE_URL/notification/mark-read"

}
