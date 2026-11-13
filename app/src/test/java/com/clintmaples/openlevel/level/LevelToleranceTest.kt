package com.clintmaples.openlevel.level

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelToleranceTest {
    @Test
    fun zeroIsLevel() {
        assertTrue(LevelTolerance.isWithin(0f))
        assertTrue(LevelTolerance.isSurfaceLevel(0f, 0f))
        assertTrue(LevelTolerance.isEdgeLevel(0f))
    }

    @Test
    fun boundaryIsInsideTheTolerance() {
        assertTrue(LevelTolerance.isWithin(LevelTolerance.DEGREES))
        assertTrue(LevelTolerance.isWithin(-LevelTolerance.DEGREES))
        assertTrue(LevelTolerance.isSurfaceLevel(0.5f, -0.5f))
        assertTrue(LevelTolerance.isEdgeLevel(-0.5f))
    }

    @Test
    fun justOutsideTheToleranceIsNotLevel() {
        assertFalse(LevelTolerance.isWithin(0.51f))
        assertFalse(LevelTolerance.isWithin(-0.51f))
        assertFalse(LevelTolerance.isEdgeLevel(0.51f))
    }

    @Test
    fun surfaceLevelRequiresBothAxes() {
        assertFalse(LevelTolerance.isSurfaceLevel(0.2f, 0.8f))
        assertFalse(LevelTolerance.isSurfaceLevel(0.8f, 0.2f))
        assertTrue(LevelTolerance.isSurfaceLevel(0.4f, -0.4f))
    }

    @Test
    fun nonFiniteReadingsAreNotLevel() {
        assertFalse(LevelTolerance.isWithin(Float.NaN))
        assertFalse(LevelTolerance.isWithin(Float.POSITIVE_INFINITY))
        assertFalse(LevelTolerance.isSurfaceLevel(0f, Float.NaN))
        assertFalse(LevelTolerance.isEdgeLevel(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun customToleranceIsHonored() {
        assertTrue(LevelTolerance.isWithin(1.5f, tolerance = 2f))
        assertFalse(LevelTolerance.isWithin(1.5f, tolerance = 1f))
        assertTrue(LevelTolerance.isSurfaceLevel(1.2f, -1.2f, tolerance = 1.2f))
        assertFalse(LevelTolerance.isEdgeLevel(1f, tolerance = 0.2f))
    }
}
