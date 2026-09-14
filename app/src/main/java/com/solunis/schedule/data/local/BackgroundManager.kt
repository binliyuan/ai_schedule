package com.solunis.schedule.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

object BackgroundManager {

    private const val FILE_NAME = "bg_custom.jpg"

    fun saveBackground(context: Context, bitmap: Bitmap) {
        val file = File(context.filesDir, FILE_NAME)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
    }

    fun getBackgroundFile(context: Context): File? {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) file else null
    }

    fun hasCustomBackground(context: Context): Boolean =
        File(context.filesDir, FILE_NAME).exists()

    fun clearBackground(context: Context) {
        File(context.filesDir, FILE_NAME).delete()
    }
}
