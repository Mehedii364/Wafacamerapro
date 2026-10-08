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
}
