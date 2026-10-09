package com.example.camera

import android.net.Uri
import androidx.camera.core.CameraSelector
import com.example.core.compatibility.LensCapabilities
import com.example.imageprocessing.QualityPreset

enum class CaptureMode {
    PHOTO,
    PORTRAIT,
    NIGHT,
    HDR,
    VIDEO,
    PRO,
    HI_RES,
    PANO,
    MACRO,
    SLO_MO,
    TIME_LAPSE,
    DUAL_VIDEO,
    UNDERWATER,
    STICKER,
    DOC_SCANNER
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

enum class AspectRatioSetting {
    RATIO_4_3,
    RATIO_16_9,
    RATIO_1_1
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
    val isZoomSliderVisible: Boolean = false,
    val exposureIndex: Int = 0,
    val minExposureIndex: Int = -4,
    val maxExposureIndex: Int = 4,
    val exposureStep: Float = 0.5f,
    val isAeLocked: Boolean = false,
    val isBacklitCompensationActive: Boolean = false,
    val qualityPreset: QualityPreset = QualityPreset.NATURAL,
    val isQualityEnhanceEnabled: Boolean = true,
    val saveBothOriginalAndEnhanced: Boolean = false,
    val rawCaptureEnabled: Boolean = false,
    val aspectRatioSetting: AspectRatioSetting = AspectRatioSetting.RATIO_4_3,
    val lastProcessingTimeMs: Long = 0L,
    val isProMode: Boolean = false,
    val hasManualSensorSupport: Boolean = false,
    val hasFlashUnit: Boolean = true,
    val supportedExtensions: List<String> = emptyList(),
    val activeExtensionName: String? = null,
    val isRecordingVideo: Boolean = false,
    val isVideoPaused: Boolean = false,
    val videoDurationSeconds: Long = 0L,
    val audioEnabled: Boolean = true,
    val lastCapturedThumbnailUri: Uri? = null,
    val isCapturing: Boolean = false,
    val statusMessage: String? = null,
    val focusRingPosition: Pair<Float, Float>? = null,
    val isFocusRingVisible: Boolean = false,
    val watermarkEnabled: Boolean = true,
    val currentLensCapabilities: LensCapabilities? = null,
    val pinnedModes: List<CaptureMode> = listOf(
        CaptureMode.VIDEO,
        CaptureMode.PHOTO,
        CaptureMode.PORTRAIT,
        CaptureMode.NIGHT,
        CaptureMode.PRO
    ),
    val isRearrangeModesVisible: Boolean = false,
    val timeLapseIntervalSeconds: Int = 2,
    val isUnderwaterTouchLocked: Boolean = false,
    val isStickerActive: Boolean = true
)
