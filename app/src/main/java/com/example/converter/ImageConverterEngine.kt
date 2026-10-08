package com.example.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class ConversionResult(
    val outputBytes: ByteArray,
    val outputFormat: String,
    val originalSizeBytes: Long,
    val outputSizeBytes: Long,
    val reductionPercentage: Double,
    val outputBitmap: Bitmap
)

object ImageConverterEngine {

    suspend fun convertAndCompress(
        context: Context,
        inputUri: Uri,
        targetFormat: String, // "JPEG", "PNG", "WEBP"
        quality: Int = 85,
        scaleFactor: Float = 1.0f
    ): Result<ConversionResult> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val originalSize = resolver.openFileDescriptor(inputUri, "r")?.statSize ?: 0L

            val inputStream = resolver.openInputStream(inputUri)
                ?: return@withContext Result.failure(Exception("Cannot open image stream"))

            val originalBmp = BitmapFactory.decodeStream(inputStream)
                ?: return@withContext Result.failure(Exception("Failed to decode image"))

            val scaledBmp = if (scaleFactor < 0.99f) {
                val newW = (originalBmp.width * scaleFactor).toInt().coerceAtLeast(1)
                val newH = (originalBmp.height * scaleFactor).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(originalBmp, newW, newH, true)
            } else {
                originalBmp
            }

            val compressFormat = when (targetFormat.uppercase()) {
                "PNG" -> Bitmap.CompressFormat.PNG
                "WEBP" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (quality >= 100) Bitmap.CompressFormat.WEBP_LOSSLESS
                        else Bitmap.CompressFormat.WEBP_LOSSY
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.WEBP
                    }
                }
                else -> Bitmap.CompressFormat.JPEG
            }

            val outStream = ByteArrayOutputStream()
            scaledBmp.compress(compressFormat, quality.coerceIn(1, 100), outStream)
            val bytes = outStream.toByteArray()

            val finalSize = bytes.size.toLong()
            val reduction = if (originalSize > 0) {
                ((originalSize - finalSize).toDouble() / originalSize.toDouble()) * 100.0
            } else 0.0

            Result.success(
                ConversionResult(
                    outputBytes = bytes,
                    outputFormat = targetFormat,
                    originalSizeBytes = originalSize,
                    outputSizeBytes = finalSize,
                    reductionPercentage = reduction,
                    outputBitmap = scaledBmp
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
