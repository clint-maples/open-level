package com.clintmaples.openlevel.level

/**
 * Zero offsets captured when the user calibrates.
 *
 * Surface and edge are independent: calibrating the bubble level does not
 * move the edge zero, and the reverse. [reset] clears every offset.
 */
data class CalibrationOffsets(
    val xDegrees: Float = 0f,
    val yDegrees: Float = 0f,
    val edgeDegrees: Float = 0f,
) {
    val surfaceCalibrated: Boolean get() = xDegrees != 0f || yDegrees != 0f
    val edgeCalibrated: Boolean get() = edgeDegrees != 0f

    fun withSurface(xDegrees: Float, yDegrees: Float): CalibrationOffsets {
        return copy(xDegrees = xDegrees, yDegrees = yDegrees)
    }

    fun withEdge(edgeDegrees: Float): CalibrationOffsets = copy(edgeDegrees = edgeDegrees)

    fun reset(): CalibrationOffsets = CalibrationOffsets()

    fun applySurface(rawX: Float, rawY: Float): Pair<Float, Float> {
        return AngleMath.normalizeSignedDegrees(rawX - xDegrees) to
            AngleMath.normalizeSignedDegrees(rawY - yDegrees)
    }

    fun applyEdge(rawDeviation: Float): Float {
        return AngleMath.normalizeSignedDegrees(rawDeviation - edgeDegrees)
    }
}
