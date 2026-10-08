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

    private val db = WafaDatabase.getInstance(application)

    fun setCaptureMode(mode: CaptureMode) {
        _uiState.update { it.copy(captureMode = mode) }
    }

    fun toggleLensFacing() {
        _uiState.update {
            val nextFacing = if (it.lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            it.copy(lensFacing = nextFacing, zoomRatio = 1.0f)
        }
    }

    fun cycleFlash() {
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

    fun updateExposureBounds(min: Int, max: Int, step: Float) {
        _uiState.update {
            it.copy(minExposureIndex = min, maxExposureIndex = max, exposureStep = step)
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

    fun initiateCapture(onExecuteCapture: () -> Unit) {
        val timerSec = _uiState.value.timerSeconds
        if (timerSec <= 0) {
            onExecuteCapture()
            return
        }

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _uiState.update { it.copy(isCountingDown = true, countdownRemaining = timerSec) }
            for (sec in timerSec downTo 1) {
                _uiState.update { it.copy(countdownRemaining = sec) }
                delay(1000)
            }
            _uiState.update { it.copy(isCountingDown = false, countdownRemaining = 0) }
            onExecuteCapture()
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        _uiState.update { it.copy(isCountingDown = false, countdownRemaining = 0) }
    }

    fun processCapturedPhotoBytes(jpegBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true, statusMessage = "Processing & Saving...") }
            try {
                val originalBmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                if (originalBmp == null) {
                    _uiState.update { it.copy(isCapturing = false, statusMessage = "Failed to decode photo") }
                    return@launch
                }

                val finalBmp = if (_uiState.value.watermarkEnabled) {
                    WatermarkRenderer.applyWatermark(
                        src = originalBmp,
                        text = "Developed by Mehedi364 • Wafa Camera Pro",
                        position = "BOTTOM_RIGHT",
                        includeTimestamp = true
                    )
                } else {
                    originalBmp
                }

                val saveResult = MediaSaver.saveBitmapToGallery(
                    context = getApplication(),
                    bitmap = finalBmp,
                    titlePrefix = "WAFA_PRO",
                    format = Bitmap.CompressFormat.JPEG,
                    quality = 95
                )

                saveResult.onSuccess { uri ->
                    _uiState.update {
                        it.copy(
                            isCapturing = false,
                            lastCapturedThumbnailUri = uri,
                            statusMessage = "Photo saved to Gallery!"
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

            delay(2500)
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
