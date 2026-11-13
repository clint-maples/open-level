package com.clintmaples.openlevel.level

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.tan

/**
 * Tilt math for a phone level.
 *
 * Device axes follow the Android sensor frame: X to the right, Y toward the
 * top of the screen, Z out of the screen. At rest the accelerometer and the
 * gravity sensor both read about +9.8 along the upward axis, so a phone lying
 * face-up is approximately (0, 0, +g).
 *
 * Roll is positive when the right edge is higher. Pitch is positive when the
 * top edge is higher. Both are 0 when the phone is flat, face up or face down.
 * Edge rotation is clockwise degrees from upright (bottom edge down): 0 is
 * plumb, +90 is resting on the left edge.
 */
object AngleMath {
    const val UNAVAILABLE = "—"

    /** Screen is upright enough for the edge reading when |z| / |g| is below this. */
    const val EDGE_FLATNESS_LIMIT = 0.75f

    fun rollDegrees(x: Float, y: Float, z: Float): Float {
        return Math.toDegrees(atan2(x.toDouble(), hypot(y.toDouble(), z.toDouble()))).toFloat()
    }

    fun pitchDegrees(x: Float, y: Float, z: Float): Float {
        return Math.toDegrees(atan2(y.toDouble(), hypot(x.toDouble(), z.toDouble()))).toFloat()
    }

    fun rotationFromVerticalDegrees(x: Float, y: Float): Float {
        return normalizeSignedDegrees(Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat())
    }

    fun edgePoseValid(x: Float, y: Float, z: Float): Boolean {
        val magnitude = hypot(hypot(x.toDouble(), y.toDouble()), z.toDouble())
        if (magnitude < 1.0) return false
        return abs(z.toDouble()) / magnitude < EDGE_FLATNESS_LIMIT
    }

    /** Signed degrees from the nearest of 0, ±90, and 180. Always within ±45. */
    fun deviationFromNearestCardinal(rotationDegrees: Float): Float {
        val rotation = normalizeSignedDegrees(rotationDegrees)
        val cardinals = floatArrayOf(0f, 90f, -90f, 180f)
        var best = normalizeSignedDegrees(rotation - cardinals[0])
        var bestAbs = abs(best)
        for (index in 1 until cardinals.size) {
            val delta = normalizeSignedDegrees(rotation - cardinals[index])
            val distance = abs(delta)
            if (distance < bestAbs) {
                best = delta
                bestAbs = distance
            }
        }
        return best
    }

    fun isCloserToVertical(rotationDegrees: Float): Boolean {
        val rotation = abs(normalizeSignedDegrees(rotationDegrees))
        val fromVertical = minOf(rotation, abs(rotation - 180f))
        val fromHorizontal = abs(rotation - 90f)
        return fromVertical <= fromHorizontal
    }

    fun percentSlope(degrees: Float): Float {
        if (!degrees.isFinite()) return Float.NaN
        return (tan(Math.toRadians(degrees.toDouble())) * 100.0).toFloat()
    }

    fun normalizeSignedDegrees(degrees: Float): Float {
        if (!degrees.isFinite()) return 0f
        var wrapped = degrees % 360f
        if (wrapped > 180f) wrapped -= 360f
        if (wrapped <= -180f) wrapped += 360f
        return wrapped
    }

    fun formatDegrees(degrees: Float): String = formatSignedOneDecimal(degrees, "°")

    fun formatPercent(degrees: Float): String {
        if (!degrees.isFinite()) return UNAVAILABLE
        val slope = percentSlope(degrees)
        if (!slope.isFinite() || abs(slope) > 999.9f) return UNAVAILABLE
        return formatSignedOneDecimal(slope, "%")
    }

    fun formatMeasurement(degrees: Float, usePercent: Boolean): String {
        return if (usePercent) formatPercent(degrees) else formatDegrees(degrees)
    }

    fun roundedTenths(degrees: Float): Float {
        if (!degrees.isFinite()) return Float.NaN
        return BigDecimal.valueOf(degrees.toDouble()).setScale(1, RoundingMode.HALF_UP).toFloat()
    }

    fun formatSignedOneDecimal(value: Float, suffix: String): String {
        if (!value.isFinite()) return UNAVAILABLE
        val rounded = BigDecimal.valueOf(value.toDouble()).setScale(1, RoundingMode.HALF_UP)
        val digits = rounded.abs().toPlainString()
        val sign = if (rounded.signum() < 0) "-" else "+"
        return sign + digits + suffix
    }
}
