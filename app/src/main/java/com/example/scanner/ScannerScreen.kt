package com.example.scanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.storage.MediaSaver
import com.example.imageprocessing.ColorFilterProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScannerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pages = remember { mutableStateListOf<ScannedPage>() }
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var recognizedOcrText by remember { mutableStateOf<String?>(null) }

    // Visual media picker for document pages
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                isProcessing = true
                withContext(Dispatchers.IO) {
                    uris.forEach { uri ->
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            if (bmp != null) {
                                val processed = ColorFilterProcessor.applyDocumentEnhance(bmp, "DOCUMENT_CRISP")
                                pages.add(
                                    ScannedPage(
                                        originalBitmap = bmp,
                                        processedBitmap = processed,
                                        filterMode = "DOCUMENT_CRISP"
                                    )
                                )
                            }
                        }
                    }
                }
                isProcessing = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Smart Document Scanner",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "High-contrast scanning, on-device OCR & PDF export",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("add_document_page_button")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Page")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pages.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .clickable {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No pages added yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to import document images to scan & enhance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Active Page Preview
            val currentPage = pages.getOrNull(selectedPageIndex) ?: pages[0]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = currentPage.processedBitmap.asImageBitmap(),
                    contentDescription = "Page Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )

                // Delete Page Button
                IconButton(
                    onClick = {
                        pages.removeAt(selectedPageIndex)
                        if (selectedPageIndex >= pages.size && pages.isNotEmpty()) {
                            selectedPageIndex = pages.size - 1
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-Page Thumbnails Strip
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(pages) { index, page ->
                    val isSelected = index == selectedPageIndex
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedPageIndex = index }
                    ) {
                        Image(
                            bitmap = page.processedBitmap.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "${index + 1}",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f), RoundedCornerShape(topStart = 4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Selector
            Text(
                text = "Document Filters",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ORIGINAL", "DOCUMENT_CRISP", "GRAYSCALE", "BW_HIGH_CONTRAST").forEach { filter ->
                    val label = when (filter) {
                        "ORIGINAL" -> "Original"
                        "DOCUMENT_CRISP" -> "Crisp"
                        "GRAYSCALE" -> "Grayscale"
                        else -> "B&W High"
                    }
                    val isSelected = currentPage.filterMode == filter
                    Button(
                        onClick = {
                            val enhanced = ColorFilterProcessor.applyDocumentEnhance(currentPage.originalBitmap, filter)
                            pages[selectedPageIndex] = currentPage.copy(
                                processedBitmap = enhanced,
                                filterMode = filter
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: OCR & Export PDF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            statusText = "Running OCR on page ${selectedPageIndex + 1}..."
                            val ocrResult = ScannerEngine.recognizeText(currentPage.processedBitmap)
                            ocrResult.onSuccess { text ->
                                recognizedOcrText = text
                                statusText = if (text.isBlank()) "No text detected." else "Text recognized successfully!"
                            }.onFailure { err ->
                                statusText = "OCR failed: ${err.localizedMessage}"
                            }
                            isProcessing = false
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("scanner_ocr_button")
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Read OCR")
                }

                Button(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            statusText = "Generating PDF document (${pages.size} pages)..."
                            val pdfResult = ScannerEngine.generatePdf(pages)
                            pdfResult.onSuccess { bytes ->
                                val saveResult = MediaSaver.savePdfToDocuments(context, bytes, "WafaScan_${System.currentTimeMillis()}")
                                saveResult.onSuccess {
                                    statusText = "PDF saved to Documents folder!"
                                    Toast.makeText(context, "PDF saved to Documents!", Toast.LENGTH_SHORT).show()
                                }.onFailure { e ->
                                    statusText = "Failed to save PDF: ${e.localizedMessage}"
                                }
                            }.onFailure { e ->
                                statusText = "PDF generation failed: ${e.localizedMessage}"
                            }
                            isProcessing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).testTag("scanner_export_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF")
                }
            }

            // Status message / progress indicator
            if (isProcessing) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(statusText ?: "Processing...", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
            } else if (statusText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusText!!, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            }

            // OCR Result Box
            if (!recognizedOcrText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recognized Text (OCR)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("Scanned Text", recognizedOcrText))
                                    Toast.makeText(context, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = recognizedOcrText!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
