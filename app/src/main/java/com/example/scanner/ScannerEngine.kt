package com.example.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

data class ScannedPage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val originalBitmap: Bitmap,
    val processedBitmap: Bitmap,
    val filterMode: String = "ORIGINAL", // ORIGINAL, GRAYSCALE, BW_HIGH_CONTRAST, DOCUMENT_CRISP
    val recognizedText: String = ""
)

object ScannerEngine {

    suspend fun recognizeText(bitmap: Bitmap): Result<String> = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(Result.success(visionText.text))
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(Result.failure(e))
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    suspend fun generatePdf(pages: List<ScannedPage>): Result<ByteArray> = withContext(Dispatchers.IO) {
        if (pages.isEmpty()) return@withContext Result.failure(Exception("No pages to generate PDF"))

        val document = PdfDocument()
        try {
            pages.forEachIndexed { index, page ->
                val bmp = page.processedBitmap
                // Standard A4 aspect: 595 x 842 points
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
                val pdfPage = document.startPage(pageInfo)
                val canvas: Canvas = pdfPage.canvas

                // Scale image to fit within A4 page margins
                val margin = 20f
                val maxWidth = 595f - (margin * 2)
                val maxHeight = 842f - (margin * 2)

                val scale = minOf(maxWidth / bmp.width, maxHeight / bmp.height)
                val destWidth = bmp.width * scale
                val destHeight = bmp.height * scale
                val left = margin + (maxWidth - destWidth) / 2f
                val top = margin + (maxHeight - destHeight) / 2f

                val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                canvas.drawColor(Color.WHITE)
                val scaledBmp = Bitmap.createScaledBitmap(bmp, destWidth.toInt().coerceAtLeast(1), destHeight.toInt().coerceAtLeast(1), true)
                canvas.drawBitmap(scaledBmp, left, top, paint)

                document.finishPage(pdfPage)
            }

            val outputStream = ByteArrayOutputStream()
            document.writeTo(outputStream)
            document.close()
            Result.success(outputStream.toByteArray())
        } catch (e: Exception) {
            document.close()
            Result.failure(e)
        }
    }
}
