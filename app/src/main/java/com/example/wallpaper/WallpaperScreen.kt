package com.example.wallpaper

import android.app.WallpaperManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun WallpaperScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var wallpaperBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var selectedStyle by remember { mutableStateOf("AMBIENT_GRADIENT") }
    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }

    fun refreshWallpaper() {
        val src = sourceBitmap ?: return
        wallpaperBitmap = WallpaperEngine.generateWallpaper(
            src = src,
            targetWidth = 1080,
            targetHeight = 2400,
            style = selectedStyle
        )
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        sourceBitmap = BitmapFactory.decodeStream(stream)
                    }
                }
                refreshWallpaper()
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
                    text = "Wallpaper Creator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Fit photos into phone aspect ratio with ambient styling",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("wallpaper_pick_button")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sourceBitmap == null) {
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
                    Icon(Icons.Default.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select a photo to create a wallpaper", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Phone Ratio Mockup Box
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(380.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(androidx.compose.ui.graphics.Color.Black)
                    .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                wallpaperBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Wallpaper Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
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
                    Text("Backdrop & Fit Style", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "AMBIENT_GRADIENT" to "Ambient Dark",
                            "FIT_CENTER" to "Fit Center",
                            "STRETCH" to "Fill Crop"
                        ).forEach { (st, label) ->
                            FilterChip(
                                selected = selectedStyle == st,
                                onClick = {
                                    selectedStyle = st
                                    refreshWallpaper()
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val bmp = wallpaperBitmap ?: return@OutlinedButton
                        scope.launch {
                            isProcessing = true
                            val res = WallpaperEngine.applyToDevice(context, bmp, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                            res.onSuccess {
                                statusText = "Wallpaper applied to Home & Lock screen!"
                                Toast.makeText(context, "Wallpaper Applied!", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                statusText = "Failed to set wallpaper: ${it.localizedMessage}"
                            }
                            isProcessing = false
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set as Wallpaper", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val bmp = wallpaperBitmap ?: return@Button
                        scope.launch {
                            isProcessing = true
                            val saveRes = MediaSaver.saveBitmapToGallery(context, bmp, "WAFA_WALLPAPER", Bitmap.CompressFormat.JPEG, 98)
                            saveRes.onSuccess {
                                statusText = "Saved wallpaper to Gallery!"
                                Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                            }
                            isProcessing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Gallery", fontSize = 12.sp)
                }
            }

            if (statusText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusText!!, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
