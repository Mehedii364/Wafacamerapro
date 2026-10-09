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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HdrOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.imageprocessing.QualityPreset
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

    // CameraX instance holders
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    var showExposureSlider by remember { mutableStateOf(false) }
    var showQualityDrawer by remember { mutableStateOf(false) }

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

                // Setup zoom & exposure bounds
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

    // Handle Torch (only for rear camera)
    LaunchedEffect(uiState.flashSetting, activeCamera, uiState.lensFacing) {
        if (uiState.lensFacing == CameraSelector.LENS_FACING_BACK) {
            activeCamera?.cameraControl?.enableTorch(uiState.flashSetting == FlashSetting.TORCH)
        } else {
            activeCamera?.cameraControl?.enableTorch(false)
        }
    }

    // Handle Zoom
    LaunchedEffect(uiState.zoomRatio, activeCamera) {
        activeCamera?.cameraControl?.setZoomRatio(uiState.zoomRatio)
    }

    // Handle Exposure Compensation
    LaunchedEffect(uiState.exposureIndex, activeCamera) {
        activeCamera?.cameraControl?.setExposureCompensationIndex(uiState.exposureIndex)
    }

    // Clean up active recording & restore window brightness when leaving
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
        // 1. Live Camera Preview View with Tap-to-Focus
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        previewView?.let { pv ->
                            val factory = pv.meteringPointFactory
                            val point = factory.createPoint(offset.x, offset.y)
                            val action = FocusMeteringAction.Builder(point).build()
                            activeCamera?.cameraControl?.startFocusAndMetering(action)
                            viewModel.triggerFocusRing(offset.x, offset.y)
                        }
                    }
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

        // 5. Top HUD Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 12.dp, end = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash Button: Hardware Flash (Rear) vs Screen Flash (Front)
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

                // Timer Button
                IconButton(
                    onClick = {
                        val nextTimer = when (uiState.timerSeconds) {
                            0 -> 3
                            3 -> 5
                            5 -> 10
                            else -> 0
                        }
                        viewModel.setTimer(nextTimer)
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .size(42.dp)
                        .testTag("timer_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = if (uiState.timerSeconds > 0) Color(0xFF00E5FF) else Color.White
                        )
                        if (uiState.timerSeconds > 0) {
                            Text(
                                text = "${uiState.timerSeconds}s",
                                color = Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }

                // Grid Button
                IconButton(
                    onClick = { viewModel.cycleGrid() },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .size(42.dp)
                        .testTag("grid_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Grid",
                        tint = if (uiState.gridType != GridType.NONE) Color(0xFF00E5FF) else Color.White
                    )
                }

                // Exposure (EV) Toggle
                IconButton(
                    onClick = {
                        showExposureSlider = !showExposureSlider
                        if (showExposureSlider) showQualityDrawer = false
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

                // Quality Presets / Studio Pipeline Toggle
                IconButton(
                    onClick = {
                        showQualityDrawer = !showQualityDrawer
                        if (showQualityDrawer) showExposureSlider = false
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .size(42.dp)
                        .testTag("quality_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Quality Pipeline",
                        tint = if (showQualityDrawer || uiState.isQualityEnhanceEnabled) Color(0xFF00E676) else Color.White
                    )
                }

                // Pro Mode Switcher Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (uiState.isProMode) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.isProMode) Color(0xFF00E5FF) else Color.Gray),
                    modifier = Modifier.clickable { viewModel.setProMode(!uiState.isProMode) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (uiState.isProMode) Color(0xFF00E5FF) else Color.Gray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isProMode) "PRO" else "AUTO",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Front Screen Flash indicator notice
            if (isFrontCamera && uiState.screenFlashMode != ScreenFlashMode.OFF) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .background(Color(0xFF131926).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LightMode, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Screen Flash: Display Illumination (${if (uiState.screenFlashMode == ScreenFlashMode.AUTO) "Auto" else "On"})",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 6. Advanced Exposure Floating Panel
        if (showExposureSlider) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .background(Color(0xFF090D16).copy(alpha = 0.9f), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp)
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

                    // Reset EV Button
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

                    // AE Lock Toggle
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Backlit Portrait Boost
                    IconButton(
                        onClick = { viewModel.toggleBacklitCompensation() },
                        modifier = Modifier
                            .size(34.dp)
                            .background(if (uiState.isBacklitCompensationActive) Color(0xFF00E676) else Color(0xFF1E2638), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Backlit Boost",
                            tint = if (uiState.isBacklitCompensationActive) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 7. Image Quality Pipeline Drawer
        if (showQualityDrawer) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
                    .background(Color(0xFF090D16).copy(alpha = 0.92f), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.55f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quality Engine",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.isQualityEnhanceEnabled) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { viewModel.toggleQualityEnhancement() }
                        ) {
                            Text(
                                text = if (uiState.isQualityEnhanceEnabled) "ON" else "OFF",
                                color = if (uiState.isQualityEnhanceEnabled) Color(0xFF00E676) else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        QualityPreset.NATURAL to "Natural Skin",
                        QualityPreset.DETAIL to "Detail Sharpen",
                        QualityPreset.LOW_LIGHT to "Night Denoise",
                        QualityPreset.HDR_STYLE to "HDR Dynamic"
                    ).forEach { (preset, label) ->
                        val isSelected = uiState.qualityPreset == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF00E676).copy(alpha = 0.25f) else Color(0xFF1E2638),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF00E676) else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .padding(vertical = 3.dp)
                                .clickable { viewModel.setQualityPreset(preset) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (preset) {
                                        QualityPreset.NATURAL -> Icons.Default.LightMode
                                        QualityPreset.DETAIL -> Icons.Default.AutoAwesome
                                        QualityPreset.LOW_LIGHT -> Icons.Default.Nightlight
                                        QualityPreset.HDR_STYLE -> Icons.Default.HdrOn
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF00E676) else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Front Screen Flash Tone (When Front Camera active)
                    if (isFrontCamera) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Flash Tone",
                            color = Color(0xFFFFB300),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                ScreenFlashTone.NEUTRAL_WHITE to "Neutral",
                                ScreenFlashTone.WARM_SOFT to "Warm",
                                ScreenFlashTone.COOL_BRIGHT to "Cool"
                            ).forEach { (tone, name) ->
                                val isToneSelected = uiState.screenFlashTone == tone
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isToneSelected) Color(0xFFFFB300).copy(alpha = 0.25f) else Color(0xFF1E2638),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isToneSelected) Color(0xFFFFB300) else Color.Transparent),
                                    modifier = Modifier.clickable { viewModel.setScreenFlashTone(tone) }
                                ) {
                                    Text(
                                        text = name,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Zoom Controls Pill (Bottom Center above shutter)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(0.6f, 1.0f, 2.0f, 5.0f).forEach { zoomLevel ->
                val isSelected = (uiState.zoomRatio >= zoomLevel - 0.2f && uiState.zoomRatio <= zoomLevel + 0.2f)
                Text(
                    text = "${zoomLevel}x",
                    color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { viewModel.setZoom(zoomLevel) }
                )
            }
        }

        // 9. Status Message Notification
        AnimatedVisibility(
            visible = uiState.statusMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF131926).copy(alpha = 0.92f),
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

        // 10. Countdown Timer Display
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

        // 11. Video Recording Duration Indicator
        if (uiState.isRecordingVideo) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp)
                    .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
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

        // 12. Bottom Control Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF090D16).copy(alpha = 0.95f))
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Selector Bar (PHOTO / VIDEO / PRO)
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                CaptureMode.values().forEach { mode ->
                    val isSelected = uiState.captureMode == mode
                    Text(
                        text = when (mode) {
                            CaptureMode.PHOTO -> "PHOTO"
                            CaptureMode.VIDEO -> "VIDEO"
                            CaptureMode.PRO_MANUAL -> "PRO"
                        },
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF9EAEC1),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { viewModel.setCaptureMode(mode) }
                            .padding(4.dp)
                    )
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

                // Shutter Button (Photo capture or Video record)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, if (uiState.captureMode == CaptureMode.VIDEO) Color.Red else Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (uiState.captureMode == CaptureMode.VIDEO) Color.Red else Color.White)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (uiState.captureMode == CaptureMode.VIDEO) {
                                if (uiState.isRecordingVideo) {
                                    activeRecording?.stop()
                                    activeRecording = null
                                    viewModel.setRecordingState(false)
                                } else {
                                    val vidCap = videoCapture
                                    if (vidCap != null) {
                                        val contentValues = ContentValues().apply {
                                            put(MediaStore.Video.Media.DISPLAY_NAME, "WAFA_VID_${System.currentTimeMillis()}.mp4")
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
                                // Photo capture with screen flash coordination
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
