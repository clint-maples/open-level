package com.clintmaples.openlevel.level

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class AngleMathTest {
    private val g = 9.80665f

    @Test
    fun flatFaceUpIsLevelOnBothAxes() {
        assertEquals(0f, AngleMath.rollDegrees(0f, 0f, g), 0.001f)
        assertEquals(0f, AngleMath.pitchDegrees(0f, 0f, g), 0.001f)
    }

    @Test
    fun flatFaceDownIsAlsoLevel() {
        assertEquals(0f, AngleMath.rollDegrees(0f, 0f, -g), 0.001f)
        assertEquals(0f, AngleMath.pitchDegrees(0f, 0f, -g), 0.001f)
    }

    @Test
    fun rollIsPositiveWhenTheRightEdgeIsHigher() {
        val tilt = Math.toRadians(10.0)
        val x = (g * sin(tilt)).toFloat()
        val z = (g * cos(tilt)).toFloat()
        assertEquals(10f, AngleMath.rollDegrees(x, 0f, z), 0.05f)
        assertEquals(0f, AngleMath.pitchDegrees(x, 0f, z), 0.05f)
    }

    @Test
    fun pitchIsPositiveWhenTheTopEdgeIsHigher() {
        val tilt = Math.toRadians(10.0)
        val y = (g * sin(tilt)).toFloat()
        val z = (g * cos(tilt)).toFloat()
        assertEquals(10f, AngleMath.pitchDegrees(0f, y, z), 0.05f)
        assertEquals(0f, AngleMath.rollDegrees(0f, y, z), 0.05f)
    }

    @Test
    fun negativeRollMeansTheLeftEdgeIsHigher() {
        val tilt = Math.toRadians(8.0)
        val x = (-g * sin(tilt)).toFloat()
        val z = (g * cos(tilt)).toFloat()
        assertEquals(-8f, AngleMath.rollDegrees(x, 0f, z), 0.05f)
    }

    @Test
    fun rotationFromVerticalMatchesClockwiseLean() {
        assertEquals(0f, AngleMath.rotationFromVerticalDegrees(0f, g), 0.001f)
        assertEquals(90f, AngleMath.rotationFromVerticalDegrees(g, 0f), 0.001f)
        assertEquals(-90f, AngleMath.rotationFromVerticalDegrees(-g, 0f), 0.001f)
        assertEquals(180f, AngleMath.rotationFromVerticalDegrees(0f, -g), 0.001f)
        val tilt = Math.toRadians(30.0)
        val x = (g * sin(tilt)).toFloat()
        val y = (g * cos(tilt)).toFloat()
        assertEquals(30f, AngleMath.rotationFromVerticalDegrees(x, y), 0.05f)
    }

    @Test
    fun deviationSnapsToTheNearestCardinal() {
        assertEquals(0f, AngleMath.deviationFromNearestCardinal(0f), 0.001f)
        assertEquals(2f, AngleMath.deviationFromNearestCardinal(2f), 0.001f)
        assertEquals(-2f, AngleMath.deviationFromNearestCardinal(-2f), 0.001f)
        assertEquals(0f, AngleMath.deviationFromNearestCardinal(90f), 0.001f)
        assertEquals(-2f, AngleMath.deviationFromNearestCardinal(88f), 0.001f)
        assertEquals(2f, AngleMath.deviationFromNearestCardinal(92f), 0.001f)
        assertEquals(0f, AngleMath.deviationFromNearestCardinal(-90f), 0.001f)
        assertEquals(0f, AngleMath.deviationFromNearestCardinal(180f), 0.001f)
        assertEquals(-2f, AngleMath.deviationFromNearestCardinal(178f), 0.001f)
        assertEquals(30f, AngleMath.deviationFromNearestCardinal(30f), 0.001f)
        assertEquals(-30f, AngleMath.deviationFromNearestCardinal(60f), 0.001f)
    }

    @Test
    fun verticalReferenceWinsTiesAtFortyFiveDegrees() {
        assertTrue(AngleMath.isCloserToVertical(0f))
        assertTrue(AngleMath.isCloserToVertical(180f))
        assertTrue(AngleMath.isCloserToVertical(45f))
        assertFalse(AngleMath.isCloserToVertical(46f))
        assertFalse(AngleMath.isCloserToVertical(90f))
        assertFalse(AngleMath.isCloserToVertical(-90f))
    }

    @Test
    fun edgePoseRejectsAPhoneThatIsNearlyFlat() {
        assertFalse(AngleMath.edgePoseValid(0f, 0f, g))
        assertFalse(AngleMath.edgePoseValid(0f, 0f, -g))
        assertTrue(AngleMath.edgePoseValid(0f, g, 0f))
        assertTrue(AngleMath.edgePoseValid(g, 0f, 0f))
        val shallow = Math.toRadians(40.0)
        assertFalse(
            AngleMath.edgePoseValid(
                0f,
                (g * sin(shallow)).toFloat(),
                (g * cos(shallow)).toFloat(),
            ),
        )
        val steep = Math.toRadians(50.0)
        assertTrue(
            AngleMath.edgePoseValid(
                0f,
                (g * sin(steep)).toFloat(),
                (g * cos(steep)).toFloat(),
            ),
        )
        assertFalse(AngleMath.edgePoseValid(0f, 0f, 0f))
    }

    @Test
    fun percentSlopeIsZeroAtLevelAndOneHundredAtFortyFive() {
        assertEquals(0f, AngleMath.percentSlope(0f), 0.001f)
        assertEquals(100f, AngleMath.percentSlope(45f), 0.05f)
        assertEquals(-100f, AngleMath.percentSlope(-45f), 0.05f)
        assertTrue(AngleMath.percentSlope(1f) > 1.7f)
        assertTrue(AngleMath.percentSlope(1f) < 1.8f)
    }

    @Test
    fun normalizeWrapsIntoSignedHalfTurn() {
        assertEquals(0f, AngleMath.normalizeSignedDegrees(0f), 0.001f)
        assertEquals(0f, AngleMath.normalizeSignedDegrees(360f), 0.001f)
        assertEquals(180f, AngleMath.normalizeSignedDegrees(180f), 0.001f)
        assertEquals(180f, AngleMath.normalizeSignedDegrees(-180f), 0.001f)
        assertEquals(-179f, AngleMath.normalizeSignedDegrees(181f), 0.001f)
        assertEquals(179f, AngleMath.normalizeSignedDegrees(-181f), 0.001f)
        assertEquals(180f, AngleMath.normalizeSignedDegrees(540f), 0.001f)
        assertEquals(90f, AngleMath.normalizeSignedDegrees(-270f), 0.001f)
        assertEquals(20f, AngleMath.normalizeSignedDegrees(-340f), 0.001f)
        assertEquals(0f, AngleMath.normalizeSignedDegrees(Float.NaN), 0f)
        assertEquals(0f, AngleMath.normalizeSignedDegrees(Float.POSITIVE_INFINITY), 0f)
    }

    @Test
    fun formatsOneDecimalWithAnExplicitSign() {
        assertEquals("+0.0°", AngleMath.formatDegrees(0f))
        assertEquals("+0.0°", AngleMath.formatDegrees(0.04f))
        assertEquals("+0.1°", AngleMath.formatDegrees(0.05f))
        assertEquals("-0.1°", AngleMath.formatDegrees(-0.05f))
        assertEquals("+1.3°", AngleMath.formatDegrees(1.25f))
        assertEquals("+45.0°", AngleMath.formatDegrees(45f))
        assertEquals("+100.0%", AngleMath.formatPercent(45f))
        assertEquals("-100.0%", AngleMath.formatPercent(-45f))
        assertEquals("+0.0%", AngleMath.formatPercent(0f))
        assertEquals(AngleMath.UNAVAILABLE, AngleMath.formatPercent(89f))
        assertEquals(AngleMath.UNAVAILABLE, AngleMath.formatDegrees(Float.NaN))
        assertEquals("+1.2°", AngleMath.formatMeasurement(1.24f, usePercent = false))
        assertEquals("+1.7%", AngleMath.formatMeasurement(1f, usePercent = true))
    }
}
