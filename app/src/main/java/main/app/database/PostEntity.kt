package main.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import main.app.dataModel.Comment
import main.app.dataModel.Post

/**
 * this is a database table definition for the local database
 * to store the posts.
 */

@Entity(tableName = "posts", primaryKeys = ["postID", "cachedByUserId"])
data class PostEntity(
    val postID: Int,
    val cachedByUserId: Int,
    val userId: Int?,
    val username: String?,
    val description: String?,
    val likeCount: Int?,
    val likedByCurrentUser: Boolean?,
    val comments: List<Comment>?,
    val imageIds: List<Long>?,
)


/**
 *  takes a network available post and turns it into a post
 *  object that can be cashed and saved, this is allso where we
 *  inject the userId
 *  @param cachedByUserId the id of the user who owns the post
 */
fun Post.toEntity(cachedByUserId: Int) = PostEntity(
    postID = postID ?: 0,
    cachedByUserId = cachedByUserId,
    userId = userId,
    username = username,
    description = description,
    likeCount = likeCount,
    likedByCurrentUser = likedByCurrentUser,
    comments = comments,
    imageIds = imageIds,
)


/**
 *  Allows us to convert cashed posts into post object's that the
 *  rest of the project can understand
 */
fun PostEntity.toPost() = Post(
    postID = postID,
    userId = userId,
    username = username,
    description = description,
    likeCount = likeCount,
    likedByCurrentUser = likedByCurrentUser,
    comments = comments,
    imageIds = imageIds,
)