package com.clintmaples.openlevel.level

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class LevelMathTest {
    private val g = 9.80665f

    @Test
    fun flatPhoneWithNoCalibrationIsSurfaceLevel() {
        val snapshot = LevelMath.snapshot(0f, 0f, g, CalibrationOffsets())
        assertEquals(0f, snapshot.xDegrees, 0.001f)
        assertEquals(0f, snapshot.yDegrees, 0.001f)
        assertTrue(snapshot.surfaceLevel)
        assertTrue(snapshot.xLevel)
        assertTrue(snapshot.yLevel)
        assertFalse(snapshot.edgePoseValid)
        assertFalse(snapshot.edgeLevel)
    }

    @Test
    fun calibrationRemovesAKnownSurfaceTilt() {
        val tilt = Math.toRadians(2.0)
        val x = (g * sin(tilt)).toFloat()
        val z = (g * cos(tilt)).toFloat()
        val raw = LevelMath.snapshot(x, 0f, z, CalibrationOffsets())
        assertFalse(raw.surfaceLevel)
        assertEquals(2f, raw.rawX, 0.05f)

        val calibrated = LevelMath.snapshot(x, 0f, z, CalibrationOffsets(xDegrees = raw.rawX))
        assertEquals(0f, calibrated.xDegrees, 0.001f)
        assertTrue(calibrated.xLevel)
        assertTrue(calibrated.surfaceLevel)
    }

    @Test
    fun uprightPhoneIsEdgeLevelFromVertical() {
        val snapshot = LevelMath.snapshot(0f, g, 0f, CalibrationOffsets())
        assertTrue(snapshot.edgePoseValid)
        assertTrue(snapshot.edgeReferenceVertical)
        assertEquals(0f, snapshot.edgeDeviationDegrees, 0.05f)
        assertTrue(snapshot.edgeLevel)
        assertFalse(snapshot.surfaceLevel)
    }

    @Test
    fun phoneOnItsLeftEdgeIsLevelFromHorizontal() {
        val snapshot = LevelMath.snapshot(g, 0f, 0f, CalibrationOffsets())
        assertTrue(snapshot.edgePoseValid)
        assertFalse(snapshot.edgeReferenceVertical)
        assertEquals(0f, snapshot.edgeDeviationDegrees, 0.05f)
        assertTrue(snapshot.edgeLevel)
    }

    @Test
    fun edgeCalibrationZeroesTheCurrentDeviation() {
        val tilt = Math.toRadians(3.0)
        val x = (g * sin(tilt)).toFloat()
        val y = (g * cos(tilt)).toFloat()
        val raw = LevelMath.snapshot(x, y, 0f, CalibrationOffsets())
        assertEquals(3f, raw.rawEdgeDeviation, 0.05f)
        assertFalse(raw.edgeLevel)

        val calibrated = LevelMath.snapshot(
            x,
            y,
            0f,
            CalibrationOffsets(edgeDegrees = raw.rawEdgeDeviation),
        )
        assertEquals(0f, calibrated.edgeDeviationDegrees, 0.001f)
        assertTrue(calibrated.edgeLevel)
        assertEquals(raw.xDegrees, calibrated.xDegrees, 0.001f)
    }
}
