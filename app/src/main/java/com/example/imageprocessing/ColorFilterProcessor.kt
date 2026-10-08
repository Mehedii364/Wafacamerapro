package com.example.imageprocessing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ColorFilterProcessor {

    fun applyAdjustments(
        src: Bitmap,
        brightness: Float = 0f, // -100 to 100
        contrast: Float = 0f,   // -100 to 100
        saturation: Float = 1f, // 0.0 to 2.0
        warmth: Float = 0f,     // -100 to 100
        isSepia: Boolean = false,
        isGrayscale: Boolean = false
    ): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val finalMatrix = ColorMatrix()

        if (isGrayscale) {
            val grayMatrix = ColorMatrix().apply { setSaturation(0f) }
            finalMatrix.postConcat(grayMatrix)
        } else if (isSepia) {
            val sepiaMatrix = ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,     1f, 0f
                )
            )
            finalMatrix.postConcat(sepiaMatrix)
        } else {
            // Saturation
            if (saturation != 1.0f) {
                val satMatrix = ColorMatrix().apply { setSaturation(saturation) }
                finalMatrix.postConcat(satMatrix)
            }

            // Warmth (tint red up, blue down or vice versa)
            if (warmth != 0f) {
                val warmFactor = warmth / 100f
                val warmMatrix = ColorMatrix(
                    floatArrayOf(
                        1f + (warmFactor * 0.2f), 0f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 1f - (warmFactor * 0.2f), 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                finalMatrix.postConcat(warmMatrix)
            }
        }

        // Contrast and Brightness
        val scale = if (contrast != 0f) {
            val c = (contrast + 100f) / 100f
            c * c
        } else 1.0f

        val translate = (brightness * 2.55f) + (128f * (1f - scale))
        val contrastBrightnessMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        finalMatrix.postConcat(contrastBrightnessMatrix)

        paint.colorFilter = ColorMatrixColorFilter(finalMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)

        return output
    }

    fun applyDocumentEnhance(src: Bitmap, mode: String): Bitmap {
        return when (mode) {
            "GRAYSCALE" -> applyAdjustments(src, brightness = 5f, contrast = 15f, isGrayscale = true)
            "BW_HIGH_CONTRAST" -> applyAdjustments(src, brightness = 15f, contrast = 55f, isGrayscale = true)
            "DOCUMENT_CRISP" -> applyAdjustments(src, brightness = 10f, contrast = 35f, saturation = 1.1f)
            else -> src // ORIGINAL
        }
    }
}
