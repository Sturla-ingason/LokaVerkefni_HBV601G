package main.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostDao {

    @Query("SELECT * FROM posts WHERE cachedByUserId = :userId ORDER BY postID DESC")
    suspend fun getPostsForUser(userId: Int): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE cachedByUserId = :userId")
    suspend fun deleteAllForUser(userId: Int)
}