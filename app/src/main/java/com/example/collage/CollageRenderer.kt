package com.example.collage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

enum class CollageLayout {
    SPLIT_HORIZONTAL, // 2 items side-by-side
    SPLIT_VERTICAL,   // 2 items top & bottom
    GRID_2X2,         // 4 items
    TRIPLE_HERO,      // 1 hero left, 2 stacked right
    CONTACT_SHEET     // Multi-item contact sheet
}

object CollageRenderer {

    fun renderCollage(
        bitmaps: List<Bitmap>,
        layout: CollageLayout,
        outputWidth: Int = 1800,
        outputHeight: Int = 1800,
        spacing: Float = 16f,
        backgroundColor: Int = Color.BLACK
    ): Bitmap {
        if (bitmaps.isEmpty()) {
            val empty = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
            Canvas(empty).drawColor(backgroundColor)
            return empty
        }

        val result = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(backgroundColor)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        when (layout) {
            CollageLayout.SPLIT_HORIZONTAL -> {
                val count = minOf(2, bitmaps.size)
                val w = (outputWidth - spacing * (count + 1)) / count
                val h = outputHeight - spacing * 2
                bitmaps.take(count).forEachIndexed { i, bmp ->
                    val left = spacing + i * (w + spacing)
                    val top = spacing
                    drawBitmapCover(canvas, bmp, RectF(left, top, left + w, top + h), paint)
                }
            }
            CollageLayout.SPLIT_VERTICAL -> {
                val count = minOf(2, bitmaps.size)
                val w = outputWidth - spacing * 2
                val h = (outputHeight - spacing * (count + 1)) / count
                bitmaps.take(count).forEachIndexed { i, bmp ->
                    val left = spacing
                    val top = spacing + i * (h + spacing)
                    drawBitmapCover(canvas, bmp, RectF(left, top, left + w, top + h), paint)
                }
            }
            CollageLayout.GRID_2X2 -> {
                val w = (outputWidth - spacing * 3) / 2
                val h = (outputHeight - spacing * 3) / 2
                val slots = listOf(
                    RectF(spacing, spacing, spacing + w, spacing + h),
                    RectF(spacing * 2 + w, spacing, outputWidth - spacing, spacing + h),
                    RectF(spacing, spacing * 2 + h, spacing + w, outputHeight - spacing),
                    RectF(spacing * 2 + w, spacing * 2 + h, outputWidth - spacing, outputHeight - spacing)
                )
                bitmaps.take(4).forEachIndexed { i, bmp ->
                    drawBitmapCover(canvas, bmp, slots[i], paint)
                }
            }
            CollageLayout.TRIPLE_HERO -> {
                val halfW = (outputWidth - spacing * 3) / 2
                val leftRect = RectF(spacing, spacing, spacing + halfW, outputHeight - spacing)
                val halfH = (outputHeight - spacing * 3) / 2
                val rightTop = RectF(spacing * 2 + halfW, spacing, outputWidth - spacing, spacing + halfH)
                val rightBottom = RectF(spacing * 2 + halfW, spacing * 2 + halfH, outputWidth - spacing, outputHeight - spacing)

                if (bitmaps.isNotEmpty()) drawBitmapCover(canvas, bitmaps[0], leftRect, paint)
                if (bitmaps.size > 1) drawBitmapCover(canvas, bitmaps[1], rightTop, paint)
                if (bitmaps.size > 2) drawBitmapCover(canvas, bitmaps[2], rightBottom, paint)
            }
            CollageLayout.CONTACT_SHEET -> {
                val cols = 3
                val rows = ((bitmaps.size + cols - 1) / cols).coerceAtLeast(1)
                val cellW = (outputWidth - spacing * (cols + 1)) / cols
                val cellH = (outputHeight - spacing * (rows + 1)) / rows
                bitmaps.forEachIndexed { i, bmp ->
                    val r = i / cols
                    val c = i % cols
                    val left = spacing + c * (cellW + spacing)
                    val top = spacing + r * (cellH + spacing)
                    drawBitmapCover(canvas, bmp, RectF(left, top, left + cellW, top + cellH), paint)
                }
            }
        }

        return result
    }

    private fun drawBitmapCover(canvas: Canvas, bmp: Bitmap, dest: RectF, paint: Paint) {
        val srcRatio = bmp.width.toFloat() / bmp.height.toFloat()
        val destRatio = dest.width() / dest.height()

        val srcRect = if (srcRatio > destRatio) {
            // Source is wider than destination: crop sides
            val cropWidth = (bmp.height * destRatio).toInt()
            val left = (bmp.width - cropWidth) / 2
            Rect(left, 0, left + cropWidth, bmp.height)
        } else {
            // Source is taller than destination: crop top/bottom
            val cropHeight = (bmp.width / destRatio).toInt()
            val top = (bmp.height - cropHeight) / 2
            Rect(0, top, bmp.width, top + cropHeight)
        }

        canvas.drawBitmap(bmp, srcRect, dest, paint)
    }
}
