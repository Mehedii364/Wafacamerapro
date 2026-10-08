package com.example.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperEngine {

    fun generateWallpaper(
        src: Bitmap,
        targetWidth: Int = 1080,
        targetHeight: Int = 2400,
        style: String = "AMBIENT_GRADIENT" // "AMBIENT_GRADIENT", "FIT_CENTER", "STRETCH"
    ): Bitmap {
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        when (style) {
            "AMBIENT_GRADIENT" -> {
                // Draw a sleek dark luxury ambient gradient
                val gradient = LinearGradient(
                    0f, 0f, 0f, targetHeight.toFloat(),
                    intArrayOf(Color.rgb(11, 15, 25), Color.rgb(20, 30, 48), Color.rgb(11, 15, 25)),
                    null,
                    Shader.TileMode.CLAMP
                )
                val bgPaint = Paint().apply { shader = gradient }
                canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)

                // Fit image in center with subtle shadow and rounded corners
                val aspect = src.width.toFloat() / src.height.toFloat()
                val targetAspect = targetWidth.toFloat() / targetHeight.toFloat()

                val drawW: Float
                val drawH: Float
                if (aspect > targetAspect) {
                    drawW = targetWidth.toFloat() * 0.92f
                    drawH = drawW / aspect
                } else {
                    drawH = targetHeight.toFloat() * 0.75f
                    drawW = drawH * aspect
                }

                val left = (targetWidth - drawW) / 2f
                val top = (targetHeight - drawH) / 2f
                val destRect = RectF(left, top, left + drawW, top + drawH)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(src, null, destRect, paint)
            }
            "FIT_CENTER" -> {
                canvas.drawColor(Color.BLACK)
                val aspect = src.width.toFloat() / src.height.toFloat()
                val targetAspect = targetWidth.toFloat() / targetHeight.toFloat()
                val drawW: Float
                val drawH: Float
                if (aspect > targetAspect) {
                    drawW = targetWidth.toFloat()
                    drawH = drawW / aspect
                } else {
                    drawH = targetHeight.toFloat()
                    drawW = drawH * aspect
                }
                val left = (targetWidth - drawW) / 2f
                val top = (targetHeight - drawH) / 2f
                canvas.drawBitmap(src, null, RectF(left, top, left + drawW, top + drawH), Paint(Paint.FILTER_BITMAP_FLAG))
            }
            else -> { // Fill crop
                val srcAspect = src.width.toFloat() / src.height.toFloat()
                val destAspect = targetWidth.toFloat() / targetHeight.toFloat()
                val srcRect = if (srcAspect > destAspect) {
                    val w = (src.height * destAspect).toInt()
                    val l = (src.width - w) / 2
                    Rect(l, 0, l + w, src.height)
                } else {
                    val h = (src.width / destAspect).toInt()
                    val t = (src.height - h) / 2
                    Rect(0, t, src.width, t + h)
                }
                canvas.drawBitmap(src, srcRect, Rect(0, 0, targetWidth, targetHeight), Paint(Paint.FILTER_BITMAP_FLAG))
            }
        }

        return result
    }

    suspend fun applyToDevice(
        context: Context,
        bitmap: Bitmap,
        which: Int = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val wm = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wm.setBitmap(bitmap, null, true, which)
            } else {
                wm.setBitmap(bitmap)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
