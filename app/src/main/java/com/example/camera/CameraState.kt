package com.example.camera

import android.net.Uri
import androidx.camera.core.CameraSelector
import com.example.imageprocessing.QualityPreset

enum class CaptureMode {
    PHOTO,
    VIDEO,
    PRO_MANUAL
}

enum class GridType {
    NONE,
    RULE_OF_THIRDS,
    GOLDEN_RATIO,
    CENTER_CROSS
}

enum class FlashSetting {
    AUTO,
    ON,
    OFF,
    TORCH
}

enum class ScreenFlashMode {
    AUTO,
    ON,
    OFF
}

enum class ScreenFlashTone {
    NEUTRAL_WHITE,
    WARM_SOFT,
    COOL_BRIGHT
}

data class CameraUiState(
    val captureMode: CaptureMode = CaptureMode.PHOTO,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val flashSetting: FlashSetting = FlashSetting.AUTO,
    val screenFlashMode: ScreenFlashMode = ScreenFlashMode.AUTO,
    val isScreenFlashActive: Boolean = false,
    val screenFlashTone: ScreenFlashTone = ScreenFlashTone.NEUTRAL_WHITE,
    val screenFlashBrightness: Float = 1.0f,
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val timerSeconds: Int = 0,
    val countdownRemaining: Int = 0,
    val isCountingDown: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val minZoomRatio: Float = 1.0f,
    val maxZoomRatio: Float = 8.0f,
    val exposureIndex: Int = 0,
    val minExposureIndex: Int = -4,
    val maxExposureIndex: Int = 4,
    val exposureStep: Float = 0.5f,
    val isAeLocked: Boolean = false,
    val isBacklitCompensationActive: Boolean = false,
    val qualityPreset: QualityPreset = QualityPreset.NATURAL,
    val isQualityEnhanceEnabled: Boolean = true,
    val saveBothOriginalAndEnhanced: Boolean = false,
    val lastProcessingTimeMs: Long = 0L,
    val isProMode: Boolean = false,
    val hasManualSensorSupport: Boolean = false,
    val isHardwareHdrSupported: Boolean = false,
    val isHardwareNightSupported: Boolean = false,
    val isRecordingVideo: Boolean = false,
    val isVideoPaused: Boolean = false,
    val videoDurationSeconds: Long = 0L,
    val audioEnabled: Boolean = true,
    val lastCapturedThumbnailUri: Uri? = null,
    val isCapturing: Boolean = false,
    val statusMessage: String? = null,
    val focusRingPosition: Pair<Float, Float>? = null,
    val isFocusRingVisible: Boolean = false,
    val watermarkEnabled: Boolean = true
)
