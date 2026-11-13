package com.clintmaples.openlevel.level

data class LevelSnapshot(
    val rawX: Float,
    val rawY: Float,
    val rawEdgeDeviation: Float,
    val edgePoseValid: Boolean,
    val xDegrees: Float,
    val yDegrees: Float,
    val edgeDeviationDegrees: Float,
    val edgeReferenceVertical: Boolean,
    val xLevel: Boolean,
    val yLevel: Boolean,
    val surfaceLevel: Boolean,
    val edgeLevel: Boolean,
)

object LevelMath {
    fun snapshot(
        x: Float,
        y: Float,
        z: Float,
        calibration: CalibrationOffsets,
        tolerance: Float = LevelTolerance.DEGREES,
    ): LevelSnapshot {
        val rawX = AngleMath.rollDegrees(x, y, z)
        val rawY = AngleMath.pitchDegrees(x, y, z)
        val shown = calibration.applySurface(rawX, rawY)
        val xLevel = LevelTolerance.isWithin(shown.first, tolerance)
        val yLevel = LevelTolerance.isWithin(shown.second, tolerance)
        val poseValid = AngleMath.edgePoseValid(x, y, z)
        val rotation = AngleMath.rotationFromVerticalDegrees(x, y)
        val rawEdge = if (poseValid) AngleMath.deviationFromNearestCardinal(rotation) else 0f
        val shownEdge = if (poseValid) calibration.applyEdge(rawEdge) else 0f
        return LevelSnapshot(
            rawX = rawX,
            rawY = rawY,
            rawEdgeDeviation = rawEdge,
            edgePoseValid = poseValid,
            xDegrees = shown.first,
            yDegrees = shown.second,
            edgeDeviationDegrees = shownEdge,
            edgeReferenceVertical = if (poseValid) AngleMath.isCloserToVertical(rotation) else true,
            xLevel = xLevel,
            yLevel = yLevel,
            surfaceLevel = xLevel && yLevel,
            edgeLevel = poseValid && LevelTolerance.isWithin(shownEdge, tolerance),
        )
    }
}
