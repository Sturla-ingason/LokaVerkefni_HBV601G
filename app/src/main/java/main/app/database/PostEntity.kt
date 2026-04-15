package main.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import main.app.dataModel.Comment
import main.app.dataModel.Post

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