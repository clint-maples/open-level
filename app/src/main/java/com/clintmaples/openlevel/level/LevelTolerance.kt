package com.clintmaples.openlevel.level

import kotlin.math.abs

object LevelTolerance {
    const val DEGREES = 0.5f

    fun isWithin(degrees: Float, tolerance: Float = DEGREES): Boolean {
        return degrees.isFinite() && abs(degrees) <= tolerance
    }

    /** True when both axes are inside the tolerance, matching the two tube vials. */
    fun isSurfaceLevel(xDegrees: Float, yDegrees: Float, tolerance: Float = DEGREES): Boolean {
        return isWithin(xDegrees, tolerance) && isWithin(yDegrees, tolerance)
    }

    fun isEdgeLevel(deviationDegrees: Float, tolerance: Float = DEGREES): Boolean {
        return isWithin(deviationDegrees, tolerance)
    }
}
