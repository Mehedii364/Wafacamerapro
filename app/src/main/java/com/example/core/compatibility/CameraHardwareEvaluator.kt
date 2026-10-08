package com.example.core.compatibility

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build

data class CameraDeviceInfo(
    val cameraId: String,
    val facing: String,
    val hardwareLevel: String,
    val maxResolution: String,
    val hasFlash: Boolean,
    val zoomRange: String,
    val exposureRange: String,
    val hasOis: Boolean,
    val hasVideoStabilization: Boolean,
    val supportsRaw: Boolean,
    val isoRange: String,
    val exposureTimeRange: String,
    val hasManualSensor: Boolean,
    val hasManualPostProcessing: Boolean
)

object CameraHardwareEvaluator {

    fun getDeviceCapabilities(context: Context): List<CameraDeviceInfo> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return emptyList()

        val results = mutableListOf<CameraDeviceInfo>()

        try {
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)

                val facingInt = chars.get(CameraCharacteristics.LENS_FACING)
                val facingStr = when (facingInt) {
                    CameraCharacteristics.LENS_FACING_BACK -> "Rear (Main)"
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front (Selfie)"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External USB"
                    else -> "Unknown ($facingInt)"
                }

                val levelInt = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                val levelStr = when (levelInt) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY (Basic)"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL (Manual Controls)"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL 3 (Pro RAW & Burst)"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
                    else -> "Unknown"
                }

                // Max resolution
                val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val jpegSizes = map?.getOutputSizes(ImageFormat.JPEG)
                val maxRes = if (!jpegSizes.isNullOrEmpty()) {
                    val largest = jpegSizes.maxByOrNull { it.width * it.height }
                    if (largest != null) {
                        val mp = (largest.width * largest.height) / 1_000_000f
                        "${largest.width}x${largest.height} (%.1f MP)".format(mp)
                    } else "N/A"
                } else "N/A"

                // Flash
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false

                // Zoom
                val zoomRangeStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val range = chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                    if (range != null) "%.1fx - %.1fx".format(range.lower, range.upper)
                    else "1.0x (Fixed / Crop only)"
                } else {
                    val maxZoom = chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1.0f
                    "1.0x - %.1fx (Digital)".format(maxZoom)
                }

                // Exposure
                val expRange = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                val expStep = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
                val expStr = if (expRange != null && expStep != null) {
                    val stepVal = expStep.toFloat()
                    val minEv = expRange.lower * stepVal
                    val maxEv = expRange.upper * stepVal
                    "%.1f EV to +%.1f EV (step %.2f)".format(minEv, maxEv, stepVal)
                } else "Auto Only"

                // OIS & Video Stabilization
                val opticalStab = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                val hasOis = opticalStab != null && opticalStab.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON)

                val videoStab = chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
                val hasVideoStab = videoStab != null && videoStab.contains(CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON)

                // RAW support
                val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: intArrayOf()
                val supportsRaw = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)
                val hasManualSensor = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)
                val hasManualPost = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING)

                // ISO range
                val isoRange = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                val isoStr = if (isoRange != null) "${isoRange.lower} - ${isoRange.upper}" else "Auto"

                // Exposure time
                val expTimeRange = chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
                val expTimeStr = if (expTimeRange != null) {
                    val minMs = expTimeRange.lower / 1_000_000f
                    val maxMs = expTimeRange.upper / 1_000_000f
                    "%.2f ms - %.0f ms".format(minMs, maxMs)
                } else "Auto"

                results.add(
                    CameraDeviceInfo(
                        cameraId = id,
                        facing = facingStr,
                        hardwareLevel = levelStr,
                        maxResolution = maxRes,
                        hasFlash = hasFlash,
                        zoomRange = zoomRangeStr,
                        exposureRange = expStr,
                        hasOis = hasOis,
                        hasVideoStabilization = hasVideoStab,
                        supportsRaw = supportsRaw,
                        isoRange = isoStr,
                        exposureTimeRange = expTimeStr,
                        hasManualSensor = hasManualSensor,
                        hasManualPostProcessing = hasManualPost
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return results
    }
}
