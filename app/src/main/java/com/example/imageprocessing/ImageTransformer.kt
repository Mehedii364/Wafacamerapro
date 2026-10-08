package com.example.imageprocessing

import android.graphics.Bitmap
import android.graphics.Matrix

object ImageTransformer {

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun flipHorizontal(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun flipVertical(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply { postScale(1f, -1f, bitmap.width / 2f, bitmap.height / 2f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun resize(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    fun crop(bitmap: Bitmap, left: Int, top: Int, width: Int, height: Int): Bitmap {
        val safeLeft = left.coerceIn(0, bitmap.width - 1)
        val safeTop = top.coerceIn(0, bitmap.height - 1)
        val safeWidth = width.coerceIn(1, bitmap.width - safeLeft)
        val safeHeight = height.coerceIn(1, bitmap.height - safeTop)
        return Bitmap.createBitmap(bitmap, safeLeft, safeTop, safeWidth, safeHeight)
    }
}
