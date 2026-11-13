package com.clintmaples.openlevel.level

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VectorLowPassTest {
    @Test
    fun alphaIsZeroWithoutTimeAndOneWithoutATimeConstant() {
        assertEquals(0f, lowPassAlpha(0f, 0.2f), 0f)
        assertEquals(0f, lowPassAlpha(-0.1f, 0.2f), 0f)
        assertEquals(1f, lowPassAlpha(0.2f, 0f), 0f)
        assertEquals(0.5f, lowPassAlpha(0.2f, 0.2f), 0.0001f)
    }

    @Test
    fun firstSamplePassesThrough() {
        val filter = VectorLowPass(timeConstantSeconds = 0.2f)
        val out = filter.filter(1.5f, -2f, 9.8f, 0L)
        assertEquals(1.5f, out[0], 0f)
        assertEquals(-2f, out[1], 0f)
        assertEquals(9.8f, out[2], 0f)
    }

    @Test
    fun oneTimeConstantMovesHalfwayTowardAStep() {
        val filter = VectorLowPass(timeConstantSeconds = 0.2f)
        filter.filter(0f, 0f, 0f, 0L)
        val out = filter.filter(10f, -4f, 2f, 200_000_000L)
        assertEquals(5f, out[0], 0.0001f)
        assertEquals(-2f, out[1], 0.0001f)
        assertEquals(1f, out[2], 0.0001f)
    }

    @Test
    fun constantInputStaysPut() {
        val filter = VectorLowPass(timeConstantSeconds = 0.2f)
        filter.filter(1f, 2f, 3f, 0L)
        val out = filter.filter(1f, 2f, 3f, 200_000_000L)
        assertEquals(1f, out[0], 0.0001f)
        assertEquals(2f, out[1], 0.0001f)
        assertEquals(3f, out[2], 0.0001f)
    }

    @Test
    fun shorterStepsMoveLessThanLongerSteps() {
        val small = VectorLowPass(timeConstantSeconds = 0.2f)
        small.filter(0f, 0f, 0f, 0L)
        val smallOut = small.filter(10f, 0f, 0f, 50_000_000L)

        val large = VectorLowPass(timeConstantSeconds = 0.2f)
        large.filter(0f, 0f, 0f, 0L)
        val largeOut = large.filter(10f, 0f, 0f, 400_000_000L)

        assertTrue(smallOut[0] > 0f)
        assertTrue(smallOut[0] < largeOut[0])
        assertTrue(largeOut[0] < 10f)
    }

    @Test
    fun repeatedTimestampUsesTheFallbackStep() {
        val filter = VectorLowPass(timeConstantSeconds = VectorLowPass.FALLBACK_DT_SECONDS)
        filter.filter(0f, 0f, 0f, 5_000L)
        val out = filter.filter(8f, 0f, 0f, 5_000L)
        assertEquals(4f, out[0], 0.0001f)
    }

    @Test
    fun backwardsTimestampDoesNotFreezeTheFilter() {
        val filter = VectorLowPass(timeConstantSeconds = VectorLowPass.FALLBACK_DT_SECONDS)
        filter.filter(0f, 0f, 0f, 1_000_000L)
        val out = filter.filter(8f, 0f, 0f, 100L)
        assertEquals(4f, out[0], 0.0001f)
    }

    @Test
    fun repeatedStepsConvergeWithoutOvershoot() {
        val filter = VectorLowPass(timeConstantSeconds = 0.1f)
        var time = 0L
        var out = floatArrayOf(0f, 0f, 0f)
        var previous = 0f
        repeat(80) {
            out = filter.filter(5f, -5f, 1f, time)
            assertTrue(out[0] >= previous)
            assertTrue(out[0] <= 5f)
            previous = out[0]
            time += 50_000_000L
        }
        assertEquals(5f, out[0], 0.02f)
        assertEquals(-5f, out[1], 0.02f)
        assertEquals(1f, out[2], 0.02f)
    }

    @Test
    fun resetTreatsTheNextSampleAsTheFirst() {
        val filter = VectorLowPass(timeConstantSeconds = 0.2f)
        filter.filter(0f, 0f, 0f, 0L)
        filter.filter(10f, 0f, 0f, 200_000_000L)
        filter.reset()
        val out = filter.filter(3f, 4f, 5f, 9_000L)
        assertEquals(3f, out[0], 0f)
        assertEquals(4f, out[1], 0f)
        assertEquals(5f, out[2], 0f)
    }
}
