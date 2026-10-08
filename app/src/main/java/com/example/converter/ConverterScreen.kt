package com.example.converter

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.launch

@Composable
fun ConverterScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var conversionResult by remember { mutableStateOf<ConversionResult?>(null) }

    var targetFormat by remember { mutableStateOf("WEBP") }
    var quality by remember { mutableIntStateOf(85) }
    var scaleFactor by remember { mutableFloatStateOf(1.0f) }

    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }

    fun runConversion(uri: Uri) {
        scope.launch {
            isProcessing = true
            val res = ImageConverterEngine.convertAndCompress(
                context = context,
                inputUri = uri,
                targetFormat = targetFormat,
                quality = quality,
                scaleFactor = scaleFactor
            )
            res.onSuccess {
                conversionResult = it
            }.onFailure {
                statusText = "Conversion error: ${it.localizedMessage}"
            }
            isProcessing = false
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            runConversion(uri)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Converter & Compressor",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Convert JPEG, PNG, WebP with size reduction metrics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("converter_pick_button")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .clickable {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Compress, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select an image to convert or compress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                conversionResult?.outputBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Converted Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Size Comparison Stats Card
            conversionResult?.let { res ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Original", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.1f KB".format(res.originalSizeBytes / 1024f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Output", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.1f KB".format(res.outputSizeBytes / 1024f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Reduction", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%+.1f%%".format(res.reductionPercentage), fontWeight = FontWeight.Bold, color = if (res.reductionPercentage > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Target Format", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("WEBP", "JPEG", "PNG").forEach { fmt ->
                            FilterChip(
                                selected = targetFormat == fmt,
                                onClick = {
                                    targetFormat = fmt
                                    selectedUri?.let { runConversion(it) }
                                },
                                label = { Text(fmt) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Quality ($quality%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = quality.toFloat(),
                        onValueChange = {
                            quality = it.toInt()
                        },
                        onValueChangeFinished = {
                            selectedUri?.let { runConversion(it) }
                        },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Scale Factor (${(scaleFactor * 100).toInt()}%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.5f to "50%", 0.75f to "75%", 1.0f to "100%").forEach { (sc, label) ->
                            FilterChip(
                                selected = scaleFactor == sc,
                                onClick = {
                                    scaleFactor = sc
                                    selectedUri?.let { runConversion(it) }
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val res = conversionResult ?: return@Button
                    scope.launch {
                        val formatEnum = when (res.outputFormat) {
                            "PNG" -> android.graphics.Bitmap.CompressFormat.PNG
                            "WEBP" -> android.graphics.Bitmap.CompressFormat.WEBP
                            else -> android.graphics.Bitmap.CompressFormat.JPEG
                        }
                        val saveResult = MediaSaver.saveBitmapToGallery(
                            context = context,
                            bitmap = res.outputBitmap,
                            titlePrefix = "WAFA_CONVERTED",
                            format = formatEnum,
                            quality = quality
                        )
                        saveResult.onSuccess {
                            statusText = "Converted file saved to Gallery!"
                            Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                        }.onFailure { e ->
                            statusText = "Save failed: ${e.localizedMessage}"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_converted_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Converted File", fontWeight = FontWeight.Bold)
            }

            if (statusText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusText!!, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
