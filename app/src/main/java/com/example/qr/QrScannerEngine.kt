package com.example.qr

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class ScannedBarcodeResult(
    val rawValue: String,
    val displayValue: String,
    val formatName: String,
    val valueType: Int
)

object QrScannerEngine {

    suspend fun scanBitmap(bitmap: Bitmap): Result<List<ScannedBarcodeResult>> = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val results = barcodes.map { b ->
                            val format = when (b.format) {
                                Barcode.FORMAT_QR_CODE -> "QR Code"
                                Barcode.FORMAT_EAN_13 -> "EAN-13"
                                Barcode.FORMAT_EAN_8 -> "EAN-8"
                                Barcode.FORMAT_CODE_128 -> "Code 128"
                                Barcode.FORMAT_UPC_A -> "UPC-A"
                                Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
                                else -> "Barcode (${b.format})"
                            }
                            ScannedBarcodeResult(
                                rawValue = b.rawValue ?: "",
                                displayValue = b.displayValue ?: b.rawValue ?: "",
                                formatName = format,
                                valueType = b.valueType
                            )
                        }
                        continuation.resume(Result.success(results))
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(Result.failure(e))
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }
}
