package main.app.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import main.app.dataModel.Comment

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromCommentList(comments: List<Comment>?): String? =
        comments?.let { json.encodeToString(it) }

    @TypeConverter
    fun toCommentList(value: String?): List<Comment>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromLongList(ids: List<Long>?): String? =
        ids?.let { json.encodeToString(it) }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? =
        value?.let { json.decodeFromString(it) }
}