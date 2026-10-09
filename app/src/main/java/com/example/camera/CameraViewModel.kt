package com.example.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.storage.MediaSaver
import com.example.data.database.WafaDatabase
import com.example.imageprocessing.ImageQualityEnhancer
import com.example.imageprocessing.QualityPreset
import com.example.imageprocessing.WatermarkRenderer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var videoDurationJob: Job? = null
    private var screenFlashJob: Job? = null

    private val db = WafaDatabase.getInstance(application)

    fun setCaptureMode(mode: CaptureMode) {
        _uiState.update { it.copy(captureMode = mode) }
    }

    fun setProMode(isPro: Boolean) {
        _uiState.update { it.copy(isProMode = isPro) }
    }

    fun toggleLensFacing() {
        _uiState.update {
            val nextFacing = if (it.lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            it.copy(
                lensFacing = nextFacing,
                zoomRatio = 1.0f,
                isScreenFlashActive = false
            )
        }
    }

    fun cycleFlash() {
        if (_uiState.value.lensFacing == CameraSelector.LENS_FACING_FRONT) {
            cycleScreenFlash()
            return
        }
        _uiState.update {
            val next = when (it.flashSetting) {
                FlashSetting.AUTO -> FlashSetting.ON
                FlashSetting.ON -> FlashSetting.OFF
                FlashSetting.OFF -> FlashSetting.TORCH
                FlashSetting.TORCH -> FlashSetting.AUTO
            }
            it.copy(flashSetting = next)
        }
    }

    fun cycleScreenFlash() {
        _uiState.update {
            val next = when (it.screenFlashMode) {
                ScreenFlashMode.AUTO -> ScreenFlashMode.ON
                ScreenFlashMode.ON -> ScreenFlashMode.OFF
                ScreenFlashMode.OFF -> ScreenFlashMode.AUTO
            }
            it.copy(screenFlashMode = next)
        }
    }

    fun setScreenFlashTone(tone: ScreenFlashTone) {
        _uiState.update { it.copy(screenFlashTone = tone) }
    }

    fun setScreenFlashBrightness(brightness: Float) {
        _uiState.update { it.copy(screenFlashBrightness = brightness.coerceIn(0.5f, 1.0f)) }
    }

    fun cycleGrid() {
        _uiState.update {
            val next = when (it.gridType) {
                GridType.NONE -> GridType.RULE_OF_THIRDS
                GridType.RULE_OF_THIRDS -> GridType.GOLDEN_RATIO
                GridType.GOLDEN_RATIO -> GridType.CENTER_CROSS
                GridType.CENTER_CROSS -> GridType.NONE
            }
            it.copy(gridType = next)
        }
    }

    fun setTimer(seconds: Int) {
        _uiState.update { it.copy(timerSeconds = seconds) }
    }

    fun setZoom(ratio: Float) {
        _uiState.update {
            val clamped = ratio.coerceIn(it.minZoomRatio, it.maxZoomRatio)
            it.copy(zoomRatio = clamped)
        }
    }

    fun updateZoomBounds(min: Float, max: Float) {
        _uiState.update { it.copy(minZoomRatio = min, maxZoomRatio = max) }
    }

    fun setExposureIndex(index: Int) {
        _uiState.update {
            val clamped = index.coerceIn(it.minExposureIndex, it.maxExposureIndex)
            it.copy(exposureIndex = clamped)
        }
    }

    fun resetExposureToZero() {
        _uiState.update { it.copy(exposureIndex = 0, isBacklitCompensationActive = false) }
    }

    fun toggleAeLock() {
        _uiState.update { it.copy(isAeLocked = !it.isAeLocked) }
    }

    fun toggleBacklitCompensation() {
        _uiState.update {
            val nextActive = !it.isBacklitCompensationActive
            // If active, boost exposure by +2 steps within limit
            val newIndex = if (nextActive) {
                (it.exposureIndex + 2).coerceAtMost(it.maxExposureIndex)
            } else {
                0
            }
            it.copy(
                isBacklitCompensationActive = nextActive,
                exposureIndex = newIndex
            )
        }
    }

    fun updateExposureBounds(min: Int, max: Int, step: Float) {
        _uiState.update {
            it.copy(minExposureIndex = min, maxExposureIndex = max, exposureStep = step)
        }
    }

    fun setQualityPreset(preset: QualityPreset) {
        _uiState.update { it.copy(qualityPreset = preset) }
    }

    fun toggleQualityEnhancement() {
        _uiState.update { it.copy(isQualityEnhanceEnabled = !it.isQualityEnhanceEnabled) }
    }

    fun toggleSaveBothOriginalAndEnhanced() {
        _uiState.update { it.copy(saveBothOriginalAndEnhanced = !it.saveBothOriginalAndEnhanced) }
    }

    fun setHardwareCapabilities(manualSensor: Boolean, hdrSupported: Boolean, nightSupported: Boolean) {
        _uiState.update {
            it.copy(
                hasManualSensorSupport = manualSensor,
                isHardwareHdrSupported = hdrSupported,
                isHardwareNightSupported = nightSupported
            )
        }
    }

    fun toggleWatermark() {
        _uiState.update { it.copy(watermarkEnabled = !it.watermarkEnabled) }
    }

    fun triggerFocusRing(x: Float, y: Float) {
        _uiState.update { it.copy(focusRingPosition = Pair(x, y), isFocusRingVisible = true) }
        viewModelScope.launch {
            delay(1500)
            _uiState.update { it.copy(isFocusRingVisible = false) }
        }
    }

    /**
     * Prepares capture, coordinating screen flash and countdown timers.
     */
    fun initiateCapture(
        onPrepareScreenFlash: (Boolean) -> Unit,
        onExecuteCapture: () -> Unit
    ) {
        val isFrontCamera = _uiState.value.lensFacing == CameraSelector.LENS_FACING_FRONT
        val needScreenFlash = isFrontCamera && _uiState.value.screenFlashMode != ScreenFlashMode.OFF

        val timerSec = _uiState.value.timerSeconds
        if (timerSec > 0) {
            countdownJob?.cancel()
            countdownJob = viewModelScope.launch {
                _uiState.update { it.copy(isCountingDown = true, countdownRemaining = timerSec) }
                for (sec in timerSec downTo 1) {
                    _uiState.update { it.copy(countdownRemaining = sec) }
                    delay(1000)
                }
                _uiState.update { it.copy(isCountingDown = false, countdownRemaining = 0) }

                executeCaptureWithScreenFlash(needScreenFlash, onPrepareScreenFlash, onExecuteCapture)
            }
        } else {
            executeCaptureWithScreenFlash(needScreenFlash, onPrepareScreenFlash, onExecuteCapture)
        }
    }

    private fun executeCaptureWithScreenFlash(
        needScreenFlash: Boolean,
        onPrepareScreenFlash: (Boolean) -> Unit,
        onExecuteCapture: () -> Unit
    ) {
        if (needScreenFlash) {
            screenFlashJob?.cancel()
            screenFlashJob = viewModelScope.launch {
                // 1. Activate full-screen illumination & max window brightness
                _uiState.update { it.copy(isScreenFlashActive = true) }
                onPrepareScreenFlash(true)

                // 2. Pre-flash illumination warmup (350ms) to allow front AE/AWB to adapt smoothly
                delay(350)

                // 3. Trigger CameraX ImageCapture during peak illumination
                onExecuteCapture()
            }
        } else {
            onExecuteCapture()
        }
    }

    fun dismissScreenFlash(onRestoreBrightness: () -> Unit) {
        screenFlashJob?.cancel()
        _uiState.update { it.copy(isScreenFlashActive = false) }
        onRestoreBrightness()
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        _uiState.update { it.copy(isCountingDown = false, countdownRemaining = 0) }
    }

    fun processCapturedPhotoBytes(jpegBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true, statusMessage = "Processing & Enhancing...") }
            try {
                val originalBmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                if (originalBmp == null) {
                    _uiState.update { it.copy(isCapturing = false, statusMessage = "Failed to decode photo") }
                    return@launch
                }

                var processedBmp = originalBmp
                var processingTime = 0L

                // 1. Image Quality Pipeline Enhancement
                if (_uiState.value.isQualityEnhanceEnabled) {
                    val enhancement = ImageQualityEnhancer.enhance(
                        source = originalBmp,
                        preset = _uiState.value.qualityPreset
                    )
                    processedBmp = enhancement.enhancedBitmap
                    processingTime = enhancement.processingTimeMs
                    _uiState.update { it.copy(lastProcessingTimeMs = processingTime) }
                }

                // 2. Watermark Application
                val finalBmp = if (_uiState.value.watermarkEnabled) {
                    WatermarkRenderer.applyWatermark(
                        src = processedBmp,
                        text = "Developed by Mehedi364 • Wafa Camera Pro",
                        position = "BOTTOM_RIGHT",
                        includeTimestamp = true
                    )
                } else {
                    processedBmp
                }

                // Optional: Save original photo separately if configured
                if (_uiState.value.saveBothOriginalAndEnhanced && _uiState.value.isQualityEnhanceEnabled) {
                    MediaSaver.saveBitmapToGallery(
                        context = getApplication(),
                        bitmap = originalBmp,
                        titlePrefix = "WAFA_RAW",
                        format = Bitmap.CompressFormat.JPEG,
                        quality = 95
                    )
                }

                // Save primary result
                val saveResult = MediaSaver.saveBitmapToGallery(
                    context = getApplication(),
                    bitmap = finalBmp,
                    titlePrefix = "WAFA_PRO",
                    format = Bitmap.CompressFormat.JPEG,
                    quality = 95
                )

                saveResult.onSuccess { uri ->
                    val statusMsg = if (processingTime > 0) {
                        "Photo saved! (Enhanced in ${processingTime}ms)"
                    } else {
                        "Photo saved to Gallery!"
                    }
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            lastCapturedThumbnailUri = uri,
                            statusMessage = statusMsg
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            statusMessage = "Save failed: ${err.localizedMessage}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isCapturing = false, statusMessage = "Error: ${e.localizedMessage}")
                }
            }

            delay(2800)
            _uiState.update { it.copy(statusMessage = null) }
        }
    }

    fun setRecordingState(isRecording: Boolean, isPaused: Boolean = false) {
        _uiState.update { it.copy(isRecordingVideo = isRecording, isVideoPaused = isPaused) }
        if (isRecording) {
            videoDurationJob?.cancel()
            videoDurationJob = viewModelScope.launch {
                var duration = 0L
                while (_uiState.value.isRecordingVideo) {
                    if (!_uiState.value.isVideoPaused) {
                        duration++
                        _uiState.update { it.copy(videoDurationSeconds = duration) }
                    }
                    delay(1000)
                }
            }
        } else {
            videoDurationJob?.cancel()
            _uiState.update { it.copy(videoDurationSeconds = 0L) }
        }
    }

    fun updateVideoUri(uri: Uri) {
        _uiState.update {
            it.copy(
                lastCapturedThumbnailUri = uri,
                statusMessage = "Video recorded successfully!"
            )
        }
        viewModelScope.launch {
            delay(2500)
            _uiState.update { it.copy(statusMessage = null) }
        }
    }
}
