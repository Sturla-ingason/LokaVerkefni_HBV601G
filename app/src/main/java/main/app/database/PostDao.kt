package main.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostDao {


    /**
     * Allows us to get all the posts for a user from the local database
     */
    @Query("SELECT * FROM posts WHERE cachedByUserId = :userId ORDER BY postID DESC")
    suspend fun getPostsForUser(userId: Int): List<PostEntity>


    /**
     * Allows us to insert the posts that a user has created
     * into the local database for user later
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)


    /**
     * Allows us to delete all the posts from a user inn the local
     * database.
     */
    @Query("DELETE FROM posts WHERE cachedByUserId = :userId")
    suspend fun deleteAllForUser(userId: Int)
}