package com.example

import com.example.camera.CameraUiState
import com.example.camera.CaptureMode
import com.example.camera.FlashSetting
import com.example.camera.GridType
import com.example.duplicatefinder.DuplicateDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WafaCameraProUnitTest {

    @Test
    fun cameraUiState_defaults_areProper() {
        val state = CameraUiState()
        assertEquals(CaptureMode.PHOTO, state.captureMode)
        assertEquals(FlashSetting.AUTO, state.flashSetting)
        assertEquals(GridType.RULE_OF_THIRDS, state.gridType)
        assertEquals(0, state.timerSeconds)
        assertFalse(state.isCountingDown)
        assertFalse(state.isRecordingVideo)
        assertTrue(state.watermarkEnabled)
        assertEquals(com.example.camera.ScreenFlashMode.AUTO, state.screenFlashMode)
        assertEquals(com.example.imageprocessing.QualityPreset.NATURAL, state.qualityPreset)
        assertEquals(1.0f, state.screenFlashBrightness, 0.01f)
        assertFalse(state.isAeLocked)
        assertFalse(state.isScreenFlashActive)
    }

    @Test
    fun screenFlashTone_enum_containsAllTones() {
        val tones = com.example.camera.ScreenFlashTone.values()
        assertEquals(3, tones.size)
        assertTrue(tones.contains(com.example.camera.ScreenFlashTone.NEUTRAL_WHITE))
        assertTrue(tones.contains(com.example.camera.ScreenFlashTone.WARM_SOFT))
        assertTrue(tones.contains(com.example.camera.ScreenFlashTone.COOL_BRIGHT))
    }

    @Test
    fun qualityPresets_enum_containsAllPresets() {
        val presets = com.example.imageprocessing.QualityPreset.values()
        assertEquals(5, presets.size)
        assertTrue(presets.contains(com.example.imageprocessing.QualityPreset.NATURAL))
        assertTrue(presets.contains(com.example.imageprocessing.QualityPreset.DETAIL))
        assertTrue(presets.contains(com.example.imageprocessing.QualityPreset.LOW_LIGHT))
        assertTrue(presets.contains(com.example.imageprocessing.QualityPreset.HDR_STYLE))
        assertTrue(presets.contains(com.example.imageprocessing.QualityPreset.PORTRAIT_BOKEH))
    }

    @Test
    fun duplicateDetector_hammingDistance_isAccurate() {
        // Identical hashes
        val hashA = 0b10101010L
        val hashB = 0b10101010L
        assertEquals(0, DuplicateDetector.hammingDistance(hashA, hashB))

        // Differing by 2 bits
        val hashC = 0b10101001L
        assertEquals(2, DuplicateDetector.hammingDistance(hashA, hashC))

        // Completely opposite 8 bits
        val hashD = 0b01010101L
        assertEquals(8, DuplicateDetector.hammingDistance(hashA, hashD))
    }

    @Test
    fun sizeReductionPercentage_isCalculatedAccurately() {
        val original = 2000L
        val compressed = 1000L
        val reduction = ((original - compressed).toDouble() / original.toDouble()) * 100.0
        assertEquals(50.0, reduction, 0.001)

        val increased = 2500L
        val negativeReduction = ((original - increased).toDouble() / original.toDouble()) * 100.0
        assertEquals(-25.0, negativeReduction, 0.001)
    }

    @Test
    fun captureMode_enum_containsAllFifteenModes() {
        val modes = CaptureMode.values()
        assertEquals(15, modes.size)
        assertTrue(modes.contains(CaptureMode.PHOTO))
        assertTrue(modes.contains(CaptureMode.PORTRAIT))
        assertTrue(modes.contains(CaptureMode.NIGHT))
        assertTrue(modes.contains(CaptureMode.HDR))
        assertTrue(modes.contains(CaptureMode.VIDEO))
        assertTrue(modes.contains(CaptureMode.PRO))
        assertTrue(modes.contains(CaptureMode.HI_RES))
        assertTrue(modes.contains(CaptureMode.PANO))
        assertTrue(modes.contains(CaptureMode.MACRO))
        assertTrue(modes.contains(CaptureMode.SLO_MO))
        assertTrue(modes.contains(CaptureMode.TIME_LAPSE))
        assertTrue(modes.contains(CaptureMode.DUAL_VIDEO))
        assertTrue(modes.contains(CaptureMode.UNDERWATER))
        assertTrue(modes.contains(CaptureMode.STICKER))
        assertTrue(modes.contains(CaptureMode.DOC_SCANNER))
    }

    @Test
    fun cameraUiState_pinnedModes_defaultContainsPrimaryModes() {
        val state = CameraUiState()
        assertEquals(5, state.pinnedModes.size)
        assertTrue(state.pinnedModes.contains(CaptureMode.VIDEO))
        assertTrue(state.pinnedModes.contains(CaptureMode.PHOTO))
        assertTrue(state.pinnedModes.contains(CaptureMode.PORTRAIT))
        assertTrue(state.pinnedModes.contains(CaptureMode.NIGHT))
        assertTrue(state.pinnedModes.contains(CaptureMode.PRO))
        assertFalse(state.isRearrangeModesVisible)
    }

    @Test
    fun shootingModes_serializationAndParsing_worksAccurately() {
        val modes = listOf(CaptureMode.PHOTO, CaptureMode.NIGHT, CaptureMode.HI_RES, CaptureMode.PRO)
        val serialized = modes.joinToString(",") { it.name }
        assertEquals("PHOTO,NIGHT,HI_RES,PRO", serialized)

        val deserialized = serialized.split(",").mapNotNull {
            try { CaptureMode.valueOf(it.trim()) } catch (e: Exception) { null }
        }
        assertEquals(modes, deserialized)
    }
}
