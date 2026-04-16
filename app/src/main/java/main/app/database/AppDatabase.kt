package main.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * this class is the access into the local database of the phone
 * or device that we are working on
 */
@Database(entities = [PostEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {


    /**
     * Gives access to pors related database operations
     * through the post data access object.
     */
    abstract fun postDao(): PostDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        /**
         * Creates a singleton access to the database. Creates one if there
         * is not one available at the moment.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
        }
    }
}