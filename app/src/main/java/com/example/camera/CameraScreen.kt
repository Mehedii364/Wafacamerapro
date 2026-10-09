package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import java.nio.ByteBuffer

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()

    val activity = context as? Activity
    val originalBrightness = remember {
        activity?.window?.attributes?.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }

    val setWindowBrightness: (Float) -> Unit = { brightness ->
        activity?.let { act ->
            val lp = act.window.attributes
            lp.screenBrightness = brightness
            act.window.attributes = lp
        }
    }

    val restoreWindowBrightness: () -> Unit = {
        activity?.let { act ->
            val lp = act.window.attributes
            lp.screenBrightness = originalBrightness
            act.window.attributes = lp
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF090D16))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Wafa Camera Pro needs access to your camera to preview and capture professional high-resolution photos and videos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9EAEC1),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("grant_permission_button")
                ) {
                    Text("Grant Permission", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    if (uiState.isRearrangeModesVisible) {
        BackHandler {
            viewModel.closeRearrangeModes()
        }
        RearrangeModesScreen(
            currentPinnedModes = uiState.pinnedModes,
            onSavePinnedModes = { viewModel.updatePinnedModes(it) },
            onSelectModeAndClose = { viewModel.setCaptureMode(it) },
            onBack = { viewModel.closeRearrangeModes() }
        )
        return
    }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    var showExposureSlider by remember { mutableStateOf(false) }
    var showQuickSettingsSheet by remember { mutableStateOf(false) }

    // Bind Camera lifecycle
    LaunchedEffect(uiState.lensFacing, uiState.captureMode) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(uiState.lensFacing)
                    .build()

                val preview = Preview.Builder().build().also {
                    previewView?.let { pv -> it.setSurfaceProvider(pv.surfaceProvider) }
                }

                val flashModeToUse = if (uiState.lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    ImageCapture.FLASH_MODE_OFF
                } else {
                    when (uiState.flashSetting) {
                        FlashSetting.ON -> ImageCapture.FLASH_MODE_ON
                        FlashSetting.AUTO -> ImageCapture.FLASH_MODE_AUTO
                        else -> ImageCapture.FLASH_MODE_OFF
                    }
                }

                val imgCap = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(flashModeToUse)
                    .build()
                imageCapture = imgCap

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                    .build()
                val vidCap = VideoCapture.withOutput(recorder)
                videoCapture = vidCap

                val camera = if (uiState.captureMode == CaptureMode.VIDEO) {
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, vidCap)
                } else {
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imgCap)
                }
                activeCamera = camera

                camera.cameraInfo.zoomState.value?.let { zs ->
                    viewModel.updateZoomBounds(zs.minZoomRatio, zs.maxZoomRatio)
                }
                val expState = camera.cameraInfo.exposureState
                if (expState.isExposureCompensationSupported) {
                    viewModel.updateExposureBounds(
                        expState.exposureCompensationRange.lower,
                        expState.exposureCompensationRange.upper,
                        expState.exposureCompensationStep.toFloat()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Torch control for rear camera
    LaunchedEffect(uiState.flashSetting, activeCamera, uiState.lensFacing) {
        if (uiState.lensFacing == CameraSelector.LENS_FACING_BACK) {
            activeCamera?.cameraControl?.enableTorch(uiState.flashSetting == FlashSetting.TORCH)
        } else {
            activeCamera?.cameraControl?.enableTorch(false)
        }
    }

    // Zoom ratio control
    LaunchedEffect(uiState.zoomRatio, activeCamera) {
        activeCamera?.cameraControl?.setZoomRatio(uiState.zoomRatio)
    }

    // Exposure compensation control
    LaunchedEffect(uiState.exposureIndex, activeCamera) {
        activeCamera?.cameraControl?.setExposureCompensationIndex(uiState.exposureIndex)
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            restoreWindowBrightness()
        }
    }

    val isFrontCamera = uiState.lensFacing == CameraSelector.LENS_FACING_FRONT

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Live Camera Preview View with Pinch-to-Zoom & Tap-to-Focus
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom != 1.0f) {
                            val newZoom = (uiState.zoomRatio * zoom).coerceIn(uiState.minZoomRatio, uiState.maxZoomRatio)
                            viewModel.setZoom(newZoom)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleDoubleTapZoom()
                        },
                        onTap = { offset ->
                            previewView?.let { pv ->
                                val factory = pv.meteringPointFactory
                                val point = factory.createPoint(offset.x, offset.y)
                                val action = FocusMeteringAction.Builder(point).build()
                                activeCamera?.cameraControl?.startFocusAndMetering(action)
                                viewModel.triggerFocusRing(offset.x, offset.y)
                            }
                        }
                    )
                },
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    previewView = this
                }
            }
        )

        // 2. Viewfinder Grid Overlay
        if (uiState.gridType != GridType.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeColor = Color.White.copy(alpha = 0.35f)
                val w = size.width
                val h = size.height

                when (uiState.gridType) {
                    GridType.RULE_OF_THIRDS -> {
                        drawLine(strokeColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.dp.toPx())
                        drawLine(strokeColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth = 1.dp.toPx())
                        drawLine(strokeColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.dp.toPx())
                        drawLine(strokeColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth = 1.dp.toPx())
                    }
                    GridType.GOLDEN_RATIO -> {
                        val phi = 0.618f
                        val invPhi = 1f - phi
                        drawLine(strokeColor, Offset(w * invPhi, 0f), Offset(w * invPhi, h), strokeWidth = 1.dp.toPx())
                        drawLine(strokeColor, Offset(w * phi, 0f), Offset(w * phi, h), strokeWidth = 1.dp.toPx())
                        drawLine(strokeColor, Offset(0f, h * invPhi), Offset(w, h * invPhi), strokeWidth = 1.dp.toPx())
                        drawLine(strokeColor, Offset(0f, h * phi), Offset(w, h * phi), strokeWidth = 1.dp.toPx())
                    }
                    GridType.CENTER_CROSS -> {
                        val crossLen = 32.dp.toPx()
                        drawLine(strokeColor, Offset(w / 2f - crossLen, h / 2f), Offset(w / 2f + crossLen, h / 2f), strokeWidth = 2.dp.toPx())
                        drawLine(strokeColor, Offset(w / 2f, h / 2f - crossLen), Offset(w / 2f, h / 2f + crossLen), strokeWidth = 2.dp.toPx())
                    }
                    GridType.NONE -> {}
                }
            }
        }

        // 3. Focus Ring Animation
        if (uiState.isFocusRingVisible && uiState.focusRingPosition != null) {
            val (fx, fy) = uiState.focusRingPosition!!
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 36.dp.toPx(),
                    center = Offset(fx, fy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 4. SMART FRONT SCREEN FLASH ILLUMINATION OVERLAY
        if (uiState.isScreenFlashActive) {
            val flashColor = when (uiState.screenFlashTone) {
                ScreenFlashTone.NEUTRAL_WHITE -> Color(0xFFFFFDF8)
                ScreenFlashTone.WARM_SOFT -> Color(0xFFFFF4E5)
                ScreenFlashTone.COOL_BRIGHT -> Color(0xFFF0F8FF)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(flashColor)
                    .clickable {
                        viewModel.dismissScreenFlash(restoreWindowBrightness)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = null,
                        tint = Color(0xFF2C3E50),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⚡ Display Screen Flash Active",
                        color = Color(0xFF1E2638),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Illuminating face for balanced soft selfie exposure",
                        color = Color(0xFF5A6B82),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131926).copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "Tap to cancel",
                            color = Color(0xFF334155),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. GCam Top HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Button
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.cycleFlash()
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .size(42.dp)
                    .testTag("flash_button")
            ) {
                if (isFrontCamera) {
                    Icon(
                        imageVector = when (uiState.screenFlashMode) {
                            ScreenFlashMode.AUTO -> Icons.Default.FlashAuto
                            ScreenFlashMode.ON -> Icons.Default.LightMode
                            ScreenFlashMode.OFF -> Icons.Default.FlashOff
                        },
                        contentDescription = "Screen Flash",
                        tint = if (uiState.screenFlashMode != ScreenFlashMode.OFF) Color(0xFFFFB300) else Color.White
                    )
                } else {
                    Icon(
                        imageVector = when (uiState.flashSetting) {
                            FlashSetting.AUTO -> Icons.Default.FlashAuto
                            FlashSetting.ON -> Icons.Default.FlashOn
                            FlashSetting.TORCH -> Icons.Default.Highlight
                            FlashSetting.OFF -> Icons.Default.FlashOff
                        },
                        contentDescription = "Flash Setting",
                        tint = if (uiState.flashSetting != FlashSetting.OFF) Color(0xFFFFB300) else Color.White
                    )
                }
            }

            // Quick Settings Pull-down Pill (GCam Style)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2638)),
                modifier = Modifier.clickable {
                    showQuickSettingsSheet = !showQuickSettingsSheet
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Settings",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (uiState.captureMode) {
                            CaptureMode.PORTRAIT -> "Portrait Bokeh"
                            CaptureMode.NIGHT -> "Night Sight"
                            CaptureMode.HDR -> "HDR Mode"
                            CaptureMode.PRO -> "Pro Mode"
                            CaptureMode.VIDEO -> "Video"
                            else -> "Auto Photo"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // EV / Exposure Button
            IconButton(
                onClick = {
                    showExposureSlider = !showExposureSlider
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .size(42.dp)
                    .testTag("exposure_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness6,
                    contentDescription = "Exposure",
                    tint = if (showExposureSlider || uiState.exposureIndex != 0) Color(0xFF00E5FF) else Color.White
                )
            }
        }

        // 6. Quick Settings Dropdown Overlay (GCam Style)
        if (showQuickSettingsSheet) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF090D16).copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2638)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 92.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Settings", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Timer Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Timer", color = Color.White, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0 to "Off", 3 to "3s", 5 to "5s", 10 to "10s").forEach { (sec, label) ->
                                val sel = uiState.timerSeconds == sec
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sel) Color(0xFF00E5FF) else Color(0xFF1E2638),
                                    modifier = Modifier.clickable { viewModel.setTimer(sec) }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (sel) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Aspect Ratio Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ratio", color = Color.White, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                AspectRatioSetting.RATIO_4_3 to "4:3",
                                AspectRatioSetting.RATIO_16_9 to "16:9",
                                AspectRatioSetting.RATIO_1_1 to "1:1"
                            ).forEach { (ratio, label) ->
                                val sel = uiState.aspectRatioSetting == ratio
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sel) Color(0xFF00E5FF) else Color(0xFF1E2638),
                                    modifier = Modifier.clickable { viewModel.setAspectRatio(ratio) }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (sel) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid Overlay Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grid", color = Color.White, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                GridType.NONE to "Off",
                                GridType.RULE_OF_THIRDS to "3x3",
                                GridType.GOLDEN_RATIO to "Phi"
                            ).forEach { (grid, label) ->
                                val sel = uiState.gridType == grid
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sel) Color(0xFF00E5FF) else Color(0xFF1E2638),
                                    modifier = Modifier.clickable { viewModel.cycleGrid() }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (sel) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Computational RAW & Watermark Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.rawCaptureEnabled) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF1E2638),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.rawCaptureEnabled) Color(0xFF00E5FF) else Color.Transparent),
                            modifier = Modifier.clickable { viewModel.toggleRawCapture() }
                        ) {
                            Text(
                                text = if (uiState.rawCaptureEnabled) "RAW+JPEG ON" else "RAW OFF",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.watermarkEnabled) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF1E2638),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.watermarkEnabled) Color(0xFF00E5FF) else Color.Transparent),
                            modifier = Modifier.clickable { viewModel.toggleWatermark() }
                        ) {
                            Text(
                                text = if (uiState.watermarkEnabled) "Watermark ON" else "Watermark OFF",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Live Exposure (EV) Slider & AE Lock Panel
        if (showExposureSlider) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .background(Color(0xFF090D16).copy(alpha = 0.9f), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val evVal = uiState.exposureIndex * uiState.exposureStep
                    Text(
                        text = "EV: %+.1f".format(evVal),
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = uiState.exposureIndex.toFloat(),
                        onValueChange = { viewModel.setExposureIndex(it.toInt()) },
                        valueRange = uiState.minExposureIndex.toFloat()..uiState.maxExposureIndex.toFloat(),
                        steps = (uiState.maxExposureIndex - uiState.minExposureIndex).coerceAtLeast(1) - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        ),
                        modifier = Modifier
                            .height(170.dp)
                            .width(36.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset EV
                    IconButton(
                        onClick = { viewModel.resetExposureToZero() },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF1E2638), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset EV",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // AE Lock
                    IconButton(
                        onClick = { viewModel.toggleAeLock() },
                        modifier = Modifier
                            .size(34.dp)
                            .background(if (uiState.isAeLocked) Color(0xFFFFB300) else Color(0xFF1E2638), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (uiState.isAeLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "AE Lock",
                            tint = if (uiState.isAeLocked) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 8. ADVANCED ZOOM SYSTEM: GCam Preset Pills & Continuous Draggable Slider
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Optional continuous zoom slider
            if (uiState.isZoomSliderVisible) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("%.1fx".format(uiState.zoomRatio), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = uiState.zoomRatio,
                            onValueChange = { viewModel.setZoom(it) },
                            valueRange = uiState.minZoomRatio..uiState.maxZoomRatio,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E5FF),
                                activeTrackColor = Color(0xFF00E5FF)
                            ),
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }

            // Quick Zoom Presets (0.5x, 1x, 2x, 3x, 5x, 10x, 15x depending on genuine hardware range)
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val candidatePresets = listOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f, 10.0f, 15.0f)
                val validPresets = candidatePresets.filter { it in uiState.minZoomRatio..uiState.maxZoomRatio }
                    .ifEmpty { listOf(1.0f) }

                validPresets.forEach { preset ->
                    val isSelected = (uiState.zoomRatio >= preset - 0.2f && uiState.zoomRatio <= preset + 0.2f)
                    val label = if (preset == 0.5f) ".5" else "${preset.toInt()}"

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF00E5FF) else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setZoom(preset)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${label}x",
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }

                // Slider Toggle Icon
                IconButton(
                    onClick = { viewModel.toggleZoomSlider() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Zoom Slider",
                        tint = if (uiState.isZoomSliderVisible) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 9. Status Message Toast
        AnimatedVisibility(
            visible = uiState.statusMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF131926).copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = uiState.statusMessage ?: "",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // 10. Countdown Timer Overlay
        if (uiState.isCountingDown) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { viewModel.cancelCountdown() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.countdownRemaining}",
                        color = Color(0xFF00E5FF),
                        fontSize = 92.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Tap anywhere to cancel",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 11. Video Recording Duration Pill
        if (uiState.isRecordingVideo) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp)
                    .background(Color.Red.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val minutes = uiState.videoDurationSeconds / 60
                val seconds = uiState.videoDurationSeconds % 60
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Mode Specific HUD Overlays
        val modeHudText = when (uiState.captureMode) {
            CaptureMode.NIGHT -> "🌙 Night Sight • Multi-Frame Stacking"
            CaptureMode.HI_RES -> "⊞ 50MP Full Sensor • High-Res Unbinned"
            CaptureMode.PANO -> "↔ Panorama Horizon • Keep Level & Pan Slowly"
            CaptureMode.MACRO -> "🌷 Super Macro Focus • 4-10cm Close Range"
            CaptureMode.SLO_MO -> "⚡ Slo-Mo • 120 FPS High-Speed Video"
            CaptureMode.TIME_LAPSE -> "⏱ Time-Lapse • ${uiState.timeLapseIntervalSeconds}s Interval (30x Speed)"
            CaptureMode.DUAL_VIDEO -> "📹 View Video • Dual Front & Rear Preview"
            CaptureMode.UNDERWATER -> "🌊 Underwater • Volume Keys Shutter"
            CaptureMode.STICKER -> "✨ Dynamic Live Watermark Stamp Active"
            CaptureMode.DOC_SCANNER -> "📄 Doc Scanner • Align Document Corners"
            CaptureMode.PORTRAIT -> "👤 Portrait Bokeh • Depth Simulation Active"
            CaptureMode.PRO -> "🎛 Pro Manual Control Active"
            CaptureMode.HDR -> "🔆 HDR Style • Dynamic Range Enhancement"
            else -> null
        }

        if (modeHudText != null && !uiState.isRecordingVideo) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131926).copy(alpha = 0.90f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 92.dp)
            ) {
                Text(
                    text = modeHudText,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Panorama Horizon Guide
        if (uiState.captureMode == CaptureMode.PANO) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.85f)
                    .height(2.dp)
                    .background(Color(0xFF00E5FF).copy(alpha = 0.7f))
            )
        }

        // Doc Scanner Corner Guides
        if (uiState.captureMode == CaptureMode.DOC_SCANNER) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(260.dp, 340.dp)
                    .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.75f), RoundedCornerShape(14.dp))
            )
        }

        // Dual Video PiP Frame
        if (uiState.captureMode == CaptureMode.DUAL_VIDEO) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 96.dp, end = 16.dp)
                    .size(80.dp, 110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF181D26))
                    .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Front PiP",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Front PiP", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Underwater Touch Lock Overlay
        if (uiState.captureMode == CaptureMode.UNDERWATER && uiState.isUnderwaterTouchLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF002233).copy(alpha = 0.5f))
                    .clickable { viewModel.toggleUnderwaterTouchLock() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Touchscreen Locked (Underwater Protection)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Press Volume Button to Shoot • Tap to Unlock", color = Color(0xFF9EAEC1), fontSize = 12.sp)
                }
            }
        }

        // Sticker Live Stamp
        if (uiState.captureMode == CaptureMode.STICKER) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 175.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    Text("WAFA CAMERA PRO", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Text("50MP AI MATRIX • Mehedi364", color = Color.White, fontSize = 9.sp)
                }
            }
        }

        // 12. Bottom Control Panel (GCam Carousel & Shutter Bar)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF090D16).copy(alpha = 0.95f))
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dynamic Computational Mode Selector Carousel
            val carouselModes = remember(uiState.pinnedModes, uiState.captureMode) {
                if (uiState.pinnedModes.contains(uiState.captureMode)) {
                    uiState.pinnedModes
                } else {
                    uiState.pinnedModes + uiState.captureMode
                }
            }

            LazyRow(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(carouselModes) { mode ->
                    val isSelected = uiState.captureMode == mode
                    val label = when (mode) {
                        CaptureMode.NIGHT -> "NIGHT"
                        CaptureMode.PORTRAIT -> "PORTRAIT"
                        CaptureMode.PHOTO -> "PHOTO"
                        CaptureMode.HDR -> "HDR"
                        CaptureMode.VIDEO -> "VIDEO"
                        CaptureMode.PRO -> "PRO"
                        CaptureMode.HI_RES -> "HI-RES"
                        CaptureMode.PANO -> "PANO"
                        CaptureMode.MACRO -> "MACRO"
                        CaptureMode.SLO_MO -> "SLO-MO"
                        CaptureMode.TIME_LAPSE -> "TIME-LAPSE"
                        CaptureMode.DUAL_VIDEO -> "VIEW VIDEO"
                        CaptureMode.UNDERWATER -> "UNDERWATER"
                        CaptureMode.STICKER -> "STICKER"
                        CaptureMode.DOC_SCANNER -> "DOC SCANNER"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF1E2638) else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setCaptureMode(mode)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("mode_tab_${mode.name.lowercase()}")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF9EAEC1),
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                // "MORE" tab button that opens the Rearrange Modes Screen
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E2638))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.openRearrangeModes()
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("mode_tab_more")
                    ) {
                        Text(
                            text = "MORE",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Bottom Shutter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Thumbnail / Media Browser
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF1E2638), CircleShape)
                        .background(Color(0xFF131926))
                        .clickable { onNavigateToGallery() }
                        .testTag("gallery_thumbnail_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.lastCapturedThumbnailUri != null) {
                        AsyncImage(
                            model = uiState.lastCapturedThumbnailUri,
                            contentDescription = "Last Capture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Shutter Button
                val isVideoTypeMode = (uiState.captureMode == CaptureMode.VIDEO ||
                                       uiState.captureMode == CaptureMode.SLO_MO ||
                                       uiState.captureMode == CaptureMode.TIME_LAPSE ||
                                       uiState.captureMode == CaptureMode.DUAL_VIDEO)

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, if (isVideoTypeMode) Color.Red else Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isVideoTypeMode) Color.Red else Color.White)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isVideoTypeMode) {
                                if (uiState.isRecordingVideo) {
                                    activeRecording?.stop()
                                    activeRecording = null
                                    viewModel.setRecordingState(false)
                                } else {
                                    val vidCap = videoCapture
                                    if (vidCap != null) {
                                        val modePrefix = when (uiState.captureMode) {
                                            CaptureMode.SLO_MO -> "SLOMO"
                                            CaptureMode.TIME_LAPSE -> "TIMELAPSE"
                                            CaptureMode.DUAL_VIDEO -> "DUALVIEW"
                                            else -> "VID"
                                        }
                                        val contentValues = ContentValues().apply {
                                            put(MediaStore.Video.Media.DISPLAY_NAME, "WAFA_${modePrefix}_${System.currentTimeMillis()}.mp4")
                                            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/WafaCameraPro")
                                            }
                                        }
                                        val mediaStoreOptions = MediaStoreOutputOptions.Builder(
                                            context.contentResolver,
                                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                        ).setContentValues(contentValues).build()

                                        @SuppressLint("MissingPermission")
                                        val pendingRecord = vidCap.output
                                            .prepareRecording(context, mediaStoreOptions)
                                            .apply {
                                                if (hasAudioPermission && uiState.audioEnabled) {
                                                    withAudioEnabled()
                                                }
                                            }

                                        activeRecording = pendingRecord.start(ContextCompat.getMainExecutor(context)) { event ->
                                            when (event) {
                                                is VideoRecordEvent.Start -> viewModel.setRecordingState(true)
                                                is VideoRecordEvent.Finalize -> {
                                                    viewModel.setRecordingState(false)
                                                    if (!event.hasError()) {
                                                        viewModel.updateVideoUri(event.outputResults.outputUri)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                viewModel.initiateCapture(
                                    onPrepareScreenFlash = { active ->
                                        if (active) setWindowBrightness(uiState.screenFlashBrightness)
                                        else restoreWindowBrightness()
                                    },
                                    onExecuteCapture = {
                                        val imgCap = imageCapture ?: return@initiateCapture
                                        imgCap.takePicture(
                                            ContextCompat.getMainExecutor(context),
                                            object : ImageCapture.OnImageCapturedCallback() {
                                                override fun onCaptureSuccess(image: ImageProxy) {
                                                    viewModel.dismissScreenFlash(restoreWindowBrightness)
                                                    val buffer: ByteBuffer = image.planes[0].buffer
                                                    val bytes = ByteArray(buffer.remaining())
                                                    buffer.get(bytes)
                                                    image.close()
                                                    viewModel.processCapturedPhotoBytes(bytes)
                                                }

                                                override fun onError(exception: ImageCaptureException) {
                                                    viewModel.dismissScreenFlash(restoreWindowBrightness)
                                                    exception.printStackTrace()
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                        .testTag("shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.captureMode == CaptureMode.VIDEO && uiState.isRecordingVideo) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Switch Camera Lens Facing
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleLensFacing()
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFF131926), CircleShape)
                        .border(1.dp, Color(0xFF1E2638), CircleShape)
                        .testTag("switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
