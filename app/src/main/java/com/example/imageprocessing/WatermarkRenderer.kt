package com.example.imageprocessing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WatermarkRenderer {

    fun applyWatermark(
        src: Bitmap,
        text: String,
        position: String = "BOTTOM_RIGHT",
        textColor: Int = Color.WHITE,
        textSizeSp: Float = 22f,
        alpha: Float = 0.9f,
        includeTimestamp: Boolean = true
    ): Bitmap {
        val result = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val timestamp = if (includeTimestamp) {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        } else ""

        val displayText = buildString {
            if (text.isNotBlank()) append(text)
            if (text.isNotBlank() && timestamp.isNotBlank()) append(" • ")
            if (timestamp.isNotBlank()) append(timestamp)
        }

        if (displayText.isBlank()) return result

        val scaleFactor = (result.width / 1000f).coerceAtLeast(1.0f)
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            textSize = textSizeSp * scaleFactor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setShadowLayer(4f * scaleFactor, 2f * scaleFactor, 2f * scaleFactor, Color.BLACK)
        }

        val textBounds = Rect()
        paintText.getTextBounds(displayText, 0, displayText.length, textBounds)

        val padding = 24f * scaleFactor
        val bgPadding = 12f * scaleFactor

        val textWidth = textBounds.width().toFloat()
        val textHeight = textBounds.height().toFloat()

        val x: Float
        val y: Float

        when (position) {
            "TOP_LEFT" -> {
                x = padding
                y = padding + textHeight
            }
            "TOP_RIGHT" -> {
                x = result.width - textWidth - padding
                y = padding + textHeight
            }
            "BOTTOM_LEFT" -> {
                x = padding
                y = result.height - padding
            }
            "CENTER" -> {
                x = (result.width - textWidth) / 2f
                y = (result.height + textHeight) / 2f
            }
            else -> { // BOTTOM_RIGHT
                x = result.width - textWidth - padding
                y = result.height - padding
            }
        }

        // Draw translucent dark pill behind watermark for readability
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 10, 15, 25)
            style = Paint.Style.FILL
        }
        val bgRect = RectF(
            x - bgPadding,
            y - textHeight - bgPadding,
            x + textWidth + bgPadding,
            y + bgPadding
        )
        canvas.drawRoundRect(bgRect, 8f * scaleFactor, 8f * scaleFactor, bgPaint)

        // Draw text
        canvas.drawText(displayText, x, y, paintText)

        return result
    }
}
