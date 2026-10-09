package com.example.core.compatibility

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.camera.core.CameraSelector
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LensCapabilities(
    val cameraId: String,
    val isFront: Boolean,
    val hardwareLevel: String,
    val maxResolution: String,
    val focalLengths: List<Float>,
    val minZoomRatio: Float,
    val maxZoomRatio: Float,
    val hasOpticalZoom: Boolean,
    val exposureRange: Pair<Int, Int>,
    val exposureStep: Float,
    val hasFlashUnit: Boolean,
    val afModes: List<String>,
    val supportedJpegResolutions: List<String>,
    val supportsRawDng: Boolean,
    val hasManualSensor: Boolean,
    val hasOis: Boolean,
    val hasVideoStabilization: Boolean,
    val supportedExtensions: List<String>
)

data class DeviceCameraAudit(
    val rearCamera: LensCapabilities?,
    val frontCamera: LensCapabilities?,
    val allCameras: List<LensCapabilities>
)

object CameraCapabilityManager {

    suspend fun auditDevice(context: Context): DeviceCameraAudit = withContext(Dispatchers.IO) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return@withContext DeviceCameraAudit(null, null, emptyList())

        // Discover vendor extensions via CameraX ExtensionsManager if supported
        val vendorExtensions: Pair<List<String>, List<String>> = try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            val extensionsManager = ExtensionsManager.getInstanceAsync(context, cameraProvider).get()

            val rearSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val frontSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val rearExts = mutableListOf<String>()
            val frontExts = mutableListOf<String>()

            if (extensionsManager.isExtensionAvailable(rearSelector, ExtensionMode.HDR)) rearExts.add("HDR")
            if (extensionsManager.isExtensionAvailable(rearSelector, ExtensionMode.NIGHT)) rearExts.add("NIGHT")
            if (extensionsManager.isExtensionAvailable(rearSelector, ExtensionMode.BOKEH)) rearExts.add("PORTRAIT_BOKEH")
            if (extensionsManager.isExtensionAvailable(rearSelector, ExtensionMode.FACE_RETOUCH)) rearExts.add("FACE_RETOUCH")
            if (extensionsManager.isExtensionAvailable(rearSelector, ExtensionMode.AUTO)) rearExts.add("AUTO")

            if (extensionsManager.isExtensionAvailable(frontSelector, ExtensionMode.HDR)) frontExts.add("HDR")
            if (extensionsManager.isExtensionAvailable(frontSelector, ExtensionMode.NIGHT)) frontExts.add("NIGHT")
            if (extensionsManager.isExtensionAvailable(frontSelector, ExtensionMode.BOKEH)) frontExts.add("PORTRAIT_BOKEH")
            if (extensionsManager.isExtensionAvailable(frontSelector, ExtensionMode.FACE_RETOUCH)) frontExts.add("FACE_RETOUCH")

            Pair(rearExts.toList(), frontExts.toList())
        } catch (e: Exception) {
            Pair(emptyList<String>(), emptyList<String>())
        }

        val capabilitiesList = mutableListOf<LensCapabilities>()

        try {
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)

                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                val isFront = facing == CameraCharacteristics.LENS_FACING_FRONT

                val levelInt = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                val levelStr = when (levelInt) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
                    else -> "UNKNOWN"
                }

                // Focal lengths
                val flArray = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS) ?: floatArrayOf()
                val focalLengths = flArray.toList()
                val hasOpticalZoom = focalLengths.size > 1

                // Zoom range
                var minZ = 1.0f
                var maxZ = 8.0f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val range = chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                    if (range != null) {
                        minZ = range.lower
                        maxZ = range.upper
                    }
                } else {
                    maxZ = chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 8.0f
                }

                // Exposure
                val expRange = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                val expStep = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)?.toFloat() ?: 0.5f
                val exposurePair = if (expRange != null) Pair(expRange.lower, expRange.upper) else Pair(-4, 4)

                // Flash unit
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false

                // AF Modes
                val afInts = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) ?: intArrayOf()
                val afModes = afInts.map { mode ->
                    when (mode) {
                        CameraCharacteristics.CONTROL_AF_MODE_AUTO -> "AUTO"
                        CameraCharacteristics.CONTROL_AF_MODE_MACRO -> "MACRO"
                        CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> "CONTINUOUS_PICTURE"
                        CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> "CONTINUOUS_VIDEO"
                        CameraCharacteristics.CONTROL_AF_MODE_EDOF -> "EDOF"
                        else -> "OFF"
                    }
                }.distinct()

                // Stream Configuration Map
                val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val jpegSizes = map?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()
                val resStrings = jpegSizes.map { "${it.width}x${it.height}" }
                val maxRes = if (jpegSizes.isNotEmpty()) {
                    val largest = jpegSizes.maxByOrNull { it.width * it.height }
                    if (largest != null) {
                        val mp = (largest.width * largest.height) / 1_000_000f
                        "${largest.width}x${largest.height} (%.1f MP)".format(mp)
                    } else "N/A"
                } else "N/A"

                // RAW / Manual sensor capabilities
                val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: intArrayOf()
                val supportsRaw = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)
                val hasManualSensor = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)

                // Stabilization
                val opticalStab = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                val hasOis = opticalStab != null && opticalStab.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON)

                val videoStab = chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
                val hasVideoStab = videoStab != null && videoStab.contains(CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON)

                val exts = if (isFront) vendorExtensions.second else vendorExtensions.first

                capabilitiesList.add(
                    LensCapabilities(
                        cameraId = id,
                        isFront = isFront,
                        hardwareLevel = levelStr,
                        maxResolution = maxRes,
                        focalLengths = focalLengths,
                        minZoomRatio = minZ,
                        maxZoomRatio = maxZ,
                        hasOpticalZoom = hasOpticalZoom,
                        exposureRange = exposurePair,
                        exposureStep = expStep,
                        hasFlashUnit = hasFlash,
                        afModes = afModes,
                        supportedJpegResolutions = resStrings,
                        supportsRawDng = supportsRaw,
                        hasManualSensor = hasManualSensor,
                        hasOis = hasOis,
                        hasVideoStabilization = hasVideoStab,
                        supportedExtensions = exts
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val rear = capabilitiesList.firstOrNull { !it.isFront }
        val front = capabilitiesList.firstOrNull { it.isFront }

        DeviceCameraAudit(rear, front, capabilitiesList)
    }
}
