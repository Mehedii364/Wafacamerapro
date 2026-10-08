package com.example.core.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MediaSaver {

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        titlePrefix: String = "WAFA_IMG",
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 95
    ): Result<Uri> = withContext(Dispatchers.IO) {
        val extension = when (format) {
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP, Bitmap.CompressFormat.WEBP_LOSSY, Bitmap.CompressFormat.WEBP_LOSSLESS -> "webp"
            else -> "jpg"
        }
        val mimeType = when (extension) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val displayName = "${titlePrefix}_${timestamp}.$extension"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/WafaCameraPro")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        try {
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

            resolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(format, quality, out)) {
                    return@withContext Result.failure(Exception("Bitmap compression failed"))
                }
            } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePdfToDocuments(
        context: Context,
        pdfBytes: ByteArray,
        documentName: String = "WAFA_SCAN"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val displayName = "${documentName}_${timestamp}.pdf"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/WafaScans")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        try {
            val resolver = context.contentResolver
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues)
            } else {
                resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            } ?: return@withContext Result.failure(Exception("Failed to create PDF entry"))

            resolver.openOutputStream(uri)?.use { out ->
                out.write(pdfBytes)
                out.flush()
            } ?: return@withContext Result.failure(Exception("Failed to write PDF stream"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAppTempFile(context: Context, prefix: String, suffix: String): File {
        val cacheDir = File(context.cacheDir, "temp_media").apply { mkdirs() }
        return File.createTempFile(prefix, suffix, cacheDir)
    }
}
