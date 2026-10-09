package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraScreen
import com.example.camera.CameraViewModel
import com.example.capabilities.CapabilitiesScreen
import com.example.collage.CollageScreen
import com.example.converter.ConverterScreen
import com.example.duplicatefinder.DuplicateFinderScreen
import com.example.editor.PhotoEditorScreen
import com.example.gallery.GalleryScreen
import com.example.privacy.PrivacyScreen
import com.example.qr.QrScannerScreen
import com.example.scanner.ScannerScreen
import com.example.ui.ToolsHubScreen
import com.example.ui.theme.WafaCameraTheme
import com.example.wallpaper.WallpaperScreen
import com.example.watermark.WatermarkScreen

class MainActivity : ComponentActivity() {

    private val cameraViewModel: CameraViewModel by viewModels()
    private var isCameraTabActive: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WafaCameraTheme(darkTheme = true) {
                MainContent(
                    cameraViewModel = cameraViewModel,
                    onTabChanged = { tab ->
                        isCameraTabActive = (tab == "camera")
                    }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isCameraTabActive && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            // Physical volume button shutter trigger
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    cameraViewModel: CameraViewModel,
    onTabChanged: (String) -> Unit
) {
    var currentTab by remember { mutableStateOf("camera") }
    var activeSubTool by remember { mutableStateOf<String?>(null) }

    val onSelectTab: (String) -> Unit = { tab ->
        currentTab = tab
        activeSubTool = null
        onTabChanged(tab)
    }

    BackHandler(enabled = activeSubTool != null) {
        activeSubTool = null
    }

    Scaffold(
        topBar = {
            if (activeSubTool != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (activeSubTool) {
                                "watermark" -> "Watermark Studio"
                                "collage" -> "Collage Maker"
                                "converter" -> "Format Converter"
                                "duplicates" -> "Duplicate Cleaner"
                                "qr" -> "QR & Barcode"
                                "wallpaper" -> "Wallpaper Creator"
                                "capabilities" -> "Hardware Diagnostics"
                                else -> "Tool"
                            },
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activeSubTool = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (activeSubTool == null) {
                NavigationBar(
                    containerColor = Color(0xFF090D16),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == "camera",
                        onClick = { onSelectTab("camera") },
                        icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(22.dp)) },
                        label = { Text("Camera", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF9EAEC1),
                            unselectedTextColor = Color(0xFF9EAEC1)
                        ),
                        modifier = Modifier.testTag("nav_camera")
                    )

                    NavigationBarItem(
                        selected = currentTab == "scanner",
                        onClick = { onSelectTab("scanner") },
                        icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "Scanner", modifier = Modifier.size(22.dp)) },
                        label = { Text("Scanner", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF9EAEC1),
                            unselectedTextColor = Color(0xFF9EAEC1)
                        ),
                        modifier = Modifier.testTag("nav_scanner")
                    )

                    NavigationBarItem(
                        selected = currentTab == "studio",
                        onClick = { onSelectTab("studio") },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "Studio", modifier = Modifier.size(22.dp)) },
                        label = { Text("Studio", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF9EAEC1),
                            unselectedTextColor = Color(0xFF9EAEC1)
                        ),
                        modifier = Modifier.testTag("nav_studio")
                    )

                    NavigationBarItem(
                        selected = currentTab == "tools",
                        onClick = { onSelectTab("tools") },
                        icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = "Tools", modifier = Modifier.size(22.dp)) },
                        label = { Text("Toolkit", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF9EAEC1),
                            unselectedTextColor = Color(0xFF9EAEC1)
                        ),
                        modifier = Modifier.testTag("nav_tools")
                    )

                    NavigationBarItem(
                        selected = currentTab == "gallery",
                        onClick = { onSelectTab("gallery") },
                        icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.size(22.dp)) },
                        label = { Text("Gallery", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF9EAEC1),
                            unselectedTextColor = Color(0xFF9EAEC1)
                        ),
                        modifier = Modifier.testTag("nav_gallery")
                    )

                    NavigationBarItem(
                        selected = currentTab == "privacy",
                        onClick = { onSelectTab("privacy") },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Privacy", modifier = Modifier.size(22.dp)) },
                        label = { Text("Privacy", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF9EAEC1),
                            unselectedTextColor = Color(0xFF9EAEC1)
                        ),
                        modifier = Modifier.testTag("nav_privacy")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeSubTool != null) {
                when (activeSubTool) {
                    "watermark" -> WatermarkScreen()
                    "collage" -> CollageScreen()
                    "converter" -> ConverterScreen()
                    "duplicates" -> DuplicateFinderScreen()
                    "qr" -> QrScannerScreen()
                    "wallpaper" -> WallpaperScreen()
                    "capabilities" -> CapabilitiesScreen()
                    "rearrange_modes" -> {
                        activeSubTool = null
                        onSelectTab("camera")
                        cameraViewModel.openRearrangeModes()
                    }
                }
            } else {
                when (currentTab) {
                    "camera" -> CameraScreen(
                        viewModel = cameraViewModel,
                        onNavigateToGallery = { onSelectTab("gallery") }
                    )
                    "scanner" -> ScannerScreen()
                    "studio" -> PhotoEditorScreen()
                    "tools" -> ToolsHubScreen(onSelectTool = { toolId -> activeSubTool = toolId })
                    "gallery" -> GalleryScreen(onNavigateBack = { onSelectTab("camera") })
                    "privacy" -> PrivacyScreen()
                }
            }
        }
    }
}
