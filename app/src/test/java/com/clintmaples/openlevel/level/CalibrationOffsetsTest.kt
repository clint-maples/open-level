package com.clintmaples.openlevel.level

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalibrationOffsetsTest {
    @Test
    fun zeroOffsetsLeaveReadingsUnchanged() {
        val calibration = CalibrationOffsets()
        val (x, y) = calibration.applySurface(1.5f, -2.25f)
        assertEquals(1.5f, x, 0.0001f)
        assertEquals(-2.25f, y, 0.0001f)
        assertEquals(4f, calibration.applyEdge(4f), 0.0001f)
        assertFalse(calibration.surfaceCalibrated)
        assertFalse(calibration.edgeCalibrated)
    }

    @Test
    fun surfaceCalibrationZeroesTheCurrentReading() {
        val calibration = CalibrationOffsets().withSurface(1.25f, -0.5f)
        val (x, y) = calibration.applySurface(1.25f, -0.5f)
        assertEquals(0f, x, 0.0001f)
        assertEquals(0f, y, 0.0001f)
        assertTrue(calibration.surfaceCalibrated)
    }

    @Test
    fun surfaceCalibrationDoesNotChangeTheEdgeZero() {
        val calibration = CalibrationOffsets(edgeDegrees = 3f).withSurface(1f, 2f)
        assertEquals(3f, calibration.edgeDegrees, 0f)
        assertEquals(1f, calibration.xDegrees, 0f)
        assertEquals(2f, calibration.yDegrees, 0f)
        assertEquals(0f, calibration.applyEdge(3f), 0.0001f)
        assertTrue(calibration.edgeCalibrated)
    }

    @Test
    fun edgeCalibrationDoesNotChangeTheSurfaceZero() {
        val calibration = CalibrationOffsets(xDegrees = 1f, yDegrees = -2f).withEdge(4f)
        assertEquals(1f, calibration.xDegrees, 0f)
        assertEquals(-2f, calibration.yDegrees, 0f)
        assertEquals(4f, calibration.edgeDegrees, 0f)
        val (x, y) = calibration.applySurface(1f, -2f)
        assertEquals(0f, x, 0.0001f)
        assertEquals(0f, y, 0.0001f)
        assertEquals(0f, calibration.applyEdge(4f), 0.0001f)
    }

    @Test
    fun offsetsAreSubtractedFromLaterSamples() {
        val calibration = CalibrationOffsets(xDegrees = 0.4f, yDegrees = -0.2f, edgeDegrees = 1.5f)
        val (x, y) = calibration.applySurface(1.0f, 0.3f)
        assertEquals(0.6f, x, 0.0001f)
        assertEquals(0.5f, y, 0.0001f)
        assertEquals(-0.5f, calibration.applyEdge(1.0f), 0.0001f)
    }

    @Test
    fun subtractionWrapsAcrossTheBranchCut() {
        val calibration = CalibrationOffsets(xDegrees = 170f, yDegrees = 0f, edgeDegrees = -170f)
        assertEquals(20f, calibration.applySurface(rawX = -170f, rawY = 10f).first, 0.0001f)
        assertEquals(10f, calibration.applySurface(rawX = -170f, rawY = 10f).second, 0.0001f)
        assertEquals(20f, calibration.applyEdge(-150f), 0.0001f)
    }

    @Test
    fun resetClearsEveryOffset() {
        val cleared = CalibrationOffsets(1f, -2f, 3f).reset()
        assertEquals(CalibrationOffsets(), cleared)
        assertFalse(cleared.surfaceCalibrated)
        assertFalse(cleared.edgeCalibrated)
    }
}
