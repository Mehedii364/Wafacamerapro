package com.example.collage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CollageScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedBitmaps = remember { mutableStateListOf<Bitmap>() }
    var collageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var currentLayout by remember { mutableStateOf(CollageLayout.GRID_2X2) }
    var spacing by remember { mutableFloatStateOf(16f) }
    var backgroundColor by remember { mutableStateOf(AndroidColor.BLACK) }
    var statusText by remember { mutableStateOf<String?>(null) }

    fun refreshCollage() {
        if (selectedBitmaps.isEmpty()) return
        collageBitmap = CollageRenderer.renderCollage(
            bitmaps = selectedBitmaps,
            layout = currentLayout,
            spacing = spacing,
            backgroundColor = backgroundColor
        )
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    selectedBitmaps.clear()
                    uris.forEach { uri ->
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)?.let { selectedBitmaps.add(it) }
                        }
                    }
                }
                refreshCollage()
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Collage Maker",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Multi-photo grids, split sheets & contact albums",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("collage_pick_button")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedBitmaps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .clickable {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select 2 to 9 photos to assemble a collage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                collageBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Collage Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Layout Style", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            CollageLayout.GRID_2X2 to "2x2 Grid",
                            CollageLayout.SPLIT_HORIZONTAL to "Split H",
                            CollageLayout.SPLIT_VERTICAL to "Split V",
                            CollageLayout.TRIPLE_HERO to "Triple Hero",
                            CollageLayout.CONTACT_SHEET to "Contact Sheet"
                        ).forEach { (layout, label) ->
                            FilterChip(
                                selected = currentLayout == layout,
                                onClick = {
                                    currentLayout = layout
                                    refreshCollage()
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Border Spacing (${spacing.toInt()}dp)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = spacing,
                        onValueChange = {
                            spacing = it
                            refreshCollage()
                        },
                        valueRange = 0f..40f,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Background Tone", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AndroidColor.BLACK to "Obsidian Black",
                            AndroidColor.WHITE to "Studio White",
                            AndroidColor.rgb(18, 25, 38) to "Midnight Navy"
                        ).forEach { (color, label) ->
                            FilterChip(
                                selected = backgroundColor == color,
                                onClick = {
                                    backgroundColor = color
                                    refreshCollage()
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalBmp = collageBitmap ?: return@Button
                    scope.launch {
                        val saveResult = MediaSaver.saveBitmapToGallery(
                            context = context,
                            bitmap = finalBmp,
                            titlePrefix = "WAFA_COLLAGE",
                            format = Bitmap.CompressFormat.JPEG,
                            quality = 95
                        )
                        saveResult.onSuccess {
                            statusText = "Collage saved to Gallery!"
                            Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                        }.onFailure { e ->
                            statusText = "Save failed: ${e.localizedMessage}"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_collage_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Collage to Gallery", fontWeight = FontWeight.Bold)
            }

            if (statusText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusText!!, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
