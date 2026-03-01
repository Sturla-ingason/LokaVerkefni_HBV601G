package main.app.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles creating a temporary image file on disk and returning a
 * secure URI that can be passed to the system camera intent.
 */
class CameraRepository(private val context: Context) {

    /**
     * Creates an empty .jpg file in the app's private Pictures folder
     * and returns a content:// URI the camera can write to.
     */
    fun createImageUri(): Uri {
        val fileName = "PHOTO_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        val imageFile = File(context.getExternalFilesDir("Pictures"), fileName)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }
}